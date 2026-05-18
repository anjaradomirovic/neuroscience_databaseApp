package org.baze.neuronauka.dao;

import org.baze.neuronauka.db.DBConnection;
import org.baze.neuronauka.model.IzvodjenjeDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IzvodjenjeDAO {

    // ─────────────────────────────────────────────────────────────
    // Vraća izvodjenja u statusu 'planirano' ili 'zapoceto'
    // — koristi se za dropdown pri dodavanju nove sesije
    // ─────────────────────────────────────────────────────────────
    public List<IzvodjenjeDTO> getAktivnaIzvodjenja() {
        List<IzvodjenjeDTO> lista = new ArrayList<>();

        String sql = """
                SELECT
                    iz.id_izvodjenje,
                    iz.datum_izvodjenja,
                    iz.status,
                    e.naziv  AS eksperiment,
                    l.naziv  AS laboratorija
                FROM  Izvodjenje  iz
                JOIN  Eksperiment e  ON iz.id_eksperimenta = e.id_eksperiment
                JOIN  Laboratorija l ON iz.id_laboratorija = l.id_laboratorija
                WHERE iz.status IN ('planirano', 'zapoceto')
                ORDER BY iz.datum_izvodjenja, iz.id_izvodjenje
                """;

        try (Connection con = DBConnection.getConnection();
             Statement  st  = con.createStatement();
             ResultSet  rs  = st.executeQuery(sql)) {

            while (rs.next()) {
                IzvodjenjeDTO dto = new IzvodjenjeDTO();
                dto.setIdIzvodjenje(rs.getInt("id_izvodjenje"));
                dto.setDatumIzvodjenja(rs.getDate("datum_izvodjenja").toLocalDate());
                dto.setStatus(rs.getString("status"));
                dto.setEksperiment(rs.getString("eksperiment"));
                dto.setLaboratorija(rs.getString("laboratorija"));
                lista.add(dto);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }


    // ── NOVE METODE — dodaj na kraj klase ────────────────────────

    public List<IzvodjenjeDTO> getSvaIzvodjenja(Integer idEksperimenta) {
        List<IzvodjenjeDTO> lista = new ArrayList<>();
        String sql = """
        SELECT i.id_izvodjenje,
               i.datum_izvodjenja,
               i.status,
               i.id_eksperimenta,
               e.naziv        AS naziv_eksperimenta,
               i.id_laboratorija,
               l.naziv        AS naziv_laboratorije
        FROM izvodjenje i
        JOIN eksperiment  e ON e.id_eksperiment  = i.id_eksperimenta
        JOIN laboratorija l ON l.id_laboratorija = i.id_laboratorija
        WHERE (? IS NULL OR i.id_eksperimenta = ?)
        ORDER BY i.datum_izvodjenja DESC
        """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (idEksperimenta != null) {
                ps.setInt(1, idEksperimenta);
                ps.setInt(2, idEksperimenta);
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public IzvodjenjeDTO getDetalji(int idIzvodjenje) {
        String sql = """
            SELECT i.id_izvodjenje,
                   i.datum_izvodjenja,
                   i.status,
                   i.id_eksperimenta,
                   e.naziv        AS naziv_eksperimenta,
                   i.id_laboratorija,
                   l.naziv        AS naziv_laboratorije
            FROM izvodjenje i
            JOIN eksperiment  e ON e.id_eksperiment  = i.id_eksperimenta
            JOIN laboratorija l ON l.id_laboratorija = i.id_laboratorija
            WHERE i.id_izvodjenje = ?
            """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idIzvodjenje);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                IzvodjenjeDTO dto = mapRow(rs);
                dto.setResurei(getResurei(con, idIzvodjenje));
                dto.setIzvodjaci(getIzvodjaci(con, idIzvodjenje));
                return dto;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void izmeniStatus(int idIzvodjenje, String noviStatus) throws SQLException {
        String sql = "UPDATE izvodjenje SET status = ? WHERE id_izvodjenje = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, noviStatus);
            ps.setInt(2, idIzvodjenje);
            ps.executeUpdate();
        }
    }

    public String[] kreirajIzvodjenje(int idEksperimenta, int idLaboratorija,
                                      java.time.LocalDate datum) throws SQLException {
        String sql = "CALL sp_kreiraj_izvodjenje(?, ?, ?, NULL)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEksperimenta);
            ps.setInt(2, idLaboratorija);
            ps.setDate(3, java.sql.Date.valueOf(datum));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new String[]{
                            rs.getString("id_izvodjenje"),
                            rs.getString("status")
                    };
                }
            }
        }
        return new String[]{"?", "?"};
    }

    public List<String[]> getSviEksperimentiZaFilter() {
        List<String[]> lista = new ArrayList<>();
        String sql = "SELECT id_eksperiment, naziv FROM eksperiment ORDER BY naziv";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                lista.add(new String[]{
                        rs.getString("id_eksperiment"),
                        rs.getString("naziv")
                });
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<String[]> getSveLaboratorije() {
        List<String[]> lista = new ArrayList<>();
        String sql = "SELECT id_laboratorija, naziv FROM laboratorija ORDER BY naziv";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                lista.add(new String[]{
                        rs.getString("id_laboratorija"),
                        rs.getString("naziv")
                });
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

// ── privatne pomoćne ──────────────────────────────────────────

    private IzvodjenjeDTO mapRow(ResultSet rs) throws SQLException {
        IzvodjenjeDTO dto = new IzvodjenjeDTO();
        dto.setIdIzvodjenje(rs.getInt("id_izvodjenje"));
        dto.setDatumIzvodjenja(rs.getDate("datum_izvodjenja").toLocalDate());
        dto.setStatus(rs.getString("status"));
        dto.setIdEksperimenta(rs.getInt("id_eksperimenta"));
        dto.setNazivEksperimenta(rs.getString("naziv_eksperimenta"));
        dto.setIdLaboratorija(rs.getInt("id_laboratorija"));
        dto.setNazivLaboratorije(rs.getString("naziv_laboratorije"));
        return dto;
    }

    private List<IzvodjenjeDTO.IzvodjenjeResursStavka> getResurei(
            Connection con, int idIzvodjenje) throws SQLException {
        List<IzvodjenjeDTO.IzvodjenjeResursStavka> lista = new ArrayList<>();
        String sql = """
            SELECT ir.id_resurs, r.naziv, ir.kolicina
            FROM izvodjenje_resurs ir
            JOIN resurs r ON r.id_resurs = ir.id_resurs
            WHERE ir.id_izvodjenje = ?
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idIzvodjenje);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    lista.add(new IzvodjenjeDTO.IzvodjenjeResursStavka(
                            rs.getInt("id_resurs"),
                            rs.getString("naziv"),
                            rs.getDouble("kolicina")));
            }
        }
        return lista;
    }

    private List<IzvodjenjeDTO.IzvodjenjeIzvodjacStavka> getIzvodjaci(
            Connection con, int idIzvodjenje) throws SQLException {
        List<IzvodjenjeDTO.IzvodjenjeIzvodjacStavka> lista = new ArrayList<>();
        String sql = """
            SELECT ii.id_istrazivaca,
                   i.ime, i.prezime,
                   ii.opis_uloge,
                   ii.put_do_beleski
            FROM izvodjenje_izvodjac ii
            JOIN istrazivac i ON i.id_istrazivaca = ii.id_istrazivaca
            WHERE ii.id_izvodjenje = ?
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idIzvodjenje);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    lista.add(new IzvodjenjeDTO.IzvodjenjeIzvodjacStavka(
                            rs.getInt("id_istrazivaca"),
                            rs.getString("ime") + " " + rs.getString("prezime"),
                            rs.getString("opis_uloge"),
                            rs.getString("put_do_beleski")));
            }
        }
        return lista;
    }
    public void dodajIzvodjaca(int idIzvodjenje, int idIstrazivaca, String opisUloge) throws SQLException {
        String sql = """
        INSERT INTO izvodjenje_izvodjac 
            (id_izvodjenje, id_istrazivaca, opis_uloge)
        VALUES (?, ?, ?)
        ON DUPLICATE KEY UPDATE opis_uloge = VALUES(opis_uloge)
        """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idIzvodjenje);
            ps.setInt(2, idIstrazivaca);
            ps.setString(3, opisUloge);
            ps.executeUpdate();
        }
    }


    public void ukloniIzvodjaca(int idIzvodjenje, int idIstrazivaca) throws SQLException {
        String sql = """
        DELETE FROM izvodjenje_izvodjac
        WHERE id_izvodjenje = ? AND id_istrazivaca = ?
        """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idIzvodjenje);
            ps.setInt(2, idIstrazivaca);
            ps.executeUpdate();
        }
    }

    public List<String[]> getSviIstrazivaci() {
        List<String[]> lista = new ArrayList<>();
        String sql = """
        SELECT i.id_istrazivaca, i.ime, i.prezime
        FROM izvodjac iz
        JOIN istrazivac i ON i.id_istrazivaca = iz.id_istrazivaca
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
}