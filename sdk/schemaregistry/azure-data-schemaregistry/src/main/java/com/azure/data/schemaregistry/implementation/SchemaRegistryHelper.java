// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.schemaregistry.implementation;

//<<<<<<< HEAD
//import com.azure.core.http.rest.ResponseBase;
//import com.azure.core.util.BinaryData;
//=======
import com.azure.core.http.HttpHeaders;
import com.azure.data.schemaregistry.implementation.models.SchemaFormatImpl;
//>>>>>>> upstream/main
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
import java.util.regex.Pattern;

/**
 * Helper to access private-package methods of models.
 */
public final class SchemaRegistryHelper {
    private static final HashMap<String, SchemaFormatImpl> SCHEMA_FORMAT_HASH_MAP = new HashMap<>();
    private static final Pattern SCHEMA_FORMAT_PATTERN = Pattern.compile("\\s");

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

//<<<<<<< HEAD
//    public static SchemaProperties getSchemaPropertiesFromSchemasGetSchemaVersionHeaders(ResponseBase<SchemasGetSchemaVersionHeaders, BinaryData> response) {
//        final SchemasGetSchemaVersionHeaders headers = response.getDeserializedHeaders();
//=======
    public static SchemaProperties getSchemaProperties(SchemasRegisterResponse response, SchemaFormat fallbackFormat) {
        final SchemasRegisterHeaders headers = response.getDeserializedHeaders();
        final SchemaFormat responseFormat = getSchemaFormat(response.getHeaders());
        final SchemaFormat schemaFormat = responseFormat != null ? responseFormat : fallbackFormat;
//>>>>>>> upstream/main

        return accessor.getSchemaProperties(headers.getSchemaId(), schemaFormat, headers.getSchemaGroupName(),
            headers.getSchemaName(), headers.getSchemaVersion());
    }

//<<<<<<< HEAD
//    public static SchemaProperties getSchemaPropertiesFromSchemasQueryIdByContentHeaders(ResponseBase<SchemasQueryIdByContentHeaders, Void> response) {
//        final SchemasQueryIdByContentHeaders headers = response.getDeserializedHeaders();
//=======
    public static SchemaProperties getSchemaProperties(SchemasGetByIdResponse response) {
        final SchemasGetByIdHeaders headers = response.getDeserializedHeaders();
        final SchemaFormat schemaFormat = getSchemaFormat(response.getHeaders());
//>>>>>>> upstream/main

        return accessor.getSchemaProperties(headers.getSchemaId(), schemaFormat, headers.getSchemaGroupName(),
            headers.getSchemaName(), headers.getSchemaVersion());
    }

//<<<<<<< HEAD
//    public static SchemaProperties getSchemaPropertiesFromSchemasGetByIdHeaders(ResponseBase<SchemasGetByIdHeaders, BinaryData> response) {
//        final SchemasGetByIdHeaders headers = response.getDeserializedHeaders();
//=======
    public static SchemaProperties getSchemaProperties(SchemasQueryIdByContentResponse response,
        SchemaFormat fallbackFormat) {

        final SchemasQueryIdByContentHeaders headers = response.getDeserializedHeaders();
        final SchemaFormat responseFormat = getSchemaFormat(response.getHeaders());
        final SchemaFormat schemaFormat = responseFormat != null ? responseFormat : fallbackFormat;
//>>>>>>> upstream/main

        return accessor.getSchemaProperties(headers.getSchemaId(), schemaFormat, headers.getSchemaGroupName(),
            headers.getSchemaName(), headers.getSchemaVersion());
    }

//<<<<<<< HEAD
//    public static SchemaProperties getSchemaPropertiesFromSchemaRegisterHeaders(ResponseBase<SchemasRegisterHeaders, Void> response) {
//        final SchemasRegisterHeaders headers = response.getDeserializedHeaders();
//=======
    public static SchemaProperties getSchemaProperties(SchemasGetSchemaVersionResponse response) {
        final SchemasGetSchemaVersionHeaders headers = response.getDeserializedHeaders();
        final SchemaFormat schemaFormat = getSchemaFormat(response.getHeaders());
//>>>>>>> upstream/main

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

        final String replaced = SCHEMA_FORMAT_PATTERN.matcher(contentType).replaceAll("")
            .toLowerCase(Locale.ROOT);

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

