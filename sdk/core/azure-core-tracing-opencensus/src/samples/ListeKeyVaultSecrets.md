# List Key Vault Secrets with Azure Core Tracing OpenCensus 
 
Following documentation describes instructions to run a sample program for creating and listing secrets of a Key Vault with tracing instrumentation.

## Getting Started
Sample uses **[opencensus-impl][opencensus_impl]** as implementation package and **[ZipkinExporter][zipkin_exporter]** as exporter.

### Adding dependencies to your project:
[//]: # ({x-version-update-start;com.azure:azure-security-keyvault-secrets;current})
```xml
<!-- Add Key Vault Secrets dependency  -->
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-security-keyvault-secrets</artifactId>
    <version>4.0.1</version> <!-- {x-version-update;com.azure:azure-security-keyvault-secrets;current} -->
</dependency>
```
[//]: # ({x-version-update-end})
[//]: # ({x-version-update-start;com.azure:azure-core-tracing-opencensus;current})
```xml
<!-- Add Azure core tracing OpenCensus plugin package to your project -->
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core-tracing-opencensus</artifactId>
  <version>1.0.0-beta.5</version> <!-- {x-version-update;com.azure:azure-core-tracing-opencensus;current} -->
</dependency>
```
[//]: # ({x-version-update-end})
```xml
<!-- Add opencensus-impl and opencensus-zipkin-exporter to your project -->
<dependency>
  <groupId>io.opencensus</groupId>
  <artifactId>opencensus-exporter-trace-zipkin</artifactId>
  <version>0.24.0</version>
</dependency>
<dependency>
  <groupId>io.opencensus</groupId>
  <artifactId>opencensus-impl</artifactId>
  <version>0.24.0</version>
</dependency>
```
> All client libraries, by default, use Netty HTTP client. For adding client library dependency without netty, please follow the documentation [here][alternate_http_client].

#### Sample demonstrates tracing when creating and listing secrets from a Key Vault using [azure-security-keyvault-secrets][azure_keyvault_secrets] client library.
```java
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import io.opencensus.common.Scope;
import io.opencensus.exporter.trace.zipkin.ZipkinExporterConfiguration;
import io.opencensus.exporter.trace.zipkin.ZipkinTraceExporter;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.samplers.Samplers;
import reactor.util.context.Context;

import static com.azure.core.util.tracing.Tracer.PARENT_SPAN_KEY;

public class Sample {
    private static final Tracer TRACER = Tracing.getTracer();

    static {
      setupOpenCensusAndZipkinExporter();
    }

    public static void main(String[] args) {
        SecretClient client = new SecretClientBuilder()
            .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        try(Scope scope = TRACER.spanBuilder("user-parent-span").startScopedSpan()) {
            Context traceContext = new Context(PARENT_SPAN_KEY, TRACER.getCurrentSpan());
            
            client.setSecretWithResponse(new KeyVaultSecret("Secret1", "password1"), traceContext);
            client.setSecretWithResponse(new KeyVaultSecret("Secret2", "password2"), traceContext);

            for (SecretProperties secret : client.listPropertiesOfSecrets(traceContext)) {
                KeyVaultSecret secretWithValue = client.getSecretWithResponse(secret.getName(), "", traceContext).getValue();
                System.out.printf("Received secret with name %s and value %s%n", secretWithValue.getName(), secretWithValue.getValue());
            }
        } finally {
            Tracing.getExportComponent().shutdown();
        }
    }
    
    /**
     * Please refer to the <a href=https://zipkin.io/pages/quickstart>Quickstart Zipkin</a> for more documentation on
     * using a Zipkin exporter.
     */
    private static void setupOpenCensusAndZipkinExporter() {
        TraceConfig traceConfig = Tracing.getTraceConfig();
        traceConfig.updateActiveTraceParams(
            traceConfig.getActiveTraceParams().toBuilder().setSampler(Samplers.alwaysSample()).build());

        ZipkinExporterConfiguration configuration =
            ZipkinExporterConfiguration.builder()
                .setServiceName("sample-service")
                .setV2Url("http://localhost:9411/api/v2/spans")
                .build();

        ZipkinTraceExporter.createAndRegister(configuration);
    }
}
```

<!-- Links -->
[alternate_http_client]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/keyvault/azure-security-keyvault-secrets#alternate-http-client
[azure_keyvault_secrets]: https://mvnrepository.com/artifact/com.azure/azure-security-keyvault-secrets
[opencensus_impl]: https://mvnrepository.com/artifact/io.opencensus/opencensus-impl/
[zipkin_exporter]: https://mvnrepository.com/artifact/io.opencensus/opencensus-exporter-trace-zipkin
