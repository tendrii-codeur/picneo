package mg.emit.picneo.tenten.controllers;

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

public class Main_Image_Statistics_RGB_Controller {

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

	private double dragOffsetX;
	private double dragOffsetY;
	
	@FXML
	public void initialize() {
		button_exit.setOnMouseEntered(event -> button_exit.setStyle(
				"-fx-background-color: #F1707A; -fx-background-radius: 0; -fx-border-width: 0; -fx-cursor: hand;"));
		button_exit.setOnMouseExited(event -> button_exit.setStyle(
				"-fx-background-color: #E81123; -fx-background-radius: 0; -fx-border-width: 0; -fx-cursor: hand;"));

		title_bar.setOnMousePressed(event -> {
			Stage stage = getStage();
			if (stage == null) {
				return;
			}
			dragOffsetX = event.getSceneX();
			dragOffsetY = event.getSceneY();
		});
		title_bar.setOnMouseDragged(event -> {
			Stage stage = getStage();
			if (stage == null) {
				return;
			}
			stage.setX(event.getScreenX() - dragOffsetX);
			stage.setY(event.getScreenY() - dragOffsetY);
		});
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
		
		ImagePlus[] images_rgb = ChannelSplitter.split(image);
		
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
