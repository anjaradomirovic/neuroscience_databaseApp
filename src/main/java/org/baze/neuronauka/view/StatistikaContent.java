package org.baze.neuronauka.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import org.baze.neuronauka.model.StatistikaDTO;
import org.baze.neuronauka.model.StatistikaDTO.EksperimentStatistika;
import org.baze.neuronauka.model.StatistikaDTO.LaboratorijaStatistika;
import org.baze.neuronauka.model.StatistikaDTO.SumarneKartice;

import java.util.List;

public class StatistikaContent {

    private final Button osveziBtn = new Button("↻  Osveži");

    // ── sumarne kartice ───────────────────────────────────────────
    private final Label karticaUkupno   = new Label("—");
    private final Label karticaAktivna  = new Label("—");
    private final Label karticaZavrsena = new Label("—");
    private final Label karticaProsek   = new Label("—");

    // ── tabele ────────────────────────────────────────────────────
    private final TableView<EksperimentStatistika>  ekspTabela = new TableView<>();
    private final TableView<LaboratorijaStatistika> labTabela  = new TableView<>();

    // ── callback ──────────────────────────────────────────────────
    private Runnable onOsvezi;

    // ─────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public Node build() {

        // ── toolbar ───────────────────────────────────────────────
        osveziBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #534AB7;" +
                        "-fx-font-size: 12px; -fx-background-radius: 6; -fx-cursor: hand;" +
                        "-fx-border-color: #534AB7; -fx-border-radius: 6; -fx-padding: 5 14 5 14;");
        osveziBtn.setOnAction(e -> { if (onOsvezi != null) onOsvezi.run(); });

        HBox toolbar = new HBox(8, osveziBtn);
        toolbar.setPadding(new Insets(10, 16, 10, 16));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle(
                "-fx-background-color: #fafafa;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 0 0 1 0;");

        // ── sumarne kartice ───────────────────────────────────────
        HBox kartice = new HBox(12,
                kartica("Ukupno izvodjenja", karticaUkupno,  "#534AB7"),
                kartica("Aktivna",           karticaAktivna, "#854F0B"),
                kartica("Završena",          karticaZavrsena,"#3B6D11"),
                kartica("Prosečna uspešnost",karticaProsek,  "#185FA5")
        );
        kartice.setPadding(new Insets(16, 16, 8, 16));
        for (Node n : kartice.getChildren())
            HBox.setHgrow(n, Priority.ALWAYS);

        // ── tabela eksperimenata ───────────────────────────────────
        TableColumn<EksperimentStatistika, String> cNaziv =
                ekspCol("Eksperiment", 200, s -> s.getNaziv());
        TableColumn<EksperimentStatistika, String> cUkupno =
                ekspCol("Ukupno", 70, s -> String.valueOf(s.getUkupno()));
        TableColumn<EksperimentStatistika, String> cAkt =
                ekspCol("Aktivna", 70, s -> String.valueOf(s.getAktivna()));
        TableColumn<EksperimentStatistika, String> cUsp =
                ekspCol("Uspešno", 70, s -> String.valueOf(s.getUspesno()));
        TableColumn<EksperimentStatistika, String> cNeusp =
                ekspCol("Neuspešno", 80, s -> String.valueOf(s.getNeuspesno()));

        // kolona sa progress bar-om za uspešnost
        TableColumn<EksperimentStatistika, String> cProc = new TableColumn<>("Uspešnost");
        cProc.setMinWidth(140);
        cProc.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        String.format("%.1f%%", data.getValue().getProcenatUspesnosti())));
        cProc.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || getTableRow() == null
                        || getTableRow().getItem() == null) {
                    setGraphic(null); return;
                }
                double pct = ((EksperimentStatistika) getTableRow().getItem())
                        .getProcenatUspesnosti();
                setGraphic(progressRow(pct));
                setText(null);
            }
        });

        ekspTabela.getColumns().addAll(cNaziv, cUkupno, cAkt, cUsp, cNeusp, cProc);
        ekspTabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        ekspTabela.setPlaceholder(new Label("Nema podataka."));
        ekspTabela.setStyle("-fx-border-color: transparent;");

        Label ekspNaslov = sectionHeader("Po eksperimentu");
        VBox ekspSekcija = new VBox(0, ekspNaslov, ekspTabela);
        VBox.setVgrow(ekspTabela, Priority.ALWAYS);
        ekspSekcija.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0;" +
                "-fx-border-radius: 8; -fx-background-radius: 8;");

        // ── tabela laboratorija ────────────────────────────────────
        TableColumn<LaboratorijaStatistika, String> lNaziv =
                labCol("Laboratorija", 220, s -> s.getNaziv());
        TableColumn<LaboratorijaStatistika, String> lZavr =
                labCol("Završenih izvodjenja", 180, s -> String.valueOf(s.getZavrsenih()));

        // bar chart kolona
        TableColumn<LaboratorijaStatistika, String> lBar = new TableColumn<>("Pregled");
        lBar.setMinWidth(160);
        lBar.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(""));
        lBar.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); return;
                }
                LaboratorijaStatistika row =
                        (LaboratorijaStatistika) getTableRow().getItem();
                int max = labTabela.getItems().stream()
                        .mapToInt(LaboratorijaStatistika::getZavrsenih).max().orElse(1);
                double pct = max > 0 ? (row.getZavrsenih() * 100.0 / max) : 0;
                setGraphic(barOnly(pct, "#534AB7"));
                setText(null);
            }
        });

        labTabela.getColumns().addAll(lNaziv, lZavr, lBar);
        labTabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        labTabela.setPlaceholder(new Label("Nema podataka."));
        labTabela.setStyle("-fx-border-color: transparent;");
        labTabela.setPrefHeight(200);

        Label labNaslov = sectionHeader("Po laboratoriji — završena izvodjenja");
        VBox labSekcija = new VBox(0, labNaslov, labTabela);
        labSekcija.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0;" +
                "-fx-border-radius: 8; -fx-background-radius: 8;");

        // ── sklopi sve ────────────────────────────────────────────
        VBox sadrzaj = new VBox(16, kartice, ekspSekcija, labSekcija);
        sadrzaj.setPadding(new Insets(0, 16, 16, 16));
        VBox.setVgrow(ekspSekcija, Priority.ALWAYS);

        VBox root = new VBox(0, toolbar, sadrzaj);
        root.setStyle("-fx-background-color: #f9f9f9;");
        VBox.setVgrow(sadrzaj, Priority.ALWAYS);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #f9f9f9; -fx-background: #f9f9f9;");
        return scroll;
    }

    // ── PUNJENJE PODATAKA ─────────────────────────────────────────

    public void prikaziKartice(SumarneKartice k) {
        karticaUkupno.setText(String.valueOf(k.getUkupno()));
        karticaAktivna.setText(String.valueOf(k.getAktivna()));
        karticaZavrsena.setText(String.valueOf(k.getZavrsena()));
        karticaProsek.setText(String.format("%.1f%%", k.getProsekUspesnosti()));
    }

    public void prikaziEksperimente(List<EksperimentStatistika> lista) {
        ekspTabela.getItems().setAll(lista);
    }

    public void prikaziLaboratorije(List<LaboratorijaStatistika> lista) {
        labTabela.getItems().setAll(lista);
    }

    public Button getOsveziBtn()             { return osveziBtn; }
    public void   setOnOsvezi(Runnable r)    { this.onOsvezi = r; }

    // ── HELPERS ───────────────────────────────────────────────────

    private VBox kartica(String lbl, Label valLabel, String boja) {
        Label l = new Label(lbl);
        l.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");
        valLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + boja + ";");
        VBox card = new VBox(4, l, valLabel);
        card.setPadding(new Insets(12, 16, 12, 16));
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #e8e8e8;" +
                        "-fx-border-radius: 8; -fx-background-radius: 8;");
        return card;
    }

    private Label sectionHeader(String t) {
        Label l = new Label(t);
        l.setStyle(
                "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #555;" +
                        "-fx-padding: 10 14 10 14;" +
                        "-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        l.setMaxWidth(Double.MAX_VALUE);
        l.setStyle(l.getStyle() + "-fx-background-color: #fafafa;");
        return l;
    }

    private HBox progressRow(double pct) {
        double clamped = Math.min(100, Math.max(0, pct));
        String boja = clamped >= 70 ? "#639922" : clamped >= 40 ? "#BA7517" : "#A32D2D";

        StackPane bar = new StackPane();
        bar.setMinWidth(80);
        bar.setPrefWidth(80);
        bar.setMaxHeight(8);
        bar.setMinHeight(8);
        bar.setStyle("-fx-background-color: #eeeeee; -fx-background-radius: 4;");

        Rectangle fill = new Rectangle();
        fill.setHeight(8);
        fill.setWidth(80 * clamped / 100.0);
        fill.setArcWidth(4);
        fill.setArcHeight(4);
        fill.setFill(javafx.scene.paint.Color.web(boja));
        bar.getChildren().add(fill);
        StackPane.setAlignment(fill, Pos.CENTER_LEFT);

        Label pctLbl = new Label(String.format("%.0f%%", clamped));
        pctLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: " + boja + "; -fx-min-width: 36;");

        HBox row = new HBox(8, bar, pctLbl);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private HBox barOnly(double pct, String boja) {
        double clamped = Math.min(100, Math.max(0, pct));
        StackPane bar = new StackPane();
        bar.setMinWidth(100);
        bar.setPrefWidth(100);
        bar.setMaxHeight(8);
        bar.setMinHeight(8);
        bar.setStyle("-fx-background-color: #eeeeee; -fx-background-radius: 4;");

        Rectangle fill = new Rectangle();
        fill.setHeight(8);
        fill.setWidth(100 * clamped / 100.0);
        fill.setArcWidth(4);
        fill.setArcHeight(4);
        fill.setFill(javafx.scene.paint.Color.web(boja));
        bar.getChildren().add(fill);
        StackPane.setAlignment(fill, Pos.CENTER_LEFT);

        HBox row = new HBox(bar);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private <T> TableColumn<T, String> ekspCol(
            String naslov, double minW,
            java.util.function.Function<EksperimentStatistika, String> getter) {
        TableColumn<T, String> col = new TableColumn<>(naslov);
        col.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        getter.apply((EksperimentStatistika) data.getValue())));
        col.setMinWidth(minW);
        return col;
    }

    private <T> TableColumn<T, String> labCol(
            String naslov, double minW,
            java.util.function.Function<LaboratorijaStatistika, String> getter) {
        TableColumn<T, String> col = new TableColumn<>(naslov);
        col.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        getter.apply((LaboratorijaStatistika) data.getValue())));
        col.setMinWidth(minW);
        return col;
    }
}