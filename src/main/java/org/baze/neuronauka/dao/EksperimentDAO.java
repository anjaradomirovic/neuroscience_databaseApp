package org.baze.neuronauka.dao;

import org.baze.neuronauka.db.DBConnection;
import org.baze.neuronauka.model.EksperimentDTO;
import org.baze.neuronauka.model.EksperimentDTO.EksperimentResursStavka;
import org.baze.neuronauka.model.EksperimentDTO.EksperimentAlatStavka;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EksperimentDAO {

    // ══════════════════════════════════════════════════════════════
    //  EKSPERIMENT — CRUD
    // ══════════════════════════════════════════════════════════════

    public List<EksperimentDTO> getSviEksperimenti() {
        List<EksperimentDTO> lista = new ArrayList<>();
        String sql = """
        SELECT
            e.id_eksperiment, e.naziv, e.opis,
            e.tip_eksperimenta, e.metod_izvodjenja, e.eticki_odobren,
            GROUP_CONCAT(DISTINCT CONCAT(i.ime, ' ', i.prezime) SEPARATOR ', ') AS dizajneri,
            GROUP_CONCAT(DISTINCT to2.naziv SEPARATOR ', ')                     AS teorijski_okviri
        FROM eksperiment e
        LEFT JOIN eksperiment_dizajner ed ON ed.id_eksperiment  = e.id_eksperiment
        LEFT JOIN dizajner d              ON d.id_istrazivaca   = ed.id_dizajner
        LEFT JOIN istrazivac i            ON i.id_istrazivaca   = d.id_istrazivaca
        LEFT JOIN teorijski_okvir to2     ON to2.id_teorijski_okvir = ed.id_teorijski_okvir
        GROUP BY e.id_eksperiment, e.naziv, e.opis,
                 e.tip_eksperimenta, e.metod_izvodjenja, e.eticki_odobren
        ORDER BY e.naziv
        """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                EksperimentDTO dto = mapRow(rs);
                dto.setDizajneriStr(rs.getString("dizajneri"));
                dto.setTeorijskiOkvirStr(rs.getString("teorijski_okviri"));
                lista.add(dto);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatanju eksperimenata", e);
        }
        return lista;
    }

    public EksperimentDTO getEksperimentDetalji(int idEksperiment) {
        String sql = """
                SELECT id_eksperiment, naziv, opis,
                       tip_eksperimenta, metod_izvodjenja, eticki_odobren
                FROM eksperiment
                WHERE id_eksperiment = ?
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEksperiment);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                EksperimentDTO dto = mapRow(rs);
                dto.setResurei(getResursiEksperimenta(con, idEksperiment));
                dto.setAlati(getAlatiEksperimenta(con, idEksperiment));
                dto.setDizajneri(getDizajneriEksperimenta(con, idEksperiment));
                return dto;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatanju detalja eksperimenta", e);
        }
    }

    public int dodajEksperiment(EksperimentDTO dto) throws SQLException {
        String sql = """
                INSERT INTO eksperiment
                    (naziv, opis, tip_eksperimenta, metod_izvodjenja, eticki_odobren)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int noviId;
                try (PreparedStatement ps = con.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, dto.getNaziv());
                    ps.setString(2, dto.getOpis());
                    ps.setString(3, dto.getTipEksperimenta());
                    ps.setString(4, dto.getMetodIzvodjenja());
                    ps.setBoolean(5, dto.isEtickyOdobren());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        noviId = keys.getInt(1);
                    }
                }
                umetniResurse(con, noviId, dto.getResurei());
                umetniAlate(con, noviId, dto.getAlati());
                umetniDizajnere(con, noviId, dto.getDizajneri());
                con.commit();
                return noviId;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public void izmeniEksperiment(EksperimentDTO dto) throws SQLException {
        String sql = """
            UPDATE eksperiment
            SET naziv            = ?,
                opis             = ?,
                tip_eksperimenta = ?,
                metod_izvodjenja = ?,
                eticki_odobren   = ?
            WHERE id_eksperiment = ?
            """;
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, dto.getNaziv());
                    ps.setString(2, dto.getOpis());
                    ps.setString(3, dto.getTipEksperimenta());
                    ps.setString(4, dto.getMetodIzvodjenja());
                    ps.setBoolean(5, dto.isEtickyOdobren());
                    ps.setInt(6, dto.getIdEksperiment());
                    ps.executeUpdate();
                }
                // Briši samo resurse i alate — NE dizajnere!
                obrisiStavkeBezDizajnera(con, dto.getIdEksperiment());
                umetniResurse(con, dto.getIdEksperiment(), dto.getResurei());
                umetniAlate(con, dto.getIdEksperiment(), dto.getAlati());
                // Dizajneri se upravljaju posebno kroz dodajDizajnera/ukloniDizajnera
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }
    private void obrisiStavkeBezDizajnera(Connection con, int idEksperiment) throws SQLException {
        for (String tbl : new String[]{"resurs_eksperiment", "alat_eksperiment"}) {
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM " + tbl + " WHERE id_eksperiment = ?")) {
                ps.setInt(1, idEksperiment);
                ps.executeUpdate();
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  KATALOZI — za ComboBox u formi
    // ══════════════════════════════════════════════════════════════

    /** Resursi — nema jedinica u tabeli, koristimo svojstva kao opis. */
    public List<EksperimentResursStavka> getSviResurei() {
        List<EksperimentResursStavka> lista = new ArrayList<>();
        // resurs nema 'jedinica' — koristimo svojstva umesto nje
        String sql = "SELECT id_resurs, naziv, svojstva FROM resurs ORDER BY naziv";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new EksperimentResursStavka(
                        rs.getInt("id_resurs"),
                        rs.getString("naziv"),
                        rs.getString("svojstva"),   // mapuje se u 'jedinica' polje DTO-a
                        0));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatanju resursa", e);
        }
        return lista;
    }

    public List<EksperimentAlatStavka> getSviAlati() {
        List<EksperimentAlatStavka> lista = new ArrayList<>();
        String sql = "SELECT id_alat, naziv FROM alat ORDER BY naziv";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new EksperimentAlatStavka(
                        rs.getInt("id_alat"),
                        rs.getString("naziv"),
                        0));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatanju alata", e);
        }
        return lista;
    }

    /** Svi istraživači koji mogu biti dizajneri (iz tabele dizajner). */
    public List<Integer> getSviDizajneri() {
        List<Integer> lista = new ArrayList<>();
        String sql = "SELECT id_istrazivaca FROM dizajner ORDER BY id_istrazivaca";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(rs.getInt("id_istrazivaca"));
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatanju dizajnera", e);
        }
        return lista;
    }

    /** Svi teorijski okviri (id + naziv). */
    public List<int[]> getSviTeoriOkviri() {
        List<int[]> lista = new ArrayList<>();
        String sql = "SELECT id_teorijski_okvir, naziv FROM teorijski_okvir ORDER BY naziv";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                lista.add(new int[]{rs.getInt("id_teorijski_okvir")});
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatanju teorijskih okvira", e);
        }
        return lista;
    }

    // ══════════════════════════════════════════════════════════════
    //  PRIVATNE POMOĆNE METODE
    // ══════════════════════════════════════════════════════════════

    private EksperimentDTO mapRow(ResultSet rs) throws SQLException {
        return new EksperimentDTO(
                rs.getInt("id_eksperiment"),
                rs.getString("naziv"),
                rs.getString("opis"),
                rs.getString("tip_eksperimenta"),
                rs.getString("metod_izvodjenja"),
                rs.getBoolean("eticki_odobren")
        );
    }

    private List<EksperimentResursStavka> getResursiEksperimenta(
            Connection con, int idEksperiment) throws SQLException {
        List<EksperimentResursStavka> lista = new ArrayList<>();
        // resurs nema 'jedinica' — uzimamo svojstva
        String sql = """
                SELECT re.id_resurs, r.naziv, r.svojstva AS jedinica, re.kolicina
                FROM resurs_eksperiment re
                JOIN resurs r ON r.id_resurs = re.id_resurs
                WHERE re.id_eksperiment = ?
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEksperiment);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new EksperimentResursStavka(
                            rs.getInt("id_resurs"),
                            rs.getString("naziv"),
                            rs.getString("jedinica"),
                            rs.getDouble("kolicina")));
                }
            }
        }
        return lista;
    }

    private List<EksperimentAlatStavka> getAlatiEksperimenta(
            Connection con, int idEksperiment) throws SQLException {
        List<EksperimentAlatStavka> lista = new ArrayList<>();
        String sql = """
                SELECT ae.id_alat, a.naziv, ae.kolicina
                FROM alat_eksperiment ae
                JOIN alat a ON a.id_alat = ae.id_alat
                WHERE ae.id_eksperiment = ?
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEksperiment);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new EksperimentAlatStavka(
                            rs.getInt("id_alat"),
                            rs.getString("naziv"),
                            // kolicina je decimal u bazi, čitamo kao double pa kastujemo
                            (int) rs.getDouble("kolicina")));
                }
            }
        }
        return lista;
    }

    private List<int[]> getDizajneriEksperimenta(
            Connection con, int idEksperiment) throws SQLException {
        List<int[]> lista = new ArrayList<>();
        String sql = """
            SELECT id_dizajner, id_teorijski_okvir, glavni
            FROM eksperiment_dizajner
            WHERE id_eksperiment = ?
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEksperiment);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new int[]{
                            rs.getInt("id_dizajner"),
                            rs.getInt("id_teorijski_okvir"), // 0 ako NULL
                            rs.getInt("glavni")
                    });
                }
            }
        }
        return lista;
    }

    private void umetniResurse(Connection con, int idEksperiment,
                               List<EksperimentResursStavka> stavke) throws SQLException {
        if (stavke == null || stavke.isEmpty()) return;
        String sql = "INSERT INTO resurs_eksperiment (id_eksperiment, id_resurs, kolicina) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (EksperimentResursStavka s : stavke) {
                ps.setInt(1, idEksperiment);
                ps.setInt(2, s.getIdResurs());
                ps.setDouble(3, s.getKolicina());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void umetniAlate(Connection con, int idEksperiment,
                             List<EksperimentAlatStavka> stavke) throws SQLException {
        if (stavke == null || stavke.isEmpty()) return;
        String sql = "INSERT INTO alat_eksperiment (id_eksperiment, id_alat, kolicina) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (EksperimentAlatStavka s : stavke) {
                ps.setInt(1, idEksperiment);
                ps.setInt(2, s.getIdAlat());
                ps.setDouble(3, s.getKolicina());  // decimal(10,2) — šaljemo double
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void umetniDizajnere(Connection con, int idEksperiment,
                                 List<int[]> stavke) throws SQLException {
        if (stavke == null || stavke.isEmpty()) return;
        // int[] sada: [0]=id_dizajner, [1]=id_teorijski_okvir, [2]=glavni
        String sql = """
        INSERT INTO eksperiment_dizajner 
            (id_eksperiment, id_dizajner, id_teorijski_okvir, glavni)
        VALUES (?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE
            id_teorijski_okvir = VALUES(id_teorijski_okvir),
            glavni = VALUES(glavni)
        """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (int[] s : stavke) {
                ps.setInt(1, idEksperiment);
                ps.setInt(2, s[0]);
                if (s[1] == 0) ps.setNull(3, Types.INTEGER);
                else           ps.setInt(3, s[1]);
                ps.setInt(4, s.length > 2 ? s[2] : 0); // glavni
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
    private void obrisiStavke(Connection con, int idEksperiment) throws SQLException {
        for (String tbl : new String[]{
                "resurs_eksperiment", "alat_eksperiment", "eksperiment_dizajner"}) {
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM " + tbl + " WHERE id_eksperiment = ?")) {
                ps.setInt(1, idEksperiment);
                ps.executeUpdate();
            }
        }
    }
    // Dohvati sve teorijske okvire za dropdown
    public List<String[]> getSveTeorijeZaDropdown() {
        List<String[]> lista = new ArrayList<>();
        String sql = "SELECT id_teorijski_okvir, naziv FROM teorijski_okvir ORDER BY naziv";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                lista.add(new String[]{
                        rs.getString("id_teorijski_okvir"),
                        rs.getString("naziv")
                });
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    // Postavi teorijski okvir za sve dizajnere eksperimenta
    public void postaviTeorijskiOkvir(int idEksperiment, int idOkvir) throws SQLException {
        String sql = """
        UPDATE eksperiment_dizajner
        SET id_teorijski_okvir = ?
        WHERE id_eksperiment = ?
        """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOkvir);
            ps.setInt(2, idEksperiment);
            ps.executeUpdate();
        }
    }

    // Dodaj dizajnera na eksperiment
    public void dodajDizajnera(int idEksperiment, int idDizajner) throws SQLException {
        String sql = """
        INSERT INTO eksperiment_dizajner 
            (id_eksperiment, id_dizajner, glavni, id_teorijski_okvir)
        VALUES (?, ?, 0, NULL)
        ON DUPLICATE KEY UPDATE glavni = glavni  -- ne menjaj ako već postoji
        """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEksperiment);
            ps.setInt(2, idDizajner);
            ps.executeUpdate();
        }
    }

    // Ukloni dizajnera sa eksperimenta
    public void dodajDizajnera(int idEksperiment, int idDizajner, int idTeorijskiOkvir) throws SQLException {
        String sql = """
        INSERT INTO eksperiment_dizajner 
            (id_eksperiment, id_dizajner, glavni, id_teorijski_okvir)
        VALUES (?, ?, 0, ?)
        ON DUPLICATE KEY UPDATE id_teorijski_okvir = VALUES(id_teorijski_okvir)
        """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEksperiment);
            ps.setInt(2, idDizajner);
            ps.setInt(3, idTeorijskiOkvir);
            ps.executeUpdate();
        }
    }
    // Postavi glavnog dizajnera (resetuje ostale)
    public void postaviGlavnogDizajnera(int idEksperiment, int idDizajner) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                // resetuj sve na 0
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE eksperiment_dizajner SET glavni = 0 WHERE id_eksperiment = ?")) {
                    ps.setInt(1, idEksperiment);
                    ps.executeUpdate();
                }
                // postavi odabranog na 1
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE eksperiment_dizajner SET glavni = 1 WHERE id_eksperiment = ? AND id_dizajner = ?")) {
                    ps.setInt(1, idEksperiment);
                    ps.setInt(2, idDizajner);
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    // Dohvati dizajnere eksperimenta sa imenom i glavnim flagom
    public List<String[]> getDizajneriZaPrikaz(int idEksperiment) {
        List<String[]> lista = new ArrayList<>();
        String sql = """
        SELECT ed.id_dizajner, i.ime, i.prezime, ed.glavni
        FROM eksperiment_dizajner ed
        JOIN istrazivac i ON i.id_istrazivaca = ed.id_dizajner
        WHERE ed.id_eksperiment = ?
        """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEksperiment);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    lista.add(new String[]{
                            rs.getString("id_dizajner"),
                            rs.getString("ime") + " " + rs.getString("prezime"),
                            rs.getString("glavni")  // "1" ili "0"
                    });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    // Dohvati sve istrazivace koji su dizajneri za dropdown
    public List<String[]> getSviDizajneriZaDropdown() {
        List<String[]> lista = new ArrayList<>();
        String sql = """
        SELECT d.id_istrazivaca, i.ime, i.prezime
        FROM dizajner d
        JOIN istrazivac i ON i.id_istrazivaca = d.id_istrazivaca
        ORDER BY i.prezime, i.ime
        """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                lista.add(new String[]{
                        rs.getString("id_istrazivaca"),
                        rs.getString("ime") + " " + rs.getString("prezime")
                });
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }
    public void ukloniDizajnera(int idEksperiment, int idDizajner) throws SQLException {
        String sql = """
        DELETE FROM eksperiment_dizajner
        WHERE id_eksperiment = ? AND id_dizajner = ?
        """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEksperiment);
            ps.setInt(2, idDizajner);
            ps.executeUpdate();
        }
    }
}