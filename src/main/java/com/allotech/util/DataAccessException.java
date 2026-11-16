package com.allotech.util;

import java.sql.SQLException;

/**
 * Exception lancee par les DAO quand MySQL renvoie une erreur.
 *
 * <p>Elle remplace {@link SQLException} (qui oblige a ecrire des try/catch partout)
 * car elle est <b>non verifiee</b> (RuntimeException). Elle contient un message
 * clair en francais, pret a afficher a l'utilisateur :</p>
 *
 * <pre>
 *     try {
 *         categorieService.ajouter(nom);
 *     } catch (DataAccessException e) {
 *         AlertUtil.erreur(e.getUserMessage());   // message clair
 *         // e.getCause() contient l'erreur technique SQL pour le debogage
 *     }
 * </pre>
 */
public class DataAccessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String userMessage;
    private final int sqlErrorCode;

    public DataAccessException(String userMessage, Throwable cause, int sqlErrorCode) {
        super(userMessage + (cause != null ? " [" + cause.getMessage() + "]" : ""), cause);
        this.userMessage = userMessage;
        this.sqlErrorCode = sqlErrorCode;
    }

    public DataAccessException(String userMessage) {
        this(userMessage, null, 0);
    }

    /** Message clair a afficher a l'utilisateur. */
    public String getUserMessage() {
        return userMessage;
    }

    /** Code d'erreur MySQL (ex : 1062 pour un doublon), 0 si inconnu. */
    public int getSqlErrorCode() {
        return sqlErrorCode;
    }

    /** Vrai si l'erreur est un doublon (valeur UNIQUE deja existante). */
    public boolean estDoublon() {
        return sqlErrorCode == 1062;
    }

    /** Vrai si l'erreur vient d'un element encore utilise ailleurs (cle etrangere). */
    public boolean estElementUtilise() {
        return sqlErrorCode == 1451;
    }

    /**
     * Transforme une {@link SQLException} en {@link DataAccessException}
     * avec un message comprehensible.
     *
     * @param operation description de ce qu'on faisait (ex : "Ajout d'une categorie")
     * @param e         l'erreur SQL d'origine
     */
    public static DataAccessException from(String operation, SQLException e) {
        return new DataAccessException(
                operation + " impossible : " + traduire(e), e, e.getErrorCode());
    }

    /** Traduit un code d'erreur MySQL en message francais. */
    static String traduire(SQLException e) {
        int code = e.getErrorCode();
        String etat = e.getSQLState() == null ? "" : e.getSQLState();

        switch (code) {
            case 1062:
                return "cette valeur existe déjà (doublon interdit).";
            case 1451:
                return "cet élément est encore utilisé ailleurs dans l'application.";
            case 1452:
                return "un élément lié n'existe pas (référence invalide).";
            case 1048:
            case 1364:
                return "un champ obligatoire est vide.";
            case 1406:
                return "un texte saisi est trop long.";
            case 3819:
            case 4025:
                return "une valeur ne respecte pas les règles de la base de données.";
            case 1045:
                return "accès refusé à MySQL : vérifiez db.user et db.password dans config.properties.";
            case 1049:
                return "la base de données 'allotech' est introuvable : exécutez database/schema.sql.";
            case 1146:
                return "une table est introuvable : exécutez database/schema.sql.";
            case 1213:
            case 1205:
                return "la base est occupée, veuillez réessayer dans un instant.";
            default:
                break;
        }
        if (etat.startsWith("08")) {
            return "impossible de joindre MySQL : vérifiez qu'il est démarré et que db.url est correct.";
        }
        return "erreur de base de données (code " + code + ").";
    }
}
