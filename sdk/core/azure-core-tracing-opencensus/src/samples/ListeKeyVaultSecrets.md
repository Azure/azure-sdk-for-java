# List Key Vault Secrets with Azure Core Tracing OpenCensus 
 
Following documentation describes instructions to run a sample program for listing secrets in a Key Vault with tracing instrumentation for Java SDK libraries.

## Getting Started

### Adding the Azure client library for Key Vault secrets package to your project:
[//]: # ({x-version-update-start;com.azure:azure-security-keyvault-secrets;current})
```xml
<!-- Add KeyVault Secrets dependency without Netty HTTP client -->
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-security-keyvault-secrets</artifactId>
    <version>4.0.0</version>
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
```xml
<!-- Add OkHTTP client to use with KeyVault Secrets -->
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core-http-okhttp</artifactId>
  <version>1.0.0</version>
</dependency>
```
### Adding the Azure core tracing OpenCensus plugin package to your project:
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
public class ListOperations {
    /**
     * Authenticates with the key vault and shows how to list secrets and list versions of a specific secret in the key
     * vault with trace spans exported to Zipkin.
     *
     * Please refer to the <a href=https://zipkin.io/pages/quickstart>Quickstart Zipkin</a> for more documentation on
     * using a Zipkin exporter.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when invalid key vault endpoint is passed.
     */
    public static void main(String[] args) throws IllegalArgumentException {
        ZipkinTraceExporter.createAndRegister("http://localhost:9411/api/v2/spans", "tracing-to-zipkin-service");

        TraceConfig traceConfig = Tracing.getTraceConfig();
        TraceParams activeTraceParams = traceConfig.getActiveTraceParams();
        traceConfig.updateActiveTraceParams(activeTraceParams.toBuilder().setSampler(Samplers.alwaysSample()).build());

        Tracer tracer = Tracing.getTracer();

        // Notice that the client is using default Azure credentials. Ensure that environment variables 
        // 'AZURE_CLIENT_ID', 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' credentials to work
        // with the service principal credentials.
        SecretClient client = new SecretClientBuilder()
            .endpoint("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        Scope scope = tracer.spanBuilder("user-parent-span").startScopedSpan();
        try {

            Context traceContext = new Context(PARENT_SPAN_KEY, tracer.getCurrentSpan());
            // Let's create secrets holding storage and bank accounts credentials. if the secret
            // already exists in the key vault, then a new version of the secret is created.
            client.setSecretWithResponse(new Secret("StorageAccountPassword", "password"), traceContext);

            client.setSecretWithResponse(new Secret("BankAccountPassword", "password"), traceContext);

            // You need to check if any of the secrets are sharing same values. Let's list the secrets and print their values.
            // List operations don't return the secrets with value information. So, for each returned secret we call getSecret to get the secret with its value information.
            for (SecretProperties secret : client.listSecrets(traceContext)) {
                Secret secretWithValue = client.getSecretWithResponse(secret, traceContext).getValue();
                System.out.printf("Received secret with name %s and value %s%n", secretWithValue.getName(), secretWithValue.getValue());
            }
        } finally {
            scope.close();
            Tracing.getExportComponent().shutdown();
        }
    }
}
```
