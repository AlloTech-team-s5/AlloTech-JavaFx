package com.allotech.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Transforme UNE ligne d'un {@link ResultSet} en objet Java.
 *
 * <p>Exemple (dans un DAO) :</p>
 * <pre>
 *     private static final RowMapper&lt;Categorie&gt; MAPPER = rs -&gt;
 *             new Categorie(rs.getInt("id"), rs.getString("nom"), rs.getBoolean("actif"));
 * </pre>
 *
 * <p>Ne jamais appeler {@code rs.next()} dans un mapper : c'est {@link BaseDAO} qui parcourt les lignes.</p>
 *
 * @param <T> type de l'objet produit
 */
@FunctionalInterface
public interface RowMapper<T> {
    T map(ResultSet rs) throws SQLException;
}
