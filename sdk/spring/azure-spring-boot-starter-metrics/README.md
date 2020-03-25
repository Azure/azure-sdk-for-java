#Azure Metrics Spring Boot Starter client library for Java

## Getting started

### Add the dependency

`azure-spring-boot-starter-metrics` is published on Maven Central Repository.  
If you are using Maven, add the following dependency.  

[//]: # ({x-version-update-start;com.azure:azure-spring-boot-starter-metrics;dependency})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-spring-boot-starter-metrics</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Add the property setting

Open `application.properties` file and add below properties with your instrumentation key.

```
management.metrics.export.azuremonitor.instrumentation-key=<your-instrumentation-key-here>
```

## Troubleshooting
1. If the JDK version you use is greater than 1.8, You may meet this problem: 
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

## Key concepts
## Examples
## Next steps
## Contributing
