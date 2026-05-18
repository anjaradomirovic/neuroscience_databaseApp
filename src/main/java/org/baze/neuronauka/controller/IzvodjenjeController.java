package org.baze.neuronauka.controller;

import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.baze.neuronauka.dao.IzvodjenjeDAO;
import org.baze.neuronauka.model.IzvodjenjeDTO;
import org.baze.neuronauka.view.IzvodjenjeContent;
import org.baze.neuronauka.view.NovoIzvodjenjeFormView;

import java.sql.SQLException;
import java.util.List;

public class IzvodjenjeController {

    private final IzvodjenjeContent content;
    private final IzvodjenjeDAO     dao   = new IzvodjenjeDAO();
    private final Stage             stage;

    public IzvodjenjeController(IzvodjenjeContent content, Stage stage) {
        this.content = content;
        this.stage   = stage;
        init();
    }

    // ── INICIJALIZACIJA ───────────────────────────────────────────

    private void init() {
        content.setEksperimentiZaFilter(dao.getSviEksperimentiZaFilter());
        ucitajTabelu();

        content.getEkspFilter().setOnAction(e   -> ucitajTabelu());
        content.getStatusFilter().setOnAction(e -> ucitajTabelu());

        // selekcija — detalji + izvodjači sa X dugmetom
        content.setOnSelekt(dto -> osveziDesniPanel(dto.getIdIzvodjenje()));

        // čuvanje statusa
        content.setOnSacuvajStatus(dto -> {
            try {
                dao.izmeniStatus(dto.getIdIzvodjenje(), dto.getStatus());
                ucitajTabelu();
                osveziDesniPanel(dto.getIdIzvodjenje());
            } catch (SQLException ex) {
                prikaziGrešku("Greška pri izmeni statusa", ex.getMessage());
            }
        });

        content.setOnNovo(this::otvoriNovoIzvodjenje);

        // punjenje combo-a izvodjača
        content.getDodajIzvodjacaCombo().getItems().setAll(dao.getSviIstrazivaci());

        // dodavanje izvodjača
        content.getDodajIzvodjacaBtn().setOnAction(e -> {
            IzvodjenjeDTO sel = content.getTabela().getSelectionModel().getSelectedItem();
            String[] odabran  = content.getDodajIzvodjacaCombo().getValue();
            if (sel == null || odabran == null) return;
            try {
                dao.dodajIzvodjaca(
                        sel.getIdIzvodjenje(),
                        Integer.parseInt(odabran[0]),
                        content.getOpisUloge().getText().trim()
                );
                content.getOpisUloge().clear();
                osveziDesniPanel(sel.getIdIzvodjenje());
            } catch (SQLException ex) {
                prikaziGrešku("Greška pri dodavanju izvodjača", ex.getMessage());
            }
        });
    }

    // novi helper koji osvežava ceo desni panel i zakači X dugmad
    private void osveziDesniPanel(int idIzvodjenje) {
        IzvodjenjeDTO dto = dao.getDetalji(idIzvodjenje);
        if (dto == null) return;
        // prikaziDetalje resetuje izvodjacBox — prikaziIzvodjace ga ponovo puni
        content.prikaziDetalje(dto);
        // ← ovo mora biti POSLE prikaziDetalje
        content.prikaziIzvodjace(dto.getIzvodjaci(), idIstr -> {
            try {
                dao.ukloniIzvodjaca(idIzvodjenje, idIstr);
                osveziDesniPanel(idIzvodjenje);
            } catch (SQLException ex) {
                prikaziGrešku("Greška pri uklanjanju izvodjača", ex.getMessage());
            }
        });
    }


    // ── UČITAVANJE TABELE ─────────────────────────────────────────

    private void ucitajTabelu() {
        String[] odabraniEksp   = content.getEkspFilter().getValue();
        String   odabraniStatus = content.getStatusFilter().getValue();

        Integer idEksp = odabraniEksp != null
                ? Integer.parseInt(odabraniEksp[0]) : null;

        List<IzvodjenjeDTO> sva = dao.getSvaIzvodjenja(idEksp);

        // klijentski filter po statusu (malo redova, nema potrebe za dodatnim SQL-om)
        if (odabraniStatus != null) {
            sva = sva.stream()
                    .filter(d -> odabraniStatus.equals(d.getStatus()))
                    .collect(java.util.stream.Collectors.toList());
        }

        content.setIzvodjenja(sva);
    }

    // ── NOVO IZVODJENJE ───────────────────────────────────────────

    private void otvoriNovoIzvodjenje() {
        NovoIzvodjenjeFormView forma = new NovoIzvodjenjeFormView();
        forma.setEksperimenti(dao.getSviEksperimentiZaFilter());
        forma.setLaboratorije(dao.getSveLaboratorije());

        forma.getSacuvajBtn().setOnAction(e -> {
            String[] eksp = forma.getOdabraniEksp();
            String[] lab  = forma.getOdabranaLab();
            java.time.LocalDate datum = forma.getDatum();

            if (eksp == null || lab == null || datum == null) {
                prikaziGrešku("Nepotpuni podaci",
                        "Odaberite eksperiment, laboratoriju i datum.");
                return;
            }

            try {
                String[] rezultat = dao.kreirajIzvodjenje(
                        Integer.parseInt(eksp[0]),
                        Integer.parseInt(lab[0]),
                        datum);

                forma.close();
                ucitajTabelu();

                // obavesti korisnika o statusu koji je procedura postavila
                String poruka = "Izvodjenje #" + rezultat[0] +
                        " kreirano sa statusom: " + rezultat[1];
                if ("odlozeno".equals(rezultat[1])) {
                    poruka += "\n\nLaboratorija nema dovoljno resursa — " +
                            "izvodjenje je postavljeno kao odloženo.";
                }
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Izvodjenje kreirano");
                info.setHeaderText(null);
                info.setContentText(poruka);
                info.showAndWait();

            } catch (SQLException ex) {
                prikaziGrešku("Greška pri kreiranju izvodjenja", ex.getMessage());
            }
        });

        forma.show(stage);
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