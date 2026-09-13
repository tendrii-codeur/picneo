package mg.emit.picneo.tenten.controllers.image;

import mg.emit.picneo.tenten.enums.Threshold_Lut_Types;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class Image_Threshold_Adjust_Controller {

	@FXML
	public Button button_adjust;
	
	@FXML
	public Button button_reset;
	
	@FXML
	public Button button_cancel;
	
	@FXML
	public ComboBox<Threshold_Lut_Types> comboBox_lut;
	
	@FXML
	public ImageView imageView_preview;
	
	@FXML
	public Label label_minimum_threshold;
	
	@FXML
	public Label label_maximum_threshold;
	
	@FXML
	public Slider slider_min;
	
	@FXML
	public Slider slider_max;
	
	private Integer lut;
	
	private double min_current_value;
	private double max_current_value;
	
	private double default_value = new Double(0);
	
	private boolean stage_closed_on_exit_status = true;
	
	private ImagePlus image = new ImagePlus();
	private ImagePlus image_preview_ip = new ImagePlus();
	
	@FXML
	public void initialize(){

		slider_min.valueProperty().addListener((observable, oldValue, newValue) -> {
			min_current_value = slider_min.getValue();
			label_minimum_threshold.setText(Integer.toString((int) slider_min.getValue()));
			
			if(max_current_value <= min_current_value) {
				slider_max.setValue(min_current_value);
			}
		});
        
		slider_max.valueProperty().addListener((observable, oldValue, newValue) -> {
			max_current_value = slider_max.getValue();
			label_maximum_threshold.setText(Integer.toString((int) slider_max.getValue()));
			
			if(max_current_value <= min_current_value) {
				slider_min.setValue(max_current_value);
			}
		});
		
		comboBox_lut.getItems().addAll(Threshold_Lut_Types.values());
		comboBox_lut.getSelectionModel().select(Threshold_Lut_Types.RED_LUT);
	    comboBox_lut.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
	    	lut = newValue.getDisplayLutValue();
	    });
    }
	
	@FXML
	public void button_adjust_action_event(ActionEvent event) {
		
		stage_closed_on_exit_status = false;
		
		IJ.run(image_preview_ip, "Convert to Mask",""); 
		
	    Stage stage = (Stage) button_adjust.getScene().getWindow();
	    stage.close();
	}
	
	@FXML
	public void button_reset_action_event(ActionEvent event) {
	    
		slider_min.setValue(default_value);
	    slider_max.setValue(default_value);
	    
	    min_current_value = slider_min.getValue();
	    max_current_value = slider_max.getValue();
	}
	
	@FXML
	public void button_cancel_action_event(ActionEvent event) {
		
		stage_closed_on_exit_status = true;
		
		Stage stage = (Stage) button_adjust.getScene().getWindow();
	    stage.close();
	}
	
	@FXML
	public void imageView_threshold_preview() {
		
		image_preview_ip = new ImagePlus();
		image_preview_ip = image.duplicate();
		
		lut = comboBox_lut.getValue().getDisplayLutValue();
		
		image_preview_ip.getProcessor().setThreshold(min_current_value, max_current_value, lut);
	
		Image image_preview_fx = SwingFXUtils.toFXImage(image_preview_ip.getBufferedImage(), null);
		imageView_preview.setImage(image_preview_fx);
	}
	
	public void setImage(ImagePlus ip) {
		
		image = new ImagePlus();
		image = ip.duplicate();
		
	}
	
	public boolean getStageClosedOnExit() {
		return stage_closed_on_exit_status;
	}
	
	public ImageProcessor getImageProcessor() {
		return image_preview_ip.getProcessor();
	}
}
