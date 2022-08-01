# Cosmos DB client-side metrics

The Cosmos DB SDK for Java allows enabling micrometer.io metrics to track latency, request charge (RU/s), request rates etc. for logical operations (API calls into the SDK from your application/service) as well as actual requests to the Cosmos DB service (for example when due to Consistency level Strong or Bounded Staleness requests need to be sent to multiple replica). These metrics are tagged with different dimensions - so it is possible to look at metrics like latency, request charge etc. for a certain dimension like operation type, response status code etc.

**<u>Please note:</u> There is some performance overhead when enabling metrics due to increased CPU usage. The overhead is small (<10%) but should be tested before enabling it in production especially for large containers/workloads (> 5 TB of data or > 1,000,000 RU/s)** 

## How to enable metrics?

To enable client metrics a `io.micrometer.core.instrument.MeterRegistry` needs to be passed to the `CosmosClientBuilder` when creating the singleton Cosmos client.

```java
this.client = new CosmosClientBuilder()
    .endpoint("<Endpint>")
    .key("<Key>")
    .clientTelemetryConfig().clientMetrics(createConsoleLoggingMeterRegistry())
    .buildClient();
```

A sample for a `MeterRegistry` simply logging to the Console can be create like this:

```java
private static MeterRegistry createConsoleLoggingMeterRegistry() {

    final MetricRegistry dropwizardRegistry = new MetricRegistry();

    final ConsoleReporter consoleReporter = ConsoleReporter
        .forRegistry(dropwizardRegistry)
        .convertRatesTo(TimeUnit.SECONDS)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
        .build();

    consoleReporter.start(1, TimeUnit.SECONDS);

    DropwizardConfig dropwizardConfig = new DropwizardConfig() {

        @Override
        public String get(@Nullable String key) {
            return null;
        }

        @Override
        public String prefix() {
            return "console";
        }

    };

    final DropwizardMeterRegistry consoleLoggingRegistry = new DropwizardMeterRegistry(
        dropwizardConfig, dropwizardRegistry, HierarchicalNameMapper.DEFAULT, Clock.SYSTEM) {
        @Override
        protected Double nullGaugeValue() {
            return Double.NaN;
        }

        @Override
        public void close() {
            super.close();
            consoleReporter.stop();
            consoleReporter.close();
        }
    };

    consoleLoggingRegistry.config().namingConvention(NamingConvention.dot);
    return consoleLoggingRegistry;
}
```

The micrometer.io documentation has a list with samples on how to create a `MeterRegistry` for several telemetry systems: [Micrometer Application Monitoring](https://micrometer.io/docs). You can also find an implementation via an Azure Monitor `MeterRegistry` here: [Spark sample with Azure Monitor `MeterRegistry`](https://aka.ms/azure-cosmos-spark_CosmosMetricsApplicationInsightsPlugin)



## Which tags/dimensions are associated with metrics?

| Tag/dimension name                   | Sample value                                                 | Description                                                  | Scope                       |
| ------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ | --------------------------- |
| `Container`                          | `accountName / databaseName / containerName`                 | The fully qualified container name                           | operations + requests       |
| `Operation`                          | `Document / ReadFeed` or `Document / queryItems / SomeLogicalQueryIdentifier` | The operation type and for queries with optional logical operation identifier as suffix | operations + requests       |
| `OperationStatusCode`                | `200` or `429` etc.                                          | The status code of the operation reported to the app/svc (could indicate sucess `200` even after hitting errors and retyring successfully) | operations  + requests      |
| `ClientCorrelationId`                | `MyClientUsingAADAuth`                                       | An identifier of the Cosmos client instance - can be specified via the `CosmosClientBuilder. clientTelemetryConfig(). clientCorrelationId(String)` method or gets auto-generated | operations + requests       |
| `IsPayloadLargerThan1KB`             | `True` or `False`                                            | A flag indicating whether the request/response payload size exceeded 1KB (relevant to determine whether latency SLA is applicable or not) | point operations            |
| `ConsistencyLevel`                   | `Eventual`, `BoundedStaleness`, `Strong` or `Session`        | The consistency level used for the operation                 | operations + requests       |
| `PartitionKeyRangeId`                | `1`                                                          | The partition key range id - an identifier for the physical shard/partition in the backend. This can be helpful to identify whether load is skewed across physical partitions. | operations + requests       |
| `RequestStatusCode`                  | `200` or `429` etc.                                          | The status code of an individual request to the Cosmos DB Gateway or a replica | requests                    |
| `RequestOperationType`               | `Document / ReadFeed` etc.                                   | The resource type and operation type of an individual request to the Cosmos DB Gateway or replica | requests                    |
| `RegionName`                         | `West Europe`                                                | The Azure region name for the Cosmos DB Gateway or replica endpoint being called | requests                    |
| `ServiceEndpoint`                    | ***TODO add***                                               | The hostname and port of the service endpoint being called   | requests                    |
| `ServiceAddress`                     | ***TODO add***                                               | The path information allowing to determine physical partition and replica being called | requests (direct TCP/rntbd) |
| `IsForceRefresh`                     | `True` or `False`                                            | A flag indicating whether a forced address refresh is requested | address refresh requests    |
| `IsForceCollectionRoutingMapRefresh` | `True` or `False`                                            | A flag indicating whether a forced refresh of partition and collection metadata is requested | address refresh requests    |





## What metrics are emitted?



### Metrics for logical operations

```
| Name    | Supported Spark Versions | Minimum Java Version | Supported Scala Versions | Supported Databricks Runtimes |
```



### Metrics for requests to the Cosmos DB Gateway endpoint

ddd



### Metrics for communication with the Cosmos DB backend replicas via direct TCP (aka RNTBD)

ddd



## How can I ignore tags/dimensions I am not interested in?

dddd

## How can I filter which metrics I want to collect?



ddd



