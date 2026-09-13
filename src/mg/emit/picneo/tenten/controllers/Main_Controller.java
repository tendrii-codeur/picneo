package mg.emit.picneo.tenten.controllers;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;


import mg.emit.picneo.tenten.controllers.analyze.Analyze_Particles_Controller;
import mg.emit.picneo.tenten.controllers.file.File_SaveAs_Controller;
import mg.emit.picneo.tenten.controllers.image.Image_BrightnessContrast_Adjust_Controller;
import mg.emit.picneo.tenten.controllers.image.Image_ConvertType_Controller;
import mg.emit.picneo.tenten.controllers.image.Image_Threshold_Adjust_Controller;
import mg.emit.picneo.tenten.controllers.image.Image_Threshold_Auto_Controller;
import mg.emit.picneo.tenten.controllers.process.Process_Add_Noise_Controller;
import mg.emit.picneo.tenten.controllers.process.Process_Filter_Controller;
import mg.emit.picneo.tenten.controllers.process.Process_Filter_Gaussian_Blur_Controller;
import mg.emit.picneo.tenten.controllers.process.Process_Filter_Unsharp_Mask_Controller;
import mg.emit.picneo.tenten.controllers.process.Process_Shadow_Controller;
import mg.emit.picneo.tenten.controllers.stitching.Stitching_Pairwise_Controller;
import mg.emit.picneo.tenten.enums.Image_Types;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.HistogramWindow;
import ij.io.FileSaver;
import ij.plugin.filter.RankFilters;
import ij.plugin.*;
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
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class Main_Controller {

    @FXML
    public Button button_close_all;

    @FXML
    public Button button_save_all;

    @FXML
    public Button button_image_statistics_rgb;

    @FXML
    public Button button_image_statistics_rgb_roi;

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
    public MenuItem stitching_menuItem_pairwise;

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

    private Image default_image;

    private Integer roi_x = 0;
    private Integer roi_y = 0;

    private boolean imagePanActive;
    private double imagePanStartX;
    private double imagePanStartY;
    private int imagePanStartRoiX;
    private int imagePanStartRoiY;

    private ImagePlus current_image;
    private ImagePlus stitching_result;

    private Integer position;

    @FXML
    public void initialize() {
        initialize_imageView_arrays();
        initialize_image_pan();
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
    }

    private boolean isImagePanEnabled() {
        return current_image != null && (int) scroll_zoom.getValue() > 1;
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

        int valueZoom = (int) scroll_zoom.getValue();
        if (valueZoom <= 1) {
            roi_x = 0;
            roi_y = 0;
            return;
        }

        int viewportW = current_image.getWidth() / valueZoom;
        int viewportH = current_image.getHeight() / valueZoom;
        int maxX = Math.max(0, current_image.getWidth() - viewportW);
        int maxY = Math.max(0, current_image.getHeight() - viewportH);

        roi_x = Math.max(0, Math.min(roi_x, maxX));
        roi_y = Math.max(0, Math.min(roi_y, maxY));
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

        int valueZoom = (int) scroll_zoom.getValue();
        double displayW = iv_current_image.getBoundsInLocal().getWidth();
        double displayH = iv_current_image.getBoundsInLocal().getHeight();

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

    @FXML
    public void main_image_statistics_rgb() {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mg/emit/picneo/tenten/fxmls/Main_Image_Statistics_RGB_Layout.fxml"));
            Parent root = (Parent) loader.load();

            ImagePlus image = new ImagePlus();
            image.setProcessor(current_image.getImageStack().getProcessor(position));

            Main_Image_Statistics_RGB_Controller controller = loader.<Main_Image_Statistics_RGB_Controller>getController();
            controller.setImage(image);

            Stage stage = new Stage();

            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UTILITY);
            stage.setTitle("Statistiques RGB pour " + current_image.getTitle());
            stage.setScene(new Scene(root));

            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void main_image_statistics_rgb_roi() {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mg/emit/picneo/tenten/fxmls/Main_Image_Statistics_RGB_Layout.fxml"));
            Parent root = (Parent) loader.load();

            ImagePlus image = new ImagePlus();
            image.setProcessor(current_image.getImageStack().getProcessor(position));

            ImageProcessor cropped = image.getProcessor();

            Integer value_zoom = (int) scroll_zoom.getValue();

            if (value_zoom > 1) {
                cropped.setRoi(roi_x, roi_y, image.getWidth() / value_zoom, image.getHeight() / value_zoom);
            } else {
                roi_x = 0;
                roi_y = 0;

                cropped.setRoi(0, 0, image.getWidth() / value_zoom, image.getHeight() / value_zoom);
            }

            image.setProcessor(cropped.crop());

            Main_Image_Statistics_RGB_Controller controller = loader.<Main_Image_Statistics_RGB_Controller>getController();
            controller.setImage(image.duplicate());

            Stage stage = new Stage();

            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UTILITY);
            stage.setTitle("Statistiques RGB de la ROI pour " + current_image.getTitle());
            stage.setScene(new Scene(root));

            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void analyze_particles() {

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
        alert.showAndWait();
    }

    private void analyze_particles_controller() {

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mg/emit/picneo/tenten/fxmls/analyze/Analyze_Particles_Layout.fxml"));
            Parent root = (Parent) loader.load();

            ImagePlus image = new ImagePlus();
            image.setProcessor(current_image.getImageStack().getProcessor(position));

            Analyze_Particles_Controller controller = loader.<Analyze_Particles_Controller>getController();
            controller.setImage(image.duplicate());

            Stage stage = new Stage();

            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UTILITY);
            stage.setTitle("Analyse des particules pour " + current_image.getTitle());
            stage.setScene(new Scene(root));

            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void file_closeImage() {

        Alert alert = new Alert(AlertType.CONFIRMATION, "Voulez-vous enregistrer avant de fermer ?",
                ButtonType.NO, ButtonType.OK, ButtonType.CANCEL);

        alert.setTitle("Alerte !");

        alert.showAndWait().ifPresent(type -> {

            if (type == ButtonType.OK || type == ButtonType.NO) {

                Integer id = Integer.valueOf(iv_current_image.getAccessibleText());
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

        String path = ip_array.get(id).getOriginalFileInfo().directory + ip_array.get(id).getTitle();

        IJ.save(ip_array.get(id), path);

        Alert alert = new Alert(AlertType.INFORMATION, "L'image " + ip_array.get(id).getTitle() + " a été enregistrée !",
                ButtonType.OK);

        alert.setTitle("Alerte !");
        alert.showAndWait();
    }

    private void file_closeImage_sortImages(Integer id) {

        Tooltip.uninstall(iv_array.get(id), new Tooltip(ip_array.get(id).getTitle()));
        iv_array.get(id).setImage(default_image);

        ip_array.remove(ip_array.get(id));

        if (ip_array.size() == 0) {
            stitching_menuItem_pairwise.setDisable(true);
        }

        for (int i = 0; i < iv_array.size(); i++) {
            iv_array.get(i).setImage(default_image);
        }

        for (int i = 0; i < ip_array.size(); i++) {

            Image image_preview_fx = SwingFXUtils.toFXImage(ip_array.get(i).getProcessor().getBufferedImage(), null);

            iv_array.get(i).setImage(image_preview_fx);
            Tooltip.install(iv_array.get(i), new Tooltip(ip_array.get(i).getTitle()));
        }

        onImageClosed();
    }

    @FXML
    public void file_closeAllImage() {

        Alert alert = new Alert(AlertType.CONFIRMATION, "Voulez-vous enregistrer avant de fermer toutes les images ?",
                ButtonType.NO, ButtonType.OK, ButtonType.CANCEL);

        alert.setTitle("Alerte !");
        alert.showAndWait().ifPresent(type -> {

            if (type == ButtonType.OK || type == ButtonType.NO) {

                if (type == ButtonType.OK) {
                    file_closeAllImage_saveAll();
                }

                for (int i = 0; i < ip_array.size(); i++) {
                    Tooltip.uninstall(iv_array.get(i), new Tooltip(ip_array.get(i).getTitle()));
                    iv_array.get(i).setImage(default_image);
                }

                ip_array.removeAll(ip_array);
                stitching_menuItem_pairwise.setDisable(true);
                onImageClosed();
            }
        });
    }

    private void file_closeAllImage_saveAll() {

        for (int i = 0; i < ip_array.size(); i++) {

            Tooltip.uninstall(iv_array.get(i), new Tooltip(ip_array.get(i).getTitle()));
            iv_array.get(i).setImage(default_image);

            String path = ip_array.get(i).getOriginalFileInfo().directory + ip_array.get(i).getTitle();

            IJ.save(ip_array.get(i), path);
        }

        Alert alert_save = new Alert(AlertType.INFORMATION, "Toutes les images ont été enregistrées !",
                ButtonType.OK);

        alert_save.setTitle("Alerte !");
        alert_save.showAndWait();
    }

    @FXML
    public void file_openImage() throws InvocationTargetException {

        if (ip_array.size() < 9) {

            try {
                current_image = new ImagePlus();
                current_image = IJ.openImage();
                file_openImage_check(current_image);
            } catch (NullPointerException e) {
            }
        } else {
            Alert alert = new Alert(AlertType.WARNING, "Capacité maximale atteinte ! Veuillez fermer d'autres images !", ButtonType.OK);
            alert.setTitle("Avertissement !");
            alert.showAndWait();
        }
    }

    public void file_openImage_check(
            ImagePlus image) {

        boolean same_image_check = false;

        for (int i = 0; i < ip_array.size(); i++) {

            if (image.getTitle().equals(ip_array.get(i).getTitle())) {

                Alert alert = new Alert(AlertType.WARNING, "Vous avez déjà ouvert la même image !", ButtonType.OK);
                alert.setTitle("Avertissement ImageJ !");
                alert.showAndWait();

                same_image_check = true;
                break;
            }
        }

        if (!same_image_check) {

            Image image_preview_fx = SwingFXUtils.toFXImage(image.getProcessor().getBufferedImage(), null);

            ip_array.add(image);

            iv_array.get(ip_array.size() - 1).setImage(image_preview_fx);
            Tooltip.install(iv_array.get(ip_array.size() - 1), new Tooltip(image.getTitle().toString()));

            stitching_menuItem_pairwise.setDisable(false);

            file_openSuccessfullMessage(image.getTitle());

        }
    }

    @FXML
    public void file_save() {

        Integer id = Integer.valueOf(iv_current_image.getAccessibleText());

        String path = ip_array.get(id).getOriginalFileInfo().directory + ip_array.get(id).getTitle();

        IJ.save(ip_array.get(id), path);

        setCurrentImage(id);

        Alert alert = new Alert(AlertType.INFORMATION, "L'image " + ip_array.get(id).getTitle() + " a été enregistrée !",
                ButtonType.OK);

        alert.setTitle("Alerte !");
        alert.showAndWait();
    }

    @FXML
    public void file_saveAll() {

        Integer id = Integer.valueOf(iv_current_image.getAccessibleText());

        for (int i = 0; i < ip_array.size(); i++) {

            String path = ip_array.get(i).getOriginalFileInfo().directory + ip_array.get(i).getTitle();

            IJ.save(ip_array.get(i), path);
        }

        setCurrentImage(id);

        Alert alert = new Alert(AlertType.INFORMATION, "Toutes les images ont été enregistrées !",
                ButtonType.OK);

        alert.setTitle("Alerte !");
        alert.showAndWait();
    }

    @FXML
    public void file_saveAs() {

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mg/emit/picneo/tenten/fxmls/file/File_SaveAs_Layout.fxml"));
            Parent root = (Parent) loader.load();

            File_SaveAs_Controller controller = loader.<File_SaveAs_Controller>getController();
            controller.setImage(current_image.duplicate());

            Stage stage = new Stage();

            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UTILITY);
            stage.setTitle("Option d'enregistrement pour " + current_image.getTitle());
            stage.setScene(new Scene(root));

            stage.showAndWait();

            Integer id = Integer.parseInt(iv_current_image.getAccessibleText());

            setCurrentImage(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void file_quit() {

        Alert alert = new Alert(AlertType.WARNING, "Voulez-vous fermer le programme ?\nToutes les modifications non enregistrées seront perdues !",
                ButtonType.NO, ButtonType.YES);
        alert.setWidth(600);
        alert.setTitle("Avertissement !");
        alert.showAndWait().ifPresent(type -> {

            if (type == ButtonType.YES) {
                Platform.exit();

            }
        });
    }

    @FXML
    public void image_adjustBrightnessContrast() {

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mg/emit/picneo/tenten/fxmls/image/Image_BrightnessContrast_Adjust_Layout.fxml"));
            Parent root = (Parent) loader.load();

            ImagePlus image = new ImagePlus();
            image.setProcessor(current_image.getImageStack().getProcessor(position));

            Image_BrightnessContrast_Adjust_Controller controller = loader.<Image_BrightnessContrast_Adjust_Controller>getController();
            controller.setImage(image.getProcessor().getMin(), image.getProcessor().getMax(), image.duplicate());

            Stage stage = new Stage();

            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UTILITY);
            stage.setTitle("Ajuster Luminosité/Contraste pour " + current_image.getTitle());
            stage.setScene(new Scene(root));

            stage.showAndWait();

            if (!controller.getStageClosedOnExit()) {
                setImageProcessor(controller.getImageProcessor());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void image_ConvertType() {

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mg/emit/picneo/tenten/fxmls/image/Image_ConvertType_Layout.fxml"));
            Parent root = (Parent) loader.load();

            Image_ConvertType_Controller controller = loader.<Image_ConvertType_Controller>getController();
            controller.setImage(current_image.duplicate());

            Stage stage = new Stage();

            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UTILITY);
            stage.setTitle("Convertir le type d'image pour " + current_image.getTitle());
            stage.setScene(new Scene(root));

            stage.showAndWait();

            if (!controller.getStageClosedOnExit()) {
                setImagePlus(controller.getImagePlus());

                Integer id = Integer.parseInt(iv_current_image.getAccessibleText());

                setCurrentImage(id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void image_threshold_adjust() {

        if (current_image.getType() != ImagePlus.GRAY8) {
            image_threshold_alert();
        } else {
            image_threshold_adjust_controller();
        }
    }

    private void image_threshold_adjust_controller() {

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mg/emit/picneo/tenten/fxmls/image/Image_Threshold_Adjust_Layout.fxml"));
            Parent root = (Parent) loader.load();

            ImagePlus image = new ImagePlus();
            image.setProcessor(current_image.getImageStack().getProcessor(position));

            Image_Threshold_Adjust_Controller controller = loader.<Image_Threshold_Adjust_Controller>getController();
            controller.setImage(image.duplicate());

            Stage stage = new Stage();

            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UTILITY);
            stage.setTitle("Aperçu du seuillage ajusté pour " + current_image.getTitle());
            stage.setScene(new Scene(root));

            stage.showAndWait();

            if (!controller.getStageClosedOnExit()) {
                setImageProcessor(controller.getImageProcessor());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void image_threshold_auto() {

        if (current_image.getType() != ImagePlus.GRAY8) {
            image_threshold_alert();
        } else {
            image_threshold_auto_controller();
        }
    }

    private void image_threshold_auto_controller() {

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mg/emit/picneo/tenten/fxmls/image/Image_Threshold_Auto_Layout.fxml"));
            Parent root = (Parent) loader.load();

            ImagePlus image = new ImagePlus();
            image.setProcessor(current_image.getImageStack().getProcessor(position));

            Image_Threshold_Auto_Controller controller = loader.<Image_Threshold_Auto_Controller>getController();
            controller.setImage(image.duplicate());

            Stage stage = new Stage();

            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UTILITY);
            stage.setTitle("Aperçu du seuillage automatique pour " + current_image.getTitle());
            stage.setScene(new Scene(root));

            stage.showAndWait();

            if (!controller.getStageClosedOnExit()) {
                setImageProcessor(controller.getImageProcessor());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void image_threshold_auto_instant() {

        if (current_image.getType() != ImagePlus.GRAY8) {
            image_threshold_alert();
        } else {

            ImagePlus image_backup = new ImagePlus();
            image_backup.setProcessor(current_image.getImageStack().getProcessor(position));

            image_backup.getProcessor().autoThreshold();

            setImageProcessor(image_backup.getProcessor());
        }
    }

    private void image_threshold_alert() {

        Alert alert = new Alert(AlertType.WARNING, "Une image 8-bit est requise !", ButtonType.OK);
        alert.setTitle("Avertissement de seuillage !");
        alert.showAndWait();
    }

    @FXML
    public void process_find_edges() {

        ImagePlus image_backup = new ImagePlus();
        image_backup.setProcessor(current_image.getImageStack().getProcessor(position));

        image_backup.getProcessor().findEdges();

        setImageProcessor(image_backup.getProcessor());
    }

    @FXML
    public void process_invert() {

        ImagePlus image_backup = new ImagePlus();
        image_backup.setProcessor(current_image.getImageStack().getProcessor(position));

        image_backup.getProcessor().invert();

        setImageProcessor(image_backup.getProcessor());
    }

    @FXML
    public void process_filter() {

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mg/emit/picneo/tenten/fxmls/process/Process_Filter_Layout.fxml"));
            Parent root = (Parent) loader.load();

            ImagePlus image = new ImagePlus();
            image.setProcessor(current_image.getImageStack().getProcessor(position));

            Process_Filter_Controller controller = loader.<Process_Filter_Controller>getController();
            controller.setImage(image.duplicate());

            Stage stage = new Stage();

            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UTILITY);
            stage.setTitle("Aperçu du filtre pour " + current_image.getTitle());
            stage.setScene(new Scene(root));

            stage.showAndWait();

            if (!controller.getStageClosedOnExit()) {
                setImageProcessor(controller.getImageProcessor());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void process_filter_gaussian_blur() {

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mg/emit/picneo/tenten/fxmls/process/Process_Filter_Gaussian_Blur_Layout.fxml"));
            Parent root = (Parent) loader.load();

            ImagePlus image = new ImagePlus();
            image.setProcessor(current_image.getImageStack().getProcessor(position));

            Process_Filter_Gaussian_Blur_Controller controller = loader.<Process_Filter_Gaussian_Blur_Controller>getController();
            controller.setImage(image.duplicate());

            Stage stage = new Stage();

            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UTILITY);
            stage.setTitle("Aperçu du flou gaussien pour " + current_image.getTitle());
            stage.setScene(new Scene(root));

            stage.showAndWait();

            if (!controller.getStageClosedOnExit()) {
                setImageProcessor(controller.getImageProcessor());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void process_filter_unsharp_mask() {

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mg/emit/picneo/tenten/fxmls/process/Process_Filter_Unsharp_Mask_Layout.fxml"));
            Parent root = (Parent) loader.load();

            ImagePlus image = new ImagePlus();
            image.setProcessor(current_image.getImageStack().getProcessor(position));

            Process_Filter_Unsharp_Mask_Controller controller = loader.<Process_Filter_Unsharp_Mask_Controller>getController();
            controller.setImage(image.duplicate());

            Stage stage = new Stage();

            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UTILITY);
            stage.setTitle("Aperçu du masque flou pour " + current_image.getTitle());
            stage.setScene(new Scene(root));

            stage.showAndWait();

            if (!controller.getStageClosedOnExit()) {
                setImageProcessor(controller.getImageProcessor());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void process_shadow() {

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mg/emit/picneo/tenten/fxmls/process/Process_Shadow_Layout.fxml"));
            Parent root = (Parent) loader.load();

            ImagePlus image = new ImagePlus();
            image.setProcessor(current_image.getImageStack().getProcessor(position));

            Process_Shadow_Controller controller = loader.<Process_Shadow_Controller>getController();
            controller.setImage(image.duplicate());

            Stage stage = new Stage();

            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UTILITY);
            stage.setTitle("Aperçu de l'ombrage pour " + current_image.getTitle());
            stage.setScene(new Scene(root));

            stage.showAndWait();

            if (!controller.getStageClosedOnExit()) {
                setImageProcessor(controller.getImageProcessor());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void process_sharpen() {

        ImagePlus image_backup = new ImagePlus();
        image_backup.setProcessor(current_image.getImageStack().getProcessor(position));

        image_backup.getProcessor().sharpen();

        setImageProcessor(image_backup.getProcessor());
    }

    @FXML
    public void process_smooth() {

        ImagePlus image_backup = new ImagePlus();
        image_backup.setProcessor(current_image.getImageStack().getProcessor(position));

        image_backup.getProcessor().smooth();

        setImageProcessor(image_backup.getProcessor());
    }

    @FXML
    public void process_noise_add() {

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mg/emit/picneo/tenten/fxmls/process/Process_Add_Noise_Layout.fxml"));
            Parent root = (Parent) loader.load();

            ImagePlus image = new ImagePlus();
            image.setProcessor(current_image.getImageStack().getProcessor(position));

            Process_Add_Noise_Controller controller = loader.<Process_Add_Noise_Controller>getController();
            controller.setImage(current_image.duplicate());

            Stage stage = new Stage();

            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UTILITY);
            stage.setTitle("Aperçu de l'ajout de bruit pour " + current_image.getTitle());
            stage.setScene(new Scene(root));

            stage.showAndWait();

            if (!controller.getStageClosedOnExit()) {
                setImageProcessor(controller.getImageProcessor());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void process_noise_despekle() {

        ImagePlus image_backup = new ImagePlus();
        image_backup.setProcessor(current_image.getImageStack().getProcessor(position));

        RankFilters filter = new RankFilters();
        filter.rank(image_backup.getProcessor(), 1.0, RankFilters.MEDIAN);

        current_image.setProcessor(image_backup.getProcessor());
    }

    @FXML
    public void reset_image_roi() {

        scroll_zoom.setValue(1);
        label_zoom_value.setText(String.valueOf(scroll_zoom.getValue()));

        show_image_roi();
    }

    @FXML
    public void show_image() {
        current_image.show();
    }

    @FXML
    public void show_image_roi_left() {

        Integer value_zoom = (int) scroll_zoom.getValue();
        Integer width = current_image.getWidth() / value_zoom;

        roi_x -= 5;

        if (roi_x >= 0 && roi_x <= current_image.getWidth()) {
            show_image_roi();
        } else if (roi_x < 0) {
            roi_x = current_image.getWidth() - width - 15;
            show_image_roi();
        }
    }

    @FXML
    public void show_image_roi_right() {

        Integer value_zoom = (int) scroll_zoom.getValue();
        Integer width = current_image.getWidth() / value_zoom;

        roi_x += 5;

        if (roi_x + width >= 0 && roi_x + width <= current_image.getWidth()) {
            show_image_roi();
        } else if (roi_x + width > current_image.getWidth()) {
            roi_x = 0;
            show_image_roi();
        }
    }

    @FXML
    public void show_image_roi_up() {

        Integer value_zoom = (int) scroll_zoom.getValue();
        Integer height = current_image.getHeight() / value_zoom;

        roi_y -= 5;

        if (roi_y >= 0 && roi_y + height <= current_image.getHeight()) {
            show_image_roi();
        } else if (roi_y < 0) {
            roi_y = current_image.getHeight() - height - 15;
            show_image_roi();
        }
    }

    @FXML
    public void show_image_roi_down() {

        Integer value_zoom = (int) scroll_zoom.getValue();
        Integer height = current_image.getHeight() / value_zoom;

        roi_y += 5;

        if (roi_y + height >= 0 && roi_y + height <= current_image.getHeight()) {
            show_image_roi();
        } else if (roi_y + height > current_image.getHeight()) {
            roi_y = 0;
            show_image_roi();
        }
    }

    @FXML
    public void show_image_roi() {

        ImagePlus image = new ImagePlus();
        image.setProcessor(current_image.getImageStack().getProcessor(position));

        ImageProcessor cropped = image.getProcessor();

        Integer value_zoom = (int) scroll_zoom.getValue();
        label_zoom_value.setText(String.valueOf(scroll_zoom.getValue()));

        if (value_zoom > 1) {
            cropped.setRoi(roi_x, roi_y, image.getWidth() / value_zoom, image.getHeight() / value_zoom);

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

        ImagePlus image = new ImagePlus();
        image.setProcessor(current_image.getImageStack().getProcessor(position));

        ImageProcessor cropped = image.getProcessor();

        Integer value_zoom = (int) scroll_zoom.getValue();

        if (value_zoom > 1) {
            cropped.setRoi(roi_x, roi_y, image.getWidth() / value_zoom, image.getHeight() / value_zoom);
        } else {
            roi_x = 0;
            roi_y = 0;

            cropped.setRoi(0, 0, image.getWidth() / value_zoom, image.getHeight() / value_zoom);
        }

        image.setProcessor(cropped.crop());

        image.show();
    }

    @FXML
    public void plugin_stiching_pairwise() {

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mg/emit/picneo/tenten/fxmls/stitching/Stitching_Pairwise_Layout.fxml"));
            Parent root = (Parent) loader.load();

            WindowManager.closeAllWindows();

            Stitching_Pairwise_Controller controller = loader.<Stitching_Pairwise_Controller>getController();
            controller.setImageArray(ip_array);

            Stage stage = new Stage();

            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UTILITY);
            stage.setTitle("Assemblage par paire");
            stage.setScene(new Scene(root));

            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    public void plugin_stitching_grid() {

        file_grid_stitching_running();

        WindowManager.closeAllWindows();

        try {
            Class<?> clazz;
            try {
                clazz = Class.forName("Stitching_Grid");
            } catch (ClassNotFoundException e) {
                clazz = Class.forName("Grid_Stitching");
            }
            ij.plugin.PlugIn plugin = (ij.plugin.PlugIn) clazz.newInstance();
            plugin.run("");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (WindowManager.getWindow("") != null) {

            WindowManager.getImage("").setTitle("Résultat d'assemblage");

            stitching_result = new ImagePlus();
            stitching_result = WindowManager.getImage("Résultat d'assemblage").duplicate();

            WindowManager.closeAllWindows();

            stitching_result.show();

            file_grid_stitching_done_success();

            Alert alert = new Alert(AlertType.CONFIRMATION, "Voulez-vous enregistrer l'image assemblée ?",
                    ButtonType.NO, ButtonType.YES);

            alert.setTitle("Alerte !");

            alert.showAndWait().ifPresent(type -> {

                if (type == ButtonType.YES) {
                    FileSaver filesaver = new FileSaver(stitching_result);
                    filesaver.saveAsJpeg();
                }
            });
        } else {

            WindowManager.closeAllWindows();
            file_grid_stitching_cancelled();
        }

    }

    private void file_openSuccessfullMessage(String imageTitle) {
        textMessage.setText(imageTitle + " a été ouverte avec succès !");
    }

    private void file_closeSuccessfullMessage(String imageTitle) {
        textMessage.setText(imageTitle + " a été fermée avec succès !");
    }

    private void file_grid_stitching_running() {
        textMessage.setText("Le plugin d'assemblage en grille est en cours d'exécution !");
    }

    private void file_grid_stitching_cancelled() {
        textMessage.setText("L'opération d'assemblage en grille a été annulée");
    }

    private void file_grid_stitching_done_success() {
        textMessage.setText("L'opération d'assemblage en grille est terminée !");
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

    private void checkIfImageIsRgb() {

        if (current_image.getType() == Image_Types.RGB.getDisplayImageTypeValue()) {
            button_image_statistics_rgb.setDisable(false);
            button_image_statistics_rgb_roi.setDisable(false);
        } else {
            button_image_statistics_rgb.setDisable(true);
            button_image_statistics_rgb_roi.setDisable(true);
        }
    }

    private void onImageOpened() {

        analyze_particles.setDisable(false);
        file_menuItem_closeImage.setDisable(false);
        file_menuItem_closeAllImage.setDisable(false);
        file_menuItem_saveImage.setDisable(false);
        file_menuItem_saveAllImage.setDisable(false);
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
        button_save_all.setDisable(false);

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

        button_image_statistics_rgb.setDisable(true);
        button_image_statistics_rgb_roi.setDisable(true);
    }

    private void setCurrentImage(Integer id) {

        if (id < ip_array.size()) {

            position = 1;

            current_image = new ImagePlus();
            current_image = ip_array.get(id).duplicate();
            current_image.setTitle(ip_array.get(id).getTitle());

            Image image_preview_fx = SwingFXUtils.toFXImage(current_image.getImageStack().getProcessor(position).getBufferedImage(), null);
            iv_current_image.setImage(image_preview_fx);
            iv_current_image.setAccessibleText(String.valueOf(id));

            ImagePlus current_histogram = new ImagePlus();
            current_histogram.setProcessor(current_image.getImageStack().getProcessor(position));

            HistogramWindow histogram = new HistogramWindow(current_histogram);
            Image image_preview_histogram = SwingFXUtils.toFXImage(histogram.getImagePlus().getBufferedImage(), null);
            iv_current_histogram.setImage(image_preview_histogram);

            scroll_zoom.setValue(1);
            label_zoom_value.setText(String.valueOf(scroll_zoom.getValue()));

            show_image_roi();
            getImageStats(current_image);
            onImageOpened();
        }
    }

    private void setImageProcessor(ImageProcessor ip) {

        Integer id = Integer.parseInt(iv_current_image.getAccessibleText());

        if (current_image.getImageStackSize() > 1) {
            current_image.getImageStack().setProcessor(ip, position);
            ip_array.get(id).getImageStack().setProcessor(ip, position);
        } else {
            current_image.setProcessor(ip);
            ip_array.get(id).setProcessor(ip);
        }

        Image image_preview_fx1 = SwingFXUtils.toFXImage(current_image.getImageStack().getProcessor(position).getBufferedImage(), null);
        iv_current_image.setImage(image_preview_fx1);

        Image image_preview_fx2 = SwingFXUtils.toFXImage(ip_array.get(id).getImageStack().getProcessor(position).getBufferedImage(), null);
        iv_array.get(id).setImage(image_preview_fx2);

        ImagePlus current_histogram = new ImagePlus();
        current_histogram.setProcessor(current_image.getImageStack().getProcessor(position));

        HistogramWindow histogram = new HistogramWindow(current_histogram);
        Image image_preview_histogram = SwingFXUtils.toFXImage(histogram.getImagePlus().getBufferedImage(), null);
        iv_current_histogram.setImage(image_preview_histogram);

        scroll_zoom.setValue(1);
        label_zoom_value.setText(String.valueOf(scroll_zoom.getValue()));

        show_image_roi();
        getImageStats(current_image);
    }

    private void setImagePlus(ImagePlus ip) {

        Integer id = Integer.parseInt(iv_current_image.getAccessibleText());

        current_image = ip.duplicate();
        ip_array.get(id).setImage(ip.duplicate());

        Image image_preview_fx1 = SwingFXUtils.toFXImage(current_image.getImageStack().getProcessor(position).getBufferedImage(), null);
        iv_current_image.setImage(image_preview_fx1);

        Image image_preview_fx2 = SwingFXUtils.toFXImage(ip_array.get(id).getImageStack().getProcessor(position).getBufferedImage(), null);
        iv_array.get(id).setImage(image_preview_fx2);

        ImagePlus current_histogram = new ImagePlus();
        current_histogram.setProcessor(current_image.getImageStack().getProcessor(position));

        HistogramWindow histogram = new HistogramWindow(current_histogram);
        Image image_preview_histogram = SwingFXUtils.toFXImage(histogram.getImagePlus().getBufferedImage(), null);
        iv_current_histogram.setImage(image_preview_histogram);

        scroll_zoom.setValue(1);
        label_zoom_value.setText(String.valueOf(scroll_zoom.getValue()));

        show_image_roi();
        getImageStats(current_image);
    }

    private void showRoiHistogram(ImagePlus ip) {

        HistogramWindow histogram = new HistogramWindow(ip);

        Image image_preview_histogram = SwingFXUtils.toFXImage(histogram.getImagePlus().getBufferedImage(), null);
        iv_current_roi_histogram.setImage(image_preview_histogram);
    }

    public void getImageStats(ImagePlus ip) {

        ImageInfo ii = new ImageInfo();
        String info = ii.getImageInfo(ip);
        if (info != null) {
            info = info.replace("Title:", "Titre :")
                       .replace("Width:", "Largeur :")
                       .replace("Height:", "Hauteur :")
                       .replace("Size:", "Taille :")
                       .replace("Type:", "Type :")
                       .replace("Bits per pixel:", "Bits par pixel :")
                       .replace("Display range:", "Plage d'affichage :")
                       .replace("Channels:", "Canaux :")
                       .replace("Slices:", "Coupes :")
                       .replace("Frames:", "Trames :")
                       .replace("No pixels are selected", "Aucun pixel sélectionné")
                       .replace("Selection:", "Sélection :")
                       .replace("Mean:", "Moyenne :")
                       .replace("StdDev:", "Écart type :")
                       .replace("Min:", "Min :")
                       .replace("Max:", "Max :")
                       .replace("Mode:", "Mode :")
                       .replace("Count:", "Nombre :")
                       .replace("Pixels:", "Pixels :")
                       .replace("Uncalibrated", "Non étalonné")
                       .replace("Inverted LUT", "LUT inversée")
                       .replace("Resolution:", "Résolution :")
                       .replace("Voxel size:", "Taille du voxel :")
                       .replace("Field of view:", "Champ de vision :");
        }
        ta_current_image.setText(info);
    }

    public void setImage(ArrayList<ImagePlus> array) {

        this.ip_array = array;
    }
}
