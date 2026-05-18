package org.baze.neuronauka.dao;

import org.baze.neuronauka.db.DBConnection;
import org.baze.neuronauka.model.StatistikaDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StatistikaDAO {

    /**
     * Za svaki eksperiment: naziv, ukupno izvodjenja, uspešnost %.
     * Poziva funkciju uspesnost_eksperimenta() za svaki red.
     */
    public List<StatistikaDTO.EksperimentStatistika> getStatistikaPoEksperimentu() {
        List<StatistikaDTO.EksperimentStatistika> lista = new ArrayList<>();
        String sql = """
                SELECT e.id_eksperiment,
                       e.naziv,
                       COUNT(i.id_izvodjenje)                        AS ukupno,
                       SUM(i.status = 'zapoceto')                    AS aktivna,
                       SUM(i.status = 'zavrseno_uspesno')            AS uspesno,
                       SUM(i.status = 'zavrseno_neuspesno')          AS neuspesno,
                       SUM(i.status IN ('planirano','odlozeno'))      AS na_cekanju,
                       uspesnost_eksperimenta(e.id_eksperiment)       AS procenat_uspesnosti
                FROM eksperiment e
                LEFT JOIN izvodjenje i ON i.id_eksperimenta = e.id_eksperiment
                GROUP BY e.id_eksperiment, e.naziv
                ORDER BY ukupno DESC
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new StatistikaDTO.EksperimentStatistika(
                        rs.getInt("id_eksperiment"),
                        rs.getString("naziv"),
                        rs.getInt("ukupno"),
                        rs.getInt("aktivna"),
                        rs.getInt("uspesno"),
                        rs.getInt("neuspesno"),
                        rs.getInt("na_cekanju"),
                        rs.getDouble("procenat_uspesnosti")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatanju statistike eksperimenata", e);
        }
        return lista;
    }

    /**
     * Po laboratoriji: naziv, broj završenih izvodjenja (funkcija iz baze).
     */
    public List<StatistikaDTO.LaboratorijaStatistika> getStatistikaPoLaboratoriji() {
        List<StatistikaDTO.LaboratorijaStatistika> lista = new ArrayList<>();
        String sql = """
                SELECT l.id_laboratorija,
                       l.naziv,
                       broj_zavrsenih_izvodjenja(l.id_laboratorija) AS zavrsenih
                FROM laboratorija l
                ORDER BY zavrsenih DESC
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new StatistikaDTO.LaboratorijaStatistika(
                        rs.getInt("id_laboratorija"),
                        rs.getString("naziv"),
                        rs.getInt("zavrsenih")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatanju statistike laboratorija", e);
        }
        return lista;
    }

    /** Sumarne kartice — ukupno, aktivna, prosečna uspešnost. */
    public StatistikaDTO.SumarneKartice getSumarneKartice() {
        String sql = """
                SELECT COUNT(*)                                       AS ukupno,
                       SUM(status = 'zapoceto')                       AS aktivna,
                       SUM(status IN ('zavrseno_uspesno',
                                      'zavrseno_neuspesno'))          AS zavrsena,
                       ROUND(AVG(
                           CASE WHEN status IN ('zavrseno_uspesno','zavrseno_neuspesno')
                                THEN (status = 'zavrseno_uspesno') * 100.0
                                ELSE NULL END), 1)                    AS prosek_uspesnosti
                FROM izvodjenje
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new StatistikaDTO.SumarneKartice(
                        rs.getInt("ukupno"),
                        rs.getInt("aktivna"),
                        rs.getInt("zavrsena"),
                        rs.getDouble("prosek_uspesnosti"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatanju sumarnih kartica", e);
        }
        return new StatistikaDTO.SumarneKartice(0, 0, 0, 0);
    }
}