package mg.emit.picneo.tenten.util;

import java.io.File;

/**
 * Nom des copies numérotées produites par "Enregistrer" :
 * « photo.png » devient « photo(1).png », « photo(2).png », etc.
 */
public final class File_Naming_Util {

    private File_Naming_Util() {
    }

    /**
     * Nom du fichier sans son extension (« photo » pour « photo.png »).
     * Un point en début de nom n'est pas une extension (« .bashrc »).
     */
    public static String baseName(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            return fileName.substring(0, lastDot);
        }
        return fileName;
    }

    /**
     * Extension avec son point (« .png »), vide s'il n'y en a pas.
     */
    public static String extension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            return fileName.substring(lastDot);
        }
        return "";
    }

    /**
     * Nom de la copie n° {@code n} : « photo(3).png ».
     */
    public static String numberedName(String fileName, int n) {
        return baseName(fileName) + "(" + n + ")" + extension(fileName);
    }

    /**
     * Première copie libre « base(n).ext » du répertoire, en excluant
     * éventuellement le fichier source lui-même.
     *
     * @param directory répertoire cible
     * @param fileName nom du fichier source (avec extension)
     * @param excludedAbsolute fichier à ne jamais proposer (fichier d'origine), ou null
     * @return le fichier « base(1).ext », « base(2).ext »… qui n'existe pas encore
     */
    public static File firstFreeNumberedCopy(String directory, String fileName, File excludedAbsolute) {
        int n = 1;
        File candidate;
        do {
            candidate = new File(directory, numberedName(fileName, n));
            n++;
        } while (candidate.exists()
                || (excludedAbsolute != null && candidate.getAbsoluteFile().equals(excludedAbsolute)));
        return candidate;
    }
}
