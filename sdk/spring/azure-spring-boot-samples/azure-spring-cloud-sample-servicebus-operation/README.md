# Spring Cloud Azure Service Bus Operation Sample shared library for Java

## Key concepts

This code sample demonstrates how to use 
[ServiceBusTopicOperation.java][servicebus-topic-operation] and
[ServiceBusQueueOperation.java][servicebus-queue-operation].

## Getting started

Running this sample will be charged by Azure. You can check the usage and bill at 
[this link][azure-account].

### Environment checklist

We need to ensure that this [environment checklist][ready-to-run-checklist] is 
completed before the run.

### Create Azure resources

1.  Create Azure Service Bus namespace, queue and topic.
    Please see [how to create][create-service-bus].

## Examples

1. Update [application.yaml][application.yaml].
    ```yaml
    spring:
      cloud:
        azure:
          servicebus:
            connection-string: [servicebus-namespace-connection-string]
    ```
1.  Update queue name in [QueueController.java][queue-controller], 
    topic name and subscription in [TopicController.java][topic-controller].


1.  Run the `mvn spring-boot:run` in the root of the code sample to get the app running.

1.  Send a POST request to service bus queue

        $ curl -X POST localhost:8080/queues?message=hello

1.  Verify in your app’s logs that a similar message was posted:

        New message received: 'hello'
        Message 'hello' successfully checkpointed

1.  Send a POST request to service bus topic

        $ curl -X POST localhost:8080/topics?message=hello

1.  Verify in your app’s logs that a similar message was posted:

        New message received: 'hello'
        Message 'hello' successfully checkpointed        

1.  Delete the resources on [Azure Portal][azure-portal] to avoid unexpected charges.

## Troubleshooting

## Next steps

## Contributing


<!-- LINKS -->

[azure-account]: https://azure.microsoft.com/account/
[azure-portal]: https://ms.portal.azure.com/
[create-service-bus]: https://docs.microsoft.com/azure/service-bus-messaging/service-bus-create-namespace-portal
[queue-controller]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-cloud-sample-servicebus-operation/src/main/java/com/azure/spring/sample/servicebus/operation/QueueController.java 
[topic-controller]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-cloud-sample-servicebus-operation/src/main/java/com/azure/spring/sample/servicebus/operation/TopicController.java 
[ready-to-run-checklist]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/README.md#ready-to-run-checklist
[servicebus-queue-operation]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-integration-servicebus/src/main/java/com/azure/spring/integration/servicebus/queue/ServiceBusQueueOperation.java
[servicebus-topic-operation]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-integration-servicebus/src/main/java/com/azure/spring/integration/servicebus/topic/ServiceBusTopicOperation.java
[application.yaml]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-cloud-sample-servicebus-operation/src/main/resources/application.yaml
