# Developing the Azure Cosmos DB Kafka connector

## Prerequisites

- Git and Maven ([download](https://maven.apache.org/download.cgi)).
- Java **17 or newer** to build the connector; Java **21** is recommended. The supported test matrix is **17 and 21** only.
- An existing Docker daemon with Linux containers for Testcontainers integration tests, or Docker with Compose v2 for the optional [local demo](../docs/Confluent_Platform_Setup.md). Docker is not needed for unit tests. Do not install Docker locally just to edit/build the connector; the existing Linux CI agents run container tests.
- Bash or PowerShell 7. Cloud Shell and WSL1 are not suitable for these local container workflows.

Connector 3.x targets Kafka Connect **4.2.2**. The demo and automated worker containers use **Confluent Platform 8.2.5**; stock worker containers run **Java 21**, independently of the host JDK used to run Maven. See [2.x-to-3.x migration](../docs/migration-2-to-3.md) for compatibility, preserved state, security settings, and vendor lifecycle caveats.

## Validation

Run commands from the repository root using JDK 17, then JDK 21. These profiles use **Failsafe**; Surefire is skipped, so `mvn test` alone does not execute the connector suite. Check nonzero executed test counts under `target/failsafe-reports`, not just Maven's exit code. Keep Checkstyle, SpotBugs, and other quality rules enabled.

### Unit tests (no Docker/account required)

```powershell
mvn -f sdk\cosmos\azure-cosmos-kafka-connect\pom.xml -Pbuild-kafka,unit clean verify
```

### Integration tests (existing Testcontainers workflow)

The `kafka-integration` profile creates its own broker, Connect worker, and Schema Registry through [Testcontainers](https://testcontainers.com). It does **not** use the Compose demo. The local broker is KRaft, with Confluent 8.2.5 as the default image version. The cloud variant runs a self-managed worker against external brokers and preserves the configured broker/Schema Registry authentication.

Create `kafka-cosmos-local.properties` in your user home directory, outside source control. For tests using the local broker containers:

```properties
ACCOUNT_HOST=[emulator or Cosmos account HTTPS endpoint]
ACCOUNT_KEY=[emulator or Cosmos account key]
ACCOUNT_TENANT_ID=[tenant if service-principal authentication is required]
ACCOUNT_AAD_CLIENT_ID=[client id if required]
ACCOUNT_AAD_CLIENT_SECRET=[client secret if required]
SASL_JAAS=
BOOTSTRAP_SERVER=
CONNECT_CONFIG_STORAGE_REPLICATION_FACTOR=1
CONNECT_OFFSET_STORAGE_REPLICATION_FACTOR=1
CONNECT_STATUS_STORAGE_REPLICATION_FACTOR=1
```

For Confluent Cloud:

```properties
ACCOUNT_HOST=[emulator or Cosmos account HTTPS endpoint]
ACCOUNT_KEY=[emulator or Cosmos account key]
ACCOUNT_TENANT_ID=[tenant if service-principal authentication is required]
ACCOUNT_AAD_CLIENT_ID=[client id if required]
ACCOUNT_AAD_CLIENT_SECRET=[client secret if required]
SASL_JAAS=[JAAS configuration for the cloud Kafka cluster]
BOOTSTRAP_SERVER=[cloud broker bootstrap endpoint]
SCHEMA_REGISTRY_URL=[cloud Schema Registry URL]
SCHEMA_REGISTRY_KEY=[Schema Registry API key]
SCHEMA_REGISTRY_SECRET=[Schema Registry API secret]
CONNECT_CONFIG_STORAGE_REPLICATION_FACTOR=3
CONNECT_OFFSET_STORAGE_REPLICATION_FACTOR=3
CONNECT_STATUS_STORAGE_REPLICATION_FACTOR=3
```

See [Confluent Cloud setup](../docs/Confluent_Cloud_Setup.md). Configure only a development account with appropriate permissions and retain test resource cleanup. Do not place keys in committed files.

```powershell
mvn -f sdk\cosmos\azure-cosmos-kafka-connect\pom.xml -Pbuild-kafka,kafka-integration clean verify
```

Existing `kafka-emulator` and `kafka` pipeline profiles remain in use. Integration/image checks require a Docker-capable machine and correctly scoped Cosmos test-account configuration. Verify the actual worker Kafka/JVM versions and plugin/task health; dependency resolution alone does not verify a worker image.

## Optional local demo (manual only)

Follow [Confluent Platform setup](../docs/Confluent_Platform_Setup.md) to build the shaded 3.x connector and start the three-service KRaft demo. The Bash and PowerShell scripts propagate native-command failures and preserve the demo's named broker volume on restart. Use Connect REST and Kafka CLI, not removed UI services.

The guide includes a **manual** version/plugin/source/sink/restart verification procedure for that distinct Dockerfile installation path. No Compose CI job or release gate is added. **The manual demo was not run on the Windows implementation host because Docker is unavailable.** This is not evidence that Testcontainers/image validation passed; record those results separately on an existing Docker-capable environment.

## Further setup

- [Confluent Platform demo](../docs/Confluent_Platform_Setup.md)
- [Confluent Cloud](../docs/Confluent_Cloud_Setup.md)
- [Cosmos DB account](../docs/CosmosDB_Setup.md)
- [Sink connector quickstart](../docs/README_Sink.md)
- [Source connector quickstart](../docs/README_Source.md)
- [2.x-to-3.x migration](../docs/migration-2-to-3.md)
