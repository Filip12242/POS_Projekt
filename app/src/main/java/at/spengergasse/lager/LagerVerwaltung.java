package at.spengergasse.lager;

import java.util.ArrayList;

// Verwaltet alle Artikel im Lager.
// Diese Klasse haelt die Liste und stellt alle fachlichen Operationen bereit.
// In diesem Stand: nur grundlegende CRUD-Methoden.
// Filter, Sortierung, Aggregate und Combo-Methoden folgen im naechsten Schritt.
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
}
