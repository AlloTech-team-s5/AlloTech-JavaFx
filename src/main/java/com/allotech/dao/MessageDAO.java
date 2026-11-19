package com.allotech.dao;

import com.allotech.model.Message;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class MessageDAO extends BaseDAO implements CrudDAO<Message> {

    private static final RowMapper<Message> MAPPER = rs -> new Message(
            rs.getInt("id"),
            rs.getInt("conversation_id"),
            rs.getInt("expediteur_id"),
            rs.getString("contenu"),
            getDateTime(rs, "date_envoi"),
            rs.getBoolean("lu")
    );

    public MessageDAO() {
        super("les messages");
    }

    @Override
    public List<Message> findAll() {
        return queryList("SELECT * FROM message ORDER BY date_envoi ASC, id ASC", MAPPER);
    }

    @Override
    public Optional<Message> findById(int id) {
        return queryOne("SELECT * FROM message WHERE id = ?", MAPPER, id);
    }

    public List<Message> findByConversationId(int conversationId) {
        return queryList(
                "SELECT * FROM message WHERE conversation_id = ? ORDER BY date_envoi ASC, id ASC",
                MAPPER,
                conversationId
        );
    }

    public int marquerCommeLus(int conversationId, int lecteurId) {
        return executeUpdate(
                "UPDATE message SET lu = TRUE WHERE conversation_id = ? AND expediteur_id <> ? AND lu = FALSE",
                conversationId,
                lecteurId
        );
    }

    public long countNonLus(int conversationId, int lecteurId) {
        return count(
                "SELECT COUNT(*) FROM message WHERE conversation_id = ? AND expediteur_id <> ? AND lu = FALSE",
                conversationId,
                lecteurId
        );
    }

    @Override
    public Message insert(Message message) {
        LocalDateTime date = message.getDateEnvoi() != null
                ? message.getDateEnvoi()
                : LocalDateTime.now();
        int id = executeInsert(
                "INSERT INTO message (conversation_id, expediteur_id, contenu, date_envoi, lu) VALUES (?, ?, ?, ?, ?)",
                message.getConversationId(),
                message.getExpediteurId(),
                message.getContenu(),
                date,
                message.isLu()
        );
        message.setId(id);
        message.setDateEnvoi(date);
        return message;
    }

    @Override
    public boolean update(Message message) {
        return executeUpdate(
                "UPDATE message SET contenu = ?, lu = ? WHERE id = ?",
                message.getContenu(),
                message.isLu(),
                message.getId()
        ) > 0;
    }

    @Override
    public boolean delete(int id) {
        return executeUpdate("DELETE FROM message WHERE id = ?", id) > 0;
    }
}