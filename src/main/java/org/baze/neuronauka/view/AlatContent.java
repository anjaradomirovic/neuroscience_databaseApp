package org.baze.neuronauka.view;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.baze.neuronauka.model.AlatDetaljiDTO;
import org.baze.neuronauka.model.AlatGrupaDTO;
import org.baze.neuronauka.model.LaboratorijaDTO;

import java.util.List;
import java.util.function.Consumer;

public class AlatContent {

    // ── filter ────────────────────────────────────────────────────
    private final ComboBox<LaboratorijaDTO> labCombo = new ComboBox<>();
    private final Button dodajBtn = new Button("+ Dodaj alat");
    private Runnable onDodajAlat;

    // ── leva tabela (tip alata + ukupno) ──────────────────────────
    private final TableView<AlatGrupaDTO> grupaTable = new TableView<>();

    // ── desna lista (konkretni alati sa toggle) ───────────────────
    private final ListView<AlatDetaljiDTO> detaljiList = new ListView<>();

    // ── placeholder ───────────────────────────────────────────────
    private final Label     placeholderLabel = new Label("Odaberite laboratoriju da vidite alate.");
    private final StackPane ph               = new StackPane(placeholderLabel);
    private final SplitPane splitPane        = new SplitPane();

    // ── root ──────────────────────────────────────────────────────
    private final VBox root;

    // ── callback-ovi ──────────────────────────────────────────────
    private Consumer<AlatGrupaDTO>   onGrupaSelected;
    private Consumer<AlatDetaljiDTO> onToggleDetalj;

    // ─────────────────────────────────────────────────────────────
    public AlatContent() {
        initGrupaTable();
        initDetaljiList();

        placeholderLabel.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 13px;");
        ph.setStyle("-fx-background-color: #f5f5f5;");
        VBox.setVgrow(ph, Priority.ALWAYS);

        // desna strana — placeholder dok ništa nije kliknuto
        Label detaljiPlaceholder = new Label("Kliknite na tip alata.");
        detaljiPlaceholder.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 12px;");
        detaljiList.setPlaceholder(detaljiPlaceholder);

        splitPane.getItems().addAll(grupaTable, detaljiList);
        splitPane.setDividerPositions(0.45);
        splitPane.setVisible(false);
        splitPane.setManaged(false);
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        Label title = new Label("Status alata");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        title.setStyle("-fx-text-fill: #333333;");

        Label filterLbl = new Label("Laboratorija");
        filterLbl.setStyle(
                "-fx-font-size: 11px; -fx-font-weight: bold;" +
                        "-fx-text-fill: #888888; -fx-padding: 4 0 0 0;");

        labCombo.setPromptText("— odaberi laboratoriju —");
        labCombo.setPrefWidth(280);
        labCombo.setStyle("-fx-font-size: 12px;");


        dodajBtn.setStyle(
                "-fx-background-color: #0F6E56; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-font-size: 12px;" +
                        "-fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 5 14 5 14;");
        dodajBtn.setOnAction(e -> { if (onDodajAlat != null) onDodajAlat.run(); });
        dodajBtn.disableProperty().bind(labCombo.valueProperty().isNull());

        HBox filterRow = new HBox(8, filterLbl, labCombo, dodajBtn);
        filterRow.setAlignment(Pos.CENTER_LEFT);


        root = new VBox(12, title, filterRow, ph, splitPane);
        root.setPadding(new Insets(16, 20, 16, 20));
        root.setStyle("-fx-background-color: #f5f5f5;");
        VBox.setVgrow(root, Priority.ALWAYS);
    }

    // ── LEVA TABELA ───────────────────────────────────────────────
    private void initGrupaTable() {
        TableColumn<AlatGrupaDTO, String> colTip = new TableColumn<>("Tip alata");
        colTip.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getTipAlata()));
        colTip.setPrefWidth(200);

        TableColumn<AlatGrupaDTO, Number> colUkupno = new TableColumn<>("Ukupno");
        colUkupno.setCellValueFactory(c ->
                new SimpleIntegerProperty(c.getValue().getUkupnoAlata()));
        colUkupno.setPrefWidth(80);
        colUkupno.setStyle("-fx-alignment: CENTER;");

        grupaTable.getColumns().addAll(colTip, colUkupno);
        grupaTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        grupaTable.setPlaceholder(new Label("Nema alata za ovu laboratoriju."));
        VBox.setVgrow(grupaTable, Priority.ALWAYS);

        // klik na red → poziva controller
        grupaTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, selected) -> {
                    if (selected != null && onGrupaSelected != null)
                        onGrupaSelected.accept(selected);
                });
    }

    // ── DESNA LISTA ───────────────────────────────────────────────
    private void initDetaljiList() {
        detaljiList.setCellFactory(lv -> new ListCell<>() {
            private final Label    idLbl  = new Label();
            private final ToggleButton btn = new ToggleButton();
            private final HBox     row    = new HBox(12, idLbl, btn);
            {
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(4, 8, 4, 8));

                btn.setOnAction(e -> {
                    AlatDetaljiDTO item = getItem();
                    if (item == null) return;
                    boolean aktivan = btn.isSelected();
                    item.setStatusAlata(aktivan ? "aktivan" : "na servisu");
                    stilizujToggle(btn, aktivan);
                    if (onToggleDetalj != null) onToggleDetalj.accept(item);
                });
            }

            @Override
            protected void updateItem(AlatDetaljiDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                idLbl.setText("Alat #" + item.getIdAlat());
                idLbl.setStyle("-fx-font-size: 12px; -fx-min-width: 80px;");
                btn.setSelected(item.isAktivan());
                stilizujToggle(btn, item.isAktivan());
                setGraphic(row);
            }
        });
        VBox.setVgrow(detaljiList, Priority.ALWAYS);
    }

    // ── BUILD ─────────────────────────────────────────────────────
    public Node build() { return root; }


    // ── METODE KOJE CONTROLLER POZIVA ─────────────────────────────

    public void setLaboratorije(List<LaboratorijaDTO> lista) {
        labCombo.getItems().setAll(lista);
    }

    public void prikaziGrupe(List<AlatGrupaDTO> lista) {
        grupaTable.getItems().setAll(lista);
        detaljiList.getItems().clear();
        ph.setVisible(false);
        ph.setManaged(false);
        splitPane.setVisible(true);
        splitPane.setManaged(true);
    }

    public void prikaziDetalje(List<AlatDetaljiDTO> lista) {
        detaljiList.getItems().setAll(lista);
    }

    public void resetPrikaz() {
        grupaTable.getItems().clear();
        detaljiList.getItems().clear();
        splitPane.setVisible(false);
        splitPane.setManaged(false);
        ph.setVisible(true);
        ph.setManaged(true);
    }

    public void osvežiDetalj(AlatDetaljiDTO updated) {
        var items = detaljiList.getItems();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getIdAlat() == updated.getIdAlat()) {
                items.set(i, updated);
                break;
            }
        }
        detaljiList.refresh();
    }
    public void setOnDodajAlat(Runnable h)          { this.onDodajAlat = h; }
    public LaboratorijaDTO getOdabranaLaboratorija() { return labCombo.getValue(); }

    // ── GETTERI / SETTERI ─────────────────────────────────────────
    public ComboBox<LaboratorijaDTO> getLabCombo()              { return labCombo; }
    public void setOnGrupaSelected(Consumer<AlatGrupaDTO> h)    { this.onGrupaSelected = h; }
    public void setOnToggleDetalj(Consumer<AlatDetaljiDTO> h)   { this.onToggleDetalj = h; }

    // ── STYLING ───────────────────────────────────────────────────
    private void stilizujToggle(ToggleButton btn, boolean aktivan) {
        if (aktivan) {
            btn.setText("Aktivan");
            btn.setStyle(
                    "-fx-background-color: #E1F5EE; -fx-text-fill: #0F6E56;" +
                            "-fx-font-weight: bold; -fx-font-size: 11px;" +
                            "-fx-border-color: #0F6E56; -fx-border-radius: 20;" +
                            "-fx-background-radius: 20; -fx-padding: 3 10 3 10; -fx-cursor: hand;");
        } else {
            btn.setText("Na servisu");
            btn.setStyle(
                    "-fx-background-color: #FAEEDA; -fx-text-fill: #854F0B;" +
                            "-fx-font-weight: bold; -fx-font-size: 11px;" +
                            "-fx-border-color: #854F0B; -fx-border-radius: 20;" +
                            "-fx-background-radius: 20; -fx-padding: 3 10 3 10; -fx-cursor: hand;");
        }
    }
}