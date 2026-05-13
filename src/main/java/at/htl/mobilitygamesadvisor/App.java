package at.htl.mobilitygamesadvisor;

import at.htl.mobilitygamesadvisor.model.VideoSyncService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("login.fxml"));
        new VideoSyncService().sync();


        // Bildschirmgröße ermitteln und Fenster fast auf Vollbild setzen
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double width  = screenBounds.getWidth()  * 0.92;
        double height = screenBounds.getHeight() * 0.92;

        Scene scene = new Scene(fxmlLoader.load(), width, height);
        stage.setTitle("Mobility Games Advisor");
        stage.setScene(scene);
        // Fenster zentriert auf dem Bildschirm
        stage.setX(screenBounds.getMinX() + (screenBounds.getWidth()  - width)  / 2);
        stage.setY(screenBounds.getMinY() + (screenBounds.getHeight() - height) / 2);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
