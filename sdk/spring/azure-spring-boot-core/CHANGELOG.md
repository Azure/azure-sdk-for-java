# Release History

## 1.0.0-beta.1 (Unreleased)
### New Features
- Support unified properties as below when the service specific properties are not configured.([#22396](https://github.com/Azure/azure-sdk-for-java/issues/22396))
    ```
    spring:
      cloud:
        azure:
          authority-host:
          client-id:
          client-secret:
          certificate-path:
          msi-enabled:
          tenant-id:
          environment:
    ```
### Breaking Changes
- Property type of `spring.cloud.azure.environment` are changed from Enum to String. Supported values are "Azure", "AzureChina", "AzureGermany" and "AzureUSGovernment".