# Security Report: Integer Overflow in AMQP Credit Calculation

**Report ID**: SEC-003
**Date**: 2026-03-18
**SDK**: azure-messaging-eventhubs (Java)
**Version Tested**: 5.18.0
**Severity**: MEDIUM
**Status**: Draft
**CWE**: [CWE-190](https://cwe.mitre.org/data/definitions/190.html) - Integer Overflow or Wraparound

## Executive Summary

The AMQP receive link processor performs unsafe integer truncation when converting `Long` credit requests to `int` values. When credit requests exceed `Integer.MAX_VALUE` (2,147,483,647), the truncation causes wraparound to negative values, potentially leading to denial of service or message loss in high-throughput scenarios.

## Technical Details

### Affected Component
- File: `AmqpReceiveLinkProcessor.java`
- Method: `request(long)`
- Line: 699

### Vulnerability Description

The credit calculation performs unsafe truncation:

```java
// Line 699 in AmqpReceiveLinkProcessor.java
final int credit = Long.valueOf(request).intValue();
```

When `request > Integer.MAX_VALUE`, `intValue()` truncates the upper 32 bits, causing:
- Values 2,147,483,648 - 4,294,967,295 become negative numbers
- Values > 4,294,967,295 wrap around unpredictably

### Attack Vector

1. Attacker sends large prefetch requests (> 2.1 billion)
2. Integer overflow causes negative credit values
3. AMQP receiver enters invalid state with negative credits
4. Message receiving stops or behaves unpredictably
5. Denial of service or message loss occurs

### Proof of Concept

```java
// Demonstrate the overflow
long largeRequest = 3_000_000_000L; // 3 billion, > Integer.MAX_VALUE
int truncatedCredit = Long.valueOf(largeRequest).intValue();
System.out.println("Request: " + largeRequest);
System.out.println("Truncated: " + truncatedCredit); // Outputs: -1294967296

// In actual AmqpReceiveLinkProcessor usage:
AmqpReceiveLinkProcessor processor = new AmqpReceiveLinkProcessor(...);

// This would cause integer overflow and negative credits
processor.request(3_000_000_000L);
```

### Impact Assessment

- **Confidentiality**: NONE - No data exposure
- **Integrity**: LOW - Potential message ordering issues
- **Availability**: MEDIUM - Can cause receiver denial of service
- **CVSS 3.1 Score**: 4.3
- **CVSS Vector**: CVSS:3.1/AV:N/AC:L/PR:L/UI:N/S:U/C:N/I:L/A:L

## Remediation

### Recommended Fix

Add safe integer conversion with bounds checking:

```java
// Replace line 699 in AmqpReceiveLinkProcessor.java
final int credit = (int) Math.min(request - messageQueue.size(), Integer.MAX_VALUE);
```

### Alternative: Full Range Support

If full long range support is needed:

```java
// Handle large requests by chunking
private void requestCredits(long totalRequest) {
    final int queueSize = messageQueue.size();
    long remainingRequest = totalRequest - queueSize;
    
    while (remainingRequest > 0) {
        final int creditBatch = (int) Math.min(remainingRequest, Integer.MAX_VALUE);
        receiver.flow(creditBatch);
        remainingRequest -= creditBatch;
    }
}
```

### Complete Fix

```java
@Override
public void request(long request) {
    if (request <= 0) {
        return;
    }

    final long currentMessageQueueSize = messageQueue.size();
    // Prevent negative results and integer overflow
    final long actualRequest = Math.max(0, request - currentMessageQueueSize);
    
    // Safe integer conversion with bounds checking
    if (actualRequest > Integer.MAX_VALUE) {
        LOGGER.atWarning()
            .addKeyValue("requestedCredits", request)
            .addKeyValue("queueSize", currentMessageQueueSize)
            .log("Credit request exceeds Integer.MAX_VALUE, clamping to maximum safe value");
    }
    
    final int credit = (int) Math.min(actualRequest, Integer.MAX_VALUE);
    
    if (credit > 0) {
        receiver.flow(credit);
    }
}
```

### Patch

See `fix.patch` in this directory.

### Production Workaround

Monitor and limit prefetch values:
1. Set reasonable upper bounds on prefetch requests in application code
2. Add monitoring for unusually large credit requests
3. Use client-side validation to reject excessive prefetch values

```java
// Defensive programming in application code
public void configurePrefetch(EventHubConsumerClient consumer, int prefetch) {
    if (prefetch > 1_000_000) { // Reasonable upper bound
        throw new IllegalArgumentException("Prefetch too large: " + prefetch);
    }
    // Configure consumer...
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

- [CWE-190: Integer Overflow or Wraparound](https://cwe.mitre.org/data/definitions/190.html)
- [Java Integer Overflow Detection](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/Math.html#addExact(int,int))
- [AMQP 1.0 Flow Control Specification](https://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-transport-v1.0-os.html#section-flow-control)

## Credits

Discovered by Security Audit Team during comprehensive review of azure-messaging-eventhubs SDK.