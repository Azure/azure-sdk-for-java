# Azure Key Vault JCA client library for Java

# Getting started

# Key concepts

The JCA Provider for Azure Key Vault is a JCA provider for certificates in 
Azure Key Vault. It is built on four principles:
 
1. Must be extremely thin to run within a JVM.
1. Must not introduce any library version conflicts with Java app code dependencies.
1. Must not introduce any class loader hierarchy conflicts with Java app code dependencies.
1. Must be ready for "never trust, always verify and credential-free" Zero Trust environments.

# Examples

## Server side SSL

If you are looking to integrate the JCA provider to create an SSLServerSocket
see the example below.

<!-- embedme src/samples/java/sample/ServerSSLSample.java#L21-L39 -->

```java
KeyVaultJcaProvider provider = new KeyVaultJcaProvider();
Security.addProvider(provider);

KeyStore ks = KeyStore.getInstance("AzureKeyVault");
KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
    System.getProperty("azure.keyvault.uri"),
    System.getProperty("azure.keyvault.tenant-id"),
    System.getProperty("azure.keyvault.client-id"),
    System.getProperty("azure.keyvault.client-secret"));
ks.load(parameter);

KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
kmf.init(ks, "".toCharArray());

SSLContext context = SSLContext.getInstance("TLS");
context.init(kmf.getKeyManagers(), null, null);

SSLServerSocketFactory factory = context.getServerSocketFactory();
SSLServerSocket serverSocket = (SSLServerSocket) factory.createServerSocket(8765);
```

Note if you want to use Azure Managed Identity, you should set the value
of `azure.keyvault.uri`, and the rest of the parameters would be `null`.

## Client side SSL

If you are looking to integrate the JCA provider for client side socket 
connections, see the Apache HTTP client example below.

<!-- embedme src/samples/java/sample/ClientSSLSample.java#L30-L74 -->

```java
KeyVaultJcaProvider provider = new KeyVaultJcaProvider();
Security.addProvider(provider);

KeyStore ks = KeyStore.getInstance("AzureKeyVault");
KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
        System.getProperty("azure.keyvault.uri"),
        System.getProperty("azure.keyvault.tenant-id"),
        System.getProperty("azure.keyvault.client-id"),
        System.getProperty("azure.keyvault.client-secret"));
ks.load(parameter);

SSLContext sslContext = SSLContexts
        .custom()
        .loadTrustMaterial(ks, new TrustSelfSignedStrategy())
        .build();

SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactoryBuilder
        .create()
        .setSslContext(sslContext)
        .setHostnameVerifier((hostname, session) -> {
            return true;
        })
        .build();

PoolingHttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder
        .create()
        .setSSLSocketFactory(sslSocketFactory)
        .build();

String result = null;

try (CloseableHttpClient client = HttpClients.custom().setConnectionManager(cm).build()) {
    HttpGet httpGet = new HttpGet("https://localhost:8766");
    HttpClientResponseHandler<String> responseHandler = (ClassicHttpResponse response) -> {
        int status = response.getCode();
        String result1 = "Not success";
        if (status == 204) {
            result1 = "Success";
        }
        return result1;
    };
    result = client.execute(httpGet, responseHandler);
} catch (IOException ioe) {
    ioe.printStackTrace();
}
```

Note if you want to use Azure managed identity, you should set the value
of `azure.keyvault.uri`, and the rest of the parameters would be `null`.

# Troubleshooting

# Next steps

## Spring Boot

For Spring Boot applications see our [Spring Boot starter](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-keyvault-certificates/README.md).

## Reference

1. [Java Cryptography Architecture (JCA) Reference Guide](https://docs.oracle.com/javase/8/docs/technotes/guides/security/crypto/CryptoSpec.html)

# Contributing

## Testing the version under development

If you want to test the current version under development you will have to
build and install it into your local Maven repository. To do so use the 
following command line:

```
  mvn clean install -DskipTests=true
```
