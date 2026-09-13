package mg.emit.picneo.tenten.controllers.process;

import ij.ImagePlus;
import ij.plugin.filter.Shadows;
import ij.process.ImageProcessor;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Process_Shadow_Controller {

	@FXML
	public Button button_adjust;
	
	@FXML
	public Button button_cancel;
	
	@FXML
	public Button button_reset;
	
	@FXML
	public Button button_north_west;
	
	@FXML
	public Button button_north;
	
	@FXML
	public Button button_north_east;
	
	@FXML
	public Button button_east;
	
	@FXML
	public Button button_south_east;

	@FXML
	public Button button_south;
	
	@FXML
	public Button button_south_west;
	
	@FXML
	public Text textView_position;
	
	@FXML
	public ImageView imageView_preview;
	
	public boolean stage_closed_on_exit_status = true;
	
	public ImagePlus image = new ImagePlus();
	public ImagePlus image_preview_ip = new ImagePlus();
	public ImagePlus image_return = new ImagePlus();
	
	@FXML
	public void button_adjust_action_event(ActionEvent event) {
		
		image_return = new ImagePlus();
		image_return = image_preview_ip.duplicate();
		
		stage_closed_on_exit_status = false;
		
	    Stage stage = (Stage) button_adjust.getScene().getWindow();
	    stage.close();
	}
	
	@FXML
	public void button_cancel_action_event(ActionEvent event) {
		
		image_return = new ImagePlus();
		image_return = image.duplicate();
		
		stage_closed_on_exit_status = false;
		
	    Stage stage = (Stage) button_adjust.getScene().getWindow();
	    stage.close();
	}
	
	@FXML
	public void button_reset_action_event(ActionEvent event) {
		
		image_preview_ip = new ImagePlus();
		image_preview_ip = image.duplicate();
		
		set_imageViewPreview_textViewPosition(image_preview_ip, "NEUTRAL");
	}
	
	@FXML
	public void imageView_preview_onClick() {
		
		image_preview_ip = new ImagePlus();
		image_preview_ip = image.duplicate();
		
		set_imageViewPreview_textViewPosition(image_preview_ip, "NEUTRAL");
	}
	
	@FXML
	public void button_north_east_onClick() {
		
		image_preview_ip = new ImagePlus();
		image_preview_ip = image.duplicate();
		
		Shadows shadows = new Shadows();
		shadows.northeast(image_preview_ip.getProcessor());
		
		set_imageViewPreview_textViewPosition(image_preview_ip, "NORTH-EAST");
	}
	
	@FXML
	public void button_north_onClick() {
		
		image_preview_ip = new ImagePlus();
		image_preview_ip = image.duplicate();
		
		Shadows shadows = new Shadows();
		shadows.north(image_preview_ip.getProcessor());
		
		set_imageViewPreview_textViewPosition(image_preview_ip, "NORTH");
	}
	
	@FXML
	public void button_north_west_onClick() {
		
		image_preview_ip = new ImagePlus();
		image_preview_ip = image.duplicate();
		
		Shadows shadows = new Shadows();
		shadows.northwest(image_preview_ip.getProcessor());
		
		set_imageViewPreview_textViewPosition(image_preview_ip, "NORTH-WEST");
	}
	
	@FXML
	public void button_west_onClick() {
		
		image_preview_ip = new ImagePlus();
		image_preview_ip = image.duplicate();
		
		Shadows shadows = new Shadows();
		shadows.west(image_preview_ip.getProcessor());
		
		set_imageViewPreview_textViewPosition(image_preview_ip, "WEST");
	}
	
	@FXML
	public void button_south_west_onClick() {
		
		image_preview_ip = new ImagePlus();
		image_preview_ip = image.duplicate();
		
		Shadows shadows = new Shadows();
		shadows.southwest(image_preview_ip.getProcessor());
		
		set_imageViewPreview_textViewPosition(image_preview_ip, "SOUTH-WEST");
	}
	
	@FXML
	public void button_south_onClick() {
		
		image_preview_ip = new ImagePlus();
		image_preview_ip = image.duplicate();
		
		Shadows shadows = new Shadows();
		shadows.south(image_preview_ip.getProcessor());
		
		set_imageViewPreview_textViewPosition(image_preview_ip, "SOUTH");
	}
	
	@FXML
	public void button_south_east_onClick() {
		
		image_preview_ip = new ImagePlus();
		image_preview_ip = image.duplicate();
		
		Shadows shadows = new Shadows();
		shadows.southeast(image_preview_ip.getProcessor());
		
		set_imageViewPreview_textViewPosition(image_preview_ip, "SOUTH-EAST");
	}
	
	@FXML
	public void button_east_onClick() {
		
		image_preview_ip = new ImagePlus();
		image_preview_ip = image.duplicate();
		
		Shadows shadows = new Shadows();
		shadows.east(image_preview_ip.getProcessor());
		
		set_imageViewPreview_textViewPosition(image_preview_ip, "EAST");
	}
	
	public void set_imageViewPreview_textViewPosition(ImagePlus image_preview_ip, String position) {
		
		Image image_preview_fx = SwingFXUtils.toFXImage(image_preview_ip.getProcessor().getBufferedImage(), null);
		imageView_preview.setImage(image_preview_fx);
		
		textView_position.setText(position);
	}
	
	public void setImage(ImagePlus ip) {
		
		image = new ImagePlus();
		
		this.image = ip.duplicate();
	}
	
	public Boolean getStageClosedOnExit() {
		return stage_closed_on_exit_status;
	}
	
	public ImageProcessor getImageProcessor() {
		return image_return.getProcessor();
	}
}
