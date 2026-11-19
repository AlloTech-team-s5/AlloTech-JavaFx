package com.allotech.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Offre {

    private int id;
    private int conversationId;
    private double montant;
    private StatutOffre statut;
    private LocalDateTime dateProposition;
    private LocalDateTime dateReponse;

    public Offre() {
    }

    public Offre(int id, int conversationId, double montant, StatutOffre statut,
                 LocalDateTime dateProposition, LocalDateTime dateReponse) {
        this.id = id;
        this.conversationId = conversationId;
        this.montant = montant;
        this.statut = statut;
        this.dateProposition = dateProposition;
        this.dateReponse = dateReponse;
    }

    public Offre(int conversationId, double montant, StatutOffre statut,
                 LocalDateTime dateProposition, LocalDateTime dateReponse) {
        this(0, conversationId, montant, statut, dateProposition, dateReponse);
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

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public StatutOffre getStatut() {
        return statut;
    }

    public void setStatut(StatutOffre statut) {
        this.statut = statut;
    }

    public LocalDateTime getDateProposition() {
        return dateProposition;
    }

    public void setDateProposition(LocalDateTime dateProposition) {
        this.dateProposition = dateProposition;
    }

    public LocalDateTime getDateReponse() {
        return dateReponse;
    }

    public void setDateReponse(LocalDateTime dateReponse) {
        this.dateReponse = dateReponse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Offre offre)) return false;
        return id != 0 && id == offre.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Offre{" +
                "id=" + id +
                ", conversationId=" + conversationId +
                ", montant=" + montant +
                ", statut=" + statut +
                ", dateProposition=" + dateProposition +
                '}';
    }
}