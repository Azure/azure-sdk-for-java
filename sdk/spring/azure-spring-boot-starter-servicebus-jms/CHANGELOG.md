# Release History

## 3.12.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 3.11.0 (2021-11-24)

This release is compatible with Spring Boot 2.5.0 - 2.5.4.
### Dependency Upgrades
Regular updates for Azure SDK dependency versions.

## 3.10.0 (2021-11-02)

### Features Added
Add property for JmsDefaultPrefetchPolicy.([#24304](https://github.com/Azure/azure-sdk-for-java/issues/24304))
- Add property `spring.jms.servicebus.prefetch-policy.all` to configure all prefetchPolicy values.
- Add property `spring.jms.servicebus.durable-topic-prefetch` to configure the durable topic prefetch value.
- Add property `spring.jms.servicebus.queue-browser-prefetch` to configure the queueBrowserPrefetch value.
- Add property `spring.jms.servicebus.queue-prefetch` to configure the queuePrefetch value.
- Add property `spring.jms.servicebus.topic-prefetch` to configure the topicPrefetch value.

### Breaking Changes
For standard tier in ServiceBus, the default value of prefetch number has been changed from 1000 to 0.


## 3.9.0 (2021-09-27)
This release is compatible with Spring Boot 2.5.0 - 2.5.4.
### Dependency Upgrades
- Upgrade to [spring-boot-dependencies:2.5.4](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/2.5.4/spring-boot-dependencies-2.5.4.pom).
### Features Added
- Support configuration of `AbstractJmsListenerContainerFactory` for `JmsListener`. Enabled properties include `replyPubSubDomain`, `replyQosSettings`, `subscriptionDurable`, `subscriptionShared` and `phase` with prefix as `spring.jms.servicebus.listener`.
### Bugs Fixed
- Fix the bug of not supporting Spring Boot autoconfiguration of JMS listener.


## 3.8.0 (2021-08-25)
This release is compatible with Spring Boot 2.5.0 - 2.5.3.
### Dependency Upgrades
- Upgrade to [spring-boot-dependencies:2.5.3](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/2.5.3/spring-boot-dependencies-2.5.3.pom).

## 3.7.0 (2021-07-20)
### Dependency Upgrades
- Upgrade to [spring-boot-dependencies:2.5.2](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/2.5.2/spring-boot-dependencies-2.5.2.pom).

## 3.6.0 (2021-06-23)
### New Features
- Upgrade to [spring-boot-dependencies:2.5.0](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/2.5.0/spring-boot-dependencies-2.5.0.pom).


## 3.5.0 (2021-05-24)
### New Features
- Upgrade to [spring-boot-dependencies:2.4.5](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/2.4.5/spring-boot-dependencies-2.4.5.pom).
- Upgrade to [spring-cloud-dependencies:2020.0.2](https://repo.maven.apache.org/maven2/org/springframework/cloud/spring-cloud-dependencies/2020.0.2/spring-cloud-dependencies-2020.0.2.pom).



## 3.4.0 (2021-04-19)


## 3.3.0 (2021-03-22)
### New Features
- Upgrade to `Spring Boot` [2.4.3](https://github.com/spring-projects/spring-boot/releases/tag/v2.4.3).
### Key Bug Fixes
- Fix bug of using closed `MessageProducer` and `MessageConsumer` when a link is force detached.

## 3.2.0 (2021-03-03)
### Breaking Changes
- Require new property of `spring.jms.servicebus.pricing-tier` to set pricing tier of Azure Service Bus. Supported values are `premium`, `standard` and `basic`.

### New Features
- Enable MessageConverter bean customization.
- Update the underpinning JMS library for the Premium pricing tier of Service Bus to JMS 2.0.

## 3.1.0 (2021-01-20)


## 3.0.0 (2020-12-30)


## 3.0.0-beta.1 (2020-11-18)
### Breaking Changes
- Change group id from `com.microsoft.azure` to `com.azure.spring`.
- Change artifact id from `azure-servicebus-jms-spring-boot-starter` to `azure-spring-boot-starter-servicebus-jms`.

## 2.3.5 (2020-09-14)
### Breaking Changes
- Unify spring-boot-starter version

## 2.3.4 (2020-08-20)
### Key Bug Fixes
- Replace underpinning JMS library for Service Bus of Service Bus JMS Starter to Apache Qpid to support all tiers of Service Bus.

## 2.3.3 (2020-08-13)

### Breaking Changes 
- Update the underpinning JMS library for Service Bus to JMS 2.0 to support seamlessly lift and shift their Spring workloads to Azure and automatic creation of resources.
 
### Key Bug Fixes
- Address CVEs and cleaned up all warnings at build time. 
