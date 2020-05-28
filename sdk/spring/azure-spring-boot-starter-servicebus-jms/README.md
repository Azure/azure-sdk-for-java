#Azure Service Bus JMS Spring Boot Starter client library for Java

## Key concepts
With this starter you could easily use Spring JMS Queue and Topic with Azure Service Bus.

### Java agent based monitoring

Azure Application Insights has released one Java agent that can be used to capture metrics inside JVM. Consider use the Java agent based approach. 

Refer following document for more details:

https://docs.microsoft.com/azure/azure-monitor/app/java-in-process-agent


## Getting started

### Add the dependency

`azure-servicebus-jms-spring-boot-starter` is published on Maven Central Repository.  
Add the following dependency to your project:

[//]: # ({x-version-update-start;com.azure:azure-spring-boot-metrics-starter;current})
```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure-servicebus-jms-spring-boot-starter</artifactId>
    <version>2.2.5-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})


## Examples
These code [Service Bus Queue Sample](../../azure-spring-boot-samples/azure-spring-boot-sample-servicebus-jms-queue/) 
and [Service Bus Topic Sample](../../azure-spring-boot-samples/azure-spring-boot-sample-servicebus-jms-topic/) 
respectively demonstrates how to use Spring JMS Queue and Topic for Azure Service Bus via the Starter.

Running these samples will be charged by Azure. You can check the usage and bill at this [link](https://azure.microsoft.com/account/).


## Troubleshooting

## Next steps

### Allow telemetry
Microsoft would like to collect data about how users use this Spring boot starter. Microsoft uses this information to improve our tooling experience. Participation is voluntary. If you don't want to participate, just simply disable it by setting below configuration in `application.properties`.
```
azure.servicebus.allow-telemetry=false
```
When telemetry is enabled, an HTTP request will be sent to URL `https://dc.services.visualstudio.com/v2/track`. So please make sure it's not blocked by your firewall.  
Find more information about Azure Service Privacy Statement, please check [Microsoft Online Services Privacy Statement](https://www.microsoft.com/privacystatement/OnlineServices/Default.aspx). 

## Contributing
