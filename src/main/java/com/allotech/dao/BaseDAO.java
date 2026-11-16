package com.allotech.dao;

import com.allotech.util.DBConnection;
import com.allotech.util.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Classe de base de tous les DAO : elle fournit les outils JDBC pour que chaque membre
 * n'ecrive que son SQL et sa conversion ligne -> objet.
 *
 * <p><b>Ce que BaseDAO gere a ta place :</b> ouverture et fermeture de la connexion,
 * requetes preparees (protection contre l'injection SQL), transactions, traduction
 * des erreurs MySQL en {@link DataAccessException}.</p>
 *
 * <p><b>Exemple de DAO :</b></p>
 * <pre>
 * public class CategorieDAO extends BaseDAO implements CrudDAO&lt;Categorie&gt; {
 *
 *     private static final RowMapper&lt;Categorie&gt; MAPPER = rs -&gt;
 *             new Categorie(rs.getInt("id"), rs.getString("nom"), rs.getBoolean("actif"));
 *
 *     public CategorieDAO() { super("les catégories"); }
 *
 *     public List&lt;Categorie&gt; findAll() {
 *         return queryList("SELECT * FROM categorie ORDER BY nom", MAPPER);
 *     }
 *
 *     public Categorie insert(Categorie c) {
 *         int id = executeInsert("INSERT INTO categorie (nom, actif) VALUES (?, ?)", c.getNom(), c.isActif());
 *         c.setId(id);
 *         return c;
 *     }
 * }
 * </pre>
 *
 * <p><b>Parametres acceptes :</b> String, int, double, boolean, BigDecimal, LocalDate, LocalDateTime,
 * LocalTime, les enumerations (stockees par leur nom) et {@code null}.</p>
 */
public abstract class BaseDAO {

    private final String entite;

    /**
     * @param entite nom de ce que gere le DAO, avec son article, au pluriel
     *               (ex : "les catégories", "les demandes"). Sert a former les messages d'erreur :
     *               "Opération sur les catégories impossible : ..."
     */
    protected BaseDAO(String entite) {
        this.entite = entite;
    }

    private DataAccessException erreur(SQLException e) {
        return DataAccessException.from("Opération sur " + entite, e);
    }

    // =====================================================================
    //  LECTURE
    // =====================================================================

    /** Execute un SELECT et renvoie toutes les lignes sous forme de liste (vide si aucune). */
    protected <T> List<T> queryList(String sql, RowMapper<T> mapper, Object... params) {
        try (Connection cnx = DBConnection.getConnection()) {
            return queryList(cnx, sql, mapper, params);
        } catch (SQLException e) {
            throw erreur(e);
        }
    }

    /** Version qui reutilise une connexion existante (pour les transactions). */
    protected <T> List<T> queryList(Connection cnx, String sql, RowMapper<T> mapper, Object... params) {
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            lier(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> resultat = new ArrayList<>();
                while (rs.next()) {
                    resultat.add(mapper.map(rs));
                }
                return resultat;
            }
        } catch (SQLException e) {
            throw erreur(e);
        }
    }

    /** Execute un SELECT et renvoie la premiere ligne (Optional vide si aucune). */
    protected <T> Optional<T> queryOne(String sql, RowMapper<T> mapper, Object... params) {
        List<T> lignes = queryList(sql, mapper, params);
        return lignes.isEmpty() ? Optional.empty() : Optional.ofNullable(lignes.get(0));
    }

    /** Version avec connexion existante. */
    protected <T> Optional<T> queryOne(Connection cnx, String sql, RowMapper<T> mapper, Object... params) {
        List<T> lignes = queryList(cnx, sql, mapper, params);
        return lignes.isEmpty() ? Optional.empty() : Optional.ofNullable(lignes.get(0));
    }

    /**
     * Execute un SELECT qui renvoie un seul nombre (ex : {@code SELECT COUNT(*) FROM ...}).
     *
     * @return ce nombre, ou 0 si la requete ne renvoie rien
     */
    protected long count(String sql, Object... params) {
        return queryOne(sql, rs -> rs.getLong(1), params).orElse(0L);
    }

    /** Version avec connexion existante. */
    protected long count(Connection cnx, String sql, Object... params) {
        return queryOne(cnx, sql, rs -> rs.getLong(1), params).orElse(0L);
    }

    /** Vrai si le SELECT COUNT(*) donne un resultat superieur a zero. */
    protected boolean exists(String sql, Object... params) {
        return count(sql, params) > 0;
    }

    // =====================================================================
    //  ECRITURE
    // =====================================================================

    /**
     * Execute un INSERT, UPDATE ou DELETE.
     *
     * @return le nombre de lignes touchees (0 = rien n'a ete modifie)
     */
    protected int executeUpdate(String sql, Object... params) {
        try (Connection cnx = DBConnection.getConnection()) {
            return executeUpdate(cnx, sql, params);
        } catch (SQLException e) {
            throw erreur(e);
        }
    }

    /** Version avec connexion existante (pour les transactions). */
    protected int executeUpdate(Connection cnx, String sql, Object... params) {
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            lier(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw erreur(e);
        }
    }

    /**
     * Execute un INSERT et renvoie l'identifiant genere par MySQL (AUTO_INCREMENT).
     *
     * @throws DataAccessException si MySQL ne renvoie aucun identifiant
     */
    protected int executeInsert(String sql, Object... params) {
        try (Connection cnx = DBConnection.getConnection()) {
            return executeInsert(cnx, sql, params);
        } catch (SQLException e) {
            throw erreur(e);
        }
    }

    /** Version avec connexion existante (pour les transactions). */
    protected int executeInsert(Connection cnx, String sql, Object... params) {
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            lier(ps, params);
            ps.executeUpdate();
            try (ResultSet cles = ps.getGeneratedKeys()) {
                if (cles.next()) {
                    return cles.getInt(1);
                }
            }
            throw new DataAccessException("Opération sur " + entite + " impossible : aucun identifiant généré.");
        } catch (SQLException e) {
            throw erreur(e);
        }
    }

    // =====================================================================
    //  TRANSACTION
    // =====================================================================

    /**
     * Execute plusieurs requetes "tout ou rien" : si l'une echoue, toutes sont annulees (rollback).
     *
     * <p>Exemple : creer un probleme ET sa demande ensemble.</p>
     * <pre>
     *     int idDemande = inTransaction(cnx -&gt; {
     *         int idProbleme = executeInsert(cnx, "INSERT INTO probleme (...) VALUES (...)", ...);
     *         return executeInsert(cnx, "INSERT INTO demande (...) VALUES (...)", idProbleme, ...);
     *     });
     * </pre>
     * Dans le bloc, utiliser les versions des methodes qui prennent {@code cnx} en premier parametre.
     */
    protected <R> R inTransaction(TransactionWork<R> travail) {
        try (Connection cnx = DBConnection.getConnection()) {
            cnx.setAutoCommit(false);
            try {
                R resultat = travail.execute(cnx);
                cnx.commit();
                return resultat;
            } catch (SQLException e) {
                annuler(cnx);
                throw erreur(e);
            } catch (RuntimeException e) {
                annuler(cnx);
                throw e;
            }
        } catch (SQLException e) {
            throw erreur(e);
        }
    }

    private static void annuler(Connection cnx) {
        try {
            cnx.rollback();
        } catch (SQLException ignore) {
            // la connexion va etre fermee de toute facon
        }
    }

    // =====================================================================
    //  OUTILS INTERNES
    // =====================================================================

    /** Associe les parametres aux "?" de la requete. */
    private static void lier(PreparedStatement ps, Object[] params) throws SQLException {
        if (params == null) {
            return;
        }
        for (int i = 0; i < params.length; i++) {
            Object valeur = params[i];
            int position = i + 1;
            if (valeur == null) {
                ps.setNull(position, Types.NULL);
            } else if (valeur instanceof Enum<?> enumeration) {
                ps.setString(position, enumeration.name());
            } else {
                ps.setObject(position, valeur);
            }
        }
    }

    // =====================================================================
    //  OUTILS DE LECTURE POUR LES ROWMAPPER (valeurs pouvant etre NULL)
    // =====================================================================

    /** Colonne DATETIME -> LocalDateTime (null si la colonne est NULL). */
    protected static LocalDateTime getDateTime(ResultSet rs, String colonne) throws SQLException {
        return rs.getObject(colonne, LocalDateTime.class);
    }

    /** Colonne DATE -> LocalDate (null si la colonne est NULL). */
    protected static LocalDate getDate(ResultSet rs, String colonne) throws SQLException {
        return rs.getObject(colonne, LocalDate.class);
    }

    /** Colonne TIME -> LocalTime (null si la colonne est NULL). */
    protected static LocalTime getTime(ResultSet rs, String colonne) throws SQLException {
        return rs.getObject(colonne, LocalTime.class);
    }

    /** Colonne INT -> Integer (null si la colonne est NULL, au lieu de 0). */
    protected static Integer getIntegerOrNull(ResultSet rs, String colonne) throws SQLException {
        int valeur = rs.getInt(colonne);
        return rs.wasNull() ? null : valeur;
    }

    /** Colonne DECIMAL -> Double (null si la colonne est NULL, au lieu de 0.0). */
    protected static Double getDoubleOrNull(ResultSet rs, String colonne) throws SQLException {
        double valeur = rs.getDouble(colonne);
        return rs.wasNull() ? null : valeur;
    }

    /** Colonne ENUM -> enumeration Java (null si la colonne est NULL). */
    protected static <E extends Enum<E>> E getEnum(ResultSet rs, String colonne, Class<E> type) throws SQLException {
        String texte = rs.getString(colonne);
        return texte == null ? null : Enum.valueOf(type, texte);
    }
}
