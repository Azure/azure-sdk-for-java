# Spring Cloud Azure Event Hub Operation Sample shared library for Java

## Key concepts

This code sample demonstrates how to use [EventHubOperation.java][eventhub-operation].

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

## Examples

1. Update [application.yaml][application.yaml].
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


<!-- LINKS -->

[azure-account]: https://azure.microsoft.com/account/
[azure-portal]: https://ms.portal.azure.com/
[create-event-hubs]: https://docs.microsoft.com/azure/event-hubs/ 
[create-azure-storage]: https://docs.microsoft.com/azure/storage/ 
[eventhub-operation]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-integration-eventhubs/src/main/java/com/azure/spring/integration/eventhub/api/EventHubOperation.java
[ready-to-run-checklist]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/README.md#ready-to-run-checklist
[application.yaml]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-cloud-sample-eventhubs-operation/src/main/resources/application.yaml