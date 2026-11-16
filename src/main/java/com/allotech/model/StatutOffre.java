package com.allotech.model;

/**
 * Statut d'une offre de prix dans une conversation.
 * Les valeurs portent exactement les memes noms que dans la colonne ENUM de MySQL (schema.sql).
 * Utiliser {@link #getLibelle()} pour l'affichage a l'ecran.
 */
public enum StatutOffre {
    PROPOSEE("Proposée"),
    ACCEPTEE("Acceptée"),
    REFUSEE("Refusée"),
    ANNULEE("Annulée");

    private final String libelle;

    StatutOffre(String libelle) {
        this.libelle = libelle;
    }

    /** Texte lisible pour l'interface (ex : "En attente"). */
    public String getLibelle() {
        return libelle;
    }

    /** Convertit le texte lu dans MySQL en enumeration (null reste null). */
    public static StatutOffre fromString(String valeur) {
        return valeur == null ? null : StatutOffre.valueOf(valeur);
    }

    @Override
    public String toString() {
        return libelle;
    }
}
