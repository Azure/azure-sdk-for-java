## Overview

This sample project demonstrates how to use Spring JMS for Azure Service Bus Queue via Spring Boot Starter `azure-servicebus-jms-spring-boot-starter`. 

Running this sample will be charged by Azure. You can check the usage and bill at this [link](https://azure.microsoft.com/en-us/account/).

## Prerequisites

* An Azure subscription. If you don't already have an Azure subscription, you can activate your [MSDN subscriber benefits](https://azure.microsoft.com/en-us/pricing/member-offers/msdn-benefits-details/) or sign up for a [free Azure account](https://azure.microsoft.com/en-us/free/).

* A [Java Development Kit (JDK)](http://www.oracle.com/technetwork/java/javase/downloads/), version 1.8.

* [Apache Maven](http://maven.apache.org/), version 3.0 or later.

## Quick Start

### Create Service Bus on Azure and Apply it's Properties

1. Go to [Azure portal](https://portal.azure.com/) and create the service by following this [link](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-create-namespace-portal). 

2. Update [application.properties](./src/main/resources/application.properties)

    ```
    # Fill service bus namespace connection string copied from portal
    spring.jms.servicebus.connection-string=[servicebus-namespace-connection-string]
    
    # The idle timeout in milliseconds after which the connection will be failed if the peer sends no AMQP frames
    # Default is 1800000
    spring.jms.servicebus.idle-timeout=[idle-timeout]
    ```

3. Specify your queue name. Update `QUEUE_NAME` in [QueueSendController](src/main/java/sample/jms/queue/QueueSendController.java) and [QueueReceiveController](src/main/java/sample/jms/queue/QueueReceiveController.java).
                                                                                                                                                                                    
### How to run

1. Run the `mvn clean spring-boot:run` in the root of the code sample to get the app running.

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

### More usage

Please check the following table for reference links of detailed Service Bus usage. 

Type | Reference Link
--- | ---
`Queues` | [https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-java-how-to-use-queues](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-java-how-to-use-queues)
