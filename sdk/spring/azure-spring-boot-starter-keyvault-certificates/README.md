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

1. Log into <https://portal.azure.com>.

1. Click `Create a resource`.

1. Input `Key Vault`.

1. Click `Key Vault`
    ![Find Azure Key Vault Resource 01](resource/creating-an-azure-key-vault-01.png)

    ![Find Azure Key Vault Resource 02](resource/creating-an-azure-key-vault-02.png)

1. Click **Create**.

    ![Create new Key Vault](resource/creating-an-azure-key-vault-03.png)

1. On the **Create key vault** page, input `Subscription`, `Resource group`, `Key vault name` and `Pricing tier`, then click `Review + Create`.

    ![Specify the options](resource/specify-the-options.png)

    ![Create Key Vault resource](resource/create-key-vault-resource.png)

1. When complete, click `Go to resource`.

    ![Go to resource](resource/go-to-resource.png)

1. When the page for your app registration appears, copy your **Vault URI**;

    ![Save vault uri](resource/save-vault-uri.png)

1. Click **Certificates** in the left navigation pane.  Then click **Generate/Import**.

    ![Create Certificates](resource/create-certificates.png)

1. Enter a **Certificates name**, and enter a **Subject** like `CN=mydomain.com`. then click **create**.

    ![Specify Certificates Info](resource/specify-certificates-info.png)

1. After the certificate is successfully created, it takes a while for the status to become `Enabled`. You can click **refresh** to check current status.

    ![Check Certificates status](resource/check-certificates-status.png)

## Key concepts
This starter allows you to securely manage and tightly control your certificates by using Azure Key Vault or side-load certificates by supplying them as part of the application.

## Examples
### Server side SSL

#### Using a client ID and client secret

1. Click **Show portal menu**

2. Click **Azure Active Directory**.

    ![Select Azure Active Directory](resource/select-azure-active-directory.png)

1. From the portal menu, Click **App registrations**,

1. Click **New registration**.

    ![New registration](resource/new-registration.png)

1. Specify your application, and then Click **Register**.

    ![Specify application](resource/specify-application.png)

1. When the page for your app registration appears, copy your **Application ID** and the **Tenant ID**;

    ![Get info for app](resource/get-info-for-app.png)

1. Click **Certificates & secrets** in the left navigation pane.  Then click **New client secret**.

1. Add a **Description** and click duration in the **Expires** list.  Click **Add**. The value for the key will be automatically filled in.
   
    ![Create secrets](resource/create-secrets.png)

1. Copy and save the value of the client secret. (You will not be able to retrieve this value later.)

    ![Copy secrets](resource/copy-secrets.png)

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

1. Type your key vault name in **Search resources, services, and docs** and click your key vault created before.

    ![Back to key vault](resource/back-to-key-vault.png)

1. Click **Access policies** in the left navigation pane. Then click **Add Access Policy**.

    ![Add Access Policy](resource/add-access-policy.png)

1. Select **Key, Secret, &Certificate Management** as **Configure for template(optional)**. Permissions will be added automatically. 

    ![Select configure](resource/select-configure.png)

1. Click **None selected** and choose application created before, click **Select**, then click **Add**.

    ![Choose application](resource/choose-application.png)

1. Click **Save**.

    ![Save Access Policy](resource/save-access-policy.png)

#### Using a managed identity

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
    KeyStore trustStore = KeyStore.getInstance("AzureKeyVault");
    KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
        System.getProperty("azure.keyvault.uri"),
        System.getProperty("azure.keyvault.tenant-id"),
        System.getProperty("azure.keyvault.client-id"),
        System.getProperty("azure.keyvault.client-secret"));
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

<!-- embedme ../azure-spring-boot/src/samples/java/com/azure/spring/keyvault/KeyVaultJcaManagedIdentitySample.java#L18-L36 -->
```java
@Bean
public RestTemplate restTemplateCreatedByManagedIdentity() throws Exception {
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
    KeyStore azuerKeyVaultKeyStore = KeyStore.getInstance("AzureKeyVault");
    KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
        System.getProperty("azure.keyvault.uri"),
        System.getProperty("azure.keyvault.tenant-id"),
        System.getProperty("azure.keyvault.client-id"),
        System.getProperty("azure.keyvault.client-secret"));
    azuerKeyVaultKeyStore.load(parameter);
    SSLContext sslContext = SSLContexts.custom()
                                       .loadTrustMaterial(azuerKeyVaultKeyStore, null)
                                       .loadKeyMaterial(azuerKeyVaultKeyStore, "".toCharArray(), new ClientPrivateKeyStrategy())
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

### Side-loading certificates

This starter allows you to side-load certificates by supplying them as part of
the application. 

To side-load add your certificates to the `src/main/resources/keyvault` folder.

Notes: 
1. The alias (certificate name) is constructed from the filename of the 
certificate (minus the extension). So if your filename is `mycert.x509` the
certificate will be added with the alias of `mycert`. 
2. Certificates coming from Azure Key Vault take precedence over 
side-loaded certificates.


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

For more information about setting logging in spring, please refer to the [official doc](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-logging).

## Next steps
The following section provide a sample project illustrating how to use the starter.
### More sample code
- [Key Vault Certificates][sample]

## Contributing
This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [instructions here](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/CONTRIBUTING.md) to build from source or contribute.

<!-- LINKS -->
[refdocs]: https://azure.github.io/azure-sdk-for-java/springboot.html#azure-spring-boot
[package]: https://mvnrepository.com/artifact/com.azure.spring/azure-spring-boot-starter-keyvault-certificates
[sample]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-keyvault-certificates-server-side
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK#use-logback-logging-framework-in-a-spring-boot-application
[environment_checklist]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/ENVIRONMENT_CHECKLIST.md#ready-to-run-checklist

