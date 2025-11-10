# Azure Event Hubs for Kafka - Spring Cloud Azure Auto-Configuration

This package provides auto-configuration for Azure Event Hubs for Kafka, supporting multiple authentication methods.

## Architecture

The Kafka authentication support follows a **Strategy Pattern** combined with **Template Method Pattern** to handle different authentication mechanisms:

### Class Hierarchy

```
KafkaAuthenticationConfigurer (interface)
    ↑
AbstractKafkaAuthenticationConfigurer (template base class)
    ↑                                    ↑
OAuth2AuthenticationConfigurer   ConnectionStringAuthenticationConfigurer
```

### Components

1. **`KafkaAuthenticationConfigurer`** - Strategy interface for authentication configuration
2. **`AbstractKafkaAuthenticationConfigurer`** - Template base class with common validation logic
3. **`OAuth2AuthenticationConfigurer`** - Implements OAuth2/OAUTHBEARER authentication using Azure Identity
4. **`ConnectionStringAuthenticationConfigurer`** - Implements connection string authentication (deprecated)
5. **`AbstractKafkaPropertiesBeanPostProcessor`** - Base class for processing Kafka properties
6. **`KafkaPropertiesBeanPostProcessor`** - Processes standard Spring Kafka properties
7. **`KafkaBinderConfigurationPropertiesBeanPostProcessor`** - Processes Spring Cloud Stream Kafka binder properties

### Template Method Pattern

The `AbstractKafkaAuthenticationConfigurer` provides common functionality:
- **Bootstrap server validation** - Checks if server points to Event Hubs (*.servicebus.windows.net:9093)
- **SASL protocol checking** - Validates security protocol configuration
- **Property extraction** - Helper methods to get security properties

Subclasses implement specific authentication logic:
- **`meetAuthenticationConditions()`** - Check if this auth type can be applied
- **`configure()`** - Apply the authentication configuration

## Supported Authentication Methods

### 1. OAuth2/OAUTHBEARER (Recommended)

**Package**: `com.azure.spring.cloud.autoconfigure.implementation.kafka`

This is the recommended authentication method that uses Azure Identity credentials (Managed Identity, Service Principal, etc.) to authenticate with Azure Event Hubs.

**Auto-Configuration Class**: `AzureEventHubsKafkaOAuth2AutoConfiguration`

**How it works**:
- Automatically configures SASL_SSL security protocol
- Sets OAUTHBEARER as the SASL mechanism
- Configures `KafkaOAuth2AuthenticateCallbackHandler` for token acquisition
- Supports all Azure Identity credential types

**Configuration Example**:
```properties
spring.kafka.bootstrap-servers=<your-eventhubs-namespace>.servicebus.windows.net:9093
spring.cloud.azure.credential.managed-identity-enabled=true
```

### 2. Connection String (Deprecated)

**Package**: `com.azure.spring.cloud.autoconfigure.implementation.eventhubs.kafka`

This method uses Event Hubs connection strings with SASL_PLAIN mechanism. It is deprecated in favor of OAuth2.

**Auto-Configuration Class**: `AzureEventHubsKafkaAutoConfiguration` (deprecated since 4.3.0)

**How it works**:
- Extracts connection string from properties
- Configures SASL_SSL with PLAIN mechanism
- Sets up username/password authentication
- Uses `ConnectionStringAuthenticationConfigurer` strategy

**Configuration Example** (deprecated):
```properties
spring.cloud.azure.eventhubs.connection-string=<connection-string>
```

## Configuration Hierarchy

The auto-configuration applies in the following order:

1. **OAuth2 Configuration** (`AzureEventHubsKafkaOAuth2AutoConfiguration`)
   - Enabled by default (`spring.cloud.azure.eventhubs.kafka.enabled=true`)
   - Applies to standard Kafka properties via `KafkaPropertiesBeanPostProcessor`

2. **Spring Cloud Stream Binder Support** (`AzureEventHubsKafkaBinderOAuth2AutoConfiguration`)
   - Enabled when Spring Cloud Stream Kafka binder is on classpath
   - Applies OAuth2 configuration to binder properties

3. **Connection String Configuration** (`AzureEventHubsKafkaAutoConfiguration`)
   - Deprecated - use OAuth2 instead
   - Only applies when connection string is explicitly configured

## Migration Guide

### Migrating from Connection String to OAuth2

**Before** (Connection String):
```properties
spring.cloud.azure.eventhubs.connection-string=Endpoint=sb://...
```

**After** (OAuth2 with Managed Identity):
```properties
spring.kafka.bootstrap-servers=<your-namespace>.servicebus.windows.net:9093
spring.cloud.azure.credential.managed-identity-enabled=true
```

**After** (OAuth2 with Service Principal):
```properties
spring.kafka.bootstrap-servers=<your-namespace>.servicebus.windows.net:9093
spring.cloud.azure.credential.client-id=<client-id>
spring.cloud.azure.credential.client-secret=<client-secret>
spring.cloud.azure.profile.tenant-id=<tenant-id>
```

## Extension Points

To add a new authentication method, extend the `AbstractKafkaAuthenticationConfigurer`:

1. **Extend `AbstractKafkaAuthenticationConfigurer`** - Inherit common validation logic
2. **Implement `meetAuthenticationConditions()`** - Check if this auth type applies
3. **Implement `configure()`** - Apply authentication configuration
4. **Register in auto-configuration** - Wire up in appropriate auto-configuration class

### Example: Custom Authentication

```java
public class CustomAuthenticationConfigurer extends AbstractKafkaAuthenticationConfigurer {
    
    public CustomAuthenticationConfigurer(Logger logger) {
        super(logger);
    }

    @Override
    protected boolean meetAuthenticationConditions(Map<String, Object> sourceProperties) {
        // Check if this authentication method should be used
        // You can use inherited methods: getSecurityProtocol(), getSaslMechanism(), etc.
        String mechanism = getSaslMechanism(sourceProperties);
        return "CUSTOM".equals(mechanism);
    }

    @Override
    public void configure(Map<String, Object> mergedProperties, Map<String, String> rawProperties) {
        // Configure the authentication properties
        rawProperties.put(SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
        rawProperties.put(SASL_MECHANISM, "CUSTOM");
        rawProperties.put(SASL_JAAS_CONFIG, buildCustomJaasConfig());
    }
}
```

### Inherited Template Methods

When extending `AbstractKafkaAuthenticationConfigurer`, you get:

**Validation Methods:**
- `meetBootstrapServerConditions()` - Validates Event Hubs bootstrap server
- `meetSaslProtocolConditions()` - Checks SASL_SSL protocol
- `extractBootstrapServerList()` - Parses bootstrap server configuration

**Property Getters:**
- `getSecurityProtocol()` - Gets security protocol from properties
- `getSaslMechanism()` - Gets SASL mechanism from properties
- `getJaasConfig()` - Gets JAAS config from properties

## Implementation Notes

### BeanPostProcessor Flow

1. `postProcessBeforeInitialization()` is called for each bean
2. Checks if bean needs processing (`needsPostProcess()`)
3. For each client type (producer, consumer, admin):
   - Gets merged properties (all config sources combined)
   - Gets raw properties (Map to modify)
   - Creates appropriate `KafkaAuthenticationConfigurer`
   - Calls `canConfigure()` to check if authentication should be applied
   - If yes, calls `configure()` to set authentication properties
   - Clears Azure-specific properties from raw map

### User-Agent Configuration

The implementation automatically configures a Spring Cloud Azure user-agent for Kafka clients to help with diagnostics and tracking.

## Testing

All authentication configurers should be tested with:
- Various bootstrap server configurations
- Different security protocol settings
- Multiple SASL mechanism combinations
- Edge cases (null values, invalid formats, etc.)

See `AbstractKafkaPropertiesBeanPostProcessorTest` for test patterns.
