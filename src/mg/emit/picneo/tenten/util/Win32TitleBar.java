package mg.emit.picneo.tenten.util;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.swing.SwingUtilities;

/**
 * Barre de titre des fenetres ImageJ ouvertes par l'application.
 *
 * Cote "Agrandir", Windows n'affiche le carre double ("Restaurer") que si la
 * fenetre est reellement agrandie ; ImageJ empeche cela en traitant lui-meme
 * l'agrandissement (setBounds() + pack() dans ImageWindow.maximize()) :
 * installMaximizeToggle() retire cette interception pour que le bouton
 * alterne correctement et que l'image revienne a sa taille d'avant.
 *
 * disableMinimize()/refuseIconify() (grisonnement du bouton "Reduire", qu'AWT
 * ne sait pas faire : il faut modifier le style natif WS_MINIMIZEBOX via
 * user32.dll, d'ou la librairie JNA) restent disponibles mais ne sont plus
 * appeles : le bouton "Reduire" est laisse actif.
 */
public final class Win32TitleBar {

    /** Index du style natif d'une fenetre (GetWindowLong / SetWindowLong). */
    private static final int GWL_STYLE = -16;
    /** Bouton "Reduire" de la barre de titre. */
    private static final int WS_MINIMIZEBOX = 0x00020000;
    /** NASIZE|NOMOVE|NOZORDER|NOACTIVATE|FRAMECHANGED : recalcule la barre de titre. */
    private static final int SWP_FRAME_FLAGS = 0x0001 | 0x0002 | 0x0004 | 0x0010 | 0x0020;
    /** Classe AWT des fenetres ImageJ / Java sur Windows. */
    private static final String AWT_FRAME_CLASS = "SunAwtFrame";

    /** user32.dll (stdcall, chaines Unicode grace a W32APIOptions). */
    public interface User32 extends StdCallLibrary {

        User32 INSTANCE = Native.load("user32", User32.class, W32APIOptions.DEFAULT_OPTIONS);

        int GetWindowLong(Pointer hWnd, int nIndex);

        int SetWindowLong(Pointer hWnd, int nIndex, int dwNewLong);

        boolean SetWindowPos(Pointer hWnd, Pointer hWndInsertAfter,
                int x, int y, int cx, int cy, int uFlags);

        Pointer FindWindow(String lpClassName, String lpWindowName);
    }

    private Win32TitleBar() {
    }

    /**
     * Grise (et rend inutilisable) le bouton "Reduire" de la fenetre donnee.
     *
     * @return true si le style natif a bien ete modifie
     */
    public static boolean disableMinimize(Window window) {
        if (window == null || !isWindows()) {
            return false;
        }
        try {
            long hwnd = nativeHandle(window);
            if (hwnd == 0) {
                return false;
            }
            Pointer handle = new Pointer(hwnd);
            User32 user32 = User32.INSTANCE;
            int style = user32.GetWindowLong(handle, GWL_STYLE);
            if ((style & WS_MINIMIZEBOX) == 0) {
                return true; // deja grise
            }
            user32.SetWindowLong(handle, GWL_STYLE, style & ~WS_MINIMIZEBOX);
            user32.SetWindowPos(handle, null, 0, 0, 0, 0, SWP_FRAME_FLAGS);
            return (user32.GetWindowLong(handle, GWL_STYLE) & WS_MINIMIZEBOX) == 0;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Bouton "Agrandir" de la fenetre ImageJ : carre double + restauration.
     *
     * ImageJ intercepte l'agrandissement (ImageWindow.windowStateChanged
     * appelle maximize(), qui fait setBounds() + pack()) : la fenetre repasse
     * en fenetre normale et Windows affiche alors un simple carre. On retire
     * cette interception pour que la fenetre reste reellement agrandie :
     * Windows affiche donc le carre double ("Restaurer") comme pour la
     * fenetre principale, l'image est ajustee a la fenetre agrandie, et un
     * clic sur le carre double rend la taille ET le zoom d'avant.
     *
     * Ne fait rien si la fenetre n'est pas une fenetre ImageJ standard.
     */
    public static void installMaximizeToggle(final ImageWindow window) {
        if (window == null) {
            return;
        }
        // 1) desinscription de l'ecouteur d'ImageJ (qui ne fait que maximize())
        WindowStateListener[] listeners = window.getWindowStateListeners();
        for (int i = 0; i < listeners.length; i++) {
            if (listeners[i] == window) {
                window.removeWindowStateListener(listeners[i]);
            }
        }
        // 2) comportement "agrandir puis restaurer"
        window.addWindowStateListener(new WindowAdapter() {

            /** Zoom de la fenetre avant l'agrandissement (-1 = fenetre non agrandie). */
            private double zoomBefore = -1.0;

            @Override
            public void windowStateChanged(WindowEvent e) {
                boolean maximized = (e.getNewState() & Frame.MAXIMIZED_BOTH) != 0;
                boolean wasMaximized = (e.getOldState() & Frame.MAXIMIZED_BOTH) != 0;
                if (maximized == wasMaximized) {
                    return;
                }
                ImageCanvas canvas = window.getCanvas();
                ImagePlus imp = window.getImagePlus();
                if (canvas == null || imp == null) {
                    return;
                }
                if (maximized) {
                    if (zoomBefore < 0) {
                        zoomBefore = canvas.getMagnification();
                    }
                    // Ajuste l'image a la fenetre agrandie : fitToWindow()
                    // ne redimensionne que le canvas (pack() quitterait
                    // l'etat "agrandie" et ferait retomber le bouton).
                    canvas.fitToWindow();
                } else if (zoomBefore >= 0) {
                    // Retour a la taille et au zoom d'avant l'agrandissement :
                    // la fenetre est deja restauree par Windows, on remet
                    // l'image a son zoom initial et on relance la disposition.
                    int imageWidth = imp.getWidth();
                    int imageHeight = imp.getHeight();
                    canvas.setSourceRect(new Rectangle(0, 0, imageWidth, imageHeight));
                    canvas.setSize((int) (imageWidth * zoomBefore),
                            (int) (imageHeight * zoomBefore));
                    canvas.setMagnification(zoomBefore);
                    window.doLayout();
                    canvas.repaint();
                    zoomBefore = -1.0;
                }
            }
        });
    }

    /**
     * Filet de securite : si le style natif n'a pas pu etre modifie, la
     * fenetre refuse quand meme de s'iconifier (elle ne disparait donc jamais
     * derriere une autre fenetre).
     */
    public static void refuseIconify(final Window window) {
        if (window == null) {
            return;
        }
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowIconified(WindowEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (window instanceof Frame) {
                            Frame frame = (Frame) window;
                            if ((frame.getExtendedState() & Frame.ICONIFIED) != 0) {
                                frame.setExtendedState(frame.getExtendedState() & ~Frame.ICONIFIED);
                            }
                        }
                    }
                });
            }
        });
    }

    /** Handle natif (HWND) de la fenetre AWT, avec repli sur FindWindow. */
    private static long nativeHandle(Window window) {
        try {
            // Le peer AWT Windows expose getHWnd() : handle exact, sans recherche.
            Field peerField = Component.class.getDeclaredField("peer");
            peerField.setAccessible(true);
            Object peer = peerField.get(window);
            if (peer != null) {
                Method getHWnd = Class.forName("sun.awt.windows.WComponentPeer")
                        .getMethod("getHWnd");
                getHWnd.setAccessible(true);
                Object handle = getHWnd.invoke(peer);
                if (handle instanceof Long && ((Long) handle).longValue() != 0) {
                    return ((Long) handle).longValue();
                }
            }
        } catch (Throwable t) {
            // repli ci-dessous
        }
        try {
            String title = null;
            if (window instanceof Frame) {
                title = ((Frame) window).getTitle();
            }
            if (title == null || title.isEmpty()) {
                return 0L;
            }
            Pointer handle = User32.INSTANCE.FindWindow(AWT_FRAME_CLASS, title);
            return handle == null ? 0L : Pointer.nativeValue(handle);
        } catch (Throwable t) {
            return 0L;
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }
}
