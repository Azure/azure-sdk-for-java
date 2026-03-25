# Documentation: Clarify minimum SDK version for UseDevelopmentEmulator parameter

## Issue Summary

The Azure Event Hubs Java SDK version 5.18.0 throws an "Illegal connection string parameter name" error when using the `UseDevelopmentEmulator=true` parameter with Azure Event Hubs Emulator connection strings. This parameter works correctly in SDK version 5.19.2+, but the documentation doesn't clearly specify the minimum version requirement.

## Expected Behavior

Connection strings with `UseDevelopmentEmulator=true` should either:
1. Work seamlessly (in supported versions), or 
2. Provide clear documentation indicating the minimum required SDK version

## Actual Behavior

- **SDK 5.18.0**: Throws `IllegalArgumentException: Illegal connection string parameter name: UseDevelopmentEmulator`
- **SDK 5.19.2+**: Works correctly via `ConnectionStringProperties.useDevelopmentEmulator()`

## Reproduction Steps

1. Use Azure Event Hubs Java SDK version 5.18.0
2. Create connection string with emulator parameter:
   ```java
   String connStr = "Endpoint=sb://localhost;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=SAS_KEY_VALUE;UseDevelopmentEmulator=true";
   ```
3. Attempt to create EventHubProducerClient:
   ```java
   EventHubProducerClient producer = new EventHubClientBuilder()
       .connectionString(connStr, "my-event-hub")
       .buildProducerClient();
   ```
4. **Result**: Exception thrown with message about illegal parameter name

## Environment

- **SDK Version**: 5.18.0 (fails), 5.19.2+ (works)
- **Java Version**: Any
- **Azure Event Hubs Emulator**: Latest version
- **OS**: Any

## Cross-SDK Compatibility

This parameter is supported and documented in:
- ✅ Python SDK (`azure-eventhub`)
- ✅ JavaScript SDK (`@azure/event-hubs`) 
- ✅ .NET SDK (`Azure.Messaging.EventHubs`)
- ❌ Java SDK (only 5.19.2+, undocumented version requirement)

## Impact

Developers following the official Azure Event Hubs Emulator documentation encounter unexpected failures when using Java SDK versions < 5.19.2, leading to:
- Development workflow disruption
- Confusion about Java SDK compatibility
- Inconsistent behavior across language SDKs

## Suggested Resolution

1. **Update documentation** to clearly specify that `UseDevelopmentEmulator` parameter requires Java SDK version 5.19.2 or higher
2. **Add version compatibility matrix** to the Event Hubs Emulator documentation
3. **Consider backporting support** to earlier SDK versions if feasible
4. **Improve error message** in older versions to suggest upgrading SDK

## Code References

The fix was implemented in SDK 5.19.2:
- File: `EventHubClientBuilder.java`
- Lines: 1206-1207
- Method: `connectionStringProperties.useDevelopmentEmulator()`

## Labels Suggestion

`Event Hubs`, `documentation`, `emulator`, `version-compatibility`, `customer-reported`

## Priority

**Medium** - Affects developer experience but has clear workaround (upgrade to 5.19.2+)

---

## Discovered During

Performance audit of Azure Event Hubs SDKs - March 2026
