package Kontroladoreak;

import DatuBaseak.MySql;
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
import java.sql.*;
import java.util.ArrayList;
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
    private TableColumn<Langilea, String> colErabiltzailea;
    @FXML
    private TableColumn<Langilea, String> colPasahitza;
    @FXML
    private TableColumn<Langilea, String> colLangileMotaId;
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
    private final MySql db = new MySql();

    private void kargatuLangileak() {
        colIzena.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getIzena()));
        colAbizena.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAbizena()));
        colEmaila.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        colTelefonoa.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTelefonoa()));
        colErabiltzailea.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getErabiltzailea()));
        colPasahitza.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPasahitza()));
        colLangileMotaId.setCellValueFactory(data -> new SimpleStringProperty(
                String.valueOf(data.getValue().getLangileMotaId())
        ));
        colBaimena.setCellValueFactory(data -> new SimpleBooleanProperty(data.getValue().isBaimena()));

        // Toggle para baimena
        colBaimena.setCellFactory(col -> new TableCell<Langilea, Boolean>() {
            private final ToggleButton toggle = new ToggleButton();
            private final Region thumb = new Region();

            {
                toggle.getStyleClass().add("switch-toggle");
                thumb.getStyleClass().add("thumb");
                toggle.setGraphic(thumb);

                // Acción al pulsar
                toggle.setOnAction(e -> {
                    Langilea l = getTableView().getItems().get(getIndex());
                    boolean nuevoEstado = toggle.isSelected();
                    l.setBaimena(nuevoEstado);

                    // Actualiza en BD
                    updateLangileaBaimena(l);
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

        // SELECT desde MySQL
        List<Langilea> lista = new ArrayList<>();
        try (Connection conn = db.konektatu();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, izena, abizena, erabiltzailea, pasahitza, baimena, email, telefonoa, langile_mota_id FROM langileak")) {

            while (rs.next()) {
                Langilea l = new Langilea();
                l.setId(rs.getInt("id"));
                l.setIzena(rs.getString("izena"));
                l.setAbizena(rs.getString("abizena"));
                l.setErabiltzailea(rs.getString("erabiltzailea"));
                l.setPasahitza(rs.getString("pasahitza"));
                l.setBaimena(rs.getBoolean("baimena"));
                l.setEmail(rs.getString("email"));
                l.setTelefonoa(rs.getString("telefonoa"));
                l.setLangileMotaId(rs.getInt("langile_mota_id"));
                lista.add(l);
            }
            Platform.runLater(() -> langileakTable.getItems().setAll(lista));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * PUT → actualizar un langilea
     */
    private void updateLangileaBaimena(Langilea l) {
        String sql = "UPDATE langileak SET baimena=? WHERE id=?";

        try (Connection conn = db.konektatu();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, l.isBaimena());
            ps.setInt(2, l.getId());

            ps.executeUpdate();
            System.out.println("Baimena aktualizatua: " + l.getIzena() + " → " + (l.isBaimena() ? "ON" : "OFF"));

        } catch (SQLException e) {
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
        dialog.setTitle("Langilea sartu");
        dialog.setHeaderText("Sartu langilearen informazioa:");

        ButtonType insertButtonType = new ButtonType("Sartu", ButtonBar.ButtonData.OK_DONE);
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

    public TableColumn<Langilea, Void> getColAkzioak() {
        return colAkzioak;
    }

    public void setColAkzioak(TableColumn<Langilea, Void> colAkzioak) {
        this.colAkzioak = colAkzioak;
    }
}