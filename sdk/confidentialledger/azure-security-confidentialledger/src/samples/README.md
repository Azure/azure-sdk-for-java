---
page_type: sample
languages:
  - java
products:
  - azure
urlFragment: confidential-ledger-java-samples
---

# Azure Confidential Ledger client library samples for Java

Azure Confidential Ledger samples are a set of self-contained Java programs that demonstrate interacting with Azure self-contained service using the client library. Each sample focuses on a specific scenario and can be executed independently.

## Key concepts

Key concepts are explained in detail [here](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/README.md#key-concepts).

## Getting started

Getting started explained in detail [here](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/README.md#getting-started).

## Examples

```java readme-sample-createClient
ConfidentialLedgerIdentityClientBuilder confidentialLedgerIdentityClientbuilder = new ConfidentialLedgerIdentityClientBuilder()
    .identityServiceUri("https://identity.confidential-ledger.core.azure.com")
    .credential(new DefaultAzureCredentialBuilder().build())
    .httpClient(HttpClient.createDefault());
        
ConfidentialLedgerIdentityClient confidentialLedgerIdentityClient = confidentialLedgerIdentityClientbuilder.buildClient();

String ledgerId = "java-tests";
// this is a built in test of getLedgerIdentity
Response<BinaryData> ledgerIdentityWithResponse = confidentialLedgerIdentityClient
    .getLedgerIdentityWithResponse(ledgerId, null);
BinaryData identityResponse = ledgerIdentityWithResponse.getValue();
ObjectMapper mapper = new ObjectMapper();
JsonNode jsonNode = mapper.readTree(identityResponse.toBytes());
String ledgerTslCertificate = jsonNode.get("ledgerTlsCertificate").asText();


SslContext sslContext = SslContextBuilder.forClient()
    .trustManager(new ByteArrayInputStream(ledgerTslCertificate.getBytes(StandardCharsets.UTF_8))).build();
reactor.netty.http.client.HttpClient reactorClient = reactor.netty.http.client.HttpClient.create()
    .secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
HttpClient httpClient = new NettyAsyncHttpClientBuilder(reactorClient).wiretap(true).build();

ConfidentialLedgerClient confidentialLedgerClient =
    new ConfidentialLedgerClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .httpClient(httpClient)
            .ledgerEndpoint("https://my-ledger.confidential-ledger.azure.com")
            .buildClient();
```

The following sections provide code samples covering common scenario operations with the Azure Confidential Ledger client library.

All of these samples need the endpoint to your Confidential Ledger resource, and your Confidential Ledger API key.

|**File Name**|**Description**|
|----------------|-------------|
[ConfidentialLedgerClientBase.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/ConfidentialLedgerClientBase.java)|Sample code to configure your client base with|
[CreateOrUpdateUser.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/CreateOrUpdateUser.java)|Add a user to a ledger or update an existing user|
[DeleteUser.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/DeleteUser.java)|Delete a user from a ledger|
[GetCollectionIds.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/GetCollectionIds.java)|Get a list of all collection ids for a ledger|
[GetConsortiumMembers.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/GetConsortiumMembers.java)|Get all consortium members for a ledger|
[GetConstitution.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/GetConstitution.java)|Get the constitution for a ledger|
[GetCurrentLedgerEntry.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/GetCurrentLedgerEntry.java)|Get the most recent ledger entry|
[GetEnclaveQuotes.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/GetEnclaveQuotes.java)|Get the enclave quotes for a ledger|
|[GetLedgerEntries.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/GetLedgerEntries.java)|List ledger entries in the confidential ledger|
[GetLedgerEntry.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/GetLedgerEntry.java)|Get a specific ledger entry|
[GetLedgerIdentity.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/GetLedgerIdentity.java)|Get the identity of a ledger|
[GetReceipt.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/GetReceipt.java)|Get a receipt from a transaction|
[GetTransactionStatus.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/GetTransactionStatus.java)|Get the status of a ledger entry|
[GetUser.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/GetUser.java)|Get specific user data from a ledger|
[PostLedgerEntry.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/PostLedgerEntry.java)|Add a ledger entry. This also contains a more robust example of response handling.|

## Troubleshooting

Troubleshooting steps can be found [here](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/README.md#troubleshooting).

## Next steps

See [Next steps](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/README.md#next-steps).

## Contributing

If you would like to become an active contributor to this project please refer to our [Contribution Guidelines](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/README.md#contributing) for more information.
