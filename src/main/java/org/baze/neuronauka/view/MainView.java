package org.baze.neuronauka.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class MainView {

    // ── root — jedini BorderPane koji živi ceo životni vek prozora ─
    private final BorderPane root = new BorderPane();
    private final Label userLabel = new Label();

    // ── Sesija menu ────────────────────────────────────────────────
    private final MenuItem pregledSesijaItem = new MenuItem("Pregled sesija");
    private final MenuItem novaSesijaItem    = new MenuItem("Nova sesija");
    private final MenuButton sesijaMenuBtn   = new MenuButton("Sesija", null,
            pregledSesijaItem, novaSesijaItem);

    // ── Laboratorija menu ──────────────────────────────────────────
    private final MenuItem editLabItem    = new MenuItem("Edit");
    private final MenuItem resursiLabItem = new MenuItem("Resursi");
    private final MenuItem alatLabItem    = new MenuItem("Alat");
    private final MenuButton labMenuBtn   = new MenuButton("Laboratorija", null,
            editLabItem, resursiLabItem, alatLabItem);

    // ── Eksperiment menu ───────────────────────────────────────────
    private final MenuItem editExpItem       = new MenuItem("Edit");
    private final MenuItem statsExpItem      = new MenuItem("Statistika");
    private final MenuItem izvodjenjeExpItem = new MenuItem("Izvodjenje");
    private final MenuButton expMenuBtn = new MenuButton("Eksperiment", null,
            editExpItem, statsExpItem, izvodjenjeExpItem);

    public enum ActiveView { SESIJE, RESURSI, ALAT, LABORATORIJA, EKSPERIMENT, STATISTIKA, IZVODJENJE }
    private ActiveView activeView = ActiveView.SESIJE;

    // ──────────────────────────────────────────────────────────────

    public void show(Stage stage, String loggedInUser) {
        userLabel.setText("Korisnik: " + loggedInUser);

        root.setTop(buildHeader());
        root.setStyle("-fx-background-color: #f5f5f5;");

        stage.setScene(new Scene(root, 1100, 700));
        stage.setTitle("Neuronauka");
        stage.show();
    }

    // ── JAVNE METODE ZA CONTROLLER ─────────────────────────────────

    /** Zamenjuje centralni sadržaj. Header ostaje netaknut. */
    public void switchCenter(Node node) {
        root.setCenter(node);
    }

    /**
     * Postavlja aktivan prikaz i rebuilda header
     * da bi highlight bio tačan.
     */
    public void setActiveView(ActiveView view) {
        this.activeView = view;
        root.setTop(buildHeader());
    }

    /** Vraća Stage iz bilo kog čvora scene (za dijalog prozore). */
    public Stage getStage() {
        return (Stage) root.getScene().getWindow();
    }

    // ── HEADER ─────────────────────────────────────────────────────

    private HBox buildHeader() {
        styleMenuBtn(sesijaMenuBtn, activeView == ActiveView.SESIJE);
        styleMenuBtn(labMenuBtn,
                activeView == ActiveView.LABORATORIJA || activeView == ActiveView.RESURSI);
        styleMenuBtn(expMenuBtn,   activeView == ActiveView.EKSPERIMENT);

        // Alat dugme posebno — deo Lab menija ali ima svoj highlight
        // (highlight se vidi kroz labMenuBtn kad je ALAT aktivan)

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        userLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 13px;");

        HBox header = new HBox(10, sesijaMenuBtn, labMenuBtn, expMenuBtn, spacer, userLabel);
        header.setPadding(new Insets(10, 20, 10, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 0 0 1 0;"
        );
        return header;
    }

    // ── GETTERI ZA CONTROLLER ──────────────────────────────────────

    // ── GETTERI ZA CONTROLLER ──────────────────────────────────────

    public MenuItem getPregledSesijaItem()    { return pregledSesijaItem; }
    public MenuItem getNovaSesijaItem()       { return novaSesijaItem; }
    public MenuItem getEditLabItem()          { return editLabItem; }
    public MenuItem getResuriLabItem()        { return resursiLabItem; }
    public MenuItem getAlatLabItem()          { return alatLabItem; }
    public MenuItem getEditExpItem()          { return editExpItem; }
    public MenuItem getStatsExpItem()         { return statsExpItem; }
    public MenuItem getIzvodjenjeExpItem()    { return izvodjenjeExpItem; } // OVDE
    public Label    getUserLabel()            { return userLabel; }


    // ── STYLING ────────────────────────────────────────────────────

    private void styleMenuBtn(MenuButton btn, boolean active) {
        String bg     = active ? "#EEEDFE" : "white";
        String fg     = active ? "#534AB7" : "#333333";
        String border = active ? "#534AB7" : "#d0d0d0";
        String base =
                "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-color: " + bg + ";" +
                        "-fx-text-fill: " + fg + ";" +
                        "-fx-border-color: " + border + ";" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 5 12 5 12;";
        btn.setStyle(base);
        String hoverBg = active ? "#DDDCFD" : "#f0f0f0";
        btn.setOnMouseEntered(e -> btn.setStyle(
                base.replace("-fx-background-color: " + bg + ";",
                        "-fx-background-color: " + hoverBg + ";")));
        btn.setOnMouseExited(e -> btn.setStyle(base));
    }
}