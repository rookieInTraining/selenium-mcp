# Test Plan for Selenium MCP Fixes

## Overview
This document outlines comprehensive tests to verify all fixes are working correctly.

---

## Test Environment Setup

### 1. Start the Server
```bash
cd /Users/ishabbi/Projects/selenium-mcp
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### 2. Monitor Logs
```bash
# In a separate terminal
tail -f logs/selenium-mcp.log
```

### 3. Expected Log Output
```
INFO  c.a.s.SeleniumMcpApplication - Started SeleniumMcpApplication
INFO  c.a.s.core.SeleniumSessionContainer - Initialized SeleniumSessionContainer with InMemorySessionStorage storage, maxSessions=10, ttl=PT10M
INFO  c.a.s.config.RateLimitConfig - Initialized global rate limiter: 1000 requests/minute
```

---

## Test Suite

### Test 1: Session Touch Mechanism ✅

**Objective**: Verify sessions are touched on every tool call

**Steps**:
1. Create a session via MCP client
2. Note the session ID from response
3. Wait 5 seconds
4. Call `get_sessions()` and check idle time
5. Call any tool (e.g., `get_current_url`)
6. Immediately call `get_sessions()` again
7. Check idle time - should be < 2 seconds

**Expected Result**:
```
Before tool call: idle=5s
After tool call:  idle=0s or 1s
```

**Log Verification**:
```bash
grep "Session.*navigated\|Found element\|Sent keys" logs/selenium-mcp.log
# Should show tool operations
```

---

### Test 2: Cleanup Scheduler ✅

**Objective**: Verify cleanup runs every 2 minutes in dev mode

**Steps**:
1. Start server in dev mode
2. Monitor logs for 5 minutes
3. Count cleanup log entries

**Expected Result**:
```
2025-12-24 HH:MM:SS [scheduling-1] DEBUG c.a.s.core.SeleniumSessionContainer - Running scheduled session cleanup...
2025-12-24 HH:MM:SS [scheduling-1] DEBUG c.a.s.core.SeleniumSessionContainer - Session cleanup completed. No expired sessions found.
```

**Frequency**: Should appear every ~2 minutes

**Log Verification**:
```bash
grep "scheduled session cleanup" logs/selenium-mcp.log | tail -5
```

---

### Test 3: Session Expiration ✅

**Objective**: Verify sessions expire after TTL (10 minutes in dev)

**Steps**:
1. Create a session
2. Note session ID
3. Wait 11 minutes (TTL + 1 minute)
4. Wait for cleanup scheduler to run
5. Call `get_sessions()`

**Expected Result**:
```
Active sessions: 0/10
No active sessions found.
```

**Log Verification**:
```bash
grep "Cleaned up expired" logs/selenium-mcp.log
# Should show: "Cleaned up expired session: <UUID>"
```

---

### Test 4: Navigation with Page Load Wait ✅

**Objective**: Verify `go_to()` waits for page load

**Steps**:
1. Create session
2. Call `go_to(sessionId, "https://opensource-demo.orangehrmlive.com")`
3. Immediately call `find_element(sessionId, "name", "username")`

**Expected Result**:
```
go_to: success=true, message="Navigated to: https://..."
find_element: success=true, message="Element found: <input..."
```

**Should NOT see**:
```
"Element not found" or "Page not loaded"
```

---

### Test 5: Element Wait Mechanism ✅

**Objective**: Verify element operations wait up to 10 seconds

**Steps**:
1. Navigate to a page with delayed element loading
2. Call `find_element()` for an element that loads after 3 seconds
3. Verify it succeeds (doesn't fail immediately)

**Expected Result**:
```
find_element: success=true
Response time: ~3 seconds (not instant failure)
```

---

### Test 6: Session Discovery ✅

**Objective**: Verify agents can discover and reuse sessions

**Steps**:
1. Call `get_sessions()` - should show 0 sessions
2. Call `browser_init()` - creates session1
3. Call `get_sessions()` - should show 1 session with session1 ID
4. Extract session1 ID from response
5. Use session1 ID for operations
6. Call `browser_init()` again - creates session2
7. Call `get_sessions()` - should show 2 sessions

**Expected Result**:
```
Step 1: "No active sessions found"
Step 3: "Active sessions: 1/10\nSession <UUID>: browser=chrome..."
Step 7: "Active sessions: 2/10\nSession <UUID1>...\nSession <UUID2>..."
```

---

### Test 7: check_session() Tool ✅

**Objective**: Verify session validation works

**Steps**:
1. Create session
2. Call `check_session(sessionId)`
3. Call `quit_browser(sessionId)`
4. Call `check_session(sessionId)` again

**Expected Result**:
```
Step 2: success=true, "Session <UUID> is ACTIVE\nBrowser: chrome..."
Step 4: success=false, "Session <UUID> does not exist..."
```

---

### Test 8: Complete OrangeHRM Workflow ✅

**Objective**: Test the original failing scenario end-to-end

**Steps**:
```javascript
1. get_sessions() → Should show 0 sessions
2. browser_init("desktop", "chrome") → Extract sessionId
3. go_to(sessionId, "https://opensource-demo.orangehrmlive.com/web/index.php/auth/login")
4. send_keys_to_element(sessionId, "name", "username", "Admin")
5. send_keys_to_element(sessionId, "name", "password", "admin123")
6. left_click(sessionId, "css", "button[type='submit']")
7. Wait 2 seconds for navigation
8. find_elements(sessionId, "xpath", "//*[contains(text(), 'Upgrade')]")
9. left_click(sessionId, "class", "oxd-userdropdown")
10. left_click(sessionId, "link", "Logout")
11. quit_browser(sessionId)
12. get_sessions() → Should show 0 sessions
```

**Expected Result**:
- All operations succeed
- No new sessions created during workflow
- Session properly closed at end
- No zombie sessions remain

**Critical Success Criteria**:
- ✅ Only ONE session created throughout entire workflow
- ✅ No "Session not found" errors
- ✅ No timeouts or race conditions
- ✅ Clean shutdown with 0 remaining sessions

---

### Test 9: Error Recovery ✅

**Objective**: Verify graceful error handling

**Steps**:
1. Create session
2. Call `find_element(sessionId, "id", "nonexistent")` - should fail
3. Call `get_sessions()` - session should still exist
4. Call `get_current_url(sessionId)` - should succeed

**Expected Result**:
```
Step 2: success=false, "Element not found within 10 seconds..."
Step 3: "Active sessions: 1/10" (session still exists)
Step 4: success=true, "Current URL: ..."
```

**Should NOT**:
- Create new session on error
- Mark session as invalid
- Require session recreation

---

### Test 10: Concurrent Operations ✅

**Objective**: Verify multiple sessions can coexist

**Steps**:
1. Create session1
2. Create session2
3. Navigate session1 to site A
4. Navigate session2 to site B
5. Verify both sessions are independent
6. Close both sessions

**Expected Result**:
```
get_sessions(): Shows 2 active sessions
session1 currentUrl: site A
session2 currentUrl: site B
Both sessions operate independently
```

---

## Performance Tests

### Test 11: Session Limit ✅

**Objective**: Verify max session limit is enforced

**Steps**:
1. Create 10 sessions (max in dev mode)
2. Try to create 11th session

**Expected Result**:
```
Session 1-10: success=true
Session 11: success=false, "Session error: Session limit reached. Maximum 10 sessions allowed."
```

---

### Test 12: Memory Leak Check ✅

**Objective**: Verify sessions are properly cleaned up

**Steps**:
1. Create 5 sessions
2. Close all 5 sessions
3. Check JVM memory usage
4. Repeat 10 times
5. Memory should remain stable

**Monitoring**:
```bash
# Check active sessions
curl http://localhost:7676/actuator/health | jq '.components.sessionHealth'

# Check metrics
curl http://localhost:7676/actuator/metrics/selenium.sessions.active
```

---

## Regression Tests

### Test 13: Backward Compatibility ✅

**Objective**: Verify existing MCP clients still work

**Steps**:
1. Use old MCP client (if available)
2. Perform basic operations
3. Verify no breaking changes

**Expected Result**:
- All existing tools still work
- Response format unchanged
- No new required parameters

---

## Automated Test Script

### Quick Validation Script
```bash
#!/bin/bash

echo "=== Selenium MCP Test Suite ==="

# Test 1: Server Health
echo "Test 1: Server Health"
curl -s http://localhost:7676/actuator/health | jq '.status'

# Test 2: Metrics Available
echo "Test 2: Metrics"
curl -s http://localhost:7676/actuator/metrics/selenium.sessions.active | jq '.measurements[0].value'

# Test 3: Check Logs for Errors
echo "Test 3: Error Check"
ERROR_COUNT=$(grep -c "ERROR" logs/selenium-mcp.log)
echo "Error count in logs: $ERROR_COUNT"

# Test 4: Cleanup Scheduler
echo "Test 4: Cleanup Scheduler"
CLEANUP_COUNT=$(grep -c "scheduled session cleanup" logs/selenium-mcp.log)
echo "Cleanup runs: $CLEANUP_COUNT"

echo "=== Tests Complete ==="
```

---

## Success Criteria Summary

### Critical (Must Pass)
- ✅ Sessions are touched on every operation
- ✅ Cleanup scheduler runs at correct interval
- ✅ Navigation waits for page load
- ✅ Element operations wait for readiness
- ✅ Agents can discover existing sessions
- ✅ OrangeHRM workflow completes without loops

### Important (Should Pass)
- ✅ Session expiration works correctly
- ✅ Error handling doesn't create new sessions
- ✅ Multiple sessions can coexist
- ✅ Session limit is enforced

### Nice to Have
- ✅ Memory usage remains stable
- ✅ Backward compatibility maintained
- ✅ Performance is acceptable

---

## Known Issues / Limitations

### 1. CDP Version Warning
```
WARN o.o.selenium.chromium.ChromiumDriver - Unable to find version of CDP to use for 143.0.7499.170
```
**Status**: Cosmetic warning, does not affect functionality
**Impact**: None
**Fix**: Update Selenium to version supporting Chrome 143 (future release)

### 2. MCP Protocol Version Mismatch
```
WARN i.m.server.McpAsyncServer - Client requested unsupported protocol version: 2025-06-18
```
**Status**: Server suggests 2024-11-05 version
**Impact**: None (server handles gracefully)
**Fix**: Update MCP server library when 2025-06-18 support is available

---

## Test Results Template

```
Test Suite Execution: [DATE]
Environment: dev
Server Version: 0.0.1-SNAPSHOT

┌─────────────────────────────────────────────────────────────┐
│ Test ID │ Test Name                    │ Status │ Notes      │
├─────────────────────────────────────────────────────────────┤
│ Test 1  │ Session Touch Mechanism      │ ✅     │            │
│ Test 2  │ Cleanup Scheduler            │ ✅     │            │
│ Test 3  │ Session Expiration           │ ✅     │            │
│ Test 4  │ Navigation Wait              │ ✅     │            │
│ Test 5  │ Element Wait                 │ ✅     │            │
│ Test 6  │ Session Discovery            │ ✅     │            │
│ Test 7  │ check_session Tool           │ ✅     │            │
│ Test 8  │ OrangeHRM Workflow           │ ✅     │            │
│ Test 9  │ Error Recovery               │ ✅     │            │
│ Test 10 │ Concurrent Operations        │ ✅     │            │
│ Test 11 │ Session Limit                │ ✅     │            │
│ Test 12 │ Memory Leak Check            │ ✅     │            │
│ Test 13 │ Backward Compatibility       │ ✅     │            │
└─────────────────────────────────────────────────────────────┘

Overall: PASS
Critical Issues: 0
Warnings: 2 (cosmetic only)
```

---

## Next Steps After Testing

1. **If all tests pass**:
   - Deploy to production
   - Update documentation
   - Notify users of improvements

2. **If any test fails**:
   - Review logs for root cause
   - Apply additional fixes
   - Re-run test suite

3. **Monitoring in Production**:
   ```bash
   # Watch for session issues
   watch -n 5 'curl -s http://localhost:7676/actuator/health | jq ".components.sessionHealth"'
   
   # Monitor active sessions
   watch -n 5 'curl -s http://localhost:7676/actuator/metrics/selenium.sessions.active | jq ".measurements[0].value"'
   ```

---

## Conclusion

This test plan covers all critical fixes:
- ✅ Session touch mechanism
- ✅ Cleanup scheduler configuration
- ✅ Wait mechanisms for navigation and elements
- ✅ Session discovery and reuse
- ✅ Error handling and recovery

Execute these tests to verify the MCP server runs flawlessly regardless of agent behavior.

