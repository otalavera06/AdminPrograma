package Kontroladoreak;

import Modeloak.Faktura;
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

public class FakturaKontrola {

    @FXML private TableView<Faktura> table;
    @FXML private TableColumn<Faktura, Number> colPrezioTotala;
    @FXML private TableColumn<Faktura, Number> colSortuta;
    @FXML private TableColumn<Faktura, String> colPath;
    @FXML private TableColumn<Faktura, Number> colZerbitzuaId;
    @FXML private TableColumn<Faktura, Void> colAkzioak;
    @FXML private Button btnInsert;

    @FXML private Button btnAtzera;
    @FXML private Button btnAurrera;
    @FXML private Label lblOrrialdea;

    private java.util.List<Faktura> masterData = new java.util.ArrayList<>();
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

        List<Faktura> pageItems = masterData.subList(fromIndex, toIndex);
        table.getItems().setAll(pageItems);

        lblOrrialdea.setText((currentPage + 1) + " / " + totalPages);
        btnAtzera.setDisable(currentPage == 0);
        btnAurrera.setDisable(currentPage >= totalPages - 1);
    }

    private void kargatuDatuak() {
        colPrezioTotala.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getPrezio_totala()));
        colSortuta.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getSortuta()));
        colPath.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPath()));
        colZerbitzuaId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getZerbitzua_id()));

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
                    Faktura f = getTableView().getItems().get(getIndex());
                    mostrarDialogoUpdate(f);
                });

                btnDelete.setOnAction(e -> {
                    Faktura f = getTableView().getItems().get(getIndex());
                    deleteFaktura(f.getId());
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
                .uri(URI.create(API_BASE_URL + "/Fakturak"))
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(json -> {
                    try {
                        List<Faktura> list = mapper.readValue(json, new TypeReference<List<Faktura>>() {});
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

    private void updateFaktura(Faktura f) {
        try {
            String json = buildJson(f);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/Faktura/" + f.getId()))
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

    private void deleteFaktura(int id) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/Faktura/" + id))
                .DELETE()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(resp -> {
                    System.out.println("Delete OK: " + resp.statusCode());
                    Platform.runLater(this::kargatuDatuak);
                });
    }

    private void insertFaktura(Faktura f) {
        try {
            String json = buildJson(f);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/Fakturak"))
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
        Dialog<Faktura> dialog = new Dialog<>();
        dialog.setTitle("Faktura sartu");
        dialog.setHeaderText("Sartu fakturaren informazioa:");
        ButtonType okButton = new ButtonType("Sartu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField tfPrezioa = new TextField();
        TextField tfSortuta = new TextField();
        TextField tfPath = new TextField();
        TextField tfZerbitzuaId = new TextField();

        grid.add(new Label("Prezio Totala:"), 0, 0); grid.add(tfPrezioa, 1, 0);
        grid.add(new Label("Sortuta (0/1):"), 0, 1); grid.add(tfSortuta, 1, 1);
        grid.add(new Label("Path:"), 0, 2); grid.add(tfPath, 1, 2);
        grid.add(new Label("Zerbitzua ID:"), 0, 3); grid.add(tfZerbitzuaId, 1, 3);

        dialog.getDialogPane().setContent(grid);

        final Button btOk = (Button) dialog.getDialogPane().lookupButton(okButton);
        btOk.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                Double.parseDouble(tfPrezioa.getText());
                Integer.parseInt(tfSortuta.getText());
                Integer.parseInt(tfZerbitzuaId.getText());
            } catch (NumberFormatException e) {
                event.consume();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Errorea");
                alert.setHeaderText("Datu okerrak");
                alert.setContentText("Prezioa, Sortuta eta Zerbitzua ID zenbakiak izan behar dira.");
                alert.showAndWait();
            }
        });

        dialog.setResultConverter(button -> {
            if (button == okButton) {
                Faktura f = new Faktura();
                f.setPath(tfPath.getText());
                f.setPrezio_totala(Double.parseDouble(tfPrezioa.getText()));
                f.setSortuta(Integer.parseInt(tfSortuta.getText()));
                f.setZerbitzua_id(Integer.parseInt(tfZerbitzuaId.getText()));
                return f;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(this::insertFaktura);
    }

    private void mostrarDialogoUpdate(Faktura f) {
        Dialog<Faktura> dialog = new Dialog<>();
        dialog.setTitle("Faktura eguneratu");
        dialog.setHeaderText("Aldatu fakturaren datuak:");
        ButtonType okButton = new ButtonType("Gorde", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField tfPrezioa = new TextField(String.valueOf(f.getPrezio_totala()));
        TextField tfSortuta = new TextField(String.valueOf(f.getSortuta()));
        TextField tfPath = new TextField(f.getPath());
        TextField tfZerbitzuaId = new TextField(String.valueOf(f.getZerbitzua_id()));

        grid.add(new Label("Prezio Totala:"), 0, 0); grid.add(tfPrezioa, 1, 0);
        grid.add(new Label("Sortuta (0/1):"), 0, 1); grid.add(tfSortuta, 1, 1);
        grid.add(new Label("Path:"), 0, 2); grid.add(tfPath, 1, 2);
        grid.add(new Label("Zerbitzua ID:"), 0, 3); grid.add(tfZerbitzuaId, 1, 3);

        dialog.getDialogPane().setContent(grid);

        final Button btOk = (Button) dialog.getDialogPane().lookupButton(okButton);
        btOk.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                Double.parseDouble(tfPrezioa.getText());
                Integer.parseInt(tfSortuta.getText());
                Integer.parseInt(tfZerbitzuaId.getText());
            } catch (NumberFormatException e) {
                event.consume();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Errorea");
                alert.setHeaderText("Datu okerrak");
                alert.setContentText("Prezioa, Sortuta eta Zerbitzua ID zenbakiak izan behar dira.");
                alert.showAndWait();
            }
        });

        dialog.setResultConverter(button -> {
            if (button == okButton) {
                f.setPath(tfPath.getText());
                f.setPrezio_totala(Double.parseDouble(tfPrezioa.getText()));
                f.setSortuta(Integer.parseInt(tfSortuta.getText()));
                f.setZerbitzua_id(Integer.parseInt(tfZerbitzuaId.getText()));
                return f;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(this::updateFaktura);
    }

    private String buildJson(Faktura f) {
        return String.format(
                "{\"Prezio_totala\":%s,\"Sortuta\":%d,\"Path\":\"%s\",\"Zerbitzua_id\":%d}",
                String.valueOf(f.getPrezio_totala()).replace(",", "."),
                f.getSortuta(),
                escape(f.getPath()),
                f.getZerbitzua_id()
        );
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
