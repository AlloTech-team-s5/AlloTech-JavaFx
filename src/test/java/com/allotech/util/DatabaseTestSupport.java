package com.allotech.util;

import org.junit.jupiter.api.Assumptions;

/**
 * Outil des tests d'integration : ils ne s'executent que si MySQL repond.
 * Sans MySQL (ou sans config.properties), les tests sont IGNORES (et non en echec).
 */
public final class DatabaseTestSupport {

    private DatabaseTestSupport() {
    }

    /** A appeler au debut d'un test (ou dans @BeforeEach) qui a besoin de MySQL. */
    public static void exigerBaseDeDonnees() {
        boolean disponible;
        try {
            disponible = DBConnection.testerConnexion();
        } catch (RuntimeException e) {
            disponible = false;
        }
        Assumptions.assumeTrue(disponible,
                "MySQL indisponible : verifiez config.properties et lancez schema.sql + data.sql");
    }
}
