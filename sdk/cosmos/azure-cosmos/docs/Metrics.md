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

The micrometer.io documentation has a list with samples on how to create a `MeterRegistry` for several telemetry systems: `https://micrometer.io/docs`. You can also find an implementation via an Azure Monitor `MeterRegistry` here: [Spark sample with Azure Monitor `MeterRegistry`](https://aka.ms/azure-cosmos-spark_CosmosMetricsApplicationInsightsPlugin)



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

| Name                             | Unit                                                      | Percentiles            | Description                                                  |
| -------------------------------- | --------------------------------------------------------- | ---------------------- | ------------------------------------------------------------ |
| cosmos.client.op.RUs             | RU (request unit)                                         | 95th, 99th + histogram | Total request units per operation (sum of RUs for all requested needed when processing an operation) |
| cosmos.client.op.latency         | duration (MeterRegistry determines default - usually ms ) | 95th, 99th + histogram | Total end-to-end duration of the operation                   |
| cosmos.client.op.maxItemCount    | #                                                         | None                   | For feed operations (query, readAll, readMany, change feed) this meter capture the requested maxItemCount per page/request |
| cosmos.client.op.actualItemCount | #                                                         | None                   | For feed operations (query, readAll, readMany, change feed) this meter capture the actual item count in responses from the service |

### Metrics for requests to the Cosmos DB Gateway endpoint

| Name                              | Unit     | Percentiles            | Description                                                |
| --------------------------------- | -------- | ---------------------- | ---------------------------------------------------------- |
| cosmos.client.req.gw.timeline.xxx | duration | 95th, 99th + histogram | Duration spent in different stages of the request pipeline |
| cosmos.client.req.reqPayloadSize  | bytes    | None                   | The request payload size in bytes                          |
| cosmos.client.req.rspPayloadSize  | bytes    | None                   | The response payload size in bytes                         |

### Metrics for communication with the Cosmos DB backend replicas via direct TCP (aka RNTBD)

| Name                                                         | Unit     | Percentiles            | Description                                                  |
| ------------------------------------------------------------ | -------- | ---------------------- | ------------------------------------------------------------ |
| cosmos.client.req.rntbd.backendLatency            | duration | 95th, 99th + histogram | Duration spent for processing the request in the Cosmos DB service endpoint (self-attested by backend) |
| cosmos.client.req.rntbd.timeline.xxx                         | duration | 95th, 99th + histogram | Duration spent in different stages of the request pipeline   |
| cosmos.client.req.reqPayloadSize                             |    bytes    | None                   | The request payload size in bytes                             |
| cosmos.client.req.rspPayloadSize                             |    bytes    | None                   | The response payload size in bytes                             |
| cosmos.client.req.rntbd.addressResolution.latency | duration | 95th, 99th + histogram | Duration spent for resolving physical addresses of replica for a certain partition |
| cosmos.client.req.rntbd.stats.channel.pendingRequestQueueSize | #        | None                   | Request queue size of a single RNTBD channel (one TCP connection to a certain service endpoint) - any value larger than 0 results in higher latency |
| cosmos.client.req.rntbd.stats.channel.channelTaskQueueSize   | #        | None                   | Total number of pending requests + requests being processed per channel |
| cosmos.client.req.rntbd.stats.endpoint.acquiredChannels      | #        | 95th, 99th + histogram | Number of new TCP connections being established per Cosmos DB service endpoint |
| cosmos.client.req.rntbd.stats.endpoint.availableChannels     | #        | None                   | Number of established TCP connections per  Cosmos DB service endpoint |
| cosmos.client.req.rntbd.stats.endpoint.inflightRequests      | #        | 95th, 99th + histogram | Number of concurrently processed requests  per Cosmos DB service endpoint |
| cosmos.client.req.rntbd.stats.endpoint.executorTaskQueueSize | #        | None                   | Number of pending tasks per Cosmos DB service endpoint       |
| cosmos.client.req.rntbd.channel.acquisition.timeline.xxx     | duration | None                   | Duration spent in different stages of grabbing an existing or creating a new channel (TCP connection) |
| cosmos.client.req.rntbd.channel.acquisition.timeline.completeNew | duration | None                   | Duration of completing SSL handshake for new TCP connection  |

### Metrics for RNTBD service endpoints (across operations, no operation-level tags)

| Name                                            | Unit     | Percentiles            | Description                                                  |
| ----------------------------------------------- | -------- | ---------------------- | ------------------------------------------------------------ |
| cosmos.client.rntbd.requests.latency            | duration | 95th, 99th + histogram | End-to-end duration of processing RNTBD requests per service endpoint across all operations |
| cosmos.client.rntbd.requests.failed.latency     | duration | 95th, 99th + histogram | End-to-end duration of failed RNTBD requests per service endpoint across all operations |
| cosmos.client.rntbd.requests.successful.latency | duration | 95th, 99th + histogram | End-to-end duration of successful RNTBD requests per service endpoint across all operations |
| cosmos.client.rntbd.req.reqSize                 | bytes    | 95th, 99th + histogram | Request payload size in RNTBD encoded bytes across requests for all operations per service endpoint |
| cosmos.client.rntbd.req.rspSize                 | bytes    | 95th, 99th + histogram | Response payload size in RNTBD encoded bytes across requests for all operations per service endpoint |
| cosmos.client.rntbd.endpoints.count             | snapshot | None                   | Current number (snapshot) of service endpoints with any active connections |
| cosmos.client.rntbd.endpoints.evicted           | snapshot | None                   | Current number (snapshot) of endpoints for which TCP connections were closed |
| cosmos.client.rntbd.requests.concurrent.count   | snapshot | None                   | Current number (snapshot) of concurrent requests handled per service endpoint |
| cosmos.client.rntbd.requests.queued.count       | snapshot | None                   | Current number (snapshot) of requests being queued (delayed due to no channel available) per service endpoint |
| cosmos.client.rntbd.channels.acquired.count     | snapshot | None                   | Current number (snapshot) of new channels (TCP connections) being acquired per service endpoint |
| cosmos.client.rntbd.channels.acquired.count     | snapshot | None                   | Current number (snapshot) of existing channels (TCP connections)  per service endpoint |

### System status  metrics (CPU, memory etc.)

| Name                                     | Unit    | Percentiles            | Description                                                  |
| ---------------------------------------- | ------- | ---------------------- | ------------------------------------------------------------ |
| cosmos.client.system.avgCpuLoad          | Percent | 95th, 99th + histogram | SDK measures avg. system-wide CPU every 5 seconds. This meter captures the 5-second avg. CPU usage measurements. |
| cosmos.client.system.freeMemoryAvailable | MB      | None                   | SDK measures free memory available for the process in MB every 5 seconds. This meter captures the 5-second measurements. |

### Request timeline stages

dddd


## How can I ignore tags/dimensions I am not interested in?

dddd

## How can I filter which metrics I want to collect?



ddd



