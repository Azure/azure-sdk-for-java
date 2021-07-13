# Azure Maps client library for Java

Azure Maps is a collection of geospatial services and SDKs that use fresh mapping data to provide geographic context to web and mobile applications. Azure Maps provides:

* REST APIs to render vector and raster maps in multiple styles and satellite imagery.
* Creator services (Preview) to create and render maps based on private indoor map data.
* Search services to locate addresses, places, and points of interest around the world.
* Various routing options; such as point-to-point, multipoint, multipoint optimization, isochrone, electric vehicle, commercial vehicle, traffic influenced, and matrix routing.
* Traffic flow view and incidents view, for applications that require real-time traffic information.
* Mobility services (Preview) to request public transit information, plan routes by blending different travel modes and real-time arrivals.
* Time zone and Geolocation (Preview) services.
* Elevation services (Preview) with Digital Elevation Model
* Geofencing service and mapping data storage, with location information hosted in Azure.
* Location intelligence through geospatial analytics.

## Getting started

### Adding the package to your project
Maven dependency for the Azure Maps library. Add it to your project's POM file.

[//]: # ({x-version-update-start;com.azure:azure-security-attestation;current})
```xml
<!-- Install the Azure Maps SDK -->
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-maps-service</artifactId>
    <version>2.0.0-preview</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Prerequisites
- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
- [Azure Subscription][azure_subscription]
- [Azure Maps Account](https://azure.microsoft.com/en-us/services/azure-maps/).

### Authenticate the client
In order to interact with the Azure Maps service, your client must present an Azure Active Directory bearer token to the service.

The simplest way of providing a bearer token is to use the  `DefaultAzureCredential` authentication method by providing client secret credentials is being used in this getting started section but you can find more ways to authenticate with [azure-identity][azure_identity].
## Key concepts

The *Key concepts* section should describe the functionality of the main classes. Point out the most important and 
useful classes in the package (with links to their reference pages) and explain how those classes work together. Feel 
free to use bulleted lists, tables, code blocks, or even diagrams for clarity.

## Examples

Include code snippets and short descriptions for each task you listed in the [Introduction](#introduction) (the bulleted list). 
Briefly explain each operation, but include enough clarity to explain complex or otherwise tricky operations.

If possible, use the same example snippets that your in-code documentation uses. For example, use the snippets in your 
`ReadmeSamples.java` that `embedme` ingests via its [alternate syntax](https://github.com/zakhenry/embedme#alternate-embedding-syntax) 
directive. The `ReadmeSamples.java` file containing the snippets should reside alongside your package's code, and should be 
validated in an automated fashion.

Each example in the *Examples* section starts with an H3 that describes the example. At the top of this section, just 
under the *Examples* H2, add a bulleted list linking to each example H3. Each example should deep-link to the types 
and/or members used in the example.

* [Create the thing](#create-the-thing)
* [Get the thing](#get-the-thing)
* [List the things](#list-the-things)

### Create the thing

Use the `createThing` method to create a Thing reference; this method does not make a network call. To persist the 
Thing in the service, call `Thing.save`.

```java
Thing thing = client.createThing(id, name);
thing.save();
```

### Get the thing

The `getThing` method retrieves a Thing from the service. The `id` parameter is the unique ID of the Thing, not its 
"name" property.

```java
Thing thing = client.getThing(id);
```

### List the things

Use `listThings` to get one or more Thing objects from the service. If there are no Things available, a `404` exception 
is thrown (see [Troubleshooting](#troubleshooting) for details on handling exceptions).

```java
List<Thing> things = client.listThings();
```

## Troubleshooting

Describe common errors and exceptions, how to "unpack" them if necessary, and include guidance for graceful handling and recovery.

Provide information to help developers avoid throttling or other service-enforced errors they might encounter. For example, 
provide guidance and examples for using retry or connection policies in the API.

If the package, or a related package supports it, include tips for logging or enabling instrumentation to help them debug their code.

### Enabling Logging

Azure SDKs for Java provide a consistent logging story to help aid in troubleshooting application errors and expedite
their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help
locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

### Default HTTP Client

By default, a Netty based HTTP client will be used. The [HTTP clients wiki](https://github.com/Azure/azure-sdk-for-java/wiki/HTTP-clients)
provides more information on configuring or changing the HTTP client.

## Next steps

* Provide a link to additional code examples, ideally to those sitting alongside the README in the package's `/samples` directory.
* If appropriate, point users to other packages that might be useful.
* If you think there's a good chance that developers might stumble across your package in error (because they're searching 
  for specific functionality and mistakenly think the package provides that functionality), point them to the packages 
  they might be looking for.
  
* After adding the new SDK, you need to include the package in the following locations
1. version_client.txt - include the package with the version.
2. parent pom - <enlistmentroot>\pom.xml - Multiple places in the file.

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a
[Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights
to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate
the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to
do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the
[Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

<!-- LINKS -->
[style-guide-msft]: https://docs.microsoft.com/style-guide/capitalization
[jdk]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Ftemplate%2Fazure-sdk-template%2FREADME.png)
