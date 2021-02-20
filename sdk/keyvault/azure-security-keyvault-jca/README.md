# Azure Key Vault JCA client library for Java
The JCA Provider for Azure Key Vault is a Java Cryptography Architecture provider for certificates in
Azure Key Vault. It is built on four principles:

1. Must be extremely thin to run within a JVM.
2. Must not introduce any library version conflicts with Java app code dependencies.
3. Must not introduce any class loader hierarchy conflicts with Java app code dependencies.
4. Must be ready for "never trust, always verify and credential-free" Zero Trust environments.

[Source code][source_code] | [API reference documentation][api_documentation] | [Product documentation][azkeyvault_docs] | [Samples][certificates_samples]

## Getting started
### Adding the package to your project
Maven dependency for the Azure Key Vault JCA client library. Add it to your project's POM file.

[//]: # ({x-version-update-start;com.azure:azure-security-keyvault-jca;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-security-keyvault-jca</artifactId>
    <version>1.0.0-beta.3</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Prerequisites
- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
- [Azure Subscription][azure_subscription]
- An existing [Azure Key Vault][azure_keyvault]. If you need to create a Key Vault, you can use the [Azure Cloud Shell][azure_cloud_shell] to create one with this Azure CLI command. Replace `<your-resource-group-name>` and `<your-key-vault-name>` with your own, unique names:

    ```Bash
    az keyvault create --resource-group <your-resource-group-name> --name <your-key-vault-name>
    ```

## Key concepts

## Examples
### Server side SSL
If you are looking to integrate the JCA provider to create an SSLServerSocket see the example below.

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

Note if you want to use Azure Managed Identity, you should set the value of `azure.keyvault.uri`, and the rest of the parameters would be `null`.

### Client side SSL
If you are looking to integrate the JCA provider for client side socket connections, see the Apache HTTP client example below.

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
    .setHostnameVerifier((hostname, session) -> true)
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

Note if you want to use Azure managed identity, you should set the value of `azure.keyvault.uri`, and the rest of the parameters would be `null`.

## Troubleshooting
### General
Azure Key Vault JCA clients raise exceptions. For example, if you try to check a client's identity with a certificate chain that does not include a trusted certificate, a `CertificateException` will be thrown. In the following snippet, the error is handled gracefully by catching the exception and displaying additional information about the error.

```java
try {
    KeyVaultJcaProvider provider = new KeyVaultJcaProvider();
    Security.addProvider(provider);
    ...
    // Start SSL server socket
    ...
} catch (CertificateException e) {
    System.out.println(e.getMessage());
}
```

## Next steps
### Spring Boot
For Spring Boot applications see our [Spring Boot starter][spring_boot_starter].

### References
1. [Java Cryptography Architecture (JCA) Reference Guide][jca_reference_guide]

### Additional documentation
For more extensive documentation on Azure Key Vault, see the [API reference documentation][azkeyvault_rest].

## Contributing
This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][microsoft_code_of_conduct]. For more information see the Code of Conduct FAQ or contact <opencode@microsoft.com> with any additional questions or comments.

<!-- LINKS -->
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/keyvault/azure-security-keyvault-jca/src
[api_documentation]: https://azure.github.io/azure-sdk-for-java
[azkeyvault_docs]: https://docs.microsoft.com/azure/key-vault/
[jca_samples]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/keyvault/azure-security-keyvault-jca/src/samples/java/com/azure/security/keyvault/jca
[azure_subscription]: https://azure.microsoft.com/
[azure_keyvault]: https://docs.microsoft.com/azure/key-vault/keys/quick-create-portal
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[azure_cloud_shell]: https://shell.azure.com/bash
[spring_boot_starter]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/spring/azure-spring-boot-starter-keyvault-certificates/README.md
[jca_reference_guide]: https://docs.oracle.com/javase/8/docs/technotes/guides/security/crypto/CryptoSpec.html
[microsoft_code_of_conduct]: https://opensource.microsoft.com/codeofconduct/

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fkeyvault%2Fazure-security-keyvault-jca%2FREADME.png)
