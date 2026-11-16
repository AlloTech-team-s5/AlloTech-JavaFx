package com.allotech.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ValidatorTest {

    @Test
    void textesVidesOuNull() {
        assertFalse(Validator.estNonVide(null));
        assertFalse(Validator.estNonVide("   "));
        assertTrue(Validator.estNonVide(" a "));
    }

    @Test
    void longueur() {
        assertTrue(Validator.longueurEntre("ab", 2, 5));
        assertTrue(Validator.longueurEntre(" abcde ", 2, 5));
        assertFalse(Validator.longueurEntre("a", 2, 5));
        assertFalse(Validator.longueurEntre("abcdef", 2, 5));
        assertFalse(Validator.longueurEntre(null, 2, 5));
    }

    @Test
    void emails() {
        assertTrue(Validator.estEmailValide("sara.alaoui@test.ma"));
        assertTrue(Validator.estEmailValide(" a_b+c@mail.example.com "));
        assertFalse(Validator.estEmailValide("sans-arobase.ma"));
        assertFalse(Validator.estEmailValide("a@b"));
        assertFalse(Validator.estEmailValide("a@@b.ma"));
        assertFalse(Validator.estEmailValide(""));
        assertFalse(Validator.estEmailValide(null));
    }

    @Test
    void telephonesMarocains() {
        assertTrue(Validator.estTelephoneValide("0612345678"));
        assertTrue(Validator.estTelephoneValide("06 12 34 56 78"));
        assertTrue(Validator.estTelephoneValide("+212612345678"));
        assertTrue(Validator.estTelephoneValide("0528-12-34-56"));
        assertFalse(Validator.estTelephoneValide("0112345678"));
        assertFalse(Validator.estTelephoneValide("061234567"));
        assertFalse(Validator.estTelephoneValide("abcdefghij"));
        assertFalse(Validator.estTelephoneValide(null));
    }

    @Test
    void nombres() {
        assertTrue(Validator.estPositif(0.01));
        assertFalse(Validator.estPositif(0));
        assertFalse(Validator.estPositif(-5));
        assertTrue(Validator.estNoteValide(1));
        assertTrue(Validator.estNoteValide(5));
        assertFalse(Validator.estNoteValide(0));
        assertFalse(Validator.estNoteValide(6));
    }

    @Test
    void liensGoogleMaps() {
        assertTrue(Validator.estLienGoogleMaps("https://www.google.com/maps/place/Agadir"));
        assertTrue(Validator.estLienGoogleMaps("https://maps.google.com/?q=30.4,-9.6"));
        assertTrue(Validator.estLienGoogleMaps("https://maps.app.goo.gl/abc123"));
        assertTrue(Validator.estLienGoogleMaps("https://goo.gl/maps/xyz"));
        assertFalse(Validator.estLienGoogleMaps("http://www.google.com/maps/place/Agadir"));
        assertFalse(Validator.estLienGoogleMaps("https://www.google.com/search?q=maps"));
        assertFalse(Validator.estLienGoogleMaps("https://evil.com/maps/place"));
        assertFalse(Validator.estLienGoogleMaps("pas un lien"));
        assertFalse(Validator.estLienGoogleMaps(""));
        assertFalse(Validator.estLienGoogleMaps(null));
    }
}
