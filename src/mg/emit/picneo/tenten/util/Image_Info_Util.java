package mg.emit.picneo.tenten.util;

/**
 * Francisation de l'aide de l'image produite par ImageInfo.getImageInfo() :
 * traduction des libellés et suppression des lignes inutiles.
 */
public final class Image_Info_Util {

    private Image_Info_Util() {
    }

    /**
     * Texte d'information de l'image, traduit et nettoyé.
     * Retourne null si {@code info} est null (comportement inchangé).
     */
    public static String toFrench(String info) {
        if (info == null) {
            return null;
        }
        return filterNoiseLines(translate(info));
    }

    /**
     * Traduit les libellés anglais de ImageInfo en français.
     */
    public static String translate(String info) {
        if (info == null) {
            return null;
        }
        return info.replace("Title:", "Titre :")
                   .replace("Width:", "Largeur :")
                   .replace("Height:", "Hauteur :")
                   .replace("Size:", "Taille :")
                   .replace("Type:", "Type :")
                   .replace("Bits per pixel:", "Bits par pixel :")
                   .replace("Display range:", "Plage d'affichage :")
                   .replace("Channels:", "Canaux :")
                   .replace("Slices:", "Coupes :")
                   .replace("Frames:", "Trames :")
                   .replace("No pixels are selected", "Aucun pixel sélectionné")
                   .replace("Selection:", "Sélection :")
                   .replace("Mean:", "Moyenne :")
                   .replace("StdDev:", "Écart type :")
                   .replace("Min:", "Min :")
                   .replace("Max:", "Max :")
                   .replace("Mode:", "Mode :")
                   .replace("Count:", "Nombre :")
                   .replace("Pixels:", "Pixels :")
                   .replace("Uncalibrated", "Non étalonné")
                   .replace("Inverted LUT", "LUT inversée")
                   .replace("Resolution:", "Résolution :")
                   .replace("Pixel size:", "Taille du pixel :")
                   .replace("Voxel size:", "Taille du voxel :")
                   .replace("Field of view:", "Champ de vision :");
    }

    /**
     * Supprime les lignes purement informatives qui n'apportent rien
     * (seuil absent, sélection absente, etc.).
     */
    public static String filterNoiseLines(String info) {
        if (info == null || info.isEmpty()) {
            return info;
        }

        String[] linesToRemove = {
            "No threshold",
            "Uncalibrated",
            "Non étalonné",
            "No overlay",
            "No selection"
        };

        StringBuilder filtered = new StringBuilder();
        for (String line : info.split("\\r?\\n")) {
            String trimmed = line.trim();
            boolean remove = false;
            for (String excluded : linesToRemove) {
                if (trimmed.equals(excluded)) {
                    remove = true;
                    break;
                }
            }
            if (!remove) {
                if (filtered.length() > 0) {
                    filtered.append("\n");
                }
                filtered.append(line);
            }
        }
        return filtered.toString();
    }
}
