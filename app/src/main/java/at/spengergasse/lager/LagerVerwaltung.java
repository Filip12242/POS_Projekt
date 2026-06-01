package at.spengergasse.lager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

// Verwaltet alle Artikel im Lager.
// Diese Klasse haelt die Liste und stellt alle fachlichen Operationen bereit:
// CRUD, Filter, Sortierung, Gruppierung, Auswertungen, Combo-Methoden,
// Favoriten-Filter und ein Verlaufs-Log (optionale Features lt. Aufgabe).
public class LagerVerwaltung {

    private ArrayList<Artikel> artikelListe;
    // Verlaufs-Log: bei jeder CRUD-Aktion wird ein Eintrag angelegt.
    private ArrayList<LogEintrag> verlauf;

    public LagerVerwaltung() {
        this.artikelListe = new ArrayList<>();
        this.verlauf = new ArrayList<>();
    }

    // Interner Helfer, der einen Log-Eintrag anlegt. Wird von allen
    // mutierenden Methoden aufgerufen.
    private void log(String aktion) {
        verlauf.add(new LogEintrag(aktion));
    }

    // Neuen Artikel hinzufuegen. ID muss eindeutig sein.
    public void hinzufuegen(Artikel a) throws UngueltigeEingabeException {
        if (a == null) {
            throw new UngueltigeEingabeException("Artikel darf nicht null sein");
        }
        // Pruefen, ob ID schon vergeben ist
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

    // Sucht den ersten Artikel mit dem angegebenen Namen (case-insensitive).
    // Gibt null zurueck, wenn nichts gefunden wurde.
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

    // Gibt eine Kopie der Artikel-Liste zurueck.
    // Kopie statt Original, damit der Aufrufer die interne Liste nicht versehentlich
    // veraendern kann (z.B. .clear() oder .add() von aussen).
    public ArrayList<Artikel> alleAnzeigen() {
        return new ArrayList<>(artikelListe);
    }

    public int anzahl() {
        return artikelListe.size();
    }

    // ---- Filter-Methoden ----

    // Alle Artikel im angegebenen Preisbereich (inklusive Grenzen).
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

    // Alle Artikel mit mindestens dem angegebenen Bestand.
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

    // Alle Lebensmittel-Artikel, die bereits abgelaufen sind.
    // Zeigt instanceof + Cast — Polymorphie auf Subklassen-Ebene.
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

    // ---- Sortier-Methoden ----
    // Jede Methode arbeitet auf einer KOPIE der Liste, damit die interne Reihenfolge
    // erhalten bleibt. Die Comparator-Klassen sind anonyme innere Klassen — kein Lambda,
    // damit der Code mit Java-Grundwissen (Interfaces) lesbar bleibt.

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

    // Gesamtwert des Lagers = Summe aller berechneWert().
    public double gesamtWert() {
        double summe = 0;
        for (Artikel a : artikelListe) {
            summe = summe + a.berechneWert();
        }
        return summe;
    }

    // Durchschnitt der Listenpreise. 0 wenn das Lager leer ist.
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

    // Summe der Stueckzahlen aller Artikel.
    public int gesamtBestand() {
        int summe = 0;
        for (Artikel a : artikelListe) {
            summe = summe + a.getBestand();
        }
        return summe;
    }

    // Artikel mit dem hoechsten Preis. null wenn das Lager leer ist.
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

    // Artikel mit dem niedrigsten Preis. null wenn das Lager leer ist.
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

    // Teuerstes Elektronik-Geraet. null wenn keines vorhanden ist.
    // Typ-spezifischer Rueckgabewert -> kein Cast beim Aufrufer noetig.
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

    // Teuerstes Kleidungsstueck. null wenn keines vorhanden ist.
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

    // Gruppiert die Artikel nach ihrer Kategorie (Elektronik / Lebensmittel / Kleidung).
    // Schluessel = Kategorie-Name (aus a.getKategorie()), Wert = Liste der Artikel.
    // TreeMap (statt HashMap) -> Keys sind alphabetisch sortiert, Ausgabe deterministisch.
    public Map<String, ArrayList<Artikel>> gruppiereNachKategorie() {
        Map<String, ArrayList<Artikel>> gruppen = new TreeMap<>();
        for (Artikel a : artikelListe) {
            String kategorie = a.getKategorie();
            // Wenn die Gruppe noch nicht existiert, neue Liste anlegen.
            if (!gruppen.containsKey(kategorie)) {
                gruppen.put(kategorie, new ArrayList<Artikel>());
            }
            gruppen.get(kategorie).add(a);
        }
        return gruppen;
    }

    // ---- Combo-Methode (Filter + Sortieren in einem Aufruf) ----

    // Filtert alle Artikel >= minPreis und sortiert das Ergebnis nach Preis aufsteigend.
    // Erfuellt die Angabe-Anforderung "mind. eine Methode, die mehrere
    // Verarbeitungsschritte kombiniert".
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

    // ---- Favoriten-Filter (optionales Feature lt. Aufgabe) ----

    public ArrayList<Artikel> filterFavoriten() {
        ArrayList<Artikel> ergebnis = new ArrayList<>();
        for (Artikel a : artikelListe) {
            if (a.istFavorit()) {
                ergebnis.add(a);
            }
        }
        return ergebnis;
    }

    // Markiert oder demarkiert einen Artikel als Favorit. Logged die Aktion.
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

    // Gibt eine Kopie der Log-Eintraege zurueck (defensive Kopie).
    public ArrayList<LogEintrag> getVerlauf() {
        return new ArrayList<>(verlauf);
    }

    // ---- Datei-Operationen (delegieren an DateiService, mit try-catch) ----
    // Erfuellt §3 ("try-catch in der Verwaltung") und §4 ("Behandlung von I/O-Fehlern").

    // Laedt Artikel aus einer Text-Datei und fuegt sie zur Liste hinzu.
    // Defekte Zeilen werden uebersprungen und in den Log geschrieben — der Rest
    // wird trotzdem geladen. Gibt die Anzahl erfolgreich geladener Artikel zurueck.
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
                // z.B. doppelte ID -> ueberspringen, weiter laden
                log("Beim Laden uebersprungen: #" + a.getId() + " (" + e.getMessage() + ")");
            }
        }
        log("Aus Datei geladen: " + erfolg + " von " + geladen.size() + " Artikeln");
        return erfolg;
    }

    // Analog fuer CSV-Format.
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

    // Speichert die aktuelle Liste in eine Text-Datei.
    public void speichernInDatei(String dateipfad) throws UngueltigeEingabeException {
        try {
            DateiService.speichern(artikelListe, dateipfad);
            log("In Datei gespeichert: " + dateipfad + " (" + artikelListe.size() + " Artikel)");
        } catch (IOException e) {
            throw new UngueltigeEingabeException(
                    "Datei konnte nicht geschrieben werden: " + e.getMessage());
        }
    }

    // Speichert die aktuelle Liste als CSV.
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
