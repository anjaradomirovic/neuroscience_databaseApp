package org.baze.neuronauka.controller;

import javafx.scene.Node;
import javafx.stage.Stage;
import org.baze.neuronauka.dao.IzvodjenjeDAO;
import org.baze.neuronauka.dao.ResursiDAO;
import org.baze.neuronauka.dao.SesijaDAO;
import org.baze.neuronauka.model.AlatDetaljiDTO;
import org.baze.neuronauka.model.AlatGrupaDTO;
import org.baze.neuronauka.model.LaboratorijaDTO;
import org.baze.neuronauka.model.SesijaDTO;
import org.baze.neuronauka.view.*;
import javafx.scene.layout.VBox;
import org.baze.neuronauka.view.DodajAlatView;
import org.baze.neuronauka.view.IzvodjenjeContent;

import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SesijaController {

    private final SesijaView view;
    private final SesijaDAO  dao = new SesijaDAO();
    private String loggedInUser  = "";
    private Stage  stage;

    // ── čuvamo ResursiContent da controller ne gubi state ──────────
    private ResursiContent    resursiContent;
    private ResursiController resursiController;

    private EksperimentContent    eksperimentContent;
    private EksperimentController eksperimentController;

    public SesijaController(SesijaView view, String loggedInUser) {
        this.view         = view;
        this.loggedInUser = loggedInUser;
        ucitajDropdownove();
        initActions();
        osvežiKalendar();
    }

    public void reinitNavActions() {
        if (stage == null && view.getPrevMonthBtn().getScene() != null) {
            stage = (Stage) view.getPrevMonthBtn().getScene().getWindow();
        }
        initActions();
        osvežiKalendar();
    }
    // ── INICIJALIZACIJA ────────────────────────────────────────────

    private void ucitajDropdownove() {
        String odabranaLab = view.getLabFilter().getValue();
        view.getLabFilter().getItems().clear();
        view.getLabFilter().getItems().add(null);
        view.getLabFilter().getItems().addAll(dao.getLaboratorije());
        if (odabranaLab != null && view.getLabFilter().getItems().contains(odabranaLab))
            view.getLabFilter().setValue(odabranaLab);
        else
            view.getLabFilter().getSelectionModel().selectFirst();

        String odabraniExp = view.getExpFilter().getValue();
        view.getExpFilter().getItems().clear();
        view.getExpFilter().getItems().add(null);
        view.getExpFilter().getItems().addAll(dao.getEksperimenti());
        if (odabraniExp != null && view.getExpFilter().getItems().contains(odabraniExp))
            view.getExpFilter().setValue(odabraniExp);
        else
            view.getExpFilter().getSelectionModel().selectFirst();
    }

    private void initActions() {

        // ── Sesija menu ────────────────────────────────────────────
        view.getPregledSesijaItem().setOnAction(e -> prikaziSesije());
        view.getNovaSesijaItem().setOnAction(e    -> otvoriNovuSesiju());

        // ── Navigacija kalendarom ──────────────────────────────────
        view.getPrevMonthBtn().setOnAction(e -> {
            view.setCurrentYearMonth(view.getCurrentYearMonth().minusMonths(1));
            osvežiKalendar();
        });
        view.getNextMonthBtn().setOnAction(e -> {
            view.setCurrentYearMonth(view.getCurrentYearMonth().plusMonths(1));
            osvežiKalendar();
        });
        view.getYearSpinner().valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            view.setCurrentYearMonth(YearMonth.of(newVal, view.getCurrentYearMonth().getMonth()));
            osvežiKalendar();
        });

        // ── Sidebar filteri ────────────────────────────────────────
        view.getLabFilter().setOnAction(e -> osvežiKalendar());
        view.getExpFilter().setOnAction(e -> osvežiKalendar());

        // ── Laboratorija menu ──────────────────────────────────────
        view.getEditLabItem().setOnAction(e   -> otvoriUpravljanjeLaboratorijama());
        view.getResuriLabItem().setOnAction(e -> prikaziResurse());
        view.getAlatLabItem().setOnAction(e -> prikaziAlat());

        // ── Eksperiment menu ───────────────────────────────────────
        // ── Eksperiment menu ───────────────────────────────────────────
        view.getEditExpItem().setOnAction(e -> otvoriUpravljanjeEksperimentima());
        view.getStatsExpItem().setOnAction(e -> {
            StatistikaContent sc = new StatistikaContent();
            new StatistikaController(sc);
            view.switchCenter(sc.build());
        });

        view.getIzvodjenjeExpItem().setOnAction(e -> {
            if (stage == null) {
                stage = (Stage) view.getSesijaMenuBtn().getScene().getWindow();
            }
            IzvodjenjeContent ic = new IzvodjenjeContent();
            new IzvodjenjeController(ic, stage);
            view.switchCenter(ic.build());
        });
    }

    // ── NAVIGACIJA IZMEĐU PRIKAZA ──────────────────────────────────

    private void prikaziSesije() {
        view.setActiveView("sesije");
        view.switchCenter(view.buildSesijaCenter());
        initActions();
        osvežiKalendar();
    }

    private void prikaziResurse() {
        if (resursiContent == null) {
            resursiContent    = new ResursiContent();
            resursiController = new ResursiController(
                    resursiContent,
                    loggedInUser,
                    this::prikaziSesije
            );
        } else {
            // osvezi laboratorije svaki put kada se otvori ekran
            resursiController.osveziLaboratorije();
        }
        Node resursiNode = resursiContent.build();
        view.setActiveView("resursi");
        view.switchCenter(resursiNode);
    }
    private AlatContent  alatContent;

    private void prikaziAlat() {
        if (alatContent == null) {
            alatContent = new AlatContent();
            initAlatActions();
        }
        ResursiDAO resursiDAO = new ResursiDAO();
        alatContent.setLaboratorije(resursiDAO.getSveLaboratorije());
        view.setActiveView("alat");
        view.switchCenter(alatContent.build());
    }

    private void initAlatActions() {
        ResursiDAO resursiDAO = new ResursiDAO();

        // Bira se laboratorija → puni levu tabelu
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
                // tiha greška — možeš dodati Alert ako želiš
            }
        });

        // Klikne se tip alata → puni desnu listu
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
                // tiha greška
            }
        });

        // Toggle na konkretnom alatu → UPDATE u bazi
        alatContent.setOnToggleDetalj(detalj -> {
            try {
                resursiDAO.promeniStatusAlata(detalj.getIdAlat(), detalj.isAktivan());
                alatContent.osvežiDetalj(detalj);
            } catch (Exception ex) {
                // vrati u prethodno stanje
                detalj.setStatusAlata(detalj.isAktivan() ? "na servisu" : "aktivan");
                alatContent.osvežiDetalj(detalj);
            }
        });
        alatContent.setOnDodajAlat(() -> {
            LaboratorijaDTO lab = alatContent.getOdabranaLaboratorija();
            ResursiDAO reDAO = new ResursiDAO();
            DodajAlatView dodajView = new DodajAlatView(lab.getNaziv());
            new DodajAlatController(dodajView, reDAO, lab, () -> {
                try {
                    alatContent.prikaziGrupe(
                            reDAO.getAlatPoLaboratoriji(lab.getIdLaboratorija()));
                } catch (Exception ex) {
                    // tiha greška
                }
            });
            dodajView.show(stage);
        });


    }



    // ── OTVARANJE PROZORA ──────────────────────────────────────────

    private void otvoriNovuSesiju() {
        IzvodjenjeDAO  izvDao   = new IzvodjenjeDAO();
        NovaSesijaView novaView = new NovaSesijaView();
        new NovaSesijaController(novaView, this::osvežiKalendar);
        novaView.show(stage, izvDao.getAktivnaIzvodjenja());
    }

    private void otvoriUpravljanjeLaboratorijama() {
        LaboratorijaView labView = new LaboratorijaView();
        new LaboratorijaController(labView, this::ucitajDropdownove);
        labView.show(stage, loggedInUser);
    }

    private void otvoriUpravljanjeEksperimentima() {
        if (eksperimentContent == null) {
            eksperimentContent    = new EksperimentContent();
            eksperimentController = new EksperimentController(
                    eksperimentContent, stage, loggedInUser);
        }
        view.switchCenter(eksperimentContent.build());
    }

    // ── GLAVNI REFRESH ─────────────────────────────────────────────

    private void osvežiKalendar() {
        String odabranaLab = view.getLabFilter().getValue();
        String odabraniExp = view.getExpFilter().getValue();

        List<SesijaDTO> sesije = dao.getSesijeFiltered(odabranaLab, odabraniExp);

        if (!sesije.isEmpty()) {
            YearMonth trenutni = view.getCurrentYearMonth();
            boolean imaUTrenutnom = sesije.stream()
                    .anyMatch(s -> YearMonth.from(s.getDatumSesije()).equals(trenutni));
            if (!imaUTrenutnom) {
                sesije.stream()
                        .map(s -> YearMonth.from(s.getDatumSesije()))
                        .max(Comparator.naturalOrder())
                        .ifPresent(view::setCurrentYearMonth);
            }
        }

        renderKalendar(sesije);
        renderSidebar(sesije);
    }

    // ── RENDERING KALENDARA ────────────────────────────────────────

    private void renderKalendar(List<SesijaDTO> sveSesjeSaFiltera) {
        view.clearCalendarGrid();

        YearMonth ym    = view.getCurrentYearMonth();
        LocalDate danas = LocalDate.now();
        int ukupnoDana  = ym.lengthOfMonth();
        int offset      = ym.atDay(1).getDayOfWeek().getValue() - 1;

        List<SesijaDTO> ovogMeseca = sveSesjeSaFiltera.stream()
                .filter(s -> YearMonth.from(s.getDatumSesije()).equals(ym))
                .collect(Collectors.toList());

        for (int i = 0; i < offset; i++)
            view.addCellToGrid(view.makeEmptyCell(), i, 0);

        int col = offset, row = 0;
        for (int dan = 1; dan <= ukupnoDana; dan++) {
            LocalDate datum = ym.atDay(dan);
            boolean jeToday = datum.equals(danas);
            VBox cell       = view.makeDayCell(dan, jeToday);

            ovogMeseca.stream()
                    .filter(s -> s.getDatumSesije().equals(datum))
                    .sorted(Comparator.comparing(SesijaDTO::getVremePocetka))
                    .forEach(s -> {
                        int trajMin = (int) Duration.between(
                                s.getVremePocetka(), s.getVremeKraja()).toMinutes();
                        VBox chip = view.makeSesijaChip(
                                s.getIdSesija(),
                                s.getVremePocetka().toString(),
                                trajMin,
                                s.getEksperiment(),
                                s.getTipSesije()
                        );
                        chip.setOnMouseClicked(e -> otvoriIzmenu(s));
                        cell.getChildren().add(chip);
                    });

            view.addCellToGrid(cell, col, row);
            col++;
            if (col == 7) { col = 0; row++; }
        }
    }

    // ── RENDERING SIDEBARA ─────────────────────────────────────────

    private void renderSidebar(List<SesijaDTO> sveSesjeSaFiltera) {
        view.getSesijaListView().getItems().clear();
        YearMonth ym = view.getCurrentYearMonth();

        List<SesijaDTO> ovogMeseca = sveSesjeSaFiltera.stream()
                .filter(s -> YearMonth.from(s.getDatumSesije()).equals(ym))
                .collect(Collectors.toList());

        if (ovogMeseca.isEmpty()) {
            view.getSesijaListView().getItems().add("Nema eksperimenata za ovaj mesec.");
            return;
        }

        ovogMeseca.stream()
                .map(SesijaDTO::getEksperiment)
                .distinct()
                .sorted()
                .forEach(eksperiment -> {
                    String idSesija = ovogMeseca.stream()
                            .filter(s -> eksperiment.equals(s.getEksperiment()))
                            .sorted(Comparator.comparing(SesijaDTO::getDatumSesije)
                                    .thenComparing(SesijaDTO::getVremePocetka))
                            .map(s -> String.valueOf(s.getIdSesija()))
                            .collect(Collectors.joining(", "));
                    view.getSesijaListView().getItems()
                            .add(eksperiment + "  (sesije: " + idSesija + ")");
                });
    }

    // ── IZMENA SESIJE ──────────────────────────────────────────────


    private void otvoriIzmenu(SesijaDTO s) {
        SesijaIzmenaView izmenaView = new SesijaIzmenaView();
        izmenaView.getIdLabel().setText("ID sesije: " + s.getIdSesija());
        izmenaView.getEksperimentLabel().setText(s.getEksperiment());
        izmenaView.getLaboratorijaLabel().setText(s.getLaboratorija());
        izmenaView.getDatumPicker().setValue(s.getDatumSesije());
        izmenaView.getPocetakHH().getValueFactory().setValue(s.getVremePocetka().getHour());
        izmenaView.getPocetakMM().getValueFactory().setValue(s.getVremePocetka().getMinute());
        izmenaView.getKrajHH().getValueFactory().setValue(s.getVremeKraja().getHour());
        izmenaView.getKrajMM().getValueFactory().setValue(s.getVremeKraja().getMinute());
        izmenaView.getTipSesijeBox().setValue(s.getTipSesije());
        new SesijaIzmenaController(izmenaView, s, this::osvežiKalendar);
        izmenaView.show(stage);
    }
}