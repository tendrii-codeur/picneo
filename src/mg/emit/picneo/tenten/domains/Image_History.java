package mg.emit.picneo.tenten.domains;

import java.util.ArrayList;
import java.util.function.Function;

/**
 * Historique des états successifs d'une image (annuler / refaire / restaurer)
 * et position du dernier enregistrement.
 *
 * Générique sur le type d'état : la copie des états est déléguée à une
 * fonction fournie, ce qui rend la classe testable sans ImageJ ni JavaFX.
 * Une image est « modifiée » tant que sa position d'historique diffère de
 * celle du dernier enregistrement (ou si cet enregistrement a été abandonné).
 */
public class Image_History<T> {

    private final ArrayList<ArrayList<T>> history = new ArrayList<ArrayList<T>>();
    private final ArrayList<Integer> historyIndex = new ArrayList<Integer>();
    /** Position d'historique du dernier enregistrement (-1 = état enregistré abandonné). */
    private final ArrayList<Integer> savedIndex = new ArrayList<Integer>();
    private final Function<T, T> copier;
    private boolean recording = true;

    public Image_History(Function<T, T> copier) {
        this.copier = copier;
    }

    /**
     * Enregistre une image avec son état initial (position 0, considéré
     * comme enregistré).
     */
    public void add(T state) {
        ArrayList<T> states = new ArrayList<T>();
        states.add(copier.apply(state));
        history.add(states);
        historyIndex.add(0);
        savedIndex.add(0);
    }

    /** Oublie tout l'historique d'une image (fermeture). */
    public void remove(int id) {
        if (id < 0 || id >= history.size()) {
            return;
        }
        history.remove(id);
        historyIndex.remove(id);
        if (id < savedIndex.size()) {
            savedIndex.remove(id);
        }
    }

    /** Oublie tous les historiques (fermeture de tout). */
    public void clear() {
        history.clear();
        historyIndex.clear();
        savedIndex.clear();
    }

    public int size() {
        return history.size();
    }

    /**
     * Enregistre un nouvel état. La fin de branche éventuelle (refaires
     * devenus inaccessibles) est abandonnée, tout comme le dernier
     * enregistrement s'il appartenait à cette branche.
     */
    public void record(int id, T state) {
        if (!recording || id < 0 || id >= history.size()) {
            return;
        }

        ArrayList<T> states = history.get(id);
        int index = historyIndex.get(id);
        while (states.size() > index + 1) {
            states.remove(states.size() - 1);
        }
        if (id < savedIndex.size() && savedIndex.get(id) >= states.size()) {
            // L'état enregistré a été abandonné par cette nouvelle branche.
            savedIndex.set(id, -1);
        }

        states.add(copier.apply(state));
        historyIndex.set(id, states.size() - 1);
    }

    /**
     * Passe d'un pas dans l'historique.
     *
     * @param direction -1 (annuler) ou +1 (refaire)
     * @return l'état à afficher, ou null s'il n'y a rien à faire
     */
    public T step(int id, int direction) {
        if (id < 0 || id >= history.size()) {
            return null;
        }

        int index = historyIndex.get(id) + direction;
        ArrayList<T> states = history.get(id);
        if (index < 0 || index >= states.size()) {
            return null;
        }

        historyIndex.set(id, index);
        return states.get(index);
    }

    /**
     * Ramène l'image à son état d'origine (supprime les états suivants).
     *
     * @return l'état d'origine, ou null s'il n'y a rien à restaurer
     */
    public T restoreOriginal(int id) {
        if (id < 0 || id >= history.size()) {
            return null;
        }

        ArrayList<T> states = history.get(id);
        if (states.isEmpty() || historyIndex.get(id) <= 0) {
            return null;
        }

        T original = states.get(0);
        while (states.size() > 1) {
            states.remove(states.size() - 1);
        }
        historyIndex.set(id, 0);
        return original;
    }

    /**
     * Une image est « modifiée » si sa position d'historique diffère de celle
     * du dernier enregistrement. Retour arrière jusqu'au point enregistré
     * = image de nouveau propre (plus de demande à la fermeture).
     */
    public boolean isDirty(Integer id) {
        if (id == null || id < 0 || id >= historyIndex.size() || id >= history.size()) {
            return false;
        }
        int index = historyIndex.get(id);
        int saved = id < savedIndex.size() ? savedIndex.get(id) : 0;
        return saved < 0 || index != saved;
    }

    /**
     * Vrai s'il existe des modifications non enregistrées
     * (utilisé pour l'alerte de fermeture).
     */
    public boolean hasUnsavedChanges() {
        for (int i = 0; i < history.size(); i++) {
            if (isDirty(i)) {
                return true;
            }
        }
        return false;
    }

    /** Mémorise l'état courant de l'image comme enregistré. */
    public void markSaved(Integer id) {
        if (id != null && id >= 0 && id < historyIndex.size() && id < savedIndex.size()) {
            savedIndex.set(id, historyIndex.get(id));
        }
    }

    /**
     * Marque l'image comme n'ayant jamais été enregistrée
     * (résultat créé dans l'application, sans fichier source).
     */
    public void markUnsaved(int id) {
        if (id >= 0 && id < savedIndex.size()) {
            savedIndex.set(id, -1);
        }
    }

    /** Active ou coupe l'enregistrement (restauration d'un état sans reboucler). */
    public void setRecording(boolean recording) {
        this.recording = recording;
    }

    /** Identifiant d'historique exploitable (liste encore synchronisée). */
    public boolean isValidId(Integer id) {
        return id != null && id < history.size() && id < historyIndex.size();
    }

    /** Position courante dans l'historique d'une image. */
    public int currentIndex(Integer id) {
        return historyIndex.get(id);
    }

    /** Nombre d'états enregistrés pour une image. */
    public int depth(Integer id) {
        return history.get(id).size();
    }
}
