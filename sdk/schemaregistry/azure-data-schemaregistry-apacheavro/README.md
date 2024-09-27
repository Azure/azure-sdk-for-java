# Azure Schema Registry Apache Avro Serializer client library for Java

Azure Schema Registry Apache Avro is a serializer and deserializer library for Avro data format that is integrated with
Azure Schema Registry hosted in Azure Event Hubs, providing schema storage, versioning, and management. This package
provides an Avro serializer capable of serializing and deserializing payloads containing Schema Registry schema
identifiers and Avro-encoded data. This library uses [Apache Avro][apache_avro] implementation for Avro serialization
and deserialization.

[Source code][source_code] | [Package (Maven)][package_maven] | [API reference documentation][api_reference_doc] | [Product Documentation][product_documentation] | [Samples][sample_readme]

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
- [Azure Subscription][azure_subscription]
- An [Event Hubs namespace][event_hubs_namespace]

### Include the Package

[//]: # ({x-version-update-start;com.azure:azure-data-schemaregistry-apacheavro;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-data-schemaregistry-apacheavro</artifactId>
  <version>1.1.21</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Create `SchemaRegistryApacheAvroSerializer` instance

The `SchemaRegistryApacheAvroSerializer` instance is the main class that provides APIs for serializing and
deserializing avro data format. The avro schema is stored and retrieved from the Schema Registry service
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
    <version>1.13.3</version>
</dependency>
```

You will also need to [register a new AAD application][register_aad_app] and [grant access][aad_grant_access] to
 Schema Registry service.

```java readme-sample-createSchemaRegistryAsyncClient
TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

// {schema-registry-endpoint} is the fully qualified namespace of the Event Hubs instance. It is usually
// of the form "{your-namespace}.servicebus.windows.net"
SchemaRegistryAsyncClient schemaRegistryAsyncClient = new SchemaRegistryClientBuilder()
    .fullyQualifiedNamespace("{your-event-hubs-namespace}.servicebus.windows.net")
    .credential(tokenCredential)
    .buildAsyncClient();
```

#### Create `SchemaRegistryAvroSerializer` through the builder

```java readme-sample-createSchemaRegistryAvroSerializer
SchemaRegistryApacheAvroSerializer serializer = new SchemaRegistryApacheAvroSerializerBuilder()
    .schemaRegistryClient(schemaRegistryAsyncClient)
    .schemaGroup("{schema-group}")
    .buildSerializer();
```

## Key concepts

This library provides a serializer, `SchemaRegistryApacheAvroSerializer`. The
`SchemaRegistryAvroSerializer` utilizes a `SchemaRegistryAsyncClient` to construct messages using a wire format
containing schema information such as a schema ID.

This serializer requires the Apache Avro library. The payload types accepted by this serializer include
[GenericRecord][generic_record] and [SpecificRecord][specific_record].

## Examples

* [Serialize](#serialize)
* [Deserialize](#deserialize)

### Serialize
Serialize a strongly-typed object into Schema Registry-compatible avro payload.

```java readme-sample-serializeSample
PlayingCard playingCard = new PlayingCard();
playingCard.setPlayingCardSuit(PlayingCardSuit.SPADES);
playingCard.setIsFaceCard(false);
playingCard.setCardValue(5);

MessageContent message = serializer.serialize(playingCard,
    TypeReference.createInstance(MessageContent.class));
```

The avro type `PlayingCard` is available in samples package
[`com.azure.data.schemaregistry.avro.generatedtestsources`][generated_types].

### Deserialize
Deserialize a Schema Registry-compatible avro payload into a strongly-type object.

```java readme-sample-deserializeSample
SchemaRegistryApacheAvroSerializer serializer = createAvroSchemaRegistrySerializer();
MessageContent message = getSchemaRegistryAvroMessage();
PlayingCard playingCard = serializer.deserialize(message, TypeReference.createInstance(PlayingCard.class));
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
[package_maven]: https://central.sonatype.com/artifact/com.azure/azure-data-schemaregistry-avro
[sample_readme]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/schemaregistry/azure-data-schemaregistry-apacheavro/src/samples
[samples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/schemaregistry/azure-data-schemaregistry-apacheavro/src/samples/java/com/azure/data/schemaregistry/apacheavro
[generated_types]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/schemaregistry/azure-data-schemaregistry-apacheavro/src/samples/java/com/azure/data/schemaregistry/apacheavro/generatedtestsources
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/schemaregistry/azure-data-schemaregistry-apacheavro/src
[samples_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/schemaregistry/azure-data-schemaregistry-apacheavro/src/samples/
[azure_subscription]: https://azure.microsoft.com/free/
[apache_avro]: https://avro.apache.org/
[api_reference_doc]: https://azure.github.io/azure-sdk-for-java/
[azure_cli]: https://docs.microsoft.com/cli/azure
[azure_portal]: https://portal.azure.com
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity
[DefaultAzureCredential]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md#defaultazurecredential
[event_hubs_namespace]: https://docs.microsoft.com/azure/event-hubs/event-hubs-about
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[product_documentation]: https://aka.ms/schemaregistry
[specific_record]: https://avro.apache.org/docs/++version++/api/java/org/apache/avro/specific/SpecificRecord.html
[generic_record]: https://avro.apache.org/docs/++version++/api/java/org/apache/avro/generic/GenericRecord.html
[custom_subdomain]: https://docs.microsoft.com/azure/cognitive-services/authentication#create-a-resource-with-a-custom-subdomain
[register_aad_app]: https://docs.microsoft.com/azure/cognitive-services/authentication#assign-a-role-to-a-service-principal
[aad_grant_access]: https://docs.microsoft.com/azure/cognitive-services/authentication#assign-a-role-to-a-service-principal
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-in-Azure-SDK
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fschemaregistry%2Fazure-data-schemaregistry-apacheavro%2FREADME.png)
