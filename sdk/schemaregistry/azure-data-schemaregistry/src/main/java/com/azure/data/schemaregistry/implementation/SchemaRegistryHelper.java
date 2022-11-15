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

import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

/**
 * Helper to access private-package methods of models.
 */
public final class SchemaRegistryHelper {
    private static final HashMap<String, SchemaFormatImpl> SCHEMA_FORMAT_HASH_MAP = new HashMap<>();
    private static SchemaRegistryModelsAccessor accessor;

    static {
        SchemaFormatImpl.values().forEach(value -> {
            final String mimeTypeLower = value.toString().replaceAll("\\s", "")
                .toLowerCase(Locale.ROOT);

            SCHEMA_FORMAT_HASH_MAP.put(mimeTypeLower, value);
        });
    }

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

    public static SchemaProperties getSchemaProperties(SchemasRegisterResponse response, SchemaFormat fallbackFormat) {
        final SchemasRegisterHeaders headers = response.getDeserializedHeaders();
        final SchemaFormat responseFormat = getSchemaFormat(response.getHeaders());
        final SchemaFormat schemaFormat = responseFormat != null ? responseFormat : fallbackFormat;

        return accessor.getSchemaProperties(headers.getSchemaId(), schemaFormat, headers.getSchemaGroupName(),
            headers.getSchemaName(), headers.getSchemaVersion());
    }

    public static SchemaProperties getSchemaProperties(SchemasGetByIdResponse response) {
        final SchemasGetByIdHeaders headers = response.getDeserializedHeaders();
        final SchemaFormat schemaFormat = getSchemaFormat(response.getHeaders());

        return accessor.getSchemaProperties(headers.getSchemaId(), schemaFormat, headers.getSchemaGroupName(),
            headers.getSchemaName(), headers.getSchemaVersion());
    }

    public static SchemaProperties getSchemaProperties(SchemasQueryIdByContentResponse response,
        SchemaFormat fallbackFormat) {

        final SchemasQueryIdByContentHeaders headers = response.getDeserializedHeaders();
        final SchemaFormat responseFormat = getSchemaFormat(response.getHeaders());
        final SchemaFormat schemaFormat = responseFormat != null ? responseFormat : fallbackFormat;

        return accessor.getSchemaProperties(headers.getSchemaId(), schemaFormat, headers.getSchemaGroupName(),
            headers.getSchemaName(), headers.getSchemaVersion());
    }

    public static SchemaProperties getSchemaProperties(SchemasGetSchemaVersionResponse response) {
        final SchemasGetSchemaVersionHeaders headers = response.getDeserializedHeaders();
        final SchemaFormat schemaFormat = getSchemaFormat(response.getHeaders());

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

    /**
     * Extracts "Content-Type" from HttpHeaders and translates it into {@link SchemaFormat}.
     *
     * @param headers Headers to read.
     * @return The corresponding {@link SchemaFormat} or {@code null} if the header does not exist.
     */
    public static SchemaFormat getSchemaFormat(HttpHeaders headers) {
        final String contentType = headers.getValue("Content-Type");

        if (contentType == null) {
            return null;
        }

        final String replaced = contentType.replaceAll("\\s", "").toLowerCase(Locale.ROOT);

        // Default value if nothing matches is CUSTOM.
        final SchemaFormatImpl implementationFormat = SCHEMA_FORMAT_HASH_MAP
            .getOrDefault(replaced, SchemaFormatImpl.TEXT_PLAIN_CHARSET_UTF8);

        if (SchemaFormatImpl.APPLICATION_JSON_SERIALIZATION_AVRO.equals(implementationFormat)) {
            return SchemaFormat.AVRO;
        } else if (SchemaFormatImpl.APPLICATION_JSON_SERIALIZATION_JSON.equals(implementationFormat)) {
            return SchemaFormat.JSON;
        } else {
            return SchemaFormat.CUSTOM;
        }
    }
}

