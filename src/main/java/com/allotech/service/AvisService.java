package com.allotech.service;

import com.allotech.dao.AvisDAO;
import com.allotech.dao.BaseDAO;
import com.allotech.model.Avis;
import com.allotech.util.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class AvisService extends BaseDAO {

    private final AvisDAO avisDAO;

    public AvisService() {
        this(new AvisDAO());
    }

    public AvisService(AvisDAO avisDAO) {
        super("les avis");
        this.avisDAO = avisDAO;
    }

    public Avis ajouterAvis(int demandeId, int clientId, int technicienId, int note, String commentaire) {
        if (note < 1 || note > 5) {
            throw new IllegalArgumentException("La note doit être comprise entre 1 et 5.");
        }
        if (demandeId <= 0 || clientId <= 0 || technicienId <= 0) {
            throw new IllegalArgumentException("Identifiants de demande, client ou technicien invalides.");
        }

        return inTransaction(cnx -> {
            validerDemandeAccepteePourAvis(cnx, demandeId, clientId, technicienId);

            if (existsByDemandeIdTxn(cnx, demandeId)) {
                throw new IllegalStateException("Un avis a déjà été publié pour cette demande.");
            }

            String commentaireNettoye = (commentaire == null || commentaire.isBlank())
                    ? null
                    : commentaire.trim();

            LocalDate dateAvis = LocalDate.now();
            int id = executeInsert(
                    cnx,
                    "INSERT INTO avis (demande_id, client_id, technicien_id, note, commentaire, date_avis) VALUES (?, ?, ?, ?, ?, ?)",
                    demandeId,
                    clientId,
                    technicienId,
                    note,
                    commentaireNettoye,
                    dateAvis
            );

            recalculerEtMettreAJourScoreTechnicien(cnx, technicienId);

            Avis avis = new Avis(id, demandeId, clientId, technicienId, note, commentaireNettoye, dateAvis);
            return avis;
        });
    }

    public double recalculerScoreTechnicien(int technicienId) {
        return inTransaction(cnx -> recalculerEtMettreAJourScoreTechnicien(cnx, technicienId));
    }

    public List<Avis> listerAvisTechnicien(int technicienId) {
        return avisDAO.findByTechnicienId(technicienId);
    }

    public List<Avis> listerAvisClient(int clientId) {
        return avisDAO.findByClientId(clientId);
    }

    public Optional<Avis> trouverAvisParDemande(int demandeId) {
        return avisDAO.findByDemandeId(demandeId);
    }

    public boolean supprimerAvis(int avisId) {
        return inTransaction(cnx -> {
            Optional<Integer> technicienIdOpt = queryOne(
                    cnx,
                    "SELECT technicien_id FROM avis WHERE id = ?",
                    rs -> rs.getInt("technicien_id"),
                    avisId
            );
            if (technicienIdOpt.isEmpty()) {
                return false;
            }
            int technicienId = technicienIdOpt.get();
            int supprime = executeUpdate(cnx, "DELETE FROM avis WHERE id = ?", avisId);
            if (supprime > 0) {
                recalculerEtMettreAJourScoreTechnicien(cnx, technicienId);
                return true;
            }
            return false;
        });
    }

    private void validerDemandeAccepteePourAvis(Connection cnx, int demandeId, int clientId, int technicienId) {
        String sql = """
                SELECT d.statut, d.technicien_id, p.client_id
                FROM demande d
                JOIN probleme p ON d.probleme_id = p.id
                WHERE d.id = ?
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, demandeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Demande introuvable.");
                }
                String statut = rs.getString("statut");
                int demandeClientId = rs.getInt("client_id");
                int demandeTechnicienId = rs.getInt("technicien_id");

                if (!"ACCEPTEE".equals(statut)) {
                    throw new IllegalStateException("Un avis ne peut être donné que sur une demande acceptée.");
                }
                if (demandeClientId != clientId || demandeTechnicienId != technicienId) {
                    throw new IllegalArgumentException("La demande ne correspond pas à ce client et ce technicien.");
                }
            }
        } catch (SQLException e) {
            throw DataAccessException.from("Vérification de la demande pour l'avis", e);
        }
    }

    private boolean existsByDemandeIdTxn(Connection cnx, int demandeId) {
        return count(cnx, "SELECT COUNT(*) FROM avis WHERE demande_id = ?", demandeId) > 0;
    }

    private double recalculerEtMettreAJourScoreTechnicien(Connection cnx, int technicienId) {
        double moyenne = queryOne(
                cnx,
                "SELECT COALESCE(AVG(note), 0.0) FROM avis WHERE technicien_id = ?",
                rs -> rs.getDouble(1),
                technicienId
        ).orElse(0.0);

        double moyenneArrondie = Math.round(moyenne * 100.0) / 100.0;
        executeUpdate(
                cnx,
                "UPDATE technicien SET score_moyenne = ? WHERE id = ?",
                moyenneArrondie,
                technicienId
        );
        return moyenneArrondie;
    }
}