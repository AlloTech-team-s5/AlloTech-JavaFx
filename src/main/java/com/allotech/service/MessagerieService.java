package com.allotech.service;

import com.allotech.dao.BaseDAO;
import com.allotech.dao.ConversationDAO;
import com.allotech.dao.MessageDAO;
import com.allotech.dao.OffreDAO;
import com.allotech.model.Conversation;
import com.allotech.model.Message;
import com.allotech.model.Offre;
import com.allotech.model.StatutOffre;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class MessagerieService extends BaseDAO {

    private final ConversationDAO conversationDAO;
    private final MessageDAO messageDAO;
    private final OffreDAO offreDAO;

    public MessagerieService() {
        this(new ConversationDAO(), new MessageDAO(), new OffreDAO());
    }

    public MessagerieService(ConversationDAO conversationDAO, MessageDAO messageDAO, OffreDAO offreDAO) {
        super("la messagerie");
        this.conversationDAO = conversationDAO;
        this.messageDAO = messageDAO;
        this.offreDAO = offreDAO;
    }

    public Conversation ouvrirConversationSurAcceptation(int demandeId) {
        if (demandeId <= 0) {
            throw new IllegalArgumentException("Identifiant de demande invalide.");
        }
        return inTransaction(cnx -> {
            Optional<Conversation> existante = queryOne(
                    cnx,
                    "SELECT id, demande_id, date_creation FROM conversation WHERE demande_id = ?",
                    rs -> new Conversation(
                            rs.getInt("id"),
                            rs.getInt("demande_id"),
                            getDateTime(rs, "date_creation")
                    ),
                    demandeId
            );
            if (existante.isPresent()) {
                return existante.get();
            }

            String statutDemande = queryOne(
                    cnx,
                    "SELECT statut FROM demande WHERE id = ?",
                    rs -> rs.getString("statut"),
                    demandeId
            ).orElseThrow(() -> new IllegalArgumentException("Demande introuvable."));

            if (!"ACCEPTEE".equals(statutDemande)) {
                throw new IllegalStateException("La conversation ne peut être ouverte que pour une demande acceptée.");
            }

            LocalDateTime dateCreation = LocalDateTime.now();
            int id = executeInsert(
                    cnx,
                    "INSERT INTO conversation (demande_id, date_creation) VALUES (?, ?)",
                    demandeId,
                    dateCreation
            );
            return new Conversation(id, demandeId, dateCreation);
        });
    }

    public Optional<Conversation> trouverConversationParDemande(int demandeId) {
        return conversationDAO.findByDemandeId(demandeId);
    }

    public List<Conversation> listerConversationsUtilisateur(int utilisateurId) {
        return conversationDAO.findByUtilisateurId(utilisateurId);
    }

    public Message envoyerMessage(int conversationId, int expediteurId, String contenu) {
        if (conversationId <= 0 || expediteurId <= 0) {
            throw new IllegalArgumentException("Conversation ou expéditeur invalide.");
        }
        if (contenu == null || contenu.isBlank()) {
            throw new IllegalArgumentException("Le contenu du message ne peut pas être vide.");
        }
        if (!exists("SELECT COUNT(*) FROM conversation WHERE id = ?", conversationId)) {
            throw new IllegalArgumentException("Conversation introuvable.");
        }

        Message message = new Message(
                conversationId,
                expediteurId,
                contenu.trim(),
                LocalDateTime.now(),
                false
        );
        return messageDAO.insert(message);
    }

    public List<Message> listerMessages(int conversationId) {
        return messageDAO.findByConversationId(conversationId);
    }

    public int marquerMessagesLus(int conversationId, int lecteurId) {
        return messageDAO.marquerCommeLus(conversationId, lecteurId);
    }

    public Offre proposerOffre(int conversationId, double montant) {
        if (conversationId <= 0) {
            throw new IllegalArgumentException("Conversation invalide.");
        }
        if (montant <= 0) {
            throw new IllegalArgumentException("Le montant de l'offre doit être strictement positif (en DH).");
        }

        return inTransaction(cnx -> {
            if (count(cnx, "SELECT COUNT(*) FROM conversation WHERE id = ?", conversationId) == 0) {
                throw new IllegalArgumentException("Conversation introuvable.");
            }

            LocalDateTime maintenant = LocalDateTime.now();
            executeUpdate(
                    cnx,
                    "UPDATE offre SET statut = ?, date_reponse = ? WHERE conversation_id = ? AND statut = ?",
                    StatutOffre.ANNULEE,
                    maintenant,
                    conversationId,
                    StatutOffre.PROPOSEE
            );

            int id = executeInsert(
                    cnx,
                    "INSERT INTO offre (conversation_id, montant, statut, date_proposition, date_reponse) VALUES (?, ?, ?, ?, ?)",
                    conversationId,
                    montant,
                    StatutOffre.PROPOSEE,
                    maintenant,
                    null
            );
            return new Offre(id, conversationId, montant, StatutOffre.PROPOSEE, maintenant, null);
        });
    }

    public Offre accepterOffre(int offreId) {
        return inTransaction(cnx -> {
            Offre offre = queryOne(
                    cnx,
                    "SELECT id, conversation_id, montant, statut, date_proposition, date_reponse FROM offre WHERE id = ?",
                    rs -> new Offre(
                            rs.getInt("id"),
                            rs.getInt("conversation_id"),
                            rs.getDouble("montant"),
                            getEnum(rs, "statut", StatutOffre.class),
                            getDateTime(rs, "date_proposition"),
                            getDateTime(rs, "date_reponse")
                    ),
                    offreId
            ).orElseThrow(() -> new IllegalArgumentException("Offre introuvable."));

            if (offre.getStatut() != StatutOffre.PROPOSEE) {
                throw new IllegalStateException("Seule une offre proposée peut être acceptée.");
            }

            LocalDateTime dateReponse = LocalDateTime.now();
            executeUpdate(
                    cnx,
                    "UPDATE offre SET statut = ?, date_reponse = ? WHERE id = ?",
                    StatutOffre.ACCEPTEE,
                    dateReponse,
                    offreId
            );

            executeUpdate(
                    cnx,
                    """
                    UPDATE demande d
                    JOIN conversation c ON c.demande_id = d.id
                    SET d.prix_convenu = ?
                    WHERE c.id = ?
                    """,
                    offre.getMontant(),
                    offre.getConversationId()
            );

            offre.setStatut(StatutOffre.ACCEPTEE);
            offre.setDateReponse(dateReponse);
            return offre;
        });
    }

    public Offre refuserOffre(int offreId) {
        return inTransaction(cnx -> {
            Offre offre = queryOne(
                    cnx,
                    "SELECT id, conversation_id, montant, statut, date_proposition, date_reponse FROM offre WHERE id = ?",
                    rs -> new Offre(
                            rs.getInt("id"),
                            rs.getInt("conversation_id"),
                            rs.getDouble("montant"),
                            getEnum(rs, "statut", StatutOffre.class),
                            getDateTime(rs, "date_proposition"),
                            getDateTime(rs, "date_reponse")
                    ),
                    offreId
            ).orElseThrow(() -> new IllegalArgumentException("Offre introuvable."));

            if (offre.getStatut() != StatutOffre.PROPOSEE) {
                throw new IllegalStateException("Seule une offre proposée peut être refusée.");
            }

            LocalDateTime dateReponse = LocalDateTime.now();
            executeUpdate(
                    cnx,
                    "UPDATE offre SET statut = ?, date_reponse = ? WHERE id = ?",
                    StatutOffre.REFUSEE,
                    dateReponse,
                    offreId
            );

            offre.setStatut(StatutOffre.REFUSEE);
            offre.setDateReponse(dateReponse);
            return offre;
        });
    }

    public Optional<Offre> trouverOffreActive(int conversationId) {
        return offreDAO.findOffreActive(conversationId);
    }

    public List<Offre> listerOffresConversation(int conversationId) {
        return offreDAO.findByConversationId(conversationId);
    }
}