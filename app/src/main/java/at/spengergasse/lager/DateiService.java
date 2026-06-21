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
import java.util.regex.Pattern;

// Liest/schreibt Artikel-Listen aus Dateien. Zwei Formate: Text (";") und CSV ("," + Header).
// Spalten: Typ | ID | Name | Marke | Preis | Bestand | Favorit | Extra1 | Extra2
// Extra-Spalten je Typ:
//   Elektronik:   garantieMonate | batterieTyp
//   Lebensmittel: haltbarBis | bio
//   Kleidung:     groesse | material
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

    // ---- Private Helfer: eine Zeile schreiben / parsen ----

    // Backslash muss vor dem Trennzeichen escaped werden, sonst stimmt das Unescapen nicht.
    private static String escapeField(String value, String trenner) {
        return value.replace("\\", "\\\\").replace(trenner, "\\" + trenner);
    }

    private static String unescapeField(String value, String trenner) {
        return value.replace("\\" + trenner, trenner).replace("\\\\", "\\");
    }

    private static String formatZeile(Artikel a, String t) {
        String basis = a.getKategorie() + t
                + a.getId() + t
                + escapeField(a.getName(), t) + t
                + escapeField(a.getMarke(), t) + t
                + String.format(Locale.US, "%.2f", a.getPreis()) + t
                + a.getBestand() + t
                + a.istFavorit();

        if (a instanceof ElektronikArtikel) {
            ElektronikArtikel e = (ElektronikArtikel) a;
            String batterie = e.getBatterieTyp() == null ? "" : escapeField(e.getBatterieTyp(), t);
            return basis + t + e.getGarantieMonate() + t + batterie;
        }
        if (a instanceof Lebensmittel) {
            Lebensmittel l = (Lebensmittel) a;
            return basis + t + l.getHaltbarBis() + t + l.istBio();
        }
        if (a instanceof Kleidung) {
            Kleidung k = (Kleidung) a;
            return basis + t + k.getGroesse() + t + escapeField(k.getMaterial(), t);
        }
        throw new IllegalStateException("Unbekannte Artikel-Subklasse: "
                + a.getClass().getName());
    }

    private static Artikel parseZeile(String zeile, String trenner)
            throws UngueltigeEingabeException {
        // Nur an Trennzeichen ohne Backslash davor splitten.
        String[] felder = zeile.split("(?<!\\\\)" + Pattern.quote(trenner), -1);
        if (felder.length < 9) {
            throw new UngueltigeEingabeException(
                    "Zeile hat zu wenige Felder (" + felder.length + "): " + zeile);
        }

        String typ = felder[0];
        int id = Integer.parseInt(felder[1]);
        String name = unescapeField(felder[2], trenner);
        String marke = unescapeField(felder[3], trenner);
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
                e.batterieHinzufuegen(unescapeField(batterieTyp, trenner));
            }
            a = e;
        } else if (typ.equals("Lebensmittel")) {
            LocalDate haltbarBis = LocalDate.parse(felder[7]);
            boolean bio = Boolean.parseBoolean(felder[8]);
            a = new Lebensmittel(id, name, marke, preis, bestand, haltbarBis, bio);
        } else if (typ.equals("Kleidung")) {
            Groesse groesse = Groesse.valueOf(felder[7]);
            String material = unescapeField(felder[8], trenner);
            a = new Kleidung(id, name, marke, preis, bestand, groesse, material);
        } else {
            throw new UngueltigeEingabeException("Unbekannter Artikel-Typ: " + typ);
        }

        a.setFavorit(favorit);
        return a;
    }
}
