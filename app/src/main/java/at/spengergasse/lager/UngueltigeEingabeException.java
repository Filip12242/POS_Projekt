package at.spengergasse.lager;

// Eigene Exception fuer ungueltige Eingaben (negativer Preis, leerer Name, ...).
// Wird im Konstruktor, in Settern und in der LagerVerwaltung geworfen.
public class UngueltigeEingabeException extends Exception {

    public UngueltigeEingabeException(String msg) {
        super(msg);
    }
}
