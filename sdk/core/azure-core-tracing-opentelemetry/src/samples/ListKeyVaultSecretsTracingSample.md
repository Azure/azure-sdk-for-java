# List key Vault secrets with Azure Core Tracing Opentelemetry
 
Following documentation describes instructions to run a sample program for creating and listing secrets of a Key Vault with tracing instrumentation.

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
        <artifactId>azure-security-keyvault-secrets</artifactId>
        <version>4.0.0</version>
    </dependency>
    <dependency>
        <groupId>com.azure</groupId>
        <artifactId>azure-core-tracing-opentelemetry</artifactId>
        <version>1.0.0-preview.1</version>
    </dependency>
    <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-exporters-logging</artifactId>
        <version>0.2.0-SNAPSHOT</version>
    </dependency>
</dependencies>

```
#### Sample demonstrates tracing when creating and listing secrets from a Key Vault using [azure_keyvault_secrets][azure_keyvault_secrets] client library.
```java
import com.azure.core.util.Context;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.trace.TracerSdkFactory;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;

import java.util.logging.Logger;

import static com.azure.core.util.tracing.Tracer.PARENT_SPAN_KEY;
import static java.util.logging.Logger.getLogger;

public class Sample {
    final static String VAULT_URL = "<YOUR_VAULT_URL>";
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
        SecretClient secretClient = new SecretClientBuilder()
            .vaultUrl(VAULT_URL)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        LOGGER.info("=== Start user scoped span  ===");

        Span span = tracer.spanBuilder("user-parent-span").startSpan();
        try (final Scope scope = TRACER.withSpan(span)) {
            secretClient.setSecretWithResponse(new KeyVaultSecret("StorageAccountPassword", "password"), Context.NONE);
            secretClient.listPropertiesOfSecrets().forEach(secretProperties -> {
                KeyVaultSecret secret = secretClient.getSecret(secretProperties.getName());
                System.out.printf("Secret with name %s retrieved %n", secret.getName());
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
