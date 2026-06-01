package at.spengergasse.lager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

// Verwaltet alle Artikel im Lager.
// Diese Klasse haelt die Liste und stellt alle fachlichen Operationen bereit:
// CRUD, Filter, Sortierung, Gruppierung, Auswertungen und Combo-Methoden.
// Noch offen: DateiService (Session 3), optionale Features (Log, Favoriten).
public class LagerVerwaltung {

    private ArrayList<Artikel> artikelListe;

    public LagerVerwaltung() {
        this.artikelListe = new ArrayList<>();
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
    }

    // Artikel per ID entfernen. Gibt true zurueck, wenn etwas geloescht wurde.
    public boolean entfernen(int id) {
        for (int i = 0; i < artikelListe.size(); i++) {
            if (artikelListe.get(i).getId() == id) {
                artikelListe.remove(i);
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
}
