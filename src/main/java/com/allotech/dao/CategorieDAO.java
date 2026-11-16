package com.allotech.dao;

import com.allotech.model.Categorie;

import java.util.List;
import java.util.Optional;

/**
 * Acces MySQL a la table {@code categorie} : CRUD complet.
 *
 * <p>Les autres membres utilisent surtout {@link #findAllActives()} pour remplir
 * leurs listes deroulantes (inscription technicien, recherche client...).</p>
 */
public class CategorieDAO extends BaseDAO implements CrudDAO<Categorie> {

    private static final RowMapper<Categorie> MAPPER = rs ->
            new Categorie(rs.getInt("id"), rs.getString("nom"), rs.getBoolean("actif"));

    public CategorieDAO() {
        super("les catégories");
    }

    // ---------------------------------------------------------------- READ

    @Override
    public List<Categorie> findAll() {
        return queryList("SELECT id, nom, actif FROM categorie ORDER BY nom", MAPPER);
    }

    /** Seulement les categories actives (pour les listes deroulantes). */
    public List<Categorie> findAllActives() {
        return queryList("SELECT id, nom, actif FROM categorie WHERE actif = TRUE ORDER BY nom", MAPPER);
    }

    @Override
    public Optional<Categorie> findById(int id) {
        return queryOne("SELECT id, nom, actif FROM categorie WHERE id = ?", MAPPER, id);
    }

    /** Cherche par nom exact (sans tenir compte des majuscules ni des accents). */
    public Optional<Categorie> findByNom(String nom) {
        return queryOne("SELECT id, nom, actif FROM categorie WHERE nom = ?", MAPPER, nom);
    }

    /** Recherche partielle dans le nom (pour le champ de recherche de l'ecran). */
    public List<Categorie> search(String motCle) {
        return queryList(
                "SELECT id, nom, actif FROM categorie WHERE nom LIKE ? ORDER BY nom",
                MAPPER, "%" + motCle + "%");
    }

    /** Nombre de techniciens rattaches a cette categorie (pour savoir si elle est utilisee, RG14). */
    public long countTechniciens(int idCategorie) {
        return count("SELECT COUNT(*) FROM technicien WHERE categorie_id = ?", idCategorie);
    }

    // -------------------------------------------------------------- CREATE

    @Override
    public Categorie insert(Categorie categorie) {
        int id = executeInsert(
                "INSERT INTO categorie (nom, actif) VALUES (?, ?)",
                categorie.getNom(), categorie.isActif());
        categorie.setId(id);
        return categorie;
    }

    // -------------------------------------------------------------- UPDATE

    @Override
    public boolean update(Categorie categorie) {
        return executeUpdate(
                "UPDATE categorie SET nom = ?, actif = ? WHERE id = ?",
                categorie.getNom(), categorie.isActif(), categorie.getId()) > 0;
    }

    /** Active ou desactive une categorie sans toucher a son nom. */
    public boolean updateActif(int id, boolean actif) {
        return executeUpdate("UPDATE categorie SET actif = ? WHERE id = ?", actif, id) > 0;
    }

    // -------------------------------------------------------------- DELETE

    /**
     * Supprime la categorie. MySQL refuse (DataAccessException) si un technicien l'utilise :
     * dans ce cas il faut la desactiver (voir CategorieService).
     */
    @Override
    public boolean delete(int id) {
        return executeUpdate("DELETE FROM categorie WHERE id = ?", id) > 0;
    }
}
