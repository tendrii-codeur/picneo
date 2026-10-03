package mg.emit.picneo.tenten.controllers.file;

import mg.emit.picneo.tenten.controllers.Tool_Dialog_Controller;
import mg.emit.picneo.tenten.util.TitleBar_Util;

import ij.ImagePlus;
import ij.io.FileSaver;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import mg.emit.picneo.tenten.util.Alert_Util;

public class File_SaveAs_Controller implements Tool_Dialog_Controller {

	@FXML
	public Button button_exit;

	@FXML
	public Label label_title;

	@FXML
	public HBox title_bar;

	@FXML
	public Button button_save;
	
	@FXML 
	public Button button_save_bmp;
	
	@FXML 
	public Button button_save_fits;
	
	@FXML 
	public Button button_save_gif;
	
	@FXML 
	public Button button_save_jpeg;
	
	@FXML 
	public Button button_save_lut;
	
	@FXML 
	public Button button_save_pgm;
	
	@FXML 
	public Button button_save_png;
	
	@FXML 
	public Button button_save_raw;
	
	@FXML 
	public Button button_save_text;
	
	@FXML 
	public Button button_save_tiff;
	
	@FXML 
	public Button button_save_zip;
	
	private ImagePlus image_preview_ip = new ImagePlus();


	@FXML
	public void initialize() {
		TitleBar_Util.bind(title_bar, button_exit);
	}

	@FXML
	public void button_exit_action_event(ActionEvent event) {
		Stage stage = (Stage) button_exit.getScene().getWindow();
		stage.close();
	}

	public void setWindowTitle(String title) {
		if (label_title != null) {
			label_title.setText(title);
		}
	}

	
	@FXML
	public void file_saveImage() {

		FileSaver filesaver = new FileSaver(image_preview_ip);

		if(filesaver.save()) {
			file_saveSuccessfullMessage(image_preview_ip.getTitle(), button_save);
		}
	}
	
	@FXML
	public void file_saveImageAsBmp() {
		
		FileSaver filesaver = new FileSaver(image_preview_ip);
				
		if(filesaver.saveAsBmp() == true) {
			file_saveSuccessfullMessage(image_preview_ip.getTitle(), button_save_bmp);
		}
	}
			
	@FXML
	public void file_saveImageAsFits() {
		
		FileSaver filesaver = new FileSaver(image_preview_ip);
				
		if(filesaver.saveAsFits()) {
			file_saveSuccessfullMessage(image_preview_ip.getTitle(), button_save_fits);
		}
		else {
			file_saveAsFitsFailedMessage();
		}
	}
			
	@FXML
	public void file_saveImageAsGif() {
			
		FileSaver filesaver = new FileSaver(image_preview_ip);
				
		if(filesaver.saveAsGif() == true) {
			file_saveSuccessfullMessage(image_preview_ip.getTitle(), button_save_gif);
		}
	}
			
	@FXML
	public void file_saveImageAsJpeg() {
		
		FileSaver filesaver = new FileSaver(image_preview_ip);
				
		if(filesaver.saveAsJpeg() == true) {
			file_saveSuccessfullMessage(image_preview_ip.getTitle(), button_save_jpeg);
		}
	}
			
	@FXML
	public void file_saveImageAsLut() {
		FileSaver filesaver = new FileSaver(image_preview_ip);
				
		if(filesaver.saveAsLut() == true) {
			file_saveSuccessfullMessage(image_preview_ip.getTitle(), button_save_lut);
		}
		else {
			file_saveAsLutFailedMessage();
		}
	}
			
	@FXML
	public void file_saveImageAsPgm() {
		FileSaver filesaver = new FileSaver(image_preview_ip);
				
		if(filesaver.saveAsPgm() == true) {
			file_saveSuccessfullMessage(image_preview_ip.getTitle(), button_save_pgm);
		}
	}
			
	@FXML
	public void file_saveImageAsPng() {
		
		FileSaver filesaver = new FileSaver(image_preview_ip);
				
		if(filesaver.saveAsPng() == true) {
			file_saveSuccessfullMessage(image_preview_ip.getTitle(), button_save_png);
		}
	}
			
	@FXML
	public void file_saveImageAsRaw() {
		
		FileSaver filesaver = new FileSaver(image_preview_ip);
				
		if(filesaver.saveAsRaw() == true) {
			file_saveSuccessfullMessage(image_preview_ip.getTitle(), button_save_raw);
		}
	}
		
	@FXML
	public void file_saveImageAsText() {
		
		FileSaver filesaver = new FileSaver(image_preview_ip);
				
		if(filesaver.saveAsText() == true) {
			file_saveSuccessfullMessage(image_preview_ip.getTitle(), button_save_text);
		}
	}
			
	@FXML
	public void file_saveImageAsTiff() {
		
		FileSaver filesaver = new FileSaver(image_preview_ip);
				
		if(filesaver.saveAsTiff() == true) {
			file_saveSuccessfullMessage(image_preview_ip.getTitle(), button_save_tiff);
		}
	}
			
	@FXML
	public void file_saveImageAsZip() {

		FileSaver filesaver = new FileSaver(image_preview_ip);

		if(filesaver.saveAsZip()) {
			file_saveSuccessfullMessage(image_preview_ip.getTitle(), button_save_zip);
		}
	}
		
	public void file_saveSuccessfullMessage(String imageTitle, Button button) {
		Alert alert = new Alert(AlertType.INFORMATION, imageTitle + " a été enregistrée avec succès !", ButtonType.OK);
		alert.setTitle("Enregistrement réussi !");
		Alert_Util.style(alert);
		alert.showAndWait();
		
		Stage stage = (Stage) button.getScene().getWindow();
	    stage.close();
	}
			
	public void file_saveAsFitsFailedMessage() {
		Alert alert = new Alert(AlertType.ERROR, "L'enregistrement a été annulé ou l'image actuelle n'est pas en niveaux de gris !", ButtonType.OK);
		alert.setTitle("Erreur d'enregistrement !");
		Alert_Util.style(alert);
		alert.showAndWait();
	}
		
	public void file_saveAsLutFailedMessage() {
		Alert alert = new Alert(AlertType.ERROR, "L'enregistrement a été annulé ou les images RGB ne sont pas autorisées !", ButtonType.OK);
		alert.setTitle("Erreur d'enregistrement !");
		Alert_Util.style(alert);
		alert.showAndWait();
	}
	
	public void setImage(ImagePlus ip) {

		this.image_preview_ip = ip;
	}
	
}
