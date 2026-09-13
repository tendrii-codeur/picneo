package mg.emit.picneo.tenten.controllers.analyze;

import java.util.ArrayList;

import mg.emit.picneo.tenten.domains.Particle_Result_Domain;
import mg.emit.picneo.tenten.enums.Analyze_Particles_Options;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import ij.plugin.filter.ParticleAnalyzer;
import ij.process.ImageProcessor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class Analyze_Particles_Controller {

	@FXML
	public Button button_analyze;
	
	@FXML
	public Button button_close;
	
	@FXML
	public Button button_show;
	
	@FXML
	public ComboBox<Analyze_Particles_Options> comboBox_options;
	
	@FXML
	public HBox hbox_image_preview_1;
	
	@FXML
	public HBox hbox_image_preview_2;
	
	@FXML
	public ImageView imageView_preview;
	
	@FXML
	public Label label_roi_x;
	
	@FXML
	public Label label_roi_y;
	
	@FXML
	public Label label_zoom_value;
	
	@FXML
	public ScrollBar scroll_zoom;
	
	@FXML
	public Spinner<Double> spinner_pixel_size_min;
	
	@FXML
	public Spinner<Double> spinner_pixel_size_max;
	
	@FXML
	public Spinner<Double> spinner_circurality_min;
	
	@FXML
	public Spinner<Double> spinner_circurality_max;
	
	
	@FXML
	public TableColumn<Particle_Result_Domain, Integer> tc_id;
	
	@FXML
	public TableColumn<Particle_Result_Domain, Double> tc_width;
	
	@FXML
	public TableColumn<Particle_Result_Domain, Double> tc_height;
	
	@FXML
	public TableColumn<Particle_Result_Domain, Double> tc_area;
	
	@FXML
	public TableColumn<Particle_Result_Domain, Double> tc_mean;
	
	@FXML
	public TableColumn<Particle_Result_Domain, Double> tc_min;
	
	@FXML
	public TableColumn<Particle_Result_Domain, Double> tc_max;
	
	@FXML
	public TableColumn<Particle_Result_Domain, Double> tc_x;
	
	@FXML
	public TableColumn<Particle_Result_Domain, Double> tc_y;
	
	@FXML
	public TableColumn<Particle_Result_Domain, Double> tc_int_den;
	
	@FXML
	public TableColumn<Particle_Result_Domain, Double> tc_raw_int_den;
	
	@FXML
	public TableView<Particle_Result_Domain> tableView;
	
	@FXML
	public ToggleButton toggleButton_pixel_size_max_infinite;
	
	@FXML
	public ToggleButton toggleButton_include_holes;
	
	@FXML
	public ToggleButton toggleButton_exclude_edges;
	
	private ImagePlus image = new ImagePlus();
	private ImagePlus image_preview = new ImagePlus();
	
	private Integer roi_x = 0;
	private Integer roi_y = 0;
	
	@FXML
	public void initialize() {
	
		initialize_combo_box();
	    initialize_table();
	}
	
	private void initialize_combo_box() {
		
		comboBox_options.getItems().addAll(Analyze_Particles_Options.values());
		comboBox_options.getSelectionModel().select(Analyze_Particles_Options.OUTLINES);
	}
	
	private void initialize_table() {
		
		tc_id.setCellValueFactory(new PropertyValueFactory<>("Id"));
	    tc_width.setCellValueFactory(new PropertyValueFactory<>("Width"));
	    tc_height.setCellValueFactory(new PropertyValueFactory<>("Height"));
	    tc_area.setCellValueFactory(new PropertyValueFactory<>("Area"));
	    tc_mean.setCellValueFactory(new PropertyValueFactory<>("Mean"));
	    tc_min.setCellValueFactory(new PropertyValueFactory<>("Min"));
	    tc_max.setCellValueFactory(new PropertyValueFactory<>("Max"));
	    tc_x.setCellValueFactory(new PropertyValueFactory<>("X"));
	    tc_y.setCellValueFactory(new PropertyValueFactory<>("Y"));
	    tc_int_den.setCellValueFactory(new PropertyValueFactory<>("Int_den"));
	    tc_raw_int_den.setCellValueFactory(new PropertyValueFactory<>("Raw_int_den"));
	}
	
	@FXML
	public void analyze_particles_preview() {
		
		double pixel_size_min = spinner_pixel_size_min.getValue();
		double pixel_size_max = spinner_pixel_size_max.getValue();
		
		double circularity_min = spinner_circurality_min.getValue();
		double circularity_max = spinner_circurality_max.getValue();
		
		int option = comboBox_options.getValue().getDisplayOptionValue();
		
		if(toggleButton_include_holes.isSelected()) {
			option += ParticleAnalyzer.INCLUDE_HOLES;
		}
		if(toggleButton_exclude_edges.isSelected()) {
			option += ParticleAnalyzer.EXCLUDE_EDGE_PARTICLES;
		}
	
		if(toggleButton_pixel_size_max_infinite.isSelected()) {
			pixel_size_max = Double.POSITIVE_INFINITY;
		}
		
		ResultsTable rt = new ResultsTable();
		
		ParticleAnalyzer pa = new ParticleAnalyzer(option, 
				ParticleAnalyzer.ALL_STATS, rt, pixel_size_min, pixel_size_max,
				circularity_min, circularity_max);
		
		pa.setHideOutputImage(true);
		pa.analyze(image);
	
		image_preview = new ImagePlus();
		image_preview = pa.getOutputImage().duplicate();
	
		Image image_preview_fx = SwingFXUtils.toFXImage(image_preview.getBufferedImage(), null);
		imageView_preview.setImage(image_preview_fx);
		
		ArrayList<Particle_Result_Domain> list = new ArrayList<>();
		
		for(int i = 0; i < rt.getCounter() - 1; i++) {
			
			double width = rt.getValue("Width", i);
			double height = rt.getValue("Height", i);
			double area = rt.getValue("Area", i);
			double mean = rt.getValue("Mean", i);
			double min = rt.getValue("Min", i);
			double max = rt.getValue("Max", i);
			double x = rt.getValue("X", i);
			double y = rt.getValue("Y", i);
			double int_den = rt.getValue("IntDen", i);
			double raw_int_den = rt.getValue("RawIntDen", i);
			
			list.add(new Particle_Result_Domain(i + 1, width, height, area, mean, min, max,
												x, y, int_den, raw_int_den));
		}
		
		ObservableList<Particle_Result_Domain> model = FXCollections.observableArrayList(list);
		tableView.setItems(model);
		
		hbox_image_preview_1.setDisable(false);
		hbox_image_preview_2.setDisable(false);
	}
	
	@FXML
	public void close() {
		Stage stage = (Stage) button_close.getScene().getWindow();
		stage.close();
	}
	
	@FXML
	public void reset_image_roi() {
		
		scroll_zoom.setValue(1);
		label_zoom_value.setText(String.valueOf(scroll_zoom.getValue()));
		show_image_roi();
	}
	
	@FXML
	public void showImage() {
		
                if (this.image.getType() == ImagePlus.COLOR_RGB) {
                    ij.process.ImageConverter ic = new ij.process.ImageConverter(this.image);
                    ic.convertToGray8();
                }
            
		double pixel_size_min = spinner_pixel_size_min.getValue();
		double pixel_size_max = spinner_pixel_size_max.getValue();
		
		double circularity_min = spinner_circurality_min.getValue();
		double circularity_max = spinner_circurality_max.getValue();
		
		int option = comboBox_options.getValue().getDisplayOptionValue();
		
		if(toggleButton_pixel_size_max_infinite.isSelected()) {
			pixel_size_max = Double.POSITIVE_INFINITY;
		}
		
		ResultsTable rt = new ResultsTable();
		
		ParticleAnalyzer pa = new ParticleAnalyzer(option, 
				ParticleAnalyzer.ALL_STATS, rt, pixel_size_min, pixel_size_max,
				circularity_min, circularity_max);
		
		pa.setHideOutputImage(true);
		pa.analyze(image);
		pa.getOutputImage().show();
	}
	
	@FXML
	public void show_image_roi_left() {
		
		Integer value_zoom = (int) scroll_zoom.getValue();
		Integer width = image_preview.getWidth() / value_zoom;
		
		roi_x -= 5;
		
		if(roi_x >= 0 && roi_x <= image_preview.getWidth()) {
			show_image_roi();
		}
		else if(roi_x < 0){
			roi_x = image_preview.getWidth() - width - 15;
			show_image_roi();
		}
	}
	
	@FXML
	public void show_image_roi_right() {
		
		Integer value_zoom = (int) scroll_zoom.getValue();
		Integer width = image_preview.getWidth() / value_zoom;
		
		roi_x += 5;
		
		if(roi_x + width >= 0 && roi_x + width <= image_preview.getWidth()) {
			show_image_roi();
		}
		else if(roi_x + width > image_preview.getWidth()) {
			roi_x = 0;
			show_image_roi();
		}
	}
	
	@FXML
	public void show_image_roi_up() {
		
		Integer value_zoom = (int) scroll_zoom.getValue();
		Integer height = image_preview.getHeight() / value_zoom;
		
		roi_y -= 5;
		
		if(roi_y >= 0 && roi_y + height <= image_preview.getHeight()) {
			show_image_roi();
		}
		else if(roi_y < 0){
			roi_y = image_preview.getHeight() - height - 15;;
			show_image_roi();
		}
	}
	
	@FXML
	public void show_image_roi_down() {
		
		Integer value_zoom = (int) scroll_zoom.getValue();
		Integer height = image_preview.getHeight() / value_zoom;
		
		roi_y += 5;
		
		if(roi_y + height >= 0 && roi_y + height <= image_preview.getHeight()) {
			show_image_roi();
		}
		else if(roi_y + height > image_preview.getHeight()) {
			roi_y = 0;
			show_image_roi();
		}
	}
	
	@FXML
	public void show_image_roi() {
		
		ImagePlus image = new ImagePlus();
		image = image_preview.duplicate();
		
		ImageProcessor cropped = image.getProcessor(); //image from currently selected roi
		
		Integer value_zoom = (int) scroll_zoom.getValue();
		label_zoom_value.setText(String.valueOf(scroll_zoom.getValue()));
		
		if(value_zoom > 1) {
			cropped.setRoi(roi_x , roi_y, image.getWidth() / value_zoom, image.getHeight() / value_zoom);
			
			image.setProcessor(cropped.crop());
			
			Image image_preview_fx = SwingFXUtils.toFXImage(image.getBufferedImage(), null);
			imageView_preview.setImage(image_preview_fx);
		}
		else {
			roi_x = 0;
			roi_y = 0;
			cropped.setRoi(0 , 0, image.getWidth() / value_zoom, image.getHeight() / value_zoom);
			
			image.setProcessor(cropped.crop());
			
			Image image_preview_fx = SwingFXUtils.toFXImage(image.getBufferedImage(), null);
			imageView_preview.setImage(image_preview_fx);
		}
		
		label_roi_x.setText(roi_x.toString());
		label_roi_y.setText(roi_y.toString());
	}
	
	@FXML
	public void show_imagej_roi() {
		
		ImagePlus image = new ImagePlus();
		image = image_preview.duplicate();
		
		ImageProcessor cropped = image.getProcessor();
		
		Integer value_zoom = (int) scroll_zoom.getValue();
		
		if(value_zoom > 1) {
			cropped.setRoi(roi_x , roi_y, image.getWidth() / value_zoom, image.getHeight() / value_zoom);
		}
		else {
			roi_x = 0;
			roi_y = 0;
			
			cropped.setRoi(0 , 0, image.getWidth() / value_zoom, image.getHeight() / value_zoom);
		}
		
		image.setProcessor(cropped.crop());
		
		image.show();
	}
	
	@FXML
	public void toggle_button_action() {
		
		if(toggleButton_pixel_size_max_infinite.isSelected()) {
			spinner_pixel_size_max.setDisable(true);
		}
		else {
			spinner_pixel_size_max.setDisable(false);
		}
	}
	
	public void setImage(ImagePlus ip) {
		
		image = new ImagePlus();
		
		this.image = ip.duplicate();
	}
	
}