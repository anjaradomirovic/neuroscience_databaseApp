package org.baze.neuronauka.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Glavni prozor za upravljanje eksperimentima.
 *
 * Levo  – tabela svih eksperimenata + toolbar (Dodaj / Izmeni)
 * Desno – scrollable detalji selektovanog eksperimenta
 */
public class EksperimentView {

    // ── top bar ───────────────────────────────────────────────────
    private final Label userLabel = new Label();

    // ── toolbar ───────────────────────────────────────────────────
    private final Button dodajBtn = new Button("Dodaj");
    private final Button editBtn  = new Button("Izmeni");

    // ── tabela ────────────────────────────────────────────────────
    private final TableView<EksperimentRow> tabela = new TableView<>();

    // ── detalji ───────────────────────────────────────────────────
    private final Label detaljiNaslov = new Label("Detalji eksperimenta");
    private final Label idDet         = new Label();
    private final Label nazivDet      = new Label();
    private final Label tipDet        = new Label();
    private final Label metodDet      = new Label();
    private final Label etickiDet     = new Label();
    private final Label opisDet       = new Label();

    private Stage stage;

    // ─────────────────────────────────────────────────────────────

    public void show(Stage parentStage, String loggedInUser) {
        userLabel.setText("Korisnik: " + loggedInUser);

        // Stage mora biti kreiran i dodeljen PRIJE buildBody(),
        // jer controller koji se pravi posle show() poziva getStage().
        stage = new Stage();
        stage.initOwner(parentStage);
        stage.setTitle("Upravljanje eksperimentima");

        BorderPane root = new BorderPane();
        root.setTop(buildTopBar());
        root.setCenter(buildBody());
        root.setStyle("-fx-background-color: #f5f5f5;");

        stage.setScene(new Scene(root, 1050, 650));
        stage.show();
    }

    // ── TOP BAR ──────────────────────────────────────────────────

    private HBox buildTopBar() {
        Label title = new Label("Eksperimenti");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        userLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 13px;");

        HBox bar = new HBox(12, title, spacer, userLabel);
        bar.setPadding(new Insets(12, 20, 12, 20));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 0 0 1 0;");
        return bar;
    }

    // ── TELO ─────────────────────────────────────────────────────

    private SplitPane buildBody() {
        VBox levo = buildLevo();
        ScrollPane desno = buildDetaljiScroll();

        SplitPane split = new SplitPane(levo, desno);
        split.setDividerPositions(0.57);
        SplitPane.setResizableWithParent(desno, false);
        return split;
    }

    private VBox buildLevo() {
        VBox box = new VBox(0, buildToolbar(), buildTabela());
        VBox.setVgrow(tabela, Priority.ALWAYS);
        box.setStyle("-fx-background-color: white;");
        return box;
    }

    // ── TOOLBAR ──────────────────────────────────────────────────

    private HBox buildToolbar() {
        styleBtn(dodajBtn, "add.png",     "#534AB7", "white", false);
        styleBtn(editBtn,  "editLab.png", "transparent", "#534AB7", true);
        editBtn.setDisable(true);

        tabela.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, sel) -> editBtn.setDisable(sel == null));

        tabela.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, sel) -> prikaziDetalje(sel));

        HBox bar = new HBox(8, dodajBtn, editBtn);
        bar.setPadding(new Insets(10, 16, 10, 16));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle(
                "-fx-background-color: #fafafa;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 0 0 1 0;");
        return bar;
    }

    private void styleBtn(Button btn, String icon, String bg, String fg, boolean outline) {
        try {
            ImageView iv = new ImageView(
                    new Image(getClass().getResourceAsStream(
                            "/org/baze/neuronauka/icons/" + icon)));
            iv.setFitWidth(16);
            iv.setFitHeight(16);
            btn.setGraphic(iv);
        } catch (Exception ignored) {}

        String border = outline
                ? "-fx-border-color: #534AB7; -fx-border-radius: 6;"
                : "";
        btn.setStyle(
                "-fx-background-color: " + bg + ";" +
                        "-fx-text-fill: " + fg + ";" +
                        "-fx-font-size: 12px; -fx-background-radius: 6;" +
                        "-fx-cursor: hand; -fx-padding: 6 14 6 14;" + border);
    }

    // ── TABELA ────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private TableView<EksperimentRow> buildTabela() {
        TableColumn<EksperimentRow, String> cId     = col("ID",     60,  r -> r.idEksperiment());
        TableColumn<EksperimentRow, String> cNaziv  = col("Naziv", 200,  r -> r.naziv());
        TableColumn<EksperimentRow, String> cTip    = col("Tip",   140,  r -> r.tipEksperimenta());
        TableColumn<EksperimentRow, String> cMetod  = col("Metod", 130,  r -> r.metodIzvodjenja());

        tabela.getColumns().addAll(cId, cNaziv, cTip, cMetod);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabela.setPlaceholder(new Label("Nema eksperimenata."));
        tabela.setStyle("-fx-border-color: transparent;");
        VBox.setVgrow(tabela, Priority.ALWAYS);
        return tabela;
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

    // ── DETALJI ───────────────────────────────────────────────────

    private ScrollPane buildDetaljiScroll() {
        detaljiNaslov.setFont(Font.font("System", FontWeight.BOLD, 14));
        detaljiNaslov.setStyle("-fx-text-fill: #333;");
        idDet.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;");
        opisDet.setWrapText(true);
        opisDet.setMaxWidth(300);

        VBox forma = new VBox(12,
                detaljiNaslov,
                new Separator(),
                idDet,
                detRow("Naziv",         nazivDet),
                detRow("Tip",           tipDet),
                detRow("Metod",         metodDet),
                detRow("Etički odobren",etickiDet),
                detRow("Opis",          opisDet)
        );
        forma.setPadding(new Insets(20));
        forma.setStyle("-fx-background-color: white;");

        ScrollPane scroll = new ScrollPane(forma);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: white; -fx-background: white;");
        return scroll;
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

    private void prikaziDetalje(EksperimentRow row) {
        if (row == null) {
            idDet.setText(""); nazivDet.setText(""); tipDet.setText("");
            metodDet.setText(""); etickiDet.setText(""); opisDet.setText("");
            return;
        }
        idDet.setText("ID: " + row.idEksperiment());
        nazivDet.setText(row.naziv());
        tipDet.setText(row.tipEksperimenta());
        metodDet.setText(row.metodIzvodjenja());
        etickiDet.setText(row.etickiOdobren() ? "Da" : "Ne");
        opisDet.setText(row.opis());
    }

    // ── GETTERI ───────────────────────────────────────────────────

    public Button getDodajBtn() { return dodajBtn; }
    public Button getEditBtn()  { return editBtn; }
    public TableView<EksperimentRow> getTabela() { return tabela; }
    public Label  getUserLabel() { return userLabel; }
    public Stage  getStage()     { return stage; }

    public void setEksperimenti(java.util.List<EksperimentRow> rows) {
        tabela.getItems().setAll(rows);
    }

    public EksperimentRow getOdabrani() {
        return tabela.getSelectionModel().getSelectedItem();
    }

    // ── RECORD (red u tabeli) ─────────────────────────────────────

    public record EksperimentRow(
            String  idEksperiment,
            String  naziv,
            String  tipEksperimenta,
            String  metodIzvodjenja,
            String  opis,
            boolean etickiOdobren
    ) {}
}