package org.baze.neuronauka.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.baze.neuronauka.model.IzvodjenjeDTO;
import org.baze.neuronauka.model.SesijaResursDTO;
import org.baze.neuronauka.model.SesijaResursDTO;
import java.util.List;

public class NovaSesijaView {

    // ── editable polja ────────────────────────────────────────────
    private final DatePicker             datumPicker   = new DatePicker();
    private final Spinner<Integer>       pocetakHH     = new Spinner<>(0, 23, 8);
    private final Spinner<Integer>       pocetakMM     = new Spinner<>(0, 59, 0);
    private final Spinner<Integer>       krajHH        = new Spinner<>(0, 23, 9);
    private final Spinner<Integer>       krajMM        = new Spinner<>(0, 59, 0);
    private final ComboBox<String>       tipSesijeBox  = new ComboBox<>();
    private final ComboBox<IzvodjenjeDTO> izvodjenjeBox = new ComboBox<>();
    // ── novo polje ────────────────────────────────────────────────
    private final ListView<SesijaResursDTO> resursiListView = new ListView<>();

    // ── dugmad ────────────────────────────────────────────────────
    private final Button saveBtn   = new Button("Dodaj sesiju");
    private final Button cancelBtn = new Button("Odustani");

    // ── status poruka ─────────────────────────────────────────────
    private final Label statusLabel = new Label();

    // ── referenca na dialog Stage ─────────────────────────────────
    private Stage dialogStage;

    // ─────────────────────────────────────────────────────────────
    public void show(Stage parentStage, List<IzvodjenjeDTO> izvodjenja) {
        izvodjenjeBox.getItems().setAll(izvodjenja);

        dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(parentStage);
        dialogStage.setTitle("Nova sesija");
        dialogStage.setResizable(true);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #f5f5f5;");

        // ── buildBody() se poziva JEDNOM ──────────────────────────
        VBox body = buildBody();
        ScrollPane scroll = new ScrollPane(body);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #f5f5f5; -fx-background: #f5f5f5;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // ── buildHeader() i buildFooter() su OK jer nemaju polja ──
        root.getChildren().addAll(buildHeader(), scroll, buildFooter());

        Scene scene = new Scene(root, 460, 580);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE)
                dialogStage.close();
        });

        dialogStage.setScene(scene);
        dialogStage.setMinWidth(400);
        dialogStage.setMinHeight(400);
        dialogStage.show();
    }

    // ── HEADER ────────────────────────────────────────────────────
    private HBox buildHeader() {
        Label naslov = new Label("Nova sesija");
        naslov.setFont(Font.font("System", FontWeight.BOLD, 15));

        HBox header = new HBox(naslov);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 18, 14, 18));
        header.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 0 0 1 0;"
        );
        return header;
    }

    // ── BODY ──────────────────────────────────────────────────────
    private VBox cachedBody = null;

    private VBox buildBody() {
        if (cachedBody != null) return cachedBody;

        VBox body = new VBox(14);
        body.setPadding(new Insets(20, 18, 10, 18));

        body.getChildren().add(buildFieldLabel("Izvodjenje"));
        izvodjenjeBox.setPromptText("— odaberi izvodjenje —");
        izvodjenjeBox.setMaxWidth(Double.MAX_VALUE);
        izvodjenjeBox.setStyle("-fx-font-size: 12px;");
        body.getChildren().add(izvodjenjeBox);

        body.getChildren().add(buildSeparator());

        body.getChildren().add(buildFieldLabel("Datum sesije"));
        datumPicker.setMaxWidth(Double.MAX_VALUE);
        datumPicker.setPromptText("Odaberi datum");
        datumPicker.setStyle("-fx-font-size: 13px;");
        body.getChildren().add(datumPicker);

        body.getChildren().add(buildFieldLabel("Vreme početka"));
        body.getChildren().add(buildVremeRow(pocetakHH, pocetakMM));

        body.getChildren().add(buildFieldLabel("Vreme kraja"));
        body.getChildren().add(buildVremeRow(krajHH, krajMM));

        body.getChildren().add(buildFieldLabel("Tip sesije"));
        tipSesijeBox.getItems().addAll(
                "baseline", "stimulus", "recovery", "wash_out",
                "experimental", "calibration", "training", "follow_up", "control"
        );
        tipSesijeBox.setMaxWidth(Double.MAX_VALUE);
        tipSesijeBox.setStyle("-fx-font-size: 13px;");
        body.getChildren().add(tipSesijeBox);

        body.getChildren().add(buildSeparator());
        body.getChildren().add(buildFieldLabel("Resursi izvodjenja (podesite količine)"));

        resursiListView.setPrefHeight(160);
        resursiListView.setStyle("-fx-font-size: 12px;");
        resursiListView.setPlaceholder(new Label("Odaberite izvodjenje da vidite resurse."));
        resursiListView.setCellFactory(lv -> new ListCell<>() {
            private final Label     nazivLbl = new Label();
            private final TextField kolField = new TextField();
            private final HBox      row      = new HBox(12, nazivLbl, kolField);
            {
                row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                HBox.setHgrow(nazivLbl, Priority.ALWAYS);
                nazivLbl.setMinWidth(180);
                kolField.setPrefWidth(90);
                kolField.setPromptText("količina");
                kolField.setStyle("-fx-font-size: 12px;");
                kolField.textProperty().addListener((obs, old, val) -> {
                    if (!val.matches("\\d*\\.?\\d*")) {
                        kolField.setText(old);
                        return;
                    }
                    if (getItem() != null && !val.isBlank()) {
                        try {
                            getItem().setKolicina(Double.parseDouble(val));
                        } catch (NumberFormatException ignored) {}
                    }
                });
            }
            @Override
            protected void updateItem(SesijaResursDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                nazivLbl.setText(item.getNaziv());
                kolField.setText(String.valueOf(item.getKolicina()));
                setGraphic(row);
            }
        });
        body.getChildren().add(resursiListView);

        // statusLabel se dodaje SAMO JEDNOM, na kraju
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #cc3333;");
        statusLabel.setWrapText(true);
        body.getChildren().add(statusLabel);

        cachedBody = body;
        return body;
    }

    // ── FOOTER ────────────────────────────────────────────────────
    private HBox buildFooter() {
        styleSaveBtn();
        styleCancelBtn();

        cancelBtn.setOnAction(e -> dialogStage.close());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox footer = new HBox(10, cancelBtn, spacer, saveBtn);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(14, 18, 14, 18));
        footer.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1 0 0 0;"
        );
        return footer;
    }

    // ── HELPERS ───────────────────────────────────────────────────

    private HBox buildVremeRow(Spinner<Integer> hh, Spinner<Integer> mm) {
        hh.setEditable(true);
        mm.setEditable(true);
        hh.setPrefWidth(72);
        mm.setPrefWidth(72);
        hh.setStyle("-fx-font-size: 13px;");
        mm.setStyle("-fx-font-size: 13px;");

        StringConverter<Integer> dvaBroja = new StringConverter<>() {
            public String toString(Integer v)   { return v == null ? "00" : String.format("%02d", v); }
            public Integer fromString(String s) {
                try { return Integer.parseInt(s.trim()); }
                catch (NumberFormatException e) { return 0; }
            }
        };
        hh.getValueFactory().setConverter(dvaBroja);
        mm.getValueFactory().setConverter(dvaBroja);

        Label dvotacka = new Label(":");
        dvotacka.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #555555;");

        HBox row = new HBox(6, hh, dvotacka, mm);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Label buildFieldLabel(String tekst) {
        Label l = new Label(tekst);
        l.setStyle(
                "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #888888;"
        );
        return l;
    }

    private Separator buildSeparator() {
        Separator sep = new Separator();
        sep.setPadding(new Insets(2, 0, 2, 0));
        return sep;
    }

    private void styleSaveBtn() {
        String base =
                "-fx-background-color: #534AB7;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 8 20 8 20;" +
                        "-fx-cursor: hand;";
        saveBtn.setStyle(base);
        saveBtn.setOnMouseEntered(e -> saveBtn.setStyle(base.replace("#534AB7", "#3C3489")));
        saveBtn.setOnMouseExited(e  -> saveBtn.setStyle(base));
    }

    private void styleCancelBtn() {
        String base =
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #888888;" +
                        "-fx-font-size: 13px;" +
                        "-fx-border-color: #d0d0d0;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 8 20 8 20;" +
                        "-fx-cursor: hand;";
        cancelBtn.setStyle(base);
        cancelBtn.setOnMouseEntered(e -> cancelBtn.setStyle(
                base.replace("-fx-background-color: transparent;", "-fx-background-color: #f0f0f0;")));
        cancelBtn.setOnMouseExited(e  -> cancelBtn.setStyle(base));
    }

    // ── GETTERI ───────────────────────────────────────────────────
    public DatePicker              getDatumPicker()   { return datumPicker;   }
    public Spinner<Integer>        getPocetakHH()     { return pocetakHH;     }
    public Spinner<Integer>        getPocetakMM()     { return pocetakMM;     }
    public Spinner<Integer>        getKrajHH()        { return krajHH;        }
    public Spinner<Integer>        getKrajMM()        { return krajMM;        }
    public ComboBox<String>        getTipSesijeBox()  { return tipSesijeBox;  }
    public ComboBox<IzvodjenjeDTO> getIzvodjenjeBox() { return izvodjenjeBox; }
    public Button                  getSaveBtn()       { return saveBtn;       }
    public Button                  getCancelBtn()     { return cancelBtn;     }
    public Label                   getStatusLabel()   { return statusLabel;   }
    public Stage                   getDialogStage()   { return dialogStage;   }
    public ListView<SesijaResursDTO> getResuriListView() { return resursiListView; }
}