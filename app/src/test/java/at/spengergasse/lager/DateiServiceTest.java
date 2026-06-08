package at.spengergasse.lager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

// Testet den DateiService: normaler Roundtrip + Sonderzeichen in Feldern.
// @TempDir: JUnit legt automatisch einen temporaeren Ordner an und raeumt ihn danach auf.
class DateiServiceTest {

    @TempDir
    Path tmpDir;

    // ---- Hilfsmethode: kleine Verwaltung mit Testartikeln befuellen ----

    private LagerVerwaltung testVerwaltung() throws UngueltigeEingabeException {
        LagerVerwaltung v = new LagerVerwaltung();

        v.hinzufuegen(new ElektronikArtikel(1, "ThinkPad X1", "Lenovo", 1499.00, 5, 24));
        v.hinzufuegen(new Lebensmittel(2, "Bio-Joghurt", "Berghof",
                1.29, 50, LocalDate.of(2026, 12, 31), true));
        v.hinzufuegen(new Kleidung(3, "Basic T-Shirt", "H&M",
                9.99, 20, Groesse.M, "Baumwolle"));
        return v;
    }

    // ---- Text-Format Roundtrip (normal) ----

    @Test
    void textFormat_roundtrip_anzahlStimmt() throws Exception {
        LagerVerwaltung original = testVerwaltung();
        String pfad = tmpDir.resolve("lager.txt").toString();

        original.speichernInDatei(pfad);

        LagerVerwaltung geladen = new LagerVerwaltung();
        int anzahl = geladen.ladenAusDatei(pfad);

        assertEquals(3, anzahl);
    }

    @Test
    void textFormat_roundtrip_felderStimmen() throws Exception {
        LagerVerwaltung original = testVerwaltung();
        String pfad = tmpDir.resolve("lager.txt").toString();

        original.speichernInDatei(pfad);

        LagerVerwaltung geladen = new LagerVerwaltung();
        geladen.ladenAusDatei(pfad);

        Artikel laptop = geladen.suchen("ThinkPad X1");
        assertNotNull(laptop);
        assertEquals("Lenovo", laptop.getMarke());
        assertEquals(1499.00, laptop.getPreis(), 0.001);
        assertEquals(5, laptop.getBestand());
    }

    // ---- CSV-Format Roundtrip (normal) ----

    @Test
    void csvFormat_roundtrip_anzahlStimmt() throws Exception {
        LagerVerwaltung original = testVerwaltung();
        String pfad = tmpDir.resolve("lager.csv").toString();

        original.speichernAlsCSV(pfad);

        LagerVerwaltung geladen = new LagerVerwaltung();
        int anzahl = geladen.ladenAusCSV(pfad);

        assertEquals(3, anzahl);
    }

    // ---- Sonderzeichen: Semikolon im Namen (Text-Format) ----
    // Das war der Bug: "Müller;Söhne" wurde beim Laden falsch gesplittet.

    @Test
    void textFormat_semikolonImNamen_wirdKorrektGespeichertUndGeladen() throws Exception {
        LagerVerwaltung v = new LagerVerwaltung();
        v.hinzufuegen(new ElektronikArtikel(1, "Gerät;Spezial", "Marke;XY", 99.0, 1, 12));
        String pfad = tmpDir.resolve("sonderzeichen.txt").toString();

        v.speichernInDatei(pfad);

        LagerVerwaltung geladen = new LagerVerwaltung();
        geladen.ladenAusDatei(pfad);

        Artikel a = geladen.suchen("Gerät;Spezial");
        assertNotNull(a, "Artikel mit Semikolon im Namen muss gefunden werden");
        assertEquals("Marke;XY", a.getMarke());
    }

    // ---- Sonderzeichen: Komma im Namen (CSV-Format) ----

    @Test
    void csvFormat_kommaImNamen_wirdKorrektGespeichertUndGeladen() throws Exception {
        LagerVerwaltung v = new LagerVerwaltung();
        v.hinzufuegen(new Kleidung(1, "Jacke, Winter", "Brand,Co",
                199.0, 2, Groesse.L, "Wolle,Mix"));
        String pfad = tmpDir.resolve("komma.csv").toString();

        v.speichernAlsCSV(pfad);

        LagerVerwaltung geladen = new LagerVerwaltung();
        geladen.ladenAusCSV(pfad);

        Artikel a = geladen.suchen("Jacke, Winter");
        assertNotNull(a, "Artikel mit Komma im Namen muss gefunden werden");
        assertEquals("Brand,Co", a.getMarke());
        assertEquals("Wolle,Mix", ((Kleidung) a).getMaterial());
    }

    // ---- Sonderzeichen: Backslash im Namen ----

    @Test
    void textFormat_backslashImNamen_wirdKorrektGespeichertUndGeladen() throws Exception {
        LagerVerwaltung v = new LagerVerwaltung();
        v.hinzufuegen(new ElektronikArtikel(1, "C:\\Gerät", "My\\Brand", 50.0, 1, 6));
        String pfad = tmpDir.resolve("backslash.txt").toString();

        v.speichernInDatei(pfad);

        LagerVerwaltung geladen = new LagerVerwaltung();
        geladen.ladenAusDatei(pfad);

        Artikel a = geladen.suchen("C:\\Gerät");
        assertNotNull(a, "Artikel mit Backslash im Namen muss gefunden werden");
        assertEquals("My\\Brand", a.getMarke());
    }

    // ---- Favorit-Flag bleibt nach Roundtrip erhalten ----

    @Test
    void textFormat_favoritBleibtErhalten() throws Exception {
        LagerVerwaltung v = testVerwaltung();
        v.markiereFavorit(1, true);
        String pfad = tmpDir.resolve("favorit.txt").toString();

        v.speichernInDatei(pfad);

        LagerVerwaltung geladen = new LagerVerwaltung();
        geladen.ladenAusDatei(pfad);

        assertTrue(geladen.suchen("ThinkPad X1").istFavorit());
        assertFalse(geladen.suchen("Bio-Joghurt").istFavorit());
    }

    // ---- Leere Datei laden wirft keine Exception ----

    @Test
    void laden_leereDate_liefertNull_Artikel() throws Exception {
        String pfad = tmpDir.resolve("leer.txt").toString();
        // Leere Datei anlegen
        new java.io.File(pfad).createNewFile();

        LagerVerwaltung v = new LagerVerwaltung();
        int anzahl = v.ladenAusDatei(pfad);

        assertEquals(0, anzahl);
    }

    // ---- Nicht-existente Datei wirft UngueltigeEingabeException ----

    @Test
    void laden_nichtExistenteDatei_wirftException() {
        LagerVerwaltung v = new LagerVerwaltung();
        assertThrows(UngueltigeEingabeException.class,
                () -> v.ladenAusDatei(tmpDir.resolve("gibts_nicht.txt").toString()));
    }
}
