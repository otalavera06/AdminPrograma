package Kontroladoreak;

import Modeloak.Produktua;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

    private static final String API_URL = "http://localhost:5005/api/Produktua";

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

                        // Lógica de colores según lo solicitado
                        if (stock < min) {
                            // Rojo si está por debajo del mínimo
                            setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: black; -fx-alignment: CENTER;");
                        } else if (stock >= min && stock < max) {
                            // Amarillo si está entre mínimo y máximo
                            setStyle("-fx-background-color: #feca57; -fx-text-fill: black; -fx-alignment: CENTER;");
                        } else if (stock == max) {
                            // Verde si es igual al máximo
                            setStyle("-fx-background-color: #1dd1a1; -fx-text-fill: black; -fx-alignment: CENTER;");
                        } else {
                            // Por si acaso (mayor que max o algo no contemplado), verde también o por defecto
                            setStyle("-fx-background-color: #1dd1a1; -fx-text-fill: black; -fx-alignment: CENTER;");
                        }
                    }
                }
            }
        });

        kargatuDatuak();
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
                                table.getItems().clear();
                                table.getItems().addAll(produktuak);
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