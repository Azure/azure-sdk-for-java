// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.schemaregistry.implementation;

import com.azure.core.http.HttpHeaders;
import com.azure.data.schemaregistry.implementation.models.SchemaFormatImpl;
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
        final SchemaFormat schemaFormat = getSchemaFormat(response.getHeaders());

        return accessor.getSchemaProperties(headers.getSchemaId(), schemaFormat, headers.getSchemaGroupName(),
            headers.getSchemaName(), headers.getSchemaVersion());
    }

    public static SchemaProperties getSchemaProperties(SchemasGetByIdResponse response) {
        final SchemasGetByIdHeaders headers = response.getDeserializedHeaders();
        final SchemaFormat schemaFormat = getSchemaFormat(headers.getContentType());

        return accessor.getSchemaProperties(headers.getSchemaId(), schemaFormat, headers.getSchemaGroupName(),
            headers.getSchemaName(), headers.getSchemaVersion());
    }

    public static SchemaProperties getSchemaProperties(SchemasQueryIdByContentResponse response) {
        final SchemasQueryIdByContentHeaders headers = response.getDeserializedHeaders();
        final SchemaFormat schemaFormat = getSchemaFormat(response.getHeaders());

        return accessor.getSchemaProperties(headers.getSchemaId(), schemaFormat, headers.getSchemaGroupName(),
            headers.getSchemaName(), headers.getSchemaVersion());
    }

    public static SchemaProperties getSchemaProperties(SchemasGetSchemaVersionResponse response) {
        final SchemasGetSchemaVersionHeaders headers = response.getDeserializedHeaders();
        final SchemaFormat schemaFormat = getSchemaFormat(headers.getContentType());

        return accessor.getSchemaProperties(headers.getSchemaId(), schemaFormat, headers.getSchemaGroupName(),
            headers.getSchemaName(), headers.getSchemaVersion());
    }

    public static SchemaFormatImpl getContentType(SchemaFormat schemaFormat) {
        Objects.requireNonNull(schemaFormat, "'schemaFormat' cannot be null.'");

        if (schemaFormat == SchemaFormat.AVRO) {
            return SchemaFormatImpl.APPLICATION_JSON_SERIALIZATION_AVRO;
        } else if (schemaFormat == SchemaFormat.JSON) {
            return SchemaFormatImpl.APPLICATION_JSON_SERIALIZATION_JSON;
        } else {
            return SchemaFormatImpl.TEXT_PLAIN_CHARSET_UTF8;
        }
    }

    private static SchemaFormat getSchemaFormat(SchemaFormatImpl contentType) {
        Objects.requireNonNull(contentType, "'schemaFormat' cannot be null.'");

        if (contentType == SchemaFormatImpl.APPLICATION_JSON_SERIALIZATION_AVRO) {
            return SchemaFormat.AVRO;
        } else if (contentType == SchemaFormatImpl.APPLICATION_JSON_SERIALIZATION_JSON) {
            return SchemaFormat.JSON;
        } else {
            return SchemaFormat.CUSTOM;
        }
    }

    /**
     * Extracts "Content-Type" from HttpHeaders and translates it into {@link SchemaFormat}.
     *
     * @param headers Headers to read.
     * @return The corresponding {@link SchemaFormat} or {@code null} if the header does not exist.
     */
    private static SchemaFormat getSchemaFormat(HttpHeaders headers) {
        final String contentType = headers.getValue("Content-Type");
        if (contentType != null) {
            return getSchemaFormat(SchemaFormatImpl.fromString(contentType));
        } else {
            return null;
        }
    }

    private static SchemaFormat getSchemaFormat(String mimeType) {
        final int limit = 2;
        final String[] parts = mimeType.split(";", limit);

        if (parts.length < limit) {
            return SchemaFormat.CUSTOM;
        }

        final String[] serializationParts = parts[1].split("=", limit);

        if (serializationParts.length < limit) {
            return SchemaFormat.CUSTOM;
        }

        final SchemaFormat schemaFormat = SchemaFormat.fromString(serializationParts[1]);

        return schemaFormat != null ? schemaFormat : SchemaFormat.CUSTOM;
    }
}

