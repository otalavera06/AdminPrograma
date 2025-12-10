package Kontroladoreak;

import Pantailak.LoginPantaila;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.adminprograma.Kontrola;

import java.net.URL;
import java.util.ResourceBundle;

public class menuKontrola extends Kontrola implements Initializable {
    @FXML
    private Button btnItxi;
    @FXML
    private Button btnMinimizatu;

    @FXML
    private Label Menu;
    @FXML
    private Label MenuBack;
    @FXML
    private AnchorPane slider;

    @FXML
    private Button btnSaioaBukatu;

    @FXML
    private VBox menuVBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnItxi.setOnMouseClicked(e -> System.exit(0));

        // Binding responsive: 20% del ancho del BorderPane padre, 100% del alto
        BorderPane root = (BorderPane) slider.getParent();
        slider.prefWidthProperty().bind(root.widthProperty().multiply(0.2));
        slider.prefHeightProperty().bind(root.heightProperty());

        // Inicialmente visible
        slider.setTranslateX(0);
        Menu.setVisible(false);
        MenuBack.setVisible(true);

        // Ajuste dinámico del alto del VBox y del tamaño de fuente
        root.heightProperty().addListener((obs, oldVal, newVal) -> {
            double totalHeight = newVal.doubleValue();

            // El VBox ocupa el 70% de la altura de la ventana → centrado en el AnchorPane
            double vboxHeight = totalHeight * 0.7;
            menuVBox.setPrefHeight(vboxHeight);

            // Cada botón ocupa 1/8 del VBox → calculamos el alto de cada botón
            double buttonHeight = vboxHeight / menuVBox.getChildren().size();
            double fontSize = Math.max(12, buttonHeight * 0.4); // fuente proporcional al alto del botón

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
        Menu.setOnMouseClicked(e -> {
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
        });

        MenuBack.setOnMouseClicked(e -> {
            TranslateTransition slide = new TranslateTransition();
            slide.setDuration(Duration.seconds(0.4));
            slide.setNode(slider);
            slide.setToX(-slider.getPrefWidth());
            slide.play();
            slide.setOnFinished((Act) -> {
                Menu.setVisible(true);
                MenuBack.setVisible(false);
            });
        });
    }

    @Override
    protected void lehioaJaitsi() {
        super.lehioaJaitsi();
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
}
