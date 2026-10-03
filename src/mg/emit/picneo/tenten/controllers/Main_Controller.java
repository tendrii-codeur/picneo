package mg.emit.picneo.tenten.controllers;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.github.sarxos.webcam.Webcam;

import mg.emit.picneo.tenten.domains.Image_History;
import mg.emit.picneo.tenten.util.Camera_Capture_Dialog;
import mg.emit.picneo.tenten.util.File_Naming_Util;
import mg.emit.picneo.tenten.util.Image_Info_Util;
import mg.emit.picneo.tenten.util.Zoom_Roi_Util;
import mg.emit.picneo.tenten.controllers.analyze.Analyze_Particles_Controller;
import mg.emit.picneo.tenten.controllers.file.File_SaveAs_Controller;
import mg.emit.picneo.tenten.controllers.image.Image_BrightnessContrast_Adjust_Controller;
import mg.emit.picneo.tenten.controllers.image.Image_ConvertType_Controller;
import mg.emit.picneo.tenten.controllers.image.Image_Threshold_Controller;
import mg.emit.picneo.tenten.controllers.process.Process_Add_Noise_Controller;
import mg.emit.picneo.tenten.controllers.process.Process_Filter_Controller;
import mg.emit.picneo.tenten.controllers.process.Process_Filter_Gaussian_Blur_Controller;
import mg.emit.picneo.tenten.controllers.process.Process_Filter_Unsharp_Mask_Controller;
import mg.emit.picneo.tenten.controllers.process.Process_Shadow_Controller;
import mg.emit.picneo.tenten.controllers.collage.Collage_Controller;
import mg.emit.picneo.tenten.util.FrenchHistogramWindow;
import mg.emit.picneo.tenten.util.Alert_Util;
import mg.emit.picneo.tenten.util.Roi_Zoom_Viewer;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.HistogramWindow;
import ij.io.OpenDialog;
import ij.plugin.filter.RankFilters;
import ij.plugin.*;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollBar;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;


public class Main_Controller {

    @FXML
    public Button button_close_all;

    @FXML
    public Button button_save;

    @FXML
    public Button button_save_all;

    @FXML
    public Button button_image_undo;

    @FXML
    public Button button_image_redo;

    @FXML
    public Button button_image_restore;

    @FXML
    public Button button_image_statistics_rgb_roi;

    @FXML
    public Button button_roi_left;

    @FXML
    public Button button_roi_right;

    @FXML
    public Button button_roi_up;

    @FXML
    public Button button_roi_down;

    @FXML
    public Button button_roi_reset;

    @FXML
    public Label label_roi_x;

    @FXML
    public Label label_roi_y;

    @FXML
    public Label label_zoom_value;

    @FXML
    public MenuItem analyze_particles;

    @FXML
    public MenuItem file_menuItem_closeImage;

    @FXML
    public MenuItem file_menuItem_closeAllImage;

    @FXML
    public MenuItem file_menuItem_openImage;

    @FXML
    public MenuItem file_menuItem_saveImage;

    @FXML
    public MenuItem file_menuItem_saveAsImage;

    @FXML
    public MenuItem file_menuItem_saveAllImage;

    @FXML
    public MenuItem image_menuItem_brightness_contrast;

    @FXML
    public MenuItem image_menuItem_convert_type;

    @FXML
    public Menu image_menu_threshold;

    @FXML
    public Menu process_menu_filters;

    @FXML
    public MenuItem process_menuItem_shadow;

    @FXML
    public Menu process_menu_noise;

    @FXML
    public Menu process_menu_others;

    @FXML
    public MenuItem collage_menuItem_create;

    @FXML
    public Button button_collage;

    /** Menu + bouton collage actifs uniquement avec au moins deux images. */
    private void updateCollageButtons() {
        boolean enabled = ip_array != null && ip_array.size() >= 2;
        if (collage_menuItem_create != null) {
            collage_menuItem_create.setDisable(!enabled);
        }
        if (button_collage != null) {
            button_collage.setDisable(!enabled);
        }
    }

    @FXML
    public ImageView iv_1;

    @FXML
    public ImageView iv_2;

    @FXML
    public ImageView iv_3;

    @FXML
    public ImageView iv_4;

    @FXML
    public ImageView iv_5;

    @FXML
    public ImageView iv_6;

    @FXML
    public ImageView iv_7;

    @FXML
    public ImageView iv_8;

    @FXML
    public ImageView iv_9;

    @FXML
    public ImageView iv_current_image;

    @FXML
    public ImageView iv_current_histogram;

    @FXML
    public ImageView iv_current_roi_histogram;

    @FXML
    public ScrollBar scroll_zoom;

    @FXML
    public StackPane stack_current_image;

    @FXML
    public StackPane stack_histogram;

    @FXML
    public StackPane stack_roi_histogram;


    @FXML
    public TableColumn<String, String> tc_current_image_type;

    @FXML
    public TableColumn<String, String> tc_current_image_value;

    @FXML
    public TableView<String> tv_current_image;

    @FXML
    public Text textMessage;

    @FXML
    public TextArea ta_current_image;

    @FXML
    public VBox vbox_current_image_1;

    @FXML
    public VBox vbox_current_image_2;

    private ArrayList<ImageView> iv_array = new ArrayList<ImageView>();
    private ArrayList<ImagePlus> ip_array = new ArrayList<ImagePlus>();

    /** Historique annuler / refaire / état enregistré de chaque image ouverte. */
    private final Image_History<ImagePlus> image_history = new Image_History<ImagePlus>(this::duplicateImageState);

    private Image default_image;

    private Integer roi_x = 0;
    private Integer roi_y = 0;

    private boolean imagePanActive;
    private double imagePanStartX;
    private double imagePanStartY;
    private int imagePanStartRoiX;
    private int imagePanStartRoiY;

    /**
     * Seuil (en units de molette) à atteindre avant de changer de niveau de zoom.
     * Les molettes fines envoient de petits deltas : on les cumule pour éviter de
     * zoomer à chaque micro-mouvement.
     */
    private static final double ZOOM_WHEEL_STEP_THRESHOLD = 20.0;
    private final Zoom_Roi_Util.Wheel_Accumulator zoomWheelAccumulator = new Zoom_Roi_Util.Wheel_Accumulator();

    private ImagePlus current_image;

    private Integer position;

    @FXML
    public void initialize() {
        initialize_imageView_arrays();
        initialize_image_pan();
        initialize_responsive_image();
        initialize_zoom_listener();
        default_image = iv_1.getImage();
    }

    public void initialize_imageView_arrays() {

        iv_array.add(iv_1);
        iv_array.add(iv_2);
        iv_array.add(iv_3);
        iv_array.add(iv_4);
        iv_array.add(iv_5);
        iv_array.add(iv_6);
        iv_array.add(iv_7);
        iv_array.add(iv_8);
        iv_array.add(iv_9);
    }

    private void initialize_image_pan() {
        iv_current_image.setOnMouseEntered(e -> updateImagePanCursor());
        iv_current_image.setOnMouseExited(e -> {
            imagePanActive = false;
            iv_current_image.setCursor(Cursor.DEFAULT);
        });
        iv_current_image.setOnMousePressed(this::imagePanMousePressed);
        iv_current_image.setOnMouseDragged(this::imagePanMouseDragged);
        iv_current_image.setOnMouseReleased(this::imagePanMouseReleased);
        iv_current_image.setOnScroll(this::imageWheelZoom);
    }

    private boolean isImagePanEnabled() {
        return current_image != null && (int) Math.round(scroll_zoom.getValue()) > 1;
    }

    /**
     * L'image centrale suit la taille de la fenêtre (y compris maximisée).
     * Sans ce binding, l'ImageView gardait une taille fixe et les calculs
     * de pan/zoom basés sur ses bornes devenaient incohérents.
     */
    private void initialize_responsive_image() {
        if (stack_current_image != null) {
            iv_current_image.fitWidthProperty().bind(stack_current_image.widthProperty().subtract(20));
            iv_current_image.fitHeightProperty().bind(stack_current_image.heightProperty().subtract(20));
            iv_current_image.setPreserveRatio(true);
        }
        if (stack_histogram != null) {
            iv_current_histogram.fitWidthProperty().bind(stack_histogram.widthProperty().subtract(10));
            iv_current_histogram.fitHeightProperty().bind(stack_histogram.heightProperty().subtract(10));
            iv_current_histogram.setPreserveRatio(true);
        }
        if (stack_roi_histogram != null) {
            iv_current_roi_histogram.fitWidthProperty().bind(stack_roi_histogram.widthProperty().subtract(10));
            iv_current_roi_histogram.fitHeightProperty().bind(stack_roi_histogram.heightProperty().subtract(10));
            iv_current_roi_histogram.setPreserveRatio(true);
        }
        scroll_zoom.setMin(1);
        scroll_zoom.setMax(10);
        scroll_zoom.setUnitIncrement(1);
        scroll_zoom.setBlockIncrement(1);
    }

    /**
     * Un seul listener remplace les onMouseMoved/onMouseDragged/onMouseClicked
     * du FXML : l'ancien onMouseMoved recalculait l'histogramme à chaque pixel
     * survolé et figeait l'interface, surtout en fenêtre maximisée.
     */
    private void initialize_zoom_listener() {
        scroll_zoom.valueProperty().addListener((obs, oldVal, newVal) -> {
            int rounded = (int) Math.round(newVal.doubleValue());
            updateRoiBottomButtonsState(current_image != null && rounded > 1);
            if (current_image == null) {
                return;
            }
            if (rounded < 1) {
                rounded = 1;
            }
            if (rounded > (int) scroll_zoom.getMax()) {
                rounded = (int) scroll_zoom.getMax();
            }
            if (Math.abs(scroll_zoom.getValue() - rounded) > 0.001) {
                scroll_zoom.setValue(rounded);
                return;
            }
            clampRoiToImage();
            show_image_roi();
        });
    }

    private Window getMainWindow() {
        try {
            if (button_close_all != null && button_close_all.getScene() != null) {
                return button_close_all.getScene().getWindow();
            }
            if (iv_current_image != null && iv_current_image.getScene() != null) {
                return iv_current_image.getScene().getWindow();
            }
        } catch (Exception e) {
        }
        return null;
    }

    private Stage openModal(Parent root, String title, StageStyle style) {
        Stage stage = new Stage();
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(style);
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        Window owner = getMainWindow();
        if (owner != null) {
            stage.initOwner(owner);
        }
        stage.centerOnScreen();
        return stage;
    }

    /**
     * Charge le FXML d'une fenêtre outil, configure son contrôleur,
     * l'intitule et l'ouvre en modale. Retourne le contrôleur,
     * ou null si le chargement a échoué.
     */
    private <T extends Tool_Dialog_Controller> T openToolDialog(String fxmlPath, String title,
            Class<T> controllerType, Consumer<T> configure) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = (Parent) loader.load();

            T controller = controllerType.cast(loader.getController());
            if (configure != null) {
                configure.accept(controller);
            }

            Stage stage = openModal(root, title, StageStyle.UNDECORATED);
            controller.setWindowTitle(title);
            stage.showAndWait();
            return controller;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Copie de la tranche (coupe) courante de l'image affichée. */
    private ImagePlus currentSlice() {
        ImagePlus image = new ImagePlus();
        image.setProcessor(current_image.getImageStack().getProcessor(position));
        return image;
    }

    /** Applique une transformation sur une copie de la tranche courante, puis l'affiche. */
    private void applyToCurrentSlice(Consumer<ImageProcessor> transformation) {
        if (!requireCurrentImage()) {
            return;
        }
        ImagePlus image = currentSlice();
        transformation.accept(image.getProcessor());
        setImageProcessor(image.getProcessor());
    }

    private boolean requireCurrentImage() {
        if (current_image == null || ip_array == null || ip_array.isEmpty()) {
            Alert alert = new Alert(AlertType.INFORMATION, "Veuillez d'abord ouvrir une image !", ButtonType.OK);
            alert.setTitle("Information");
            Alert_Util.style(alert);
            Window owner = getMainWindow();
            if (owner != null) {
                alert.initOwner(owner);
            }
            alert.showAndWait();
            return false;
        }
        return true;
    }

    private Integer getCurrentIdOrNull() {
        try {
            String text = iv_current_image.getAccessibleText();
            if (text == null || text.isEmpty()) {
                return null;
            }
            int id = Integer.parseInt(text);
            if (id < 0 || id >= ip_array.size()) {
                return null;
            }
            return id;
        } catch (Exception e) {
            return null;
        }
    }

    private void updateImagePanCursor() {
        if (isImagePanEnabled()) {
            iv_current_image.setCursor(imagePanActive ? Cursor.CLOSED_HAND : Cursor.OPEN_HAND);
        } else {
            iv_current_image.setCursor(Cursor.DEFAULT);
        }
    }

    private void clampRoiToImage() {

        if (current_image == null) {
            return;
        }

        int zoom = (int) Math.round(scroll_zoom.getValue());
        int[] clamped = Zoom_Roi_Util.clampRoi(current_image.getWidth(), current_image.getHeight(),
                zoom, roi_x, roi_y);
        roi_x = clamped[0];
        roi_y = clamped[1];
    }

    /**
     * Taille réellement affichée de l'image (tient compte du preserveRatio :
     * l'ImageView peut être plus grande que l'image letterboxée). Utilisée
     * pour convertir les pixels écran en pixels source lors du pan/zoom,
     * quelle que soit la taille de la fenêtre.
     */
    private double[] getDisplayedImageSize() {
        double fitW = iv_current_image.getFitWidth();
        double fitH = iv_current_image.getFitHeight();
        if (fitW <= 0 || fitH <= 0) {
            double bw = iv_current_image.getBoundsInLocal().getWidth();
            double bh = iv_current_image.getBoundsInLocal().getHeight();
            return new double[]{Math.max(1, bw), Math.max(1, bh)};
        }
        javafx.scene.image.Image fx = iv_current_image.getImage();
        if (fx == null || fx.getWidth() <= 0 || fx.getHeight() <= 0) {
            return new double[]{Math.max(1, fitW), Math.max(1, fitH)};
        }
        double scale = Math.min(fitW / fx.getWidth(), fitH / fx.getHeight());
        return new double[]{Math.max(1, fx.getWidth() * scale), Math.max(1, fx.getHeight() * scale)};
    }

    private void imagePanMousePressed(MouseEvent event) {

        if (!isImagePanEnabled()) {
            return;
        }

        imagePanActive = true;
        imagePanStartX = event.getX();
        imagePanStartY = event.getY();
        imagePanStartRoiX = roi_x;
        imagePanStartRoiY = roi_y;
        iv_current_image.setCursor(Cursor.CLOSED_HAND);
        event.consume();
    }

    private void imagePanMouseDragged(MouseEvent event) {

        if (!imagePanActive || !isImagePanEnabled()) {
            return;
        }

        int valueZoom = (int) Math.round(scroll_zoom.getValue());
        double[] display = getDisplayedImageSize();
        double displayW = display[0];
        double displayH = display[1];

        if (displayW <= 0 || displayH <= 0) {
            return;
        }

        double scaleX = (current_image.getWidth() / (double) valueZoom) / displayW;
        double scaleY = (current_image.getHeight() / (double) valueZoom) / displayH;

        roi_x = imagePanStartRoiX - (int) Math.round((event.getX() - imagePanStartX) * scaleX);
        roi_y = imagePanStartRoiY - (int) Math.round((event.getY() - imagePanStartY) * scaleY);
        clampRoiToImage();
        show_image_roi();
        event.consume();
    }

    private void imagePanMouseReleased(MouseEvent event) {

        if (!imagePanActive) {
            return;
        }

        imagePanActive = false;
        updateImagePanCursor();
        event.consume();
    }

    /**
     * Agrandit ou rétrécit l'image avec la molette de la souris, en gardant le
     * point de l'image situé sous le curseur au même endroit à l'écran.
     */
    private void imageWheelZoom(ScrollEvent event) {

        if (current_image == null) {
            return;
        }

        double delta = event.getDeltaY();

        int direction = zoomWheelAccumulator.add(delta, ZOOM_WHEEL_STEP_THRESHOLD);
        if (direction == 0) {
            event.consume();
            return;
        }

        int valueZoom = (int) Math.round(scroll_zoom.getValue());
        int newZoom = valueZoom + direction;

        if (newZoom < 1 || newZoom > (int) scroll_zoom.getMax()) {
            event.consume();
            return;
        }

        double[] display = getDisplayedImageSize();
        double displayW = display[0];
        double displayH = display[1];

        if (displayW <= 0 || displayH <= 0) {
            event.consume();
            return;
        }

        // Fenêtre affichée en pixels source, avant et après le changement de zoom.
        double cropW = current_image.getWidth() / valueZoom;
        double cropH = current_image.getHeight() / valueZoom;
        double newCropW = current_image.getWidth() / newZoom;
        double newCropH = current_image.getHeight() / newZoom;

        // On recale la fenêtre à afficher pour que ce même point reste sous le curseur.
        int[] anchored = Zoom_Roi_Util.keepPointUnderCursor(roi_x, roi_y,
                event.getX() / displayW, event.getY() / displayH,
                cropW, cropH, newCropW, newCropH);
        roi_x = anchored[0];
        roi_y = anchored[1];

        scroll_zoom.setValue(newZoom);
        clampRoiToImage();
        show_image_roi();

        event.consume();
    }

    @FXML
    public void main_image_statistics_rgb_roi() {

        if (!requireCurrentImage()) {
            return;
        }
        ImagePlus image = currentSlice();
        ImageProcessor cropped = image.getProcessor();

        int value_zoom = Math.max(1, (int) Math.round(scroll_zoom.getValue()));

        // Sans zoom, le clamp remet la ROI à l'origine : elle couvre toute l'image.
        clampRoiToImage();
        cropped.setRoi(roi_x, roi_y,
                Math.max(1, image.getWidth() / value_zoom), Math.max(1, image.getHeight() / value_zoom));

        image.setProcessor(cropped.crop());

        openToolDialog("/mg/emit/picneo/tenten/fxmls/Main_Image_Statistics_RGB_Layout.fxml",
                "Statistiques RGB de la ROI pour " + current_image.getTitle(),
                Main_Image_Statistics_RGB_Controller.class,
                controller -> controller.setImage(image.duplicate()));
    }

    @FXML
    public void analyze_particles() {

        if (!requireCurrentImage()) {
            return;
        }
        ImagePlus image = new ImagePlus();
        image.setProcessor(current_image.getImageStack().getProcessor(position));

        ImageStatistics stats = image.getStatistics();

        if (stats.histogram[0] + stats.histogram[255] != stats.pixelCount) {
            analyze_particles_alert();
        } else {
            analyze_particles_controller();
        }
    }

    private void analyze_particles_alert() {

        Alert alert = new Alert(AlertType.WARNING, "Une image seuillée est requise !", ButtonType.OK);
        alert.setTitle("Avertissement !");
        Alert_Util.style(alert);
        alert.showAndWait();
    }

    private void analyze_particles_controller() {

        openToolDialog("/mg/emit/picneo/tenten/fxmls/analyze/Analyze_Particles_Layout.fxml",
                "Analyse des particules pour " + current_image.getTitle(),
                Analyze_Particles_Controller.class,
                controller -> controller.setImage(currentSlice().duplicate()));
    }

    @FXML
    public void file_closeImage() {

        Integer id = getCurrentIdOrNull();
        if (id == null) {
            requireCurrentImage();
            return;
        }
        if (!image_history.isDirty(id)) {
            // Image non modifiée : simple confirmation de fermeture.
            Alert confirm = new Alert(AlertType.CONFIRMATION, "Voulez-vous vraiment fermer l'image actuelle ?",
                    ButtonType.NO, ButtonType.YES);
            Alert_Util.style(confirm);
            confirm.showAndWait().ifPresent(type -> {
                if (type == ButtonType.YES) {
                    String image_title = ip_array.get(id).getTitle();
                    file_closeImage_sortImages(id);
                    file_closeSuccessfullMessage(image_title);
                }
            });
            return;
        }
        Alert alert = new Alert(AlertType.CONFIRMATION, "Voulez-vous enregistrer avant de fermer ?",
                ButtonType.NO, ButtonType.OK, ButtonType.CANCEL);

        Alert_Util.style(alert);

        alert.showAndWait().ifPresent(type -> {

            if (type == ButtonType.OK || type == ButtonType.NO) {

                String image_title = ip_array.get(id).getTitle();

                if (type == ButtonType.OK) {
                    file_closeImage_save(id);
                }

                file_closeImage_sortImages(id);
                file_closeSuccessfullMessage(image_title);
            }
        });
    }

    private void file_closeImage_save(Integer id) {

        String savedName = saveImageAsNumberedCopy(ip_array.get(id));

        Alert alert = new Alert(AlertType.INFORMATION, "L'image a été enregistrée sous " + savedName + " !",
                ButtonType.OK);

        Alert_Util.style(alert);
        alert.showAndWait();
    }

    private void file_closeImage_sortImages(Integer id) {

        Tooltip.uninstall(iv_array.get(id), new Tooltip(ip_array.get(id).getTitle()));
        iv_array.get(id).setImage(default_image);

        ip_array.remove(ip_array.get(id));
        image_history.remove(id.intValue());

        updateCollageButtons();

        for (int i = 0; i < iv_array.size(); i++) {
            iv_array.get(i).setImage(default_image);
        }

        for (int i = 0; i < ip_array.size(); i++) {

            Image image_preview_fx = SwingFXUtils.toFXImage(ip_array.get(i).getProcessor().getBufferedImage(), null);

            iv_array.get(i).setImage(image_preview_fx);
            Tooltip.install(iv_array.get(i), new Tooltip(ip_array.get(i).getTitle()));
        }

        if (ip_array.isEmpty()) {
            onImageClosed();
        } else {
            // Il reste des images importées : "Tout fermer" (et le reste)
            // doit rester actif, on affiche la première restante.
            setCurrentImage(0);
        }
    }

    @FXML
    public void file_closeAllImage() {

        if (!hasUnsavedChanges()) {
            // Aucune modification : simple confirmation de fermeture.
            Alert confirm = new Alert(AlertType.CONFIRMATION, "Voulez-vous vraiment fermer toutes les images ?",
                    ButtonType.NO, ButtonType.YES);
            Alert_Util.style(confirm);
            confirm.showAndWait().ifPresent(type -> {
                if (type == ButtonType.YES) {
                    file_closeAllNow();
                }
            });
            return;
        }

        Alert alert = new Alert(AlertType.CONFIRMATION, "Voulez-vous enregistrer avant de fermer toutes les images ?",
                ButtonType.NO, ButtonType.OK, ButtonType.CANCEL);

        Alert_Util.style(alert);
        alert.showAndWait().ifPresent(type -> {

            if (type == ButtonType.OK || type == ButtonType.NO) {

                if (type == ButtonType.OK) {
                    file_closeAllImage_saveAll();
                }

                file_closeAllNow();
            }
        });
    }

    private void file_closeAllNow() {

        for (int i = 0; i < ip_array.size(); i++) {
            Tooltip.uninstall(iv_array.get(i), new Tooltip(ip_array.get(i).getTitle()));
            iv_array.get(i).setImage(default_image);
        }

        ip_array.clear();
        image_history.clear();
        updateCollageButtons();
        onImageClosed();
    }

    private void file_closeAllImage_saveAll() {

        for (int i = 0; i < ip_array.size(); i++) {

            Tooltip.uninstall(iv_array.get(i), new Tooltip(ip_array.get(i).getTitle()));
            iv_array.get(i).setImage(default_image);

            saveImageAsNumberedCopy(ip_array.get(i));
        }

        Alert alert_save = new Alert(AlertType.INFORMATION, "Toutes les images ont été enregistrées !",
                ButtonType.OK);

        Alert_Util.style(alert_save);
        alert_save.showAndWait();
    }

    @FXML
    public void file_openImage() throws InvocationTargetException {

        try {
            List<File> files = chooseImageFiles();
            if (files == null || files.isEmpty()) {
                return;
            }

            for (File file : files) {
                if (ip_array.size() >= 9) {
                    Alert alert = new Alert(AlertType.WARNING, "Capacité maximale atteinte ! Veuillez fermer d'autres images !", ButtonType.OK);
                    alert.setTitle("Avertissement !");
                    Alert_Util.style(alert);
                    alert.showAndWait();
                    break;
                }

                ImagePlus opened = IJ.openImage(file.getAbsolutePath());
                if (opened == null) {
                    continue;
                }
                current_image = opened;
                file_openImage_check(current_image);
            }
        } catch (NullPointerException e) {
        }
    }

    /**
     * Demande le fichier de destination (dossier + nom) pour les boutons
     * Enregistrer / Tout enregistrer.
     */
    private File chooseSaveFile(String title, String initialFileName) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        if (initialFileName != null && !initialFileName.isEmpty()) {
            chooser.setInitialFileName(initialFileName);
        }
        FileChooser.ExtensionFilter pngFilter = new FileChooser.ExtensionFilter("PNG (*.png)", "*.png");
        FileChooser.ExtensionFilter jpegFilter = new FileChooser.ExtensionFilter("JPEG (*.jpg)", "*.jpg", "*.jpeg");
        FileChooser.ExtensionFilter tiffFilter = new FileChooser.ExtensionFilter("TIFF (*.tif)", "*.tif", "*.tiff");
        FileChooser.ExtensionFilter bmpFilter = new FileChooser.ExtensionFilter("BMP (*.bmp)", "*.bmp");
        FileChooser.ExtensionFilter gifFilter = new FileChooser.ExtensionFilter("GIF (*.gif)", "*.gif");
        chooser.getExtensionFilters().addAll(pngFilter, jpegFilter, tiffFilter, bmpFilter, gifFilter);
        selectExtensionFilter(chooser, initialFileName,
                jpegFilter, pngFilter, tiffFilter, bmpFilter, gifFilter);
        Window owner = getMainWindow();
        if (owner == null) {
            return null;
        }
        File selected = chooser.showSaveDialog(owner);
        if (selected == null) {
            return null;
        }
        // Si l'utilisateur a retiré l'extension, on complète avec celle du
        // filtre choisi (JPEG pour une capture caméra) : jamais avec .tif.
        if (File_Naming_Util.extension(selected.getName()).isEmpty()) {
            String extension = firstExtensionPattern(chooser.getSelectedExtensionFilter());
            if (!extension.isEmpty()) {
                selected = new File(selected.getParentFile(), selected.getName() + extension);
            }
        }
        return selected;
    }

    /** « *.jpg » du filtre sélectionné, transformé en « .jpg » ("" si absent). */
    private String firstExtensionPattern(FileChooser.ExtensionFilter filter) {
        if (filter == null || filter.getExtensions() == null || filter.getExtensions().isEmpty()) {
            return "";
        }
        String pattern = filter.getExtensions().get(0);
        if (pattern == null || !pattern.startsWith("*.")) {
            return "";
        }
        return pattern.substring(1);
    }

    /**
     * Nom de fichier proposé pour une image : son titre, avec une extension
     * par défaut (.tif, .jpg pour une capture caméra...) si le titre n'en a pas.
     */
    private String suggestedFileName(ImagePlus image) {
        String title = image.getTitle();
        if (title == null || title.isEmpty()) {
            title = "image";
        }
        return ensureExtensionName(title);
    }

    /** Ajoute l'extension par défaut (.tif) si le nom n'en a pas. */
    private String ensureExtensionName(String fileName) {
        if (File_Naming_Util.extension(fileName).isEmpty()) {
            return fileName + ".tif";
        }
        return fileName;
    }

    /**
     * Sélectionne dans la boîte « Enregistrer sous... » le filtre d'extension
     * correspondant au nom proposé : une capture caméra (« Caméra ....jpg »)
     * s'ouvre donc directement sur JPEG au lieu de PNG.
     */
    private void selectExtensionFilter(FileChooser chooser, String fileName,
            FileChooser.ExtensionFilter... filters) {
        if (fileName == null) {
            return;
        }
        String extension = File_Naming_Util.extension(fileName).toLowerCase();
        if (extension.isEmpty()) {
            return;
        }
        for (FileChooser.ExtensionFilter filter : filters) {
            for (String pattern : filter.getExtensions()) {
                if (pattern.replace("*", "").equalsIgnoreCase(extension)) {
                    chooser.setSelectedExtensionFilter(filter);
                    return;
                }
            }
        }
    }

    private List<File> chooseImageFiles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Ouvrir une ou plusieurs images...");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images",
                        "*.tif", "*.tiff", "*.jpg", "*.jpeg", "*.png", "*.gif",
                        "*.bmp", "*.pgm", "*.fits", "*.fit", "*.fts", "*.zip", "*.raw"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*"));

        String lastDirectory = OpenDialog.getLastDirectory();
        if (lastDirectory != null) {
            File directory = new File(lastDirectory);
            if (directory.isDirectory()) {
                fileChooser.setInitialDirectory(directory);
            }
        }

        Window owner = getMainWindow();
        if (owner == null) {
            return null;
        }
        List<File> files = fileChooser.showOpenMultipleDialog(owner);
        if (files != null && !files.isEmpty() && files.get(0).getParent() != null) {
            OpenDialog.setLastDirectory(files.get(0).getParent() + File.separator);
        }
        return files;
    }

    /**
     * Bouton Caméra : capture une image via la caméra de l'ordinateur, à sa
     * résolution native réelle, et l'importe en JPEG (.jpg) comme une image
     * ouverte classiquement.
     */
    @FXML
    public void file_captureCamera() {

        Webcam webcam = null;
        try {
            List<Webcam> webcams = Webcam.getWebcams();
            if (webcams == null || webcams.isEmpty()) {
                cameraMessage("Aucune caméra n'a été détectée sur cet ordinateur.");
                return;
            }

            webcam = webcams.get(0);
            useNativeResolution(webcam);
            if (!webcam.open()) {
                webcam = null;
                cameraMessage("Impossible d'ouvrir la caméra.");
                return;
            }

            BufferedImage captured = Camera_Capture_Dialog.show(getMainWindow(), webcam);
            if (captured == null) {
                return;
            }

            if (ip_array.size() >= 9) {
                Alert alert = new Alert(AlertType.WARNING, "Capacité maximale atteinte ! Veuillez fermer d'autres images !", ButtonType.OK);
                alert.setTitle("Avertissement !");
                Alert_Util.style(alert);
                alert.showAndWait();
                return;
            }

            String title = "Caméra " + new java.text.SimpleDateFormat("yyyyMMdd-HHmmss").format(new java.util.Date()) + ".jpg";
            int countBefore = ip_array.size();
            current_image = new ImagePlus(title, new ColorProcessor(captured));
            file_openImage_check(current_image);

            // Capture sans fichier source : considérée comme à enregistrer,
            // ce qui active « Enregistrer » et « Tout enregistrer ».
            if (ip_array.size() > countBefore) {
                image_history.markUnsaved(ip_array.size() - 1);
                updateSaveButtons();
            }
        } catch (Throwable t) {
            t.printStackTrace();
            cameraMessage("Erreur lors de l'accès à la caméra : " + t);
        } finally {
            if (webcam != null) {
                try {
                    webcam.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Demande à la caméra sa résolution native maximale : la photo importée
     * a alors exactement la taille réelle capturée par l'appareil, sans
     * réduction ni recadrage. Doit être appelée avant l'ouverture de la caméra.
     */
    private void useNativeResolution(Webcam webcam) {
        try {
            Dimension nativeSize = null;
            Dimension[] sizes = webcam.getViewSizes();
            if (sizes != null) {
                for (Dimension size : sizes) {
                    if (size == null) {
                        continue;
                    }
                    if (nativeSize == null
                            || size.getWidth() * size.getHeight()
                            > nativeSize.getWidth() * nativeSize.getHeight()) {
                        nativeSize = size;
                    }
                }
            }
            if (nativeSize != null) {
                webcam.setViewSize(nativeSize);
            }
        } catch (RuntimeException ex) {
            // Résolution refusée par le pilote : on garde celle par défaut.
            ex.printStackTrace();
        }
    }

    private void cameraMessage(String text) {
        Alert alert = new Alert(AlertType.INFORMATION, text, ButtonType.OK);
        alert.setTitle("Caméra");
        Alert_Util.style(alert);
        Window owner = getMainWindow();
        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.showAndWait();
    }

    public void file_openImage_check(
            ImagePlus image) {

        boolean same_image_check = false;

        for (int i = 0; i < ip_array.size(); i++) {

            if (image.getTitle().equals(ip_array.get(i).getTitle())) {

                Alert alert = new Alert(AlertType.WARNING, "Vous avez déjà ouvert la même image !", ButtonType.OK);
                alert.setTitle("Avertissement ImageJ !");
                Alert_Util.style(alert);
                alert.showAndWait();

                same_image_check = true;
                break;
            }
        }

        if (!same_image_check) {

            Image image_preview_fx = SwingFXUtils.toFXImage(image.getProcessor().getBufferedImage(), null);

            ip_array.add(image);
            image_history.add(image);

            iv_array.get(ip_array.size() - 1).setImage(image_preview_fx);
            Tooltip.install(iv_array.get(ip_array.size() - 1), new Tooltip(image.getTitle().toString()));

            updateCollageButtons();

            updateCloseAllState();

            file_openSuccessfullMessage(image.getTitle());

        }
    }

    @FXML
    public void file_save() {

        if (!requireCurrentImage()) {
            return;
        }
        Integer id = getCurrentIdOrNull();
        if (id == null) {
            return;
        }
        if (!image_history.isDirty(id)) {
            return;
        }

        File destination = chooseSaveFile("Enregistrer l'image sous...", suggestedFileName(ip_array.get(id)));
        if (destination == null) {
            return;
        }

        String savedName = saveImageToFile(ip_array.get(id), destination);

        setCurrentImage(id);
        image_history.markSaved(id);
        updateSaveButtons();

        Alert alert = new Alert(AlertType.INFORMATION, "L'image a été enregistrée sous " + savedName + " !",
                ButtonType.OK);

        Alert_Util.style(alert);
        alert.showAndWait();
    }

    @FXML
    public void file_saveAll() {

        if (!requireCurrentImage()) {
            return;
        }

        File destination = chooseSaveFile("Enregistrer toutes les images dans...", suggestedFileName(ip_array.get(0)));
        if (destination == null) {
            return;
        }

        File directory = destination.getParentFile();
        String fileName = ensureExtensionName(destination.getName());

        for (int i = 0; i < ip_array.size(); i++) {
            saveImageAsNumberedCopy(ip_array.get(i), directory, fileName);
            image_history.markSaved(i);
        }

        Integer id = getCurrentIdOrNull();
        if (id != null) {
            setCurrentImage(id);
        }
        updateSaveButtons();

        Alert alert = new Alert(AlertType.INFORMATION, "Toutes les images ont été enregistrées !",
                ButtonType.OK);

        Alert_Util.style(alert);
        alert.showAndWait();
    }

    /** Enregistre l'image sous le nom de fichier exactement choisi par l'utilisateur. */
    private String saveImageToFile(ImagePlus image, File destination) {
        File target = new File(destination.getParentFile(), ensureExtensionName(destination.getName()));
        IJ.save(image, target.getAbsolutePath());
        return target.getName();
    }

    /**
     * « Tout enregistrer » : copie numérotée sous le nom de base choisi
     * (« vacances.png » devient « vacances(1).png », « vacances(2).png »...).
     */
    private String saveImageAsNumberedCopy(ImagePlus image, File directory, String fileName) {
        File copy = File_Naming_Util.firstFreeNumberedCopy(directory.getAbsolutePath(), fileName, null);
        IJ.save(image, copy.getAbsolutePath());
        return copy.getName();
    }

    /**
     * Enregistrement automatique (fermeture d'image) : copie numérotée
     * dans le dossier d'origine, ou dans le dossier personnel sans fichier source.
     */
    private String saveImageAsNumberedCopy(ImagePlus image) {
        File copy = getUniqueNumberedCopyFile(image);
        IJ.save(image, copy.getAbsolutePath());
        return copy.getName();
    }

    private File getUniqueNumberedCopyFile(ImagePlus image) {
        File source = getOriginalSourceFile(image);
        String directory;
        String fileName;

        if (source != null) {
            directory = source.getParent();
            fileName = source.getName();
        } else {
            directory = System.getProperty("user.home");
            fileName = suggestedFileName(image);
        }

        if (directory == null) {
            directory = System.getProperty("user.home");
        }

        File originalAbsolute = source != null ? source.getAbsoluteFile() : null;
        return File_Naming_Util.firstFreeNumberedCopy(directory, fileName, originalAbsolute);
    }

    private File getOriginalSourceFile(ImagePlus image) {
        if (image.getOriginalFileInfo() == null
                || image.getOriginalFileInfo().directory == null
                || image.getTitle() == null) {
            return null;
        }
        return new File(image.getOriginalFileInfo().directory, image.getTitle());
    }

    @FXML
    public void file_saveAs() {

        if (!requireCurrentImage()) {
            return;
        }
        File_SaveAs_Controller controller = openToolDialog(
                "/mg/emit/picneo/tenten/fxmls/file/File_SaveAs_Layout.fxml",
                "Option d'enregistrement pour " + current_image.getTitle(),
                File_SaveAs_Controller.class,
                c -> c.setImage(current_image.duplicate()));

        if (controller == null) {
            return;
        }

        Integer id = getCurrentIdOrNull();
        if (id == null) {
            return;
        }

        setCurrentImage(id);
    }

    @FXML
    public void file_quit() {

        Alert alert = new Alert(AlertType.WARNING, "Voulez-vous fermer le programme ?\nToutes les modifications non enregistrées seront perdues !",
                ButtonType.NO, ButtonType.YES);
        alert.setWidth(600);
        alert.setTitle("Avertissement !");
        Alert_Util.style(alert);
        alert.showAndWait().ifPresent(type -> {

            if (type == ButtonType.YES) {
                Platform.exit();

            }
        });
    }

    @FXML
    public void image_adjustBrightnessContrast() {

        if (!requireCurrentImage()) {
            return;
        }
        Image_BrightnessContrast_Adjust_Controller controller = openToolDialog(
                "/mg/emit/picneo/tenten/fxmls/image/Image_BrightnessContrast_Adjust_Layout.fxml",
                "Ajuster Luminosité/Contraste pour " + current_image.getTitle(),
                Image_BrightnessContrast_Adjust_Controller.class,
                c -> {
                    ImagePlus image = currentSlice();
                    c.setImage(image.getProcessor().getMin(), image.getProcessor().getMax(), image.duplicate());
                });

        if (controller != null && !controller.getStageClosedOnExit()) {
            setImageProcessor(controller.getImageProcessor());
        }
    }

    @FXML
    public void image_ConvertType() {

        if (!requireCurrentImage()) {
            return;
        }
        Image_ConvertType_Controller controller = openToolDialog(
                "/mg/emit/picneo/tenten/fxmls/image/Image_ConvertType_Layout.fxml",
                "Convertir le type d'image pour " + current_image.getTitle(),
                Image_ConvertType_Controller.class,
                c -> c.setImage(current_image.duplicate()));

        if (controller == null || controller.getStageClosedOnExit()) {
            return;
        }

        setImagePlus(controller.getImagePlus());

        Integer id = getCurrentIdOrNull();
        if (id == null) {
            return;
        }

        setCurrentImage(id);
    }

    @FXML
    public void image_threshold_adjust() {

        if (!requireCurrentImage()) {
            return;
        }
        if (current_image.getType() != ImagePlus.GRAY8) {
            image_threshold_alert();
        } else {
            image_threshold_controller();
        }
    }

    private void image_threshold_controller() {

        Image_Threshold_Controller controller = openToolDialog(
                "/mg/emit/picneo/tenten/fxmls/image/Image_Threshold_Layout.fxml",
                "Seuillage pour " + current_image.getTitle(),
                Image_Threshold_Controller.class,
                c -> c.setImage(currentSlice().duplicate()));

        if (controller != null && !controller.getStageClosedOnExit()) {
            setImageProcessor(controller.getImageProcessor());
        }
    }

    private void image_threshold_alert() {

        Alert alert = new Alert(AlertType.WARNING, "Une image 8-bit est requise !", ButtonType.OK);
        alert.setTitle("Avertissement de seuillage !");
        Alert_Util.style(alert);
        alert.showAndWait();
    }

    @FXML
    public void process_find_edges() {

        applyToCurrentSlice(ImageProcessor::findEdges);
    }

    @FXML
    public void process_invert() {

        applyToCurrentSlice(ImageProcessor::invert);
    }

    @FXML
    public void process_filter() {

        if (!requireCurrentImage()) {
            return;
        }

        Process_Filter_Controller controller = openToolDialog(
                "/mg/emit/picneo/tenten/fxmls/process/Process_Filter_Layout.fxml",
                "Aperçu du filtre pour " + current_image.getTitle(),
                Process_Filter_Controller.class,
                c -> c.setImage(currentSlice().duplicate()));

        if (controller != null && !controller.getStageClosedOnExit()) {
            setImageProcessor(controller.getImageProcessor());
        }
    }

    @FXML
    public void process_filter_gaussian_blur() {

        if (!requireCurrentImage()) {
            return;
        }

        Process_Filter_Gaussian_Blur_Controller controller = openToolDialog(
                "/mg/emit/picneo/tenten/fxmls/process/Process_Filter_Gaussian_Blur_Layout.fxml",
                "Aperçu du flou gaussien pour " + current_image.getTitle(),
                Process_Filter_Gaussian_Blur_Controller.class,
                c -> c.setImage(currentSlice().duplicate()));

        if (controller != null && !controller.getStageClosedOnExit()) {
            setImageProcessor(controller.getImageProcessor());
        }
    }

    @FXML
    public void process_filter_unsharp_mask() {

        if (!requireCurrentImage()) {
            return;
        }

        Process_Filter_Unsharp_Mask_Controller controller = openToolDialog(
                "/mg/emit/picneo/tenten/fxmls/process/Process_Filter_Unsharp_Mask_Layout.fxml",
                "Aperçu du masque flou pour " + current_image.getTitle(),
                Process_Filter_Unsharp_Mask_Controller.class,
                c -> c.setImage(currentSlice().duplicate()));

        if (controller != null && !controller.getStageClosedOnExit()) {
            setImageProcessor(controller.getImageProcessor());
        }
    }

    @FXML
    public void process_shadow() {

        if (!requireCurrentImage()) {
            return;
        }

        Process_Shadow_Controller controller = openToolDialog(
                "/mg/emit/picneo/tenten/fxmls/process/Process_Shadow_Layout.fxml",
                "Aperçu de l'ombrage pour " + current_image.getTitle(),
                Process_Shadow_Controller.class,
                c -> c.setImage(currentSlice().duplicate()));

        if (controller != null && !controller.getStageClosedOnExit()) {
            setImageProcessor(controller.getImageProcessor());
        }
    }

    @FXML
    public void process_sharpen() {

        applyToCurrentSlice(ImageProcessor::sharpen);
    }

    @FXML
    public void process_smooth() {

        applyToCurrentSlice(ImageProcessor::smooth);
    }

    @FXML
    public void process_noise_add() {

        if (!requireCurrentImage()) {
            return;
        }

        Process_Add_Noise_Controller controller = openToolDialog(
                "/mg/emit/picneo/tenten/fxmls/process/Process_Add_Noise_Layout.fxml",
                "Aperçu de l'ajout de bruit pour " + current_image.getTitle(),
                Process_Add_Noise_Controller.class,
                c -> c.setImage(current_image.duplicate()));

        if (controller != null && !controller.getStageClosedOnExit()) {
            setImageProcessor(controller.getImageProcessor());
        }
    }

    @FXML
    public void process_noise_despekle() {

        applyToCurrentSlice(ip -> {
            RankFilters filter = new RankFilters();
            filter.rank(ip, 1.0, RankFilters.MEDIAN);
        });
    }

    @FXML
    public void reset_image_roi() {

        if (!requireCurrentImage()) {
            return;
        }
        scroll_zoom.setValue(1);
        label_zoom_value.setText(String.valueOf((int) scroll_zoom.getValue()));

        show_image_roi();
    }

    @FXML
    public void show_image_roi_left() {

        if (!requireCurrentImage()) {
            return;
        }
        roi_x -= 5;
        clampRoiToImage();
        show_image_roi();
    }

    @FXML
    public void show_image_roi_right() {

        if (!requireCurrentImage()) {
            return;
        }
        roi_x += 5;
        clampRoiToImage();
        show_image_roi();
    }

    @FXML
    public void show_image_roi_up() {

        if (!requireCurrentImage()) {
            return;
        }
        roi_y -= 5;
        clampRoiToImage();
        show_image_roi();
    }

    @FXML
    public void show_image_roi_down() {

        if (!requireCurrentImage()) {
            return;
        }
        roi_y += 5;
        clampRoiToImage();
        show_image_roi();
    }

    @FXML
    public void show_image_roi() {

        if (current_image == null) {
            return;
        }

        if (position == null || position < 1 || position > current_image.getImageStackSize()) {
            position = 1;
        }

        ImagePlus image = new ImagePlus();
        image.setProcessor(current_image.getImageStack().getProcessor(position));

        ImageProcessor cropped = image.getProcessor();

        int value_zoom = Math.max(1, (int) Math.round(scroll_zoom.getValue()));
        if (Math.abs(scroll_zoom.getValue() - value_zoom) > 0.001) {
            scroll_zoom.setValue(value_zoom);
            return;
        }
        label_zoom_value.setText(String.valueOf(value_zoom));

        clampRoiToImage();

        if (value_zoom > 1) {
            int cropW = Math.max(1, image.getWidth() / value_zoom);
            int cropH = Math.max(1, image.getHeight() / value_zoom);
            cropped.setRoi(roi_x, roi_y, cropW, cropH);

            image.setProcessor(cropped.crop());
            showRoiHistogram(image);

            Image image_preview_fx = SwingFXUtils.toFXImage(image.getBufferedImage(), null);
            iv_current_image.setImage(image_preview_fx);
        } else {
            roi_x = 0;
            roi_y = 0;

            Image image_preview_fx = SwingFXUtils.toFXImage(image.getBufferedImage(), null);
            iv_current_image.setImage(image_preview_fx);

            iv_current_roi_histogram.setImage(default_image);
        }

        label_roi_x.setText(roi_x.toString());
        label_roi_y.setText(roi_y.toString());
        updateImagePanCursor();
    }

    @FXML
    public void show_imagej_roi() {

        if (!requireCurrentImage()) {
            return;
        }
        if (position == null || position < 1 || position > current_image.getImageStackSize()) {
            position = 1;
        }

        ImagePlus image = new ImagePlus();
        image.setProcessor(current_image.getImageStack().getProcessor(position));
        image.setTitle(current_image.getTitle());

        // Initialise la figure avec le zoom/ROI actuel de la vue principale.
        int value_zoom = Math.max(1, (int) Math.round(scroll_zoom.getValue()));
        clampRoiToImage();
        int initW = Math.max(1, image.getWidth() / value_zoom);
        int initH = Math.max(1, image.getHeight() / value_zoom);

        openRoiZoomViewer(image, roi_x, roi_y, initW, initH);
    }

    /**
     * Ouvre la visionneuse Zoom/ROI. La sélection peut être dessinée (curseur +),
     * déplacée, agrandie/rétrécie (poignées, molette, boutons) et enregistrée.
     * "Appliquer" recadre l'image actuelle sur la sélection (annulable via Retour).
     */
    private void openRoiZoomViewer(ImagePlus image, int initX, int initY, int initW, int initH) {
        try {
            Roi_Zoom_Viewer.show(image, initX, initY, initW, initH, getMainWindow(),
                    (x, y, w, h) -> {
                        if (current_image == null || w <= 0 || h <= 0) {
                            return;
                        }
                        if (position == null || position < 1 || position > current_image.getImageStackSize()) {
                            position = 1;
                        }
                        // Recadre l'image actuelle sur la partie sélectionnée.
                        int imgW = current_image.getWidth();
                        int imgH = current_image.getHeight();
                        int cx = Math.max(0, Math.min(x, imgW - 1));
                        int cy = Math.max(0, Math.min(y, imgH - 1));
                        int cw = Math.max(1, Math.min(w, imgW - cx));
                        int ch = Math.max(1, Math.min(h, imgH - cy));
                        ImageProcessor proc = current_image.getImageStack().getProcessor(position).duplicate();
                        proc.setRoi(cx, cy, cw, ch);
                        setImageProcessor(proc.crop());
                    });
        } catch (Exception e) {
            e.printStackTrace();
            // Repli : ancien comportement (fenêtre ImageJ).
            image.show();
        }
    }

    @FXML
    public void plugin_collage() {

        if (ip_array == null || ip_array.isEmpty()) {
            requireCurrentImage();
            return;
        }
        openToolDialog("/mg/emit/picneo/tenten/fxmls/collage/Collage_Layout.fxml",
                "Créer un collage",
                Collage_Controller.class,
                controller -> {
                    controller.setImageArray(ip_array);
                    controller.setOnResult(this::addCollageResultToGallery);
                });
    }

    /**
     * Ajoute un collage à la galerie (vignette, historique, sélection).
     */
    private void addCollageResultToGallery(ImagePlus result) {
        if (result == null) {
            return;
        }
        if (ip_array.size() >= 9) {
            Alert alert = new Alert(AlertType.WARNING,
                    "Capacité maximale atteinte ! Veuillez fermer d'autres images !", ButtonType.OK);
            alert.setTitle("Avertissement !");
            Alert_Util.style(alert);
            alert.showAndWait();
            return;
        }
        Image image_preview_fx = SwingFXUtils.toFXImage(result.getProcessor().getBufferedImage(), null);
        ip_array.add(result);
        image_history.add(result);
        // Création ex nihilo (aucun original sur disque) : à enregistrer.
        image_history.markUnsaved(image_history.size() - 1);
        iv_array.get(ip_array.size() - 1).setImage(image_preview_fx);
        Tooltip.install(iv_array.get(ip_array.size() - 1), new Tooltip(result.getTitle()));
        updateCollageButtons();
        setCurrentImage(ip_array.size() - 1);
        textMessage.setText(result.getTitle() + " a été créé avec succès !");
    }

    private void file_openSuccessfullMessage(String imageTitle) {
        textMessage.setText(imageTitle + " a été ouverte avec succès !");
    }

    private void file_closeSuccessfullMessage(String imageTitle) {
        textMessage.setText(imageTitle + " a été fermée avec succès !");
    }

    @FXML
    public void iv_array_1_setCurrentImage_OnClick() {
        setCurrentImage(Integer.parseInt(iv_1.getAccessibleText()));
    }

    @FXML
    public void iv_array_2_setCurrentImage_OnClick() {
        setCurrentImage(Integer.parseInt(iv_2.getAccessibleText()));
    }

    @FXML
    public void iv_array_3_setCurrentImage_OnClick() {
        setCurrentImage(Integer.parseInt(iv_3.getAccessibleText()));
    }

    @FXML
    public void iv_array_4_setCurrentImage_OnClick() {
        setCurrentImage(Integer.parseInt(iv_4.getAccessibleText()));
    }

    @FXML
    public void iv_array_5_setCurrentImage_OnClick() {
        setCurrentImage(Integer.parseInt(iv_5.getAccessibleText()));
    }

    @FXML
    public void iv_array_6_setCurrentImage_OnClick() {
        setCurrentImage(Integer.parseInt(iv_6.getAccessibleText()));
    }

    @FXML
    public void iv_array_7_setCurrentImage_OnClick() {
        setCurrentImage(Integer.parseInt(iv_7.getAccessibleText()));
    }

    @FXML
    public void iv_array_8_setCurrentImage_OnClick() {
        setCurrentImage(Integer.parseInt(iv_8.getAccessibleText()));
    }

    @FXML
    public void iv_array_9_setCurrentImage_OnClick() {
        setCurrentImage(Integer.parseInt(iv_9.getAccessibleText()));
    }

    /**
     * Bouton Statistiques RGB (barre ROI) : toujours actif quand une image
     * est ouverte, même sans zoom (sans zoom la ROI couvre toute l'image).
     */
    private void checkIfImageIsRgb() {

        button_image_statistics_rgb_roi.setDisable(current_image == null);
        updateRoiBottomButtonsState(isImagePanEnabled());
    }

    /**
     * Déplacement de ROI et réinitialisation n'ont de sens que si
     * l'image courante est zoomée : sans zoom la ROI couvre toute l'image et ces
     * actions seraient sans effet. La barre de zoom, son libellé, le bouton Zoom ROI
     * et le bouton Statistiques RGB restent toujours actifs.
     */
    private void updateRoiBottomButtonsState(boolean zoomed) {

        button_roi_left.setDisable(!zoomed);
        button_roi_right.setDisable(!zoomed);
        button_roi_up.setDisable(!zoomed);
        button_roi_down.setDisable(!zoomed);
        button_roi_reset.setDisable(!zoomed);
    }

    /**
     * "Tout fermer" (menu + bouton) actif dès qu'au moins une image
     * est importée, même sans clic de sélection.
     */
    private void updateCloseAllState() {
        boolean hasImages = ip_array != null && !ip_array.isEmpty();
        file_menuItem_closeAllImage.setDisable(!hasImages);
        button_close_all.setDisable(!hasImages);
    }

    private void onImageOpened() {

        analyze_particles.setDisable(false);
        file_menuItem_closeImage.setDisable(false);
        file_menuItem_closeAllImage.setDisable(false);
        file_menuItem_saveAsImage.setDisable(false);
        image_menuItem_brightness_contrast.setDisable(false);
        image_menuItem_convert_type.setDisable(false);
        image_menu_threshold.setDisable(false);
        process_menu_filters.setDisable(false);
        process_menuItem_shadow.setDisable(false);
        process_menu_noise.setDisable(false);
        process_menu_others.setDisable(false);

        vbox_current_image_1.setDisable(false);
        vbox_current_image_2.setDisable(false);

        button_close_all.setDisable(false);

        updateSaveButtons();
        checkIfImageIsRgb();
    }

    public void onImageClosed() {
        analyze_particles.setDisable(true);
        file_menuItem_closeImage.setDisable(true);
        file_menuItem_saveImage.setDisable(true);
        file_menuItem_saveAsImage.setDisable(true);
        image_menuItem_brightness_contrast.setDisable(true);
        image_menuItem_convert_type.setDisable(true);
        image_menu_threshold.setDisable(true);
        process_menu_filters.setDisable(true);
        process_menuItem_shadow.setDisable(true);
        process_menu_noise.setDisable(true);
        process_menu_others.setDisable(true);

        vbox_current_image_1.setDisable(true);
        vbox_current_image_2.setDisable(true);

        file_menuItem_closeAllImage.setDisable(true);
        file_menuItem_saveAllImage.setDisable(true);

        iv_current_image.setImage(default_image);
        iv_current_histogram.setImage(default_image);
        iv_current_roi_histogram.setImage(default_image);

        ta_current_image.setText("");

        button_close_all.setDisable(true);
        button_save_all.setDisable(true);

        if (button_save != null) {
            button_save.setDisable(true);
        }

        button_image_statistics_rgb_roi.setDisable(true);
        updateRoiBottomButtonsState(false);
        updateImageHistoryButtons();
    }

    private void setCurrentImage(Integer id) {

        if (id != null && id >= 0 && id < ip_array.size()) {

            position = 1;

            current_image = ip_array.get(id).duplicate();
            current_image.setTitle(ip_array.get(id).getTitle());

            Image image_preview_fx = SwingFXUtils.toFXImage(current_image.getImageStack().getProcessor(position).getBufferedImage(), null);
            iv_current_image.setImage(image_preview_fx);
            iv_current_image.setAccessibleText(String.valueOf(id));

            updateHistogramView();

            scroll_zoom.setValue(1);
            label_zoom_value.setText(String.valueOf((int) Math.round(scroll_zoom.getValue())));

            show_image_roi();
            getImageStats(current_image);
            onImageOpened();
            updateImageHistoryButtons();
        }
    }

    private void setImageProcessor(ImageProcessor ip) {

        Integer id = getCurrentIdOrNull();
        if (id == null || current_image == null) {
            return;
        }

        if (current_image.getImageStackSize() > 1) {
            current_image.getImageStack().setProcessor(ip, position);
            ip_array.get(id).getImageStack().setProcessor(ip, position);
        } else {
            current_image.setProcessor(ip);
            ip_array.get(id).setProcessor(ip);
        }

        updateThumbnails(id);

        refreshCurrentViews();
    }

    private void setImagePlus(ImagePlus ip) {

        Integer id = getCurrentIdOrNull();
        if (id == null || current_image == null || ip == null) {
            return;
        }

        current_image = ip.duplicate();
        current_image.setTitle(ip.getTitle());
        ip_array.get(id).setImage(ip.duplicate());
        ip_array.get(id).setTitle(ip.getTitle());

        updateThumbnails(id);

        refreshCurrentViews();
    }

    @FXML
    public void image_undo() {
        applyImageHistoryStep(-1);
    }

    @FXML
    public void image_redo() {
        applyImageHistoryStep(1);
    }

    @FXML
    public void image_restore() {
        Integer id = getCurrentImageHistoryId();
        if (id == null) {
            return;
        }

        ImagePlus original = image_history.restoreOriginal(id.intValue());
        if (original != null) {
            restoreImageFromHistory(original);
        }
    }

    /**
     * Vrai s'il existe des modifications non enregistrées
     * (utilisé pour l'alerte de fermeture du programme).
     */
    public boolean hasUnsavedChanges() {
        return image_history.hasUnsavedChanges();
    }

    /**
     * Enregistrer (image / tout) actif uniquement si modification non
     * enregistrée. "Enregistrer sous" reste toujours disponible (export).
     */
    private void updateSaveButtons() {
        boolean currentDirty = image_history.isDirty(getCurrentIdOrNull());
        boolean anyDirty = hasUnsavedChanges();
        if (file_menuItem_saveImage != null) {
            file_menuItem_saveImage.setDisable(!currentDirty);
        }
        if (button_save != null) {
            button_save.setDisable(!currentDirty);
        }
        if (file_menuItem_saveAllImage != null) {
            file_menuItem_saveAllImage.setDisable(!anyDirty);
        }
        if (button_save_all != null) {
            button_save_all.setDisable(!anyDirty);
        }
    }

    private void recordCurrentImageHistory() {
        if (current_image == null) {
            return;
        }

        Integer id = getCurrentImageHistoryId();
        if (id == null) {
            return;
        }

        image_history.record(id.intValue(), current_image);
    }

    private void applyImageHistoryStep(int direction) {
        Integer id = getCurrentImageHistoryId();
        if (id == null) {
            return;
        }

        ImagePlus state = image_history.step(id.intValue(), direction);
        if (state != null) {
            restoreImageFromHistory(state);
        }
    }

    private void restoreImageFromHistory(ImagePlus image) {
        image_history.setRecording(false);
        setImagePlus(duplicateImageState(image));
        image_history.setRecording(true);
        updateImageHistoryButtons();
    }

    private ImagePlus duplicateImageState(ImagePlus image) {
        ImagePlus copy = image.duplicate();
        copy.setTitle(image.getTitle());
        return copy;
    }

    private Integer getCurrentImageHistoryId() {
        return getCurrentIdOrNull();
    }

    private void updateImageHistoryButtons() {
        boolean canUndo = false;
        boolean canRedo = false;
        boolean canRestore = false;

        Integer id = getCurrentImageHistoryId();
        if (image_history.isValidId(id)) {
            int index = image_history.currentIndex(id);
            canUndo = index > 0;
            canRedo = index < image_history.depth(id) - 1;
            canRestore = index > 0;
        }

        if (button_image_undo != null) {
            button_image_undo.setDisable(!canUndo);
        }
        if (button_image_redo != null) {
            button_image_redo.setDisable(!canRedo);
        }
        if (button_image_restore != null) {
            button_image_restore.setDisable(!canRestore);
        }
    }

    private void showRoiHistogram(ImagePlus ip) {

        HistogramWindow histogram = new FrenchHistogramWindow(ip);

        Image image_preview_histogram = SwingFXUtils.toFXImage(histogram.getImagePlus().getBufferedImage(), null);
        iv_current_roi_histogram.setImage(image_preview_histogram);
    }

    /** Met à jour l'histogramme de la tranche courante (vue principale). */
    private void updateHistogramView() {
        ImagePlus current_histogram = new ImagePlus();
        current_histogram.setProcessor(current_image.getImageStack().getProcessor(position));

        HistogramWindow histogram = new FrenchHistogramWindow(current_histogram);
        Image image_preview_histogram = SwingFXUtils.toFXImage(histogram.getImagePlus().getBufferedImage(), null);
        iv_current_histogram.setImage(image_preview_histogram);
    }

    /** Actualise la vignette de la galerie correspondant à l'image modifiée. */
    private void updateThumbnails(Integer id) {

        if (id == null || id < 0 || id >= ip_array.size() || id >= iv_array.size()) {
            return;
        }

        ImagePlus image = ip_array.get(id);
        if (image == null || image.getProcessor() == null) {
            return;
        }

        ImageView vignette = iv_array.get(id);
        Image image_preview_fx = SwingFXUtils.toFXImage(image.getProcessor().getBufferedImage(), null);
        vignette.setImage(image_preview_fx);
        Tooltip.install(vignette, new Tooltip(image.getTitle()));
    }

    /** Après toute modification : histogramme, zoom à 1, vue ROI, stats, état des boutons. */
    private void refreshCurrentViews() {
        updateHistogramView();

        scroll_zoom.setValue(1);
        label_zoom_value.setText(String.valueOf(scroll_zoom.getValue()));

        show_image_roi();
        getImageStats(current_image);
        checkIfImageIsRgb();
        recordCurrentImageHistory();
        updateImageHistoryButtons();
        updateSaveButtons();
    }

    public void getImageStats(ImagePlus ip) {

        ImageInfo ii = new ImageInfo();
        ta_current_image.setText(Image_Info_Util.toFrench(ii.getImageInfo(ip)));
    }
}
