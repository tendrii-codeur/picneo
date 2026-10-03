package mg.emit.picneo.tenten.domains;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests du modele d'historique (annuler / refaire / enregistrements) avec
 * des etats simples : la classe est generique, aucun ImageJ necessaire.
 */
public class Image_HistoryTest {

    private Image_History<String> newHistory() {
        return new Image_History<String>(state -> state);
    }

    // ------------------------------------------------------------------
    // Ajout / bornes
    // ------------------------------------------------------------------

    @Test
    public void add_startsAtOriginAndClean() {
        Image_History<String> history = newHistory();
        history.add("original");

        assertEquals(1, history.size());
        assertTrue(history.isValidId(0));
        assertEquals(0, history.currentIndex(0));
        assertEquals(1, history.depth(0));
        assertFalse(history.isDirty(0));
        assertFalse(history.hasUnsavedChanges());
    }

    @Test
    public void invalidIdsAreSafe() {
        Image_History<String> history = newHistory();

        assertFalse(history.isDirty(null));
        assertFalse(history.isDirty(0));
        assertFalse(history.isValidId(null));
        assertNull(history.step(0, 1));
        assertNull(history.restoreOriginal(0));
        history.markSaved(null); // sans effet, sans exception
    }

    @Test
    public void recordAndStepOnUnknownImageAreIgnored() {
        Image_History<String> history = newHistory();
        history.add("a");

        history.record(5, "x");
        assertEquals(1, history.size());
        assertNull(history.step(5, -1));
    }

    // ------------------------------------------------------------------
    // Annuler / refaire
    // ------------------------------------------------------------------

    @Test
    public void record_thenUndoThenRedo() {
        Image_History<String> history = newHistory();
        history.add("a");
        history.record(0, "b");
        history.record(0, "c");

        assertEquals(3, history.depth(0));
        assertEquals(2, history.currentIndex(0));

        assertEquals("b", history.step(0, -1));
        assertEquals("a", history.step(0, -1));
        assertNull(history.step(0, -1)); // déjà au début

        assertEquals("b", history.step(0, 1));
        assertEquals("c", history.step(0, 1));
        assertNull(history.step(0, 1)); // déjà à la fin
    }

    @Test
    public void recordingAfterUndoDiscardsRedoBranch() {
        Image_History<String> history = newHistory();
        history.add("a");
        history.record(0, "b");
        history.record(0, "c");
        history.step(0, -1); // retour à "b"

        history.record(0, "d"); // "c" abandonné

        assertEquals(3, history.depth(0));
        assertEquals("b", history.step(0, -1));
        assertEquals("a", history.step(0, -1));
    }

    @Test
    public void recordingCanBeSuspended() {
        Image_History<String> history = newHistory();
        history.add("a");

        history.setRecording(false);
        history.record(0, "fantome");
        assertEquals(1, history.depth(0));

        history.setRecording(true);
        history.record(0, "b");
        assertEquals(2, history.depth(0));
    }

    // ------------------------------------------------------------------
    // Enregistrements (état "modifié")
    // ------------------------------------------------------------------

    @Test
    public void recordMarksImageAsDirty_andUndoCleansItAgain() {
        Image_History<String> history = newHistory();
        history.add("a");
        assertFalse(history.isDirty(0));

        history.record(0, "b");
        assertTrue(history.isDirty(0));
        assertTrue(history.hasUnsavedChanges());

        history.step(0, -1); // retour au point enregistré
        assertFalse(history.isDirty(0));
        assertFalse(history.hasUnsavedChanges());
    }

    @Test
    public void markSavedClearsDirtyState() {
        Image_History<String> history = newHistory();
        history.add("a");
        history.record(0, "b");
        assertTrue(history.isDirty(0));

        history.markSaved(0);
        assertFalse(history.isDirty(0));
    }

    @Test
    public void markUnsavedKeepsImageDirtyFromOrigin() {
        Image_History<String> history = newHistory();
        history.add("a");
        history.markUnsaved(0);

        assertTrue(history.isDirty(0));
        assertTrue(history.hasUnsavedChanges());
    }

    @Test
    public void newBranchInvalidatesPreviousSave() {
        Image_History<String> history = newHistory();
        history.add("a");
        history.record(0, "b");
        history.markSaved(0);
        assertFalse(history.isDirty(0));

        history.step(0, -1); // retour à "a"
        history.record(0, "c"); // nouvelle branche : l'enregistrement "b" disparaît

        assertTrue(history.isDirty(0));
        history.step(0, -1);
        assertTrue(history.isDirty(0)); // toujours modifié, même au départ
    }

    @Test
    public void hasUnsavedChangesLooksAtEveryImage() {
        Image_History<String> history = newHistory();
        history.add("a");
        history.add("b");
        assertFalse(history.hasUnsavedChanges());

        history.record(1, "b2");
        assertTrue(history.hasUnsavedChanges());

        history.markSaved(1);
        assertFalse(history.hasUnsavedChanges());
    }

    // ------------------------------------------------------------------
    // Restauration d'origine
    // ------------------------------------------------------------------

    @Test
    public void restoreOriginalResetsHistoryToFirstState() {
        Image_History<String> history = newHistory();
        history.add("a");
        history.record(0, "b");
        history.record(0, "c");

        assertEquals("a", history.restoreOriginal(0));
        assertEquals(1, history.depth(0));
        assertEquals(0, history.currentIndex(0));
        assertNull(history.restoreOriginal(0)); // rien de plus à restaurer
    }

    @Test
    public void restoreOriginalDoesNothingWhenAtOrigin() {
        Image_History<String> history = newHistory();
        history.add("a");

        assertNull(history.restoreOriginal(0));
        assertEquals(1, history.depth(0));
    }

    // ------------------------------------------------------------------
    // Fermeture des images
    // ------------------------------------------------------------------

    @Test
    public void removeForgetsTheImageAndShiftsFollowingOnes() {
        Image_History<String> history = newHistory();
        history.add("a");
        history.add("b");
        history.record(1, "b2");
        assertTrue(history.isDirty(1));

        history.remove(0);

        assertEquals(1, history.size());
        // L'image restante est désormais à l'identifiant 0.
        assertFalse(history.isValidId(1));
        assertEquals("b", history.step(0, -1));
        history.markSaved(0);
        assertFalse(history.isDirty(0));
    }

    @Test
    public void clearForgetsEverything() {
        Image_History<String> history = newHistory();
        history.add("a");
        history.add("b");

        history.clear();

        assertEquals(0, history.size());
        assertFalse(history.isValidId(0));
        assertFalse(history.isDirty(0));
        assertFalse(history.hasUnsavedChanges());
    }
}
