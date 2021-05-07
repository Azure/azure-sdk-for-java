# Azure Spring Boot Key Vault Certificates client library for Java

## Key concepts
This sample illustrates how to use [Azure Spring Boot Starter Key Vault Certificates ][azure_spring_boot_starter_key_vault_certificates].

In this sample, a certificate named `self-signed` is stored into an Azure Key Vault, and a sample Spring application will use its value as a configuration property value.

This sample can work together with [azure-spring-boot-sample-keyvault-certificates-client-side].

## Getting started

### Prerequisites
- [Environment checklist][environment_checklist]

### Store Certificate
We need to store certificate `self-signed` into Azure Key Vault with the following steps: [store certificate ][steps_to_store_certificate]

### Config the sample
Fulfill these properties in application.yml:
```yaml
azure:
  keyvault:
    uri:                 # The URI to the Azure Key Vault used
    tenant-id:           # The Tenant ID for your Azure Key Vault (needed if you are not using managed identity).
    client-id:           # The Client ID that has been setup with access to your Azure Key Vault (needed if you are not using managed identity).
    client-secret:       # The Client Secret that will be used for accessing your Azure Key Vault (needed if you are not using managed identity).
server:
  port: 8443
  ssl:
    key-alias:           # The alias corresponding to the certificate in Azure Key Vault.
    key-store-type:      # The keystore type that enables the use of Azure Key Vault for your server-side SSL certificate.
```

### How to run

#### Run with TLS
1. Start SampleApplication
1. Access https://localhost:8443/

Then you will get
```text
Hello World
``` 

#### Run with MTLS

1. Add properties in application.yml:
```yaml
azure:
  keyvault:
    uri:                 # The URI to the Azure Key Vault used
    tenant-id:           # The Tenant ID for your Azure Key Vault (needed if you are not using managed identity).
    client-id:           # The Client ID that has been setup with access to your Azure Key Vault (needed if you are not using managed identity).
    client-secret:       # The Client Secret that will be used for accessing your Azure Key Vault (needed if you are not using managed identity).
server:
  port: 8443
  ssl:
    key-alias:           # The alias corresponding to the certificate in Azure Key Vault.
    key-store-type:      # The keystore type that enables the use of Azure Key Vault for your server-side SSL certificate.
    client-auth:         # Used for MTLS
    trust-store-type:    # Used for MTLS   
```
1. Start SampleApplication
1. MTLS for mutual authentication. So your client needs have a trusted CA certificate.([azure-spring-boot-sample-keyvault-certificates-client-side]is a trusted client sample.)
1. Your client access https://localhost:8443/

Then the client or server will get
```text
Hello World
``` 

## Examples
## Troubleshooting
## Next steps
## Run with Maven
```
cd azure-spring-boot-samples/azure-spring-boot-sample-keyvault-certificates-server-side
mvn spring-boot:run
```
## Contributing

<!-- LINKS -->
[environment_checklist]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/ENVIRONMENT_CHECKLIST.md#ready-to-run-checklist
[azure_spring_boot_starter_key_vault_certificates]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-keyvault-certificates/README.md
[steps_to_store_certificate]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-keyvault-certificates/README.md#creating-an-azure-key-vault
[azure-spring-boot-sample-keyvault-certificates-client-side]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-keyvault-certificates-client-side
