package mg.emit.picneo.tenten.controllers.image;

import ij.ImagePlus;
import ij.gui.HistogramWindow;
import ij.process.ImageProcessor;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class Image_BrightnessContrast_Adjust_Controller {

	@FXML 
	public Button button_adjust;
	
	@FXML
	public Button button_reset;

	@FXML 
	public Button button_cancel;
	
	@FXML
	public Slider slider_min;
	
	@FXML
	public Slider slider_max;
	
	@FXML
	public Label label_min;
	
	@FXML
	public Label label_max;
	
	@FXML
	public ImageView imageView_preview;
	
	@FXML
	public ImageView imageView_histogram_preview;
	
	private boolean stage_closed_on_exit_status = true;
	
	private double min_current_value;
	private double max_current_value;
	
	private double min_default_value;
	private double max_default_value;
	
	private ImagePlus image = new ImagePlus();
	private ImagePlus image_preview_ip = new ImagePlus();
	
	@FXML
	public void initialize(){

		slider_min.valueProperty().addListener((observable, oldValue, newValue) -> {
			min_current_value = slider_min.getValue();
			label_min.setText(Integer.toString((int) slider_min.getValue()));
			
			if(max_current_value <= min_current_value) {
				slider_max.setValue(min_current_value);
			}
		});
        
		slider_max.valueProperty().addListener((observable, oldValue, newValue) -> {
			max_current_value = slider_max.getValue();
			label_max.setText(Integer.toString((int) slider_max.getValue()));
			
			if(max_current_value <= min_current_value) {
				slider_min.setValue(max_current_value);
			}
		});
    }
	
	@FXML
	public void button_adjust_action_event(ActionEvent event) {
		
		stage_closed_on_exit_status = false;
		
	    Stage stage = (Stage) button_adjust.getScene().getWindow();
	    stage.close();
	}
	
	@FXML
	public void button_reset_action_event(ActionEvent event) {
	    
		slider_min.setValue(min_default_value);
	    slider_max.setValue(max_default_value);
	}
	
	@FXML
	public void button_cancel_action_event(ActionEvent event) {
		
		stage_closed_on_exit_status = true;
		
		Stage stage = (Stage) button_adjust.getScene().getWindow();
	    stage.close();
	}
	
	@FXML
	public void imageView_preview_drag_over() throws NullPointerException {
		
		image_preview_ip = new ImagePlus();
		image_preview_ip = image.duplicate();
		
		image_preview_ip.getProcessor().setMinAndMax(min_current_value, max_current_value);
		
		Image image_preview_fx = SwingFXUtils.toFXImage(image_preview_ip.getBufferedImage(), null);
		imageView_preview.setImage(image_preview_fx);
		
		HistogramWindow histogram = new HistogramWindow(image_preview_ip);
		Image image_histogram_preview_fx = SwingFXUtils.toFXImage(histogram.getImagePlus().getBufferedImage(), null);
		imageView_histogram_preview.setImage(image_histogram_preview_fx);
	}
	
	public void setImage(Double min_value, Double max_value, ImagePlus ip) {
		
		image = new ImagePlus();
		
		this.slider_min.setValue(min_value);
		this.slider_max.setValue(max_value);
		this.min_default_value = min_value;
		this.max_default_value = max_value;
		this.image = ip.duplicate();
		
	}
	
	public boolean getStageClosedOnExit() {
		return stage_closed_on_exit_status;
	}
	
	public ImageProcessor getImageProcessor() {
		return image_preview_ip.getProcessor();
	}
	
}
