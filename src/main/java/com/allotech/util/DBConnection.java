package com.allotech.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Point d'entree unique pour obtenir une connexion MySQL.
 *
 * <p><b>Principe :</b> chaque appel de {@link #getConnection()} ouvre une NOUVELLE connexion.
 * Il faut donc la fermer apres usage, le plus simple etant le {@code try-with-resources} :</p>
 *
 * <pre>
 *     try (Connection cnx = DBConnection.getConnection();
 *          PreparedStatement ps = cnx.prepareStatement("SELECT ...")) {
 *         ...
 *     }
 * </pre>
 *
 * <p>Pourquoi pas une seule connexion partagee ? Parce que l'application a plusieurs fils
 * d'execution (l'interface JavaFX, la verification des notifications toutes les 5 secondes,
 * l'expiration automatique) : une connexion JDBC unique n'est pas sure dans ce cas et peut
 * etre coupee par MySQL apres une longue inactivite.</p>
 *
 * <p>Dans la pratique, les membres de l'equipe n'appellent presque jamais cette classe
 * directement : les methodes de {@link com.allotech.dao.BaseDAO} s'en occupent.</p>
 *
 * <p>Parametres lus dans config.properties : {@code db.url}, {@code db.user}, {@code db.password}.</p>
 */
public final class DBConnection {

    private DBConnection() {
        // classe utilitaire : pas d'instance
    }

    /**
     * Ouvre une nouvelle connexion MySQL.
     *
     * @throws DataAccessException si MySQL est arrete, si le mot de passe est faux, etc.
     *                             (le message est deja traduit en francais)
     */
    public static Connection getConnection() {
        String url = AppConfig.get("db.url");
        String utilisateur = AppConfig.get("db.user");
        String motDePasse = AppConfig.get("db.password", "");
        try {
            return DriverManager.getConnection(url, utilisateur, motDePasse);
        } catch (SQLException e) {
            throw DataAccessException.from("Connexion à la base de données", e);
        }
    }

    /**
     * Teste rapidement la connexion (sans lancer d'exception).
     *
     * @return true si MySQL repond, false sinon
     */
    public static boolean testerConnexion() {
        try (Connection cnx = getConnection()) {
            return cnx.isValid(2);
        } catch (SQLException | RuntimeException e) {
            return false;
        }
    }
}
