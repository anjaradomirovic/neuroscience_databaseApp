package org.baze.neuronauka.controller;

import javafx.scene.control.Alert;
import org.baze.neuronauka.dao.ResursiDAO;
import org.baze.neuronauka.model.HitanResursDTO;
import org.baze.neuronauka.model.LaboratorijaDTO;
import org.baze.neuronauka.model.ResursiDTO;
import org.baze.neuronauka.view.ResursiContent;

import java.sql.SQLException;
import java.util.List;

public class ResursiController {

    private final ResursiContent view;
    private final ResursiDAO     dao = new ResursiDAO();
    private final String         loggedInUser;
    private final Runnable       onBack;

    public ResursiController(ResursiContent view, String loggedInUser, Runnable onBack) {
        this.view         = view;
        this.loggedInUser = loggedInUser;
        this.onBack       = onBack;
        ucitajLaboratorije();
        initActions();
    }

    // ── INICIJALIZACIJA ────────────────────────────────────────────

    private void ucitajLaboratorije() {
        view.setLaboratorijeSve(dao.getSveLaboratorije());
        view.setLaboratorijeCekanje(dao.getLaboratorijeCekanje());
    }

    private void initActions() {

        // ── levi filter — sve laboratorije, prikazuje zalihe ──────
        view.getLabSveCombo().setOnAction(e -> {
            LaboratorijaDTO odabrana = view.getLabSveCombo().getValue();
            if (odabrana == null) {
                view.resetZalihe();
            } else {
                List<ResursiDTO> zalihe =
                        dao.getZaliheLaboratorije(odabrana.getIdLaboratorija());
                view.prikaziZalihe(zalihe);
            }
        });

        // ── desni filter — lab na čekanju, prikazuje šta fali ─────
        view.getLabCekanjeCombo().setOnAction(e -> {
            LaboratorijaDTO odabrana = view.getLabCekanjeCombo().getValue();
            if (odabrana == null) {
                view.resetHitne();
            } else {
                List<HitanResursDTO> hitni =
                        dao.getHitniResuriLaboratorije(odabrana.getIdLaboratorija());
                view.prikaziHitne(hitni);
            }
        });

        // ── plus dugme ─────────────────────────────────────────────
        view.setOnPlus(resurs -> {
            LaboratorijaDTO lab = view.getLabSveCombo().getValue();
            if (lab == null) return;
            try {
                ResursiDTO updated = dao.dopuniZalihe(
                        lab.getIdLaboratorija(), resurs.getIdResurs(), 1.0);
                view.osvežiStavkuZalihe(updated);
            } catch (SQLException ex) {
                prikaziGrešku("Greška pri dodavanju", ex.getMessage());
            }
        });



        // ── dodaj sve dugme ────────────────────────────────────────
        view.getDodajSveBtn().setOnAction(e -> {
            LaboratorijaDTO lab = view.getLabCekanjeCombo().getValue();
            if (lab == null) {
                prikaziGrešku("Nije odabrana laboratorija",
                        "Odaberite laboratoriju sa desne strane.");
                return;
            }
            try {
                dao.dodajSveIZapocniIzvodjenja(lab.getIdLaboratorija());

                // osvezi desnu stranu
                List<HitanResursDTO> hitni =
                        dao.getHitniResuriLaboratorije(lab.getIdLaboratorija());
                view.prikaziHitne(hitni);

                // osvezi desni filter jer se cekanje promenilo
                view.setLaboratorijeCekanje(dao.getLaboratorijeCekanje());

                // osvezi levu stranu ako je ista laboratorija odabrana levo
                LaboratorijaDTO labLevo = view.getLabSveCombo().getValue();
                if (labLevo != null &&
                        labLevo.getIdLaboratorija() == lab.getIdLaboratorija()) {
                    List<ResursiDTO> zalihe =
                            dao.getZaliheLaboratorije(lab.getIdLaboratorija());
                    view.prikaziZalihe(zalihe);
                }
            } catch (SQLException ex) {
                prikaziGrešku("Greška pri dodavanju", ex.getMessage());
            }
        });
    }

    public void osveziLaboratorije() {
        view.setLaboratorijeSve(dao.getSveLaboratorije());
        view.setLaboratorijeCekanje(dao.getLaboratorijeCekanje());
    }

    // ── HELPER ────────────────────────────────────────────────────

    private void prikaziGrešku(String naslov, String poruka) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greška");
        alert.setHeaderText(naslov);
        alert.setContentText(poruka);
        alert.showAndWait();
    }
}