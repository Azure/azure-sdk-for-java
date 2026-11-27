# Confluent Platform Setup

This local development demo runs exactly three services: a combined KRaft broker/controller, Kafka Connect in distributed mode, and Schema Registry. All use Confluent Platform **8.2.5**, which includes Kafka **4.2.2**. There are no browser UI services; manage connectors with the Connect REST API and topics with the Kafka CLI.

**Do not use this demo to upgrade a production cluster.** Combined broker/controller mode, plaintext networking, single-node replication, and the fixed development cluster ID are for local experimentation only. Existing 2.x deployments must follow the [migration guide](migration-2-to-3.md), including the upstream staged ZooKeeper-to-KRaft procedure when applicable.

## Prerequisites

- Git and Maven ([download](https://maven.apache.org/download.cgi)).
- Java 17 or newer on the build host; Java 21 is recommended. Connector tests cover JDK 17 and 21.
- An existing Docker Engine/Desktop with the `docker compose` v2 plugin and Linux containers. Docker is needed only on the machine running this demo or Testcontainers tests; no local installation is required for unit tests or documentation changes.
- Bash (not Cloud Shell or WSL1), or PowerShell 7 ([download](https://learn.microsoft.com/powershell/scripting/install/installing-powershell)).
- Access to Maven dependencies, Confluent images/Hub, and the [Insert UUID SMT repository](https://github.com/confluentinc/kafka-connect-insert-uuid).
- A development [Cosmos DB account and containers](CosmosDB_Setup.md) for source/sink flows.

Allow sufficient Docker memory for all three JVMs (start with at least 4 GB). Ports 9092, 8081, and 8083 must be free. If the Cosmos DB Emulator already occupies host port 8081, change only the Schema Registry host port mapping in Compose; its container address remains `http://schema-registry:8081`.

## Startup

Run from the repository root, or call the scripts by their full path from any working directory:

```powershell
# PowerShell
pwsh -File sdk\cosmos\azure-cosmos-kafka-connect\src\docker\startup.ps1
```

```bash
# Bash
bash sdk/cosmos/azure-cosmos-kafka-connect/src/docker/startup.sh
```

The scripts build with `-Pbuild-kafka` to produce the shaded **3.x** JAR, build the Insert UUID SMT against Connect 4.2.2 at pinned revision `ea0986d8e3ae33f0889c5aafdb7e75cd224b1ecc`, and copy only these plugin JARs into separate `connectors/cosmos` and `connectors/insert-uuid` directories. The Dockerfile also installs the demo-only Datagen 0.6.6 connector. Worker Kafka dependencies are not copied into these plugin directories.

Build or native-command failures stop the scripts. They then stop only this Compose project's services, remove orphaned services from the old demo, build the Connect image, and start the new stack. They replace the Cosmos/Insert UUID plugin directories and remove the old demo's loose `azure-cosmos-kafka-connect-*.jar`/`InsertUuid-*.jar` files; other plugin directories/files are retained. Check any custom plugin directories for additional old Cosmos JARs before startup. Do not leave both 2.x and 3.x connector JARs installed.

Broker data, including Connect internal topics and Schema Registry schemas, is stored in the demo's named `broker-data` volume. Rerunning the scripts or `docker compose down` does not delete that volume. A new KRaft volume is **not** an import of the old ZooKeeper demo's state.

### Service endpoints

| Service | Host address | Container-network address |
| --- | --- | --- |
| Kafka | `localhost:9092` | `broker:29092` |
| Connect REST API | <http://localhost:8083> | `http://connect:8083` |
| Schema Registry REST API | <http://localhost:8081> | `http://schema-registry:8081` |

Published ports bind to host loopback only. Do not expose the unauthenticated demo to a shared network.

### Log4j2

Connect loads `src/docker/resources/connect-log4j2.properties` using `KAFKA_LOG4J_OPTS` and `log4j2.configurationFile`. The root level is WARN, Kafka is INFO, and `com.azure.cosmos.kafka` is DEBUG; console output includes connector context. Adjust this file and rebuild the image for persistent changes. It does not use the old monitoring interceptor classpath or a Log4j 1 configuration. See [Confluent Connect logging](https://docs.confluent.io/platform/8.2/connect/logging.html#change-log-levels) for logging and authenticated admin REST API guidance.

## Manual demo verification

This is **separate** from automated Testcontainers validation: the demo installs plugins through its Dockerfile/startup scripts, whereas tests create their own containers. Do not add the demo to CI. The manual demo was **not run on the Windows implementation host because Docker is unavailable**; syntax/reference checks alone do not establish image or task health.

On a Docker-capable machine, run these commands from `sdk/cosmos/azure-cosmos-kafka-connect/src/docker` after startup:

```bash
docker compose config
docker compose ps
docker compose images
docker compose exec -T connect java -version
docker compose exec -T broker kafka-topics --version
curl --fail --silent --show-error http://localhost:8083/
curl --fail --silent --show-error http://localhost:8083/connector-plugins
curl --fail --silent --show-error http://localhost:8081/subjects
docker compose logs connect
```

1. Verify the resolved Compose configuration contains only `broker`, `connect`, and `schema-registry`. Wait for REST readiness; `up -d` does not imply services are ready.
2. Record image tags/IDs, actual worker REST version, broker CLI version, and worker JVM. Distribution builds may report a Confluent version/suffix instead of the upstream Kafka version; correlate that runtime information with the [8.2.5 component changelog](https://docs.confluent.io/platform/8.2/release-notes/changelog.html#version-8-2-5) to confirm Kafka 4.2.2, rather than inferring it from Maven resolution. Stock Confluent 8.2 images run Java 21 even when Maven ran on host JDK 17. Verify source and sink plugin discovery reports version `3.0.0-beta.1`; also verify `io.confluent.kafka.connect.datagen.DatagenConnector` is present.
3. Follow the [source](README_Source.md) and [sink](README_Sink.md) quickstarts. Check `/connectors/<name>/status` for both a RUNNING connector and RUNNING tasks, then confirm a new Cosmos DB document reaches Kafka and a Kafka record reaches Cosmos DB.
4. To exercise Insert UUID, use a separate sink workload whose input lacks an `id`, configure the [SMT](README_Sink.md#using-the-insertuuid-smt-to-automatically-add-item-ids), and confirm an item with a generated `id` arrives in Cosmos DB. The connector plugin listing alone does not verify an SMT. If using Avro, configure `io.confluent.connect.avro.AvroConverter` with `value.converter.schema.registry.url=http://schema-registry:8081` and verify schema registration and a round trip.
5. Restart only Connect (`docker compose restart connect`), wait for RUNNING tasks, and verify continued source/sink progress without resetting offsets or deleting metadata. Account for at-least-once replay. Record commands, versions, task states, and observed data flow; do not record account keys or credentials.

Use the [Connect REST reference](https://docs.confluent.io/platform/8.2/connect/references/restapi.html) for configuration, status, pause/resume, and deletion.

## Cleanup

Stop only resources from this demo, from its `src/docker` directory:

```bash
docker compose down --remove-orphans
```

This retains the demo volume. Only for an intentionally disposable reset, after backing up any desired data, use `docker compose down --volumes --remove-orphans`; it permanently removes this demo's Kafka records, schemas, configurations, and offsets. Never use a global Docker prune command. Delete only the Cosmos DB resources you created for the quickstarts, following [Cosmos DB cleanup](CosmosDB_Setup.md#cleanup).
