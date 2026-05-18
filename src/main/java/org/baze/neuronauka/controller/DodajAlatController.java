package org.baze.neuronauka.controller;

import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.baze.neuronauka.dao.ResursiDAO;
import org.baze.neuronauka.model.LaboratorijaDTO;
import org.baze.neuronauka.view.DodajAlatView;


public class DodajAlatController {

    private final DodajAlatView view;
    private final ResursiDAO    dao;
    private final LaboratorijaDTO lab;
    private final Runnable      onUspeh;

    public DodajAlatController(DodajAlatView view,
                               ResursiDAO dao,
                               LaboratorijaDTO lab,
                               Runnable onUspeh) {
        this.view    = view;
        this.dao     = dao;
        this.lab     = lab;
        this.onUspeh = onUspeh;

        view.getTipCombo().getItems().setAll(dao.getSveTipoveAlata());

        view.getSacuvajBtn().setOnAction(e -> sacuvaj());
    }

    private void sacuvaj() {
        // ── validacija ────────────────────────────────────────────
        if (view.getNazivField().getText().isBlank()) {
            prikaziGrešku("Naziv je obavezan."); return;
        }
        if (view.getTipCombo().getValue() == null) {
            prikaziGrešku("Odaberite tip alata."); return;
        }
        if (view.getDatumNabavke().getValue() == null) {
            prikaziGrešku("Datum nabavke je obavezan."); return;
        }
        if (view.getDatumProizvodnje().getValue() == null) {
            prikaziGrešku("Datum proizvodnje je obavezan."); return;
        }
        if (view.getDatumKalibracije().getValue() == null) {
            prikaziGrešku("Datum kalibracije je obavezan."); return;
        }

        int interval;
        try {
            interval = Integer.parseInt(view.getIntervalField().getText().trim());
        } catch (NumberFormatException ex) {
            prikaziGrešku("Interval kalibracije mora biti broj."); return;
        }

        // ── slanje u bazu ─────────────────────────────────────────
        try {
            dao.dodajAlatULaboratoriju(
                    view.getNazivField().getText().trim(),
                    view.getSerijskiBrojField().getText().trim(),
                    view.getDatumNabavke().getValue(),
                    view.getDatumProizvodnje().getValue(),
                    view.getTipCombo().getValue().getIdTipAlata(),
                    lab.getIdLaboratorija(),
                    view.getDatumKalibracije().getValue(),
                    interval,
                    view.getNacinUpotrebe().getText().trim()
            );
            view.close();
            if (onUspeh != null) onUspeh.run();
        } catch (Exception ex) {
            prikaziGrešku("Greška pri čuvanju: " + ex.getMessage());
        }
    }

    private void prikaziGrešku(String poruka) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greška");
        alert.setHeaderText(null);
        alert.setContentText(poruka);
        alert.showAndWait();
    }
}