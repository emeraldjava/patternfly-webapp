package fr.pgervaise.patternfly.domain;

/**
 * 
 * @author Philippe Gervaise
 *
 */
public class Navigator {

    private String renderingEngine;
    private String browser;
    private String platform;
    private String engineVersion;
    private String cssGrade;

    public Navigator() {
    }

    /**
     * 
     * @param args
     */
    public Navigator(String[] args) {
        int index = 0;

        this.renderingEngine = args[index++];
        this.browser = args[index++];
        this.platform = args[index++];
        this.engineVersion = args[index++];
        this.cssGrade = args[index++];
    }

    public String getRenderingEngine() {
        return renderingEngine;
    }

    public void setRenderingEngine(String renderingEngine) {
        this.renderingEngine = renderingEngine;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getEngineVersion() {
        return engineVersion;
    }

    public void setEngineVersion(String engineVersion) {
        this.engineVersion = engineVersion;
    }

    public String getCssGrade() {
        return cssGrade;
    }

    public void setCssGrade(String cssGrade) {
        this.cssGrade = cssGrade;
    }
    
    public static void main(String[] args) {
		System.out.println("4".compareTo("5") < 0);
	}
}
