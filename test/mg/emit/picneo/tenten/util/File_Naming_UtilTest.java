package mg.emit.picneo.tenten.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class File_Naming_UtilTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void baseName_stripsExtension() {
        assertEquals("photo", File_Naming_Util.baseName("photo.png"));
        assertEquals("archive.tar", File_Naming_Util.baseName("archive.tar.gz"));
    }

    @Test
    public void baseName_keepsFileWithoutExtension() {
        assertEquals("sanspoint", File_Naming_Util.baseName("sanspoint"));
        // Un point en debut de nom n'est pas une extension.
        assertEquals(".bashrc", File_Naming_Util.baseName(".bashrc"));
    }

    @Test
    public void extension_returnsDottedSuffix() {
        assertEquals(".png", File_Naming_Util.extension("photo.png"));
        assertEquals(".gz", File_Naming_Util.extension("archive.tar.gz"));
        assertEquals("", File_Naming_Util.extension("sanspoint"));
        assertEquals("", File_Naming_Util.extension(".bashrc"));
    }

    @Test
    public void numberedName_insertsIndexBeforeExtension() {
        assertEquals("photo(3).png", File_Naming_Util.numberedName("photo.png", 3));
        assertEquals("sanspoint(1)", File_Naming_Util.numberedName("sanspoint", 1));
    }

    @Test
    public void firstFreeNumberedCopy_startsAtIndexOne() {
        File copy = File_Naming_Util.firstFreeNumberedCopy(
                folder.getRoot().getAbsolutePath(), "photo.png", null);
        assertEquals("photo(1).png", copy.getName());
        assertFalse(copy.exists());
    }

    @Test
    public void firstFreeNumberedCopy_skipsExistingCopies() throws IOException {
        folder.newFile("photo(1).png");
        folder.newFile("photo(2).png");

        File copy = File_Naming_Util.firstFreeNumberedCopy(
                folder.getRoot().getAbsolutePath(), "photo.png", null);
        assertEquals("photo(3).png", copy.getName());
        assertFalse(copy.exists());
    }

    @Test
    public void firstFreeNumberedCopy_neverReturnsTheExcludedOriginal() {
        // Le fichier d'origine s'appelle lui-meme "photo(1).png" : il faut
        // proposer la copie suivante.
        File excluded = new File(folder.getRoot(), "photo(1).png");

        File copy = File_Naming_Util.firstFreeNumberedCopy(
                folder.getRoot().getAbsolutePath(), "photo.png", excluded);
        assertEquals("photo(2).png", copy.getName());
    }
}
