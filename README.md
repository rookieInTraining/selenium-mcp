# Selenium MCP Server

A Spring Boot-based Model Context Protocol (MCP) server that provides Selenium WebDriver automation tools for browser automation through a standardized MCP interface.

## Overview

This project implements an MCP server that exposes Selenium WebDriver functionality as tools that can be consumed by MCP clients. It provides comprehensive browser automation capabilities including web driver management, element interactions, keyboard/mouse actions, and Chrome DevTools Protocol (CDP) features.

## Features

### Core WebDriver Operations
- **Browser Session Management**: Create and manage browser sessions for desktop and mobile devices
- **Navigation**: Navigate to URLs and fetch page sources
- **Session Cleanup**: Properly close browser sessions

### Web Element Interactions
- **Element Location**: Find elements using various locator strategies (ID, CSS, XPath, etc.)
- **Element Actions**: Click, send keys, and interact with web elements
- **Element Information**: Get element attributes and properties

### Advanced Actions
- **Pointer Actions**: Mouse movements, clicks, and drag operations
- **Keyboard Actions**: Text input and keyboard shortcuts
- **JavaScript Execution**: Execute JavaScript code in the browser context

### Chrome DevTools Protocol (CDP)
- **Browser Information**: Get browser version and capabilities
- **Network Monitoring**: Enable/disable network request monitoring
- **Cookie Management**: Fetch and manage browser cookies

## Prerequisites

- **Java 21** or higher
- **Gradle** (wrapper included)
- **WebDriver binaries** (automatically managed by WebDriverManager)

## Installation

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd selenium-mcp
   ```

2. **Build the project**:
   ```bash
   ./gradlew build
   ```

3. **Run the application**:
   ```bash
   ./gradlew bootRun
   ```

## Configuration

The application is configured via `application.properties`:

```properties
# MCP Server Configuration
spring.ai.mcp.server.name=selenium-mcp
spring.ai.mcp.server.version=1.0.0
spring.ai.mcp.server.type=SYNC
spring.ai.mcp.server.instructions=This server provides a set of selenium tools to automate browser actions

# Endpoints
spring.ai.mcp.server.sse-message-endpoint=/mcp/messages

# Capabilities
spring.ai.mcp.server.capabilities.tool=true
spring.ai.mcp.server.capabilities.resource=true
spring.ai.mcp.server.capabilities.prompt=true
spring.ai.mcp.server.capabilities.completion=true
```

## Usage

### Starting the Server

The MCP server runs on the default Spring Boot port (8080) and exposes the following endpoints:

- **MCP Messages**: `/mcp/messages` (Server-Sent Events)
- **Health Check**: `/actuator/health` (if actuator is enabled)

### Available Tools

#### Browser Management
- `browser_init(driverType, device)` - Create a new browser session
- `go_to(sessionId, url)` - Navigate to a URL
- `get_page_source(sessionId)` - Get current page source
- `quit_browser(sessionId)` - Close browser session

#### Element Operations
- `find_element(sessionId, locatorStrategy, webElement)` - Find a single element
- `find_elements(sessionId, locatorStrategy, webElement)` - Find multiple elements
- `left_click(sessionId, locatorStrategy, webElement)` - Click an element
- `send_keys_to_element(sessionId, locatorStrategy, webElement, textToSend)` - Send text to element
- `get_attribute(sessionId, locatorStrategy, webElement, attributeName)` - Get element attribute

#### Advanced Actions
- `pointer_actions(sessionId, locatorStrategy, webElement, actionType)` - Perform mouse actions
- `keyboard_actions(sessionId, locatorStrategy, webElement, actionType)` - Perform keyboard actions
- `execute_javascript(sessionId, script)` - Execute JavaScript code

#### CDP Features
- `get_browser_version(sessionId)` - Get browser version
- `enable_network_cdp(sessionId, maxTotalBufferSize, maxResourceBufferSize, maxPostDataSize, reportDirectSocketTraffic)` - Enable network monitoring
- `disable_network_cdp(sessionId)` - Disable network monitoring
- `get_all_cookies(sessionId)` - Get all cookies

## Project Structure

```
src/main/java/com/autom8tr/selenium_mcp/
├── core/
│   ├── SeleniumLocatorBuilder.java
│   ├── SeleniumMCPResponse.java
│   └── SeleniumSessionContainer.java
├── lenium/
│   ├── AbstractWebDriverFactory.java
│   ├── AppiumWebDriverFactory.java
│   ├── SeleniumWebDriverFactory.java
│   └── WebDrivers.java
├── tools/
│   ├── WebDriverService.java
│   ├── WebElementService.java
│   ├── PointerActionsService.java
│   ├── KeyboardActionsService.java
│   ├── JavaScriptService.java
│   └── cdp/
│       ├── BrowserService.java
│       └── NetworkService.java
└── SeleniumMcpApplication.java
```

## Dependencies

- **Spring Boot 3.5.4** - Application framework
- **Spring AI 1.0.1** - MCP server implementation
- **Selenium WebDriver 4.34.0** - Browser automation
- **WebDriverManager 6.2.0** - Automatic driver management

## Development

### Building
```bash
./gradlew build
```

### Testing
```bash
./gradlew test
```

### Running Tests
```bash
./gradlew bootRun
```

## Logging

Logs are written to `./build/starter-webflux-server.log` with the following pattern:
```
%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
```

For debugging, uncomment the debug lines in `application.properties`:
```properties
#logging.level.org.springframework=DEBUG
#logging.level.root=DEBUG
#debug=true
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

MIT License

Copyright (c) 2024 [Your Name or Organization]

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

## Support

For issues and questions, please create an issue in the repository.

