package com.allotech.model;

import java.util.Objects;

/**
 * Categorie de metier (Plombier, Electricien...). Correspond a la table {@code categorie}.
 *
 * <p>{@link #toString()} renvoie le nom : une {@code ComboBox<Categorie>} JavaFX affiche
 * donc directement le nom de la categorie.</p>
 */
public class Categorie {

    private int id;
    private String nom;
    private boolean actif;

    public Categorie() {
        this.actif = true;
    }

    /** Pour une nouvelle categorie (l'id sera donne par MySQL). */
    public Categorie(String nom) {
        this.nom = nom;
        this.actif = true;
    }

    public Categorie(int id, String nom, boolean actif) {
        this.id = id;
        this.nom = nom;
        this.actif = actif;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Categorie)) {
            return false;
        }
        Categorie autre = (Categorie) o;
        return id != 0 && id == autre.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nom;
    }
}
