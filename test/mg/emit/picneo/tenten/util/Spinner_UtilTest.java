package mg.emit.picneo.tenten.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class Spinner_UtilTest {

    @Test
    public void parse_acceptsDotDecimalSeparator() {
        assertEquals(1.5, Spinner_Util.parse("1.5", 0.0), 0.0);
        assertEquals(-2.25, Spinner_Util.parse("-2.25", 0.0), 0.0);
    }

    @Test
    public void parse_acceptsCommaDecimalSeparator() {
        assertEquals(1.5, Spinner_Util.parse("1,5", 0.0), 0.0);
    }

    @Test
    public void parse_ignoresSurroundingSpaces() {
        assertEquals(2.5, Spinner_Util.parse("  2.5  ", 0.0), 0.0);
    }

    @Test
    public void parse_emptyOrBlankTextIsZero() {
        assertEquals(0.0, Spinner_Util.parse("", 7.0), 0.0);
        assertEquals(0.0, Spinner_Util.parse("   ", 7.0), 0.0);
        assertEquals(0.0, Spinner_Util.parse(null, 7.0), 0.0);
    }

    @Test
    public void parse_invalidTextReturnsFallback() {
        assertEquals(7.0, Spinner_Util.parse("abc", 7.0), 0.0);
        // Valeur courante nulle : on retombe sur 0.0 plutot que de planter.
        assertEquals(0.0, Spinner_Util.parse("abc", null), 0.0);
    }
}
