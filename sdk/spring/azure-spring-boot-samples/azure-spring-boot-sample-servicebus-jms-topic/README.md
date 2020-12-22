# Sample for Spring JMS with Azure Service Bus Topic Spring Boot client library for Java
## Key concepts

This sample project demonstrates how to use Spring JMS Topic for Azure Service Bus via Spring Boot Starter `azure-spring-boot-starter-servicebus-jms`.

Running this sample will be charged by Azure. You can check the usage and bill at this [link](https://azure.microsoft.com/account/).

## Getting started

### Environment checklist
We need to ensure that this [environment checklist][ready-to-run-checklist] is completed before the run.

### Create Service Bus on Azure
1. Go to [Azure portal](https://portal.azure.com/) and create the service by following this [link](https://docs.microsoft.com/azure/service-bus-messaging/service-bus-create-namespace-portal). 


## Examples                                           
### Config the sample
1. Update [application.properties](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-servicebus-jms-topic/src/main/resources/application.properties)

    ```properties
    # Fill service bus namespace connection string copied from portal
    spring.jms.servicebus.connection-string=[servicebus-namespace-connection-string]
    
    # The JMS client id needs to be specified when using topic and durable subscription
    # Default is empty string
    spring.jms.servicebus.topic-client-id=[topic-client-id]
    
    # The idle timeout in milliseconds after which the connection will be failed if the peer sends no AMQP frames
    # Default is 1800000
    spring.jms.servicebus.idle-timeout=[idle-timeout]
    ```

2. Specify your topic name and subscription name. Update `TOPIC_NAME` in [TopicSendController] and [TopicReceiveController], and `SUBSCRIPTION_NAME` in [TopicReceiveController].

### How to run
1. Run with Maven:
    ```
    cd azure-spring-boot-samples/azure-spring-boot-sample-servicebus-jms-topic
    mvn spring-boot:run
    ```

2. Send a POST request to service bus topic.
    ```
    $ curl -X POST localhost:8080/topic?message=hello
    ```

3. Verify in your app's logs that a similar message was posted:
    ```
    Sending message
    Received message from topic: hello
    ```
    
4. Delete the resources on [Azure Portal](https://ms.portal.azure.com/) to avoid extra charges.

## Troubleshooting
## Next steps

Please check the following table for reference links of detailed Service Bus usage. 

Type | Reference Link
--- | ---
`Topics` | [https://docs.microsoft.com/azure/service-bus-messaging/service-bus-java-how-to-use-topics-subscriptions](https://docs.microsoft.com/azure/service-bus-messaging/service-bus-java-how-to-use-topics-subscriptions)
`Subscriptions` | [https://docs.microsoft.com/azure/service-bus-messaging/service-bus-java-how-to-use-topics-subscriptions](https://docs.microsoft.com/azure/service-bus-messaging/service-bus-java-how-to-use-topics-subscriptions)

## Contributing

<!-- LINKS -->
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[ready-to-run-checklist]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/README.md#ready-to-run-checklist
[TopicSendController]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-servicebus-jms-topic/src/main/java/com/azure/spring/sample/jms/topic/TopicSendController.java
[TopicReceiveController]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-servicebus-jms-topic/src/main/java/com/azure/spring/sample/jms/topic/TopicReceiveController.java
