// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.schemaregistry.implementation.AzureSchemaRegistryImpl;
import com.azure.data.schemaregistry.implementation.SchemaRegistryHelper;
import com.azure.data.schemaregistry.implementation.models.ErrorException;
import com.azure.data.schemaregistry.implementation.models.SchemasGetSchemaVersionHeaders;
import com.azure.data.schemaregistry.implementation.models.SchemasQueryIdByContentHeaders;
import com.azure.data.schemaregistry.implementation.models.SchemaFormatImpl;
import com.azure.data.schemaregistry.implementation.models.SchemasGetByIdHeaders;
import com.azure.data.schemaregistry.implementation.models.SchemasRegisterHeaders;
import com.azure.data.schemaregistry.models.SchemaFormat;
import com.azure.data.schemaregistry.models.SchemaProperties;
import com.azure.data.schemaregistry.models.SchemaRegistrySchema;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;


/**
 * HTTP-based client that interacts with Azure Schema Registry service to store and retrieve schemas on demand.
 *
 * <p><strong>Register a schema</strong></p>
 * Registering a schema returns a unique schema id that can be used to quickly associate payloads with that schema.
 * <!-- src_embed com.azure.data.schemaregistry.schemaregistryclient.registerschema -->
 * <pre>
 * String schema = &quot;&#123;&#92;&quot;type&#92;&quot;:&#92;&quot;enum&#92;&quot;,&#92;&quot;name&#92;&quot;:&#92;&quot;TEST&#92;&quot;,&#92;&quot;symbols&#92;&quot;:[&#92;&quot;UNIT&#92;&quot;,&#92;&quot;INTEGRATION&#92;&quot;]&#125;&quot;;
 * SchemaProperties properties = client.registerSchema&#40;&quot;&#123;schema-group&#125;&quot;, &quot;&#123;schema-name&#125;&quot;, schema,
 *     SchemaFormat.AVRO&#41;;
 *
 * System.out.printf&#40;&quot;Schema id: %s, schema format: %s%n&quot;, properties.getId&#40;&#41;, properties.getFormat&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.schemaregistry.schemaregistryclient.registerschema -->
 *
 * <p><strong>Get a schema</strong></p>
 * <!-- src_embed com.azure.data.schemaregistry.schemaregistryclient.getSchema -->
 * <pre>
 * SchemaRegistrySchema schema = client.getSchema&#40;&quot;&#123;schema-id&#125;&quot;&#41;;
 *
 * System.out.printf&#40;&quot;Schema id: %s, schema format: %s%n&quot;, schema.getProperties&#40;&#41;.getId&#40;&#41;,
 *     schema.getProperties&#40;&#41;.getFormat&#40;&#41;&#41;;
 * System.out.println&#40;&quot;Schema contents: &quot; + schema.getDefinition&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.schemaregistry.schemaregistryclient.getSchema -->
 *
 * <p><strong>Get a schema's properties</strong></p>
 * <!-- src_embed com.azure.data.schemaregistry.schemaregistryclient.getSchemaProperties -->
 * <pre>
 * String schema = &quot;&#123;&#92;&quot;type&#92;&quot;:&#92;&quot;enum&#92;&quot;,&#92;&quot;name&#92;&quot;:&#92;&quot;TEST&#92;&quot;,&#92;&quot;symbols&#92;&quot;:[&#92;&quot;UNIT&#92;&quot;,&#92;&quot;INTEGRATION&#92;&quot;]&#125;&quot;;
 * SchemaProperties properties = client.getSchemaProperties&#40;&quot;&#123;schema-group&#125;&quot;, &quot;&#123;schema-name&#125;&quot;, schema,
 *     SchemaFormat.AVRO&#41;;
 *
 * System.out.println&#40;&quot;The schema id: &quot; + properties.getId&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.schemaregistry.schemaregistryclient.getSchemaProperties -->
 *
 * @see SchemaRegistryClientBuilder Builder object instantiation and additional samples.
 */
@ServiceClient(builder = SchemaRegistryClientBuilder.class)
public final class SchemaRegistryClient {
    private static final String HTTP_REST_PROXY_SYNC_PROXY_ENABLE = "com.azure.core.http.restproxy.syncproxy.enable";
    private final ClientLogger logger = new ClientLogger(SchemaRegistryClient.class);
    private final AzureSchemaRegistryImpl restService;


    SchemaRegistryClient(AzureSchemaRegistryImpl restService) {
        this.restService = restService;

        // So the accessor is initialised because there were NullPointerExceptions before.
        new SchemaProperties("", SchemaFormat.AVRO);
    }

    /**
     * Gets the fully qualified namespace of the Schema Registry instance.
     *
     * @return The fully qualified namespace of the Schema Registry instance.
     */
    public String getFullyQualifiedNamespace() {
        return this.restService.getEndpoint();
    }

    /**
     * Registers a new schema in the specified schema group with the given schema name. If a schema
     * <b>does not exist</b>does not exist with the same {@code groupName}, {@code name}, {@code format}, and
     * {@code schemaDefinition}, it is added to the Schema Registry Instance and assigned a schema id. If a schema
     * exists with a matching {@code groupName}, {@code name}, {@code format}, and {@code schemaDefinition}, the id of
     * that schema is returned. If the Schema Registry instance contains an existing {@code groupName}, {@code name},
     * and {@code format} but the {@code schemaDefinition} is different, it is considered a new version, and schema id
     * is assigned to it.
     *
     * @param groupName The schema group.
     * @param name The schema name.
     * @param schemaDefinition The string representation of the schema.
     * @param format The serialization type of this schema.
     *
     * @return The schema properties on successful registration of the schema.
     *
     * @throws NullPointerException if {@code groupName}, {@code name}, {@code format}, or
     *     {@code schemaDefinition} are null.
     * @throws HttpResponseException if an issue was encountered while registering the schema.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SchemaProperties registerSchema(String groupName, String name, String schemaDefinition,
        SchemaFormat format) {
        return registerSchemaWithResponse(groupName, name, schemaDefinition, format, Context.NONE).getValue();
    }

    /**
     * Registers a new schema in the specified schema group with the given schema name. If a schema
     * <b>does not exist</b>does not exist with the same {@code groupName}, {@code name}, {@code format}, and
     * {@code schemaDefinition}, it is added to the Schema Registry Instance and assigned a schema id. If a schema
     * exists with a matching {@code groupName}, {@code name}, {@code format}, and {@code schemaDefinition}, the id of
     * that schema is returned. If the Schema Registry instance contains an existing {@code groupName}, {@code name},
     * and {@code format} but the {@code schemaDefinition} is different, it is considered a new version, and schema id
     * is assigned to it.
     *
     * @param groupName The schema group.
     * @param name The schema name.
     * @param schemaDefinition The string representation of the schema.
     * @param format The serialization type of this schema.
     * @param context The context to pass to the Http pipeline.
     *
     * @return The schema properties on successful registration of the schema.
     *
     * @throws NullPointerException if {@code groupName}, {@code name}, {@code format}, or
     *     {@code schemaDefinition} are null.
     * @throws HttpResponseException if an issue was encountered while registering the schema.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SchemaProperties> registerSchemaWithResponse(String groupName, String name, String schemaDefinition,
        SchemaFormat format, Context context) {
        if (Objects.isNull(groupName)) {
            throw logger.logExceptionAsError(new NullPointerException("'groupName' should not be null."));
        } else if (Objects.isNull(name)) {
            throw logger.logExceptionAsError(new NullPointerException("'name' should not be null."));
        } else if (Objects.isNull(schemaDefinition)) {
            throw logger.logExceptionAsError(new NullPointerException("'schemaDefinition' should not be null."));
        } else if (Objects.isNull(format)) {
            throw logger.logExceptionAsError(new NullPointerException("'format' should not be null."));
        }

        logger.verbose("Registering schema. Group: '{}', name: '{}', serialization type: '{}', payload: '{}'",
            groupName, name, format, schemaDefinition);

        context = enableSyncRestProxy(context);
        final BinaryData binaryData = BinaryData.fromString(schemaDefinition);
        final SchemaFormatImpl contentType = SchemaRegistryHelper.getContentType(format);

        ResponseBase<SchemasRegisterHeaders, Void> response = restService.getSchemas().registerWithResponse(groupName, name, contentType.toString(), binaryData, binaryData.getLength(), context);
        final SchemaProperties registered = SchemaRegistryHelper.getSchemaProperties(response.getDeserializedHeaders(), response.getHeaders(), format);
        return new SimpleResponse<>(
            response.getRequest(), response.getStatusCode(),
            response.getHeaders(), registered);
    }

    /**
     * Gets the schema properties of the schema associated with the unique schema id.
     *
     * @param schemaId The unique identifier of the schema.
     *
     * @return The {@link SchemaRegistrySchema} associated with the given {@code schemaId}.
     *
     * @throws NullPointerException if {@code schemaId} is null.
     * @throws ResourceNotFoundException if a schema with the matching {@code schemaId} could not be found.
     * @throws HttpResponseException if an issue was encountered while fetching the schema.
     * @throws UncheckedIOException if an error occurred while deserializing response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SchemaRegistrySchema getSchema(String schemaId) {
        return getSchemaWithResponse(schemaId, Context.NONE).getValue();
    }

    /**
     * Gets the schema properties of the schema associated with the group name, schema name, and schema version.
     *
     * @param groupName Group name for the schema
     * @param schemaName Name of the schema
     * @param schemaVersion Version of schema
     *
     * @return The {@link SchemaRegistrySchema} matching the parameters.
     *
     * @throws NullPointerException if {@code groupName} or {@code schemaName} is null.
     * @throws ResourceNotFoundException if a schema with the matching {@code groupName} or {@code schemaName} could
     *     not be found.
     * @throws HttpResponseException if an issue was encountered while fetching the schema.
     * @throws UncheckedIOException if an error occurred while deserializing response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SchemaRegistrySchema getSchema(String groupName, String schemaName, int schemaVersion) {
        return getSchemaWithResponse(groupName, schemaName, schemaVersion, Context.NONE).getValue();
    }

    /**
     * Gets the schema properties of the schema associated with the unique schema id.
     *
     * @param schemaId The unique identifier of the schema.
     * @param context The context to pass to the Http pipeline.
     *
     * @return The {@link SchemaRegistrySchema} associated with the given {@code schemaId} and its HTTP response.
     *
     * @throws NullPointerException if {@code schemaId} is null.
     * @throws ResourceNotFoundException if a schema with the matching {@code schemaId} could not be found.
     * @throws HttpResponseException if an issue was encountered while fetching the schema.
     * @throws UncheckedIOException if an error occurred while deserializing response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SchemaRegistrySchema> getSchemaWithResponse(String schemaId, Context context) {
        if (Objects.isNull(schemaId)) {
            throw logger.logExceptionAsError(new NullPointerException("'schemaId' should not be null."));
        }
        context = enableSyncRestProxy(context);
        try {
            ResponseBase<SchemasGetByIdHeaders, BinaryData> response = this.restService.getSchemas().getByIdWithResponse(schemaId, context);
            final SchemaProperties schemaObject = SchemaRegistryHelper.getSchemaProperties(response.getDeserializedHeaders(), response.getHeaders());
            final String schema = convertToString(response.getValue().toStream());
            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                response.getHeaders(), new SchemaRegistrySchema(schemaObject, schema));
        } catch (ErrorException ex) {
            throw logger.logExceptionAsError(SchemaRegistryAsyncClient.remapError(ex));
        }
    }

    /**
     * Gets the schema properties of the schema associated with the group name, schema name, and schema version.
     *
     * @param groupName Group name for the schema
     * @param schemaName Name of the schema
     * @param schemaVersion Version of schema
     * @param context The context to pass to the Http pipeline.
     *
     * @return The {@link SchemaRegistrySchema} matching the parameters.
     *
     * @throws NullPointerException if {@code groupName} or {@code schemaName} is null.
     * @throws ResourceNotFoundException if a schema with the matching {@code groupName} or {@code schemaName} could
     *     not be found.
     * @throws HttpResponseException if an issue was encountered while fetching the schema.
     * @throws UncheckedIOException if an error occurred while deserializing response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SchemaRegistrySchema> getSchemaWithResponse(String groupName, String schemaName,
        int schemaVersion, Context context) {
        if (Objects.isNull(groupName)) {
            throw logger.logExceptionAsError(new NullPointerException("'groupName' should not be null."));
        }
        context = enableSyncRestProxy(context);

        ResponseBase<SchemasGetSchemaVersionHeaders, BinaryData> response = this.restService.getSchemas().getSchemaVersionWithResponse(groupName, schemaName, schemaVersion,
            context);
        final InputStream schemaInputStream = response.getValue().toStream();
        final SchemaProperties schemaObject = SchemaRegistryHelper.getSchemaProperties(response.getDeserializedHeaders(), response.getHeaders());
        final String schema;

        if (schemaInputStream == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException(String.format(
                "Schema definition should not be null. Group Name: %s. Schema Name: %s. Version: %d",
                groupName, schemaName, schemaVersion)));
        }
        schema = convertToString(schemaInputStream);
        return new SimpleResponse<>(
            response.getRequest(), response.getStatusCode(),
            response.getHeaders(), new SchemaRegistrySchema(schemaObject, schema));
    }

    /**
     * Gets schema properties for a schema with matching {@code groupName}, {@code name}, {@code schemaDefinition}, and
     * {@code format}.
     *
     * @param groupName The schema group.
     * @param name The schema name.
     * @param schemaDefinition The string representation of the schema.
     * @param format The serialization type of this schema.
     *
     * @return The properties for a matching schema.
     *
     * @throws NullPointerException if {@code groupName}, {@code name}, {@code schemaDefinition}, or {@code format} is
     *     null.
     * @throws ResourceNotFoundException if a schema with matching parameters could not be located.
     * @throws HttpResponseException if an issue was encountered while finding a matching schema.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SchemaProperties getSchemaProperties(String groupName, String name, String schemaDefinition,
        SchemaFormat format) {
        return getSchemaPropertiesWithResponse(groupName, name, schemaDefinition, format, Context.NONE).getValue();
    }

    /**
     * Gets schema properties for a schema with matching {@code groupName}, {@code name}, {@code schemaDefinition}, and
     * {@code format} along with its HTTP response.
     *
     * @param groupName The schema group.
     * @param name The schema name.
     * @param schemaDefinition The string representation of the schema.
     * @param format The serialization type of this schema.
     * @param context The context to pass to the Http pipeline.
     *
     * @return A mono that completes with the properties for a matching schema.
     *
     * @throws NullPointerException if {@code groupName}, {@code name}, {@code schemaDefinition}, or {@code format} is
     *     null.
     * @throws ResourceNotFoundException if a schema with matching parameters could not be located.
     * @throws HttpResponseException if an issue was encountered while finding a matching schema.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SchemaProperties> getSchemaPropertiesWithResponse(String groupName, String name,
        String schemaDefinition, SchemaFormat format, Context context) {
        if (Objects.isNull(groupName)) {
            throw logger.logExceptionAsError(new NullPointerException("'groupName' cannot be null."));
        } else if (Objects.isNull(name)) {
            throw logger.logExceptionAsError(new NullPointerException("'name' cannot be null."));
        } else if (Objects.isNull(schemaDefinition)) {
            throw logger.logExceptionAsError(new NullPointerException("'schemaDefinition' cannot be null."));
        } else if (Objects.isNull(format)) {
            throw logger.logExceptionAsError(new NullPointerException("'format' cannot be null."));
        }

        if (context == null) {
            context = Context.NONE;
        }
        context = enableSyncRestProxy(context);

        final BinaryData binaryData = BinaryData.fromString(schemaDefinition);
        final SchemaFormatImpl contentType = SchemaRegistryHelper.getContentType(format);

        try {
            ResponseBase<SchemasQueryIdByContentHeaders, Void>  response = restService.getSchemas()
                .queryIdByContentWithResponse(groupName, name, com.azure.data.schemaregistry.implementation.models.SchemaFormat.fromString(contentType.toString()), binaryData, binaryData.getLength(), context);
            final SchemaProperties properties = SchemaRegistryHelper.getSchemaProperties(response.getDeserializedHeaders(), response.getHeaders(), format);
            return new SimpleResponse<>(
                response.getRequest(), response.getStatusCode(),
                response.getHeaders(), properties);
        } catch (ErrorException ex) {
            throw logger.logExceptionAsError(SchemaRegistryAsyncClient.remapError(ex));
        }
    }

    private Context enableSyncRestProxy(Context context) {
        return context.addData(HTTP_REST_PROXY_SYNC_PROXY_ENABLE, true);
    }

    /**
     * Converts an input stream into its string representation.
     *
     * @param inputStream Input stream.
     *
     * @return A string representation.
     *
     * @throws UncheckedIOException if an {@link IOException} is thrown when creating the readers.
     */
    static String convertToString(InputStream inputStream) {
        final StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String str;
            while ((str = reader.readLine()) != null) {
                builder.append(str);
            }
        } catch (IOException exception) {
            throw new UncheckedIOException("Error occurred while deserializing schemaContent.", exception);
        }

        return builder.toString();
    }
}
