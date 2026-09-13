package mg.emit.picneo.tenten.controllers.stitching;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import ij.ImagePlus;
import ij.WindowManager;
import ij.io.FileSaver;
import ij.plugin.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;

public class Stitching_Pairwise_Controller {

	@FXML
	public Button button_close;
	
	@FXML
	public Button button_load_images;
	
	@FXML
	public Button button_stitching;
	
	@FXML
	public CheckBox checkBox_save;
	
	@FXML
	public ImageView iv_1;
	
	@FXML
	public ImageView iv_2;
	
	@FXML
	public ImageView iv_3;
	
	@FXML
	public ImageView iv_4;
	
	@FXML
	public ImageView iv_5;
	
	@FXML
	public ImageView iv_6;
	
	@FXML
	public ImageView iv_7;
	
	@FXML
	public ImageView iv_8;
	
	@FXML
	public ImageView iv_9;
	
	@FXML
	public ImageView iv_stitching_left;
	
	@FXML
	public ImageView iv_stitching_right;
	
	@FXML
	public TextField tf_result_name;
	
	private ArrayList<ImageView> iv_array = new ArrayList<ImageView>();
	private ArrayList<ImagePlus> ip_array = new ArrayList<ImagePlus>();
	
	private ImagePlus ip_stitching_left = new ImagePlus();
	private ImagePlus ip_stitching_right = new ImagePlus();
	
	@FXML
	public void initialize() {
		initialize_imageView_arrays();
		initialize_imageView_select();
	}
	
	private void initialize_imageView_arrays() {
		
		iv_array.add(iv_1);
		iv_array.add(iv_2);
		iv_array.add(iv_3);
		iv_array.add(iv_4);
		iv_array.add(iv_5);
		iv_array.add(iv_6);
		iv_array.add(iv_7);
		iv_array.add(iv_8);
		iv_array.add(iv_9);
	}
	
	private void initialize_imageView_select() {
		
		for(int i = 0; i < 9; i++) {
			
			Integer x = Integer.valueOf(iv_array.get(i).getAccessibleText());
			
			iv_array.get(i).setOnMouseClicked(event -> {
				
				if(event.getButton() == MouseButton.PRIMARY)
	            {
	                setLeftImageStitching(x);
	            } 
				else if (event.getButton() == MouseButton.SECONDARY)
	            {
	            	setRightImageStitching(x);
	            }
			});
		}
	}
	
	@FXML
	public void close() {
		Stage stage = (Stage) button_close.getScene().getWindow();
		stage.close();
	}
	
	@FXML
	public void executeStitching() throws RuntimeException, InvocationTargetException{
		
		Alert alert = new Alert(AlertType.INFORMATION, "Click on images that are opened right now!");
		alert.setTitle("Warning!");
		alert.show();
		
		ImagePlus ip1 = new ImagePlus();
		ImagePlus ip2 = new ImagePlus();
		
		ip1 = ip_stitching_left.duplicate();
		ip2 = ip_stitching_right.duplicate();
		
		ip1.show();
		ip2.show();
		
		for(int i = 0; i < WindowManager.getWindowCount(); i++) {
			
			if(WindowManager.getWindow("Histogram of null") != null) {
				WindowManager.removeWindow(WindowManager.getWindow("Histogram of null"));
			}
		}
		
		try {
                    Class<?> clazz = Class.forName("Stitching_Pairwise");
                    ij.plugin.PlugIn plugin = (ij.plugin.PlugIn) clazz.newInstance();
                    plugin.run("");
                } catch (Exception e) {
                    e.printStackTrace();
                }
		
		if(WindowManager.getWindow("") != null) {
			
			WindowManager.getImage("").setTitle(tf_result_name.getText());
			
			ImagePlus result = new ImagePlus();
			result = WindowManager.getImage(tf_result_name.getText()).duplicate();
			
			WindowManager.closeAllWindows();
			
			result.show();
			
			if(checkBox_save.isSelected()) {
				FileSaver filesaver = new FileSaver(result);
				filesaver.saveAsJpeg();
			}
			
			close();
		}
		else {
			WindowManager.closeAllWindows();
		}
		
		alert.close();
	}

	@FXML
	public void loadImages() {
		
		for(int i = 0; i < ip_array.size(); i++) {
			Image image = SwingFXUtils.toFXImage(ip_array.get(i).getBufferedImage(), null);
			iv_array.get(i).setImage(image);
		}
	}
	
	private void setLeftImageStitching(Integer i) {
		
		if(i < ip_array.size()) {
			ip_stitching_left = new ImagePlus();
			ip_stitching_left = ip_array.get(i).duplicate();
		
			Image image_stitching_left = SwingFXUtils.toFXImage(ip_stitching_left.getBufferedImage(), null);
			iv_stitching_left.setImage(image_stitching_left);
			
			if(ip_stitching_left.getImage() != null && ip_stitching_right.getImage() != null) {
				button_stitching.setDisable(false);
			}
		}
	}
	
	private void setRightImageStitching(Integer i) {
		
		if(i < ip_array.size()) {
			ip_stitching_right = new ImagePlus();
			ip_stitching_right = ip_array.get(i).duplicate();
			
			Image image_stitching_right = SwingFXUtils.toFXImage(ip_stitching_right.getBufferedImage(), null);
			iv_stitching_right.setImage(image_stitching_right);
			
			if(ip_stitching_left.getImage() != null && ip_stitching_right.getImage() != null) {
				button_stitching.setDisable(false);
			}
		}
	}
	
	public void setImageArray(ArrayList<ImagePlus> array) {
		this.ip_array = array;
	}
}
