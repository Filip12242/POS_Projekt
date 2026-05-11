package at.spengergasse.lager;

// Elektronik-Artikel (Handys, Fernseher, Akkugeraete, ...).
// Hat Garantie und kann optional eine Batterie haben.
public class ElektronikArtikel extends Artikel {

    private int garantieMonate;

    // Wenn null -> keine Batterie. Wenn gesetzt -> Batterie mit diesem Typ.
    // Frueher gab es zusaetzlich ein 'hatBatterie:boolean'-Feld; das war redundant,
    // weil es immer (batterieTyp != null) entsprach. Jetzt nur noch ein Feld.
    private String batterieTyp;

    public ElektronikArtikel(int id, String name, String marke,
                             double preis, int bestand, int garantieMonate)
            throws UngueltigeEingabeException {
        super(id, name, marke, preis, bestand);
        pruefeGarantieMonate(garantieMonate);

        this.garantieMonate = garantieMonate;
        this.batterieTyp = null;
    }

    public int getGarantieMonate() {
        return garantieMonate;
    }

    public void setGarantieMonate(int garantieMonate) throws UngueltigeEingabeException {
        pruefeGarantieMonate(garantieMonate);
        this.garantieMonate = garantieMonate;
    }

    public String getBatterieTyp() {
        return batterieTyp;
    }

    // Batterie nachtraeglich zuweisen.
    public void batterieHinzufuegen(String typ) throws UngueltigeEingabeException {
        if (typ == null || typ.isBlank()) {
            throw new UngueltigeEingabeException("Batterietyp darf nicht leer sein");
        }
        this.batterieTyp = typ;
    }

    // Batterie wieder entfernen.
    public void batterieEntfernen() {
        this.batterieTyp = null;
    }

    // "Hat Batterie?" wird aus dem Typ-Feld abgeleitet — keine doppelte Buchfuehrung.
    public boolean istMitBatterie() {
        return batterieTyp != null;
    }

    @Override
    public double berechnePreis() {
        // Bruttopreis mit Standard-MwSt.
        return getPreis() * (1 + MWST_NORMAL);
    }

    @Override
    public String getKategorie() {
        return "Elektronik";
    }

    private static void pruefeGarantieMonate(int garantieMonate) throws UngueltigeEingabeException {
        if (garantieMonate < 0) {
            throw new UngueltigeEingabeException("Garantie darf nicht negativ sein");
        }
    }
}
