package com.allotech.dao;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Bloc de travail execute dans une transaction (voir {@link BaseDAO#inTransaction}).
 *
 * @param <R> type du resultat renvoye
 */
@FunctionalInterface
public interface TransactionWork<R> {
    R execute(Connection cnx) throws SQLException;
}
