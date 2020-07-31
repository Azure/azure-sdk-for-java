package com.azure.data.schemaregistry.client;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.data.schemaregistry.client.implementation.AzureSchemaRegistryRestService;

@ServiceClient(
    builder = CachedSchemaRegistryClientBuilder.class,
    serviceInterfaces = AzureSchemaRegistryRestService.class)
public final class CachedSchemaRegistryClient {
    private final CachedSchemaRegistryAsyncClient asyncClient;

    CachedSchemaRegistryClient(CachedSchemaRegistryAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    public SchemaRegistryObject registerSchema(String schemaGroup, String schemaName, String schemaString,
        String schemaType) {
        return registerSchemaWithResponse(schemaGroup, schemaName, schemaString, schemaType, Context.NONE).getValue();
    }

    public Response<SchemaRegistryObject> registerSchemaWithResponse(String schemaGroup, String schemaName,
        String schemaString, String schemaType, Context context) {
        return this.asyncClient.registerSchemaWithResponse(schemaGroup, schemaName, schemaString, schemaType,
            context).block();
    }

    public SchemaRegistryObject getSchema(String schemaId) {
        return getSchemaWithResponse(schemaId, Context.NONE).getValue();
    }

    public Response<SchemaRegistryObject> getSchemaWithResponse(String schemaId, Context context) {
        return this.asyncClient.getSchemaWithResponse(schemaId).block();
    }

    public String getSchemaId(String schemaGroup, String schemaName, String schemaString, String schemaType) {
        return getSchemaIdWithResponse(schemaGroup, schemaName, schemaString, schemaType, Context.NONE).getValue();
    }

    public Response<String> getSchemaIdWithResponse(String schemaGroup, String schemaName, String schemaString,
        String schemaType, Context context) {
        return this.asyncClient.getSchemaIdWithResponse(schemaGroup, schemaName, schemaString, schemaType, context)
            .block();
    }

}
