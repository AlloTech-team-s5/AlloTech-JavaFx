package com.allotech.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DBConnectionTest {

    @BeforeEach
    void avant() {
        DatabaseTestSupport.exigerBaseDeDonnees();
    }

    @Test
    void ouvreUneConnexionValide() throws Exception {
        try (Connection cnx = DBConnection.getConnection()) {
            assertNotNull(cnx);
            assertFalse(cnx.isClosed());
            assertTrue(cnx.isValid(2));
        }
    }

    @Test
    void chaqueAppelDonneUneNouvelleConnexion() throws Exception {
        try (Connection a = DBConnection.getConnection();
             Connection b = DBConnection.getConnection()) {
            assertTrue(a != b);
        }
    }

    @Test
    void peutExecuterUneRequete() throws Exception {
        try (Connection cnx = DBConnection.getConnection();
             Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery("SELECT 1")) {
            assertTrue(rs.next());
        }
    }

    @Test
    void testerConnexionRenvoieVrai() {
        assertTrue(DBConnection.testerConnexion());
    }
}
