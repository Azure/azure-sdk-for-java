# Security Report: Package-Private TLS Verification Mode Setter

**Report ID**: SEC-002
**Date**: 2026-03-18
**SDK**: azure-messaging-eventhubs (Java)
**Version Tested**: 5.18.0
**Severity**: MEDIUM
**Status**: Draft
**CWE**: [CWE-295](https://cwe.mitre.org/data/definitions/295.html) - Improper Certificate Validation

## Executive Summary

The `EventHubClientBuilder.verifyMode(SslDomain.VerifyMode)` method is package-private, allowing any code in the same package to disable TLS certificate validation. This creates a potential attack vector where malicious code or compromised dependencies within the Azure messaging package can bypass secure communications.

## Technical Details

### Affected Component
- File: `EventHubClientBuilder.java`
- Method: `verifyMode(SslDomain.VerifyMode)`
- Lines: 881-883

### Vulnerability Description

The TLS verification mode setter has package-private visibility:

```java
// Line 881-883 in EventHubClientBuilder.java
EventHubClientBuilder verifyMode(SslDomain.VerifyMode verifyMode) {
    this.verifyMode = verifyMode;
    return this;
}
```

This allows any class in the `com.azure.messaging.eventhubs` package to call:
```java
eventHubClientBuilder.verifyMode(SslDomain.VerifyMode.ANONYMOUS_PEER);
```

### Attack Vector

1. Malicious code is introduced into the `com.azure.messaging.eventhubs` package via:
   - Compromised dependency with same package name
   - Supply chain attack on existing package classes
   - Malicious code injection through bytecode manipulation
2. Attacker calls `verifyMode(ANONYMOUS_PEER)` before client creation
3. All subsequent TLS connections bypass certificate validation
4. Man-in-the-middle attacks become possible

### Proof of Concept

```java
// Malicious code within com.azure.messaging.eventhubs package
public class MaliciousHelper {
    public static EventHubClientBuilder compromiseBuilder(EventHubClientBuilder builder) {
        // This compiles because verifyMode is package-private
        return builder.verifyMode(SslDomain.VerifyMode.ANONYMOUS_PEER);
    }
}

// Usage that would bypass TLS validation
EventHubClientBuilder builder = new EventHubClientBuilder()
    .connectionString(connectionString, "eventhub");

// Malicious call that disables certificate validation  
builder = MaliciousHelper.compromiseBuilder(builder);

EventHubProducerClient client = builder.buildProducerClient();
// Client now connects without certificate validation!
```

### Impact Assessment

- **Confidentiality**: MEDIUM - TLS bypass enables traffic interception
- **Integrity**: MEDIUM - Messages can be modified in transit
- **Availability**: NONE - Service remains functional
- **CVSS 3.1 Score**: 5.3
- **CVSS Vector**: CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:L/I:L/A:N

## Remediation

### Recommended Fix

Make the method private and add input validation:

```java
// Change from package-private to private
private EventHubClientBuilder verifyMode(SslDomain.VerifyMode verifyMode) {
    // Add validation to reject insecure modes
    if (verifyMode == SslDomain.VerifyMode.ANONYMOUS_PEER) {
        throw new IllegalArgumentException(
            "ANONYMOUS_PEER verification mode is not allowed. " +
            "Use enableDevelopmentEmulator() for emulator connections.");
    }
    this.verifyMode = verifyMode;
    return this;
}
```

### Alternative: Controlled Access

If package-private access is needed for testing, add validation:

```java
EventHubClientBuilder verifyMode(SslDomain.VerifyMode verifyMode) {
    // Log security-relevant calls
    LOGGER.atDebug()
        .addKeyValue("verifyMode", verifyMode)
        .log("TLS verification mode explicitly set");
    
    // Reject dangerous modes in production
    if (verifyMode == SslDomain.VerifyMode.ANONYMOUS_PEER && !isDevelopmentEmulator) {
        LOGGER.atError()
            .addKeyValue("verifyMode", verifyMode)
            .log("Attempted to set ANONYMOUS_PEER verification mode outside emulator context");
        throw new SecurityException(
            "ANONYMOUS_PEER verification mode requires development emulator mode");
    }
    
    this.verifyMode = verifyMode;
    return this;
}
```

### Patch

See `fix.patch` in this directory.

### Production Workaround

- Monitor application logs for unexpected `verifyMode` calls
- Use static analysis tools to detect calls to package-private security methods
- Implement runtime security monitoring to detect TLS configuration changes

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
- [Java Package Access Control](https://docs.oracle.com/javase/tutorial/java/javaOO/accesscontrol.html)
- [Secure Coding Guidelines for Java](https://www.oracle.com/java/technologies/javase/seccodeguide.html)

## Credits

Discovered by Security Audit Team during comprehensive review of azure-messaging-eventhubs SDK.