import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main extends Application {

//    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("fxml/Client.fxml"));
        primaryStage.setTitle("PasDataClient");
        primaryStage.setScene(new Scene(root, 750, 500));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
