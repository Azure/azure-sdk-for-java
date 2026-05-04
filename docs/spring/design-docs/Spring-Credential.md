## Authenticate/Authorize Methods
### Authenticate with Azure Active Directory
This method leverages the Azure Active Directory as the Authorization Server and performs an OAuth 2.0 request to acquire an access token. The access token could be issued to: 
- Users
- Service principals
- Managed identities for Azure resources

#### Authorize with access tokens in HTTP requests
Pass the access token in the Authorization header using the Bearer scheme:
```
Request:
GET /container/file.txt
Authorization: Bearer eyJ0eXAiO...V09ccgQ
Host: sampleoautheast2.blob.core.windows.net
```
#### Authorize with access tokens in AMQP
The [AMQP Claims-Based-Authorization (CBS)][amqp-cbs] specification draft builds on the management specification request/response pattern, and describes a generalized model for how to use federated security tokens with AMQP. CBS defines a virtual management node, named `$cbs`, to be provided by the messaging infrastructure. The management node accepts tokens on behalf of any other nodes in the messaging infrastructure. The token type could be either `jwt` or `servicebus.windows.net:sastoken`. 

### Authorize with Shared Key Credentials
#### Storage
A client using Shared Key passes a header with every request that is signed using the storage account access key. For more information, see [Authorize with Shared Key](https://docs.microsoft.com/rest/api/storageservices/authorize-with-shared-key).

A request using Shared Key authorization would be like this:
```
PUT http://myaccount/mycontainer?restype=container&timeout=30 HTTP/1.1  
x-ms-version: 2014-02-14  
x-ms-date: Fri, 26 Jun 2015 23:39:12 GMT  
Authorization: SharedKey myaccount:ctzMq410TV3wS7upTBcunJTDLEJwMAZuFPfr0mrrA08=  
``` 


### Authorize with Shared Access Signature
#### [How a shared access signature works][sas-101]
A shared access signature is a signed URI that points to one or more storage resources. The URI includes a token that contains a special set of query parameters. The token indicates how the resources may be accessed by the client. One of the query parameters, the signature, is constructed from the SAS parameters and signed with the key that was used to create the SAS. This signature is used by Azure Storage to authorize access to the storage resource.

![SAS URI][sas-uri-png]
 
#### Storage
You can sign a SAS token with a user delegation key or with a storage account key (Shared Key). By any chance, if these SAS keys are compromised, we can only stop further access by regenerating our storage account keys, which is an expensive thing to do. `Stored access policies` give you the option to revoke permissions for a service SAS without having to regenerate the storage account keys. [Here][sas-best-practices] are the best practices when using SAS. 
 





### Authentication methods supported in Azure SDKs
|Azure SDK|[TokenCredential]|[AzureKeyCredential] / Key|[AzureNamedKeyCredential]|[AzureSasCredential]|SharedKeyCredential|  
|:-:|:-:|:-:|:-:|:-:|:-:|
|[Key Vault Secret][secret-client-builder]|✅|❌|❌|❌|❌|
|[Cosmos][cosmos-client-builder]|✅|✅|❌|❌|❌|
|[Storage Blob][blobservice-client-builder]|✅|❌|❌|✅|✅ [StorageSharedKeyCredential]|
|[Storage File Share][shareservice-client-builder]|❌|❌|❌|✅|✅ [StorageSharedKeyCredential]|
|[Storage Queue][queue-client-builder]|✅|❌|❌|✅|✅ [StorageSharedKeyCredential]|
|[Event Hubs][eventhubs-client-builder]|✅|❌|✅|✅ </br> ✔️ [EventHubSharedKeyCredential] (the name is SharedKeyCredential, but it generates a SAS token)|❌| 
|[Service Bus][shareservice-client-builder]|✅|❌|✅|✅ </br> ✔️ [ServiceBusSharedKeyCredential] (the name is SharedKeyCredential, but it generates a SAS token)|❌| 



[TokenCredential]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/credential/TokenCredential.java
[AzureKeyCredential]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/credential/AzureKeyCredential.java
[AzureNamedKeyCredential]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/credential/AzureNamedKeyCredential.java
[AzureSasCredential]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/credential/AzureSasCredential.java
[StorageSharedKeyCredential]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/storage/azure-storage-common/src/main/java/com/azure/storage/common/StorageSharedKeyCredential.java
[EventHubSharedKeyCredential]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs/src/main/java/com/azure/messaging/eventhubs/implementation/EventHubSharedKeyCredential.java
[ServiceBusSharedKeyCredential]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src/main/java/com/azure/messaging/servicebus/implementation/ServiceBusSharedKeyCredential.java
[secret-client-builder]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-secrets/src/main/java/com/azure/security/keyvault/secrets/SecretClientBuilder.java
[cosmos-client-builder]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/azure-cosmos/src/main/java/com/azure/cosmos/CosmosClientBuilder.java
[blobservice-client-builder]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/storage/azure-storage-blob/src/main/java/com/azure/storage/blob/BlobServiceClientBuilder.java
[shareservice-client-builder]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/storage/azure-storage-file-share/src/main/java/com/azure/storage/file/share/ShareServiceClientBuilder.java
[queue-client-builder]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/storage/azure-storage-queue/src/main/java/com/azure/storage/queue/QueueClientBuilder.java
[eventhubs-client-builder]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs/src/main/java/com/azure/messaging/eventhubs/EventHubClientBuilder.java
[servicebus-client-builder]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/src/main/java/com/azure/messaging/servicebus/ServiceBusClientBuilder.java

[sas-best-practices]: https://docs.microsoft.com/azure/storage/common/storage-sas-overview?toc=/azure/storage/blobs/toc.json#best-practices-when-using-sas
[sas-uri-png]: https://docs.microsoft.com/azure/storage/common/media/storage-sas-overview/sas-storage-uri.png
[sas-101]: https://docs.microsoft.com/azure/storage/common/storage-sas-overview#how-a-shared-access-signature-works
[amqp-cbs]: https://docs.microsoft.com/azure/service-bus-messaging/service-bus-amqp-protocol-guide#claims-based-authorization