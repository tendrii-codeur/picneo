package mg.emit.picneo.tenten.main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import mg.emit.picneo.tenten.util.Alert_Util;


public class Main extends Application {

	private static BorderPane root;
	private Stage primaryStage;
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) {
		
		primaryStage = stage;
		
		try {
			root = (BorderPane)FXMLLoader.load(getClass().getResource("/mg/emit/picneo/tenten/fxmls/Main_Layout.fxml"));
			
			Scene scene = new Scene(root);

			primaryStage.setTitle("PICNEO");
			primaryStage.setResizable(true);
			primaryStage.setScene(scene);

			// Icône de l'application (logo PICNEO) : barre de titre + barre des tâches.
			java.io.InputStream iconStream = getClass().getResourceAsStream("/icons/picneo.png");
			if (iconStream != null) {
				primaryStage.getIcons().add(new Image(iconStream));
			}

			primaryStage.show();
			
			primaryStage.setOnCloseRequest(e -> {
			
				Alert alert = new Alert(AlertType.WARNING, "Voulez-vous vraiment quitter l'application ?",
								ButtonType.NO, ButtonType.YES);
				Alert_Util.style(alert);
				ButtonType choice = alert.showAndWait().orElse(ButtonType.NO);
				if (choice == ButtonType.YES) {
					Platform.exit();
				}
				else {
					// "Non" (ou croix) : on annule vraiment la fermeture.
					e.consume();
				}
			});
			
		}
		
		catch(Exception e) {
			e.printStackTrace();
			// Évite de laisser une JVM zombie qui verrouille dist/ si le FXML échoue au chargement.
			Platform.exit();
			System.exit(1);
		}
		
	}
	

}
