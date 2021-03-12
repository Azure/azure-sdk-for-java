# Spring Cloud Azure Service Bus Integration Code Sample shared library for Java

## Key concepts

This code sample demonstrates how to use Spring Integration for Azure Service Bus.


## Getting started

Running this sample will be charged by Azure. You can check the usage and bill at
[this link][azure-account].

### Environment checklist

We need to ensure that this [environment checklist][ready-to-run-checklist] is
completed before the run.

### Create Azure resources

1. Create Azure Service Bus namespace, queue and topic. Please see 
   [how to create][create-service-bus].

1.  **[Optional]** if you want to use service principal, please follow
    [create service principal from Azure CLI][create-sp-using-azure-cli] to create one.

1.  **[Optional]** if you want to use managed identity, please follow
    [create managed identity][create-managed-identity] to set up managed identity.


## Examples

1. Update [application.yaml]. If you choose to use
   service principal or managed identity, update the `application-sp.yaml` or
   `application-mi.yaml` respectively.
    ```yaml
    spring:
      cloud:
        azure:
          servicebus:
            connection-string: [servicebus-namespace-connection-string]
    ```

1. Update queue name in 
   [QueueReceiveController.java][queue-receive-controller] and
   [QueueSendController.java][queue-send-controller], 
   and update topic name and subscription in
   [TopicReceiveController.java][topic-receive-controller] and
   [TopicSendController.java][topic-send-controller].
   
    
1.  Run the `mvn spring-boot:run` in the root of the code sample to get the app running.

1. Send a POST request to service bus queue

        $ curl -X POST http://localhost:8080/queues?message=hello

1.  Verify in your app’s logs that a similar message was posted:

        New message received: 'hello'
        Message 'hello' successfully checkpointed

1. Send a POST request to service bus topic

        $ curl -X POST http://localhost:8080/topics?message=hello

1.  Verify in your app’s logs that a similar message was posted:

        New message received: 'hello'
        Message 'hello' successfully checkpointed

1.  Delete the resources on [Azure Portal][azure-portal] to avoid unexpected charges.


## Troubleshooting

## Next steps

## Contributing

[azure-account]: https://azure.microsoft.com/account/
[azure-portal]: https://ms.portal.azure.com/
[create-service-bus]: https://docs.microsoft.com/azure/service-bus-messaging/service-bus-create-namespace-portal
[create-managed-identity]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/create-managed-identity.md
[create-sp-using-azure-cli]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/create-sp-using-azure-cli.md
[ready-to-run-checklist]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/README.md#ready-to-run-checklist
[queue-receive-controller]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-integration-sample-servicebus/src/main/java/com/azure/spring/sample/servicebus/QueueReceiveController.java
[queue-send-controller]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-integration-sample-servicebus/src/main/java/com/azure/spring/sample/servicebus/QueueSendController.java
[topic-receive-controller]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-integration-sample-servicebus/src/main/java/com/azure/spring/sample/servicebus/TopicReceiveController.java
[topic-send-controller]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-integration-sample-servicebus/src/main/java/com/azure/spring/sample/servicebus/TopicSendController.java
[application.yaml]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-integration-sample-servicebus/src/main/resources/application.yaml



