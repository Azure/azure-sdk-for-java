# Migrating the Cosmos DB Kafka connector from 2.x to 3.x

Connector **3.0.0-beta.1** starts the new 3.x release line. It raises the worker/Java baseline without intentionally changing connector configuration, data formats, source partitions/offsets, or metadata. Validate the upgrade in a non-production environment before changing workers that process production data.

## Compatibility and deployment boundaries

| Component | Connector 2.x (legacy line) | Connector 3.x |
| --- | --- | --- |
| Maven coordinates | `com.azure.cosmos.kafka:azure-cosmos-kafka-connect` | Unchanged; first release `3.0.0-beta.1` |
| Kafka Connect API/worker target | Kafka Connect 3.x; consult the particular 2.x release | Kafka Connect **4.2.2**, minimum validated target |
| Pinned distribution for demo/test workers | Previous 7.x Confluent images | Confluent Platform **8.2.5**, including Kafka 4.2.2 |
| Connector build/worker Java baseline | Java 8 connector bytecode; worker requirements vary by distribution | Java **17** minimum; Java **21** recommended |
| Connector build/test JDKs | Previous release's matrix | **17 and 21** |
| Broker cluster | Governed by the worker/client and vendor compatibility rules | Governed by the worker/client and vendor compatibility rules; not upgraded by the connector JAR |

Kafka Connect is a separate service from both the connector plugin and the Kafka broker. The Maven `connect-api:4.2.2` dependency has **provided** scope: the worker supplies it. `connect-runtime:4.2.2` and the connector's Log4j dependencies have **test** scope and are not a shipped worker. Replacing the Cosmos connector JAR therefore does **not** patch worker, broker, or container vulnerabilities.

The connector uses the shared SDK **Jackson 2.18.12** security patch, not a connector-specific Jackson version. Its test modules use that same version to avoid mixed-version serializer failures. The worker image's bundled Jackson is independent of the SDK's shaded dependencies: do not install the SDK's Jackson separately into the worker or change other plugins' dependencies. No production Java API change or connector configuration/state-format migration is intended.

A host JDK used by Maven is also distinct from the JVM inside a Connect container. Stock Confluent 8.2 images use Java 21; selecting host JDK 17 for a test does not change that container JVM. Consult [Kafka Java compatibility](https://kafka.apache.org/42/getting-started/compatibility/) and [Confluent supported versions and interoperability](https://docs.confluent.io/platform/current/installation/versions-interoperability.html) for the complete deployment requirements. Do not infer that every broker cluster must move to Kafka 4 merely to load this connector.

### What remains unchanged

- Source class: `com.azure.cosmos.kafka.connect.CosmosSourceConnector`.
- Sink class: `com.azure.cosmos.kafka.connect.CosmosSinkConnector`.
- Existing [configuration keys](configuration-reference.md), converters' configured wire formats, and topic-to-container mappings.
- Source partition identities, change feed continuation state, and existing offset/metadata formats.

No connector class rename or configuration migration is intended. A connector name ending in `-v2` is an **identity**, not a runtime version; retain that name when upgrading an existing deployment.

## State-preserving upgrade

1. **Inventory and back up.** Record the worker JVM/distribution, every worker's `plugin.path`, connector configurations/names, converters, SMTs, custom REST extensions, authentication callbacks, and configuration providers. Obtain original configurations from your secure configuration/secret store: REST responses may mask password fields and are not a complete secret backup. Record topic settings/ACLs and take operator-supported backups of Kafka/Connect state and Cosmos metadata. Protect credentials; do not commit exports.
2. **Retain identities and state.** Keep the same broker cluster, worker `group.id`, `config.storage.topic`, `offset.storage.topic`, and `status.storage.topic`. Preserve sink consumer groups (by default based on the connector name), any configured `consumer.override.group.id`, committed offsets, topic subscriptions, source partitions, and `azure.cosmos.source.metadata.storage.type`/`azure.cosmos.source.metadata.storage.name`. Preserve the existing Kafka metadata topic or Cosmos metadata container, database/container identities, and change feed continuation information.
3. **Pause and drain.** Quiesce upstream producers where practical, pause connectors using `PUT /connectors/<name>/pause`, wait for task transitions and in-flight work/offset commits, and capture source positions and sink consumer lag. Stop workers gracefully. Arrange a maintenance window or a vendor-supported rolling procedure; do not assume an arbitrary mixed Kafka 3/4 worker cluster is supported.
4. **Upgrade the worker environment.** Install a compatible Java 17+ runtime and Kafka Connect 4.2.2 worker (the pinned distribution is Confluent 8.2.5). Check all other plugins, SMTs, converters, and extensions against the new worker before restarting. Preserve the worker identity, security settings, plugin paths, and internal topics from step 2.
5. **Replace only the plugin.** Place the shaded 3.x connector JAR in an isolated plugin directory on **every** worker. Remove the old Cosmos connector JAR; do not install both versions side by side or copy Kafka runtime/test logging JARs into that directory. Retain compatible SMTs and converters in their own plugin directories. Do not recreate existing connectors.
6. **Restart and verify before resuming.** Check actual JVM and worker versions from startup logs and `GET /`. Check `GET /connector-plugins` on every worker for the unchanged source/sink class names and expected connector version. Compare configurations with the saved originals (allowing masked secrets), confirm tasks have no class-loading, authentication, or converter errors, then `PUT /connectors/<name>/resume`.
7. **Verify catch-up and continuity.** Inspect `GET /connectors/<name>/status` for RUNNING connectors **and tasks**. Compare source resume positions/change feed progress and sink committed offsets/lag with the pre-upgrade capture. Write a uniquely identifiable source document and sink record and verify both directions. Restart a worker once and confirm continued progress with the same state.

**Do not delete/recreate internal topics, metadata containers, connectors, or offsets; do not reset topics or change `azure.cosmos.source.changeFeed.startFrom` as an upgrade step.** Source and sink processing can replay records around a restart because delivery is at least once. Plan for duplicate processing and idempotent writes; changing IDs (for example generating a new UUID on replay) can create duplicate Cosmos items. Confirm catch-up rather than assuming no duplicates or claiming exactly-once delivery.

The [local demo](Confluent_Platform_Setup.md) is a fresh development cluster and has a separate manual verification procedure. It is not a substitute for preserving and validating an existing 2.x deployment's state.

## Upstream worker and broker changes

Review the [Kafka 4.0 migration/removals](https://kafka.apache.org/40/getting-started/upgrade/) and [Kafka 4.2.2 upgrade notes](https://kafka.apache.org/42/getting-started/upgrade/#upgrading-to-422), not only this connector's dependency list:

- **Custom plugins and REST extensions:** Kafka 4 runtime changes include Jakarta REST/Jersey 3 and Jetty 12 rather than the older `javax` stack. Rebuild or replace incompatible extensions and inspect each plugin's dependency isolation. The same applies to custom converters/SMTs; preserving their class names does not establish binary compatibility.
- **OAuth and JAAS:** Review Kafka 4 callback class changes, configured token/JWKS URLs, `org.apache.kafka.sasl.oauthbearer.allowed.urls`, and the login-module allowlists/denylist. Kafka 4's OAuth URL allowlist defaults to deny. Allow only the exact required trusted endpoints; do not disable these protections globally to get an old plugin running. Worker-to-broker OAuth and Cosmos account authentication are different settings.
- **Logging:** Confluent 8.2 uses Log4j2; migrate any custom Log4j 1 properties, bindings, or startup arguments. Use the worker's logging framework instead of bundling another binding in the connector plugin. See [Connect logging](https://docs.confluent.io/platform/8.2/connect/logging.html).
- **Admin API authentication:** Kafka 4.2.2 fixes the separate admin listener REST-extension bypass (CVE-2026-115644) and configuration-provider validation exposure (CVE-2026-116593). Confluent 8.2.5 also applies admin authentication/authorization fixes. Scripts that previously called a separate `/admin/loggers` listener without credentials may now require them. Secure both main and admin listeners, and check configuration-provider permissions.

If brokers must be upgraded, follow the [upstream staged ZooKeeper-to-KRaft migration](https://docs.confluent.io/platform/8.2/migrate-zk-kraft.html) while still on a supported migration-capable release, then follow the vendor's broker upgrade order. Kafka 4/Confluent 8 no longer support ZooKeeper. **Do not replace a production ZooKeeper broker image with the demo's Kafka 4 image or use `docker compose down --volumes` as a migration procedure.**

### Residual broker security configuration

Kafka **4.2.2 is not universally CVE-free**. Scan the resolved dependencies and actual worker/broker images and reconcile applicable [Apache advisories](https://kafka.apache.org/community/cve-list/). Connector API updates do not remediate customer-managed services.

- **CVE-2026-110952:** For brokers using the JWKS-backed SASL/OAUTHBEARER validator, explicitly configure both `sasl.oauthbearer.expected.issuer` (the provider's exact issuer) and `sasl.oauthbearer.expected.audience` (the intended audience) for every applicable listener/mechanism. The secure-by-default requirement is introduced in Kafka 4.4.0, not the pinned 4.2.2 target. Also use a signature-verifying validator and the appropriate trusted JWKS endpoint.
- **CVE-2026-114349:** Upgrade affected brokers and set a bounded `max.decompressed.message.bytes` at the broker/topic level appropriate to legitimate decompressed records and available heap. The effectively unlimited default remains after upgrading; `max.message.bytes` and quotas alone are not enough. The development demo uses a 10 MiB bound as an example, not a production sizing recommendation.

These are **broker-operator settings**, not `azure.cosmos.*` connector properties. Apply listener prefixes as required by the chosen distribution and validate the resulting broker configuration.

## Rollback

Keep the old plugin and worker distribution available outside active `plugin.path` directories. A connector-only rollback is different from a worker/broker downgrade: restore the old plugin only in an environment compatible with that release, with tasks stopped, and validate its resume behavior against preserved state. Do not assume connector 2.x on a Kafka 4 worker is a supported fallback.

A worker rollback must follow the vendor's allowed version/rolling-downgrade procedure. Broker rollback must additionally respect metadata version, feature levels, log/protocol compatibility, and ZooKeeper-to-KRaft migration finalization constraints. Follow the upstream upgrade/downgrade documentation; a plugin rollback does not reverse broker metadata changes. Never delete offsets or metadata to force a rollback to start.

## Legacy releases and lifecycle

Previous 2.x artifacts remain available under the same Maven coordinates. An exceptional Kafka 3 hotfix could be developed from a previous release branch; this does not promise ongoing maintenance, a support SLA, or a second main-branch module.

As of November 2026, the [Confluent lifecycle table](https://docs.confluent.io/platform/current/installation/versions-interoperability.html#confluent-platform-and-apache-kafka-compatibility) lists **February 19, 2027 for Confluent 7.9 community/standard support**, not a universal Apache Kafka 3 end-of-life. Confluent 7.6 standard support has already ended; Platinum dates differ. The selected **8.2** line's community window ends **March 4, 2027**, with Enterprise standard/Platinum support extending longer. Plan follow-up upgrades within your vendor contract; these vendor dates are not Microsoft connector support commitments.
