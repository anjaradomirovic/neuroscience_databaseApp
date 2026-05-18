package org.baze.neuronauka.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

import org.baze.neuronauka.model.IzvodjenjeDTO;
import org.baze.neuronauka.model.IzvodjenjeDTO.IzvodjenjeIzvodjacStavka;
import org.baze.neuronauka.model.IzvodjenjeDTO.IzvodjenjeResursStavka;

public class IzvodjenjeContent {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final List<String> STATUSI = List.of(
            "planirano", "zapoceto", "odlozeno",
            "otkazano", "zavrseno_uspesno", "zavrseno_neuspesno"
    );

    // ── toolbar ───────────────────────────────────────────────────
    private final Button             novoBtn       = new Button("Novo izvodjenje");
    private final ComboBox<String[]> ekspFilter    = new ComboBox<>();
    private final ComboBox<String>   statusFilter  = new ComboBox<>();

    // nova polja
    private final VBox   dizajnerBox     = new VBox(4);
    private final VBox   teorijskiOkvir  = new VBox(4);
    // ── tabela ────────────────────────────────────────────────────
    private final TableView<IzvodjenjeDTO> tabela  = new TableView<>();

    // ── detalji — desni panel ─────────────────────────────────────
    private final Label  detId        = new Label();
    private final Label  detEksp      = new Label();
    private final Label  detLab       = new Label();
    private final Label  detDatum     = new Label();
    private final ComboBox<String> statusCombo = new ComboBox<>();
    private final Button sacuvajStatusBtn = new Button("Sačuvaj status");
    private final VBox   resursiBox   = new VBox(4);
    private final VBox   izvodjacBox  = new VBox(4);

    // ── callbacks ─────────────────────────────────────────────────
    private Runnable                    onNovo;
    private Consumer<IzvodjenjeDTO>     onSelekt;
    private Consumer<IzvodjenjeDTO>     onSacuvajStatus;

    // ─────────────────────────────────────────────────────────────
    private final ComboBox<String[]> dodajIzvodjacaCombo = new ComboBox<>();
    private final TextField          opisUloge           = new TextField();
    private final Button             dodajIzvodjacaBtn   = new Button("+ Dodaj");

    @SuppressWarnings("unchecked")
    public Node build() {

        // ── toolbar ───────────────────────────────────────────────
        styleBtn(novoBtn, "#534AB7", "white", false);
        novoBtn.setOnAction(e -> { if (onNovo != null) onNovo.run(); });

        ekspFilter.setPromptText("Svi eksperimenti");
        ekspFilter.setCellFactory(lv -> eksCell());
        ekspFilter.setButtonCell(eksCell());
        ekspFilter.setMinWidth(200);

        statusFilter.getItems().add(null);
        statusFilter.getItems().addAll(STATUSI);
        statusFilter.setPromptText("Svi statusi");
        statusFilter.setCellFactory(lv -> statusCell());
        statusFilter.setButtonCell(statusCell());
        statusFilter.setMinWidth(160);

        ekspFilter.setOnAction(e -> { if (onSelekt != null) osveziTabelu(); });
        statusFilter.setOnAction(e -> osveziTabelu());

        HBox toolbar = new HBox(8, novoBtn, new Separator() {{
            setOrientation(javafx.geometry.Orientation.VERTICAL);
        }}, ekspFilter, statusFilter);
        toolbar.setPadding(new Insets(10, 16, 10, 16));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle(
                "-fx-background-color: #fafafa;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 0 0 1 0;");

        // ── tabela ────────────────────────────────────────────────
        TableColumn<IzvodjenjeDTO, String> cDatum  = strCol("Datum",       180,
                d -> d.getDatumIzvodjenja() != null ? d.getDatumIzvodjenja().format(FMT) : "");
        TableColumn<IzvodjenjeDTO, String> cStatus = strCol("Status",      150,
                d -> d.getStatus() != null ? d.getStatus() : "");
        TableColumn<IzvodjenjeDTO, String> cEksp   = strCol("Eksperiment", 200,
                d -> d.getNazivEksperimenta() != null ? d.getNazivEksperimenta() : "");
        TableColumn<IzvodjenjeDTO, String> cLab    = strCol("Laboratorija",160,
                d -> d.getNazivLaboratorije() != null ? d.getNazivLaboratorije() : "");

        // status kolona — bojana ćelija
        cStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label lbl = new Label(item);
                lbl.setStyle(statusStyle(item) +
                        "-fx-font-size: 11px; -fx-padding: 2 8 2 8;" +
                        "-fx-background-radius: 4;");
                setGraphic(lbl);
                setText(null);
            }
        });

        tabela.getColumns().addAll(cDatum, cStatus, cEksp, cLab);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabela.setPlaceholder(new Label("Nema izvodjenja."));
        tabela.setStyle("-fx-border-color: transparent;");
        VBox.setVgrow(tabela, Priority.ALWAYS);

        tabela.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null && onSelekt != null) onSelekt.accept(sel);
        });

        VBox levo = new VBox(0, toolbar, tabela);
        levo.setStyle("-fx-background-color: white;");
        VBox.setVgrow(tabela, Priority.ALWAYS);

        // ── desni panel ───────────────────────────────────────────
        ScrollPane desno = buildDetaljiPanel();

        SplitPane split = new SplitPane(levo, desno);
        split.setDividerPositions(0.58);
        SplitPane.setResizableWithParent(desno, false);
        return split;
    }

    // ── DETALJI PANEL ─────────────────────────────────────────────

    private ScrollPane buildDetaljiPanel() {
        Label naslov = new Label("Detalji izvodjenja");
        naslov.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");
        detId.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;");
// ── dodaj izvodjača row ───────────────────────────────────
        dodajIzvodjacaCombo.setMaxWidth(Double.MAX_VALUE);
        dodajIzvodjacaCombo.setPromptText("Odaberi izvodjača...");
        dodajIzvodjacaCombo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(String[] item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item[1]);
            }
        });
        dodajIzvodjacaCombo.setButtonCell(dodajIzvodjacaCombo.getCellFactory().call(null));
        opisUloge.setPromptText("Opis uloge (opciono)");
        opisUloge.setMaxWidth(Double.MAX_VALUE);
        dodajIzvodjacaBtn.setStyle(
                "-fx-background-color: #534AB7; -fx-text-fill: white;" +
                        "-fx-font-size: 11px; -fx-background-radius: 5;" +
                        "-fx-cursor: hand; -fx-padding: 4 10 4 10;");
        HBox.setHgrow(dodajIzvodjacaCombo, Priority.ALWAYS);
        VBox dodajIzvodjacaBox = new VBox(6, dodajIzvodjacaCombo, opisUloge, dodajIzvodjacaBtn);

        // status combo + dugme
        statusCombo.getItems().addAll(STATUSI);
        statusCombo.setCellFactory(lv -> statusCell());
        statusCombo.setButtonCell(statusCell());
        statusCombo.setMaxWidth(Double.MAX_VALUE);
        styleBtn(sacuvajStatusBtn, "#534AB7", "white", false);
        sacuvajStatusBtn.setOnAction(e -> {
            IzvodjenjeDTO sel = tabela.getSelectionModel().getSelectedItem();
            if (sel == null || statusCombo.getValue() == null) return;
            sel.setStatus(statusCombo.getValue());
            if (onSacuvajStatus != null) onSacuvajStatus.accept(sel);
        });

        VBox statusRow = new VBox(6,
                sectionLbl("Status"),
                statusCombo,
                sacuvajStatusBtn);

        VBox forma = new VBox(14,
                naslov,
                new Separator(),
                detId,
                detRow("Eksperiment",  detEksp),
                detRow("Laboratorija", detLab),
                detRow("Datum",        detDatum),
                statusRow,
                sectionLbl("Resursi"),
                resursiBox,
                sectionLbl("Izvodjači"),
                dodajIzvodjacaBox,
                izvodjacBox
        );
        forma.setPadding(new Insets(20));
        forma.setStyle("-fx-background-color: white;");


        ScrollPane sp = new ScrollPane(forma);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: white; -fx-background: white;");

        return sp;
    }

    // ── PUNJENJE DETALJA ──────────────────────────────────────────

    public void prikaziDetalje(IzvodjenjeDTO dto) {
        if (dto == null) {
            detId.setText(""); detEksp.setText(""); detLab.setText("");
            detDatum.setText(""); statusCombo.setValue(null);
            resursiBox.getChildren().clear();
            izvodjacBox.getChildren().clear();
            return;
        }
        detId.setText("ID: " + dto.getIdIzvodjenje());
        detEksp.setText(nvl(dto.getNazivEksperimenta()));
        detLab.setText(nvl(dto.getNazivLaboratorije()));
        detDatum.setText(dto.getDatumIzvodjenja() != null
                ? dto.getDatumIzvodjenja().format(FMT) : "");
        statusCombo.setValue(dto.getStatus());

        resursiBox.getChildren().clear();
        if (dto.getResurei() == null || dto.getResurei().isEmpty()) {
            resursiBox.getChildren().add(hint("Nema resursa za ovo izvodjenje."));
        } else {
            for (IzvodjenjeResursStavka r : dto.getResurei()) {
                HBox row = new HBox();
                Label naziv = new Label(r.getNaziv());
                naziv.setStyle("-fx-font-size: 12px;");
                Label kol = new Label("× " + String.format("%.2f", r.getKolicina()));
                kol.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
                Region sp = new Region();
                HBox.setHgrow(sp, Priority.ALWAYS);
                row.getChildren().addAll(naziv, sp, kol);
                resursiBox.getChildren().add(row);
            }
        }

        izvodjacBox.getChildren().clear();
        if (dto.getIzvodjaci() == null || dto.getIzvodjaci().isEmpty()) {
            izvodjacBox.getChildren().add(hint("Nema izvodjača za ovo izvodjenje."));
        } else {
            for (IzvodjenjeIzvodjacStavka iz : dto.getIzvodjaci()) {
                VBox card = new VBox(2);
                card.setStyle(
                        "-fx-background-color: #fafafa;" +
                                "-fx-border-color: #e8e8e8;" +
                                "-fx-border-radius: 6; -fx-background-radius: 6;" +
                                "-fx-padding: 6 10 6 10;");
                Label ime = new Label(iz.getImeIPrezime());
                ime.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
                Label uloga = new Label(nvl(iz.getOpisUloge()));
                uloga.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
                card.getChildren().addAll(ime, uloga);
                izvodjacBox.getChildren().add(card);
            }
        }
        dizajnerBox.getChildren().clear();
        if (dto.getDizajner() == null || dto.getDizajner().isBlank()) {
            dizajnerBox.getChildren().add(hint("Nema dizajnera."));
        } else {
            Label d = new Label(dto.getDizajner());
            d.setStyle("-fx-font-size: 12px;");
            d.setWrapText(true);
            dizajnerBox.getChildren().add(d);
        }

        teorijskiOkvir.getChildren().clear();
        if (dto.getTeorijskiOkvir() == null || dto.getTeorijskiOkvir().isBlank()) {
            teorijskiOkvir.getChildren().add(hint("Nema teorijskog okvira."));
        } else {
            Label t = new Label(dto.getTeorijskiOkvir());
            t.setStyle("-fx-font-size: 12px;");
            t.setWrapText(true);
            teorijskiOkvir.getChildren().add(t);
        }
    }

    // ── FILTER HELPER ─────────────────────────────────────────────

    /** Poziva se iz controllera kada se menja filter — controller osveži tabelu. */
    private void osveziTabelu() {
        // controller sluša ekspFilter i statusFilter direktno
    }

    // ── JAVNI API ─────────────────────────────────────────────────

    public void setIzvodjenja(List<IzvodjenjeDTO> lista) {
        tabela.getItems().setAll(lista);
        prikaziDetalje(null);
    }

    public void setEksperimentiZaFilter(List<String[]> eksperimenti) {
        ekspFilter.getItems().clear();
        ekspFilter.getItems().add(null);
        ekspFilter.getItems().addAll(eksperimenti);
    }

    public IzvodjenjeDTO getOdabrano() {
        return tabela.getSelectionModel().getSelectedItem();
    }

    public Button             getNovoBtn()         { return novoBtn; }
    public ComboBox<String[]> getEkspFilter()      { return ekspFilter; }
    public ComboBox<String>   getStatusFilter()    { return statusFilter; }
    public ComboBox<String>   getStatusCombo()     { return statusCombo; }
    public Button             getSacuvajStatusBtn(){ return sacuvajStatusBtn; }
    public TableView<IzvodjenjeDTO> getTabela()    { return tabela; }

    public void setOnNovo(Runnable r)                          { this.onNovo = r; }
    public void setOnSelekt(Consumer<IzvodjenjeDTO> c)         { this.onSelekt = c; }
    public void setOnSacuvajStatus(Consumer<IzvodjenjeDTO> c)  { this.onSacuvajStatus = c; }

    public void prikaziIzvodjace(List<IzvodjenjeIzvodjacStavka> izvodjaci,
                                 java.util.function.Consumer<Integer> onUkloni) {
        izvodjacBox.getChildren().clear();
        if (izvodjaci == null || izvodjaci.isEmpty()) {
            izvodjacBox.getChildren().add(hint("Nema izvodjača za ovo izvodjenje."));
            return;
        }
        for (IzvodjenjeIzvodjacStavka iz : izvodjaci) {
            HBox card = new HBox(8);
            card.setAlignment(Pos.CENTER_LEFT);
            card.setStyle(
                    "-fx-background-color: #fafafa; -fx-border-color: #e8e8e8;" +
                            "-fx-border-radius: 6; -fx-background-radius: 6;" +
                            "-fx-padding: 6 10 6 10;");
            VBox info = new VBox(2);
            Label ime = new Label(iz.getImeIPrezime());
            ime.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
            Label uloga = new Label(nvl(iz.getOpisUloge()));
            uloga.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
            info.getChildren().addAll(ime, uloga);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button xBtn = new Button("×");
            xBtn.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: #cc3333;" +
                            "-fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 0 4 0 4;");
            xBtn.setOnAction(e -> onUkloni.accept(iz.getIdIstrazivaca()));
            card.getChildren().addAll(info, spacer, xBtn);
            izvodjacBox.getChildren().add(card);
        }
    }

    public ComboBox<String[]> getDodajIzvodjacaCombo() { return dodajIzvodjacaCombo; }
    public TextField          getOpisUloge()           { return opisUloge; }
    public Button             getDodajIzvodjacaBtn()   { return dodajIzvodjacaBtn; }
    // ── HELPERS ───────────────────────────────────────────────────

    private HBox detRow(String lbl, Label val) {
        Label l = new Label(lbl + ":");
        l.setMinWidth(120);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
        val.setStyle("-fx-font-size: 12px; -fx-text-fill: #222;");
        HBox row = new HBox(10, l, val);
        row.setAlignment(Pos.TOP_LEFT);
        return row;
    }

    private Label sectionLbl(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #444;" +
                "-fx-padding: 6 0 0 0;");
        return l;
    }

    private Label hint(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-font-size: 11px; -fx-text-fill: #aaa;");
        return l;
    }

    private TableColumn<IzvodjenjeDTO, String> strCol(
            String naslov, double minW,
            java.util.function.Function<IzvodjenjeDTO, String> getter) {
        TableColumn<IzvodjenjeDTO, String> col = new TableColumn<>(naslov);
        col.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(getter.apply(data.getValue())));
        col.setMinWidth(minW);
        return col;
    }

    private ListCell<String[]> eksCell() {
        return new ListCell<>() {
            @Override protected void updateItem(String[] item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Svi eksperimenti" : item[1]);
            }
        };
    }

    private ListCell<String> statusCell() {
        return new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText("Svi statusi"); setGraphic(null); return; }
                Label lbl = new Label(item);
                lbl.setStyle(statusStyle(item) +
                        "-fx-font-size: 11px; -fx-padding: 2 8 2 8; -fx-background-radius: 4;");
                setGraphic(lbl);
                setText(null);
            }
        };
    }

    private String statusStyle(String status) {
        return switch (status) {
            case "planirano"             -> "-fx-background-color: #E6F1FB; -fx-text-fill: #185FA5;";
            case "zapoceto"             -> "-fx-background-color: #FAEEDA; -fx-text-fill: #854F0B;";
            case "zavrseno_uspesno"     -> "-fx-background-color: #EAF3DE; -fx-text-fill: #3B6D11;";
            case "zavrseno_neuspesno"   -> "-fx-background-color: #FCEBEB; -fx-text-fill: #A32D2D;";
            case "odlozeno"             -> "-fx-background-color: #F1EFE8; -fx-text-fill: #5F5E5A;";
            case "otkazano"             -> "-fx-background-color: #FBEAF0; -fx-text-fill: #993556;";
            default                     -> "-fx-background-color: #f0f0f0; -fx-text-fill: #555;";
        };
    }

    private void styleBtn(Button btn, String bg, String fg, boolean outline) {
        btn.setStyle(
                "-fx-background-color: " + bg + ";" +
                        "-fx-text-fill: " + fg + ";" +
                        "-fx-font-size: 12px; -fx-background-radius: 6;" +
                        "-fx-cursor: hand; -fx-padding: 6 14 6 14;" +
                        (outline ? "-fx-border-color: #534AB7; -fx-border-radius: 6;" : ""));
    }

    private String nvl(String s) { return s != null ? s : ""; }
}