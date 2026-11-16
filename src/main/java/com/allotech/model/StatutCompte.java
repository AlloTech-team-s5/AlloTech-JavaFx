package com.allotech.model;

/**
 * Statut du compte d'un utilisateur (voir section 2.3 du cahier des charges).
 * Les valeurs portent exactement les memes noms que dans la colonne ENUM de MySQL (schema.sql).
 * Utiliser {@link #getLibelle()} pour l'affichage a l'ecran.
 */
public enum StatutCompte {
    EN_ATTENTE("En attente"),
    APPROUVE("Approuvé"),
    REFUSE("Refusé"),
    REFUSE_DEFINITIF("Refusé définitivement"),
    SUSPENDU("Suspendu");

    private final String libelle;

    StatutCompte(String libelle) {
        this.libelle = libelle;
    }

    /** Texte lisible pour l'interface (ex : "En attente"). */
    public String getLibelle() {
        return libelle;
    }

    /** Convertit le texte lu dans MySQL en enumeration (null reste null). */
    public static StatutCompte fromString(String valeur) {
        return valeur == null ? null : StatutCompte.valueOf(valeur);
    }

    @Override
    public String toString() {
        return libelle;
    }
}
