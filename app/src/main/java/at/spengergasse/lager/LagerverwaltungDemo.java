package at.spengergasse.lager;

import java.time.LocalDate;

// Kleine Demo-Klasse, um den Lagerverwaltungs-Code auf der Konsole zu testen.
// Wird ueber "Run" auf der main-Methode aus IntelliJ gestartet.
// Hat NICHTS mit der Vaadin-App zu tun — laeuft komplett unabhaengig.
public class LagerverwaltungDemo {

    public static void main(String[] args) {

        LagerVerwaltung verwaltung = new LagerVerwaltung();

        try {
            // ---- Drei Artikel anlegen (einen pro Subklasse) ----

            ElektronikArtikel laptop = new ElektronikArtikel(
                    1, "ThinkPad X1", "Lenovo",
                    1499.00, 5,
                    24);

            Lebensmittel joghurt = new Lebensmittel(
                    2, "Bio-Naturjoghurt", "Berghof",
                    1.29, 50,
                    LocalDate.of(2026, 5, 15),  // laeuft bald ab (heute = 11.5.2026)
                    true);

            Kleidung tshirt = new Kleidung(
                    3, "Basic T-Shirt", "H&M",
                    9.99, 20,
                    Groesse.M, "Baumwolle");

            verwaltung.hinzufuegen(laptop);
            verwaltung.hinzufuegen(joghurt);
            verwaltung.hinzufuegen(tshirt);

            // ---- Batterie nachtraeglich zuweisen ----
            laptop.batterieHinzufuegen("Lithium-Ion");

            // ---- Alle Artikel ausgeben ----
            System.out.println("=== Alle Artikel (" + verwaltung.anzahl() + ") ===");
            for (Artikel a : verwaltung.alleAnzeigen()) {
                System.out.println(a);
                System.out.println("  Bruttopreis: "
                        + String.format("%.2f", a.berechnePreis()) + " EUR");
                System.out.println("  Lagerwert:   "
                        + String.format("%.2f", a.berechneWert()) + " EUR");
                System.out.println("  Verfuegbar:  " + a.istVerfuegbar());
            }
            System.out.println();

            // ---- Subklassen-spezifische Logik testen ----
            System.out.println("=== Spezifische Pruefungen ===");
            System.out.println("Laptop hat Batterie: " + laptop.istMitBatterie()
                    + " (" + laptop.getBatterieTyp() + ")");
            System.out.println("Joghurt laeuft ab am: " + joghurt.getHaltbarBis());
            System.out.println("  tageAbgelaufen: " + joghurt.tageAbgelaufen()
                    + " (negativ = noch haltbar)");
            System.out.println("  Bald abgelaufen (in 7 Tagen)? "
                    + joghurt.istBaldAbgelaufen(7));
            System.out.println("  Schon abgelaufen? " + joghurt.istAbgelaufen());
            System.out.println("T-Shirt-Groesse: " + tshirt.getGroesse());
            System.out.println("  kleiner gleich L? "
                    + tshirt.istGroesseKleinerGleich(Groesse.L));
            System.out.println("  kleiner gleich S? "
                    + tshirt.istGroesseKleinerGleich(Groesse.S));
            System.out.println();

            // ---- Suchen ----
            System.out.println("=== Suche nach 'Bio-Naturjoghurt' ===");
            Artikel gefunden = verwaltung.suchen("Bio-Naturjoghurt");
            System.out.println(gefunden);
            System.out.println();

            // ---- Entfernen ----
            System.out.println("=== Artikel #2 entfernen ===");
            boolean weg = verwaltung.entfernen(2);
            System.out.println("Entfernt: " + weg);
            System.out.println("Verbleibend: " + verwaltung.anzahl());
            System.out.println();

            // ---- Validierung: ungueltige Eingabe muss Exception werfen ----
            System.out.println("=== Validierung testen ===");
            try {
                ElektronikArtikel falsch = new ElektronikArtikel(
                        -1, "", "",
                        -50, -5, -12);
                System.out.println("FEHLER: Hier haette eine Exception kommen muessen!");
            } catch (UngueltigeEingabeException e) {
                System.out.println("OK, Exception gefangen: " + e.getMessage());
            }

        } catch (UngueltigeEingabeException e) {
            System.out.println("Unerwarteter Fehler: " + e.getMessage());
        }
    }
}
