package com.azure.data.schemaregistry;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.data.schemaregistry.implementation.AzureSchemaRegistryRestService;
import com.azure.data.schemaregistry.models.SchemaRegistryObject;
import com.azure.data.schemaregistry.models.SerializationType;

@ServiceClient(
    builder = SchemaRegistryClientBuilder.class,
    serviceInterfaces = AzureSchemaRegistryRestService.class)
public final class SchemaRegistryClient {
    private final SchemaRegistryAsyncClient asyncClient;

    SchemaRegistryClient(SchemaRegistryAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public SchemaRegistryObject registerSchema(String schemaGroup, String schemaName, String schemaString,
        SerializationType schemaType) {
        return registerSchemaWithResponse(schemaGroup, schemaName, schemaString, schemaType, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SchemaRegistryObject> registerSchemaWithResponse(String schemaGroup, String schemaName,
        String schemaString, SerializationType serializationType, Context context) {
        return this.asyncClient.registerSchemaWithResponse(schemaGroup, schemaName, schemaString, serializationType,
            context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public SchemaRegistryObject getSchema(String schemaId) {
        return getSchemaWithResponse(schemaId, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SchemaRegistryObject> getSchemaWithResponse(String schemaId, Context context) {
        return this.asyncClient.getSchemaWithResponse(schemaId).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public String getSchemaId(String schemaGroup, String schemaName, String schemaString, SerializationType serializationType) {
        return getSchemaIdWithResponse(schemaGroup, schemaName, schemaString, serializationType, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<String> getSchemaIdWithResponse(String schemaGroup, String schemaName, String schemaString,
        SerializationType serializationType, Context context) {
        return this.asyncClient.getSchemaIdWithResponse(schemaGroup, schemaName, schemaString, serializationType, context)
            .block();
    }

    void clearCache() {
        this.asyncClient.clearCache();
    }

}
