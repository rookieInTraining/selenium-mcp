package com.autom8tr.selenium_mcp.lenium;

import com.autom8tr.selenium_mcp.core.SeleniumMCPResponse;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeDriverService;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.remote.Augmenter;

import java.io.File;
import java.util.UUID;

import static com.autom8tr.selenium_mcp.core.SeleniumSessionContainer.sessions;

public class SeleniumWebDriverFactory implements WebDrivers{

    @Override
    public WebDriver getDriver(String browser) {
        WebDriver driver;

        switch (browser.toLowerCase()) {
            case "edge":
                WebDriverManager.edgedriver().setup();
                EdgeDriverService service = new EdgeDriverService.Builder()
                        .usingDriverExecutable(new File(System.getProperty("webdriver.edge.driver")))
                        .usingAnyFreePort()
                        .build();
                driver = new Augmenter().augment(new EdgeDriver(service));
                break;
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                GeckoDriverService gDService = new GeckoDriverService.Builder()
                        .usingDriverExecutable(new File(System.getProperty("webdriver.gecko.driver")))
                        .usingAnyFreePort()
                        .build();
                driver = new Augmenter().augment(new FirefoxDriver(gDService));
                break;
            default:
                throw new RuntimeException("The mentioned browser <-> web-driver configuration is not supported or found!");
        }

        return driver;
    }
}
