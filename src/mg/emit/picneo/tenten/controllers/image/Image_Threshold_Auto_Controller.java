package mg.emit.picneo.tenten.controllers.image;

import mg.emit.picneo.tenten.enums.Threshold_Background_Types;
import mg.emit.picneo.tenten.enums.Threshold_Lut_Types;
import mg.emit.picneo.tenten.enums.Threshold_Method_Types;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class Image_Threshold_Auto_Controller {

	@FXML
	public Button button_adjust;
	
	@FXML
	public Button button_cancel;
	
	@FXML
	public ComboBox<Threshold_Method_Types> comboBox_method;
	
	@FXML
	public ComboBox<Threshold_Background_Types> comboBox_background;
	
	@FXML
	public ComboBox<Threshold_Lut_Types> comboBox_lut;
	
	@FXML
	public ImageView imageView_preview;
	
	@FXML
	public Label label_minimum_threshold;
	
	@FXML
	public Label label_maximum_threshold;
	
	private String method;
	private Boolean background;
	private Integer lut;
	
	private boolean stage_closed_on_exit_status = true;
	
	private ImagePlus image = new ImagePlus();
	private ImagePlus image_preview_ip = new ImagePlus();
	
	@FXML
	public void initialize(){
		
	    comboBox_method.getItems().addAll(Threshold_Method_Types.values());
	    comboBox_method.getSelectionModel().select(Threshold_Method_Types.DEFAULT);
	    
	    comboBox_background.getItems().addAll(Threshold_Background_Types.values());
	    comboBox_background.getSelectionModel().select(Threshold_Background_Types.TRUE);
	    
	    comboBox_lut.getItems().addAll(Threshold_Lut_Types.values());
	    comboBox_lut.getSelectionModel().select(Threshold_Lut_Types.RED_LUT);
	}
	
	@FXML
	public void button_adjust_action_event(ActionEvent event) {
		
		stage_closed_on_exit_status = false;
		
		IJ.run(image_preview_ip, "Convert to Mask",""); 
		
	    Stage stage = (Stage) button_adjust.getScene().getWindow();
	    stage.close();
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
		
		method = comboBox_method.getValue().getDisplayMethodName();
		background = comboBox_background.getValue().getDisplayBackgroundName();
		lut = comboBox_lut.getValue().getDisplayLutValue();
		
		image_preview_ip.getProcessor().setAutoThreshold(method, background, lut);
		
		int min = (int) image_preview_ip.getProcessor().getMinThreshold();
		int max = (int)	image_preview_ip.getProcessor().getMaxThreshold();
		
		label_minimum_threshold.setText(String.valueOf(min));
		label_minimum_threshold.setText(String.valueOf(max));
		
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
