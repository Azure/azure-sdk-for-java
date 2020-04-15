# Create a configuration setting with Azure Core Tracing OpenTelemetry
 
Following documentation describes instructions to run a sample program for creating a Configuration Setting with tracing instrumentation.

## Getting Started
Sample uses **[opentelemetry-sdk][opentelemetry_sdk]** as implementation package and **[Logging Exporter][logging_exporter]** as exporter.
### Adding dependencies to your project:
```xml
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-sdk</artifactId>
    <version>0.2.4</version>
</dependency>
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-exporters-logging</artifactId>
    <version>0.2.4</version>
</dependency>
```

[//]: # ({x-version-update-start;com.azure:azure-data-appconfiguration;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-data-appconfiguration</artifactId>
    <version>1.2.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})
[//]: # ({x-version-update-start;com.azure:azure-core-tracing-opentelemetry;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-core-tracing-opentelemetry</artifactId>
    <version>1.0.0-beta.4</version>
</dependency>
```
[//]: # ({x-version-update-end})

#### Sample demonstrates tracing when adding a configuration setting using [azure-data-app-configuration][azure_data_app_configuration] client library.
```java
import com.azure.core.util.Context;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;

import java.util.logging.Logger;

import static com.azure.core.util.tracing.Tracer.PARENT_SPAN_KEY;
import static java.util.logging.Logger.getLogger;

public class Sample {
    final static String CONNECTION_STRING = "<YOUR-CONNECTION_STRING>";
    private static final Logger LOGGER = getLogger("Sample");
    private static  final Tracer TRACER;
    private static final TracerSdkProvider TRACER_SDK_PROVIDER;

    static {
        TRACER_SDK_PROVIDER = configureOpenTelemetryAndLoggingExporter();
        TRACER = TRACER_SDK_PROVIDER.get("Sample");
    }

    public static void main(String[] args) {
        doClientWork();
        TRACER_SDK_PROVIDER.shutdown();
    }

    private static TracerSdkProvider configureOpenTelemetryAndLoggingExporter() {
        LoggingExporter exporter = new LoggingExporter();
        TracerSdkProvider tracerSdkProvider = (TracerSdkProvider) OpenTelemetry.getTracerFactory();
        tracerSdkProvider.addSpanProcessor(SimpleSpansProcessor.newBuilder(exporter).build());

        return tracerSdkProvider;
    }

    private static void doClientWork() {
        ConfigurationClient client = new ConfigurationClientBuilder()
            .connectionString(CONNECTION_STRING)
            .buildClient();

        Span span = TRACER.spanBuilder("user-parent-span").startSpan();
        try (final Scope scope = TRACER.withSpan(span)) {
            final Context traceContext = new Context(PARENT_SPAN_KEY, span);
            client.setConfigurationSettingWithResponse(new ConfigurationSetting().setKey("hello").setValue("world"), true, traceContext);
        } finally {
            span.end();
        }
    }
}
```

<!-- Links -->
[azure_data_app_configuration]: https://mvnrepository.com/artifact/com.azure/azure-data-appconfiguration
[opentelemetry_sdk]: https://github.com/open-telemetry/opentelemetry-java/tree/master/sdk
[logging_exporter]: https://github.com/open-telemetry/opentelemetry-java/tree/master/exporters/logging
