package it.polito.tdp.emergency;
	
import it.polito.tdp.emergency.model.Simulatore;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("Emergency.fxml")) ;
			BorderPane root = (BorderPane)loader.load();

			EmergencyController controller = loader.getController() ;
			Simulatore sim = new Simulatore() ;
			controller.setSim(sim) ;

			Scene scene = new Scene(root);
//			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
