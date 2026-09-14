package mg.emit.picneo.tenten.controllers;

import ij.ImagePlus;
import ij.gui.HistogramWindow;
import ij.plugin.ChannelSplitter;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import mg.emit.picneo.tenten.util.FrenchHistogramWindow;

public class Main_Image_Statistics_RGB_Controller {

	@FXML
	public Button button_exit;
	
	@FXML
	public ImageView iv_image_red;
	
	@FXML
	public ImageView iv_image_green;
	
	@FXML
	public ImageView iv_image_blue;
	
	@FXML
	public ImageView iv_histogram_red;
	
	@FXML
	public ImageView iv_histogram_green;
	
	@FXML
	public ImageView iv_histogram_blue;
	
	private ImagePlus image = new ImagePlus();
	
	private ImagePlus image_red = new ImagePlus();
	private ImagePlus image_green = new ImagePlus();
	private ImagePlus image_blue = new ImagePlus();
	
	@FXML
	public void button_exit_action_event(ActionEvent event) {
		
		Stage stage = (Stage) button_exit.getScene().getWindow();
	    stage.close();
	}
	
	@FXML
	public void show_rgb_image() {
		
		ImagePlus[] images_rgb = ChannelSplitter.split(image);
		
		image_red = images_rgb[0].duplicate();
		image_green = images_rgb[1].duplicate();
		image_blue = images_rgb[2].duplicate();
		
		Image image_sample_red = SwingFXUtils.toFXImage(image_red.getBufferedImage(), null);
		Image image_sample_green = SwingFXUtils.toFXImage(image_green.getBufferedImage(), null);
		Image image_sample_blue = SwingFXUtils.toFXImage(image_blue.getBufferedImage(), null);	
		
		iv_image_red.setImage(image_sample_red);
		iv_image_green.setImage(image_sample_green);
		iv_image_blue.setImage(image_sample_blue);
		
		HistogramWindow histogram_red = new FrenchHistogramWindow(image_red);
		HistogramWindow histogram_green = new FrenchHistogramWindow(image_green);
		HistogramWindow histogram_blue = new FrenchHistogramWindow(image_blue);
		
		image_sample_red = SwingFXUtils.toFXImage(histogram_red.getImagePlus().getBufferedImage(), null);
		image_sample_green = SwingFXUtils.toFXImage(histogram_green.getImagePlus().getBufferedImage(), null);
		image_sample_blue = SwingFXUtils.toFXImage(histogram_blue.getImagePlus().getBufferedImage(), null);	
		
		iv_histogram_red.setImage(image_sample_red);
		iv_histogram_green.setImage(image_sample_green);
		iv_histogram_blue.setImage(image_sample_blue);
	}
	
	@FXML
	public void show_image_red() {
		if(image_red != null) {
			image_red.show();
		}
	}
	
	@FXML
	public void show_image_green() {
		if(image_green != null) {
			image_green.show();
		}
	}
	
	@FXML
	public void show_image_blue() {
		if(image_blue != null) {
			image_blue.show();
		}
	}
	
	public void setImage(ImagePlus ip) {
		
		image = new ImagePlus();
		
		this.image = ip.duplicate();
	}
}
