package org.baze.neuronauka.dao;

import org.baze.neuronauka.model.*;
import org.baze.neuronauka.db.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResursiDAO {

    // ══════════════════════════════════════════════════════════════
    //  LABORATORIJE
    // ══════════════════════════════════════════════════════════════

    public List<LaboratorijaDTO> getSveLaboratorije() {
        List<LaboratorijaDTO> lista = new ArrayList<>();
        String sql = "SELECT id_laboratorija, naziv FROM Laboratorija ORDER BY naziv";
        try (Connection con = DBConnection.getConnection();
             Statement  st  = con.createStatement();
             ResultSet  rs  = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new LaboratorijaDTO(
                        rs.getInt("id_laboratorija"),
                        rs.getString("naziv")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri učitavanju laboratorija: " + e.getMessage(), e);
        }
        return lista;
    }

    public List<LaboratorijaDTO> getLaboratorijeCekanje() {
        List<LaboratorijaDTO> lista = new ArrayList<>();
        String sql = "SELECT id_laboratorija, naziv FROM v_laboratorije_na_cekanju ORDER BY naziv";
        try (Connection con = DBConnection.getConnection();
             Statement  st  = con.createStatement();
             ResultSet  rs  = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new LaboratorijaDTO(
                        rs.getInt("id_laboratorija"),
                        rs.getString("naziv")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri učitavanju laboratorija na čekanju: " + e.getMessage(), e);
        }
        return lista;
    }

    // ══════════════════════════════════════════════════════════════
    //  DOSTUPNE ZALIHE
    // ══════════════════════════════════════════════════════════════

    public List<ResursiDTO> getZaliheLaboratorije(int idLaboratorija) {
        List<ResursiDTO> lista = new ArrayList<>();
        String sql =
                "SELECT r.id_resurs, r.naziv, lr.kolicina " +
                        "FROM Laboratorija_Resurs lr " +
                        "JOIN Resurs r ON r.id_resurs = lr.id_resurs " +
                        "WHERE lr.id_laboratorija = ? " +
                        "ORDER BY r.naziv";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idLaboratorija);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ResursiDTO(
                            rs.getInt("id_resurs"),
                            rs.getString("naziv"),
                            rs.getBigDecimal("kolicina")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri učitavanju zaliha: " + e.getMessage(), e);
        }
        return lista;
    }

    // ══════════════════════════════════════════════════════════════
    //  NEOPHODNI RESURSI
    // ══════════════════════════════════════════════════════════════

    public List<HitanResursDTO> getHitniResuriLaboratorije(int idLaboratorija) {
        List<HitanResursDTO> lista = new ArrayList<>();
        String sql =
                "SELECT r.id_resurs, r.naziv, lc.kolicina_fali " +
                        "FROM Laboratorija_Cekanje lc " +
                        "JOIN Resurs r ON r.id_resurs = lc.id_resurs " +
                        "WHERE lc.id_laboratorija = ? " +
                        "ORDER BY r.naziv";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idLaboratorija);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new HitanResursDTO(
                            rs.getInt("id_resurs"),
                            rs.getString("naziv"),
                            rs.getBigDecimal("kolicina_fali")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri učitavanju hitnih resursa: " + e.getMessage(), e);
        }   
        return lista;
    }

    // ══════════════════════════════════════════════════════════════
    //  PROCEDURE ZA IZMENU KOLIČINE
    // ══════════════════════════════════════════════════════════════

    public ResursiDTO dopuniZalihe(int idLaboratorija, int idResurs, double kolicina)
            throws SQLException {
        try (Connection con = DBConnection.getConnection();
             CallableStatement cs = con.prepareCall("{CALL dopuni_zalihe(?, ?, ?)}")) {
            cs.setInt(1, idLaboratorija);
            cs.setInt(2, idResurs);
            cs.setDouble(3, kolicina);
            cs.execute();
        }
        return getJedanResurs(idLaboratorija, idResurs);
    }

    public ResursiDTO umanjiZalihe(int idLaboratorija, int idResurs, double kolicina)
            throws SQLException {
        try (Connection con = DBConnection.getConnection();
             CallableStatement cs = con.prepareCall("{CALL umanji_zalihe(?, ?, ?)}")) {
            cs.setInt(1, idLaboratorija);
            cs.setInt(2, idResurs);
            cs.setDouble(3, kolicina);
            cs.execute();
        }
        return getJedanResurs(idLaboratorija, idResurs);
    }

    public void dodajSveIZapocniIzvodjenja(int idLaboratorija) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             CallableStatement cs = con.prepareCall("{CALL sp_dodaj_resurse_i_nastavi(?)}")) {
            cs.setInt(1, idLaboratorija);
            cs.execute();
        }
    }

    private ResursiDTO getJedanResurs(int idLaboratorija, int idResurs) throws SQLException {
        String sql =
                "SELECT r.id_resurs, r.naziv, lr.kolicina " +
                        "FROM Laboratorija_Resurs lr " +
                        "JOIN Resurs r ON r.id_resurs = lr.id_resurs " +
                        "WHERE lr.id_laboratorija = ? AND lr.id_resurs = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idLaboratorija);
            ps.setInt(2, idResurs);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new ResursiDTO(
                            rs.getInt("id_resurs"),
                            rs.getString("naziv"),
                            rs.getBigDecimal("kolicina"));
                }
            }
        }
        throw new SQLException("Resurs nije pronađen nakon izmene.");
    }

    // ══════════════════════════════════════════════════════════════
    //  ALAT — nove metode (pozivaju procedure)
    // ══════════════════════════════════════════════════════════════

    public List<AlatGrupaDTO> getAlatPoLaboratoriji(int idLaboratorija) throws SQLException {
        List<AlatGrupaDTO> lista = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             CallableStatement cs = con.prepareCall("{CALL GetAlatPoLaboratoriji(?)}")) {
            cs.setInt(1, idLaboratorija);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    lista.add(new AlatGrupaDTO(
                            rs.getInt("id_tip_alata"),
                            rs.getString("tip_alata"),
                            rs.getInt("ukupno_alata")));
                }
            }
        }
        return lista;
    }

    public List<AlatDetaljiDTO> getDetaljiAlata(int idTipAlata, int idLaboratorija)
            throws SQLException {
        List<AlatDetaljiDTO> lista = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             CallableStatement cs = con.prepareCall("{CALL GetDetaljiAlata(?, ?)}")) {
            cs.setInt(1, idTipAlata);
            cs.setInt(2, idLaboratorija);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    lista.add(new AlatDetaljiDTO(
                            rs.getInt("id_alat"),
                            rs.getString("tip_alata"),
                            rs.getString("status_alata")));
                }
            }
        }
        return lista;
    }

    public void promeniStatusAlata(int idAlat, boolean aktivan) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             CallableStatement cs = con.prepareCall("{CALL PromeniStatusAlata(?, ?)}")) {
            cs.setInt(1, idAlat);
            cs.setInt(2, aktivan ? 1 : 0);  // samo konverzija tipa, ne logika
            cs.executeUpdate();
        }
    }
    public void dodajAlatULaboratoriju(String naziv,
                                       String serijskiBroj,
                                       java.time.LocalDate datumNabavke,
                                       java.time.LocalDate datumProizvodnje,
                                       int idTipAlata,
                                       int idLaboratorija,
                                       java.time.LocalDate datumKalibracije,
                                       int intervalKalibracije,
                                       String nacinUpotrebe) throws SQLException {

        try (Connection con = DBConnection.getConnection();
             CallableStatement cs = con.prepareCall(
                     "{CALL DodajAlatULaboratoriju(?, ?, ?, ?, ?, ?, ?, ?, ?)}")) {

            cs.setString(1, naziv);
            cs.setString(2, serijskiBroj);
            cs.setDate(3, java.sql.Date.valueOf(datumNabavke));
            cs.setDate(4, java.sql.Date.valueOf(datumProizvodnje));
            cs.setInt(5, idTipAlata);
            cs.setInt(6, idLaboratorija);
            cs.setDate(7, java.sql.Date.valueOf(datumKalibracije));
            cs.setInt(8, intervalKalibracije);
            cs.setString(9, nacinUpotrebe);

            cs.executeUpdate();
        }
    }


    public List<TipAlataDTO> getSveTipoveAlata() {
        List<TipAlataDTO> lista = new ArrayList<>();
        String sql = "SELECT id_tip_alata, naziv FROM Tip_Alata ORDER BY naziv";
        try (Connection con = DBConnection.getConnection();
             Statement  st  = con.createStatement();
             ResultSet  rs  = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new TipAlataDTO(
                        rs.getInt("id_tip_alata"),
                        rs.getString("naziv")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri učitavanju tipova alata: " + e.getMessage(), e);
        }
        return lista;
    }
}