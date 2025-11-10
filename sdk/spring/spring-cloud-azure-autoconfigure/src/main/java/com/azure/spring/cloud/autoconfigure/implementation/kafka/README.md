# Azure Event Hubs for Kafka - Spring Cloud Azure Auto-Configuration

This package provides auto-configuration for Azure Event Hubs for Kafka, supporting multiple authentication methods.

## Architecture

The Kafka authentication support follows a strategy pattern to handle different authentication mechanisms:

### Components

1. **`KafkaAuthenticationConfigurer`** - Strategy interface for authentication configuration
2. **`OAuth2AuthenticationConfigurer`** - Implements OAuth2/OAUTHBEARER authentication using Azure Identity
3. **`AbstractKafkaPropertiesBeanPostProcessor`** - Base class for processing Kafka properties
4. **`KafkaPropertiesBeanPostProcessor`** - Processes standard Spring Kafka properties
5. **`KafkaBinderConfigurationPropertiesBeanPostProcessor`** - Processes Spring Cloud Stream Kafka binder properties

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

To add a new authentication method:

1. Implement the `KafkaAuthenticationConfigurer` interface
2. Override `createAuthenticationConfigurer()` in your custom `AbstractKafkaPropertiesBeanPostProcessor` subclass
3. Register your custom BeanPostProcessor in an auto-configuration class

Example:
```java
class CustomAuthenticationConfigurer implements KafkaAuthenticationConfigurer {
    @Override
    public boolean canConfigure(Map<String, Object> mergedProperties) {
        // Check if properties indicate this authentication method should be used
        return ...;
    }

    @Override
    public void configure(Map<String, Object> mergedProperties, Map<String, String> rawProperties) {
        // Configure the authentication properties
        ...
    }
}
```

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
