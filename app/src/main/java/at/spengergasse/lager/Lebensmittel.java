package at.spengergasse.lager;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

// Lebensmittel-Artikel (Joghurt, Milch, Brot, ...).
// Hat ein Ablaufdatum und ein Bio-Flag.
public class Lebensmittel extends Artikel {

    private LocalDate haltbarBis;
    private boolean bio;

    public Lebensmittel(int id, String name, String marke,
                        double preis, int bestand,
                        LocalDate haltbarBis, boolean bio)
            throws UngueltigeEingabeException {
        super(id, name, marke, preis, bestand);
        pruefeHaltbarBis(haltbarBis);

        this.haltbarBis = haltbarBis;
        this.bio = bio;
    }

    public LocalDate getHaltbarBis() {
        return haltbarBis;
    }

    public void setHaltbarBis(LocalDate haltbarBis) throws UngueltigeEingabeException {
        pruefeHaltbarBis(haltbarBis);
        this.haltbarBis = haltbarBis;
    }

    public boolean istBio() {
        return bio;
    }

    public void setBio(boolean bio) {
        this.bio = bio;
    }

    // Negativ = noch X Tage haltbar, positiv = seit X Tagen abgelaufen.
    public int tageAbgelaufen() {
        long tage = ChronoUnit.DAYS.between(haltbarBis, LocalDate.now());
        return (int) tage;
    }

    public boolean istAbgelaufen() {
        return tageAbgelaufen() > 0;
    }

    // True, wenn der Artikel innerhalb der naechsten 'tage' Tage ablaeuft.
    public boolean istBaldAbgelaufen(int tage) {
        if (istAbgelaufen()) {
            return false;
        }
        return -tageAbgelaufen() <= tage;
    }

    @Override
    public double berechnePreis() {
        // Bruttopreis mit reduzierter MwSt fuer Lebensmittel.
        return getPreis() * (1 + MWST_REDUZIERT);
    }

    @Override
    public String getKategorie() {
        return "Lebensmittel";
    }

    private static void pruefeHaltbarBis(LocalDate haltbarBis) throws UngueltigeEingabeException {
        if (haltbarBis == null) {
            throw new UngueltigeEingabeException("Haltbarkeits-Datum darf nicht null sein");
        }
    }
}
