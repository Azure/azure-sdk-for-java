# Azure Spring Boot Key Vault Certificates client library for Java

## Key concepts
This sample illustrates how to use [Azure Spring Boot Starter Key Vault Certificates ][azure_spring_boot_starter_key_vault_certificates].

This sample should work together with [azure-spring-boot-sample-keyvault-certificates-server-side].

## Getting started

### Prerequisites
- [Environment checklist][environment_checklist]
- Start azure-spring-boot-sample-keyvault-certificates-server-side's SampleApplication.

### Run sample with service principal
1. Set environment variables created in `azure-spring-boot-sample-keyvault-certificates-server-side` application by running command:
   ```
   source script/setup.sh
   ```
#### Using TLS with service principal
1. Start azure-spring-boot-sample-keyvault-certificates-client-side's SampleApplication by running command:
   ```
   mvn spring-boot:run
   ```
1. Access http://localhost:8080/tls

    Then you will get
    ```text
    Response from "https://localhost:8443/": Hello World
    ```

#### Using mTLS with service principal
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
1. Add properties in application.yml of `server side` on the base of current configuration:
    ```yaml
    server:
      ssl:
        client-auth: need        # Used for mTLS
        trust-store-type: AzureKeyVault   # Used for mTLS   
    ```
1. Start azure-spring-boot-sample-keyvault-certificates-client-side's SampleApplication by running command:
   ```
   mvn spring-boot:run
   ```
1. When the mTLS server starts, `tls endpoint`(http://localhost:8080/tls) will not be able to access the resource. Access http://localhost:8080/mTLS

    Then you will get
    ```text
    Response from "https://localhost:8443/": Hello World
    ```

### Run sample with managed identity
1. If you are using managed identity instead of service principal, use below properties in your `application.yml`:

    ```yaml
    azure:
      keyvault:
        uri: ${KEY_VAULT_URI}
        managed-identity: # client-id of the user-assigned managed identity to use. If empty, then system-assigned managed identity will be used.
    ```
    Make sure the managed identity can access target Key Vault.
1. Set environment variables created in `azure-spring-boot-sample-keyvault-certificates-server-side` application by running command:
   ```
   source script/setup.sh
   ```

#### Using TLS with managed identity
1. Replace the `restTemplateWithTLS` bean in `SampleApplicationConfiguration.java` as
    <!-- embedme ../../azure-spring-boot/src/samples/java/com/azure/spring/keyvault/KeyVaultJcaManagedIdentitySample.java#L18-L36 -->
    ```java
    @Bean
    public RestTemplate restTemplateWithTLS() throws Exception {
        KeyStore trustStore = KeyStore.getInstance("AzureKeyVault");
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
            System.getProperty("azure.keyvault.uri"),
            System.getProperty("azure.keyvault.managed-identity"));
        trustStore.load(parameter);
        SSLContext sslContext = SSLContexts.custom()
                                           .loadTrustMaterial(trustStore, null)
                                           .build();
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext,
            (hostname, session) -> true);
        CloseableHttpClient httpClient = HttpClients.custom()
                                                    .setSSLSocketFactory(socketFactory)
                                                    .build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

        return new RestTemplate(requestFactory);
    }
    ```
1. Follow the above step of [Using TLS with service principal](#using-tls-with-service-principal).

#### Using mTLS with managed identity
1. Replace the `restTemplateWithMTLS` bean in `SampleApplicationConfiguration.java` as
    <!-- embedme ../../azure-spring-boot/src/samples/java/com/azure/spring/keyvault/KeyVaultJcaManagedIdentitySample.java#L42-L61 -->
    ```java
    @Bean
    public RestTemplate restTemplateWithMTLS() throws Exception {
        KeyStore azureKeyVaultKeyStore = KeyStore.getInstance("AzureKeyVault");
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
            System.getProperty("azure.keyvault.uri"),
            System.getProperty("azure.keyvault.managed-identity"));
        azureKeyVaultKeyStore.load(parameter);
        SSLContext sslContext = SSLContexts.custom()
                                           .loadTrustMaterial(azureKeyVaultKeyStore, null)
                                           .loadKeyMaterial(azureKeyVaultKeyStore, "".toCharArray(), new ClientPrivateKeyStrategy())
                                           .build();
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext,
            (hostname, session) -> true);
        CloseableHttpClient httpClient = HttpClients.custom()
                                                    .setSSLSocketFactory(socketFactory)
                                                    .build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

        return new RestTemplate(requestFactory);
    }
    ```
1. Follow the above step of [Using mTLS with service principal](#using-mtls-with-service-principal).

## Examples
## Troubleshooting
## Next steps
## Contributing

<!-- LINKS -->
[environment_checklist]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/ENVIRONMENT_CHECKLIST.md#ready-to-run-checklist
[azure_spring_boot_starter_key_vault_certificates]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-keyvault-certificates/README.md
[steps_to_store_certificate]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-keyvault-certificates/README.md#creating-an-azure-key-vault
[azure-spring-boot-sample-keyvault-certificates-server-side]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-keyvault-certificates-server-side
