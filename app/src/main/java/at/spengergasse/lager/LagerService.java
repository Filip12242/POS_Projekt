package at.spengergasse.lager;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;

// Spring-Service: haelt EINE gemeinsame LagerVerwaltung fuer alle Views.
// Laedt beim Start aus der CSV-Datei (oder legt Demo-Daten an) und speichert
// nach jeder veraendernden Aktion. Die Geschaeftslogik bleibt in LagerVerwaltung;
// dieser Service kuemmert sich nur um die gemeinsame Instanz und die Persistenz.
@Service
public class LagerService {

    private static final String DATEI = "data/lager.csv";

    private final LagerVerwaltung verwaltung = new LagerVerwaltung();

    // Beim Start einmal aufgerufen: laden oder mit Demo-Daten initialisieren.
    @PostConstruct
    void init() {
        File f = new File(DATEI);
        if (f.exists()) {
            try {
                verwaltung.ladenAusCSV(DATEI);
            } catch (UngueltigeEingabeException e) {
                // Defekte/unlesbare Datei -> mit leerem Lager weiterarbeiten.
            }
        } else {
            seedDemo();
            speichern();
        }
    }

    // ---- Lesende Methoden (delegieren nur) ----

    public ArrayList<Artikel> alleAnzeigen() {
        return verwaltung.alleAnzeigen();
    }

    public int anzahl() {
        return verwaltung.anzahl();
    }

    public ArrayList<Artikel> filtern(String kategorie, String marke,
                                      double minPreis, double maxPreis)
            throws UngueltigeEingabeException {
        return verwaltung.filtern(kategorie, marke, minPreis, maxPreis);
    }

    public double gesamtWert() {
        return verwaltung.gesamtWert();
    }

    public double durchschnittPreis() {
        return verwaltung.durchschnittPreis();
    }

    public int gesamtBestand() {
        return verwaltung.gesamtBestand();
    }

    public Artikel teuersterArtikel() {
        return verwaltung.teuersterArtikel();
    }

    public Artikel guenstigsterArtikel() {
        return verwaltung.guenstigsterArtikel();
    }

    public ElektronikArtikel teuerstesElektronik() {
        return verwaltung.teuerstesElektronik();
    }

    public Kleidung teuersteKleidung() {
        return verwaltung.teuersteKleidung();
    }

    public Map<String, ArrayList<Artikel>> gruppiereNachKategorie() {
        return verwaltung.gruppiereNachKategorie();
    }

    public ArrayList<Lebensmittel> abgelaufeneLebensmittel() {
        return verwaltung.abgelaufeneLebensmittel();
    }

    public ArrayList<LogEintrag> getVerlauf() {
        return verwaltung.getVerlauf();
    }

    // ---- Veraendernde Methoden (speichern nach Erfolg) ----

    public void hinzufuegen(Artikel a) throws UngueltigeEingabeException {
        verwaltung.hinzufuegen(a);
        speichern();
    }

    public void aktualisieren(Artikel a) throws UngueltigeEingabeException {
        verwaltung.aendern(a.getId(), a);   // Kollegen-Methode aus LagerVerwaltung
        speichern();
    }

    public boolean entfernen(int id) {
        boolean weg = verwaltung.entfernen(id);
        if (weg) {
            speichern();
        }
        return weg;
    }

    public boolean markiereFavorit(int id, boolean favorit) {
        boolean ok = verwaltung.markiereFavorit(id, favorit);
        if (ok) {
            speichern();
        }
        return ok;
    }

    // ---- Persistenz-Helfer ----

    private void speichern() {
        try {
            File f = new File(DATEI).getAbsoluteFile();
            if (f.getParentFile() != null) {
                f.getParentFile().mkdirs();
            }
            verwaltung.speichernAlsCSV(DATEI);
        } catch (UngueltigeEingabeException e) {
            // Persistenz-Fehler ist nicht fatal: weiter im Speicher arbeiten.
        }
    }

    // Ein paar Beispiel-Artikel fuer den ersten Start.
    private void seedDemo() {
        try {
            ElektronikArtikel laptop =
                    new ElektronikArtikel(1, "ThinkPad X1", "Lenovo", 1499.00, 5, 24);
            laptop.batterieHinzufuegen("Lithium-Ion");
            verwaltung.hinzufuegen(laptop);
            verwaltung.hinzufuegen(
                    new ElektronikArtikel(2, "Galaxy S25", "Samsung", 899.00, 12, 12));
            verwaltung.hinzufuegen(new Lebensmittel(3, "Bio-Naturjoghurt", "Berghof",
                    1.29, 50, LocalDate.now().plusDays(7), true));
            verwaltung.hinzufuegen(new Lebensmittel(4, "Vollmilch", "Joya",
                    1.49, 8, LocalDate.now().minusDays(5), false));
            verwaltung.hinzufuegen(new Kleidung(5, "Basic T-Shirt", "H&M",
                    9.99, 20, Groesse.M, "Baumwolle"));
            verwaltung.hinzufuegen(new Kleidung(6, "Winterjacke", "Jack Wolfskin",
                    199.00, 3, Groesse.XL, "Polyester"));
        } catch (UngueltigeEingabeException e) {
            // Demo-Daten sind statisch gueltig; sollte nie passieren.
        }
    }
}
