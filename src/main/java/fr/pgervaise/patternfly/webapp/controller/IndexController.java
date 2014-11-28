package fr.pgervaise.patternfly.webapp.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.pgervaise.patternfly.datatable.core.DataTable;
import fr.pgervaise.patternfly.datatable.core.DataTableColumn;
import fr.pgervaise.patternfly.datatable.core.DataTableFilter;
import fr.pgervaise.patternfly.datatable.core.DataTableFilter.Operator;
import fr.pgervaise.patternfly.datatable.core.DataTableFilterOperator;
import fr.pgervaise.patternfly.datatable.core.DataTableFilterValue;
import fr.pgervaise.patternfly.datatable.datasource.DataTableDataSource;
import fr.pgervaise.patternfly.domain.Navigator;
import fr.pgervaise.patternfly.webapp.view.NavigatorView;

/**
 * 
 * @author Philippe Gervaise
 *
 */
@Controller
public class IndexController {

    @RequestMapping({"/", "/index"})
    public String viewHome(HttpServletRequest request, Model model) {
        
        String dataArray[][] = new String[][] {
            { "Trident", "Internet Explorer 4.0", "Win 95+", "4", "X" }, 
            { "Trident", "Internet Explorer 5.0", "Win 95+", "5", "C" }, 
            { "Trident", "Internet Explorer 5.5", "Win 95+", "5.5", "A" }, 
            { "Trident", "Internet Explorer 6", "Win 98+", "6", "A" }, 
            { "Trident", "Internet Explorer 7", "Win XP SP2+", "7", "A" }, 
            { "Trident", "AOL browser (AOL desktop)", "Win XP", "6", "A" }, 
            { "Gecko", "Firefox 1.0", "Win 98+ / OSX.2+", "1.7", "A" }, 
            { "Gecko", "Firefox 1.5", "Win 98+ / OSX.2+", "1.8", "A" }, 
            { "Gecko", "Firefox 2.0", "Win 98+ / OSX.2+", "1.8", "A" }, 
            { "Gecko", "Firefox 3.0", "Win 2k+ / OSX.3+", "1.9", "A" }, 
            { "Gecko", "Camino 1.0", "OSX.2+", "1.8", "A" }, 
            { "Gecko", "Camino 1.5", "OSX.3+", "1.8", "A" }, 
            { "Gecko", "Netscape 7.2", "Win 95+ / Mac OS 8.6-9.2", "1.7", "A" }, 
            { "Gecko", "Netscape Browser 8", "Win 98SE+", "1.7", "A" }, 
            { "Gecko", "Netscape Navigator 9", "Win 98+ / OSX.2+", "1.8", "A" }, 
            { "Gecko", "Mozilla 1.0", "Win 95+ / OSX.1+", "1", "A" }, 
            { "Gecko", "Mozilla 1.1", "Win 95+ / OSX.1+", "1.1", "A" }, 
            { "Gecko", "Mozilla 1.2", "Win 95+ / OSX.1+", "1.2", "A" }, 
            { "Gecko", "Mozilla 1.3", "Win 95+ / OSX.1+", "1.3", "A" }, 
            { "Gecko", "Mozilla 1.4", "Win 95+ / OSX.1+", "1.4", "A" }, 
            { "Gecko", "Mozilla 1.5", "Win 95+ / OSX.1+", "1.5", "A" }, 
            { "Gecko", "Mozilla 1.6", "Win 95+ / OSX.1+", "1.6", "A" }, 
            { "Gecko", "Mozilla 1.7", "Win 98+ / OSX.1+", "1.7", "A" }, 
            { "Gecko", "Mozilla 1.8", "Win 98+ / OSX.1+", "1.8", "A" }, 
            { "Gecko", "Seamonkey 1.1", "Win 98+ / OSX.2+", "1.8", "A" }, 
            { "Gecko", "Epiphany 2.20", "Gnome", "1.8", "A" }, 
            { "Webkit", "Safari 1.2", "OSX.3", "125.5", "A" }, 
            { "Webkit", "Safari 1.3", "OSX.3", "312.8", "A" }, 
            { "Webkit", "Safari 2.0", "OSX.4+", "419.3", "A" }, 
            { "Webkit", "Safari 3.0", "OSX.4+", "522.1", "A" }, 
            { "Webkit", "OmniWeb 5.5", "OSX.4+", "420", "A" }, 
            { "Webkit", "iPod Touch / iPhone", "iPod", "420.1", "A" }, 
            { "Webkit", "S60", "S60", "413", "A" }, 
            { "Presto", "Opera 7.0", "Win 95+ / OSX.1+", "-", "A" }, 
            { "Presto", "Opera 7.5", "Win 95+ / OSX.2+", "-", "A" }, 
            { "Presto", "Opera 8.0", "Win 95+ / OSX.2+", "-", "A" }, 
            { "Presto", "Opera 8.5", "Win 95+ / OSX.2+", "-", "A" }, 
            { "Presto", "Opera 9.0", "Win 95+ / OSX.3+", "-", "A" }, 
            { "Presto", "Opera 9.2", "Win 88+ / OSX.3+", "-", "A" }, 
            { "Presto", "Opera 9.5", "Win 88+ / OSX.3+", "-", "A" }, 
            { "Presto", "Opera for Wii", "Wii", "-", "A" }, 
            { "Presto", "Nokia N800", "N800", "-", "A" }, 
            { "Presto", "Nintendo DS browser", "Nintendo DS", "8.5", "C/A1" }, 
            { "KHTML", "Konqureror 3.1", "KDE 3.1", "3.1", "C" }, 
            { "KHTML", "Konqureror 3.3", "KDE 3.3", "3.3", "A" }, 
            { "KHTML", "Konqureror 3.5", "KDE 3.5", "3.5", "A" }, 
            { "Tasman", "Internet Explorer 4.5", "Mac OS 8-9", "-", "X" }, 
            { "Tasman", "Internet Explorer 5.1", "Mac OS 7.6-9", "1", "C" }, 
            { "Tasman", "Internet Explorer 5.2", "Mac OS 8-X", "1", "C" }, 
            { "Misc", "NetFront 3.1", "Embedded devices", "-", "C" }, 
            { "Misc", "NetFront 3.4", "Embedded devices", "-", "A" }, 
            { "Misc", "Dillo 0.8", "Embedded devices", "-", "X" }, 
            { "Misc", "Links", "Text only", "-", "X" }, 
            { "Misc", "Lynx", "Text only", "-", "X" }, 
            { "Misc", "IE Mobile", "Windows Mobile 6", "-", "C" }, 
            { "Misc", "PSP browser", "PSP", "-", "C" }, 
            { "Other browsers", "All others", "-", "-", "U" }
        };
        
        // TODO: Use Java 8 !
        List<Navigator> navigatorList = new ArrayList<Navigator>();

        for (String data[] : dataArray)
            navigatorList.add(new Navigator(data));

        DataTable<NavigatorView> navigatorDataTable = new DataTable<NavigatorView>(NavigatorView.class, "static");

        navigatorDataTable.addColumn(new DataTableColumn("renderingEngine", "Rendering Engine"));
        navigatorDataTable.addColumn(new DataTableColumn("browser", "Browser"));
        navigatorDataTable.addColumn(new DataTableColumn("platform", "Platform(s)"));
        navigatorDataTable.addColumn(new DataTableColumn("engineVersion", "Engine version"));
        navigatorDataTable.addColumn(new DataTableColumn("cssGrade", "CSS grade"));

        navigatorDataTable.addFilter(new DataTableFilter("Rendering Engine").setId("renderingEngine").setOperator(Operator.CONTAINS));
        navigatorDataTable.addFilter(new DataTableFilter("Browser").setId("browser").setOperator(Operator.CONTAINS));
        navigatorDataTable.addFilter(new DataTableFilter("Platform(s)").setId("platform").setOperator(Operator.CONTAINS));

        navigatorDataTable.addFilter(new DataTableFilter("Engine version").setId("engineVersion").setNewLine().setAcceptedOperators(
        	Arrays.asList(Operator.LOWER, Operator.LOWER_OR_EQUAL, Operator.GREATER, Operator.GREATER_OR_EQUAL)
        		.stream().map(DataTableFilterOperator::new).collect(Collectors.toList())
        ));

        navigatorDataTable.addFilter(new DataTableFilter("CSS Grade").setId("cssGrade").setAcceptedValues(
                Arrays.asList("A", "C", "X").stream().map(DataTableFilterValue::new).collect(Collectors.toList())
        ));

        navigatorDataTable.setDataSource(new DataTableDataSource<NavigatorView>() {
            @Override
            public List<? extends Object> getResults(DataTable<NavigatorView> dataTable) {
                // No dynamic filter
                return navigatorList;
            }
        });

        // Launch the "query"
        navigatorDataTable.setDoQueryOnFirstView(true);
        navigatorDataTable.init(request);

        model.addAttribute("navigatorDataTable", navigatorDataTable);
        
        // All data
        DataTable<NavigatorView> navigatorDataTableAll = new DataTable<NavigatorView>(NavigatorView.class, "all");
        for (DataTableColumn column : navigatorDataTable.getColumns())
            navigatorDataTableAll.addColumn(column);

        navigatorDataTableAll.setDataSource(navigatorDataTable.getDataSource());
        navigatorDataTableAll.setDoQueryOnFirstView(true);
        navigatorDataTableAll.setResultsPerPage(1000); // max 1000 results
        navigatorDataTableAll.init(request);
        
        model.addAttribute("navigatorDataTableAll", navigatorDataTableAll);

        return "index";
    }
}
