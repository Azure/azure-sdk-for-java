# Microsoft Azure Key Vault SDK for Java

This is the Microsoft Azure Key Vault client library which allows for the consumption of Key Vault services. Azure Key Vault helps safeguard cryptographic keys and secrets used by cloud applications and services. By using Key Vault, you can encrypt keys and secrets (such as authentication keys, storage account keys, data encryption keys, .PFX files, and passwords) using keys protected by hardware security modules (HSMs). For added assurance, you can import or generate keys in HSMs. If you choose to do this, Microsoft processes your keys in FIPS 140-2 Level 2 validated HSMs (hardware and firmware).
Key Vault streamlines the key management process and enables you to maintain control of keys that access and encrypt your data. Developers can create keys for development and testing in minutes, and then seamlessly migrate them to production keys. Security administrators can grant (and revoke) permission to keys, as needed.
For more information refer to [What is Key Vault?](https://docs.microsoft.com/en-us/azure/key-vault/key-vault-whatis) or [Getting Started](https://docs.microsoft.com/en-us/azure/key-vault/key-vault-get-started).

## Sample code
You can find sample code that illustrates key vault usage scenarios [here](https://azure.microsoft.com/en-us/resources/samples/?sort=0&service=key-vault&platform=java).

## Download

To get the binaries of this library as distributed by Microsoft, ready for use within your project, you can use Maven.

```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure-keyvault</artifactId>
    <version>1.1-beta-1</version>
</dependency>
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure-keyvault-cryptography</artifactId>
    <version>1.1-beta-1</version>
</dependency>
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure-keyvault-extensions</artifactId>
    <version>1.1-beta-1</version>
</dependency>
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure-keyvault-core</artifactId>
    <version>1.1-beta-1</version>
</dependency>
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure-keyvault-webkey</artifactId>
    <version>1.1-beta-1</version>
</dependency>
```

## Pre-requisites
- A Java Developer Kit (JDK), v 1.7 or later
- Maven

## Building and Testing

Clone the repo, then run `mvn compile` from the root directory.

To run the recorded tests:
1. If you have not already, you need to install the [Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files](http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html) package.
2. run `mvn jetty:run` to start a jetty server. This starts a service that will block the terminal so you will likely want to open a second terminal to run the actual tests.
3. In your second terminal run `mvn test`.

## Contributing

If you would like to become an active contributor to this project please follow the instructions provided in [Microsoft Azure Projects Contribution Guidelines](http://azure.github.io/guidelines.html).

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

# More information
* [Azure Key Vault Java Documentation](https://docs.microsoft.com/en-us/java/api/overview/azure/keyvault)
* [What is Key Vault?](https://docs.microsoft.com/en-us/azure/key-vault/key-vault-whatis)
* [Get started with Azure Key Vault](https://docs.microsoft.com/en-us/azure/key-vault/key-vault-get-started)
* [Azure Key Vault General Documentation](https://docs.microsoft.com/en-us/azure/key-vault/)
* [Azure Key Vault REST API Reference](https://docs.microsoft.com/en-us/rest/api/keyvault/)
* [Azure Active Directory Documenation](https://docs.microsoft.com/en-us/azure/active-directory/)
