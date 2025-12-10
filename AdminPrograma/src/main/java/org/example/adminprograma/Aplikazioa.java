package org.example.adminprograma;

import Pantailak.LoginPantaila;
import Pantailak.MenuPantaila;
import javafx.application.Application;
import javafx.stage.Stage;

public class Aplikazioa extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
//        LoginPantaila login = new  LoginPantaila();
//        login.irekiLogina();
        MenuPantaila menu = new MenuPantaila();
        menu.irekiMenua();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
