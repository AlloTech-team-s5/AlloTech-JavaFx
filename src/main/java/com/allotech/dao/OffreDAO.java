package com.allotech.dao;

import com.allotech.model.Offre;
import com.allotech.model.StatutOffre;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class OffreDAO extends BaseDAO implements CrudDAO<Offre> {

    private static final RowMapper<Offre> MAPPER = rs -> new Offre(
            rs.getInt("id"),
            rs.getInt("conversation_id"),
            rs.getDouble("montant"),
            getEnum(rs, "statut", StatutOffre.class),
            getDateTime(rs, "date_proposition"),
            getDateTime(rs, "date_reponse")
    );

    public OffreDAO() {
        super("les offres");
    }

    @Override
    public List<Offre> findAll() {
        return queryList("SELECT * FROM offre ORDER BY date_proposition DESC, id DESC", MAPPER);
    }

    @Override
    public Optional<Offre> findById(int id) {
        return queryOne("SELECT * FROM offre WHERE id = ?", MAPPER, id);
    }

    public List<Offre> findByConversationId(int conversationId) {
        return queryList(
                "SELECT * FROM offre WHERE conversation_id = ? ORDER BY date_proposition ASC, id ASC",
                MAPPER,
                conversationId
        );
    }

    public Optional<Offre> findOffreActive(int conversationId) {
        return queryOne(
                "SELECT * FROM offre WHERE conversation_id = ? AND statut = 'PROPOSEE' ORDER BY date_proposition DESC LIMIT 1",
                MAPPER,
                conversationId
        );
    }

    public int annulerOffresProposees(int conversationId) {
        return executeUpdate(
                "UPDATE offre SET statut = 'ANNULEE', date_reponse = ? WHERE conversation_id = ? AND statut = 'PROPOSEE'",
                LocalDateTime.now(),
                conversationId
        );
    }

    @Override
    public Offre insert(Offre offre) {
        LocalDateTime dateProp = offre.getDateProposition() != null
                ? offre.getDateProposition()
                : LocalDateTime.now();
        StatutOffre statut = offre.getStatut() != null ? offre.getStatut() : StatutOffre.PROPOSEE;
        int id = executeInsert(
                "INSERT INTO offre (conversation_id, montant, statut, date_proposition, date_reponse) VALUES (?, ?, ?, ?, ?)",
                offre.getConversationId(),
                offre.getMontant(),
                statut,
                dateProp,
                offre.getDateReponse()
        );
        offre.setId(id);
        offre.setStatut(statut);
        offre.setDateProposition(dateProp);
        return offre;
    }

    @Override
    public boolean update(Offre offre) {
        return executeUpdate(
                "UPDATE offre SET montant = ?, statut = ?, date_reponse = ? WHERE id = ?",
                offre.getMontant(),
                offre.getStatut(),
                offre.getDateReponse(),
                offre.getId()
        ) > 0;
    }

    @Override
    public boolean delete(int id) {
        return executeUpdate("DELETE FROM offre WHERE id = ?", id) > 0;
    }
}