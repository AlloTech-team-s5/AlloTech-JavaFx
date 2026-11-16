package com.allotech.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import org.junit.jupiter.api.Test;

class DataAccessExceptionTest {

    @Test
    void doublon() {
        DataAccessException e = DataAccessException.from("Ajout",
                new SQLException("Duplicate entry", "23000", 1062));
        assertTrue(e.estDoublon());
        assertFalse(e.estElementUtilise());
        assertTrue(e.getUserMessage().contains("existe déjà"));
        assertTrue(e.getUserMessage().startsWith("Ajout impossible"));
        assertEquals(1062, e.getSqlErrorCode());
    }

    @Test
    void elementUtilise() {
        DataAccessException e = DataAccessException.from("Suppression",
                new SQLException("FK", "23000", 1451));
        assertTrue(e.estElementUtilise());
        assertTrue(e.getUserMessage().contains("encore utilisé"));
    }

    @Test
    void erreursDeConnexion() {
        DataAccessException e = DataAccessException.from("Connexion",
                new SQLException("refused", "08S01", 0));
        assertTrue(e.getUserMessage().contains("joindre MySQL"));

        DataAccessException e2 = DataAccessException.from("Connexion",
                new SQLException("denied", "28000", 1045));
        assertTrue(e2.getUserMessage().contains("db.password"));
    }

    @Test
    void erreurInconnue() {
        DataAccessException e = DataAccessException.from("Lecture",
                new SQLException("???", "HY000", 9999));
        assertTrue(e.getUserMessage().contains("9999"));
    }
}
