package mg.emit.picneo.tenten.controllers.process;

import ij.ImagePlus;
import ij.plugin.filter.GaussianBlur;
import ij.process.ImageProcessor;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class Process_Filter_Gaussian_Blur_Controller {

	@FXML
	public Button button_adjust;
	
	@FXML
	public Button button_cancel;
	
	@FXML
	public ImageView imageView_preview;
	
	@FXML
	public TextField tf_sigma;
	
	public boolean stage_closed_on_exit_status = true;
	
	public ImagePlus image = new ImagePlus();
	public ImagePlus image_preview_ip = new ImagePlus();
	
	@FXML
	public void initialize() {
		tf_sigma.textProperty().addListener((observable, oldValue, newValue) -> {
		    if (newValue.matches("\\d*(\\.\\d*)?") && newValue.chars().filter(ch -> ch == '.').count() <= 1)
		    	return;
		    tf_sigma.setText("1.0");
		});
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
	public void button_reset_action_event(ActionEvent event) {
		
		image_preview_ip = new ImagePlus();
		image_preview_ip = image.duplicate();
		
		tf_sigma.setText("1.0");
		
		GaussianBlur gaussianBlur = new GaussianBlur();
		gaussianBlur.blurGaussian(image_preview_ip.getProcessor(), 1.0);

		Image image_preview_fx = SwingFXUtils.toFXImage(image_preview_ip.getBufferedImage(), null);
		imageView_preview.setImage(image_preview_fx);
	}
	
	@FXML
	public void imageView_gaussian_blur_preview() {
		
		image_preview_ip = new ImagePlus();
		image_preview_ip = image.duplicate();
		
		if(tf_sigma.getText().isEmpty()) {
			tf_sigma.setText("1.0");
		}
		
		Double sigma = Double.valueOf(tf_sigma.getText());
		
		GaussianBlur gaussianBlur = new GaussianBlur();
		gaussianBlur.blurGaussian(image_preview_ip.getProcessor(), sigma);

		Image image_preview_fx = SwingFXUtils.toFXImage(image_preview_ip.getBufferedImage(), null);
		imageView_preview.setImage(image_preview_fx);
	}
	
	public void setImage(ImagePlus ip) {
		
		image = new ImagePlus();
		
		this.image = ip.duplicate();
	}
	
	public Boolean getStageClosedOnExit() {
		return stage_closed_on_exit_status;
	}
	
	public ImageProcessor getImageProcessor() {
		return image_preview_ip.getProcessor();
	}
}
