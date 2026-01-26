package Kontroladoreak;

import Modeloak.Erabiltzailea;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class ErabiltzaileaKontrola {

    @FXML private TableView<Erabiltzailea> table;
    @FXML private TableColumn<Erabiltzailea, String> colIzena;
    @FXML private TableColumn<Erabiltzailea, String> colAbizena;
    @FXML private TableColumn<Erabiltzailea, String> colEmaila;
    @FXML private TableColumn<Erabiltzailea, String> colTelefonoa;
    @FXML private TableColumn<Erabiltzailea, String> colPasahitza;
    @FXML private TableColumn<Erabiltzailea, Void> colAkzioak;
    @FXML private Button btnInsert;

    private static final String API_BASE_URL = "http://localhost:5005/api";
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

    @FXML
    public void initialize() {
        btnInsert.setText("Berria");
        btnInsert.getStyleClass().add("berria-button");
        HBox.setMargin(btnInsert, new Insets(10, 10, 0, 10));
        btnInsert.setOnAction(e -> mostrarDialogoInsert());

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        kargatuDatuak();
    }

    private void kargatuDatuak() {
        colIzena.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getIzena()));
        colAbizena.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAbizena()));
        colEmaila.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        colTelefonoa.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTelefonoa()));
        colPasahitza.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPasahitza()));

        colAkzioak.setCellFactory(col -> new TableCell<>() {
            private final Button btnUpdate = new Button("✎");
            private final Button btnDelete = new Button("✖");
            private final HBox box = new HBox(5, btnUpdate, btnDelete);

            {
                btnUpdate.getStyleClass().add("edit-button");
                btnDelete.getStyleClass().add("delete-button");
                box.getStyleClass().add("actions-cell");

                btnUpdate.setOnAction(e -> {
                    Erabiltzailea u = getTableView().getItems().get(getIndex());
                    mostrarDialogoUpdate(u);
                });

                btnDelete.setOnAction(e -> {
                    Erabiltzailea u = getTableView().getItems().get(getIndex());
                    deleteErabiltzailea(u.getId());
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

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/Erabiltzailea"))
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(json -> {
                    try {
                        List<Erabiltzailea> list = mapper.readValue(json, new TypeReference<List<Erabiltzailea>>() {});
                        Platform.runLater(() -> table.getItems().setAll(list));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    private void updateErabiltzailea(Erabiltzailea u) {
        try {
            String json = buildJson(u);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/Erabiltzailea/" + u.getId()))
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .header("Content-Type", "application/json")
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(resp -> System.out.println("Update OK: " + resp.statusCode()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteErabiltzailea(int id) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/Erabiltzailea/" + id))
                .DELETE()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(resp -> Platform.runLater(() ->
                        table.getItems().removeIf(u -> u.getId() == id)
                ));
    }

    private void insertErabiltzailea(Erabiltzailea u) {
        try {
            String json = buildJson(u);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/Erabiltzailea"))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .header("Content-Type", "application/json")
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(resp -> {
                        System.out.println("Insert OK: " + resp.statusCode());
                        kargatuDatuak();
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarDialogoInsert() {
        Dialog<Erabiltzailea> dialog = new Dialog<>();
        dialog.setTitle("Erabiltzailea sartu");
        dialog.setHeaderText("Sartu erabiltzailearen informazioa:");
        ButtonType okButton = new ButtonType("Sartu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField tfIzena = new TextField();
        TextField tfAbizena = new TextField();
        TextField tfEmaila = new TextField();
        TextField tfTelefonoa = new TextField();
        PasswordField tfPasahitza = new PasswordField();

        grid.add(new Label("Izena:"), 0, 0); grid.add(tfIzena, 1, 0);
        grid.add(new Label("Abizena:"), 0, 1); grid.add(tfAbizena, 1, 1);
        grid.add(new Label("Emaila:"), 0, 2); grid.add(tfEmaila, 1, 2);
        grid.add(new Label("Telefonoa:"), 0, 3); grid.add(tfTelefonoa, 1, 3);
        grid.add(new Label("Pasahitza:"), 0, 4); grid.add(tfPasahitza, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == okButton) {
                Erabiltzailea u = new Erabiltzailea();
                u.setIzena(tfIzena.getText());
                u.setAbizena(tfAbizena.getText());
                u.setEmail(tfEmaila.getText());
                u.setTelefonoa(tfTelefonoa.getText());
                u.setPasahitza(tfPasahitza.getText());
                return u;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(this::insertErabiltzailea);
    }

    private void mostrarDialogoUpdate(Erabiltzailea u) {
        Dialog<Erabiltzailea> dialog = new Dialog<>();
        dialog.setTitle("Erabiltzailea eguneratu");
        dialog.setHeaderText("Aldatu erabiltzailearen datuak:");
        ButtonType okButton = new ButtonType("Gorde", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField tfIzena = new TextField(u.getIzena());
        TextField tfAbizena = new TextField(u.getAbizena());
        TextField tfEmaila = new TextField(u.getEmail());
        TextField tfTelefonoa = new TextField(u.getTelefonoa());
        PasswordField tfPasahitza = new PasswordField();
        tfPasahitza.setText(u.getPasahitza());

        grid.add(new Label("Izena:"), 0, 0); grid.add(tfIzena, 1, 0);
        grid.add(new Label("Abizena:"), 0, 1); grid.add(tfAbizena, 1, 1);
        grid.add(new Label("Emaila:"), 0, 2); grid.add(tfEmaila, 1, 2);
        grid.add(new Label("Telefonoa:"), 0, 3); grid.add(tfTelefonoa, 1, 3);
        grid.add(new Label("Pasahitza:"), 0, 4); grid.add(tfPasahitza, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == okButton) {
                u.setIzena(tfIzena.getText());
                u.setAbizena(tfAbizena.getText());
                u.setEmail(tfEmaila.getText());
                u.setTelefonoa(tfTelefonoa.getText());
                u.setPasahitza(tfPasahitza.getText());
                return u;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(this::updateErabiltzailea);
    }

    private String buildJson(Erabiltzailea u) {
        return String.format(
                "{" +
                        "\"Izena\":\"%s\"," +
                        "\"Abizena\":\"%s\"," +
                        "\"Email\":\"%s\"," +
                        "\"Telefonoa\":\"%s\"," +
                        "\"Pasahitza\":\"%s\"" +
                        "}",
                escape(u.getIzena()),
                escape(u.getAbizena()),
                escape(u.getEmail()),
                escape(u.getTelefonoa()),
                escape(u.getPasahitza())
        );
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
