# Azure Communication Service Common client library for Java

Azure Communication Common contains data structures commonly used for communicating with Azure Communication Services. 
It is intended to provide cross-cutting concerns, e.g. authentication. 

## Getting started

### Prerequisites

- An Azure account with an active subscription. [Create an account for free](https://azure.microsoft.com/free/?WT.mc_id=A261C142F).
- [Java Development Kit (JDK)](https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable) version 8 or above.
- [Apache Maven](https://maven.apache.org/download.cgi).
- A deployed Communication Services resource.

#### Include the BOM file

Please include the azure-sdk-bom to your project to take dependency on the General Availability (GA) version of the library. In the following snippet, replace the {bom_version_to_target} placeholder with the version number.
To learn more about the BOM, see the [AZURE SDK BOM README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/boms/azure-sdk-bom/README.md).

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.azure</groupId>
            <artifactId>azure-sdk-bom</artifactId>
            <version>{bom_version_to_target}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```
and then include the direct dependency in the dependencies section without the version tag.

```xml
<dependencies>
  <dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-communication-common</artifactId>
  </dependency>
</dependencies>
```

#### Include direct dependency

If you want to take dependency on a particular version of the library that is not present in the BOM,
add the direct dependency to your project as follows.

[//]: # ({x-version-update-start;com.azure:azure-communication-common;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-communication-common</artifactId>
    <version>2.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts

To work with Azure Communication Services, a resource access key is used for authentication.

Azure Communication Service supports HMAC authentication with resource access key. To
apply HMAC authentication, construct `CommunicationClientCredential` with the access key and instantiate
a `CommunicationIdentityClient` to manage users and tokens.

### CommunicationTokenCredential

The `CommunicationTokenCredential` object is used to authenticate a user with Communication Services, such as Chat or Calling. It optionally provides an auto-refresh mechanism to ensure a continuously stable authentication state during communications.

Depending on your scenario, you may want to initialize the `CommunicationTokenCredential` with:

- a static token (suitable for short-lived clients used to e.g. send one-off Chat messages) or
- a callback function that ensures a continuous authentication state (ideal e.g. for long Calling sessions).

The tokens supplied to the `CommunicationTokenCredential` either through the constructor or via the token refresher callback can be obtained using the Azure Communication Identity library.

## Examples

### Create a credential with a static token

For short-lived clients, refreshing the token upon expiry is not necessary and `CommunicationTokenCredential` may be instantiated with a static token.

```java
String token = System.getenv("COMMUNICATION_SERVICES_USER_TOKEN");
CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(token);
```

### Create a credential with proactive refreshing with a callback

Alternatively, for long-lived clients, you can create a `CommunicationTokenCredential` with a callback to renew tokens if expired.
Here we assume that we have a function `fetchTokenFromMyServerForUser` that makes a network request to retrieve a token string for a user.
It's necessary that the `fetchTokenFromMyServerForUser` function returns a valid token (with an expiration date set in the future) at all times.

Optionally, you can enable proactive token refreshing where a fresh token will be acquired as soon as the
previous token approaches expiry. Using this method, your requests are less likely to be blocked to acquire a fresh token:

```java
String token = System.getenv("COMMUNICATION_SERVICES_USER_TOKEN");
CommunicationTokenRefreshOptions tokenRefreshOptions = new CommunicationTokenRefreshOptions(fetchTokenFromMyServerForUser)
    .setRefreshProactively(true)
    .setInitialToken(token);
CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(tokenRefreshOptions);     
```

## Troubleshooting

In progress.

## Next steps

Check out other client libraries for Azure communication service

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a [Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

<!-- LINKS -->
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
