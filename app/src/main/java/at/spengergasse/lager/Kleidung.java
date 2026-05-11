package at.spengergasse.lager;

// Kleidungs-Artikel (T-Shirts, Hosen, Jacken, ...).
// Hat eine Konfektionsgroesse (Enum) und ein Material.
public class Kleidung extends Artikel {

    private Groesse groesse;
    private String material;

    public Kleidung(int id, String name, String marke,
                    double preis, int bestand,
                    Groesse groesse, String material)
            throws UngueltigeEingabeException {
        super(id, name, marke, preis, bestand);
        pruefeGroesse(groesse);
        pruefeMaterial(material);

        this.groesse = groesse;
        this.material = material;
    }

    public Groesse getGroesse() {
        return groesse;
    }

    public void setGroesse(Groesse groesse) throws UngueltigeEingabeException {
        pruefeGroesse(groesse);
        this.groesse = groesse;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) throws UngueltigeEingabeException {
        pruefeMaterial(material);
        this.material = material;
    }

    // Prueft, ob diese Kleidung kleiner oder gleich der angegebenen Groesse ist.
    // ordinal() liefert die Position im Enum: XS=0, S=1, M=2, L=3, XL=4
    public boolean istGroesseKleinerGleich(Groesse vergleich) {
        return this.groesse.ordinal() <= vergleich.ordinal();
    }

    // Prueft, ob das Material ein Naturfaserstoff ist.
    // Beispielnutzung: Filter im Shop ("nur Naturmaterialien anzeigen").
    public boolean istMaterialNatuerlich() {
        String m = material.toLowerCase();
        return m.equals("baumwolle")
                || m.equals("wolle")
                || m.equals("leinen")
                || m.equals("seide");
    }

    @Override
    public double berechnePreis() {
        // Bruttopreis mit Standard-MwSt.
        return getPreis() * (1 + MWST_NORMAL);
    }

    @Override
    public String getKategorie() {
        return "Kleidung";
    }

    private static void pruefeGroesse(Groesse groesse) throws UngueltigeEingabeException {
        if (groesse == null) {
            throw new UngueltigeEingabeException("Groesse darf nicht null sein");
        }
    }

    private static void pruefeMaterial(String material) throws UngueltigeEingabeException {
        if (material == null || material.isBlank()) {
            throw new UngueltigeEingabeException("Material darf nicht leer sein");
        }
    }
}
