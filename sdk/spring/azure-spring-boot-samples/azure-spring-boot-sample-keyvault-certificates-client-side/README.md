# Azure Spring Boot Key Vault Certificates client library for Java

## Key concepts
This sample illustrates how to use [Azure Spring Boot Starter Key Vault Certificates ][azure_spring_boot_starter_key_vault_certificates].

This sample should work together with [azure-spring-boot-sample-keyvault-certificates-server-side].

## Getting started

### Prerequisites
- [Environment checklist][environment_checklist]

### Config the sample
Fulfill these properties in application.yml:
```yaml
azure:
  keyvault:
    uri:                 # The URI to the Azure Key Vault used
    tenant-id:           # The Tenant ID for your Azure Key Vault (needed if you are not using managed identity).
    client-id:           # The Client ID that has been setup with access to your Azure Key Vault (needed if you are not using managed identity).
    client-secret:       # The Client Secret that will be used for accessing your Azure Key Vault (needed if you are not using managed identity).
```

### How to run

#### Run with TLS
1. Start azure-spring-boot-sample-keyvault-certificates-server-side's SampleApplication
1. Start azure-spring-boot-sample-keyvault-certificates-client-side's SampleApplication
1. Access http://localhost:8080/tls

Then you will get
```text
Response from "https://localhost:8443/": Hello World
```

#### Run with MTLS
1. In the sample `ApplicationConfiguration.class`, change the `self-signed` to your certificate alias.
    <!-- embedme ../azure-spring-boot-samples/azure-spring-boot-sample-keyvault-certificates-client-side/src/main/java/com/azure/spring/security/keyvault/certificates/sample/client/side/SampleApplicationConfiguration.java#L72-L77 -->
    ```java
    private static class ClientPrivateKeyStrategy implements PrivateKeyStrategy {
       @Override
       public String chooseAlias(Map<String, PrivateKeyDetails> map, Socket socket) {
          return "self-signed"; // It should be your certificate alias used in client-side
       }
    }
    ``` 
1. Start azure-spring-boot-sample-keyvault-certificates-server-side's SampleApplication
1. Start azure-spring-boot-sample-keyvault-certificates-client-side's SampleApplication with [MTLS] configuration.  
1. When the [MTLS] server starts, `tls endpoint`(http://localhost:8080/tls) will not be able to access the resource. Access http://localhost:8080/mtls

Then you will get
```text
Response from "https://localhost:8443/": Hello World
```


## Examples
## Troubleshooting
## Next steps
## Contributing

<!-- LINKS -->
[environment_checklist]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/ENVIRONMENT_CHECKLIST.md#ready-to-run-checklist
[azure_spring_boot_starter_key_vault_certificates]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-keyvault-certificates/README.md
[steps_to_store_certificate]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-keyvault-certificates/README.md#creating-an-azure-key-vault
[azure-spring-boot-sample-keyvault-certificates-server-side]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-keyvault-certificates-server-side
[MTLS]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-keyvault-certificates-client-side/README.md#run-with-MTLS
