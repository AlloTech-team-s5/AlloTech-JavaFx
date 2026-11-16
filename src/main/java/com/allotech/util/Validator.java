package com.allotech.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Verifications de saisie communes a toute l'equipe.
 * Toutes les methodes sont statiques et renvoient true/false (jamais d'exception).
 *
 * <pre>
 *     if (!Validator.estEmailValide(txtEmail.getText())) {
 *         AlertUtil.erreur("L'adresse e-mail n'est pas valide.");
 *         return;
 *     }
 * </pre>
 */
public final class Validator {

    private static final Pattern EMAIL = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");

    /** Numero marocain : 06 12 34 56 78, 07..., 05... ou +212 6 12 34 56 78 (espaces, points et tirets acceptes). */
    private static final Pattern TELEPHONE = Pattern.compile("^(0|\\+212)[5-7][0-9]{8}$");

    private Validator() {
        // classe utilitaire : pas d'instance
    }

    /** Vrai si le texte n'est pas null et contient autre chose que des espaces. */
    public static boolean estNonVide(String texte) {
        return texte != null && !texte.trim().isEmpty();
    }

    /** Vrai si la longueur du texte (sans espaces autour) est comprise entre min et max (inclus). */
    public static boolean longueurEntre(String texte, int min, int max) {
        if (texte == null) {
            return false;
        }
        int longueur = texte.trim().length();
        return longueur >= min && longueur <= max;
    }

    /** Vrai si l'adresse e-mail a un format correct. */
    public static boolean estEmailValide(String email) {
        return email != null && EMAIL.matcher(email.trim()).matches();
    }

    /** Vrai si le numero de telephone marocain est valide. */
    public static boolean estTelephoneValide(String telephone) {
        if (telephone == null) {
            return false;
        }
        String nettoye = telephone.replaceAll("[\\s.\\-]", "");
        return TELEPHONE.matcher(nettoye).matches();
    }

    /** Vrai si le nombre est strictement positif (montant d'une offre, par exemple). */
    public static boolean estPositif(double valeur) {
        return valeur > 0;
    }

    /** Vrai si la note est comprise entre 1 et 5 (RG11). */
    public static boolean estNoteValide(int note) {
        return note >= 1 && note <= 5;
    }

    /**
     * Vrai si le texte est un lien Google Maps (RG15). Formes acceptees :
     * https://www.google.com/maps/..., https://maps.google.com/..., https://maps.app.goo.gl/...,
     * https://goo.gl/maps/...
     */
    public static boolean estLienGoogleMaps(String lien) {
        if (!estNonVide(lien)) {
            return false;
        }
        try {
            URI uri = new URI(lien.trim());
            String schema = uri.getScheme();
            String hote = uri.getHost();
            if (schema == null || hote == null || !schema.equalsIgnoreCase("https")) {
                return false;
            }
            hote = hote.toLowerCase(Locale.ROOT);
            String chemin = uri.getPath() == null ? "" : uri.getPath();

            if (hote.equals("maps.app.goo.gl") || hote.equals("maps.google.com")) {
                return true;
            }
            boolean hoteGoogle = hote.equals("google.com") || hote.startsWith("www.google.");
            if (hoteGoogle && chemin.startsWith("/maps")) {
                return true;
            }
            return hote.equals("goo.gl") && chemin.startsWith("/maps");
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
