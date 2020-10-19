# Sample for Spring JMS with Azure Service Bus Queue Spring Boot client library for Java

## Key concepts

This sample project demonstrates how to use Spring JMS for Azure Service Bus Queue via Spring Boot Starter `azure-servicebus-jms-spring-boot-starter`. 

Running this sample will be charged by Azure. You can check the usage and bill at this [link](https://azure.microsoft.com/account/).

## Getting started
### Prerequisites

* An Azure subscription. If you don't already have an Azure subscription, you can activate your [MSDN subscriber benefits](https://azure.microsoft.com/pricing/member-offers/msdn-benefits-details/) or sign up for a [free Azure account](https://azure.microsoft.com/free/).

* A [Java Development Kit (JDK)][jdk_link], version 1.8.

* [Apache Maven](http://maven.apache.org/), version 3.0 or later.

### Create Service Bus on Azure

1. Go to [Azure portal](https://portal.azure.com/) and create the service by following this [link](https://docs.microsoft.com/azure/service-bus-messaging/service-bus-create-namespace-portal). 

## Examples                                           
### Config the sample

1. Update [application.properties](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-servicebus-jms-queue/src/main/resources/application.properties)

    ```properties
    # Fill service bus namespace connection string copied from portal
    spring.jms.servicebus.connection-string=[servicebus-namespace-connection-string]

    # The idle timeout in milliseconds after which the connection will be failed if the peer sends no AMQP frames
    # Default is 1800000
    spring.jms.servicebus.idle-timeout=[idle-timeout]
    ```

2. Specify your queue name. Update `QUEUE_NAME` in [QueueSendController](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-servicebus-jms-queue/src/main/java/com/azure/jms/queue/QueueSendController.java) and [QueueReceiveController](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-servicebus-jms-queue/src/main/java/com/azure/jms/queue/QueueReceiveController.java).
                                                                                          
### How to run

1. Run with Maven
    ```
    # Under sdk/spring project root directory
    mvn clean install -DskipTests
    cd azure-spring-boot-samples/azure-spring-boot-sample-servicebus-jms-queue
    mvn spring-boot:run
    ```

2. Send a POST request to service bus queue.
    ```
    $ curl -X POST localhost:8080/queue?message=hello
    ```
    
3. Verify in your app's logs that a similar message was posted:
    ```
    Sending message
    Received message from queue: hello
    ```
    
4. Delete the resources on [Azure Portal](http://ms.portal.azure.com/) to avoid extra charges.

## Troubleshooting
## Next steps
Please check the following table for reference links of detailed Service Bus usage. 

Type | Reference Link
--- | ---
`Queues` | [https://docs.microsoft.com/azure/service-bus-messaging/service-bus-java-how-to-use-queues](https://docs.microsoft.com/azure/service-bus-messaging/service-bus-java-how-to-use-queues)

## Contributing

<!-- LINKS -->
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
