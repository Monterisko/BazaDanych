package org.example.bazadanych;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainController.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 500, 300);
        stage.setTitle("Baza Danych MySQL");
        stage.setScene(scene);
        stage.show();
        MainController mainController = fxmlLoader.getController();
        mainController.readTables();
    }

    public static void main(String[] args) {
        launch();
    }
}