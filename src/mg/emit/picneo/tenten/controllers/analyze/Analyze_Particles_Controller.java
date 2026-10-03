package mg.emit.picneo.tenten.controllers.analyze;

import mg.emit.picneo.tenten.controllers.Tool_Dialog_Controller;
import mg.emit.picneo.tenten.util.Spinner_Util;
import mg.emit.picneo.tenten.util.TitleBar_Util;

import java.util.ArrayList;

import mg.emit.picneo.tenten.domains.Particle_Result_Domain;
import mg.emit.picneo.tenten.enums.Analyze_Particles_Options;
import mg.emit.picneo.tenten.util.Win32TitleBar;
import ij.ImagePlus;
import ij.gui.ImageWindow;
import ij.measure.ResultsTable;
import ij.plugin.filter.ParticleAnalyzer;
import ij.process.ImageProcessor;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Analyze_Particles_Controller implements Tool_Dialog_Controller {

	@FXML
	public Button button_exit;

	@FXML
	public Label label_title;

	@FXML
	public HBox title_bar;
	
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
	public Button button_roi_left;
	
	@FXML
	public Button button_roi_right;
	
	@FXML
	public Button button_roi_up;
	
	@FXML
	public Button button_roi_down;
	
	@FXML
	public Button button_roi_reset;
	
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
	public TableColumn<Particle_Result_Domain, Double> tc_x;
	
	@FXML
	public TableColumn<Particle_Result_Domain, Double> tc_y;
	
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

	private boolean imagePanActive;
	private double imagePanStartX;
	private double imagePanStartY;
	private int imagePanStartRoiX;
	private int imagePanStartRoiY;

	/** Évite de relancer une analyse complète à chaque clic flèche : une seule analyse quand la saisie se stabilise. */
	private PauseTransition analysisDebounce;

	@FXML
	public void initialize() {

		initialize_combo_box();
	    initialize_table();
	    initialize_zoom_and_pan();
	    initialize_auto_analysis();
	    initialize_table_selection();
	    TitleBar_Util.bind(title_bar, button_exit, TitleBar_Util.EXIT_STYLE_FLAT_HOVER, TitleBar_Util.EXIT_STYLE_FLAT_DEFAULT);
	}

	/**
	 * Clic sur une ligne : l'image se centre et zoome sur la particule
	 * (centroïde X/Y, cadre proportionnel à sa taille).
	 */
	private void initialize_table_selection() {
		tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
			if (newSel != null) {
				zoomOnParticle(newSel);
			}
		});
	}

	private void zoomOnParticle(Particle_Result_Domain particle) {
		if (!hasPreviewImage() || particle == null
				|| particle.getX() == null || particle.getY() == null
				|| particle.getWidth() == null || particle.getHeight() == null) {
			return;
		}
		int imgW = image_preview.getWidth();
		int imgH = image_preview.getHeight();
		double boxW = Math.max(1.0, particle.getWidth() * 2.0);
		double boxH = Math.max(1.0, particle.getHeight() * 2.0);
		int zoom = (int) Math.round(Math.min(imgW / boxW, imgH / boxH));
		zoom = Math.max(1, Math.min(zoom, (int) scroll_zoom.getMax()));
		int viewW = Math.max(1, imgW / zoom);
		int viewH = Math.max(1, imgH / zoom);
		roi_x = (int) Math.round(particle.getX() - viewW / 2.0);
		roi_y = (int) Math.round(particle.getY() - viewH / 2.0);
		clampRoi();
		scroll_zoom.setValue(zoom);
		show_image_roi();
	}

	/**
	 * Infini activé par défaut + ré-analyse automatique à chaque
	 * changement de paramètre (spinners, liste, boutons).
	 */
	private void initialize_auto_analysis() {
		toggleButton_pixel_size_max_infinite.setSelected(true);
		spinner_pixel_size_max.setDisable(true);

		analysisDebounce = new PauseTransition(Duration.millis(150));
		analysisDebounce.setOnFinished(event -> analyze_particles_preview());

		Spinner_Util.setupDoubleSpinner(spinner_pixel_size_min);
		Spinner_Util.setupDoubleSpinner(spinner_pixel_size_max);
		Spinner_Util.setupDoubleSpinner(spinner_circurality_min);
		Spinner_Util.setupDoubleSpinner(spinner_circurality_max);

		spinner_pixel_size_min.valueProperty().addListener((obs, oldVal, newVal) -> requestAnalysis());
		spinner_pixel_size_max.valueProperty().addListener((obs, oldVal, newVal) -> requestAnalysis());
		spinner_circurality_min.valueProperty().addListener((obs, oldVal, newVal) -> requestAnalysis());
		spinner_circurality_max.valueProperty().addListener((obs, oldVal, newVal) -> requestAnalysis());
		comboBox_options.setOnAction(event -> analyze_particles_preview());
		toggleButton_pixel_size_max_infinite.selectedProperty().addListener((obs, oldVal, newVal) -> {
			toggle_button_action();
			analyze_particles_preview();
		});
		toggleButton_include_holes.selectedProperty().addListener((obs, oldVal, newVal) -> analyze_particles_preview());
		toggleButton_exclude_edges.selectedProperty().addListener((obs, oldVal, newVal) -> analyze_particles_preview());
	}

	/**
	 * Planifie l'analyse après stabilisation (clics flèches / frappe clavier),
	 * au lieu d'une analyse synchrone bloquante par changement.
	 */
	private void requestAnalysis() {
		if (analysisDebounce != null) {
			analysisDebounce.playFromStart();
		}
	}

	/**
	 * Navigation souris comme l'image actuelle : glisser pour déplacer (zoom > 1),
	 * molette pour zoomer en gardant le point sous le curseur.
	 */
	private void initialize_zoom_and_pan() {
		scroll_zoom.setMin(1);
		scroll_zoom.setMax(10);
		scroll_zoom.setUnitIncrement(1);
		scroll_zoom.setBlockIncrement(1);
		imageView_preview.setOnMouseEntered(e -> updatePanCursor());
		imageView_preview.setOnMouseExited(e -> {
			imagePanActive = false;
			imageView_preview.setCursor(Cursor.DEFAULT);
		});
		imageView_preview.setOnMousePressed(this::imagePanMousePressed);
		imageView_preview.setOnMouseDragged(this::imagePanMouseDragged);
		imageView_preview.setOnMouseReleased(this::imagePanMouseReleased);
		imageView_preview.setOnScroll(this::imageWheelZoom);
	}

	private boolean hasPreviewImage() {
		return image_preview != null && image_preview.getWidth() > 0 && image_preview.getHeight() > 0;
	}

	private boolean isPanEnabled() {
		return hasPreviewImage() && (int) Math.round(scroll_zoom.getValue()) > 1;
	}

	private void updatePanCursor() {
		if (isPanEnabled()) {
			imageView_preview.setCursor(imagePanActive ? Cursor.CLOSED_HAND : Cursor.OPEN_HAND);
		} else {
			imageView_preview.setCursor(Cursor.DEFAULT);
		}
	}

	private void clampRoi() {
		if (!hasPreviewImage()) {
			return;
		}
		int zoom = Math.max(1, (int) Math.round(scroll_zoom.getValue()));
		if (zoom <= 1) {
			roi_x = 0;
			roi_y = 0;
			return;
		}
		int viewW = Math.max(1, image_preview.getWidth() / zoom);
		int viewH = Math.max(1, image_preview.getHeight() / zoom);
		roi_x = Math.max(0, Math.min(roi_x, image_preview.getWidth() - viewW));
		roi_y = Math.max(0, Math.min(roi_y, image_preview.getHeight() - viewH));
	}

	/**
	 * Taille réellement affichée (preserveRatio) pour convertir
	 * les pixels écran en pixels source lors du pan/zoom.
	 */
	private double[] getDisplayedImageSize() {
		double fitW = imageView_preview.getFitWidth();
		double fitH = imageView_preview.getFitHeight();
		Image fx = imageView_preview.getImage();
		if (fitW <= 0 || fitH <= 0 || fx == null || fx.getWidth() <= 0 || fx.getHeight() <= 0) {
			double bw = imageView_preview.getBoundsInLocal().getWidth();
			double bh = imageView_preview.getBoundsInLocal().getHeight();
			return new double[]{Math.max(1, bw), Math.max(1, bh)};
		}
		double scale = Math.min(fitW / fx.getWidth(), fitH / fx.getHeight());
		return new double[]{Math.max(1, fx.getWidth() * scale), Math.max(1, fx.getHeight() * scale)};
	}

	private void imagePanMousePressed(MouseEvent event) {
		if (!isPanEnabled()) {
			return;
		}
		imagePanActive = true;
		imagePanStartX = event.getX();
		imagePanStartY = event.getY();
		imagePanStartRoiX = roi_x;
		imagePanStartRoiY = roi_y;
		imageView_preview.setCursor(Cursor.CLOSED_HAND);
		event.consume();
	}

	private void imagePanMouseDragged(MouseEvent event) {
		if (!imagePanActive || !isPanEnabled()) {
			return;
		}
		int zoom = Math.max(1, (int) Math.round(scroll_zoom.getValue()));
		double[] display = getDisplayedImageSize();
		if (display[0] <= 0 || display[1] <= 0) {
			return;
		}
		double scaleX = (image_preview.getWidth() / (double) zoom) / display[0];
		double scaleY = (image_preview.getHeight() / (double) zoom) / display[1];
		roi_x = imagePanStartRoiX - (int) Math.round((event.getX() - imagePanStartX) * scaleX);
		roi_y = imagePanStartRoiY - (int) Math.round((event.getY() - imagePanStartY) * scaleY);
		clampRoi();
		show_image_roi();
		event.consume();
	}

	private void imagePanMouseReleased(MouseEvent event) {
		if (!imagePanActive) {
			return;
		}
		imagePanActive = false;
		updatePanCursor();
		event.consume();
	}

	/**
	 * Molette : zoome en gardant le point sous le curseur au même endroit à l'écran.
	 */
	private void imageWheelZoom(ScrollEvent event) {
		if (!hasPreviewImage()) {
			return;
		}
		int zoom = Math.max(1, (int) Math.round(scroll_zoom.getValue()));
		int direction = event.getDeltaY() > 0 ? 1 : -1;
		int newZoom = zoom + direction;
		if (newZoom < 1 || newZoom > (int) scroll_zoom.getMax()) {
			event.consume();
			return;
		}
		double[] display = getDisplayedImageSize();
		if (display[0] <= 0 || display[1] <= 0) {
			event.consume();
			return;
		}
		double cropW = image_preview.getWidth() / (double) zoom;
		double cropH = image_preview.getHeight() / (double) zoom;
		double newCropW = image_preview.getWidth() / (double) newZoom;
		double newCropH = image_preview.getHeight() / (double) newZoom;
		double srcX = roi_x + (event.getX() / display[0]) * cropW;
		double srcY = roi_y + (event.getY() / display[1]) * cropH;
		roi_x = (int) Math.round(srcX - (event.getX() / display[0]) * newCropW);
		roi_y = (int) Math.round(srcY - (event.getY() / display[1]) * newCropH);
		scroll_zoom.setValue(newZoom);
		clampRoi();
		show_image_roi();
		event.consume();
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
	    tc_x.setCellValueFactory(new PropertyValueFactory<>("X"));
	    tc_y.setCellValueFactory(new PropertyValueFactory<>("Y"));
	}
	
	@FXML
	public void analyze_particles_preview() {

		if (image == null || image.getWidth() <= 0 || image.getHeight() <= 0) {
			return;
		}

		double pixel_size_min = Spinner_Util.doubleValueOrDefault(spinner_pixel_size_min, 0.0);
		double pixel_size_max = Spinner_Util.doubleValueOrDefault(spinner_pixel_size_max, 0.0);

		double circularity_min = Spinner_Util.doubleValueOrDefault(spinner_circurality_min, 0.0);
		double circularity_max = Spinner_Util.doubleValueOrDefault(spinner_circurality_max, 0.0);
		
		Analyze_Particles_Options selectedOption = comboBox_options.getValue();
		if (selectedOption == null) {
			selectedOption = Analyze_Particles_Options.OUTLINES;
		}
		int option = selectedOption.getDisplayOptionValue();
		
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

		if (pa.getOutputImage() == null) {
			return;
		}

		image_preview = new ImagePlus();
		image_preview = pa.getOutputImage().duplicate();
	
		Image image_preview_fx = SwingFXUtils.toFXImage(image_preview.getBufferedImage(), null);
		imageView_preview.setImage(image_preview_fx);
		
		ArrayList<Particle_Result_Domain> list = new ArrayList<>();
		
		for(int i = 0; i < rt.getCounter(); i++) {
			
			double width = rt.getValue("Width", i);
			double height = rt.getValue("Height", i);
			double area = rt.getValue("Area", i);
			double x = rt.getValue("X", i);
			double y = rt.getValue("Y", i);
			
			list.add(new Particle_Result_Domain(i + 1, width, height, area,
												x, y));
		}
		
		ObservableList<Particle_Result_Domain> model = FXCollections.observableArrayList(list);
		tableView.setItems(model);
		
		hbox_image_preview_1.setDisable(false);
		hbox_image_preview_2.setDisable(false);
	}
	
	@FXML
	public void button_exit_action_event() {
		Stage stage = (Stage) button_exit.getScene().getWindow();
		stage.close();
	}

	public void setWindowTitle(String title) {
		if (label_title != null) {
			label_title.setText(title);
		}
	}

	@FXML
	public void reset_image_roi() {
		
		scroll_zoom.setValue(1);
		label_zoom_value.setText(String.valueOf(scroll_zoom.getValue()));
		show_image_roi();
	}
	
	@FXML
	public void show_image_roi_left() {
		
		if (!hasPreviewImage()) {
			return;
		}
		
		roi_x -= 5;
		clampRoi();
		show_image_roi();
	}
	
	@FXML
	public void show_image_roi_right() {
		
		if (!hasPreviewImage()) {
			return;
		}
		
		roi_x += 5;
		clampRoi();
		show_image_roi();
	}
	
	@FXML
	public void show_image_roi_up() {
		
		if (!hasPreviewImage()) {
			return;
		}
		
		roi_y -= 5;
		clampRoi();
		show_image_roi();
	}
	
	@FXML
	public void show_image_roi_down() {
		
		if (!hasPreviewImage()) {
			return;
		}
		
		roi_y += 5;
		clampRoi();
		show_image_roi();
	}
	
	@FXML
	public void show_image_roi() {

		if (!hasPreviewImage()) {
			updateNavButtons();
			return;
		}
		clampRoi();

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
		updatePanCursor();
		updateNavButtons();
	}
	
	/**
	 * Flèches grisées au zoom 1 ou contre un bord, Réinitialiser
	 * grisé quand il n'y a rien à réinitialiser (zoom 1, origine).
	 * La barre de zoom et Afficher l'image restent toujours actifs.
	 */
	private void updateNavButtons() {
		if (button_roi_left == null || button_roi_reset == null) {
			return;
		}
		boolean canPan = hasPreviewImage() && (int) Math.round(scroll_zoom.getValue()) > 1;
		if (!canPan) {
			button_roi_left.setDisable(true);
			button_roi_right.setDisable(true);
			button_roi_up.setDisable(true);
			button_roi_down.setDisable(true);
		} else {
			int zoom = Math.max(1, (int) Math.round(scroll_zoom.getValue()));
			int viewW = Math.max(1, image_preview.getWidth() / zoom);
			int viewH = Math.max(1, image_preview.getHeight() / zoom);
			button_roi_left.setDisable(roi_x <= 0);
			button_roi_right.setDisable(roi_x + viewW >= image_preview.getWidth());
			button_roi_up.setDisable(roi_y <= 0);
			button_roi_down.setDisable(roi_y + viewH >= image_preview.getHeight());
		}
		boolean atHome = !hasPreviewImage()
				|| ((int) Math.round(scroll_zoom.getValue()) <= 1 && roi_x == 0 && roi_y == 0);
		button_roi_reset.setDisable(atHome);
	}

	@FXML
	public void show_imagej_roi() {

		if (!hasPreviewImage()) {
			return;
		}

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
		// Bouton "Agrandir" : carré double + restauration de la taille
		// ("Réduire" laissé actif, comme dans les autres fenêtres).
		ImageWindow window = image.getWindow();
		if (window != null) {
			Win32TitleBar.installMaximizeToggle(window);
		}
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

		// Image visible dès l'ouverture + aperçu initialisé pour la navigation ROI.
		image_preview = this.image.duplicate();
		roi_x = 0;
		roi_y = 0;
		if (scroll_zoom != null) {
			scroll_zoom.setValue(1);
		}
		Image image_fx = SwingFXUtils.toFXImage(this.image.getBufferedImage(), null);
		imageView_preview.setImage(image_fx);
		label_roi_x.setText("0");
		label_roi_y.setText("0");
		if (label_zoom_value != null) {
			label_zoom_value.setText("1");
		}
		updatePanCursor();

		// Première analyse dès l'ouverture de la fenêtre.
		analyze_particles_preview();
		updateNavButtons();
	}
	
}