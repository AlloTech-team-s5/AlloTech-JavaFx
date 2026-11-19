package com.allotech.dao;

import com.allotech.model.Conversation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ConversationDAO extends BaseDAO implements CrudDAO<Conversation> {

    private static final RowMapper<Conversation> MAPPER = rs -> new Conversation(
            rs.getInt("id"),
            rs.getInt("demande_id"),
            getDateTime(rs, "date_creation")
    );

    public ConversationDAO() {
        super("les conversations");
    }

    @Override
    public List<Conversation> findAll() {
        return queryList("SELECT * FROM conversation ORDER BY date_creation DESC, id DESC", MAPPER);
    }

    @Override
    public Optional<Conversation> findById(int id) {
        return queryOne("SELECT * FROM conversation WHERE id = ?", MAPPER, id);
    }

    public Optional<Conversation> findByDemandeId(int demandeId) {
        return queryOne("SELECT * FROM conversation WHERE demande_id = ?", MAPPER, demandeId);
    }

    public boolean existsByDemandeId(int demandeId) {
        return exists("SELECT COUNT(*) FROM conversation WHERE demande_id = ?", demandeId);
    }

    public List<Conversation> findByUtilisateurId(int utilisateurId) {
        String sql = """
                SELECT c.*
                FROM conversation c
                JOIN demande d ON c.demande_id = d.id
                JOIN probleme p ON d.probleme_id = p.id
                WHERE p.client_id = ? OR d.technicien_id = ?
                ORDER BY c.date_creation DESC, c.id DESC
                """;
        return queryList(sql, MAPPER, utilisateurId, utilisateurId);
    }

    @Override
    public Conversation insert(Conversation conversation) {
        LocalDateTime date = conversation.getDateCreation() != null
                ? conversation.getDateCreation()
                : LocalDateTime.now();
        int id = executeInsert(
                "INSERT INTO conversation (demande_id, date_creation) VALUES (?, ?)",
                conversation.getDemandeId(),
                date
        );
        conversation.setId(id);
        conversation.setDateCreation(date);
        return conversation;
    }

    @Override
    public boolean update(Conversation conversation) {
        return executeUpdate(
                "UPDATE conversation SET demande_id = ? WHERE id = ?",
                conversation.getDemandeId(),
                conversation.getId()
        ) > 0;
    }

    @Override
    public boolean delete(int id) {
        return executeUpdate("DELETE FROM conversation WHERE id = ?", id) > 0;
    }
}