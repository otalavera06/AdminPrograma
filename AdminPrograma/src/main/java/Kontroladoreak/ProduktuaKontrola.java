package Kontroladoreak;

import Modeloak.Produktua;
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

public class ProduktuaKontrola {

    @FXML private TableView<Produktua> table;
    @FXML private TableColumn<Produktua, String> colIzena;
    @FXML private TableColumn<Produktua, Number> colPrezioa;
    @FXML private TableColumn<Produktua, Number> colStock;
    @FXML private TableColumn<Produktua, String> colIrudia;
    @FXML private TableColumn<Produktua, Number> colMota;
    @FXML private TableColumn<Produktua, Void> colAkzioak;
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
        colPrezioa.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getPrezioa()));
        colStock.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getStock()));
        colIrudia.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getIrudia_path()));
        colMota.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getProduktuen_motak_id()));

        colAkzioak.setCellFactory(col -> new TableCell<>() {
            private final Button btnUpdate = new Button("✎");
            private final Button btnDelete = new Button("✖");
            private final HBox box = new HBox(5, btnUpdate, btnDelete);

            {
                btnUpdate.getStyleClass().add("edit-button");
                btnDelete.getStyleClass().add("delete-button");
                box.getStyleClass().add("actions-cell");

                btnUpdate.setOnAction(e -> {
                    Produktua p = getTableView().getItems().get(getIndex());
                    mostrarDialogoUpdate(p);
                });

                btnDelete.setOnAction(e -> {
                    Produktua p = getTableView().getItems().get(getIndex());
                    deleteProduktua(p.getId());
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
                .uri(URI.create(API_BASE_URL + "/Produktua"))
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(json -> {
                    try {
                        List<Produktua> list = mapper.readValue(json, new TypeReference<List<Produktua>>() {});
                        Platform.runLater(() -> table.getItems().setAll(list));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    private void updateProduktua(Produktua p) {
        try {
            String json = buildJson(p);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/Produktua/" + p.getId()))
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .header("Content-Type", "application/json")
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(resp -> System.out.println("Update OK: " + resp.statusCode()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteProduktua(int id) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/Produktua/" + id))
                .DELETE()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(resp -> Platform.runLater(() ->
                        table.getItems().removeIf(p -> p.getId() == id)
                ));
    }

    private void insertProduktua(Produktua p) {
        try {
            String json = buildJson(p);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/Produktua"))
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
        Dialog<Produktua> dialog = new Dialog<>();
        dialog.setTitle("Produktua sartu");
        dialog.setHeaderText("Sartu produktuaren informazioa:");
        ButtonType okButton = new ButtonType("Sartu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField tfIzena = new TextField();
        TextField tfPrezioa = new TextField();
        TextField tfStock = new TextField();
        TextField tfIrudia = new TextField();
        TextField tfMota = new TextField();

        grid.add(new Label("Izena:"), 0, 0); grid.add(tfIzena, 1, 0);
        grid.add(new Label("Prezioa:"), 0, 1); grid.add(tfPrezioa, 1, 1);
        grid.add(new Label("Stock:"), 0, 2); grid.add(tfStock, 1, 2);
        grid.add(new Label("Irudia Path:"), 0, 3); grid.add(tfIrudia, 1, 3);
        grid.add(new Label("Mota ID:"), 0, 4); grid.add(tfMota, 1, 4);

        dialog.getDialogPane().setContent(grid);

        final Button btOk = (Button) dialog.getDialogPane().lookupButton(okButton);
        btOk.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                Double.parseDouble(tfPrezioa.getText());
                Integer.parseInt(tfStock.getText());
                Integer.parseInt(tfMota.getText());
            } catch (NumberFormatException e) {
                event.consume();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Errorea");
                alert.setHeaderText("Datu okerrak");
                alert.setContentText("Prezioa, Stock eta Mota ID zenbakiak izan behar dira.");
                alert.showAndWait();
            }
        });

        dialog.setResultConverter(button -> {
            if (button == okButton) {
                Produktua p = new Produktua();
                p.setIzena(tfIzena.getText());
                p.setIrudia_path(tfIrudia.getText());
                p.setPrezioa(Double.parseDouble(tfPrezioa.getText()));
                p.setStock(Integer.parseInt(tfStock.getText()));
                p.setProduktuen_motak_id(Integer.parseInt(tfMota.getText()));
                return p;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(this::insertProduktua);
    }

    private void mostrarDialogoUpdate(Produktua p) {
        Dialog<Produktua> dialog = new Dialog<>();
        dialog.setTitle("Produktua eguneratu");
        dialog.setHeaderText("Aldatu produktuaren datuak:");
        ButtonType okButton = new ButtonType("Gorde", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField tfIzena = new TextField(p.getIzena());
        TextField tfPrezioa = new TextField(String.valueOf(p.getPrezioa()));
        TextField tfStock = new TextField(String.valueOf(p.getStock()));
        TextField tfIrudia = new TextField(p.getIrudia_path());
        TextField tfMota = new TextField(String.valueOf(p.getProduktuen_motak_id()));

        grid.add(new Label("Izena:"), 0, 0); grid.add(tfIzena, 1, 0);
        grid.add(new Label("Prezioa:"), 0, 1); grid.add(tfPrezioa, 1, 1);
        grid.add(new Label("Stock:"), 0, 2); grid.add(tfStock, 1, 2);
        grid.add(new Label("Irudia Path:"), 0, 3); grid.add(tfIrudia, 1, 3);
        grid.add(new Label("Mota ID:"), 0, 4); grid.add(tfMota, 1, 4);

        dialog.getDialogPane().setContent(grid);

        final Button btOk = (Button) dialog.getDialogPane().lookupButton(okButton);
        btOk.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                Double.parseDouble(tfPrezioa.getText());
                Integer.parseInt(tfStock.getText());
                Integer.parseInt(tfMota.getText());
            } catch (NumberFormatException e) {
                event.consume();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Errorea");
                alert.setHeaderText("Datu okerrak");
                alert.setContentText("Prezioa, Stock eta Mota ID zenbakiak izan behar dira.");
                alert.showAndWait();
            }
        });

        dialog.setResultConverter(button -> {
            if (button == okButton) {
                p.setIzena(tfIzena.getText());
                p.setIrudia_path(tfIrudia.getText());
                p.setPrezioa(Double.parseDouble(tfPrezioa.getText()));
                p.setStock(Integer.parseInt(tfStock.getText()));
                p.setProduktuen_motak_id(Integer.parseInt(tfMota.getText()));
                return p;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(this::updateProduktua);
    }

    private String buildJson(Produktua p) {
        return String.format(
                "{\"Izena\":\"%s\",\"Prezioa\":%s,\"Stock\":%d,\"Irudia_path\":\"%s\",\"Produktuen_motak_id\":%d}",
                escape(p.getIzena()),
                String.valueOf(p.getPrezioa()).replace(",", "."),
                p.getStock(),
                escape(p.getIrudia_path()),
                p.getProduktuen_motak_id()
        );
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
