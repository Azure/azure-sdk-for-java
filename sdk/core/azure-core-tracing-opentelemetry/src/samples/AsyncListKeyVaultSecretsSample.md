# Asynchronously list Key Vault secrets with Azure Core Tracing OpenTelemetry
 
Following documentation describes instructions to run a sample program for asynchronously creating and listing secrets of a Key Vault with tracing instrumentation.

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

```xml
<!-- SDK dependencies   -->
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
    <version>1.0.6</version>
</dependency>
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-security-keyvault-secrets</artifactId>
    <version>4.2.0-beta.2</version>
</dependency>
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-core-tracing-opentelemetry</artifactId>
    <version>1.0.0-beta.4</version>
</dependency>
```

#### Sample demonstrates tracing when asynchronously creating and listing secrets from a Key Vault using [azure-security-keyvault-secrets][azure_keyvault_secrets] client library.
```java
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.Scope;
import io.opentelemetry.exporters.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpansProcessor;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;
import reactor.util.context.Context;

import static com.azure.core.util.tracing.Tracer.PARENT_SPAN_KEY;

/**
 * Sample demonstrates tracing how to add and list secrets in a Key Vault with tracing enabled with a Logging Exporter.
 */
public class Sample { 
  private static final Tracer TRACER = configureOpenTelemetryAndLoggingExporter();
  private static final String VAULT_URL = "<YOUR_VAULT_URL>";

  public static void main(String[] args) throws InterruptedException {
      Span userSpan = TRACER.spanBuilder("user-parent-span").startSpan();
      final Scope scope = TRACER.withSpan(userSpan);
      doClientWork();
      userSpan.end();
      scope.close();
  }

  private static Tracer configureOpenTelemetryAndLoggingExporter() {
      LoggingSpanExporter exporter = new LoggingSpanExporter();
      TracerSdkProvider tracerSdkProvider = (TracerSdkProvider) OpenTelemetry.getTracerProvider();
      tracerSdkProvider.addSpanProcessor(SimpleSpansProcessor.newBuilder(exporter).build());
      return tracerSdkProvider.get("Sample");
  }

  public static void doClientWork() throws InterruptedException {
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
              .subscribe(secretBase -> client.getSecret(secretBase.getName())
                      .subscriberContext(traceContext)
                      .subscribe(secret -> System.out.printf("Secret with name: %s%n", secret.getName())));

      Thread.sleep(10000);
  }
}
```

<!-- Links -->
[azure_keyvault_secrets]: https://mvnrepository.com/artifact/com.azure/azure-security-keyvault-secrets
[opentelemetry_sdk]: https://github.com/open-telemetry/opentelemetry-java/tree/master/sdk
[logging_exporter]: https://github.com/open-telemetry/opentelemetry-java/tree/master/exporters/logging
