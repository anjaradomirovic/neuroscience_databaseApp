package org.baze.neuronauka.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

public class SesijaView {

    // ── navigacija ─────────────────────────────────────────────────
    private Button prevMonthBtn  = new Button("‹");
    private Button nextMonthBtn  = new Button("›");
    private Label  monthLabel    = new Label();
    private Spinner<Integer> yearSpinner = new Spinner<>(2000, 2100, YearMonth.now().getYear());

    // ── filteri u sidebaru ─────────────────────────────────────────
    private ComboBox<String> labFilter = new ComboBox<>();
    private ComboBox<String> expFilter = new ComboBox<>();

    // ── kalendarska mreža ──────────────────────────────────────────
    private GridPane calendarGrid = new GridPane();

    // ── lista sesija u sidebaru ────────────────────────────────────
    private ListView<String> sesijaListView = new ListView<>();

    // ── header info ────────────────────────────────────────────────
    private Label userLabel = new Label();

    // ── trenutni mesec/godina ──────────────────────────────────────
    private YearMonth currentYearMonth;

    // ── root layout — čuva se da bi switchCenter radio ─────────────
    private BorderPane root = new BorderPane();

    // ── HEADER TOOLBAR — tri MenuButton-a ─────────────────────────

    // Sesija menu
    private MenuItem pregledSesijaItem = new MenuItem("Pregled sesija");
    private MenuItem novaSesijaItem    = new MenuItem("Nova sesija");
    private MenuButton sesijaMenuBtn   = new MenuButton("Sesija", null,
            pregledSesijaItem, novaSesijaItem);

    // Laboratorija menu
    private MenuItem editLabItem    = new MenuItem("Edit");
    private MenuItem resursiLabItem = new MenuItem("Resursi");
    private MenuItem alatLabItem    = new MenuItem("Alat");
    private MenuButton labMenuBtn   = new MenuButton("Laboratorija", null,
            editLabItem, resursiLabItem, alatLabItem);

    // Eksperiment menu — dodaj izvodjenjeExpItem
    private MenuItem editExpItem        = new MenuItem("Edit");
    private MenuItem statsExpItem       = new MenuItem("Statistika");
    private MenuItem izvodjenjeExpItem  = new MenuItem("Izvodjenje"); // NOVO
    private MenuButton expMenuBtn = new MenuButton("Eksperiment", null,
            editExpItem, statsExpItem, izvodjenjeExpItem);

    // ── aktivni prikaz (za highlight u headeru) ────────────────────
    private enum ActiveView { SESIJE, RESURSI, LABORATORIJA, EKSPERIMENT, OTHER }
    private ActiveView activeView = ActiveView.SESIJE;

    // ──────────────────────────────────────────────────────────────

    public void show(Stage stage, String loggedInUser) {
        userLabel.setText("Korisnik: " + loggedInUser);
        currentYearMonth = YearMonth.now();

        root.setTop(buildTopBar());
        root.setCenter(buildSesijaCenter());
        root.setStyle("-fx-background-color: #f5f5f5;");

        Scene scene = new Scene(root, 1100, 700);
        stage.setScene(scene);
        stage.setTitle("Neuronauka");
        stage.show();

        refreshMonthLabel();
    }

    // ── ZAMENA CENTRALNOG SADRŽAJA ─────────────────────────────────

    /**
     * Menja centralni sadržaj prozora (header ostaje isti).
     * Controller poziva ovu metodu umesto otvaranja novog Stage-a.
     */
    public void switchCenter(Node node) {
        root.setCenter(node);
    }

    /**
     * Vraća prikaz sesija (kalendar + sidebar).
     * Controller poziva ovo kad korisnik klikne "Sesija → Pregled sesija".
     */
    public Node buildSesijaCenter() {
        return buildCenter();
    }

    // ── TOP BAR ────────────────────────────────────────────────────

    public HBox buildTopBar() {
        styleMenuBtn(sesijaMenuBtn, activeView == ActiveView.SESIJE);
        styleMenuBtn(labMenuBtn,    activeView == ActiveView.LABORATORIJA || activeView == ActiveView.RESURSI);
        styleMenuBtn(expMenuBtn,    activeView == ActiveView.EKSPERIMENT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        userLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 13px;");

        HBox topBar = new HBox(10, sesijaMenuBtn, labMenuBtn, expMenuBtn, spacer, userLabel);
        topBar.setPadding(new Insets(10, 20, 10, 20));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 0 0 1 0;"
        );
        return topBar;
    }

    /**
     * Osvežava izgled header dugmadi (aktivan vs neaktivan).
     * Poziva se nakon svakog switchCenter-a.
     */
    public void setActiveView(String viewName) {
        switch (viewName) {
            case "sesije"      -> activeView = ActiveView.SESIJE;
            case "resursi"     -> activeView = ActiveView.RESURSI;
            case "laboratorija"-> activeView = ActiveView.LABORATORIJA;
            case "eksperiment" -> activeView = ActiveView.EKSPERIMENT;
            default            -> activeView = ActiveView.OTHER;
        }
        root.setTop(buildTopBar());
    }

    // ── CENTAR = kalendar (levo) + sidebar (desno) ─────────────────

    private HBox buildCenter() {
        VBox calendarPane = buildCalendarPane();
        VBox sidebar      = buildSidebar();
        HBox.setHgrow(calendarPane, Priority.ALWAYS);
        return new HBox(0, calendarPane, sidebar);
    }

    // ── LEVO — naslov + navigacija + kalendar ──────────────────────

    private VBox buildCalendarPane() {
        VBox pane = new VBox(12);
        pane.setPadding(new Insets(16, 16, 16, 20));
        HBox.setHgrow(pane, Priority.ALWAYS);

        Label title = new Label("Pregled sesija");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        title.setStyle("-fx-text-fill: #333333;");

        pane.getChildren().addAll(title, buildNavRow(), buildDayHeaders(), calendarGrid);

        calendarGrid.setHgap(4);
        calendarGrid.setVgap(4);
        VBox.setVgrow(calendarGrid, Priority.ALWAYS);

        return pane;
    }

    private HBox buildNavRow() {
        styleNavBtn(prevMonthBtn);
        styleNavBtn(nextMonthBtn);

        monthLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        monthLabel.setMinWidth(140);

        yearSpinner.setEditable(true);
        yearSpinner.setPrefWidth(85);
        yearSpinner.setStyle("-fx-font-size: 13px;");

        HBox nav = new HBox(8, prevMonthBtn, monthLabel, nextMonthBtn, yearSpinner);
        nav.setAlignment(Pos.CENTER_LEFT);
        return nav;
    }

    private HBox buildDayHeaders() {
        String[] dani = {"Pon", "Uto", "Sri", "Čet", "Pet", "Sub", "Ned"};
        HBox row = new HBox();
        for (String dan : dani) {
            Label l = new Label(dan);
            l.setAlignment(Pos.CENTER);
            l.setStyle(
                    "-fx-text-fill: #888888;" +
                            "-fx-font-size: 11px;" +
                            "-fx-font-weight: bold;"
            );
            HBox.setHgrow(l, Priority.ALWAYS);
            l.setMaxWidth(Double.MAX_VALUE);
            row.getChildren().add(l);
        }
        return row;
    }

    // ── DESNO — filteri + lista sesija ─────────────────────────────

    private VBox buildSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPrefWidth(230);
        sidebar.setMinWidth(210);
        sidebar.setPadding(new Insets(16, 16, 16, 12));
        sidebar.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 0 0 0 1;"
        );

        Label labLbl = makeFilterLabel("Laboratorija");
        labFilter.setPromptText("— sve laboratorije —");
        labFilter.setMaxWidth(Double.MAX_VALUE);
        labFilter.setStyle("-fx-font-size: 12px;");

        Label expLbl = makeFilterLabel("Eksperiment");
        expFilter.setPromptText("— svi eksperimenti —");
        expFilter.setMaxWidth(Double.MAX_VALUE);
        expFilter.setStyle("-fx-font-size: 12px;");

        Separator sep = new Separator();
        sep.setPadding(new Insets(4, 0, 4, 0));

        Label listLbl = makeFilterLabel("Eksperimenti ovog meseca");
        sesijaListView.setStyle("-fx-font-size: 12px;");
        VBox.setVgrow(sesijaListView, Priority.ALWAYS);

        sidebar.getChildren().addAll(
                labLbl, labFilter,
                expLbl, expFilter,
                sep,
                listLbl, sesijaListView
        );
        return sidebar;
    }

    // ── METODE koje controller poziva ──────────────────────────────

    public void clearCalendarGrid() {
        calendarGrid.getChildren().clear();
    }

    public void addCellToGrid(VBox cell, int col, int row) {
        calendarGrid.add(cell, col, row);
        GridPane.setHgrow(cell, Priority.ALWAYS);
        GridPane.setVgrow(cell, Priority.ALWAYS);
    }

    public VBox makeEmptyCell() {
        VBox cell = new VBox();
        cell.setMinSize(80, 90);
        cell.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: transparent;");
        return cell;
    }

    public VBox makeDayCell(int dayNum, boolean isToday) {
        VBox cell = new VBox(3);
        cell.setMinSize(80, 90);
        cell.setPadding(new Insets(4));
        cell.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: " + (isToday ? "#534AB7" : "#e8e8e8") + ";" +
                        "-fx-border-width: " + (isToday ? "1.5" : "0.5") + ";" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;"
        );

        Label numLabel = new Label(String.valueOf(dayNum));
        numLabel.setStyle(
                "-fx-font-size: 11px;" +
                        "-fx-text-fill: " + (isToday ? "#534AB7" : "#999999") + ";" +
                        (isToday ? "-fx-font-weight: bold;" : "")
        );
        cell.getChildren().add(numLabel);
        return cell;
    }

    public VBox makeSesijaChip(int idSesija, String pocetakVremena, int trajMin,
                               String eksperimentNaziv, String tip) {
        VBox chip = new VBox(1);
        chip.setPadding(new Insets(2, 5, 2, 5));
        chip.setStyle(
                "-fx-border-radius: 4;" +
                        "-fx-background-radius: 4;" +
                        "-fx-border-width: 0 0 0 2.5;" +
                        chipColors(tip)
        );
        chip.setCursor(javafx.scene.Cursor.HAND);

        Label idLbl = new Label("sesija (" + idSesija + ")");
        idLbl.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; " + chipTextColor(tip));

        Label timeLbl = new Label(pocetakVremena + " (" + trajMin + "min)");
        timeLbl.setStyle("-fx-font-size: 9px; " + chipTextColor(tip));

        Label expLbl = new Label(eksperimentNaziv);
        expLbl.setStyle("-fx-font-size: 9px; -fx-text-fill: #666666;");
        expLbl.setMaxWidth(Double.MAX_VALUE);

        chip.getChildren().addAll(idLbl, timeLbl, expLbl);

        Tooltip.install(chip, new Tooltip(
                "sesija " + idSesija + "\n" +
                        eksperimentNaziv + "\n" +
                        pocetakVremena + " | " + trajMin + " min"
        ));
        return chip;
    }

    public void refreshMonthLabel() {
        String mesec = currentYearMonth.getMonth()
                .getDisplayName(TextStyle.FULL_STANDALONE, new Locale("sr"));
        monthLabel.setText(mesec.substring(0, 1).toUpperCase() + mesec.substring(1));
        yearSpinner.getValueFactory().setValue(currentYearMonth.getYear());
    }

    // ── GETTERI ────────────────────────────────────────────────────

    public Button getPrevMonthBtn()             { return prevMonthBtn; }
    public Button getNextMonthBtn()             { return nextMonthBtn; }
    public Spinner<Integer> getYearSpinner()    { return yearSpinner; }
    public ComboBox<String> getLabFilter()      { return labFilter; }
    public ComboBox<String> getExpFilter()      { return expFilter; }
    public ListView<String> getSesijaListView() { return sesijaListView; }
    public Label getUserLabel()                 { return userLabel; }
    public YearMonth getCurrentYearMonth()      { return currentYearMonth; }
    public void setCurrentYearMonth(YearMonth ym) {
        this.currentYearMonth = ym;
        refreshMonthLabel();
    }

    // Sesija menu items
    public MenuItem getPregledSesijaItem() { return pregledSesijaItem; }
    public MenuItem getNovaSesijaItem()    { return novaSesijaItem; }

    // Laboratorija menu items
    public MenuItem getEditLabItem()    { return editLabItem; }
    public MenuItem getResuriLabItem()  { return resursiLabItem; }
    public MenuItem getAlatLabItem()    { return alatLabItem; }

    // Eksperiment menu items
    public MenuItem getEditExpItem()   { return editExpItem; }
    public MenuItem getStatsExpItem()  { return statsExpItem; }
    public MenuItem getIzvodjenjeExpItem()  { return izvodjenjeExpItem; }

    // MenuButton getteri (za ResursiController koji ih reattach-uje)
    public MenuButton getSesijaMenuBtn() { return sesijaMenuBtn; }
    public MenuButton getLabMenuBtn()    { return labMenuBtn; }
    public MenuButton getExpMenuBtn()    { return expMenuBtn; }

    // ── PRIVATNE POMOĆNE METODE ────────────────────────────────────

    private void styleMenuBtn(MenuButton btn, boolean active) {
        String base =
                "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-color: " + (active ? "#EEEDFE" : "white") + ";" +
                        "-fx-text-fill: " + (active ? "#534AB7" : "#333333") + ";" +
                        "-fx-border-color: " + (active ? "#534AB7" : "#d0d0d0") + ";" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 5 12 5 12;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(base
                .replace("-fx-background-color: " + (active ? "#EEEDFE" : "white") + ";",
                        "-fx-background-color: " + (active ? "#DDDCFD" : "#f0f0f0") + ";")));
        btn.setOnMouseExited(e -> btn.setStyle(base));
    }

    private void styleNavBtn(Button btn) {
        btn.setStyle(
                "-fx-font-size: 18px;" +
                        "-fx-background-color: transparent;" +
                        "-fx-border-color: #d0d0d0;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;" +
                        "-fx-min-width: 30px;" +
                        "-fx-min-height: 30px;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle() +
                "-fx-background-color: #f0f0f0;"));
        btn.setOnMouseExited(e -> btn.setStyle(btn.getStyle()
                .replace("-fx-background-color: #f0f0f0;", "-fx-background-color: transparent;")));
    }

    private Label makeFilterLabel(String tekst) {
        Label l = new Label(tekst);
        l.setStyle(
                "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #888888;" +
                        "-fx-padding: 4 0 0 0;"
        );
        return l;
    }

    private String chipColors(String tip) {
        return switch (tip == null ? "" : tip) {
            case "baseline" -> "-fx-background-color: #E1F5EE; -fx-border-color: #0F6E56;";
            case "stimulus" -> "-fx-background-color: #FAEEDA; -fx-border-color: #854F0B;";
            case "recovery" -> "-fx-background-color: #FAECE7; -fx-border-color: #993C1D;";
            case "wash_out" -> "-fx-background-color: #E6F1FB; -fx-border-color: #185FA5;";
            default         -> "-fx-background-color: #EEEDFE; -fx-border-color: #534AB7;";
        };
    }

    private String chipTextColor(String tip) {
        return switch (tip == null ? "" : tip) {
            case "baseline" -> "-fx-text-fill: #04342C;";
            case "stimulus" -> "-fx-text-fill: #412402;";
            case "recovery" -> "-fx-text-fill: #4A1B0C;";
            case "wash_out" -> "-fx-text-fill: #042C53;";
            default         -> "-fx-text-fill: #26215C;";
        };
    }
}