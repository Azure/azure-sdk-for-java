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
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.schemaregistry.implementation.AzureSchemaRegistryImpl;
import com.azure.data.schemaregistry.implementation.SchemaRegistryHelper;
import com.azure.data.schemaregistry.implementation.models.ErrorException;
import com.azure.data.schemaregistry.implementation.models.SchemaFormatImpl;
import com.azure.data.schemaregistry.models.SchemaFormat;
import com.azure.data.schemaregistry.models.SchemaProperties;
import com.azure.data.schemaregistry.models.SchemaRegistrySchema;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * {@link SchemaRegistryAsyncClient} is an HTTP-based client that interacts with Azure Schema Registry service to store
 * and retrieve schemas on demand.  Azure Schema Registry supports multiple schema formats such as Avro, JSON, and
 * custom formats.
 *
 * <p><strong>Sample: Construct a {@link SchemaRegistryAsyncClient}</strong></p>
 *
 * <p>The following code sample demonstrates the creation of the asynchronous client
 * {@link com.azure.data.schemaregistry.SchemaRegistryAsyncClient}.  The {@code fullyQualifiedNamespace} is the Event
 * Hubs Namespace's host name.  It is listed under the "Essentials" panel after navigating to the Event Hubs Namespace
 * via Azure Portal.  The credential used is {@code DefaultAzureCredential} for authentication, which is appropriate
 * for most scenarios, including local development and production environments. Additionally, we recommend using
 * <a href="https://learn.microsoft.com/azure/active-directory/managed-identities-azure-resources/">managed identity</a>
 * for authentication in production environments.
 * You can find more information on different ways of authenticating and their corresponding credential types in the
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme">Azure Identity documentation"</a>.
 * </p>
 *
 * <!-- src_embed com.azure.data.schemaregistry.schemaregistryasyncclient.construct -->
 * <pre>
 * DefaultAzureCredential azureCredential = new DefaultAzureCredentialBuilder&#40;&#41;
 *     .build&#40;&#41;;
 * SchemaRegistryAsyncClient client = new SchemaRegistryClientBuilder&#40;&#41;
 *     .fullyQualifiedNamespace&#40;&quot;https:&#47;&#47;&lt;your-schema-registry-endpoint&gt;.servicebus.windows.net&quot;&#41;
 *     .credential&#40;azureCredential&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.data.schemaregistry.schemaregistryasyncclient.construct -->
 *
 * <p><strong>Sample: Register a schema</strong></p>
 *
 * <p>Registering a schema returns a unique schema id that can be used to quickly associate payloads with that schema.
 * The credential used is {@code DefaultAzureCredential} because it combines commonly used credentials in deployment
 * and development and chooses the credential to used based on its running environment.  Reactive operations must be
 * subscribed to; this kicks off the operation.  {@link #registerSchema(String, String, String, SchemaFormat)} is a
 * non-blocking call, the program will move onto the next line of code after setting up the async operation.</p>
 *
 * <!-- src_embed com.azure.data.schemaregistry.schemaregistryasyncclient.registerschema-avro -->
 * <pre>
 * String schema = &quot;&#123;&#92;&quot;type&#92;&quot;:&#92;&quot;enum&#92;&quot;,&#92;&quot;name&#92;&quot;:&#92;&quot;TEST&#92;&quot;,&#92;&quot;symbols&#92;&quot;:[&#92;&quot;UNIT&#92;&quot;,&#92;&quot;INTEGRATION&#92;&quot;]&#125;&quot;;
 * client.registerSchema&#40;&quot;&#123;schema-group&#125;&quot;, &quot;&#123;schema-name&#125;&quot;, schema, SchemaFormat.AVRO&#41;
 *     .subscribe&#40;properties -&gt; &#123;
 *         System.out.printf&#40;&quot;Schema id: %s, schema format: %s%n&quot;, properties.getId&#40;&#41;,
 *             properties.getFormat&#40;&#41;&#41;;
 *     &#125;, error -&gt; &#123;
 *         System.err.println&#40;&quot;Error occurred registering schema: &quot; + error&#41;;
 *     &#125;, &#40;&#41; -&gt; &#123;
 *         System.out.println&#40;&quot;Register schema completed.&quot;&#41;;
 *     &#125;&#41;;
 * </pre>
 * <!-- end com.azure.data.schemaregistry.schemaregistryasyncclient.registerschema-avro -->
 *
 * <p><strong>Sample: Get a schema using a schema id</strong></p>
 *
 * <p>The following code sample demonstrates how to fetch a schema using its schema id.  The schema id can be found in
 * {@link com.azure.data.schemaregistry.models.SchemaProperties#getId()} when a schema is registered or using
 * {@link com.azure.data.schemaregistry.SchemaRegistryAsyncClient#getSchemaProperties(java.lang.String, java.lang.String, java.lang.String, com.azure.data.schemaregistry.models.SchemaFormat)}.
 * Reactive operations must be subscribed to; this kicks off the operation.  {@link #getSchema(String)} is a
 * non-blocking call, the program will move onto the next line of code after setting up the async operation.</p>
 *
 * <!-- src_embed com.azure.data.schemaregistry.schemaregistryasyncclient.getschema -->
 * <pre>
 * client.getSchema&#40;&quot;&#123;schema-id&#125;&quot;&#41;
 *     .subscribe&#40;schema -&gt; &#123;
 *         System.out.printf&#40;&quot;Schema id: %s, schema format: %s%n&quot;, schema.getProperties&#40;&#41;.getId&#40;&#41;,
 *             schema.getProperties&#40;&#41;.getFormat&#40;&#41;&#41;;
 *         System.out.println&#40;&quot;Schema contents: &quot; + schema.getDefinition&#40;&#41;&#41;;
 *     &#125;, error -&gt; &#123;
 *         System.err.println&#40;&quot;Error occurred getting schema: &quot; + error&#41;;
 *     &#125;, &#40;&#41; -&gt; &#123;
 *         System.out.println&#40;&quot;Get schema completed.&quot;&#41;;
 *     &#125;&#41;;
 * </pre>
 * <!-- end com.azure.data.schemaregistry.schemaregistryasyncclient.getschema -->
 *
 * <p><strong>Sample: Get a schema's properties</strong></p>
 *
 * <p>The following code sample demonstrates how to get a schema's properties given its schema contents.  Fetching
 * schema properties is useful in cases where developers want to get the unique schema id.
 * {@link #getSchemaProperties(String, String, String, SchemaFormat)} is a non-blocking call, the program will move
 * onto the next line of code after setting up the async operation.</p>
 *
 * <!-- src_embed com.azure.data.schemaregistry.schemaregistryasyncclient.getschemaproperties -->
 * <pre>
 * String schema = &quot;&#123;&#92;&quot;type&#92;&quot;:&#92;&quot;enum&#92;&quot;,&#92;&quot;name&#92;&quot;:&#92;&quot;TEST&#92;&quot;,&#92;&quot;symbols&#92;&quot;:[&#92;&quot;UNIT&#92;&quot;,&#92;&quot;INTEGRATION&#92;&quot;]&#125;&quot;;
 * client.getSchemaProperties&#40;&quot;&#123;schema-group&#125;&quot;, &quot;&#123;schema-name&#125;&quot;, schema, SchemaFormat.AVRO&#41;
 *     .subscribe&#40;properties -&gt; &#123;
 *         System.out.println&#40;&quot;Schema id: &quot; + properties.getId&#40;&#41;&#41;;
 *         System.out.println&#40;&quot;Format: &quot; + properties.getFormat&#40;&#41;&#41;;
 *         System.out.println&#40;&quot;Version: &quot; + properties.getVersion&#40;&#41;&#41;;
 *     &#125;, error -&gt; &#123;
 *         System.err.println&#40;&quot;Error occurred getting schema: &quot; + error&#41;;
 *     &#125;, &#40;&#41; -&gt; &#123;
 *         System.out.println&#40;&quot;Get schema completed.&quot;&#41;;
 *     &#125;&#41;;
 * </pre>
 * <!-- end com.azure.data.schemaregistry.schemaregistryasyncclient.getschemaproperties -->
 *
 * <p><strong>Sample: Get a schema with its HTTP response</strong></p>
 *
 * <p>The following code sample demonstrates how to get a schema using its group name, schema name, and version number.
 * In addition, it gets the underlying HTTP response that backs this service call.  This is useful in cases where
 * customers want more insight into the HTTP request/response.
 * {@link #getSchemaWithResponse(String, String, int, Context)} is a non-blocking call, the program will move onto the
 * next line of code after setting up the async operation.</p>
 *
 * <!-- src_embed com.azure.data.schemaregistry.schemaregistryasyncclient.getschemawithresponse -->
 * <pre>
 * client.getSchemaWithResponse&#40;&quot;&#123;group-name&#125;&quot;,
 *         &quot;&#123;schema-name&#125;&quot;, 1, Context.NONE&#41;
 *     .subscribe&#40;response -&gt; &#123;
 *         System.out.println&#40;&quot;Headers in HTTP response: &quot;&#41;;
 *
 *         for &#40;HttpHeader header : response.getHeaders&#40;&#41;&#41; &#123;
 *             System.out.printf&#40;&quot;%s: %s%n&quot;, header.getName&#40;&#41;, header.getValue&#40;&#41;&#41;;
 *         &#125;
 *
 *         SchemaRegistrySchema schema = response.getValue&#40;&#41;;
 *
 *         System.out.printf&#40;&quot;Schema id: %s, schema format: %s%n&quot;, schema.getProperties&#40;&#41;.getId&#40;&#41;,
 *             schema.getProperties&#40;&#41;.getFormat&#40;&#41;&#41;;
 *         System.out.println&#40;&quot;Schema contents: &quot; + schema.getDefinition&#40;&#41;&#41;;
 *     &#125;, error -&gt; &#123;
 *         System.err.println&#40;&quot;Error occurred getting schema: &quot; + error&#41;;
 *     &#125;, &#40;&#41; -&gt; &#123;
 *         System.out.println&#40;&quot;Get schema with response completed.&quot;&#41;;
 *     &#125;&#41;;
 * </pre>
 * <!-- end com.azure.data.schemaregistry.schemaregistryasyncclient.getschemawithresponse -->
 *
 * @see SchemaRegistryClientBuilder
 * @see SchemaRegistryClient
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

        final BinaryData binaryData = BinaryData.fromString(schemaDefinition);
        final SchemaFormatImpl contentType = SchemaRegistryHelper.getContentType(format);

        return restService.getSchemas().registerWithResponseAsync(groupName, name, contentType.toString(), binaryData,
                binaryData.getLength(), context)
            .map(response -> {
                final SchemaProperties registered = SchemaRegistryHelper.getSchemaProperties(response.getDeserializedHeaders(), response.getHeaders(), format);
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
     * @return The {@link SchemaRegistrySchema} associated with the given {@code schemaId}.
     *
     * @throws NullPointerException if {@code schemaId} is null.
     * @throws ResourceNotFoundException if a schema with the matching {@code schemaId} could not be found.
     * @throws HttpResponseException if an issue was encountered while fetching the schema.
     * @throws UncheckedIOException if an error occurred while deserializing response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SchemaRegistrySchema> getSchema(String schemaId) {
        return getSchemaWithResponse(schemaId).map(Response::getValue);
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
    public Mono<SchemaRegistrySchema> getSchema(String groupName, String schemaName, int schemaVersion) {
        return getSchemaWithResponse(groupName, schemaName, schemaVersion).map(Response::getValue);
    }

    /**
     * Gets the schema properties of the schema associated with the unique schema id.
     *
     * @param schemaId The unique identifier of the schema.
     *
     * @return The {@link SchemaRegistrySchema} associated with the given {@code schemaId} along with the HTTP response.
     *
     * @throws NullPointerException if {@code schemaId} is null.
     * @throws ResourceNotFoundException if a schema with the matching {@code schemaId} could not be found.
     * @throws HttpResponseException if an issue was encountered while fetching the schema.
     * @throws UncheckedIOException if an error occurred while deserializing response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SchemaRegistrySchema>> getSchemaWithResponse(String schemaId) {
        return FluxUtil.withContext(context -> getSchemaWithResponse(schemaId, context));
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
    public Mono<Response<SchemaRegistrySchema>> getSchemaWithResponse(String groupName, String schemaName,
        int schemaVersion) {

        return FluxUtil.withContext(context -> getSchemaWithResponse(groupName, schemaName, schemaVersion, context));
    }

    Mono<Response<SchemaRegistrySchema>> getSchemaWithResponse(String schemaId, Context context) {
        if (Objects.isNull(schemaId)) {
            return monoError(logger, new NullPointerException("'schemaId' should not be null."));
        }

        return this.restService.getSchemas().getByIdWithResponseAsync(schemaId, context)
            .onErrorMap(ErrorException.class, SchemaRegistryAsyncClient::remapError)
            .flatMap(response -> {
                final SchemaProperties schemaObject = SchemaRegistryHelper.getSchemaProperties(response.getDeserializedHeaders(), response.getHeaders());
                return convertToString(response.getValue())
                    .map(schema -> new SimpleResponse<>(
                    response.getRequest(), response.getStatusCode(),
                    response.getHeaders(), new SchemaRegistrySchema(schemaObject, schema)));
            });
    }

    Mono<Response<SchemaRegistrySchema>> getSchemaWithResponse(String groupName, String schemaName, int schemaVersion,
        Context context) {

        if (Objects.isNull(groupName)) {
            return monoError(logger, new NullPointerException("'groupName' should not be null."));
        }

        return this.restService.getSchemas().getSchemaVersionWithResponseAsync(groupName, schemaName, schemaVersion,
                context)
            .onErrorMap(ErrorException.class, SchemaRegistryAsyncClient::remapError)
            .flatMap(response -> {
                final Flux<ByteBuffer> schemaFlux = response.getValue();
                final SchemaProperties schemaObject = SchemaRegistryHelper.getSchemaProperties(response.getDeserializedHeaders(), response.getHeaders());

                if (schemaFlux == null) {
                    return Mono.error(new IllegalArgumentException(String.format(
                        "Schema definition should not be null. Group Name: %s. Schema Name: %s. Version: %d",
                        groupName, schemaName, schemaVersion)));
                }
                return convertToString(schemaFlux)
                    .map(schema -> new SimpleResponse<>(
                        response.getRequest(), response.getStatusCode(),
                        response.getHeaders(), new SchemaRegistrySchema(schemaObject, schema)));
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
     * @throws NullPointerException if {@code groupName}, {@code name}, {@code schemaDefinition}, or {@code format}
     *     is null.
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
     * @throws NullPointerException if {@code groupName}, {@code name}, {@code schemaDefinition}, or {@code format}
     *     is null.
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
     * @throws NullPointerException if {@code groupName}, {@code name}, {@code schemaDefinition}, or {@code format}
     *     is null.
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

        final BinaryData binaryData = BinaryData.fromString(schemaDefinition);
        final SchemaFormatImpl contentType = SchemaRegistryHelper.getContentType(format);

        return restService.getSchemas()
            .queryIdByContentWithResponseAsync(groupName, name, com.azure.data.schemaregistry.implementation.models.SchemaFormat.fromString(contentType.toString()),
                binaryData, binaryData.getLength(),
                context)
            .onErrorMap(ErrorException.class, SchemaRegistryAsyncClient::remapError)
            .map(response -> {
                final SchemaProperties properties = SchemaRegistryHelper.getSchemaProperties(response.getDeserializedHeaders(), response.getHeaders(), format);

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
    static HttpResponseException remapError(ErrorException error) {
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

    /**
     * Converts a Flux of Byte Buffer into its string representation.
     *
     * @param byteBufferFlux the Byte Buffer Flux input.
     *
     * @return A string representation.
     *
     */
    static Mono<String> convertToString(Flux<ByteBuffer> byteBufferFlux) {
        final StringBuilder builder = new StringBuilder();
        return byteBufferFlux
            .map(byteBuffer -> {
                builder.append(new String(byteBuffer.array(), StandardCharsets.UTF_8));
                return Mono.empty();
            }).then(Mono.defer(() -> Mono.just(builder.toString())));
    }
}
