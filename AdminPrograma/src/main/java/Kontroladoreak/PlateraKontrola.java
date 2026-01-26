package Kontroladoreak;

import Modeloak.Platera;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
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

public class PlateraKontrola {

    @FXML private TableView<Platera> table;
    @FXML private TableColumn<Platera, String> colIzena;
    @FXML private TableColumn<Platera, String> colMota;
    @FXML private TableColumn<Platera, Number> colPrezioa;
    @FXML private TableColumn<Platera, Number> colMotaId;
    @FXML private TableColumn<Platera, Void> colAkzioak;
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
        colMota.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMota()));
        colPrezioa.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getPrezioa()));
        colMotaId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getPlatera_motak_id()));

        colAkzioak.setCellFactory(col -> new TableCell<>() {
            private final Button btnUpdate = new Button("✎");
            private final Button btnDelete = new Button("✖");
            private final HBox box = new HBox(5, btnUpdate, btnDelete);

            {
                btnUpdate.getStyleClass().add("edit-button");
                btnDelete.getStyleClass().add("delete-button");
                box.getStyleClass().add("actions-cell");

                btnUpdate.setOnAction(e -> {
                    Platera p = getTableView().getItems().get(getIndex());
                    mostrarDialogoUpdate(p);
                });

                btnDelete.setOnAction(e -> {
                    Platera p = getTableView().getItems().get(getIndex());
                    deletePlatera(p.getId());
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
                .uri(URI.create(API_BASE_URL + "/Platera"))
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(json -> {
                    try {
                        List<Platera> list = mapper.readValue(json, new TypeReference<List<Platera>>() {});
                        Platform.runLater(() -> table.getItems().setAll(list));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    private void updatePlatera(Platera p) {
        try {
            String json = buildJson(p);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/Platera/" + p.getId()))
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .header("Content-Type", "application/json")
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(resp -> System.out.println("Update OK: " + resp.statusCode()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deletePlatera(int id) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/Platera/" + id))
                .DELETE()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(resp -> Platform.runLater(() ->
                        table.getItems().removeIf(p -> p.getId() == id)
                ));
    }

    private void insertPlatera(Platera p) {
        try {
            String json = buildJson(p);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/Platera"))
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
        Dialog<Platera> dialog = new Dialog<>();
        dialog.setTitle("Platera sartu");
        dialog.setHeaderText("Sartu plateraren informazioa:");
        ButtonType okButton = new ButtonType("Sartu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField tfIzena = new TextField();
        TextField tfMota = new TextField();
        TextField tfPrezioa = new TextField();
        TextField tfMotaId = new TextField();

        grid.add(new Label("Izena:"), 0, 0); grid.add(tfIzena, 1, 0);
        grid.add(new Label("Mota:"), 0, 1); grid.add(tfMota, 1, 1);
        grid.add(new Label("Prezioa:"), 0, 2); grid.add(tfPrezioa, 1, 2);
        grid.add(new Label("Mota ID:"), 0, 3); grid.add(tfMotaId, 1, 3);

        dialog.getDialogPane().setContent(grid);

        final Button btOk = (Button) dialog.getDialogPane().lookupButton(okButton);
        btOk.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                Double.parseDouble(tfPrezioa.getText());
                Integer.parseInt(tfMotaId.getText());
            } catch (NumberFormatException e) {
                event.consume();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Errorea");
                alert.setHeaderText("Datu okerrak");
                alert.setContentText("Prezioa eta Mota ID zenbakiak izan behar dira.");
                alert.showAndWait();
            }
        });

        dialog.setResultConverter(button -> {
            if (button == okButton) {
                Platera p = new Platera();
                p.setIzena(tfIzena.getText());
                p.setMota(tfMota.getText());
                p.setPrezioa(Double.parseDouble(tfPrezioa.getText()));
                p.setPlatera_motak_id(Integer.parseInt(tfMotaId.getText()));
                return p;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(this::insertPlatera);
    }

    private void mostrarDialogoUpdate(Platera p) {
        Dialog<Platera> dialog = new Dialog<>();
        dialog.setTitle("Platera eguneratu");
        dialog.setHeaderText("Aldatu plateraren datuak:");
        ButtonType okButton = new ButtonType("Gorde", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField tfIzena = new TextField(p.getIzena());
        TextField tfMota = new TextField(p.getMota());
        TextField tfPrezioa = new TextField(String.valueOf(p.getPrezioa()));
        TextField tfMotaId = new TextField(String.valueOf(p.getPlatera_motak_id()));

        grid.add(new Label("Izena:"), 0, 0); grid.add(tfIzena, 1, 0);
        grid.add(new Label("Mota:"), 0, 1); grid.add(tfMota, 1, 1);
        grid.add(new Label("Prezioa:"), 0, 2); grid.add(tfPrezioa, 1, 2);
        grid.add(new Label("Mota ID:"), 0, 3); grid.add(tfMotaId, 1, 3);

        dialog.getDialogPane().setContent(grid);

        final Button btOk = (Button) dialog.getDialogPane().lookupButton(okButton);
        btOk.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                Double.parseDouble(tfPrezioa.getText());
                Integer.parseInt(tfMotaId.getText());
            } catch (NumberFormatException e) {
                event.consume();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Errorea");
                alert.setHeaderText("Datu okerrak");
                alert.setContentText("Prezioa eta Mota ID zenbakiak izan behar dira.");
                alert.showAndWait();
            }
        });

        dialog.setResultConverter(button -> {
            if (button == okButton) {
                p.setIzena(tfIzena.getText());
                p.setMota(tfMota.getText());
                p.setPrezioa(Double.parseDouble(tfPrezioa.getText()));
                p.setPlatera_motak_id(Integer.parseInt(tfMotaId.getText()));
                return p;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(this::updatePlatera);
    }

    private String buildJson(Platera p) {
        return String.format(
                "{\"Izena\":\"%s\",\"Mota\":\"%s\",\"Prezioa\":%s,\"Platera_motak_id\":%d}",
                escape(p.getIzena()),
                escape(p.getMota()),
                String.valueOf(p.getPrezioa()).replace(",", "."),
                p.getPlatera_motak_id()
        );
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
