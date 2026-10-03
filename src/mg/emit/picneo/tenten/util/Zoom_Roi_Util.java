package mg.emit.picneo.tenten.util;

/**
 * Calculs purs de recadrage ROI / zoom (fenêtre principale et visionneuses).
 * Aucune dépendance à JavaFX ni à ImageJ : testable unitairement.
 */
public final class Zoom_Roi_Util {

    private Zoom_Roi_Util() {
    }

    /**
     * Recadre la ROI dans l'image en fonction du niveau de zoom :
     * au repos (zoom &lt;= 1) la ROI couvre toute l'image (origine 0,0) ;
     * sinon elle reste dans les bornes de la fenêtre affichée.
     *
     * @return {roiX, roiY} bornés
     */
    public static int[] clampRoi(int imageWidth, int imageHeight, int zoom, int roiX, int roiY) {
        if (zoom <= 1) {
            return new int[]{0, 0};
        }

        int viewportW = Math.max(1, imageWidth / zoom);
        int viewportH = Math.max(1, imageHeight / zoom);
        int maxX = Math.max(0, imageWidth - viewportW);
        int maxY = Math.max(0, imageHeight - viewportH);

        return new int[]{
            Math.max(0, Math.min(roiX, maxX)),
            Math.max(0, Math.min(roiY, maxY))
        };
    }

    /**
     * Recalcule la ROI après un changement de zoom pour que le point de
     * l'image situé sous le curseur (coordonnées relatives 0..1 dans la
     * fenêtre affichée) reste au même endroit à l'écran.
     *
     * @param roiX roi courante en pixels source
     * @param roiY roi courante en pixels source
     * @param relativeX position du curseur (0 = gauche, 1 = droite)
     * @param relativeY position du curseur (0 = haut, 1 = bas)
     * @param cropW fenêtre affichée en pixels source, avant zoom
     * @param cropH fenêtre affichée en pixels source, avant zoom
     * @param newCropW fenêtre affichée en pixels source, après zoom
     * @param newCropH fenêtre affichée en pixels source, après zoom
     * @return {roiX, roiY} recalculées
     */
    public static int[] keepPointUnderCursor(int roiX, int roiY,
            double relativeX, double relativeY,
            double cropW, double cropH, double newCropW, double newCropH) {

        double srcX = roiX + relativeX * cropW;
        double srcY = roiY + relativeY * cropH;

        return new int[]{
            (int) Math.round(srcX - relativeX * newCropW),
            (int) Math.round(srcY - relativeY * newCropH)
        };
    }

    /**
     * Cumul des deltas de la molette : les molettes fines envoient de petits
     * deltas, on les cumule avant de changer de niveau de zoom. Un changement
     * de sens de la molette redémarre le cumul.
     */
    public static final class Wheel_Accumulator {

        private static final double DEFAULT_THRESHOLD = 20.0;

        private double accumulated = 0.0;

        /**
         * Ajoute un delta de molette.
         *
         * @return 1 (zoom avant), -1 (zoom arrière) ou 0 (seuil non atteint)
         */
        public int add(double delta) {
            return add(delta, DEFAULT_THRESHOLD);
        }

        /**
         * Ajoute un delta de molette avec un seuil de pas personnalisé.
         *
         * @return 1 (zoom avant), -1 (zoom arrière) ou 0 (seuil non atteint)
         */
        public int add(double delta, double threshold) {
            // Un changement de sens de la molette redémarre le cumul.
            if (delta != 0 && Math.signum(delta) != Math.signum(accumulated)) {
                accumulated = 0.0;
            }

            accumulated += delta;

            if (Math.abs(accumulated) < threshold) {
                return 0;
            }

            int direction = accumulated > 0 ? 1 : -1;
            accumulated = 0.0;
            return direction;
        }
    }
}
