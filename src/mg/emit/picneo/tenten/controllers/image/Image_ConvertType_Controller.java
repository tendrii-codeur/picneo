package mg.emit.picneo.tenten.controllers.image;

import ij.ImagePlus;
import ij.process.ImageConverter;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

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
	public Button button_exit;

	@FXML
	public Label label_title;

	@FXML
	public HBox title_bar;
	
	@FXML
	public ImageView imageView_preview;
	
	@FXML
	public Text textView_status;
	
	private boolean stage_closed_on_exit_status = true;
	
	private ImagePlus image = new ImagePlus();
	private ImagePlus image_preview_ip = new ImagePlus();
	private double dragOffsetX;
	private double dragOffsetY;

	@FXML
	public void initialize() {
		bindTitleBar();
	}
	
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
			updatePreviewAfterConversion();
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
			updatePreviewAfterConversion();
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
			updatePreviewAfterConversion();
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
		updatePreviewAfterConversion();
	}

	private void updatePreviewAfterConversion() {
		showImagePreview(image_preview_ip);
		button_adjust.setDisable(false);
	}

	private void showImagePreview(ImagePlus image_preview_ip) {
		Image image_preview_fx = SwingFXUtils.toFXImage(image_preview_ip.getProcessor().getBufferedImage(), null);
		imageView_preview.setImage(image_preview_fx);
		textView_status.setText(getImageTypeLabel(image_preview_ip));
	}

	private String getImageTypeLabel(ImagePlus ip) {
		switch (ip.getType()) {
			case ImagePlus.GRAY8:
			case ImagePlus.COLOR_256:
				return "8 bits";
			case ImagePlus.GRAY16:
				return "16 bits";
			case ImagePlus.GRAY32:
				return "32 bits";
			case ImagePlus.COLOR_RGB:
				return "RGB";
			default:
				return "";
		}
	}

	public void image_convertFailedMessage(String message) {
		
		Alert alert = new Alert(AlertType.ERROR, message, ButtonType.OK);
		alert.setTitle("Erreur de conversion de type d'image !");
		alert.showAndWait();
	}
	
	public void setImage(ImagePlus ip) {
		this.image = ip.duplicate();
		showImagePreview(this.image);
		button_adjust.setDisable(true);
	}

	@FXML
	public void button_exit_action_event(ActionEvent event) {
		button_cancel_action_event(event);
	}

	public void setWindowTitle(String title) {
		if (label_title != null) {
			label_title.setText(title);
		}
	}

	private void bindTitleBar() {
		button_exit.setOnMouseEntered(event -> button_exit.setStyle(
				"-fx-background-color: #F1707A; -fx-background-radius: 0; -fx-border-width: 0; -fx-cursor: hand;"));
		button_exit.setOnMouseExited(event -> button_exit.setStyle(
				"-fx-background-color: #E81123; -fx-background-radius: 0; -fx-border-width: 0; -fx-cursor: hand;"));

		title_bar.setOnMousePressed(event -> {
			dragOffsetX = event.getSceneX();
			dragOffsetY = event.getSceneY();
		});
		title_bar.setOnMouseDragged(event -> {
			Stage stage = (Stage) button_exit.getScene().getWindow();
			stage.setX(event.getScreenX() - dragOffsetX);
			stage.setY(event.getScreenY() - dragOffsetY);
		});
	}
	
	public Boolean getStageClosedOnExit() {
		return stage_closed_on_exit_status;
	}
	
	public ImagePlus getImagePlus() {
		return image_preview_ip;
	}
	
}
