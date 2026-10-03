package mg.emit.picneo.tenten.controllers.image;

import mg.emit.picneo.tenten.controllers.Tool_Dialog_Controller;
import mg.emit.picneo.tenten.util.TitleBar_Util;

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
import javafx.scene.layout.HBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 * Fenêtre de seuillage unique (ajustement + automatique) :
 * - le haut de l'image affiche le seuil minimum et maximum ;
 * - les réglages Min/Max (curseurs) permettent l'ajustement manuel ;
 * - méthode / arrière-plan / LUT recalculent automatiquement et
 *   repositionnent les curseurs ; tout fonctionne ensemble.
 */
public class Image_Threshold_Controller implements Tool_Dialog_Controller {

	@FXML
	public Button button_exit;
	
	@FXML
	public Label label_title;

	@FXML
	public HBox title_bar;
	
	@FXML
	public Button button_adjust;
	
	@FXML
	public Button button_reset;
	
	@FXML
	public Button button_cancel;

	@FXML
	public ComboBox<Threshold_Method_Types> comboBox_method;
	
	@FXML
	public ComboBox<Threshold_Background_Types> comboBox_background;
	
	@FXML
	public ComboBox<Threshold_Lut_Types> comboBox_lut;
	
	@FXML
	public Slider slider_min;
	
	@FXML
	public Slider slider_max;
	
	@FXML
	public ImageView imageView_preview;
	
	@FXML
	public Label label_minimum_threshold;
	
	@FXML
	public Label label_maximum_threshold;
	
	private String method;
	private Boolean background;
	private Integer lut;
	
	private double min_current_value;
	private double max_current_value;

	private double base_min_value;
	private double base_max_value;
	private Threshold_Method_Types base_method;
	private Threshold_Background_Types base_background;
	private Threshold_Lut_Types base_lut;
	
	private boolean stage_closed_on_exit_status = true;
	
	private ImagePlus image = new ImagePlus();
	private ImagePlus image_preview_ip = new ImagePlus();
	
	@FXML
	public void initialize(){
		TitleBar_Util.bind(title_bar, button_exit);

		slider_min.valueProperty().addListener((observable, oldValue, newValue) -> {
			min_current_value = slider_min.getValue();
			if (max_current_value <= min_current_value) {
				slider_max.setValue(min_current_value);
			}
			refreshPreview();
			updateActionButtons();
		});

		slider_max.valueProperty().addListener((observable, oldValue, newValue) -> {
			max_current_value = slider_max.getValue();
			if (max_current_value <= min_current_value) {
				slider_min.setValue(max_current_value);
			}
			refreshPreview();
			updateActionButtons();
		});
		
	    comboBox_method.getItems().addAll(Threshold_Method_Types.values());
	    comboBox_method.getSelectionModel().select(Threshold_Method_Types.DEFAULT);
	    comboBox_method.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
	    	if (newValue != null) {
	    		method = newValue.getApiMethodName();
	    		applyAutoThreshold();
	    	}
	    });
	    
	    comboBox_background.getItems().addAll(Threshold_Background_Types.values());
	    comboBox_background.getSelectionModel().select(Threshold_Background_Types.TRUE);
	    comboBox_background.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
	    	if (newValue != null) {
	    		background = newValue.getDisplayBackgroundName();
	    		applyAutoThreshold();
	    	}
	    });
	    
	    comboBox_lut.getItems().addAll(Threshold_Lut_Types.values());
	    comboBox_lut.getSelectionModel().select(Threshold_Lut_Types.RED_LUT);
	    comboBox_lut.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
	    	if (newValue != null) {
	    		lut = newValue.getDisplayLutValue();
	    		refreshPreview();
	    		updateActionButtons();
	    	}
	    });

	    updateActionButtons();
    }

	/**
	 * Recalcule le seuillage automatique et repositionne les curseurs
	 * Min/Max dessus (l'aperçu suit via les listeners des curseurs).
	 * Les seuils sont bornés à 0..255 (jamais négatifs).
	 */
	private void applyAutoThreshold() {
		if (image == null || image.getWidth() <= 0) {
			updateActionButtons();
			return;
		}
		if (method == null) {
			method = Threshold_Method_Types.DEFAULT.getApiMethodName();
		}
		if (background == null) {
			background = Threshold_Background_Types.TRUE.getDisplayBackgroundName();
		}
		if (lut == null) {
			lut = Threshold_Lut_Types.RED_LUT.getDisplayLutValue();
		}
		ImagePlus probe = image.duplicate();
		probe.getProcessor().setAutoThreshold(method, background, lut);
		double autoMin = clampThreshold(probe.getProcessor().getMinThreshold());
		double autoMax = clampThreshold(probe.getProcessor().getMaxThreshold());
		slider_min.setValue(autoMin);
		slider_max.setValue(autoMax);
		updateActionButtons();
	}

	private double clampThreshold(double v) {
		if (v == ImageProcessor.NO_THRESHOLD || Double.isNaN(v)) {
			return 0;
		}
		if (v < 0) {
			return 0;
		}
		if (v > 255) {
			return 255;
		}
		return v;
	}

	/**
	 * Aperçu à partir des curseurs Min/Max (manuel ou issus de l'auto).
	 */
	private void refreshPreview() {
		if (image == null || image.getWidth() <= 0) {
			return;
		}
		if (lut == null) {
			lut = Threshold_Lut_Types.RED_LUT.getDisplayLutValue();
		}
		image_preview_ip = new ImagePlus();
		image_preview_ip = image.duplicate();

		image_preview_ip.getProcessor().setThreshold(min_current_value, max_current_value, lut);

		label_minimum_threshold.setText(Integer.toString((int) min_current_value));
		label_maximum_threshold.setText(Integer.toString((int) max_current_value));

		Image image_preview_fx = SwingFXUtils.toFXImage(image_preview_ip.getBufferedImage(), null);
		imageView_preview.setImage(image_preview_fx);
	}

	/**
	 * Appliquer reste toujours actif. Réinitialiser est grisé quand
	 * il ne fait rien (aucune modification par rapport à l'ouverture).
	 */
	private void updateActionButtons() {
		button_adjust.setDisable(false);
		boolean modified = slider_min.getValue() != base_min_value
				|| slider_max.getValue() != base_max_value
				|| comboBox_method.getValue() != base_method
				|| comboBox_background.getValue() != base_background
				|| comboBox_lut.getValue() != base_lut;
		button_reset.setDisable(!modified);
	}
	
	@FXML
	public void button_adjust_action_event(ActionEvent event) {
		
		stage_closed_on_exit_status = false;
		
		IJ.run(image_preview_ip, "Convert to Mask",""); 
		
	    Stage stage = (Stage) button_adjust.getScene().getWindow();
	    stage.close();
	}
	
	@FXML
	public void button_reset_action_event(ActionEvent event) {
		
		comboBox_method.getSelectionModel().select(Threshold_Method_Types.DEFAULT);
		comboBox_background.getSelectionModel().select(Threshold_Background_Types.TRUE);
		comboBox_lut.getSelectionModel().select(Threshold_Lut_Types.RED_LUT);
		// Restauration explicite : re-sélectionner une valeur déjà
		// active ne déclenche aucun listener.
		slider_min.setValue(base_min_value);
		slider_max.setValue(base_max_value);
		refreshPreview();
		updateActionButtons();
	}
	
	@FXML
	public void button_cancel_action_event(ActionEvent event) {
		
		stage_closed_on_exit_status = true;
		
		Stage stage = (Stage) button_adjust.getScene().getWindow();
	    stage.close();
	}
	
	public void setImage(ImagePlus ip) {
		
		image = new ImagePlus();
		
		this.image = ip.duplicate();

		method = Threshold_Method_Types.DEFAULT.getApiMethodName();
		background = Threshold_Background_Types.TRUE.getDisplayBackgroundName();
		lut = Threshold_Lut_Types.RED_LUT.getDisplayLutValue();

		// Seuillage automatique initial : positionne les curseurs,
		// affiche l'aperçu et mémorise la référence.
		applyAutoThreshold();

		base_min_value = slider_min.getValue();
		base_max_value = slider_max.getValue();
		base_method = comboBox_method.getValue();
		base_background = comboBox_background.getValue();
		base_lut = comboBox_lut.getValue();
		refreshPreview();
		updateActionButtons();
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
}
