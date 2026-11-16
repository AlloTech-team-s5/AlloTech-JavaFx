package com.allotech.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppConfigTest {

    @TempDir
    Path dossier;

    /** Remet la configuration par defaut pour ne pas perturber les tests MySQL qui suivent. */
    @AfterEach
    void remettreLaConfigurationParDefaut() {
        AppConfig.reinitialiser();
    }

    @Test
    void litLesValeursDuFichier() throws Exception {
        Path f = dossier.resolve("test.properties");
        Files.writeString(f, "cle.texte=  bonjour \ncle.nombre=42\ncle.invalide=abc\n", StandardCharsets.UTF_8);
        AppConfig.charger(f);

        assertEquals("bonjour", AppConfig.get("cle.texte"));
        assertEquals(42, AppConfig.getInt("cle.nombre", 0));
    }

    @Test
    void utiliseLaValeurParDefautQuandLaCleEstAbsenteOuInvalide() throws Exception {
        Path f = dossier.resolve("test.properties");
        Files.writeString(f, "cle.invalide=abc\n", StandardCharsets.UTF_8);
        AppConfig.charger(f);

        assertEquals(7, AppConfig.getInt("cle.absente", 7));
        assertEquals(7, AppConfig.getInt("cle.invalide", 7));
        assertEquals("defaut", AppConfig.get("cle.absente", "defaut"));
    }

    @Test
    void cleObligatoireManquanteLanceUneErreurClaire() throws Exception {
        Path f = dossier.resolve("test.properties");
        Files.writeString(f, "a=1\n", StandardCharsets.UTF_8);
        AppConfig.charger(f);

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> AppConfig.get("cle.absente"));
        assertEquals(true, e.getMessage().contains("cle.absente"));
    }

    @Test
    void fichierIntrouvableLanceUneErreurClaire() {
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> AppConfig.charger(dossier.resolve("inexistant.properties")));
        assertEquals(true, e.getMessage().contains("config.example.properties"));
    }
}
