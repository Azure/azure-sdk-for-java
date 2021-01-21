# List key Vault secrets with Azure Core Tracing OpenTelemetry

Following documentation describes instructions to run a sample program for creating and listing secrets of a Key Vault with tracing instrumentation.

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
    <artifactId>azure-identity</artifactId>
    <version>1.2.2</version>
</dependency>
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-security-keyvault-secrets</artifactId>
    <version>4.2.4</version>
</dependency>
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-core-tracing-opentelemetry</artifactId>
    <version>1.0.0-beta.7</version>
</dependency>
```

#### Sample demonstrates tracing when creating and listing secrets from a Key Vault using [azure-security-keyvault-secrets][azure_keyvault_secrets] client library.
```java
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

public class Sample {
  private static final Tracer TRACER = configureOpenTelemetryAndLoggingExporter();
  private static final String VAULT_URL = "<YOUR_VAULT_URL>";

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

  private static void doClientWork() {
    SecretClient secretClient = new SecretClientBuilder()
        .vaultUrl(VAULT_URL)
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildClient();

    Span userParentSpan = TRACER.spanBuilder("user-parent-span").startSpan();
    try (final Scope scope = userParentSpan.makeCurrent()) {
        secretClient.setSecret(new KeyVaultSecret("StorageAccountPassword", "password"));
        secretClient.listPropertiesOfSecrets().forEach(secretProperties -> {
          // Thread bound (sync) calls will automatically pick up the parent span and you don't need to pass it explicitly.
          KeyVaultSecret secret = secretClient.getSecret(secretProperties.getName());
          System.out.printf("Retrieved Secret with name: %s%n", secret.getName());
        });
    } finally {
        userParentSpan.end();
    }
  }
}
```

<!-- Links -->
[azure_keyvault_secrets]: https://mvnrepository.com/artifact/com.azure/azure-security-keyvault-secrets
[opentelemetry_sdk]: https://github.com/open-telemetry/opentelemetry-java/tree/master/sdk
[logging_exporter]: https://github.com/open-telemetry/opentelemetry-java/tree/master/exporters/logging
