// repository/AlatRepository.java
package org.baze.neuronauka.repository;

import org.baze.neuronauka.model.AlatDetaljiDTO;
import org.baze.neuronauka.model.AlatGrupaDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlatRepository {

    private final Connection conn;

    public AlatRepository(Connection conn) {
        this.conn = conn;
    }

    // ── Poziva GetAlatPoLaboratoriji ──────────────────────────────
    public List<AlatGrupaDTO> getAlatPoLaboratoriji(int idLaboratorija)
            throws SQLException {

        List<AlatGrupaDTO> lista = new ArrayList<>();

        try (CallableStatement cs = conn.prepareCall(
                "{CALL GetAlatPoLaboratoriji(?)}")) {

            cs.setInt(1, idLaboratorija);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    lista.add(new AlatGrupaDTO(
                            rs.getInt("id_tip_alata"),
                            rs.getString("tip_alata"),
                            rs.getInt("ukupno_alata")
                    ));
                }
            }
        }
        return lista;
    }

    // ── Poziva GetDetaljiAlata ────────────────────────────────────
    public List<AlatDetaljiDTO> getDetaljiAlata(int idTipAlata,
                                                int idLaboratorija)
            throws SQLException {

        List<AlatDetaljiDTO> lista = new ArrayList<>();

        try (CallableStatement cs = conn.prepareCall(
                "{CALL GetDetaljiAlata(?, ?)}")) {

            cs.setInt(1, idTipAlata);
            cs.setInt(2, idLaboratorija);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    lista.add(new AlatDetaljiDTO(
                            rs.getInt("id_alat"),
                            rs.getString("tip_alata"),
                            rs.getString("status_alata")
                    ));
                }
            }
        }
        return lista;
    }

    // ── Poziva PromeniStatusAlata ─────────────────────────────────
    public void promeniStatus(int idAlat, boolean aktivan)
            throws SQLException {

        String noviStatus = aktivan ? "aktivan" : "na servisu";

        try (CallableStatement cs = conn.prepareCall(
                "{CALL PromeniStatusAlata(?, ?)}")) {

            cs.setInt(1, idAlat);
            cs.setString(2, noviStatus);
            cs.executeUpdate();
        }
    }
}