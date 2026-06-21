package at.spengergasse.lager.ui;

import at.spengergasse.lager.Artikel;
import at.spengergasse.lager.ElektronikArtikel;
import at.spengergasse.lager.Groesse;
import at.spengergasse.lager.Kleidung;
import at.spengergasse.lager.Lebensmittel;
import at.spengergasse.lager.UngueltigeEingabeException;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;

import java.time.LocalDate;

// Dialog zum Anlegen/Bearbeiten eines Artikels. Zeigt je nach Kategorie die passenden
// Zusatzfelder. Validiert wird im Artikel-Konstruktor, hier wird der Fehler nur angezeigt.
public class ArtikelFormDialog extends Dialog {

    // Callback an die View: legt an (hinzufuegen) oder speichert (aktualisieren).
    public interface Speichern {
        void speichern(Artikel a) throws UngueltigeEingabeException;
    }

    private final IntegerField idFeld = new IntegerField("ID");
    private final ComboBox<String> kategorieFeld = new ComboBox<>("Kategorie");
    private final TextField nameFeld = new TextField("Name");
    private final TextField markeFeld = new TextField("Marke");
    private final NumberField preisFeld = new NumberField("Preis (EUR)");
    private final IntegerField bestandFeld = new IntegerField("Bestand");

    // Elektronik
    private final IntegerField garantieFeld = new IntegerField("Garantie (Monate)");
    private final TextField batterieFeld = new TextField("Batterietyp (optional)");
    // Lebensmittel
    private final DatePicker haltbarFeld = new DatePicker("Haltbar bis");
    private final Checkbox bioFeld = new Checkbox("Bio");
    // Kleidung
    private final ComboBox<Groesse> groesseFeld = new ComboBox<>("Groesse");
    private final TextField materialFeld = new TextField("Material");

    private final Artikel vorhanden; // null = neuer Artikel

    public ArtikelFormDialog(Artikel vorhanden, Speichern callback) {
        this.vorhanden = vorhanden;
        setHeaderTitle(vorhanden == null ? "Neuer Artikel" : "Artikel bearbeiten");

        kategorieFeld.setItems("Elektronik", "Lebensmittel", "Kleidung");
        groesseFeld.setItems(Groesse.values());

        FormLayout form = new FormLayout(
                idFeld, kategorieFeld, nameFeld, markeFeld, preisFeld, bestandFeld,
                garantieFeld, batterieFeld, haltbarFeld, bioFeld, groesseFeld, materialFeld);
        add(form);

        kategorieFeld.addValueChangeListener(e -> zeigePassendeFelder());

        Button speichern = new Button("Speichern", e -> aufSpeichern(callback));
        speichern.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button abbrechen = new Button("Abbrechen", e -> close());
        getFooter().add(abbrechen, speichern);

        if (vorhanden != null) {
            vorbefuellen();
        } else {
            zeigePassendeFelder();
        }
    }

    // Blendet die Zusatzfelder passend zur Kategorie ein/aus.
    private void zeigePassendeFelder() {
        String k = kategorieFeld.getValue();
        boolean elektronik = "Elektronik".equals(k);
        boolean lebensmittel = "Lebensmittel".equals(k);
        boolean kleidung = "Kleidung".equals(k);

        garantieFeld.setVisible(elektronik);
        batterieFeld.setVisible(elektronik);
        haltbarFeld.setVisible(lebensmittel);
        bioFeld.setVisible(lebensmittel);
        groesseFeld.setVisible(kleidung);
        materialFeld.setVisible(kleidung);
    }

    // Beim Bearbeiten die Felder mit den vorhandenen Werten fuellen.
    private void vorbefuellen() {
        idFeld.setValue(vorhanden.getId());
        idFeld.setReadOnly(true);                 // ID nicht aenderbar
        kategorieFeld.setValue(vorhanden.getKategorie());
        kategorieFeld.setReadOnly(true);          // Typ nicht aenderbar
        nameFeld.setValue(vorhanden.getName());
        markeFeld.setValue(vorhanden.getMarke());
        preisFeld.setValue(vorhanden.getPreis());
        bestandFeld.setValue(vorhanden.getBestand());

        if (vorhanden instanceof ElektronikArtikel) {
            ElektronikArtikel e = (ElektronikArtikel) vorhanden;
            garantieFeld.setValue(e.getGarantieMonate());
            if (e.getBatterieTyp() != null) {
                batterieFeld.setValue(e.getBatterieTyp());
            }
        } else if (vorhanden instanceof Lebensmittel) {
            Lebensmittel l = (Lebensmittel) vorhanden;
            haltbarFeld.setValue(l.getHaltbarBis());
            bioFeld.setValue(l.istBio());
        } else if (vorhanden instanceof Kleidung) {
            Kleidung k = (Kleidung) vorhanden;
            groesseFeld.setValue(k.getGroesse());
            materialFeld.setValue(k.getMaterial());
        }
        zeigePassendeFelder();
    }

    private void aufSpeichern(Speichern callback) {
        try {
            Artikel a = baueArtikel();
            if (vorhanden != null) {
                a.setFavorit(vorhanden.istFavorit());  // Favorit-Markierung erhalten
            }
            callback.speichern(a);
            close();
        } catch (UngueltigeEingabeException ex) {
            Notification.show(ex.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

    // Baut aus den Feldern den passenden Artikel-Typ. Wirft bei ungueltigen Werten.
    private Artikel baueArtikel() throws UngueltigeEingabeException {
        String k = kategorieFeld.getValue();
        if (k == null) {
            throw new UngueltigeEingabeException("Bitte eine Kategorie waehlen");
        }
        int id = pflichtInt(idFeld, "ID");
        String name = nameFeld.getValue();
        String marke = markeFeld.getValue();
        double preis = pflichtDouble(preisFeld, "Preis");
        int bestand = pflichtInt(bestandFeld, "Bestand");

        if (k.equals("Elektronik")) {
            int garantie = pflichtInt(garantieFeld, "Garantie");
            ElektronikArtikel e =
                    new ElektronikArtikel(id, name, marke, preis, bestand, garantie);
            String batterie = batterieFeld.getValue();
            if (batterie != null && !batterie.isBlank()) {
                e.batterieHinzufuegen(batterie);
            }
            return e;
        } else if (k.equals("Lebensmittel")) {
            LocalDate haltbar = haltbarFeld.getValue();   // null -> Konstruktor wirft
            return new Lebensmittel(id, name, marke, preis, bestand, haltbar, bioFeld.getValue());
        } else {
            Groesse groesse = groesseFeld.getValue();     // null -> Konstruktor wirft
            return new Kleidung(id, name, marke, preis, bestand, groesse, materialFeld.getValue());
        }
    }

    private int pflichtInt(IntegerField feld, String name) throws UngueltigeEingabeException {
        Integer v = feld.getValue();
        if (v == null) {
            throw new UngueltigeEingabeException(name + " muss eine Zahl sein");
        }
        return v;
    }

    private double pflichtDouble(NumberField feld, String name) throws UngueltigeEingabeException {
        Double v = feld.getValue();
        if (v == null) {
            throw new UngueltigeEingabeException(name + " muss eine Zahl sein");
        }
        return v;
    }
}
