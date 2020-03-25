#Azure Service Bus Spring Boot Starter client library for Java
## Getting started

### Add the dependency

`azure-spring-boot-starter-servicebus` is published on Maven Central Repository.  
Add the following dependency to your project:

*Maven*

[//]: # ({x-version-update-start;com.azure:azure-spring-boot-starter-servicebus;dependency})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-spring-boot-starter-servicebus</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

*Gradle*<br>
```compile 'com.azure:azure-spring-boot-starter-servicebus:1.0.0-beta.1'```

### Add the property setting

Open `application.properties` file and add below property with your Service Bus connection string.

```
azure.servicebus.connection-string=Endpoint=myEndpoint;SharedAccessKeyName=mySharedAccessKeyName;SharedAccessKey=mySharedAccessKey
```

If you want to use queue, please specify your created queue name and receive mode as below. 

```
azure.servicebus.queue-name=put-your-queue-name-here
azure.servicebus.queue-receive-mode=peeklock
```

For topic, please specify your created topic name. 

```
azure.servicebus.topic-name=put-your-topic-name-here
```

For subscription, please specify your created subscription name and receive mode.

```
azure.servicebus.subscription-name=put-your-subscription-name-here
azure.servicebus.subscription-receive-mode=peeklock
```

### Add auto-wiring code

You can use the following code to autowire the Azure Service Bus Queue, Topic, and Subscription clients in your Spring Boot application. Please see sample code in the [azure-servicebus-spring-boot-sample](../azure-spring-boot-samples/azure-spring-boot-sample-servicebus) folder as a reference.

```
@Autowired
private QueueClient queueClient;
```

```
@Autowired
private TopicClient topicClient;
```

```
@Autowired
private SubscriptionClient subscriptionClient;
```


### Allow telemetry
Microsoft would like to collect data about how users use this Spring boot starter. Microsoft uses this information to improve our tooling experience. Participation is voluntary. If you don't want to participate, just simply disable it by setting below configuration in `application.properties`.
```
azure.servicebus.allow-telemetry=false
```
When telemetry is enabled, an HTTP request will be sent to URL `https://dc.services.visualstudio.com/v2/track`. So please make sure it's not blocked by your firewall.  
Find more information about Azure Service Privacy Statement, please check [Microsoft Online Services Privacy Statement](https://www.microsoft.com/en-us/privacystatement/OnlineServices/Default.aspx). 

## Key concepts
## Examples
## Troubleshooting
## Next steps
## Contributing
