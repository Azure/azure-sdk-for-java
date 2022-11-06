// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.schemaregistry.implementation;

import com.azure.data.schemaregistry.implementation.models.*;
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

    public static ContentType getContentType(SchemaFormat schemaFormat) {
        Objects.requireNonNull(schemaFormat, "'schemaFormat' cannot be null.'");

        if (schemaFormat == SchemaFormat.AVRO) {
            return ContentType.APPLICATION_JSON_SERIALIZATION_AVRO;
        } else if (schemaFormat == SchemaFormat.JSON) {
            return ContentType.APPLICATION_JSON_SERIALIZATION_JSON;
        } else {
            return ContentType.TEXT_PLAIN_CHARSET_UTF8;
        }
    }

    public static SchemaFormat getSchemaFormat(ContentType contentType) {
        Objects.requireNonNull(contentType, "'schemaFormat' cannot be null.'");

        if (contentType == ContentType.APPLICATION_JSON_SERIALIZATION_AVRO) {
            return SchemaFormat.AVRO;
        } else if (contentType == ContentType.APPLICATION_JSON_SERIALIZATION_JSON) {
            return SchemaFormat.JSON;
        } else {
            return SchemaFormat.CUSTOM;
        }
    }
}

