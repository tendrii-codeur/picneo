package mg.emit.picneo.tenten.util;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import com.github.sarxos.webcam.Webcam;

import javafx.animation.AnimationTimer;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * Fenêtre modale affichant l'aperçu en direct de la caméra, au même
 * style que les autres fenêtres de l'application (barre de titre,
 * bordures, boutons à icônes). Retourne l'image capturée, ou null si
 * l'utilisateur a annulé.
 */
public final class Camera_Capture_Dialog {

    private Camera_Capture_Dialog() {
    }

    /**
     * @param owner fenêtre propriétaire (peut être null)
     * @param webcam caméra déjà ouverte par l'appelant
     */
    public static BufferedImage show(Window owner, Webcam webcam) {

        final BufferedImage[] result = new BufferedImage[1];

        // Barre de titre identique aux autres fenêtres
        ImageView icon = new ImageView(new Image(Camera_Capture_Dialog.class.getResourceAsStream("/icons/camera.png")));
        icon.setFitWidth(18);
        icon.setFitHeight(18);
        icon.setPreserveRatio(true);
        HBox.setMargin(icon, new Insets(0, 0, 0, 12));

        Label title = new Label("Capture caméra");
        title.setStyle("-fx-font-family: Arial; -fx-font-size: 13;");
        title.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(title, Priority.ALWAYS);
        HBox.setMargin(title, new Insets(0, 12, 0, 12));

        Button buttonExit = new Button("✕");
        buttonExit.setPrefSize(46, 36);
        buttonExit.setMinSize(46, 36);
        buttonExit.setMaxSize(46, 36);
        buttonExit.setStyle("-fx-background-color: #E81123; -fx-background-radius: 0;"
                + " -fx-border-width: 0; -fx-cursor: hand; -fx-text-fill: white;"
                + " -fx-font-family: Arial; -fx-font-size: 14;");
        buttonExit.setTooltip(new Tooltip("Fermer"));

        HBox titleBar = new HBox(icon, title, buttonExit);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setMinHeight(36);
        titleBar.setPrefHeight(36);
        titleBar.setStyle("-fx-background-color: #f3f3f3; -fx-border-color: #d0d0d0; -fx-border-width: 0 0 1 0;");

        // Aperçu en direct
        ImageView preview = new ImageView();
        preview.setPreserveRatio(true);
        preview.setFitWidth(640);
        preview.setFitHeight(460);
        preview.setStyle("-fx-background-color: white;");

        Label info = new Label("Aperçu de la caméra — cliquez sur Capturer pour importer l'image.");
        info.setStyle("-fx-font-family: Arial; -fx-font-size: 13;");

        // Résolution réelle utilisée par la caméra (aucun recadrage ni réduction).
        Dimension viewSize = null;
        try {
            viewSize = webcam.getViewSize();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }
        if (viewSize != null) {
            info.setText("Aperçu de la caméra (" + viewSize.width + " × " + viewSize.height
                    + ") — cliquez sur Capturer pour importer l'image.");
        }

        // Boutons identiques à ceux des autres fenêtres (icône 24 + texte 13)
        ImageView captureIcon = new ImageView(new Image(Camera_Capture_Dialog.class.getResourceAsStream("/icons/camera.png")));
        captureIcon.setFitWidth(24);
        captureIcon.setFitHeight(24);
        captureIcon.setPreserveRatio(true);

        Button buttonCapture = new Button("Capturer", captureIcon);
        buttonCapture.setStyle("-fx-font-size: 13;");
        buttonCapture.setTooltip(new Tooltip("Capturer et importer l'image"));
        buttonCapture.setDefaultButton(true);
        HBox.setMargin(buttonCapture, new Insets(15, 8, 15, 8));

        ImageView cancelIcon = new ImageView(new Image(Camera_Capture_Dialog.class.getResourceAsStream("/icons/close.png")));
        cancelIcon.setFitWidth(24);
        cancelIcon.setFitHeight(24);
        cancelIcon.setPreserveRatio(true);

        Button buttonCancel = new Button("Annuler", cancelIcon);
        buttonCancel.setStyle("-fx-font-size: 13;");
        buttonCancel.setTooltip(new Tooltip("Fermer sans importer l'image"));
        buttonCancel.setCancelButton(true);
        HBox.setMargin(buttonCancel, new Insets(15, 8, 15, 8));

        HBox buttons = new HBox(14, buttonCapture, buttonCancel);
        buttons.setAlignment(Pos.CENTER);

        VBox bottom = new VBox(8, info, buttons);
        bottom.setAlignment(Pos.CENTER);

        BorderPane root = new BorderPane();
        root.setTop(titleBar);
        root.setCenter(preview);
        BorderPane.setMargin(preview, new Insets(20));
        root.setBottom(bottom);
        BorderPane.setMargin(bottom, new Insets(0, 0, 10, 0));
        root.setStyle("-fx-background-color: white; -fx-border-color: #9a9a9a; -fx-border-width: 1;");
        root.setPrefSize(700, 630);

        final Stage stage = openModal(root, "Capture caméra", owner);

        final Webcam cam = webcam;
        final AnimationTimer timer = new AnimationTimer() {
            /** Aperçu limité à ~30 images/s : inutile de convertir plus vite. */
            private long lastFrameNanos;

            @Override
            public void handle(long now) {
                if (now - lastFrameNanos < 33_000_000L) {
                    return;
                }
                lastFrameNanos = now;
                BufferedImage frame = cam.getImage();
                if (frame != null) {
                    // L'aperçu est mis à l'échelle pour l'affichage seul :
                    // l'image capturée reste à sa résolution native complète.
                    preview.setImage(SwingFXUtils.toFXImage(frame, null));
                }
            }
        };

        buttonExit.setOnAction(e -> stage.close());
        buttonCapture.setOnAction(e -> {
            result[0] = cam.getImage();
            stage.close();
        });
        buttonCancel.setOnAction(e -> stage.close());
        stage.setOnHidden(e -> timer.stop());

        timer.start();
        stage.showAndWait();
        timer.stop();
        return result[0];
    }

    private static Stage openModal(javafx.scene.Parent root, String title, Window owner) {
        Stage stage = new Stage();
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        if (owner != null) {
            stage.initOwner(owner);
        }
        stage.centerOnScreen();
        return stage;
    }
}
