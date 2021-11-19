# 4.0.0-beta.1 (Unreleased)

Please refer to [Spring-Cloud-Azure-Migration-Guide-for-4.0] to learn how to migrate to version 4.0.

## spring-cloud-azure-stream-binder-eventhubs
### Features Added
- Support batch consumers.
### Breaking Changes
- Change artifact id from `azure-spring-cloud-stream-binder-eventhubs` to `spring-cloud-azure-stream-binder-eventhubs`.
- Change the binder type from `eventhub` to `eventhubs`.
- Change the Spring Cloud Stream Binding extended properties prefix from `spring.cloud.stream.eventhub` to `spring.cloud.stream.eventhubs`.
- BATCH checkpoint-mode only works in batch-consuming mode.

## spring-cloud-azure-stream-binder-servicebus
### Breaking Changes
- Combine libraries of `azure-spring-cloud-stream-binder-servicebus-queue` and `azure-spring-cloud-stream-binder-servicebus-topic` to `spring-cloud-azure-stream-binder-servicebus` 

## spring-cloud-azure-trace-sleuth
### Features Added
- Support http pipeline policy implemented by Spring Cloud Sleuth API. ([#24192])

## spring-messaging-azure
### Breaking Changes
- Move module _azure-spring-cloud-messaging_ to module _spring-messaging-azure_.




[Spring-Cloud-Azure-Migration-Guide-for-4.0]: https://github.com/Azure/azure-sdk-for-java/wiki/Spring-Cloud-Azure-Migration-Guide-for-4.0
[#24192]: https://github.com/Azure/azure-sdk-for-java/pull/24192