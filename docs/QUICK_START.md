# Quick Start Guide - Selenium MCP Server

## 🚀 Get Started in 60 Seconds

### 1. Start the Server
```bash
cd /Users/ishabbi/Projects/selenium-mcp
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### 2. Verify It's Running
```bash
curl http://localhost:7676/actuator/health
# Should return: {"status":"UP"}
```

### 3. Test with Your Agent
```javascript
// Step 1: Check for existing sessions
get_sessions()

// Step 2: Create a session (if none exist)
browser_init("desktop", "chrome")
// Save the returned session ID!

// Step 3: Navigate
go_to(sessionId, "https://example.com")

// Step 4: Interact with elements
find_element(sessionId, "id", "username")
send_keys_to_element(sessionId, "id", "username", "myuser")

// Step 5: Clean up
quit_browser(sessionId)
```

---

## 📖 Documentation Index

### For Everyone
- **[SUMMARY.md](SUMMARY.md)** - Executive overview of fixes and changes
- **[QUICK_START.md](QUICK_START.md)** - This file

### For Developers
- **[README.md](README.md)** - Full project documentation
- **[FIXES_APPLIED.md](FIXES_APPLIED.md)** - Technical details of all fixes
- **[CHANGELOG.md](CHANGELOG.md)** - Version history

### For AI Agents
- **[AGENT_BEST_PRACTICES.md](AGENT_BEST_PRACTICES.md)** - Complete guide for agents

### For QA/Testing
- **[TEST_PLAN.md](TEST_PLAN.md)** - Comprehensive test suite

---

## 🎯 Key Improvements (v0.0.2)

### What Was Fixed
1. ✅ **Sessions now stay alive** while being used
2. ✅ **Cleanup scheduler works** (runs every 2-5 minutes)
3. ✅ **Navigation waits** for page load automatically
4. ✅ **Element operations wait** up to 10 seconds
5. ✅ **Agents can discover** and reuse existing sessions

### What This Means
- ✅ **No more infinite loops**
- ✅ **No more zombie sessions**
- ✅ **No more race conditions**
- ✅ **Tasks complete successfully**

---

## 🔧 Common Commands

### Development
```bash
# Build
./gradlew clean build

# Run in dev mode (10min TTL, 2min cleanup)
./gradlew bootRun --args='--spring.profiles.active=dev'

# Run in prod mode (30min TTL, 5min cleanup)
./gradlew bootRun --args='--spring.profiles.active=prod'

# Run tests
./gradlew test
```

### Monitoring
```bash
# Check health
curl http://localhost:7676/actuator/health | jq

# Check active sessions
curl http://localhost:7676/actuator/metrics/selenium.sessions.active | jq

# Watch logs
tail -f logs/selenium-mcp.log

# Watch cleanup scheduler
tail -f logs/selenium-mcp.log | grep cleanup
```

### Debugging
```bash
# Enable debug logging
# Edit application-dev.properties:
logging.level.com.autom8tr.selenium_mcp=DEBUG

# Check session details
curl http://localhost:7676/actuator/health | jq '.components.sessionHealth'

# Check metrics
curl http://localhost:7676/actuator/prometheus | grep selenium
```

---

## 🎓 Golden Rules for Agents

### ✅ DO
1. **Call `get_sessions()` first** to check for existing sessions
2. **Reuse the same session ID** for all operations
3. **Save the session ID** from `browser_init()` response
4. **Always call `quit_browser()`** when done
5. **Trust the server's waits** - don't add manual delays

### ❌ DON'T
1. **Don't create new sessions** on every operation
2. **Don't create new sessions** on errors
3. **Don't add manual waits** - server handles them
4. **Don't forget to close sessions** when done
5. **Don't use generic locators** when specific ones exist

---

## 🆘 Troubleshooting

### Issue: "Session not found"
**Cause**: Session expired or was closed  
**Solution**: Call `get_sessions()` to check, or create new session

### Issue: "Element not found within 10 seconds"
**Cause**: Wrong locator or page not fully loaded  
**Solution**: 
- Verify locator is correct
- Check if page navigation completed
- Try more specific locator (id > name > css > xpath)

### Issue: "Session limit reached"
**Cause**: Too many sessions open (max 10 in dev, 100 in prod)  
**Solution**: 
- Close unused sessions with `quit_browser()`
- Check for zombie sessions with `get_sessions()`
- Wait for cleanup scheduler to run

### Issue: "Click intercepted"
**Cause**: Another element is covering the target  
**Solution**: 
- Use `scroll_to_element()` first
- Or use `js_click()` as alternative

---

## 📊 Configuration Reference

### Development Mode
```properties
selenium.session.max-sessions=10
selenium.session.ttl=10m
selenium.session.cleanup-interval=2m
selenium.browser.headless=false
selenium.rate-limit.enabled=false
```

### Production Mode
```properties
selenium.session.max-sessions=100
selenium.session.ttl=30m
selenium.session.cleanup-interval=5m
selenium.browser.headless=true
selenium.rate-limit.enabled=true
```

---

## 🔗 Available Tools (44 Total)

### Session Management
- `browser_init()` - Create new session
- `get_sessions()` - List all sessions
- `check_session()` - Validate specific session
- `quit_browser()` - Close session

### Navigation
- `go_to()` - Navigate to URL (waits for load)
- `get_current_url()` - Get current URL
- `get_page_title()` - Get page title
- `get_page_source()` - Get HTML source

### Element Finding
- `find_element()` - Find single element (waits 10s)
- `find_elements()` - Find multiple elements
- `is_element_displayed()` - Check visibility

### Element Interaction
- `get_text()` - Get element text
- `get_attribute()` - Get element attribute
- `send_keys_to_element()` - Type text (waits 10s)
- `clear_and_type()` - Clear and type

### Mouse Actions
- `left_click()` - Click element (waits 10s)
- `double_click()` - Double-click
- `right_click()` - Right-click (context menu)
- `hover()` - Hover over element
- `drag_and_drop()` - Drag and drop
- `scroll_to_element()` - Scroll to element

### Keyboard Actions
- `press_key()` - Press special key (Enter, Tab, etc.)
- `keyboard_shortcut()` - Perform shortcut (Ctrl+C, etc.)
- `submit_form()` - Submit form

### JavaScript
- `js_execute_sync()` - Execute JavaScript
- `js_execute_async()` - Execute async JavaScript
- `js_click()` - Click via JavaScript
- `js_set_value()` - Set value via JavaScript

### Window Management
- `maximize_window()` - Maximize browser
- `set_window_size()` - Set window size
- `get_window_bounds()` - Get window position/size

### Screenshots & Scrolling
- `take_screenshot()` - Capture screenshot (base64)
- `scroll_page()` - Scroll by pixels
- `scroll_to_top()` - Scroll to top
- `scroll_to_bottom()` - Scroll to bottom

### Cookies & CDP
- `get_all_cookies()` - Get all cookies
- `add_cookie()` - Add cookie
- `delete_cookie()` - Delete specific cookie
- `delete_all_cookies()` - Delete all cookies
- `clear_browser_data()` - Clear cache and cookies
- `get_browser_version()` - Get browser version
- `enable_network_cdp()` - Enable network monitoring
- `disable_network_cdp()` - Disable network monitoring

**Full documentation**: See [README.md](README.md)

---

## 📞 Need Help?

1. **Read the docs**:
   - [AGENT_BEST_PRACTICES.md](AGENT_BEST_PRACTICES.md) for usage patterns
   - [FIXES_APPLIED.md](FIXES_APPLIED.md) for technical details
   - [TEST_PLAN.md](TEST_PLAN.md) for testing

2. **Check logs**:
   ```bash
   tail -100 logs/selenium-mcp.log
   ```

3. **Check health**:
   ```bash
   curl http://localhost:7676/actuator/health | jq
   ```

4. **Enable debug mode**:
   ```properties
   logging.level.com.autom8tr.selenium_mcp=DEBUG
   ```

---

## ✅ Verification Checklist

Before reporting issues, verify:

- [ ] Server is running (`curl http://localhost:7676/actuator/health`)
- [ ] Using correct session ID (from `browser_init()` response)
- [ ] Not creating multiple sessions (check with `get_sessions()`)
- [ ] Waiting for operations to complete (server waits automatically)
- [ ] Closing sessions when done (`quit_browser()`)
- [ ] Using specific locators (id/name preferred over xpath)

---

## 🎉 Success!

Your Selenium MCP server is now ready to run flawlessly!

**Key Points**:
- ✅ All critical bugs fixed
- ✅ Build successful
- ✅ Comprehensive documentation
- ✅ Ready for production

**Happy Automating! 🚀**

