# Azure Service Bus JMS Spring Boot Starter client library for Java

With this starter you could easily use Spring JMS Queue and Topic with Azure Service Bus.

[Package (Maven)][package] | [API reference documentation][refdocs] | [Product documentation][docs] | [Samples][sample]

## Getting started

### Prerequisites
- [Environment checklist][environment_checklist]

### Include the package
1. [Add azure-spring-boot-bom].
1. Add dependency. `<version>` can be skipped because we already add `azure-spring-boot-bom`.
```xml
<dependency>
  <groupId>com.azure.spring</groupId>
  <artifactId>azure-spring-boot-starter-servicebus-jms</artifactId>
</dependency>
```

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
# Supported values for pricing-tier are premium, standard and basic. Premium uses Java Message Service (JMS) 2.0, while standard and basic use JMS 1.0 to interact with Azure Service Bus.
spring.jms.servicebus.pricing-tier=<ServiceBusPricingTier>
```

#### Use Service Bus topic

Append the following code to the end of the *application.properties* file. Replace the sample values with the appropriate values for your service bus:

```yaml
spring.jms.servicebus.connection-string=<ServiceBusNamespaceConnectionString>
spring.jms.servicebus.topic-client-id=<ServiceBusTopicClientId>
spring.jms.servicebus.idle-timeout=<IdleTimeout>
# Supported values for pricing-tier are premium and standard.
spring.jms.servicebus.pricing-tier=<ServiceBusPricingTier>
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
### Optional Service Bus functionality
A customized `MessageConverter` bean can be used to convert between Java objects and JMS messages.

#### Set [content-type][servicebus-message-payloads] of messages 

Below code snippet sets content-type of `BytesMessage` as `application/json`.

<!-- embedme ../azure-spring-boot/src/samples/java/com/azure/spring/jms/CustomizedMessageConverter.java#L25-L45 -->
```java
@Component
public class CustomMessageConverter extends MappingJackson2MessageConverter {

    private static final String TYPE_ID_PROPERTY = "_type";
    private static final Symbol CONTENT_TYPE = Symbol.valueOf("application/json");

    public CustomMessageConverter() {
        this.setTargetType(MessageType.BYTES);
        this.setTypeIdPropertyName(TYPE_ID_PROPERTY);
    }

    @Override
    protected BytesMessage mapToBytesMessage(Object object, Session session, ObjectWriter objectWriter)
        throws JMSException, IOException {
        final BytesMessage bytesMessage = super.mapToBytesMessage(object, session, objectWriter);
        JmsBytesMessage jmsBytesMessage = (JmsBytesMessage) bytesMessage;
        AmqpJmsMessageFacade facade = (AmqpJmsMessageFacade) jmsBytesMessage.getFacade();
        facade.setContentType(CONTENT_TYPE);
        return jmsBytesMessage;
    }
}
```

For more information about `MessageConverter`, see the official [Spring JMS guide][spring_jms_guide].
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
Azure SDKs for Java offers a consistent logging story to help aid in troubleshooting application errors and expedite their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

### Enable Spring logging
Spring allow all the supported logging systems to set logger levels set in the Spring Environment (for example, in application.properties) by using `logging.level.<logger-name>=<level>` where level is one of TRACE, DEBUG, INFO, WARN, ERROR, FATAL, or OFF. The root logger can be configured by using logging.level.root.

The following example shows potential logging settings in `application.properties`:

```
logging.level.root=WARN
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate=ERROR
```

For more information about setting logging in spring, please refer to the [official doc](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#boot-features-logging).
 

## Next steps
The following section provides sample projects illustrating how to use the starter in different cases.
### More sample code
- [JMS Service Bus Queue](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/azure-spring-boot_3.7/servicebus/azure-spring-boot-sample-servicebus-jms-queue)
- [JMS Service Bus Topic](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/azure-spring-boot_3.7/servicebus/azure-spring-boot-sample-servicebus-jms-topic)


## Contributing
This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [instructions here](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/CONTRIBUTING.md) to build from source or contribute.

<!-- LINKS -->
[docs]: https://docs.microsoft.com/azure/developer/java/spring-framework/configure-spring-boot-starter-java-app-with-azure-service-bus
[refdocs]: https://azure.github.io/azure-sdk-for-java/springboot.html#azure-spring-boot
[package]: https://mvnrepository.com/artifact/com.microsoft.azure/azure-servicebus-jms-spring-boot-starter
[sample]: https://github.com/Azure-Samples/azure-spring-boot-samples/
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK#use-logback-logging-framework-in-a-spring-boot-application
[servicebus-message-payloads]: https://docs.microsoft.com/azure/service-bus-messaging/service-bus-messages-payloads
[spring_jms_guide]: https://spring.io/guides/gs/messaging-jms/
[environment_checklist]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/ENVIRONMENT_CHECKLIST.md#ready-to-run-checklist
[Add azure-spring-boot-bom]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/AZURE_SPRING_BOMS_USAGE.md#add-azure-spring-boot-bom
