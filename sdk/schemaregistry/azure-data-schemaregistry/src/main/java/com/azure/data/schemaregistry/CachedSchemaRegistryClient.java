package com.azure.data.schemaregistry;

import com.azure.core.annotation.ServiceClient;
import com.azure.data.schemaregistry.Codec;
import com.azure.data.schemaregistry.implementation.AzureSchemaRegistryRestService;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;

@ServiceClient(
    builder = CachedSchemaRegistryClientBuilder.class,
    serviceInterfaces = AzureSchemaRegistryRestService.class)
public final class CachedSchemaRegistryClient {
    private final CachedSchemaRegistryAsyncClient asyncClient;

    CachedSchemaRegistryClient(CachedSchemaRegistryAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    public SchemaRegistryObject register(String schemaGroup, String schemaName, String schemaString, String schemaType) {
        return this.asyncClient.register(schemaGroup, schemaName, schemaString, schemaType).block();
    }

    public SchemaRegistryObject getSchemaById(String schemaId) {
        return this.asyncClient.getSchemaById(schemaId).block();
    }

    public String getSchemaId(String schemaGroup, String schemaName, String schemaString, String schemaType) {
        return this.asyncClient.getSchemaId(schemaGroup, schemaName, schemaString, schemaType).block();
    }
}
