package Kontroladoreak;

import Modeloak.Produktua;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.control.Alert;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class StockKontrola implements Initializable {

    @FXML private TableView<Produktua> table;
    @FXML private TableColumn<Produktua, String> colIzena;
    @FXML private TableColumn<Produktua, Integer> colStock;
    @FXML private Button btnAtzera;
    @FXML private Button btnAurrera;
    @FXML private Label lblOrrialdea;

    private java.util.List<Produktua> masterData = new java.util.ArrayList<>();
    private int currentPage = 0;
    private static final int ROWS_PER_PAGE = 13;

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

    private static final String API_URL = "http://192.168.1.104:5005/api/Produktuak";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colIzena.setCellValueFactory(new PropertyValueFactory<>("izena"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        // Custom Cell Factory for Stock column
        colStock.setCellFactory(column -> new TableCell<Produktua, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.valueOf(item));
                    
                    // Obtener la fila actual para acceder a stock_min y stock_max
                    Produktua produktua = getTableView().getItems().get(getIndex());
                    if (produktua != null) {
                        int stock = item;
                        int min = produktua.getStock_min();
                        int max = produktua.getStock_max();
                        int typeId = produktua.getProduktuen_motak_id();

                        // Fallback logic if min/max are 0 (legacy data)
                        if (min == 0) min = 5;
                        if (max == 0) {
                             if (typeId == 6) {
                                max = 60;
                            } else if (typeId == 7) {
                                max = 40;
                            } else if (typeId == 8 || typeId == 9 || typeId == 11 || typeId == 13) {
                                max = 80;
                            } else if (typeId == 14) {
                                max = 60;
                            } else {
                                max = Math.max(stock, 20);
                            }
                        }

                        // Lógica de colores
                        if (stock < min) {
                            // Rojo: Stock bajo mínimos
                            setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: black; -fx-alignment: CENTER;");
                        } else if (stock >= max) {
                            // Verde: Stock lleno
                            setStyle("-fx-background-color: #1dd1a1; -fx-text-fill: black; -fx-alignment: CENTER;");
                        } else {
                            // Amarillo: Stock normal (entre min y max)
                            setStyle("-fx-background-color: #feca57; -fx-text-fill: black; -fx-alignment: CENTER;");
                        }
                    }
                }
            }
        });

        kargatuDatuak();
        
        btnAtzera.setOnAction(e -> {
            if (currentPage > 0) {
                currentPage--;
                updatePagination();
            }
        });

        btnAurrera.setOnAction(e -> {
            int totalPages = (int) Math.ceil((double) masterData.size() / ROWS_PER_PAGE);
            if (currentPage < totalPages - 1) {
                currentPage++;
                updatePagination();
            }
        });
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

        if (currentPage < 0) currentPage = 0;
        if (currentPage >= totalPages) currentPage = totalPages - 1;

        int fromIndex = currentPage * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, masterData.size());

        List<Produktua> pageItems = masterData.subList(fromIndex, toIndex);
        table.getItems().setAll(pageItems);

        lblOrrialdea.setText((currentPage + 1) + " / " + totalPages);

        btnAtzera.setDisable(currentPage == 0);
        btnAurrera.setDisable(currentPage >= totalPages - 1);
    }

    private void kargatuDatuak() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            List<Produktua> produktuak = mapper.readValue(response.body(), new TypeReference<List<Produktua>>() {});
                            Platform.runLater(() -> {
                                masterData = new java.util.ArrayList<>(produktuak);
                                currentPage = 0;
                                updatePagination();
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            Platform.runLater(() -> erakutsiErrorea("Errorea datuak irakurtzean: " + e.getMessage()));
                        }
                    } else {
                        Platform.runLater(() -> erakutsiErrorea("Errorea zerbitzarian: " + response.statusCode()));
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
}