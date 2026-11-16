package com.allotech.dao;

import com.allotech.model.Role;
import com.allotech.model.StatutCompte;
import com.allotech.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Outil des tests : cree et supprime des donnees de test directement en JDBC.
 * Chaque test nettoie ce qu'il a cree, pour ne pas polluer la base de developpement.
 */
public final class TestData {

    private TestData() {
    }

    /** Cree un utilisateur (et sa ligne client si role = CLIENT). @return son id */
    public static int creerUtilisateur(Role role, StatutCompte statut) {
        String email = "junit_" + System.nanoTime() + "@test.ma";
        try (Connection cnx = DBConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(
                     "INSERT INTO utilisateur (nom_complet, email, mot_de_passe, ville, role, statut_compte) "
                             + "VALUES (?, ?, 'x', 'VilleTest', ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "Utilisateur Test");
            ps.setString(2, email);
            ps.setString(3, role.name());
            ps.setString(4, statut.name());
            ps.executeUpdate();
            try (ResultSet cles = ps.getGeneratedKeys()) {
                cles.next();
                int id = cles.getInt(1);
                if (role == Role.CLIENT) {
                    try (PreparedStatement p2 = cnx.prepareStatement("INSERT INTO client (id) VALUES (?)")) {
                        p2.setInt(1, id);
                        p2.executeUpdate();
                    }
                }
                return id;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Creation de l'utilisateur de test impossible", e);
        }
    }

    /** Cree un technicien APPROUVE rattache a la categorie donnee. @return son id */
    public static int creerTechnicien(int categorieId) {
        int id = creerUtilisateur(Role.TECHNICIEN, StatutCompte.APPROUVE);
        try (Connection cnx = DBConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(
                     "INSERT INTO technicien (id, categorie_id, cin, photo_cin) VALUES (?, ?, ?, 'x.jpg')")) {
            ps.setInt(1, id);
            ps.setInt(2, categorieId);
            ps.setString(3, "T" + System.nanoTime() % 1_000_000_000L);
            ps.executeUpdate();
            return id;
        } catch (SQLException e) {
            throw new IllegalStateException("Creation du technicien de test impossible", e);
        }
    }

    /** Supprime un utilisateur (ses lignes client/technicien partent en cascade). */
    public static void supprimerUtilisateur(int id) {
        executer("DELETE FROM utilisateur WHERE id = ?", id);
    }

    public static void supprimerCategorie(int id) {
        executer("DELETE FROM categorie WHERE id = ?", id);
    }

    private static void executer(String sql, int id) {
        try (Connection cnx = DBConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Nettoyage impossible", e);
        }
    }
}
