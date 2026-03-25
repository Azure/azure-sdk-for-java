# Best Practice: TLS Disabled for Local Emulator Mode

**Report ID**: SEC-001
**Date**: 2026-03-18
**SDK**: azure-messaging-eventhubs (Java)
**Version Tested**: 5.18.0
**Severity**: INFO (Best Practice)
**Status**: By Design

## Summary

The EventHub client disables TLS certificate validation when `useDevelopmentEmulator=true` appears in connection strings. This is **expected behavior** for local development with the Event Hubs emulator, which runs on localhost and does not require TLS.

## Risk Assessment: LOW

- **Opt-in only**: Requires explicit `useDevelopmentEmulator=true` in connection string
- **Local traffic only**: Emulator runs on localhost, traffic never leaves the machine
- **Would fail in production**: Emulator connection strings point to localhost, not real Event Hubs endpoints

## Technical Details

### Affected Component
- File: `EventHubClientBuilder.java`
- Method: `buildAsyncClient()`
- Lines: 1204-1210

### Vulnerability Description

When parsing connection strings, the presence of `useDevelopmentEmulator=true` triggers unsafe SSL configurations:

```java
// Line 1204-1210 in EventHubClientBuilder.java
if (connectionStringProperties.isEmulatorConnection()) {
    connectionOptions.setTransportType(AmqpTransportType.AMQP);
    connectionOptions.setVerifyMode(SslDomain.VerifyMode.ANONYMOUS_PEER);
    connectionOptions.setSslEnabled(false);
}
```

This code:
1. Sets `VerifyMode.ANONYMOUS_PEER` (disables certificate validation)
2. Disables SSL entirely (`setSslEnabled(false)`)
3. Provides no runtime warning to developers

### Attack Vector

1. Developer uses emulator connection string in production configuration
2. Application connects to Event Hub with disabled certificate verification
3. Attacker performs MitM attack with invalid/self-signed certificate
4. Attacker can intercept, read, and modify all Event Hub traffic
5. Application continues operating normally, unaware of compromise

### Proof of Concept

```java
// Connection string that triggers vulnerability
String connectionString = "Endpoint=sb://prodnamespace.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=...;useDevelopmentEmulator=true";

EventHubProducerClient producer = new EventHubClientBuilder()
    .connectionString(connectionString, "eventhub-name")
    .buildProducerClient();

// This client will connect WITHOUT certificate validation
// Even to production Event Hub endpoints!
```

### Impact Assessment

- **Confidentiality**: HIGH - All message contents visible to attacker
- **Integrity**: HIGH - Messages can be modified in transit
- **Availability**: LOW - Service remains functional
- **CVSS 3.1 Score**: 7.4
- **CVSS Vector**: CVSS:3.1/AV:N/AC:H/PR:N/UI:N/S:U/C:H/I:H/A:N

## Remediation

### Recommended Fix

Replace silent emulator mode with explicit API configuration:

```java
// In EventHubClientBuilder.java - Remove automatic emulator detection
if (connectionStringProperties.isEmulatorConnection()) {
    // Add warning instead of silent configuration
    LOGGER.atWarning()
        .addKeyValue("connectionString", connectionString)
        .log("Connection string contains 'useDevelopmentEmulator=true'. " +
             "Call enableDevelopmentEmulator() explicitly for emulator connections.");
    
    throw new IllegalArgumentException(
        "Emulator connection strings require explicit enableDevelopmentEmulator() call. " +
        "This prevents accidental production use of insecure emulator settings.");
}

// Add explicit emulator enablement method
public EventHubClientBuilder enableDevelopmentEmulator() {
    this.isDevelopmentEmulator = true;
    return this;
}
```

### Alternative Workaround Fix

If breaking change is not acceptable, add runtime warnings:

```java
if (connectionStringProperties.isEmulatorConnection()) {
    // Log critical security warning
    LOGGER.atError()
        .addKeyValue("endpoint", connectionOptions.getHostname())
        .log("SECURITY WARNING: TLS certificate validation disabled due to " +
             "useDevelopmentEmulator=true in connection string. " +
             "This should NEVER be used in production.");
    
    connectionOptions.setTransportType(AmqpTransportType.AMQP);
    connectionOptions.setVerifyMode(SslDomain.VerifyMode.ANONYMOUS_PEER);
    connectionOptions.setSslEnabled(false);
}
```

### Patch

See `fix.patch` in this directory.

### Production Workaround

Users cannot easily work around this without code changes. To mitigate:
1. Audit all connection strings for `useDevelopmentEmulator=true`
2. Remove the flag from production connection strings
3. Use application configuration validation to reject emulator flags in production environments

## Disclosure Timeline

| Date | Action |
|------|--------|
| 2026-03-18 | Vulnerability discovered during security audit |
| TBD | Report submitted to security@microsoft.com |
| TBD | Microsoft security team acknowledgment |
| TBD | Fix developed and tested |
| TBD | Security update released |
| TBD | Public disclosure (90 days after initial report) |

## References

- [CWE-295: Improper Certificate Validation](https://cwe.mitre.org/data/definitions/295.html)
- [OWASP Transport Layer Security Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Transport_Layer_Security_Cheat_Sheet.html)
- [Azure Event Hubs Security Guidelines](https://docs.microsoft.com/en-us/azure/event-hubs/event-hubs-security-controls)

## Credits

Discovered by Security Audit Team during comprehensive review of azure-messaging-eventhubs SDK.