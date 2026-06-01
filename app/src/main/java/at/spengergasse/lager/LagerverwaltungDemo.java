package at.spengergasse.lager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;

// Demo-Klasse, um das Backend auf der Konsole zu testen.
// Wird ueber "Run" auf der main-Methode aus IntelliJ gestartet.
// Hat NICHTS mit der Vaadin-App zu tun — laeuft komplett unabhaengig.
public class LagerverwaltungDemo {

    public static void main(String[] args) {

        LagerVerwaltung v = new LagerVerwaltung();

        try {
            // ---- Sechs Artikel anlegen (gemischt aus allen drei Subklassen) ----
            // Datumsangaben relativ zu heute, damit der Demo immer sinnvolle
            // Ergebnisse zeigt (sonst veraltet das Hardcoded-Datum).

            ElektronikArtikel laptop = new ElektronikArtikel(
                    1, "ThinkPad X1", "Lenovo", 1499.00, 5, 24);

            ElektronikArtikel handy = new ElektronikArtikel(
                    2, "Galaxy S25", "Samsung", 899.00, 12, 12);

            Lebensmittel joghurt = new Lebensmittel(
                    3, "Bio-Naturjoghurt", "Berghof", 1.29, 50,
                    LocalDate.now().plusDays(7), true);   // laeuft in 7 Tagen ab

            Lebensmittel milch = new Lebensmittel(
                    4, "Vollmilch", "Joya", 1.49, 8,
                    LocalDate.now().minusDays(5), false); // schon 5 Tage abgelaufen

            Kleidung tshirt = new Kleidung(
                    5, "Basic T-Shirt", "H&M", 9.99, 20,
                    Groesse.M, "Baumwolle");

            Kleidung jacke = new Kleidung(
                    6, "Winterjacke", "Jack Wolfskin", 199.00, 3,
                    Groesse.XL, "Polyester");

            laptop.batterieHinzufuegen("Lithium-Ion");

            v.hinzufuegen(laptop);
            v.hinzufuegen(handy);
            v.hinzufuegen(joghurt);
            v.hinzufuegen(milch);
            v.hinzufuegen(tshirt);
            v.hinzufuegen(jacke);

            // ---- Alle Artikel ausgeben ----
            System.out.println("=== Alle Artikel (" + v.anzahl() + ") ===");
            for (Artikel a : v.alleAnzeigen()) {
                System.out.println(a);
            }
            System.out.println();

            // ---- Subklassen-spezifische Logik ----
            System.out.println("=== Spezifische Pruefungen ===");
            System.out.println("Laptop Batterie: " + laptop.istMitBatterie()
                    + " (" + laptop.getBatterieTyp() + ")");
            System.out.println("Joghurt Tage abgelaufen: " + joghurt.tageAbgelaufen()
                    + " (negativ = noch haltbar)");
            System.out.println("Joghurt bald abgelaufen (7 Tage)? "
                    + joghurt.istBaldAbgelaufen(7));
            System.out.println("Milch schon abgelaufen? " + milch.istAbgelaufen());
            System.out.println("T-Shirt kleiner gleich L? "
                    + tshirt.istGroesseKleinerGleich(Groesse.L));
            System.out.println("T-Shirt natuerliches Material? "
                    + tshirt.istMaterialNatuerlich());
            System.out.println("Jacke natuerliches Material? "
                    + jacke.istMaterialNatuerlich());
            System.out.println();

            // ---- Filter ----
            System.out.println("=== Filter: Preis 5 - 250 EUR ===");
            for (Artikel a : v.filterNachPreis(5, 250)) {
                System.out.println("  " + a.getName() + " ("
                        + String.format("%.2f", a.getPreis()) + " EUR)");
            }
            System.out.println();

            System.out.println("=== Filter: Bestand >= 10 ===");
            for (Artikel a : v.filterNachBestand(10)) {
                System.out.println("  " + a.getName() + " (" + a.getBestand() + " Stueck)");
            }
            System.out.println();

            System.out.println("=== Filter: Marke 'Lenovo' ===");
            for (Artikel a : v.filterNachMarke("Lenovo")) {
                System.out.println("  " + a);
            }
            System.out.println();

            System.out.println("=== Filter: abgelaufene Lebensmittel ===");
            for (Lebensmittel l : v.abgelaufeneLebensmittel()) {
                System.out.println("  " + l.getName() + " (seit " + l.tageAbgelaufen()
                        + " Tagen abgelaufen)");
            }
            System.out.println();

            // ---- Sortierung ----
            System.out.println("=== Sortiert nach Name ===");
            for (Artikel a : v.sortiereNachName()) {
                System.out.println("  " + a.getName());
            }
            System.out.println();

            System.out.println("=== Sortiert nach Preis (aufsteigend) ===");
            for (Artikel a : v.sortiereNachPreis()) {
                System.out.println("  " + String.format("%7.2f", a.getPreis())
                        + " EUR - " + a.getName());
            }
            System.out.println();

            System.out.println("=== Sortiert nach Bestand (absteigend) ===");
            for (Artikel a : v.sortiereNachBestand()) {
                System.out.println("  " + a.getBestand() + "x " + a.getName());
            }
            System.out.println();

            // ---- Aggregate ----
            // Ergebnisse einmal holen und zwischenspeichern — jeder Aufruf scannt die Liste.
            Artikel teuerster = v.teuersterArtikel();
            Artikel guenstigster = v.guenstigsterArtikel();
            ElektronikArtikel teuerstesEl = v.teuerstesElektronik();
            Kleidung teuersteKl = v.teuersteKleidung();

            System.out.println("=== Statistik ===");
            System.out.println("Gesamtwert Lager:   "
                    + String.format("%.2f", v.gesamtWert()) + " EUR");
            System.out.println("Durchschnittspreis: "
                    + String.format("%.2f", v.durchschnittPreis()) + " EUR");
            System.out.println("Gesamtbestand:      " + v.gesamtBestand() + " Stueck");
            System.out.println("Teuerster Artikel:    " + teuerster.getName()
                    + " (" + String.format("%.2f", teuerster.getPreis()) + " EUR)");
            System.out.println("Guenstigster Artikel: " + guenstigster.getName()
                    + " (" + String.format("%.2f", guenstigster.getPreis()) + " EUR)");
            System.out.println("Teuerstes Elektronik: " + teuerstesEl.getName());
            System.out.println("Teuerste Kleidung:    " + teuersteKl.getName());
            System.out.println();

            // ---- Gruppierung ----
            System.out.println("=== Gruppiert nach Kategorie ===");
            Map<String, ArrayList<Artikel>> gruppen = v.gruppiereNachKategorie();
            for (String kategorie : gruppen.keySet()) {
                System.out.println(kategorie + " (" + gruppen.get(kategorie).size() + "):");
                for (Artikel a : gruppen.get(kategorie)) {
                    System.out.println("  " + a.getName());
                }
            }
            System.out.println();

            // ---- Combo-Methode ----
            System.out.println("=== Filter + Sortierung: Preis >= 100 EUR, nach Preis ===");
            for (Artikel a : v.filterUndSortierenNachPreis(100)) {
                System.out.println("  " + String.format("%7.2f", a.getPreis())
                        + " EUR - " + a.getName());
            }
            System.out.println();

            // ---- Favoriten (optionales Feature) ----
            System.out.println("=== Favoriten markieren ===");
            v.markiereFavorit(1, true);   // Laptop
            v.markiereFavorit(3, true);   // Joghurt
            for (Artikel a : v.filterFavoriten()) {
                System.out.println("  ★ " + a.getName());
            }
            System.out.println();

            // ---- DateiService Roundtrip: Text-Format ----
            String pfadText = System.getProperty("java.io.tmpdir") + "/demo-lager.txt";
            System.out.println("=== Speichern in Text-Datei: " + pfadText + " ===");
            v.speichernInDatei(pfadText);
            LagerVerwaltung neuText = new LagerVerwaltung();
            int geladenText = neuText.ladenAusDatei(pfadText);
            System.out.println("Geladen: " + geladenText + " Artikel");
            for (Artikel a : neuText.alleAnzeigen()) {
                System.out.println("  " + a + (a.istFavorit() ? " ★" : ""));
            }
            System.out.println();

            // ---- DateiService Roundtrip: CSV-Format ----
            String pfadCSV = System.getProperty("java.io.tmpdir") + "/demo-lager.csv";
            System.out.println("=== Speichern als CSV: " + pfadCSV + " ===");
            v.speichernAlsCSV(pfadCSV);
            LagerVerwaltung neuCSV = new LagerVerwaltung();
            int geladenCSV = neuCSV.ladenAusCSV(pfadCSV);
            System.out.println("Geladen: " + geladenCSV + " Artikel aus CSV");
            System.out.println();

            // ---- CRUD-Edge-Tests ----
            System.out.println("=== Suche nach 'Vollmilch' ===");
            System.out.println(v.suchen("Vollmilch"));
            System.out.println();

            System.out.println("=== Artikel #2 entfernen ===");
            System.out.println("Entfernt: " + v.entfernen(2));
            System.out.println("Verbleibend: " + v.anzahl());
            System.out.println();

            // ---- Validierung ----
            System.out.println("=== Validierung testen ===");
            try {
                new ElektronikArtikel(-1, "", "", -50, -5, -12);
                System.out.println("FEHLER: Hier haette eine Exception kommen muessen!");
            } catch (UngueltigeEingabeException e) {
                System.out.println("OK, Exception gefangen: " + e.getMessage());
            }
            System.out.println();

            // ---- Verlaufs-Log (optionales Feature) ----
            // Am Ende, damit alle vorherigen Aktionen drinstehen.
            System.out.println("=== Verlaufs-Log (" + v.getVerlauf().size() + " Eintraege) ===");
            for (LogEintrag e : v.getVerlauf()) {
                System.out.println(e);
            }

        } catch (UngueltigeEingabeException e) {
            System.out.println("Unerwarteter Fehler: " + e.getMessage());
        }
    }
}
