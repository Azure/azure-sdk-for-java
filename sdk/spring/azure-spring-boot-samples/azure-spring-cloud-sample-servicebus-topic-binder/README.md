# Spring Cloud Azure Stream Binder for Service Bus topic Sample shared library for Java

## Key concepts

This code sample demonstrates how to use the Spring Cloud Stream binder for 
Azure Service Bus topic. The sample app exposes a RESTful API to receive string message.
Then message is sent through Azure Service Bus to a `sink` which simply logs the message.

## Getting started

Running this sample will be charged by Azure. You can check the usage and bill at 
[this link][azure-account].

### Environment checklist

We need to ensure that this [environment checklist][ready-to-run-checklist] is 
completed before the run.

### Create Azure resources

We have several ways to config the Spring Cloud Stream Binder for Azure
Event Hub. You can choose anyone of them.


#### Method 1: Connection string based usage

1.  Create Azure Service Bus namespace and topic.
    Please see [how to create][create-service-bus].

1.  Update [application.yaml].
    ```yaml
    spring:
      cloud:
        azure:
          servicebus:
            connection-string: [servicebus-namespace-connection-string] 
        stream:
          bindings: 
            input: 
              destination: [servicebus-queue-name]
            output:
              destination: [servicebus-queue-name-same-as-above]
    ```

#### Method 2: Service principal based usage

1.  Create a service principal for use in by your app. Please follow 
    [create service principal from Azure CLI][create-sp-using-azure-cli].

1.  Create Azure Service Bus namespace and queue.
        Please see [how to create][create-service-bus].
    
1.  Update [application-sp.yaml].
    ```yaml
    spring:
      cloud:
        azure:
          client-id: [service-principal-id]
          client-secret: [service-principal-secret]
          tenant-id: [tenant-id]
          resource-group: [resource-group]
          servicebus:
            namespace: [servicebus-namespace]
        stream:
          bindings:
            input:
              destination: [servicebus-queue-name]
            output:
              destination: [servicebus-queue-name-same-as-above]
    ```
        
#### Method 3: MSI credential based usage

##### Set up managed identity

Please follow [create managed identity][create-managed-identity] to set up managed identity.

##### Create other Azure resources

1.  Create Azure Service Bus namespace and queue.
        Please see [how to create][create-service-bus].
        
1.  Add Role Assignment for Service Bus. See
    [Managed identities for Azure resources with Service Bus][role-assignment]
    to add role assignment for Service Bus. Assign `Contributor` role for managed identity.
    

##### Update MSI related properties

1.  Update [application-mi.yaml]
    ```yaml
    spring:
      cloud:
        azure:
          msi-enabled: true
          client-id: [the-id-of-managed-identity]
          resource-group: [resource-group]
          subscription-id: [subscription-id]
          servicebus:
            namespace: [servicebus-namespace]
        stream:
          bindings:
            input:
              destination: [servicebus-queue-name]
            output:
              destination: [servicebus-queue-name-same-as-above]
    ```
    > We should specify `spring.profiles.active=mi` to run the Spring Boot application. 
      For App Service, please add a configuration entry for this.

##### Redeploy Application

If you update the `spring.cloud.azure.managed-identity.client-id`
property after deploying the app, or update the role assignment for
services, please try to redeploy the app again.

> You can follow 
> [Deploy a Spring Boot JAR file to Azure App Service][deploy-spring-boot-application-to-app-service] 
> to deploy this application to App Service

#### Enable auto create

If you want to auto create the Azure Service Bus instances, make sure you add such properties 
(only support the service principal and managed identity cases):

```yaml
spring:
  cloud:
    azure:
      subscription-id: [subscription-id]
      auto-create-resources: true
      environment: Azure
      region: [region]
```


## Examples

1.  Run the `mvn spring-boot:run` in the root of the code sample to get the app running.

1.  Send a POST request

        $ curl -X POST http://localhost:8080/messages?message=hello

    or when the app runs on App Service or VM

        $ curl -d -X POST https://[your-app-URL]/messages?message=hello

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
[create-azure-storage]: https://docs.microsoft.com/azure/storage/ 
[create-sp-using-azure-cli]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/create-sp-using-azure-cli.md
[create-managed-identity]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/create-managed-identity.md
[deploy-spring-boot-application-to-app-service]: https://docs.microsoft.com/java/azure/spring-framework/deploy-spring-boot-java-app-with-maven-plugin?toc=%2Fazure%2Fapp-service%2Fcontainers%2Ftoc.json&view=azure-java-stable
[ready-to-run-checklist]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/README.md#ready-to-run-checklist
[role-assignment]: https://docs.microsoft.com/azure/role-based-access-control/role-assignments-portal
[application-mi.yaml]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-cloud-sample-servicebus-topic-binder/src/main/resources/application-mi.yaml
[application-sp.yaml]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-cloud-sample-servicebus-topic-binder/src/main/resources/application-sp.yaml
[application.yaml]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-cloud-sample-servicebus-topic-binder/src/main/resources/application.yaml
