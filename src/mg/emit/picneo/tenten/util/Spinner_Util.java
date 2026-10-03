package mg.emit.picneo.tenten.util;

import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.StringConverter;

/**
 * Saisie clavier des spinbox à valeur décimale : point décimal accepté
 * (ainsi que la virgule), validation à Entrée et à la perte de focus.
 * Comportement identique dans toutes les fenêtres de l'application.
 */
public final class Spinner_Util {

    private Spinner_Util() {
    }

    /**
     * Rend une spinbox "amie du clavier" : éditable, séparateur décimal
     * point/virgule, validation à Entrée et à la perte de focus.
     */
    public static void setupDoubleSpinner(final Spinner<Double> spinner) {
        if (spinner == null) {
            return;
        }
        spinner.setEditable(true);

        if (spinner.getValueFactory() instanceof SpinnerValueFactory.DoubleSpinnerValueFactory) {
            final SpinnerValueFactory.DoubleSpinnerValueFactory factory =
                    (SpinnerValueFactory.DoubleSpinnerValueFactory) spinner.getValueFactory();
            factory.setConverter(new StringConverter<Double>() {
                @Override
                public String toString(Double value) {
                    if (value == null) {
                        return "0";
                    }
                    return Double.toString(value);
                }

                @Override
                public Double fromString(String text) {
                    if (text == null || text.trim().isEmpty()) {
                        return 0.0;
                    }
                    return parse(text, factory.getValue());
                }
            });
        }

        spinner.getEditor().setOnAction(event -> commitEditorText(spinner));
        spinner.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                commitEditorText(spinner);
            }
        });
    }

    /**
     * Valide le texte courant de l'éditeur : vide devient 0.0,
     * sinon re-parse via la valeur de la spinbox.
     */
    public static void commitEditorText(Spinner<Double> spinner) {
        String text = spinner.getEditor().getText();
        if (text == null || text.trim().isEmpty()) {
            spinner.getValueFactory().setValue(0.0);
        } else {
            spinner.increment(0);
        }
    }

    /**
     * Interprète un texte saisi : accepte le point ou la virgule décimale.
     * Retourne 0.0 si le texte est vide et {@code fallback} si invalide.
     */
    public static double parse(String text, Double fallback) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(text.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return fallback != null ? fallback : 0.0;
        }
    }

    /**
     * Valeur sûre d'une spinbox : {@code def} si la valeur est nulle,
     * en réalignant au passage la valeur de la spinbox.
     */
    public static double doubleValueOrDefault(Spinner<Double> spinner, double def) {
        if (spinner == null) {
            return def;
        }
        Double value = spinner.getValue();
        if (value != null) {
            return value;
        }
        spinner.getValueFactory().setValue(def);
        return def;
    }
}
