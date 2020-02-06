## Usage

It is Azure Service Bus JMS Spring Boot Starter.

These code [Service Bus Queue Sample](../../azure-spring-boot-samples/azure-servicebus-jms-queue-spring-boot-sample/) and [Service Bus Topic Sample](../../azure-spring-boot-samples/azure-servicebus-jms-topic-spring-boot-sample/) respectively demonstrates how to use Spring JMS Queue and Topic for Azure Service Bus via the Starter.

Running these samples will be charged by Azure. You can check the usage and bill at this [link](https://azure.microsoft.com/en-us/account/).

### Add the dependency

`azure-servicebus-jms-spring-boot-starter` is published on Maven Central Repository.  
Add the following dependency to your project:

```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure-servicebus-jms-spring-boot-starter</artifactId>
    <version>2.1.7</version>
</dependency>
```

### Allow telemetry
Microsoft would like to collect data about how users use this Spring boot starter. Microsoft uses this information to improve our tooling experience. Participation is voluntary. If you don't want to participate, just simply disable it by setting below configuration in `application.properties`.
```
azure.servicebus.allow-telemetry=false
```
When telemetry is enabled, an HTTP request will be sent to URL `https://dc.services.visualstudio.com/v2/track`. So please make sure it's not blocked by your firewall.  
Find more information about Azure Service Privacy Statement, please check [Microsoft Online Services Privacy Statement](https://www.microsoft.com/en-us/privacystatement/OnlineServices/Default.aspx). 
