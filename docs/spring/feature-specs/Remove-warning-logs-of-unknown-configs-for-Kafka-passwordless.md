
<!-- vscode-markdown-toc-config
	numbering=false
	autoSave=false
	/vscode-markdown-toc-config -->
<!-- /vscode-markdown-toc -->
## <a name='Problemstatement'></a>Problem statement
As is reported in https://github.com/Azure/azure-sdk-for-java/issues/30800#issuecomment-1254620865, when using Event Hubs for Kafka passwordless-connection, there are a batch of warning logs saying, "The configuration 'xxx' was supplied but isn't a known config".
We should consider preventing those warning logs being printed.
```java
2022-09-22 14:51:10.825  WARN 30520 --- [           main] o.a.k.clients.consumer.ConsumerConfig    : The configuration 'azure.profile.environment.gallery-endpoint' was supplied but isn't a known config.
2022-09-22 14:51:10.825  WARN 30520 --- [           main] o.a.k.clients.consumer.ConsumerConfig    : The configuration 'azure.profile.cloud-type' was supplied but isn't a known config.
2022-09-22 14:51:10.825  WARN 30520 --- [           main] o.a.k.clients.consumer.ConsumerConfig    : The configuration 'azure.profile.environment.azure-data-lake-store-file-system-endpoint-suffix' was supplied but isn't a known config.
2022-09-22 14:51:10.825  WARN 30520 --- [           main] o.a.k.clients.consumer.ConsumerConfig    : The configuration 'azure.profile.environment.azure-data-lake-analytics-catalog-and-job-endpoint-suffix' was supplied but isn't a known config.
2022-09-22 14:51:10.825  WARN 30520 --- [           main] o.a.k.clients.consumer.ConsumerConfig    : The configuration 'azure.credential.managed-identity-enabled' was supplied but isn't a known config.
2022-09-22 14:51:10.825  WARN 30520 --- [           main] o.a.k.clients.consumer.ConsumerConfig    : The configuration 'azure.profile.environment.azure-application-insights-endpoint' was supplied but isn't a known config.
2022-09-22 14:51:10.825  WARN 30520 --- [           main] o.a.k.clients.consumer.ConsumerConfig    : The configuration 'azure.profile.environment.resource-manager-endpoint' was supplied but isn't a known config.
2022-09-22 14:51:10.825  WARN 30520 --- [           main] o.a.k.clients.consumer.ConsumerConfig    : The configuration 'azure.profile.environment.azure-log-analytics-endpoint' was supplied but isn't a known config.
2022-09-22 14:51:10.825  WARN 30520 --- [           main] o.a.k.clients.consumer.ConsumerConfig    : The configuration 'azure.profile.environment.sql-management-endpoint' was supplied but isn't a known config.
```

## <a name='Userscenarios'></a>User scenarios
Currently there are 3 scenarios that developer uses Kafka password-less connection from the perspective of configuration, and all of them can bring in the warning logs:

1. By default, developers use the least and required properties:
   ```properties
   # When using org.springframework.cloud:spring-cloud-starter-stream-kafka, developers set the only required broker config
   spring.cloud.stream.kafka.binder.brokers=<namespace>.servicebus.windows.net:9093
   ```
   or
   ```properties
   # When using org.springframework.kafka:spring-kafka in a Spring Boot application, developers set the only required bootstrap-server config
   spring.kafka.bootstrap-servers=<namespace>.servicebus.windows.net:9093
   ```
2. When developers manually configure any customized credential/profile properties via Spring Cloud Azure global configuration:
   ```properties
   # When using org.springframework.cloud:spring-cloud-starter-stream-kafka
   spring.cloud.stream.kafka.binder.brokers=<namespace>.servicebus.windows.net:9093
   spring.cloud.azure.credential.managed-identity-enabled=true
   ```
   or
   ```properties
   # When using org.springframework.kafka:spring-kafka in a Spring Boot application
   spring.kafka.bootstrap-servers=<namespace>.servicebus.windows.net:9093
   spring.cloud.azure.credential.managed-identity-enabled=true
   ```
3. When developers manually configure any customized credential/profile properties via Kafka configuration:
   ```properties
   # When using org.springframework.cloud:spring-cloud-starter-stream-kafka
   spring.cloud.stream.kafka.binder.brokers=<namespace>.servicebus.windows.net:9093
   spring.cloud.stream.kafka.binder.producer-properties.azure.credential.managed-identity-enabled=true
   ```
   or
   ```properties
   # When using org.springframework.kafka:spring-kafka in a Spring Boot application
   spring.kafka.bootstrap-servers=<namespace>.servicebus.windows.net:9093
   spring.kafka.properties.azure.credential.managed-identity-enabled=true
   ```

## <a name='Goal'></a>Goal
We should make sure there won't be any warning logs in the above 3 cases. That's to say, developers could keep the existing way of using Kafka password-less connection including the same API, properties for spring boot and cloud stream, library dependency. But will not see any warning logs of "The configuration 'xxx' was supplied but isn't a known config".


## <a name='Exitcriteria'></a>Exit criteria
1. With all the above mentioned 3 scenarios, the application can connect successfully without warning logs of "The configuration 'xxx' was supplied but isn't a known config".
2. We should make sure the existing functions won't be broken by this feature:
   1. We developers set customized properties from Spring Cloud Azure properties, Spring Boot Kafka properties and SCS Kafka binder properties, then those configuration should still take effects to the Event Hubs connection.
   2. When developers set properties from Spring Boot Kafka properties, we should make sure it can be passed to SCS Kafka without warnings.

