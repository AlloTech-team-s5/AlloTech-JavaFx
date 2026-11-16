package com.allotech.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.allotech.dao.CategorieDAO;
import com.allotech.dao.TestData;
import com.allotech.model.Categorie;
import com.allotech.util.DatabaseTestSupport;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CategorieServiceTest {

    private final CategorieService service = new CategorieService();
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

    private Categorie ajouter(String nom) {
        Categorie c = service.ajouter(nom);
        aNettoyer.add(c.getId());
        return c;
    }

    @Test
    void ajouterCreeUneCategorieActiveEtRetireLesEspaces() {
        String nom = "Svc_" + System.nanoTime();
        Categorie c = ajouter("  " + nom + "  ");
        assertEquals(nom, c.getNom());
        assertTrue(c.isActif());
        assertTrue(dao.findById(c.getId()).isPresent());
    }

    @Test
    void ajouterRefuseUnNomVideOuTropCourtOuTropLong() {
        assertThrows(IllegalArgumentException.class, () -> service.ajouter(null));
        assertThrows(IllegalArgumentException.class, () -> service.ajouter("   "));
        assertThrows(IllegalArgumentException.class, () -> service.ajouter("a"));
        assertThrows(IllegalArgumentException.class, () -> service.ajouter("x".repeat(51)));
    }

    @Test
    void ajouterRefuseUnDoublonMemeEnMajuscules() {
        Categorie c = ajouter("Svc_doublon_" + System.nanoTime());
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.ajouter(c.getNom().toUpperCase()));
        assertTrue(e.getMessage().contains("existe déjà"));
    }

    @Test
    void modifierChangeLeNomEtAutoriseLeMemeNomPourLaMemeCategorie() {
        Categorie c = ajouter("Svc_modif_" + System.nanoTime());
        Categorie memeNom = service.modifier(c.getId(), c.getNom(), false);
        assertFalse(memeNom.isActif());

        String nouveau = c.getNom() + "_bis";
        Categorie modifiee = service.modifier(c.getId(), nouveau, true);
        assertEquals(nouveau, modifiee.getNom());
        assertEquals(nouveau, dao.findById(c.getId()).orElseThrow().getNom());
    }

    @Test
    void modifierRefuseLeNomDUneAutreCategorie() {
        Categorie a = ajouter("Svc_a_" + System.nanoTime());
        Categorie b = ajouter("Svc_b_" + System.nanoTime());
        assertThrows(IllegalArgumentException.class, () -> service.modifier(b.getId(), a.getNom(), true));
    }

    @Test
    void modifierUneCategorieInexistante() {
        assertThrows(IllegalArgumentException.class, () -> service.modifier(-1, "Nom valide", true));
    }

    @Test
    void desactiverPuisActiver() {
        Categorie c = ajouter("Svc_etat_" + System.nanoTime());
        service.desactiver(c.getId());
        assertFalse(dao.findById(c.getId()).orElseThrow().isActif());
        assertFalse(service.listerActives().contains(c));
        service.activer(c.getId());
        assertTrue(dao.findById(c.getId()).orElseThrow().isActif());
    }

    @Test
    void supprimerUneCategorieInutiliseeLaSupprimeVraiment() {
        Categorie c = ajouter("Svc_suppr_" + System.nanoTime());
        assertTrue(service.supprimerOuDesactiver(c.getId()));
        assertTrue(dao.findById(c.getId()).isEmpty());
    }

    @Test
    void supprimerUneCategorieUtiliseeLaDesactiveSeulement_RG14() {
        Categorie c = ajouter("Svc_rg14_" + System.nanoTime());
        int technicien = TestData.creerTechnicien(c.getId());
        try {
            assertFalse(service.supprimerOuDesactiver(c.getId()));
            Categorie relue = dao.findById(c.getId()).orElseThrow();
            assertFalse(relue.isActif());
        } finally {
            TestData.supprimerUtilisateur(technicien);
        }
    }

    @Test
    void rechercherSansMotCleRenvoieToutesLesCategories() {
        Categorie c = ajouter("Svc_tout_" + System.nanoTime());
        assertTrue(service.rechercher("").contains(c));
        assertTrue(service.rechercher(null).contains(c));
        assertTrue(service.rechercher(c.getNom().substring(0, 8)).contains(c));
    }
}
