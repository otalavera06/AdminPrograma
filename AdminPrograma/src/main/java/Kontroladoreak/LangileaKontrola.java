package Kontroladoreak;

import Modeloak.Langilea;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class LangileaKontrola {

    @FXML private TableView<Langilea> langileakTable;
    @FXML private TableColumn<Langilea, String> colIzena;
    @FXML private TableColumn<Langilea, String> colAbizena;
    @FXML private TableColumn<Langilea, String> colEmaila;
    @FXML private TableColumn<Langilea, String> colTelefonoa;
    @FXML private TableColumn<Langilea, Boolean> colBaimena;
    @FXML private TableColumn<Langilea, Void> colAkzioak;
    @FXML private Button btnInsertLangilea;

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    public void initialize() {
        btnInsertLangilea.setOnAction(e -> insertLangilea());
        kargatuLangileak();
    }

    /** GET → cargar todos los langileak */
    private void kargatuLangileak() {
        colIzena.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getIzena()));
        colAbizena.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAbizena()));
        colEmaila.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        colTelefonoa.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTelefonoa()));
        colBaimena.setCellValueFactory(data -> new SimpleBooleanProperty(data.getValue().isBaimena()));

        // Mostrar baimena como ✔ o ✖
        colBaimena.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : (item ? "✔" : "✖"));
            }
        });

        // Botones Update/Delete en cada fila
        colAkzioak.setCellFactory(col -> new TableCell<>() {
            private final Button btnUpdate = new Button("Update");
            private final Button btnDelete = new Button("Delete");

            {
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
                setGraphic(empty ? null : new HBox(5, btnUpdate, btnDelete));
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
                        List<Langilea> langileak = mapper.readValue(json, new TypeReference<List<Langilea>>() {});
                        Platform.runLater(() -> langileakTable.getItems().setAll(langileak));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });
    }

    /** PUT → actualizar un langilea */
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

    /** DELETE → eliminar un langilea */
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

    /** POST → insertar un nuevo langilea */
    private void insertLangilea() {
        Langilea nuevo = new Langilea();
        nuevo.setIzena("Test");
        nuevo.setAbizena("Insert");
        nuevo.setEmail("test@insert.com");
        nuevo.setTelefonoa("123456789");
        nuevo.setBaimena(true);
        nuevo.setLangileMotaId(1);
        nuevo.setErabiltzailea("usuario");
        nuevo.setPasahitza("clave");

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
