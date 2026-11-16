package com.allotech.model;

/**
 * Statut d'une demande de service.
 * Les valeurs portent exactement les memes noms que dans la colonne ENUM de MySQL (schema.sql).
 * Utiliser {@link #getLibelle()} pour l'affichage a l'ecran.
 */
public enum StatutDemande {
    EN_ATTENTE("En attente"),
    ACCEPTEE("Acceptée"),
    REFUSEE("Refusée"),
    ANNULEE("Annulée"),
    EXPIREE("Expirée");

    private final String libelle;

    StatutDemande(String libelle) {
        this.libelle = libelle;
    }

    /** Texte lisible pour l'interface (ex : "En attente"). */
    public String getLibelle() {
        return libelle;
    }

    /** Convertit le texte lu dans MySQL en enumeration (null reste null). */
    public static StatutDemande fromString(String valeur) {
        return valeur == null ? null : StatutDemande.valueOf(valeur);
    }

    @Override
    public String toString() {
        return libelle;
    }
}
