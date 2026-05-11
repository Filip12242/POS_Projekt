package at.spengergasse.lager;

// Abstrakte Oberklasse fuer alle Artikel im Lager.
// Drei konkrete Subklassen: ElektronikArtikel, Lebensmittel, Kleidung.
public abstract class Artikel {

    // Mehrwertsteuersaetze in Oesterreich (zentral hier, damit die Subklassen
    // nicht je einen Magic-Number-1.20/1.10 im Code stehen haben).
    public static final double MWST_NORMAL = 0.20;        // 20 % Standard
    public static final double MWST_REDUZIERT = 0.10;     // 10 % fuer Lebensmittel

    private int id;
    private String name;
    private String marke;
    private double preis;
    private int bestand;

    public Artikel(int id, String name, String marke, double preis, int bestand)
            throws UngueltigeEingabeException {
        // Erst pruefen, dann zuweisen. Die pruefe...-Methoden werfen bei Fehlern.
        pruefeId(id);
        pruefeName(name);
        pruefeMarke(marke);
        pruefePreis(preis);
        pruefeBestand(bestand);

        this.id = id;
        this.name = name;
        this.marke = marke;
        this.preis = preis;
        this.bestand = bestand;
    }

    // ---- Getter ----
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMarke() {
        return marke;
    }

    public double getPreis() {
        return preis;
    }

    public int getBestand() {
        return bestand;
    }

    // ---- Setter mit Validierung ----
    // Achtung: setId ist heikel — wenn der Artikel schon in einer LagerVerwaltung
    // liegt, kann das die Konsistenz brechen (entfernen(id), suchen funktionieren
    // dann nicht mehr wie erwartet). Aufrufer ist selbst verantwortlich.
    public void setId(int id) throws UngueltigeEingabeException {
        pruefeId(id);
        this.id = id;
    }

    public void setName(String name) throws UngueltigeEingabeException {
        pruefeName(name);
        this.name = name;
    }

    public void setMarke(String marke) throws UngueltigeEingabeException {
        pruefeMarke(marke);
        this.marke = marke;
    }

    public void setPreis(double preis) throws UngueltigeEingabeException {
        pruefePreis(preis);
        this.preis = preis;
    }

    public void setBestand(int bestand) throws UngueltigeEingabeException {
        pruefeBestand(bestand);
        this.bestand = bestand;
    }

    // ---- Logik-Methoden ----

    // Gesamtwert eines Artikels im Lager = Preis * Bestand.
    public double berechneWert() {
        return preis * bestand;
    }

    // True, wenn mindestens ein Stueck auf Lager ist.
    public boolean istVerfuegbar() {
        return bestand > 0;
    }

    // ---- Abstrakte Methoden ----
    // Jede Subklasse berechnet den Endpreis (z.B. mit Mehrwertsteuer)
    // und liefert ihren Kategorie-Namen zurueck.
    public abstract double berechnePreis();
    public abstract String getKategorie();

    @Override
    public String toString() {
        // %.2f -> immer 2 Nachkommastellen
        return getKategorie() + " #" + id + " - " + name + " (" + marke + "), "
                + String.format("%.2f", preis) + " EUR, Bestand: " + bestand;
    }

    // ---- Private statische Validierungs-Helfer ----
    // Static, damit Subklassen sie nicht ueberschreiben koennen — das macht
    // den Aufruf aus dem Konstruktor sicher (im Gegensatz zu einem normalen
    // Setter, der durch Polymorphie unerwartet auf nicht-initialisierte
    // Subklassenfelder zugreifen koennte).
    // Konstruktor und Setter rufen denselben Helfer auf — Validierung ist nur
    // an einer Stelle definiert.
    private static void pruefeId(int id) throws UngueltigeEingabeException {
        if (id <= 0) {
            throw new UngueltigeEingabeException("ID muss groesser als 0 sein");
        }
    }

    private static void pruefeName(String name) throws UngueltigeEingabeException {
        if (name == null || name.isBlank()) {
            throw new UngueltigeEingabeException("Name darf nicht leer sein");
        }
    }

    private static void pruefeMarke(String marke) throws UngueltigeEingabeException {
        if (marke == null || marke.isBlank()) {
            throw new UngueltigeEingabeException("Marke darf nicht leer sein");
        }
    }

    private static void pruefePreis(double preis) throws UngueltigeEingabeException {
        if (preis < 0) {
            throw new UngueltigeEingabeException("Preis darf nicht negativ sein");
        }
    }

    private static void pruefeBestand(int bestand) throws UngueltigeEingabeException {
        if (bestand < 0) {
            throw new UngueltigeEingabeException("Bestand darf nicht negativ sein");
        }
    }
}
