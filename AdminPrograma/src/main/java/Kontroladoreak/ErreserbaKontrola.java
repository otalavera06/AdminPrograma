package Kontroladoreak;

import Modeloak.Erreserba;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
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

public class ErreserbaKontrola {

    @FXML private TableView<Erreserba> table;
    @FXML private TableColumn<Erreserba, String> colData;
    @FXML private TableColumn<Erreserba, Number> colMota;
    @FXML private TableColumn<Erreserba, Number> colErabiltzaileaId;
    @FXML private TableColumn<Erreserba, Number> colMahaiaId;
    @FXML private TableColumn<Erreserba, Void> colAkzioak;
    @FXML private Button btnInsert;

    @FXML private Button btnAtzera;
    @FXML private Button btnAurrera;
    @FXML private Label lblOrrialdea;

    private java.util.List<Erreserba> masterData = new java.util.ArrayList<>();
    private int currentPage = 0;
    private static final int ROWS_PER_PAGE = 13;

    private static final String API_BASE_URL = "http://192.168.1.104:5005/api";
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

        // Pagination buttons
        btnAtzera.setOnAction(e -> {
            if (currentPage > 0) {
                currentPage--;
                updatePagination();
            }
        });
        btnAurrera.setOnAction(e -> {
            int maxPage = (int) Math.ceil((double) masterData.size() / ROWS_PER_PAGE) - 1;
            if (currentPage < maxPage) {
                currentPage++;
                updatePagination();
            }
        });

        kargatuDatuak();
    }

    private void updatePagination() {
        if (masterData == null || masterData.isEmpty()) {
            table.getItems().clear();
            lblOrrialdea.setText("0 / 0");
            btnAtzera.setDisable(true);
            btnAurrera.setDisable(true);
            return;
        }

        int totalPages = (int) Math.ceil((double) masterData.size() / ROWS_PER_PAGE);
        if (currentPage >= totalPages) currentPage = totalPages - 1;
        if (currentPage < 0) currentPage = 0;

        int fromIndex = currentPage * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, masterData.size());

        List<Erreserba> pageItems = masterData.subList(fromIndex, toIndex);
        table.getItems().setAll(pageItems);

        lblOrrialdea.setText((currentPage + 1) + " / " + totalPages);
        btnAtzera.setDisable(currentPage == 0);
        btnAurrera.setDisable(currentPage >= totalPages - 1);
    }

    private void kargatuDatuak() {
        colData.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getData()));
        colMota.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getMota()));
        colErabiltzaileaId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getErabiltzaileak_id()));
        colMahaiaId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getMahaiak_id()));

        colAkzioak.setCellFactory(col -> new TableCell<>() {
            private final Button btnUpdate = new Button("✎");
            private final Button btnDelete = new Button("✖");
            private final HBox box = new HBox(5, btnUpdate, btnDelete);

            {
                btnUpdate.getStyleClass().add("edit-button");
                btnDelete.getStyleClass().add("delete-button");
                box.getStyleClass().add("actions-cell");
                box.setAlignment(javafx.geometry.Pos.CENTER);

                btnUpdate.setOnAction(e -> {
                    Erreserba r = getTableView().getItems().get(getIndex());
                    mostrarDialogoUpdate(r);
                });

                btnDelete.setOnAction(e -> {
                    Erreserba r = getTableView().getItems().get(getIndex());
                    deleteErreserba(r.getId());
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
                .uri(URI.create(API_BASE_URL + "/Erreserbak"))
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(json -> {
                    try {
                        List<Erreserba> list = mapper.readValue(json, new TypeReference<List<Erreserba>>() {});
                        Platform.runLater(() -> {
                            masterData = list;
                            currentPage = 0;
                            updatePagination();
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    private void updateErreserba(Erreserba r) {
        try {
            String json = buildJson(r);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/Erreserba/" + r.getId()))
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .header("Content-Type", "application/json")
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(resp -> {
                        System.out.println("Update OK: " + resp.statusCode());
                        Platform.runLater(this::kargatuDatuak);
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteErreserba(int id) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/Erreserba/" + id))
                .DELETE()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(resp -> {
                    System.out.println("Delete OK: " + resp.statusCode());
                    Platform.runLater(this::kargatuDatuak);
                });
    }

    private void insertErreserba(Erreserba r) {
        try {
            String json = buildJson(r);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/Erreserbak"))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .header("Content-Type", "application/json")
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(resp -> {
                        System.out.println("Insert OK: " + resp.statusCode());
                        Platform.runLater(this::kargatuDatuak);
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarDialogoInsert() {
        Dialog<Erreserba> dialog = new Dialog<>();
        dialog.setTitle("Erreserba sartu");
        dialog.setHeaderText("Sartu erreserbaren informazioa:");
        ButtonType okButton = new ButtonType("Sartu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField tfData = new TextField();
        TextField tfMota = new TextField();
        TextField tfErabiltzaileaId = new TextField();
        TextField tfMahaiaId = new TextField();

        grid.add(new Label("Data:"), 0, 0); grid.add(tfData, 1, 0);
        grid.add(new Label("Mota:"), 0, 1); grid.add(tfMota, 1, 1);
        grid.add(new Label("Erabiltzailea ID:"), 0, 2); grid.add(tfErabiltzaileaId, 1, 2);
        grid.add(new Label("Mahaia ID:"), 0, 3); grid.add(tfMahaiaId, 1, 3);

        dialog.getDialogPane().setContent(grid);

        final Button btOk = (Button) dialog.getDialogPane().lookupButton(okButton);
        btOk.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                Integer.parseInt(tfMota.getText());
                Integer.parseInt(tfErabiltzaileaId.getText());
                Integer.parseInt(tfMahaiaId.getText());
            } catch (NumberFormatException e) {
                event.consume();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Errorea");
                alert.setHeaderText("Datu okerrak");
                alert.setContentText("Mota, Erabiltzailea ID eta Mahaia ID zenbakiak izan behar dira.");
                alert.showAndWait();
            }
        });

        dialog.setResultConverter(button -> {
            if (button == okButton) {
                Erreserba r = new Erreserba();
                r.setData(tfData.getText());
                r.setMota(Integer.parseInt(tfMota.getText()));
                r.setErabiltzaileak_id(Integer.parseInt(tfErabiltzaileaId.getText()));
                r.setMahaiak_id(Integer.parseInt(tfMahaiaId.getText()));
                return r;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(this::insertErreserba);
    }

    private void mostrarDialogoUpdate(Erreserba r) {
        Dialog<Erreserba> dialog = new Dialog<>();
        dialog.setTitle("Erreserba eguneratu");
        dialog.setHeaderText("Aldatu erreserbaren datuak:");
        ButtonType okButton = new ButtonType("Gorde", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField tfData = new TextField(r.getData());
        TextField tfMota = new TextField(String.valueOf(r.getMota()));
        TextField tfErabiltzaileaId = new TextField(String.valueOf(r.getErabiltzaileak_id()));
        TextField tfMahaiaId = new TextField(String.valueOf(r.getMahaiak_id()));

        grid.add(new Label("Data:"), 0, 0); grid.add(tfData, 1, 0);
        grid.add(new Label("Mota:"), 0, 1); grid.add(tfMota, 1, 1);
        grid.add(new Label("Erabiltzailea ID:"), 0, 2); grid.add(tfErabiltzaileaId, 1, 2);
        grid.add(new Label("Mahaia ID:"), 0, 3); grid.add(tfMahaiaId, 1, 3);

        dialog.getDialogPane().setContent(grid);

        final Button btOk = (Button) dialog.getDialogPane().lookupButton(okButton);
        btOk.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                Integer.parseInt(tfMota.getText());
                Integer.parseInt(tfErabiltzaileaId.getText());
                Integer.parseInt(tfMahaiaId.getText());
            } catch (NumberFormatException e) {
                event.consume();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Errorea");
                alert.setHeaderText("Datu okerrak");
                alert.setContentText("Mota, Erabiltzailea ID eta Mahaia ID zenbakiak izan behar dira.");
                alert.showAndWait();
            }
        });

        dialog.setResultConverter(button -> {
            if (button == okButton) {
                r.setData(tfData.getText());
                r.setMota(Integer.parseInt(tfMota.getText()));
                r.setErabiltzaileak_id(Integer.parseInt(tfErabiltzaileaId.getText()));
                r.setMahaiak_id(Integer.parseInt(tfMahaiaId.getText()));
                return r;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(this::updateErreserba);
    }

    private String buildJson(Erreserba r) {
        return String.format(
                "{\"Data\":\"%s\",\"Mota\":%d,\"Erabiltzaileak_id\":%d,\"Mahaiak_id\":%d}",
                escape(r.getData()),
                r.getMota(),
                r.getErabiltzaileak_id(),
                r.getMahaiak_id()
        );
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
