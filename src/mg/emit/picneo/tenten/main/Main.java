package mg.emit.picneo.tenten.main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


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
			
			primaryStage.setTitle("Image Processor with ImageJ Framework - WIP");
			primaryStage.setResizable(false);
			primaryStage.setScene(scene);
			primaryStage.show();
			
			primaryStage.setOnCloseRequest(e -> {
			
				Alert alert = new Alert(AlertType.WARNING, "Do you want to close program? Any unsaved changes will be lost!", 
								ButtonType.NO, ButtonType.YES);
				alert.setTitle("Warning!");
				alert.showAndWait().ifPresent(type -> {
					
					if(type == ButtonType.YES) {
						Platform.exit();	
					}
				});
			});
			
		}
		
		catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	

}
