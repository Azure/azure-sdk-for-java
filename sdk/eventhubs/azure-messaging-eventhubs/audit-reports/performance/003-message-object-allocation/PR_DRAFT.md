# PR Title: [performance] Reduce Java message conversion churn in MessageUtils

## Summary

Reduces transient allocation churn in `MessageUtils.toProtonJMessage()` by replacing stream-based annotation conversion with a pre-sized loop, returning a shared empty map for empty inputs, and centralizing Proton `Properties` creation. This keeps the current conversion behavior intact without introducing risky ThreadLocal pooling for mutable Proton objects.

## Changes

- `MessageUtils.java`: replace `convert(...)` stream/collector logic with a pre-sized `HashMap` loop
- `MessageUtils.java`: return `Collections.emptyMap()` for empty annotation inputs
- `MessageUtils.java`: add a `getOrCreateProperties(...)` helper to avoid repeated null checks and property lookups during conversion
- `MessageUtilsTest.java`: add focused tests for empty-map conversion and `Instant` to `Date` conversion

### Core Implementation

```java
public static Map<Symbol, Object> convert(Map<String, Object> sourceMap) {
  if (sourceMap == null) {
    return null;
  } else if (sourceMap.isEmpty()) {
    return Collections.emptyMap();
  }

  final Map<Symbol, Object> converted = new HashMap<>(calculateInitialCapacity(sourceMap.size()));
  for (Map.Entry<String, Object> entry : sourceMap.entrySet()) {
    if (entry.getValue() instanceof Instant) {
      converted.put(Symbol.valueOf(entry.getKey()), new Date(((Instant) entry.getValue()).toEpochMilli()));
    } else {
      converted.put(Symbol.valueOf(entry.getKey()), entry.getValue());
    }
  }

  return converted;
}

private static Properties getOrCreateProperties(Message protonJMessage) {
  if (protonJMessage.getProperties() == null) {
    protonJMessage.setProperties(new Properties());
  }

  return protonJMessage.getProperties();
}
```

## Expected Impact

- fewer temporary objects on every annotation conversion
- lower map resize pressure for non-trivial annotation/property sets
- less collector/lambda overhead in the `toProtonJMessage()` hot path

## Testing

- `mvn -Dtest=MessageUtilsTest,EventHubMessageSerializerTest test`
- Result: `BUILD SUCCESS`
- `Tests run: 18, Failures: 0, Errors: 0, Skipped: 0`

## Related

- **Benchmark Script**: [benchmark.java](./benchmark.java)
- **Memory Profiler Tests**: [memory-profile.java](./memory-profile.java)
- **Performance Report**: [REPORT.md](./REPORT.md)
- **Proposed Patch**: [fix.patch](./fix.patch)

---

**Breaking Changes**: None - all changes are internal to Java message conversion

**Migration Path**: No user action required

**Risk Assessment**: Low - no mutable object pooling, only smaller internal allocation cleanup