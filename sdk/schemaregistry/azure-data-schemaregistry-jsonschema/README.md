# Azure Schema Registry Json Schema Serializer client library for Java

Azure Schema Registry Json Schema is a serializer and deserializer library for JSON data format that is integrated with
Azure Schema Registry hosted in Azure Event Hubs, providing schema storage, versioning, and management. This package
provides a serializer capable of serializing and deserializing payloads containing Schema Registry schema
identifiers and JSON encoded data.

[Source code][source_code] | [Package (Maven)][package_maven] | [API reference documentation][api_reference_doc] | [Product Documentation][product_documentation] | [Samples][sample_readme]

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
- [Azure Subscription][azure_subscription]
- An [Event Hubs namespace][event_hubs_namespace]

### Include the Package

[//]: # ({x-version-update-start;com.azure:azure-data-schemaregistry-jsonschema;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-data-schemaregistry-jsonschema</artifactId>
  <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Create `SchemaRegistryJsonSchemaSerializer` instance

The `SchemaRegistryJsonSchemaSerializer` instance is the main class that provides APIs for serializing and
deserializing JSON schema format. The JSON schema is stored and retrieved from the Schema Registry service
through the `SchemaRegistryAsyncClient`. So, before we create the serializer, we should create the client.

#### Create `SchemaRegistryAsyncClient` with Azure Active Directory Credential

In order to interact with the Azure Schema Registry service, you'll need to create an instance of the
`SchemaRegistryAsyncClient` class through the `SchemaRegistryClientBuilder`. You will need the Schema Registry **endpoint**.

You can authenticate with Azure Active Directory using the [Azure Identity library][azure_identity]. Note that regional
endpoints do not support AAD authentication. Create a [custom subdomain][custom_subdomain] for your resource in order to
use this type of authentication.

To use the [DefaultAzureCredential][DefaultAzureCredential] provider shown below, or other credential providers provided
with the Azure SDK, please include the `azure-identity` package:

[//]: # ({x-version-update-start;com.azure:azure-identity;dependency})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
    <version>1.13.1</version>
</dependency>
```

You will also need to [register a new AAD application][register_aad_app] and [grant access][aad_grant_access] to
 Schema Registry service.

```java readme-sample-createSchemaRegistryAsyncClient
TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
JsonSchemaGenerator jsonSchemaGenerator = null;

// {schema-registry-endpoint} is the fully qualified namespace of the Event Hubs instance. It is usually
// of the form "{your-namespace}.servicebus.windows.net"
SchemaRegistryAsyncClient schemaRegistryAsyncClient = new SchemaRegistryClientBuilder()
    .fullyQualifiedNamespace("{your-event-hubs-namespace}.servicebus.windows.net")
    .credential(tokenCredential)
    .buildAsyncClient();
```

#### Create `SchemaRegistryJsonSchemaSerializer` through the builder

```java readme-sample-createSchemaRegistryJsonSchemaSerializer
SchemaRegistryJsonSchemaSerializer serializer = new SchemaRegistryJsonSchemaSerializerBuilder()
    .schemaRegistryClient(schemaRegistryAsyncClient)
    .schemaGroup("{schema-group}")
    .jsonSchemaGenerator(jsonSchemaGenerator)
    .buildSerializer();
```

## Key concepts

This library provides a serializer, `SchemaRegistryJsonSchemaSerializer`. The
`SchemaRegistryJsonSchemaSerializer` utilizes a `SchemaRegistryAsyncClient` to construct messages using a wire format
containing schema information such as a schema ID.  This library allows users to serialize, deserialize, register, and
validate JSON objects by plugging in a JSON schema implementation.

## Examples

* [Serialize](#serialize)
* [Deserialize](#deserialize)

### Serialize

Serialize a POJO object, Address, and register the matching JSON schema if 
`SchemaRegistryJsonSchemaSerializerBuilder.autoRegisterSchema(boolean)` is set to `true` in when creating the 
serializer.

```java readme-sample-serializeSample
Address address = new Address();
address.setNumber(105);
address.setStreetName("1st");
address.setStreetType("Street");

EventData eventData = serializer.serialize(address, TypeReference.createInstance(EventData.class));
```

The type `Address` is available in the test package
[`com.azure.data.schemaregistry.jsonschema.Address`][address_type].

### Deserialize

Deserialize a Schema Registry-compatible JSON payload into a strongly-type object and validate it against its JSON
schema.

```java readme-sample-deserializeSample
SchemaRegistryJsonSchemaSerializer serializer = createSerializer();
MessageContent message = getSchemaRegistryJSONMessage();
Address address = serializer.deserialize(message, TypeReference.createInstance(Address.class));
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
[package_maven]: https://central.sonatype.com/artifact/com.azure/azure-data-schemaregistry-jsonschema 
[sample_readme]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/schemaregistry/azure-data-schemaregistry-jsonschema/src/samples
[samples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/schemaregistry/azure-data-schemaregistry-jsonschema/src/samples/java/com/azure/data/schemaregistry/jsonschema
[address_type]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/schemaregistry/azure-data-schemaregistry-jsonschema/src/test/java/com/azure/data/schemaregistry/jsonschema/Address.java
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/schemaregistry/azure-data-schemaregistry-jsonschema/src
[samples_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/schemaregistry/azure-data-schemaregistry-jsonschema/src/samples/
[azure_subscription]: https://azure.microsoft.com/free/
[api_reference_doc]: https://aka.ms/schemaregistry
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity
[DefaultAzureCredential]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md#defaultazurecredential
[event_hubs_namespace]: https://docs.microsoft.com/azure/event-hubs/event-hubs-about
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[product_documentation]: https://aka.ms/schemaregistry
[custom_subdomain]: https://docs.microsoft.com/azure/cognitive-services/authentication#create-a-resource-with-a-custom-subdomain
[register_aad_app]: https://docs.microsoft.com/azure/cognitive-services/authentication#assign-a-role-to-a-service-principal
[aad_grant_access]: https://docs.microsoft.com/azure/cognitive-services/authentication#assign-a-role-to-a-service-principal
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-in-Azure-SDK
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fschemaregistry%2Fazure-data-schemaregistry-jsonschema%2FREADME.png)
