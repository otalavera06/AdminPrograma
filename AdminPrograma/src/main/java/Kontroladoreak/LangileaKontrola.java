package Kontroladoreak;

import Modeloak.Langilea;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class LangileaKontrola {

    @FXML
    private TableView<Langilea> langileakTable;

    @FXML
    private TableColumn<Langilea, String> colIzena;
    @FXML
    private TableColumn<Langilea, String> colAbizena;
    @FXML
    private TableColumn<Langilea, String> colEmaila;
    @FXML
    private TableColumn<Langilea, String> colTelefonoa;
    @FXML
    private TableColumn<Langilea, String> colErabiltzailea;
    @FXML
    private TableColumn<Langilea, String> colPasahitza;
    @FXML
    private TableColumn<Langilea, Boolean> colBaimena;
    @FXML
    private TableColumn<Langilea, Void> colAkzioak;

    @FXML
    private Button btnInsertLangilea;

    // C# API1 aplikazioaren oinarrizko URLa
    private static final String API_BASE_URL = "http://localhost:5005/api";

    private final HttpClient client = HttpClient.newHttpClient();

    // JSON parserra:
    //  - FAIL_ON_UNKNOWN_PROPERTIES = false  → APIk bidaltzen duen gehiegizko informazioa ez du molestatuko
    //  - ACCEPT_CASE_INSENSITIVE_PROPERTIES = true → propietateen izenekin malgua
    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

    @FXML
    public void initialize() {
        btnInsertLangilea.setText("Berria");
        btnInsertLangilea.getStyleClass().add("berria-button");
        HBox.setMargin(btnInsertLangilea, new Insets(10, 10, 0, 10));

        btnInsertLangilea.setOnAction(e -> mostrarDialogoInsert());
        langileakTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        kargatuLangileak();
    }

    /**
     * GET → langile guztiak API-tik kargatu.
     */
    private void kargatuLangileak() {
        // Zutabeen datu-iturria
        colIzena.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getIzena()));
        colAbizena.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAbizena()));
        colEmaila.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        colTelefonoa.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTelefonoa()));
        colErabiltzailea.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getErabiltzailea()));
        colPasahitza.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPasahitza()));
        colBaimena.setCellValueFactory(data -> new SimpleBooleanProperty(data.getValue().isBaimena()));

        // 🔀 BAIMENA TOGGLE-A (switch modua, zure CSS-ko .switch-toggle erabilita)
        colBaimena.setCellFactory(col -> new TableCell<Langilea, Boolean>() {
            private final ToggleButton toggle = new ToggleButton();
            private final Region thumb = new Region();

            {
                // 🔴 GARRANTZITSUA: hemen .switch-toggle klasea erabiltzen dugu, Estiloak.css-ekoarekin berdin-berdin
                toggle.getStyleClass().add("switch-toggle"); // antes era "toggle-switch"
                thumb.getStyleClass().add("thumb");
                toggle.setGraphic(thumb);

                toggle.setOnAction(e -> {
                    Langilea l = getTableView().getItems().get(getIndex());
                    boolean egoeraBerria = toggle.isSelected();
                    l.setBaimena(egoeraBerria);   // boolean aldatzen dugu
                    updateLangilea(l);            // eta API-ra bidali (PUT)
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    toggle.setSelected(item);
                    setGraphic(toggle);
                }
            }
        });

        // ✏️ / ❌ botoiak (editatu / ezabatu)
        colAkzioak.setCellFactory(col -> new TableCell<>() {
            private final Button btnUpdate = new Button("✎");
            private final Button btnDelete = new Button("✖");
            private final HBox box = new HBox(5, btnUpdate, btnDelete);

            {
                btnUpdate.getStyleClass().add("edit-button");
                btnDelete.getStyleClass().add("delete-button");
                box.getStyleClass().add("actions-cell");

                btnUpdate.setOnAction(e -> {
                    Langilea langilea = getTableView().getItems().get(getIndex());
                    mostrarDialogoUpdate(langilea);
                });

                btnDelete.setOnAction(e -> {
                    Langilea langilea = getTableView().getItems().get(getIndex());
                    deleteLangilea(langilea.getId());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(box);
                }
            }
        });

        // API → GET api/Langilea
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/Langilea"))
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(json -> {
                    try {
                        List<Langilea> langileak = mapper.readValue(
                                json,
                                new TypeReference<List<Langilea>>() {}
                        );
                        Platform.runLater(() -> langileakTable.getItems().setAll(langileak));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    /**
     * PUT → langilea eguneratu (baimena edo beste eremuak).
     */
    private void updateLangilea(Langilea l) {
        try {
            String json = buildLangileaJson(l);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/Langilea/" + l.getId()))
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .header("Content-Type", "application/json")
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(resp ->
                            System.out.println("Update OK (" + l.getId() + "): " + resp.statusCode())
                    );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * DELETE → langilea ezabatu.
     */
    private void deleteLangilea(int id) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/Langilea/" + id))
                .DELETE()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(resp -> Platform.runLater(() ->
                        langileakTable.getItems().removeIf(l -> l.getId() == id)
                ));
    }

    /**
     * INSERT → langile berria sortzeko dialogoa.
     */
    private void mostrarDialogoInsert() {
        Dialog<Langilea> dialog = new Dialog<>();
        dialog.setTitle("Langilea sartu");
        dialog.setHeaderText("Sartu langilearen informazioa:");

        ButtonType insertButtonType = new ButtonType("Sartu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(insertButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField tfIzena = new TextField();
        TextField tfAbizena = new TextField();
        TextField tfEmaila = new TextField();
        TextField tfTelefonoa = new TextField();
        TextField tfErabiltzailea = new TextField();
        PasswordField tfPasahitza = new PasswordField();
        ToggleButton toggleBaimena = new ToggleButton("Baimena");

        grid.add(new Label("Izena:"),        0, 0);
        grid.add(tfIzena,                    1, 0);
        grid.add(new Label("Abizena:"),      0, 1);
        grid.add(tfAbizena,                  1, 1);
        grid.add(new Label("Emaila:"),       0, 2);
        grid.add(tfEmaila,                   1, 2);
        grid.add(new Label("Telefonoa:"),    0, 3);
        grid.add(tfTelefonoa,                1, 3);
        grid.add(new Label("Erabiltzailea:"),0, 4);
        grid.add(tfErabiltzailea,            1, 4);
        grid.add(new Label("Pasahitza:"),    0, 5);
        grid.add(tfPasahitza,                1, 5);
        grid.add(new Label("Baimena:"),      0, 6);
        grid.add(toggleBaimena,              1, 6);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == insertButtonType) {
                Langilea l = new Langilea();
                l.setIzena(tfIzena.getText());
                l.setAbizena(tfAbizena.getText());
                l.setEmail(tfEmaila.getText());
                l.setTelefonoa(tfTelefonoa.getText());
                l.setErabiltzailea(tfErabiltzailea.getText());
                l.setPasahitza(tfPasahitza.getText());
                l.setBaimena(toggleBaimena.isSelected());
                return l;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(this::insertLangilea);
    }

    /**
     * UPDATE → lehendik dagoen langilea editatzeko dialogoa.
     */
    private void mostrarDialogoUpdate(Langilea langilea) {
        Dialog<Langilea> dialog = new Dialog<>();
        dialog.setTitle("Langilea eguneratu");
        dialog.setHeaderText("Aldatu langilearen datuak:");

        ButtonType updateButtonType = new ButtonType("Gorde", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField tfIzena = new TextField(langilea.getIzena());
        TextField tfAbizena = new TextField(langilea.getAbizena());
        TextField tfEmaila = new TextField(langilea.getEmail());
        TextField tfTelefonoa = new TextField(langilea.getTelefonoa());
        TextField tfErabiltzailea = new TextField(langilea.getErabiltzailea());
        PasswordField tfPasahitza = new PasswordField();
        tfPasahitza.setText(langilea.getPasahitza());
        ToggleButton toggleBaimena = new ToggleButton("Baimena");
        toggleBaimena.setSelected(langilea.isBaimena());

        grid.add(new Label("Izena:"),        0, 0);
        grid.add(tfIzena,                    1, 0);
        grid.add(new Label("Abizena:"),      0, 1);
        grid.add(tfAbizena,                  1, 1);
        grid.add(new Label("Emaila:"),       0, 2);
        grid.add(tfEmaila,                   1, 2);
        grid.add(new Label("Telefonoa:"),    0, 3);
        grid.add(tfTelefonoa,                1, 3);
        grid.add(new Label("Erabiltzailea:"),0, 4);
        grid.add(tfErabiltzailea,            1, 4);
        grid.add(new Label("Pasahitza:"),    0, 5);
        grid.add(tfPasahitza,                1, 5);
        grid.add(new Label("Baimena:"),      0, 6);
        grid.add(toggleBaimena,              1, 6);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                langilea.setIzena(tfIzena.getText());
                langilea.setAbizena(tfAbizena.getText());
                langilea.setEmail(tfEmaila.getText());
                langilea.setTelefonoa(tfTelefonoa.getText());
                langilea.setErabiltzailea(tfErabiltzailea.getText());
                langilea.setPasahitza(tfPasahitza.getText());
                langilea.setBaimena(toggleBaimena.isSelected());
                return langilea;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(this::updateLangilea);
    }

    /**
     * POST → langile berria API-n sortu.
     */
    private void insertLangilea(Langilea l) {
        try {
            String json = buildLangileaJson(l);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/Langilea"))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .header("Content-Type", "application/json")
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(resp -> {
                        System.out.println("Insert OK: " + resp.statusCode());
                        kargatuLangileak();   // taula berriz kargatu
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Java objektua → APIk ulertzen duen JSON-a.
     * Baimena boolean (true/false) → 1/0 bihurtzen dugu.
     */
    private String buildLangileaJson(Langilea l) {
        int baimenaInt = l.isBaimena() ? 1 : 0;

        return String.format(
                "{" +
                        "\"Izena\":\"%s\"," +
                        "\"Abizena\":\"%s\"," +
                        "\"Email\":\"%s\"," +
                        "\"Telefonoa\":\"%s\"," +
                        "\"Baimena\":%d," +
                        "\"Erabiltzailea\":\"%s\"," +
                        "\"Pasahitza\":\"%s\"" +
                        "}",
                escape(l.getIzena()),
                escape(l.getAbizena()),
                escape(l.getEmail()),
                escape(l.getTelefonoa()),
                baimenaInt,
                escape(l.getErabiltzailea()),
                escape(l.getPasahitza())
        );
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
