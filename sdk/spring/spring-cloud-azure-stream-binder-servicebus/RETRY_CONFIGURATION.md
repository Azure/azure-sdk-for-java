# ServiceBus Binder Retry Configuration

## Overview

The ServiceBus Binder now supports Spring Cloud Stream's consumer retry properties, enabling automatic retry with exponential backoff for message processing failures.

## Configuration

You can configure retry behavior using the following properties in your `application.yml` or `application.properties`:

### YAML Configuration Example

```yaml
spring:
  cloud:
    stream:
      bindings:
        consumer-in-0:
          destination: my-queue
          group: my-group
          consumer:
            max-attempts: 5                    # Maximum number of retry attempts (default: 3)
            back-off-initial-interval: 1000    # Initial backoff interval in milliseconds (default: 1000)
            back-off-max-interval: 10000       # Maximum backoff interval in milliseconds (default: 10000)
            back-off-multiplier: 2.0           # Backoff multiplier (default: 2.0)
      binders:
        servicebus:
          type: servicebus
```

### Properties Configuration Example

```properties
spring.cloud.stream.bindings.consumer-in-0.destination=my-queue
spring.cloud.stream.bindings.consumer-in-0.group=my-group
spring.cloud.stream.bindings.consumer-in-0.consumer.max-attempts=5
spring.cloud.stream.bindings.consumer-in-0.consumer.back-off-initial-interval=1000
spring.cloud.stream.bindings.consumer-in-0.consumer.back-off-max-interval=10000
spring.cloud.stream.bindings.consumer-in-0.consumer.back-off-multiplier=2.0
```

## How It Works

### Retry Behavior

When a message processing fails (throws an exception), the binder will:

1. **Retry Automatically**: Retry processing the message based on the `max-attempts` setting
2. **Exponential Backoff**: Wait between retries using an exponential backoff strategy:
   - First retry: waits `back-off-initial-interval` milliseconds
   - Subsequent retries: wait time is multiplied by `back-off-multiplier`
   - Maximum wait: capped at `back-off-max-interval` milliseconds

### Example Retry Timeline

With the configuration above (`max-attempts: 5`, `back-off-initial-interval: 1000`, `back-off-multiplier: 2.0`, `back-off-max-interval: 10000`):

- **Attempt 1**: Initial processing (fails)
- **Wait**: 1000ms (1 second)
- **Attempt 2**: Retry 1 (fails)
- **Wait**: 2000ms (2 seconds) = 1000ms × 2.0
- **Attempt 3**: Retry 2 (fails)
- **Wait**: 4000ms (4 seconds) = 2000ms × 2.0
- **Attempt 4**: Retry 3 (fails)
- **Wait**: 8000ms (8 seconds) = 4000ms × 2.0
- **Attempt 5**: Retry 4 (final attempt, fails)
- **Result**: Message is sent to error channel or dead letter queue (if configured)

### After All Retries Exhausted

When all retry attempts are exhausted:

- If `requeue-rejected: false` (default), the message is **abandoned**
- If `requeue-rejected: true`, the message is sent to the **dead letter queue**

## Consumer Example

```java
@Bean
public Consumer<Message<String>> consumer() {
    return message -> {
        // This method will be automatically retried if it throws an exception
        processMessage(message.getPayload());
    };
}

private void processMessage(String payload) {
    // Your business logic here
    // If this throws an exception, the message will be retried
    if (shouldFail(payload)) {
        throw new RuntimeException("Processing failed");
    }
    // Successfully processed
}
```

## Dead Letter Queue Configuration

To send failed messages to the dead letter queue after all retries are exhausted:

```yaml
spring:
  cloud:
    stream:
      servicebus:
        bindings:
          consumer-in-0:
            consumer:
              requeue-rejected: true  # Send failed messages to DLQ
```

## Disabling Retry

To disable retry (process message only once), set `max-attempts` to 1:

```yaml
spring:
  cloud:
    stream:
      bindings:
        consumer-in-0:
          consumer:
            max-attempts: 1  # No retries
```

## Best Practices

1. **Choose Appropriate Max Attempts**: Consider the nature of your failures. Transient network issues might benefit from more retries, while business logic errors might not.

2. **Configure Realistic Backoff Intervals**: Ensure your backoff intervals are appropriate for your use case:
   - Too short: May overwhelm downstream services
   - Too long: May delay message processing unnecessarily

3. **Monitor Dead Letter Queues**: Set up monitoring and alerting for messages in the DLQ to handle persistent failures.

4. **Use Specific Exception Types**: Consider catching specific exceptions in your consumer and only retrying for transient errors.

5. **Test Your Configuration**: Use integration tests to verify your retry configuration works as expected.

## Troubleshooting

### Retries Not Working

- Verify `max-attempts` is greater than 1
- Check that exceptions are being thrown from your consumer
- Enable debug logging: `logging.level.org.springframework.retry=DEBUG`

### Too Many Retries

- Reduce `max-attempts`
- Increase `back-off-initial-interval` or reduce `back-off-multiplier`
- Consider implementing circuit breaker patterns for persistent failures

### Messages Going to DLQ Immediately

- Verify `requeue-rejected` is set correctly
- Check if `max-attempts` is set to 1
- Review error handling logic in your consumer

## Related Configuration

- [Spring Cloud Stream Binding Properties](https://docs.spring.io/spring-cloud-stream/docs/current/reference/html/spring-cloud-stream.html#_consumer_properties)
- [Azure Service Bus Error Handling](https://learn.microsoft.com/azure/service-bus-messaging/service-bus-dead-letter-queues)
