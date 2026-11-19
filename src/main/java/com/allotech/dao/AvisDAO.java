package com.allotech.dao;

import com.allotech.model.Avis;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class AvisDAO extends BaseDAO implements CrudDAO<Avis> {

    private static final RowMapper<Avis> MAPPER = rs -> new Avis(
            rs.getInt("id"),
            rs.getInt("demande_id"),
            rs.getInt("client_id"),
            rs.getInt("technicien_id"),
            rs.getInt("note"),
            rs.getString("commentaire"),
            getDate(rs, "date_avis")
    );

    public AvisDAO() {
        super("les avis");
    }

    @Override
    public List<Avis> findAll() {
        return queryList("SELECT * FROM avis ORDER BY date_avis DESC, id DESC", MAPPER);
    }

    @Override
    public Optional<Avis> findById(int id) {
        return queryOne("SELECT * FROM avis WHERE id = ?", MAPPER, id);
    }

    public Optional<Avis> findByDemandeId(int demandeId) {
        return queryOne("SELECT * FROM avis WHERE demande_id = ?", MAPPER, demandeId);
    }

    public boolean existsByDemandeId(int demandeId) {
        return exists("SELECT COUNT(*) FROM avis WHERE demande_id = ?", demandeId);
    }

    public List<Avis> findByTechnicienId(int technicienId) {
        return queryList(
                "SELECT * FROM avis WHERE technicien_id = ? ORDER BY date_avis DESC, id DESC",
                MAPPER,
                technicienId
        );
    }

    public List<Avis> findByClientId(int clientId) {
        return queryList(
                "SELECT * FROM avis WHERE client_id = ? ORDER BY date_avis DESC, id DESC",
                MAPPER,
                clientId
        );
    }

    public double calculerMoyenneTechnicien(int technicienId) {
        return queryOne(
                "SELECT COALESCE(AVG(note), 0.0) FROM avis WHERE technicien_id = ?",
                rs -> rs.getDouble(1),
                technicienId
        ).orElse(0.0);
    }

    @Override
    public Avis insert(Avis avis) {
        LocalDate date = avis.getDateAvis() != null ? avis.getDateAvis() : LocalDate.now();
        int id = executeInsert(
                "INSERT INTO avis (demande_id, client_id, technicien_id, note, commentaire, date_avis) VALUES (?, ?, ?, ?, ?, ?)",
                avis.getDemandeId(),
                avis.getClientId(),
                avis.getTechnicienId(),
                avis.getNote(),
                avis.getCommentaire(),
                date
        );
        avis.setId(id);
        avis.setDateAvis(date);
        return avis;
    }

    @Override
    public boolean update(Avis avis) {
        return executeUpdate(
                "UPDATE avis SET note = ?, commentaire = ? WHERE id = ?",
                avis.getNote(),
                avis.getCommentaire(),
                avis.getId()
        ) > 0;
    }

    @Override
    public boolean delete(int id) {
        return executeUpdate("DELETE FROM avis WHERE id = ?", id) > 0;
    }
}