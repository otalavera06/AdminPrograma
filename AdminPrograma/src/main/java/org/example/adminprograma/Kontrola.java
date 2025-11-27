package org.example.adminprograma;

import DatuBaseak.Login;
import DatuBaseak.MySql;
import Pantailak.MenuPantaila;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.w3c.dom.Text;

import java.awt.*;
import java.net.URL;
import java.sql.Connection;
import java.util.ResourceBundle;

public class Kontrola implements Initializable {

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
        Login logina = new Login();
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
            jakinarazpena.erakutsi(
                    "Errorea gaizki joan da.",
                    "Erabiltzailea edo pasahitzak oker daude.",
                    TrayIcon.MessageType.ERROR
            );
        }
    }
}
