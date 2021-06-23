# Azure Key Vault Certificates Spring Boot starter client library for Java
Azure Key Vault Certificates Spring Boot Starter is Spring starter for [Azure Key Vault Certificates](https://docs.microsoft.com/rest/api/keyvault/about-keys--secrets-and-certificates#BKMK_WorkingWithSecrets), it allows you to securely manage and tightly control your certificates.

[Package (Maven)][package] | [API reference documentation][refdocs] | [Samples][sample]

## Getting started

### Prerequisites
- [Environment checklist][environment_checklist]

### Include the package
[//]: # ({x-version-update-start;com.azure.spring:azure-spring-boot-starter-keyvault-certificates;current})
```xml
<dependency>
    <groupId>com.azure.spring</groupId>
    <artifactId>azure-spring-boot-starter-keyvault-certificates</artifactId>
    <version>3.0.0-beta.7</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Creating an Azure Key Vault

To create an Azure Key Vault use the command line below:

```shell
  export KEY_VAULT=mykeyvault
  export RESOURCE_GROUP=myresourcegroup
  az keyvault create --name ${KEY_VAULT} -g ${RESOURCE_GROUP}
```

### Create a self-signed certificate

To create a self-signed certificate use the command line below:

```shell
  export CERTIFICATE_ALIAS=self-signed
  az keyvault certificate create --vault-name ${KEY_VAULT} \
    -n ${CERTIFICATE_ALIAS} -p "$(az keyvault certificate get-default-policy)"
```

## Key concepts
This starter provides a KeyStore (`AzureKeyVault`) which can get certificates from `JRE` / `specific path` / `Azure Key Vault` / `classpath` .

## Examples
### Server side SSL

#### Using a client ID and client secret

To create a client and client secret use the command line below:
```shell
  export APP_NAME=myApp
  az ad app create --display-name ${APP_NAME}
  az ad sp create-for-rbac --name ${APP_NAME}
  export CLIENT_ID=$(az ad sp list --display-name ${APP_NAME} | jq -r '.[0].appId')
  az ad app credential reset --id ${CLIENT_ID}
```

Store the values returned, which will be used later.

Add these items in your `application.yml`:
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
    key-store-type: AzureKeyVault  # The keystore type that enables the use of Azure Key Vault for your server-side SSL certificate.
```

Make sure the client-id can access target Key Vault. Here are steps to configure access policy:

To grant access use the command line below:

```shell
  az keyvault set-policy --name ${KEY_VAULT} \
        --object-id ${CLIENT_ID} \
        --secret-permissions get list \
        --certificate-permissions get list \
        --key-permissions get list
```
#### Using a managed identity

To assign a managed identity use the command line below:

```shell
  export SPRING_CLOUD_APP=myspringcloudapp
  az spring-cloud app identity assign --name ${SPRING_CLOUD_APP}
  export MANAGED_IDENTITY=$(az spring-cloud app show \
    --name ${SPRING_CLOUD_APP} --query identity.principalId --output tsv)
```

If you are using managed identity instead of App registrations, add these items in your `application.yml`:

```yaml
azure:
  keyvault:
    uri: <the URI of the Azure Key Vault to use>
#    managed-identity: # client-id of the user-assigned managed identity to use. If empty, then system-assigned managed identity will be used.
server:
  ssl:
    key-alias: <the name of the certificate in Azure Key Vault to use>
    key-store-type: AzureKeyVault
```
Make sure the managed identity can access target Key Vault.

To grant access use the command line below:

```shell
  az keyvault set-policy --name ${KEY_VAULT} \
        --object-id ${MANAGED_IDENTITY} \
        --key-permissions get list \
        --secret-permissions get list \
        --certificate-permissions get list
```

### Client side SSL

#### Using a client ID and client secret
Add these items in your `application.yml`:
```yaml
azure:
  keyvault:
    uri:                 # The URI to the Azure Key Vault used
    tenant-id:           # The Tenant ID for your Azure Key Vault (needed if you are not using managed identity).
    client-id:           # The Client ID that has been setup with access to your Azure Key Vault (needed if you are not using managed identity).
    client-secret:       # The Client Secret that will be used for accessing your Azure Key Vault (needed if you are not using managed identity).
```
Make sure the client-id can access target Key Vault. 

Configure a `RestTemplate` bean which set the `AzureKeyVault` as trust store:

<!-- embedme ../azure-spring-boot-samples/azure-spring-boot-sample-keyvault-certificates-client-side/src/main/java/com/azure/spring/security/keyvault/certificates/sample/client/side/SampleApplicationConfiguration.java#L25-L45 -->
```java
@Bean
public RestTemplate restTemplateWithTLS() throws Exception {
    KeyStore azureKeyVaultKeyStore = KeyStore.getInstance("AzureKeyVault");
    KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
        System.getProperty("azure.keyvault.uri"),
        System.getProperty("azure.keyvault.tenant-id"),
        System.getProperty("azure.keyvault.client-id"),
        System.getProperty("azure.keyvault.client-secret"));
    azureKeyVaultKeyStore.load(parameter);
    SSLContext sslContext = SSLContexts.custom()
                                       .loadTrustMaterial(azureKeyVaultKeyStore, null)
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

#### Using a managed identity

If you are using managed identity instead of App registration, add these items in your `application.yml`:
```yaml
azure:
  keyvault:
    uri: <the URI of the Azure Key Vault to use>
#    managed-identity:  # client-id of the user-assigned managed identity to use. If empty, then system-assigned managed identity will be used.
```
Make sure the managed identity can access target Key Vault.

Configure a `RestTemplate` bean which set the `AzureKeyVault` as trust store:

<!-- embedme ../azure-spring-boot/src/samples/java/com/azure/spring/keyvault/KeyVaultJcaManagedIdentitySample.java#L22-L40 -->
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


### Enable mutual SSL (mTLS).
 
Step 1. On the server side, add these items in your `application.yml`:

```yaml
server:
  ssl:
    client-auth: need
    trust-store-type: AzureKeyVault
```

Step 2. On the client side, update `RestTemplate`. Example:

<!-- embedme ../azure-spring-boot-samples/azure-spring-boot-sample-keyvault-certificates-client-side/src/main/java/com/azure/spring/security/keyvault/certificates/sample/client/side/SampleApplicationConfiguration.java#L47-L75 -->
```java
@Bean
public RestTemplate restTemplateWithMTLS() throws Exception {
    KeyStore azureKeyVaultKeyStore = KeyStore.getInstance("AzureKeyVault");
    KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
        System.getProperty("azure.keyvault.uri"),
        System.getProperty("azure.keyvault.tenant-id"),
        System.getProperty("azure.keyvault.client-id"),
        System.getProperty("azure.keyvault.client-secret"));
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

private static class ClientPrivateKeyStrategy implements PrivateKeyStrategy {
    @Override
    public String chooseAlias(Map<String, PrivateKeyDetails> map, Socket socket) {
        return "self-signed"; // It should be your certificate alias used in client-side
    }
}
```

### Configuring Spring Cloud Gateway

To configure Spring Cloud Gateway for outbound SSL, add the following configuration:

```yaml
azure:
  keyvault:
    uri: <the URI of the Azure Key Vault to use>
    jca: 
      overrideTrustManagerFactory: true
```

Note: if any of your routes point to a service where the FQDN does not match the
issued certificate you will need to disable hostname verification. This will
be the case if your service is dynamically assigned a hostname by the hosting
platform you use. In this particular case add the configuration below to disable
hostname verification:

```yaml
azure:
  keyvault:
    jca:
      disableHostnameVerification: true
```

If you are developing you can completely disable the certificate and hostname
validation altogether by using the configuration below. **Note this is NOT 
recommended for production!**

```yaml
spring:
  cloud:
    gateway:
      httpclient:
        ssl:
          useInsecureTrustManager: true
```

### Refresh certificates when have un trust certificate

When the inbound certificate is not trusted, the KeyVaultKeyStore can fetch 
certificates from KeyVault if the following property is configured:

```yaml
azure:
  keyvault:
    jca:
      refresh-certificates-when-have-un-trust-certificate: true
```

Note: If you set refresh-certificates-when-have-un-trust-certificate=true, your server will be vulnerable
to attack, because every untrusted certificate will cause your application to send a re-acquire certificate request.

### Refresh certificate periodically

KeyVaultKeyStore can fetch certificates from KeyVault periodically if following property is configured:

```yaml
azure:
  keyvault:
    jca:
       certificates-refresh-interval: 1800000
```

Its value is 0(ms) by default, and certificate will not automatically refresh when its value <= 0.

### Refresh certificate by java code

You can also manually refresh the certificate by calling this method:
```java
KeyVaultCertificates.refreshCertsInfo();
```

### Specific path certificates
AzureKeyVault keystore will load certificates in the specific path:

well-know path: /etc/certs/well-known/
custom path: /etc/certs/custom/
The 2 paths can be configured by these propreties:

```yaml
azure:
  cert-path:
    well-known:     # The file location where you store the well-known certificate
    custom:         # The file location where you store the custom certificate
```

### Classpath certificates

AzureKeyVault keystore will load certificates in the classpath.

Add the certificates to `src/main/resources/keyvault` as classpath certificates.

Notes: 
1. The alias (certificate name) is constructed from the filename of the 
certificate (minus the extension). So if your filename is `mycert.x509` the
certificate will be added with the alias of `mycert`. 
2. The priority order of the certificates is: 
    1. Certificates from JRE.
    2. Certificates from well-known file path.
    3. Certificates from custom file path.
    4. Certificates from Azure Key Vault. 
    5. Certificates from classpath.


## Troubleshooting
### Enable client logging
Azure SDKs for Java offers a consistent logging story to help aid in troubleshooting application errors and expedite their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

### Enable Spring logging
Spring allow all the supported logging systems to set logger levels set in the Spring Environment (for example, in application.yml) by using `logging.level.<logger-name>=<level>` where level is one of TRACE, DEBUG, INFO, WARN, ERROR, FATAL, or OFF. The root logger can be configured by using logging.level.root.

The following example shows potential logging settings in `application.yml`:
```yaml
logging:
  level:
    root: WARN
    org:
      springframework.web: DEBUG
      hibernate: ERROR
```

For more information about setting logging in spring, please refer to the [official doc](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#boot-features-logging).

## Next steps
The following section provide a sample project illustrating how to use the starter.
### More sample code
- [Key Vault Certificates][sample]

## Contributing
This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [instructions here](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/CONTRIBUTING.md) to build from source or contribute.

<!-- LINKS -->
[refdocs]: https://azure.github.io/azure-sdk-for-java/springboot.html#azure-spring-boot
[package]: https://mvnrepository.com/artifact/com.azure.spring/azure-spring-boot-starter-keyvault-certificates
[sample]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-keyvault-certificates-server-side
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK#use-logback-logging-framework-in-a-spring-boot-application
[environment_checklist]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/ENVIRONMENT_CHECKLIST.md#ready-to-run-checklist

