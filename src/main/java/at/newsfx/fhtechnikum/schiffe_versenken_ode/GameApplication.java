package at.newsfx.fhtechnikum.schiffe_versenken_ode;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GameApplication extends Application {

    @Override
    public void start(Stage stage) {
        GameController controller = new GameController();
        Scene scene = new Scene(controller.buildUI(), 950, 700);
        stage.setTitle("Schiffe Versenken");
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> controller.shutdown());
        stage.show();
    }
}
