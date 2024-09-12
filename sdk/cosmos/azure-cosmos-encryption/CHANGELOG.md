## Release History

### 2.15.0-beta.1 (Unreleased)

#### Features Added

#### Breaking Changes

#### Bugs Fixed

#### Other Changes

### 2.14.1 (2024-09-10)

#### Other Changes
* Updated `azure-cosmos` to version `4.63.3`.

### 2.14.0 (2024-07-26)
#### Other Changes
* Updated `azure-cosmos` to version `4.63.0`.

### 2.13.0 (2024-07-02)
#### Other Changes
* Updated `azure-cosmos` to version `4.62.0`.

### 2.12.0 (2024-05-19)
#### Other Changes
* Updated `azure-cosmos` to version `4.60.0`.

### 2.11.0 (2024-04-27)
#### Other Changes
* Updated `azure-cosmos` to version `4.59.0`.

#### Features Added
* Added public APIs `getCustomItemSerializer` and `setCustomItemSerializer` to allow customers to specify custom payload transformations or serialization settings. - See [PR 38997](https://github.com/Azure/azure-sdk-for-java/pull/38997)

### 2.10.0 (2024-04-16)

#### Other Changes
* Updated `azure-cosmos` to version `4.58.0`.

### 2.9.0 (2024-03-26)
#### Other Changes
* Updated `azure-cosmos` to version `4.57.0`.

### 2.8.0 (2024-02-08)
#### Other Changes
* Updated `azure-cosmos` to version `4.55.0`.

### 2.7.0 (2023-12-01)
#### Other Changes
* Updated `azure-cosmos` to version `4.53.0`.

### 2.6.0 (2023-10-24)
#### Other Changes
* Updated `azure-cosmos` to version `4.52.0`.

### 2.5.0 (2023-09-25)
#### Other Changes
* Updated `azure-cosmos` to version `4.50.0`.

### 2.4.0 (2023-08-21)
#### Other Changes
* Updated `azure-cosmos` to version `4.49.0`.

### 2.3.0 (2023-07-18)
#### Other Changes
* Updated `azure-cosmos` to version `4.48.0`.

### 2.2.0 (2023-06-09)
#### Other Changes
* Updated `azure-cosmos` to version `4.46.0`.

### 2.1.0 (2023-05-12)
#### Bugs Fixed
* Fixed an issue where empty array causes `NoSuchElementException` in `EncryptionProcessor` - See [PR 34847](https://github.com/Azure/azure-sdk-for-java/pull/34847)

#### Other Changes
* Updated `azure-cosmos` to version `4.45.0`.

### 2.0.0 (2023-04-25)

#### Features Added
* Added support for allowing partition key path and id to be part of client encryption policy - See [PR 33648](https://github.com/Azure/azure-sdk-for-java/pull/33648)

#### Breaking Changes
* Adds support for ParititonKey and Id encryption, when the PolicyFormatVersion is set to 2 - See [PR 33648](https://github.com/Azure/azure-sdk-for-java/pull/33648)

### 1.12.0 (2023-03-17)

#### Other Changes
* Updated `azure-cosmos` to version `4.42.0`.

### 1.11.0 (2023-02-17)

#### Other Changes
* Updated `azure-cosmos` to version `4.41.0`.

### 1.10.0 (2023-01-13)
#### Other Changes
* Updated `azure-cosmos` to version `4.40.0`.

### 1.9.0 (2022-11-16)
#### Other Changes
* Updated `azure-cosmos` to version `4.39.0`.

### 1.8.1 (2022-10-21)
#### Other Changes
* Updated test dependency of apache commons-text to version 1.10.0 - CVE-2022-42889 - See [PR 31674](https://github.com/Azure/azure-sdk-for-java/pull/31674)
* Updated `azure-cosmos` to version `4.38.1`.

### 1.8.0 (2022-10-12)
#### Other Changes
* Updated `azure-cosmos` to version `4.38.0`.

### 1.7.1 (2022-10-07)
#### Other Changes
* Updated `azure-cosmos` to version `4.37.1`.

### 1.7.0 (2022-09-30)
#### Other Changes
* Updated `azure-cosmos` to version `4.37.0`.

### 1.6.0 (2022-09-15)
#### Other Changes
* Updated `azure-cosmos` to version `4.36.0`.

### 1.5.0 (2022-08-19)
#### Other Changes
* Updated `azure-cosmos` to version `4.35.0`.

### 1.4.1 (2022-07-22)
#### Other Changes
* Updated `azure-cosmos` to version `4.33.1`.

### 1.4.0 (2022-07-14)
#### Other Changes
* Updated `azure-cosmos` to version `4.33.0`.

### 1.3.0 (2022-06-08)
#### Other Changes
* Updated `azure-cosmos` to version `4.31.0`.

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
* Added Azure Cosmos encryption used for encrypting data with user provided key before saving into Cosmos DB and decrypting it when reading back from the database.

