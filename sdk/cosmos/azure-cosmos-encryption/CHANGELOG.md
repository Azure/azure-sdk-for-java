## Release History

### 1.3.0-beta.1 (Unreleased)

#### Features Added

#### Breaking Changes

#### Bugs Fixed

#### Other Changes

### 1.2.1 (2022-06-01)
#### Other Changes
* Updated `azure-cosmos` to version `4.30.1`.

### 1.2.0 (2022-05-20)
#### Other Changes
* Updated `azure-cosmos` to version `4.30.0`.

### 1.1.0 (2022-04-22)
#### Other Changes
* Updated `azure-cosmos` to version `4.29.0`.

### 1.0.1 (2022-04-14)
### Other Changes
* Upgraded `azure-cosmos` to version `4.28.1`.

### 1.0.0 (2022-03-18)
#### Features Added
* Released GA version 1.0.0 of `azure-cosmos-encryption`.

### 1.0.0-beta.10 (2022-03-10)
#### New Features
* Added `Patch API` for encryption - See [PR 26672](https://github.com/Azure/azure-sdk-for-java/pull/26672)
* Added `Bulk API` for encryption - See [PR 25195](https://github.com/Azure/azure-sdk-for-java/pull/25195)
* Added `CosmosEncryptionClientBuilder` for creating `CosmosEncryptionAsyncClient` and `CosmosEncryptionClient` - See [PR 27158](https://github.com/Azure/azure-sdk-for-java/pull/27158)
* Renamed source package to `com.azure.cosmos.encryption` inline with artifact id.
* Updated `azure-cosmos` to version `4.27.0`.

#### Bugs Fixed
* Fixed issue with collection/pkrange cache on collection recreate scenario for gateway mode - See [PR 25811](https://github.com/Azure/azure-sdk-for-java/pull/25811)

### 1.0.0-beta.9 (2021-10-14)
#### New Features
* Added support for transaction batch.
* Added support for aggregate query.
* Updated `azure-cosmos` to version `4.20.0`.

#### Bugs Fixed
* Fixed Json property name of ClientEncryptionKeyProperties.

### 1.0.0-beta.8 (2021-09-09)
#### New Features
* Added change feed support for pull and push model.

### 1.0.0-beta.7 (2021-08-16)
#### New Features
* Redesigned Database and Container encryption cache for staleness when recreating database and containers in encryption.

#### Bugs Fixed
* Fixed encryption create with contentResponseOnWriteEnabled false

### 1.0.0-beta.6 (2021-06-11)
#### New Features
* Signed and moved AAP cryptography jars to Azure dev-ops feed from Azure blob storage.

### 1.0.0-beta.5 (2021-05-20)
#### New Features
* Added type in EncryptionKeyWrapMetadata constructor.
* Added validation for partition key on encrypted field during Container creation.
* Exposed policyFormatVersion in ClientEncryptionPolicy.

### 1.0.0-beta.4 (2021-05-12)
#### New Features
* Added sync api support for Cosmos encryption.
* Increased the encryption string length support from 8000 to max supported by Cosmos.

### 1.0.0-beta.3 (2021-04-26)
#### New Features
* Removed algorithm string from encryptionKeyWrapMetadata.
* Increased the encryption string length support from 1024 to 8000.
* Converted MicrosoftDataEncryptionException to CosmosException on ClientEncryptionKey fetch.

### 1.0.0-beta.2 (2021-04-07)
#### Bugs Fixed
* Fixed after burner exception on encryption.

### 1.0.0-beta.1 (2021-04-06)
#### New Features
* Added Azure Cosmos encryption used for encrypting data with user provided key before saving into CosmosDB and decrypting it when reading back from the database.

