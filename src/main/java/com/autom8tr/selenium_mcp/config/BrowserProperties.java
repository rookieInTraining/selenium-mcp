package com.autom8tr.selenium_mcp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Configuration properties for browser/WebDriver settings.
 */
@Component
@ConfigurationProperties(prefix = "selenium.browser")
public class BrowserProperties {

    /**
     * Default browser type.
     */
    private String defaultBrowser = "chrome";

    /**
     * Run browsers in headless mode.
     */
    private boolean headless = true;

    /**
     * Default window width.
     */
    private int windowWidth = 1920;

    /**
     * Default window height.
     */
    private int windowHeight = 1080;

    /**
     * Implicit wait timeout.
     */
    private Duration implicitWait = Duration.ofSeconds(10);

    /**
     * Page load timeout.
     */
    private Duration pageLoadTimeout = Duration.ofSeconds(30);

    /**
     * Script execution timeout.
     */
    private Duration scriptTimeout = Duration.ofSeconds(30);

    /**
     * Enable Chrome DevTools Protocol features.
     */
    private boolean cdpEnabled = true;

    /**
     * Additional Chrome arguments.
     */
    private String[] chromeArgs = {};

    /**
     * Additional Firefox arguments.
     */
    private String[] firefoxArgs = {};

    /**
     * Additional Edge arguments.
     */
    private String[] edgeArgs = {};

    // Getters and setters

    public String getDefaultBrowser() {
        return defaultBrowser;
    }

    public void setDefaultBrowser(String defaultBrowser) {
        this.defaultBrowser = defaultBrowser;
    }

    public boolean isHeadless() {
        return headless;
    }

    public void setHeadless(boolean headless) {
        this.headless = headless;
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(int windowWidth) {
        this.windowWidth = windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public void setWindowHeight(int windowHeight) {
        this.windowHeight = windowHeight;
    }

    public Duration getImplicitWait() {
        return implicitWait;
    }

    public void setImplicitWait(Duration implicitWait) {
        this.implicitWait = implicitWait;
    }

    public Duration getPageLoadTimeout() {
        return pageLoadTimeout;
    }

    public void setPageLoadTimeout(Duration pageLoadTimeout) {
        this.pageLoadTimeout = pageLoadTimeout;
    }

    public Duration getScriptTimeout() {
        return scriptTimeout;
    }

    public void setScriptTimeout(Duration scriptTimeout) {
        this.scriptTimeout = scriptTimeout;
    }

    public boolean isCdpEnabled() {
        return cdpEnabled;
    }

    public void setCdpEnabled(boolean cdpEnabled) {
        this.cdpEnabled = cdpEnabled;
    }

    public String[] getChromeArgs() {
        return chromeArgs;
    }

    public void setChromeArgs(String[] chromeArgs) {
        this.chromeArgs = chromeArgs;
    }

    public String[] getFirefoxArgs() {
        return firefoxArgs;
    }

    public void setFirefoxArgs(String[] firefoxArgs) {
        this.firefoxArgs = firefoxArgs;
    }

    public String[] getEdgeArgs() {
        return edgeArgs;
    }

    public void setEdgeArgs(String[] edgeArgs) {
        this.edgeArgs = edgeArgs;
    }

    public String getWindowSize() {
        return windowWidth + "," + windowHeight;
    }
}

