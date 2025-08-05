package com.autom8tr.selenium_mcp;

import com.autom8tr.selenium_mcp.tools.*;
import com.autom8tr.selenium_mcp.tools.cdp.BrowserService;
import com.autom8tr.selenium_mcp.tools.cdp.NetworkService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SeleniumMcpApplication {

	public static void main(String[] args) {
		SpringApplication.run(SeleniumMcpApplication.class, args);
	}

	@Bean
	public ToolCallbackProvider seleniumTools(
			WebDriverService wDService,
			WebElementService wEService,
			PointerActionsService pointerActionsService,
			KeyboardActionsService keyService,
			BrowserService browserService,
			NetworkService networkService,
            JavaScriptService jsService
	) {
		return  MethodToolCallbackProvider.builder()
					.toolObjects(
							wDService,
							wEService,
							pointerActionsService,
							keyService,
							browserService,
							networkService,
                            jsService
					)
					.build();
	}

}
