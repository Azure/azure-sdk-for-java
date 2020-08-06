# Azure Metrics Spring Boot Starter client library for Java

With this starter you could easily use Azure Metrics with Spring Boot.

## Key concepts
**Java agent based monitoring**: Azure Application Insights has released one Java agent that can be used to capture metrics inside JVM. Consider use the Java agent based approach. 

Refer following document for more details:

https://docs.microsoft.com/azure/azure-monitor/app/java-in-process-agent

## Getting started
### Add the dependency

`azure-spring-boot-metrics-starter` is published on Maven Central Repository.  
If you are using Maven, add the following dependency.  

[//]: # ({x-version-update-start;com.azure:azure-spring-boot-metrics-starter;current})
```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure-spring-boot-metrics-starter</artifactId>
    <version>2.3.3-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Add the property setting

Open `application.properties` file and add below properties with your instrumentation key.

```
management.metrics.export.azuremonitor.instrumentation-key=<your-instrumentation-key-here>
```

## Examples

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
azure.keyvault.allow.telemetry=false
```
When telemetry is enabled, an HTTP request will be sent to URL `https://dc.services.visualstudio.com/v2/track`. So please make sure it's not blocked by your firewall.    
Find more information about Azure Service Privacy Statement, please check [Microsoft Online Services Privacy Statement](https://www.microsoft.com/privacystatement/OnlineServices/Default.aspx). 

## Contributing
This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [instructions here](../CONTRIBUTING.md) to build from source or contribute.
