// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.schemaregistry.implementation.AzureSchemaRegistryImpl;
import com.azure.data.schemaregistry.implementation.SchemaRegistryHelper;
import com.azure.data.schemaregistry.implementation.models.ErrorException;
import com.azure.data.schemaregistry.implementation.models.SchemasQueryIdByContentHeaders;
import com.azure.data.schemaregistry.models.SchemaFormat;
import com.azure.data.schemaregistry.models.SchemaProperties;
import com.azure.data.schemaregistry.models.SchemaRegistrySchema;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * HTTP-based client that interacts with Azure Schema Registry service to store and retrieve schemas on demand.
 *
 * <p><strong>Register a schema</strong></p>
 * Registering a schema returns a unique schema id that can be used to quickly associate payloads with that schema.
 * Reactive operations must be subscribed to; this kicks off the operation.
 *
 * <!-- src_embed com.azure.data.schemaregistry.schemaregistryasyncclient.registerschema -->
 * <pre>
 * String schema = &quot;&#123;&#92;&quot;type&#92;&quot;:&#92;&quot;enum&#92;&quot;,&#92;&quot;name&#92;&quot;:&#92;&quot;TEST&#92;&quot;,&#92;&quot;symbols&#92;&quot;:[&#92;&quot;UNIT&#92;&quot;,&#92;&quot;INTEGRATION&#92;&quot;]&#125;&quot;;
 * client.registerSchema&#40;&quot;&#123;schema-group&#125;&quot;, &quot;&#123;schema-name&#125;&quot;, schema, SchemaFormat.AVRO&#41;
 *     .subscribe&#40;properties -&gt; &#123;
 *         System.out.printf&#40;&quot;Schema id: %s, schema format: %s%n&quot;, properties.getId&#40;&#41;,
 *             properties.getFormat&#40;&#41;&#41;;
 *     &#125;&#41;;
 * </pre>
 * <!-- end com.azure.data.schemaregistry.schemaregistryasyncclient.registerschema -->
 *
 * <p><strong>Get a schema</strong></p>
 * <!-- src_embed com.azure.data.schemaregistry.schemaregistryasyncclient.getSchema -->
 * <pre>
 * client.getSchema&#40;&quot;&#123;schema-id&#125;&quot;&#41;.subscribe&#40;schema -&gt; &#123;
 *     System.out.printf&#40;&quot;Schema id: %s, schema format: %s%n&quot;, schema.getProperties&#40;&#41;.getId&#40;&#41;,
 *         schema.getProperties&#40;&#41;.getFormat&#40;&#41;&#41;;
 *     System.out.println&#40;&quot;Schema contents: &quot; + schema.getDefinition&#40;&#41;&#41;;
 * &#125;&#41;;
 * </pre>
 * <!-- end com.azure.data.schemaregistry.schemaregistryasyncclient.getSchema -->
 *
 * <p><strong>Get a schema's properties</strong></p>
 * <!-- src_embed com.azure.data.schemaregistry.schemaregistryasyncclient.getSchemaProperties -->
 * <pre>
 * String schema = &quot;&#123;&#92;&quot;type&#92;&quot;:&#92;&quot;enum&#92;&quot;,&#92;&quot;name&#92;&quot;:&#92;&quot;TEST&#92;&quot;,&#92;&quot;symbols&#92;&quot;:[&#92;&quot;UNIT&#92;&quot;,&#92;&quot;INTEGRATION&#92;&quot;]&#125;&quot;;
 * client.getSchemaProperties&#40;&quot;&#123;schema-group&#125;&quot;, &quot;&#123;schema-name&#125;&quot;, schema,
 *     SchemaFormat.AVRO&#41;.subscribe&#40;properties -&gt; &#123;
 *         System.out.println&#40;&quot;The schema id: &quot; + properties.getId&#40;&#41;&#41;;
 *     &#125;&#41;;
 * </pre>
 * <!-- end com.azure.data.schemaregistry.schemaregistryasyncclient.getSchemaProperties -->
 *
 * @see SchemaRegistryClientBuilder Builder object instantiation and additional samples.
 */
@ServiceClient(builder = SchemaRegistryClientBuilder.class, isAsync = true)
public final class SchemaRegistryAsyncClient {
    private final ClientLogger logger = new ClientLogger(SchemaRegistryAsyncClient.class);
    private final AzureSchemaRegistryImpl restService;

    SchemaRegistryAsyncClient(AzureSchemaRegistryImpl restService) {
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
     * @return The {@link SchemaProperties} of a successfully registered schema.
     *
     * @throws NullPointerException if {@code groupName}, {@code name}, {@code format}, or {@code schemaDefinition}
     *     are null.
     * @throws HttpResponseException if an issue was encountered while registering the schema.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SchemaProperties> registerSchema(String groupName, String name, String schemaDefinition,
        SchemaFormat format) {
        return registerSchemaWithResponse(groupName, name, schemaDefinition, format)
            .map(Response::getValue);
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
     * @throws NullPointerException if {@code groupName}, {@code name}, {@code format}, or {@code schemaDefinition}
     *     are null.
     * @throws HttpResponseException if an issue was encountered while registering the schema.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SchemaProperties>> registerSchemaWithResponse(String groupName, String name,
        String schemaDefinition, SchemaFormat format) {
        return FluxUtil.withContext(context -> registerSchemaWithResponse(groupName, name, schemaDefinition,
            format, context));
    }

    Mono<Response<SchemaProperties>> registerSchemaWithResponse(String groupName, String name, String schemaDefinition,
        SchemaFormat format, Context context) {

        if (Objects.isNull(groupName)) {
            return monoError(logger, new NullPointerException("'groupName' should not be null."));
        } else if (Objects.isNull(name)) {
            return monoError(logger, new NullPointerException("'name' should not be null."));
        } else if (Objects.isNull(schemaDefinition)) {
            return monoError(logger, new NullPointerException("'schemaDefinition' should not be null."));
        } else if (Objects.isNull(format)) {
            return monoError(logger, new NullPointerException("'format' should not be null."));
        }

        logger.verbose("Registering schema. Group: '{}', name: '{}', serialization type: '{}', payload: '{}'",
            groupName, name, format, schemaDefinition);

        final String contentType = getContentType(format);

        return restService.getSchemas().registerWithResponseAsync(groupName, name, schemaDefinition, contentType, context)
            .map(response -> {
                final SchemaProperties registered = SchemaRegistryHelper.getSchemaProperties(response);

                return new SimpleResponse<>(
                    response.getRequest(), response.getStatusCode(),
                    response.getHeaders(), registered);
            });
    }

    /**
     * Gets the schema properties of the schema associated with the unique schema id.
     *
     * @param schemaId The unique identifier of the schema.
     *
     * @return The {@link SchemaProperties} associated with the given {@code schemaId}.
     *
     * @throws NullPointerException if {@code schemaId} is null.
     * @throws ResourceNotFoundException if a schema with the matching {@code schemaId} could not be found.
     * @throws HttpResponseException if an issue was encountered while fetching the schema.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SchemaRegistrySchema> getSchema(String schemaId) {
        return getSchemaWithResponse(schemaId).map(Response::getValue);
    }

    /**
     * Gets the schema properties of the schema associated with the unique schema id.
     *
     * @param schemaId The unique identifier of the schema.
     *
     * @return The {@link SchemaProperties} associated with the given {@code schemaId} along with the HTTP response.
     *
     * @throws NullPointerException if {@code schemaId} is null.
     * @throws ResourceNotFoundException if a schema with the matching {@code schemaId} could not be found.
     * @throws HttpResponseException if an issue was encountered while fetching the schema.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SchemaRegistrySchema>> getSchemaWithResponse(String schemaId) {
        return FluxUtil.withContext(context -> getSchemaWithResponse(schemaId, context));
    }

    Mono<Response<SchemaRegistrySchema>> getSchemaWithResponse(String schemaId, Context context) {
        if (Objects.isNull(schemaId)) {
            return monoError(logger, new NullPointerException("'schemaId' should not be null."));
        }

        return this.restService.getSchemas().getByIdWithResponseAsync(schemaId, context)
            .onErrorMap(ErrorException.class, SchemaRegistryAsyncClient::remapError)
            .map(response -> {
                final SchemaProperties schemaObject = SchemaRegistryHelper.getSchemaProperties(response);
                final String schema = new String(response.getValue(), StandardCharsets.UTF_8);

                return new SimpleResponse<>(
                    response.getRequest(), response.getStatusCode(),
                    response.getHeaders(), new SchemaRegistrySchema(schemaObject, schema));
            });
    }

    /**
     * Gets the schema identifier associated with the given schema. Gets a cached value if it exists, otherwise makes a
     * call to the service.
     *
     * @param groupName The schema group.
     * @param name The schema name.
     * @param schemaDefinition The string representation of the schema.
     * @param format The serialization type of this schema.
     *
     * @return A mono that completes with the properties for a matching schema.
     *
     * @throws NullPointerException if {@code groupName}, {@code name}, {@code schemaDefinition}, or {@code format} is
     *     null.
     * @throws ResourceNotFoundException if a schema with matching parameters could not be located.
     * @throws HttpResponseException if an issue was encountered while finding a matching schema.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SchemaProperties> getSchemaProperties(String groupName, String name, String schemaDefinition,
        SchemaFormat format) {

        return getSchemaPropertiesWithResponse(groupName, name, schemaDefinition, format)
            .map(response -> response.getValue());
    }

    /**
     * Gets the schema identifier associated with the given schema. Always makes a call to the service.
     *
     * @param groupName The schema group.
     * @param name The schema name.
     * @param schemaDefinition The string representation of the schema.
     * @param format The serialization type of this schema.
     *
     * @return A mono that completes with the properties for a matching schema.
     *
     * @throws NullPointerException if {@code groupName}, {@code name}, {@code schemaDefinition}, or {@code format} is
     *     null.
     * @throws ResourceNotFoundException if a schema with matching parameters could not be located.
     * @throws HttpResponseException if an issue was encountered while finding a matching schema.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SchemaProperties>> getSchemaPropertiesWithResponse(String groupName, String name,
        String schemaDefinition, SchemaFormat format) {

        return FluxUtil.withContext(context ->
            getSchemaPropertiesWithResponse(groupName, name, schemaDefinition, format, context));
    }

    /**
     * Gets the schema id associated with the schema name a string representation of the schema.
     *
     * @param groupName The schema group.
     * @param name The schema name.
     * @param schemaDefinition The string representation of the schema.
     * @param format The serialization type of this schema.
     * @param context Context to pass along with this request.
     *
     * @return A mono that completes with the properties for a matching schema.
     *
     * @throws NullPointerException if {@code groupName}, {@code name}, {@code schemaDefinition}, or {@code format} is
     *     null.
     * @throws ResourceNotFoundException if a schema with matching parameters could not be located.
     * @throws HttpResponseException if an issue was encountered while finding a matching schema.
     */
    Mono<Response<SchemaProperties>> getSchemaPropertiesWithResponse(String groupName, String name,
        String schemaDefinition, SchemaFormat format, Context context) {

        if (Objects.isNull(groupName)) {
            return monoError(logger, new NullPointerException("'groupName' cannot be null."));
        } else if (Objects.isNull(name)) {
            return monoError(logger, new NullPointerException("'name' cannot be null."));
        } else if (Objects.isNull(schemaDefinition)) {
            return monoError(logger, new NullPointerException("'schemaDefinition' cannot be null."));
        } else if (Objects.isNull(format)) {
            return monoError(logger, new NullPointerException("'format' cannot be null."));
        }

        if (context == null) {
            context = Context.NONE;
        }

        final String contentType = getContentType(format);

        return restService.getSchemas()
            .queryIdByContentWithResponseAsync(groupName, name, schemaDefinition, contentType, context)
            .onErrorMap(ErrorException.class, SchemaRegistryAsyncClient::remapError)
            .map(response -> {
                final SchemasQueryIdByContentHeaders deserializedHeaders = response.getDeserializedHeaders();
                final SchemaProperties properties = SchemaRegistryHelper.getSchemaProperties(response);

                return new SimpleResponse<>(
                    response.getRequest(), response.getStatusCode(),
                    response.getHeaders(), properties);
            });
    }

    /**
     * Remaps a generic ErrorException to more specific HTTP exceptions.
     *
     * @param error Error to map.
     *
     * @return The remapped error.
     */
    private static Throwable remapError(ErrorException error) {
        if (error.getResponse().getStatusCode() == 404) {
            final String message;
            if (error.getValue() != null && error.getValue().getError() != null) {
                message = error.getValue().getError().getMessage();
            } else {
                message = error.getMessage();
            }

            return new ResourceNotFoundException(message, error.getResponse(), error);
        }

        return error;
    }

    private static String getContentType(SchemaFormat schemaFormat) {
        return "application/json; serialization=" + schemaFormat;
    }
}
