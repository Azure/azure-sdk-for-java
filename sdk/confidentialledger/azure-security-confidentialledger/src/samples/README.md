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

Key concepts are explained in detail [here][SDK_README_KEY_CONCEPTS].

## Getting started

Getting started explained in detail [here][SDK_README_GETTING_STARTED].

## Examples

```java readme-sample-createClient
ConfidentialLedgerClient confidentialLedgerClient =
        new ConfidentialLedgerClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .ledgerUri("https://my-ledger.confidential-ledger.azure.com")
                .buildClient();
```

The following sections provide code samples covering common scenario operations with the Azure Confidential Ledger client library.

All of these samples need the endpoint to your Confidential Ledger resource, and your Confidential Ledger API key.

|**File Name**|**Description**|
|----------------|-------------|
[ConfidentialLedgerClientBase.java][confidential_ledger_client_base]|Sample code to configure your client base with|
[CreateOrUpdateUser.java][create_or_update_user]|Add a user to a ledger or update an existing user|
[DeleteUser.java][delete_user]|Delete a user from a ledger|
[GetCollectionIds.java][get_collection_ids]|Get a list of all collection ids for a ledger|
[GetConsortiumMembers.java][get_consortium_members]|Get all consortium members for a ledger|
[GetConstitution.java][get_constitution]|Get the constitution for a ledger|
[GetCurrentLedgerEntry.java][get_current_ledger_entry]|Get the most recent ledger entry|
[GetEnclaveQuotes.java][get_enclave_quotes]|Get the enclave quotes for a ledger|
|[GetLedgerEntries.java][get_ledger_entries]|List ledger entries in the confidential ledger|
[GetLedgerEntry.java][get_ledger_entry]|Get a specific ledger entry|
[GetLedgerIdentity.java][get_ledger_identity]|Get the identity of a ledger|
[GetReceipt.java][get_receipt]|Get a receipt from a transaction|
[GetTransactionStatus.java][get_transaction_status]|Get the status of a ledger entry|
[GetUser.java][get_user]|Get specific user data from a ledger|
[PostLedgerEntry.java][post_ledger_entry]|Add a ledger entry|

## Troubleshooting

Troubleshooting steps can be found [here][SDK_README_TROUBLESHOOTING].

## Next steps

See [Next steps][SDK_README_NEXT_STEPS].

## Contributing

If you would like to become an active contributor to this project please refer to our [Contribution Guidelines][SDK_README_CONTRIBUTING] for more information.

<!-- LINKS -->
[SDK_README_CONTRIBUTING]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/README.md#contributing
[SDK_README_GETTING_STARTED]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/README.md#getting-started
[SDK_README_TROUBLESHOOTING]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/README.md#troubleshooting
[SDK_README_KEY_CONCEPTS]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/README.md#key-concepts
[SDK_README_DEPENDENCY]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/README.md#include-the-package
[SDK_README_NEXT_STEPS]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/README.md#next-steps
[get_ledger_entries]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/GetLedgerEntries.java
[delete_user]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/DeleteUser.java
[confidential_ledger_client_base]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/ConfidentialLedgerClientBase.java
[post_ledger_entry]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/PostLedgerEntry.java
[get_user]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/GetUser.java
[create_or_update_user]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/CreateOrUpdateUser.java
[get_collection_ids]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/GetCollectionIds.java
[get_consortium_members]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/GetConsortiumMembers.java
[get_constitution]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/GetConstitution.java
[get_current_ledger_entry]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/GetCurrentLedgerEntry.java
[get_enclave_quotes]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/GetEnclaveQuotes.java
[get_ledger_entry]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/GetLedgerEntry.java
[get_ledger_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/GetLedgerIdentity.java
[get_receipt]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/GetReceipt.java
[get_transaction_status]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/GetTransactionStatus.java
