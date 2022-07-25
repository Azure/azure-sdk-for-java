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
ConfidentialLedgerCertificateClientBuilder confidentialLedgerCertificateClientbuilder = new ConfidentialLedgerCertificateClientBuilder()
    .certificateEndpoint("https://identity.confidential-ledger.core.azure.com")
    .credential(new DefaultAzureCredentialBuilder().build())
    .httpClient(HttpClient.createDefault());
        
ConfidentialLedgerCertificateClient confidentialLedgerCertificateClient = confidentialLedgerCertificateClientbuilder.buildClient();

String ledgerId = "java-tests";
// this is a built in test of getLedgerCertificate
Response<BinaryData> ledgerCertificateWithResponse = confidentialLedgerCertificateClient
    .getLedgerIdentityWithResponse(ledgerId, null);
BinaryData certificateResponse = ledgerCertificateWithResponse.getValue();
ObjectMapper mapper = new ObjectMapper();
JsonNode jsonNode = mapper.readTree(certificateResponse.toBytes());
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
[ConfidentialLedgerClientSample.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/ConfidentialLedgerClientSample.java)|Sample code to configure your client base with|
[CreateOrUpdateUserSample.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/CreateOrUpdateUserSample.java)|Add a user to a ledger or update an existing user|
[DeleteUserSample.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/DeleteUserSample.java)|Delete a user from a ledger|
[ListCollectionIdsSample.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/ListCollectionIdsSample.java)|Get a list of all collection ids for a ledger|
[ListConsortiumMembersSample.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/ListConsortiumMembersSample.java)|Get all consortium members for a ledger|
[GetConstitutionSample.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/GetConstitutionSample.java)|Get the constitution for a ledger|
[GetCurrentLedgerEntrySample.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/GetCurrentLedgerEntrySample.java)|Get the most recent ledger entry|
[GetEnclaveQuotesSample.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/GetEnclaveQuotesSample.java)|Get the enclave quotes for a ledger|
|[GetLedgerEntriesSample.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/GetLedgerEntriesSample.java)|List ledger entries in the confidential ledger|
[GetLedgerEntrySample.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/GetLedgerEntrySample.java)|Get a specific ledger entry|
[GetLedgerIdentitySample.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/GetLedgerIdentitySample.java)|Get the identity of a ledger|
[GetReceiptSample.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/GetReceiptSample.java)|Get a receipt from a transaction|
[GetTransactionStatusSample.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/GetTransactionStatusSample.java)|Get the status of a ledger entry|
[GetUserSample.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/GetUserSample.java)|Get specific user data from a ledger|
[PostLedgerEntrySample.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/PostLedgerEntrySample.java)|Add a ledger entry. This also contains a more robust example of response handling.|

## Troubleshooting

Troubleshooting steps can be found [here](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/README.md#troubleshooting).

## Next steps

See [Next steps](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/README.md#next-steps).

## Contributing

If you would like to become an active contributor to this project please refer to our [Contribution Guidelines](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/README.md#contributing) for more information.
