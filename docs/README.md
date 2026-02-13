# Selenium MCP Server

A production-ready, horizontally scalable Spring Boot-based Model Context Protocol (MCP) server that provides Selenium WebDriver automation tools for browser automation through a standardized MCP interface.

## Overview

This project implements an MCP server that exposes Selenium WebDriver functionality as tools that can be consumed by MCP clients. It provides comprehensive browser automation capabilities including web driver management, element interactions, keyboard/mouse actions, and Chrome DevTools Protocol (CDP) features.

## Key Features

### Scalability & Production Readiness
- **Distributed Session Management**: Redis-backed session storage for horizontal scaling
- **Hybrid Architecture**: Local Caffeine cache + Redis for optimal performance
- **Session Affinity**: Pod-level session ownership tracking for Kubernetes
- **Rate Limiting**: Per-session and global rate limits using Bucket4j
- **Graceful Shutdown**: Proper session cleanup during pod termination

### Core WebDriver Operations
- **Browser Session Management**: Create and manage browser sessions for Chrome, Firefox, Edge
- **Navigation**: Navigate to URLs, get page source, current URL, and title
- **Session Cleanup**: Automatic TTL-based session expiration and cleanup

### Web Element Interactions
- **Element Location**: Find elements using ID, CSS, XPath, name, tag, link text, class
- **Element Actions**: Click, send keys, get text, get attributes
- **Element Information**: Check visibility, get properties

### Advanced Actions
- **Pointer Actions**: Click, double-click, right-click, hover, drag & drop, scroll
- **Keyboard Actions**: Send keys, keyboard shortcuts, special keys (Enter, Tab, etc.)
- **JavaScript Execution**: Sync/async JavaScript execution, page scrolling

### Chrome DevTools Protocol (CDP)
- **Browser Information**: Get browser version and capabilities
- **Network Monitoring**: Enable/disable network request monitoring
- **Cookie Management**: Get, add, delete cookies, clear browser data

### Observability
- **Prometheus Metrics**: Active sessions, creation rate, tool execution times, error rates
- **Health Checks**: Session storage, Redis connectivity, capacity monitoring
- **Distributed Tracing**: Zipkin integration for request tracing
- **Structured Logging**: JSON logs for Kubernetes log aggregation

## Prerequisites

- **Java 21** or higher
- **Gradle** (wrapper included)
- **Redis** (for distributed mode)
- **Chrome/Firefox/Edge browser** (with drivers managed by Selenium Manager)

## Quick Start

### Local Development

1. **Clone and build**:
   ```bash
   git clone <repository-url>
   cd selenium-mcp
   ./gradlew build
   ```

2. **Run in development mode**:
   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=dev'
   ```

3. **Access the server**:
   - MCP Endpoint: `http://localhost:7676/mcp/messages`
   - Health: `http://localhost:7676/actuator/health`
   - Metrics: `http://localhost:7676/actuator/prometheus`

### Production Mode

```bash
./gradlew bootRun --args='--spring.profiles.active=prod'
```

### Kubernetes Deployment

1. **Build Docker image**:
   ```bash
   docker build -t selenium-mcp:latest .
   ```

2. **Deploy to Kubernetes**:
   ```bash
   kubectl apply -k k8s/
   ```

## Configuration

### Application Properties

```properties
# Session Management
selenium.session.max-sessions=100
selenium.session.ttl=30m
selenium.session.cleanup-interval=5m
selenium.session.distributed-enabled=false

# Browser Configuration
selenium.browser.default-browser=chrome
selenium.browser.headless=true
selenium.browser.window-width=1920
selenium.browser.window-height=1080

# Rate Limiting
selenium.rate-limit.enabled=true
selenium.rate-limit.global-requests-per-minute=1000
selenium.rate-limit.session-requests-per-minute=60
```

### Environment Variables (Kubernetes)

| Variable | Description | Default |
|----------|-------------|---------|
| `REDIS_HOST` | Redis hostname | `redis` |
| `REDIS_PORT` | Redis port | `6379` |
| `REDIS_PASSWORD` | Redis password | `` |
| `HOSTNAME` | Pod name (auto-set) | - |
| `POD_IP` | Pod IP address | - |

### Spring Profiles

- **dev**: Non-headless browsers, verbose logging, no rate limits
- **prod**: Headless browsers, production logging, strict rate limits
- **k8s**: Distributed mode, Redis sessions, JSON logging, health probes

## Available Tools

### Browser Management
| Tool | Description |
|------|-------------|
| `browser_init(deviceType, browserType)` | Create a new browser session |
| `get_sessions()` | List all active sessions with metadata |
| `go_to(sessionId, url)` | Navigate to a URL |
| `get_page_source(sessionId)` | Get current page HTML |
| `get_current_url(sessionId)` | Get current URL |
| `get_page_title(sessionId)` | Get page title |
| `quit_browser(sessionId)` | Close browser session |

### Element Operations
| Tool | Description |
|------|-------------|
| `find_element(sessionId, locatorStrategy, locatorValue)` | Find a single element |
| `find_elements(sessionId, locatorStrategy, locatorValue)` | Find multiple elements |
| `get_attribute(sessionId, locatorStrategy, locatorValue, attributeName)` | Get element attribute |
| `get_text(sessionId, locatorStrategy, locatorValue)` | Get element text |
| `is_element_displayed(sessionId, locatorStrategy, locatorValue)` | Check element visibility |

### Pointer Actions
| Tool | Description |
|------|-------------|
| `left_click(sessionId, locatorStrategy, locatorValue)` | Click an element |
| `double_click(sessionId, locatorStrategy, locatorValue)` | Double-click an element |
| `right_click(sessionId, locatorStrategy, locatorValue)` | Right-click an element |
| `hover(sessionId, locatorStrategy, locatorValue)` | Hover over element |
| `drag_and_drop(sessionId, source..., target...)` | Drag and drop |
| `scroll_to_element(sessionId, locatorStrategy, locatorValue)` | Scroll to element |

### Keyboard Actions
| Tool | Description |
|------|-------------|
| `send_keys_to_element(sessionId, locatorStrategy, locatorValue, text)` | Type text |
| `clear_and_type(sessionId, locatorStrategy, locatorValue, text)` | Clear and type |
| `press_key(sessionId, keyName, ...)` | Press special key |
| `keyboard_shortcut(sessionId, modifier, key)` | Keyboard shortcut |
| `submit_form(sessionId, locatorStrategy, locatorValue)` | Submit form |

### JavaScript
| Tool | Description |
|------|-------------|
| `js_execute_sync(sessionId, script)` | Execute sync JavaScript |
| `js_execute_async(sessionId, script)` | Execute async JavaScript |
| `get_page_html(sessionId)` | Get full page HTML via JS |
| `scroll_page(sessionId, x, y)` | Scroll by pixels |
| `scroll_to_top(sessionId)` | Scroll to top |
| `scroll_to_bottom(sessionId)` | Scroll to bottom |
| `js_click(sessionId, locatorStrategy, locatorValue)` | JS click |
| `js_set_value(sessionId, locatorStrategy, locatorValue, value)` | Set value via JS |

### CDP Features
| Tool | Description |
|------|-------------|
| `get_browser_version(sessionId)` | Get browser version via CDP |
| `enable_network_cdp(sessionId, ...)` | Enable network monitoring |
| `disable_network_cdp(sessionId)` | Disable network monitoring |
| `get_all_cookies(sessionId)` | Get all cookies |
| `add_cookie(sessionId, name, value, ...)` | Add a cookie |
| `delete_cookie(sessionId, cookieName)` | Delete a cookie |
| `delete_all_cookies(sessionId)` | Delete all cookies |
| `clear_browser_data(sessionId)` | Clear cookies and storage |

### Browser Window
| Tool | Description |
|------|-------------|
| `get_window_bounds(sessionId)` | Get window position/size |
| `set_window_size(sessionId, width, height)` | Set window size |
| `maximize_window(sessionId)` | Maximize window |
| `take_screenshot(sessionId)` | Take screenshot (base64) |

## Project Structure

```
src/main/java/com/autom8tr/selenium_mcp/
├── config/                 # Configuration classes
│   ├── SessionProperties.java
│   ├── BrowserProperties.java
│   ├── RedisConfig.java
│   ├── RateLimitConfig.java
│   ├── AsyncConfig.java
│   └── GracefulShutdownConfig.java
├── core/                   # Core components
│   ├── SeleniumSessionContainer.java
│   ├── SeleniumLocatorBuilder.java
│   ├── SeleniumMCPResponse.java
│   ├── SessionMetadata.java
│   ├── ValidationService.java
│   └── session/            # Session storage
│       ├── SessionStorage.java
│       ├── InMemorySessionStorage.java
│       ├── HybridSessionManager.java
│       ├── RedisSessionRegistry.java
│       └── SessionRegistryEntry.java
├── exception/              # Custom exceptions
│   ├── SeleniumMcpException.java
│   ├── SessionException.java
│   ├── BrowserException.java
│   ├── ElementException.java
│   ├── CDPException.java
│   └── ValidationException.java
├── health/                 # Health indicators
│   ├── SessionHealthIndicator.java
│   └── RedisHealthIndicator.java
├── metrics/                # Metrics
│   └── SeleniumMetrics.java
├── sulphur/                # WebDriver factories
│   ├── WebDrivers.java
│   ├── AbstractWebDriverFactory.java
│   ├── SeleniumWebDriverFactory.java
│   └── AppiumWebDriverFactory.java
├── tools/                  # MCP Tools
│   ├── WebDriverService.java
│   ├── WebElementService.java
│   ├── PointerActionsService.java
│   ├── KeyboardActionsService.java
│   ├── JavaScriptService.java
│   └── cdp/
│       ├── BrowserService.java
│       └── NetworkService.java
└── SeleniumMcpApplication.java

k8s/                        # Kubernetes manifests
├── namespace.yaml
├── configmap.yaml
├── secret.yaml
├── deployment.yaml
├── service.yaml
├── hpa.yaml
├── pdb.yaml
├── serviceaccount.yaml
├── redis.yaml
└── kustomization.yaml
```

## Architecture

### Single Instance Mode
```
Client → MCP Server → SeleniumSessionContainer → InMemorySessionStorage → WebDriver
```

### Distributed Mode (Kubernetes)
```
Client → Load Balancer → Pod N → HybridSessionManager
                              ↓
                    ┌─────────┴─────────┐
                    ↓                   ↓
              Local Caffeine         Redis
              (WebDriver)         (Metadata)
```

## Dependencies

- **Spring Boot 3.5.4** - Application framework
- **Spring AI 1.0.1** - MCP server implementation
- **Selenium WebDriver 4.34.0** - Browser automation
- **Redis (Lettuce)** - Distributed session storage
- **Caffeine 3.1.8** - Local caching
- **Bucket4j 8.10.1** - Rate limiting
- **Micrometer** - Metrics and tracing
- **Gson 2.11.0** - JSON serialization

## Development

### Building
```bash
./gradlew build
```

### Running Tests
```bash
./gradlew test
```

### Running with Debug Logging
```bash
./gradlew bootRun --args='--spring.profiles.active=dev --logging.level.com.autom8tr.selenium_mcp=DEBUG'
```

## Monitoring

### Prometheus Metrics
- `selenium_sessions_active` - Current active sessions
- `selenium_sessions_created_total` - Total sessions created
- `selenium_sessions_closed_total` - Total sessions closed
- `selenium_tools_executions_total` - Tool execution count
- `selenium_tools_errors_total` - Tool error count
- `selenium_session_creation_duration_seconds` - Session creation time
- `selenium_tool_execution_duration_seconds` - Tool execution time

### Health Endpoints
- `/actuator/health` - Overall health
- `/actuator/health/liveness` - Kubernetes liveness probe
- `/actuator/health/readiness` - Kubernetes readiness probe

## License

MIT License

Copyright (c) 2024

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
