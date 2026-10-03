package mg.emit.picneo.tenten.controllers.process;

import mg.emit.picneo.tenten.controllers.Tool_Dialog_Controller;
import mg.emit.picneo.tenten.util.TitleBar_Util;

import ij.ImagePlus;
import ij.plugin.filter.Shadows;
import ij.process.ImageProcessor;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Process_Shadow_Controller implements Tool_Dialog_Controller {

	@FXML
	public Button button_exit;
	
	@FXML
	public Label label_title;
	
	@FXML
	public HBox title_bar;
	
	@FXML
	public Button button_adjust;
	
	@FXML
	public Button button_cancel;

	@FXML
	public Button button_reset;
	
	@FXML
	public Button button_north_west;
	
	@FXML
	public Button button_north;
	
	@FXML
	public Button button_north_east;
	
	@FXML
	public Button button_east;
	
	@FXML
	public Button button_south_east;

	@FXML
	public Button button_south;
	
	@FXML
	public Button button_south_west;
	
	@FXML
	public Text textView_position;
	
	@FXML
	public ImageView imageView_preview;
	
	public boolean stage_closed_on_exit_status = true;
	
	public ImagePlus image = new ImagePlus();
	public ImagePlus image_preview_ip = new ImagePlus();
	public ImagePlus image_return = new ImagePlus();

	/** Direction courante + nombre de clics (intensité cumulée). */
	private String currentShadowDirection = "NEUTRE";
	private int shadowIntensity = 0;
	
	@FXML
	public void initialize() {
		TitleBar_Util.bind(title_bar, button_exit);
		updateActionButtons();
	}
	
	@FXML
	public void button_adjust_action_event(ActionEvent event) {
		
		image_return = new ImagePlus();
		image_return = image_preview_ip.duplicate();
		
		stage_closed_on_exit_status = false;
		
	    Stage stage = (Stage) button_adjust.getScene().getWindow();
	    stage.close();
	}
	
	@FXML
	public void button_cancel_action_event(ActionEvent event) {
		
		image_return = new ImagePlus();
		image_return = image.duplicate();

		// Annuler / fermer : ne pas appliquer (l'original est conservé).
		stage_closed_on_exit_status = true;

	    Stage stage = (Stage) button_adjust.getScene().getWindow();
	    stage.close();
	}
	
	@FXML
	public void button_reset_action_event(ActionEvent event) {
		
		image_preview_ip = new ImagePlus();
		image_preview_ip = image.duplicate();
		
		resetShadowState();
		set_imageViewPreview_textViewPosition(image_preview_ip, "NEUTRE");
	}
	
	@FXML
	public void imageView_preview_onClick() {
		
		image_preview_ip = new ImagePlus();
		image_preview_ip = image.duplicate();
		
		resetShadowState();
		set_imageViewPreview_textViewPosition(image_preview_ip, "NEUTRE");
	}
	
	@FXML
	public void button_north_east_onClick() {
		
		applyShadowDirection("NORD-EST");
	}
	
	@FXML
	public void button_north_onClick() {
		
		applyShadowDirection("NORD");
	}
	
	@FXML
	public void button_north_west_onClick() {
		
		applyShadowDirection("NORD-OUEST");
	}
	
	@FXML
	public void button_west_onClick() {
		
		applyShadowDirection("OUEST");
	}
	
	@FXML
	public void button_south_west_onClick() {
		
		applyShadowDirection("SUD-OUEST");
	}
	
	@FXML
	public void button_south_onClick() {
		
		applyShadowDirection("SUD");
	}
	
	@FXML
	public void button_south_east_onClick() {
		
		applyShadowDirection("SUD-EST");
	}
	
	@FXML
	public void button_east_onClick() {
		
		applyShadowDirection("EST");
	}

	/**     * Même bouton : incrémente (NORD x1, x2, x3...).
	 * Bouton opposé (NORD/SUD, EST/OUEST, NORD-EST/SUD-OUEST,
	 * NORD-OUEST/SUD-EST) : décrémente l'intensité courante ;
	 * à zéro on revient à NEUTRE. Autre axe : on bascule
	 * sur la nouvelle direction à x1. L'image suit à chaque clic.
	 */
	private void applyShadowDirection(String direction) {
		if (direction.equals(currentShadowDirection)) {
			shadowIntensity++;
		} else if (direction.equals(getOppositeDirection(currentShadowDirection))) {
			shadowIntensity--;
			if (shadowIntensity <= 0) {
				resetShadowState();
				image_preview_ip = image.duplicate();
				set_imageViewPreview_textViewPosition(image_preview_ip, "NEUTRE");
				return;
			}
		} else {
			currentShadowDirection = direction;
			shadowIntensity = 1;
		}
		renderCurrentShadow();
	}

	private void renderCurrentShadow() {
		image_preview_ip = image.duplicate();
		Shadows shadows = new Shadows();
		ImageProcessor processor = image_preview_ip.getProcessor();
		for (int i = 0; i < shadowIntensity; i++) {
			switch (currentShadowDirection) {
				case "NORD-EST": shadows.northeast(processor); break;
				case "NORD": shadows.north(processor); break;
				case "NORD-OUEST": shadows.northwest(processor); break;
				case "OUEST": shadows.west(processor); break;
				case "SUD-OUEST": shadows.southwest(processor); break;
				case "SUD": shadows.south(processor); break;
				case "SUD-EST": shadows.southeast(processor); break;
				case "EST": shadows.east(processor); break;
				default: break;
			}
		}
		String label = shadowIntensity > 1
				? currentShadowDirection + " x" + shadowIntensity
				: currentShadowDirection;
		set_imageViewPreview_textViewPosition(image_preview_ip, label);
	}

	private String getOppositeDirection(String direction) {
		if (direction == null) {
			return null;
		}
		switch (direction) {
			case "NORD": return "SUD";
			case "SUD": return "NORD";
			case "EST": return "OUEST";
			case "OUEST": return "EST";
			case "NORD-EST": return "SUD-OUEST";
			case "SUD-OUEST": return "NORD-EST";
			case "NORD-OUEST": return "SUD-EST";
			case "SUD-EST": return "NORD-OUEST";
			default: return null;
		}
	}

	private void resetShadowState() {
		currentShadowDirection = "NEUTRE";
		shadowIntensity = 0;
	}
	
	public void set_imageViewPreview_textViewPosition(ImagePlus image_preview_ip, String position) {
		
		Image image_preview_fx = SwingFXUtils.toFXImage(image_preview_ip.getProcessor().getBufferedImage(), null);
		imageView_preview.setImage(image_preview_fx);
		
		textView_position.setText(position);
		updateActionButtons();
	}

	/**
	 * Appliquer et Réinitialiser grisés sans modification
	 * (position NEUTRE = état d'ouverture).
	 */
	private void updateActionButtons() {
		String pos = textView_position != null ? textView_position.getText() : null;
		boolean modified = pos != null && !pos.trim().isEmpty()
				&& !"NEUTRE".equalsIgnoreCase(pos.trim())
				&& !"NEUTRAL".equalsIgnoreCase(pos.trim())
				&& !"POSITION".equalsIgnoreCase(pos.trim());
		button_adjust.setDisable(!modified);
		button_reset.setDisable(!modified);
	}
	
	public void setImage(ImagePlus ip) {
		
		image = new ImagePlus();
		
		this.image = ip.duplicate();

		// Image déjà visible à l'ouverture (sinon empty-image.png).
		image_preview_ip = image.duplicate();
		image_return = image.duplicate();
		resetShadowState();
		set_imageViewPreview_textViewPosition(image_preview_ip, "NEUTRE");
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
		return image_return.getProcessor();
	}
}
