package org.example.adminprograma;

import DatuBaseak.Erabiltzailea;
import Pantailak.MenuPantaila;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.awt.*;
import java.net.URL;
import java.util.ResourceBundle;

public class erabiltzaileKontrola implements Initializable {

    @FXML
    private TextField txtErabiltzailea;
    @FXML
    private PasswordField txtPasahitza;

    private final Jakinarazpenak jakinarazpena = new  Jakinarazpenak();

    @FXML
    private Button btnLogin;

    @FXML
    private Button btnItxi;
    @FXML
    private Button btnMinimizatu;


    @Override
    public void initialize(URL location, ResourceBundle resources){
        btnItxi.setOnMouseClicked(e -> {
            System.exit(0);
        });
    }

    @FXML
    protected void lehioaJaitsi() {
        Stage stage = (Stage) btnMinimizatu.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    protected void btnLoginEgin() {
        String erabiltzailea = txtErabiltzailea.getText();
        String pasahitza = txtPasahitza.getText();
        Erabiltzailea logina = new Erabiltzailea();
        boolean egiaztatu = logina.loginaEgin(erabiltzailea, pasahitza);
        if (egiaztatu) {
            txtErabiltzailea.clear();
            txtPasahitza.clear();
            jakinarazpena.erakutsi(
                    "Logina zuzen egin da.",
                    "Ongi etorri " + erabiltzailea + "!",
                    TrayIcon.MessageType.INFO
            );
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.close();
            MenuPantaila menua = new MenuPantaila();
            menua.irekiMenua();
        }else {
            txtErabiltzailea.clear();
            txtPasahitza.clear();
            txtErabiltzailea.requestFocus();
            jakinarazpena.erakutsi(
                    "Errorea gaizki joan da.",
                    "Erabiltzailea edo pasahitzak oker daude.",
                    TrayIcon.MessageType.ERROR
            );
        }
    }
}
