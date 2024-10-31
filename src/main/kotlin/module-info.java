module com.example.daifugo {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;

    requires org.kordamp.bootstrapfx.core;

    opens com.example.daifugo to javafx.fxml;
    exports com.example.daifugo;
}