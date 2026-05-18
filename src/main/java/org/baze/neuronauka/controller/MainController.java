package org.baze.neuronauka.controller;

import javafx.scene.control.Alert;
import org.baze.neuronauka.dao.ResursiDAO;
import org.baze.neuronauka.model.AlatDetaljiDTO;
import org.baze.neuronauka.model.AlatGrupaDTO;
import org.baze.neuronauka.model.LaboratorijaDTO;
import org.baze.neuronauka.view.*;

import java.util.List;

public class MainController {

    private final MainView   mainView;
    private final ResursiDAO resursiDAO = new ResursiDAO();
    private final String     loggedInUser;

    private SesijaView       sesijaView;
    private SesijaController sesijaController;

    private ResursiContent    resursiContent;
    private ResursiController resursiController;
    private AlatContent       alatContent;

    private EksperimentContent    eksperimentContent;
    private EksperimentController eksperimentController;

    private StatistikaContent    statistikaContent;
    private StatistikaController statistikaController;

    private IzvodjenjeContent    izvodjenjeContent;
    private IzvodjenjeController izvodjenjeController;

    public MainController(MainView mainView, String loggedInUser) {
        this.mainView     = mainView;
        this.loggedInUser = loggedInUser;
        initActions();
        prikaziSesije();
    }



    private void initActions() {
        mainView.getPregledSesijaItem().setOnAction(e -> prikaziSesije());
        mainView.getNovaSesijaItem().setOnAction(e    -> { /* TODO */ });

        mainView.getEditLabItem().setOnAction(e   -> otvoriEditLab());
        mainView.getResuriLabItem().setOnAction(e -> prikaziResurse());
        mainView.getAlatLabItem().setOnAction(e   -> prikaziAlat());

        mainView.getEditExpItem().setOnAction(e     -> otvoriEditExp());
        mainView.getStatsExpItem().setOnAction(e    -> prikaziStatistiku());  // samo jednom
        mainView.getIzvodjenjeExpItem().setOnAction(e -> prikaziIzvodjenje());
    }

    private void prikaziStatistiku() {
        if (statistikaContent == null) {
            statistikaContent    = new StatistikaContent();
            statistikaController = new StatistikaController(statistikaContent);
        }
        // bez setActiveView
        mainView.switchCenter(statistikaContent.build());
    }

    private void prikaziIzvodjenje() {
        if (izvodjenjeContent == null) {
            izvodjenjeContent    = new IzvodjenjeContent();
            izvodjenjeController = new IzvodjenjeController(
                    izvodjenjeContent, mainView.getStage());
        }
        mainView.switchCenter(izvodjenjeContent.build());
    }
    // ══════════════════════════════════════════════════════════════
    //  PRIKAZ SESIJA
    // ══════════════════════════════════════════════════════════════

    private void prikaziSesije() {
        if (sesijaView == null) {
            sesijaView       = new SesijaView();
            sesijaController = new SesijaController(sesijaView, loggedInUser);
        }
        mainView.setActiveView(MainView.ActiveView.SESIJE);
        mainView.switchCenter(sesijaView.buildSesijaCenter());
        sesijaController.reinitNavActions();
    }

    // ══════════════════════════════════════════════════════════════
    //  PRIKAZ RESURSA
    // ══════════════════════════════════════════════════════════════

    private void prikaziResurse() {
        if (resursiContent == null) {
            resursiContent    = new ResursiContent();
            resursiController = new ResursiController(
                    resursiContent, loggedInUser, this::prikaziSesije);
        }
        resursiContent.setLaboratorijeSve(resursiDAO.getSveLaboratorije());
        resursiContent.setLaboratorijeCekanje(resursiDAO.getLaboratorijeCekanje());

        mainView.setActiveView(MainView.ActiveView.RESURSI);
        mainView.switchCenter(resursiContent.build());
    }

    // ══════════════════════════════════════════════════════════════
    //  PRIKAZ ALATA
    // ══════════════════════════════════════════════════════════════

    private void prikaziAlat() {
        if (alatContent == null) {
            alatContent = new AlatContent();
            initAlatActions();
        }
        alatContent.setLaboratorije(resursiDAO.getSveLaboratorije());

        mainView.setActiveView(MainView.ActiveView.ALAT);
        mainView.switchCenter(alatContent.build());
    }

    private void initAlatActions() {

        // ── Bira se laboratorija → puni levu tabelu ───────────────
        alatContent.getLabCombo().setOnAction(e -> {
            LaboratorijaDTO lab = alatContent.getLabCombo().getValue();
            if (lab == null) {
                alatContent.resetPrikaz();
                return;
            }
            try {
                List<AlatGrupaDTO> grupe =
                        resursiDAO.getAlatPoLaboratoriji(lab.getIdLaboratorija());
                alatContent.prikaziGrupe(grupe);
            } catch (Exception ex) {
                prikaziGrešku("Greška pri učitavanju alata", ex.getMessage());
            }
        });

        // ── Klikne se tip alata → puni desnu listu ────────────────
        alatContent.setOnGrupaSelected(grupa -> {
            LaboratorijaDTO lab = alatContent.getLabCombo().getValue();
            if (lab == null) return;
            try {
                List<AlatDetaljiDTO> detalji =
                        resursiDAO.getDetaljiAlata(
                                grupa.getIdTipAlata(),
                                lab.getIdLaboratorija());
                alatContent.prikaziDetalje(detalji);
            } catch (Exception ex) {
                prikaziGrešku("Greška pri učitavanju detalja", ex.getMessage());
            }
        });

        alatContent.setOnDodajAlat(() -> {
            LaboratorijaDTO lab = alatContent.getOdabranaLaboratorija();
            DodajAlatView dodajView = new DodajAlatView(lab.getNaziv());
            new DodajAlatController(dodajView, resursiDAO, lab, () -> {
                // osvezi levu tabelu nakon dodavanja
                try {
                    alatContent.prikaziGrupe(
                            resursiDAO.getAlatPoLaboratoriji(lab.getIdLaboratorija()));
                } catch (Exception ex) {
                    prikaziGrešku("Greška pri osvežavanju", ex.getMessage());
                }
            });
            dodajView.show(mainView.getStage());
        });

        // ── Toggle na konkretnom alatu → UPDATE u bazi ────────────
        alatContent.setOnToggleDetalj(detalj -> {
            try {
                resursiDAO.promeniStatusAlata(
                        detalj.getIdAlat(), detalj.isAktivan());
                alatContent.osvežiDetalj(detalj);
            } catch (Exception ex) {
                // vrati toggle u prethodno stanje
                detalj.setStatusAlata(
                        detalj.isAktivan() ? "na servisu" : "aktivan");
                alatContent.osvežiDetalj(detalj);
                prikaziGrešku("Greška pri promeni statusa", ex.getMessage());
            }
        });
        alatContent.setOnDodajAlat(() -> {
            LaboratorijaDTO lab = alatContent.getOdabranaLaboratorija();
            DodajAlatView dodajView = new DodajAlatView(lab.getNaziv());
            new DodajAlatController(dodajView, resursiDAO, lab, () -> {
                try {
                    alatContent.prikaziGrupe(
                            resursiDAO.getAlatPoLaboratoriji(lab.getIdLaboratorija()));
                } catch (Exception ex) {
                    prikaziGrešku("Greška pri osvežavanju", ex.getMessage());
                }
            });
            dodajView.show(mainView.getStage());
        });
    }

    // ══════════════════════════════════════════════════════════════
    //  EDIT PROZORI
    // ══════════════════════════════════════════════════════════════


    private void otvoriEditLab() {
        LaboratorijaView labView = new LaboratorijaView();
        new LaboratorijaController(labView, () -> {});
        labView.show(mainView.getStage(), loggedInUser);
    }

    private void otvoriEditExp() {
        if (eksperimentContent == null) {
            eksperimentContent    = new EksperimentContent();
            eksperimentController = new EksperimentController(
                    eksperimentContent, mainView.getStage(), loggedInUser);
        }
        mainView.switchCenter(eksperimentContent.build());
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