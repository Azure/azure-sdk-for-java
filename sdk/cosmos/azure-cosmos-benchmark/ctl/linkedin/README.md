## Enable Client Telemetry

* Enable the flag through `CosmosClientBuilder`.
```java
CosmosClient cosmosClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .directMode()
            .clientTelemetryEnabled(true)
            .buildClient();
```

* Set the system property either through code - `System.setProperty("COSMOS.CLIENT_TELEMETRY_ENDPOINT", endpoint)` or through JVM arguments - `-DCOSMOS.CLIENT_TELEMETRY_ENDPOINT=$endpoint`

* Usage of Client Telemetry Endpoint in various environments:
  * **Production Endpoint:** https://tools.cosmos.azure.com/api/clienttelemetry/trace _(recommended)_
  * **Stage Endpoint :** https://tools-staging.cosmos.azure.com/api/clienttelemetry/trace/ _(recommended for local Testing)_
  * **Test Endpoint :** https://juno-test.documents-dev.windows-int.net/api/clienttelemetry/trace/ _(Allows only dogfood accounts i.e. https://df.onecloud.azure-test.net/)_
