# Spring Cloud Azure Storage Queue Operation Code Sample shared library for Java

## Key concepts

This code sample demonstrates how to use [Storage Queue Operation][storage-queue-operation].

## Getting started

Running this sample will be charged by Azure. You can check the usage and bill at
[this link][azure-account].

### Environment checklist

We need to ensure that this [environment checklist][ready-to-run-checklist] is
completed before the run.

### Create Azure resources

1.  Create [Azure Storage][create-azure-storage]. 
    
1.  **[Optional]** if you want to use service principal, please follow
    [create service principal from Azure CLI][create-sp-using-azure-cli] to create one.

1.  **[Optional]** if you want to use managed identity, please follow
    [create managed identity][create-managed-identity] to set up managed identity.

## Examples

1.  Update stream binding related properties in
    [application.yaml]. If you choose to use
    service principal or managed identity, update the `application-sp.yaml` or
    `application-mi.yaml` respectively.

    ```yaml
    spring:
      cloud:
        azure:
          storage:
            account: [storage-account-name]
            access-key: [storage-account-access-key]
    ```

1.  Update queue name in [WebController.java][web-controller].

1.  Run the `mvn spring-boot:run` in the root of the code sample to get
    the app running.

1.  Send a POST request

        $ curl -X POST localhost:8080/messages?message=hello

1.  Receive the message you posted

        $ curl -X GET localhost:8080/messages

1.  Verify in your app’s logs that a similar message was posted:

        Message arrived! Payload: hello
        Message 'hello' successfully checkpointed

1.  Delete the resources on [Azure Portal][azure-portal] to avoid unexpected charges.


## Troubleshooting

## Next steps

## Contributing

<!-- LINKS -->

[azure-account]: https://azure.microsoft.com/account/
[azure-portal]: https://ms.portal.azure.com/
[create-azure-storage]: https://docs.microsoft.com/azure/storage/
[create-managed-identity]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/create-managed-identity.md
[create-sp-using-azure-cli]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/create-sp-using-azure-cli.md
[ready-to-run-checklist]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/README.md#ready-to-run-checklist
[storage-queue-operation]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-integration-storage-queue/src/main/java/com/azure/spring/integration/storage/queue/StorageQueueOperation.java
[web-controller]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-cloud-sample-storage-queue-operation/src/main/java/com/azure/spring/sample/storage/queue/operation/WebController.java#L26
[application.yaml]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-cloud-sample-storage-queue-operation/src/main/resources/application.yaml
