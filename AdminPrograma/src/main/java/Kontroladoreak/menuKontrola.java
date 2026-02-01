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


        // Añadir iconos a los botones
        btnList1.setText("🍱 Produktuak");
        btnList2.setText("👨‍🍳 Langileak");
        btnList3.setText("📅 Erreserbak");
        btnList5.setText("🧾 Fakturak");
        btnList6.setText("👥 Erabiltzaileak");
        btnList8.setText("📦 Stock");

        // Ajuste dinámico del VBox y botones
        javafx.beans.value.ChangeListener<Number> resizeListener = (obs, oldVal, newVal) -> {
            double totalHeight = root.getHeight();
            double totalWidth = root.getWidth();
            
            // Altura del menú: 70% de la altura total
            double vboxHeight = totalHeight * 0.7;
            menuVBox.setPrefHeight(vboxHeight);

            double buttonHeight = vboxHeight / menuVBox.getChildren().size();
            
            // Cálculo del tamaño de fuente:
            // 1. Basado en altura: 40% de la altura del botón
            double fontHeightBased = buttonHeight * 0.4;
            
            // 2. Basado en anchura: El botón es el 20% del ancho total.
            // "Erabiltzaileak" tiene ~15 caracteres + icono.
            // Necesitamos que quepa. Factor conservador ~1.2 px por punto de fuente.
            // Ancho disponible ~ totalWidth * 0.18 (dejando margen)
            double availableWidth = totalWidth * 0.18;
            double fontWidthBased = availableWidth / 10.0; // Dividimos por un factor para el texto largo
            
            // Elegimos el menor para que quepa siempre, con un mínimo de 12px y máximo de 20px
            double fontSize = Math.min(fontHeightBased, fontWidthBased);
            fontSize = Math.max(12, Math.min(fontSize, 20));

            String fontStyle = "-fx-font-size: " + (int)fontSize + "px; -fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-padding: 0 0 0 20;";

            menuVBox.getChildren().forEach(node -> {
                if (node instanceof Button btn) {
                    btn.setPrefHeight(buttonHeight);
                    btn.setStyle(fontStyle);
                }
            });
        };

        // Escuchar cambios en altura y anchura
        root.heightProperty().addListener(resizeListener);
        root.widthProperty().addListener(resizeListener);

        // Conexión de botones del menú
        btnList1.setOnAction(e -> kargatuPantaila("/org/example/adminprograma/Pantailak/Produktuak.fxml"));
        btnList2.setOnAction(e -> kargatuPantaila("/org/example/adminprograma/Pantailak/Langileak.fxml"));
        btnList3.setOnAction(e -> kargatuPantaila("/org/example/adminprograma/Pantailak/Erreserbak.fxml"));
        // btnList4 no se usa en el FXML actual
        btnList5.setOnAction(e -> kargatuPantaila("/org/example/adminprograma/Pantailak/Fakturak.fxml"));
        btnList6.setOnAction(e -> kargatuPantaila("/org/example/adminprograma/Pantailak/Erabiltzaileak.fxml"));
        btnList8.setOnAction(e -> kargatuPantaila("/org/example/adminprograma/Pantailak/Stock.fxml"));
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
