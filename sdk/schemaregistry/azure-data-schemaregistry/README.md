# Azure Schema Registry client library for Java

Azure Schema Registry is a schema repository service hosted by Azure Event Hubs, providing schema storage, versioning,
and management. The registry is leveraged by serializers to reduce payload size while describing payload structure with
schema identifiers rather than full schemas.

[Source code][source_code] | [Package (Maven)][package_maven] | [API reference documentation][api_reference_doc] | [Product Documentation][product_documentation] | [Samples][sample_readme]

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
- [Azure Subscription][azure_subscription]
- An [Event Hubs namespace][event_hubs_namespace]

### Include the package

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
and then include the direct dependency in the dependencies section without the version tag as shown below.

```xml
<dependencies>
    <dependency>
        <groupId>com.azure</groupId>
        <artifactId>azure-data-schemaregistry</artifactId>
    </dependency>
</dependencies>
```

#### Include direct dependency
If you want to take dependency on a particular version of the library that is not present in the BOM,
add the direct dependency to your project as follows.

[//]: # ({x-version-update-start;com.azure:azure-data-schemaregistry;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-data-schemaregistry</artifactId>
  <version>1.2.2</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authenticate the client
In order to interact with the Azure Schema Registry service, you'll need to create an instance of the
`SchemaRegistryClient` class through the `SchemaRegistryClientBuilder`. You will need an **endpoint** and an
**API key** to instantiate a client object.

#### Create SchemaRegistryClient with Azure Active Directory Credential

You can authenticate with Azure Active Directory using the [Azure Identity library][azure_identity]. Note that regional endpoints do not support AAD authentication. Create a [custom subdomain][custom_subdomain] for your resource in order to use this type of authentication.

To use the [DefaultAzureCredential][DefaultAzureCredential] provider shown below, or other credential providers provided with the Azure SDK, please include the `azure-identity` package:

[//]: # ({x-version-update-start;com.azure:azure-identity;dependency})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
    <version>1.5.3</version>
</dependency>
```

You will also need to [register a new AAD application][register_aad_app] and [grant access][aad_grant_access] to
 Schema Registry service.

Set the values of the client ID, tenant ID, and client secret of the AAD application as environment variables: AZURE_CLIENT_ID, AZURE_TENANT_ID, AZURE_CLIENT_SECRET.

##### Async client

```java readme-sample-createAsyncClient
TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

SchemaRegistryAsyncClient schemaRegistryAsyncClient = new SchemaRegistryClientBuilder()
    .fullyQualifiedNamespace("{schema-registry-endpoint")
    .credential(tokenCredential)
    .buildAsyncClient();
```

##### Sync client

```java readme-sample-createSyncClient
TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

SchemaRegistryClient schemaRegistryClient = new SchemaRegistryClientBuilder()
    .fullyQualifiedNamespace("{schema-registry-endpoint")
    .credential(tokenCredential)
    .buildClient();
```

## Key concepts
### Schemas

A schema has 6 components:
- Group Name: The name of the group of schemas in the Schema Registry instance.
- Schema Name: The name of the schema.
- Schema ID: The ID assigned by the Schema Registry instance for the schema.
- Serialization Type: The format used for serialization of the schema. For example, Avro.
- Schema Content: The string representation of the schema.
- Schema Version: The version assigned to the schema in the Schema Registry instance.

These components play different roles. Some are used as input into the operations and some are outputs. Currently,
[SchemaProperties][schema_properties] only exposes those properties that are potential outputs that are used in
SchemaRegistry operations. Those exposed properties are `Content` and `Id`.

## Examples

* [Register a schema](#register-a-schema)
* [Retrieve a schema's properties](#retrieve-a-schemas-properties)
* [Retrieve a schema](#retrieve-a-schema)

### Register a schema
Register a schema to be stored in the Azure Schema Registry.

```java readme-sample-registerSchema
String schemaContent = "{\n"
    + "    \"type\" : \"record\",  \n"
    + "    \"namespace\" : \"SampleSchemaNameSpace\", \n"
    + "    \"name\" : \"Person\", \n"
    + "    \"fields\" : [\n"
    + "        { \n"
    + "            \"name\" : \"FirstName\" , \"type\" : \"string\" \n"
    + "        }, \n"
    + "        { \n"
    + "            \"name\" : \"LastName\", \"type\" : \"string\" \n"
    + "        }\n"
    + "    ]\n"
    + "}";
SchemaProperties schemaProperties = schemaRegistryClient.registerSchema("{schema-group}", "{schema-name}",
    schemaContent, SchemaFormat.AVRO);

System.out.println("Registered schema: " + schemaProperties.getId());
```

### Retrieve a schema's properties
Retrieve a previously registered schema's properties from the Azure Schema Registry.

```java readme-sample-getSchema
SchemaRegistrySchema schema = schemaRegistryClient.getSchema("{schema-id}");

System.out.printf("Retrieved schema: '%s'. Contents: %s%n", schema.getProperties().getId(),
    schema.getDefinition());
```

### Retrieve a schema
Retrieve a previously registered schema's content and properties from the Azure Schema Registry.

```java readme-sample-getSchemaId
String schemaContent = "{\n"
    + "    \"type\" : \"record\",  \n"
    + "    \"namespace\" : \"SampleSchemaNameSpace\", \n"
    + "    \"name\" : \"Person\", \n"
    + "    \"fields\" : [\n"
    + "        { \n"
    + "            \"name\" : \"FirstName\" , \"type\" : \"string\" \n"
    + "        }, \n"
    + "        { \n"
    + "            \"name\" : \"LastName\", \"type\" : \"string\" \n"
    + "        }\n"
    + "    ]\n"
    + "}";
SchemaProperties properties = schemaRegistryClient.getSchemaProperties("{schema-group}", "{schema-name}",
    schemaContent, SchemaFormat.AVRO);

System.out.println("Retrieved schema id: " + properties.getId());
```

## Troubleshooting

### Enabling Logging

Azure SDKs for Java offer a consistent logging story to help aid in troubleshooting application errors and expedite
their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help
locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

## Next steps
More samples can be found [here][samples].

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a [Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

<!-- LINKS -->
[package_maven]: https://search.maven.org/artifact/com.azure/azure-data-schemaregistry
[sample_readme]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/schemaregistry/azure-data-schemaregistry/src/samples
[samples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/schemaregistry/azure-data-schemaregistry/src/samples/java/com/azure/data/schemaregistry
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/schemaregistry/azure-data-schemaregistry/src
[samples_code]: src/samples/
[azure_subscription]: https://azure.microsoft.com/free/
[api_reference_doc]: https://aka.ms/schemaregistry
[azure_cli]: https://docs.microsoft.com/cli/azure
[azure_portal]: https://portal.azure.com
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity
[DefaultAzureCredential]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md#defaultazurecredential
[event_hubs_namespace]: https://docs.microsoft.com/azure/event-hubs/event-hubs-about
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[product_documentation]: https://aka.ms/schemaregistry
[custom_subdomain]: https://docs.microsoft.com/azure/cognitive-services/authentication#create-a-resource-with-a-custom-subdomain
[register_aad_app]: https://docs.microsoft.com/azure/cognitive-services/authentication#assign-a-role-to-a-service-principal
[aad_grant_access]: https://docs.microsoft.com/azure/cognitive-services/authentication#assign-a-role-to-a-service-principal
[schema_properties]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/schemaregistry/azure-data-schemaregistry/src/main/java/com/azure/data/schemaregistry/models/SchemaProperties.java
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK#use-logback-logging-framework-in-a-spring-boot-application
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fschemaregistry%2Fazure-data-schemaregistry%2FREADME.png)
