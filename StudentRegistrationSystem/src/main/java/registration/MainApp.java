package registration;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/registration/main-view.fxml"));
        Scene scene = new Scene(loader.load(), 800, 600);

        // Add the stylesheet
        scene.getStylesheets().add(getClass().getResource("/registration/style.css").toExternalForm());

        primaryStage.setTitle("Student Registration System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
