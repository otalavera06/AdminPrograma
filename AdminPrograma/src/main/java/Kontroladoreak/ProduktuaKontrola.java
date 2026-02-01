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
import javafx.stage.FileChooser;
import java.io.File;

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

    @FXML private Button btnAtzera;
    @FXML private Button btnAurrera;
    @FXML private Label lblOrrialdea;

    private java.util.List<Produktua> masterData = new java.util.ArrayList<>();
    private int currentPage = 0;
    private static final int ROWS_PER_PAGE = 13;

    private static final String API_BASE_URL = "http://192.168.1.104:5005/api";
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

    @FXML
    public void initialize() {
        // "Berria" botoia kendu (erabiltzailearen eskaera)
        if (btnInsert != null) {
            btnInsert.setVisible(false);
            btnInsert.setManaged(false);
        }

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

        List<Produktua> pageItems = masterData.subList(fromIndex, toIndex);
        table.getItems().setAll(pageItems);

        lblOrrialdea.setText((currentPage + 1) + " / " + totalPages);
        btnAtzera.setDisable(currentPage == 0);
        btnAurrera.setDisable(currentPage >= totalPages - 1);
    }

    private void kargatuDatuak() {
        colIzena.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getIzena()));
        colPrezioa.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getPrezioa()));
        colStock.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getStock()));

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
                .uri(URI.create(API_BASE_URL + "/Produktuak"))
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            List<Produktua> list = mapper.readValue(response.body(), new TypeReference<List<Produktua>>() {});
                            Platform.runLater(() -> {
                                masterData = list;
                                currentPage = 0;
                                updatePagination();
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                            Platform.runLater(() -> erakutsiErrorea("Errorea datuak irakurtzean: " + e.getMessage()));
                        }
                    } else {
                        Platform.runLater(() -> erakutsiErrorea("Zerbitzari errorea: " + response.statusCode()));
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    Platform.runLater(() -> erakutsiErrorea("Konexio errorea: " + e.getMessage()));
                    return null;
                });
    }

    private void erakutsiErrorea(String mezua) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errorea");
        alert.setHeaderText(null);
        alert.setContentText(mezua);
        alert.showAndWait();
    }

    private void updateProduktua(Produktua p) {
        if (!isMotaValid(p.getProduktuen_motak_id())) {
            erakutsiErrorea("Errorea: Produktu mota ez da existitzen (ID: " + p.getProduktuen_motak_id() + ")");
            return;
        }
        try {
            String json = buildJson(p);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/Produktuak/" + p.getId()))
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .header("Content-Type", "application/json")
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(resp -> {
                        if (resp.statusCode() == 200 || resp.statusCode() == 204) {
                            System.out.println("Update OK: " + resp.statusCode());
                            kargatuDatuak(); // Recargar datos para ver cambios
                        } else {
                            Platform.runLater(() -> erakutsiErrorea("Update error: " + resp.statusCode() + " - " + resp.body()));
                        }
                    })
                    .exceptionally(e -> {
                        e.printStackTrace();
                        Platform.runLater(() -> erakutsiErrorea("Update konexio errorea: " + e.getMessage()));
                        return null;
                    });
        } catch (Exception e) {
            e.printStackTrace();
            erakutsiErrorea("Errorea eguneratzean: " + e.getMessage());
        }
    }

    private void deleteProduktua(int id) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/Produktuak/" + id))
                .DELETE()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(resp -> Platform.runLater(() -> {
                    if (resp.statusCode() == 200 || resp.statusCode() == 204) {
                        table.getItems().removeIf(p -> p.getId() == id);
                    } else {
                        erakutsiErrorea("Delete error: " + resp.statusCode() + " - " + resp.body());
                    }
                }))
                .exceptionally(e -> {
                    e.printStackTrace();
                    Platform.runLater(() -> erakutsiErrorea("Delete konexio errorea: " + e.getMessage()));
                    return null;
                });
    }

    private void insertProduktua(Produktua p) {
        if (!isMotaValid(p.getProduktuen_motak_id())) {
            erakutsiErrorea("Errorea: Produktu mota ez da existitzen (ID: " + p.getProduktuen_motak_id() + ")");
            return;
        }
        try {
            String json = buildJson(p);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/Produktuak"))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .header("Content-Type", "application/json")
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(resp -> {
                        if (resp.statusCode() == 200 || resp.statusCode() == 201) {
                            System.out.println("Insert OK: " + resp.statusCode());
                            kargatuDatuak();
                        } else {
                            Platform.runLater(() -> erakutsiErrorea("Insert error: " + resp.statusCode() + " - " + resp.body()));
                        }
                    })
                    .exceptionally(e -> {
                        e.printStackTrace();
                        Platform.runLater(() -> erakutsiErrorea("Insert konexio errorea: " + e.getMessage()));
                        return null;
                    });
        } catch (Exception e) {
            e.printStackTrace();
            erakutsiErrorea("Errorea txertatzean: " + e.getMessage());
        }
    }

    private boolean isMotaValid(int id) {
        return motaOptions.stream().anyMatch(m -> m.id == id);
    }

    // Helper inner class for ComboBox items
    private static class MotaOption {
        int id;
        String name;

        public MotaOption(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    // List of available product types
    private final java.util.List<MotaOption> motaOptions = java.util.List.of(
            new MotaOption(6, "edariak"),
            new MotaOption(7, "txutxeriak"),
            new MotaOption(8, "maki"),
            new MotaOption(9, "uramaki"),
            new MotaOption(10, "narezushi"),
            new MotaOption(11, "inarizushi"),
            new MotaOption(12, "oshizushi"),
            new MotaOption(13, "nigiri"),
            new MotaOption(14, "postreak")
    );

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

        grid.add(new Label("Izena:"), 0, 0); grid.add(tfIzena, 1, 0);
        grid.add(new Label("Prezioa:"), 0, 1); grid.add(tfPrezioa, 1, 1);
        grid.add(new Label("Stock:"), 0, 2); grid.add(tfStock, 1, 2);

        dialog.getDialogPane().setContent(grid);

        final Button btOk = (Button) dialog.getDialogPane().lookupButton(okButton);
        btOk.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                Double.parseDouble(tfPrezioa.getText());
                Integer.parseInt(tfStock.getText());
            } catch (NumberFormatException e) {
                event.consume();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Errorea");
                alert.setHeaderText("Datu okerrak");
                alert.setContentText("Prezioa eta Stock zenbakiak izan behar dira.");
                alert.showAndWait();
            }
        });

        dialog.setResultConverter(button -> {
            if (button == okButton) {
                Produktua p = new Produktua();
                p.setIzena(tfIzena.getText());
                p.setPrezioa(Double.parseDouble(tfPrezioa.getText()));
                int stock = Integer.parseInt(tfStock.getText());
                p.setStock(stock);
                
                // Balio lehenetsiak
                p.setIrudia_path("");
                // Mota lehenetsia 8 (Maki)
                p.setProduktuen_motak_id(8);

                // Stock Minimoa: beti 5
                p.setStock_min(5);

                // Stock Maximoa: default logika
                p.setStock_max(Math.max(stock, 20));

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

        grid.add(new Label("Izena:"), 0, 0); grid.add(tfIzena, 1, 0);
        grid.add(new Label("Prezioa:"), 0, 1); grid.add(tfPrezioa, 1, 1);
        grid.add(new Label("Stock:"), 0, 2); grid.add(tfStock, 1, 2);

        dialog.getDialogPane().setContent(grid);

        final Button btOk = (Button) dialog.getDialogPane().lookupButton(okButton);
        btOk.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                Double.parseDouble(tfPrezioa.getText());
                Integer.parseInt(tfStock.getText());
            } catch (NumberFormatException e) {
                event.consume();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Errorea");
                alert.setHeaderText("Datu okerrak");
                alert.setContentText("Prezioa eta Stock zenbakiak izan behar dira.");
                alert.showAndWait();
            }
        });

        dialog.setResultConverter(button -> {
            if (button == okButton) {
                p.setIzena(tfIzena.getText());
                p.setPrezioa(Double.parseDouble(tfPrezioa.getText()));
                int stock = Integer.parseInt(tfStock.getText());
                p.setStock(stock);
                
                // Mota ID eta Irudia mantendu egiten dira, baina Stock Maximoa eguneratu daiteke stock berriarekin
                int typeId = p.getProduktuen_motak_id();

                // Eguneratu Stock Maximoa motaren arabera
                int stockMax;
                if (typeId == 6) {
                    stockMax = 60;
                } else if (typeId == 7) {
                    stockMax = 40;
                } else if (typeId == 8 || typeId == 9 || typeId == 11 || typeId == 13) {
                    stockMax = 80;
                } else if (typeId == 14) {
                    stockMax = 60;
                } else {
                    stockMax = Math.max(stock, 20);
                }
                p.setStock_max(stockMax);
                p.setStock_min(5);

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
