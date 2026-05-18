package org.baze.neuronauka.controller;

import org.baze.neuronauka.dao.SesijaDAO;
import org.baze.neuronauka.model.HitanResursDTO;
import org.baze.neuronauka.model.IzvodjenjeDTO;
import org.baze.neuronauka.model.SesijaResursDTO;
import org.baze.neuronauka.view.NovaSesijaView;

import java.sql.SQLException;
import java.time.LocalTime;
import java.util.List;

public class NovaSesijaController {

    private final NovaSesijaView view;
    private final SesijaDAO      dao = new SesijaDAO();
    private final Runnable       onSaveSuccess;


    // ─────────────────────────────────────────────────────────────
    public NovaSesijaController(NovaSesijaView view, Runnable onSaveSuccess) {
        this.view          = view;
        this.onSaveSuccess = onSaveSuccess;
        initActions();
    }

    // ─────────────────────────────────────────────────────────────
    private void initActions() {
        view.getSaveBtn().setOnAction(e -> handleSave());

        // ── puni resurse kad se odabere izvodjenje ────────────────
        view.getIzvodjenjeBox().setOnAction(e -> {
            IzvodjenjeDTO izv = view.getIzvodjenjeBox().getValue();
            if (izv == null) {
                view.getResuriListView().getItems().clear();
                return;
            }
            List<SesijaResursDTO> resursi = dao.getResuriZaIzvodjenje(izv.getIdIzvodjenje());
            view.getResuriListView().getItems().setAll(resursi);
        });
    }

    private void setStatus(String tekst, boolean jeGreska) {
        view.getStatusLabel().setText(tekst);
        view.getStatusLabel().setStyle(
                "-fx-font-size: 12px; -fx-wrap-text: true;" +
                        (jeGreska ? "-fx-text-fill: #cc3333;" : "-fx-text-fill: #2e7d32;")
        );
    }

    // ─────────────────────────────────────────────────────────────
    // SAVE LOGIKA
    // ─────────────────────────────────────────────────────────────
    private void handleSave() {
        setStatus("", false);

        // čitaj vrednosti
        var datum              = view.getDatumPicker().getValue();
        int pocHH              = view.getPocetakHH().getValue();
        int pocMM              = view.getPocetakMM().getValue();
        int krajHH             = view.getKrajHH().getValue();
        int krajMM             = view.getKrajMM().getValue();
        String tip             = view.getTipSesijeBox().getValue();
        IzvodjenjeDTO izv      = view.getIzvodjenjeBox().getValue();

        // validacija
        if (izv == null) {
            setStatus("Izvodjenje nije odabrano.", true);
            return;
        }
        if (datum == null) {
            setStatus("Datum nije odabran.", true);
            return;
        }
        if (tip == null || tip.isBlank()) {
            setStatus("Tip sesije nije odabran.", true);
            return;
        }

        LocalTime pocetak = LocalTime.of(pocHH, pocMM);
        LocalTime kraj    = LocalTime.of(krajHH, krajMM);

        if (!kraj.isAfter(pocetak)) {
            setStatus("Vreme kraja mora biti posle vremena početka.", true);
            return;
        }

        // INSERT
        try {
            // 1. proveri ima li resursa u izvodjenju pre inserta
            List<HitanResursDTO> fali = dao.proveriResurseIzvodjenja(izv.getIdIzvodjenje());

            if (!fali.isEmpty()) {
                StringBuilder sb = new StringBuilder("Sesija nije dodata. Nedostaju resursi:\n");
                for (HitanResursDTO h : fali) {
                    sb.append("• ").append(h.getNaziv()).append("\n");
                }
                setStatus(sb.toString(), true);
                return;
            }

            // 2. ima sve — dodaj sesiju
            int idSesija = dao.insertSesijaVratiId(datum, pocetak, kraj, tip, izv.getIdIzvodjenje());

            dao.azurirajStatusAkoJePrvaSesija(izv.getIdIzvodjenje());
            // 3. upiši utrošak za svaki resurs
            for (SesijaResursDTO r : view.getResuriListView().getItems()) {
                if (r.getKolicina() > 0) {
                    dao.zabeležiUtrosak(idSesija, r.getIdResurs(), r.getKolicina());
                }
            }

            // 4. pozovi proceduru koja oduzima resurse
            dao.pokreniZabeležiUtrosak(idSesija);

            setStatus("Sesija dodata.", false);
            onSaveSuccess.run();
            view.getDialogStage().close();

        } catch (SQLException ex) {
            String msg = ex.getMessage();
            if (msg != null && msg.toLowerCase().contains("overlap")) {
                setStatus("Sesija se preklapa sa drugom sesijom u ovoj laboratoriji.", true);
            } else {
                setStatus("Greška baze: " + msg, true);
            }
        }
    }



}