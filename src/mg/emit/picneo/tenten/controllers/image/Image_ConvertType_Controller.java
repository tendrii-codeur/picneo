package mg.emit.picneo.tenten.controllers.image;

import ij.ImagePlus;
import ij.process.ImageConverter;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.control.Button;

public class Image_ConvertType_Controller {

	@FXML
	public Button button_adjust;
	
	@FXML
	public Button button_cancel;
	
	@FXML
	public Button button_gray_8_bit;
	
	@FXML
	public Button button_gray_16_bit;
	
	@FXML
	public Button button_gray_32_bit;
	
	@FXML
	public Button button_rgb;
	
	@FXML
	public ImageView imageView_preview;
	
	@FXML
	public Text textView_status;
	
	private boolean stage_closed_on_exit_status = true;
	
	private ImagePlus image = new ImagePlus();
	private ImagePlus image_preview_ip = new ImagePlus();
	
	@FXML
	public void button_adjust_action_event(ActionEvent event) {
		
		stage_closed_on_exit_status = false;
		
	    Stage stage = (Stage) button_adjust.getScene().getWindow();
	    stage.close();
	}
	
	@FXML
	public void button_cancel_action_event(ActionEvent event) {
		
		Stage stage = (Stage) button_adjust.getScene().getWindow();
	    stage.close();
	}
	
	@FXML
	public void image_convertToGray8Bit() {
		
		image_preview_ip = new ImagePlus();
		image_preview_ip = image.duplicate();
		
		try {
			ImageConverter imageConverter = new ImageConverter(image_preview_ip);
			imageConverter.convertToGray8();
			set_imageViewPreview_textViewStatus(image_preview_ip);
			textView_status.setText("Gray 8-bit");
		}
		catch(IllegalArgumentException e) {
			image_convertFailedMessage(e.getMessage());
		}
	}
		
	@FXML
	public void image_convertToGray16Bit() {
		
		image_preview_ip = new ImagePlus();
		image_preview_ip = image.duplicate();
		
		try {
			ImageConverter imageConverter = new ImageConverter(image_preview_ip);
			imageConverter.convertToGray16();
			set_imageViewPreview_textViewStatus(image_preview_ip);
			textView_status.setText("Gray 16-bit");
		}
		catch(IllegalArgumentException e) {
			image_convertFailedMessage(e.getMessage());
		}
	}
		
	@FXML
	public void image_convertToGray32Bit() {
		
		image_preview_ip = new ImagePlus();
		image_preview_ip = image.duplicate();
		
		try {
			ImageConverter imageConverter = new ImageConverter(image_preview_ip);
			imageConverter.convertToGray32();
			set_imageViewPreview_textViewStatus(image_preview_ip);
			textView_status.setText("Gray 32-bit");
		}
		catch(IllegalArgumentException e) {
			image_convertFailedMessage(e.getMessage());
		}
	}
		
	@FXML
	public void image_convertToRGB() {
		
		image_preview_ip = new ImagePlus();
		image_preview_ip = image.duplicate();
		
		ImageConverter imageConverter = new ImageConverter(image_preview_ip);
		imageConverter.convertToRGB();
		set_imageViewPreview_textViewStatus(image_preview_ip);
		textView_status.setText("RGB");
	}
		
	public void set_imageViewPreview_textViewStatus(ImagePlus image_preview_ip) {
		
		Image image_preview_fx = SwingFXUtils.toFXImage(image_preview_ip.getProcessor().getBufferedImage(), null);
		imageView_preview.setImage(image_preview_fx);
	}

	public void image_convertFailedMessage(String message) {
		
		Alert alert = new Alert(AlertType.ERROR, message, ButtonType.OK);
		alert.setTitle("Erreur de conversion de type d'image !");
		alert.showAndWait();
	}
	
	public void setImage(ImagePlus ip) {
		
		image = new ImagePlus();
		
		this.image = ip.duplicate();
	}
	
	public Boolean getStageClosedOnExit() {
		return stage_closed_on_exit_status;
	}
	
	public ImagePlus getImagePlus() {
		return image_preview_ip;
	}
	
}
