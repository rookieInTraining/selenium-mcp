package com.autom8tr.selenium_mcp.sulphur;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.Augmenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating desktop browser WebDriver instances.
 * Supports Chrome, Firefox, and Edge browsers with configurable options.
 * Uses WebDriverManager for automatic driver binary management to resolve version mismatches.
 */
public class SeleniumWebDriverFactory implements WebDrivers {

    private static final Logger log = LoggerFactory.getLogger(SeleniumWebDriverFactory.class);

    // Default configuration - can be made configurable via @ConfigurationProperties
    private boolean headless = false;
    private int implicitWaitSeconds = 10;

    @Override
    public WebDriver getDriver(String browser) {
        if (browser == null || browser.isBlank()) {
            browser = "chrome"; // Default to Chrome
        }

        String browserType = browser.toLowerCase().trim();
        log.info("Creating WebDriver for browser: {} (headless: {})", browserType, headless);

        WebDriver driver = switch (browserType) {
            case "chrome" -> createChromeDriver();
            case "firefox" -> createFirefoxDriver();
            case "edge" -> createEdgeDriver();
            default -> throw new UnsupportedBrowserException(
                    "Unsupported browser: '" + browserType + "'. " +
                            "Supported browsers: chrome, firefox, edge");
        };

        // Configure implicit wait
        driver.manage().timeouts().implicitlyWait(java.time.Duration.ofSeconds(implicitWaitSeconds));

        // Augment driver for CDP support
        driver = new Augmenter().augment(driver);

        log.info("WebDriver created successfully for browser: {}", browserType);
        return driver;
    }

    /**
     * Creates a Chrome WebDriver with appropriate options.
     */
    private WebDriver createChromeDriver() {
        // Use WebDriverManager to automatically download and setup the correct ChromeDriver version
        WebDriverManager.chromedriver().setup();
        
        ChromeOptions options = new ChromeOptions();

        // Add common options for stability
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--remote-allow-origins=*");

        if (headless) {
            options.addArguments("--headless=new");
        }

        // Set window size for consistency
        options.addArguments("--window-size=1920,1080");

        return new ChromeDriver(options);
    }

    /**
     * Creates a Firefox WebDriver with appropriate options.
     */
    private WebDriver createFirefoxDriver() {
        // Use WebDriverManager to automatically download and setup the correct GeckoDriver version
        WebDriverManager.firefoxdriver().setup();
        
        FirefoxOptions options = new FirefoxOptions();

        if (headless) {
            options.addArguments("-headless");
        }

        // Set window size
        options.addArguments("--width=1920");
        options.addArguments("--height=1080");

        return new FirefoxDriver(options);
    }

    /**
     * Creates an Edge WebDriver with appropriate options.
     */
    private WebDriver createEdgeDriver() {
        // Use WebDriverManager to automatically download and setup the correct EdgeDriver version
        WebDriverManager.edgedriver().setup();
        
        EdgeOptions options = new EdgeOptions();

        // Add common options for stability
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        if (headless) {
            options.addArguments("--headless=new");
        }

        // Set window size
        options.addArguments("--window-size=1920,1080");

        return new EdgeDriver(options);
    }

    /**
     * Sets whether to run browsers in headless mode.
     */
    public void setHeadless(boolean headless) {
        this.headless = headless;
    }

    /**
     * Sets the implicit wait timeout in seconds.
     */
    public void setImplicitWaitSeconds(int seconds) {
        this.implicitWaitSeconds = seconds;
    }

    /**
     * Exception thrown when an unsupported browser is requested.
     */
    public static class UnsupportedBrowserException extends RuntimeException {
        public UnsupportedBrowserException(String message) {
            super(message);
        }
    }
}
