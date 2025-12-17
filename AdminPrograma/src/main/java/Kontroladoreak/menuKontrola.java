package Kontroladoreak;

import Pantailak.LoginPantaila;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class menuKontrola implements Initializable {

    @FXML private BorderPane root;
    @FXML private Button btnItxi;
    @FXML private Button btnMinimizatu;
    @FXML private AnchorPane slider;
    @FXML private Button btnSaioaBukatu;
    @FXML private VBox menuVBox;

    // Botones del menú lateral
    @FXML private Button btnList1;
    @FXML private Button btnList2;
    @FXML private Button btnList3;
    @FXML private Button btnList4;
    @FXML private Button btnList5;
    @FXML private Button btnList6;
    @FXML private Button btnList7;
    @FXML private Button btnList8;

    // Contenedor central
    @FXML private StackPane contentArea;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // Botón cerrar aplicación
        btnItxi.setOnMouseClicked(e -> System.exit(0));

        // Barra lateral fija
        root.setLeft(slider);
        slider.setTranslateX(0);

        // Tamaño relativo del menú
        slider.prefWidthProperty().bind(root.widthProperty().multiply(0.2));
        slider.prefHeightProperty().bind(root.heightProperty());


        // Ajuste dinámico del VBox y botones
        root.heightProperty().addListener((obs, oldVal, newVal) -> {
            double totalHeight = newVal.doubleValue();
            double vboxHeight = totalHeight * 0.7;
            menuVBox.setPrefHeight(vboxHeight);

            double buttonHeight = vboxHeight / menuVBox.getChildren().size();
            double fontSize = Math.max(12, buttonHeight * 0.4);

            menuVBox.getChildren().forEach(node -> {
                if (node instanceof Button btn) {
                    btn.setPrefHeight(buttonHeight);
                    btn.setStyle(
                            "-fx-font-size: " + fontSize + "px;" +
                                    "-fx-background-color: transparent;" +
                                    "-fx-text-fill: white;"
                    );
                }
            });
        });

        // Conexión de botones del menú
        btnList2.setOnAction(e ->
                kargatuPantaila("/org/example/adminprograma/Pantailak/Langileak.fxml")
        );
    }

    @FXML
    private void saioaBukatu() {
        try {
            Stage stage = (Stage) btnSaioaBukatu.getScene().getWindow();
            stage.close();
            LoginPantaila login = new LoginPantaila();
            login.irekiLogina();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void lehioaJaitsi() {
        Stage stage = (Stage) btnMinimizatu.getScene().getWindow();
        stage.setIconified(true);
    }

    /** Cargar vistas en el centro */
    private void kargatuPantaila(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
