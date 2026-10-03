package mg.emit.picneo.tenten.controllers.process;

import mg.emit.picneo.tenten.controllers.Tool_Dialog_Controller;
import mg.emit.picneo.tenten.util.Spinner_Util;
import mg.emit.picneo.tenten.util.TitleBar_Util;

import ij.ImagePlus;
import ij.plugin.filter.GaussianBlur;
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

public class Process_Filter_Gaussian_Blur_Controller implements Tool_Dialog_Controller {

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
	public Spinner<Double> spinner_sigma;
	
	public boolean stage_closed_on_exit_status = true;
	
	public ImagePlus image = new ImagePlus();
	public ImagePlus image_preview_ip = new ImagePlus();

	/** Clics flèches répétés : un seul re-rendu quand la saisie se stabilise. */
	private PauseTransition previewDebounce;
	
	@FXML
	public void initialize() {
		TitleBar_Util.bind(title_bar, button_exit);
		Spinner_Util.setupDoubleSpinner(spinner_sigma);
		previewDebounce = new PauseTransition(Duration.millis(200));
		previewDebounce.setOnFinished(event -> imageView_gaussian_blur_preview());
		// Aperçu en direct à chaque frappe / flèche.
		spinner_sigma.valueProperty().addListener((observable, oldValue, newValue) -> {
			updateActionButtons();
			requestPreview();
		});
		updateActionButtons();
	}

	/**
	 * Appliquer/Réinitialiser grisés à l'ouverture et sans modification
	 * (valeurs à zéro).
	 */
	private void updateActionButtons() {
		boolean modified = Spinner_Util.doubleValueOrDefault(spinner_sigma, 0.0) != 0.0;
		button_adjust.setDisable(!modified);
		button_reset.setDisable(!modified);
	}

	private void requestPreview() {
		if (previewDebounce != null) {
			previewDebounce.playFromStart();
		} else {
			imageView_gaussian_blur_preview();
		}
	}
	@FXML
	public void button_adjust_action_event(ActionEvent event) {

		// Applique toujours les valeurs actuelles des champs,
		// même si l'utilisateur n'a pas validé par Entrée.
		imageView_gaussian_blur_preview();
		spinner_sigma.getValueFactory().setValue(Spinner_Util.doubleValueOrDefault(spinner_sigma, 0.0));

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
		
		spinner_sigma.getValueFactory().setValue(0.0);
		imageView_gaussian_blur_preview();
	}
	
	@FXML
	public void imageView_gaussian_blur_preview() {

		if (image == null || image.getWidth() <= 0 || image.getHeight() <= 0) {
			return;
		}

		image_preview_ip = new ImagePlus();
		image_preview_ip = image.duplicate();
		
		Double sigma = Spinner_Util.doubleValueOrDefault(spinner_sigma, 0.0);
		
		GaussianBlur gaussianBlur = new GaussianBlur();
		gaussianBlur.blurGaussian(image_preview_ip.getProcessor(), sigma);

		Image image_preview_fx = SwingFXUtils.toFXImage(image_preview_ip.getBufferedImage(), null);
		imageView_preview.setImage(image_preview_fx);
	}
	
	public void setImage(ImagePlus ip) {
		
		image = new ImagePlus();
		
		this.image = ip.duplicate();

		// Aperçu initial : sinon "Appliquer" sans toucher aux champs
		// écraserait l'image avec un processeur vide.
		imageView_gaussian_blur_preview();
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
