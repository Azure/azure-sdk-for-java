# Azure Schema Registry Avro Serializer/Deserializer client library for Java

Azure Schema Registry Avro is a serializer/deserializer library for Avro data format that is integrated with Azure
Schema Registry hosted in Azure Event Hubs, providing schema storage, versioning, and management. This package
provides an Avro serializer capable of serializing and deserializing payloads containing Schema Registry schema
identifiers and Avro-encoded data. This library uses [Apache Avro][apache_avro] implementation for Avro serialization
and deserialization.

[Source code][source_code] | Package (Maven) | [API reference documentation][api_reference_doc] | [Product Documentation][product_documentation] | [Samples][sample_readme]

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
- [Azure Subscription][azure_subscription]
- An [Event Hubs namespace][event_hubs_namespace]

### Include the Package

[//]: # ({x-version-update-start;com.azure:azure-data-schemaregistry-avro;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-data-schemaregistry-avro</artifactId>
  <version>1.0.0-beta.3</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Create `SchemaRegistryAvroSerializer` instance

The `SchemaRegistryAvroSerializer` instance is the main class that provides APIs for serializing and
deserializing avro data format. The avro schema is stored and retrieved from the Schema Registry service
through the `SchemaRegistryClient`. So, before we create the serializer, we should create the client.

#### Create `SchemaRegistryClient` with Azure Active Directory Credential
 
In order to interact with the Azure Schema Registry service, you'll need to create an instance of the
`SchemaRegistryClient` class through the `SchemaRegistryClientBuilder`. You will need an **endpoint** and an 
**API key** to instantiate a client object.  

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
    <version>1.1.2</version>
</dependency>
```

You will also need to [register a new AAD application][register_aad_app] and [grant access][aad_grant_access] to
 Schema Registry service.
 
 <!-- embedme ./src/samples/java/com/azure/data/schemaregistry/avro/ReadmeSamples.java#L32-L37 -->
 ```java
 TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
 
 SchemaRegistryAsyncClient schemaRegistryAsyncClient = new SchemaRegistryClientBuilder()
     .endpoint("{schema-registry-endpoint")
     .credential(tokenCredential)
     .buildAsyncClient();
 ```

#### Create `SchemaRegistryAvroSerializer` through the builder

<!-- embedme ./src/samples/java/com/azure/data/schemaregistry/avro/ReadmeSamples.java#L39-L42 -->
```java
SchemaRegistryAvroSerializer schemaRegistryAvroSerializer = new SchemaRegistryAvroSerializerBuilder()
    .schemaRegistryAsyncClient(schemaRegistryAsyncClient)
    .schemaGroup("{schema-group}")
    .buildSerializer();
```

## Key concepts
### ObjectSerializer
This library provides a serializer, `SchemaRegistryAvroSerializer`, that implements the `ObjectSerializer` interface. 
This allows a developer to use this serializer in any Java Azure SDKs that utilize ObjectSerializer. The
SchemaRegistryAvroSerializer utilitizes a SchemaRegistryClient to construct messages using a wire format containing
schema information such as a schema ID.

This serializer requires the Apache Avro library. The payload types accepted by this serializer include
[GenericRecord][generic_record] and [SpecificRecord][specific_record].

### Wire Format
The serializer in this library creates messages in a wire format. The format is the following:

- Bytes [0-3] – record format indicator – currently is \x00\x00\x00\x00
- Bytes [4-35] – UTF-8 GUID, identifying the schema in a Schema Registry instance
- Bytes [36-end] – serialized payload bytes

## Examples

* [Serialize][schema_serialize]
* [Deserialize][schema_deserialize]

### Serialize
Serialize a strongly-typed object into Schema Registry-compatible avro payload.
<!-- embedme ./src/samples/java/com/azure//data/schemaregistry/avro/ReadmeSamples.java#L53-L61 -->
```java
PlayingCard playingCard = new PlayingCard();
playingCard.setPlayingCardSuit(PlayingCardSuit.SPADES);
playingCard.setIsFaceCard(false);
playingCard.setCardValue(5);

// write serialized data to byte array outputstream
ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

schemaRegistryAvroSerializer.serialize(outputStream, playingCard);
```

The avro type `PlayinCard` is available in samples package 
[`com.azure.data.schemaregistry.avro.generatedtestsources`][generated_types].

### Deserialize
Deserialize a Schema Registry-compatible avro payload into a strongly-type object.

<!-- embedme ./src/samples/java/com/azure//data/schemaregistry/avro/ReadmeSamples.java#L68-L71 -->
```java
SchemaRegistryAvroSerializer schemaRegistryAvroSerializer = createAvroSchemaRegistrySerializer();
InputStream inputStream = getSchemaRegistryAvroData();
PlayingCard playingCard = schemaRegistryAvroSerializer.deserialize(inputStream,
    TypeReference.createInstance(PlayingCard.class));
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
[samples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/schemaregistry/azure-data-schemaregistry-avro/src/samples/java/com/azure/data/schemaregistry/avro
[generated_types]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/schemaregistry/azure-data-schemaregistry-avro/src/samples/java/com/azure/data/schemaregistry/avro/generatedtestsources
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/schemaregistry/azure-data-schemaregistry-avro/src
[samples_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/schemaregistry/azure-data-schemaregistry-avro/src/samples/
[azure_subscription]: https://azure.microsoft.com/free/
[apache_avro]: https://avro.apache.org/
[api_reference_doc]: https://aka.ms/schemaregistry
[azure_cli]: https://docs.microsoft.com/cli/azure
[azure_portal]: https://portal.azure.com
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity
[DefaultAzureCredential]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md#defaultazurecredential
[event_hubs_namespace]: https://docs.microsoft.com/azure/event-hubs/event-hubs-about
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[product_documentation]: https://aka.ms/schemaregistry
[specific_record]: https://avro.apache.org/docs/1.9.2/api/java/org/apache/avro/specific/SpecificRecord.html
[generic_record]: https://avro.apache.org/docs/1.9.2/api/java/org/apache/avro/generic/GenericRecord.html

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fschemaregistry%2Fazure-data-schemaregistry-avro%2FREADME.png)
