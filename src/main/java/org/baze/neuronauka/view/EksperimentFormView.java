package org.baze.neuronauka.view;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.baze.neuronauka.model.EksperimentDTO.EksperimentAlatStavka;
import org.baze.neuronauka.model.EksperimentDTO.EksperimentResursStavka;

import java.util.List;

/**
 * Modalni dijalog — dodavanje i izmena eksperimenta.
 *
 * Tab 1 "Osnovni podaci"  – naziv, opis, tip (dropdown), metod (dropdown), etičko odobrenje
 * Tab 2 "Resursi i alati" – odabir iz combo-a + količina + lista (zasebno za resurse i alate)
 */
public class EksperimentFormView {

    // ── konstante ─────────────────────────────────────────────────
    private static final List<String> TIPOVI = List.of(
            "EEG", "fMRI", "Behavioral", "Cognitive", "Memory Test",
            "Perception Test", "Reaction Time", "Sleep Study",
            "Neurostimulation", "Brain Mapping", "Neuroimaging",
            "Decision Making Study"
    );
    private static final List<String> METODI = List.of(
            "IN_VIVO", "IN_VITRO", "IN_SILICO", "EX_VIVO", "HUMAN_SUBJECT"
    );

    // ── polja — osnovno ───────────────────────────────────────────
    private final Label              idLabel     = new Label();
    private final TextField          nazivField  = new TextField();
    private final TextArea           opisField   = new TextArea();
    private final ComboBox<String>   tipCombo    = new ComboBox<>();
    private final ComboBox<String>   metodCombo  = new ComboBox<>();
    private final CheckBox           etickiCheck = new CheckBox("Etički odobren");

    // ── resursi ───────────────────────────────────────────────────
    private final ComboBox<EksperimentResursStavka> resursCombo   = new ComboBox<>();
    private final Spinner<Double>  resursKol     = new Spinner<>(0.0, 99999.0, 1.0, 0.5);
    private final Button           dodajResBtn   = new Button("+ Dodaj");
    private final Button           ukloniResBtn  = new Button("Ukloni");
    private final TableView<ResursRed> resTabela = new TableView<>();
    private final ObservableList<ResursRed> resStavke = FXCollections.observableArrayList();

    // ── alati ─────────────────────────────────────────────────────
    private final ComboBox<EksperimentAlatStavka> alatCombo     = new ComboBox<>();
    private final Spinner<Integer> alatKol       = new Spinner<>(1, 9999, 1);
    private final Button           dodajAlatBtn  = new Button("+ Dodaj");
    private final Button           ukloniAlatBtn = new Button("Ukloni");
    private final TableView<AlatRed> alatTabela  = new TableView<>();
    private final ObservableList<AlatRed> alatStavke = FXCollections.observableArrayList();

    // ── dugmad ────────────────────────────────────────────────────
    private final Button sacuvajBtn = new Button();
    private final Button otkaziBtn  = new Button("Otkaži");

    private Stage dialog;

    // ═════════════════════════════════════════════════════════════
    //  JAVNI ULAZI
    // ═════════════════════════════════════════════════════════════

    public void showDodaj(Stage parentStage) {
        reset();
        sacuvajBtn.setText("Dodaj eksperiment");
        open(parentStage, "Novi eksperiment");
    }

    public void showEdit(Stage parentStage, int id, String naziv, String opis,
                         String tip, String metod, boolean eticki) {
        reset();
        idLabel.setText("ID: " + id);
        nazivField.setText(nvl(naziv));
        opisField.setText(nvl(opis));
        tipCombo.setValue(tip);
        metodCombo.setValue(metod);
        etickiCheck.setSelected(eticki);
        sacuvajBtn.setText("Sačuvaj izmene");
        open(parentStage, "Izmena eksperimenta");
    }

    private void reset() {
        idLabel.setText("");
        nazivField.clear();
        opisField.clear();
        tipCombo.setValue(null);
        metodCombo.setValue(null);
        etickiCheck.setSelected(false);
        resStavke.clear();
        alatStavke.clear();
    }

    // ═════════════════════════════════════════════════════════════
    //  GRADNJA UI
    // ═════════════════════════════════════════════════════════════

    private void open(Stage parentStage, String title) {
        dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(parentStage);
        dialog.setTitle(title);
        dialog.setResizable(true);
        dialog.setMinWidth(580);
        dialog.setMinHeight(500);

        opisField.setPrefRowCount(3);
        opisField.setWrapText(true);
        tipCombo.getItems().setAll(TIPOVI);
        tipCombo.setMaxWidth(Double.MAX_VALUE);
        metodCombo.getItems().setAll(METODI);
        metodCombo.setMaxWidth(Double.MAX_VALUE);
        resursKol.setEditable(true);
        resursKol.setPrefWidth(100);
        alatKol.setEditable(true);
        alatKol.setPrefWidth(100);

        TabPane tabPane = new TabPane(buildOsnovniTab(), buildResursiAlatTab());
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: white;");
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        VBox root = new VBox(0, tabPane, buildBtnRow());
        root.setStyle("-fx-background-color: white;");

        dialog.setScene(new Scene(root, 600, 600));
        dialog.show();
    }

    // ── TAB 1 – OSNOVNO ──────────────────────────────────────────

    private Tab buildOsnovniTab() {
        VBox forma = new VBox(12);
        forma.setPadding(new Insets(20));
        forma.setStyle("-fx-background-color: white;");
        if (!idLabel.getText().isEmpty()) forma.getChildren().add(idLabel);
        forma.getChildren().addAll(
                row("Naziv",  nazivField),
                row("Opis",   opisField),
                row("Tip",    tipCombo),
                row("Metod",  metodCombo),
                etickiCheck
        );
        return new Tab("Osnovni podaci", wrapScroll(forma));
    }

    // ── TAB 2 – RESURSI + ALATI ──────────────────────────────────

    private Tab buildResursiAlatTab() {
        VBox sadrzaj = new VBox(20,
                buildResursiPanel(),
                new Separator(),
                buildAlatiPanel());
        sadrzaj.setPadding(new Insets(16));
        sadrzaj.setStyle("-fx-background-color: white;");
        return new Tab("Resursi i alati", wrapScroll(sadrzaj));
    }

    // ── RESURSI panel ─────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private VBox buildResursiPanel() {
        resursCombo.setMaxWidth(Double.MAX_VALUE);
        resursCombo.setPromptText("Odaberi resurs…");
        // Prikazuje naziv + jedinicu u dropdownu
        resursCombo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(EksperimentResursStavka item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null
                        : item.getNaziv() + (item.getJedinica() != null
                        ? " (" + item.getJedinica() + ")" : ""));
            }
        });
        resursCombo.setButtonCell(resursCombo.getCellFactory().call(null));
        HBox.setHgrow(resursCombo, Priority.ALWAYS);

        styleDodaj(dodajResBtn);
        styleUkloni(ukloniResBtn);
        ukloniResBtn.setDisable(true);

        HBox addRow = new HBox(8, resursCombo,
                lbl("Količina:"), resursKol, dodajResBtn);
        addRow.setAlignment(Pos.CENTER_LEFT);

        // tabela
        TableColumn<ResursRed, String> cNaziv    = tCol("Resurs",   "naziv",    180);
        TableColumn<ResursRed, String> cJed      = tCol("Jedinica", "jedinica",  80);
        TableColumn<ResursRed, Double> cKol      = new TableColumn<>("Količina");
        cKol.setCellValueFactory(new PropertyValueFactory<>("kolicina"));
        cKol.setMinWidth(80);
        resTabela.getColumns().addAll(cNaziv, cJed, cKol);
        resTabela.setItems(resStavke);
        resTabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        resTabela.setPrefHeight(160);
        resTabela.setPlaceholder(new Label("Nema odabranih resursa."));
        resTabela.getSelectionModel().selectedItemProperty()
                .addListener((o, old, s) -> ukloniResBtn.setDisable(s == null));

        dodajResBtn.setOnAction(e -> {
            EksperimentResursStavka odabran = resursCombo.getValue();
            if (odabran == null) return;
            double kol = resursKol.getValue();
            resStavke.removeIf(r -> r.getIdResurs() == odabran.getIdResurs());
            resStavke.add(new ResursRed(odabran.getIdResurs(),
                    odabran.getNaziv(), odabran.getJedinica(), kol));
        });
        ukloniResBtn.setOnAction(e -> {
            ResursRed s = resTabela.getSelectionModel().getSelectedItem();
            if (s != null) resStavke.remove(s);
        });

        HBox tblBar = new HBox(ukloniResBtn);
        tblBar.setAlignment(Pos.CENTER_RIGHT);
        tblBar.setPadding(new Insets(4, 0, 2, 0));

        return new VBox(8, sectionLbl("Resursi"), addRow, tblBar, resTabela);
    }

    // ── ALATI panel ───────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private VBox buildAlatiPanel() {
        alatCombo.setMaxWidth(Double.MAX_VALUE);
        alatCombo.setPromptText("Odaberi alat…");
        alatCombo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(EksperimentAlatStavka item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNaziv());
            }
        });
        alatCombo.setButtonCell(alatCombo.getCellFactory().call(null));
        HBox.setHgrow(alatCombo, Priority.ALWAYS);

        styleDodaj(dodajAlatBtn);
        styleUkloni(ukloniAlatBtn);
        ukloniAlatBtn.setDisable(true);

        HBox addRow = new HBox(8, alatCombo,
                lbl("Količina:"), alatKol, dodajAlatBtn);
        addRow.setAlignment(Pos.CENTER_LEFT);

        // tabela
        TableColumn<AlatRed, String>  cNaziv = tCol("Alat",     "naziv",    220);
        TableColumn<AlatRed, Integer> cKol   = new TableColumn<>("Količina");
        cKol.setCellValueFactory(new PropertyValueFactory<>("kolicina"));
        cKol.setMinWidth(80);
        alatTabela.getColumns().addAll(cNaziv, cKol);
        alatTabela.setItems(alatStavke);
        alatTabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        alatTabela.setPrefHeight(150);
        alatTabela.setPlaceholder(new Label("Nema odabranih alata."));
        alatTabela.getSelectionModel().selectedItemProperty()
                .addListener((o, old, s) -> ukloniAlatBtn.setDisable(s == null));

        dodajAlatBtn.setOnAction(e -> {
            EksperimentAlatStavka odabran = alatCombo.getValue();
            if (odabran == null) return;
            int kol = alatKol.getValue();
            alatStavke.removeIf(a -> a.getIdAlat() == odabran.getIdAlat());
            alatStavke.add(new AlatRed(odabran.getIdAlat(), odabran.getNaziv(), kol));
        });
        ukloniAlatBtn.setOnAction(e -> {
            AlatRed s = alatTabela.getSelectionModel().getSelectedItem();
            if (s != null) alatStavke.remove(s);
        });

        HBox tblBar = new HBox(ukloniAlatBtn);
        tblBar.setAlignment(Pos.CENTER_RIGHT);
        tblBar.setPadding(new Insets(4, 0, 2, 0));

        return new VBox(8, sectionLbl("Alati"), addRow, tblBar, alatTabela);
    }

    // ── DUGMAD ROW ────────────────────────────────────────────────

    private HBox buildBtnRow() {
        sacuvajBtn.setStyle(
                "-fx-background-color: #534AB7; -fx-text-fill: white;" +
                        "-fx-font-size: 12px; -fx-background-radius: 6;" +
                        "-fx-cursor: hand; -fx-padding: 6 18 6 18;");
        otkaziBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #888888;" +
                        "-fx-font-size: 12px; -fx-cursor: hand;" +
                        "-fx-border-color: #cccccc; -fx-border-radius: 6; -fx-padding: 6 14 6 14;");
        otkaziBtn.setOnAction(e -> dialog.close());

        HBox row = new HBox(8, sacuvajBtn, otkaziBtn);
        row.setAlignment(Pos.CENTER_RIGHT);
        row.setPadding(new Insets(12, 16, 12, 16));
        row.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1 0 0 0;");
        return row;
    }

    // ── MICRO HELPERS ─────────────────────────────────────────────

    private HBox row(String labelTekst, Control field) {
        Label l = new Label(labelTekst);
        l.setMinWidth(80);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");
        HBox r = new HBox(10, l, field);
        r.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(field, Priority.ALWAYS);
        field.setMaxWidth(Double.MAX_VALUE);
        return r;
    }

    private Label lbl(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
        return l;
    }

    private Label sectionLbl(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #333;");
        return l;
    }

    private void styleDodaj(Button btn) {
        btn.setStyle(
                "-fx-background-color: #534AB7; -fx-text-fill: white;" +
                        "-fx-font-size: 11px; -fx-background-radius: 5;" +
                        "-fx-cursor: hand; -fx-padding: 5 12 5 12;");
    }

    private void styleUkloni(Button btn) {
        btn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #cc3333;" +
                        "-fx-font-size: 11px; -fx-cursor: hand;" +
                        "-fx-border-color: #cc3333; -fx-border-radius: 5; -fx-padding: 4 10 4 10;");
    }

    private <T> TableColumn<T, String> tCol(String n, String prop, double min) {
        TableColumn<T, String> col = new TableColumn<>(n);
        col.setCellValueFactory(new PropertyValueFactory<>(prop));
        col.setMinWidth(min);
        return col;
    }

    private ScrollPane wrapScroll(javafx.scene.Node node) {
        ScrollPane sp = new ScrollPane(node);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: white; -fx-background: white;");
        return sp;
    }

    private String nvl(String s) { return s != null ? s : ""; }

    public void close() { if (dialog != null) dialog.close(); }

    // ═════════════════════════════════════════════════════════════
    //  GETTERI za controller
    // ═════════════════════════════════════════════════════════════

    public TextField         getNazivField()   { return nazivField; }
    public TextArea          getOpisField()    { return opisField; }
    public ComboBox<String>  getTipCombo()     { return tipCombo; }
    public ComboBox<String>  getMetodCombo()   { return metodCombo; }
    public CheckBox          getEtickiCheck()  { return etickiCheck; }
    public Button            getSacuvajBtn()   { return sacuvajBtn; }
    public Button            getOtkaziBtn()    { return otkaziBtn; }

    public ComboBox<EksperimentResursStavka> getResursCombo() { return resursCombo; }
    public ComboBox<EksperimentAlatStavka>   getAlatCombo()   { return alatCombo; }
    public ObservableList<ResursRed>         getOdabraneResurse() { return resStavke; }
    public ObservableList<AlatRed>           getOdabraneAlate()   { return alatStavke; }

    /** Popunjava tabelu resursa za edit mod. */
    public void setResurseZaEdit(List<EksperimentResursStavka> stavke) {
        resStavke.clear();
        for (EksperimentResursStavka s : stavke)
            resStavke.add(new ResursRed(
                    s.getIdResurs(), s.getNaziv(), s.getJedinica(), s.getKolicina()));
    }

    /** Popunjava tabelu alata za edit mod. */
    public void setAlateZaEdit(List<EksperimentAlatStavka> stavke) {
        alatStavke.clear();
        for (EksperimentAlatStavka s : stavke)
            alatStavke.add(new AlatRed(s.getIdAlat(), s.getNaziv(), s.getKolicina()));
    }

    // ═════════════════════════════════════════════════════════════
    //  JavaFX row modeli  (PropertyValueFactory kompatibilni)
    // ═════════════════════════════════════════════════════════════

    public static class ResursRed {
        private final SimpleIntegerProperty idResurs;
        private final SimpleStringProperty  naziv;
        private final SimpleStringProperty  jedinica;
        private final SimpleDoubleProperty  kolicina;

        public ResursRed(int id, String naziv, String jed, double kol) {
            this.idResurs = new SimpleIntegerProperty(id);
            this.naziv    = new SimpleStringProperty(naziv);
            this.jedinica = new SimpleStringProperty(jed != null ? jed : "");
            this.kolicina = new SimpleDoubleProperty(kol);
        }
        public int    getIdResurs()  { return idResurs.get(); }
        public String getNaziv()     { return naziv.get(); }
        public String getJedinica()  { return jedinica.get(); }
        public double getKolicina()  { return kolicina.get(); }
    }

    public static class AlatRed {
        private final SimpleIntegerProperty idAlat;
        private final SimpleStringProperty  naziv;
        private final SimpleIntegerProperty kolicina;

        public AlatRed(int id, String naziv, int kol) {
            this.idAlat   = new SimpleIntegerProperty(id);
            this.naziv    = new SimpleStringProperty(naziv);
            this.kolicina = new SimpleIntegerProperty(kol);
        }
        public int    getIdAlat()   { return idAlat.get(); }
        public String getNaziv()    { return naziv.get(); }
        public int    getKolicina() { return kolicina.get(); }
    }
}