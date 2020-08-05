# Azure Service Bus JMS Spring Boot Starter client library for Java

With this starter you could easily use Spring JMS Queue and Topic with Azure Service Bus.

## Getting started

### Add the dependency

`azure-servicebus-jms-spring-boot-starter` is published on Maven Central Repository.  
Add the following dependency to your project:

[//]: # ({x-version-update-start;com.azure:azure-spring-boot-metrics-starter;current})
```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure-servicebus-jms-spring-boot-starter</artifactId>
    <version>2.3.3-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts
This starter uses Azure Service Bus messaging features (queues and publish/subscribe topics) from Java applications using the popular Java Message Service (JMS) API standard with AMQP 1.0.

The Advanced Message Queuing Protocol (AMQP) 1.0 is an efficient, reliable, wire-level messaging protocol that you can use to build robust, cross-platform messaging applications.

Support for AMQP 1.0 in Azure Service Bus means that you can use the queuing and publish/subscribe brokered messaging features from a range of platforms using an efficient binary protocol. Furthermore, you can build applications comprised of components built using a mix of languages, frameworks, and operating systems.
## Examples
These code [Service Bus Queue Sample](../../azure-spring-boot-samples/azure-spring-boot-sample-servicebus-jms-queue/) 
and [Service Bus Topic Sample](../../azure-spring-boot-samples/azure-spring-boot-sample-servicebus-jms-topic/) 
respectively demonstrates how to use Spring JMS Queue and Topic for Azure Service Bus via the Starter.

Running these samples will be charged by Azure. You can check the usage and bill at this [link](https://azure.microsoft.com/account/).


## Troubleshooting
If the JDK version you use is greater than 1.8, You may meet this problem: 
```
NoClassDefFoundError: javax/xml/bind/JAXBException
```

To solve this issue, you need to add the dependency below into your classpath:
```
<dependency>
   <groupId>javax.xml.bind</groupId>
   <artifactId>jaxb-api</artifactId>
   <version>2.3.0</version>
</dependency>
```

## Next steps

### Allow telemetry
Microsoft would like to collect data about how users use this Spring boot starter. Microsoft uses this information to improve our tooling experience. Participation is voluntary. If you don't want to participate, just simply disable it by setting below configuration in `application.properties`.
```
azure.servicebus.allow-telemetry=false
```
When telemetry is enabled, an HTTP request will be sent to URL `https://dc.services.visualstudio.com/v2/track`. So please make sure it's not blocked by your firewall.  
Find more information about Azure Service Privacy Statement, please check [Microsoft Online Services Privacy Statement](https://www.microsoft.com/privacystatement/OnlineServices/Default.aspx). 

## Contributing
This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [instructions here](../CONTRIBUTING.md) to build from source or contribute.
