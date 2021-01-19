# Azure Key Vault Certificates Spring Boot starter client library for Java
Azure Key Vault Certificates Spring Boot Starter is Spring starter for [Azure Key Vault Certificates](https://docs.microsoft.com/rest/api/keyvault/about-keys--secrets-and-certificates#BKMK_WorkingWithSecrets), it allows you to securely manage and tightly control your certificates.

[Package (Maven)][package] | [API reference documentation][refdocs] | [Samples][sample]

## Getting started
### Prerequisites
- [Java Development Kit (JDK)][jdk_link] with version 8 or above
- [Azure Subscription][azure_subscription]
- [Maven](https://maven.apache.org/) 3.0 and above

### Include the package
[//]: # ({x-version-update-start;com.azure.spring:azure-spring-boot-starter-keyvault-certificates;current})
```xml
<dependency>
    <groupId>com.azure.spring</groupId>
    <artifactId>azure-spring-boot-starter-keyvault-certificates</artifactId>
    <version>3.2.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts
This starter is based on a JCA Provider for Azure Key Vault which is a JCA provider for certificates in 
Azure Key Vault. It is built on four principles:
 
1. Must be extremely thin to run within a JVM.
1. Must not introduce any library version conflicts with Java app code dependencies.
1. Must not introduce any class loader hierarchy conflicts with Java app code dependencies.
1. Must be ready for "never trust, always verify and credential-free" Zero Trust environments.

## Examples
### Server side SSL

#### Using a managed identity

To use the starter for server side SSL, you will need to add the following to
your `application.properties` (if the application is using Spring Cloud Config 
Server for its configuration add it to the `bootstrap.yml` of the application)

```
azure.keyvault.uri=<the URI of the Azure Key Vault to use>
server.ssl.key-alias=<the name of the certificate in Azure Key Vault to use>
server.ssl.key-store-type=AzureKeyVault
```

Note: make sure the managed identity has access to the Azure Key Vault to access
keys, secrets and certificates.

#### Using a client ID and client secret

To use the starter for server side SSL, you will need to add the following to
your `application.properties` (if the application is using Spring Cloud Config 
Server for its configuration add it to the `bootstrap.yml` of the application)

```
azure.keyvault.uri=<the URI of the Azure Key Vault to use>
azure.keyvault.tenant-id=<the ID of your Azure tenant>
azure.keyvault.client-id=<the client ID with access to Azure Key Vault>
azure.keyvault.client-secret=<the client secret associated wit the client ID>
server.ssl.key-alias=<the name of the certificate in Azure Key Vault to use>
server.ssl.key-store-type=AzureKeyVault
```

Note: make sure the client ID has access to the Azure Key Vault to access
keys, secrets and certificates.

### Client side SSL

#### Using a managed identity

To use the starter for client side SSL, you will need to add the following to
your `application.properties` (if the application is using Spring Cloud Config 
Server for its configuration add it to the `bootstrap.yml` of the application)

```
azure.keyvault.uri=<the URI of the Azure Key Vault to use>
```
Note: make sure the managed identity has access to the Azure Key Vault to access
keys, secrets and certificates.

If you are using `RestTemplate` use code similar to the example below.

```java
    @Bean
    public RestTemplate restTemplate() throws Exception {
        KeyStore ks = KeyStore.getInstance("AzureKeyVault");
        SSLContext sslContext = SSLContexts.custom()
            .loadTrustMaterial(ks, new TrustSelfSignedStrategy())
            .build();

        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

        CloseableHttpClient httpClient = HttpClients.custom()
            .setSSLSocketFactory(csf)
            .build();

        HttpComponentsClientHttpRequestFactory requestFactory =
                        new HttpComponentsClientHttpRequestFactory();

        requestFactory.setHttpClient(httpClient);
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        return restTemplate;
    }
```

#### Using a client ID and client secret

To use the starter for client side SSL, you will need to add the following to
your `application.properties` (if the application is using Spring Cloud Config 
Server for its configuration add it to the `bootstrap.yml` of the application)

```
azure.keyvault.uri=<the URI of the Azure Key Vault to use>
azure.keyvault.tenant-id=<the ID of your Azure tenant>
azure.keyvault.client-id=<the client ID with access to Azure Key Vault>
azure.keyvault.client-secret=<the client secret associated wit the client ID>
```

Note: make sure the client ID has access to the Azure Key Vault to access
keys, secrets and certificates.

Then if you are using `RestTemplate` use the code below as a starting
point:

```java
    @Bean
    public RestTemplate restTemplate() throws Exception {
        KeyStore ks = KeyStore.getInstance("AzureKeyVault");
        SSLContext sslContext = SSLContexts.custom()
            .loadTrustMaterial(ks, new TrustSelfSignedStrategy())
            .build();

        HostnameVerifier allowAll = (String hostName, SSLSession session) -> true;
        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, allowAll);

        CloseableHttpClient httpClient = HttpClients.custom()
            .setSSLSocketFactory(csf)
            .build();

        HttpComponentsClientHttpRequestFactory requestFactory =
                        new HttpComponentsClientHttpRequestFactory();

        requestFactory.setHttpClient(httpClient);
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        return restTemplate;
    }
```

### Configuring Spring Cloud Gateway

To configure Spring Cloud Gateway for outbound SSL you will need
to add the following configuration:

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

### Assign a managed identity (to an Azure Spring Cloud application)

To assign a managed identity use the command line below:

```shell
  export SPRING_CLOUD_APP=myspringcloudapp
  az spring-cloud app identity assign --name ${SPRING_CLOUD_APP}
  export MANAGED_IDENTITY=$(az spring-cloud app show \
    --name ${SPRING_CLOUD_APP} --query identity.principalId --output tsv)
```

### Grant a managed identity with access to Azure Key Vault

To grant access use the command line below:

```shell
  az keyvault set-policy --name ${KEY_VAULT} \
        --object-id ${MANAGED_IDENTITY} \
        --key-permisssions get list \
        --secret-permissions get list \
        --certificate-permissions get list
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

### Testing the current version under development 

If you want to test the current version under development you will have to

1. Build and install the [Azure Key Vault JCA client library for Java](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/keyvault/azure-security-keyvault-jca/README.md)
1. Build and install this starter.

To build and install the starter use the following command line:

```
  mvn clean install -DskipTests=true
```

## Troubleshooting
### Enable client logging
Azure SDKs for Java offers a consistent logging story to help aid in troubleshooting application errors and expedite their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

### Enable Spring logging
Spring allow all the supported logging systems to set logger levels set in the Spring Environment (for example, in application.properties) by using `logging.level.<logger-name>=<level>` where level is one of TRACE, DEBUG, INFO, WARN, ERROR, FATAL, or OFF. The root logger can be configured by using logging.level.root.

The following example shows potential logging settings in `application.properties`:

```properties
logging.level.root=WARN
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate=ERROR
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
[sample]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-keyvault-certificates
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK#use-logback-logging-framework-in-a-spring-boot-application
[azure_subscription]: https://azure.microsoft.com/free
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
