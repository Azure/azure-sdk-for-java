# Asynchronously list Key Vault secrets with Azure Core Tracing OpenTelemetry

Following documentation describes instructions to run a sample program for asynchronously creating and listing secrets of a Key Vault with tracing instrumentation.

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

#### Sample demonstrates tracing when asynchronously creating and listing secrets from a Key Vault using [azure-security-keyvault-secrets][azure_keyvault_secrets] client library.
```java
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import reactor.util.context.Context;

import static com.azure.core.util.tracing.Tracer.PARENT_SPAN_KEY;

/**
 * Sample demonstrates tracing how to add and list secrets in a Key Vault with tracing enabled with a Logging Exporter.
 */
public class Sample {
  // Get the Tracer Provider
  private static final Tracer TRACER = configureOpenTelemetryAndLoggingExporter();
  private static final String VAULT_URL = "<YOUR_VAULT_URL>";

  public static void main(String[] args) {
      Span userSpan = TRACER.spanBuilder("user-parent-span").startSpan();
      final Scope scope = userSpan.makeCurrent();
      doClientWork();
      userSpan.end();
      scope.close();
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
      SecretAsyncClient client = new SecretClientBuilder()
              .vaultUrl(VAULT_URL)
              .credential(new DefaultAzureCredentialBuilder().build())
              .buildAsyncClient();

      Context traceContext = Context.of(PARENT_SPAN_KEY, TRACER.getCurrentSpan());

      client.setSecret(new KeyVaultSecret("Secret1", "password1"))
              .subscriberContext(traceContext)
              .subscribe(secretResponse -> System.out.printf("Secret with name: %s%n", secretResponse.getName()));
      client.listPropertiesOfSecrets()
              .subscriberContext(traceContext)
              .doOnNext(secretBase -> client.getSecret(secretBase.getName())
                      .subscriberContext(traceContext)
                      .doOnNext(secret -> System.out.printf("Secret with name: %s%n", secret.getName())))
                      .blockLast();
          
  }
}
```

<!-- Links -->
[azure_keyvault_secrets]: https://mvnrepository.com/artifact/com.azure/azure-security-keyvault-secrets
[opentelemetry_sdk]: https://github.com/open-telemetry/opentelemetry-java/tree/master/sdk
[logging_exporter]: https://github.com/open-telemetry/opentelemetry-java/tree/master/exporters/logging
