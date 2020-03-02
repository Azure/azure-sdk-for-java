## Overview
This sample project demonstrates how to use Service Bus via Spring Boot Starter `azure-servicebus-spring-boot-starter`. 

## Prerequisites

* An Azure subscription; if you don't already have an Azure subscription, you can activate your [MSDN subscriber benefits](https://azure.microsoft.com/en-us/pricing/member-offers/msdn-benefits-details/) or sign up for a [free Azure account](https://azure.microsoft.com/en-us/free/).

* A [Java Development Kit (JDK)](http://www.oracle.com/technetwork/java/javase/downloads/), version 1.8.

* [Apache Maven](http://maven.apache.org/), version 3.0 or later.

## Quick Start

### Create Service Bus on Azure

1. Go to [Azure portal](https://portal.azure.com/) and create the service by following this [link](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-create-namespace-portal). 
2. Mark down the `Primary Connection String`.
3. In the `Overview` blade, create queue and topic. Mark down your queue name and topic name. 
4. Click your created topic, add subscription in the `Subscriptions` blade. Mark down your subscription name.
                                                                                                                                                                                                  
### Config the sample

1. Navigate to `src/main/resources` and open `application.properties`.
2. Fill in the `connection-string`,  `queue-name`,`topic-name` and `subscription-name`. 

### Run the sample

1. Change directory to folder `azure-servicebus-spring-boot-sample`.
2. Run below commands. 

   - Use Maven 

     ```
     mvn package
     java -jar target/azure-servicebus-spring-boot-sample-0.0.1-SNAPSHOT.jar
     ```

   - Use Gradle 
   
     ```
     gradle bootJar
     java -jar build/libs/azure-servicebus-spring-boot-sample-0.0.1-SNAPSHOT.jar
     ```

### More usage

Please check the following table for reference links of detailed Service Bus usage. 

Type | Reference Link
--- | ---
`Queues` | [https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-java-how-to-use-queues](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-java-how-to-use-queues)
`Topics` | [https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-java-how-to-use-topics-subscriptions](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-java-how-to-use-topics-subscriptions)
`Subscriptions` | [https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-java-how-to-use-topics-subscriptions](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-java-how-to-use-topics-subscriptions)
