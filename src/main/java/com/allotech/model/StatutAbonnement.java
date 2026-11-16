package com.allotech.model;

/**
 * Statut de l'abonnement d'un technicien.
 * Les valeurs portent exactement les memes noms que dans la colonne ENUM de MySQL (schema.sql).
 * Utiliser {@link #getLibelle()} pour l'affichage a l'ecran.
 */
public enum StatutAbonnement {
    ESSAI("Essai gratuit"),
    ACTIF("Actif"),
    EXPIRE("Expiré");

    private final String libelle;

    StatutAbonnement(String libelle) {
        this.libelle = libelle;
    }

    /** Texte lisible pour l'interface (ex : "En attente"). */
    public String getLibelle() {
        return libelle;
    }

    /** Convertit le texte lu dans MySQL en enumeration (null reste null). */
    public static StatutAbonnement fromString(String valeur) {
        return valeur == null ? null : StatutAbonnement.valueOf(valeur);
    }

    @Override
    public String toString() {
        return libelle;
    }
}
