package com.allotech.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.allotech.model.Categorie;
import com.allotech.util.DataAccessException;
import com.allotech.util.DatabaseTestSupport;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CategorieDAOTest {

    private final CategorieDAO dao = new CategorieDAO();
    private final List<Integer> aNettoyer = new ArrayList<>();

    @BeforeEach
    void avant() {
        DatabaseTestSupport.exigerBaseDeDonnees();
    }

    @AfterEach
    void nettoyer() {
        for (int id : aNettoyer) {
            TestData.supprimerCategorie(id);
        }
    }

    private Categorie creer(String prefixe, boolean actif) {
        Categorie c = dao.insert(new Categorie(0, prefixe + System.nanoTime(), actif));
        aNettoyer.add(c.getId());
        return c;
    }

    @Test
    void insertDonneUnIdEtFindByIdLeRetrouve() {
        Categorie c = creer("DAO_insert_", true);
        assertNotEquals(0, c.getId());

        Categorie relue = dao.findById(c.getId()).orElseThrow();
        assertEquals(c.getNom(), relue.getNom());
        assertTrue(relue.isActif());
    }

    @Test
    void findByIdInconnuRenvoieVide() {
        assertTrue(dao.findById(-1).isEmpty());
    }

    @Test
    void updateModifieNomEtEtat() {
        Categorie c = creer("DAO_update_", true);
        c.setNom(c.getNom() + "_modif");
        c.setActif(false);
        assertTrue(dao.update(c));

        Categorie relue = dao.findById(c.getId()).orElseThrow();
        assertEquals(c.getNom(), relue.getNom());
        assertFalse(relue.isActif());
    }

    @Test
    void updateActifBascule() {
        Categorie c = creer("DAO_actif_", true);
        assertTrue(dao.updateActif(c.getId(), false));
        assertFalse(dao.findById(c.getId()).orElseThrow().isActif());
        assertTrue(dao.updateActif(c.getId(), true));
        assertTrue(dao.findById(c.getId()).orElseThrow().isActif());
    }

    @Test
    void deleteSupprime() {
        Categorie c = creer("DAO_delete_", true);
        assertTrue(dao.delete(c.getId()));
        assertTrue(dao.findById(c.getId()).isEmpty());
        assertFalse(dao.delete(c.getId()));
    }

    @Test
    void doublonDeNomLanceUneDataAccessException() {
        Categorie c = creer("DAO_doublon_", true);
        DataAccessException e = assertThrows(DataAccessException.class,
                () -> dao.insert(new Categorie(c.getNom().toUpperCase())));
        assertTrue(e.estDoublon());
        assertTrue(e.getUserMessage().contains("existe déjà"));
    }

    @Test
    void findAllActivesNeContientPasLesInactives() {
        Categorie active = creer("DAO_a_", true);
        Categorie inactive = creer("DAO_i_", false);

        List<Categorie> actives = dao.findAllActives();
        assertTrue(actives.contains(active));
        assertFalse(actives.contains(inactive));
        assertTrue(dao.findAll().contains(inactive));
    }

    @Test
    void searchTrouveParMotCle() {
        Categorie c = creer("DAO_recherche_", true);
        assertTrue(dao.search("DAO_recherche_").contains(c));
        assertFalse(dao.search("zzz_introuvable_zzz").contains(c));
    }

    @Test
    void suppressionImpossibleSiUnTechnicienUtiliseLaCategorie() {
        Categorie c = creer("DAO_utilisee_", true);
        int technicien = TestData.creerTechnicien(c.getId());
        try {
            assertEquals(1, dao.countTechniciens(c.getId()));
            DataAccessException e = assertThrows(DataAccessException.class, () -> dao.delete(c.getId()));
            assertTrue(e.estElementUtilise());
        } finally {
            TestData.supprimerUtilisateur(technicien);
        }
    }
}
