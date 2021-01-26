# Create a configuration setting with Azure Core Tracing OpenTelemetry

Following documentation describes instructions to run a sample program for creating a Configuration Setting with tracing instrumentation.

## Getting Started
Sample uses **[opentelemetry-sdk][opentelemetry_sdk]** as implementation package and **[Logging Exporter][logging_exporter]** as exporter.
### Adding dependencies to your project:
```xml
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-sdk</artifactId>
    <version>0.14.1</version>
</dependency>
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-exporters-logging</artifactId>
    <version>0.14.1</version>
</dependency>
```

```xml
<!-- SDK dependencies   -->
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-data-appconfiguration</artifactId>
    <version>1.1.7</version>
</dependency>
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-core-tracing-opentelemetry</artifactId>
    <version>1.0.0-beta.7</version>
</dependency>
```

#### Sample demonstrates tracing when adding a configuration setting using [azure-data-app-configuration][azure_data_app_configuration] client library.
```java
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

public class Sample {
    private static final Tracer TRACER = configureOpenTelemetryAndLoggingExporter();
    private static final String CONNECTION_STRING = "<YOUR_CONNECTION_STRING>";

    public static void main(String[] args) {
        doClientWork();
    }

    private static Tracer configureOpenTelemetryAndLoggingExporter() {
        LoggingSpanExporter exporter = new LoggingSpanExporter();
        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder().build();
        openTelemetry
            .getTracerManagement()
            .addSpanProcessor(SimpleSpanProcessor.builder(exporter).build());
        return openTelemetry.getTracer("Sample");
    }

    public static void doClientWork() {
        ConfigurationClient client = new ConfigurationClientBuilder()
                .connectionString(CONNECTION_STRING)
                .buildClient();

        Span userParentSpan = TRACER.spanBuilder("user-parent-span").startSpan();
        try (final Scope scope = userParentSpan.makeCurrent()) {
            // Thread bound (sync) calls will automatically pick up the parent span and you don't need to pass it explicitly.
            client.setConfigurationSetting("hello", "text", "World");
        } finally {
            userParentSpan.end();
        }
    }
}
```

<!-- Links -->
[azure_data_app_configuration]: https://mvnrepository.com/artifact/com.azure/azure-data-appconfiguration
[opentelemetry_sdk]: https://github.com/open-telemetry/opentelemetry-java/tree/master/sdk
[logging_exporter]: https://github.com/open-telemetry/opentelemetry-java/tree/master/exporters/logging
