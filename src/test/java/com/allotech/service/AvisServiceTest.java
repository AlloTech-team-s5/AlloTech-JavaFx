package com.allotech.service;

import com.allotech.model.Avis;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AvisServiceTest {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/allotech?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    private AvisService avisService;
    private int clientId;
    private int technicienId;
    private int demandeAccepteeId;

    @BeforeEach
    void setUp() {
        avisService = new AvisService();
        String suffix = String.valueOf(System.nanoTime());

        try (Connection cnx = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            int categorieId = insertCategorie(cnx, "TestCat_" + suffix);
            clientId = insertClient(cnx, "client_" + suffix + "@test.ma", "Casablanca");
            technicienId = insertTechnicien(cnx, "tech_" + suffix + "@test.ma", "CIN_" + suffix, categorieId, "Casablanca");
            int problemeId = insertProbleme(cnx, clientId, "Fuite d'eau", "Test problème");
            demandeAccepteeId = insertDemande(cnx, problemeId, technicienId, "ACCEPTEE", null);
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur préparation test avis", e);
        }
    }

    @AfterEach
    void tearDown() {
        try (Connection cnx = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            try (PreparedStatement ps = cnx.prepareStatement("DELETE FROM demande WHERE id = ?")) {
                ps.setInt(1, demandeAccepteeId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = cnx.prepareStatement("DELETE FROM utilisateur WHERE id IN (?, ?)")) {
                ps.setInt(1, clientId);
                ps.setInt(2, technicienId);
                ps.executeUpdate();
            }
        } catch (SQLException ignored) {
        }
    }

    @Test
    void ajouterAvisValideEtCalculerMoyenneTechnicien() {
        Avis avis1 = avisService.ajouterAvis(demandeAccepteeId, clientId, technicienId, 4, "Très bon travail");
        assertNotNull(avis1);
        assertTrue(avis1.getId() > 0);
        assertEquals(4, avis1.getNote());

        double score = getScoreTechnicien(technicienId);
        assertEquals(4.00, score, 0.01);
    }

    @Test
    void refuserDeuxAvisPourMemeDemande() {
        avisService.ajouterAvis(demandeAccepteeId, clientId, technicienId, 5, "Excellent");

        assertThrows(IllegalStateException.class, () ->
                avisService.ajouterAvis(demandeAccepteeId, clientId, technicienId, 3, "Deuxième avis")
        );
    }

    @Test
    void refuserNoteHorsIntervalleUnCinq() {
        assertThrows(IllegalArgumentException.class, () ->
                avisService.ajouterAvis(demandeAccepteeId, clientId, technicienId, 0, "Note trop basse")
        );
        assertThrows(IllegalArgumentException.class, () ->
                avisService.ajouterAvis(demandeAccepteeId, clientId, technicienId, 6, "Note trop haute")
        );
    }

    private double getScoreTechnicien(int techId) {
        try (Connection cnx = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = cnx.prepareStatement("SELECT score_moyenne FROM technicien WHERE id = ?")) {
            ps.setInt(1, techId);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return rs.getDouble("score_moyenne");
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private int insertCategorie(Connection cnx, String nom) throws SQLException {
        String sql = "INSERT INTO categorie (nom, actif) VALUES (?, TRUE)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nom);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private int insertClient(Connection cnx, String email, String ville) throws SQLException {
        String sql = "INSERT INTO utilisateur (nom_complet, email, mot_de_passe, ville, role, statut_compte) VALUES ('Client Test', ?, '$2a$10$hash', ?, 'CLIENT', 'APPROUVE')";
        int userId;
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, email);
            ps.setString(2, ville);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                userId = rs.getInt(1);
            }
        }
        try (PreparedStatement ps = cnx.prepareStatement("INSERT INTO client (id, adresse) VALUES (?, 'Adresse Test')")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
        return userId;
    }

    private int insertTechnicien(Connection cnx, String email, String cin, int categorieId, String ville) throws SQLException {
        String sqlUser = "INSERT INTO utilisateur (nom_complet, email, mot_de_passe, ville, role, statut_compte) VALUES ('Tech Test', ?, '$2a$10$hash', ?, 'TECHNICIEN', 'APPROUVE')";
        int userId;
        try (PreparedStatement ps = cnx.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, email);
            ps.setString(2, ville);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                userId = rs.getInt(1);
            }
        }
        String sqlTech = "INSERT INTO technicien (id, categorie_id, cin, photo_cin, score_moyenne, nombre_soumissions) VALUES (?, ?, ?, 'cin.png', 0.00, 1)";
        try (PreparedStatement ps = cnx.prepareStatement(sqlTech)) {
            ps.setInt(1, userId);
            ps.setInt(2, categorieId);
            ps.setString(3, cin);
            ps.executeUpdate();
        }
        return userId;
    }

    private int insertProbleme(Connection cnx, int cliId, String titre, String desc) throws SQLException {
        String sql = "INSERT INTO probleme (client_id, titre, description) VALUES (?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, cliId);
            ps.setString(2, titre);
            ps.setString(3, desc);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private int insertDemande(Connection cnx, int probId, int techId, String statut, Double prixConvenu) throws SQLException {
        String sql = "INSERT INTO demande (probleme_id, technicien_id, date_expiration, statut, prix_convenu) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, probId);
            ps.setInt(2, techId);
            ps.setObject(3, LocalDateTime.now().plusHours(24));
            ps.setString(4, statut);
            if (prixConvenu == null) {
                ps.setNull(5, java.sql.Types.DECIMAL);
            } else {
                ps.setDouble(5, prixConvenu);
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}