import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    
    public static void main(String[] args) throws Exception {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/main.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Simple Gravity Simulator");
        primaryStage.setFullScreenExitHint("");
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case F: primaryStage.setFullScreen(!primaryStage.isFullScreen());
                default: {}
            }
        });
        primaryStage.show();
    }
}
