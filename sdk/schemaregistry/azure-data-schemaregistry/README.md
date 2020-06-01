# Azure Schema Registry shared library for Java

This library contains common implementations for Azure Schema Registry-backed serializers and deserializers.

## Getting started

## Key concepts

Using registry-backed serializers and deserializers allows schema information such as names and types of fields to be held externally in schema documents, allowing serialization and deserialization frameworks to produce very compact on-wire representations of structured data (e.g. payload of events and messages). To democratize schema handling for all clients, all parties who need to serialize or deserialize event or message payloads require consistent access to the same schema store.

The serialization and deserialization implementations in this library utilize the `CachedSchemaRegistryClient` class to register and fetch schemas in Azure Schema Registry.

## Examples

## Troubleshooting

## Next steps

## Contributing
