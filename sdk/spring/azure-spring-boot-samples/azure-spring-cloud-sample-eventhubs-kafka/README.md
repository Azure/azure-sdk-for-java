# Spring Cloud Azure Stream Kafka Binder for Event Hub Code Sample shared library for Java

## Key concepts

This code sample demonstrates how to use the Spring Cloud Stream Kafka
binder for Azure Event Hub. The sample app exposes a RESTful API to receive
string message. Then message is sent through Azure Event Hub to a `sink`
which simply logs the message.

## Getting started


Running this sample will be charged by Azure. You can check the usage and bill at
[this link][azure-account].


### Environment checklist

We need to ensure that this [environment checklist][ready-to-run-checklist] is
completed before the run.


### Create Azure resources

1. Create a service principal for use in by your app. Please follow 
   [create service principal from Azure CLI][create-sp-using-azure-cli].

1. Create [Azure Event Hubs][create-event-hubs]. 

## Examples

1.  Update
    [application.yaml][application.yaml]
    file
    
    ```yaml
    spring:
      cloud:
        azure:
          client-id: [service-principal-id]
          client-secret: [service-principal-secret]
          tenant-id: [tenant-id]
          resource-group: [resource-group]
          eventhub:
            namespace: [eventhub-namespace]
        stream:
          bindings:
            input:
              destination: [eventhub-name]
              group: [consumer-group]
            output:
              destination: [the-same-eventhub-name-as-above]
    ```

1.  Run the `mvn spring-boot:run` in the root of the code sample to get the app running.

1.  Send a POST request

        $ curl -X POST http://localhost:8080/messages?message=hello

1.  Verify in your app’s logs that a similar message was posted:

    `New message received: hello`

1.  Delete the resources on [Azure Portal][azure-portal] to avoid unexpected charges.



## Troubleshooting

## Next steps

## Contributing

<!-- LINKS -->
[azure-account]: https://azure.microsoft.com/account/
[azure-portal]: https://ms.portal.azure.com/
[create-event-hubs]: https://docs.microsoft.com/azure/event-hubs/
[create-sp-using-azure-cli]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/create-sp-using-azure-cli.md
[ready-to-run-checklist]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/README.md#ready-to-run-checklist
[application.yaml]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-cloud-sample-eventhubs-kafka/src/main/resources/application.yaml