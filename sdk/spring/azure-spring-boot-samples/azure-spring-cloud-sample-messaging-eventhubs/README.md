# Spring Cloud Azure Messaging Event Hubs Sample shared library for Java

## Key concepts

This code sample demonstrates how to use [AzureMessageListener.java][annotation-azure-message-listener] to listen to events from Event Hubs.

## Getting started

Running this sample will be charged by Azure. You can check the usage and bill at 
[this link][azure-account].

### Prerequisites
- [Environment checklist][environment_checklist]

### Create Azure resources

1.  Create [Azure Event Hubs Namespace][create-event-hubs-namespace].
    Please note `Basic` tier is unsupported. After creating the Azure Event Hub, you
    can create your own Consumer Group or use the default `$Default` Consumer Group.
    
1.  Create [Azure Event Hub][create-event-hub-instance] and named `event-hub-name`.

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

        $ curl -H "Content-type: application/json" -X POST http://localhost:8080/messages -d "{\"name\":\"hello\"}"

1.  Verify in your app’s logs that a similar message was posted:

        New message received: 'User{name='hello'}'

1.  Delete the resources on [Azure Portal][azure-portal] to avoid unexpected charges.

## Troubleshooting

## Next steps

## Contributing


<!-- LINKS -->

[azure-account]: https://azure.microsoft.com/account/
[azure-portal]: https://ms.portal.azure.com/
[create-event-hubs-namespace]: https://docs.microsoft.com/azure/event-hubs/event-hubs-create#create-an-event-hubs-namespace
[create-event-hub-instance]: https://docs.microsoft.com/azure/event-hubs/event-hubs-create#create-an-event-hub
[create-azure-storage]: https://docs.microsoft.com/azure/storage/ 
[annotation-azure-message-listener]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-cloud-messaging/src/main/java/com/azure/spring/messaging/annotation/AzureMessageListener.java
[environment_checklist]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/ENVIRONMENT_CHECKLIST.md#ready-to-run-checklist
[application.yaml]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-cloud-sample-eventhubs-operation/src/main/resources/application.yaml