package Kontroladoreak;

import Pantailak.LoginPantaila;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.adminprograma.Kontrola;

import java.awt.event.ActionEvent;
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
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnItxi.setOnMouseClicked(e -> {
           System.exit(0);
        });
        slider.setTranslateX(-176);
        Menu.setOnMouseClicked(e -> {
            TranslateTransition slide = new TranslateTransition();
            slide.setDuration(Duration.seconds(0.4));
            slide.setNode(slider);

            slide.setToX(-176);
            slide.play();

            slider.setTranslateX(-176);
            slide.setOnFinished((Act)->{
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
    private void saioaBukatu(){
        try {
            Stage stage = (Stage) btnSaioaBukatu.getScene().getWindow();
            stage.close();
            LoginPantaila login = new  LoginPantaila();
            login.irekiLogina();
        }catch (Exception e){
            e.printStackTrace();
        }
    }



}
