module com.example.richtexteditor {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires org.jsoup;
    requires jdk.jsobject;

    opens com.example.richtexteditor to javafx.fxml;
    exports com.example.richtexteditor;
}