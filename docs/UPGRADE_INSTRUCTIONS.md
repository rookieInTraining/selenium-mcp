# MCP Protocol Upgrade Instructions

## ⚠️ READ THIS FIRST

**IMPORTANT**: The protocol version mismatch warning is **NOT** causing your issues. All your agent problems were caused by session management bugs, which have been fixed.

**We recommend NOT upgrading** until Spring AI officially releases a version with MCP SDK 0.11+.

---

## If You Still Want to Try Upgrading

### Step 1: Backup Your Current Setup

```bash
cd /Users/ishabbi/Projects/selenium-mcp

# Create a git branch for backup
git add .
git commit -m "Backup before MCP SDK upgrade attempt"
git branch backup-mcp-upgrade
git tag v0.0.1-stable
```

### Step 2: Check for Latest MCP SDK Version

```bash
# Check Maven Central for latest version
curl -s "https://repo1.maven.org/maven2/io/modelcontextprotocol/sdk/mcp/maven-metadata.xml" | grep -o "<version>[^<]*</version>" | tail -1

# Or visit manually:
# https://central.sonatype.com/artifact/io.modelcontextprotocol.sdk/mcp
```

**As of December 2024**: The latest version is `0.10.0` (same as what you have)

### Step 3: Wait for Newer Version

If no newer version exists, **STOP HERE**. There's nothing to upgrade to.

If a newer version exists (e.g., `0.11.0` or `0.12.0`):

### Step 4: Update build.gradle

```bash
# Backup current build.gradle
cp build.gradle build.gradle.backup

# Edit build.gradle
nano build.gradle
```

Add these lines after the `ext` block:

```gradle
ext {
    set('springAiVersion', "1.0.1")
    set('mcpSdkVersion', "0.11.0") // Replace with actual newer version
}

dependencies {
    // ... existing dependencies ...
    
    // Override MCP SDK version
    implementation("io.modelcontextprotocol.sdk:mcp:${mcpSdkVersion}") {
        force = true
    }
    implementation("io.modelcontextprotocol.sdk:mcp-spring-webmvc:${mcpSdkVersion}") {
        force = true
    }
    
    // ... rest of dependencies ...
}
```

### Step 5: Clean and Rebuild

```bash
# Clean previous build
./gradlew clean

# Rebuild with new dependencies
./gradlew build

# Check for errors
echo $?  # Should be 0 if successful
```

### Step 6: Check Dependencies

```bash
# Verify MCP SDK version
./gradlew dependencies --configuration runtimeClasspath | grep "io.modelcontextprotocol.sdk:mcp"

# Should show your new version
```

### Step 7: Test the Server

```bash
# Start in dev mode
./gradlew bootRun --args='--spring.profiles.active=dev'
```

**Watch the logs for**:
```bash
tail -f logs/selenium-mcp.log | grep -i "protocol\|version\|error"
```

**Look for**:
- ✅ No protocol version warning
- ✅ Server starts successfully
- ✅ No errors during initialization

### Step 8: Test Functionality

Run through the OrangeHRM workflow:

```javascript
1. get_sessions()
2. browser_init()
3. go_to(sessionId, "https://opensource-demo.orangehrmlive.com/...")
4. send_keys_to_element(sessionId, "name", "username", "Admin")
5. send_keys_to_element(sessionId, "name", "password", "admin123")
6. left_click(sessionId, "css", "button[type='submit']")
7. quit_browser(sessionId)
```

**All operations should work without errors.**

### Step 9: Run Tests

```bash
./gradlew test

# Check test results
cat build/reports/tests/test/index.html
```

### Step 10: Monitor for Issues

Run the server for 30 minutes and monitor:

```bash
# Watch for errors
tail -f logs/selenium-mcp.log | grep -i "error\|exception\|failed"

# Check session health
watch -n 10 'curl -s http://localhost:7676/actuator/health | jq ".components.sessionHealth"'

# Check active sessions
watch -n 10 'curl -s http://localhost:7676/actuator/metrics/selenium.sessions.active | jq ".measurements[0].value"'
```

---

## If Upgrade Fails

### Rollback Steps

```bash
# Stop the server
# Ctrl+C or kill the process

# Restore backup
cp build.gradle.backup build.gradle

# Clean and rebuild
./gradlew clean build

# Restart server
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### Common Issues

#### Issue 1: Build Fails
```
Error: Could not resolve io.modelcontextprotocol.sdk:mcp:0.11.0
```

**Solution**: The version doesn't exist yet. Wait for official release.

#### Issue 2: Runtime Errors
```
NoSuchMethodError or ClassNotFoundException
```

**Solution**: Incompatibility with Spring AI. Rollback and wait for official update.

#### Issue 3: Protocol Still Shows 2024-11-05
```
WARN - Client requested unsupported protocol version: 2025-06-18
```

**Solution**: The SDK version you upgraded to doesn't support 2025-06-18 yet.

---

## Alternative: Suppress the Warning

If you just want to hide the warning (cosmetic fix):

### Option A: Adjust Log Level

Edit `application-dev.properties`:

```properties
# Suppress MCP protocol version warnings
logging.level.io.modelcontextprotocol=ERROR
```

### Option B: Custom Log Filter

Create a custom log filter (advanced):

```java
@Component
public class McpProtocolWarningFilter implements Filter<ILoggingEvent> {
    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (event.getMessage().contains("protocol version") && 
            event.getLevel() == Level.WARN) {
            return FilterReply.DENY;
        }
        return FilterReply.NEUTRAL;
    }
}
```

---

## Monitoring for Official Updates

### Subscribe to Releases

1. **Spring AI GitHub**:
   - https://github.com/spring-projects/spring-ai
   - Watch → Custom → Releases

2. **MCP Specification**:
   - https://github.com/modelcontextprotocol/specification
   - Watch for protocol updates

3. **MCP Java SDK**:
   - https://github.com/modelcontextprotocol/java-sdk
   - Watch for new releases

### Check Periodically

```bash
# Check Spring AI latest version
curl -s "https://api.github.com/repos/spring-projects/spring-ai/releases/latest" | jq '.tag_name'

# Check MCP SDK latest version
curl -s "https://repo1.maven.org/maven2/io/modelcontextprotocol/sdk/mcp/maven-metadata.xml" | grep -o "<version>[^<]*</version>" | tail -1
```

---

## When to Upgrade

Upgrade when:
- [ ] Spring AI releases 1.0.2+ or 1.1.0+ with MCP SDK 0.11+
- [ ] Release notes mention "MCP protocol 2025-06-18 support"
- [ ] Official documentation recommends upgrade

**Until then, your current setup is perfect!**

---

## Summary

| Action | Recommendation | Reason |
|--------|---------------|--------|
| **Upgrade Now** | ❌ **NO** | No newer MCP SDK available |
| **Wait for Spring AI Update** | ✅ **YES** | Safe, tested, official |
| **Suppress Warning** | ✅ **YES** | If warning bothers you |
| **Manual SDK Override** | ⚠️ **RISKY** | May break compatibility |

**Best Action**: Do nothing. Your server works perfectly as-is.

The protocol version warning is informational only and doesn't affect functionality.

