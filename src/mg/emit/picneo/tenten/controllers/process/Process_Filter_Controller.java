package mg.emit.picneo.tenten.controllers.process;

import mg.emit.picneo.tenten.controllers.Tool_Dialog_Controller;
import mg.emit.picneo.tenten.util.TitleBar_Util;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import mg.emit.picneo.tenten.enums.Process_Filter_Types;
import ij.ImagePlus;
import ij.plugin.filter.RankFilters;
import ij.process.ImageProcessor;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;

public class Process_Filter_Controller implements Tool_Dialog_Controller {

	@FXML
	public Button button_adjust;
	
	@FXML
	public Button button_cancel;

	@FXML
	public Button button_exit;

	@FXML
	public Label label_title;

	@FXML
	public HBox title_bar;

	@FXML
	public Button button_reset;

	@FXML
	public ComboBox<Process_Filter_Types> comboBox_filter;
	
	@FXML
	public ImageView imageView_preview;
	
	@FXML
	public Spinner<Double> spinner_radius;
	
	private static final double DEFAULT_RADIUS = 0.0;
	private static final double WAIT_CURSOR_DELAY_MS = 250.0;
	
	public boolean stage_closed_on_exit_status = true;
	
	private boolean preview_requested = false;
	private Process_Filter_Types last_preview_filter_type;
	private double last_preview_radius;
	
	private boolean preview_rendered = false;
	private Process_Filter_Types rendered_filter_type;
	private double rendered_radius;
	
	private volatile long preview_request_id = 0;
	private ExecutorService preview_executor;
	private PauseTransition wait_cursor_delay;
	
	public ImagePlus image = new ImagePlus();
	public volatile ImagePlus image_preview_ip = new ImagePlus();
	
	@FXML
	public void initialize(){
	    
		comboBox_filter.getItems().addAll(Process_Filter_Types.values());
	    comboBox_filter.getSelectionModel().select(Process_Filter_Types.MOYEN);
	    
	    /*
	     * Le calcul de l'aperçu se fait sur ce thread unique (daemon) pour ne pas
	     * bloquer l'interface pendant les filtres coûteux.
	     */
	    preview_executor = Executors.newSingleThreadExecutor(runnable -> {
	    	Thread thread = new Thread(runnable, "apercu-filtre");
	    	thread.setDaemon(true);
	    	return thread;
	    });
	    
	    spinner_radius.getValueFactory().setConverter(new RadiusStringConverter());
	    spinner_radius.getEditor().setText(spinner_radius.getValueFactory().getConverter().toString(spinner_radius.getValue()));
	    
	    /*
	     * La saisie clavier est prise en compte au fur et à mesure de la frappe (et
	     * non seulement à la validation), pour que le rayon reste modifiable aussi
	     * bien au clavier qu'avec les boutons haut/bas.
	     */
	    spinner_radius.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
	    	updateActionButtonsEnabled();
	    	imageView_filter_preview();
	    });
	    
	    spinner_radius.valueProperty().addListener((observable, oldValue, newValue) -> {
	    	updateActionButtonsEnabled();
	    	imageView_filter_preview();
	    });
	    comboBox_filter.valueProperty().addListener((observable, oldValue, newValue) -> updateActionButtonsEnabled());
	    
    TitleBar_Util.bind(title_bar, button_exit);
    
    updateActionButtonsEnabled();
	}
	
	@FXML
	public void button_adjust_action_event(ActionEvent event) {
		
		ensurePreviewUpToDate();
		shutdownPreview();
		
		stage_closed_on_exit_status = false;
		
	    Stage stage = (Stage) button_adjust.getScene().getWindow();
	    stage.close();
	}
	
	
	@FXML
	public void button_cancel_action_event(ActionEvent event) {
		
		shutdownPreview();
		
		stage_closed_on_exit_status = true;
		
		Stage stage = (Stage) button_adjust.getScene().getWindow();
	    stage.close();
	}
	
	@FXML
	public void button_reset_action_event(ActionEvent event) {
		
		comboBox_filter.getSelectionModel().select(Process_Filter_Types.MOYEN);
		spinner_radius.getValueFactory().setValue(DEFAULT_RADIUS);
		
		imageView_filter_preview();
		updateActionButtonsEnabled();
	}
	
	@FXML
	public void imageView_filter_preview() {
		
		if (image == null || image.getProcessor() == null) {
			return;
		}
		
		Process_Filter_Types filterType = comboBox_filter.getValue();
		
		if (filterType == null) {
			return;
		}
		
		double radius = getRadiusValue();
		
		/*
		 * Le calcul du filtre est coûteux (surtout la médiane) : on ne le refait que
		 * lorsque le type de filtre ou le rayon a réellement changé depuis la dernière
		 * demande.
		 */
		if (preview_requested && filterType == last_preview_filter_type && radius == last_preview_radius) {
			return;
		}
		
		last_preview_filter_type = filterType;
		last_preview_radius = radius;
		preview_requested = true;
		
		runPreview(filterType, radius);
	}
	
	/**
	 * Lance le calcul du filtre en arrière-plan : l'interface reste réactive, même
	 * avec la médiane et un grand rayon, et l'aperçu s'affiche dès que le résultat
	 * est prêt. Un résultat devenu obsolète est ignoré.
	 */
	private void runPreview(final Process_Filter_Types filterType, final double radius) {
		
		if (preview_executor == null || preview_executor.isShutdown()) {
			return;
		}
		
		final long requestId = ++preview_request_id;
		final ImagePlus source = image;
		
		startWaitCursor();
		
		preview_executor.submit(() -> {
			
			// Une demande plus récente a été faite entre-temps : inutile de calculer.
			if (requestId != preview_request_id) {
				return;
			}
			
			ImagePlus result = source.duplicate();
			
			RankFilters rankFilter = new RankFilters();
			rankFilter.rank(result.getProcessor(), radius, filterType.getDisplayFilterValue());
			
			Image preview = SwingFXUtils.toFXImage(result.getBufferedImage(), null);
			
			if (requestId != preview_request_id) {
				return;
			}
			
			image_preview_ip = result;
			
			Platform.runLater(() -> publishPreview(requestId, preview, filterType, radius));
		});
	}
	
	private void publishPreview(long requestId, Image preview, Process_Filter_Types filterType, double radius) {
		
		if (requestId != preview_request_id) {
			return;
		}
		
		imageView_preview.setImage(preview);
		
		rendered_filter_type = filterType;
		rendered_radius = radius;
		preview_rendered = true;
		
		stopWaitCursor();
	}
	
	/**
	 * Garantit que l'aperçu correspond aux paramètres courants au moment d'appliquer.
	 * Si le calcul en arrière-plan n'est pas encore terminé, il est refait ici de
	 * façon synchrone pour que l'image appliquée soit bien la bonne.
	 */
	private void ensurePreviewUpToDate() {
		
		Process_Filter_Types filterType = comboBox_filter.getValue();
		
		if (image == null || image.getProcessor() == null || filterType == null) {
			return;
		}
		
		double radius = getRadiusValue();
		
		if (preview_rendered && filterType == rendered_filter_type && radius == rendered_radius) {
			return;
		}
		
		ImagePlus result = image.duplicate();
		
		RankFilters rankFilter = new RankFilters();
		rankFilter.rank(result.getProcessor(), radius, filterType.getDisplayFilterValue());
		
		image_preview_ip = result;
		imageView_preview.setImage(SwingFXUtils.toFXImage(result.getBufferedImage(), null));
		
		rendered_filter_type = filterType;
		rendered_radius = radius;
		preview_rendered = true;
	}
	
	/**
	 * N'affiche le curseur d'attente qu'à partir de {@link #WAIT_CURSOR_DELAY_MS} :
	 * les filtres rapides ne font donc pas clignoter le curseur.
	 */
	private void startWaitCursor() {
		
		if (wait_cursor_delay == null) {
			wait_cursor_delay = new PauseTransition(Duration.millis(WAIT_CURSOR_DELAY_MS));
			wait_cursor_delay.setOnFinished(event -> applyCursor(Cursor.WAIT));
		}
		
		wait_cursor_delay.playFromStart();
	}
	
	private void stopWaitCursor() {
		
		if (wait_cursor_delay != null) {
			wait_cursor_delay.stop();
		}
		
		applyCursor(Cursor.DEFAULT);
	}
	
	private void applyCursor(Cursor cursor) {
		
		if (imageView_preview.getScene() != null) {
			imageView_preview.getScene().setCursor(cursor);
		}
	}
	
	private void shutdownPreview() {
		
		if (preview_executor != null) {
			preview_executor.shutdownNow();
		}
	}
	
	public void setImage(ImagePlus ip) {
		image = new ImagePlus();
		
		this.image = ip.duplicate();
		updateActionButtonsEnabled();
		imageView_filter_preview();
	}
	
	/**
	 * Active les boutons "Appliquer" et "Réinitialiser" uniquement
	 * lorsque les paramètres du filtre ont été modifiés.
	 */
	private void updateActionButtonsEnabled() {
		boolean modified = comboBox_filter.getValue() != Process_Filter_Types.MOYEN
				|| !isRadiusDefault();
		button_adjust.setDisable(!modified);
		button_reset.setDisable(!modified);
	}
	
	private boolean isRadiusDefault() {
		return getRadiusValue() == DEFAULT_RADIUS;
	}
	
	/**
	 * Retourne le rayon courant. La saisie clavier non encore validée est
	 * prioritaire, sinon la valeur du spinner est utilisée.
	 */
	private double getRadiusValue() {
		Double value = spinner_radius.getValueFactory().getConverter()
				.fromString(spinner_radius.getEditor().getText());
		return value == null ? DEFAULT_RADIUS : value;
	}
	
	@FXML
	public void button_exit_action_event(ActionEvent event) {
		button_cancel_action_event(event);
	}

	public void setWindowTitle(String title) {
		if (label_title != null) {
			label_title.setText(title);
		}
	}

	public Boolean getStageClosedOnExit() {
		return stage_closed_on_exit_status;
	}
	
	public ImageProcessor getImageProcessor() {
		return image_preview_ip.getProcessor();
	}
	
	/**
	 * Convertisseur du rayon : affiche une décimale séparée par un point
	 * ("0.0") et accepte indifféremment le point ou la virgule à la saisie.
	 * Une saisie invalide conserve la dernière valeur valide (jamais null).
	 */
	private static final class RadiusStringConverter extends StringConverter<Double> {
		
		private double lastValidValue = DEFAULT_RADIUS;
		
		@Override
		public String toString(Double value) {
			if (value == null) {
				return "";
			}
			lastValidValue = value;
			return String.format(Locale.US, "%.1f", value);
		}
		
		@Override
		public Double fromString(String text) {
			if (text != null) {
				try {
					double parsed = Double.parseDouble(text.trim().replace(',', '.'));
					if (parsed >= 0.0) {
						lastValidValue = parsed;
						return parsed;
					}
				}
				catch (NumberFormatException e) {
					// saisie invalide : on conserve la dernière valeur valide
				}
			}
			return lastValidValue;
		}
	}
}
