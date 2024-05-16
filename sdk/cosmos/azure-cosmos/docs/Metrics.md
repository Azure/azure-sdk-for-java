# Cosmos DB client-side metrics

The Cosmos DB SDK for Java allows enabling micrometer.io metrics to track latency, request charge (RU/s), request rates etc. for logical operations (API calls into the SDK from your application/service) as well as actual requests to the Cosmos DB service (for example when due to Consistency level Strong or Bounded Staleness requests need to be sent to multiple replica). These metrics are tagged with different dimensions - so it is possible to look at metrics like latency, request charge etc. for a certain dimension like operation type, response status code etc.

**<u>Please note:</u> There is some performance overhead when enabling metrics due to increased CPU usage. The overhead is small (<10%) but should be tested before enabling it in production especially for large containers/workloads (> 5 TB of data or > 1,000,000 RU/s)** 

## How to enable metrics?

To enable client metrics a `io.micrometer.core.instrument.MeterRegistry` needs to be passed to the `CosmosClientBuilder` when creating the singleton Cosmos client.

```java
CosmosClientTelemetryConfig telemetryConfig = new CosmosClientTelemetryConfig()
    .metricsOptions(new CosmosMicrometerMetricsOptions().meterRegistry(createConsoleLoggingMeterRegistry()));

this.client = new CosmosClientBuilder()
    .endpoint("<Endpint>")
    .key("<Key>")
    .clientTelemetryConfig(telemetryConfig)
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

| Tag/dimension name                   | Sample value                                                                                                                                                 | Description                                                                                                                                                                                                                                                  | Scope                       |
|--------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------| --------------------------- |
| `Container`                          | `accountName / databaseName / containerName`                                                                                                                 | The fully qualified container name                                                                                                                                                                                                                           | operations + requests       |
| `Operation`                          | `Document / ReadFeed` or `Document / queryItems / SomeLogicalQueryIdentifier`                                                                                | The operation type and for queries with optional logical operation identifier as suffix                                                                                                                                                                      | operations + requests       |
| `OperationStatusCode`                | `200` or `429` etc.                                                                                                                                          | The status code of the operation reported to the app/svc (could indicate sucess `200` even after hitting errors and retyring successfully)                                                                                                                   | operations  + requests      |
| `ClientCorrelationId`                | `MyClientUsingAADAuth`                                                                                                                                       | An identifier of the Cosmos client instance - can be specified via the `CosmosClientBuilder. clientTelemetryConfig(). clientCorrelationId(String)` method or gets auto-generated                                                                             | operations + requests       |
| `ConsistencyLevel`                   | `Eventual`, `ConsistentPrefix`, `BoundedStaleness`, `Strong` or `Session`                                                                                    | The consistency level used for the operation  <br /><br />**Disabled by default**                                                                                                                                                                            | operations + requests       |
| `PartitionKeyRangeId`                | `1`                                                                                                                                                          | The partition key range id - an identifier for the physical shard/partition in the backend. This can be helpful to identify whether load is skewed across physical partitions.<br /><br />**Disabled by default**                                            | operations + requests       |
| `RequestStatusCode`                  | `200` or `429` etc.                                                                                                                                          | The status code of an individual request to the Cosmos DB Gateway or a replica                                                                                                                                                                               | requests                    |
| `RequestOperationType`               | `Document / ReadFeed` etc.                                                                                                                                   | The resource type and operation type of an individual request to the Cosmos DB Gateway or replica                                                                                                                                                            | requests                    |
| `RegionName`                         | `West Europe`                                                                                                                                                | The Azure region name for the Cosmos DB Gateway or replica endpoint being called                                                                                                                                                                             | requests                    |
| `ServiceEndpoint`                    | cdb-ms-prod-westeurope1-fd39.documents.azure.com_14050                                                                                                       | The hostname and port of the service endpoint being called<br /><br />**Disabled by default**                                                                                                                                                                | requests                    |
| `ServiceAddress`                     | /apps/f88bfdf4-2954-4324-aad3-f1686668076d/services/3359112a-719d-474e-aa51-e89a142ae1b3/partitions/512fe816-24fa-4fbb-bbb1-587d2ce19851/replicas/133038444008943156p/ | The path information allowing to determine physical partition and replica being called                                                                                                                                                                       | requests (direct TCP/rntbd) |
| `IsForceRefresh`                     | `True` or `False`                                                                                                                                            | A flag indicating whether a forced address refresh is requested<br /><br />**Disabled by default**                                                                                                                                                           | address refresh requests    |
| `IsForceCollectionRoutingMapRefresh` | `True` or `False`                                                                                                                                            | A flag indicating whether a forced refresh of partition and collection metadata is requested<br /><br />**Disabled by default**                                                                                                                              | address refresh requests    |
| `PartitionId`                        | `512fe816-24fa-4fbb-bbb1-587d2ce19851`                                                                                                              | The unique identifier of a physical partition - can be used in addition aor as alternative for `ServiceAddress`.  <br /><br />**Disabled by default**                                                                                                        | requests (direct TCP/rntbd)   |
| `ReplicaId`                          | `133038444008943156p`                                                                                                              | The replica identifier with a suffix - `p` (primary/write) or `s` (seocndary/read-only) - indicating whther it is a read-only replica or not. This tag can be used in addition aor as alternative for `ServiceAddress`.  <br /><br />**Disabled by default** | requests (direct TCP/rntbd)    |





## What metrics are emitted?



### Metrics for logical operations

| Name                              | Unit                                                      | Default Percentiles    | Description                                                                                                                                         |
|-----------------------------------|-----------------------------------------------------------|------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------|
| cosmos.client.op.calls            | #  calls                                                  | None                   | Number of operation calls                                                                                                                           |
| cosmos.client.op.RUs              | RU (request unit)                                         | 95th, 99th + histogram | Total request units per operation (sum of RUs for all requested needed when processing an operation)                                                |
| cosmos.client.op.latency          | duration (MeterRegistry determines default - usually ms ) | 95th, 99th + histogram | Total end-to-end duration of the operation                                                                                                          |
| cosmos.client.op.maxItemCount     | #                                                         | None                   | For feed operations (query, readAll, readMany, change feed) and batch operations this meter capture the requested maxItemCount per page/request     |
| cosmos.client.op.actualItemCount  | #                                                         | None                   | For feed operations (query, readAll, readMany, change feed) batch operations this meter capture the actual item count in responses from the service |
| cosmos.client.op.regionscontacted | # regions                                                 | None                   | Number of regions contacted when executing an operation                                                                                             |

### Metrics for requests to the Cosmos DB Gateway endpoint

| Name                                 | Unit        | Default Percentiles    | Description                                                |
|--------------------------------------|-------------|------------------------|------------------------------------------------------------|
| cosmos.client.req.gw.requests        | # requests  | None                   | Number of requests                                         |
| cosmos.client.req.gw.latency         | duration    | 95th, 99th + histogram | End-to-end duration spent for processing the request       |
| cosmos.client.req.gw.timeline.xxx    | duration    | 95th, 99th + histogram | Duration spent in different stages of the request pipeline |
| cosmos.client.req.gw.actualItemCount | #           | None                   | For feed operations (query, readAll, readMany, change feed) and batch operations this meter capture the actual item count in responses from the service                                      |
| cosmos.client.req.reqPayloadSize     | bytes       | None                   | The request payload size in bytes                          |
| cosmos.client.req.rspPayloadSize     | bytes       | None                   | The response payload size in bytes                         |

### Metrics for communication with the Cosmos DB backend replicas via direct TCP (aka RNTBD)

| Name                                                     | Unit        | Percentiles            | Description                                                                                                                                                                                  |
|----------------------------------------------------------|-------------|------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| cosmos.client.req.rntbd.requests                         | # requests  | None                   | Number of requests                                                                                                                                                                           |
| cosmos.client.req.rntbd.latency                          | duration    | 95th, 99th + histogram | End-to-end duration spent for processing the request                                                                                                                                         |
| cosmos.client.req.rntbd.backendLatency                   | duration    | 95th, 99th + histogram | Duration spent for processing the request in the Cosmos DB service endpoint (self-attested by backend)                                                                                       |
| cosmos.client.req.rntbd.timeline.xxx                     | duration    | 95th, 99th + histogram | Duration spent in different stages of the request pipeline                                                                                                                                   |
| cosmos.client.req.rntbd.actualItemCount                  | #           | None                   | For feed operations (query, readAll, readMany, change feed) and batch operations this meter capture the actual item count in responses from the service                                      |
| cosmos.client.req.reqPayloadSize                         | bytes       | None                   | The request payload size in bytes                                                                                                                                                            |
| cosmos.client.req.rspPayloadSize                         | bytes       | None                   | The response payload size in bytes                                                                                                                                                           |
| cosmos.client.req.rntbd.addressResolution.requests       | # requests  | None                   | Number of physical address resolution requests of replica for a certain partition                                                                                                            |
| cosmos.client.req.rntbd.addressResolution.latency        | duration    | 95th, 99th + histogram | Duration spent for resolving physical addresses of replica for a certain partition                                                                                                           |
| cosmos.client.req.rntbd.stats.endpoint.acquiredChannels  | #           | None                   | Number of actively used TCP connections per Cosmos DB service endpoint                                                                                                                       |
| cosmos.client.req.rntbd.stats.endpoint.availableChannels | #           | None                   | Number of established TCP connections per  Cosmos DB service endpoint that are not actively used. The total number of established connections would be availableChannels + acquiredChannels. |
| cosmos.client.req.rntbd.stats.endpoint.inflightRequests  | #           | 95th, 99th + histogram | Number of concurrently processed requests  per Cosmos DB service endpoint                                                                                                                    |

### Metrics for RNTBD service endpoints (across operations, no operation-level tags)

| Name                                            | Unit       | Percentiles            | Description                                                                                                   |
|-------------------------------------------------|------------| ---------------------- |---------------------------------------------------------------------------------------------------------------|
| cosmos.client.rntbd.requests                    | duration   | 95th, 99th + histogram | End-to-end duration of processing RNTBD requests per service endpoint across all operations                   |
| cosmos.client.rntbd.requests.latency            | duration   | 95th, 99th + histogram | End-to-end duration of processing RNTBD requests per service endpoint across all operations                   |
| cosmos.client.rntbd.requests.failed.latency     | duration   | 95th, 99th + histogram | End-to-end duration of failed RNTBD requests per service endpoint across all operations                       |
| cosmos.client.rntbd.requests.successful.latency | duration   | 95th, 99th + histogram | End-to-end duration of successful RNTBD requests per service endpoint across all operations                   |
| cosmos.client.rntbd.req.reqSize                 | bytes      | 95th, 99th + histogram | Request payload size in RNTBD encoded bytes across requests for all operations per service endpoint           |
| cosmos.client.rntbd.req.rspSize                 | bytes      | 95th, 99th + histogram | Response payload size in RNTBD encoded bytes across requests for all operations per service endpoint          |
| cosmos.client.rntbd.endpoints.count             | snapshot   | None                   | Current number (snapshot) of service endpoints with any active connections                                    |
| cosmos.client.rntbd.endpoints.evicted           | snapshot   | None                   | Current number (snapshot) of endpoints for which TCP connections were closed                                  |
| cosmos.client.rntbd.requests.concurrent.count   | snapshot   | None                   | Current number (snapshot) of concurrent requests handled per service endpoint                                 |
| cosmos.client.rntbd.requests.queued.count       | snapshot   | None                   | Current number (snapshot) of requests being queued (delayed due to no channel available) per service endpoint |
| cosmos.client.rntbd.channels.acquired.count     | snapshot   | None                   | Current number (snapshot) of newly established channels (TCP connections)  per service endpoint               |
| cosmos.client.rntbd.channels.available.count    | snapshot   | None                   | Current number (snapshot) of established channels (TCP connections)  per service endpoint                     |
| cosmos.client.rntbd.channels.closed.count       | snapshot   | None                   | Current number (snapshot) of closed channels (TCP connections)  per service endpoint                          |

### System status  metrics (CPU, memory etc.)

| Name                                     | Unit    | Percentiles            | Description                                                  |
| ---------------------------------------- | ------- | ---------------------- | ------------------------------------------------------------ |
| cosmos.client.system.avgCpuLoad          | Percent | 95th, 99th + histogram | SDK measures avg. system-wide CPU every 5 seconds. This meter captures the 5-second avg. CPU usage measurements. |
| cosmos.client.system.freeMemoryAvailable | MB      | None                   | SDK measures free memory available for the process in MB every 5 seconds. This meter captures the 5-second measurements. |

### Request timeline stages

Metrics are available for other stages in the request pipeline as well - but the following stages will be of special interest for monitoring applications:

- channelAcquisitionStarted: Higher values indicate that no channel (TCP connection) is available and a new one needs to be created - either because there is no open connection for the requested replica or because the number of concurrent requests is so high that another connection is needeed
- transitTime: This is the duration between first byte sent and last byte received - so the time spent in the backend for processing the request + the network turnaround-time. High value with high variance to the backend-reported latency usually indicate either network problems or some resource constraint on the client (or network component like SNAT ports)
- pipelined: is the time the requests waits because too many concurrent requests are being processed on a channel (TCP connection). Constantly high values here would indicate that the capacity of the application/service isn't sufficient and would need to be scaled-out.
- The other pipeline stages are not expected to have regularly high values - any high values usually would indicate either some thread-pool issue on the client or client overload. Best way to double check usually is looking whether high values happen on only single (few) client machines or all of them - and what the CPU utilization (max) is at the time in question.




## How can I ignore tags/dimensions I am not interested in?

See the `MeterFilter.ignoreTags` documentation in the micrometer documentation here - `https://micrometer.io/docs/concepts#_transforming_metrics`



## How can I filter which metrics I want to collect?

See the `MeterFilter.accceptXXX/denyXXX` documentation in the micrometer documentation here - `https://micrometer.io/docs/concepts#_convenience_methods`

