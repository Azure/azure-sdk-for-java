# Guide for migrating to `azure-identity` from `microsoft-azure-authentication-msi-token-provider`. 

This guide is intended to assist migration to `azure-identity` library. It will focus on side-by-side comparisons for similar operations between the two packages.

We assume that you are familiar with the `microsoft-azure-authentication-msi-token-provider` library. If not, please refer to the [README for azure-identity](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md) and [azure-identity samples](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md#examples) rather than this guide.

## Table of contents
 - [Migration benefits](#migration-benefits)
    - [Cross Service SDK improvements](#cross-service-sdk-improvements)
 - [Important changes](#important-changes)
     - [Group id, artifact id, and package names](#group-id-artifact-id-and-package-names)
     - [Authentication with managed identity](#authentication-with-managed-identity)     
         - [Authentication with system assigned managed identity](#authentication-with-system-assigned-managed-identity)
         - [Authentication with user assigned managed identity](#authentication-with-user-assigned-managed-identity)

## Migration benefits

As Azure has matured and been embraced by a more diverse group of developers, we have been focused on learning the patterns and practices to best support developer productivity and add value to our customers.

There were several areas of consistent feedback expressed across the Azure client library ecosystem. One of the most important is that the client libraries for different Azure services have not had a consistent approach to organization, naming, and API structure. Additionally, many developers have felt that the learning curve was difficult, and the APIs did not offer a good, approachable, and consistent onboarding story for those learning Azure or exploring a specific Azure service.

To improve the development experience across Azure services, including this library, a set of uniform [design guidelines](https://azure.github.io/azure-sdk/general_introduction.html) was created for all languages to drive a consistent experience with established API patterns for all services. A set of [Java-specific guidelines](https://azure.github.io/azure-sdk/java_introduction.html) was also introduced to ensure that Java clients have a natural and idiomatic feel that mirrors that of the Java ecosystem. The new `azure-identity` library follows these guidelines.

We believe that there is significant benefit to adopting the new `azure-identity` library, and the support for this library will be deprecated in the future.

### Cross Service SDK improvements

The modern Azure identity client library also provides the ability to share in some cross-service improvements made to the Azure development experience, such as

- A unified authentication approach between clients.
- A unified logging and diagnostics pipeline offering a common view of the activities across each of the client libraries.
- A unified asynchronous programming model using [Project Reactor][project-reactor].
- A unified method of creating clients via client builders to interact with Azure services.

## Important changes
The Azure Identity library provides [Azure Active Directory (AAD)](https://docs.microsoft.com/azure/active-directory/fundamentals/active-directory-whatis) token authentication support across the Azure SDK. It provides a set of TokenCredential implementations which support AAD token authentication for Azure SDK clients.
This includes [Managed identity authentication](https://docs.microsoft.com/azure/active-directory/managed-identities-azure-resources/overview) support for Azure Virtual Machines and Azure App Services.

### Group id, artifact id, and package names

Group ids, artifact ids, and package names for the modern Azure client libraries for Java have changed. They follow the [Java SDK naming guidelines][GuidelinesJavaDesign]. Each will have the group id `com.azure`, an artifact id following the pattern `azure-[area]-[service]`, and the root package name `com.azure.[area].[Service]`. The legacy clients have a group id `com.microsoft.azure` and their package names followed the pattern `com.microsoft. Azure.[service]`. This provides a quick and accessible means to help understand, at a glance, whether you are using modern or legacy clients.

In azure-identity, the modern client libraries have packages and namespaces that begin with `com.azure.identity`. The legacy library has package names starting with `com.microsoft.azure`.

### Authentication with managed identity
`microsoft-azure-authentication-msi-token-provider` allows authenticating using system and user assigned managed identity. It supports Azure App Services and Azure Virtual Machines.
You can use [azure-identity](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md) to do the same in the following way.


#### Authentication with system assigned managed identity

```java
/**
 * Authenticate with a System Assigned Managed identity.
 */
public void createManagedIdentityCredential() {
    ManagedIdentityCredential managedIdentityCredential = new ManagedIdentityCredentialBuilder()
    .build();

    // Azure SDK client builders accept the credential as a parameter
    SecretClient client = new SecretClientBuilder()
    .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
    .credential(managedIdentityCredential)
    .buildClient();
    }
```

#### Authentication with user assigned managed identity

```java
/**
 * Authenticate with a User Assigned Managed identity.
 */
public void createManagedIdentityCredential() {
    ManagedIdentityCredential managedIdentityCredential = new ManagedIdentityCredentialBuilder()
        .clientId("<USER ASSIGNED MANAGED IDENTITY CLIENT ID>") // only required for user assigned
        .build();

    // Azure SDK client builders accept the credential as a parameter
    SecretClient client = new SecretClientBuilder()
        .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
        .credential(managedIdentityCredential)
        .buildClient();
}
```


[Here](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md#managed-identity-support) is the complete list of Azure Services for which `azure-identity` supports managed identity authentication.
Please refer to [azure-identity docs](https://docs.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable) for further reading.

<!-- Links -->
[Guidelines]: https://azure.github.io/azure-sdk/general_introduction.html
[GuidelinesJava]: https://azure.github.io/azure-sdk/java_introduction.html
[GuidelinesJavaDesign]: https://azure.github.io/azure-sdk/java_introduction.html#namespaces
[project-reactor]: https://projectreactor.io/
[README-Samples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md#examples
[README]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%authorization%2Fmicrosoft-azure-authentication-msi-token-provider%2Fmigration-guide.png)
