package at.spengergasse.lager;

// Konfektionsgroessen fuer Kleidung.
// Reihenfolge ist wichtig: XS < S < M < L < XL
// (wird in istGroesseKleinerGleich() ueber ordinal() ausgewertet).
public enum Groesse {
    XS,
    S,
    M,
    L,
    XL
}
