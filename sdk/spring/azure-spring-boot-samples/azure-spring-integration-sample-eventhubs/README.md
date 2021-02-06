# Spring Cloud Azure Event Hub Integration Code Sample shared library for Java

## Key concepts

This code sample demonstrates how to use Spring Integration for Azure
Event Hub.


## Getting started

Running this sample will be charged by Azure. You can check the usage and bill at
[this link][azure-account].

### Environment checklist

We need to ensure that this [environment checklist][ready-to-run-checklist] is
completed before the run.

### Create Azure resources

1.  Create [Azure Event Hubs][create-event-hubs].
    Please note `Basic` tier is unsupported. After creating the Azure Event Hub, you
    can create your own Consumer Group or use the default "$Default" Consumer Group.

1.  Create [Azure Storage][create-azure-storage] for checkpoint use.

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
          eventhub:
            connection-string: [eventhub-namespace-connection-string]
            checkpoint-storage-account: [checkpoint-storage-account]
            checkpoint-access-key: [checkpoint-access-key]
            checkpoint-container: [checkpoint-container]
    ```

1.  Update event hub name and consumer group in
    [ReceiveController][receive-controller] and [SendController][send-controller].
    
1.  Run the `mvn spring-boot:run` in the root of the code sample to get the app running.

1.  Send a POST request

        $ curl -X POST http://localhost:8080/messages?message=hello

1.  Verify in your app’s logs that a similar message was posted:

        New message received: 'hello'
        Message 'hello' successfully checkpointed

1.  Delete the resources on [Azure Portal][azure-portal] to avoid unexpected charges.


## Troubleshooting

## Next steps

## Contributing

[azure-account]: https://azure.microsoft.com/account/
[azure-portal]: https://ms.portal.azure.com/
[create-event-hubs]: https://docs.microsoft.com/azure/event-hubs/
[create-azure-storage]: https://docs.microsoft.com/azure/storage/
[create-managed-identity]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/create-managed-identity.md
[create-sp-using-azure-cli]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/create-sp-using-azure-cli.md
[eventhub-operation]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-integration-eventhubs/src/main/java/com/azure/spring/integration/eventhub/api/EventHubOperation.java
[ready-to-run-checklist]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/README.md#ready-to-run-checklist
[receive-controller]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-integration-sample-eventhubs/src/main/java/com/azure/spring/sample/eventhubs/ReceiveController.java
[send-controller]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-integration-sample-eventhubs/src/main/java/com/azure/spring/sample/eventhubs/SendController.java
[application.yaml]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-integration-sample-eventhubs/src/main/resources/application.yaml


