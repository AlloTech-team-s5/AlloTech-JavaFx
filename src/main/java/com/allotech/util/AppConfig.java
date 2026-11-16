package com.allotech.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Lit le fichier {@code config.properties} (place a la racine du projet, jamais sur GitHub).
 *
 * <p>Utilisation :</p>
 * <pre>
 *     int limite = AppConfig.getInt("demande.limite.par.probleme.par.jour", 3);
 *     String url = AppConfig.get("db.url");
 * </pre>
 *
 * <p>Le fichier est cherche dans cet ordre :</p>
 * <ol>
 *   <li>le chemin donne par l'option JVM {@code -Dallotech.config=chemin}</li>
 *   <li>{@code config.properties} dans le dossier courant (racine du projet)</li>
 * </ol>
 */
public final class AppConfig {

    private static final String FICHIER_PAR_DEFAUT = "config.properties";
    private static final String PROPRIETE_CHEMIN = "allotech.config";

    private static Properties proprietes;

    private AppConfig() {
        // classe utilitaire : pas d'instance
    }

    /** Charge le fichier par defaut si ce n'est pas deja fait. */
    private static synchronized Properties proprietes() {
        if (proprietes == null) {
            String chemin = System.getProperty(PROPRIETE_CHEMIN, FICHIER_PAR_DEFAUT);
            charger(Paths.get(chemin));
        }
        return proprietes;
    }

    /**
     * Charge (ou recharge) un fichier de configuration precis.
     * Utile pour les tests.
     *
     * @throws IllegalStateException si le fichier est introuvable ou illisible
     */
    public static synchronized void charger(Path fichier) {
        if (!Files.exists(fichier)) {
            throw new IllegalStateException(
                    "Fichier de configuration introuvable : " + fichier.toAbsolutePath()
                            + ". Copiez config.example.properties en config.properties "
                            + "puis renseignez votre mot de passe MySQL.");
        }
        Properties p = new Properties();
        try (Reader lecteur = Files.newBufferedReader(fichier, StandardCharsets.UTF_8)) {
            p.load(lecteur);
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de lire " + fichier.toAbsolutePath(), e);
        }
        proprietes = p;
    }

    /**
     * Charge la configuration depuis une ressource du classpath (ex : "/config.properties").
     * Non utilise par defaut, disponible si besoin pour le packaging final.
     */
    public static synchronized void chargerDepuisClasspath(String ressource) {
        try (InputStream in = AppConfig.class.getResourceAsStream(ressource)) {
            if (in == null) {
                throw new IllegalStateException("Ressource introuvable : " + ressource);
            }
            Properties p = new Properties();
            p.load(new java.io.InputStreamReader(in, StandardCharsets.UTF_8));
            proprietes = p;
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de lire la ressource " + ressource, e);
        }
    }

    /** Oublie la configuration chargee : elle sera relue au prochain acces (utile pour les tests). */
    public static synchronized void reinitialiser() {
        proprietes = null;
    }

    /**
     * Valeur obligatoire.
     *
     * @throws IllegalStateException si la cle n'existe pas
     */
    public static String get(String cle) {
        String valeur = proprietes().getProperty(cle);
        if (valeur == null) {
            throw new IllegalStateException(
                    "Cle manquante dans config.properties : " + cle);
        }
        return valeur.trim();
    }

    /** Valeur avec une valeur par defaut si la cle est absente. */
    public static String get(String cle, String parDefaut) {
        String valeur = proprietes().getProperty(cle);
        return valeur == null ? parDefaut : valeur.trim();
    }

    /** Valeur entiere avec une valeur par defaut si la cle est absente ou invalide. */
    public static int getInt(String cle, int parDefaut) {
        String valeur = proprietes().getProperty(cle);
        if (valeur == null) {
            return parDefaut;
        }
        try {
            return Integer.parseInt(valeur.trim());
        } catch (NumberFormatException e) {
            return parDefaut;
        }
    }
}
