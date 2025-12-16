package Pantailak;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class MenuPantaila {
    public void irekiMenua(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/adminprograma/Pantailak/menuPantaila.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Menua");
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));
            stage.setResizable(true);
            stage.setMaximized(true);
            stage.show();
        }catch(NoClassDefFoundError | IOException throwable){
            throwable.printStackTrace();
        }
    }
}
