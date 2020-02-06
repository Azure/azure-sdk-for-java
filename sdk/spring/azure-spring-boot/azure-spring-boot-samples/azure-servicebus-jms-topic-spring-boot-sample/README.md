## Overview

This sample project demonstrates how to use Spring JMS Topic for Azure Service Bus via Spring Boot Starter `azure-servicebus-jms-spring-boot-starter`. 

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
    
    # The JMS client id needs to be specified when using topic and durable subscription
    # Default is empty string
    spring.jms.servicebus.topic-client-id=[topic-client-id]
    
    # The idle timeout in milliseconds after which the connection will be failed if the peer sends no AMQP frames
    # Default is 1800000
    spring.jms.servicebus.idle-timeout=[idle-timeout]
    ```

3. Specify your topic name and subscription name. Update `TOPIC_NAME` in [TopicSendController](src/main/java/sample/jms/topic/TopicSendController.java) and [TopicReceiveController](src/main/java/sample/jms/topic/TopicReceiveController.java), and `SUBSCRIPTION_NAME` in [TopicReceiveController](src/main/java/sample/jms/topic/TopicReceiveController.java).

### How to run

1. Run the `mvn clean spring-boot:run` in the root of the code sample to get the app running.

2. Send a POST request to service bus topic.
    ```
    $ curl -X POST localhost:8080/topic?message=hello
    ```

3. Verify in your app's logs that a similar message was posted:
    ```
    Sending message
    Received message from topic: hello
    ```
    
4. Delete the resources on [Azure Portal](http://ms.portal.azure.com/) to avoid extra charges.

### More usage

Please check the following table for reference links of detailed Service Bus usage. 

Type | Reference Link
--- | ---
`Topics` | [https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-java-how-to-use-topics-subscriptions](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-java-how-to-use-topics-subscriptions)
`Subscriptions` | [https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-java-how-to-use-topics-subscriptions](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-java-how-to-use-topics-subscriptions)
