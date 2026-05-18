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
import org.baze.neuronauka.model.TipAlataDTO;

import java.util.List;

public class DodajAlatView {

    private final Stage stage = new Stage();

    private final TextField             nazivField        = new TextField();
    private final TextField             serijskiBrojField = new TextField();
    private final DatePicker            datumNabavke      = new DatePicker();
    private final DatePicker            datumProizvodnje  = new DatePicker();
    private final ComboBox<TipAlataDTO> tipCombo          = new ComboBox<>();
    private final DatePicker            datumKalibracije  = new DatePicker();
    private final TextField             intervalField     = new TextField();
    private final TextArea              nacinUpotrebe     = new TextArea();

    private final Button sacuvajBtn = new Button("Sačuvaj");
    private final Button otkaziBtn  = new Button("Otkaži");

    public DodajAlatView(String nazivLaboratorije) {

        // ── naslov ────────────────────────────────────────────────
        Label title = new Label("Dodavanje novog alata");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        title.setStyle("-fx-text-fill: #1a1a1a;");

        Label labInfo = new Label("Laboratorija:  " + nazivLaboratorije);
        labInfo.setStyle(
                "-fx-font-size: 13px; -fx-font-weight: bold;" +
                        "-fx-text-fill: #0F6E56; -fx-padding: 0 0 6 0;");

        Separator sep = new Separator();

        // ── grid sa poljima ───────────────────────────────────────
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(12);
        grid.setPadding(new Insets(12, 0, 12, 0));

        // podesi širinu kolone sa labelama
        ColumnConstraints col0 = new ColumnConstraints(190);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col0, col1);

        nazivField.setPromptText("npr. Coulbourn H21-01");
        serijskiBrojField.setPromptText("npr. CLB-H21-2024-001");
        intervalField.setPromptText("npr. 365");
        nacinUpotrebe.setPromptText("Kratak opis načina upotrebe...");
        nacinUpotrebe.setPrefRowCount(3);
        nacinUpotrebe.setWrapText(true);
        tipCombo.setPromptText("— odaberi tip —");
        tipCombo.setMaxWidth(Double.MAX_VALUE);

        dodajRed(grid, 0, "Naziv alata  [naziv]:",              nazivField);
        dodajRed(grid, 1, "Serijski broj  [serijski_broj]:",    serijskiBrojField);
        dodajRed(grid, 2, "Tip alata  [id_tip_alata]:",         tipCombo);
        dodajRed(grid, 3, "Datum nabavke  [datum_nabavke]:",    datumNabavke);
        dodajRed(grid, 4, "Datum proizvodnje  [datum_proizvodnje]:", datumProizvodnje);
        dodajRed(grid, 5, "Poslednja kalibracija  [datum_poslednje_kalibracije]:", datumKalibracije);
        dodajRed(grid, 6, "Interval kalibracije u danima  [interval_kalibracije_dani]:", intervalField);
        dodajRed(grid, 7, "Način upotrebe  [nacin_upotrebe]:",  nacinUpotrebe);

        // ── info labela dole ──────────────────────────────────────
        Label infoLbl = new Label("Status alata se postavlja automatski na 'aktivan' pri dodavanju.");
        infoLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #888888; -fx-font-style: italic;");

        // ── dugmad ────────────────────────────────────────────────
        stilizujBtn(sacuvajBtn, "#0F6E56", "white");
        stilizujBtn(otkaziBtn,  "#eeeeee", "#333333");
        otkaziBtn.setOnAction(e -> stage.close());

        HBox btnRow = new HBox(10, sacuvajBtn, otkaziBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        // ── root ──────────────────────────────────────────────────
        VBox root = new VBox(10, title, labInfo, sep, grid, infoLbl, new Separator(), btnRow);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f9f9f9;");

        Scene scene = new Scene(root, 520, 560);
        stage.setScene(scene);
        stage.setTitle("Dodaj alat — " + nazivLaboratorije);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
    }

    private void dodajRed(GridPane grid, int row, String labelTekst, Control control) {
        Label lbl = new Label(labelTekst);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #333333; -fx-font-weight: bold;");
        lbl.setWrapText(true);
        grid.add(lbl, 0, row);
        grid.add(control, 1, row);
        GridPane.setHgrow(control, Priority.ALWAYS);
        GridPane.setFillWidth(control, true);
    }

    private void stilizujBtn(Button btn, String bg, String fg) {
        btn.setStyle(
                "-fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";" +
                        "-fx-font-weight: bold; -fx-font-size: 12px;" +
                        "-fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 6 18 6 18;");
        btn.setPrefWidth(110);
    }

    public void show(Stage owner) {
        stage.initOwner(owner);
        stage.showAndWait();
    }

    public void close() { stage.close(); }

    // ── GETTERI ───────────────────────────────────────────────────
    public TextField             getNazivField()        { return nazivField; }
    public TextField             getSerijskiBrojField() { return serijskiBrojField; }
    public DatePicker            getDatumNabavke()      { return datumNabavke; }
    public DatePicker            getDatumProizvodnje()  { return datumProizvodnje; }
    public ComboBox<TipAlataDTO> getTipCombo()          { return tipCombo; }
    public DatePicker            getDatumKalibracije()  { return datumKalibracije; }
    public TextField             getIntervalField()     { return intervalField; }
    public TextArea              getNacinUpotrebe()     { return nacinUpotrebe; }
    public Button                getSacuvajBtn()        { return sacuvajBtn; }
}