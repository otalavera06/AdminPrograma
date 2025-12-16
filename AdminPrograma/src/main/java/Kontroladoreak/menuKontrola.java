package Kontroladoreak;

import Pantailak.LoginPantaila;
import javafx.animation.TranslateTransition;
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
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class menuKontrola implements Initializable {

    @FXML private Button btnItxi;
    @FXML private Button btnMinimizatu;
    @FXML private Label Menu;
    @FXML private Label MenuBack;
    @FXML private AnchorPane slider;
    @FXML private Button btnSaioaBukatu;
    @FXML private VBox menuVBox;

    // Botones del menú lateral
    @FXML private Button btnList1; // Produktuak
    @FXML private Button btnList2; // Langileak
    @FXML private Button btnList3;
    @FXML private Button btnList4;
    @FXML private Button btnList5;
    @FXML private Button btnList6;
    @FXML private Button btnList7;
    @FXML private Button btnList8;

    // Contenedor central donde se cargan las vistas
    @FXML private StackPane contentArea;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnItxi.setOnMouseClicked(e -> System.exit(0));

        BorderPane root = (BorderPane) slider.getParent();
        slider.prefWidthProperty().bind(root.widthProperty().multiply(0.2));
        slider.prefHeightProperty().bind(root.heightProperty());

        slider.setTranslateX(0);
        Menu.setVisible(false);
        MenuBack.setVisible(true);

        // Ajuste dinámico del alto del VBox y del tamaño de fuente
        root.heightProperty().addListener((obs, oldVal, newVal) -> {
            double totalHeight = newVal.doubleValue();
            double vboxHeight = totalHeight * 0.7;
            menuVBox.setPrefHeight(vboxHeight);

            double buttonHeight = vboxHeight / menuVBox.getChildren().size();
            double fontSize = Math.max(12, buttonHeight * 0.4);

            menuVBox.getChildren().forEach(node -> {
                if (node instanceof Button btn) {
                    btn.setPrefHeight(buttonHeight);
                    btn.setStyle("-fx-font-size: " + fontSize + "px; "
                            + "-fx-background-color: transparent; "
                            + "-fx-text-fill: white;");
                }
            });
        });

        // Lógica de abrir/cerrar menú (slider)
        Menu.setOnMouseClicked(e -> toggleSlider());
        MenuBack.setOnMouseClicked(e -> toggleSlider());

        // Conexión de botones del menú lateral
        btnList2.setOnAction(e -> kargatuPantaila("/org/example/adminprograma/Pantailak/Langileak.fxml"));

    }

    private void toggleSlider() {
        TranslateTransition slide = new TranslateTransition();
        slide.setDuration(Duration.seconds(0.4));
        slide.setNode(slider);

        if (slider.getTranslateX() != 0) {
            slide.setToX(0);
            slide.play();
            slide.setOnFinished((Act) -> {
                Menu.setVisible(false);
                MenuBack.setVisible(true);
            });
        } else {
            slide.setToX(-slider.getPrefWidth());
            slide.play();
            slide.setOnFinished((Act) -> {
                Menu.setVisible(true);
                MenuBack.setVisible(false);
            });
        }
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

    /** Método genérico para cargar vistas en el centro */
    private void kargatuPantaila(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();

            // Limpiar y añadir la nueva vista al contentArea
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
