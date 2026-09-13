package mg.emit.picneo.tenten.controllers.file;

import ij.ImagePlus;
import ij.io.FileSaver;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class File_SaveAs_Controller {

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
	
	public void file_saveImage() {
		
		FileSaver filesaver = new FileSaver(image_preview_ip);
	
		if(filesaver.save() == true) {
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
				
		if(filesaver.saveAsFits() == true) {
			file_saveSuccessfullMessage(image_preview_ip.getTitle(), button_save_fits);
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
			
	public void file_saveImageAsZip() {
		
		FileSaver filesaver = new FileSaver(image_preview_ip);
				
		if(filesaver.saveAsZip() == true) {
			file_saveSuccessfullMessage(image_preview_ip.getTitle(), button_save_zip);
		}
	}
		
	public void file_saveSuccessfullMessage(String imageTitle, Button button) {
		Alert alert = new Alert(AlertType.INFORMATION, imageTitle + " " + "was successfully saved!", ButtonType.OK);
		alert.setTitle("Save successfull!");
		alert.showAndWait();
		
		Stage stage = (Stage) button.getScene().getWindow();
	    stage.close();
	}
			
	public void file_saveAsFitsFailedMessage() {
		Alert alert = new Alert(AlertType.ERROR, "Save was cancelled or The current image is not Greyscale image!", ButtonType.OK);
		alert.setTitle("Save error!");
		alert.showAndWait();
	}
		
	public void file_saveAsLutFailedMessage() {
		Alert alert = new Alert(AlertType.ERROR, "Save was cancelled or RGB Image is not allowed!", ButtonType.OK);
		alert.setTitle("Save error!");
		alert.showAndWait();
	}
	
	public void setImage(ImagePlus ip) {
		
		image_preview_ip = new ImagePlus();
		
		this.image_preview_ip = ip;
	}
	
}
