# Overview
In Spring Cloud Azure 4.0 we are going to reorganize Spring Cloud Azure Messaging projects to follow Spring abstraction of messaging projects, in order to make our projects easy to be integrated with more features of Spring abstraction.

# Problems
1. Flat and over-aggregated project structure.
   - All fundamental classes are placed in azure-spring-integration-core neglecting their internal hierarchical relationships.
   - Customers cannot follow Spring abstraction to use associated APIs from current Spring Cloud Azure Messaging modules.
2. Unreasonable dependency relationships violating Spring conventions.
   - azure-spring-cloud-messaging depends on azure-spring-integration-core, which is incorrect and is confusing to users.

# Goals
Make the structure of Spring Cloud Azure Messaging projects follow Spring conventions, to be specific:
1. Spring Cloud Azure Messaging projects should have similar sub-modules aligned to Spring, including Spring Messaging, Spring Integration and Spring Cloud Stream.
2. The dependency relationships of Spring Cloud Azure Messaging projects should be aligned with Spring convention.
3. The way current classes being organized should follow associated Spring projects.

# Dependency relationship maps in this doc.
1. Explanation of line type.
   - **Solid lines**: [transitive-dependencies].
   - **Dashed lines**: [optional-dependencies].
2. The dependency is not fixed all the time, 
   it's OK to add new dependencies when developing related module.
   The new added dependency should follow the design rule.

# Project Structure analysis
The current project structure of Spring Cloud Azure Messaging is not ideal and needs reorganization. To achieve the above goals, we analyze the projects of Spring Kafka, Spring Integration support of Apache Kafka and Spring Cloud Stream support of Apache Kafka, and thus design the structure for Spring Cloud Azure Messaging of Event Hubs, Service Bus and Storage Queue.
 
## Spring support for Azure Event Hubs
The project of Spring Cloud Azure Messaging for Event Hubs is expected to follow the convention of Spring support for Kafka. Thus the project structure is designed as below:

![mermaid-diagram-20211013135354](https://user-images.githubusercontent.com/63028776/137075027-a819bc3a-9e8f-4469-84c0-07bf3687f485.png)

[Edit: Spring support Event Hubs]

## Spring support for Azure Service Bus
This project covers two sub projects, one is to provide spring support for native Service Bus service, the other is to support Service Bus from JMS API.

### Spring support for native Service Bus
The project of Spring Cloud Azure Messaging for Service Bus is expected to follow the convention of Spring support for Kafka. Thus the project structure is designed as below:

![mermaid-diagram-20211013135242](https://user-images.githubusercontent.com/63028776/137074908-abb66807-4a51-4b99-929d-1fee3eb8baf2.png)

[Edit: Spring support Service Bus]

### Spring JMS support for Service Bus
This project is aimed to provide Spring JMS support to interact with Service Bus with JMS API. The project structure is designed as below:

![mermaid-diagram-20211013140431](https://user-images.githubusercontent.com/63028776/137076126-67b5937e-0bea-4f10-898a-c03cab85a1b9.png)

[Edit: Spring JMS support Service Bus]

## Spring support for Azure Storage Queue
The current project of Spring Cloud Azure Messaging for Storage Queue only involves the integration of Spring Messaging and Spring Integration, the structure is designed as below:

![mermaid-diagram-20211013140153](https://user-images.githubusercontent.com/63028776/137075852-1cb5858c-0c9a-4bce-89fe-9904445c4fc6.png)

[Edit: Spring support Storage Queue]

Note: we need further investigation on whether it can support Spring Cloud Stream.

# Module introduction
With reference of Spring support for Apache Kafka, we reorganize the structure of Spring Cloud Azure Messaging modules to meet the goal of 4.0. Modules include spring messaging azure, spring integration azure and spring cloud azure stream binder.
## Azure support for Spring Messaging
This project covers three Azure services: Azure Event Hubs, Azure Service Bus and Azure Storage Queue. For each service, the Azure support for Spring Messaging should contain two modules: a core module implements Spring Messaging abstraction and common utils applied with all Azure Messaging services, the other module implements customization with each service and client factories.
### Spring Messaging Azure 
The module is based on spring-messaging and should provide the following components:
- Message Converter to transfer between Spring message and service message
- Message header
- Exception of message handling
- Checkpointer to abstract the check point behavior of different Azure services

### Spring Messaging for Azure services
We should have three sub modules as 
- spring-messaging-azure-eventhubs
- spring-messaging-azure-servicebus
- spring-messaging-azure-storage-queue

On the basis of spring-messaging-azure components, the above modules should also contains the following components:
- Client Factory to build clients
- Operation to perform different message publishing and receiving operation

## Azure support for Spring Integration
This project covers three Azure services: Azure Event Hubs, Azure Service Bus and Azure Storage Queue. For each service, the Azure support for Spring Integration should contain two modules: a core module implements basic Spring Integration abstraction and the other module implements customization with each service.
### Spring Integration Azure Core
The module is based on spring-integration-core and should provide the following components:
- Inbound Channel Adapter to receive messages from the broker
- Message Handler to publish messages from the broker

### Azure Spring Integration for services
We should have three sub modules as 
- spring-integration-azure-eventhubs
- spring-integration-azure-servicebus
- spring-integration-azure-storage-queue

## Azure support for Spring Cloud Stream Binder 
This project covers two Azure services: Azure Event Hubs and Azure Service Bus. For each service, the Azure support for Spring Cloud Stream should contain two modules: a core module containing configuration properties and a binder module for SCS abstraction, health indicator and autoconfiguration.
### Spring Cloud Azure Stream Binder Core
We should have two modules as 
- spring-cloud-azure-stream-binder-eventhubs-core
- spring-cloud-azure-stream-binder-servicebus-core

The core module should provide the following components:
- Binding properties, which is to describe extended consumer/producer properties for Azure services
- Provisioner that allow users to provision destinations
- Destination that describe the broker destination

### Spring Cloud Azure Stream Binder
We should have two modules as 
- spring-cloud-azure-stream-binder-eventhub
- spring-cloud-azure-stream-binder-servicebus

The binder module should provide the following components:
- Message Channel Binder that integrate with SCS binder abstraction
- Autoconfiguration and customizer to provide binder configuration and enable to customize each clients
- Health Indicator

## Azure support for Spring JMS
We should have a module of
- azure-spring-servicebus-jms

This module aims to provide Spring JMS implemention integrated with Service Bus, which should provide the following components:
- All kinds of configuration provider
- Connection Factory to provide amqp connection to Service Bus

[transitive-dependencies]: https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#transitive-dependencies
[optional-dependencies]: https://maven.apache.org/guides/introduction/introduction-to-optional-and-excludes-dependencies.html#optional-dependencies
[Edit: Spring support Event Hubs]: https://mermaid.live/edit#pako:#eyJjb2RlIjoiZ3JhcGggVERcblxuc3ByaW5nLWNsb3VkLWF6dXJlLXN0cmVhbS1iaW5kZXItZXZlbnRodWJzIC0tPiBzcHJpbmctY2xvdWQtYXp1cmUtc3RyZWFtLWJpbmRlci1ldmVudGh1YnMtY29yZVxuc3ByaW5nLWNsb3VkLWF6dXJlLXN0cmVhbS1iaW5kZXItZXZlbnRodWJzIC0uLT4gc3ByaW5nMVtzcHJpbmctYm9vdC1zdGFydGVyLWFjdHVhdG9yXVxuc3ByaW5nLWNsb3VkLWF6dXJlLXN0cmVhbS1iaW5kZXItZXZlbnRodWJzIC0uLT4gc3ByaW5nMltzcHJpbmctYm9vdC1jb25maWd1cmF0aW9uLXByb2Nlc3Nvcl1cbnNwcmluZy1jbG91ZC1henVyZS1zdHJlYW0tYmluZGVyLWV2ZW50aHVicyAtLi0-IHNwcmluZy1jbG91ZC1henVyZS1hdXRvY29uZmlndXJlXG5zdHlsZSBzcHJpbmcxIGZpbGw6IzZEQjMzRixzdHJva2U6IzQ4NzUyOVxuc3R5bGUgc3ByaW5nMiBmaWxsOiM2REIzM0Ysc3Ryb2tlOiM0ODc1Mjlcblxuc3ByaW5nLWNsb3VkLWF6dXJlLXN0cmVhbS1iaW5kZXItZXZlbnRodWJzLWNvcmUgLS0-IHNwcmluZzNbc3ByaW5nLWNsb3VkLXN0cmVhbV1cbnNwcmluZy1jbG91ZC1henVyZS1zdHJlYW0tYmluZGVyLWV2ZW50aHVicy1jb3JlIC0tPiBzcHJpbmctaW50ZWdyYXRpb24tYXp1cmUtZXZlbnRodWJzXG5zdHlsZSBzcHJpbmczIGZpbGw6IzZEQjMzRixzdHJva2U6IzQ4NzUyOVxuXG5zcHJpbmctaW50ZWdyYXRpb24tYXp1cmUtZXZlbnRodWJzIC0tPiBzcHJpbmctaW50ZWdyYXRpb24tYXp1cmUtY29yZVxuc3ByaW5nLWludGVncmF0aW9uLWF6dXJlLWV2ZW50aHVicyAtLT4gc3ByaW5nLW1lc3NhZ2luZy1henVyZS1ldmVudGh1YnNcbnNwcmluZy1pbnRlZ3JhdGlvbi1henVyZS1jb3JlIC0tPiBzcHJpbmc0W3NwcmluZy1pbnRlZ3JhdGlvbi1jb3JlXVxuc3R5bGUgc3ByaW5nNCBmaWxsOiM2REIzM0Ysc3Ryb2tlOiM0ODc1Mjlcblxuc3ByaW5nLW1lc3NhZ2luZy1henVyZS1ldmVudGh1YnMgLS0-IHNwcmluZy1tZXNzYWdpbmctYXp1cmVcbnNwcmluZy1tZXNzYWdpbmctYXp1cmUtZXZlbnRodWJzIC0tPiBhenVyZVthenVyZS1tZXNzYWdpbmctZXZlbnRodWJzXVxuc3ByaW5nLW1lc3NhZ2luZy1henVyZSAtLT4gc3ByaW5nNVtzcHJpbmctbWVzc2FnaW5nXVxuc3R5bGUgYXp1cmUgZmlsbDojMkZBNkZGLHN0cm9rZTojMDA1REEyXG5zdHlsZSBzcHJpbmc1IGZpbGw6IzZEQjMzRixzdHJva2U6IzQ4NzUyOSIsIm1lcm1haWQiOiJ7XG4gIFwidGhlbWVcIjogXCJkZWZhdWx0XCJcbn0iLCJ1cGRhdGVFZGl0b3IiOmZhbHNlLCJhdXRvU3luYyI6ZmFsc2UsInVwZGF0ZURpYWdyYW0iOmZhbHNlfQ
[Edit: Spring support Service Bus]: https://mermaid.live/edit#pako:#eyJjb2RlIjoiZ3JhcGggVERcblxuc3ByaW5nLWNsb3VkLWF6dXJlLXN0cmVhbS1iaW5kZXItc2VydmljZWJ1cyAtLT4gc3ByaW5nLWNsb3VkLWF6dXJlLXN0cmVhbS1iaW5kZXItc2VydmljZWJ1cy1jb3JlXG5zcHJpbmctY2xvdWQtYXp1cmUtc3RyZWFtLWJpbmRlci1zZXJ2aWNlYnVzIC0uLT4gc3ByaW5nMVtzcHJpbmctYm9vdC1zdGFydGVyLWFjdHVhdG9yXVxuc3ByaW5nLWNsb3VkLWF6dXJlLXN0cmVhbS1iaW5kZXItc2VydmljZWJ1cyAtLi0-IHNwcmluZzJbc3ByaW5nLWJvb3QtY29uZmlndXJhdGlvbi1wcm9jZXNzb3JdXG5zcHJpbmctY2xvdWQtYXp1cmUtc3RyZWFtLWJpbmRlci1zZXJ2aWNlYnVzIC0uLT4gc3ByaW5nLWNsb3VkLWF6dXJlLWF1dG9jb25maWd1cmVcbnN0eWxlIHNwcmluZzEgZmlsbDojNkRCMzNGLHN0cm9rZTojNDg3NTI5XG5zdHlsZSBzcHJpbmcyIGZpbGw6IzZEQjMzRixzdHJva2U6IzQ4NzUyOVxuXG5zcHJpbmctY2xvdWQtYXp1cmUtc3RyZWFtLWJpbmRlci1zZXJ2aWNlYnVzLWNvcmUgLS0-IHNwcmluZzNbc3ByaW5nLWNsb3VkLXN0cmVhbV1cbnNwcmluZy1jbG91ZC1henVyZS1zdHJlYW0tYmluZGVyLXNlcnZpY2VidXMtY29yZSAtLT4gc3ByaW5nLWludGVncmF0aW9uLWF6dXJlLXNlcnZpY2VidXNcbnN0eWxlIHNwcmluZzMgZmlsbDojNkRCMzNGLHN0cm9rZTojNDg3NTI5XG5cbnNwcmluZy1pbnRlZ3JhdGlvbi1henVyZS1zZXJ2aWNlYnVzIC0tPiBzcHJpbmctaW50ZWdyYXRpb24tYXp1cmUtY29yZVxuc3ByaW5nLWludGVncmF0aW9uLWF6dXJlLXNlcnZpY2VidXMgLS0-IHNwcmluZy1tZXNzYWdpbmctYXp1cmUtc2VydmljZWJ1c1xuc3ByaW5nLWludGVncmF0aW9uLWF6dXJlLWNvcmUgLS0-IHNwcmluZzRbc3ByaW5nLWludGVncmF0aW9uLWNvcmVdXG5zdHlsZSBzcHJpbmc0IGZpbGw6IzZEQjMzRixzdHJva2U6IzQ4NzUyOVxuXG5zcHJpbmctbWVzc2FnaW5nLWF6dXJlLXNlcnZpY2VidXMgLS0-IHNwcmluZy1tZXNzYWdpbmctYXp1cmVcbnNwcmluZy1tZXNzYWdpbmctYXp1cmUtc2VydmljZWJ1cyAtLT4gYXp1cmVbYXp1cmUtbWVzc2FnaW5nLXNlcnZpY2VidXNdXG5zcHJpbmctbWVzc2FnaW5nLWF6dXJlIC0tPiBzcHJpbmc1W3NwcmluZy1tZXNzYWdpbmddXG5zdHlsZSBhenVyZSBmaWxsOiMyRkE2RkYsc3Ryb2tlOiMwMDVEQTJcbnN0eWxlIHNwcmluZzUgZmlsbDojNkRCMzNGLHN0cm9rZTojNDg3NTI5IiwibWVybWFpZCI6IntcbiAgXCJ0aGVtZVwiOiBcImRlZmF1bHRcIlxufSIsInVwZGF0ZUVkaXRvciI6ZmFsc2UsImF1dG9TeW5jIjpmYWxzZSwidXBkYXRlRGlhZ3JhbSI6ZmFsc2V9
[Edit: Spring JMS support Service Bus]: https://mermaid.live/edit#pako:#eyJjb2RlIjoiZ3JhcGggVERcblxuc3ByaW5nLWNsb3VkLWF6dXJlLXN0YXJ0ZXItc2VydmljZWJ1cy1qbXMgLS0-IHNwcmluZy1jbG91ZC1henVyZS1zdGFydGVyXG5zcHJpbmctY2xvdWQtYXp1cmUtc3RhcnRlciAtLT4gYXp1cmUyW3NwcmluZy1jbG91ZC1henVyZS1hdXRvY29uZmlndXJlXVxuYXp1cmUyW3NwcmluZy1jbG91ZC1henVyZS1hdXRvY29uZmlndXJlXSAtLi0-IGF6dXJlMVthenVyZS1zZXJ2aWNlYnVzLWptc11cbmF6dXJlMltzcHJpbmctY2xvdWQtYXp1cmUtYXV0b2NvbmZpZ3VyZV0gLS4tPiBzcHJpbmcxW3NwcmluZy1qbXNdXG5zdHlsZSBzcHJpbmcxIGZpbGw6IzZEQjMzRixzdHJva2U6IzQ4NzUyOVxuc3R5bGUgYXp1cmUxIGZpbGw6IzJGQTZGRixzdHJva2U6IzAwNURBMlxuc3R5bGUgYXp1cmUyIGZpbGw6IzJGQTZGRixzdHJva2U6IzAwNURBMiIsIm1lcm1haWQiOiJ7XG4gIFwidGhlbWVcIjogXCJkZWZhdWx0XCJcbn0iLCJ1cGRhdGVFZGl0b3IiOmZhbHNlLCJhdXRvU3luYyI6ZmFsc2UsInVwZGF0ZURpYWdyYW0iOmZhbHNlfQ
[Edit: Spring support Storage Queue]: https://mermaid.live/edit#pako:#eyJjb2RlIjoiZ3JhcGggVERcblxuc3ByaW5nLWludGVncmF0aW9uLWF6dXJlLXN0b3JhZ2UtcXVldWUgLS0-IHNwcmluZy1pbnRlZ3JhdGlvbi1henVyZS1jb3JlXG5zcHJpbmctaW50ZWdyYXRpb24tYXp1cmUtc3RvcmFnZS1xdWV1ZSAtLT4gc3ByaW5nLW1lc3NhZ2luZy1henVyZS1zdG9yYWdlLXF1ZXVlXG5zcHJpbmctaW50ZWdyYXRpb24tYXp1cmUtY29yZSAtLT4gc3ByaW5nNFtzcHJpbmctaW50ZWdyYXRpb24tY29yZV1cbnN0eWxlIHNwcmluZzQgZmlsbDojNkRCMzNGLHN0cm9rZTojNDg3NTI5XG5cbnNwcmluZy1tZXNzYWdpbmctYXp1cmUtc3RvcmFnZS1xdWV1ZSAtLT4gc3ByaW5nLW1lc3NhZ2luZy1henVyZVxuc3ByaW5nLW1lc3NhZ2luZy1henVyZS1zdG9yYWdlLXF1ZXVlIC0tPiBhenVyZVthenVyZS1zdG9yYWdlLXF1ZXVlXVxuc3ByaW5nLW1lc3NhZ2luZy1henVyZSAtLT4gc3ByaW5nNVtzcHJpbmctbWVzc2FnaW5nXVxuc3R5bGUgYXp1cmUgZmlsbDojMkZBNkZGLHN0cm9rZTojMDA1REEyXG5zdHlsZSBzcHJpbmc1IGZpbGw6IzZEQjMzRixzdHJva2U6IzQ4NzUyOSIsIm1lcm1haWQiOiJ7XG4gIFwidGhlbWVcIjogXCJkZWZhdWx0XCJcbn0iLCJ1cGRhdGVFZGl0b3IiOmZhbHNlLCJhdXRvU3luYyI6ZmFsc2UsInVwZGF0ZURpYWdyYW0iOmZhbHNlfQ