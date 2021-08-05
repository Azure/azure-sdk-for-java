# IoT Models Repository Samples

The Azure IoT Models Repository enables builders to manage and share digital twin models for global consumption. The models are [JSON-LD][json_ld_reference] documents defined using the Digital Twins Definition Language ([DTDL][dtdlv2_reference]).

For more info about the Azure IoT Models Repository checkout the [docs][modelsrepository_msdocs].

## Introduction

You can explore the models repository APIs with the client library using the samples project.

The samples project demonstrates the following:

- Instantiate the client
- Get models and their dependencies from either a remote endpoint or local repository.

## Initializing the models repository client

```java
// When no URI is provided for instantiation, the Azure IoT Models Repository global endpoint
// https://devicemodels.azure.com/ is used and the model dependency resolution
// configuration is set to TRY_FROM_EXPANDED.
ModelsRepositoryAsyncClient asyncClient = new ModelsRepositoryClientBuilder()
    .buildAsyncClient();

ModelsRepositoryClient syncClient = new ModelsRepositoryClientBuilder()
    .buildClient();

System.out.println("Initialized the async client pointing to the global endpoint" + asyncClient.getRepositoryEndpoint());
System.out.println("Initialized the sync client pointing to the global endpoint" + syncClient.getRepositoryEndpoint());
```

```java 
// This form shows specifying a custom URI for the models repository with default client options.
// The default client options will enable model dependency resolution.
ModelsRepositoryAsyncClient asyncClient = new ModelsRepositoryClientBuilder()
    .repositoryEndpoint("https://contoso.com/models")
    .buildAsyncClient();

ModelsRepositoryClient syncClient = new ModelsRepositoryClientBuilder()
    .repositoryEndpoint("https://contoso.com/models")
    .buildClient();

System.out.println("Initialized the async client pointing to the custom endpoint" + asyncClient.getRepositoryEndpoint);
System.out.println("Initialized the sync client pointing to the custom endpoint" + syncClient.getRepositoryEndpoint);
```

```java
// The client will also work with a local file-system URI. This example shows initialization
// with a local URI and disabling model dependency resolution.
ModelsRepositoryAsyncClient asyncClient = new ModelsRepositoryClientBuilder()
    .repositoryEndpoint(CLIENT_SAMPLES_DIRECTORY_PATH)
    .modelDependencyResolution(ModelDependencyResolution.DISABLED)
    .buildAsyncClient();

ModelsRepositoryClient syncClient = new ModelsRepositoryClientBuilder()
    .repositoryEndpoint(CLIENT_SAMPLES_DIRECTORY_PATH)
    .modelDependencyResolution(ModelDependencyResolution.DISABLED)
    .buildClient();

System.out.println("Initialized the async client pointing to the local file-system: " + asyncClient.getRepositoryEndpoint);
System.out.println("Initialized the sync client pointing to the local file-system: " + syncClient.getRepositoryEndpoint);
```

## Sync vs Async clients

Azure Models Repository SDK for java has two sets of APIs available for every operation, sync APIs and async APIs.

You can use `ModelsRepositoryClientBuilder` to build either a sync client: `buildClient()` or an async client: `buildAsyncClient()`.

While using the sync client, the running thread will be blocked by the SDK for the duration of the HTTP request/response.
The async client is implemented using [Reactor](https://projectreactor.io/docs/core/release/reference/). The rest of this document assumes the reader has basic understanding of Reactor and how to interact with async API response types.

### Override options

If you need to override pipeline behavior, such as provide your own `HttpClient` instance, you can do that via `clientOptions` in the `ModelsRepositoryClientBuilder`.
It provides an opportunity to override default behavior including:

- Specifying API version
- Overriding [HttpPipeline](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/http/HttpPipeline.java).
- Enabling [HttpLogOptions](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/http/policy/HttpLogOptions.java).
- Controlling [retry strategy](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/http/policy/RetryPolicy.java).

## Publish models

Publishing models to the models repository requires [exercising][modelsrepository_publish_msdocs] common GitHub workflows.

## Get models

After publishing, your model(s) will be available for consumption from the global repository endpoint. The following snippet shows how to retrieve the corresponding JSON-LD content.

```java
// Global endpoint client
// Global endpoint client
ModelsRepositoryAsyncClient asyncClient = new ModelsRepositoryClientBuilder()
    .buildAsyncClient();

// The output of getModels will include at least the definition for the target dtmi.
// If the model dependency resolution configuration is not disabled, then models in which the
// target dtmi depends on will also be included in the returned Map<String, String>.
String targetDtmi = "dtmi:com:example:TemperatureController;1";

// In this case the above dtmi has 2 model dependencies.
// dtmi:com:example:Thermostat;1 and dtmi:azure:DeviceManagement:DeviceInformation;1
asyncClient.getModels(targetDtmi)
    .doOnSuccess(aVoid -> System.out.println("Fetched the model and dependencies for: " + targetDtmi))
    .subscribe(res -> System.out.println(String.format("%s resolved in %s interfaces.", targetDtmi, res.size())));
```

GitHub pull-request workflows are a core aspect of the IoT Models Repository service. To submit models, the user is expected to fork and clone the global [models repository project][modelsrepository_github_repo] then iterate against the local copy. Changes would then be pushed to the fork (ideally in a new branch) and a PR created against the global repository.

To support testing local changes using this workflow and similar use cases, the client supports initialization with a local file-system URI. You can use this for example, to test and ensure newly added models to the locally cloned models repository are in their proper locations.

```java
// Local sample repository client
ModelsRepositoryAsyncClient asyncClient = new ModelsRepositoryClientBuilder()
    .repositoryEndpoint(CLIENT_SAMPLES_DIRECTORY_PATH)
    .buildAsyncClient();

// The output of getModels will include at least the definition for the target dtmi.
// If the model dependency resolution configuration is not disabled, then models in which the
// target dtmi depends on will also be included in the returned Map<String, String>.
String targetDtmi = "dtmi:com:example:TemperatureController;1";

// In this case the above dtmi has 2 model dependencies.
// dtmi:com:example:Thermostat;1 and dtmi:azure:DeviceManagement:DeviceInformation;1
asyncClient.getModels(targetDtmi)
    .doOnSuccess(aVoid -> System.out.println("Fetched the model and dependencies for: " + targetDtmi))
    .subscribe(res -> System.out.println(String.format("%s resolved in %s interfaces.", targetDtmi, res.size())));
```

You are also able to get definitions for multiple root models at a time by leveraging
the `getModels` overload that supports an `Iterable`.

```java
// Global endpoint client
ModelsRepositoryAsyncClient asyncClient = new ModelsRepositoryClientBuilder()
    .buildAsyncClient();

// When given an Iterable of dtmis, the output of getModels() will include at
// least the definitions of each dtmi enumerated in the Iterable.
// If the model dependency resolution configuration is not disabled, then models in which each
// enumerated dtmi depends on will also be included in the returned Map<String, String>.
Iterable<String> dtmis = Arrays.asList("dtmi:com:example:TemperatureController;1", "dtmi:com:example:azuresphere:sampledevice;1");

// In this case the dtmi "dtmi:com:example:TemperatureController;1" has 2 model dependencies
// and the dtmi "dtmi:com:example:azuresphere:sampledevice;1" has no additional dependencies.
// The returned Map<String, String> will include 4 models.
asyncClient.getModels(dtmis)
    .doOnSuccess(aVoid -> System.out.println("Fetched the models and dependencies for: " + String.join(", ", dtmis)))
    .subscribe(res -> System.out.println(String.format("Dtmis %s resolved in %s interfaces.", String.join(", ", dtmis), res.size())));
```

## DtmiConventions utility functions

The IoT Models Repository applies a set of conventions for organizing digital twin models. This package exposes a class
called `DtmiConventions` which exposes utility functions supporting these conventions. These same functions are used throughout the client.

`isValidDtmi` will ensure that the provided DigitalTwins Model Id (DTMI) has the correct format according to [DTDL specifications][dtdlv2_reference].
```java
// This snippet shows how to validate a given DTMI string is well-formed.

// Returns True
String validDtmi = "dtmi:com:example:Thermostat;1";
System.out.println(String.format("Dtmi %s is a valid dtmi: %s", validDtmi, DtmiConventions.isValidDtmi(validDtmi)));

// Returns False
String invalidDtmi = "dtmi:com:example:Thermostat";
System.out.println(String.format("Dtmi %s is a valid dtmi: %s", invalidDtmi, DtmiConventions.isValidDtmi(invalidDtmi)));
```

`getModelUri` allows for constructing the path in which the provided DTMI is supposed to be located at based on a repository URI.
For instance: If the repository URI is `https://contoso.com/models/` and the DTMI (DigitalTwins Model Id) is `dtmi:com:example:Thermostat;1` then the path in which the file should exist will be `https://constoso.com/models/dtmi/com/example/thermostat-1.json`.

Same pattern is expected while working with the local file system.
For instance: If the repository URI is `file:///path/to/repository` and the DTMI (DigitalTwins Model Id) is `dtmi:com:example:Thermostat;1` then the path in which the file should exist will be `file:///path/to/repository/dtmi/com/example/thermostat-1.json`.

If the `boolean` parameter to the `getModelUri` method (`expanded`) is `true`, the result of the resolved path will have `.expanded.json` file extension instead of `.json`. This will allow for the `ModelsRepositoryClient` to only load the pre-computed dependency tree instead of the entire dependency tree. If the `.expanded.json` form of the model payload cannot be found, the client will load the full dependency tree as a fall back mechanism. 

```java
// This snippet shows obtaining a fully qualified path to a model file.

// Local repository example:
URI localRepositoryUri = new URI("file:///path/to/repository");
String fullyQualifiedModelUri = DtmiConventions.getModelUri("dtmi:com:example:Thermostat;1", localRepositoryUri, false).toString();

// Prints: file:///path/to/repository/dtmi/com/example/thermostat-1.json
System.out.println(fullyQualifiedModelUri);

// Remote repository example with expanded enabled
URI remoteRepositoryUri = new URI("https://contoso.com/models/");
fullyQualifiedModelUri = DtmiConventions.getModelUri("dtmi:com:example:Thermostat;1", remoteRepositoryUri, true).toString();

// Prints: https://constoso.com/models/dtmi/com/example/thermostat-1.expanded.json
System.out.println(fullyQualifiedModelUri);
```

<!-- LINKS -->
[modelsrepository_github_repo]: https://github.com/Azure/iot-plugandplay-models
[modelsrepository_sample_extension]: https://github.com/Azure/azure-sdk-for-net/blob/main/sdk/modelsrepository/Azure.IoT.ModelsRepository/samples/ModelsRepositoryClientSamples/ModelsRepositoryClientExtensions.cs
[modelsrepository_clientoptions]: https://github.com/Azure/azure-sdk-for-net/blob/main/sdk/modelsrepository/Azure.IoT.ModelsRepository/src/ModelsRepositoryClientOptions.cs
[modelsrepository_msdocs]: https://docs.microsoft.com/azure/iot-pnp/concepts-model-repository
[modelsrepository_publish_msdocs]: https://docs.microsoft.com/azure/iot-pnp/concepts-model-repository#publish-a-model
[modelsrepository_iot_endpoint]: https://devicemodels.azure.com/
[json_ld_reference]: https://json-ld.org
[dtdlv2_reference]: https://github.com/Azure/opendigitaltwins-dtdl/blob/master/DTDL/v2/dtdlv2.md
[azure_core_transport]: https://github.com/Azure/azure-sdk-for-net/blob/main/sdk/core/Azure.Core/samples/Pipeline.md
[azure_core_diagnostics]: https://github.com/Azure/azure-sdk-for-net/blob/main/sdk/core/Azure.Core/samples/Diagnostics.md
[azure_core_configuration]: https://github.com/Azure/azure-sdk-for-net/blob/main/sdk/core/Azure.Core/samples/Configuration.md
