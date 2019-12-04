# Asynchronously list Key Vault secrets with Azure Core Tracing OpenTelemetry
 
Following documentation describes instructions to run a sample program for asynchronously creating and listing secrets of a Key Vault with tracing instrumentation.

## Getting Started
Sample uses **[opentelemetry-sdk][opentelemetry_sdk]** as implementation package and **[Logging Exporter][logging_exporter]** as exporter.
### Adding dependencies to your project:
```xml
<dependencies>
    <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-sdk</artifactId>
        <version>0.2.0</version>
    </dependency>
    <dependency>
        <groupId>com.azure</groupId>
        <artifactId>azure-security-keyvault-secrets</artifactId>
        <version>4.0.1</version> <!-- {x-version-update;com.azure:azure-security-keyvault-secrets;current} -->
    </dependency>
    <dependency>
        <groupId>com.azure</groupId>
        <artifactId>azure-core-tracing-opentelemetry</artifactId>
        <version>1.0.0-beta.1</version> <!-- {x-version-update;com.azure:azure-core-tracing-opentelemetry;current} -->
    </dependency>
    <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-exporters-logging</artifactId>
        <version>0.2.0</version>
    </dependency>
</dependencies>
```

#### Sample demonstrates tracing when asynchronously creating and listing secrets from a Key Vault using [azure-security-keyvault-secrets][azure_keyvault_secrets] client library.
```java
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.trace.TracerSdkFactory;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;
import reactor.util.context.Context;

import java.util.logging.Logger;

import static com.azure.core.util.tracing.Tracer.PARENT_SPAN_KEY;
import static java.util.logging.Logger.getLogger;

/**
 * Sample demonstrates tracing how to add and list secrets in a Key vault with tracing enabled with a Logging Exporter.
 */
public class Sample {

    private static final Logger LOGGER = getLogger("Sample");
    private static final Tracer TRACER;
    private static final TracerSdkFactory TRACER_SDK_FACTORY;

    static {
        TRACER_SDK_FACTORY = Helper.configureOpenTelemetryAndJaegerExporter(LOGGER);
        TRACER = TRACER_SDK_FACTORY.get("Sample");
    }

    public static void main(String[] args) throws InterruptedException {
        Span userSpan = TRACER.spanBuilder("user-parent-span").startSpan();
        final Scope scope = TRACER.withSpan(userSpan);
        doClientWork();
        userSpan.end();
        scope.close();
        TRACER_SDK_FACTORY.shutdown();
    }
    
    public static void doClientWork() throws InterruptedException {
        SecretAsyncClient client = new SecretClientBuilder()
            .vaultUrl("YOUR_VAULT_URL")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

        Context traceContext = Context.of(PARENT_SPAN_KEY, TRACER.getCurrentSpan());

        client.setSecret(new KeyVaultSecret("Secret1", "password1"))
            .subscriberContext(traceContext)
            .subscribe(secretResponse ->
                    LOGGER.info("Secret with name: " + secret.getName()),
                err -> {
                    LOGGER.info("Error occurred: " + err.getMessage());
                });

       client.listPropertiesOfSecrets()
            .subscriberContext(traceContext)
            .subscribe(secretBase -> client.getSecret(secretBase.getName())
                .subscriberContext(traceContext)
                .subscribe(secret -> LOGGER.info("Secret with name: " + secret.getName())));

        Thread.sleep(10000);
    }
}
```

<!-- Links -->
[azure_keyvault_secrets]: https://mvnrepository.com/artifact/com.azure/azure-security-keyvault-secrets
[opentelemetry_sdk]: https://github.com/open-telemetry/opentelemetry-java/tree/master/sdk
[logging_exporter]: https://github.com/open-telemetry/opentelemetry-java/tree/master/exporters/logging
