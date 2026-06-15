package at.spengergasse.lager.ui;

import at.spengergasse.lager.LagerService;
import at.spengergasse.lager.LogEintrag;
import at.spengergasse.lager.base.ui.ViewTitle;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;

// Verlauf-View: zeigt das Aenderungs-Log (Zeitpunkt + Aktion) aus getVerlauf().
// Neueste Eintraege zuerst.
@Route("verlauf")
@Menu(order = 3, icon = "vaadin:time-backward", title = "Verlauf")
public class VerlaufView extends VerticalLayout {

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public VerlaufView(LagerService service) {
        setSizeFull();
        add(new ViewTitle("Verlauf"));

        Grid<LogEintrag> grid = new Grid<>(LogEintrag.class, false);
        grid.addColumn(le -> FORMAT.format(le.getZeitpunkt()))
                .setHeader("Zeitpunkt").setAutoWidth(true);
        grid.addColumn(LogEintrag::getAktion).setHeader("Aktion").setAutoWidth(true);

        ArrayList<LogEintrag> liste = service.getVerlauf();
        Collections.reverse(liste);   // neueste zuerst
        grid.setItems(liste);
        grid.setSizeFull();
        add(grid);
    }
}
