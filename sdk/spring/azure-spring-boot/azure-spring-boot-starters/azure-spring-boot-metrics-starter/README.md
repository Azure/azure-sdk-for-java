## Usage

### Add the dependency

`azure-spring-boot-metrics-starter` is published on Maven Central Repository.  
If you are using Maven, add the following dependency.  

```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
   <artifactId>azure-spring-boot-metrics-starter</artifactId>
    <version>2.2.0</version>
</dependency>
```

### Add the property setting

Open `application.properties` file and add below properties with your instrumentation key.

```
management.metrics.export.azuremonitor.instrumentation-key=<your-instrumentation-key-here>
```

### Trouble Shooting
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
