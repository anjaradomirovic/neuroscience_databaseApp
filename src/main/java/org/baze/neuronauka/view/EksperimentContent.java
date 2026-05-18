package org.baze.neuronauka.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Inline content panel za prikaz eksperimenata.
 * Koristi se kao ResursiContent — switchuje se u centar glavnog prozora
 * pozivom view.switchCenter(eksperimentContent.build()).
 *
 * Layout:
 *   ┌─ toolbar ──────────────────────────────────────────────┐
 *   │  [+ Dodaj eksperiment]  [✎ Izmeni]                     │
 *   ├─ tabela (levo 58%) ──┬─ detalji scroll (desno 42%) ───┤
 *   │  ID │ Naziv │ Tip    │  Naziv, Opis, Tip, Metod,       │
 *   │     │       │ Metod  │  Etički odobren                 │
 *   └──────────────────────┴─────────────────────────────────┘
 */
public class EksperimentContent {

    // ── toolbar dugmad ────────────────────────────────────────────
    private final Button dodajBtn = new Button("Dodaj eksperiment");
    private final Button editBtn  = new Button("Izmeni");

    // ── tabela ────────────────────────────────────────────────────
    private final TableView<EksperimentRow> tabela = new TableView<>();

    // ── detalji labele ────────────────────────────────────────────
    private final Label idDet      = new Label();
    private final Label nazivDet   = new Label();
    private final Label tipDet     = new Label();
    private final Label metodDet   = new Label();
    private final Label etickiDet  = new Label();
    private final Label opisDet    = new Label();
    // dodaj uz ostale label deklaracije
    private final Label dizajneriDet    = new Label();
    private final Label teorijskiDet    = new Label();
    private final ComboBox<String[]> teorijskiOkvirCombo = new ComboBox<>();
    private final VBox               dizajneriBox        = new VBox(6);
    private final ComboBox<String[]> dodajDizajneraCombo = new ComboBox<>();
    private final Button             dodajDizajneraBtn   = new Button("+ Dodaj");

    // ─────────────────────────────────────────────────────────────

    /** Gradi i vraća Node koji se smešta u centar glavnog prozora. */
    @SuppressWarnings("unchecked")
    public Node build() {

        // ── toolbar ───────────────────────────────────────────────
        styleBtn(dodajBtn, "add.png",     "#534AB7", "white", false);
        styleBtn(editBtn,  "editLab.png", "transparent", "#534AB7", true);
        editBtn.setDisable(true);

        tabela.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, sel) -> {
                    editBtn.setDisable(sel == null);
                    prikaziDetalje(sel);
                });

        HBox toolbar = new HBox(8, dodajBtn, editBtn);
        toolbar.setPadding(new Insets(10, 16, 10, 16));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle(
                "-fx-background-color: #fafafa;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 0 0 1 0;");

        // ── tabela ────────────────────────────────────────────────
        TableColumn<EksperimentRow, String> cId    = col("ID",    55,  r -> r.idEksperiment());
        TableColumn<EksperimentRow, String> cNaziv = col("Naziv", 200, r -> r.naziv());
        TableColumn<EksperimentRow, String> cTip   = col("Tip",   150, r -> r.tipEksperimenta());
        TableColumn<EksperimentRow, String> cMetod = col("Metod", 140, r -> r.metodIzvodjenja());

        tabela.getColumns().addAll(cId, cNaziv, cTip, cMetod);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabela.setPlaceholder(new Label("Nema eksperimenata."));
        tabela.setStyle("-fx-border-color: transparent;");
        VBox.setVgrow(tabela, Priority.ALWAYS);

        VBox levo = new VBox(0, toolbar, tabela);
        levo.setStyle("-fx-background-color: white;");
        VBox.setVgrow(tabela, Priority.ALWAYS);

        // ── detalji ───────────────────────────────────────────────
        ScrollPane desno = buildDetaljiScroll();

        SplitPane split = new SplitPane(levo, desno);
        split.setDividerPositions(0.58);
        SplitPane.setResizableWithParent(desno, false);
        return split;
    }

    // ── DETALJI ───────────────────────────────────────────────────

    private ScrollPane buildDetaljiScroll() {
        Label naslov = new Label("Detalji eksperimenta");
        naslov.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");
        idDet.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;");
        opisDet.setWrapText(true);
        opisDet.setMaxWidth(320);

        // teorijski okvir combo
        teorijskiOkvirCombo.setMaxWidth(Double.MAX_VALUE);
        teorijskiOkvirCombo.setPromptText("Odaberi teorijski okvir...");
        teorijskiOkvirCombo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(String[] item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item[1]);
            }
        });
        teorijskiOkvirCombo.setButtonCell(teorijskiOkvirCombo.getCellFactory().call(null));

        // dodaj dizajnera combo + dugme
        dodajDizajneraCombo.setMaxWidth(Double.MAX_VALUE);
        dodajDizajneraCombo.setPromptText("Odaberi dizajnera...");
        dodajDizajneraCombo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(String[] item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item[1]);
            }
        });
        dodajDizajneraCombo.setButtonCell(dodajDizajneraCombo.getCellFactory().call(null));
        dodajDizajneraBtn.setStyle(
                "-fx-background-color: #534AB7; -fx-text-fill: white;" +
                        "-fx-font-size: 11px; -fx-background-radius: 5;" +
                        "-fx-cursor: hand; -fx-padding: 4 10 4 10;");
        HBox.setHgrow(dodajDizajneraCombo, Priority.ALWAYS);
        HBox dodajRow = new HBox(6, dodajDizajneraCombo, dodajDizajneraBtn);
        dodajRow.setAlignment(Pos.CENTER_LEFT);

        Label dizNaslov = new Label("Dizajneri:");
        dizNaslov.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
        Label teorNaslov = new Label("Teorijski okvir:");
        teorNaslov.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");

        VBox forma = new VBox(12,
                naslov, new Separator(), idDet,
                detRow("Naziv",          nazivDet),
                detRow("Tip",            tipDet),
                detRow("Metod",          metodDet),
                detRow("Etički odobren", etickiDet),
                detRow("Opis",           opisDet),
                teorNaslov,
                teorijskiOkvirCombo,
                dizNaslov,
                dodajRow,
                dizajneriBox
        );
        forma.setPadding(new Insets(20));
        forma.setStyle("-fx-background-color: white;");

        ScrollPane sp = new ScrollPane(forma);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: white; -fx-background: white;");
        return sp;
    }

    private HBox detRow(String lbl, Label val) {
        Label l = new Label(lbl + ":");
        l.setMinWidth(120);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
        val.setStyle("-fx-font-size: 12px; -fx-text-fill: #222;");
        HBox row = new HBox(10, l, val);
        row.setAlignment(Pos.TOP_LEFT);
        return row;
    }

    public void prikaziDetalje(EksperimentRow r) {
        if (r == null) {
            idDet.setText(""); nazivDet.setText(""); tipDet.setText("");
            metodDet.setText(""); etickiDet.setText(""); opisDet.setText("");
            return;
        }
        idDet.setText("ID: " + r.idEksperiment());
        nazivDet.setText(nvl(r.naziv()));
        tipDet.setText(nvl(r.tipEksperimenta()));
        metodDet.setText(nvl(r.metodIzvodjenja()));
        etickiDet.setText(r.etickiOdobren() ? "Da" : "Ne");
        opisDet.setText(nvl(r.opis()));
        dizajneriDet.setWrapText(true);
        dizajneriDet.setMaxWidth(320);
        teorijskiDet.setWrapText(true);
        teorijskiDet.setMaxWidth(320);
        tabela.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, sel) -> {
                    editBtn.setDisable(sel == null);
                    if (sel == null) clearDetalji();
                    // NE pozivaj prikaziDetalje ovde — controller to radi
                });

// dodaj uz ostale setove:
        dizajneriDet.setText(r == null ? "" : nvl(r.dizajneri()));
        teorijskiDet.setText(r == null ? "" : nvl(r.teorijskiOkvir()));
    }
    private void clearDetalji() {
        idDet.setText(""); nazivDet.setText(""); tipDet.setText("");
        metodDet.setText(""); etickiDet.setText(""); opisDet.setText("");
        dizajneriBox.getChildren().clear();
    }

    // ── HELPERS ───────────────────────────────────────────────────

    private void styleBtn(Button btn, String icon,
                          String bg, String fg, boolean outline) {
        try {
            ImageView iv = new ImageView(
                    new Image(getClass().getResourceAsStream(
                            "/org/baze/neuronauka/icons/" + icon)));
            iv.setFitWidth(16);
            iv.setFitHeight(16);
            btn.setGraphic(iv);
        } catch (Exception ignored) {}

        btn.setStyle(
                "-fx-background-color: " + bg + ";" +
                        "-fx-text-fill: " + fg + ";" +
                        "-fx-font-size: 12px; -fx-background-radius: 6;" +
                        "-fx-cursor: hand; -fx-padding: 6 14 6 14;" +
                        (outline ? "-fx-border-color: #534AB7; -fx-border-radius: 6;" : ""));
    }

    private TableColumn<EksperimentRow, String> col(
            String naslov, double minW,
            java.util.function.Function<EksperimentRow, String> getter) {
        TableColumn<EksperimentRow, String> col = new TableColumn<>(naslov);
        col.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        getter.apply(data.getValue())));
        col.setMinWidth(minW);
        return col;
    }

    private String nvl(String s) { return s != null ? s : ""; }

    // ── JAVNI API ─────────────────────────────────────────────────

    public Button getDodajBtn()  { return dodajBtn; }
    public Button getEditBtn()   { return editBtn; }
    public TableView<EksperimentRow> getTabela() { return tabela; }

    public void setEksperimenti(java.util.List<EksperimentRow> rows) {
        tabela.getItems().setAll(rows);
    }

    public EksperimentRow getOdabrani() {
        return tabela.getSelectionModel().getSelectedItem();
    }

    // ── ROW MODEL ─────────────────────────────────────────────────

    public void prikaziDizajnere(List<String[]> dizajneri,
                                 java.util.function.BiConsumer<String, Boolean> onGlavni,
                                 java.util.function.Consumer<String> onUkloni) {
        dizajneriBox.getChildren().clear();
        for (String[] d : dizajneri) {
            // d[0]=id, d[1]=ime, d[2]="1"/"0"
            boolean jeGlavni = "1".equals(d[2]);
            HBox card = new HBox(8);
            card.setAlignment(Pos.CENTER_LEFT);
            card.setPadding(new Insets(4, 8, 4, 8));
            card.setStyle(jeGlavni
                    ? "-fx-background-color: #e8f5e9; -fx-border-color: #66bb6a;" +
                    "-fx-border-radius: 5; -fx-background-radius: 5;"
                    : "-fx-background-color: #fafafa; -fx-border-color: #e0e0e0;" +
                    "-fx-border-radius: 5; -fx-background-radius: 5;");

            Label ime = new Label(d[1]);
            ime.setStyle("-fx-font-size: 12px;" +
                    (jeGlavni ? "-fx-text-fill: #2e7d32; -fx-font-weight: bold;"
                            : "-fx-text-fill: #222;"));
            ime.setCursor(javafx.scene.Cursor.HAND);
            ime.setOnMouseClicked(e -> onGlavni.accept(d[0], true));

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button xBtn = new Button("×");
            xBtn.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: #cc3333;" +
                            "-fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 0 4 0 4;");
            xBtn.setOnAction(e -> onUkloni.accept(d[0]));

            card.getChildren().addAll(ime, spacer, xBtn);
            dizajneriBox.getChildren().add(card);
        }
        if (dizajneri.isEmpty()) {
            Label hint = new Label("Nema dizajnera.");
            hint.setStyle("-fx-font-size: 11px; -fx-text-fill: #aaa;");
            dizajneriBox.getChildren().add(hint);
        }
    }

    public ComboBox<String[]> getTeorijskiOkvirCombo()  { return teorijskiOkvirCombo; }
    public ComboBox<String[]> getDodajDizajneraCombo()  { return dodajDizajneraCombo; }
    public Button             getDodajDizajneraBtn()    { return dodajDizajneraBtn; }
    public record EksperimentRow(
            String  idEksperiment,
            String  naziv,
            String  tipEksperimenta,
            String  metodIzvodjenja,
            String  opis,
            boolean etickiOdobren,
            String  dizajneri,       // NOVO
            String  teorijskiOkvir
    ) {}
}