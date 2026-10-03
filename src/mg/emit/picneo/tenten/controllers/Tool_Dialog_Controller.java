package mg.emit.picneo.tenten.controllers;

/**
 * Contrôleur d'une fenêtre modale ouverte depuis la fenêtre principale :
 * chargée depuis un FXML, titrée puis restituée à l'appelant.
 */
public interface Tool_Dialog_Controller {

    /**
     * Met à jour le libellé affiché dans la barre de titre de la fenêtre.
     */
    void setWindowTitle(String title);
}
