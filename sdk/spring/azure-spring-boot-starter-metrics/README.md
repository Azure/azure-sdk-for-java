#Azure Metrics Spring Boot Starter client library for Java

## Java agent based monitoring

Azure Application Insights has released one Java agent that can be used to capture metrics inside JVM. Consider use the Java agent based approach. 

Refer following document for more details:

https://docs.microsoft.com/azure/azure-monitor/app/java-in-process-agent


## Key concepts

## Getting started

## Examples
### Add the dependency

`azure-spring-boot-metrics-starter` is published on Maven Central Repository.  
If you are using Maven, add the following dependency.  

[//]: # ({x-version-update-start;com.azure:azure-spring-boot-metrics-starter;current})
```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure-spring-boot-metrics-starter</artifactId>
    <version>2.2.5-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Add the property setting

Open `application.properties` file and add below properties with your instrumentation key.

```
management.metrics.export.azuremonitor.instrumentation-key=<your-instrumentation-key-here>
```

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
## Contributing
