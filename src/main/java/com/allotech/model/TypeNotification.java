package com.allotech.model;

/**
 * Type d'une notification (exigence RF39).
 * Les valeurs portent exactement les memes noms que dans la colonne ENUM de MySQL (schema.sql).
 * Utiliser {@link #getLibelle()} pour l'affichage a l'ecran.
 */
public enum TypeNotification {
    NOUVELLE_DEMANDE("Nouvelle demande"),
    DEMANDE_ACCEPTEE("Demande acceptée"),
    DEMANDE_REFUSEE("Demande refusée"),
    DEMANDE_EXPIREE("Demande expirée"),
    NOUVELLE_OFFRE("Nouvelle offre de prix"),
    OFFRE_ACCEPTEE("Offre acceptée"),
    OFFRE_REFUSEE("Offre refusée"),
    NOUVEL_AVIS("Nouvel avis"),
    SIGNALEMENT("Signalement");

    private final String libelle;

    TypeNotification(String libelle) {
        this.libelle = libelle;
    }

    /** Texte lisible pour l'interface (ex : "En attente"). */
    public String getLibelle() {
        return libelle;
    }

    /** Convertit le texte lu dans MySQL en enumeration (null reste null). */
    public static TypeNotification fromString(String valeur) {
        return valeur == null ? null : TypeNotification.valueOf(valeur);
    }

    @Override
    public String toString() {
        return libelle;
    }
}
