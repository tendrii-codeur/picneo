package mg.emit.picneo.tenten.util;

import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 * Icônes + titres français des fenêtres de confirmation
 * et de notification (Alert JavaFX) :
 * - INFORMATION : rond bleu "i" ;
 * - WARNING : triangle ambre "!" ;
 * - ERROR : rond rouge "x" ;
 * - CONFIRMATION : rond vert "?" .
 * L'icône est appliquée au contenu ET à la barre de titre.
 */
public final class Alert_Util {

	private Alert_Util() {
	}

	public static void style(Alert alert) {
		if (alert == null) {
			return;
		}
		String path;
		String title;
		switch (alert.getAlertType()) {
			case INFORMATION:
				path = "/icons/alert_info.png";
				title = "Information";
				break;
			case WARNING:
				path = "/icons/alert_warning.png";
				title = "Avertissement";
				break;
			case ERROR:
				path = "/icons/alert_error.png";
				title = "Erreur";
				break;
			case CONFIRMATION:
				path = "/icons/alert_confirm.png";
				title = "Confirmation";
				break;
			default:
				return;
		}
		alert.getDialogPane().setGraphic(new ImageView(new Image(path, 48, 48, true, true)));
		// Titre par type, sauf titre spécifique déjà posé (ex. "Collage",
		// "Enregistrement réussi !") : seul le générique "Alerte !" est remplacé.
		String currentTitle = alert.getTitle();
		if (currentTitle == null || currentTitle.trim().isEmpty()
				|| "Alerte !".equals(currentTitle.trim())) {
			alert.setTitle(title);
		}
		alert.setHeaderText(null);
		Image windowIcon = new Image(path);
		alert.setOnShowing(event -> {
			try {
				Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
				if (stage != null) {
					stage.getIcons().clear();
					stage.getIcons().add(windowIcon);
				}
			} catch (Exception ignored) {
			}
		});
	}
}
