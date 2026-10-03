package mg.emit.picneo.tenten.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class Image_Info_UtilTest {

    @Test
    public void translate_replacesKnownEnglishLabels() {
        String info = "Title: photo.tif\nWidth: 100\nHeight: 50";
        String translated = Image_Info_Util.translate(info);
        assertEquals("Titre : photo.tif\nLargeur : 100\nHauteur : 50", translated);
    }

    @Test
    public void translate_translatesStatisticsLabels() {
        String info = "Mean: 12.5\nStdDev: 3.1\nMin: 0\nMax: 255";
        String translated = Image_Info_Util.translate(info);
        assertEquals("Moyenne : 12.5\nÉcart type : 3.1\nMin : 0\nMax : 255", translated);
    }

    @Test
    public void translate_keepsUnknownContentUntouched() {
        String info = "Quelque chose de non traduit";
        assertEquals(info, Image_Info_Util.translate(info));
    }

    @Test
    public void translate_nullReturnsNull() {
        assertNull(Image_Info_Util.translate(null));
    }

    @Test
    public void filterNoiseLines_removesInformatoryLines() {
        String info = "Titre : photo.tif\nNo threshold\nNo overlay\nNo selection\nLargeur : 100";
        String filtered = Image_Info_Util.filterNoiseLines(info);
        assertEquals("Titre : photo.tif\nLargeur : 100", filtered);
    }

    @Test
    public void filterNoiseLines_keepsEmptyAndNullValues() {
        assertEquals("", Image_Info_Util.filterNoiseLines(""));
        assertNull(Image_Info_Util.filterNoiseLines(null));
    }

    @Test
    public void filterNoiseLines_onlyRemovesExactMatches() {
        String info = "No threshold details\nNo threshold";
        assertEquals("No threshold details", Image_Info_Util.filterNoiseLines(info));
    }

    @Test
    public void toFrench_translatesThenCleans() {
        // "Uncalibrated" est traduit puis la ligne traduite est supprimee.
        String info = "Titre : photo.tif\nUncalibrated";
        assertEquals("Titre : photo.tif", Image_Info_Util.toFrench(info));
    }

    @Test
    public void toFrench_nullReturnsNull() {
        assertNull(Image_Info_Util.toFrench(null));
    }
}
