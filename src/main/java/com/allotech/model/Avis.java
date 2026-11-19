package com.allotech.model;

import java.time.LocalDate;
import java.util.Objects;

public class Avis {

    private int id;
    private int demandeId;
    private int clientId;
    private int technicienId;
    private int note;
    private String commentaire;
    private LocalDate dateAvis;

    public Avis() {
    }

    public Avis(int id, int demandeId, int clientId, int technicienId,
                int note, String commentaire, LocalDate dateAvis) {
        this.id = id;
        this.demandeId = demandeId;
        this.clientId = clientId;
        this.technicienId = technicienId;
        this.note = note;
        this.commentaire = commentaire;
        this.dateAvis = dateAvis;
    }

    public Avis(int demandeId, int clientId, int technicienId,
                int note, String commentaire, LocalDate dateAvis) {
        this(0, demandeId, clientId, technicienId, note, commentaire, dateAvis);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDemandeId() {
        return demandeId;
    }

    public void setDemandeId(int demandeId) {
        this.demandeId = demandeId;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public int getTechnicienId() {
        return technicienId;
    }

    public void setTechnicienId(int technicienId) {
        this.technicienId = technicienId;
    }

    public int getNote() {
        return note;
    }

    public void setNote(int note) {
        this.note = note;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public LocalDate getDateAvis() {
        return dateAvis;
    }

    public void setDateAvis(LocalDate dateAvis) {
        this.dateAvis = dateAvis;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Avis avis)) return false;
        return id != 0 && id == avis.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Avis{" +
                "id=" + id +
                ", demandeId=" + demandeId +
                ", clientId=" + clientId +
                ", technicienId=" + technicienId +
                ", note=" + note +
                ", dateAvis=" + dateAvis +
                '}';
    }
}