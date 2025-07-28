# Azure Core HTTP Pipeline Policy Samples

This directory contains comprehensive samples demonstrating how to write custom HTTP pipeline policies, use HttpTrait to configure service clients, and implement context propagation patterns.

## Samples Overview

### [CustomPolicyExamples.java](CustomPolicyExamples.java)

Demonstrates how to write custom HTTP pipeline policies for various observability and cross-cutting concerns:

- **ObservabilityLoggingPolicy**: Logs request/response information for monitoring
- **MetricsCollectionPolicy**: Collects performance metrics (request count, duration)
- **ContextAwarePolicy**: Reads values from context and adds them as HTTP headers
- **RetryAwarePolicy**: Tracks retry attempts and modifies behavior based on retry count

**Use cases**: Observability, monitoring, distributed tracing, custom headers, metrics collection

### [HttpTraitExamples.java](HttpTraitExamples.java)

Shows how to use the `HttpTrait` interface to configure Azure SDK service clients with custom policies:

- Adding single and multiple custom policies
- Configuring retry options, logging, and timeouts
- Using HttpClientOptions for advanced configuration
- Providing pre-built HTTP pipelines
- Fluent configuration patterns for complex scenarios

**Use cases**: Service client configuration, policy integration, HTTP client customization

### [ContextPropagationExamples.java](ContextPropagationExamples.java)

Demonstrates context propagation patterns for passing data through the HTTP pipeline:

- Basic context creation and usage
- Multi-value context propagation
- Async context propagation with Mono/Flux
- Synchronous context usage
- Concurrent operations with context
- Context enrichment during request processing

**Use cases**: Correlation tracking, distributed tracing, user context propagation, observability data flow

## Common Patterns

### Adding Observability to Service Clients

```java
// Create client with observability policies
ExampleServiceClient client = new ExampleServiceClientBuilder()
    .endpoint("https://example.service.azure.com")
    .addPolicy(new ObservabilityLoggingPolicy("my-service"))
    .addPolicy(new MetricsCollectionPolicy("my-service"))
    .addPolicy(new ContextAwarePolicy())
    .build();
```

### Context Propagation for Correlation

```java
// Create context with correlation data
Context context = Context.NONE
    .addData("correlationId", UUID.randomUUID().toString())
    .addData("userId", "user123");

// Send request with context
Mono<HttpResponse> response = client.sendRequest(request, context);
```

### Policy for Custom Headers

```java
public class CustomHeaderPolicy implements HttpPipelinePolicy {
    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        // Add custom header based on context or configuration
        context.getHttpRequest().getHeaders()
            .set(HttpHeaderName.fromString("X-Custom-Header"), "value");
        return next.process();
    }
}
```

## Best Practices

1. **Always implement both async and sync methods** in your policies (`process` and `processSync`)
2. **Use structured logging** with key-value pairs for better observability
3. **Handle errors gracefully** in policies to avoid breaking the pipeline
4. **Use Context.NONE** as the base when creating new contexts
5. **Make policies immutable** and thread-safe
6. **Document policy behavior** and any context keys they use or require
7. **Test policies thoroughly** in both success and failure scenarios

## Testing Custom Policies

When testing custom policies, create mock pipelines and verify:
- Policy is called in the correct order
- Context data is properly read and propagated
- Headers are added correctly
- Metrics/logs are recorded as expected
- Error handling works properly

## Integration with Azure SDK Clients

All Azure SDK client builders that implement `HttpTrait` can use these custom policies. Examples include:
- Storage clients (BlobClient, QueueClient, etc.)
- Key Vault clients
- Service Bus clients
- Event Hubs clients
- And many more

Simply use the `.addPolicy()` method when building any Azure SDK client to include your custom observability or processing logic.