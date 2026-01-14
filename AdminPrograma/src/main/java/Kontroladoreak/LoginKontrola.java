package Kontroladoreak;

import DatuBaseak.Erabiltzailea;
import Pantailak.MenuPantaila;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Login pantailako kontroladorea.
 * API1-erako deia egiten du login egiaztatzeko.
 */
public class LoginKontrola {

    @FXML
    private TextField txtErabiltzailea;    // fx:id="txtErabiltzailea"
    @FXML
    private PasswordField txtPasahitza;   // fx:id="txtPasahitza"
    @FXML
    private Label lblErrorea;             // fx:id="lblErrorea"
    @FXML
    private Button btnLogin;              // fx:id="btnLogin"

    private final Erabiltzailea loginService = new Erabiltzailea();

    @FXML
    public void initialize() {
        if (lblErrorea != null) {
            lblErrorea.setText("");
        }
    }

    // LOGIN botoia: onAction="#onLoginClick"
    @FXML
    private void onLoginClick(ActionEvent event) {
        String erabiltzailea = txtErabiltzailea.getText().trim();
        String pasahitza = txtPasahitza.getText().trim();

        if (erabiltzailea.isEmpty() || pasahitza.isEmpty()) {
            erakutsiErrorea("Sartu erabiltzailea eta pasahitza.");
            return;
        }

        boolean ondo = loginService.loginaEgin(erabiltzailea, pasahitza);

        if (ondo) {
            irekiMenuNagusia();
        } else {
            erakutsiErrorea("Erabiltzailea edo pasahitza okerra da.");
            txtPasahitza.clear();
            txtErabiltzailea.requestFocus();
        }
    }

    // Goiko "_" botoia: onAction="#lehioaJaitsi"
    @FXML
    private void lehioaJaitsi(ActionEvent event) {
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource())
                .getScene().getWindow();
        stage.setIconified(true);
    }

    // "X" botoia: onAction="#itxi"
    @FXML
    private void itxi(ActionEvent event) {
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource())
                .getScene().getWindow();
        stage.close();
    }

    private void erakutsiErrorea(String mezua) {
        if (lblErrorea != null) {
            lblErrorea.setText(mezua);
        } else {
            System.err.println(mezua);
        }
    }

    /**
     * Hurrengo pantaila ireki (menu nagusia).
     * Hemen erabiltzen dugu zure MenuPantaila klasea.
     */
    private void irekiMenuNagusia() {
        // Menu nagusia ireki
        MenuPantaila menuPantaila = new MenuPantaila();
        menuPantaila.irekiMenua();

        // eta login leihoa itxi
        Stage unekoStage = (Stage) btnLogin.getScene().getWindow();
        unekoStage.close();
    }
}
