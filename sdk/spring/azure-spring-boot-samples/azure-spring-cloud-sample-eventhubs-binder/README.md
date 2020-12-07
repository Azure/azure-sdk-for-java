# Azure Spring Cloud Stream Binder for Event Hub Code Sample shared library for Java

## Key concepts

This code sample demonstrates how to use the Spring Cloud Stream Binder
for Azure Event Hub. The sample app exposes a RESTful API to receive string
message. Then message is sent through Azure Event Hub to a `sink` which
simply logs the message.

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

1.  Create [Azure Event Hubs][create-event-hubs].
    Please note `Basic` tier is unsupported. After creating the Azure Event Hub, you 
    can create your own Consumer Group or use the default "$Default" Consumer Group.

1.  Create [Azure Storage][create-azure-storage] for checkpoint use.

1.  Update [application.yaml](src/main/resources/application.yaml).

    ```yaml
    spring:
      cloud:
        azure:
          eventhub:
            # Fill event hub namespace connection string copied from portal
            connection-string: [eventhub-namespace-connection-string] 
            # Fill checkpoint storage account name, access key and container 
            checkpoint-storage-account: [checkpoint-storage-account]
            checkpoint-access-key: [checkpoint-access-key]
            checkpoint-container: [checkpoint-container]
        stream:
          bindings:
            input:
              destination: [eventhub-name]
              group: [consumer-group]
            output:
              destination: [the-same-eventhub-name-as-above]
    ```

#### Method 2: Service principal based usage

1.  Create a service principal for use in by your app. Please follow 
    [create service principal from Azure CLI][create-sp-using-azure-cli].

1.  Create [Azure Event Hubs][create-event-hubs].
    Please note `Basic` tier is unsupported. After creating the Azure Event Hub, you
    can create your own Consumer Group or use the default "$Default" Consumer Group.

1.  Create [Azure Storage][create-azure-storage] for checkpoint use.
    
1.  Update [application-sp.yaml](src/main/resources/application-sp.yaml).
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
            checkpoint-storage-account: [checkpoint-storage-account]
            checkpoint-container: [checkpoint-container]
        stream:
          bindings:
            input:
              destination: [eventhub-name]
              group: [consumer-group]
            output:
              destination: [the-same-eventhub-name-as-above]
    ```
        
#### Method 3: MSI credential based usage

##### Overview

[MSI][managed-identities] (Managed Service Identity, aka Managed Identity) for Azure resources
provides Azure services with an automatically managed identity in [Azure AD][azure-ad].
You can use this identity to authenticate to any service that supports Azure AD authentication 
without having any credentials in your code.

##### Set up managed identity

Please note your application should run in VM (Virtual Machine) or App Services on Azure for 
support of MSI. Choose any of them.

###### Method 1: Set up VM and assign identity

1.  Create VM in Azure portal. Please refer to 
    [Create a Windows virtual machine in the Azure portal][create-vm-windows]
    or [Create a Linux virtual machine in the Azure portal][create-vm-linux].
    Choose either one according to your needs.

1.  Create a user-assigned identity in Azure Portal. Please refer to
    [Create a user-assigned managed identity][create-user-assigned-mi].

1.  Assign the user-assigned identity to the VM. Please refer to 
    [Assign an user-assigned managed identity to an existing VM][assign-user-assigned-mi-to-vm].

###### Method 2: Set up App Service and assign identity

1. Deploy this sample’s Spring Boot JAR file to App Service.
   You can follow 
   [Deploy a Spring Boot JAR file to Azure App Service][deploy-spring-boot-application-to-app-service]
   to deploy the JAR file. 
   Another way to deploy an executable JAR is via FTP/S. Follow 
   [Deploy your app to App Service using FTP/S][deploy-to-app-service-via-ftp]. 
   Please note that the JAR file’s name must be `app.jar`.
   
1. Create a managed identity for App Service. 
   - If you choose system-assigned identity, follow [Adding a system assigned identity][app-service-add-system-assigned-mi].
   - If you choose user-assigned identity, follow [Adding a user assigned identity][app-service-add-user-assigned-mi].

##### Add Role Assignment for Resource Group

1. Resource Group: assign `Reader` role for managed identity. See 
   [Add or remove Azure role assignments][role-assignment] 
   to add the role assignment for Resource Group.

For different built-in role’s descriptions, please see [Built-in role
descriptions][built-in-roles].

##### Create other Azure resources

1.  Create [Azure Event Hubs][create-event-hubs].
    Please note `Basic` tier is unsupported. After creating the Azure Event Hub, you
    can create your own Consumer Group or use the default "$Default" Consumer Group.

1.  Create [Azure Storage][create-azure-storage] for checkpoint use.

1.  Add Role Assignment for Event Hub and Storage Account. See
    [Managed identities for Azure resources with Event Hubs][role-assignment]
    to add role assignment for Event Hub, Storage Account is similar.

    - Event Hub: assign `Contributor` role for managed identity.
    - Storage Account: assign `Storage Account Key Operator Service Role` 
      role for managed identity.

##### Update MSI related properties

1.  Update [application-mi.yaml](src/main/resources/application-mi.yaml)
    ```yaml
    spring:
      cloud:
        azure:
          msi-enabled: true
          managed-identity:
            client-id: [the-id-of-managed-identity]
          resource-group: [resource-group]
          # Fill subscription ID copied from portal
          subscription-id: [subscription-id]
          eventhub:
            namespace: [eventhub-namespace]
            checkpoint-storage-account: [checkpoint-storage-account]
            checkpoint-container: [checkpoint-container]
        stream:
          bindings:
            input:
              destination: [eventhub-name]
              group: [consumer-group]
            output:
              destination: [the-same-eventhub-name-as-above]
    ```
    > Since we're not using the default profile to run the application,
      we should specify `spring.profiles.active=mi` run the Spring Boot application. 
      For App Service, please add a configuration entry for this.

##### Redeploy Application

If you update the `spring.cloud.azure.managed-identity.client-id`
property after deploying the app, or update the role assignment for
services, please try to redeploy the app again.

#### Enable auto create

If you want to auto create the Azure Event Hub and Azure Storage account instances,
make sure you add such properties 
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

1.  Run the `mvn clean spring-boot:run` in the root of the code sample to get the app running.

1.  Send a POST request

        $ curl -X POST http://localhost:8080/messages?message=hello

    or when the app runs on App Service or VM

        $ curl -d -X POST https://[your-app-URL]/messages?message=hello

1.  Verify in your app’s logs that a similar message was posted:

        New message received: 'hello'
        Message 'hello' successfully checkpointed

1.  Delete the resources on [Azure Portal][azure-portal] to avoid unexpected charges.

# Troubleshooting

# Next steps

# Contributing


<!-- LINKS -->
[app-service-add-system-assigned-mi]: https://docs.microsoft.com/azure/app-service/overview-managed-identity#adding-a-system-assigned-identity
[app-service-add-user-assigned-mi]: https://docs.microsoft.com/azure/app-service/overview-managed-identity#adding-a-user-assigned-identity
[assign-user-assigned-mi-to-vm]: https://docs.microsoft.com/azure/active-directory/managed-identities-azure-resources/qs-configure-portal-windows-vm#assign-a-user-assigned-managed-identity-to-an-existing-vm
[azure-account]: https://azure.microsoft.com/account/
[azure-ad]: https://docs.microsoft.com/azure/active-directory/fundamentals/active-directory-whatis 
[azure-portal]: http://ms.portal.azure.com/
[built-in-roles]: https://docs.microsoft.com/azure/role-based-access-control/built-in-roles 
[create-event-hubs]: https://docs.microsoft.com/azure/event-hubs/ 
[create-azure-storage]: https://docs.microsoft.com/azure/storage/ 
[create-sp-using-azure-cli]: ../create-sp-using-azure-cli.md
[create-user-assigned-mi]: https://docs.microsoft.com/azure/active-directory/managed-identities-azure-resources/how-to-manage-ua-identity-portal#create-a-user-assigned-managed-identity
[create-vm-windows]: https://docs.microsoft.com/azure/virtual-machines/windows/quick-create-portal
[create-vm-linux]: https://docs.microsoft.com/azure/virtual-machines/linux/quick-create-portal
[deploy-spring-boot-application-to-app-service]: https://docs.microsoft.com/java/azure/spring-framework/deploy-spring-boot-java-app-with-maven-plugin?toc=%2Fazure%2Fapp-service%2Fcontainers%2Ftoc.json&view=azure-java-stable
[deploy-to-app-service-via-ftp]: https://docs.microsoft.com/azure/app-service/deploy-ftp
[managed-identities]: https://docs.microsoft.com/azure/active-directory/managed-identities-azure-resources/
[ready-to-run-checklist]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/README.md#ready-to-run-checklist
[role-assignment]: https://docs.microsoft.com/azure/role-based-access-control/role-assignments-portal

