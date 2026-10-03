package mg.emit.picneo.tenten.controllers;

import mg.emit.picneo.tenten.util.TitleBar_Util;

import ij.ImagePlus;
import ij.gui.HistogramWindow;
import ij.plugin.ChannelSplitter;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import mg.emit.picneo.tenten.util.FrenchHistogramWindow;

public class Main_Image_Statistics_RGB_Controller implements Tool_Dialog_Controller {

	@FXML
	public Button button_exit;

	@FXML
	public Label label_title;

	@FXML
	public HBox title_bar;
	
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
	public void initialize() {
		TitleBar_Util.bind(title_bar, button_exit, this::getStage);
	}

	@FXML
	public void button_exit_action_event(ActionEvent event) {
		
		Stage stage = getStage();
		if (stage != null) {
			stage.close();
		}
	}

	public void setWindowTitle(String title) {
		if (label_title != null) {
			label_title.setText(title);
		}
	}
	
	public void show_rgb_image() {
		
		ImagePlus[] images_rgb = splitChannelsSafe(image);
		
		image_red = images_rgb[0].duplicate();
		image_green = images_rgb[1].duplicate();
		image_blue = images_rgb[2].duplicate();
		
		Image image_sample_red = SwingFXUtils.toFXImage(image_red.getBufferedImage(), null);
		Image image_sample_green = SwingFXUtils.toFXImage(image_green.getBufferedImage(), null);
		Image image_sample_blue = SwingFXUtils.toFXImage(image_blue.getBufferedImage(), null);	
		
		setPreview(iv_image_red, image_sample_red, 275.0, 240.0);
		setPreview(iv_image_green, image_sample_green, 275.0, 240.0);
		setPreview(iv_image_blue, image_sample_blue, 275.0, 240.0);
		
		HistogramWindow histogram_red = new FrenchHistogramWindow(image_red);
		HistogramWindow histogram_green = new FrenchHistogramWindow(image_green);
		HistogramWindow histogram_blue = new FrenchHistogramWindow(image_blue);
		
		image_sample_red = SwingFXUtils.toFXImage(histogram_red.getImagePlus().getBufferedImage(), null);
		image_sample_green = SwingFXUtils.toFXImage(histogram_green.getImagePlus().getBufferedImage(), null);
		image_sample_blue = SwingFXUtils.toFXImage(histogram_blue.getImagePlus().getBufferedImage(), null);

		histogram_red.close();
		histogram_green.close();
		histogram_blue.close();
		
		setPreview(iv_histogram_red, image_sample_red, 250.0, 200.0);
		setPreview(iv_histogram_green, image_sample_green, 250.0, 200.0);
		setPreview(iv_histogram_blue, image_sample_blue, 250.0, 200.0);
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
		show_rgb_image();
	}

	/**
	 * Découpe en canaux sans planter sur les images non-RGB
	 * (niveaux de gris : le même canal est montré en R, V et B).
	 */
	private ImagePlus[] splitChannelsSafe(ImagePlus ip) {
		ImagePlus[] fallback = new ImagePlus[]{
				ip.duplicate(), ip.duplicate(), ip.duplicate()};
		ImagePlus[] split = null;
		try {
			split = ChannelSplitter.split(ip);
		} catch (Exception e) {
			split = null;
		}
		if (split == null || split.length == 0 || split[0] == null) {
			return fallback;
		}
		ImagePlus red = split[0];
		ImagePlus green = split.length > 1 && split[1] != null ? split[1] : ip.duplicate();
		ImagePlus blue = split.length > 2 && split[2] != null ? split[2] : ip.duplicate();
		return new ImagePlus[]{red, green, blue};
	}

	private void setPreview(ImageView view, Image preview, double width, double height) {
		view.setFitWidth(width);
		view.setFitHeight(height);
		view.setPreserveRatio(true);
		view.setSmooth(true);
		view.setImage(preview);
	}

	private Stage getStage() {
		if (button_exit == null || button_exit.getScene() == null) {
			return null;
		}
		return (Stage) button_exit.getScene().getWindow();
	}
}
