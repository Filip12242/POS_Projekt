package at.spengergasse.lager.ui;

import at.spengergasse.lager.Artikel;
import at.spengergasse.lager.LagerService;
import at.spengergasse.lager.base.ui.ViewTitle;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.Map;

// Statistik-View: zeigt die Auswertungen der LagerVerwaltung (Gesamtwert,
// Durchschnitt, Bestand, teuerster/guenstigster Artikel, ...). Reine Anzeige -
// alle Zahlen kommen aus dem LagerService.
@Route("statistik")
@Menu(order = 2, icon = "vaadin:chart", title = "Statistik")
public class StatistikView extends VerticalLayout {

    public StatistikView(LagerService service) {
        add(new ViewTitle("Statistik"));

        add(zeile("Anzahl Artikel", String.valueOf(service.anzahl())));
        add(zeile("Gesamtwert (Preis x Bestand)",
                String.format("%.2f EUR", service.gesamtWert())));
        add(zeile("Durchschnittspreis",
                String.format("%.2f EUR", service.durchschnittPreis())));
        add(zeile("Gesamtbestand", service.gesamtBestand() + " Stueck"));
        add(zeile("Teuerster Artikel", beschreibe(service.teuersterArtikel())));
        add(zeile("Guenstigster Artikel", beschreibe(service.guenstigsterArtikel())));
        add(zeile("Teuerstes Elektronik", beschreibe(service.teuerstesElektronik())));
        add(zeile("Teuerste Kleidung", beschreibe(service.teuersteKleidung())));
        add(zeile("Abgelaufene Lebensmittel",
                String.valueOf(service.abgelaufeneLebensmittel().size())));

        add(ueberschrift("Artikel je Kategorie"));
        Map<String, ArrayList<Artikel>> gruppen = service.gruppiereNachKategorie();
        for (String kategorie : gruppen.keySet()) {
            add(zeile(kategorie, gruppen.get(kategorie).size() + " Artikel"));
        }
    }

    // Eine Kennzahl-Zeile: fetter Name links, Wert rechts.
    private HorizontalLayout zeile(String label, String wert) {
        Span name = new Span(label + ":");
        name.getStyle().set("font-weight", "bold");
        name.setWidth("260px");
        return new HorizontalLayout(name, new Span(wert));
    }

    private Span ueberschrift(String text) {
        Span s = new Span(text);
        s.getStyle().set("font-weight", "bold");
        s.getStyle().set("margin-top", "var(--vaadin-padding)");
        return s;
    }

    // Name + Preis eines Artikels; "-" wenn keiner vorhanden (leeres Lager).
    private String beschreibe(Artikel a) {
        if (a == null) {
            return "-";
        }
        return a.getName() + " (" + String.format("%.2f EUR", a.getPreis()) + ")";
    }
}
