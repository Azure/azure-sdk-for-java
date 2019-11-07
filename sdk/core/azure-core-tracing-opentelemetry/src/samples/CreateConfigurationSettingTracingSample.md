# Create a configuration setting with Azure Core Tracing Opentelemetry
 
Following documentation describes instructions to run a sample program for creating a Configuration Setting with tracing instrumentation.

## Getting Started
Sample uses **[opentelemetry-sdk][opentelemetry_sdk]** as implementation package and **[LoggingExporter][logging_exporter]** as exporter.
### Adding dependencies to your project:
```xml
<dependencies>
    <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-sdk</artifactId>
        <version>0.2.0-SNAPSHOT</version>
    </dependency>
    <dependency>
        <groupId>com.azure</groupId>
        <artifactId>azure-data-appconfiguration</artifactId>
        <version>1.0.0-beta.7</version>
    </dependency>
    <dependency>
        <groupId>com.azure</groupId>
        <artifactId>azure-core-tracing-opentelemetry</artifactId>
        <version>1.0.0-beta.1</version>
    </dependency>
    <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-exporters-logging</artifactId>
        <version>0.2.0-SNAPSHOT</version>
    </dependency>
</dependencies>

```
#### Sample demonstrates tracing when adding a configuration setting using [azure-data-app-configuration][azure_data_app_configuration] client library.
```java
import com.azure.core.util.Context;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.Scope;
import io.opentelemetry.exporters.logging.LoggingExporter;
import io.opentelemetry.sdk.trace.TracerSdkFactory;
import io.opentelemetry.sdk.trace.export.SimpleSpansProcessor;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;

import java.util.logging.Logger;

import static com.azure.core.util.tracing.Tracer.PARENT_SPAN_KEY;
import static java.util.logging.Logger.getLogger;

public class Sample {
    final static String CONNECTION_STRING = "<YOUR-CONNECTION_STRING";
    private static final Logger LOGGER = getLogger("Sample");
    private static  final Tracer TRACER;
    private static final TracerSdkFactory TRACER_SDK_FACTORY;

    static {
        TRACER_SDK_FACTORY = configureOpenTelemetryAndLoggingExporter();
        TRACER = TRACER_SDK_FACTORY.get("Azure-OpenTelemetry");
    }

    public static void main(String[] args) {
        doClientWork();
        TRACER_SDK_FACTORY.shutdown();
        LOGGER.info("=== Tracer Shutdown  ===");
    }

    private static TracerSdkFactory configureOpenTelemetryAndLoggingExporter() {
        LOGGER.info("=== Running With LoggingExporter ===");

        LoggingExporter exporter = new LoggingExporter();
        TracerSdkFactory tracerSdkFactory = (TracerSdkFactory) OpenTelemetry.getTracerFactory();
        tracerSdkFactory.addSpanProcessor(SimpleSpansProcessor.newBuilder(exporter).build());

        return tracerSdkFactory;
    }

    private static void doClientWork(Tracer tracer) {
        ConfigurationClient client = new ConfigurationClientBuilder()
            .connectionString(CONNECTION_STRING)
            .buildClient();

        LOGGER.info("=== Start user scoped span  ===");

        Span span = tracer.spanBuilder("user-parent-span").startSpan();
        try (final Scope scope = tracer.withSpan(span)) {
            final Context traceContext = new Context(PARENT_SPAN_KEY, tracer.getCurrentSpan());
            client.setConfigurationSettingWithResponse(new ConfigurationSetting().setKey("hello").setValue("world"), true, traceContext);
        } finally {
            span.end();
            LOGGER.info("=== End scoped span  ===");
        }
    }
}
```

<!-- Links -->
[azure_data_app_configuration]: https://mvnrepository.com/artifact/com.azure/azure-data-appconfiguration
[opentelemetry_sdk]: https://github.com/open-telemetry/opentelemetry-java/tree/master/sdk
[logging_exporter]: https://github.com/open-telemetry/opentelemetry-java/tree/master/exporters/logging
