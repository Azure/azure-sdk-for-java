# Azure Service Bus JMS Spring Boot Starter client library for Java

With this starter you could easily use Spring JMS Queue and Topic with Azure Service Bus.

[Package (Maven)][package] | [API reference documentation][refdocs] | [Product documentation][docs] | [Samples][sample]

## Getting started
### Prerequisites
- Java Development Kit (JDK) with version 8 or above
- [Azure Subscription][azure_subscription]
- [Maven](http://maven.apache.org/) 3.0 and above

### Include the package
[//]: # ({x-version-update-start;com.microsoft.azure:azure-servicebus-jms-spring-boot-starter;current})
```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure-servicebus-jms-spring-boot-starter</artifactId>
    <version>2.3.3</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts
This starter uses Azure Service Bus messaging features (queues and publish/subscribe topics) from Java applications using the popular Java Message Service (JMS) API standard with AMQP 1.0.

The Advanced Message Queuing Protocol (AMQP) 1.0 is an efficient, reliable, wire-level messaging protocol that you can use to build robust, cross-platform messaging applications.

Support for AMQP 1.0 in Azure Service Bus means that you can use the queuing and publish/subscribe brokered messaging features from a range of platforms using an efficient binary protocol. Furthermore, you can build applications comprised of components built using a mix of languages, frameworks, and operating systems.
## Examples

### Configure the app for your service bus
In this section, you see how to configure your app to use either a Service Bus queue or topic.

#### Use a Service Bus queue

Append the following code to the end of the *application.properties* file. Replace the sample values with the appropriate values for your service bus:

```yaml
spring.jms.servicebus.connection-string=<ServiceBusNamespaceConnectionString>
spring.jms.servicebus.idle-timeout=<IdleTimeout>
```

#### Use Service Bus topic

Append the following code to the end of the *application.properties* file. Replace the sample values with the appropriate values for your service bus:

```yaml
spring.jms.servicebus.connection-string=<ServiceBusNamespaceConnectionString>
spring.jms.servicebus.topic-client-id=<ServiceBusTopicClientId>
spring.jms.servicebus.idle-timeout=<IdleTimeout>
```

### Implement basic Service Bus functionality

In this section, you create the necessary Java classes for sending messages to your Service Bus queue or topic and receive messages from your corresponding queue or topic subscription.

#### Define a test Java class

Create a Java file named *User.java* in the package directory of your app. Define a generic user class that stores and retrieves user's name:
<!-- embedme ../azure-spring-boot/src/samples/java/com/azure/spring/jms/User.java#L11-L30 -->

```java
import java.io.Serializable;

public class User implements Serializable {

    private static final long serialVersionUID = -295422703255886286L;
    private String name;

    User(String name) {
        setName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
```

`Serializable` is implemented to use the `send` method in `JmsTemplate` in the Spring framework. Otherwise, a customized `MessageConverter` bean should be defined to serialize the content to json in text format. For more information about `MessageConverter`, see the official [Spring JMS starter project](https://spring.io/guides/gs/messaging-jms/).

#### Create a new class for the message send controller

1. Create a Java file named *SendController.java* in the package directory of your app. Add the following code to the new file:
<!-- embedme ../azure-spring-boot/src/samples/java/com/azure/spring/jms/SendController.java#L11-L35 -->

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SendController {

    private static final String DESTINATION_NAME = "<DestinationName>";

    private static final Logger LOGGER = LoggerFactory.getLogger(SendController.class);

    @Autowired
    private JmsTemplate jmsTemplate;

    @PostMapping("/messages")
    public String postMessage(@RequestParam String message) {
        LOGGER.info("Sending message");
        jmsTemplate.convertAndSend(DESTINATION_NAME, new User(message));
        return message;
    }
}
```

> [!NOTE]
> Replace `<DestinationName>` with your own queue name or topic name configured in your Service Bus namespace.

#### Create a class for the message receive controller

- Receive messages from a Service Bus queue

    Create a Java file named *QueueReceiveController.java* in the package directory of your app. Add the following code to the new file:
<!-- embedme ../azure-spring-boot/src/samples/java/com/azure/spring/jms/QueueReceiveController.java#L11-L27 -->

    ```java
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.jms.annotation.JmsListener;
    import org.springframework.stereotype.Component;
    
    @Component
    public class QueueReceiveController {
    
        private static final String QUEUE_NAME = "<ServiceBusQueueName>";
    
        private final Logger logger = LoggerFactory.getLogger(QueueReceiveController.class);
    
        @JmsListener(destination = QUEUE_NAME, containerFactory = "jmsListenerContainerFactory")
        public void receiveMessage(User user) {
            logger.info("Received message: {}", user.getName());
        }
    }
    ```

    > [!NOTE]
    > Replace `<ServiceBusQueueName>` with your own queue name configured in your Service Bus namespace.

- Receive messages from a Service Bus subscription

    Create a Java file named *TopicReceiveController.java* in the package directory of your app. Add the following code to the new file. Replace the `<ServiceBusTopicName>` placeholder with your own topic name configured in your Service Bus namespace. Replace the `<ServiceBusSubscriptionName>` placeholder with your own subscription name for your Service Bus topic.
<!-- embedme ../azure-spring-boot/src/samples/java/com/azure/spring/jms/TopicReceiveController.java#L11-L30 -->

    ```java
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.jms.annotation.JmsListener;
    import org.springframework.stereotype.Component;
    
    @Component
    public class TopicReceiveController {
    
        private static final String TOPIC_NAME = "<ServiceBusTopicName>";
    
        private static final String SUBSCRIPTION_NAME = "<ServiceBusSubscriptionName>";
    
        private final Logger logger = LoggerFactory.getLogger(TopicReceiveController.class);
    
        @JmsListener(destination = TOPIC_NAME, containerFactory = "topicJmsListenerContainerFactory",
            subscription = SUBSCRIPTION_NAME)
        public void receiveMessage(User user) {
            logger.info("Received message: {}", user.getName());
        }
    }
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
### Enable client logging
Azure SDKs for Java offer a consistent logging story to help aid in troubleshooting application errors and expedite their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

### Enable Spring logging
Spring allow all the supported logging systems to set logger levels set in the Spring Environment (for example, in application.properties) by using `logging.level.<logger-name>=<level>` where level is one of TRACE, DEBUG, INFO, WARN, ERROR, FATAL, or OFF. The root logger can be configured by using logging.level.root.

The following example shows potential logging settings in `application.properties`:

```
logging.level.root=WARN
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate=ERROR
```

For more information about setting loging in pring, please refer to the [official doc](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-logging).
 

## Next steps
The following section provides sample projects illustrating how to use the starter in different cases.
### More sample code
- [JMS Service Bus Queue](../azure-spring-boot-samples/azure-spring-boot-sample-servicebus-jms-queue)
- [JMS Service Bus Topic](../azure-spring-boot-samples/azure-spring-boot-sample-servicebus-jms-topic)


## Contributing
This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [instructions here](../CONTRIBUTING.md) to build from source or contribute.

<!-- LINKS -->
[docs]: https://docs.microsoft.com/azure/developer/java/spring-framework/configure-spring-boot-starter-java-app-with-azure-service-bus
[refdocs]: https://azure.github.io/azure-sdk-for-java/spring.html#azure-servicebus-jms-spring-boot-starter
[package]: https://mvnrepository.com/artifact/com.microsoft.azure/azure-servicebus-jms-spring-boot-starter
[sample]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-boot-samples/
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK#use-logback-logging-framework-in-a-spring-boot-application
[azure_subscription]: https://azure.microsoft.com/free
