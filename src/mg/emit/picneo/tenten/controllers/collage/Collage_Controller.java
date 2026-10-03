package mg.emit.picneo.tenten.controllers.collage;

import mg.emit.picneo.tenten.controllers.Tool_Dialog_Controller;
import mg.emit.picneo.tenten.util.TitleBar_Util;

import ij.ImagePlus;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javafx.animation.PauseTransition;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import mg.emit.picneo.tenten.util.Alert_Util;

/**
 * Collage mosaïque de 2 photos ou plus, sélectionnées visuellement
 * (miniatures). Plusieurs types de mosaïque, cadrage, fond,
 * espacement et format d'image. Le résultat rejoint la galerie.
 */
public class Collage_Controller implements Tool_Dialog_Controller {

    public static final String MOSAIC_UNIFORM = "Mosaïque uniforme";
    public static final String MOSAIC_STAR_LEFT = "Mosaïque vedette à gauche";
    public static final String MOSAIC_STAR_TOP = "Mosaïque vedette en haut";
    public static final String MOSAIC_STAR_RIGHT = "Mosaïque vedette à droite";

    public static final String FIT_CONTAIN = "Adapter (image entière)";
    public static final String FIT_COVER = "Remplir (recadrer)";
    public static final String FIT_STRETCH = "Étirer (déformer)";

    private static final double THUMB_W = 100.0;
    private static final double THUMB_H = 75.0;
    private static final double SEQ_W = 80.0;
    private static final double SEQ_H = 60.0;

    @FXML
    public FlowPane flow_available;

    @FXML
    public HBox box_sequence;

    @FXML
    public ComboBox<String> combo_type;

    @FXML
    public ComboBox<String> combo_fit;

    @FXML
    public ComboBox<String> combo_background;

    @FXML
    public Spinner<Integer> spinner_spacing;

    @FXML
    public TextField tf_result_name;

    @FXML
    public Label label_status;

    @FXML
    public ImageView imageView_preview;

    @FXML
    public Button button_create;

    @FXML
    public Button button_close;

    @FXML
    public Button button_exit;

    @FXML
    public Label label_title;

    @FXML
    public HBox title_bar;

    @FXML
    public Button button_up;

    @FXML
    public Button button_down;

    @FXML
    public Button button_remove;

    private ArrayList<ImagePlus> ip_array = new ArrayList<ImagePlus>();
    private final List<ImagePlus> sequence = new ArrayList<ImagePlus>();
    private int selectedSeqIndex = -1;

    /** Miniatures construites une seule fois (sinon reconversion full-res à chaque clic). */
    private final Map<ImagePlus, Image> thumbCache = new HashMap<ImagePlus, Image>();

    /** Évite un re-rendu complet à chaque micro-changement (flèches spinbox...). */
    private PauseTransition previewDebounce;

    /**
     * Position de la partie visible (0..1, 0.5 = centré) par photo,
     * pour le cadrage "Remplir" : glisser dans l'aperçu pour choisir
     * la zone conservée (ex. garder une tête qui serait coupée).
     */
    private final Map<ImagePlus, double[]> cropAnchor = new HashMap<ImagePlus, double[]>();

    private boolean cropDragActive;
    private int cropDragIndex = -1;
    private double cropDragStartFx;
    private double cropDragStartFy;
    private double cropDragStartX;
    private double cropDragStartY;

    /** Géométrie du dernier aperçu (pour convertir la souris en cases). */
    private Geometrie lastPreviewGeo;
    private List<ImagePlus> lastPreviewPhotos;

    private Consumer<ImagePlus> onResult;

    @FXML
    public void initialize() {
        combo_type.getItems().addAll(MOSAIC_UNIFORM, MOSAIC_STAR_LEFT, MOSAIC_STAR_TOP, MOSAIC_STAR_RIGHT);
        combo_type.getSelectionModel().select(MOSAIC_UNIFORM);
        combo_fit.getItems().addAll(FIT_CONTAIN, FIT_COVER, FIT_STRETCH);
        combo_fit.getSelectionModel().select(FIT_CONTAIN);
        combo_background.getItems().addAll("Blanc", "Noir");
        combo_background.getSelectionModel().select("Blanc");

        combo_type.setOnAction(event -> requestPreview());
        combo_fit.setOnAction(event -> requestPreview());
        combo_background.setOnAction(event -> requestPreview());
        spinner_spacing.valueProperty().addListener((obs, oldVal, newVal) -> requestPreview());

        previewDebounce = new PauseTransition(Duration.millis(200));
        previewDebounce.setOnFinished(event -> renderPreviewNow());

        imageView_preview.setOnMousePressed(this::previewPressed);
        imageView_preview.setOnMouseDragged(this::previewDragged);
        imageView_preview.setOnMouseReleased(this::previewReleased);
        imageView_preview.setOnMouseMoved(this::previewMoved);

        TitleBar_Util.bind(title_bar, button_exit);
    }

    private void requestPreview() {
        if (previewDebounce != null) {
            previewDebounce.playFromStart();
        } else {
            renderPreviewNow();
        }
    }

    /** Miniature mise en cache : une seule conversion par photo. */
    private Image thumbOf(ImagePlus imp) {
        Image thumb = thumbCache.get(imp);
        if (thumb == null) {
            thumb = SwingFXUtils.toFXImage(imp.getProcessor().getBufferedImage(), null);
            thumbCache.put(imp, thumb);
        }
        return thumb;
    }

    // ---------- Sélection visuelle ----------

    private void refreshAvailable() {
        flow_available.getChildren().clear();
        for (ImagePlus imp : ip_array) {
            Image fx = thumbOf(imp);
            ImageView view = new ImageView(fx);
            view.setFitWidth(THUMB_W);
            view.setFitHeight(THUMB_H);
            view.setPreserveRatio(true);
            int order = sequence.indexOf(imp);
            Label badge = new Label(order >= 0 ? String.valueOf(order + 1) : "");
            badge.setVisible(order >= 0);
            badge.setStyle("-fx-background-color: #E81123; -fx-text-fill: white; "
                    + "-fx-font-weight: bold; -fx-padding: 2 7 2 7; -fx-background-radius: 10;");
            StackPane.setAlignment(badge, Pos.TOP_RIGHT);
            StackPane cell = new StackPane(view, badge);
            cell.setPadding(new Insets(3));
            cell.setStyle(order >= 0
                    ? "-fx-border-color: #E81123; -fx-border-width: 3; -fx-background-color: white;"
                    : "-fx-border-color: #cccccc; -fx-border-width: 1; -fx-background-color: white;");
            Tooltip.install(cell, new Tooltip(imp.getTitle()));
            cell.setOnMouseClicked(event -> toggleAvailable(imp));
            flow_available.getChildren().add(cell);
        }
    }

    private void toggleAvailable(ImagePlus imp) {
        if (sequence.contains(imp)) {
            sequence.remove(imp);
            if (selectedSeqIndex >= sequence.size()) {
                selectedSeqIndex = sequence.size() - 1;
            }
        } else {
            sequence.add(imp);
            selectedSeqIndex = sequence.size() - 1;
        }
        refreshAvailable();
        refreshSequence();
        requestPreview();
    }

    private void refreshSequence() {
        box_sequence.getChildren().clear();
        for (int i = 0; i < sequence.size(); i++) {
            final int index = i;
            ImagePlus imp = sequence.get(i);
            Image fx = thumbOf(imp);
            ImageView view = new ImageView(fx);
            view.setFitWidth(SEQ_W);
            view.setFitHeight(SEQ_H);
            view.setPreserveRatio(true);
            Label num = new Label(String.valueOf(i + 1));
            num.setStyle("-fx-background-color: #333333; -fx-text-fill: white; "
                    + "-fx-font-weight: bold; -fx-padding: 1 6 1 6; -fx-background-radius: 9;");
            StackPane.setAlignment(num, Pos.TOP_LEFT);
            StackPane cell = new StackPane(view, num);
            cell.setPadding(new Insets(2));
            cell.setStyle(i == selectedSeqIndex
                    ? "-fx-border-color: #E81123; -fx-border-width: 3; -fx-background-color: white;"
                    : "-fx-border-color: #cccccc; -fx-border-width: 1; -fx-background-color: white;");
            Tooltip.install(cell, new Tooltip((i + 1) + ". " + imp.getTitle()
                    + " — clic = sélectionner, double-clic = retirer"));
            cell.setOnMouseClicked(event -> {
                if (event.getClickCount() >= 2) {
                    sequence.remove(index);
                    if (selectedSeqIndex >= sequence.size()) {
                        selectedSeqIndex = sequence.size() - 1;
                    }
                    refreshAvailable();
                    refreshSequence();
                    requestPreview();
                } else {
                    selectedSeqIndex = index;
                    refreshSequence();
                }
            });
            box_sequence.getChildren().add(cell);
        }
    }

    @FXML
    public void moveUp() {
        moveSelected(-1);
    }

    @FXML
    public void moveDown() {
        moveSelected(1);
    }

    private void moveSelected(int direction) {
        int target = selectedSeqIndex + direction;
        if (selectedSeqIndex < 0 || target < 0 || target >= sequence.size()) {
            return;
        }
        ImagePlus img = sequence.remove(selectedSeqIndex);
        sequence.add(target, img);
        selectedSeqIndex = target;
        refreshAvailable();
        refreshSequence();
        requestPreview();
    }

    @FXML
    public void removeSelected() {
        if (selectedSeqIndex < 0 || selectedSeqIndex >= sequence.size()) {
            return;
        }
        sequence.remove(selectedSeqIndex);
        if (selectedSeqIndex >= sequence.size()) {
            selectedSeqIndex = sequence.size() - 1;
        }
        refreshAvailable();
        refreshSequence();
        requestPreview();
    }

    // ---------- Création ----------

    @FXML
    public void createCollage() {
        List<ImagePlus> photos = new ArrayList<ImagePlus>(sequence);
        if (photos.size() < 2) {
            showMessage(AlertType.WARNING, "Cliquez au moins deux photos pour le collage.");
            return;
        }
        String name = tf_result_name.getText();
        if (name == null || name.trim().isEmpty()) {
            name = "Collage";
        }
        name = name.trim();

        BufferedImage rendered = renderCollage(photos);
        if (rendered == null) {
            showMessage(AlertType.ERROR, "Impossible de créer le collage.");
            return;
        }
        ImagePlus result = new ImagePlus(name, rendered);

        if (onResult != null) {
            onResult.accept(result);
        }
        close();
    }

    @FXML
    public void close() {
        Stage stage = (Stage) button_close.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void button_exit_action_event(javafx.event.ActionEvent event) {
        close();
    }

    public void setWindowTitle(String title) {
        if (label_title != null) {
            label_title.setText(title);
        }
    }


    private void renderPreviewNow() {
        if (sequence.size() < 2) {
            imageView_preview.setImage(null);
            lastPreviewGeo = null;
            lastPreviewPhotos = null;
            label_status.setText("Cliquez au moins deux photos pour le collage.");
            return;
        }
        try {
            List<ImagePlus> photos = new ArrayList<ImagePlus>(sequence);
            // Dimensions finales (calcul léger) + aperçu réduit (rendu rapide).
            int[] full = fullCanvasSize(photos);
            double scale = Math.min(1.0, 620.0 / Math.max(1, full[0]));
            BufferedImage small = renderCollage(photos, scale);
            imageView_preview.setImage(SwingFXUtils.toFXImage(small, null));
            lastPreviewPhotos = photos;
            lastPreviewGeo = computeGeometry(photos, scale);
            label_status.setText(full[0] + " x " + full[1] + " px — "
                    + photos.size() + " photo(s).");
        } catch (Exception e) {
            label_status.setText("Aperçu impossible.");
        }
    }

    // ---------- Ajustement souris du recadrage "Remplir" ----------

    /** Point souris → coordonnées du canevas d'aperçu (null si hors image). */
    private double[] displayToCanvas(MouseEvent event) {
        Image img = imageView_preview.getImage();
        if (img == null || img.getWidth() <= 0 || img.getHeight() <= 0) {
            return null;
        }
        double viewW = imageView_preview.getBoundsInLocal().getWidth();
        double viewH = imageView_preview.getBoundsInLocal().getHeight();
        if (viewW <= 0 || viewH <= 0) {
            return null;
        }
        double s = Math.min(viewW / img.getWidth(), viewH / img.getHeight());
        double dw = img.getWidth() * s;
        double dh = img.getHeight() * s;
        double ox = (viewW - dw) / 2.0;
        double oy = (viewH - dh) / 2.0;
        double cx = (event.getX() - ox) / s;
        double cy = (event.getY() - oy) / s;
        if (cx < 0 || cy < 0 || cx >= img.getWidth() || cy >= img.getHeight()) {
            return null;
        }
        return new double[]{cx, cy};
    }

    /** Case sous le point (tout cadrage), -1 si aucune. */
    private int cellAt(double[] pt) {
        if (pt == null || lastPreviewGeo == null || lastPreviewPhotos == null) {
            return -1;
        }
        int n = Math.min(lastPreviewGeo.cadres.size(), lastPreviewPhotos.size());
        for (int i = 0; i < n; i++) {
            if (lastPreviewGeo.cadres.get(i).contains(pt[0], pt[1])) {
                return i;
            }
        }
        return -1;
    }

    /** Dépassement de l'image dans sa case en mode Remplir (pour convertir le glisser). */
    private double[] coverOverflow(ImagePlus photo, Cadre cadre) {
        double sc = Math.max(cadre.w / (double) photo.getWidth(), cadre.h / (double) photo.getHeight());
        return new double[]{photo.getWidth() * sc - cadre.w, photo.getHeight() * sc - cadre.h};
    }

    /** Case ajustable sous le point (Remplir + matière à recadrer), -1 sinon. */
    private int adjustableCellAt(double[] pt) {
        if (!FIT_COVER.equals(comboValue(combo_fit))) {
            return -1;
        }
        int index = cellAt(pt);
        if (index < 0) {
            return -1;
        }
        double[] overflow = coverOverflow(lastPreviewPhotos.get(index), lastPreviewGeo.cadres.get(index));
        if (overflow[0] > 1.0 || overflow[1] > 1.0) {
            return index;
        }
        return -1;
    }

    private void previewPressed(MouseEvent event) {
        if (event.getClickCount() >= 2) {
            // Double-clic : recentre la photo de la case.
            int index = cellAt(displayToCanvas(event));
            if (index >= 0) {
                double[] anchor = anchorOf(lastPreviewPhotos.get(index));
                anchor[0] = 0.5;
                anchor[1] = 0.5;
                renderPreviewNow();
                label_status.setText("Photo recentrée : " + lastPreviewPhotos.get(index).getTitle());
            }
            event.consume();
            return;
        }
        int index = adjustableCellAt(displayToCanvas(event));
        if (index < 0) {
            return;
        }
        double[] pt = displayToCanvas(event);
        cropDragActive = true;
        cropDragIndex = index;
        double[] anchor = anchorOf(lastPreviewPhotos.get(index));
        cropDragStartFx = anchor[0];
        cropDragStartFy = anchor[1];
        cropDragStartX = pt[0];
        cropDragStartY = pt[1];
        imageView_preview.setCursor(Cursor.CLOSED_HAND);
        event.consume();
    }

    private void previewDragged(MouseEvent event) {
        if (!cropDragActive || cropDragIndex < 0 || lastPreviewPhotos == null
                || cropDragIndex >= lastPreviewPhotos.size() || lastPreviewGeo == null
                || cropDragIndex >= lastPreviewGeo.cadres.size()) {
            return;
        }
        double[] pt = displayToCanvas(event);
        if (pt == null) {
            return;
        }
        ImagePlus photo = lastPreviewPhotos.get(cropDragIndex);
        double[] overflow = coverOverflow(photo, lastPreviewGeo.cadres.get(cropDragIndex));
        double[] anchor = anchorOf(photo);
        // Manipulation directe : la photo suit le curseur.
        if (overflow[0] > 1.0) {
            anchor[0] = clamp01(cropDragStartFx - (pt[0] - cropDragStartX) / overflow[0]);
        }
        if (overflow[1] > 1.0) {
            anchor[1] = clamp01(cropDragStartFy - (pt[1] - cropDragStartY) / overflow[1]);
        }
        renderPreviewNow();
        event.consume();
    }

    private void previewReleased(MouseEvent event) {
        if (!cropDragActive) {
            return;
        }
        cropDragActive = false;
        cropDragIndex = -1;
        updatePreviewCursor();
        event.consume();
    }

    private void previewMoved(MouseEvent event) {
        if (cropDragActive) {
            return;
        }
        updatePreviewCursor(event);
    }

    private void updatePreviewCursor() {
        imageView_preview.setCursor(Cursor.DEFAULT);
    }

    private void updatePreviewCursor(MouseEvent event) {
        if (adjustableCellAt(displayToCanvas(event)) >= 0) {
            imageView_preview.setCursor(Cursor.OPEN_HAND);
        } else {
            imageView_preview.setCursor(Cursor.DEFAULT);
        }
    }

    /** Dimensions finales sans rastérisation (pour l'aperçu et le statut). */
    private int[] fullCanvasSize(List<ImagePlus> photos) {
        Geometrie geo = computeGeometry(photos, 1.0);
        return new int[]{
            geo.layout.cols * geo.unitW + geo.spacing * (geo.layout.cols + 1),
            geo.layout.rows * geo.unitH + geo.spacing * (geo.layout.rows + 1)};
    }

    // ---------- Moteur mosaïque ----------

    /** Une case en pixels (même géométrie pour le rendu et la souris). */
    static class Cadre {
        final int x;
        final int y;
        final int w;
        final int h;

        Cadre(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        boolean contains(double px, double py) {
            return px >= x && px < x + w && py >= y && py < y + h;
        }
    }

    /** Géométrie complète d'une mosaïque à une échelle donnée. */
    static class Geometrie {
        final Disposition layout;
        final int unitW;
        final int unitH;
        final int spacing;
        final List<Cadre> cadres;

        Geometrie(Disposition layout, int unitW, int unitH, int spacing, List<Cadre> cadres) {
            this.layout = layout;
            this.unitW = unitW;
            this.unitH = unitH;
            this.spacing = spacing;
            this.cadres = cadres;
        }
    }

    static List<Cadre> cellRects(Disposition layout, int uw, int uh, int spacing) {
        List<Cadre> rects = new ArrayList<Cadre>();
        for (Cellule cell : layout.cellules) {
            int x = spacing + cell.col * (uw + spacing);
            int y = spacing + cell.row * (uh + spacing);
            int w = cell.colSpan * uw + (cell.colSpan - 1) * spacing;
            int h = cell.rowSpan * uh + (cell.rowSpan - 1) * spacing;
            rects.add(new Cadre(x, y, Math.max(1, w), Math.max(1, h)));
        }
        return rects;
    }

    /** Ancre de recadrage (0..1) d'une photo, 0.5 = centré par défaut. */
    double[] anchorOf(ImagePlus imp) {
        double[] anchor = cropAnchor.get(imp);
        if (anchor == null) {
            anchor = new double[]{0.5, 0.5};
            cropAnchor.put(imp, anchor);
        }
        return anchor;
    }

    private double clamp01(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }

    /** Une case de la mosaïque, en unités de grille (avec étendues). */
    static class Cellule {
        final int col;
        final int row;
        final int colSpan;
        final int rowSpan;

        Cellule(int col, int row, int colSpan, int rowSpan) {
            this.col = col;
            this.row = row;
            this.colSpan = colSpan;
            this.rowSpan = rowSpan;
        }
    }

    /** Grille + cases pour un type de mosaïque et n photos (la 1ère = vedette). */
    static class Disposition {
        final int cols;
        final int rows;
        final List<Cellule> cellules;

        Disposition(int cols, int rows, List<Cellule> cellules) {
            this.cols = cols;
            this.rows = rows;
            this.cellules = cellules;
        }
    }

    static Disposition computeLayout(String type, int n) {
        List<Cellule> cellules = new ArrayList<Cellule>();
        if (MOSAIC_STAR_LEFT.equals(type)) {
            int rows = Math.max(1, n - 1);
            cellules.add(new Cellule(0, 0, 2, rows));
            for (int r = 0; r < n - 1; r++) {
                cellules.add(new Cellule(2, r, 1, 1));
            }
            return new Disposition(3, rows, cellules);
        }
        if (MOSAIC_STAR_RIGHT.equals(type)) {
            int rows = Math.max(1, n - 1);
            cellules.add(new Cellule(1, 0, 2, rows));
            for (int r = 0; r < n - 1; r++) {
                cellules.add(new Cellule(0, r, 1, 1));
            }
            return new Disposition(3, rows, cellules);
        }
        if (MOSAIC_STAR_TOP.equals(type)) {
            int cols = Math.max(1, n - 1);
            cellules.add(new Cellule(0, 0, cols, 2));
            for (int c = 0; c < n - 1; c++) {
                cellules.add(new Cellule(c, 2, 1, 1));
            }
            return new Disposition(cols, 3, cellules);
        }
        int cols = (int) Math.ceil(Math.sqrt(n));
        int rows = (int) Math.ceil(n / (double) cols);
        for (int i = 0; i < n; i++) {
            cellules.add(new Cellule(i % cols, i / cols, 1, 1));
        }
        return new Disposition(cols, rows, cellules);
    }

    private BufferedImage renderCollage(List<ImagePlus> photos) {
        return renderCollage(photos, 1.0);
    }

    /**
     * Géométrie (disposition + cases pixels) pour des photos à une échelle.
     * Utilisée à l'identique par le rendu et par la souris de l'aperçu.
     */
    private Geometrie computeGeometry(List<ImagePlus> photos, double scale) {
        int unitW = 1;
        int unitH = 1;
        for (ImagePlus imp : photos) {
            unitW = Math.max(unitW, imp.getWidth());
            unitH = Math.max(unitH, imp.getHeight());
        }
        Disposition layout = computeLayout(comboValue(combo_type), photos.size());
        double s = Math.max(0.05, Math.min(1.0, scale));
        int uw = Math.max(1, (int) Math.round(unitW * s));
        int uh = Math.max(1, (int) Math.round(unitH * s));
        int spacing = Math.max(0, (int) Math.round(spacingOrZero() * s));
        return new Geometrie(layout, uw, uh, spacing, cellRects(layout, uw, uh, spacing));
    }

    /**
     * Construit la mosaïque : cellules uniformes selon la disposition,
     * chaque photo cadrée selon le mode choisi. scale < 1 = aperçu rapide.
     */
    private BufferedImage renderCollage(List<ImagePlus> photos, double scale) {
        if (photos == null || photos.size() < 2) {
            return null;
        }
        List<BufferedImage> sources = new ArrayList<BufferedImage>();
        for (ImagePlus imp : photos) {
            sources.add(imp.getProcessor().getBufferedImage());
        }

        Geometrie geo = computeGeometry(photos, scale);
        Color bg = "Noir".equals(comboValue(combo_background)) ? Color.BLACK : Color.WHITE;
        String fit = comboValue(combo_fit);

        int width = geo.layout.cols * geo.unitW + geo.spacing * (geo.layout.cols + 1);
        int height = geo.layout.rows * geo.unitH + geo.spacing * (geo.layout.rows + 1);
        BufferedImage canvas = new BufferedImage(Math.max(1, width), Math.max(1, height),
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(bg);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (int i = 0; i < sources.size(); i++) {
            Cadre cadre = geo.cadres.get(i);
            double[] anchor = anchorOf(photos.get(i));
            drawFit(g, sources.get(i), cadre.x, cadre.y, cadre.w, cadre.h, fit, anchor[0], anchor[1]);
        }
        g.dispose();
        return canvas;
    }

    private String comboValue(ComboBox<String> combo) {
        try {
            String v = combo.getValue();
            return v == null ? "" : v;
        } catch (Exception e) {
            return "";
        }
    }

    /** Dessine une photo dans son cadre selon le cadrage et l'ancre (0..1) choisis. */
    private void drawFit(Graphics2D g, BufferedImage img, int x, int y, int w, int h,
                         String fit, double fx, double fy) {
        int iw = img.getWidth();
        int ih = img.getHeight();
        if (FIT_STRETCH.equals(fit)) {
            g.drawImage(img, x, y, w, h, null);
            return;
        }
        double scale;
        if (FIT_COVER.equals(fit)) {
            scale = Math.max(w / (double) iw, h / (double) ih);
        } else {
            scale = Math.min(w / (double) iw, h / (double) ih);
        }
        int dw = Math.max(1, (int) Math.round(iw * scale));
        int dh = Math.max(1, (int) Math.round(ih * scale));
        int dx;
        int dy;
        if (FIT_COVER.equals(fit)) {
            // Ancre réglable à la souris dans l'aperçu (ex. garder une tête visible).
            dx = x + (int) Math.round((w - dw) * clamp01(fx));
            dy = y + (int) Math.round((h - dh) * clamp01(fy));
        } else {
            dx = x + (w - dw) / 2;
            dy = y + (h - dh) / 2;
        }
        Shape oldClip = g.getClip();
        g.setClip(x, y, w, h);
        g.drawImage(img, dx, dy, dw, dh, null);
        g.setClip(oldClip);
    }

    private int spacingOrZero() {
        try {
            Integer v = spinner_spacing.getValue();
            if (v == null) {
                spinner_spacing.getValueFactory().setValue(0);
                return 0;
            }
            return Math.max(0, v);
        } catch (Exception e) {
            return 0;
        }
    }

    private void showMessage(AlertType type, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle("Collage");
        Alert_Util.style(alert);
        alert.showAndWait();
    }

    public void setImageArray(ArrayList<ImagePlus> array) {
        this.ip_array = array;
        sequence.clear();
        selectedSeqIndex = -1;
        thumbCache.clear();
        refreshAvailable();
        refreshSequence();
        renderPreviewNow();
    }

    public void setOnResult(Consumer<ImagePlus> onResult) {
        this.onResult = onResult;
    }
}
