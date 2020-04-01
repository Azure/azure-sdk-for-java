# List key Vault secrets with Azure Core Tracing OpenTelemetry
 
Following documentation describes instructions to run a sample program for creating and listing secrets of a Key Vault with tracing instrumentation.

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

[//]: # ({x-version-update-start;com.azure:azure-security-keyvault-secrets;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-security-keyvault-secrets</artifactId>
    <version>4.2.0-beta.1</version>
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

#### Sample demonstrates tracing when creating and listing secrets from a Key Vault using [azure-security-keyvault-secrets][azure_keyvault_secrets] client library.
```java
import com.azure.core.util.Context;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;

import java.util.logging.Logger;

import static com.azure.core.util.tracing.Tracer.PARENT_SPAN_KEY;
import static java.util.logging.Logger.getLogger;

public class Sample {
    final static String VAULT_URL = "<YOUR_VAULT_URL>";
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
        SecretClient secretClient = new SecretClientBuilder()
            .vaultUrl("VAULT_URL")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        Span span = tracer.spanBuilder("user-parent-span").startSpan();
        try (final Scope scope = TRACER.withSpan(span)) {
            secretClient.setSecret(new KeyVaultSecret("StorageAccountPassword", "password"));
            secretClient.listPropertiesOfSecrets().forEach(secretProperties -> {
                KeyVaultSecret secret = secretClient.getSecret(secretProperties.getName());
                LOGGER.info("Retrieved Secret with name: ", secret.getName());
            });
        } finally {
            span.end();
        }
    }
}
```

<!-- Links -->
[azure_keyvault_secrets]: https://mvnrepository.com/artifact/com.azure/azure-security-keyvault-secrets
[opentelemetry_sdk]: https://github.com/open-telemetry/opentelemetry-java/tree/master/sdk
[logging_exporter]: https://github.com/open-telemetry/opentelemetry-java/tree/master/exporters/logging
