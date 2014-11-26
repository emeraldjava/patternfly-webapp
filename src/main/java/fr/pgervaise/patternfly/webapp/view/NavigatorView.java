package fr.pgervaise.patternfly.webapp.view;

import fr.pgervaise.patternfly.datatable.view.AbstractView;
import fr.pgervaise.patternfly.domain.Navigator;

/**
 * 
 * @author pgervaise
 *
 */
public class NavigatorView extends AbstractView<Navigator> {

    /**
     * 
     * @param navigator
     */
    public NavigatorView(Navigator navigator) {
        super(navigator);
    }

    public String getRenderingEngine() {
        return getBean().getRenderingEngine();
    }

    public String getBrowser() {
        return getBean().getBrowser();
    }

    public String getPlatform() {
        return getBean().getPlatform();
    }

    public String getEngineVersion() {
        return getBean().getEngineVersion();
    }

    public String getCssGrade() {
        return getBean().getCssGrade();
    }

}
