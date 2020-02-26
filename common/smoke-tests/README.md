# Azure Smoke Test for Java
This sample code is a smoke test to ensure that Azure Preview for Java work while loaded into the same process by performing 2 or more actions with them.

Libraries tested:
* keyvault-secrets
* identity
* storage-blob
* event-hubs
* cosmos

## Getting started
### Setup Azure resources
For this sample, it is necessary to create/have the following resources in the [Azure Portal](https://portal.azure.com/):
* **App registration**: Register a new app or use an existing one.
  * Under _Certificates & secrets_ create a new **client secret** and store the value in a safe place.
* **Key Vaults**: Create a new Key Vault resource or use an existing one.
  * Under _Access policies_, add the app registrated in the previous step.
* **Storage acounts**: Create a container in a new or existing storage account. The container in this sample is named "mycontainer", if you want to use other name you can change the value in `BlobStorage.ts` file:
`const containerName = "mycontainer";`
* **Event Hubs**: Create an event hub inside a new or existing Event Hubs Namespace. The container in this sample is named "myeventhub", if you want to use other name you can change the value in `EventHubsTest.ts` file: `let eventHubName = "myeventhub";`
* **Azure Cosmos DB**: Create a new account or use an existing one.

### Azure credentials
The following environment variables are needed:
* From **App Registration**, in the _Overview_ section:
    * AZURE_TENANT_ID: The directory tentant ID.
    * AZURE_CLIENT_ID: The application ID.
    * AZURE_CLIENT_SECRET: The client secret stored previusly when creating the _client secret_.

* From **Key Vault**, in the _Overview_ section:
  * AZURE_PROJECT_URL: The DNS Name

* From **Event Hubs**, in _Shared access policies_ section:
  * AZURE_EVENT_HUBS_CONNECTION_STRING: Connection string from a policy

* From **Storage Account**, in the _Access Keys_ section:
  * AZURE_STORAGE_CONNECTION_STRING : A connection strings.

* From **Azure Cosmos DB**, in the _Keys_ section, select the _Read-Write Keys_ tab:
  * AZURE_COSMOS_ENDPOINT: URI.
  * AZURE_COSMOS_KEY: Primary or secondary key.

```bash
# Bash code to create the environment variables
export AZURE_CLIENT_ID=""
export AZURE_CLIENT_SECRET=""
export AZURE_TENANT_ID=""
export AZURE_EVENT_HUBS_CONNECTION_STRING=""
export AZURE_AZURE_PROJECT_URL=""
export AZURE_STORAGE_CONNECTION_STRING=""
export AZURE_COSMOS_ENDPOINT=""
export AZURE_COSMOS_KEY=""
```
### Client Logger
The Azure clients use a ClientLogger. Create an environment variable `AZURE_LOG_LEVEL` and set it to the desire level:
* Verbose = 1
* Informational = 2
* Warnings = 3
* Errors = 4
* Disabled = 5

### Running the console app
[Java](https://www.java.com/en/) version 11.0.4 was used to run this sample.

Install Maven dependencies:

Run `App.main()`:


## Key concepts

## Examples
All the classes in this sample not depend on each other. 

It is possible to run them individually:
```java
package com.azure;
import java.io.IOException;

public class App {
    public static void main(String[] args) throws IllegalArgumentException, IOException {
        StorageBlob.main(null);
    }
}
```

The classes can be used as base code and be changed to satisfied specific needs. For example, the method `EventHubs().sendAndReceiveEvents()` can be change to only send events from an array given from a parameter:
```java
private void sendEvents(String partitionId, Flux<EventData> events) {
        EventHubProducer producer = client.createProducer(new EventHubProducerOptions().partitionId(partitionId));

        producer.send(events).subscribe(
            (ignored) -> logger.info("sent"),
            error -> logger.error("Error received:" + error),
            () -> {
                //Closing the producer once is done with sending the events
                try {
                    producer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        );
    }
```

**Note:** The methods in the classes are not necessary independent on each other, and the order matters. For example, in order to run `StorageBlob.deleteBlob();`, the method `StorageBlob.uploadBLob();` must be run before, since in the other way it will fail because there is not going to be a blob to delete.

## Troubleshooting

### Authentication
Be sure to set the environment variables and credentials required before running the sample.

### SLF4J Logger
Be sure to include the SLF4J dependency in the `pom.xml` file.

```xml
    <dependency>
      <groupId>org.slf4j</groupId>
      <artifactId>slf4j-simple</artifactId>
      <version>1.7.28</version>
    </dependency>
```

## Next steps
Check the [Azure SDK for Java Repository](https://github.com/Azure/azure-sdk-for-java) for more samples.

## Contributing
This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

If you'd like to contribute to this library, please read the contributing guide to learn more about how to build and test the code.

This project has adopted the Microsoft Open Source Code of Conduct. For more information see the Code of Conduct FAQ or contact opencode@microsoft.com with any additional questions or comments.


![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Feng%2Fsmoke-tests%2FREADME.png)
