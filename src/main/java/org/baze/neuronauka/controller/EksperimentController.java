package org.baze.neuronauka.controller;

import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.baze.neuronauka.dao.EksperimentDAO;
import org.baze.neuronauka.model.EksperimentDTO;
import org.baze.neuronauka.model.EksperimentDTO.EksperimentAlatStavka;
import org.baze.neuronauka.model.EksperimentDTO.EksperimentResursStavka;
import org.baze.neuronauka.view.EksperimentContent;
import org.baze.neuronauka.view.EksperimentContent.EksperimentRow;
import org.baze.neuronauka.view.EksperimentFormView;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class EksperimentController {

    private final EksperimentContent content;
    private final EksperimentDAO     dao = new EksperimentDAO();
    private final String             loggedInUser;
    private final Stage              stage;
    private int odabraniEksperimentId = -1;

    public EksperimentController(EksperimentContent content, Stage stage, String loggedInUser) {
        this.content      = content;
        this.stage        = stage;
        this.loggedInUser = loggedInUser;
        ucitajTabelu();
        initActions();
    }

    // ── INICIJALIZACIJA ───────────────────────────────────────────

    private void ucitajTabelu() {
        List<EksperimentDTO> svi = dao.getSviEksperimenti();
        List<EksperimentRow> redovi = svi.stream()
                .map(d -> new EksperimentRow(
                        String.valueOf(d.getIdEksperiment()),
                        d.getNaziv()           != null ? d.getNaziv()           : "",
                        d.getTipEksperimenta() != null ? d.getTipEksperimenta() : "",
                        d.getMetodIzvodjenja() != null ? d.getMetodIzvodjenja() : "",
                        d.getOpis()            != null ? d.getOpis()            : "",
                        d.isEtickyOdobren(),
                        d.getDizajneriStr()         != null ? d.getDizajneriStr()        : "",  // NOVO
                        d.getTeorijskiOkvirStr()    != null ? d.getTeorijskiOkvirStr()   : ""   // NOVO
                ))
                .collect(Collectors.toList());
        content.setEksperimenti(redovi);
    }

    private void initActions() {
        content.getDodajBtn().setOnAction(e -> otvoriDodaj());
        content.getEditBtn().setOnAction(e  -> otvoriEdit());

        // selekcija reda — punjenje dizajnera i teorijskog okvira
        content.getTabela().getSelectionModel().selectedItemProperty()
                .addListener((obs, old, sel) -> {
                    if (sel == null) return;
                    odabraniEksperimentId = Integer.parseInt(sel.idEksperiment());
                    // Controller puni detalje
                    content.prikaziDetalje(sel);  // ← dodaj ovo
                    osveziDizajnereITeoriju(odabraniEksperimentId);
                });

        // teorijski okvir — automatski čuvaj na promenu
        content.getTeorijskiOkvirCombo().setOnAction(e -> {
            if (odabraniEksperimentId < 0) return;
            String[] val = content.getTeorijskiOkvirCombo().getValue();
            if (val == null) return;
            try {
                dao.postaviTeorijskiOkvir(odabraniEksperimentId, Integer.parseInt(val[0]));
            } catch (SQLException ex) { ex.printStackTrace(); }
        });

        // dodaj dizajnera
        content.getDodajDizajneraBtn().setOnAction(e -> {
            if (odabraniEksperimentId < 0) {
                prikaziGrešku("Nije odabran eksperiment", "Odaberite eksperiment iz tabele.");
                return;
            }
            String[] valDiz = content.getDodajDizajneraCombo().getValue();
            String[] valTeo = content.getTeorijskiOkvirCombo().getValue();
            if (valDiz == null) {
                prikaziGrešku("Nije odabran dizajner", "Odaberite dizajnera iz padajuće liste.");
                return;
            }
            if (valTeo == null) {
                prikaziGrešku("Nije odabran teorijski okvir", "Odaberite teorijski okvir pre dodavanja dizajnera.");
                return;
            }
            try {
                dao.dodajDizajnera(
                        odabraniEksperimentId,
                        Integer.parseInt(valDiz[0]),
                        Integer.parseInt(valTeo[0])  // ← prosledi teorijski okvir
                );
                osveziDizajnereITeoriju(odabraniEksperimentId);
            } catch (SQLException ex) {
                prikaziGrešku("Greška pri dodavanju dizajnera", ex.getMessage());
            }
        });

        // punjenje dropdown-ova
        punjDropdownove();
    }

    private void punjDropdownove() {
        content.getTeorijskiOkvirCombo().getItems().setAll(dao.getSveTeorijeZaDropdown());
        content.getDodajDizajneraCombo().getItems().setAll(dao.getSviDizajneriZaDropdown());
    }
    private void osveziDizajnereITeoriju(int idEksperiment) {
        List<String[]> dizajneri = dao.getDizajneriZaPrikaz(idEksperiment);
        content.prikaziDizajnere(dizajneri,
                // klik na ime → postavi kao glavnog
                (idDiz, ignored) -> {
                    try {
                        dao.postaviGlavnogDizajnera(idEksperiment, Integer.parseInt(idDiz));
                        osveziDizajnereITeoriju(idEksperiment);
                    } catch (SQLException ex) { ex.printStackTrace(); }
                },
                // klik na X → ukloni
                idDiz -> {
                    try {
                        dao.ukloniDizajnera(idEksperiment, Integer.parseInt(idDiz));
                        osveziDizajnereITeoriju(idEksperiment);
                    } catch (SQLException ex) { ex.printStackTrace(); }
                }
        );
    }

    // ── DODAJ ─────────────────────────────────────────────────────

    private void otvoriDodaj() {
        EksperimentFormView forma = new EksperimentFormView();
        punjComboKataloge(forma);

        forma.getSacuvajBtn().setOnAction(e -> {
            EksperimentDTO dto = skupiDTO(forma, -1);
            if (dto == null) return;
            try {
                dao.dodajEksperiment(dto);
                forma.close();
                ucitajTabelu();
            } catch (SQLException ex) {
                prikaziGrešku("Greška pri dodavanju eksperimenta", ex.getMessage());
            }
        });

        forma.showDodaj(stage);
    }

    // ── EDIT ──────────────────────────────────────────────────────

    private void otvoriEdit() {
        EksperimentRow sel = content.getOdabrani();
        if (sel == null) return;

        int id = Integer.parseInt(sel.idEksperiment());
        EksperimentDTO dto = dao.getEksperimentDetalji(id);
        if (dto == null) return;

        EksperimentFormView forma = new EksperimentFormView();
        punjComboKataloge(forma);

        forma.showEdit(stage,
                dto.getIdEksperiment(),
                dto.getNaziv(),
                dto.getOpis(),
                dto.getTipEksperimenta(),
                dto.getMetodIzvodjenja(),
                dto.isEtickyOdobren());

        // POSLE showEdit — reset() je već prošao, sad punimo tabele
        forma.setResurseZaEdit(dto.getResurei());
        forma.setAlateZaEdit(dto.getAlati());

        forma.getSacuvajBtn().setOnAction(e -> {
            EksperimentDTO izmenjeni = skupiDTO(forma, id);
            if (izmenjeni == null) return;
            try {
                dao.izmeniEksperiment(izmenjeni);
                forma.close();
                ucitajTabelu();
            } catch (SQLException ex) {
                prikaziGrešku("Greška pri izmeni eksperimenta", ex.getMessage());
            }
        });
    }

    // ── POMOĆNE METODE ────────────────────────────────────────────

    private void punjComboKataloge(EksperimentFormView forma) {
        try {
            forma.getResursCombo().getItems().setAll(dao.getSviResurei());
            forma.getAlatCombo().getItems().setAll(dao.getSviAlati());
        } catch (RuntimeException ex) {
            prikaziGrešku("Greška pri učitavanju kataloga", ex.getMessage());
        }
    }

    private EksperimentDTO skupiDTO(EksperimentFormView forma, int id) {
        String naziv = forma.getNazivField().getText().trim();
        if (naziv.isEmpty()) {
            prikaziGrešku("Naziv je obavezan", "Unesite naziv eksperimenta.");
            return null;
        }

        EksperimentDTO dto = new EksperimentDTO();
        if (id > 0) dto.setIdEksperiment(id);
        dto.setNaziv(naziv);
        dto.setOpis(forma.getOpisField().getText().trim());
        dto.setTipEksperimenta(forma.getTipCombo().getValue());
        dto.setMetodIzvodjenja(forma.getMetodCombo().getValue());
        dto.setEtickyOdobren(forma.getEtickiCheck().isSelected());

        List<EksperimentResursStavka> resursi = forma.getOdabraneResurse().stream()
                .map(r -> new EksperimentResursStavka(
                        r.getIdResurs(), r.getNaziv(), r.getJedinica(), r.getKolicina()))
                .collect(Collectors.toList());
        dto.setResurei(resursi);

        List<EksperimentAlatStavka> alati = forma.getOdabraneAlate().stream()
                .map(a -> new EksperimentAlatStavka(
                        a.getIdAlat(), a.getNaziv(), a.getKolicina()))
                .collect(Collectors.toList());
        dto.setAlati(alati);

        if (id > 0) {
            EksperimentDTO postojeci = dao.getEksperimentDetalji(id);
            if (postojeci != null) {
                dto.setDizajneri(postojeci.getDizajneri());
            }
        } else {
            dto.setDizajneri(new java.util.ArrayList<>());
        }
        return dto;
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