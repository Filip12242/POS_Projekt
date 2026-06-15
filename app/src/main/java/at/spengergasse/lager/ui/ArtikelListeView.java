package at.spengergasse.lager.ui;

import at.spengergasse.lager.Artikel;
import at.spengergasse.lager.LagerService;
import at.spengergasse.lager.UngueltigeEingabeException;
import at.spengergasse.lager.base.ui.ViewTitle;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;

import java.util.List;

// Haupt-View: Tabelle aller Artikel mit Suche, Mehrfach-Filter, sortierbaren
// Spalten und CRUD (Neu / Bearbeiten / Loeschen) sowie Favoriten-Stern.
// Enthaelt KEINE Geschaeftslogik - alles geht ueber den LagerService.
@Route("")
@Menu(order = 1, icon = "vaadin:package", title = "Artikel")
public class ArtikelListeView extends VerticalLayout {

    private final LagerService service;

    private final Grid<Artikel> grid = new Grid<>(Artikel.class, false);
    private GridListDataView<Artikel> dataView;

    private final TextField suche = new TextField();
    private final ComboBox<String> katFilter = new ComboBox<>("Kategorie");
    private final TextField markeFilter = new TextField("Marke");
    private final NumberField minPreis = new NumberField("Preis von");
    private final NumberField maxPreis = new NumberField("Preis bis");

    public ArtikelListeView(LagerService service) {
        this.service = service;
        setSizeFull();

        add(new ViewTitle("Artikel"), erstelleWerkzeugleiste(), erstelleFilterleiste());
        erstelleTabelle();
        add(grid);

        aktualisiere();
    }

    private HorizontalLayout erstelleWerkzeugleiste() {
        Button neu = new Button("Neuer Artikel", e -> oeffneDialog(null));

        suche.setPlaceholder("Suche nach Name...");
        suche.setClearButtonVisible(true);
        suche.setValueChangeMode(ValueChangeMode.LAZY);
        suche.addValueChangeListener(e -> {
            if (dataView != null) {
                dataView.refreshAll();
            }
        });

        HorizontalLayout leiste = new HorizontalLayout(neu, suche);
        leiste.setAlignItems(FlexComponent.Alignment.END);
        return leiste;
    }

    private HorizontalLayout erstelleFilterleiste() {
        katFilter.setItems("Elektronik", "Lebensmittel", "Kleidung");
        katFilter.setClearButtonVisible(true);
        katFilter.setPlaceholder("alle");
        markeFilter.setClearButtonVisible(true);

        Button filtern = new Button("Filtern", e -> aktualisiere());
        Button zuruecksetzen = new Button("Zuruecksetzen", e -> {
            katFilter.clear();
            markeFilter.clear();
            minPreis.clear();
            maxPreis.clear();
            suche.clear();
            aktualisiere();
        });

        HorizontalLayout leiste = new HorizontalLayout(
                katFilter, markeFilter, minPreis, maxPreis, filtern, zuruecksetzen);
        leiste.setAlignItems(FlexComponent.Alignment.END);
        return leiste;
    }

    private void erstelleTabelle() {
        grid.addColumn(Artikel::getId).setHeader("ID").setSortable(true).setAutoWidth(true);
        grid.addColumn(Artikel::getName).setHeader("Name").setSortable(true).setAutoWidth(true);
        grid.addColumn(Artikel::getKategorie).setHeader("Kategorie").setSortable(true).setAutoWidth(true);
        grid.addColumn(Artikel::getMarke).setHeader("Marke").setSortable(true).setAutoWidth(true);
        grid.addColumn(a -> String.format("%.2f EUR", a.getPreis()))
                .setHeader("Preis").setAutoWidth(true)
                .setComparator((a, b) -> Double.compare(a.getPreis(), b.getPreis()));
        grid.addColumn(Artikel::getBestand).setHeader("Bestand").setSortable(true).setAutoWidth(true);

        // Favoriten-Stern (Klick schaltet um)
        grid.addComponentColumn(a -> {
            Button stern = new Button(a.istFavorit() ? "★" : "☆");
            stern.addClickListener(e -> {
                service.markiereFavorit(a.getId(), !a.istFavorit());
                aktualisiere();
            });
            return stern;
        }).setHeader("Fav").setAutoWidth(true);

        // Aktionen: Bearbeiten / Loeschen
        grid.addComponentColumn(a -> {
            Button bearbeiten = new Button("Bearbeiten", e -> oeffneDialog(a));
            Button loeschen = new Button("Loeschen", e -> {
                service.entfernen(a.getId());
                aktualisiere();
                Notification.show("Artikel geloescht");
            });
            return new HorizontalLayout(bearbeiten, loeschen);
        }).setHeader("Aktionen").setAutoWidth(true);

        grid.setSizeFull();
    }

    // Holt die gefilterte Liste vom Service und setzt sie in die Tabelle.
    // Die Namens-Suche wird zusaetzlich als Tabellen-Filter angewendet.
    private void aktualisiere() {
        try {
            double min = minPreis.getValue() == null ? 0 : minPreis.getValue();
            double max = maxPreis.getValue() == null ? Double.MAX_VALUE : maxPreis.getValue();
            List<Artikel> liste = service.filtern(
                    katFilter.getValue(), markeFilter.getValue(), min, max);
            dataView = grid.setItems(liste);
            dataView.addFilter(this::sucheTrifft);
        } catch (UngueltigeEingabeException ex) {
            Notification.show(ex.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

    // Tabellen-Filter fuer das Suchfeld (Name enthaelt den Suchtext).
    private boolean sucheTrifft(Artikel a) {
        String term = suche.getValue();
        if (term == null || term.isBlank()) {
            return true;
        }
        return a.getName().toLowerCase().contains(term.toLowerCase());
    }

    private void oeffneDialog(Artikel vorhanden) {
        ArtikelFormDialog dialog = new ArtikelFormDialog(vorhanden, a -> {
            if (vorhanden == null) {
                service.hinzufuegen(a);
            } else {
                service.aktualisieren(a);
            }
            aktualisiere();
            Notification.show(vorhanden == null ? "Artikel angelegt" : "Artikel geaendert");
        });
        dialog.open();
    }
}
