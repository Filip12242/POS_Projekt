package at.spengergasse.lager;

// Oberklasse fuer alle Artikel. Subklassen: ElektronikArtikel, Lebensmittel, Kleidung.
public abstract class Artikel {

    // MwSt-Saetze als Konstanten, damit nicht ueberall 1.20 / 1.10 im Code steht.
    public static final double MWST_NORMAL = 0.20;        // 20 % Standard
    public static final double MWST_REDUZIERT = 0.10;     // 10 % fuer Lebensmittel

    private int id;
    private String name;
    private String marke;
    private double preis;
    private int bestand;
    private boolean favorit; // Favorit-Stern, default false

    public Artikel(int id, String name, String marke, double preis, int bestand)
            throws UngueltigeEingabeException {
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

    public boolean istFavorit() {
        return favorit;
    }

    public void setFavorit(boolean favorit) {
        this.favorit = favorit;
    }

    // ---- Setter mit Validierung ----
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

    public double berechneWert() {
        return preis * bestand;
    }

    public boolean istVerfuegbar() {
        return bestand > 0;
    }

    // ---- Abstrakte Methoden: jede Subklasse rechnet ihren Preis und Kategorie-Namen ----
    public abstract double berechnePreis();
    public abstract String getKategorie();

    @Override
    public String toString() {
        return getKategorie() + " #" + id + " - " + name + " (" + marke + "), "
                + String.format("%.2f", preis) + " EUR, Bestand: " + bestand;
    }

    // ---- Validierungs-Helfer (static, damit Konstruktor und Setter dieselbe Pruefung nutzen) ----
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
