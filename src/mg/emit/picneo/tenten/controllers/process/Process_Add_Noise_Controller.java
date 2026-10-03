package mg.emit.picneo.tenten.controllers.process;

import mg.emit.picneo.tenten.controllers.Tool_Dialog_Controller;
import mg.emit.picneo.tenten.util.Spinner_Util;
import mg.emit.picneo.tenten.util.TitleBar_Util;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class Process_Add_Noise_Controller implements Tool_Dialog_Controller {

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
	public ImageView imageView_preview;
	
	@FXML
	public Spinner<Double> spinner_value;
	
	public boolean stage_closed_on_exit_status = true;
	
	public ImagePlus image = new ImagePlus();
	public ImagePlus image_preview_ip = new ImagePlus();

	/** Clics flèches répétés : un seul re-rendu quand la saisie se stabilise. */
	private PauseTransition previewDebounce;

	/** Dernier écart type déjà rendu + aperçu d'origine en cache. */
	private Double lastPreviewValue = null;
	private Image cachedInitialFx = null;
	
	@FXML
	public void initialize() {
		TitleBar_Util.bind(title_bar, button_exit);
		Spinner_Util.setupDoubleSpinner(spinner_value);
		previewDebounce = new PauseTransition(Duration.millis(120));
		previewDebounce.setOnFinished(event -> imageView_add_noise_preview());
		// Boutons d'abord (immédiat), photo ensuite (débouncée).
		spinner_value.valueProperty().addListener((observable, oldValue, newValue) -> {
		    updateActionButtons();
		    requestPreview();
		});
		updateActionButtons();
	}

	private void requestPreview() {
		if (previewDebounce != null) {
			previewDebounce.playFromStart();
		} else {
			imageView_add_noise_preview();
		}
	}

	/**
	 * Appliquer / Réinitialiser actifs seulement si l'écart type
	 * diffère de la valeur initiale (0).
	 */
	private void updateActionButtons() {
		boolean modified = Spinner_Util.doubleValueOrDefault(spinner_value, 0.0) != 0.0;
		button_adjust.setDisable(!modified);
		button_reset.setDisable(!modified);
	}
	@FXML
	public void button_adjust_action_event(ActionEvent event) {

		// Valide la frappe en cours, puis ferme sans re-rendu inutile :
		// si l'aperçu affiche déjà cette valeur, on réutilise image_preview_ip.
		Spinner_Util.commitEditorText(spinner_value);
		double val = Spinner_Util.doubleValueOrDefault(spinner_value, 0.0);
		spinner_value.getValueFactory().setValue(val);
		if (previewDebounce != null) {
			previewDebounce.stop();
		}
		if (lastPreviewValue == null || Double.compare(lastPreviewValue, val) != 0
				|| image_preview_ip == null || image_preview_ip.getWidth() <= 0) {
			image_preview_ip = image.duplicate();
			if (val != 0.0) {
				image_preview_ip.getProcessor().noise(val);
			}
			lastPreviewValue = val;
		}
		
		stage_closed_on_exit_status = false;
		
	    Stage stage = (Stage) button_adjust.getScene().getWindow();
	    stage.close();
	}
	
	@FXML
	public void button_cancel_action_event(ActionEvent event) {
		
		Stage stage = (Stage) button_adjust.getScene().getWindow();
	    stage.close();
	}
	
	@FXML
	public void button_reset_action_event(ActionEvent event) {
		
		if (previewDebounce != null) {
			previewDebounce.stop();
		}
		spinner_value.getValueFactory().setValue(0.0);
		if (previewDebounce != null) {
			previewDebounce.stop();
		}
		// Cas courant : l'original est déjà affiché, aucun calcul.
		// Sinon on restaure depuis le cache, sans noise(0) ni conversion.
		if (lastPreviewValue != null && Double.compare(lastPreviewValue, 0.0) == 0
				&& image_preview_ip != null && image_preview_ip.getWidth() > 0) {
			if (cachedInitialFx != null) {
				imageView_preview.setImage(cachedInitialFx);
			}
		} else {
			image_preview_ip = image.duplicate();
			lastPreviewValue = 0.0;
			if (cachedInitialFx != null) {
				imageView_preview.setImage(cachedInitialFx);
			} else {
				Image image_preview_fx = SwingFXUtils.toFXImage(image_preview_ip.getBufferedImage(), null);
				imageView_preview.setImage(image_preview_fx);
				cachedInitialFx = image_preview_fx;
			}
		}
		updateActionButtons();
	}
	
	@FXML
	public void imageView_add_noise_preview() {

		if (image == null || image.getWidth() <= 0 || image.getHeight() <= 0) {
			return;
		}

		image_preview_ip = new ImagePlus();
		image_preview_ip = image.duplicate();

		Double noise_value = Spinner_Util.doubleValueOrDefault(spinner_value, 0.0);
		image_preview_ip.getProcessor().noise(noise_value);

		Image image_preview_fx = SwingFXUtils.toFXImage(image_preview_ip.getBufferedImage(), null);
		imageView_preview.setImage(image_preview_fx);
		lastPreviewValue = noise_value;
	}
	
	public void setImage(ImagePlus ip) {
		
		image = new ImagePlus();
		
		this.image = ip.duplicate();
		showInitialPreview();
	}

	/** Affiche l'image d'origine dès l'ouverture de la fenêtre. */
	private void showInitialPreview() {
		image_preview_ip = image.duplicate();
		Image image_preview_fx = SwingFXUtils.toFXImage(image_preview_ip.getBufferedImage(), null);
		imageView_preview.setImage(image_preview_fx);
		cachedInitialFx = image_preview_fx;
		lastPreviewValue = 0.0;
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
