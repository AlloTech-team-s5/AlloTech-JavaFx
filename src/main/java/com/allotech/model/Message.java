package com.allotech.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Message {

    private int id;
    private int conversationId;
    private int expediteurId;
    private String contenu;
    private LocalDateTime dateEnvoi;
    private boolean lu;

    public Message() {
    }

    public Message(int id, int conversationId, int expediteurId,
                   String contenu, LocalDateTime dateEnvoi, boolean lu) {
        this.id = id;
        this.conversationId = conversationId;
        this.expediteurId = expediteurId;
        this.contenu = contenu;
        this.dateEnvoi = dateEnvoi;
        this.lu = lu;
    }

    public Message(int conversationId, int expediteurId,
                   String contenu, LocalDateTime dateEnvoi, boolean lu) {
        this(0, conversationId, expediteurId, contenu, dateEnvoi, lu);
    }

    public void marquerCommeLu() {
        this.lu = true;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getConversationId() {
        return conversationId;
    }

    public void setConversationId(int conversationId) {
        this.conversationId = conversationId;
    }

    public int getExpediteurId() {
        return expediteurId;
    }

    public void setExpediteurId(int expediteurId) {
        this.expediteurId = expediteurId;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public LocalDateTime getDateEnvoi() {
        return dateEnvoi;
    }

    public void setDateEnvoi(LocalDateTime dateEnvoi) {
        this.dateEnvoi = dateEnvoi;
    }

    public boolean isLu() {
        return lu;
    }

    public void setLu(boolean lu) {
        this.lu = lu;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message message)) return false;
        return id != 0 && id == message.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", conversationId=" + conversationId +
                ", expediteurId=" + expediteurId +
                ", dateEnvoi=" + dateEnvoi +
                ", lu=" + lu +
                '}';
    }
}