package com.allotech.dao;

import java.util.List;
import java.util.Optional;

/**
 * Contrat commun des DAO qui offrent un CRUD complet.
 * Chaque membre l'implemente pour ses entites (exemple : {@code CategorieDAO implements CrudDAO<Categorie>}).
 *
 * @param <T> type de l'entite (modele)
 */
public interface CrudDAO<T> {

    /** Tous les elements. */
    List<T> findAll();

    /** Un element par son identifiant (Optional vide s'il n'existe pas). */
    Optional<T> findById(int id);

    /**
     * Ajoute l'element en base.
     *
     * @return le meme objet, avec son {@code id} genere par MySQL
     */
    T insert(T entite);

    /** Modifie l'element (identifie par son id). @return true si une ligne a ete modifiee */
    boolean update(T entite);

    /** Supprime l'element. @return true si une ligne a ete supprimee */
    boolean delete(int id);
}
