# Azure DeviceRegistry software update client library for Java

Software Update for Device Registry enables customers to publish updates for their IoT devices to Azure and manage the
update metadata associated with those devices. It uses the security and reliability of the Windows Update platform,
optimized for IoT devices.

Use this client library to:

- Import, retrieve, list, and delete software updates.
- List update providers, names, versions, and files.
- Monitor long-running import and delete operations.
- Retrieve and manage device classes.

Key links:

- [Source code][source_code]
- [Package (Maven)][package]
- [API reference documentation][api_reference]
- [Product documentation][product_documentation]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- An [Azure subscription][azure_subscription]
- A Software Update for Device Registry account and its endpoint hostname, such as `contoso.api.adu.microsoft.com`

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-iot-deviceregistry-softwareupdate;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-iot-deviceregistry-softwareupdate</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Create and authenticate a client

The clients authenticate with Microsoft Entra ID. The [Azure Identity][azure_identity] package provides credential
implementations such as `DefaultAzureCredential`.

Add `azure-identity` to your project to use `DefaultAzureCredential`:

[//]: # ({x-version-update-start;com.azure:azure-identity;dependency})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
    <version>1.18.5</version>
</dependency>
```
[//]: # ({x-version-update-end})

The endpoint passed to the client builder must be the account hostname without a protocol, for example,
`contoso.api.adu.microsoft.com` rather than `https://contoso.api.adu.microsoft.com`.

```java readme-sample-createClient-imports
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.iot.deviceregistry.softwareupdate.DeviceRegistrySoftwareUpdateClientBuilder;
import com.azure.iot.deviceregistry.softwareupdate.SoftwareUpdateClient;
```

```java readme-sample-createClient
String endpoint = System.getenv("DEVICE_REGISTRY_SOFTWARE_UPDATE_ENDPOINT");

SoftwareUpdateClient softwareUpdateClient = new DeviceRegistrySoftwareUpdateClientBuilder()
    .endpoint(endpoint)
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildSoftwareUpdateClient();
```

The same builder can create synchronous or asynchronous clients for software updates and device classes.

## Key concepts

### Software updates

`SoftwareUpdateClient` provides synchronous operations for importing and managing software updates. Updates are
identified by a provider, name, and version. `SoftwareUpdateAsyncClient` provides equivalent non-blocking operations.

Import and delete operations are long-running operations. Their `SyncPoller` or `PollerFlux` results can be used to
wait for completion and inspect operation status.

### Device classes

`DeviceClassesClient` provides synchronous operations for listing, retrieving, and deleting device classes.
`DeviceClassesAsyncClient` provides the equivalent asynchronous operations.

## Examples

### List imported updates

```java readme-sample-listUpdates
softwareUpdateClient.listUpdates().forEach(update -> {
    System.out.printf("%s/%s/%s%n", update.getUpdateId().getProvider(), update.getUpdateId().getName(),
        update.getUpdateId().getVersion());
});
```

### Service API versions

The client library targets the latest service API version by default.
The service client builder accepts an optional service API version parameter to specify which API version to communicate.

#### Select a service API version

You have the flexibility to explicitly select a supported service API version when initializing a service client via the service client builder.
This ensures that the client can communicate with services using the specified API version.

When selecting an API version, it is important to verify that there are no breaking changes compared to the latest API version.
If there are significant differences, API calls may fail due to incompatibility.

Always ensure that the chosen API version is fully supported and operational for your specific use case and that it aligns with the service's versioning policy.

## Troubleshooting

### Authentication and authorization

HTTP `401` responses indicate that the credential could not obtain a valid token. HTTP `403` responses indicate that
the authenticated identity does not have permission to perform the requested operation. Verify the credential
configuration and the role assignments for the Software Update for Device Registry account.

### Endpoint configuration

Pass only the account hostname to `DeviceRegistrySoftwareUpdateClientBuilder.endpoint`. Including `https://` causes
invalid request URLs because the generated client adds the HTTPS protocol.

### Update imports

Update imports require an import manifest URL and URLs for all referenced update files. Ensure that the service can read
each URL and that the supplied file sizes and hashes match the source content.

### Logging

This library uses SLF4J for logging. See the [Azure SDK for Java logging documentation][logging] for information about
configuring logs and enabling HTTP request and response diagnostics.

## Next steps

Review the [API reference documentation][api_reference] for all available client operations and model types.

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

<!-- LINKS -->
[api_reference]: https://azure.github.io/azure-sdk-for-java/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity
[azure_subscription]: https://azure.microsoft.com/free/
[jdk]: https://learn.microsoft.com/azure/developer/java/fundamentals/java-jdk-install
[logging]: https://learn.microsoft.com/azure/developer/java/sdk/logging-overview
[package]: https://central.sonatype.com/artifact/com.azure/azure-iot-deviceregistry-softwareupdate
[product_documentation]: https://learn.microsoft.com/azure/iot-hub-device-update/understand-device-update
[source_code]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/deviceregistry/azure-iot-deviceregistry-softwareupdate
