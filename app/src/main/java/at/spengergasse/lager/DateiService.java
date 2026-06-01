package at.spengergasse.lager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Liest und schreibt Artikel-Listen aus/in Dateien.
// Unterstuetzt zwei Formate: Text (semikolon-getrennt) und CSV (komma-getrennt, mit Header).
// Beide Formate haben dasselbe Feld-Schema:
//   Typ | ID | Name | Marke | Preis | Bestand | Favorit | Extra1 | Extra2
//
// Extra-Spalten je Subklasse:
//   Elektronik:   garantieMonate | batterieTyp (leer wenn null)
//   Lebensmittel: haltbarBis (ISO yyyy-MM-dd) | bio (true/false)
//   Kleidung:     groesse (Enum-Name) | material
//
// WICHTIG: Namen / Marken / Materialien mit Trennzeichen drin (";" oder ",") werden
// nicht escaped — fuer dieses Schulprojekt setzen wir voraus, dass die Daten kein
// Trennzeichen enthalten.
public class DateiService {

    private static final String TRENNER_TEXT = ";";
    private static final String TRENNER_CSV = ",";
    private static final String CSV_HEADER =
            "Typ,ID,Name,Marke,Preis,Bestand,Favorit,Extra1,Extra2";

    // ---- Text-Format (semikolon-getrennt, keine Header-Zeile) ----

    public static void speichern(List<Artikel> liste, String dateipfad) throws IOException {
        BufferedWriter w = null;
        try {
            w = new BufferedWriter(new FileWriter(dateipfad));
            for (Artikel a : liste) {
                w.write(formatZeile(a, TRENNER_TEXT));
                w.newLine();
            }
        } finally {
            if (w != null) {
                w.close();
            }
        }
    }

    public static ArrayList<Artikel> laden(String dateipfad) throws IOException {
        ArrayList<Artikel> ergebnis = new ArrayList<>();
        BufferedReader r = null;
        try {
            r = new BufferedReader(new FileReader(dateipfad));
            String zeile;
            while ((zeile = r.readLine()) != null) {
                zeile = zeile.trim();
                if (zeile.isEmpty()) {
                    continue;
                }
                try {
                    ergebnis.add(parseZeile(zeile, TRENNER_TEXT));
                } catch (UngueltigeEingabeException e) {
                    // Defekte Zeile -> ueberspringen
                } catch (IllegalArgumentException e) {
                    // ungueltige Zahl, falscher Enum-Wert, ...
                } catch (DateTimeParseException e) {
                    // ungueltiges Datum
                }
            }
        } finally {
            if (r != null) {
                r.close();
            }
        }
        return ergebnis;
    }

    // ---- CSV-Format (komma-getrennt, mit Header-Zeile) ----

    public static void speichernCSV(List<Artikel> liste, String dateipfad) throws IOException {
        BufferedWriter w = null;
        try {
            w = new BufferedWriter(new FileWriter(dateipfad));
            w.write(CSV_HEADER);
            w.newLine();
            for (Artikel a : liste) {
                w.write(formatZeile(a, TRENNER_CSV));
                w.newLine();
            }
        } finally {
            if (w != null) {
                w.close();
            }
        }
    }

    public static ArrayList<Artikel> ladenCSV(String dateipfad) throws IOException {
        ArrayList<Artikel> ergebnis = new ArrayList<>();
        BufferedReader r = null;
        try {
            r = new BufferedReader(new FileReader(dateipfad));
            // Erste Zeile = Header -> ueberspringen
            r.readLine();
            String zeile;
            while ((zeile = r.readLine()) != null) {
                zeile = zeile.trim();
                if (zeile.isEmpty()) {
                    continue;
                }
                try {
                    ergebnis.add(parseZeile(zeile, TRENNER_CSV));
                } catch (UngueltigeEingabeException e) {
                    // Defekte Zeile -> ueberspringen
                } catch (IllegalArgumentException e) {
                    // ungueltige Zahl, falscher Enum-Wert, ...
                } catch (DateTimeParseException e) {
                    // ungueltiges Datum
                }
            }
        } finally {
            if (r != null) {
                r.close();
            }
        }
        return ergebnis;
    }

    // ---- Private Helfer: Eine Zeile schreiben / parsen ----

    private static String formatZeile(Artikel a, String trenner) {
        // Basis-Felder
        String basis = a.getKategorie() + trenner
                + a.getId() + trenner
                + a.getName() + trenner
                + a.getMarke() + trenner
                + String.format(Locale.US, "%.2f", a.getPreis()) + trenner
                + a.getBestand() + trenner
                + a.istFavorit();

        // Typ-spezifische Extras
        if (a instanceof ElektronikArtikel) {
            ElektronikArtikel e = (ElektronikArtikel) a;
            String batterie = e.getBatterieTyp() == null ? "" : e.getBatterieTyp();
            return basis + trenner + e.getGarantieMonate() + trenner + batterie;
        }
        if (a instanceof Lebensmittel) {
            Lebensmittel l = (Lebensmittel) a;
            return basis + trenner + l.getHaltbarBis() + trenner + l.istBio();
        }
        if (a instanceof Kleidung) {
            Kleidung k = (Kleidung) a;
            return basis + trenner + k.getGroesse() + trenner + k.getMaterial();
        }
        // Sollte nie passieren — alle Subklassen werden oben abgedeckt.
        throw new IllegalStateException("Unbekannte Artikel-Subklasse: "
                + a.getClass().getName());
    }

    private static Artikel parseZeile(String zeile, String trenner)
            throws UngueltigeEingabeException {
        // -1 sorgt dafuer, dass auch leere Trailing-Felder nicht abgeschnitten werden
        String[] felder = zeile.split(trenner, -1);
        if (felder.length < 9) {
            throw new UngueltigeEingabeException(
                    "Zeile hat zu wenige Felder (" + felder.length + "): " + zeile);
        }

        String typ = felder[0];
        int id = Integer.parseInt(felder[1]);
        String name = felder[2];
        String marke = felder[3];
        // Locale.US -> Punkt als Dezimaltrenner (so wurde auch geschrieben)
        double preis = Double.parseDouble(felder[4]);
        int bestand = Integer.parseInt(felder[5]);
        boolean favorit = Boolean.parseBoolean(felder[6]);

        Artikel a;
        if (typ.equals("Elektronik")) {
            int garantieMonate = Integer.parseInt(felder[7]);
            String batterieTyp = felder[8];
            ElektronikArtikel e = new ElektronikArtikel(
                    id, name, marke, preis, bestand, garantieMonate);
            if (!batterieTyp.isEmpty()) {
                e.batterieHinzufuegen(batterieTyp);
            }
            a = e;
        } else if (typ.equals("Lebensmittel")) {
            LocalDate haltbarBis = LocalDate.parse(felder[7]);
            boolean bio = Boolean.parseBoolean(felder[8]);
            a = new Lebensmittel(id, name, marke, preis, bestand, haltbarBis, bio);
        } else if (typ.equals("Kleidung")) {
            // Groesse.valueOf wirft IllegalArgumentException bei unbekanntem Wert
            Groesse groesse = Groesse.valueOf(felder[7]);
            String material = felder[8];
            a = new Kleidung(id, name, marke, preis, bestand, groesse, material);
        } else {
            throw new UngueltigeEingabeException("Unbekannter Artikel-Typ: " + typ);
        }

        a.setFavorit(favorit);
        return a;
    }
}
