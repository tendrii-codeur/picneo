package mg.emit.picneo.tenten.controllers.process;

import ij.ImagePlus;
import ij.plugin.filter.UnsharpMask;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class Process_Filter_Unsharp_Mask_Controller {

	@FXML
	public Button button_adjust;
	
	@FXML
	public Button button_cancel;
	
	@FXML
	public ImageView imageView_preview;
	
	@FXML
	public TextField tf_sigma;
	
	@FXML
	public TextField tf_weight;
	
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
		tf_weight.textProperty().addListener((observable, oldValue, newValue) -> {
		    if (newValue.matches("\\d*(\\.\\d*)?") && newValue.chars().filter(ch -> ch == '.').count() <= 1)
		    	return;
		    tf_weight.setText("0.1");
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
		tf_weight.setText("0.1");
		
		ImageProcessor ip = image_preview_ip.getProcessor();
		FloatProcessor fp = null; 
		 
	    UnsharpMask um = new UnsharpMask(); 
		
	    for (int i = 0; i < ip.getNChannels(); i++) { 
		    
	    	fp = ip.toFloat(i, fp); 
		    fp.snapshot(); 
		    um.sharpenFloat(fp, 1.0, (float) 0.1); 
		    ip.setPixels(i, fp); 
		} 
		 
		image_preview_ip.setProcessor(ip);

		Image image_preview_fx = SwingFXUtils.toFXImage(image_preview_ip.getBufferedImage(), null);
		imageView_preview.setImage(image_preview_fx);
	}
	
	@FXML
	public void imageView_unsharp_mask_preview() {
		
		image_preview_ip = new ImagePlus();
		image_preview_ip = image.duplicate();
		
		if(tf_sigma.getText().isEmpty()) {
			tf_sigma.setText("1.0");
		}
		if(tf_weight.getText().isEmpty()) {
			tf_weight.setText("0.1");
		}
		
		Double sigma = Double.valueOf(tf_sigma.getText());
		Double weight = Double.valueOf(tf_weight.getText());
		
		if(weight > 0.9) {
			tf_weight.setText("0.9");
			weight = 0.9;
		}
		else if(weight < 0.1) {
			tf_weight.setText("0.1");
			weight = 0.1;
		}
		
		ImageProcessor ip = image_preview_ip.getProcessor();
		FloatProcessor fp = null; 
		 
	    UnsharpMask um = new UnsharpMask(); 
		
	    for (int i = 0; i < ip.getNChannels(); i++) { 
		    
	    	fp = ip.toFloat(i, fp); 
		    fp.snapshot(); 
		    um.sharpenFloat(fp, sigma, weight.floatValue()); 
		    ip.setPixels(i, fp); 
		} 
		 
		image_preview_ip.setProcessor(ip);

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
