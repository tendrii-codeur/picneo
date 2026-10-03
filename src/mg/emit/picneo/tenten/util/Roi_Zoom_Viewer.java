package mg.emit.picneo.tenten.util;

import ij.ImagePlus;
import ij.gui.ImageWindow;
import ij.io.FileSaver;
import ij.process.ImageProcessor;
import java.io.File;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * Visionneuse "Zoom par sélection" :
 * - curseur en croix (+) sur l'image, dessin d'un rectangle (ou carré),
 * - seule la partie à l'intérieur est affichée agrandie (vue zoom),
 * - la figure peut être déplacée / agrandie / rétrécie pour explorer l'image,
 * - bouton Enregistrer pour sauvegarder la partie sélectionnée.
 */
public class Roi_Zoom_Viewer {

    public interface RoiApplyListener {
        void onApply(int x, int y, int w, int h);
    }

    private final ImagePlus source;
    private final Window owner;
    private final RoiApplyListener applyListener;

    private static final double OVERVIEW_MAX_W = 620.0;
    private static final double OVERVIEW_MAX_H = 480.0;
    private static final double HANDLE_SIZE = 10.0;
    private static final double HANDLE_HIT = 8.0;
    private static final int MIN_SIZE_SRC = 5;
    private static final double ICON_SIZE = 25.0;
    private static final double SMALL_ICON_SIZE = 25.0;

    // Barre de titre personnalisée (même rendu que les fenêtres de filtres).
    private static final String EXIT_STYLE_NORMAL =
            "-fx-background-color: #E81123; -fx-background-radius: 0; -fx-border-width: 0; -fx-cursor: hand;";
    private static final String EXIT_STYLE_HOVER =
            "-fx-background-color: #F1707A; -fx-background-radius: 0; -fx-border-width: 0; -fx-cursor: hand;";
    private static final String TITLE_BAR_STYLE =
            "-fx-background-color: #f3f3f3; -fx-border-color: #d0d0d0; -fx-border-width: 0 0 1 0;";

    private double viewScale = 1.0;
    private double dispW = 1;
    private double dispH = 1;

    // Sélection en coordonnées source (pixels image d'origine).
    private double selX;
    private double selY;
    private double selW;
    private double selH;

    private boolean squareMode = false;

    private enum DragMode { NONE, CREATE, MOVE, RESIZE_NW, RESIZE_NE, RESIZE_SW, RESIZE_SE }

    private DragMode dragMode = DragMode.NONE;
    private double pressPaneX;
    private double pressPaneY;
    private double pressSelX;
    private double pressSelY;
    private double pressSelW;
    private double pressSelH;
    private double anchorSrcX;
    private double anchorSrcY;

    private Pane canvas;
    private Rectangle selection;
    private Rectangle[] handles = new Rectangle[4]; // 0=NW 1=NE 2=SW 3=SE
    private ImageView zoomView;
    private Button btnMoveLeft;
    private Button btnMoveRight;
    private Button btnMoveUp;
    private Button btnMoveDown;
    private Button btnBigger;
    private Button btnSmaller;
    private Button btnApply;
    private Button btnReset;

    /** Sélection telle qu'à l'ouverture (référence pour Réinitialiser et l'état grisé). */
    private double initSelX;
    private double initSelY;
    private double initSelW;
    private double initSelH;
    private Label labelInfo;
    private Label labelZoomSize;
    private CheckBox checkSquare;
    private Image fxFull;

    private Roi_Zoom_Viewer(ImagePlus source, Window owner, RoiApplyListener listener) {
        this.source = source;
        this.owner = owner;
        this.applyListener = listener;
    }

    public static void show(ImagePlus image, int initX, int initY, int initW, int initH,
                            Window owner, RoiApplyListener listener) {
        if (image == null) {
            return;
        }
        ImagePlus copy = image.duplicate();
        copy.setTitle(image.getTitle());
        Roi_Zoom_Viewer viewer = new Roi_Zoom_Viewer(copy, owner, listener);
        viewer.initSelection(initX, initY, initW, initH);
        viewer.snapshotInitialSelection();
        viewer.buildAndShow();
    }

    private void initSelection(int initX, int initY, int initW, int initH) {
        int w = Math.max(1, source.getWidth());
        int h = Math.max(1, source.getHeight());
        if (initW > 0 && initH > 0) {
            selX = clamp(initX, 0, Math.max(0, w - 1));
            selY = clamp(initY, 0, Math.max(0, h - 1));
            selW = clamp(initW, MIN_SIZE_SRC, w - (int) selX);
            selH = clamp(initH, MIN_SIZE_SRC, h - (int) selY);
            if (selW < MIN_SIZE_SRC || selH < MIN_SIZE_SRC) {
                centeredDefault(w, h);
            }
        } else {
            centeredDefault(w, h);
        }
    }

    /**
     * Mémorise la sélection d'ouverture. Réinitialiser y revient, et
     * Appliquer/Réinitialiser sont grisés tant qu'on s'y trouve.
     */
    private void snapshotInitialSelection() {
        initSelX = selX;
        initSelY = selY;
        initSelW = selW;
        initSelH = selH;
    }

    private void centeredDefault(int w, int h) {
        selW = Math.max(MIN_SIZE_SRC, w / 3.0);
        selH = Math.max(MIN_SIZE_SRC, h / 3.0);
        selX = (w - selW) / 2.0;
        selY = (h - selH) / 2.0;
    }

    private void buildAndShow() {
        fxFull = SwingFXUtils.toFXImage(source.getProcessor().getBufferedImage(), null);

        int srcW = source.getWidth();
        int srcH = source.getHeight();
        viewScale = Math.min(OVERVIEW_MAX_W / srcW, OVERVIEW_MAX_H / srcH);
        if (viewScale > 4.0) {
            viewScale = 4.0;
        }
        if (viewScale <= 0) {
            viewScale = 1.0;
        }
        dispW = Math.max(50, srcW * viewScale);
        dispH = Math.max(50, srcH * viewScale);

        Stage stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Zoom / Sélection ROI — " + source.getTitle());
        stage.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            stage.initOwner(owner);
        }

        // ---- Vue d'ensemble avec sélection ----
        Label labelLeft = new Label("Image (curseur + : dessiner / déplacer / redimensionner)");
        labelLeft.setStyle("-fx-font-weight: bold;");

        ImageView overview = new ImageView(fxFull);
        overview.setFitWidth(dispW);
        overview.setFitHeight(dispH);
        overview.setPreserveRatio(false);
        overview.setSmooth(true);

        selection = new Rectangle();
        selection.setFill(Color.TRANSPARENT);
        selection.setStroke(Color.RED);
        selection.setStrokeWidth(1.5);
        selection.getStrokeDashArray().addAll(6.0, 4.0);
        selection.setMouseTransparent(true);

        for (int i = 0; i < 4; i++) {
            Rectangle hd = new Rectangle(HANDLE_SIZE, HANDLE_SIZE);
            hd.setFill(Color.WHITE);
            hd.setStroke(Color.RED);
            hd.setStrokeWidth(1.5);
            hd.setMouseTransparent(true);
            handles[i] = hd;
        }

        canvas = new Pane(overview, selection, handles[0], handles[1], handles[2], handles[3]);
        canvas.setPrefSize(dispW, dispH);
        canvas.setMaxSize(dispW, dispH);
        canvas.setStyle("-fx-background-color: black; -fx-border-color: #888;");
        canvas.setCursor(Cursor.CROSSHAIR);
        canvas.setFocusTraversable(true);

        Label hint = new Label("Glisser = nouveau rectangle • Intérieur = déplacer • Coins = redimensionner • Maj = carré • Molette = agrandir/rétrécir");
        hint.setWrapText(true);
        hint.setStyle("-fx-font-size: 11px; -fx-text-fill: #444;");

        // Molette sur l'aperçu : agrandit / rétrécit la figure autour de son centre.
        canvas.setOnScroll(this::onScrollResize);
        canvas.setOnMousePressed(this::onMousePressed);
        canvas.setOnMouseDragged(this::onMouseDragged);
        canvas.setOnMouseReleased(this::onMouseReleased);
        canvas.setOnMouseMoved(this::onMouseMoved);
        canvas.setOnKeyPressed(e -> {
            int step = e.isShiftDown() ? 10 : 2;
            boolean used = true;
            if (e.getCode() == KeyCode.LEFT) {
                moveSelection(-step, 0);
            } else if (e.getCode() == KeyCode.RIGHT) {
                moveSelection(step, 0);
            } else if (e.getCode() == KeyCode.UP) {
                moveSelection(0, -step);
            } else if (e.getCode() == KeyCode.DOWN) {
                moveSelection(0, step);
            } else {
                used = false;
            }
            if (used) {
                e.consume();
            }
        });

        VBox leftBox = new VBox(8, labelLeft, canvas, hint);
        leftBox.setAlignment(Pos.TOP_CENTER);
        leftBox.setPadding(new Insets(10));

        // ---- Vue zoomée (seule la partie sélectionnée apparaît) ----
        Label labelRight = new Label("Aperçu zoomé (contenu de la sélection)");
        labelRight.setStyle("-fx-font-weight: bold;");

        zoomView = new ImageView();
        zoomView.setPreserveRatio(true);
        zoomView.setSmooth(true);
        zoomView.setFitWidth(420);
        zoomView.setFitHeight(380);

        Pane zoomPane = new Pane(zoomView);
        zoomPane.setPrefSize(430, 390);
        zoomPane.setStyle("-fx-background-color: #222; -fx-border-color: #888;");
        // Centre l'image dans le cadre sombre.
        zoomView.fitWidthProperty().addListener((o, a, b) -> centerZoom(zoomPane));
        zoomView.fitHeightProperty().addListener((o, a, b) -> centerZoom(zoomPane));
        zoomView.imageProperty().addListener((o, a, b) -> centerZoom(zoomPane));

        labelInfo = new Label();
        labelInfo.setStyle("-fx-font-size: 12px;");
        labelZoomSize = new Label();
        labelZoomSize.setStyle("-fx-font-size: 12px; -fx-text-fill: #444;");

        HBox moveBox = new HBox(12);
        moveBox.setAlignment(Pos.CENTER);
        moveBox.setPadding(new Insets(6, 0, 0, 0));
        btnMoveLeft = smallBtn("iconfinder_arrow-left-01_186410.png", "Déplacer à gauche");
        btnMoveRight = smallBtn("iconfinder_arrow-right-01_186409.png", "Déplacer à droite");
        btnMoveUp = smallBtn("iconfinder_arrow-up-01_186407.png", "Déplacer en haut");
        btnMoveDown = smallBtn("iconfinder_arrow-down-01_186411.png", "Déplacer en bas");
        btnMoveLeft.setOnAction(e -> { moveSelection(-5, 0); canvas.requestFocus(); });
        btnMoveRight.setOnAction(e -> { moveSelection(5, 0); canvas.requestFocus(); });
        btnMoveUp.setOnAction(e -> { moveSelection(0, -5); canvas.requestFocus(); });
        btnMoveDown.setOnAction(e -> { moveSelection(0, 5); canvas.requestFocus(); });
        moveBox.getChildren().addAll(btnMoveLeft, btnMoveUp, btnMoveDown, btnMoveRight);

        HBox sizeBox = new HBox(12);
        sizeBox.setAlignment(Pos.CENTER);
        sizeBox.setPadding(new Insets(6, 0, 6, 0));
        btnBigger = iconButton("zoom_in.png", "Agrandir la figure", ICON_SIZE);
        btnSmaller = iconButton("zoom_out.png", "Rétrécir la figure", ICON_SIZE);
        btnBigger.setOnAction(e -> scaleSelection(1.2));
        btnSmaller.setOnAction(e -> scaleSelection(1.0 / 1.2));
        sizeBox.getChildren().addAll(btnBigger, btnSmaller);

        checkSquare = new CheckBox("Forcer un carré");
        checkSquare.setTooltip(new Tooltip("Contraindre la sélection à un carré"));
        checkSquare.setSelected(false);
        // Écoute de la propriété (et non de l'évènement "action") : la contrainte
        // est appliquée dès que l'état de la case change.
        checkSquare.selectedProperty().addListener((obs, ancienEtat, nouvelEtat) -> {
            squareMode = nouvelEtat;
            if (squareMode) {
                makeSquare();
            } else {
                refresh();
            }
        });

        VBox rightBox = new VBox(8, labelRight, zoomPane, labelInfo, labelZoomSize, moveBox, sizeBox, checkSquare);
        rightBox.setAlignment(Pos.TOP_CENTER);
        rightBox.setPadding(new Insets(10));

        HBox center = new HBox(10, leftBox, rightBox);
        center.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(leftBox, Priority.ALWAYS);
        HBox.setHgrow(rightBox, Priority.ALWAYS);

        // ---- Barre d'actions ----
        Button btnSave = iconButton("save.png",
                "Enregistrer la sélection... (sauvegarde uniquement la partie dans le rectangle : PNG/JPG/TIF/BMP/GIF)",
                ICON_SIZE);
        btnSave.setOnAction(e -> saveSelection(stage));

        Button btnImageJ = iconButton("open_in_new.png",
                "Ouvrir dans ImageJ (affiche la partie sélectionnée dans une fenêtre ImageJ)",
                ICON_SIZE);
        btnImageJ.setOnAction(e -> openSelectionInImageJ());

        btnApply = iconButton("crop.png",
                "Appliquer (recadrer) : l'image actuelle devient la partie sélectionnée (annulable avec Retour)",
                ICON_SIZE);
        btnApply.setOnAction(e -> {
            if (applyListener != null) {
                applyListener.onApply(currentIntX(), currentIntY(), currentIntW(), currentIntH());
            }
            stage.close();
        });

        btnReset = iconButton("reset-512.png", "Réinitialiser la sélection", ICON_SIZE);
        btnReset.setOnAction(e -> {
            // Retour à la sélection d'ouverture : sans contrainte (la case est
            // décochée, la listener associée remet squareMode à false), puis
            // restauration des coordonnées initiales.
            checkSquare.setSelected(false);
            selX = initSelX;
            selY = initSelY;
            selW = initSelW;
            selH = initSelH;
            refresh();
        });

        Button btnClose = iconButton("close.png", "Fermer", ICON_SIZE);
        btnClose.setOnAction(e -> stage.close());

        HBox bottom = new HBox(btnSave, btnImageJ, btnApply, btnReset, btnClose);
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(new Insets(5, 10, 5, 10));
        // Même disposition que la fenêtre Ajuster Luminosité/Contraste : marge de 15 px
        // autour de chaque bouton, soit 30 px entre deux boutons. Le bas est nettement
        // plus dégagé (25 px) pour décoller la barre d'actions du bord de la fenêtre.
        bottom.getChildren().forEach(child -> HBox.setMargin((Button) child, new Insets(15, 15, 25, 15)));

        // ---- Barre de titre (identique aux fenêtres de fond) ----
        Label labelTitle = new Label(stage.getTitle());
        labelTitle.setMaxWidth(Double.MAX_VALUE);
        labelTitle.setFont(Font.font("Arial", 13.0));
        HBox.setHgrow(labelTitle, Priority.ALWAYS);
        HBox.setMargin(labelTitle, new Insets(0, 12, 0, 12));

        Button btnExit = new Button("✕");
        btnExit.setPrefSize(46, 36);
        btnExit.setMinSize(46, 36);
        btnExit.setMaxSize(46, 36);
        btnExit.setTextFill(Color.WHITE);
        btnExit.setFont(Font.font("Arial", 14.0));
        btnExit.setStyle(EXIT_STYLE_NORMAL);
        btnExit.setTooltip(new Tooltip("Fermer"));
        btnExit.setOnAction(e -> stage.close());
        btnExit.setOnMouseEntered(e -> btnExit.setStyle(EXIT_STYLE_HOVER));
        btnExit.setOnMouseExited(e -> btnExit.setStyle(EXIT_STYLE_NORMAL));

        HBox titleBar = new HBox(labelTitle, btnExit);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setMinHeight(36);
        titleBar.setPrefHeight(36);
        titleBar.setMaxHeight(36);
        titleBar.setStyle(TITLE_BAR_STYLE);

        // Déplacement de la fenêtre par la barre de titre.
        final double[] dragOffset = new double[2];
        titleBar.setOnMousePressed(e -> {
            dragOffset[0] = e.getSceneX();
            dragOffset[1] = e.getSceneY();
        });
        titleBar.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - dragOffset[0]);
            stage.setY(e.getScreenY() - dragOffset[1]);
        });

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: white; -fx-border-color: #9a9a9a; -fx-border-width: 1;");
        root.setTop(titleBar);
        root.setCenter(center);
        root.setBottom(bottom);

        refresh();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        // Fenêtre à taille fixe : pas d'agrandissement possible.
        stage.setResizable(false);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.showAndWait();
    }

    private Button smallBtn(String iconFile, String tip) {
        Button b = new Button();
        b.setGraphic(iconView(iconFile, SMALL_ICON_SIZE));
        b.setTooltip(new Tooltip(tip));
        return b;
    }

    private ImageView iconView(String fileName, double size) {
        ImageView iv = new ImageView(new Image(Roi_Zoom_Viewer.class.getResourceAsStream("/icons/" + fileName)));
        iv.setFitWidth(size);
        iv.setFitHeight(size);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        return iv;
    }

    private Button iconButton(String fileName, String tip, double size) {
        Button b = new Button();
        b.setGraphic(iconView(fileName, size));
        b.setTooltip(new Tooltip(tip));
        return b;
    }

    private void centerZoom(Pane pane) {
        Image img = zoomView.getImage();
        if (img == null) {
            return;
        }
        double fw = zoomView.getFitWidth();
        double fh = zoomView.getFitHeight();
        double iw = img.getWidth();
        double ih = img.getHeight();
        if (iw <= 0 || ih <= 0) {
            return;
        }
        double s = Math.min(fw / iw, fh / ih);
        double w = iw * s;
        double h = ih * s;
        zoomView.setX((pane.getPrefWidth() - w) / 2.0);
        zoomView.setY((pane.getPrefHeight() - h) / 2.0);
    }

    // ---------- Souris ----------

    private void onMousePressed(MouseEvent e) {
        canvas.requestFocus();
        pressPaneX = e.getX();
        pressPaneY = e.getY();
        pressSelX = selX;
        pressSelY = selY;
        pressSelW = selW;
        pressSelH = selH;

        DragMode hit = hitTest(pressPaneX, pressPaneY);
        if (hit != DragMode.NONE) {
            dragMode = hit;
            if (dragMode == DragMode.RESIZE_NW) {
                anchorSrcX = pressSelX + pressSelW;
                anchorSrcY = pressSelY + pressSelH;
            } else if (dragMode == DragMode.RESIZE_NE) {
                anchorSrcX = pressSelX;
                anchorSrcY = pressSelY + pressSelH;
            } else if (dragMode == DragMode.RESIZE_SW) {
                anchorSrcX = pressSelX + pressSelW;
                anchorSrcY = pressSelY;
            } else if (dragMode == DragMode.RESIZE_SE) {
                anchorSrcX = pressSelX;
                anchorSrcY = pressSelY;
            }
        } else if (insideSelection(pressPaneX, pressPaneY)) {
            dragMode = DragMode.MOVE;
        } else {
            dragMode = DragMode.CREATE;
            anchorSrcX = paneToSrcX(pressPaneX);
            anchorSrcY = paneToSrcY(pressPaneY);
            selX = anchorSrcX;
            selY = anchorSrcY;
            selW = 1;
            selH = 1;
        }
        e.consume();
    }

    private void onMouseDragged(MouseEvent e) {
        if (dragMode == DragMode.NONE) {
            return;
        }
        boolean square = squareMode || e.isShiftDown();
        if (dragMode == DragMode.MOVE) {
            double dxSrc = (e.getX() - pressPaneX) / viewScale;
            double dySrc = (e.getY() - pressPaneY) / viewScale;
            selX = pressSelX + dxSrc;
            selY = pressSelY + dySrc;
            clampPosition();
        } else if (dragMode == DragMode.CREATE) {
            double curX = paneToSrcX(e.getX());
            double curY = paneToSrcY(e.getY());
            buildFromAnchor(curX, curY, square);
        } else {
            // Redimensionnement depuis un coin, le coin opposé (ancre) reste fixe.
            double curX = paneToSrcX(e.getX());
            double curY = paneToSrcY(e.getY());
            buildFromAnchor(curX, curY, square);
        }
        refresh();
        e.consume();
    }

    private void onMouseReleased(MouseEvent e) {
        if (selW < MIN_SIZE_SRC) {
            selW = MIN_SIZE_SRC;
        }
        if (selH < MIN_SIZE_SRC) {
            selH = MIN_SIZE_SRC;
        }
        clampPosition();
        clampSize();
        dragMode = DragMode.NONE;
        refresh();
        e.consume();
    }

    private void onMouseMoved(MouseEvent e) {
        DragMode hit = hitTest(e.getX(), e.getY());
        if (hit == DragMode.RESIZE_NW || hit == DragMode.RESIZE_SE) {
            canvas.setCursor(Cursor.NW_RESIZE);
        } else if (hit == DragMode.RESIZE_NE || hit == DragMode.RESIZE_SW) {
            canvas.setCursor(Cursor.NE_RESIZE);
        } else if (insideSelection(e.getX(), e.getY())) {
            canvas.setCursor(Cursor.MOVE);
        } else {
            canvas.setCursor(Cursor.CROSSHAIR);
        }
    }

    private void onScrollResize(ScrollEvent e) {
        if (e.getDeltaY() > 0) {
            scaleSelection(1.1);
        } else if (e.getDeltaY() < 0) {
            scaleSelection(1.0 / 1.1);
        }
        e.consume();
    }

    private DragMode hitTest(double paneX, double paneY) {
        double[][] corners = displayCorners();
        // 0=NW 1=NE 2=SW 3=SE
        for (int i = 0; i < 4; i++) {
            double cx = corners[i][0];
            double cy = corners[i][1];
            if (Math.abs(paneX - cx) <= HANDLE_HIT && Math.abs(paneY - cy) <= HANDLE_HIT) {
                if (i == 0) {
                    return DragMode.RESIZE_NW;
                } else if (i == 1) {
                    return DragMode.RESIZE_NE;
                } else if (i == 2) {
                    return DragMode.RESIZE_SW;
                } else {
                    return DragMode.RESIZE_SE;
                }
            }
        }
        return DragMode.NONE;
    }

    private boolean insideSelection(double paneX, double paneY) {
        double x = selX * viewScale;
        double y = selY * viewScale;
        double w = selW * viewScale;
        double h = selH * viewScale;
        return paneX >= x && paneX <= x + w && paneY >= y && paneY <= y + h;
    }

    private double[][] displayCorners() {
        double x = selX * viewScale;
        double y = selY * viewScale;
        double w = selW * viewScale;
        double h = selH * viewScale;
        return new double[][]{{x, y}, {x + w, y}, {x, y + h}, {x + w, y + h}};
    }

    private double paneToSrcX(double paneX) {
        return paneX / viewScale;
    }

    private double paneToSrcY(double paneY) {
        return paneY / viewScale;
    }

    private void buildFromAnchor(double curX, double curY, boolean square) {
        double dx = curX - anchorSrcX;
        double dy = curY - anchorSrcY;
        if (square) {
            // Côté = plus grande des deux dimensions, mais jamais au-delà de ce qui
            // reste disponible dans l'image depuis l'ancre : le carré reste donc
            // toujours un carré valide (sinon clampSize() le redéformait).
            double side = Math.max(Math.abs(dx), Math.abs(dy));
            double roomX = dx < 0 ? anchorSrcX : source.getWidth() - anchorSrcX;
            double roomY = dy < 0 ? anchorSrcY : source.getHeight() - anchorSrcY;
            side = Math.min(side, Math.min(roomX, roomY));
            side = Math.max(1, side);
            dx = dx < 0 ? -side : side;
            dy = dy < 0 ? -side : side;
        }
        double x = anchorSrcX + Math.min(0, dx);
        double y = anchorSrcY + Math.min(0, dy);
        double w = Math.max(1, Math.abs(dx));
        double h = Math.max(1, Math.abs(dy));
        selX = x;
        selY = y;
        selW = w;
        selH = h;
        clampPosition();
        clampSize();
    }

    // ---------- Transformations ----------

    private void moveSelection(double dxSrc, double dySrc) {
        selX += dxSrc;
        selY += dySrc;
        clampPosition();
        refresh();
    }

    private void scaleSelection(double factor) {
        double cx = selX + selW / 2.0;
        double cy = selY + selH / 2.0;
        double nw = selW * factor;
        double nh = selH * factor;
        if (squareMode) {
            // Côté unique, borné pour tenir dans l'image : la figure reste carrée.
            double side = Math.max(nw, nh);
            side = Math.max(MIN_SIZE_SRC, side);
            side = Math.min(side, Math.min(source.getWidth(), source.getHeight()));
            nw = side;
            nh = side;
        }
        nw = Math.max(MIN_SIZE_SRC, nw);
        nh = Math.max(MIN_SIZE_SRC, nh);
        nw = Math.min(nw, source.getWidth());
        nh = Math.min(nh, source.getHeight());
        selW = nw;
        selH = nh;
        selX = cx - nw / 2.0;
        selY = cy - nh / 2.0;
        clampPosition();
        refresh();
    }

    /**
     * Ramène la sélection à un carré qui tient entièrement dans l'image :
     * le côté est limité au plus petit côté possible, puis la position est
     * recentrée et bornée (clampPosition ne peut plus déformer le carré).
     */
    private void applySquare() {
        double side = Math.max(selW, selH);
        side = Math.max(MIN_SIZE_SRC, side);
        side = Math.min(side, Math.min(source.getWidth(), source.getHeight()));
        double cx = selX + selW / 2.0;
        double cy = selY + selH / 2.0;
        selW = side;
        selH = side;
        selX = cx - side / 2.0;
        selY = cy - side / 2.0;
        clampPosition();
    }

    private void makeSquare() {
        applySquare();
        refresh();
    }

    private void clampPosition() {
        int w = source.getWidth();
        int h = source.getHeight();
        if (selX < 0) {
            selX = 0;
        }
        if (selY < 0) {
            selY = 0;
        }
        if (selX + selW > w) {
            selX = w - selW;
        }
        if (selY + selH > h) {
            selY = h - selH;
        }
        if (selX < 0) {
            selX = 0;
        }
        if (selY < 0) {
            selY = 0;
        }
    }

    private void clampSize() {
        int w = source.getWidth();
        int h = source.getHeight();
        if (selW > w) {
            selW = w;
        }
        if (selH > h) {
            selH = h;
        }
        if (selX + selW > w) {
            selW = w - selX;
        }
        if (selY + selH > h) {
            selH = h - selY;
        }
    }

    private int currentIntX() {
        return (int) Math.round(selX);
    }

    private int currentIntY() {
        return (int) Math.round(selY);
    }

    private int currentIntW() {
        int w = (int) Math.round(selW);
        w = Math.max(MIN_SIZE_SRC, Math.min(w, source.getWidth() - currentIntX()));
        return Math.max(1, w);
    }

    private int currentIntH() {
        int h = (int) Math.round(selH);
        h = Math.max(MIN_SIZE_SRC, Math.min(h, source.getHeight() - currentIntY()));
        return Math.max(1, h);
    }

    private int clamp(int v, int min, int max) {
        if (v < min) {
            return min;
        }
        if (v > max) {
            return max;
        }
        return v;
    }

    // ---------- Affichage ----------

    private void refresh() {
        // Invariant : tant que "Forcer un carré" est coché, la sélection reste un carré,
        // quel que soit le geste qui vient de la modifier (souris, molette, clavier, boutons).
        if (squareMode) {
            applySquare();
        }
        double x = selX * viewScale;
        double y = selY * viewScale;
        double w = Math.max(2, selW * viewScale);
        double h = Math.max(2, selH * viewScale);
        selection.setX(x);
        selection.setY(y);
        selection.setWidth(w);
        selection.setHeight(h);

        double hs = HANDLE_SIZE / 2.0;
        double[][] corners = new double[][]{{x, y}, {x + w, y}, {x, y + h}, {x + w, y + h}};
        for (int i = 0; i < 4; i++) {
            handles[i].setX(corners[i][0] - hs);
            handles[i].setY(corners[i][1] - hs);
        }

        updateZoomView();
        updateNavButtons();
        updateActionButtons();
    }

    /**
     * Appliquer et Réinitialiser sont grisés quand la sélection ressemble
     * à celle de l'ouverture (rien à appliquer ni à réinitialiser).
     */
    private void updateActionButtons() {
        if (btnApply == null || btnReset == null) {
            return;
        }
        boolean atInitial = Math.abs(selX - initSelX) < 0.5
                && Math.abs(selY - initSelY) < 0.5
                && Math.abs(selW - initSelW) < 0.5
                && Math.abs(selH - initSelH) < 0.5;
        btnApply.setDisable(atInitial);
        btnReset.setDisable(atInitial);
    }

    /**
     * Grise les boutons qui ne feraient rien : flèches vers un bord déjà
     * atteint, agrandir quand toute l'image est couverte, rétrécir quand
     * la taille minimale est atteinte.
     */
    private void updateNavButtons() {
        if (btnMoveLeft == null || btnBigger == null) {
            return;
        }
        int imgW = source.getWidth();
        int imgH = source.getHeight();
        btnMoveLeft.setDisable(selX < 0.5);
        btnMoveRight.setDisable(imgW - (selX + selW) < 0.5);
        btnMoveUp.setDisable(selY < 0.5);
        btnMoveDown.setDisable(imgH - (selY + selH) < 0.5);
        btnBigger.setDisable(imgW - selW < 0.5 && imgH - selH < 0.5);
        btnSmaller.setDisable(selW <= MIN_SIZE_SRC + 0.5 && selH <= MIN_SIZE_SRC + 0.5);
    }

    private void updateZoomView() {
        try {
            int x = currentIntX();
            int y = currentIntY();
            int w = currentIntW();
            int h = currentIntH();
            ImageProcessor proc = source.getProcessor().duplicate();
            proc.setRoi(x, y, w, h);
            ImageProcessor cropped = proc.crop();
            Image fx = SwingFXUtils.toFXImage(cropped.getBufferedImage(), null);
            zoomView.setImage(fx);
            labelInfo.setText("Sélection : X=" + x + "  Y=" + y + "  L=" + w + "  H=" + h);
            double zoomFactor = Math.min(zoomView.getFitWidth() / w, zoomView.getFitHeight() / h);
            labelZoomSize.setText(String.format("Image %d×%d — aperçu ×%.2f", w, h, zoomFactor));
        } catch (Exception ex) {
            labelInfo.setText("Sélection invalide");
        }
    }

    private ImagePlus buildCroppedPlus() {
        int x = currentIntX();
        int y = currentIntY();
        int w = currentIntW();
        int h = currentIntH();
        ImageProcessor proc = source.getProcessor().duplicate();
        proc.setRoi(x, y, w, h);
        return new ImagePlus(source.getTitle() + " [ROI " + w + "x" + h + "]", proc.crop());
    }

    // ---------- Actions ----------

    private void openSelectionInImageJ() {
        try {
            ImagePlus cropped = buildCroppedPlus();
            cropped.show();
            ImageWindow window = cropped.getWindow();
            if (window != null) {
                // Bouton "Agrandir" utilisable + fenêtre au premier plan
                // (elle s'ouvre au-dessus de cette fenêtre modale).
                window.setResizable(true);
                window.toFront();
                window.requestFocus();
                // Bouton "Agrandir" : carré double + restauration de la taille
                // ("Réduire" laissé actif, comme dans les autres fenêtres).
                Win32TitleBar.installMaximizeToggle(window);
            }
        } catch (Exception ex) {
            showAlert(AlertType.ERROR, "Impossible d'ouvrir la sélection dans ImageJ : " + ex.getMessage());
        }
    }

    private void saveSelection(Stage parent) {
        try {
            ImagePlus cropped = buildCroppedPlus();
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Enregistrer la partie sélectionnée...");
            String base = source.getTitle();
            if (base == null || base.isEmpty()) {
                base = "selection";
            }
            int dot = base.lastIndexOf('.');
            if (dot > 0) {
                base = base.substring(0, dot);
            }
            chooser.setInitialFileName(base + "_selection.png");
            chooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("PNG (*.png)", "*.png"),
                    new FileChooser.ExtensionFilter("JPEG (*.jpg)", "*.jpg", "*.jpeg"),
                    new FileChooser.ExtensionFilter("TIFF (*.tif)", "*.tif", "*.tiff"),
                    new FileChooser.ExtensionFilter("BMP (*.bmp)", "*.bmp"),
                    new FileChooser.ExtensionFilter("GIF (*.gif)", "*.gif"));
            File file = chooser.showSaveDialog(parent);
            if (file == null) {
                return;
            }
            String path = file.getAbsolutePath();
            String lower = path.toLowerCase();
            FileSaver saver = new FileSaver(cropped);
            boolean ok;
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
                ok = saver.saveAsJpeg(path);
            } else if (lower.endsWith(".tif") || lower.endsWith(".tiff")) {
                ok = saver.saveAsTiff(path);
            } else if (lower.endsWith(".bmp")) {
                ok = saver.saveAsBmp(path);
            } else if (lower.endsWith(".gif")) {
                ok = saver.saveAsGif(path);
            } else {
                if (!lower.endsWith(".png")) {
                    path += ".png";
                }
                ok = saver.saveAsPng(path);
            }
            if (ok) {
                showAlert(AlertType.INFORMATION, "Sélection enregistrée :\n" + path);
            } else {
                showAlert(AlertType.WARNING, "Enregistrement annulé ou impossible.");
            }
        } catch (Exception ex) {
            showAlert(AlertType.ERROR, "Erreur d'enregistrement : " + ex.getMessage());
        }
    }

    private void showAlert(AlertType type, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(type == AlertType.INFORMATION ? "Information" : "Avertissement");
        Alert_Util.style(alert);
        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.showAndWait();
    }
}
