# Azure Spring Boot Key Vault Certificates client library for Java

## Key concepts
This sample illustrates how to use [Azure Spring Boot Starter Key Vault Certificates ][azure_spring_boot_starter_key_vault_certificates].

In this sample, a certificate named `self-signed` is stored into an Azure Key Vault, and a sample Spring application will use its value as a configuration property value.

This sample can work together with [azure-spring-boot-sample-keyvault-certificates-client-side].

## Getting started

### Prerequisites
- [Environment checklist][environment_checklist]
- This sample will create a resource group and Azure Key Vault in your specified subscription. 
- This sample will create and store a certificate `self-signed` in your Azure Key Vault.
- This sample will create a service principal to read certificates/keys/secrets from your Azure Key Vault.

### Run Sample with service principal
1. Run command `az login` to login to the Azure CLI.
1. Build up required Azure resources by running command. 
   ```
   cd azure-spring-boot-samples/azure-spring-boot-sample-keyvault-certificates-server-side
   source setup.sh <your subscription id> <resource group name> <region> <key vault name> <service principal name>
   ```
#### Using TLS
1. Run command `mvn spring-boot:run`
1. Access https://localhost:8443/

Then you will get
```text
Hello World
``` 

#### Using MTLS

1. Add properties in application.yml on the base of current configuration:
```yaml
server:
  ssl:
    client-auth:         # Used for MTLS
    trust-store-type:    # Used for MTLS   
```
2. Run command `mvn spring-boot:run`
1. MTLS for mutual authentication. So your client needs have a trusted CA certificate.([azure-spring-boot-sample-keyvault-certificates-client-side]is a trusted client sample.)
1. Your client access https://localhost:8443/

Then the client or server will get
```text
Hello World
``` 
### Run Sample with managed identity
If you are using managed identity instead of service principal, use below properties in your `application.yml`:

```yaml
azure:
  keyvault:
    uri: ${KEY_VAULT_URI}
#    managed-identity: # client-id of the user-assigned managed identity to use. If empty, then system-assigned managed identity will be used.
server:
  ssl:
    key-alias: self-signed
    key-store-type: AzureKeyVault
```
Make sure the managed identity can access target Key Vault.

1. Run command `az login` to login to the Azure CLI.
1. Build up required Azure resources by running command
   ```
   cd azure-spring-boot-samples/azure-spring-boot-sample-keyvault-certificates-server-side
   source setup.sh <your subscription id> <resource group name> <region> <key vault name>
   ```

1. follow the above step of [Using TLS](#Using TLS) or [Using MTLS](#Using MTLS).

## Examples
## Troubleshooting
## Next steps
## Run with Maven
## Contributing

<!-- LINKS -->
[environment_checklist]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/ENVIRONMENT_CHECKLIST.md#ready-to-run-checklist
[azure_spring_boot_starter_key_vault_certificates]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-keyvault-certificates/README.md
[steps_to_store_certificate]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-keyvault-certificates/README.md#creating-an-azure-key-vault
[azure-spring-boot-sample-keyvault-certificates-client-side]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-keyvault-certificates-client-side
