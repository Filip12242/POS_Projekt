package at.spengergasse.lager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

// Haelt die Artikel-Liste und alle Operationen darauf: CRUD, Filter, Sortierung,
// Gruppierung, Auswertungen und ein Verlaufs-Log.
public class LagerVerwaltung {

    private ArrayList<Artikel> artikelListe;
    private ArrayList<LogEintrag> verlauf;

    public LagerVerwaltung() {
        this.artikelListe = new ArrayList<>();
        this.verlauf = new ArrayList<>();
    }

    private void log(String aktion) {
        verlauf.add(new LogEintrag(aktion));
    }

    // Neuen Artikel hinzufuegen. ID muss eindeutig sein.
    public void hinzufuegen(Artikel a) throws UngueltigeEingabeException {
        if (a == null) {
            throw new UngueltigeEingabeException("Artikel darf nicht null sein");
        }
        for (Artikel vorhandener : artikelListe) {
            if (vorhandener.getId() == a.getId()) {
                throw new UngueltigeEingabeException(
                        "ID " + a.getId() + " ist bereits vergeben");
            }
        }
        artikelListe.add(a);
        log("Hinzugefuegt: " + a.getKategorie() + " #" + a.getId()
                + " (" + a.getName() + ")");
    }

    // Artikel per ID entfernen. Gibt true zurueck, wenn etwas geloescht wurde.
    public boolean entfernen(int id) {
        for (int i = 0; i < artikelListe.size(); i++) {
            if (artikelListe.get(i).getId() == id) {
                Artikel entfernt = artikelListe.remove(i);
                log("Entfernt: " + entfernt.getKategorie() + " #" + entfernt.getId()
                        + " (" + entfernt.getName() + ")");
                return true;
            }
        }
        return false;
    }

    // Ersetzt den Artikel mit der angegebenen ID komplett durch neueArtikel.
    public boolean aendern(int id, Artikel neueArtikel) throws UngueltigeEingabeException {
        if (neueArtikel == null) {
            throw new UngueltigeEingabeException("Artikel darf nicht null sein");
        }
        if (neueArtikel.getId() != id) {
            throw new UngueltigeEingabeException(
                    "ID der neuen Artikeldaten muss mit der Ziel-ID ueberinstimmen");
        }
        for (int i = 0; i < artikelListe.size(); i++) {
            if (artikelListe.get(i).getId() == id) {
                artikelListe.set(i, neueArtikel);
                log("Geaendert: " + neueArtikel.getKategorie() + " #" + id
                        + " (" + neueArtikel.getName() + ")");
                return true;
            }
        }
        return false;
    }

    // Sucht den ersten Artikel mit diesem Namen (case-insensitive). null wenn nicht gefunden.
    public Artikel suchen(String name) {
        if (name == null) {
            return null;
        }
        for (Artikel a : artikelListe) {
            if (a.getName().equalsIgnoreCase(name)) {
                return a;
            }
        }
        return null;
    }

    // Kopie der Liste, damit von aussen nicht direkt an artikelListe rumgepfuscht werden kann.
    public ArrayList<Artikel> alleAnzeigen() {
        return new ArrayList<>(artikelListe);
    }

    public int anzahl() {
        return artikelListe.size();
    }

    // ---- Filter-Methoden ----

    // Alle Artikel im Preisbereich min-max (Grenzen inklusive).
    public ArrayList<Artikel> filterNachPreis(double min, double max)
            throws UngueltigeEingabeException {
        if (min > max) {
            throw new UngueltigeEingabeException(
                    "min (" + min + ") darf nicht groesser als max (" + max + ") sein");
        }
        ArrayList<Artikel> ergebnis = new ArrayList<>();
        for (Artikel a : artikelListe) {
            if (a.getPreis() >= min && a.getPreis() <= max) {
                ergebnis.add(a);
            }
        }
        return ergebnis;
    }

    // Alle Artikel mit Bestand >= min.
    public ArrayList<Artikel> filterNachBestand(int min) {
        ArrayList<Artikel> ergebnis = new ArrayList<>();
        for (Artikel a : artikelListe) {
            if (a.getBestand() >= min) {
                ergebnis.add(a);
            }
        }
        return ergebnis;
    }

    // Alle Artikel einer bestimmten Marke (case-insensitive).
    public ArrayList<Artikel> filterNachMarke(String marke) {
        ArrayList<Artikel> ergebnis = new ArrayList<>();
        if (marke == null) {
            return ergebnis;
        }
        for (Artikel a : artikelListe) {
            if (a.getMarke().equalsIgnoreCase(marke)) {
                ergebnis.add(a);
            }
        }
        return ergebnis;
    }

    // Filter nach Kategorie + Marke + Preisbereich gleichzeitig. Leere/null Kriterien
    // werden ignoriert.
    public ArrayList<Artikel> filtern(String kategorie, String marke,
                                      double minPreis, double maxPreis)
            throws UngueltigeEingabeException {
        if (minPreis > maxPreis) {
            throw new UngueltigeEingabeException(
                    "minPreis (" + minPreis + ") darf nicht groesser als maxPreis ("
                            + maxPreis + ") sein");
        }
        ArrayList<Artikel> ergebnis = new ArrayList<>();
        for (Artikel a : artikelListe) {
            if (kategorie != null && !kategorie.isBlank()
                    && !a.getKategorie().equalsIgnoreCase(kategorie)) {
                continue;
            }
            if (marke != null && !marke.isBlank()
                    && !a.getMarke().equalsIgnoreCase(marke)) {
                continue;
            }
            if (a.getPreis() < minPreis || a.getPreis() > maxPreis) {
                continue;
            }
            ergebnis.add(a);
        }
        return ergebnis;
    }

    // Alle Lebensmittel, die schon abgelaufen sind.
    public ArrayList<Lebensmittel> abgelaufeneLebensmittel() {
        ArrayList<Lebensmittel> ergebnis = new ArrayList<>();
        for (Artikel a : artikelListe) {
            if (a instanceof Lebensmittel) {
                Lebensmittel l = (Lebensmittel) a;
                if (l.istAbgelaufen()) {
                    ergebnis.add(l);
                }
            }
        }
        return ergebnis;
    }

    // ---- Sortier-Methoden (arbeiten auf einer Kopie, Original bleibt unsortiert) ----

    public ArrayList<Artikel> sortiereNachName() {
        ArrayList<Artikel> kopie = new ArrayList<>(artikelListe);
        Collections.sort(kopie, new Comparator<Artikel>() {
            @Override
            public int compare(Artikel a, Artikel b) {
                return a.getName().compareToIgnoreCase(b.getName());
            }
        });
        return kopie;
    }

    public ArrayList<Artikel> sortiereNachPreis() {
        ArrayList<Artikel> kopie = new ArrayList<>(artikelListe);
        Collections.sort(kopie, new Comparator<Artikel>() {
            @Override
            public int compare(Artikel a, Artikel b) {
                return Double.compare(a.getPreis(), b.getPreis());
            }
        });
        return kopie;
    }

    public ArrayList<Artikel> sortiereNachBestand() {
        ArrayList<Artikel> kopie = new ArrayList<>(artikelListe);
        Collections.sort(kopie, new Comparator<Artikel>() {
            @Override
            public int compare(Artikel a, Artikel b) {
                // Absteigend: hoeherer Bestand zuerst
                return Integer.compare(b.getBestand(), a.getBestand());
            }
        });
        return kopie;
    }

    // ---- Aggregate / Auswertungen ----

    public double gesamtWert() {
        double summe = 0;
        for (Artikel a : artikelListe) {
            summe = summe + a.berechneWert();
        }
        return summe;
    }

    public double durchschnittPreis() {
        if (artikelListe.isEmpty()) {
            return 0;
        }
        double summe = 0;
        for (Artikel a : artikelListe) {
            summe = summe + a.getPreis();
        }
        return summe / artikelListe.size();
    }

    public int gesamtBestand() {
        int summe = 0;
        for (Artikel a : artikelListe) {
            summe = summe + a.getBestand();
        }
        return summe;
    }

    // Teuerster Artikel. null wenn Lager leer.
    public Artikel teuersterArtikel() {
        if (artikelListe.isEmpty()) {
            return null;
        }
        Artikel max = artikelListe.get(0);
        for (Artikel a : artikelListe) {
            if (a.getPreis() > max.getPreis()) {
                max = a;
            }
        }
        return max;
    }

    // Guenstigster Artikel. null wenn Lager leer.
    public Artikel guenstigsterArtikel() {
        if (artikelListe.isEmpty()) {
            return null;
        }
        Artikel min = artikelListe.get(0);
        for (Artikel a : artikelListe) {
            if (a.getPreis() < min.getPreis()) {
                min = a;
            }
        }
        return min;
    }

    // Teuerstes Elektronik-Geraet. null wenn keines vorhanden.
    public ElektronikArtikel teuerstesElektronik() {
        ElektronikArtikel max = null;
        for (Artikel a : artikelListe) {
            if (a instanceof ElektronikArtikel) {
                ElektronikArtikel e = (ElektronikArtikel) a;
                if (max == null || e.getPreis() > max.getPreis()) {
                    max = e;
                }
            }
        }
        return max;
    }

    // Teuerstes Kleidungsstueck. null wenn keines vorhanden.
    public Kleidung teuersteKleidung() {
        Kleidung max = null;
        for (Artikel a : artikelListe) {
            if (a instanceof Kleidung) {
                Kleidung k = (Kleidung) a;
                if (max == null || k.getPreis() > max.getPreis()) {
                    max = k;
                }
            }
        }
        return max;
    }

    // ---- Gruppierung ----

    // Gruppiert nach Kategorie. TreeMap statt HashMap, damit die Reihenfolge alphabetisch ist.
    public Map<String, ArrayList<Artikel>> gruppiereNachKategorie() {
        Map<String, ArrayList<Artikel>> gruppen = new TreeMap<>();
        for (Artikel a : artikelListe) {
            String kategorie = a.getKategorie();
            if (!gruppen.containsKey(kategorie)) {
                gruppen.put(kategorie, new ArrayList<Artikel>());
            }
            gruppen.get(kategorie).add(a);
        }
        return gruppen;
    }

    // ---- Combo-Methode: Filter + Sortieren in einem Aufruf ----

    // Alle Artikel >= minPreis, sortiert nach Preis aufsteigend.
    public ArrayList<Artikel> filterUndSortierenNachPreis(double minPreis) {
        ArrayList<Artikel> ergebnis = new ArrayList<>();
        for (Artikel a : artikelListe) {
            if (a.getPreis() >= minPreis) {
                ergebnis.add(a);
            }
        }
        Collections.sort(ergebnis, new Comparator<Artikel>() {
            @Override
            public int compare(Artikel a, Artikel b) {
                return Double.compare(a.getPreis(), b.getPreis());
            }
        });
        return ergebnis;
    }

    // ---- Favoriten-Filter ----

    public ArrayList<Artikel> filterFavoriten() {
        ArrayList<Artikel> ergebnis = new ArrayList<>();
        for (Artikel a : artikelListe) {
            if (a.istFavorit()) {
                ergebnis.add(a);
            }
        }
        return ergebnis;
    }

    public boolean markiereFavorit(int id, boolean favorit) {
        for (Artikel a : artikelListe) {
            if (a.getId() == id) {
                a.setFavorit(favorit);
                String aktion = favorit ? "Favorit gesetzt" : "Favorit entfernt";
                log(aktion + ": #" + id + " (" + a.getName() + ")");
                return true;
            }
        }
        return false;
    }

    // ---- Verlaufs-Log Zugriff ----

    public ArrayList<LogEintrag> getVerlauf() {
        return new ArrayList<>(verlauf);
    }

    // ---- Datei-Operationen (delegieren an DateiService) ----

    // Laedt Artikel aus einer Text-Datei. Defekte Zeilen werden uebersprungen,
    // der Rest wird trotzdem geladen. Gibt die Anzahl erfolgreich geladener Artikel zurueck.
    public int ladenAusDatei(String dateipfad) throws UngueltigeEingabeException {
        ArrayList<Artikel> geladen;
        try {
            geladen = DateiService.laden(dateipfad);
        } catch (IOException e) {
            throw new UngueltigeEingabeException(
                    "Datei konnte nicht gelesen werden: " + e.getMessage());
        }
        int erfolg = 0;
        for (Artikel a : geladen) {
            try {
                hinzufuegen(a);
                erfolg++;
            } catch (UngueltigeEingabeException e) {
                log("Beim Laden uebersprungen: #" + a.getId() + " (" + e.getMessage() + ")");
            }
        }
        log("Aus Datei geladen: " + erfolg + " von " + geladen.size() + " Artikeln");
        return erfolg;
    }

    public int ladenAusCSV(String dateipfad) throws UngueltigeEingabeException {
        ArrayList<Artikel> geladen;
        try {
            geladen = DateiService.ladenCSV(dateipfad);
        } catch (IOException e) {
            throw new UngueltigeEingabeException(
                    "CSV-Datei konnte nicht gelesen werden: " + e.getMessage());
        }
        int erfolg = 0;
        for (Artikel a : geladen) {
            try {
                hinzufuegen(a);
                erfolg++;
            } catch (UngueltigeEingabeException e) {
                log("Beim CSV-Laden uebersprungen: #" + a.getId()
                        + " (" + e.getMessage() + ")");
            }
        }
        log("Aus CSV geladen: " + erfolg + " von " + geladen.size() + " Artikeln");
        return erfolg;
    }

    public void speichernInDatei(String dateipfad) throws UngueltigeEingabeException {
        try {
            DateiService.speichern(artikelListe, dateipfad);
            log("In Datei gespeichert: " + dateipfad + " (" + artikelListe.size() + " Artikel)");
        } catch (IOException e) {
            throw new UngueltigeEingabeException(
                    "Datei konnte nicht geschrieben werden: " + e.getMessage());
        }
    }

    public void speichernAlsCSV(String dateipfad) throws UngueltigeEingabeException {
        try {
            DateiService.speichernCSV(artikelListe, dateipfad);
            log("Als CSV gespeichert: " + dateipfad + " (" + artikelListe.size() + " Artikel)");
        } catch (IOException e) {
            throw new UngueltigeEingabeException(
                    "CSV-Datei konnte nicht geschrieben werden: " + e.getMessage());
        }
    }
}
