package at.spengergasse.lager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LagerVerwaltungTest {

    @Test
    void aendern_existierenderArtikel_wirdErsetzt() throws UngueltigeEingabeException {
        LagerVerwaltung v = new LagerVerwaltung();
        v.hinzufuegen(new ElektronikArtikel(1, "OldDevice", "Brand", 100.0, 5, 12));

        ElektronikArtikel neu = new ElektronikArtikel(1, "NewDevice", "BrandX", 150.0, 3, 24);
        boolean changed = v.aendern(1, neu);

        assertTrue(changed);
        Artikel a = v.suchen("NewDevice");
        assertNotNull(a);
        assertEquals(150.0, a.getPreis(), 0.001);
        assertEquals(1, v.anzahl());
    }

    @Test
    void aendern_nichtExistiert_gibtFalse() throws UngueltigeEingabeException {
        LagerVerwaltung v = new LagerVerwaltung();
        ElektronikArtikel neu = new ElektronikArtikel(1, "NewDevice", "BrandX", 150.0, 3, 24);
        assertFalse(v.aendern(1, neu));
    }

    @Test
    void aendern_null_wirft() {
        LagerVerwaltung v = new LagerVerwaltung();
        assertThrows(UngueltigeEingabeException.class, () -> v.aendern(1, null));
    }

    @Test
    void aendern_idMismatch_wirft() throws UngueltigeEingabeException {
        LagerVerwaltung v = new LagerVerwaltung();
        v.hinzufuegen(new ElektronikArtikel(1, "OldDevice", "Brand", 100.0, 5, 12));
        ElektronikArtikel neu = new ElektronikArtikel(2, "NewDevice", "BrandX", 150.0, 3, 24);
        assertThrows(UngueltigeEingabeException.class, () -> v.aendern(1, neu));
    }
}
