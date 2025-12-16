package Kontroladoreak;

import Modeloak.Langilea;
import com.fasterxml.jackson.core.type.TypeReference;
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
    private TableColumn<Langilea, Boolean> colBaimena;
    @FXML
    private TableColumn<Langilea, Void> colAkzioak;
    @FXML
    private Button btnInsertLangilea;

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    public void initialize() {
        btnInsertLangilea.setText("Berria");
        btnInsertLangilea.getStyleClass().add("berria-button");

        // margen externo de 10px alrededor del botón
        HBox.setMargin(btnInsertLangilea, new Insets(10, 10, 0, 10));

        btnInsertLangilea.setOnAction(e -> mostrarDialogoInsert());
        langileakTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        kargatuLangileak();
    }

    /**
     * GET → cargar todos los langileak
     */
    private void kargatuLangileak() {
        colIzena.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getIzena()));
        colAbizena.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAbizena()));
        colEmaila.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        colTelefonoa.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTelefonoa()));
        colBaimena.setCellValueFactory(data -> new SimpleBooleanProperty(data.getValue().isBaimena()));

        colBaimena.setCellFactory(col -> new TableCell<Langilea, Boolean>() {
            private final ToggleButton toggle = new ToggleButton();
            private final Region thumb = new Region();

            {
                toggle.getStyleClass().add("switch-toggle");
                thumb.getStyleClass().add("thumb");
                toggle.setGraphic(thumb);

                toggle.setOnAction(e -> {
                    Langilea l = getTableView().getItems().get(getIndex());
                    boolean nuevoEstado = toggle.isSelected();
                    l.setBaimena(nuevoEstado);
                    updateLangilea(l);
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

        // 👇 Aquí está la implementación estilizada de colAkzioak
        colAkzioak.setCellFactory(col -> new TableCell<>() {
            private final Button btnUpdate = new Button("✎");   // icono lápiz
            private final Button btnDelete = new Button("✖");   // icono cruz
            private final HBox box = new HBox(5, btnUpdate, btnDelete);

            {
                // aplicar estilos CSS definidos en Estiloak.css
                btnUpdate.getStyleClass().add("edit-button");
                btnDelete.getStyleClass().add("delete-button");

                btnUpdate.setOnAction(e -> {
                    Langilea l = getTableView().getItems().get(getIndex());
                    updateLangilea(l);
                });
                btnDelete.setOnAction(e -> {
                    Langilea l = getTableView().getItems().get(getIndex());
                    deleteLangilea(l.getId());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.miapp.com/langileak"))
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(json -> {
                    try {
                        List<Langilea> langileak = mapper.readValue(json, new TypeReference<List<Langilea>>() {
                        });
                        Platform.runLater(() -> langileakTable.getItems().setAll(langileak));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });
    }

    /**
     * PUT → actualizar un langilea
     */
    private void updateLangilea(Langilea l) {
        try {
            String json = mapper.writeValueAsString(l);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.miapp.com/langileak/" + l.getId()))
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .header("Content-Type", "application/json")
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(resp -> System.out.println("Update OK: " + resp.body()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * DELETE → eliminar un langilea
     */
    private void deleteLangilea(int id) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.miapp.com/langileak/" + id))
                .DELETE()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(resp -> Platform.runLater(() ->
                        langileakTable.getItems().removeIf(l -> l.getId() == id)
                ));
    }

    /**
     * Mostrar ventana de inserción
     */
    private void mostrarDialogoInsert() {
        Dialog<Langilea> dialog = new Dialog<>();
        dialog.setTitle("Insertar Langilea");
        dialog.setHeaderText("Introduce los datos del nuevo langilea");

        ButtonType insertButtonType = new ButtonType("Insertar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(insertButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField tfIzena = new TextField();
        TextField tfAbizena = new TextField();
        TextField tfEmail = new TextField();
        TextField tfTelefonoa = new TextField();
        TextField tfErabiltzailea = new TextField();
        PasswordField tfPasahitza = new PasswordField();
        ToggleButton toggleBaimena = new ToggleButton();
        toggleBaimena.setSelected(false);
        toggleBaimena.getStyleClass().add("switch-toggle");
        Region thumb = new Region();
        thumb.getStyleClass().add("thumb");
        toggleBaimena.setGraphic(thumb);



        grid.add(new Label("Izena:"), 0, 0);
        grid.add(tfIzena, 1, 0);
        grid.add(new Label("Abizena:"), 0, 1);
        grid.add(tfAbizena, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(tfEmail, 1, 2);
        grid.add(new Label("Telefonoa:"), 0, 3);
        grid.add(tfTelefonoa, 1, 3);
        grid.add(new Label("Erabiltzailea:"), 0, 4);
        grid.add(tfErabiltzailea, 1, 4);
        grid.add(new Label("Pasahitza:"), 0, 5);
        grid.add(tfPasahitza, 1, 5);
        grid.add(new Label("Baimena:"), 0, 6);
        grid.add(toggleBaimena, 1, 6);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == insertButtonType) {
                Langilea nuevo = new Langilea();
                nuevo.setIzena(tfIzena.getText());
                nuevo.setAbizena(tfAbizena.getText());
                nuevo.setEmail(tfEmail.getText());
                nuevo.setTelefonoa(tfTelefonoa.getText());
                nuevo.setErabiltzailea(tfErabiltzailea.getText());
                nuevo.setPasahitza(tfPasahitza.getText());
                nuevo.setBaimena(toggleBaimena.isSelected());
                nuevo.setLangileMotaId(1); // puedes cambiarlo según tu lógica
                return nuevo;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(this::insertLangilea);
    }

    /**
     * POST → insertar un nuevo langilea
     */
    private void insertLangilea(Langilea nuevo) {
        try {
            String json = mapper.writeValueAsString(nuevo);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.miapp.com/langileak"))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .header("Content-Type", "application/json")
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(resp -> {
                        System.out.println("Insert OK: " + resp.body());
                        kargatuLangileak(); // refrescar tabla
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}