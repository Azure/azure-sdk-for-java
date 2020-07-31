package com.azure.data.schemaregistry;

import com.azure.core.annotation.ServiceClient;
import com.azure.data.schemaregistry.implementation.AzureSchemaRegistryRestService;

@ServiceClient(
    builder = CachedSchemaRegistryClientBuilder.class,
    serviceInterfaces = AzureSchemaRegistryRestService.class)
public final class CachedSchemaRegistryClient {
    private final CachedSchemaRegistryAsyncClient asyncClient;

    CachedSchemaRegistryClient(CachedSchemaRegistryAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    public SchemaRegistryObject registerSchema(String schemaGroup, String schemaName, String schemaString, String schemaType) {
        return this.asyncClient.registerSchema(schemaGroup, schemaName, schemaString, schemaType).block();
    }

    public SchemaRegistryObject getSchema(String schemaId) {
        return this.asyncClient.getSchema(schemaId).block();
    }

    public String getSchemaId(String schemaGroup, String schemaName, String schemaString, String schemaType) {
        return this.asyncClient.getSchemaId(schemaGroup, schemaName, schemaString, schemaType).block();
    }

    public void clearCache() {
        this.asyncClient.clearCache();
    }
}
