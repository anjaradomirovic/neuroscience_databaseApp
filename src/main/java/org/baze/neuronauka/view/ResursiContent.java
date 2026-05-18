package org.baze.neuronauka.view;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.baze.neuronauka.model.HitanResursDTO;
import org.baze.neuronauka.model.LaboratorijaDTO;
import org.baze.neuronauka.model.ResursiDTO;

import java.util.List;
import java.util.function.Consumer;

public class ResursiContent {

    // ── leva strana ───────────────────────────────────────────────
    private final ComboBox<LaboratorijaDTO> labSveCombo  = new ComboBox<>();
    private final TableView<ResursiDTO>     resursiTable = new TableView<>();

    // ── desna strana ──────────────────────────────────────────────
    private final ComboBox<LaboratorijaDTO> labCekanjeCombo = new ComboBox<>();
    private final TableView<HitanResursDTO> hitniTable      = new TableView<>();
    private final Button                    dodajSveBtn     = new Button("Dodaj sve i nastavi izvođenja");

    // ── placeholder-i i wrapper-i — prave se jednom ───────────────
    private final Label     placeholderLevo  = new Label("Odaberite laboratoriju da vidite zalihe.");
    private final Label     placeholderDesno = new Label("Odaberite laboratoriju da vidite neophodne resurse.");
    private final StackPane phLevo           = new StackPane(placeholderLevo);
    private final StackPane phDesno          = new StackPane(placeholderDesno);
    private final VBox      tableWrapperLevo  = new VBox(resursiTable);
    private final VBox      tableWrapperDesno = new VBox(hitniTable);

    // ── root node — uvek isti ─────────────────────────────────────
    private final HBox root;

    // ── callbacks ─────────────────────────────────────────────────
    private Consumer<ResursiDTO> onPlus;

    // ─────────────────────────────────────────────────────────────
    // KONSTRUKTOR
    // ─────────────────────────────────────────────────────────────

    public ResursiContent() {
        initResuriTable();
        initHitniTable();

        // levo
        placeholderLevo.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 13px;");
        VBox.setVgrow(phLevo, Priority.ALWAYS);
        tableWrapperLevo.setVisible(false);
        tableWrapperLevo.setManaged(false);
        VBox.setVgrow(tableWrapperLevo, Priority.ALWAYS);

        labSveCombo.setPromptText("— odaberi laboratoriju —");
        labSveCombo.setMaxWidth(Double.MAX_VALUE);
        labSveCombo.setStyle("-fx-font-size: 12px;");

        VBox levo = new VBox(10,
                sectionTitle("Dostupne zalihe"),
                filterLabel("Laboratorija"),
                labSveCombo,
                phLevo,
                tableWrapperLevo);
        levo.setPadding(new Insets(16, 12, 16, 20));
        HBox.setHgrow(levo, Priority.ALWAYS);

        // desno
        placeholderDesno.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 13px;");
        VBox.setVgrow(phDesno, Priority.ALWAYS);
        tableWrapperDesno.setVisible(false);
        tableWrapperDesno.setManaged(false);
        VBox.setVgrow(tableWrapperDesno, Priority.ALWAYS);

        labCekanjeCombo.setPromptText("— odaberi laboratoriju —");
        labCekanjeCombo.setMaxWidth(Double.MAX_VALUE);
        labCekanjeCombo.setStyle("-fx-font-size: 12px;");

        dodajSveBtn.setMaxWidth(Double.MAX_VALUE);
        dodajSveBtn.setStyle(
                "-fx-background-color: #534AB7; -fx-text-fill: white; " +
                        "-fx-font-size: 13px; -fx-font-weight: bold; " +
                        "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 16 8 16;");

        VBox desno = new VBox(10,
                sectionTitle("Neophodni resursi"),
                filterLabel("Laboratorija (na čekanju)"),
                labCekanjeCombo,
                phDesno,
                tableWrapperDesno,
                dodajSveBtn);
        desno.setPadding(new Insets(16, 20, 16, 12));
        desno.setPrefWidth(420);
        desno.setMinWidth(360);

        root = new HBox(0, levo, new Separator(Orientation.VERTICAL), desno);
        HBox.setHgrow(root, Priority.ALWAYS);
        root.setStyle("-fx-background-color: #f5f5f5;");
    }

    private void initResuriTable() {
        TableColumn<ResursiDTO, String> colNaziv = new TableColumn<>("Resurs");
        colNaziv.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getNaziv()));
        colNaziv.setPrefWidth(200);

        TableColumn<ResursiDTO, Number> colKol = new TableColumn<>("Količina");
        colKol.setCellValueFactory(c ->
                new SimpleObjectProperty<>(c.getValue().getDostupnaKolicina()));
        colKol.setPrefWidth(100);
        colKol.setStyle("-fx-alignment: CENTER;");

        TableColumn<ResursiDTO, Void> colAkcija = new TableColumn<>("Dodaj");
        colAkcija.setPrefWidth(80);
        colAkcija.setStyle("-fx-alignment: CENTER;");
        colAkcija.setCellFactory(col -> new TableCell<>() {
            private final Button plusBtn = new Button("+");
            {
                styleKolicBtn(plusBtn);
                plusBtn.setOnAction(e -> {
                    ResursiDTO item = getTableView().getItems().get(getIndex());
                    if (onPlus != null) onPlus.accept(item);
                });
            }
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : plusBtn);
            }
        });

        resursiTable.getColumns().addAll(colNaziv, colKol, colAkcija);
        resursiTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        resursiTable.setPlaceholder(new Label("Nema zaliha za ovu laboratoriju."));
        VBox.setVgrow(resursiTable, Priority.ALWAYS);
    }

    private void initHitniTable() {
        TableColumn<HitanResursDTO, String> colNaziv = new TableColumn<>("Resurs");
        colNaziv.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getNaziv()));
        colNaziv.setPrefWidth(200);

        TableColumn<HitanResursDTO, Number> colFali = new TableColumn<>("Količina koja fali");
        colFali.setCellValueFactory(c ->
                new SimpleObjectProperty<>(c.getValue().getKolicineFali()));
        colFali.setPrefWidth(140);
        colFali.setStyle("-fx-alignment: CENTER;");

        hitniTable.getColumns().addAll(colNaziv, colFali);
        hitniTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        hitniTable.setPlaceholder(new Label("Nema neophodnih resursa."));
        VBox.setVgrow(hitniTable, Priority.ALWAYS);
    }

    // ─────────────────────────────────────────────────────────────
    // BUILD — uvek vraća isti root node
    // ─────────────────────────────────────────────────────────────

    public Node build() {
        return root;
    }

    // ── METODE KOJE CONTROLLER POZIVA ─────────────────────────────

    public void setLaboratorijeSve(List<LaboratorijaDTO> lista) {
        labSveCombo.getItems().setAll(lista);
    }

    public void setLaboratorijeCekanje(List<LaboratorijaDTO> lista) {
        labCekanjeCombo.getItems().setAll(lista);
    }

    public void prikaziZalihe(List<ResursiDTO> lista) {
        resursiTable.getItems().setAll(lista);
        phLevo.setVisible(false);
        phLevo.setManaged(false);
        tableWrapperLevo.setVisible(true);
        tableWrapperLevo.setManaged(true);
    }

    public void resetZalihe() {
        resursiTable.getItems().clear();
        tableWrapperLevo.setVisible(false);
        tableWrapperLevo.setManaged(false);
        phLevo.setVisible(true);
        phLevo.setManaged(true);
    }

    public void prikaziHitne(List<HitanResursDTO> lista) {
        hitniTable.getItems().setAll(lista);
        phDesno.setVisible(false);
        phDesno.setManaged(false);
        tableWrapperDesno.setVisible(true);
        tableWrapperDesno.setManaged(true);
    }

    public void resetHitne() {
        hitniTable.getItems().clear();
        tableWrapperDesno.setVisible(false);
        tableWrapperDesno.setManaged(false);
        phDesno.setVisible(true);
        phDesno.setManaged(true);
    }

    public void osvežiStavkuZalihe(ResursiDTO updated) {
        var items = resursiTable.getItems();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getIdResurs() == updated.getIdResurs()) {
                items.set(i, updated);
                break;
            }
        }
    }

    // ── GETTERI ───────────────────────────────────────────────────

    public ComboBox<LaboratorijaDTO> getLabSveCombo()     { return labSveCombo;     }
    public ComboBox<LaboratorijaDTO> getLabCekanjeCombo() { return labCekanjeCombo; }
    public TableView<ResursiDTO>     getResuriTable()     { return resursiTable;    }
    public Button                    getDodajSveBtn()     { return dodajSveBtn;     }

    public void setOnPlus(Consumer<ResursiDTO> handler)  { this.onPlus = handler; }

    // ── HELPERS ───────────────────────────────────────────────────

    private Label sectionTitle(String tekst) {
        Label l = new Label(tekst);
        l.setFont(Font.font("System", FontWeight.BOLD, 15));
        l.setStyle("-fx-text-fill: #333333;");
        return l;
    }

    private Label filterLabel(String tekst) {
        Label l = new Label(tekst);
        l.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; " +
                "-fx-text-fill: #888888; -fx-padding: 6 0 0 0;");
        return l;
    }

    private void styleKolicBtn(Button btn) {
        String base =
                "-fx-font-size: 14px; -fx-font-weight: bold;" +
                        "-fx-background-color: #534AB7;" +
                        "-fx-text-fill: white;" +
                        "-fx-min-width: 28px; -fx-min-height: 28px;" +
                        "-fx-background-radius: 6; -fx-border-radius: 6; -fx-cursor: hand;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(base.replace("#534AB7", "#3C3489")));
        btn.setOnMouseExited(e  -> btn.setStyle(base));
    }
}