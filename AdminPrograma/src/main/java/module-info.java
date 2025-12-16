module org.example.adminprograma {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires javafx.swing;
    requires javafx.graphics;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;


    opens org.example.adminprograma to javafx.fxml;
    exports org.example.adminprograma;
    exports Pantailak;
    opens Pantailak to javafx.fxml;
    opens Kontroladoreak to javafx.fxml;
    opens Modeloak to com.fasterxml.jackson.databind;
}