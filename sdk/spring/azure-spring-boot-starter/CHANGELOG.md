# Release History

## 2.3.3 (2020-07-28)

- Reorder & simplify KeyVaultEnvironmentPostProcessor.isKeyVaultEnabled
- Rework KeyVault refreshing logic to increase performance
- Add KeyVault Spring Boot Actuator support
- Deliver multiple KeyVault support
- Support for case sensitive key vault keys
- Support caching KeyVault values locally
- Fix issues that Spring Boot Application hangs with KeyVault starter
- Adds support for blob service in emulators
- Skip AAD internal filter when authenticated or token not issued by AAD
- Update README.md of metrics starter to refer to new Java agent based approach
- retire starters for mediaservices, storageblob, and servicebus
- Update JMS library to com.microsoft.azure:azure-servicebus-jms
