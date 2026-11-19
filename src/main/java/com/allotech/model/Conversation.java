package com.allotech.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Conversation {

    private int id;
    private int demandeId;
    private LocalDateTime dateCreation;

    public Conversation() {
    }

    public Conversation(int id, int demandeId, LocalDateTime dateCreation) {
        this.id = id;
        this.demandeId = demandeId;
        this.dateCreation = dateCreation;
    }

    public Conversation(int demandeId, LocalDateTime dateCreation) {
        this(0, demandeId, dateCreation);
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

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Conversation that)) return false;
        return id != 0 && id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Conversation{" +
                "id=" + id +
                ", demandeId=" + demandeId +
                ", dateCreation=" + dateCreation +
                '}';
    }
}