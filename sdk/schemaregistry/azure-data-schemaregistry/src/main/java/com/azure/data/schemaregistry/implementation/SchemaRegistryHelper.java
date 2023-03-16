// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.schemaregistry.implementation;

import com.azure.data.schemaregistry.implementation.models.SchemasGetByIdHeaders;
import com.azure.data.schemaregistry.implementation.models.SchemasGetByIdResponse;
import com.azure.data.schemaregistry.implementation.models.SchemasGetSchemaVersionHeaders;
import com.azure.data.schemaregistry.implementation.models.SchemasGetSchemaVersionResponse;
import com.azure.data.schemaregistry.implementation.models.SchemasQueryIdByContentHeaders;
import com.azure.data.schemaregistry.implementation.models.SchemasQueryIdByContentResponse;
import com.azure.data.schemaregistry.implementation.models.SchemasRegisterHeaders;
import com.azure.data.schemaregistry.implementation.models.SchemasRegisterResponse;
import com.azure.data.schemaregistry.models.SchemaFormat;
import com.azure.data.schemaregistry.models.SchemaProperties;

import java.util.Objects;

/**
 * Helper to access private-package methods of models.
 */
public final class SchemaRegistryHelper {
    private static SchemaRegistryModelsAccessor accessor;

    /**
     * Accessor interface.
     */
    public interface SchemaRegistryModelsAccessor {
        SchemaProperties getSchemaProperties(String id, SchemaFormat format, String groupName, String name,
            int version);
    }

    /**
     * Sets the accessor
     *
     * @param modelsAccessor The accessor.
     */
    public static void setAccessor(SchemaRegistryModelsAccessor modelsAccessor) {
        accessor = Objects.requireNonNull(modelsAccessor, "'modelsAccessor' cannot be null.");
    }

    public static SchemaProperties getSchemaProperties(SchemasRegisterResponse response) {
        final SchemasRegisterHeaders headers = response.getDeserializedHeaders();

        return accessor.getSchemaProperties(headers.getSchemaId(), SchemaFormat.AVRO, headers.getSchemaGroupName(),
            headers.getSchemaName(), headers.getSchemaVersion());
    }

    public static SchemaProperties getSchemaProperties(SchemasGetByIdResponse response) {
        final SchemasGetByIdHeaders headers = response.getDeserializedHeaders();

        return accessor.getSchemaProperties(headers.getSchemaId(), SchemaFormat.AVRO, headers.getSchemaGroupName(),
            headers.getSchemaName(), headers.getSchemaVersion());
    }

    public static SchemaProperties getSchemaProperties(SchemasQueryIdByContentResponse response) {
        final SchemasQueryIdByContentHeaders headers = response.getDeserializedHeaders();

        return accessor.getSchemaProperties(headers.getSchemaId(), SchemaFormat.AVRO, headers.getSchemaGroupName(),
            headers.getSchemaName(), headers.getSchemaVersion());
    }

    public static SchemaProperties getSchemaProperties(SchemasGetSchemaVersionResponse response) {
        final SchemasGetSchemaVersionHeaders headers = response.getDeserializedHeaders();

        return accessor.getSchemaProperties(headers.getSchemaId(), SchemaFormat.AVRO, headers.getSchemaGroupName(),
            headers.getSchemaName(), headers.getSchemaVersion());
    }

}

