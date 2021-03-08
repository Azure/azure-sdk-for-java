# Azure Remote Rendering client library for Java

Azure Remote Rendering (ARR) is a service that enables you to render high-quality, interactive 3D content in the cloud and stream it in real time to devices, such as the HoloLens 2.

This SDK offers functionality to convert assets to the format expected by the runtime, and also to manage
the lifetime of remote rendering sessions.

> NOTE: Once a session is running, a client application will connect to it using one of the "runtime SDKs".
> These SDKs are designed to best support the needs of an interactive application doing 3d rendering.
> They are available in [.NET][dotnet_api] and [C++][cpp_api].

[Source code][source_code] | [API reference documentation][api_reference_doc] | [Product Documentation][product_documentation]

## Getting started

### Prerequisites
- [Java Development Kit (JDK)][jdk_link], version 8 or later.
- [Azure Subscription][azure_subscription]
- [Azure Remote Rendering account][remote_rendering_account] to use this package.

### Install the package

**Note:** This version targets Azure Remote Rendering service API version v2021-01-01.

Add the following Maven dependency:

[//]: # ({x-version-update-start;com.azure:azure-mixedreality-remoterendering;dependency})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-mixedreality-remoterendering</artifactId>
    <version>1.0.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authenticate the client

Constructing a remote rendering client requires an authenticated account, and a remote rendering endpoint.
An account consists of its accountId and an account domain.
For an account created in the eastus region, the account domain will have the form "eastus.mixedreality.azure.com".
There are several different forms of authentication:

- Account Key authentication
  - Account keys enable you to get started quickly with using Azure Remote Rendering. But before you deploy your application
    to production, we recommend that you update your app to use Azure AD authentication.
- Azure Active Directory (AD) token authentication
  - If you're building an enterprise application and your company is using Azure AD as its identity system, you can use
    user-based Azure AD authentication in your app. You then grant access to your Azure Remote Rendering accounts by using
    your existing Azure AD security groups. You can also grant access directly to users in your organization.
  - Otherwise, we recommend that you obtain Azure AD tokens from a web service that supports your app. We recommend this
    method for production applications because it allows you to avoid embedding the credentials for access to Azure Spatial
    Anchors in your client application.

See [here][how_to_authenticate] for detailed instructions and information.

In all the following examples, the client is constructed with a `RemoteRenderingClientBuilder` object.
The parameters are always the same, except for the credential object, which is explained in each example.
The `remoteRenderingEndpoint` parameter is a URL that determines the region in which the service performs its work.
An example is `https://remoterendering.eastus2.mixedreality.azure.com`.

> NOTE: For converting assets, it is preferable to pick a region close to the storage containing the assets.

> NOTE: For rendering, it is strongly recommended that you pick the closest region to the devices using the service. 
> The time taken to communicate with the server impacts the quality of the experience.

#### Authenticating with account key authentication

Use the `AzureKeyCredential` object to use an account identifier and account key to authenticate:

<!-- embedme src/samples/java/com/azure/mixedreality/remoterendering/CreateClients.java#L35-L42 -->
```java
AzureKeyCredential credential = new AzureKeyCredential(environment.getAccountKey());

RemoteRenderingClient client = new RemoteRenderingClientBuilder()
    .accountId(environment.getAccountId())
    .accountDomain(environment.getAccountDomain())
    .endpoint(environment.getServiceEndpoint())
    .credential(credential)
    .buildClient();
```

#### Authenticating with an AAD client secret

Use the `ClientSecretCredential` object to perform client secret authentication.

<!-- embedme src/samples/java/com/azure/mixedreality/remoterendering/CreateClients.java#L53-L65 -->
```java
ClientSecretCredential credential = new ClientSecretCredentialBuilder()
    .tenantId(environment.getTenantId())
    .clientId(environment.getClientId())
    .clientSecret(environment.getClientSecret())
    .authorityHost("https://login.microsoftonline.com/" + environment.getTenantId())
    .build();

RemoteRenderingClient client = new RemoteRenderingClientBuilder()
    .accountId(environment.getAccountId())
    .accountDomain(environment.getAccountDomain())
    .endpoint(environment.getServiceEndpoint())
    .credential(credential)
    .buildClient();
```

#### Authenticating a user using device code authentication

Use the `DeviceCodeCredential` object to perform device code authentication.

<!-- embedme src/samples/java/com/azure/mixedreality/remoterendering/CreateClients.java#L76-L88 -->
```java
DeviceCodeCredential credential = new DeviceCodeCredentialBuilder()
    .challengeConsumer((DeviceCodeInfo deviceCodeInfo) -> { logger.info(deviceCodeInfo.getMessage()); })
    .clientId(environment.getClientId())
    .tenantId(environment.getTenantId())
    .authorityHost("https://login.microsoftonline.com/" + environment.getTenantId())
    .build();

RemoteRenderingClient client = new RemoteRenderingClientBuilder()
    .accountId(environment.getAccountId())
    .accountDomain(environment.getAccountDomain())
    .endpoint(environment.getServiceEndpoint())
    .credential(credential)
    .buildClient();
```

See [here](https://github.com/AzureAD/microsoft-authentication-library-for-dotnet/wiki/Device-Code-Flow) for more
information about using device code authentication flow.

#### Interactive authentication with DefaultAzureCredential

Use the `DefaultAzureCredential` object:

<!-- embedme src/samples/java/com/azure/mixedreality/remoterendering/CreateClients.java#L99-L106 -->
```java
DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

RemoteRenderingClient client = new RemoteRenderingClientBuilder()
    .accountId(environment.getAccountId())
    .accountDomain(environment.getAccountDomain())
    .endpoint(environment.getServiceEndpoint())
    .credential(credential)
    .buildClient();
```

#### Authenticating with a static access token

You can pass a Mixed Reality access token as an `AccessToken` previously retrieved from the
[Mixed Reality STS service][sts_sdk]
to be used with a Mixed Reality client library:

<!-- embedme src/samples/java/com/azure/mixedreality/remoterendering/CreateClients.java#L129-L140 -->
```java
// GetMixedRealityAccessTokenFromWebService is a hypothetical method that retrieves
// a Mixed Reality access token from a web service. The web service would use the
// MixedRealityStsClient and credentials to obtain an access token to be returned
// to the client.
AccessToken accessToken = getMixedRealityAccessTokenFromWebService();

RemoteRenderingClient client = new RemoteRenderingClientBuilder()
    .accountId(environment.getAccountId())
    .accountDomain(environment.getAccountDomain())
    .endpoint(environment.getServiceEndpoint())
    .accessToken(accessToken)
    .buildClient();
```

## Key concepts

### RemoteRenderingClient

The `RemoteRenderingClient` is the client library used to access the RemoteRenderingService.
It provides methods to create and manage asset conversions and rendering sessions.

## Examples

- [Convert a simple asset](#convert-a-simple-asset)
- [Convert a more complex asset](#convert-a-more-complex-asset)
- [Get the output when an asset conversion has finished](#get-the-output-when-an-asset-conversion-has-finished)
- [List conversions](#list-conversions)
- [Create a session](#create-a-session)
- [Extend the lease time of a session](#extend-the-lease-time-of-a-session)
- [List sessions](#list-sessions)
- [Stop a session](#stop-a-session)

### Convert a simple asset

We assume that a RemoteRenderingClient has been constructed as described in the [Authenticate the Client](#authenticate-the-client) section.
The following snippet describes how to request that "box.fbx", found at the root of the blob container at the given URL, gets converted.

<!-- embedme src/samples/java/com/azure/mixedreality/remoterendering/ConvertSimpleAsset.java#L31-L39 -->
```java
AssetConversionOptions conversionOptions = new AssetConversionOptions()
    .setInputStorageContainerUrl(getStorageURL())
    .setInputRelativeAssetPath("box.fbx")
    .setOutputStorageContainerUrl(getStorageURL());

// A randomly generated UUID is a good choice for a conversionId.
String conversionId = UUID.randomUUID().toString();

SyncPoller<AssetConversion, AssetConversion> conversionOperation = client.beginConversion(conversionId, conversionOptions);
```

The output files will be placed beside the input asset.

### Convert a more complex asset

Assets can reference other files, and blob containers can contain files belonging to many different assets.
In this example, we show how prefixes can be used to organize your blobs and how to convert an asset to take account of that organization.
Assume that the blob container at `inputStorageURL` contains many files, including "Bicycle/bicycle.gltf", "Bicycle/bicycle.bin" and "Bicycle/saddleTexture.jpg".
(So the prefix "Bicycle" is acting very like a folder.)
We want to convert the gltf so that it has access to the other files which share the prefix, without requiring the conversion service to access any other files.
To keep things tidy, we also want the output files to be written to a different storage container and given a common prefix: "ConvertedBicycle".
The code is as follows:

<!-- embedme src/samples/java/com/azure/mixedreality/remoterendering/ConvertMoreComplexAsset.java#L39-L48 -->
```java
AssetConversionOptions conversionOptions = new AssetConversionOptions()
    .setInputStorageContainerUrl(inputStorageURL)
    .setInputRelativeAssetPath("bicycle.gltf")
    .setInputBlobPrefix("Bicycle")
    .setOutputStorageContainerUrl(outputStorageURL)
    .setOutputBlobPrefix("ConvertedBicycle");

String conversionId = UUID.randomUUID().toString();

SyncPoller<AssetConversion, AssetConversion> conversionOperation = client.beginConversion(conversionId, conversionOptions);
```

> NOTE: when a prefix is given in the input options, then the input file parameter is assumed to be relative to that prefix.
> The same applies to the output file parameter in output options.

### Get the output when an asset conversion has finished

Converting an asset can take anywhere from seconds to hours.
This code uses an existing conversionOperation and polls regularly until the conversion has finished or failed.
The default polling period is 10 seconds.
Note that a conversionOperation can be constructed from the conversionId of an existing conversion and a client.

<!-- embedme src/samples/java/com/azure/mixedreality/remoterendering/ConvertMoreComplexAsset.java#L50-L57 -->
```java
AssetConversion conversion = conversionOperation.getFinalResult();
if (conversion.getStatus() == AssetConversionStatus.SUCCEEDED) {
    logger.info("Conversion succeeded: Output written to {}", conversion.getOutputAssetUrl());
} else if (conversion.getStatus() == AssetConversionStatus.FAILED) {
    logger.error("Conversion failed: {} {}", conversion.getError().getCode(), conversion.getError().getMessage());
} else {
    logger.error("Unexpected conversion status: {}", conversion.getStatus());
}
```

### List conversions

You can get information about your conversions using the `listConversions` method.
This method may return conversions which have yet to start, conversions which are running and conversions which have finished.
In this example, we just list the output URLs of successful conversions started in the last day.

<!-- embedme src/samples/java/com/azure/mixedreality/remoterendering/ListConversions.java#L32-L37 -->
```java
for (AssetConversion conversion : client.listConversions()) {
    if ((conversion.getStatus() == AssetConversionStatus.SUCCEEDED)
        && (conversion.getCreationTime().isAfter(OffsetDateTime.now().minusDays(1)))) {
        logger.info("Output Asset URL: {}", conversion.getOutputAssetUrl());
    }
}
```

### Create a rendering session

We assume that a RemoteRenderingClient has been constructed as described in the [Authenticate the Client](#authenticate-the-client) section.
The following snippet describes how to request that a new rendering session be started.

<!-- embedme src/samples/java/com/azure/mixedreality/remoterendering/CreateRenderingSession.java#L35-L42 -->
```java
BeginSessionOptions options = new BeginSessionOptions()
    .setMaxLeaseTime(Duration.ofMinutes(30))
    .setSize(RenderingSessionSize.STANDARD);

// A randomly generated GUID is a good choice for a sessionId.
String sessionId = UUID.randomUUID().toString();

SyncPoller<RenderingSession, RenderingSession> startSessionOperation = client.beginSession(sessionId, options);
```

### Extend the lease time of a session

If a session is approaching its maximum lease time, but you want to keep it alive, you will need to make a call to increase
its maximum lease time.
This example shows how to query the current properties and then extend the lease if it will expire soon.

> NOTE: The runtime SDKs also offer this functionality, and in many typical scenarios, you would use them to
> extend the session lease.

<!-- embedme src/samples/java/com/azure/mixedreality/remoterendering/QueryAndUpdateASession.java#L43-L50 -->
```java
RenderingSession currentSession = client.getSession(sessionId);

Duration sessionTimeAlive = Duration.between(OffsetDateTime.now(), currentSession.getCreationTime()).abs();
if (currentSession.getMaxLeaseTime().minus(sessionTimeAlive).toMinutes() < 2) {
    Duration newLeaseTime = currentSession.getMaxLeaseTime().plus(Duration.ofMinutes(30));
    UpdateSessionOptions longerLeaseOptions = new UpdateSessionOptions().maxLeaseTime(newLeaseTime);
    client.updateSession(sessionId, longerLeaseOptions);
}
```

### List rendering sessions

You can get information about your sessions using the `listSessions` method.
This method may return sessions which have yet to start and sessions which are ready.

<!-- embedme src/samples/java/com/azure/mixedreality/remoterendering/ListRenderingSessions.java#L47-L57 -->
```java
for (RenderingSession session : client.listSessions()) {
    if (session.getStatus() == RenderingSessionStatus.STARTING) {
        logger.info("Session {} is starting.");
    } else if (session.getStatus() == RenderingSessionStatus.READY) {
        logger.info("Session {} is ready at host {}", session.getId(), session.getHostname());
    } else if (session.getStatus() == RenderingSessionStatus.ERROR) {
        logger.error("Session {} encountered an error: {} {}", session.getId(), session.getError().getCode(), session.getError().getMessage());
    } else {
        logger.error("Session {} has unexpected status {}", session.getId(), session.getStatus());
    }
}
```

### Stop a session

The following code will stop a running session with given id.

<!-- embedme src/samples/java/com/azure/mixedreality/remoterendering/CreateRenderingSession.java#L57-L57 -->
```java
client.endSession(sessionId);
```

## Troubleshooting

For general troubleshooting advice concerning Azure Remote Rendering, see [the Troubleshoot page][troubleshoot] for remote rendering at docs.microsoft.com.

The client methods will throw exceptions if the request cannot be made.
However, in the case of both conversions and sessions, the requests can succeed but the requested operation may not be successful.
In this case, no exception will be thrown, but the returned objects can be inspected to understand what happened.

If the asset in a conversion is invalid, the conversion operation will return an AssetConversion object
with a Failed status and carrying a RemoteRenderingServiceError with details.
Once the conversion service is able to process the file, an &lt;assetName&gt;.result.json file will be written to the output container.
If the input asset is invalid, then that file will contain a more detailed description of the problem.

Similarly, sometimes when a session is requested, the session ends up in an error state.
The startSessionOperation method will return a RenderingSession object, but that object will have an Error status and carry a
RemoteRenderingServiceError with details.

## Next steps

- Read the [Product documentation](https://docs.microsoft.com/azure/remote-rendering/)
- Learn about the runtime SDKs:
  - .NET: https://docs.microsoft.com/dotnet/api/microsoft.azure.remoterendering
  - C++: https://docs.microsoft.com/cpp/api/remote-rendering/

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License
Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution.
For details, visit [https://cla.microsoft.com](https://cla.microsoft.com).

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the
PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this
once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/).
For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact
[opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

<!-- LINKS -->
[azure_subscription]: https://azure.microsoft.com/free
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning
[source_code]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/remoterendering/azure-mixedreality-remoterendering/src
[remote_rendering_account]: https://docs.microsoft.com/azure/remote-rendering/how-tos/create-an-account
[LogLevels]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/core/azure-core/src/main/java/com/azure/core/util/logging/ClientLogger.java
[product_documentation]: https://docs.microsoft.com/azure/remote-rendering/
[cpp_api]: https://docs.microsoft.com/cpp/api/remote-rendering/
[dotnet_api]: https://docs.microsoft.com/dotnet/api/microsoft.azure.remoterendering
[how_to_authenticate]: https://docs.microsoft.com/azure/remote-rendering/how-tos/authentication
[sts_sdk]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/mixedreality/azure-mixedreality-authentication
[troubleshoot]: https://docs.microsoft.com/azure/remote-rendering/resources/troubleshoot
[api_reference_doc]: https://docs.microsoft.com/rest/api/mixedreality/

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fmixedreality%2Fazure-mixedreality-remoterendering%2FREADME.png)
