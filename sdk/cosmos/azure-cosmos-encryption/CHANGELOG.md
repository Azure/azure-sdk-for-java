# Release History

## 1.0.0-beta.7 (Unreleased)


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
#### Key Bug Fixes
* Fixed after burner exception on encryption.

### 1.0.0-beta.1 (2021-04-06)
#### New Features
* Added Azure Cosmos encryption used for encrypting data with user provided key before saving into CosmosDB and decrypting it when reading back from the database.

