package org.baze.neuronauka.dao;

import org.baze.neuronauka.db.DBConnection;
import org.baze.neuronauka.model.HitanResursDTO;
import org.baze.neuronauka.model.SesijaDTO;
import org.baze.neuronauka.model.SesijaResursDTO;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class SesijaDAO {

    // ─────────────────────────────────────────────────────────────
    // POMOĆNA METODA — čita ResultSet red po red i pravi SesijaDTO
    // Koristimo je na više mesta da ne dupliramo kod
    // ─────────────────────────────────────────────────────────────
    private SesijaDTO mapRow(ResultSet rs) throws SQLException {
        SesijaDTO dto = new SesijaDTO();
        dto.setIdSesija(rs.getInt("id_sesija"));
        dto.setDatumSesije(rs.getDate("datum_sesije").toLocalDate());
        dto.setVremePocetka(rs.getTime("vreme_pocetka").toLocalTime());
        dto.setVremeKraja(rs.getTime("vreme_kraja").toLocalTime());
        dto.setTipSesije(rs.getString("tip_sesije"));
        dto.setEksperiment(rs.getString("eksperiment"));
        dto.setLaboratorija(rs.getString("laboratorija"));
        return dto;
    }

    // ─────────────────────────────────────────────────────────────
    // 1. SVE SESIJE — za punjenje kalendara
    //
    // Lanac JOIN-ova:
    //   Sesija
    //     → Izvodjenje       (da dođemo do eksperimenta i laboratorije)
    //     → Eksperiment      (naziv eksperimenta)
    //     → Laboratorija     (naziv laboratorije)
    //
    // Nema parametara → koristimo Statement (ne PreparedStatement)
    // ─────────────────────────────────────────────────────────────
    public List<SesijaDTO> getSveSesjeSaEksperimentima() {
        List<SesijaDTO> lista = new ArrayList<>();

        String sql = """
                SELECT
                    s.id_sesija,
                    s.datum_sesije,
                    s.vreme_pocetka,
                    s.vreme_kraja,
                    s.tip_sesije,
                    e.naziv  AS eksperiment,
                    l.naziv  AS laboratorija
                FROM      Sesija       s
                JOIN      Izvodjenje   iz  ON s.id_izvodjenje    = iz.id_izvodjenje
                JOIN      Eksperiment  e   ON iz.id_eksperimenta = e.id_eksperiment
                JOIN      Laboratorija l   ON iz.id_laboratorija = l.id_laboratorija
                ORDER BY  s.datum_sesije, s.vreme_pocetka
                """;

        try (Connection con = DBConnection.getConnection();
             Statement  st  = con.createStatement();
             ResultSet  rs  = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapRow(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // ─────────────────────────────────────────────────────────────
    // 2. SESIJE FILTRIRANE po laboratoriji i/ili eksperimentu
    //
    // Trik: NULL u PreparedStatement-u znači "filter nije odabran"
    // → SQL upit sam odlučuje da ignoriše taj filter
    // → cela logika filtriranja ostaje u SQL-u, ne u Javi
    //
    // Ima parametre → koristimo PreparedStatement
    // ─────────────────────────────────────────────────────────────
    public List<SesijaDTO> getSesijeFiltered(String laboratorija, String eksperiment) {
        List<SesijaDTO> lista = new ArrayList<>();

        String sql = """
                SELECT
                    s.id_sesija,
                    s.datum_sesije,
                    s.vreme_pocetka,
                    s.vreme_kraja,
                    s.tip_sesije,
                    e.naziv  AS eksperiment,
                    l.naziv  AS laboratorija
                FROM      Sesija       s
                JOIN      Izvodjenje   iz  ON s.id_izvodjenje    = iz.id_izvodjenje
                JOIN      Eksperiment  e   ON iz.id_eksperimenta = e.id_eksperiment
                JOIN      Laboratorija l   ON iz.id_laboratorija = l.id_laboratorija
                WHERE
                    (? IS NULL OR l.naziv = ?)
                    AND (? IS NULL OR e.naziv = ?)
                ORDER BY  s.datum_sesije, s.vreme_pocetka
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Ako je null prosleđeno → SQL ignoruje taj filter (? IS NULL je true)
            // Ako je vrednost prosleđena → SQL filtrira po njoj
            ps.setString(1, laboratorija);
            ps.setString(2, laboratorija);
            ps.setString(3, eksperiment);
            ps.setString(4, eksperiment);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // ─────────────────────────────────────────────────────────────
    // 3. DROPDOWN — svi nazivi laboratorija
    //
    // Nema parametara → Statement
    // ─────────────────────────────────────────────────────────────
    public List<String> getLaboratorije() {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT naziv FROM Laboratorija ORDER BY naziv";

        try (Connection con = DBConnection.getConnection();
             Statement  st  = con.createStatement();
             ResultSet  rs  = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(rs.getString("naziv"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // ─────────────────────────────────────────────────────────────
    // 4. DROPDOWN — svi nazivi eksperimenata
    //
    // Nema parametara → Statement
    // ─────────────────────────────────────────────────────────────
    public List<String> getEksperimenti() {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT naziv FROM Eksperiment ORDER BY naziv";

        try (Connection con = DBConnection.getConnection();
             Statement  st  = con.createStatement();
             ResultSet  rs  = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(rs.getString("naziv"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // ─────────────────────────────────────────────────────────────
    // 5. UPDATE SESIJE — ima parametre → PreparedStatement
    //
    // Baca SQLException ako trigger u bazi (sesija_nooverlap_update)
    // detektuje vremensko preklapanje sa drugom sesijom.
    // Caller (controller) hvata tu grešku i prikazuje je korisniku.
    // ─────────────────────────────────────────────────────────────
    public void updateSesija(int idSesija,
                             java.time.LocalDate datum,
                             java.time.LocalTime pocetakVremena,
                             java.time.LocalTime krajVremena,
                             String tipSesije) throws SQLException {

        String sql = """
                UPDATE Sesija
                SET
                    datum_sesije  = ?,
                    vreme_pocetka = ?,
                    vreme_kraja   = ?,
                    tip_sesije    = ?
                WHERE id_sesija = ?
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, java.sql.Date.valueOf(datum));
            ps.setTime(2, java.sql.Time.valueOf(pocetakVremena));
            ps.setTime(3, java.sql.Time.valueOf(krajVremena));
            ps.setString(4, tipSesije);
            ps.setInt(5, idSesija);

            ps.executeUpdate();
            // ako trigger puca, executeUpdate() baca SQLException
            // — ne hvatamo je ovde, propagiramo je ka controlleru
        }
    }

    // Učitava resurse dostupne za izvodjenje (iz Izvodjenje_Resurs)
    public List<SesijaResursDTO> getResuriZaIzvodjenje(int idIzvodjenje) {
        List<SesijaResursDTO> lista = new ArrayList<>();
        String sql =
                "SELECT r.id_resurs, r.naziv, ir.kolicina " +
                        "FROM Izvodjenje_Resurs ir " +
                        "JOIN Resurs r ON r.id_resurs = ir.id_resurs " +
                        "WHERE ir.id_izvodjenje = ? " +
                        "ORDER BY r.naziv";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idIzvodjenje);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new SesijaResursDTO(
                            rs.getInt("id_resurs"),
                            rs.getString("naziv"),
                            rs.getDouble("kolicina")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
    public void zabeležiUtrosak(int idSesija, int idResurs, double kolicina) throws SQLException {
        String sql = "INSERT INTO Utrosak_resursa (id_sesija, id_resurs, kolicina) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSesija);
            ps.setInt(2, idResurs);
            ps.setDouble(3, kolicina);
            ps.executeUpdate();
        }
    }
    public int insertSesijaVratiId(LocalDate datum, LocalTime pocetak,
                                   LocalTime kraj, String tip,
                                   int idIzvodjenje) throws SQLException {
        String sql = """
            INSERT INTO Sesija (datum_sesije, vreme_pocetka, vreme_kraja, tip_sesije, id_izvodjenje)
            VALUES (?, ?, ?, ?, ?)
            """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, java.sql.Date.valueOf(datum));
            ps.setTime(2, java.sql.Time.valueOf(pocetak));
            ps.setTime(3, java.sql.Time.valueOf(kraj));
            ps.setString(4, tip);
            ps.setInt(5, idIzvodjenje);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Nije vraćen ID nove sesije.");
    }




    public void pokreniZabeležiUtrosak(int idSesija) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             CallableStatement cs = con.prepareCall("{CALL sp_zabeleži_utrosak(?)}")) {
            cs.setInt(1, idSesija);
            cs.execute();
        }
    }
    public List<HitanResursDTO> pokrenniZabeležiUtrosak(int idSesija) throws SQLException {
        List<HitanResursDTO> lista = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             CallableStatement cs = con.prepareCall("{CALL sp_zabeleži_utrosak(?)}")) {
            cs.setInt(1, idSesija);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    lista.add(new HitanResursDTO(
                            rs.getInt("id_resurs"),
                            rs.getString("naziv_resursa"),
                            rs.getBigDecimal("kolicina_fali")));
                }
            }
        }
        return lista;
    }


    public List<HitanResursDTO> proveriResurse(int idIzvodjenje) throws SQLException {
        List<HitanResursDTO> lista = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             CallableStatement cs = con.prepareCall("{CALL sp_proveri_resurse(?)}")) {
            cs.setInt(1, idIzvodjenje);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    lista.add(new HitanResursDTO(
                            rs.getInt("id_resurs"),
                            rs.getString("naziv_resursa"),
                            rs.getBigDecimal("kolicina_fali")));
                }
            }
        }
        return lista;
    }
    public List<HitanResursDTO> proveriResurseIzvodjenja(int idIzvodjenje) throws SQLException {
        List<HitanResursDTO> lista = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             CallableStatement cs = con.prepareCall("{CALL sp_proveri_resurse(?)}")) {
            cs.setInt(1, idIzvodjenje);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    lista.add(new HitanResursDTO(
                            rs.getInt("id_resurs"),
                            rs.getString("naziv_resursa"),
                            rs.getBigDecimal("kolicina_fali")));
                }
            }
        }
        return lista;
    }
    public void azurirajStatusAkoJePrvaSesija(int idIzvodjenje) throws SQLException {
        String sql = """
            UPDATE Izvodjenje
            SET status = 'zapoceto'
            WHERE id_izvodjenje = ?
              AND status = 'planirano'
              AND (SELECT COUNT(*) FROM Sesija WHERE id_izvodjenje = ?) = 1
            """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idIzvodjenje);
            ps.setInt(2, idIzvodjenje);
            ps.executeUpdate();
        }
    }
}