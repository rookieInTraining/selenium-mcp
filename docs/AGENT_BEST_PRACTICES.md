# Best Practices for AI Agents Using Selenium MCP

## Overview
This guide helps AI agents use the Selenium MCP server effectively and avoid common pitfalls like session loops.

---

## 🎯 Golden Rule: ONE Session Per Task

**Always check for existing sessions before creating a new one!**

### ✅ CORRECT Workflow
```
1. Call get_sessions() to check for active sessions
2. If sessions exist → Reuse the session ID
3. If no sessions → Call browser_init() to create one
4. Use the SAME session ID for all subsequent operations
5. Call quit_browser() when done
```

### ❌ INCORRECT Workflow
```
1. Call browser_init() immediately (creates duplicate sessions)
2. Encounter error → Call browser_init() again (creates more duplicates)
3. Result: Multiple zombie sessions, infinite loops
```

---

## 📋 Step-by-Step Agent Workflow

### Phase 1: Session Discovery
```
Tool: get_sessions()
Purpose: Check if any sessions already exist
Response Analysis:
  - "No active sessions" → Proceed to create new session
  - Shows session IDs → Extract and reuse the session ID
```

### Phase 2: Session Creation (Only if Needed)
```
Tool: browser_init(deviceType="desktop", browserType="chrome")
Purpose: Create a new browser session
IMPORTANT: Save the returned session ID for all future operations
Response: "Initialized browser session: <UUID> ... IMPORTANT: Use this session ID..."
```

### Phase 3: Navigation
```
Tool: go_to(sessionId=<UUID>, url="https://example.com")
Purpose: Navigate to URL and wait for page load
Note: This tool automatically waits for document.readyState === "complete"
```

### Phase 4: Element Interactions
```
All element tools automatically wait up to 10 seconds:
- find_element() → Waits for element to be present
- send_keys_to_element() → Waits for element to be clickable
- left_click() → Waits for element to be clickable
- get_text() → Waits for element to be present
```

### Phase 5: Cleanup
```
Tool: quit_browser(sessionId=<UUID>)
Purpose: Close the browser and free resources
IMPORTANT: Always close sessions when done
```

---

## 🚨 Common Mistakes and Solutions

### Mistake #1: Creating Multiple Sessions
**Problem**: Agent creates new session for every operation
```javascript
// WRONG
browser_init() → sessionId1
go_to(sessionId1, url)
browser_init() → sessionId2  // ❌ Creates duplicate!
send_keys(sessionId2, ...)
```

**Solution**: Reuse the same session ID
```javascript
// CORRECT
get_sessions() → Check first
browser_init() → sessionId1
go_to(sessionId1, url)
send_keys(sessionId1, ...)   // ✅ Same session
quit_browser(sessionId1)
```

### Mistake #2: Not Handling Errors Gracefully
**Problem**: Agent creates new session on every error
```javascript
// WRONG
send_keys(sessionId, "name", "username", "Admin")
// Error: Element not found
browser_init() → Creates new session ❌
```

**Solution**: Retry with same session or check session validity
```javascript
// CORRECT
send_keys(sessionId, "name", "username", "Admin")
// Error: Element not found within 10 seconds
check_session(sessionId) → Verify session is still active
// If active, maybe wrong locator or page not loaded
// If not active, then create new session
```

### Mistake #3: Not Waiting for Page Load
**Problem**: Agent tries to interact immediately after navigation
```javascript
// WRONG (but now fixed by server)
go_to(sessionId, url)
find_element(sessionId, "id", "username") // May fail if page not loaded
```

**Solution**: The server now handles this automatically
```javascript
// CORRECT (server waits automatically)
go_to(sessionId, url) // Waits for document ready
find_element(sessionId, "id", "username") // Waits up to 10 seconds
```

### Mistake #4: Forgetting to Close Sessions
**Problem**: Sessions accumulate and hit the limit
```javascript
// WRONG
browser_init() → sessionId1
// ... do work ...
// Forget to call quit_browser()
// Session stays open for 30 minutes (TTL)
```

**Solution**: Always close sessions
```javascript
// CORRECT
try {
    browser_init() → sessionId
    // ... do work ...
} finally {
    quit_browser(sessionId) // Always cleanup
}
```

---

## 🔍 Debugging Session Issues

### Check Active Sessions
```
Tool: get_sessions()
Look for:
  - idle time: Should be < 30 seconds if actively using
  - state: Should be "ACTIVE"
  - currentUrl: Should match expected page
```

### Verify Specific Session
```
Tool: check_session(sessionId=<UUID>)
Returns:
  - Session exists: Detailed info about the session
  - Session not found: Suggests creating new or finding active ones
```

### Monitor Session Health
```
Check these indicators:
  - Age: How long session has existed (seconds)
  - Idle: How long since last operation (seconds)
  - State: ACTIVE, IDLE, CLOSING, CLOSED, ERROR
```

---

## 📊 Example: Complete Login Flow

### Scenario
Login to OrangeHRM demo site, check for "Upgrade" option, logout.

### Implementation
```javascript
// Step 1: Check for existing sessions
response = get_sessions()
if (response contains "No active sessions") {
    // Step 2: Create new session
    response = browser_init("desktop", "chrome")
    sessionId = extract_uuid_from_response(response)
} else {
    // Reuse existing session
    sessionId = extract_first_session_id(response)
}

// Step 3: Navigate to login page
response = go_to(sessionId, "https://opensource-demo.orangehrmlive.com/web/index.php/auth/login")
// Server automatically waits for page load

// Step 4: Enter username (server waits for element)
response = send_keys_to_element(sessionId, "name", "username", "Admin")

// Step 5: Enter password (server waits for element)
response = send_keys_to_element(sessionId, "name", "password", "admin123")

// Step 6: Click login button (server waits for clickable)
response = left_click(sessionId, "css", "button[type='submit']")

// Step 7: Wait for dashboard and check for "Upgrade"
response = find_elements(sessionId, "xpath", "//*[contains(text(), 'Upgrade')]")
upgradeExists = response.success && !response.message.contains("No elements found")

// Step 8: Logout - click user dropdown
response = left_click(sessionId, "class", "oxd-userdropdown")

// Step 9: Click logout link
response = left_click(sessionId, "link", "Logout")

// Step 10: Close browser
response = quit_browser(sessionId)

// Result: Single session, no loops, clean execution
```

---

## ⚡ Performance Tips

### 1. Reuse Sessions Across Multiple Tasks
If you need to perform multiple related operations, keep the session open:
```javascript
sessionId = browser_init()

// Task 1
go_to(sessionId, "https://site1.com")
// ... do work ...

// Task 2 (same session!)
go_to(sessionId, "https://site2.com")
// ... do work ...

quit_browser(sessionId)
```

### 2. Use Specific Locators
More specific locators are faster and more reliable:
```javascript
// GOOD: Specific and fast
find_element(sessionId, "id", "username")
find_element(sessionId, "name", "password")

// SLOWER: XPath is more complex
find_element(sessionId, "xpath", "//input[@name='username']")

// AVOID: Too generic, may find wrong element
find_element(sessionId, "tag", "input")
```

### 3. Check Session Before Long Operations
For long-running tasks, verify session is still active:
```javascript
// Before starting long operation
check_session(sessionId)

// If session is valid, proceed
// If not, create new session
```

---

## 🛡️ Error Handling Patterns

### Pattern 1: Retry with Backoff
```javascript
function send_keys_with_retry(sessionId, strategy, value, text, maxRetries=3) {
    for (let i = 0; i < maxRetries; i++) {
        response = send_keys_to_element(sessionId, strategy, value, text)
        if (response.success) {
            return response
        }
        // Wait before retry (1s, 2s, 4s)
        wait(Math.pow(2, i) * 1000)
    }
    throw new Error("Failed after " + maxRetries + " retries")
}
```

### Pattern 2: Session Validation
```javascript
function ensure_valid_session(sessionId) {
    response = check_session(sessionId)
    if (!response.success) {
        // Session invalid, create new one
        response = browser_init()
        return extract_session_id(response)
    }
    return sessionId
}
```

### Pattern 3: Graceful Degradation
```javascript
// Try standard click first
response = left_click(sessionId, strategy, value)
if (!response.success && response.message.contains("intercepted")) {
    // Fall back to JavaScript click
    response = js_click(sessionId, strategy, value)
}
```

---

## 📈 Session Lifecycle

```
┌─────────────────────────────────────────────────────────────┐
│                     Session Lifecycle                        │
└─────────────────────────────────────────────────────────────┘

1. INITIALIZING
   ↓ browser_init()
   
2. ACTIVE
   ↓ Any tool operation (automatically touches session)
   │ lastAccessedAt updated
   │ Idle time reset to 0
   
3. IDLE (if no operations for > TTL)
   ↓ Cleanup scheduler detects
   
4. CLOSING
   ↓ quit_browser() or cleanup
   
5. CLOSED
   ↓ Resources freed

Note: Sessions in ACTIVE state are NEVER cleaned up by scheduler
```

---

## 🎓 Summary Checklist

Before starting any automation task, ensure:

- [ ] Called `get_sessions()` to check for existing sessions
- [ ] Extracted and stored session ID from `browser_init()` response
- [ ] Using the SAME session ID for all operations
- [ ] Not creating new sessions on errors
- [ ] Calling `quit_browser()` when done
- [ ] Handling errors gracefully (retry, validate session)
- [ ] Using specific locators (id, name, css) over generic ones (tag, xpath)
- [ ] Trusting the server's built-in waits (don't add manual delays)

**Follow these practices and your automation will run flawlessly!**

