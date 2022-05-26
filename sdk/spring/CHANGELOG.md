# Release History

## 1.0.0 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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

### Bugs Fixed
- Fix the bean `AzureTokenCredentialAutoConfiguration` initialization exception when the multiple ThreadPoolTaskExecutors beans exist [#28525](https://github.com/Azure/azure-sdk-for-java/issues/28525).
- Fix incorrect bean name `staticStorageBlobConnectionStringProvider` in the auto-configuration of `AzureStorageFileShareAutoConfiguration` [#28464](https://github.com/Azure/azure-sdk-for-java/issues/28464).
- Fix application startup issue by changing property names in configuration metadata from camel-case to kebab-case [#28312](https://github.com/Azure/azure-sdk-for-java/issues/28312).

### Spring Cloud Azure Dependencies (BOM)
#### Dependency Updates
- Upgrade `azure-resourcemanager` to 2.14.0.
- Upgrade `azure-sdk-bom` to 1.2.1 [#28586](https://github.com/Azure/azure-sdk-for-java/pull/28586).
- Use `azure-cosmos:4.29.1` instead of the version `4.28.1` in `azure-sdk-bom` [#28555](https://github.com/Azure/azure-sdk-for-java/pull/28555).

## 4.0.0 (2022-03-28)
- This release is compatible with Spring Boot 2.5.0-2.5.11, 2.6.0-2.6.5. (Note: 2.5.x (x>11) and 2.6.y (y>5) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2020.0.3-2020.0.5, 2021.0.0-2021.0.1. (Note: 2020.0.x (x>5) and 2021.0.y (y>1) should be supported, but they aren't tested with this release.)

### Dependency Updates
- Upgrade dependency according to spring-boot-dependencies:2.6.3 and spring-cloud-dependencies:2021.0.0.

### Features Added 
- Add `Automatic-Module-Name` for all Spring Cloud Azure modules and change the root pacakge names to match the module names [#27350](https://github.com/Azure/azure-sdk-for-java/issues/27350), [#27420](https://github.com/Azure/azure-sdk-for-java/pull/27420).

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
- Move classes for internal usage to the implementation pacakge [#27113](https://github.com/Azure/azure-sdk-for-java/issues/27113).

#### Features Added
- Add a compatibility verifier for Spring Cloud Azure [#25437](https://github.com/Azure/azure-sdk-for-java/issues/25437).
- Support configuring an `AzureTokenCredentialResolver` for each `*ClientBuilderFactory` [#26792](https://github.com/Azure/azure-sdk-for-java/pull/26792).
- Add more hints for configuration properties in `additional-spring-configuration-metadata.json` file [#26600](https://github.com/Azure/azure-sdk-for-java/issues/26600).
- Add descriptions and logs for `namespacce` property of Service Bus and Event Hubs [#27053](https://github.com/Azure/azure-sdk-for-java/issues/27053).

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
- Move classes for internal usage to the implementation pacakge [#27281](https://github.com/Azure/azure-sdk-for-java/pull/27281).
- Change the message header prefix from `azure_eventhub` to `azure_eventhubs_` [#27746](https://github.com/Azure/azure-sdk-for-java/pull/27746).
- Refactor the constructors of `EventHubsInboundChannelAdapter` to `EventHubsInboundChannelAdapter(EventHubsMessageListenerContainer)` and `EventHubsInboundChannelAdapter(EventHubsMessageListenerContainer, ListenerMode)` [#27216](https://github.com/Azure/azure-sdk-for-java/pull/27216), [#27421](https://github.com/Azure/azure-sdk-for-java/pull/27421).

### Spring Integration Azure Service Bus
This section includes changes in the `spring-integration-azure-servicebus` module.

#### Breaking Changes
- Move classes for internal usage to the implementation pacakge [#27281](https://github.com/Azure/azure-sdk-for-java/pull/27281).
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
- Move classes for internal usage to the implementation pacakge [#27396](https://github.com/Azure/azure-sdk-for-java/pull/27396).
- Move class `PartitionSupplier` from package `com.azure.spring.messaging` to `com.azure.spring.messaging.eventhubs.core` [#27422](https://github.com/Azure/azure-sdk-for-java/issues/27422).
- Delete parameter of `PartitionSupplier` from the sending API for a single message in `EventHubsTemplate` [#27422](https://github.com/Azure/azure-sdk-for-java/pull/27422). Please use message headers of `com.azure.spring.messaging.AzureHeaders.PARTITION_ID` and `com.azure.spring.messaging.AzureHeaders.PARTITION_KEY` instead [#27422](https://github.com/Azure/azure-sdk-for-java/issues/27422).
- Change the message header prefix from `azure_eventhub` to `azure_eventhubs_` [#27746](https://github.com/Azure/azure-sdk-for-java/pull/27746).
- Refactor the `EventHubsMessageListenerContainer` [#27216](https://github.com/Azure/azure-sdk-for-java/pull/27216), [#27543](https://github.com/Azure/azure-sdk-for-java/pull/27543): 
  + Change `EventHubsProcessorContainer` to `EventHubsMessageListenerContainer`.
  + Add class `EventHubsContainerProperties` for constructing a `EventHubsMessageListenerContainer`.
  + Add `EventHubsErrorHandler` for `EventHubsMessageListenerContainer`.
  + Rename `BatchEventProcessingListener` and `RecordEventProcessingListener` to `EventHubsBatchMessageListener` and `EventHubsRecordMessageListener`.

#### Features Added
- Support adding builder custoimzers in `DefaultEventHubsNamespaceProducerFactory` and `DefaultEventHubsNamespaceProcessorFactory` [#27452](https://github.com/Azure/azure-sdk-for-java/pull/27452).

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
- Support adding builder custoimzers in `DefaultServiceBusNamespaceProducerFactory` and `DefaultServiceBusNamespaceProcessorFactory` [#27452](https://github.com/Azure/azure-sdk-for-java/pull/27452), [#27820](https://github.com/Azure/azure-sdk-for-java/pull/27820).

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
- Change Event Hubs processor configuration properties `spring.cloud.azure.eventhbs.processor.partition-ownership-expiration-interval` to `spring.cloud.azure.eventhbs.processor.load-balancing.partition-ownership-expiration-interval` [#25851](https://github.com/Azure/azure-sdk-for-java/pull/25851).
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
