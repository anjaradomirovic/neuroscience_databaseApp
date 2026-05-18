package org.baze.neuronauka.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

public class NovoIzvodjenjeFormView {

    private final ComboBox<String[]>  ekspCombo  = new ComboBox<>();
    private final ComboBox<String[]>  labCombo   = new ComboBox<>();
    private final DatePicker          datumPicker = new DatePicker(LocalDate.now());
    private final Button              sacuvajBtn = new Button("Kreiraj izvodjenje");
    private final Button              otkaziBtn  = new Button("Otkaži");

    private Stage dialog;

    public void show(Stage parentStage) {
        dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(parentStage);
        dialog.setTitle("Novo izvodjenje");
        dialog.setResizable(false);

        ekspCombo.setCellFactory(lv -> strArrCell("Odaberi eksperiment…", 1));
        ekspCombo.setButtonCell(strArrCell("Odaberi eksperiment…", 1));
        ekspCombo.setMaxWidth(Double.MAX_VALUE);

        labCombo.setCellFactory(lv -> strArrCell("Odaberi laboratoriju…", 1));
        labCombo.setButtonCell(strArrCell("Odaberi laboratoriju…", 1));
        labCombo.setMaxWidth(Double.MAX_VALUE);

        datumPicker.setMaxWidth(Double.MAX_VALUE);

        VBox forma = new VBox(14,
                formRow("Eksperiment", ekspCombo),
                formRow("Laboratorija", labCombo),
                formRow("Datum početka", datumPicker)
        );
        forma.setPadding(new Insets(20));
        forma.setStyle("-fx-background-color: white;");

        // info label
        Label info = new Label(
                "Procedura će proveriti dostupnost resursa u laboratoriji.\n" +
                        "Ako resursa nema dovoljno, izvodjenje će biti odloženo.");
        info.setWrapText(true);
        info.setStyle("-fx-font-size: 11px; -fx-text-fill: #888; -fx-padding: 0 20 0 20;");

        sacuvajBtn.setStyle(
                "-fx-background-color: #534AB7; -fx-text-fill: white;" +
                        "-fx-font-size: 12px; -fx-background-radius: 6;" +
                        "-fx-cursor: hand; -fx-padding: 6 18 6 18;");
        otkaziBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #888;" +
                        "-fx-font-size: 12px; -fx-cursor: hand;" +
                        "-fx-border-color: #ccc; -fx-border-radius: 6; -fx-padding: 6 14 6 14;");
        otkaziBtn.setOnAction(e -> dialog.close());

        HBox btnRow = new HBox(8, sacuvajBtn, otkaziBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(12, 16, 12, 16));
        btnRow.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1 0 0 0;");

        VBox root = new VBox(0, forma, info, btnRow);
        root.setStyle("-fx-background-color: white;");

        dialog.setScene(new Scene(root, 440, 280));
        dialog.show();
    }

    // ── GETTERI ───────────────────────────────────────────────────

    public Button    getSacuvajBtn()    { return sacuvajBtn; }
    public String[]  getOdabraniEksp()  { return ekspCombo.getValue(); }
    public String[]  getOdabranaLab()   { return labCombo.getValue(); }
    public LocalDate getDatum()         { return datumPicker.getValue(); }

    public void setEksperimenti(List<String[]> lista) {
        ekspCombo.getItems().setAll(lista);
    }

    public void setLaboratorije(List<String[]> lista) {
        labCombo.getItems().setAll(lista);
    }

    public void close() { if (dialog != null) dialog.close(); }

    // ── HELPERS ───────────────────────────────────────────────────

    private HBox formRow(String lbl, Control field) {
        Label l = new Label(lbl);
        l.setMinWidth(110);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
        HBox row = new HBox(10, l, field);
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(field, Priority.ALWAYS);
        field.setMaxWidth(Double.MAX_VALUE);
        return row;
    }

    private ListCell<String[]> strArrCell(String placeholder, int idx) {
        return new ListCell<>() {
            @Override protected void updateItem(String[] item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? placeholder : item[idx]);
            }
        };
    }
}