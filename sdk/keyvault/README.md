[![Build Status](https://travis-ci.org/Azure/azure-keyvault-java.svg?branch=dev)](https://travis-ci.org/Azure/azure-keyvault-java)

# Microsoft Azure Key Vault SDK for Java

This is the Microsoft Azure Key Vault client library which allows for the consumption of Key Vault services. Azure Key Vault helps safeguard cryptographic keys and secrets used by cloud applications and services. By using Key Vault, you can encrypt keys and secrets (such as authentication keys, storage account keys, data encryption keys, .PFX files, and passwords) using keys protected by hardware security modules (HSMs). For added assurance, you can import or generate keys in HSMs. If you choose to do this, Microsoft processes your keys in FIPS 140-2 Level 2 validated HSMs (hardware and firmware).
Key Vault streamlines the key management process and enables you to maintain control of keys that access and encrypt your data. Developers can create keys for development and testing in minutes, and then seamlessly migrate them to production keys. Security administrators can grant (and revoke) permission to keys, as needed.
For more information refer to [What is Key Vault?](https://docs.microsoft.com/azure/key-vault/key-vault-whatis) or [Getting Started](https://docs.microsoft.com/azure/key-vault/key-vault-get-started).

Documentation for this SDK can be found at [Azure Key Vault Java Documentation](https://docs.microsoft.com/java/api/overview/azure/keyvault)

## Getting started

To get started with a specific library, see the **README.md** file located in the library's project folder. You can find service libraries in the `/sdk/keyvault/azure-security-keyvault-<subcomponent>` directory.
- [Azure Keyvault Keys README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-keys/README.md)
- [Azure Keyvault Certificates README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-certificates/README.md)
- [Azure Keyvault Secrets README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-secrets/README.md)

## Sample code
You can find sample code that illustrates key vault usage scenarios [here](https://azure.microsoft.com/resources/samples/?sort=0&service=key-vault&platform=java).

<table>
    <tr>
        <th>Category</th>
        <th>Samples</th>
    </tr>
    <tr>
        <td>Authentication</td>
        <td>
            <ul>
                <li>
                    <a href="https://github.com/Azure-Samples/key-vault-java-authentication">Authenticating with a service principal and a self-signed certificate</a>
                </li>
                <li>
                    <a href="https://github.com/Azure-Samples/key-vault-java-authentication">Authenticating with ADAL through a callback</a>
                </li>
                <li>
                     <a href="https://github.com/Azure-Samples/key-vault-java-certificate-authentication/">Authenticating with a .pfx file</a>
                </li>
            </ul>
        </td>
    </tr>
    <tr>
        <td>Vault Management</td>
        <td>
            <ul>
                <li>
                    <a href="https://github.com/Azure-Samples/key-vault-java-certificate-authentication/">Creating a vault</a>
                </li>
                <li>
                    <a href="https://github.com/Azure-Samples/key-vault-java-network-acl/">Creating a vault with access restrictions based on IP and Azure Virtual Networks</a>
                </li>
            </ul>
        </td>
    </tr>
    <tr>
        <td>Secret Management</td>
        <td>
            <ul>
                <li>
                    <a href="https://github.com/Azure-Samples/key-vault-java-certificate-authentication
">Putting keys and secrets into a vault</a>
                </li>
                <li>
                    <a href="https://github.com/Azure-Samples/key-vault-java-certificate-authentication/">Signing</a>
                </li>
                <li>
                    <a href="https://github.com/Azure-Samples/key-vault-java-certificate-authentication/">Verification of signature with both Java Security and Azure Key Vault REST</a>
                </li>
                <li>
                    <a href="https://github.com/Azure-Samples/key-vault-java-certificate-authentication/">Injecting a .pfx file into a VM at deployment using a template</a>
                </li>
                <li>
                    <a href="https://github.com/Azure-Samples/key-vault-java-recovery/">Using the soft delete and backup restore features</a>
                </li>
                <li>
                    <a href="https://github.com/Azure-Samples/key-vault-java-recovery/">Managing storage accounts</a>
                </li>
            </ul>
        </td>
    </tr>
</table>

For more information on using Java with Azure, see [here](https://azure.microsoft.com/develop/java/)

## Download

To get the binaries of this library as distributed by Microsoft, ready for use within your project, you can use Maven. Add the following fragment to you POM's dependencies.

[//]: # ({x-version-update-start;com.microsoft.azure:azure-keyvault-complete;current})
```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure-keyvault-complete</artifactId>
    <version>1.2.3</version>
    <type>pom</type>
</dependency>
```
[//]: # ({x-version-update-end})

## Pre-requisites
- A Java Developer Kit (JDK), v 1.7 or later
- Maven

## Building and Testing

Clone the repo, then run `mvn compile` from the root directory.

To run the recorded tests:
1. If you have not already, you need to install the [Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files](https://www.oracle.com/java/technologies/javase-jce8-downloads.html) package.
2. run `mvn jetty:run` to start a jetty server. This starts a service that will block the terminal so you will likely want to open a second terminal to run the actual tests.
3. In your second terminal run `mvn test`.

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

# Previous Versions

| Version | Comments |
| :-------: | :--------- |
| [1.1.2](https://github.com/Azure/azure-keyvault-java/tree/1.1.2)   | Version 1.1.2 release |
| [1.1.1](https://github.com/Azure/azure-keyvault-java/tree/1.1.1)   | Version 1.1.1 release |
| [1.1](https://github.com/Azure/azure-keyvault-java/tree/1.1)   | Version 1.1 release |
| [1.1-beta-1](https://github.com/Azure/azure-keyvault-java/tree/1.1-beta-1)   | Version 1.1.0 **beta** release |
| [1.1-alpha-2](https://github.com/Azure/azure-keyvault-java/tree/v1.1-alpha-2)   | Version 1.1.0 **alpha** release |
| [1.0.0](https://github.com/Azure/azure-keyvault-java/tree/v1.0.0)   | Version 1.0.0 release |

# More information
* [Azure Key Vault Java Documentation](https://docs.microsoft.com/java/api/overview/azure/keyvault)
* [What is Key Vault?](https://docs.microsoft.com/azure/key-vault/key-vault-whatis)
* [Get started with Azure Key Vault](https://docs.microsoft.com/azure/key-vault/key-vault-get-started)
* [Azure Key Vault General Documentation](https://docs.microsoft.com/azure/key-vault/)
* [Azure Key Vault REST API Reference](https://docs.microsoft.com/rest/api/keyvault/)
* [Azure Active Directory Documenation](https://docs.microsoft.com/azure/active-directory/)


![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fkeyvault%2FREADME.png)
