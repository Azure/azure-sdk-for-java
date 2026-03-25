# Security Report: Endpoint Information Disclosure in Error Messages

**Report ID**: SEC-004
**Date**: 2026-03-18
**SDK**: azure-messaging-eventhubs (Java)
**Version Tested**: 5.18.0
**Severity**: LOW
**Status**: Draft
**CWE**: [CWE-209](https://cwe.mitre.org/data/definitions/209.html) - Generation of Error Message Containing Sensitive Information

## Executive Summary

The EventHub client builder includes user-provided custom endpoint URLs directly in exception messages without sanitization. This can lead to accidental logging of sensitive information such as connection strings, authentication tokens, or internal network details that appear in endpoint URLs.

## Technical Details

### Affected Component
- File: `EventHubClientBuilder.java`
- Method: `buildAsyncClient()`
- Lines: 458-460

### Vulnerability Description

Custom endpoint validation echoes user input directly in exception messages:

```java
// Lines 458-460 in EventHubClientBuilder.java
if (!CoreUtils.isNullOrEmpty(customEndpointAddress)) {
    throw new IllegalArgumentException("'customEndpointAddress' cannot be used along with 'connectionString'. "
        + "Specify either 'connectionString' or 'customEndpointAddress': " + customEndpointAddress);
}
```

The `customEndpointAddress` is included verbatim in the exception message, which can contain:
- Authentication tokens in URL parameters
- Internal network addresses  
- Development server credentials
- API keys or sensitive path components

### Attack Vector

1. Developer accidentally includes sensitive data in custom endpoint URL
2. Configuration error triggers exception during client creation
3. Exception logged to application logs, monitoring systems, or error tracking
4. Sensitive information from endpoint URL exposed in logs
5. Unauthorized personnel access logs and extract sensitive data

### Proof of Concept

```java
// Endpoint containing sensitive information
String sensitiveEndpoint = "https://internal-server:8080/eventhub?token=secret123&user=admin";

EventHubClientBuilder builder = new EventHubClientBuilder()
    .connectionString(connectionString, "eventhub")
    .customEndpointAddress(sensitiveEndpoint);

try {
    EventHubProducerClient client = builder.buildProducerClient();
} catch (IllegalArgumentException e) {
    // Exception message contains: 
    // "'customEndpointAddress' cannot be used along with 'connectionString'. 
    //  Specify either 'connectionString' or 'customEndpointAddress': 
    //  https://internal-server:8080/eventhub?token=secret123&user=admin"
    
    logger.error("Client creation failed", e); // Logs sensitive endpoint!
}
```

### Impact Assessment

- **Confidentiality**: LOW - Sensitive data may be exposed in logs
- **Integrity**: NONE - No data modification
- **Availability**: NONE - Service remains functional
- **CVSS 3.1 Score**: 3.1
- **CVSS Vector**: CVSS:3.1/AV:L/AC:L/PR:L/UI:N/S:U/C:L/I:N/A:N

## Remediation

### Recommended Fix

Sanitize endpoint URLs before including in error messages:

```java
// Replace lines 458-460 in EventHubClientBuilder.java
if (!CoreUtils.isNullOrEmpty(customEndpointAddress)) {
    String sanitizedEndpoint = sanitizeUrlForLogging(customEndpointAddress);
    throw new IllegalArgumentException("'customEndpointAddress' cannot be used along with 'connectionString'. "
        + "Specify either 'connectionString' or 'customEndpointAddress'. "
        + "Custom endpoint: " + sanitizedEndpoint);
}

private String sanitizeUrlForLogging(String url) {
    try {
        URL parsed = new URL(url);
        // Only include scheme, host, and port - remove path, query, fragment
        StringBuilder sanitized = new StringBuilder();
        sanitized.append(parsed.getProtocol()).append("://");
        sanitized.append(parsed.getHost());
        if (parsed.getPort() != -1) {
            sanitized.append(":").append(parsed.getPort());
        }
        sanitized.append("/...");
        return sanitized.toString();
    } catch (MalformedURLException e) {
        return "[invalid-url]";
    }
}
```

### Alternative: Generic Error Message

For maximum security, use generic error messages:

```java
if (!CoreUtils.isNullOrEmpty(customEndpointAddress)) {
    throw new IllegalArgumentException(
        "'customEndpointAddress' cannot be used along with 'connectionString'. " +
        "Specify either 'connectionString' or 'customEndpointAddress'.");
}
```

### Patch

See `fix.patch` in this directory.

### Production Workaround

- Configure log filtering to redact URLs containing sensitive patterns
- Use structured logging to separate error codes from potentially sensitive details
- Implement application-level URL sanitization before passing to EventHub client

```java
// Application-level protection
public String sanitizeEndpoint(String endpoint) {
    if (endpoint == null) return null;
    
    // Remove query parameters and authentication information
    int queryStart = endpoint.indexOf('?');
    if (queryStart != -1) {
        endpoint = endpoint.substring(0, queryStart);
    }
    
    // Replace userinfo if present
    return endpoint.replaceAll("://[^@]*@", "://[auth-redacted]@");
}
```

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

- [CWE-209: Information Exposure Through Error Messages](https://cwe.mitre.org/data/definitions/209.html)
- [OWASP Logging Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Logging_Cheat_Sheet.html)
- [Java URL Class Documentation](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/net/URL.html)

## Credits

Discovered by Security Audit Team during comprehensive review of azure-messaging-eventhubs SDK.