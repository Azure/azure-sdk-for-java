# Release History

## 4.0.0-beta.2 (2022-05-27)

#### Dependency Updates

- Upgrade azure-sdk related dependencies' version, make them align to azure-sdk-bom:1.2.2 managed versions.
- Upgrade spring-cloud-azure related dependencies' version, make them align to spring-cloud-azure-dependencies:4.2.0 managed versions.

## 4.0.0-beta.1 (2022-05-10)

### Features Added

- Add library `spring-cloud-azure-native-configuration` to support `Spring Native` for compiling Spring Cloud Azure applications to native executables [#28053](https://github.com/Azure/azure-sdk-for-java/issues/28053), [#28158](https://github.com/Azure/azure-sdk-for-java/pull/28158).
    - spring-cloud-azure-starter-storage-blob
    - spring-cloud-azure-starter-storage-file-share
    - spring-cloud-azure-starter-storage-queue
    - spring-cloud-azure-starter-keyvault-secrets
    - spring-cloud-azure-starter-keyvault-certificates
    - spring-cloud-azure-starter-appconfiguration
    - spring-cloud-azure-starter-eventhubs
    - spring-cloud-azure-starter-integration-storage-queue
    - spring-cloud-azure-starter-integration-eventhubs

