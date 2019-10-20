# List Key Vault Secrets with Azure Core Tracing OpenCensus asynchronously
 
Following documentation describes instructions to run a sample program for listing secrets asynchronously in a Key Vault with tracing instrumentation for Java SDK libraries.

## Getting Started

### Adding the Azure client library for Key Vault secrets package to your project:
[//]: # ({x-version-update-start;com.azure:azure-security-keyvault-secrets;current})
```xml
<!-- Add KeyVault Secrets dependency without Netty HTTP client -->
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-security-keyvault-secrets</artifactId>
  <version>4.0.0-preview.5</version>
  <exclusions>
    <exclusion>
      <groupId>com.azure</groupId>
      <artifactId>azure-core-http-netty</artifactId>
    </exclusion>
  </exclusions>
</dependency>
```
[//]: # ({x-version-update-end})
[//]: # ({x-version-update-start;com.azure:azure-core-http-okhttp;current})
```
<!-- Add OkHTTP client to use with KeyVault Secrets -->
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core-http-okhttp</artifactId>
  <version>1.0.0-preview.4</version>
</dependency>
```
[//]: # ({x-version-update-end})
### Adding the Azure core tracing opencensus plugin package to your project:
[//]: # ({x-version-update-start;com.azure:azure-core-tracing-opencensus;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core-tracing-opencensus</artifactId>
  <version>1.0.0-preview.4</version>
</dependency>
```
[//]: # ({x-version-update-end})

Azure Core Tracing OpenCensus library uses the **opencensus-api** which exposes the means for recording stats or traces and propagating context. Besides recording tracing events the application would also need to link the implementation and setup exporters to gather the tracing information.
In our example we will focus on using the  **opencensus-impl** as implementation package and  **Zipkin** exporter.

### Add the dependencies to your project:

```xml
<dependency>
  <groupId>io.opencensus</groupId>
  <artifactId>opencensus-exporter-trace-zipkin</artifactId>
  <version>0.20.0</version>
</dependency>
<dependency>
  <groupId>io.opencensus</groupId>
  <artifactId>opencensus-impl</artifactId>
  <version>0.20.0</version>
</dependency>
```

Program to demonstrate publishing multiple events with tracing support:
```java
/**
 * Sample demonstrates how to list secrets and versions of a given secret in the key vault with tracing enabled.
 */
public class ListOperationsAsync {
    /**
     * Authenticates with the key vault and shows how to list secrets and list versions of a specific secret in the key
     * vault with trace spans exported to Zipkin.
     *
     * Please refer to the <a href=https://zipkin.io/pages/quickstart>Quickstart Zipkin</a> for more documentation on
     * using a Zipkin exporter.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when invalid key vault endpoint is passed.
     * @throws InterruptedException when the thread is interrupted in sleep mode.
     */
    public static void main(String[] args) throws IllegalArgumentException, InterruptedException {
        ZipkinTraceExporter.createAndRegister("http://localhost:9411/api/v2/spans", "tracing-to-zipkin-service");

        TraceConfig traceConfig = Tracing.getTraceConfig();
        TraceParams activeTraceParams = traceConfig.getActiveTraceParams();
        traceConfig.updateActiveTraceParams(activeTraceParams.toBuilder().setSampler(Samplers.alwaysSample()).build());

        Tracer tracer = Tracing.getTracer();

        // Notice that the client is using default Azure credentials. Ensure that environment variables 'AZURE_CLIENT_ID',
        // 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set to make default credentials work, with the service principal credentials.
        SecretAsyncClient client = new SecretClientBuilder()
            .endpoint("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

        Semaphore semaphore = new Semaphore(1);
        Scope scope = tracer.spanBuilder("user-parent-span").startScopedSpan();

        semaphore.acquire();
        Context traceContext = Context.of(PARENT_SPAN_KEY, tracer.getCurrentSpan());
        // Let's create secrets holding storage and bank accounts credentials. if the secret
        // already exists in the key vault, then a new version of the secret is created.
        client.setSecret(new Secret("StorageAccountPassword", "password"))
            .then(client.setSecret(new Secret("BankAccountPassword", "password")))
            .subscriberContext(traceContext)
            .subscribe(secretResponse ->
                System.out.printf("Secret is created with name %s and value %s %n", secretResponse.getName(), secretResponse.getValue()),
                err -> {
                    System.out.printf("Error thrown when enqueue the message. Error message: %s%n",
                        err.getMessage());
                    scope.close();
                    semaphore.release();
                },
                () -> {
                    semaphore.release();
                });

        semaphore.acquire();
        // You need to check if any of the secrets are sharing same values. Let's list the secrets and print their values.
        // List operations don't return the secrets with value information. So, for each returned secret we call getSecret to get the secret with its value information.
        client.listPropertiesOfSecrets()
            .subscriberContext(traceContext)
            .subscribe(secretBase -> client.getSecret(secretBase)
                .subscriberContext(traceContext)
                .subscribe(secret -> System.out.printf("Received secret with name %s and value %s%n",
                    secret.getName(), secret.getValue())));
        
        semaphore.release();
        scope.close();
        Tracing.getExportComponent().shutdown();
    }
}
```
