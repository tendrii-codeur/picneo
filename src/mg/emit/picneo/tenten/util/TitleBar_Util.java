package mg.emit.picneo.tenten.util;

import java.util.function.Supplier;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * Comportement commun des barres de titre des fenêtres modales :
 * survol rouge du bouton fermer + déplacement de la fenêtre par glisser.
 * Réutilisé par tous les contrôleurs de fenêtres.
 */
public final class TitleBar_Util {

    /** Style du bouton fermer au repos. */
    public static final String EXIT_STYLE_DEFAULT =
            "-fx-background-color: #E81123; -fx-background-radius: 0; -fx-border-width: 0; -fx-cursor: hand;";

    /** Style du bouton fermer survolé. */
    public static final String EXIT_STYLE_HOVER =
            "-fx-background-color: #F1707A; -fx-background-radius: 0; -fx-border-width: 0; -fx-cursor: hand;";

    /** Variante à plat (sans coins arrondis ni marge) : bouton fermer au repos. */
    public static final String EXIT_STYLE_FLAT_DEFAULT =
            "-fx-background-color: #E81123; -fx-background-radius: 0; -fx-background-insets: 0; -fx-border-width: 0; -fx-cursor: hand; -fx-padding: 0;";

    /** Variante à plat : bouton fermer survolé. */
    public static final String EXIT_STYLE_FLAT_HOVER =
            "-fx-background-color: #F1707A; -fx-background-radius: 0; -fx-background-insets: 0; -fx-border-width: 0; -fx-cursor: hand; -fx-padding: 0;";

    private TitleBar_Util() {
    }

    /**
     * Variante standard : la fenêtre est récupérée via la scène du bouton fermer.
     */
    public static void bind(Node titleBar, Button exitButton) {
        bind(titleBar, exitButton, EXIT_STYLE_HOVER, EXIT_STYLE_DEFAULT, () -> windowOf(exitButton));
    }

    /**
     * Variante avec styles personnalisés pour le bouton fermer
     * (bouton sans bordure arrondie ni marge interne, par exemple).
     */
    public static void bind(Node titleBar, Button exitButton, String hoverStyle, String defaultStyle) {
        bind(titleBar, exitButton, hoverStyle, defaultStyle, () -> windowOf(exitButton));
    }

    /**
     * Variante avec fournisseur de fenêtre explicite (peut retourner null,
     * auquel cas le déplacement est simplement ignoré).
     */
    public static void bind(Node titleBar, Button exitButton, Supplier<Stage> stageSupplier) {
        bind(titleBar, exitButton, EXIT_STYLE_HOVER, EXIT_STYLE_DEFAULT, stageSupplier);
    }

    /** Forme générale : styles du bouton + fournisseur de fenêtre. */
    public static void bind(final Node titleBar, final Button exitButton, final String hoverStyle,
            final String defaultStyle, final Supplier<Stage> stageSupplier) {

        final double[] dragOffset = new double[2];

        exitButton.setOnMouseEntered(event -> exitButton.setStyle(hoverStyle));
        exitButton.setOnMouseExited(event -> exitButton.setStyle(defaultStyle));

        titleBar.setOnMousePressed(event -> {
            dragOffset[0] = event.getSceneX();
            dragOffset[1] = event.getSceneY();
        });
        titleBar.setOnMouseDragged(event -> {
            Stage stage = stageSupplier.get();
            if (stage == null) {
                return;
            }
            stage.setX(event.getScreenX() - dragOffset[0]);
            stage.setY(event.getScreenY() - dragOffset[1]);
        });
    }

    private static Stage windowOf(Button exitButton) {
        return exitButton.getScene() != null ? (Stage) exitButton.getScene().getWindow() : null;
    }
}
