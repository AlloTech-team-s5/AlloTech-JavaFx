package com.allotech.model;

/**
 * Role d'un utilisateur.
 * Les valeurs portent exactement les memes noms que dans la colonne ENUM de MySQL (schema.sql).
 * Utiliser {@link #getLibelle()} pour l'affichage a l'ecran.
 */
public enum Role {
    CLIENT("Client"),
    TECHNICIEN("Technicien"),
    ADMIN("Administrateur");

    private final String libelle;

    Role(String libelle) {
        this.libelle = libelle;
    }

    /** Texte lisible pour l'interface (ex : "En attente"). */
    public String getLibelle() {
        return libelle;
    }

    /** Convertit le texte lu dans MySQL en enumeration (null reste null). */
    public static Role fromString(String valeur) {
        return valeur == null ? null : Role.valueOf(valeur);
    }

    @Override
    public String toString() {
        return libelle;
    }
}
