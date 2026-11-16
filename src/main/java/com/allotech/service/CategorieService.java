package com.allotech.service;

import com.allotech.dao.CategorieDAO;
import com.allotech.model.Categorie;
import com.allotech.util.Validator;

import java.util.List;
import java.util.Optional;

/**
 * Logique metier des categories (exigence RF36, regle RG14).
 *
 * <p>Gestion des erreurs : les saisies invalides declenchent une {@link IllegalArgumentException}
 * dont le message est deja pret a afficher. Les erreurs MySQL declenchent une
 * {@link com.allotech.util.DataAccessException}.</p>
 */
public class CategorieService {

    private static final int LONGUEUR_MIN = 2;
    private static final int LONGUEUR_MAX = 50;

    private final CategorieDAO dao;

    public CategorieService() {
        this(new CategorieDAO());
    }

    /** Constructeur utilise par les tests pour injecter un DAO. */
    public CategorieService(CategorieDAO dao) {
        this.dao = dao;
    }

    // -------------------------------------------------------------- LECTURE

    public List<Categorie> lister() {
        return dao.findAll();
    }

    /** Categories actives, pour les listes deroulantes des autres ecrans. */
    public List<Categorie> listerActives() {
        return dao.findAllActives();
    }

    public List<Categorie> rechercher(String motCle) {
        if (!Validator.estNonVide(motCle)) {
            return dao.findAll();
        }
        return dao.search(motCle.trim());
    }

    // -------------------------------------------------------------- AJOUT

    /**
     * Ajoute une categorie active.
     *
     * @throws IllegalArgumentException si le nom est invalide ou deja utilise
     */
    public Categorie ajouter(String nom) {
        String nomPropre = verifierNom(nom);
        if (dao.findByNom(nomPropre).isPresent()) {
            throw new IllegalArgumentException("Une catégorie nommée « " + nomPropre + " » existe déjà.");
        }
        return dao.insert(new Categorie(nomPropre));
    }

    // -------------------------------------------------------------- MODIFICATION

    /**
     * Modifie le nom et l'etat d'une categorie.
     *
     * @throws IllegalArgumentException si la categorie n'existe pas, si le nom est invalide ou deja pris
     */
    public Categorie modifier(int id, String nom, boolean actif) {
        Categorie existante = trouver(id);
        String nomPropre = verifierNom(nom);

        Optional<Categorie> memeNom = dao.findByNom(nomPropre);
        if (memeNom.isPresent() && memeNom.get().getId() != id) {
            throw new IllegalArgumentException("Une catégorie nommée « " + nomPropre + " » existe déjà.");
        }
        existante.setNom(nomPropre);
        existante.setActif(actif);
        dao.update(existante);
        return existante;
    }

    public void desactiver(int id) {
        trouver(id);
        dao.updateActif(id, false);
    }

    public void activer(int id) {
        trouver(id);
        dao.updateActif(id, true);
    }

    // -------------------------------------------------------------- SUPPRESSION (RG14)

    /**
     * Supprime une categorie si personne ne l'utilise. Si un technicien l'utilise,
     * elle est seulement desactivee (RG14 : jamais supprimee si utilisee).
     *
     * @return true si la categorie a ete supprimee, false si elle a seulement ete desactivee
     * @throws IllegalArgumentException si la categorie n'existe pas
     */
    public boolean supprimerOuDesactiver(int id) {
        trouver(id);
        if (dao.countTechniciens(id) > 0) {
            dao.updateActif(id, false);
            return false;
        }
        return dao.delete(id);
    }

    // -------------------------------------------------------------- OUTILS

    private Categorie trouver(int id) {
        return dao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cette catégorie n'existe plus."));
    }

    private static String verifierNom(String nom) {
        if (!Validator.estNonVide(nom)) {
            throw new IllegalArgumentException("Le nom de la catégorie est obligatoire.");
        }
        if (!Validator.longueurEntre(nom, LONGUEUR_MIN, LONGUEUR_MAX)) {
            throw new IllegalArgumentException(
                    "Le nom de la catégorie doit contenir entre " + LONGUEUR_MIN + " et " + LONGUEUR_MAX + " caractères.");
        }
        return nom.trim();
    }
}
