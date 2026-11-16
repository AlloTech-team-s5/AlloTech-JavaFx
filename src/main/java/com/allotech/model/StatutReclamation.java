package com.allotech.model;

/**
 * Statut d'une reclamation (signalement d'un technicien).
 * Les valeurs portent exactement les memes noms que dans la colonne ENUM de MySQL (schema.sql).
 * Utiliser {@link #getLibelle()} pour l'affichage a l'ecran.
 */
public enum StatutReclamation {
    EN_ATTENTE("En attente"),
    TRAITEE("Traitée"),
    REJETEE("Rejetée");

    private final String libelle;

    StatutReclamation(String libelle) {
        this.libelle = libelle;
    }

    /** Texte lisible pour l'interface (ex : "En attente"). */
    public String getLibelle() {
        return libelle;
    }

    /** Convertit le texte lu dans MySQL en enumeration (null reste null). */
    public static StatutReclamation fromString(String valeur) {
        return valeur == null ? null : StatutReclamation.valueOf(valeur);
    }

    @Override
    public String toString() {
        return libelle;
    }
}
