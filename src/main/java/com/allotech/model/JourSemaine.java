package com.allotech.model;

/**
 * Jour de la semaine pour les disponibilites d'un technicien.
 * Les valeurs portent exactement les memes noms que dans la colonne ENUM de MySQL (schema.sql).
 * Utiliser {@link #getLibelle()} pour l'affichage a l'ecran.
 */
public enum JourSemaine {
    LUNDI("Lundi"),
    MARDI("Mardi"),
    MERCREDI("Mercredi"),
    JEUDI("Jeudi"),
    VENDREDI("Vendredi"),
    SAMEDI("Samedi"),
    DIMANCHE("Dimanche");

    private final String libelle;

    JourSemaine(String libelle) {
        this.libelle = libelle;
    }

    /** Texte lisible pour l'interface (ex : "En attente"). */
    public String getLibelle() {
        return libelle;
    }

    /** Convertit le texte lu dans MySQL en enumeration (null reste null). */
    public static JourSemaine fromString(String valeur) {
        return valeur == null ? null : JourSemaine.valueOf(valeur);
    }

    @Override
    public String toString() {
        return libelle;
    }
}
