# Azure Key Vault Certificates Spring Boot starter 

## Server side SSL

### Using a managed identity

To use the starter for server side SSL, you will need to add the following to
your application.properties (if the application is using Spring Cloud Config 
Server for its configuration add it to the bootstrap.yml of the application)

```
azure.keyvault.uri=<the URI of the Azure KeyVault to use>
server.ssl.key-alias=<the name of the certificate in Azure KeyVault to use>
server.ssl.key-store-type=AzureKeyVault
```

Note: make sure the managed identity has access to the Azure KeyVault to access
keys, secrets and certificates.

Add then add the following Maven dependency to your POM file.

```xml
    <dependency>
        <groupId>com.azure</groupId>
        <artifactId>azure-spring-boot-starter-keyvault-certificates</artifactId>
    </dependency>
```

### Using a client ID and client secret

To use the starter for server side SSL, you will need to add the following to
your application.properties (if the application is using Spring Cloud Config 
Server for its configuration add it to the bootstrap.yml of the application)

```
azure.keyvault.uri=<the URI of the Azure KeyVault to use>
azure.keyvault.tenantId=<the ID of your Azure tenant>
azure.keyvault.clientId=<the client ID with access to Azure KeyVault>
azure.keyvault.clientSecret=<the client secret associated wit the client ID>
server.ssl.key-alias=<the name of the certificate in Azure KeyVault to use>
server.ssl.key-store-type=AzureKeyVault
```

Note: make sure the client ID has access to the Azure KeyVault to access
keys, secrets and certificates.

Add then add the following Maven dependency to your POM file.

```xml
    <dependency>
        <groupId>com.azure</groupId>
        <artifactId>azure-spring-boot-starter-keyvault-certificates</artifactId>
    </dependency>
```

## Client side SSL

### Using a managed identity

To use the starter for client side SSL, you will need to add the following to
your application.properties (if the application is using Spring Cloud Config 
Server for its configuration add it to the bootstrap.yml of the application)

```
azure.keyvault.uri=<the URI of the Azure KeyVault to use>
```
Note: make sure the managed identity has access to the Azure KeyVault to access
keys, secrets and certificates.

Add then add the following Maven dependency to your POM file.

```xml
    <dependency>
        <groupId>com.azure</groupId>
        <artifactId>azure-spring-boot-starter-keyvault-certificates</artifactId>
    </dependency>
```

If you are using RestTemplate use code similar to the example below.

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

### Using a client ID and client secret

To use the starter for client side SSL, you will need to add the following to
your application.properties (if the application is using Spring Cloud Config 
Server for its configuration add it to the bootstrap.yml of the application)

```
azure.keyvault.uri=<the URI of the Azure KeyVault to use>
azure.keyvault.tenantId=<the ID of your Azure tenant>
azure.keyvault.clientId=<the client ID with access to Azure KeyVault>
azure.keyvault.clientSecret=<the client secret associated wit the client ID>
```

Note: make sure the client ID has access to the Azure KeyVault to access
keys, secrets and certificates.

Add then add the following Maven dependency to your POM file.

```xml
    <dependency>
        <groupId>com.azure</groupId>
        <artifactId>azure-spring-boot-starter-keyvault-certificates</artifactId>
    </dependency>
```

Then if you are using RestTemplate use the code below as a starting
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

## Configuring Spring Cloud Gateway

To configure Spring Cloud Gateway for outbound SSL you will need
to add the following configuration:

```yaml
azure:
  keyvault:
    uri: <the URI of the Azure KeyVault to use>
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
validation altogether by using the configuration below. Note this is NOT 
recommended for production!

```yaml
spring:
  cloud:
    gateway:
      httpclient:
        ssl:
          useInsecureTrustManager: true
```

## Creating an Azure Key Vault

To create an Azure KeyVault use the command line below:

```shell
  export KEY_VAULT=mykeyvault
  export RESOURCE_GROUP=myresourcegroup
  az keyvault create --name ${KEY_VAULT} -g ${RESOURCE_GROUP}
```

## Create a self-signed certificate

To create a self-signed certificate use the command line below:

```shell
  export CERTIFICATE_ALIAS=self-signed
  az keyvault certificate create --vault-name ${KEY_VAULT} \
    -n ${CERTIFICATE_ALIAS} -p "$(az keyvault certificate get-default-policy)"
```

## Assign a managed identity (to an Azure Spring Cloud application)

To assign a managed identity use the command line below:

```shell
  export SPRING_CLOUD_APP=myspringcloudapp
  az spring-cloud app identity assign --name ${SPRING_CLOUD_APP}
  export MANAGED_IDENTITY=$(az spring-cloud app show \
    --name ${SPRING_CLOUD_APP} --query identity.principalId --output tsv)
```

## Grant a managed identity with access to Azure Key Vault

To grant access use the command line below:

```shell
  az keyvault set-policy --name ${KEY_VAULT} \
        --object-id ${MANAGED_IDENTITY} \
        --key-permisssions get list \
        --secret-permissions get list \
        --certificate-permissions get list
```

## Side-loading certificates

This starter allows you to side-load certificates by supplying them as part of
the application. 

To side-load add your certificates to the `src/main/resources/keyvault` folder.

Notes: 
1. The alias (certificate name) is constructed from the filename of the 
certificate (minus the extension). So if your filename is `mycert.x509` the
certificate will be added with the alias of `mycert`. 
2. Certificates coming from Azure KeyVault take precedence over 
side-loaded certificates.

## Testing the current version under development 

If you want to test the current version under development you will have to

1. Build and install the [Microsoft Azure JCA Provider]<!--(../../keyvault/azure-security-keyvault-jca/README.md)--> for KeyVault
1. Build and install this starter.

To build and install the starter use the following command line:

```
  mvn clean install -DskipTests=true
```


# Azure KeyVault Certificates client library for Java

# Getting started

# Key concepts

# Examples

# Troubleshooting

# Next steps

# Contributing