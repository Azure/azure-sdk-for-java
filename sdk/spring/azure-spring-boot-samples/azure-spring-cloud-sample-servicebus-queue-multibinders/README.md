# Spring Cloud Azure Stream Binder for Multiple Service Bus Namespaces Code Sample shared library for Java

## Key concepts
This code sample demonstrates how to use the Spring Cloud Stream Binder for 
multiple Azure Service Bus namespaces.
In this sample you will bind to two Service Bus namespaces separately through 
two queue binders.
The sample app exposes RESTful APIs to receive string message.
Then message is sent through Azure Service Bus to a `sink` which simply logs the message.

## Getting started

Running this sample will be charged by Azure. You can check the usage
and bill at [this link][azure-account].

### Environment checklist

We need to ensure that this 
[environment checklist][ready-to-run-checklist] 
is completed before the run.

### Create Azure resources

1.  Create two queues in different Service Bus namespaces.
    Please see [how to create][create-service-bus].

1.  **[Optional]** if you want to use service principal, please follow 
    [create service principal from Azure CLI][create-sp-using-azure-cli] to create one.

1.  **[Optional]** if you want to use managed identity, please follow
    [create managed identity][create-managed-identity] to set up managed identity. 

## Examples

1.  Update stream binding related properties in
    [application.yaml][application.yaml]. If you choose to use 
    service principal or managed identity, update the `application-sp.yaml` or 
    `application-mi.yaml` respectively.

    ```yaml
    spring:
      cloud:
        stream:
          bindings:
            input:
              destination: [servicebus-queue-1-name]
            output:
              destination: [servicebus-queue-1-name-same-as-above]
            input1:
              binder: servicebus-2
              destination: [servicebus-queue-2-name]
            output1:
              binder: servicebus-2
              destination: [servicebus-queue-2-name-same-as-above]
    
          binders:
            servicebus-1:
              type: servicebus-queue
              default-candidate: true
              environment:
                spring:
                  cloud:
                    azure:
                      servicebus:
                        connection-string: [servicebus-namespace-1-connection-string]
            servicebus-2:
              type: servicebus-queue
              default-candidate: false
              environment:
                spring:
                  cloud:
                    azure:
                      servicebus:
                        connection-string: [servicebus-namespace-2-connection-string]         
    ```

> The **defaultCandidate** configuration item:
Whether the binder configuration is a candidate for being considered a
default binder, or can be used only when explicitly referenced. This
allows adding binder configurations without interfering with the default
processing.

1.  Run the `mvn clean spring-boot:run` in the root of the code sample
    to get the app running.

1.  Send a POST request to test the default binder

        $ curl -X POST http://localhost:8080/messages?message=hello

1.  Verify in your app’s logs that a similar message was posted:

        [1] New message received: 'hello'
        [1] Message 'hello' successfully checkpointed

1.  Send another POST request to test the other binder

        $ curl -X POST http://localhost:8080/messages1?message=hello

1.  Verify in your app’s logs that a similar message was posted:

        [2] New message received: 'hello'
        [2] Message 'hello' successfully checkpointed

6.  Delete the resources on [Azure Portal][azure-portal]
    to avoid unexpected charges.

## Troubleshooting

## Next steps

## Contributing


<!-- LINKS -->
[azure-account]: https://azure.microsoft.com/account/
[azure-portal]: https://ms.portal.azure.com/
[create-service-bus]: https://docs.microsoft.com/azure/service-bus-messaging/service-bus-create-namespace-portal
[create-sp-using-azure-cli]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/create-sp-using-azure-cli.md
[create-managed-identity]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/create-managed-identity.md
[deploy-spring-boot-application-to-app-service]: https://docs.microsoft.com/java/azure/spring-framework/deploy-spring-boot-java-app-with-maven-plugin?toc=%2Fazure%2Fapp-service%2Fcontainers%2Ftoc.json&view=azure-java-stable
[deploy-to-app-service-via-ftp]: https://docs.microsoft.com/azure/app-service/deploy-ftp
[managed-identities]: https://docs.microsoft.com/azure/active-directory/managed-identities-azure-resources/
[ready-to-run-checklist]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/README.md#ready-to-run-checklist
[role-assignment]: https://docs.microsoft.com/azure/role-based-access-control/role-assignments-portal
[application.yaml]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-cloud-sample-servicebus-queue-multibinders/src/main/resources/application.yaml
