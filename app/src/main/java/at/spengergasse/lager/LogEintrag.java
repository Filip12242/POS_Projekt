package at.spengergasse.lager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Ein Eintrag im Verlaufs-Log: Zeitpunkt + was gemacht wurde (hinzufuegen/entfernen/aendern).
public class LogEintrag {

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private LocalDateTime zeitpunkt;
    private String aktion;

    public LogEintrag(String aktion) {
        this.zeitpunkt = LocalDateTime.now();
        this.aktion = aktion;
    }

    public LocalDateTime getZeitpunkt() {
        return zeitpunkt;
    }

    public String getAktion() {
        return aktion;
    }

    @Override
    public String toString() {
        return "[" + FORMAT.format(zeitpunkt) + "] " + aktion;
    }
}
