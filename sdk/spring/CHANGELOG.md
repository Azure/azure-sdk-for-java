# Release History

## 5.15.0 (Unreleased)

### Spring Cloud Azure Dependencies (BOM)

#### Dependency Updates
 - Added a new dependency: `spring-cloud-azure-testcontainers`.

### Spring Cloud Azure Autoconfigure
This section includes changes in `spring-cloud-azure-autoconfigure` module.

#### Features Added
- Add `ConnectionDetails` for Cosmos, Storage Blob and Storage Queue. For more information about `ConnectionDetails`, please refer to [Spring Boot 3.1's ConnectionDetails abstraction](https://spring.io/blog/2023/06/19/spring-boot-31-connectiondetails-abstraction).
- Add the property `customEndpointAddress` for Service Bus SDK clients [#41279](https://github.com/Azure/azure-sdk-for-java/pull/41279).

### Spring Cloud Stream Service Bus Binder
This section includes changes in `spring-cloud-azure-stream-binder-servicebus` module.

#### Features Added
- Support share the 'ServiceBusClientBuilder' for sender client and processor clients creation [#41279](https://github.com/Azure/azure-sdk-for-java/pull/41279).

#### Bugs Fixed
- Fix bug: DLQ reason and description not work in spring-cloud-azure-stream-binder-servicebus. [40951](https://github.com/Azure/azure-sdk-for-java/issues/40951).

## 5.14.0 (2024-07-05)
- This release is compatible with Spring Boot 3.0.0-3.0.13, 3.1.0-3.1.12, 3.2.0-3.2.7, 3.3.0-3.3.1. (Note: 3.0.x (x>13), 3.1.y (y>12), 3.2.z (z>7) and 3.3.m (m>1) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2022.0.0-2022.0.5, 2023.0.0-2023.0.2. (Note: 2022.0.x (x>5) and 2023.0.y (y>2) should be supported, but they aren't tested with this release.)

### Spring Cloud Azure Dependencies (BOM)

#### Dependency Updates
- Upgrade `azure-sdk-bom` to 1.2.25.

### Spring Cloud Azure Appconfiguration Config
This section includes changes in `spring-cloud-azure-starter-appconfiguration-config`, `spring-cloud-azure-appconfiguration-config*`, and `spring-cloud-azure-feature-management*` modules.

#### Bugs Fixed
- Add "Recurrence" parameter for TimeWindowFilter to support config feature flag recur periodically [#40093](https://github.com/Azure/azure-sdk-for-java/pull/40093).

### Azure Spring Data Cosmos
This section includes changes in `azure-spring-data-cosmos` module.
Please refer to [azure-spring-data-cosmos/CHANGELOG.md](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-data-cosmos/CHANGELOG.md#5140-2024-07-05) for more details.

## 5.13.0 (2024-06-06)
- This release is compatible with Spring Boot 3.0.0-3.0.13, 3.1.0-3.1.8, 3.2.0-3.2.6. (Note: 3.0.x (x>13), 3.1.y (y>8) and 3.2.z (z>5) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2022.0.0-2022.0.5, 2023.0.0-2023.0.1. (Note: 2022.0.x (x>5) and 2023.0.y (y>1) should be supported, but they aren't tested with this release.)
- Now, Spring Boot 3.3 is compatible with this release.

### Spring Cloud Azure Dependencies (BOM)

#### Features Added
- Added the following artifacts into current bom file: `spring-cloud-azure-starter-data-redis-lettuce` [#40287](https://github.com/Azure/azure-sdk-for-java/pull/40287).

#### Dependency Updates
- Upgrade `azure-sdk-bom` to 1.2.24.

### Spring Cloud Azure Autoconfigure
This section includes changes in `spring-cloud-azure-autoconfigure` module.

#### Features Added
- Add `AzureLettucePasswordlessAutoConfiguration` to support redis passwordless [#40287](https://github.com/Azure/azure-sdk-for-java/pull/40287).

#### Bugs Fixed
- Fixed `IllegalArgumentException: Subscription cannot be null` error when only configured one subscription name of `AzureServiceBusConsumerClient` or `AzureServiceBusProcessorClient` [#40264](https://github.com/Azure/azure-sdk-for-java/pull/40264).

#### Other Changes
- Disable compatibility verifier by default [#40407](https://github.com/Azure/azure-sdk-for-java/pull/40407).

### Spring Cloud Azure Appconfiguration Config
This section includes changes in `spring-cloud-azure-starter-appconfiguration-config`, `spring-cloud-azure-appconfiguration-config*`, and `spring-cloud-azure-feature-management*` modules.

#### Bugs Fixed
- Fixing App Configuration expose the value of key in error message when parsing invalid JSON [#40132](https://github.com/Azure/azure-sdk-for-java/pull/40132).
- Remove final from App Configuration refresh endpoints, which caused errors when creating Spring AOP Aspects [#40452](https://github.com/Azure/azure-sdk-for-java/pull/40452).

### Spring Cloud Azure Starter Data Redis with Lettuce
This section includes changes in `spring-cloud-azure-starter-data-redis-lettuce` module.

#### Features Added
- Provide `spring-cloud-azure-starter-data-redis-lettuce` to support redis passwordless [#40287](https://github.com/Azure/azure-sdk-for-java/pull/40287).

### Azure Spring Data Cosmos
This section includes changes in `azure-spring-data-cosmos` module.
Please refer to [azure-spring-data-cosmos/CHANGELOG.md](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-data-cosmos/CHANGELOG.md#5130-2024-06-06) for more details.

## 4.19.0 (2024-06-03)
- This release is compatible with Spring Boot 2.5.0-2.5.15, 2.6.0-2.6.15, 2.7.0-2.7.18. (Note: 2.5.x (x>15), 2.6.y (y>15) and 2.7.z (z>18) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2020.0.3-2020.0.6, 2021.0.0-2021.0.9. (Note: 2020.0.x (x>6) and 2021.0.y (y>9) should be supported, but they aren't tested with this release.)

### Spring Cloud Azure Dependencies (BOM)

#### Dependency Updates
- Upgrade `azure-sdk-bom` to 1.2.24.

### Spring Cloud Azure Autoconfigure
This section includes changes in `spring-cloud-azure-autoconfigure` module.

#### Bugs Fixed
- Fixed `IllegalArgumentException: Subscription cannot be null` error when only configured one subscription name of `AzureServiceBusConsumerClient` or `AzureServiceBusProcessorClient` [#40283](https://github.com/Azure/azure-sdk-for-java/pull/40283).

### Spring Cloud Azure Service
This section includes changes in `spring-cloud-azure-service` module.

#### Bugs Fixed
- Update `REDIS_SCOPE_AZURE` to the latest [#40494](https://github.com/Azure/azure-sdk-for-java/pull/40494).

### Azure Spring Data Cosmos
This section includes changes in `azure-spring-data-cosmos` module.
Please refer to [azure-spring-data-cosmos/CHANGELOG.md](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-data-cosmos/CHANGELOG.md#3460-2024-06-03) for more details.

## 5.12.0 (2024-05-09)
- This release is compatible with Spring Boot 3.0.0-3.0.13, 3.1.0-3.1.8, 3.2.0-3.2.5. (Note: 3.0.x (x>13), 3.1.y (y>8) and 3.2.z (z>5) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2022.0.0-2022.0.5, 2023.0.0-2023.0.1. (Note: 2022.0.x (x>5) and 2023.0.y (y>1) should be supported, but they aren't tested with this release.)

### Spring Cloud Azure Dependencies (BOM)

#### Dependency Updates
- Upgrade `azure-sdk-bom` to 1.2.23.

### Azure Spring Data Cosmos
This section includes changes in `azure-spring-data-cosmos` module.
Please refer to [azure-spring-data-cosmos/CHANGELOG.md](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-data-cosmos/CHANGELOG.md#5120-2024-05-09) for more details.

## 4.18.0 (2024-05-07)
- This release is compatible with Spring Boot 2.5.0-2.5.15, 2.6.0-2.6.15, 2.7.0-2.7.18. (Note: 2.5.x (x>15), 2.6.y (y>15) and 2.7.z (z>18) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2020.0.3-2020.0.6, 2021.0.0-2021.0.9. (Note: 2020.0.x (x>6) and 2021.0.y (y>9) should be supported, but they aren't tested with this release.)

### Spring Cloud Azure Dependencies (BOM)

#### Dependency Updates
- Upgrade `azure-sdk-bom` to 1.2.23.

### Azure Spring Data Cosmos
This section includes changes in `azure-spring-data-cosmos` module.
Please refer to [azure-spring-data-cosmos/CHANGELOG.md](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-data-cosmos/CHANGELOG.md#3450-2024-05-07) for more details.

## 5.12.0-beta.1 (2024-04-10)
- This release is compatible with Spring Boot 3.0.0-3.0.13, 3.1.0-3.1.8, 3.2.0-3.2.4. (Note: 3.0.x (x>13), 3.1.y (y>8) and 3.2.z (z>4) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2022.0.0-2022.0.5, 2023.0.0-2023.0.0. (Note: 2022.0.x (x>5) and 2023.0.y (y>0) should be supported, but they aren't tested with this release.)

### Spring Cloud Azure Autoconfigure
This section includes changes in `spring-cloud-azure-autoconfigure` module.

#### Other Changes
- Switch to use `ServiceBusJmsConnectionFactory` from `azure-servicebus-jms` [#39612](https://github.com/Azure/azure-sdk-for-java/pull/39612).

## 5.11.0 (2024-03-29)
- This release is compatible with Spring Boot 3.0.0-3.0.13, 3.1.0-3.1.8, 3.2.0-3.2.4. (Note: 3.0.x (x>13), 3.1.y (y>8) and 3.2.z (z>4) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2022.0.0-2022.0.5, 2023.0.0-2023.0.0. (Note: 2022.0.x (x>5) and 2023.0.y (y>0) should be supported, but they aren't tested with this release.)

### Spring Cloud Azure Dependencies (BOM)

#### Dependency Updates
- Upgrade `azure-sdk-bom` to 1.2.22.

### Spring Cloud Azure Appconfiguration Config
This section includes changes in `spring-cloud-azure-starter-appconfiguration-config`, `spring-cloud-azure-appconfiguration-config*`, and `spring-cloud-azure-feature-management*` modules.

#### Features Added
- Add telemetry schema[#38933](https://github.com/Azure/azure-sdk-for-java/pull/38933).
- Added Auto fail over support. Will automatically find Azure App Configuration replica stores for provided store. The found replica stores will be used as fail over stores after all provided replicas have failed [#38534](https://github.com/Azure/azure-sdk-for-java/pull/38534).
- Added property to disable auto fail over support `spring.cloud.azure.appconfiguration.stores[0].replica-discovery-enabled` [#38534](https://github.com/Azure/azure-sdk-for-java/pull/38534).

### Spring Cloud Stream Event Hubs Binder
This section includes changes in `spring-cloud-azure-stream-binder-eventhubs` module.

#### Features Added
- Support setting values for all channels by using the `spring.cloud.stream.eventhubs.default.consumer.<property>=<value>` and `spring.cloud.stream.eventhubs.default.producer.<property>=<value>` properties [#39317](https://github.com/Azure/azure-sdk-for-java/pull/39317).

### Spring Cloud Stream Service Bus Binder
This section includes changes in `spring-cloud-azure-stream-binder-servicebus` module.

#### Features Added
- Support setting values for all channels by using the `spring.cloud.stream.servicebus.default.consumer.<property>=<value>` and `spring.cloud.stream.servicebus.default.producer.<property>=<value>` properties [#39317](https://github.com/Azure/azure-sdk-for-java/pull/39317).

### Azure Spring Data Cosmos
This section includes changes in `azure-spring-data-cosmos` module.
Please refer to [azure-spring-data-cosmos/CHANGELOG.md](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-data-cosmos/CHANGELOG.md#5110-2024-03-29) for more details.

## 4.17.0 (2024-03-28)
- This release is compatible with Spring Boot 2.5.0-2.5.15, 2.6.0-2.6.15, 2.7.0-2.7.18. (Note: 2.5.x (x>15), 2.6.y (y>15) and 2.7.z (z>18) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2020.0.3-2020.0.6, 2021.0.0-2021.0.9. (Note: 2020.0.x (x>6) and 2021.0.y (y>9) should be supported, but they aren't tested with this release.)

### Spring Cloud Azure Dependencies (BOM)

#### Dependency Updates
- Upgrade `azure-sdk-bom` to 1.2.22.

### Spring Cloud Stream Event Hubs Binder
This section includes changes in `spring-cloud-azure-stream-binder-eventhubs` module.

#### Features Added
- Support setting values for all channels by using the `spring.cloud.stream.eventhubs.default.consumer.<property>=<value>` and `spring.cloud.stream.eventhubs.default.producer.<property>=<value>` properties [#39362](https://github.com/Azure/azure-sdk-for-java/pull/39362).

### Spring Cloud Stream Service Bus Binder
This section includes changes in `spring-cloud-azure-stream-binder-servicebus` module.

#### Features Added
- Support setting values for all channels by using the `spring.cloud.stream.servicebus.default.consumer.<property>=<value>` and `spring.cloud.stream.servicebus.default.producer.<property>=<value>` properties [#39362](https://github.com/Azure/azure-sdk-for-java/pull/39362).

### Azure Spring Data Cosmos
This section includes changes in `azure-spring-data-cosmos` module.
Please refer to [azure-spring-data-cosmos/CHANGELOG.md](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-data-cosmos/CHANGELOG.md#3440-2024-03-28) for more details.

## 5.10.0 (2024-03-01)
- This release is compatible with Spring Boot 3.0.0-3.0.13, 3.1.0-3.1.8, 3.2.0-3.2.3. (Note: 3.0.x (x>13), 3.1.y (y>8) and 3.2.z (z>3) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2022.0.0-2022.0.5, 2023.0.0-2023.0.0. (Note: 2022.0.x (x>5) and 2023.0.y (y>0) should be supported, but they aren't tested with this release.)

### Spring Cloud Azure Dependencies (BOM)

#### Dependency Updates
- Upgrade `azure-sdk-bom` to 1.2.21.

### Azure Spring Data Cosmos
This section includes changes in `azure-spring-data-cosmos` module.
Please refer to [azure-spring-data-cosmos/CHANGELOG.md](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-data-cosmos/CHANGELOG.md#5100-2024-03-01) for more details.

## 4.16.0 (2024-02-28)
- This release is compatible with Spring Boot 2.5.0-2.5.15, 2.6.0-2.6.15, 2.7.0-2.7.18. (Note: 2.5.x (x>15), 2.6.y (y>15) and 2.7.z (z>18) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2020.0.3-2020.0.6, 2021.0.0-2021.0.9. (Note: 2020.0.x (x>6) and 2021.0.y (y>9) should be supported, but they aren't tested with this release.)

### Spring Cloud Azure Dependencies (BOM)

#### Dependency Updates
- Upgrade `azure-sdk-bom` to 1.2.21.

### Azure Spring Data Cosmos
This section includes changes in `azure-spring-data-cosmos` module.
Please refer to [azure-spring-data-cosmos/CHANGELOG.md](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-data-cosmos/CHANGELOG.md#3430-2024-02-28) for more details.

## 5.9.1 (2024-02-08)
- This release is compatible with Spring Boot 3.0.0-3.0.13, 3.1.0-3.1.8, 3.2.0-3.2.2. (Note: 3.0.x (x>13), 3.1.y (y>8) and 3.2.z (z>2) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2022.0.0-2022.0.4, 2023.0.0-2023.0.0. (Note: 2022.0.x (x>4) and 2023.0.y (y>0) should be supported, but they aren't tested with this release.)

### Spring Cloud Azure

#### Bugs Fixed
- Fixed issue where running on versions older than Java 21 would throw a `UnsupportedClassVersionError` [#38690](https://github.com/Azure/azure-sdk-for-java/pull/38690).

### Azure Spring Data Cosmos
This section includes changes in `azure-spring-data-cosmos` module.
Please refer to [azure-spring-data-cosmos/CHANGELOG.md](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-data-cosmos/CHANGELOG.md#591-2024-02-08) for more details.

## 5.9.0 (2024-02-04)
- This release is compatible with Spring Boot 3.0.0-3.0.13, 3.1.0-3.1.8, 3.2.0-3.2.2. (Note: 3.0.x (x>13), 3.1.y (y>8) and 3.2.z (z>2) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2022.0.0-2022.0.4, 2023.0.0-2023.0.0. (Note: 2022.0.x (x>4) and 2023.0.y (y>0) should be supported, but they aren't tested with this release.)

### Spring Cloud Azure Dependencies (BOM)

#### Dependency Updates
- Upgrade `azure-sdk-bom` to 1.2.20.

### Spring Cloud Azure Autoconfigure
This section includes changes in `spring-cloud-azure-autoconfigure` module.

#### Other Changes
- Unsupported basic tier for Service Bus JMS because of the [limitation of Azure Service Bus](https://learn.microsoft.com/azure/service-bus-messaging/jms-developer-guide?tabs=JMS-20%2Csystem-assigned-managed-identity-backed-authentication#java-message-service-jms-programming-model) [#38167](https://github.com/Azure/azure-sdk-for-java/pull/38167).

### Spring Cloud Azure Appconfiguration Config
This section includes changes in `spring-cloud-azure-starter-appconfiguration-config`, `spring-cloud-azure-appconfiguration-config*`, and `spring-cloud-azure-feature-management*` modules.

#### Features Added
- Support backend schema[#38134](https://github.com/Azure/azure-sdk-for-java/pull/38134).

### Azure Spring Data Cosmos
This section includes changes in `azure-spring-data-cosmos` module.
Please refer to [azure-spring-data-cosmos/CHANGELOG.md](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-data-cosmos/CHANGELOG.md#590-2024-02-04) for more details.

## 4.15.0 (2024-02-02)
- This release is compatible with Spring Boot 2.5.0-2.5.15, 2.6.0-2.6.15, 2.7.0-2.7.18. (Note: 2.5.x (x>15), 2.6.y (y>15) and 2.7.z (z>18) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2020.0.3-2020.0.6, 2021.0.0-2021.0.9. (Note: 2020.0.x (x>6) and 2021.0.y (y>9) should be supported, but they aren't tested with this release.)

### Spring Cloud Azure Dependencies (BOM)

#### Dependency Updates
- Upgrade `azure-sdk-bom` to 1.2.20.

### Spring Cloud Azure Autoconfigure
This section includes changes in `spring-cloud-azure-autoconfigure` module.

#### Bugs Fixed
- Fix CVE-2023-34062 [#38066](https://github.com/Azure/azure-sdk-for-java/issues/38066).

#### Other Changes
- Unsupported basic tier for Service Bus JMS because of the [limitation of Azure Service Bus](https://learn.microsoft.com/azure/service-bus-messaging/jms-developer-guide?tabs=JMS-20%2Csystem-assigned-managed-identity-backed-authentication#java-message-service-jms-programming-model) [#38164](https://github.com/Azure/azure-sdk-for-java/pull/38164).

### Azure Spring Data Cosmos
This section includes changes in `azure-spring-data-cosmos` module.
Please refer to [azure-spring-data-cosmos/CHANGELOG.md](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-data-cosmos/CHANGELOG.md#3420-2024-02-02) for more details.

## 5.8.0 (2023-12-14)
- This release is compatible with Spring Boot 3.0.0-3.0.13, 3.1.0-3.1.6, 3.2.0-3.2.0. (Note: 3.0.x (x>13), 3.1.y (y>6) and 3.2.z (z>0) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2022.0.0-2022.0.4, 2023.0.0-2023.0.0. (Note: 2022.0.x (x>4) and 2023.0.y (y>0) should be supported, but they aren't tested with this release.)

### Spring Cloud Azure Dependencies (BOM)

#### Dependency Updates
- Upgrade `azure-sdk-bom` to 1.2.19.

### Spring Cloud Azure Appconfiguration Config
This section includes changes in `spring-cloud-azure-starter-appconfiguration-config`, `spring-cloud-azure-appconfiguration-config*`, and `spring-cloud-azure-feature-management*` modules.

#### Features Added
- Snapshot support using, `spring.cloud.azure.appconfiguration.stores[0].selects[0].snapshot-name`.
- Support for trimming prefixes from keys, default value is the key-filter when key-filter is used. `spring.cloud.azure.appconfiguration.stores[0].trim-key-prefix` [#37598](https://github.com/Azure/azure-sdk-for-java/pull/37598).

### Azure Spring Data Cosmos
This section includes changes in `azure-spring-data-cosmos` module.
Please refer to [azure-spring-data-cosmos/CHANGELOG.md](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-data-cosmos/CHANGELOG.md#580-2023-12-14) for more details.

## 4.14.0 (2023-12-14)
- This release is compatible with Spring Boot 2.5.0-2.5.15, 2.6.0-2.6.15, 2.7.0-2.7.18. (Note: 2.5.x (x>15), 2.6.y (y>15) and 2.7.z (z>18) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2020.0.3-2020.0.6, 2021.0.0-2021.0.8. (Note: 2020.0.x (x>6) and 2021.0.y (y>8) should be supported, but they aren't tested with this release.)

### Spring Cloud Azure Dependencies (BOM)

#### Dependency Updates
- Upgrade `azure-sdk-bom` to 1.2.19.

### Spring Cloud Azure Autoconfigure
This section includes changes in `spring-cloud-azure-autoconfigure` module.

#### Other Changes
- Switch to use `ServiceBusJmsConnectionFactory` from `azure-servicebus-jms` [#37369](https://github.com/Azure/azure-sdk-for-java/pull/37369).

### Spring Cloud Azure Appconfiguration Config
This section includes changes in `spring-cloud-azure-starter-appconfiguration-config`, `spring-cloud-azure-appconfiguration-config*`, and `spring-cloud-azure-feature-management*` modules.

#### Features Added
- Snapshot support using, `spring.cloud.azure.appconfiguration.stores[0].selects[0].snapshot-name`.
- Support for trimming prefixes from keys, default value is the key-filter when key-filter is used. `spring.cloud.azure.appconfiguration.stores[0].trim-key-prefix` [#37470](https://github.com/Azure/azure-sdk-for-java/pull/37470).

### Azure Spring Data Cosmos
This section includes changes in `azure-spring-data-cosmos` module.
Please refer to [azure-spring-data-cosmos/CHANGELOG.md](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-data-cosmos/CHANGELOG.md#3410-2023-12-14) for more details.

## 5.7.0 (2023-11-07)
- This release is compatible with Spring Boot 3.0.0-3.1.5. (Note: 3.1.x (x>3) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2022.0.0-2022.0.4. (Note: 2022.0.x (x>4) should be supported, but they aren't tested with this release.)
- Now, Spring Boot 3.2.0-RC1 and Spring Cloud 2023.0.0-RC1 are compatible with this release.

### Spring Cloud Azure Dependencies (BOM)

#### Dependency Updates
- Upgrade `azure-sdk-bom` to 1.2.18.

### Spring Cloud Azure Autoconfigure
This section includes changes in `spring-cloud-azure-autoconfigure` module.

#### Bugs Fixed
- Use a new name Microsoft Entra ID instead of the old name Azure Active Directory in the Spring configuration metadata file [#37149](https://github.com/Azure/azure-sdk-for-java/pull/37149).

### Spring Cloud Stream Binder Service Bus

#### Features Added
- Support two topic or queue creation options (maxSizeInMegabytes, defaultMessageTimeToLive) in Service Bus channel namespace properties [#37151](https://github.com/Azure/azure-sdk-for-java/pull/37151).

#### Breaking Changes
- Deprecated APIs `ServiceBusChannelProvisioner.validateOrCreateForConsumer`, `ServiceBusChannelProvisioner.validateOrCreateForProducer` [#37151](https://github.com/Azure/azure-sdk-for-java/pull/37151).

### Spring Azure Resource Manager

#### Breaking Changes
- Deprecated APIs `ServiceBusProvisioner.provisionQueue`, `ServiceBusProvisioner.provisionTopic`, `ServiceBusProvisioner.provisionSubscription` [#37151](https://github.com/Azure/azure-sdk-for-java/pull/37151).
- Add new methods to provision queue and topic with entity properties [#37151](https://github.com/Azure/azure-sdk-for-java/pull/37151).

### Azure Spring Data Cosmos
This section includes changes in `azure-spring-data-cosmos` module.
Please refer to [azure-spring-data-cosmos/CHANGELOG.md](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-data-cosmos/CHANGELOG.md#570-2023-11-07) for more details.

## 4.13.0 (2023-11-07)
- This release is compatible with Spring Boot 2.5.0-2.5.15, 2.6.0-2.6.15, 2.7.0-2.7.17. (Note: 2.5.x (x>15), 2.6.y (y>15) and 2.7.z (z>17) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2020.0.3-2020.0.6, 2021.0.0-2021.0.8. (Note: 2020.0.x (x>6) and 2021.0.y (y>8) should be supported, but they aren't tested with this release.)

### Spring Cloud Azure Dependencies (BOM)

#### Dependency Updates
- Upgrade `azure-sdk-bom` to 1.2.18.

### Spring Cloud Azure Autoconfigure
This section includes changes in `spring-cloud-azure-autoconfigure` module.

#### Bugs Fixed
- Use a new name Microsoft Entra ID instead of the old name Azure Active Directory in the Spring configuration metadata file [#37093](https://github.com/Azure/azure-sdk-for-java/pull/37093).

### Spring Cloud Stream Binder Service Bus

#### Features Added
- Support two topic or queue creation options (maxSizeInMegabytes, defaultMessageTimeToLive) in Service Bus channel namespace properties [#36996](https://github.com/Azure/azure-sdk-for-java/pull/36996).

#### Breaking Changes
- Deprecated APIs `ServiceBusChannelProvisioner.validateOrCreateForConsumer`, `ServiceBusChannelProvisioner.validateOrCreateForProducer` [#36996](https://github.com/Azure/azure-sdk-for-java/pull/36996).

### Spring Azure Resource Manager

#### Breaking Changes
- Deprecated APIs `ServiceBusProvisioner.provisionQueue`, `ServiceBusProvisioner.provisionTopic`, `ServiceBusProvisioner.provisionSubscription` [#36996](https://github.com/Azure/azure-sdk-for-java/pull/36996).
- Add new methods to provision queue and topic with entity properties [#36996](https://github.com/Azure/azure-sdk-for-java/pull/36996).

### Azure Spring Data Cosmos
This section includes changes in `azure-spring-data-cosmos` module.
Please refer to [azure-spring-data-cosmos/CHANGELOG.md](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-data-cosmos/CHANGELOG.md#3400-2023-11-07) for more details.


## 5.6.0 (2023-10-24)
- This release is compatible with Spring Boot 3.0.0-3.1.3. (Note: 3.1.x (x>3) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2022.0.0-2022.0.4. (Note: 2022.0.x (x>4) should be supported, but they aren't tested with this release.)

### Spring Cloud Azure Dependencies (BOM)

#### Dependency Updates
- Upgrade `azure-sdk-bom` to 1.2.17.

### Spring Cloud Azure AppConfiguration Config
This section includes changes in `spring-cloud-azure-starter-appconfiguration-config` and `spring-cloud-azure-appconfiguration-config-web`

### Bug Fixed
- Fixes an issue where Web Hook authorization was validated incorrectly, resulting in an Unauthorized error [#37141](https://github.com/Azure/azure-sdk-for-java/pull/37141).

### Spring Cloud Azure Autoconfigure
This section includes changes in `spring-cloud-azure-autoconfigure` module.

#### Bugs Fixed
- Fix the issue that prevented the `disableChallengeResourceVerification` property of the AKV `SecretClient` to be configured [#36561](https://github.com/Azure/azure-sdk-for-java/issues/36561).

### Spring Integration Azure Event Hubs
This section includes changes in the `spring-integration-azure-eventhubs` module.

#### Bugs Fixed
- Fix NPE in the error handler of `EventHubsInboundChannelAdapter` when `instrumentationManager` or `instrumentationId` is null [#36930](https://github.com/Azure/azure-sdk-for-java/pull/36930).

### Spring Integration Azure Service Bus
This section includes changes in the `spring-integration-azure-servicebus` module.

#### Bugs Fixed
- Fix NPE in the error handler of `ServiceBusInboundChannelAdapter` when `instrumentationManager` or `instrumentationId` is null [#36930](https://github.com/Azure/azure-sdk-for-java/pull/36930).

### Azure Spring Data Cosmos
This section includes changes in `azure-spring-data-cosmos` module.
Please refer to [azure-spring-data-cosmos/CHANGELOG.md](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-data-cosmos/CHANGELOG.md#560-2023-10-24) for more details.

## 4.12.0 (2023-10-23)
- This release is compatible with Spring Boot 2.5.0-2.5.15, 2.6.0-2.6.15, 2.7.0-2.7.16. (Note: 2.5.x (x>15), 2.6.y (y>15) and 2.7.z (z>16) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2020.0.3-2020.0.6, 2021.0.0-2021.0.8. (Note: 2020.0.x (x>6) and 2021.0.y (y>8) should be supported, but they aren't tested with this release.)

### Spring Cloud Azure Dependencies (BOM)

#### Dependency Updates
- Upgrade `azure-sdk-bom` to 1.2.17.

### Spring Cloud Azure AppConfiguration Config
This section includes changes in `spring-cloud-azure-starter-appconfiguration-config` and `spring-cloud-azure-appconfiguration-config-web`

### Bug Fixed
- Fixes an issue where Web Hook authorization was validated incorrectly, resulting in an Unauthorized error [#37141](https://github.com/Azure/azure-sdk-for-java/pull/37141).

### Spring Cloud Azure Autoconfigure
This section includes changes in `spring-cloud-azure-autoconfigure` module.

#### Bugs Fixed
- Fix the issue that prevented the `disableChallengeResourceVerification` property of the AKV `SecretClient` to be configured [#36628](https://github.com/Azure/azure-sdk-for-java/pull/36628).

### Spring Integration Azure Event Hubs
This section includes changes in the `spring-integration-azure-eventhubs` module.

#### Bugs Fixed
- Fix NPE in the error handler of `EventHubsInboundChannelAdapter` when `instrumentationManager` or `instrumentationId` is null [#36927](https://github.com/Azure/azure-sdk-for-java/pull/36927).

### Spring Integration Azure Service Bus
This section includes changes in the `spring-integration-azure-servicebus` module.

#### Bugs Fixed
- Fix NPE in the error handler of `ServiceBusInboundChannelAdapter` when `instrumentationManager` or `instrumentationId` is null [#36927](https://github.com/Azure/azure-sdk-for-java/pull/36927).

### Azure Spring Data Cosmos
This section includes changes in `azure-spring-data-cosmos` module.
Please refer to [azure-spring-data-cosmos/CHANGELOG.md](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-data-cosmos/CHANGELOG.md#3390-2023-10-23) for more details.

## 5.5.0 (2023-08-28)
- This release is compatible with Spring Boot 3.0.0-3.1.2. (Note: 3.1.x (x>2) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2022.0.0-2022.0.4. (Note: 2022.0.x (x>4) should be supported, but they aren't tested with this release.)

### Spring Cloud Azure Dependencies (BOM)

#### Dependency Updates
- Upgrade `azure-sdk-bom` to 1.2.16.

### Azure Spring Data Cosmos
This section includes changes in `azure-spring-data-cosmos` module.
Please refer to [azure-spring-data-cosmos/CHANGELOG.md](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-data-cosmos/CHANGELOG.md#550-2023-08-28) for more details.

## 4.11.0 (2023-08-25)
- This release is compatible with Spring Boot 2.5.0-2.5.15, 2.6.0-2.6.15, 2.7.0-2.7.14. (Note: 2.5.x (x>15), 2.6.y (y>15) and 2.7.z (z>14) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2020.0.3-2020.0.6, 2021.0.0-2021.0.8. (Note: 2020.0.x (x>6) and 2021.0.y (y>8) should be supported, but they aren't tested with this release.)

### Spring Cloud Azure Dependencies (BOM)

#### Dependency Updates
- Upgrade `azure-sdk-bom` to 1.2.16.

### Azure Spring Data Cosmos
This section includes changes in `azure-spring-data-cosmos` module.
Please refer to [azure-spring-data-cosmos/CHANGELOG.md](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-data-cosmos/CHANGELOG.md#3380-2023-08-24) for more details.

## 5.4.0 (2023-08-02)
- This release is compatible with Spring Boot 3.0.0-3.1.0. (Note: 3.0.x (x>1) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2022.0.0-2022.0.3. (Note: 2022.0.x (x>0) should be supported, but they aren't tested with this release.)

### Spring Cloud Azure Dependencies (BOM)

#### Dependency Updates
- Upgrade `azure-sdk-bom` to 1.2.15.
- Upgrade `azure-resourcemanager` to 2.29.0.

### Spring Cloud Azure Autoconfigure
This section includes changes in `spring-cloud-azure-autoconfigure` module.

#### Bugs Fixed
- Fix the issue that `AzureMessagingListenerAutoConfiguration` not included in `spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` [#34690](https://github.com/Azure/azure-sdk-for-java/issues/34690), [#35717](https://github.com/Azure/azure-sdk-for-java/pull/35717).
- Improve default AAD configuration condition [#36126](https://github.com/Azure/azure-sdk-for-java/pull/36126).
- Exclude unsupported bean `AzureGlobalProperties` from AOT processing and registration [#36001](https://github.com/Azure/azure-sdk-for-java/issues/36001).

### Spring Cloud Azure Appconfiguration Config
This section includes changes in `spring-cloud-azure-starter-appconfiguration-config`, `spring-cloud-azure-appconfiguration-config*`, and `spring-cloud-azure-feature-management*` modules.

### Bugs Fixed
- Fixes a bug where exclusions from the portal don't map correctly resulting in a `java.lang.ClassCastException` [#35823](https://github.com/Azure/azure-sdk-for-java/issues/35823)

### Azure Spring Data Cosmos
This section includes changes in `azure-spring-data-cosmos` module.
Please refer to [azure-spring-data-cosmos/CHANGELOG.md](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-data-cosmos/CHANGELOG.md#540-2023-08-02) for more details.

## 4.10.0 (2023-08-01)
- This release is compatible with Spring Boot 2.5.0-2.5.15, 2.6.0-2.6.15, 2.7.0-2.7.13. (Note: 2.5.x (x>15), 2.6.y (y>15) and 2.7.z (z>11) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2020.0.3-2020.0.6, 2021.0.0-2021.0.7. (Note: 2020.0.x (x>6) and 2021.0.y (y>7) should be supported, but they aren't tested with this release.)

### Spring Cloud Azure Dependencies (BOM)

#### Dependency Updates
- Upgrade `azure-sdk-bom` to 1.2.15.
- Upgrade `azure-resourcemanager` to 2.29.0.

### Spring Cloud Azure Autoconfigure
This section includes changes in `spring-cloud-azure-autoconfigure` module.

#### Bugs Fixed
- Fix the issue that `AzureMessagingListenerAutoConfiguration` not included in spring.factories [#34690](https://github.com/Azure/azure-sdk-for-java/issues/34690), [#35716](https://github.com/Azure/azure-sdk-for-java/pull/35716).
- Fix default AAD configuration could be activated multiple times [#36124](https://github.com/Azure/azure-sdk-for-java/pull/36124).

### Azure Spring Data Cosmos
This section includes changes in `azure-spring-data-cosmos` module.
Please refer to [azure-spring-data-cosmos/CHANGELOG.md](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-data-cosmos/CHANGELOG.md#3370-2023-08-01) for more details.

## 4.9.0 (2023-06-29)
- This release is compatible with Spring Boot 2.5.0-2.5.15, 2.6.0-2.6.15, 2.7.0-2.7.11. (Note: 2.5.x (x>15), 2.6.y (y>15) and 2.7.z (z>11) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2020.0.3-2020.0.6, 2021.0.0-2021.0.7. (Note: 2020.0.x (x>6) and 2021.0.y (y>7) should be supported, but they aren't tested with this release.)

### Spring Cloud Azure Dependencies (BOM)

#### Dependency Updates
- Upgrade `azure-sdk-bom` to 1.2.14.
- Upgrade `azure-resourcemanager` to 2.28.0.

#### Features Added
- Added `spring-cloud-azure-starter-eventgrid` into current bom file.

### Spring Cloud Azure Autoconfigure
This section includes changes in `spring-cloud-azure-autoconfigure` module.

#### Features Added
- Added autoconfiguration for the Event Grid client [#35537](https://github.com/Azure/azure-sdk-for-java/pull/35537).

### Azure Spring Data Cosmos
This section includes changes in `azure-spring-data-cosmos` module.
Please refer to [azure-spring-data-cosmos/CHANGELOG.md](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-data-cosmos/CHANGELOG.md#3360-2023-06-29) for more details.

## 4.9.0-beta.1 (2023-06-28)
- This release is compatible with Spring Boot 2.5.0-2.5.15, 2.6.0-2.6.15, 2.7.0-2.7.11. (Note: 2.5.x (x>15), 2.6.y (y>15) and 2.7.z (z>11) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2020.0.3-2020.0.6, 2021.0.0-2021.0.7. (Note: 2020.0.x (x>6) and 2021.0.y (y>7) should be supported, but they aren't tested with this release.)

### Spring Cloud Azure Dependencies (BOM)

#### Features Added
- Added the following artifacts into current bom file:
    - spring-cloud-azure-starter-openai

### Azure Spring Data Cosmos
This section includes changes in `azure-spring-data-cosmos` module.
Please refer to [azure-spring-data-cosmos/CHANGELOG.md](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-data-cosmos/CHANGELOG.md#3360-beta1-2023-06-28) for more details.

## 5.3.0 (2023-06-28)
- This release is compatible with Spring Boot 3.0.0-3.1.0. (Note: 3.0.x (x>1) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2022.0.0-2022.0.3. (Note: 2022.0.x (x>0) should be supported, but they aren't tested with this release.)

### Spring Cloud Azure Dependencies (BOM)

#### Dependency Updates
- Upgrade `azure-sdk-bom` to 1.2.14.
- Upgrade `azure-resourcemanager` to 2.28.0.

#### Features Added
- Added the following artifacts into current bom file:
    - spring-cloud-azure-starter-data-cosmos
    - azure-spring-data-cosmos
    - spring-cloud-azure-starter-eventgrid

### Spring Cloud Azure Autoconfigure
This section includes changes in `spring-cloud-azure-autoconfigure` module.

#### Features Added
- Support passwordless connections for JMS ServiceBus in Spring Boot 3. [#35608](https://github.com/Azure/azure-sdk-for-java/pull/35608).
- Added autoconfiguration for the Event Grid client [#35613](https://github.com/Azure/azure-sdk-for-java/pull/35613).

### Azure Spring Data Cosmos
This section includes changes in `azure-spring-data-cosmos` module.
Please refer to [azure-spring-data-cosmos/CHANGELOG.md](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/azure-spring-data-cosmos/CHANGELOG.md#530-2023-06-28) for more details.

## 5.2.0 (2023-06-02)
- This release is compatible with Spring Boot 3.0.0-3.0.5. (Note: 3.0.x (x>1) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2022.0.0-2022.0.2. (Note: 2022.0.x (x>0) should be supported, but they aren't tested with this release.)

### Spring Cloud Azure Dependencies (BOM)

#### Dependency Updates
- Upgrade `azure-sdk-bom` to 1.2.13.
- Upgrade `azure-resourcemanager` to 2.27.0.

#### Features Added
- Added the following artifacts into current bom file:
    - spring-cloud-azure-starter-appconfiguration-config
    - spring-cloud-azure-appconfiguration-config
    - spring-cloud-azure-appconfiguration-config-web
    - spring-cloud-azure-feature-management
    - spring-cloud-azure-feature-management-web

### Spring Cloud Azure Actuator Autoconfigure
This section includes changes in `spring-cloud-azure-actuator-autoconfigure` module.

#### Bugs Fixed
- Make `spring-cloud-azure-appconfiguration-config-web` optional [#34980](https://github.com/Azure/azure-sdk-for-java/pull/34980).

### Spring Cloud Azure Appconfiguration Config
This section includes changes in `spring-cloud-azure-starter-appconfiguration-config`, `spring-cloud-azure-appconfiguration-config*`, and `spring-cloud-azure-feature-management*` modules.

#### Features Added
* Added Deny List for Targeting Filter [#34437](https://github.com/Azure/azure-sdk-for-java/pull/34437).
```yml
feature-management:
  TargetingTest:
    enabled-for:
      -
        name: Microsoft.Targeting
        parameters:
          users:
            - Jeff
            - Alicia
          groups:
            -
              name: Ring0
              rolloutPercentage: 100
            -
              name: Ring1
              rolloutPercentage: 100
          defaultRolloutPercentage: 50
          exclusion:
            users:
              - Ross
```

#### Bugs Fixed

- Fixes issue where credential from Azure Spring global properties was being overridden [#34694](https://github.com/Azure/azure-sdk-for-java/pull/34694).
- Fixes bug where Http Response wasn't checked before trying to use response [#35086](https://github.com/Azure/azure-sdk-for-java/pull/35086).
- Fixes Tracing info for ContainerApp [#35086](https://github.com/Azure/azure-sdk-for-java/pull/35086).

## 4.8.0 (2023-05-25)
- This release is compatible with Spring Boot 2.5.0-2.5.15, 2.6.0-2.6.15, 2.7.0-2.7.11. (Note: 2.5.x (x>15), 2.6.y (y>15) and 2.7.z (z>11) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2020.0.3-2020.0.6, 2021.0.0-2021.0.7. (Note: 2020.0.x (x>6) and 2021.0.y (y>7) should be supported, but they aren't tested with this release.)

### Spring Cloud Azure Dependencies (BOM)

#### Dependency Updates
- Upgrade `azure-sdk-bom` to 1.2.13.
- Upgrade `azure-resourcemanager` to 2.26.0.

### Spring Cloud Azure Core
This section includes changes in `spring-cloud-azure-core`, `spring-cloud-azure-service`, and `spring-cloud-azure-resourcemanager` modules.

#### Breaking Changes
- Deprecated `CloudType.AZURE_GERMANY`, `JDBC_SCOPE_AZURE_GERMANY`, `REDIS_SCOPE_AZURE_GERMANY`, `AzureEnvironmentProperties.AZURE_GERMANY` [#34682](https://github.com/Azure/azure-sdk-for-java/pull/34682).

### Spring Cloud Azure Autoconfigure
This section includes changes in `spring-cloud-azure-autoconfigure` module.

#### Breaking Changes
- Deprecated `SERVICE_BUS_SCOPE_AZURE_GERMANY` [#34682](https://github.com/Azure/azure-sdk-for-java/pull/34682).

### Spring Cloud Azure Actuator Autoconfigure
This section includes changes in `spring-cloud-azure-actuator-autoconfigure` module.

#### Bugs Fixed
- Make `spring-cloud-azure-appconfiguration-config-web` optional [#34980](https://github.com/Azure/azure-sdk-for-java/pull/34980).

### Spring Cloud Azure Appconfiguration Config
This section includes changes in `spring-cloud-azure-starter-appconfiguration-config`, `spring-cloud-azure-appconfiguration-config*`, and `spring-cloud-azure-feature-management*` modules.

#### Features Added
* Added Deny List for Targeting Filter [#34437](https://github.com/Azure/azure-sdk-for-java/pull/34437).
```yml
feature-management:
  TargetingTest:
    enabled-for:
      -
        name: Microsoft.Targeting
        parameters:
          users:
            - Jeff
            - Alicia
          groups:
            -
              name: Ring0
              rolloutPercentage: 100
            -
              name: Ring1
              rolloutPercentage: 100
          defaultRolloutPercentage: 50
          exclusion:
            users:
              - Ross
```

#### Bugs Fixed
- Fixes issue where credential from Azure Spring global properties was being overridden [#34694](https://github.com/Azure/azure-sdk-for-java/pull/34694).
- Fixes bug where Http Response wasn't checked before trying to use response [#35086](https://github.com/Azure/azure-sdk-for-java/pull/35086).
- Fixes Tracing info for ContainerApp [#35086](https://github.com/Azure/azure-sdk-for-java/pull/35086).

## 5.1.0 (2023-04-26)
- This release is compatible with Spring Boot 3.0.0-3.0.5. (Note: 3.0.x (x>1) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2022.0.0-2022.0.2. (Note: 2022.0.x (x>0) should be supported, but they aren't tested with this release.)

### Spring Cloud Azure Dependencies (BOM)

#### Dependency Updates
- Upgrade `azure-sdk-bom` to 1.2.12.
- Upgrade `azure-resourcemanager` to 2.25.0.

### Spring Cloud Azure Autoconfigure
This section includes changes in `spring-cloud-azure-autoconfigure` module.

#### Features Added
- Make the domain-name option optional when configuring Event Hubs/Service Bus in non-public Azure cloud [#32034](https://github.com/Azure/azure-sdk-for-java/issues/32034).

#### Bugs Fixed
- Fixed `GraphClient` exceptions handling when switching `HttpURLConnection` to `RestTemplate`. [#32779](https://github.com/Azure/azure-sdk-for-java/issues/32779)

#### Breaking Changes
- Deprecated properties for AAD and AAD B2C. [#33751](https://github.com/Azure/azure-sdk-for-java/pull/33751).
    - Deprecated properties `spring.cloud.azure.active-directory.jwt-connect-timeout`, `spring.cloud.azure.active-directory.jwt-read-timeout`, `spring.cloud.azure.active-directory.jwt-size-limit`, if you want to configure them, please provide a RestOperations bean.
    - Deprecated properties `spring.cloud.azure.active-directory.b2c.jwt-connect-timeout`, `spring.cloud.azure.active-directory.b2c.jwt-read-timeout`, `spring.cloud.azure.active-directory.b2c.jwt-size-limit`, if you want to configure them, please provide a RestOperations bean.

### Spring Messaging Event Hubs
This section includes changes in `spring-messaging-azure-eventhubs` module.

#### Bugs Fixed
- Fixed Event Hubs Message header `source-type` from `kafka` to `amqp` when using `StreamBridge#send`. [#32777](https://github.com/Azure/azure-sdk-for-java/issues/32777)

### Spring Cloud Stream Event Hubs Binder
This section includes changes in `spring-cloud-azure-stream-binder-eventhubs` module.

#### Breaking Changes
- Make the default partition behavior of Spring Cloud Azure EventHubs binder be a round-robin assignment to align with Event Hubs.[#32816](https://github.com/Azure/azure-sdk-for-java/pull/32816).

### Spring Cloud Azure Native Reachability
This library is deprecated, and all the reachability metadata have been incorporated into each Spring Cloud Azure library's source code directly.

### Spring Cloud Azure Core
This section includes changes in `spring-cloud-azure-core`, `spring-cloud-azure-service`, and `spring-cloud-azure-resourcemanager` modules.

#### Breaking Changes
- Deprecated `CloudType.AZURE_GERMANY` and remove `AzureEnvironmentProperties.AZURE_GERMANY` [#34663](https://github.com/Azure/azure-sdk-for-java/pull/34663).

### Spring Cloud Azure Appconfiguration Config
This section includes changes in `spring-cloud-azure-starter-appconfiguration-config`, `spring-cloud-azure-appconfiguration-config*`, and `spring-cloud-azure-feature-management*` modules.

#### Features Added
First release of the Spring Cloud Azure Appconfiguration Config, to support Spring Boot 3. The modules are:
  - spring-cloud-azure-starter-appconfiguration-config
  - spring-cloud-azure-appconfiguration-config
  - spring-cloud-azure-appconfiguration-config-web
  - spring-cloud-azure-feature-management
  - spring-cloud-azure-feature-management-web

#### Bugs Fixed
- Fixes issue where credential from Azure Spring global properties was being overridden [#34695](https://github.com/Azure/azure-sdk-for-java/pull/34695).

## 4.7.0 (2023-03-23)
- This release is compatible with Spring Boot 2.5.0-2.5.14, 2.6.0-2.6.14, 2.7.0-2.7.9. (Note: 2.5.x (x>14), 2.6.y (y>14) and 2.7.z (z>9) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2020.0.3-2020.0.6, 2021.0.0-2021.0.5. (Note: 2020.0.x (x>6) and 2021.0.y (y>5) should be supported, but they aren't tested with this release.)

### Spring Cloud Azure Dependencies (BOM)

#### Dependency Updates
- Upgrade `azure-sdk-bom` to 1.2.11.
- Upgrade `azure-spring-data-cosmos` to 3.33.0.
- Upgrade `azure-resourcemanager` to 2.24.0.

#### Features Added
- Added the following artifacts into current bom file:
  - spring-cloud-azure-starter-appconfiguration-config
  - spring-cloud-azure-appconfiguration-config
  - spring-cloud-azure-appconfiguration-config-web
  - spring-cloud-azure-feature-management
  - spring-cloud-azure-feature-management-web

#### Features Added
- The module `azure-spring-data-cosmos` was moved from sdk/cosmos to sdk/spring - See [PR 33905](https://github.com/Azure/azure-sdk-for-java/pull/33905)

### Spring Messaging Event Hubs
This section includes changes in `spring-messaging-azure-eventhubs` module.

#### Bugs Fixed
- Fixed Event Hubs Message header `source-type` from `kafka` to `amqp` when using `StreamBridge#send`. [#32777](https://github.com/Azure/azure-sdk-for-java/issues/32777)

### Spring Cloud Stream Event Hubs Binder
This section includes changes in `spring-cloud-azure-stream-binder-eventhubs` module.

#### Breaking Changes
- Make the default partition behavior of Spring Cloud Azure EventHubs binder be a round-robin assignment to align with Event Hubs.[#32816](https://github.com/Azure/azure-sdk-for-java/pull/32816).

### Spring Cloud Azure Autoconfigure
This section includes changes in `spring-cloud-azure-autoconfigure` module.

#### Features Added
- Support passwordless connections for JMS ServiceBus in Spring. [#33489](https://github.com/Azure/azure-sdk-for-java/pull/33489)

#### Breaking Changes
- Deprecated properties for AAD and AAD B2C. [#33538](https://github.com/Azure/azure-sdk-for-java/pull/33538).
  - Deprecated properties `spring.cloud.azure.active-directory.jwt-connect-timeout`, `spring.cloud.azure.active-directory.jwt-read-timeout`, `spring.cloud.azure.active-directory.jwt-size-limit`, if you want to configure them, please provide a RestOperations bean.
  - Deprecated properties `spring.cloud.azure.active-directory.b2c.jwt-connect-timeout`, `spring.cloud.azure.active-directory.b2c.jwt-read-timeout`, `spring.cloud.azure.active-directory.b2c.jwt-size-limit`, if you want to configure them, please provide a RestOperations bean.

## 4.6.0 (2023-02-07)
- This release is compatible with Spring Boot 2.5.0-2.5.14, 2.6.0-2.6.14, 2.7.0-2.7.8. (Note: 2.5.x (x>14), 2.6.y (y>14) and 2.7.z (z>8) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2020.0.3-2020.0.6, 2021.0.0-2021.0.5. (Note: 2020.0.x (x>6) and 2021.0.y (y>5) should be supported, but they aren't tested with this release.)

#### Features Added
- Release the `spring-cloud-azure-starter-redis`. This starter supports Azure hosted Redis service authenticating with Azure AD.

### Spring Cloud Azure Autoconfigure
This section includes changes in `spring-cloud-azure-autoconfigure` module.

#### Features Added
- Enhance the Event Hubs/Service Bus/Storage Queue message converter to support Spring autoconfiguration-pattern. [#30741](https://github.com/Azure/azure-sdk-for-java/issues/30741)
- Support the PROPERTY_AZURE_CLIENT_CERTIFICATE_PASSWORD property. [#30252](https://github.com/Azure/azure-sdk-for-java/issues/30252)
- Make the domain-name option optional when configuring Event Hubs/Service Bus in non-public Azure cloud [#32034](https://github.com/Azure/azure-sdk-for-java/issues/32034).

#### Breaking Changes
- Delete properties: `spring.jms.serviebus.username`, `spring.jms.serviebus.password` and `spring.jms.serviebus.remote-uri` [#32467](https://github.com/Azure/azure-sdk-for-java/pull/32467).
- Change the default value of `spring.jms.servicebus.idle-timeout` from 30 minutes to 2 minutes [#32799](https://github.com/Azure/azure-sdk-for-java/pull/32799).
- Change the default value of `spring.cloud.azure.eventhubs.processor.load-balancing.strategy` from `BALANCED` to `GREEDY` [#32897](https://github.com/Azure/azure-sdk-for-java/pull/32897).

#### Bugs Fixed
- Fixed `GraphClient` exceptions handling when switching `HttpURLConnection` to `RestTemplate`. [#32779](https://github.com/Azure/azure-sdk-for-java/issues/32779)

#### Dependency Updates
- Upgrade Azure SDK BOM to 1.2.9.
- Upgrade Azure Spring Data Cosmos to 3.31.0.
- Upgrade Azure Resource Manager to 2.23.0.

### Spring Cloud Azure Core
This section includes changes in `spring-cloud-azure-core`, `spring-cloud-azure-service`, and `spring-cloud-azure-resourcemanager` modules.

#### Bugs Fixed
- Remove warning logs about client properties while using Kafka passwordless. [#32235](https://github.com/Azure/azure-sdk-for-java/issues/32235)

### Spring Messaging Event Hubs
This section includes changes in `spring-messaging-azure-eventhubs` module.

#### Breaking Changes
- Change the default load-balancing strategy from `BALANCED` to `GREEDY` [#32897](https://github.com/Azure/azure-sdk-for-java/pull/32897).

### Spring Cloud Stream Event Hubs Binder
This section includes changes in `spring-cloud-azure-stream-binder-eventhubs` module.

#### Breaking Changes
- Change the default value of `spring.cloud.stream.eventhubs.bindings.<binding-name>.consumer.load-balancing.strategy` from `BALANCED` to `GREEDY` [#32897](https://github.com/Azure/azure-sdk-for-java/pull/32897).

## 5.0.0 (2023-01-17)
- This release is compatible with Spring Boot 3.0.0-3.0.1. (Note: 3.0.x (x>1) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2022.0.0. (Note: 2022.0.x (x>0) should be supported, but they aren't tested with this release.)

### Breaking Changes
- Update some classes package path, and reduce the number of public APIs [#32552](https://github.com/Azure/azure-sdk-for-java/pull/32552), [#32582](https://github.com/Azure/azure-sdk-for-java/pull/32582), [#32597](https://github.com/Azure/azure-sdk-for-java/pull/32597), [#32616](https://github.com/Azure/azure-sdk-for-java/pull/32616), [#32712](https://github.com/Azure/azure-sdk-for-java/pull/32712), [#32716](https://github.com/Azure/azure-sdk-for-java/pull/32716).
- Remove Spring AOT support [#32742](https://github.com/Azure/azure-sdk-for-java/pull/32742).
- Decrease the major version of Spring Cloud Azure to 5.0 [#32947](https://github.com/Azure/azure-sdk-for-java/pull/32947).

### Spring Cloud Azure Autoconfigure
This section includes changes in `spring-cloud-azure-autoconfigure` module.

#### Features Added
- Enhance the Event Hubs/Service Bus/Storage Queue message converter to support Spring autoconfiguration-pattern [#30741](https://github.com/Azure/azure-sdk-for-java/issues/30741).
- Support the PROPERTY_AZURE_CLIENT_CERTIFICATE_PASSWORD property [#32720](https://github.com/Azure/azure-sdk-for-java/pull/32720).

#### Breaking Changes
- Remove the `public` access modifier from bean methods [#32514](https://github.com/Azure/azure-sdk-for-java/pull/32514).
- Remove Cloud Foundry support [#32616](https://github.com/Azure/azure-sdk-for-java/pull/32616).
- Change the default value of `spring.jms.servicebus.idle-timeout` from 30 minutes to 2 minutes [#32817](https://github.com/Azure/azure-sdk-for-java/pull/32817).
- Change the default value of `spring.cloud.azure.eventhubs.processor.load-balancing.strategy` from `BALANCED` to `GREEDY` [#32913](https://github.com/Azure/azure-sdk-for-java/pull/32913).

#### Bugs Fixed
- Remove unused class `RestTemplateProxyCustomizerConfiguration` [#32616](https://github.com/Azure/azure-sdk-for-java/pull/32616)

### Spring Messaging Azure
This section includes changes in `spring-messaging-azure` module.

#### Breaking Changes
- Rename class `AbstractAzureMessageConverter` to `AbstractJacksonAzureMessageConverter` [#32716](https://github.com/Azure/azure-sdk-for-java/pull/32716).

### Spring Cloud Azure Core
This section includes changes in `spring-cloud-azure-core`, `spring-cloud-azure-service`, and `spring-cloud-azure-resourcemanager` modules.

#### Bugs Fixed
- Remove warning logs about client properties while using Kafka passwordless. [#32235](https://github.com/Azure/azure-sdk-for-java/issues/32235)

### Spring Messaging Event Hubs
This section includes changes in `spring-messaging-azure-eventhubs` module.

#### Breaking Changes
- Change the default load-balancing strategy from `BALANCED` to `GREEDY` [#32913](https://github.com/Azure/azure-sdk-for-java/pull/32913).

### Spring Cloud Stream Event Hubs Binder
This section includes changes in `spring-cloud-azure-stream-binder-eventhubs` module.

#### Breaking Changes
- Change the default value of `spring.cloud.stream.eventhubs.bindings.<binding-name>.consumer.load-balancing.strategy` from `BALANCED` to `GREEDY` [#32913](https://github.com/Azure/azure-sdk-for-java/pull/32913).

## 6.0.0-beta.4 (2022-12-07)
Upgrade Spring Boot dependencies version to 3.0.0-RC2 and Spring Cloud dependencies version to 2022.0.0-RC2.

### Spring Cloud Azure Autoconfigure
This section includes changes in `spring-cloud-azure-autoconfigure` module.

#### Features Added
- Remove warning logs of Kafka passwordless autoconfiguration. [#31182](https://github.com/Azure/azure-sdk-for-java/issues/31182).
- Enhance the Azure AD Resource Server configurer to accept the custom jwt granted authorities converter. [#28665](https://github.com/Azure/azure-sdk-for-java/issues/28665).

#### Breaking Changes
- Move Key Vault environment classes for internal usage to the implementation package [#32428](https://github.com/Azure/azure-sdk-for-java/pull/32428).
- Delete properties: `spring.jms.serviebus.username`, `spring.jms.serviebus.password` and `spring.jms.serviebus.remote-uri` [#32465](https://github.com/Azure/azure-sdk-for-java/pull/32465).

#### Dependency Updates
- Upgrade Azure SDK BOM to 1.2.8.
- Upgrade Azure Identity to 1.7.1.
- Upgrade Azure Identity Extensions to 1.0.0.
- Upgrade Azure Resource Manager to 2.21.0.

## 4.5.0 (2022-12-06)
- This release is compatible with Spring Boot 2.5.0-2.5.14, 2.6.0-2.6.14, 2.7.0-2.7.6. (Note: 2.5.x (x>14), 2.6.y (y>14) and 2.7.z (z>6) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2020.0.3-2020.0.6, 2021.0.0-2021.0.5. (Note: 2020.0.x (x>6) and 2021.0.y (y>5) should be supported, but they aren't tested with this release.)

### Spring Cloud Azure Starter
This section includes changes in `spring-cloud-azure-starter` module.

#### Features Added
- GA the `spring-cloud-azure-starter-jdbc-mysql`. This starter supports Azure hosted MySQL service authenticating with Azure AD.
- GA the `spring-cloud-azure-starter-jdbc-postgresql`. This starter supports Azure hosted PostgreSQL service authenticating with Azure AD.

### Spring Cloud Azure Autoconfigure
This section includes changes in `spring-cloud-azure-autoconfigure` module.

#### Features Added
- Remove warning logs of Kafka passwordless autoconfiguration. [#31182](https://github.com/Azure/azure-sdk-for-java/issues/31182)
- Enhance the token authentication converter and Azure AD Resource Server configurer adapter to accept the custom jwt granted authorities converter. [#28665](https://github.com/Azure/azure-sdk-for-java/issues/28665)

#### Dependency Updates
- Upgrade spring-security to 5.7.5 to address [CVE-2022-31690](https://tanzu.vmware.com/security/cve-2022-31690) [#32145](https://github.com/Azure/azure-sdk-for-java/pull/32145).
- Upgrade Azure SDK BOM to 1.2.8.
- Upgrade Azure Identity to 1.7.1.
- Upgrade Azure Identity Extensions to 1.0.0.
- Upgrade Azure Spring Data Cosmos to 3.30.0.
- Upgrade Azure Resource Manager to 2.21.0.

## 6.0.0-beta.3 (2022-11-04)
Upgrade Spring Boot dependencies version to 3.0.0-RC1 and Spring Cloud dependencies version to 2022.0.0-RC1.

#### Breaking Changes
- Delete the artifact `spring-cloud-azure-trace-sleuth`[#31543](https://github.com/Azure/azure-sdk-for-java/pull/31543).

### Spring Cloud Azure Autoconfigure
This section includes changes in `spring-cloud-azure-autoconfigure` module.

#### Breaking Changes
- Delete the class `com.azure.spring.cloud.autoconfigure.aad.implementation.oauth2.AadOAuth2AuthenticatedPrincipal`[#31543](https://github.com/Azure/azure-sdk-for-java/pull/31543).
- Delete the class `com.azure.spring.cloud.autoconfigure.aad.implementation.webapi.AadOboOAuth2AuthorizedClientProvider`[#31543](https://github.com/Azure/azure-sdk-for-java/pull/31543).
- Delete the class `com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthorizationGrantType`[#31543](https://github.com/Azure/azure-sdk-for-java/pull/31543).
- Delete the class `com.azure.spring.cloud.autoconfigure.aad.AadJwtBearerTokenAuthenticationConverter`[#31543](https://github.com/Azure/azure-sdk-for-java/pull/31543).
- Delete the `AuthorizationGrantType.PASSWORD` support[#31543](https://github.com/Azure/azure-sdk-for-java/pull/31543).
- Delete the auto configuration for Spring Cloud Sleuth support[#31543](https://github.com/Azure/azure-sdk-for-java/pull/31543).
- Use the `com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier#DefaultJWTClaimsVerifier(com.nimbusds.jwt.JWTClaimsSet, java.util.Set<java.lang.String>)` instead of `com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier#DefaultJWTClaimsVerifier()`[#31543](https://github.com/Azure/azure-sdk-for-java/pull/31543).
- Use the `org.springframework.http.ResponseEntity#getStatusCode` instead of `org.springframework.http.ResponseEntity#getStatusCodeValue`[#31543](https://github.com/Azure/azure-sdk-for-java/pull/31543).
- Use an abstract configurer `AbstractHttpConfigurer` instead of `org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter`[#31543](https://github.com/Azure/azure-sdk-for-java/pull/31543).
- Use the annotation `org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity` instead the `org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity`[#31808](https://github.com/Azure/azure-sdk-for-java/pull/31808).
- Use the class `org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken` instead of `org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken`[#31808](https://github.com/Azure/azure-sdk-for-java/pull/31808).

#### Bugs Fixed
- Fix bug: Put a value into Collections.emptyMap(). [#31190](https://github.com/Azure/azure-sdk-for-java/issues/31190).
- Fix bug: RestTemplate used to get access token should only contain 2 converters. [#31482](https://github.com/Azure/azure-sdk-for-java/issues/31482).
- Fix bug: RestOperations is not well configured when jwkResolver is null. [#31218](https://github.com/Azure/azure-sdk-for-java/issues/31218).
- Fix bug: Duplicated "scope" parameter. [#31191](https://github.com/Azure/azure-sdk-for-java/issues/31191).
- Fix bug: NimbusJwtDecoder still uses `RestTemplate()` instead `RestTemplateBuilder` [#31233](https://github.com/Azure/azure-sdk-for-java/issues/31233)
- Fix bug: Proxy setting not work in Azure AD B2C web application. [31593](https://github.com/Azure/azure-sdk-for-java/issues/31593)
- Fix Bug: NoClassDefFoundError for JSONArray. [31716](https://github.com/Azure/azure-sdk-for-java/issues/31716)
- Fix bug: `spring.main.sources` configuration from Spring Cloud Stream Kafka binder cannot take effect. [#31715](https://github.com/Azure/azure-sdk-for-java/pull/31715)

## 4.4.1 (2022-10-31)

### Spring Cloud Azure Autoconfigure
This section includes changes in `spring-cloud-azure-autoconfigure` module.

#### Bugs Fixed
- Fix bug: Put a value into Collections.emptyMap(). [#31190](https://github.com/Azure/azure-sdk-for-java/issues/31190).
- Fix bug: RestTemplate used to get access token should only contain 2 converters. [#31482](https://github.com/Azure/azure-sdk-for-java/issues/31482).
- Fix bug: RestOperations is not well configured when jwkResolver is null. [#31218](https://github.com/Azure/azure-sdk-for-java/issues/31218).
- Fix bug: Duplicated "scope" parameter. [#31191](https://github.com/Azure/azure-sdk-for-java/issues/31191).
- Fix bug: NimbusJwtDecoder still uses `RestTemplate()` instead `RestTemplateBuilder` [#31233](https://github.com/Azure/azure-sdk-for-java/issues/31233)
- Fix bug: Proxy setting not work in Azure AD B2C web application. [31593](https://github.com/Azure/azure-sdk-for-java/issues/31593)
- Fix Bug: NoClassDefFoundError for JSONArray. [31716](https://github.com/Azure/azure-sdk-for-java/issues/31716)
- Fix bug: `spring.main.sources` configuration from Spring Cloud Stream Kafka binder cannot take effect. [#31715](https://github.com/Azure/azure-sdk-for-java/pull/31715)

## 6.0.0-beta.2 (2022-09-30)
Upgrade Spring Boot dependencies version to 3.0.0-M4 and Spring Cloud dependencies version to 2022.0.0-M4.

### Spring Cloud Azure Autoconfigure
This section includes changes in `spring-cloud-azure-autoconfigure` module.

#### Features Added
- Support auto start-up for the auto-configured Service Bus Processor Client by enabling a new property of `spring.cloud.azure.servicebus.processor.auto-startup`. [#29997](https://github.com/Azure/azure-sdk-for-java/issues/29997)
- Configure the `spring.main.sources` with `AzureKafkaSpringCloudStreamConfiguration` class for Spring Cloud Stream Kafka Binder context, which helps developers omit customizing the property manually when leveraging Azure Identity with Kafka [#29976](https://github.com/Azure/azure-sdk-for-java/issues/29976).
- Provide the property of `spring.cloud.azure.eventhubs.kafka.enabled` to turn of/off the OAuth2 support of Spring Cloud Azure for Event Hubs for Kafka [#30574](https://github.com/Azure/azure-sdk-for-java/issues/30574).
- Support connecting to Azure AD via proxy. To achieve this, customer need provide a custom `RestTemplateCustomizer` bean. [#26493](https://github.com/Azure/azure-sdk-for-java/issues/26493).

#### Bugs Fixed
- Fix bug: Cannot configure "azure" authorization client. [#30354](https://github.com/Azure/azure-sdk-for-java/issues/30354).
- Fix parameter `requested_token_use` missing when using On behalf of process [#30359](https://github.com/Azure/azure-sdk-for-java/issues/30359).
- Fix the invalid user agent for Apache Kafka [#30574](https://github.com/Azure/azure-sdk-for-java/pull/30933).
- Fix Kafka `OAuth2AuthenticateCallbackHandler` cannot work with Kafka refreshing login mechanism [#30719](https://github.com/Azure/azure-sdk-for-java/issues/30719).
- Fix the cloud type cannot be configured for a consumer/producer/processor of Service Bus / Event Hubs bug [#30936](https://github.com/Azure/azure-sdk-for-java/issues/30936).

### Spring Cloud Stream Event Hubs Binder
#### Bugs Fixed
- Fix the cloud type cannot be configured for Event Hubs Binder bug [#30936](https://github.com/Azure/azure-sdk-for-java/issues/30936).

### Spring Cloud Stream Service Bus Binder
#### Breaking Changes
- Removed Spring Data Cosmos Auto Configuration support as Spring Data 3 is currently not supported.

#### Bugs Fixed
- Fix the Service Bus Binder cannot automatically create Topic/Subscriptions from consumer bug. [#30722](https://github.com/Azure/azure-sdk-for-java/pull/30722).
- Fix the cloud type cannot be configured for Service Bus Binder bug [#30936](https://github.com/Azure/azure-sdk-for-java/issues/30936).

## 4.4.0 (2022-09-26)
Upgrade Spring Boot dependencies version to 2.7.3 and Spring Cloud dependencies version to 2021.0.3
Upgrade Spring Boot dependencies version to 2.7.2 and Spring Cloud dependencies version to 2021.0.3.

### Spring Cloud Azure Autoconfigure
This section includes changes in `spring-cloud-azure-autoconfigure` module.

#### Bugs Fixed
- Fix bug: Cannot configure "azure" authorization client. [#30354](https://github.com/Azure/azure-sdk-for-java/issues/30354).
- Fix parameter `requested_token_use` missing when using On behalf of process [#30359](https://github.com/Azure/azure-sdk-for-java/issues/30359).
- Fix the invalid user agent for Apache Kafka [#30574](https://github.com/Azure/azure-sdk-for-java/pull/30933).
- Fix Kafka `OAuth2AuthenticateCallbackHandler` cannot work with Kafka refreshing login mechanism [#30719](https://github.com/Azure/azure-sdk-for-java/issues/30719).
- Fix the cloud type cannot be configured for a consumer/producer/processor of Service Bus / Event Hubs bug [#30936](https://github.com/Azure/azure-sdk-for-java/issues/30936).

#### Features Added
- Support auto start-up for the auto-configured Service Bus Processor Client by enabling a new property of `spring.cloud.azure.servicebus.processor.auto-startup`. [#29997](https://github.com/Azure/azure-sdk-for-java/issues/29997)
- Configure the `spring.main.sources` with `AzureKafkaSpringCloudStreamConfiguration` class for Spring Cloud Stream Kafka Binder context, which helps developers omit customizing the property manually when leveraging Azure Identity with Kafka [#29976](https://github.com/Azure/azure-sdk-for-java/issues/29976).
- Provide the property of `spring.cloud.azure.eventhubs.kafka.enabled` to turn of/off the OAuth2 support of Spring Cloud Azure for Event Hubs for Kafka [#30574](https://github.com/Azure/azure-sdk-for-java/issues/30574).
- Support connecting to Azure AD via proxy. To achieve this, customer need provide a custom `RestTemplateCustomizer` bean. [#26493](https://github.com/Azure/azure-sdk-for-java/issues/26493).

### Spring Cloud Stream Event Hubs Binder
#### Bugs Fixed
- Fix the cloud type cannot be configured for Event Hubs Binder bug [#30936](https://github.com/Azure/azure-sdk-for-java/issues/30936).

### Spring Cloud Stream Service Bus Binder
#### Bugs Fixed
- Fix the Service Bus Binder cannot automatically create Topic/Subscriptions from consumer bug. [#30722](https://github.com/Azure/azure-sdk-for-java/pull/30722).
- Fix the cloud type cannot be configured for Service Bus Binder bug [#30936](https://github.com/Azure/azure-sdk-for-java/issues/30936).

## 4.5.0-beta.1 (2022-09-23)
- This release is compatible with Spring Boot 2.5.0-2.5.14, 2.6.0-2.6.11, 2.7.0-2.7.3. (Note: 2.5.x (x>14), 2.6.y (y>11) and 2.7.z (z>3) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2020.0.3-2020.0.6, 2021.0.0-2021.0.3. (Note: 2020.0.x (x>6) and 2021.0.y (y>3) should be supported, but they aren't tested with this release.)

### Spring Cloud Azure Starter
This section includes changes in `spring-cloud-azure-starter` module.
#### Features Added
+ Support Azure hosted PostgreSQL and MySQL services authenticating with Azure AD [#30024](https://github.com/Azure/azure-sdk-for-java/pull/30024).

## 4.3.0 (2022-06-29)
- This release is compatible with Spring Boot 2.5.0-2.5.14, 2.6.0-2.6.9, 2.7.0-2.7.1. (Note: 2.5.x (x>14), 2.6.y (y>9) and 2.7.z (z>1) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2020.0.3-2020.0.5, 2021.0.0-2021.0.3. (Note: 2020.0.x (x>5) and 2021.0.y (y>3) should be supported, but they aren't tested with this release.)

### Features Added
- GA the `spring-cloud-azure-starter-storage`. This starter supports all features of Azure Storage.
- GA the `spring-cloud-azure-starter-keyvault`. This starter supports all features of Azure Key Vault.
- Support Jwt Client authentication for Azure AD Starter.

### Spring Cloud Azure Dependencies (BOM)
#### Dependency Updates
- Upgrade `azure-sdk-bom` to 1.2.3.
- Upgrade `azure-spring-data-cosmos` to 3.23.0.

### Spring Cloud Azure Autoconfigure
This section includes changes in `spring-cloud-azure-autoconfigure` module.

#### Features Added
+ Add `AzureStorageConfiguration` to make Azure storage service share common property configuration [#29094](https://github.com/Azure/azure-sdk-for-java/pull/29094).
    +  Add properties `spring.cloud.azure.storage.endpoint`, `spring.cloud.azure.storage.account-key`, `spring.cloud.azure.storage.sas-token`, `spring.cloud.azure.storage.connection-string`, `spring.cloud.azure.storage.account-name`.
+ Add `AzureKeyVaultConfiguration` to make Azure Key Vault service share common property configuration [#29306](https://github.com/Azure/azure-sdk-for-java/pull/29306).
    + Add properties `spring.cloud.azure.keyvault`.
+ Support OAuth2 authentication configuration for Spring ecosystems of Kafka [#29404](https://github.com/Azure/azure-sdk-for-java/pull/29404).

#### Breaking Changes
- Deprecate support of connection string or Azure Resource Manager based authentication for Spring ecosystems of Kafka [#29404](https://github.com/Azure/azure-sdk-for-java/pull/29404).

#### Dependency Updates
- Upgrade spring-security to 5.6.4 to address [CVE-2022-22978](https://spring.io/blog/2022/05/15/cve-2022-22978-authorization-bypass-in-regexrequestmatcher) [#29304](https://github.com/Azure/azure-sdk-for-java/pull/29304).
- Upgrade `azure-spring-data-cosmos` to 3.23.0. [#29679](https://github.com/Azure/azure-sdk-for-java/pull/29679)
- Upgrade `azure-cosmos` to 4.32.0. [#29679](https://github.com/Azure/azure-sdk-for-java/pull/29679)

#### Features Added
- Add `enabled` option in `AzureServiceBusJmsProperties` [#29232](https://github.com/Azure/azure-sdk-for-java/issues/29232).

#### Bugs Fixed
- Fix the Service Bus JMS autoconfiguration logic error [#29313](https://github.com/Azure/azure-sdk-for-java/pull/29313).
- Fix the authority host of azure identity client not configured bug [#29398](https://github.com/Azure/azure-sdk-for-java/issues/29398).

### Spring Messaging Azure Service Bus
This section includes changes in the `spring-messaging-azure-servicebus` module.

#### Bugs Fixed
- Fix the `ServiceBusContainerProperties` constructor with overriding the default field values [#29095](https://github.com/Azure/azure-sdk-for-java/pull/29095).
- Restrict the concurrency value to be int format in `ServiceBusListener` [#29095](https://github.com/Azure/azure-sdk-for-java/pull/29095).

### Spring Cloud Azure Starter Active Directory
This section includes changes in `spring-cloud-azure-starter-active-directory` module.

#### Dependency Updates
- Upgrade spring-security to 5.6.4 to address [CVE-2022-22978](https://spring.io/blog/2022/05/15/cve-2022-22978-authorization-bypass-in-regexrequestmatcher) [#29304](https://github.com/Azure/azure-sdk-for-java/pull/29304).

#### Features Added
+ Support Jwt Client authentication [#29471](https://github.com/Azure/azure-sdk-for-java/pull/29471).

#### Breaking Changes
+ Deprecated classes and properties type changed [#29471](https://github.com/Azure/azure-sdk-for-java/pull/29471).
    + Deprecated ~~AadAuthorizationGrantType~~, use `AuthorizationGrantType` instead.
    + Deprecated ~~AadOAuth2AuthenticatedPrincipal~~, ~~AadJwtBearerTokenAuthenticationConverter~~, use the default converter `JwtAuthenticationConverter` instead in `AadResourceServerWebSecurityConfigurerAdapter`.
    + The type of property *authorizationGrantType* is changed to `AuthorizationGrantType` in `AuthorizationClientProperties` class.
    + Deprecated ~~AadOboOAuth2AuthorizedClientProvider~~, use `JwtBearerOAuth2AuthorizedClientProvider` instead.

### Spring Cloud Azure Starter Active Directory B2C
This section includes changes in `spring-cloud-azure-starter-active-directory-b2c` module.

#### Dependency Updates
- Upgrade spring-security to 5.6.4 to address [CVE-2022-22978](https://spring.io/blog/2022/05/15/cve-2022-22978-authorization-bypass-in-regexrequestmatcher) [#29304](https://github.com/Azure/azure-sdk-for-java/pull/29304).

### Spring Integration Azure Storage Queue
This section includes changes in `spring-integration-azure-storage-queue` module.

#### Features Added
- Add configurable visibility timeout to `StorageQueueMessageSource` to allow configuring visibility timeout of message source at startup [#29567](https://github.com/Azure/azure-sdk-for-java/pull/29567).

#### Breaking Changes
+ Deprecated classes and properties type changed [#29471](https://github.com/Azure/azure-sdk-for-java/pull/29471).
    + Deprecated *~~AadAuthorizationGrantType~~*, use `AuthorizationGrantType` instead.
    + The type of property *authorizationGrantType* is changed to `AuthorizationGrantType` in `AuthorizationClientProperties` class.

## 4.2.0 (2022-05-26)

- This release is compatible with Spring Boot 2.5.0-2.5.14, 2.6.0-2.6.8, 2.7.0. (Note: 2.5.x (x>14), 2.6.y (y>8) and 2.7.z (z>0) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2020.0.3-2020.0.5, 2021.0.0-2021.0.2. (Note: 2020.0.x (x>5) and 2021.0.y (y>2) should be supported, but they aren't tested with this release.)

### Spring Cloud Azure Dependencies (BOM)
#### Dependency Updates
- Upgrade `azure-sdk-bom` to 1.2.2.
- Upgrade `azure-spring-data-cosmos` to 3.21.0.

## 4.1.0 (2022-05-05)
- This release is compatible with Spring Boot 2.5.0-2.5.13, 2.6.0-2.6.7. (Note: 2.5.x (x>13) and 2.6.y (y>7) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2020.0.3-2020.0.5, 2021.0.0-2021.0.2. (Note: 2020.0.x (x>5) and 2021.0.y (y>2) should be supported, but they aren't tested with this release.)
- Upgrade Spring Cloud to 2021.0.2 to address [CVE-2022-22963](https://github.com/advisories/GHSA-6v73-fgf6-w5j7) [#28179](https://github.com/Azure/azure-sdk-for-java/issues/28179).
- Upgrade Spring Boot to 2.6.6 to address [CVE-2022-22965](https://github.com/advisories/GHSA-36p3-wjmg-h94x) [#28280](https://github.com/Azure/azure-sdk-for-java/pull/28280).

### Features Added
- GA the `spring-cloud-azure-starter-keyvault-certificates`. This starter supports the auto-configuration of Azure Key Vault `CertificateClient` and `CertificateAsyncClient`.

### Spring Cloud Azure Dependencies (BOM)
#### Dependency Updates
- Upgrade `azure-resourcemanager` to 2.14.0.
- Upgrade `azure-sdk-bom` to 1.2.1 [#28586](https://github.com/Azure/azure-sdk-for-java/pull/28586).
- Use `azure-cosmos:4.29.1` instead of the version `4.28.1` in `azure-sdk-bom` [#28555](https://github.com/Azure/azure-sdk-for-java/pull/28555).

### Spring Cloud Azure Autoconfigure
This section includes changes in `spring-cloud-azure-autoconfigure` module.

#### Bugs Fixed
- Fix the bean `AzureTokenCredentialAutoConfiguration` initialization exception when the multiple ThreadPoolTaskExecutors beans exist [#28525](https://github.com/Azure/azure-sdk-for-java/issues/28525).
- Fix incorrect bean name `staticStorageBlobConnectionStringProvider` in the auto-configuration of `AzureStorageFileShareAutoConfiguration` [#28464](https://github.com/Azure/azure-sdk-for-java/issues/28464).
- Fix application startup issue by changing property names in configuration metadata from camel-case to kebab-case [#28312](https://github.com/Azure/azure-sdk-for-java/issues/28312).

## 4.0.0 (2022-03-28)
- This release is compatible with Spring Boot 2.5.0-2.5.11, 2.6.0-2.6.5. (Note: 2.5.x (x>11) and 2.6.y (y>5) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2020.0.3-2020.0.5, 2021.0.0-2021.0.1. (Note: 2020.0.x (x>5) and 2021.0.y (y>1) should be supported, but they aren't tested with this release.)

### Dependency Updates
- Upgrade dependency according to spring-boot-dependencies:2.6.3 and spring-cloud-dependencies:2021.0.0.

### Features Added
- Add `Automatic-Module-Name` for all Spring Cloud Azure modules and change the root package names to match the module names [#27350](https://github.com/Azure/azure-sdk-for-java/issues/27350), [#27420](https://github.com/Azure/azure-sdk-for-java/pull/27420).

### Spring Cloud Azure Dependencies (BOM)
#### Dependency Updates
- Delete the direct reference of following Azure SDKs [#27850](https://github.com/Azure/azure-sdk-for-java/pull/27850):
  + azure-core
  + azure-core-management
  + azure-core-amqp
  + azure-cosmos
  + azure-data-appconfiguration
  + azure-identity
  + azure-messaging-eventhubs
  + azure-messaging-eventhubs-checkpointstore-blob
  + azure-messaging-servicebus
  + azure-security-keyvault-certificates
  + azure-security-keyvault-secrets
  + azure-storage-blob
  + azure-storage-file-share
  + azure-storage-queue
  + azure-core-serializer-json-jackson
- Import `azure-sdk-bom:1.2.0` [#27850](https://github.com/Azure/azure-sdk-for-java/pull/27850).
- Use `azure-cosmos:4.28.0` instead of the version `4.27.0` in `azure-sdk-bom` [#27850](https://github.com/Azure/azure-sdk-for-java/pull/27850).
- Upgrade `azure-resourcemanager` to 2.13.0.
- Upgrade `azure-spring-data-cosmos` to 3.19.0.

### Spring Cloud Azure Starter Active Directory
This section includes changes in `spring-cloud-azure-starter-active-directory` module.

#### Breaking Changes
- Delete the AAD conditional access filter. [#27727](https://github.com/Azure/azure-sdk-for-java/pull/27727)
- Rename classes and methods [#27273](https://github.com/Azure/azure-sdk-for-java/pull/27273), [#27579](https://github.com/Azure/azure-sdk-for-java/pull/27579):
  + Rename classes from `AAD/AADB2C` to `Aad/AadB2c`.
  + Rename method in `UserPrincipal` from `getKid` to `getKeyId`.
  + Rename methods in `AADAuthenticationProperties` from `allowedGroupIdsConfigured/allowedGroupNamesConfigured` to `isAllowedGroupIdsConfigured/isAllowedGroupNamesConfigured`.
  + Rename methods in `AADAuthorizationServerEndpoints` from `authorizationEndpoint/endSessionEndpoint/jwkSetEndpoint/tokenEndpoint` to `getAuthorizationEndpoint/getEndSessionEndpoint/getJwkSetEndpoint/getTokenEndpoint`.
  + Rename method in `UserGroupProperties` from `getUseTransitiveMembers` to `isUseTransitiveMembers`.
- Improve `AadJwt*Validator` and `AadTokenClaim` [#27365](https://github.com/Azure/azure-sdk-for-java/pull/27365):
  + Delete `AadJwtClaimValidator` and use `JwtClaimValidator` instead.
  + Delete `AadJwtAudienceValidator` and use `JwtClaimValidator` instead.
  + Rename `AadTokenClaim` to `AadJwtClaimNames`.

#### Features Added
- Support constructing `AadOAuth2AuthorizationRequestResolver` with `authorizationRequestBaseUri` [#26494](https://github.com/Azure/azure-sdk-for-java/issues/26494).
- Make `AadWebSecurityConfigurerAdapter` more configurable [#27802](https://github.com/Azure/azure-sdk-for-java/pull/27802).

#### Dependency Updates
- Delete the dependencies `org.springframework.boot:spring-boot-starter-webflux`, `com.fasterxml.jackson.core:jackson-databind`, `io.projectreactor.netty:reactor-netty` [#27727](https://github.com/Azure/azure-sdk-for-java/pull/27727).

### Spring Cloud Azure Autoconfigure
This section includes changes in `spring-cloud-azure-autoconfigure` module.

#### Breaking Changes
- Refactor retry options [#27332](https://github.com/Azure/azure-sdk-for-java/pull/27332), [#27586](https://github.com/Azure/azure-sdk-for-java/pull/27586).
  + Delete properties `spring.cloud.azure.retry.timeout` and `spring.cloud.azure.<azure-service>.retry.timeout`.
  + Add properties `spring.cloud.azure.retry.amqp.try-timeout` and `spring.cloud.azure.<azure-amqp-service>.retry.try-timeout` instead. (`<azure-amqp-service>` means this option only applies to AMQP-based service clients).
  + Delete properties `spring.cloud.azure.retry.back-off.max-attempts`, `spring.cloud.azure.retry.back-off.delay`, `spring.cloud.azure.retry.back-off.max-delay`, and `spring.cloud.azure.retry.backoff.multiplier`.
  + Delete properties `spring.cloud.azure.<azure-service>.retry.back-off.max-attempts`, `spring.cloud.azure.<azure-service>.retry.back-off.delay`, `spring.cloud.azure.<azure-service>.retry.back-off..max-delay`, and `spring.cloud.azure.<azure-service>.retry.backoff.multiplier`.
  + Add properties `spring.cloud.azure.retry.mode`, `spring.cloud.azure.<azure-service>.retry.mode`, `spring.cloud.azure.retry.exponential.*`, `spring.cloud.azure.<azure-service>.retry.exponential.*`, `spring.cloud.azure.retry.fixed*`, and `spring.cloud.azure.<azure-service>.retry.fixed.*` instead:
    - `spring.cloud.azure.retry.exponential.base-delay`.
    - `spring.cloud.azure.retry.exponential.max-delay`.
    - `spring.cloud.azure.retry.exponential.max-retries`.
    - `spring.cloud.azure.retry.fixed.delay`.
    - `spring.cloud.azure.retry.fixed.max-retries`.
- Refactor proxy options [#27402](https://github.com/Azure/azure-sdk-for-java/pull/27402):
  + Change `spring.cloud.azure.<azure-service>.proxy.authentication-type` to `spring.cloud.azure.<azure-amqp-service>.proxy.authentication-type`. (`<azure-amqp-service>` means this property only applies to AMQP-based service clients).
  + Delete `spring.cloud.azure.proxy.authentication-type` and add `spring.cloud.azure.proxy.amqp.authentication-type` instead.
- Refactor client options [#27402](https://github.com/Azure/azure-sdk-for-java/pull/27511):
  + Change `spring.cloud.azure.<azure-service>.client.headers` to `spring.cloud.azure.<azure-http-service>.client.headers`. (`<azure-http-service>` means this property only applies to HTTP-based service clients).
  + Delete `spring.cloud.azure.client.headers` and add `spring.cloud.azure.client.http.headers` instead.
- Rename properties `spring.cloud.azure.profile.cloud` and `spring.cloud.azure.<azure-service>.cloud` to `spring.cloud.azure.profile.cloud-type` and `spring.cloud.azure.<azure-service>.cloud-type` [#27258](https://github.com/Azure/azure-sdk-for-java/pull/27258).
- Delete properties `spring.cloud.azure.credential.managed-identity-client-id` and `spring.cloud.azure.<azure-service>.credential.managed-identity-client-id`. Add `spring.cloud.azure.credential.managed-identity-enabled` and `spring.cloud.azure.<azure-service>.credential.managed-identity-enabled` instead [#27118](https://github.com/Azure/azure-sdk-for-java/pull/27118), [#27258](https://github.com/Azure/azure-sdk-for-java/pull/27258).
- Change type of JWK/JWT time duration properties from `int/long` to `Duration` [#27579](https://github.com/Azure/azure-sdk-for-java/pull/27579):
  + `spring.cloud.azure.active-directory.jwt-connect-timeout` and `spring.cloud.azure.active-directory.b2c.jwt-connect-timeout`.
  + `spring.cloud.azure.active-directory.jwt-read-timeout` and `spring.cloud.azure.active-directory.b2c.jwt-read-timeout`.
  + `spring.cloud.azure.active-directory.jwk-set-cache-lifespan`.
  + `spring.cloud.azure.active-directory.jwk-set-cache-refresh-time`.
- Delete deprecated properties for AAD [#26598](https://github.com/Azure/azure-sdk-for-java/pull/26598):
  + `spring.cloud.azure.active-directory.allow-telemetry`.
  + `spring.cloud.azure.active-directory.active-directory-groups`.
  + `spring.cloud.azure.active-directory.authorization-clients.graph.on-demand`
  + `spring.cloud.azure.active-directory.user-group.allowed-groups`.
  + `spring.cloud.azure.active-directory.user-group.enable-full-list`.
- Delete deprecated properties for AAD B2C [#26598](https://github.com/Azure/azure-sdk-for-java/pull/26598):
  + `spring.cloud.azure.active-directory.b2c.allow-telemetry`.
  + `spring.cloud.azure.active-directory.b2c.tenant`.
- Delete properties `spring.cloud.azure.cosmos.permissions`. Please use the builder customizer instead. [#27236](https://github.com/Azure/azure-sdk-for-java/pull/27236).
- Delete properties `spring.cloud.azure.cosmos.gateway-connection.proxy`. Please use `spring.cloud.azure.cosmos.proxy` instead [#27241](https://github.com/Azure/azure-sdk-for-java/pull/27241).
- Rename property `spring.cloud.azure.eventhubs.processor.partition-ownership-expiration-interval` to `spring.cloud.azure.eventhubs.processor.load-balancing.partition-ownership-expiration-interval` [#27331](https://github.com/Azure/azure-sdk-for-java/pull/27331).
- Change `KeyVaultPropertySource`'s configuration properties [27651](https://github.com/Azure/azure-sdk-for-java/pull/27651):
  + Property `spring.cloud.azure.keyvault.secret.enabled` only used to disable autoconfigure `SecretClient` bean, it can't be used to disable inserting `KeyVaultPropertySource`. Use `spring.cloud.azure.keyvault.secret.property-source-enabled` to disable inserting `KeyVaultPropertySource`.
  + Property `spring.cloud.azure.keyvault.secret.endpoint` only used to autoconfigure `SecretClient` bean, it can't be used to configure `KeyVaultPropertySource`. Use `spring.cloud.azure.keyvault.secret.property-sources[].endpoint` to configure `KeyVaultPropertySource`.
  + For properties like `credential`, `profile`, `client`, `proxy`, `retry`, if `spring.cloud.azure.keyvault.secret.property-sources[].xxx` is not configured, it will only take value from `spring.cloud.azure.xxx`, not take value from `spring.cloud.azure.keyvault.secret.xxx` anymore.
  + Conclusion:
    - Here are all `SecretClient` bean related properties: `spring.cloud.azure.keyvault.secret.enabled`, `spring.cloud.azure.keyvault.secret.xxx`, `spring.cloud.azure.xxx`.
    - Here are all `KeyVaultPropertySource` related properties: `spring.cloud.azure.keyvault.secret.property-source-enabled`, `spring.cloud.azure.keyvault.secret.property-sources[].xxx`, `spring.cloud.azure.xxx`.
- Enhance autoconfiguration for a Storage File Share client [#26645](https://github.com/Azure/azure-sdk-for-java/pull/26645):
  + Add auto-configuration for File Share directory client.
  + Rename property `spring.cloud.azure.storage.fileshare.file-name` to `spring.cloud.azure.storage.fileshare.file-path`.
  + Add property `spring.cloud.azure.storage.fileshare.directory-path`.
- Delete `EventHubsInitializationContextConsumer`, `EventHubsCloseContextConsumer`, `EventHubsErrorContextConsumer` and `ServiceBusErrorContextConsumer`. Please use `Consumer<>` directly if you want to configure them [#27288](https://github.com/Azure/azure-sdk-for-java/pull/27288).
- Delete the bean of `EventHubsProcessorContainer` in the autoconfiguration for Event Hubs Spring Messaging support. When needed, a user-defined `EventHubsMessageListenerContainer` bean should be provided for the replacement [#27216](https://github.com/Azure/azure-sdk-for-java/pull/27216).
- Delete the bean of `ServiceBusProcessorContainer` in the autoconfiguration for Service Bus Spring Messaging support. When needed, a user-defined `ServiceBusMessageListenerContainer` bean should be provided for the replacement [#27216](https://github.com/Azure/azure-sdk-for-java/pull/27216).
- Rename Beans in `AadAuthenticationFilterAutoConfiguration` from `azureADJwtTokenFilter\getJWTResourceRetriever\getJWKSetCache` to `aadAuthenticationFilter\jwtResourceRetriever\jwkSetCache` [#27301](https://github.com/Azure/azure-sdk-for-java/pull/27301).
- Rename Bean in `AadB2cResourceServerAutoConfiguration` from `aadIssuerJWSKeySelector` to `aadIssuerJwsKeySelector` [#27301](https://github.com/Azure/azure-sdk-for-java/pull/27301).
- Change non-SDK defined boolean configuration properties from `Boolean` to `boolean` [#27321](https://github.com/Azure/azure-sdk-for-java/pull/27321).
- Delete unused API from `KeyVaultOperation` and `KeyVaultPropertySource` [#27722](https://github.com/Azure/azure-sdk-for-java/pull/27722).
- Delete `Propagator` from the constructor of `SleuthHttpPolicy` [#27621](https://github.com/Azure/azure-sdk-for-java/pull/27621).
- Move classes for internal usage to the implementation package [#27113](https://github.com/Azure/azure-sdk-for-java/issues/27113).

#### Features Added
- Add a compatibility verifier for Spring Cloud Azure [#25437](https://github.com/Azure/azure-sdk-for-java/issues/25437).
- Support configuring an `AzureTokenCredentialResolver` for each `*ClientBuilderFactory` [#26792](https://github.com/Azure/azure-sdk-for-java/pull/26792).
- Add more hints for configuration properties in `additional-spring-configuration-metadata.json` file [#26600](https://github.com/Azure/azure-sdk-for-java/issues/26600).
- Add descriptions and logs for `namespace` property of Service Bus and Event Hubs [#27053](https://github.com/Azure/azure-sdk-for-java/issues/27053).

#### Bugs Fixed
- Fix AAD autoconfiguration activated when no web dependencies on the classpath [#26915](https://github.com/Azure/azure-sdk-for-java/issues/26915).
- Fix inconsistency between `getPropertyNames()` and `containsProperty(String name)` in `KeyVaultPropertySource` [#23815](https://github.com/Azure/azure-sdk-for-java/issues/23815).
- Fix Cosmos direct/gateway connection properties cannot be configured bug [#27241](https://github.com/Azure/azure-sdk-for-java/pull/27241).
- Fix Cosmos default connection mode not being set bug [#27236](https://github.com/Azure/azure-sdk-for-java/pull/27236).
- Fix `DefaultAzureCredential` still being used when global credential properties `spring.cloud.azure.credential.*` provided bug [#27626](https://github.com/Azure/azure-sdk-for-java/pull/27626).
- Fix connection strings retrieved from Azure Resource Manager not being used for Spring Messaging Azure components' autoconfiguration bug [#27831](https://github.com/Azure/azure-sdk-for-java/issues/27831).
- Fix `Azure*ResourceManagerAutoconfigure` being activated when `azure-resourcemanager` is on classpath but service sdk isn't bug [#27703](https://github.com/Azure/azure-sdk-for-java/pull/27703).
- Fix User-Agent for Spring Cloud Azure not loading correctly bug [#27303](https://github.com/Azure/azure-sdk-for-java/issues/27303).


### Spring Cloud Azure Actuator
This section includes changes in `spring-cloud-azure-actuator` module.

#### Breaking Changes
- Move classes for internal usage to the implementation package [#27263](https://github.com/Azure/azure-sdk-for-java/pull/27263).
#### Features Added
- Add health indicator for Key Vault Certificate client [#27706](https://github.com/Azure/azure-sdk-for-java/pull/27706).

### Spring Cloud Stream Event Hubs Binder
This section includes changes in `spring-cloud-azure-stream-binder-eventhubs` module.

#### Breaking Changes
- Change the type of the binding producer property of `send-timeout` from `long` to `Duration` [#26625](https://github.com/Azure/azure-sdk-for-java/pull/26625).
- Change the message header prefix from `azure_eventhub` to `azure_eventhubs_` [#27746](https://github.com/Azure/azure-sdk-for-java/pull/27746).

#### Features Added
- Support `EventHubsProducerFactoryCustomizer` and `EventHubsProcessorFactoryCustomizer` in `EventHubsMessageChannelBinder` [#27351](https://github.com/Azure/azure-sdk-for-java/issues/27351), [#27653](https://github.com/Azure/azure-sdk-for-java/pull/27653), [#27775](https://github.com/Azure/azure-sdk-for-java/pull/27775).

#### Bugs Fixed
- Fix exception when trying to send a message that was received as part of a batch [#26213](https://github.com/Azure/azure-sdk-for-java/issues/26213).
- Fix bug when provisioning an Event Hubs consumer group not uses the correct consumer group name [#26622](https://github.com/Azure/azure-sdk-for-java/pull/26622).

### Spring Cloud Stream Service Bus Binder
This section includes changes in `spring-cloud-azure-stream-binder-servicebus` module.

#### Breaking Changes
- Change the type of the binding producer property `send-timeout` from `long` to `Duration` [#26625](https://github.com/Azure/azure-sdk-for-java/pull/26625).
- Rename property `spring.cloud.stream.servicebus.bindings.<binding-name>.consumer.session-aware` to `spring.cloud.stream.servicebus.bindings.<binding-name>.consumer.session-enabled` [#27331](https://github.com/Azure/azure-sdk-for-java/pull/27331).
- Delete message header of `AzureHeaders.RAW_ID`. Please use `ServiceBusMessageHeaders.MESSAGE_ID` instead [#27675](https://github.com/Azure/azure-sdk-for-java/pull/27675).
- Delete property `spring.cloud.stream.servicebus.bindings.<binding-name>.consumer.checkpoint-mode`. Please use `spring.cloud.stream.servicebus.bindings.<binding-name>.consumer.auto-complete` instead. To disable the auto-complete mode is equivalent to `MANUAL` checkpoint mode and to enable it will trigger the `RECORD` mode [#27615](https://github.com/Azure/azure-sdk-for-java/pull/27615), [#27646](https://github.com/Azure/azure-sdk-for-java/pull/27646).

#### Features Added
- Support converting all headers and properties exposed directly by `ServiceBusReceivedMessage` when receiving messages [#27675](https://github.com/Azure/azure-sdk-for-java/pull/27675), newly supported headers and properties can be get according to the keys of:
  * ServiceBusMessageHeaders.DEAD_LETTER_ERROR_DESCRIPTION
  * ServiceBusMessageHeaders.DEAD_LETTER_REASON
  * ServiceBusMessageHeaders.DEAD_LETTER_SOURCE
  * ServiceBusMessageHeaders.DELIVERY_COUNT
  * ServiceBusMessageHeaders.ENQUEUED_SEQUENCE_NUMBER
  * ServiceBusMessageHeaders.ENQUEUED_TIME
  * ServiceBusMessageHeaders.EXPIRES_AT
  * ServiceBusMessageHeaders.LOCK_TOKEN
  * ServiceBusMessageHeaders.LOCKED_UNTIL
  * ServiceBusMessageHeaders.SEQUENCE_NUMBER
  * ServiceBusMessageHeaders.STATE
  * ServiceBusMessageHeaders.SUBJECT
- Support the message header of `ServiceBusMessageHeaders.SUBJECT` to specify the AMQP property of `subject` when sending messages [#27675](https://github.com/Azure/azure-sdk-for-java/pull/27675).
- Support `ServiceBusProducerFactoryCustomizer` and `ServiceBusProcessorFactoryCustomizer` in `ServiceBusMessageChannelBinder` [#27351](https://github.com/Azure/azure-sdk-for-java/issues/27351), [#27653](https://github.com/Azure/azure-sdk-for-java/pull/27653), [#27775](https://github.com/Azure/azure-sdk-for-java/pull/27775).

### Spring Integration Azure Event Hubs
This section includes changes in the `spring-integration-azure-eventhubs` module.

#### Breaking Changes
- Move classes for internal usage to the implementation package [#27281](https://github.com/Azure/azure-sdk-for-java/pull/27281).
- Change the message header prefix from `azure_eventhub` to `azure_eventhubs_` [#27746](https://github.com/Azure/azure-sdk-for-java/pull/27746).
- Refactor the constructors of `EventHubsInboundChannelAdapter` to `EventHubsInboundChannelAdapter(EventHubsMessageListenerContainer)` and `EventHubsInboundChannelAdapter(EventHubsMessageListenerContainer, ListenerMode)` [#27216](https://github.com/Azure/azure-sdk-for-java/pull/27216), [#27421](https://github.com/Azure/azure-sdk-for-java/pull/27421).

### Spring Integration Azure Service Bus
This section includes changes in the `spring-integration-azure-servicebus` module.

#### Breaking Changes
- Move classes for internal usage to the implementation package [#27281](https://github.com/Azure/azure-sdk-for-java/pull/27281).
- Delete message header of `AzureHeaders.RAW_ID`. Please use `ServiceBusMessageHeaders.MESSAGE_ID` instead [#27675](https://github.com/Azure/azure-sdk-for-java/pull/27675).
- Delete class `CheckpointConfig`. Please use `ServiceBusContainerProperties#setAutoComplete` instead. To disable the auto-complete mode is 
equivalent to `MANUAL` checkpoint mode and to enable it will trigger the `RECORD` mode [#27615](https://github.com/Azure/azure-sdk-for-java/pull/27615), [#27646](https://github.com/Azure/azure-sdk-for-java/pull/27646).
- Refactor the constructors of `ServiceBusInboundChannelAdapter` to `ServiceBusInboundChannelAdapter(ServiceBusMessageListenerContainer)` and `ServiceBusInboundChannelAdapter(ServiceBusMessageListenerContainer, ListenerMode)` [#27216](https://github.com/Azure/azure-sdk-for-java/pull/27216), [#27421](https://github.com/Azure/azure-sdk-for-java/pull/27421).

#### Features Added
- Support converting all headers and properties exposed directly by `ServiceBusReceivedMessage` when receiving messages [#27675](https://github.com/Azure/azure-sdk-for-java/pull/27675), newly supported headers and properties can be get according to the keys of:
  * ServiceBusMessageHeaders.DEAD_LETTER_ERROR_DESCRIPTION
  * ServiceBusMessageHeaders.DEAD_LETTER_REASON
  * ServiceBusMessageHeaders.DEAD_LETTER_SOURCE
  * ServiceBusMessageHeaders.DELIVERY_COUNT
  * ServiceBusMessageHeaders.ENQUEUED_SEQUENCE_NUMBER
  * ServiceBusMessageHeaders.ENQUEUED_TIME
  * ServiceBusMessageHeaders.EXPIRES_AT
  * ServiceBusMessageHeaders.LOCK_TOKEN
  * ServiceBusMessageHeaders.LOCKED_UNTIL
  * ServiceBusMessageHeaders.SEQUENCE_NUMBER
  * ServiceBusMessageHeaders.STATE
  * ServiceBusMessageHeaders.SUBJECT
- Support the message header of `ServiceBusMessageHeaders.SUBJECT` to specify the AMQP property of `subject` when sending messages [#27675](https://github.com/Azure/azure-sdk-for-java/pull/27675).

### Spring Messaging Azure
This section includes changes in the `spring-messaging-azure` module.

#### Breaking Changes
- Move class `com.azure.spring.messaging.PartitionSupplier` to library `com.azure.spring:spring-messaging-azure-eventhubs` [#27422](https://github.com/Azure/azure-sdk-for-java/issues/27422).
- Delete unused interfaces: `ReceiveOperation` and `SubscribeOperation` [#27265](https://github.com/Azure/azure-sdk-for-java/pull/27265).
- Refactor the `*MessageListenerContainer` [#27216](https://github.com/Azure/azure-sdk-for-java/pull/27216), [#27543](https://github.com/Azure/azure-sdk-for-java/pull/27543):
  + Add `MessagingMessageListenerAdapter` to adapt Spring Messaging listeners.
  + Rename `*ProcessingListener` to `*MessageListener`.
- Delete `getter/setter` methods from `AzureCheckpointer` [#27672](https://github.com/Azure/azure-sdk-for-java/pull/27672).  

### Spring Messaging Azure Event Hubs
This section includes changes in the `spring-messaging-azure-eventhubs` module.

#### Breaking Changes
- Move classes for internal usage to the implementation package [#27396](https://github.com/Azure/azure-sdk-for-java/pull/27396).
- Move class `PartitionSupplier` from package `com.azure.spring.messaging` to `com.azure.spring.messaging.eventhubs.core` [#27422](https://github.com/Azure/azure-sdk-for-java/issues/27422).
- Delete parameter of `PartitionSupplier` from the sending API for a single message in `EventHubsTemplate` [#27422](https://github.com/Azure/azure-sdk-for-java/pull/27422). Please use message headers of `com.azure.spring.messaging.AzureHeaders.PARTITION_ID` and `com.azure.spring.messaging.AzureHeaders.PARTITION_KEY` instead [#27422](https://github.com/Azure/azure-sdk-for-java/issues/27422).
- Change the message header prefix from `azure_eventhub` to `azure_eventhubs_` [#27746](https://github.com/Azure/azure-sdk-for-java/pull/27746).
- Refactor the `EventHubsMessageListenerContainer` [#27216](https://github.com/Azure/azure-sdk-for-java/pull/27216), [#27543](https://github.com/Azure/azure-sdk-for-java/pull/27543):
  + Change `EventHubsProcessorContainer` to `EventHubsMessageListenerContainer`.
  + Add class `EventHubsContainerProperties` for constructing a `EventHubsMessageListenerContainer`.
  + Add `EventHubsErrorHandler` for `EventHubsMessageListenerContainer`.
  + Rename `BatchEventProcessingListener` and `RecordEventProcessingListener` to `EventHubsBatchMessageListener` and `EventHubsRecordMessageListener`.

#### Features Added
- Support adding builder customizers in `DefaultEventHubsNamespaceProducerFactory` and `DefaultEventHubsNamespaceProcessorFactory` [#27452](https://github.com/Azure/azure-sdk-for-java/pull/27452).

### Spring Messaging Azure Service Bus
This section includes changes in the `spring-messaging-azure-servicebus` module.

#### Breaking Changes
- Delete parameter of `PartitionSupplier` from the sending API for a single message in `ServiceBusTemplate` [#27349](https://github.com/Azure/azure-sdk-for-java/issues/27349).
Please use message header of `com.azure.spring.messaging.AzureHeaders.PARTITION_KEY` instead [#27422](https://github.com/Azure/azure-sdk-for-java/issues/27422).
- Delete message header of `AzureHeaders.RAW_ID`. Please use `ServiceBusMessageHeaders.MESSAGE_ID` instead [#27675](https://github.com/Azure/azure-sdk-for-java/pull/27675), [#27820](https://github.com/Azure/azure-sdk-for-java/pull/27820).
- Refactor the `ServiceBusMessageListenerContainer` [#27216](https://github.com/Azure/azure-sdk-for-java/pull/27216), [#27543](https://github.com/Azure/azure-sdk-for-java/pull/27543):
  + Change `ServiceBusProcessorContainer` to `ServiceBusMessageListenerContainer`.
  + Add class `ServiceBusContainerProperties` for constructing a `ServiceBusMessageListenerContainer`.
  + Add `ServiceBusErrorHandler` for `ServiceBusMessageListenerContainer`.
  + Rename `MessageProcessingListener` to `ServiceBusRecordMessageListener`.
- Change APIs in `ServiceBusProcessorFactory.Listener` [#27770](https://github.com/Azure/azure-sdk-for-java/pull/27770):
  + Change from `processorAdded(String name, String subscription, ServiceBusProcessorClient client)` to `processorAdded(String name, ServiceBusProcessorClient client)`.
  + Change from `processorRemoved(String name, String subscription, ServiceBusProcessorClient client)` to `processorRemoved(String name, ServiceBusProcessorClient client)`.

#### Features Added
- Support converting all headers and properties exposed directly by `ServiceBusReceivedMessage` when receiving messages [#27832](https://github.com/Azure/azure-sdk-for-java/issues/27832), newly supported headers and properties can be get according to the keys of:
  * ServiceBusMessageHeaders.DEAD_LETTER_ERROR_DESCRIPTION
  * ServiceBusMessageHeaders.DEAD_LETTER_REASON
  * ServiceBusMessageHeaders.DEAD_LETTER_SOURCE
  * ServiceBusMessageHeaders.DELIVERY_COUNT
  * ServiceBusMessageHeaders.ENQUEUED_SEQUENCE_NUMBER
  * ServiceBusMessageHeaders.ENQUEUED_TIME
  * ServiceBusMessageHeaders.EXPIRES_AT
  * ServiceBusMessageHeaders.LOCK_TOKEN
  * ServiceBusMessageHeaders.LOCKED_UNTIL
  * ServiceBusMessageHeaders.SEQUENCE_NUMBER
  * ServiceBusMessageHeaders.STATE
  * ServiceBusMessageHeaders.SUBJECT
- Support the message header of `ServiceBusMessageHeaders.SUBJECT` to specify the AMQP property of `subject` when sending messages [#27675](https://github.com/Azure/azure-sdk-for-java/pull/27675).
- Support adding builder customizers in `DefaultServiceBusNamespaceProducerFactory` and `DefaultServiceBusNamespaceProcessorFactory` [#27452](https://github.com/Azure/azure-sdk-for-java/pull/27452), [#27820](https://github.com/Azure/azure-sdk-for-java/pull/27820).

### Spring Messaging Azure Storage Queue
This section includes changes in `spring-messaging-azure-storage-queue` module.

#### Dependency Updates
- Delete the dependencies `com.fasterxml.jackson.core:jackson-databind`. [#27727](https://github.com/Azure/azure-sdk-for-java/pull/27727)

#### Breaking Changes
- Delete parameter of `PartitionSupplier` from the sending API for a single message in `StorageQueueTemplate` [#27422](https://github.com/Azure/azure-sdk-for-java/issues/27422).


## 4.0.0-beta.3 (2022-01-18)
Please refer to [Spring Cloud Azure Migration Guide for 4.0](https://microsoft.github.io/spring-cloud-azure/4.0.0-beta.3/4.0.0-beta.3/reference/html/appendix.html#migration-guide-for-4-0) to learn how to migrate to version 4.0.

- Support Spring Boot version: 2.5.5+ and 2.6.0 - 2.6.1.
- Support Spring Cloud version: 2020.0.3+ and 2021.0.0.

### Dependency Updates
Upgrade dependency according to spring-boot-dependencies:2.6.1 and spring-cloud-dependencies:2021.0.0.

### Spring Cloud Azure AutoConfigure
This section includes changes in the `spring-cloud-azure-autoconfigure` module.

#### Features Added
- Enable `Caching` and `Pooling` ConnectionFactory autoconfiguration for Service Bus JMS [#26072](https://github.com/Azure/azure-sdk-for-java/issues/26072).
- Support all configuration options of the initial position when processing an event hub [#26434](https://github.com/Azure/azure-sdk-for-java/issues/26434).
- Support autoconfiguration of `QueueClient` by adding configuration property `spring.cloud.azure.storage.queue.queue-name` [#26382](https://github.com/Azure/azure-sdk-for-java/pull/26382).
- Improve the `spring-configuration-metadata.json` [#26292](https://github.com/Azure/azure-sdk-for-java/issues/26292), [#26274](https://github.com/Azure/azure-sdk-for-java/pull/26274).
- Support `StorageQueueMessageConverter` as a bean to support customize ObjectMapper [#26200](https://github.com/Azure/azure-sdk-for-java/pull/26200).
- Support `EventHubsMessageConverter` as a bean to support customize ObjectMapper [#26200](https://github.com/Azure/azure-sdk-for-java/pull/26200).

#### Breaking Changes
- Change AAD configuration properties to use the namespace for credential and environment properties [#25646](https://github.com/Azure/azure-sdk-for-java/issues/25646).
  * Property name "spring.cloud.azure.active-directory.tenant-id" changed to "spring.cloud.azure.active-directory.profile.tenant-id".
  * Property name "spring.cloud.azure.active-directory.client-id" changed to "spring.cloud.azure.active-directory.credential.client-id".
  * Property name "spring.cloud.azure.active-directory.client-secret" changed to "spring.cloud.azure.active-directory.credential.client-secret".
  * Property name "spring.cloud.azure.active-directory.base-uri" changed to "spring.cloud.azure.active-directory.profile.environment.active-directory-endpoint".
  * Property name "spring.cloud.azure.active-directory.graph-base-uri" changed to "spring.cloud.azure.active-directory.profile.environment.microsoft-graph-endpoint".
  * Property name "spring.cloud.azure.active-directory.graph-membership-uri" changed to "spring.cloud.azure.active-directory.profile.environment.microsoft-graph-endpoint" and "spring.cloud.azure.active-directory.user-group.use-transitive-members".
- Change AAD B2C configuration properties to use the namespace for credential and environment properties [#25799](https://github.com/Azure/azure-sdk-for-java/pull/25799).
- Change Event Hubs processor configuration properties `spring.cloud.azure.eventhubs.processor.partition-ownership-expiration-interval` to `spring.cloud.azure.eventhubs.processor.load-balancing.partition-ownership-expiration-interval` [#25851](https://github.com/Azure/azure-sdk-for-java/pull/25851).
- Change Event Hubs configuration properties `spring.cloud.azure.eventhubs.fqdn` to `spring.cloud.azure.eventhubs.fully-qualified-namespace` [#25851](https://github.com/Azure/azure-sdk-for-java/pull/25851).
- Rename all `*CP` classes to `*ConfigurationProperties` [#26209](https://github.com/Azure/azure-sdk-for-java/pull/26209).

#### Bugs Fixed
- Fix global HTTP client properties `spring.cloud.azure.client.http.*` not set correctly [#26190](https://github.com/Azure/azure-sdk-for-java/issues/26190).
- Fix Cosmos properties `spring.cloud.azure.cosmos.proxy.*` not set correctly [#25690](https://github.com/Azure/azure-sdk-for-java/issues/25690).
- Fix `PoolAcquirePendingLimitException` when using `EventProcessorClient` or `spring-cloud-azure-stream-binder-eventhubs` [#26027](https://github.com/Azure/azure-sdk-for-java/issues/26027).

### Spring Cloud Stream Event Hubs Binder
This section includes changes in `spring-cloud-azure-stream-binder-eventhubs` module.

#### Breaking Changes
- For the batch consuming mode, change the message header names converted from batched messages [#26291](https://github.com/Azure/azure-sdk-for-java/pull/26291).
  * Change message header from `azure_eventhub_enqueued_time` to `azure_eventhub_batch_converted_enqueued_time`.
  * Change message header from `azure_eventhub_offset` to `azure_eventhub_batch_converted_offset`.
  * Change message header from `azure_eventhub_sequence_number` to `azure_eventhub_batch_converted_sequence_number`.
  * Change message header from `azure_partition_key` to `azure_batch_converted_partition_key`.
- When publish messages to Event Hubs, ignore all message headers converted from batched messages [#26213](https://github.com/Azure/azure-sdk-for-java/issues/26213).

  Headers include:
  * `azure_eventhub_batch_converted_enqueued_time`
  * `azure_eventhub_batch_converted_offset`
  * `azure_eventhub_batch_converted_sequence_number`
  * `azure_batch_converted_partition_key`
  * `azure_eventhub_batch_converted_system_properties`
  * `azure_eventhub_batch_converted_application_properties`
- Expose message header `azure_eventhub_last_enqueued_event_properties` [#25960](https://github.com/Azure/azure-sdk-for-java/issues/25960).
- Improve logging information when converting between `com.azure.messaging.eventhubs.EventData` and `org.springframework.messaging.Message` [#26291](https://github.com/Azure/azure-sdk-for-java/pull/26291).
- Fix `PoolAcquirePendingLimitException` when using `EventHubsProcessorContainer` [#26027](https://github.com/Azure/azure-sdk-for-java/issues/26027).
### Spring Integration Event Hubs

This section includes changes in `spring-messaging-azure-eventhubs` and `spring-integration-azure-eventhubs` modules.

#### Features Added
- Expose message header `azure_eventhub_last_enqueued_event_properties` in `spring-integration-azure-eventhubs` module [#25960](https://github.com/Azure/azure-sdk-for-java/issues/25960).

#### Breaking Changes
- Refactor `spring-messaging-azure-eventhubs` message converter [#26291](https://github.com/Azure/azure-sdk-for-java/pull/26291).
  * Rename `EventHubBatchMessageConverter` to `EventHubsBatchMessageConverter`.
  * Refactor `EventHubsBatchMessageConverter` by disabling the ability of converting Spring Message to EventData.
  * Improve logging information when converting between `com.azure.messaging.eventhubs.EventData` and `org.springframework.messaging.Message`.
- Change `DefaultEventHubsNamespaceProcessorFactory` constructor parameter type from `PropertiesSupplier<Tuple2<String, String>, ProcessorProperties>` to `PropertiesSupplier<ConsumerIdentifier, ProcessorProperties>` [#26200](https://github.com/Azure/azure-sdk-for-java/pull/26200).

#### Bugs Fixed
- Fix `PoolAcquirePendingLimitException` when using `EventHubsProcessorContainer` [#26027](https://github.com/Azure/azure-sdk-for-java/issues/26027).
- Fix `EventHubsTemplate` not sending all messages in a collection [#24445](https://github.com/Azure/azure-sdk-for-java/issues/24445).


### Spring Integration Service Bus

This section includes changes in `spring-messaging-azure-servicebus` and `spring-integration-azure-servicebus` modules.

#### Breaking Changes
- Change `DefaultServiceBusNamespaceProcessorFactory` constructor parameter type from `PropertiesSupplier<Tuple2<String, String>, ProcessorProperties>` to `PropertiesSupplier<ConsumerIdentifier, ProcessorProperties>` [#26200](https://github.com/Azure/azure-sdk-for-java/pull/26200).
- Refactor `ServiceBusInboundChannelAdapter` constructors [#26200](https://github.com/Azure/azure-sdk-for-java/pull/26200).

### Spring Integration Storage Queue
This section includes changes in `spring-messaging-azure-storage-queue` and `spring-integration-azure-storage-queue` modules.

#### Breaking Changes

- Refactor `spring-messaging-azure-storage-queue` module [#26273](https://github.com/Azure/azure-sdk-for-java/issues/26273).
  * Remove `StorageQueueOperation`.
  * Remove configuration of checkpoint mode for `StorageQueueTemplate`, and support only MANUAL mode.
  * Remove auto creating Storage Queue when send/receive messages via `StorageQueueTemplate`.

### Spring Cloud Azure Core

This section includes changes in `spring-cloud-azure-core`, `spring-cloud-azure-service`, and `spring-cloud-azure-resourcemanager` modules.

#### Breaking Changes
- Refactor `spring-cloud-azure-core` module [#25851](https://github.com/Azure/azure-sdk-for-java/pull/25851).
  * Support JPMS.
- Refactor `spring-cloud-azure-service` module [#25851](https://github.com/Azure/azure-sdk-for-java/pull/25851).
  * Move all `*BuilderFactory` to `*.implementation.*` packages [#26404](https://github.com/Azure/azure-sdk-for-java/issues/26404).
  * Support JPMS [#25851](https://github.com/Azure/azure-sdk-for-java/pull/25851).
- Refactor `spring-cloud-azure-resourcemanager` module [#25851](https://github.com/Azure/azure-sdk-for-java/pull/25851).
  * Rename `com.azure.spring.resourcemanager.provisioner` to `com.azure.spring.resourcemanager.provisioning` [#26472](https://github.com/Azure/azure-sdk-for-java/pull/26472).
  * Support JPMS [#25851](https://github.com/Azure/azure-sdk-for-java/pull/25851).

#### Bugs Fixed
- Fix exception thrown by `AzureStorageFileProtocolResolver` and `AzureStorageBlobProtocolResolver` when target file, directory or container does not exist [#25916](https://github.com/Azure/azure-sdk-for-java/issues/25916).

### Other Changes
- Refactor the test structure and increase the test coverage [#23773](https://github.com/Azure/azure-sdk-for-java/issues/23773), [#26175](https://github.com/Azure/azure-sdk-for-java/issues/26175).
- Improve Java code documentation [#24332](https://github.com/Azure/azure-sdk-for-java/issues/24332).
- Improve User-Agent headers for Spring Cloud Azure libraries [#25892](https://github.com/Azure/azure-sdk-for-java/issues/25892), [#26122](https://github.com/Azure/azure-sdk-for-java/pull/26122).
- Improve [Sample Projects](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/spring-cloud-azure_4.0.0-beta.3).
  * Switch to `DefaultAzureCredential` [#25652](https://github.com/Azure/azure-sdk-for-java/issues/25652).
  * Provision resources using **Terraform** [#25652](https://github.com/Azure/azure-sdk-for-java/issues/25652).
  * Unify names of Sample Projects [#26308](https://github.com/Azure/azure-sdk-for-java/issues/26308).
- Improve [Spring Cloud Azure Reference Doc](https://microsoft.github.io/spring-cloud-azure/4.0.0-beta.3/4.0.0-beta.3/reference/html/index.html).
  * Make sure all contents were in original README files are included [#25921](https://github.com/Azure/azure-sdk-for-java/issues/25921).
  * Add RBAC description [#25973](https://github.com/Azure/azure-sdk-for-java/issues/25973).

## 4.0.0-beta.2 (2021-11-22)

Please refer to [Spring Cloud Azure Migration Guide for 4.0][Spring-Cloud-Azure-Migration-Guide-for-4.0] to learn how to migrate to version 4.0.

### Spring Cloud Azure Autoconfigure
This section includes changes in `spring-cloud-azure-autoconfigure` module.

#### Breaking Changes
- Remove the health indicator for `KeyVaultEnvironmentPostProcessor` [#24309](https://github.com/Azure/azure-sdk-for-java/pull/24309).

### spring-cloud-azure-stream-binder-eventhubs

#### Features Added

- Support batch consumers.

#### Breaking Changes

- Change artifact id from `azure-spring-cloud-stream-binder-eventhubs` to `spring-cloud-azure-stream-binder-eventhubs`.
- Change the binder type from `eventhub` to `eventhubs`.
- Change the Spring Cloud Stream Binding extended properties prefix from `spring.cloud.stream.eventhub` to `spring.cloud.stream.eventhubs`.
- BATCH checkpoint-mode only works in batch-consuming mode.

### spring-cloud-azure-stream-binder-servicebus

#### Breaking Changes

- Combine libraries of `azure-spring-cloud-stream-binder-servicebus-queue` and `azure-spring-cloud-stream-binder-servicebus-topic` to `spring-cloud-azure-stream-binder-servicebus` with new binder type as `servicebus`.
- When using the binder to send messages, one of the following two attributes must be provided:
  - spring.cloud.stream.servicebus.bindings.{channel-name}.producer.entity-type
  - spring.cloud.azure.servicebus.producer.entity-type

#### Features Added

- Provide the ability of interacting with both queue and topic.

### spring-cloud-azure-starter-integration-eventhubs

#### Breaking Changes

- Change artifact id from `azure-spring-cloud-starter-eventhubs` to
  `spring-cloud-azure-starter-integration-eventhubs`.
- Annotation of `@AzureMessageListeners`, `@AzureMessageListener` and `@EnableAzureMessaging` are dropped.
- Drop `RxJava` support of `EventHubRxOperation` and `EventHubRxTemplate` and support `Reactor` only.
- Drop `EventHubOperation`, and move its `subscribe` API to class of `EventHubsProcessorContainer`.
- Rename `EventHubsInboundChannelAdapter` as `EventHubsInboundChannelAdapter` to keep consistent with the service of
  Azure
  Event Hubs, and change constructor signature as well.

* Change `CheckpointConfig` instantiation style to simple constructor instead of build style.

### spring-integration-azure-eventhubs

#### Breaking Changes

- Change artifact id from `azure-spring-integration-eventhubs` to
  `spring-integration-azure-eventhubs`.
- Annotation of `@AzureMessageListeners`, `@AzureMessageListener` and `@EnableAzureMessaging` are dropped.
- Drop `RxJava` support of `EventHubRxOperation` and `EventHubRxTemplate` and support `Reactor` only.
- Drop `EventHubOperation`, and move its `subscribe` API to class of `EventHubsProcessorContainer`.
- Rename `EventHubsInboundChannelAdapter` as `EventHubsInboundChannelAdapter` to keep consistent with the service of
  Azure
  Event Hubs, and change constructor signature as well.

* Change `CheckpointConfig` instantiation style to simple constructor instead of build style.

### spring-cloud-azure-starter-integration-servicebus

#### Breaking Changes

- Change artifact id from `azure-spring-cloud-starter-servicebus` to `spring-cloud-azure-starter-integration-servicebus`.
- Combine the original `ServiceBusQueueTemplate#sendAsync` and `ServiceBusTopicTemplate#sendAsync` as `ServiceBusTemplate#sendAsync` and drop classes of `ServiceBusQueueTemplate` and `ServiceBusTopicTemplate`.
- Drop `CompletableFuture` support of ServiceBusTemplate and support `Reactor` instead.
- Drop interface of `ServiceBusQueueOperation` and `ServiceBusTopicOperation`.
- Drop API of `ServiceBusQueueOperation#abandon` and `ServiceBusQueueOperation#deadletter`.
- Combine the original `ServiceBusQueueTemplate#subscribe` and `ServiceBusTopicTemplate#subscribe` as `ServiceBusProcessorClient#subscribe`.
- Deprecate the interface of `SubscribeOperation`.
- Add new API of `setDefaultEntityType` for ServiceBusTemplate, the default entity type of `ServiceBusTemplate` is required when no bean of `PropertiesSupplier<String, ProducerProperties>` is provided for the `ProducerProperties#entityType`.
- Drop class of `ServiceBusQueueInboundChannelAdapter` and `ServiceBusTopicInboundChannelAdapter` and combine them as `ServiceBusInboundChannelAdapter`.
- Class of `DefaultMessageHandler` is moved from `com.azure.spring.integration.core` to package `com.azure.spring.integration.handler`

#### Features Added

- Provide the ability to connect to multiple Azure Service Bus entities in different namespaces.

### spring-integration-azure-servicebus

#### Breaking Changes

- Change artifact id from `azure-spring-integration-servicebus` to `spring-integration-azure-servicebus`.
- Combine the original `ServiceBusQueueTemplate#sendAsync` and `ServiceBusTopicTemplate#sendAsync` as `ServiceBusTemplate#sendAsync` and drop classes of `ServiceBusQueueTemplate` and `ServiceBusTopicTemplate`.
- Drop and `CompletableFuture` support of ServiceBusTemplate and support `Reactor` instead.
- Drop interface of `ServiceBusQueueOperation` and `ServiceBusTopicOperation`.
- Drop API of `ServiceBusQueueOperation#abandon` and `ServiceBusQueueOperation#deadletter`.
- Combine the original `ServiceBusQueueTemplate#subscribe` and `ServiceBusTopicTemplate#subscribe` as `ServiceBusProcessorClient#subscribe`.
- Deprecate the interface of `SubscribeOperation`.
- Add new API of `setDefaultEntityType` for ServiceBusTemplate, the default entity type of `ServiceBusTemplate` is required when no bean of `PropertiesSupplier<String, ProducerProperties>` is provided for the `ProducerProperties#entityType`.
- Drop class of `ServiceBusQueueInboundChannelAdapter` and `ServiceBusTopicInboundChannelAdapter` and combine them as `ServiceBusInboundChannelAdapter`.
- Class of `DefaultMessageHandler` is moved from `com.azure.spring.integration.core` to package `com.azure.spring.integration.handler`

#### Features Added

- Provide the ability to connect to multiple Azure Service Bus entities in different namespaces.

### spring-messaging-azure

#### Breaking Changes

- Annotation of `@AzureMessageListeners`, `@AzureMessageListener` and `@EnableAzureMessaging` are dropped.
- Change artifact id from `azure-spring-cloud-messaging` to `spring-messaging-azure`.

### spring-cloud-azure-starter-servicebus-jms

#### Breaking Changes

- Change artifact id from `azure-spring-boot-starter-servicebus-jms` to `spring-cloud-azure-starter-servicebus-jms`.

### spring-integration-azure-storage-queue

#### Breaking Changes

- Change artifact id from `azure-spring-integration-storage-queue` to `spring-integration-azure-storage-queue`.
- Class of `DefaultMessageHandler` is moved from `com.azure.spring.integration.core` to package `com.azure.spring.integration.handler`.
- Class of `StorageQueueMessageSource` is moved from `com.azure.spring.integration.core` to package `com.azure.spring.integration.storage.queue.inbound.StorageQueueMessageSource`.
- Class of `StorageQueueOperation` is moved from `com.azure.spring.integration.storage.queue.StorageQueueOperation` to package `com.azure.spring.storage.queue.core.StorageQueueOperation`.
- Class of `StorageQueueTemplate` is moved from `com.azure.spring.integration.storage.queue.StorageQueueTemplate` to package `com.azure.spring.storage.queue.core.StorageQueueTemplate`.

### spring-cloud-azure-starter-integration-storage-queue

#### Breaking Changes

- Change artifact id from `azure-spring-cloud-starter-storage-queue` to `spring-cloud-azure-starter-integration-storage-queue`.
- Class of `DefaultMessageHandler` is moved from `com.azure.spring.integration.core` to package `com.azure.spring.integration.handler`.
- Class of `StorageQueueMessageSource` is moved from `com.azure.spring.integration.storage.queue.inbound` to package `com.azure.spring.integration.storage.queue.inbound.StorageQueueMessageSource`.
- Class of `StorageQueueOperation` is moved from `com.azure.spring.integration.storage.queue.StorageQueueOperation` to package `com.azure.spring.storage.queue.core.StorageQueueOperation`.
- Class of `StorageQueueTemplate` is moved from `com.azure.spring.integration.storage.queue.StorageQueueTemplate` to package `com.azure.spring.storage.queue.core.StorageQueueTemplate`.

### spring-cloud-azure-trace-sleuth

#### Features Added

- Support http pipeline policy implemented by Spring Cloud Sleuth API. ([#24192])

[Spring-Cloud-Azure-Migration-Guide-for-4.0]: https://microsoft.github.io/spring-cloud-azure/docs/4.0.0-beta.2/reference/html/appendix.html#migration-guide-for-4-0
[#24192]: https://github.com/Azure/azure-sdk-for-java/pull/24192
