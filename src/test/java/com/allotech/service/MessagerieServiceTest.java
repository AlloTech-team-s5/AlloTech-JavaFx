package com.allotech.service;

import com.allotech.model.Conversation;
import com.allotech.model.Message;
import com.allotech.model.Offre;
import com.allotech.model.StatutOffre;
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

class MessagerieServiceTest {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/allotech?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    private MessagerieService messagerieService;
    private int clientId;
    private int technicienId;
    private int demandeAccepteeId;

    @BeforeEach
    void setUp() {
        messagerieService = new MessagerieService();
        String suffix = String.valueOf(System.nanoTime());

        try (Connection cnx = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            int categorieId = insertCategorie(cnx, "TestCatMsg_" + suffix);
            clientId = insertClient(cnx, "client_msg_" + suffix + "@test.ma", "Rabat");
            technicienId = insertTechnicien(cnx, "tech_msg_" + suffix + "@test.ma", "CIN_MSG_" + suffix, categorieId, "Rabat");
            int problemeId = insertProbleme(cnx, clientId, "Panne électrique", "Test messagerie");
            demandeAccepteeId = insertDemande(cnx, problemeId, technicienId, "ACCEPTEE");
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur préparation test messagerie", e);
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
    void ouvrirConversationEnvoyerMessageEtAccepterOffre() {
        Conversation conv = messagerieService.ouvrirConversationSurAcceptation(demandeAccepteeId);
        assertNotNull(conv);
        assertTrue(conv.getId() > 0);

        Message msg = messagerieService.envoyerMessage(conv.getId(), clientId, "Bonjour, quel est votre tarif ?");
        assertNotNull(msg);
        assertEquals("Bonjour, quel est votre tarif ?", msg.getContenu());
        assertFalse(msg.isLu());

        messagerieService.marquerMessagesLus(conv.getId(), technicienId);
        assertEquals(1, messagerieService.listerMessages(conv.getId()).size());
        assertTrue(messagerieService.listerMessages(conv.getId()).get(0).isLu());

        Offre offre = messagerieService.proposerOffre(conv.getId(), 250.0);
        assertEquals(StatutOffre.PROPOSEE, offre.getStatut());

        Offre acceptee = messagerieService.accepterOffre(offre.getId());
        assertEquals(StatutOffre.ACCEPTEE, acceptee.getStatut());

        Double prixConvenu = getPrixConvenu(demandeAccepteeId);
        assertNotNull(prixConvenu);
        assertEquals(250.0, prixConvenu, 0.01);
    }

    @Test
    void refuserOffreNegativeOuNulle() {
        Conversation conv = messagerieService.ouvrirConversationSurAcceptation(demandeAccepteeId);
        assertThrows(IllegalArgumentException.class, () -> messagerieService.proposerOffre(conv.getId(), 0.0));
        assertThrows(IllegalArgumentException.class, () -> messagerieService.proposerOffre(conv.getId(), -100.0));
    }

    private Double getPrixConvenu(int demId) {
        try (Connection cnx = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = cnx.prepareStatement("SELECT prix_convenu FROM demande WHERE id = ?")) {
            ps.setInt(1, demId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                double val = rs.getDouble("prix_convenu");
                return rs.wasNull() ? null : val;
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
        String sql = "INSERT INTO utilisateur (nom_complet, email, mot_de_passe, ville, role, statut_compte) VALUES ('Client Msg', ?, '$2a$10$hash', ?, 'CLIENT', 'APPROUVE')";
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
        try (PreparedStatement ps = cnx.prepareStatement("INSERT INTO client (id, adresse) VALUES (?, 'Adresse Msg')")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
        return userId;
    }

    private int insertTechnicien(Connection cnx, String email, String cin, int categorieId, String ville) throws SQLException {
        String sqlUser = "INSERT INTO utilisateur (nom_complet, email, mot_de_passe, ville, role, statut_compte) VALUES ('Tech Msg', ?, '$2a$10$hash', ?, 'TECHNICIEN', 'APPROUVE')";
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

    private int insertDemande(Connection cnx, int probId, int techId, String statut) throws SQLException {
        String sql = "INSERT INTO demande (probleme_id, technicien_id, date_expiration, statut) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, probId);
            ps.setInt(2, techId);
            ps.setObject(3, LocalDateTime.now().plusHours(24));
            ps.setString(4, statut);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}