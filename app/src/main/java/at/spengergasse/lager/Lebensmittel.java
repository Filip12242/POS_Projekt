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

    // boolean: keine Validierung noetig (true oder false ist immer gueltig).
    public void setBio(boolean bio) {
        this.bio = bio;
    }

    // Anzahl Tage, die der Artikel bereits abgelaufen ist.
    // Negativ heisst: noch X Tage haltbar. Positiv heisst: schon X Tage ueberfaellig.
    // Beispiel: heute 11.5.2026, haltbarBis 15.5.2026 -> -4 (noch 4 Tage haltbar)
    //          heute 18.5.2026, haltbarBis 15.5.2026 -> +3 (schon 3 Tage abgelaufen)
    public int tageAbgelaufen() {
        long tage = ChronoUnit.DAYS.between(haltbarBis, LocalDate.now());
        return (int) tage;
    }

    // Abgelaufen, sobald mindestens ein Tag ueber dem Haltbarkeitsdatum.
    // Eine Quelle der Wahrheit: tageAbgelaufen() macht den Datums-Vergleich.
    public boolean istAbgelaufen() {
        return tageAbgelaufen() > 0;
    }

    // Warnt, wenn der Artikel in den naechsten 'tage' Tagen ablaeuft.
    public boolean istBaldAbgelaufen(int tage) {
        if (istAbgelaufen()) {
            return false;
        }
        // tageAbgelaufen() ist hier negativ (noch nicht abgelaufen).
        // -tageAbgelaufen() = Tage, die noch uebrig sind.
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
