# Azure redis cache spring cloud starter shared library for Java

## Key concepts

This starter is based on spring-boot-starter-data-redis. It fetches and
auto-configures redis properties via Azure Redis Cache Management SDK.


## Getting started

Running this sample will be charged by Azure. You can check the usage and bill at [this link][azure-account].

### Environment checklist

We need to ensure that this [environment checklist][ready-to-run-checklist] is completed before the run.

### Coordinates

- Maven coordinates:

    ```xml
    <dependency>
        <groupId>com.azure.spring</groupId>
        <artifactId>azure-spring-cloud-starter-cache</artifactId>
    </dependency>
    ```

- Gradle coordinates:

    ```
    dependencies {
        compile group: 'com.azure.spring', name: 'azure-spring-cloud-starter-cache'
    }
    ```

### Create an Azure Cache for Redis instance

1. Create a service principal for use in by your app. Please follow [create service principal from Azure CLI][create-sp-using-azure-cli].
   
1. Create an Azure Cache for Redis instance. Please follow [create-azure-cache-for-redis].


## Examples

1.  Update `src/main/resources/application.yaml` to specify
    resource group, service principal, and cache instance name:
    
    ```yaml
    spring:
      cloud:
        azure:
          client-id: [service-principal-id]
          client-secret: [service-principal-secret]
          resource-group: [resource-group]
          tenant-id: [tenant-id]
          redis:
            name: [azure-cache-for-redis-instance-name]
    ```
    > :notes: if you want to auto create the redis cache, please specify the spring profile to use `autocreate` and fill
      in the subscription-id and region properties in `application-autocreate.yaml`.     

1.  Run the application using the `$ mvn spring-boot:run` command.

1.  Send a GET request to check, where `name` could be any string:

        $ curl -XGET http://localhost:8080/{name}

1.  Confirm from Azure Redis Cache console in Azure Portal

        $ keys *

1.  Delete the resources on [Azure Portal][azure-portal] to avoid unexpected charges.


## Troubleshooting

## Next steps

## Contributing

<!-- LINKS -->
[azure-account]: https://azure.microsoft.com/account/
[azure-portal]: https://ms.portal.azure.com/
[create-azure-cache-for-redis]: https://docs.microsoft.com/azure/azure-cache-for-redis/quickstart-create-redis
[create-sp-using-azure-cli]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/create-sp-using-azure-cli.md
[ready-to-run-checklist]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/README.md#ready-to-run-checklist

