package mg.emit.picneo.tenten.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class Zoom_Roi_UtilTest {

    // ------------------------------------------------------------------
    // clampRoi
    // ------------------------------------------------------------------

    @Test
    public void clampRoi_returnsOriginWithoutZoom() {
        assertArrayEquals(new int[]{0, 0}, Zoom_Roi_Util.clampRoi(1000, 800, 1, 50, 40));
        assertArrayEquals(new int[]{0, 0}, Zoom_Roi_Util.clampRoi(1000, 800, 0, 50, 40));
    }

    @Test
    public void clampRoi_keepsRoiInsideVisibleViewport() {
        // zoom 4 -> fenêtre affichée 250x200 -> bornes (750, 600)
        assertArrayEquals(new int[]{750, 600}, Zoom_Roi_Util.clampRoi(1000, 800, 4, 900, 700));
    }

    @Test
    public void clampRoi_keepsValidRoiUnchanged() {
        assertArrayEquals(new int[]{10, 20}, Zoom_Roi_Util.clampRoi(1000, 800, 4, 10, 20));
    }

    @Test
    public void clampRoi_neverReturnsNegativeCoordinates() {
        assertArrayEquals(new int[]{0, 0}, Zoom_Roi_Util.clampRoi(1000, 800, 4, -30, -5));
    }

    @Test
    public void clampRoi_boundsToImageWhenViewportIsLarge() {
        // zoom 2 sur image 10x10 : fenêtre 5x5, bornes (5, 5)
        assertArrayEquals(new int[]{5, 5}, Zoom_Roi_Util.clampRoi(10, 10, 2, 99, 99));
    }

    // ------------------------------------------------------------------
    // keepPointUnderCursor
    // ------------------------------------------------------------------

    @Test
    public void keepPointUnderCursor_isIdentityWhenZoomDoesNotChange() {
        int[] roi = Zoom_Roi_Util.keepPointUnderCursor(10, 20, 0.5, 0.5, 100, 80, 100, 80);
        assertArrayEquals(new int[]{10, 20}, roi);
    }

    @Test
    public void keepPointUnderCursor_recentersRoiWhenZoomingIn() {
        // Curseur au centre (0.5) :
        // X : point source = 10 + 0.5*100 = 60 ; nouvelle roi = 60 - 0.5*50 = 35
        // Y : point source = 20 + 0.5*80  = 60 ; nouvelle roi = 60 - 0.5*40 = 40
        int[] roi = Zoom_Roi_Util.keepPointUnderCursor(10, 20, 0.5, 0.5, 100, 80, 50, 40);
        assertArrayEquals(new int[]{35, 40}, roi);
    }

    @Test
    public void keepPointUnderCursor_keepsOriginWhenCursorAtTopLeft() {
        int[] roi = Zoom_Roi_Util.keepPointUnderCursor(10, 20, 0, 0, 100, 80, 50, 40);
        assertArrayEquals(new int[]{10, 20}, roi);
    }

    // ------------------------------------------------------------------
    // Wheel_Accumulator
    // ------------------------------------------------------------------

    @Test
    public void wheel_accumulatesSmallDeltasUntilThreshold() {
        Zoom_Roi_Util.Wheel_Accumulator accumulator = new Zoom_Roi_Util.Wheel_Accumulator();
        assertEquals(0, accumulator.add(6, 20));
        assertEquals(0, accumulator.add(6, 20));
        assertEquals(0, accumulator.add(6, 20));
        assertEquals(1, accumulator.add(6, 20)); // 24 cumulés -> un pas avant
    }

    @Test
    public void wheel_resetsWhenDirectionChanges() {
        Zoom_Roi_Util.Wheel_Accumulator accumulator = new Zoom_Roi_Util.Wheel_Accumulator();
        assertEquals(0, accumulator.add(15, 20));
        assertEquals(0, accumulator.add(-15, 20)); // sens changé : repart de zéro, puis -15
        assertEquals(0, accumulator.add(10, 20));  // sens changé : +10 seul
        assertEquals(0, accumulator.add(-4, 20));  // sens changé : -4 seul
        assertEquals(-1, accumulator.add(-20, 20)); // -24 cumulés -> pas arrière
    }

    @Test
    public void wheel_exactThresholdTriggersStepAndResets() {
        Zoom_Roi_Util.Wheel_Accumulator accumulator = new Zoom_Roi_Util.Wheel_Accumulator();
        assertEquals(1, accumulator.add(20, 20));
        assertEquals(0, accumulator.add(10, 20)); // le cumul a été remis à zéro
    }

    @Test
    public void wheel_defaultThresholdIsTwentyUnits() {
        Zoom_Roi_Util.Wheel_Accumulator accumulator = new Zoom_Roi_Util.Wheel_Accumulator();
        assertEquals(0, accumulator.add(10));
        assertEquals(1, accumulator.add(10));
    }
}
