package com.allotech.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Fenetres de dialogue simples (information, succes, erreur, confirmation).
 * A appeler depuis le fil JavaFX (c'est le cas dans les methodes de controleur).
 *
 * <pre>
 *     AlertUtil.succes("Categorie ajoutee.");
 *     if (AlertUtil.confirmer("Supprimer cette categorie ?")) { ... }
 * </pre>
 *
 * Remarque : P2 pourra enrichir cette classe (style CSS, icones) a l'iteration 3.
 */
public final class AlertUtil {

    private AlertUtil() {
        // classe utilitaire : pas d'instance
    }

    /** Message d'information. */
    public static void info(String message) {
        afficher(AlertType.INFORMATION, "Information", message);
    }

    /** Message de succes apres une action reussie. */
    public static void succes(String message) {
        afficher(AlertType.INFORMATION, "Succès", message);
    }

    /** Message d'erreur (saisie invalide, regle de gestion non respectee...). */
    public static void erreur(String message) {
        afficher(AlertType.ERROR, "Erreur", message);
    }

    /** Message d'erreur pour une erreur de base de donnees (utilise le message clair). */
    public static void erreur(DataAccessException e) {
        afficher(AlertType.ERROR, "Erreur de base de données", e.getUserMessage());
    }

    /**
     * Demande une confirmation (boutons Oui / Non).
     *
     * @return true si l'utilisateur clique sur Oui
     */
    public static boolean confirmer(String message) {
        Alert alerte = new Alert(AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alerte.setTitle("Confirmation");
        alerte.setHeaderText(null);
        Optional<ButtonType> reponse = alerte.showAndWait();
        return reponse.isPresent() && reponse.get() == ButtonType.YES;
    }

    private static void afficher(AlertType type, String titre, String message) {
        Alert alerte = new Alert(type, message, ButtonType.OK);
        alerte.setTitle(titre);
        alerte.setHeaderText(null);
        alerte.showAndWait();
    }
}
