# Sample for Azure Service Bus Spring Boot client library for Java

## Key concepts
This sample project demonstrates how to use Service Bus with Spring Boot. 

## Getting started

### Environment checklist
We need to ensure that this [environment checklist][ready-to-run-checklist] is completed before the run.

### Create Service Bus on Azure
1. Go to [Azure portal](https://portal.azure.com/) and create the service by following this [link](https://docs.microsoft.com/azure/service-bus-messaging/service-bus-create-namespace-portal). 
2. Mark down the `Primary Connection String`.
3. In the `Overview` blade, create queue and topic. Mark down your queue name and topic name. 
4. Click your created topic, add subscription in the `Subscriptions` blade. Mark down your subscription name.

## Examples                                           
### Config the sample

1. Navigate to `src/main/resources` and open `application.properties`.
2. Fill in the `connection-string`, `queue-name`, `topic-name`, and `subscription-name`. 

### Run with Maven

```
cd azure-spring-boot-samples/azure-spring-boot-sample-servicebus
mvn spring-boot:run
```

## Troubleshooting
## Next steps
Please check the following table for reference links of detailed Service Bus usage. 

Type | Reference Link
--- | ---
`Queues` | [Use Azure Service Bus queues with Java to send and receive messages](https://docs.microsoft.com/azure/service-bus-messaging/service-bus-java-how-to-use-queues)
`Topics & Subscriptions` | [Use Service Bus topics and subscriptions with Java](https://docs.microsoft.com/azure/service-bus-messaging/service-bus-java-how-to-use-topics-subscriptions)

## Contributing

<!-- LINKS -->
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[ready-to-run-checklist]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/README.md#ready-to-run-checklist