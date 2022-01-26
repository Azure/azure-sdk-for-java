// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.data.schemaregistry.models.SchemaFormat;
import com.azure.data.schemaregistry.models.SchemaProperties;
import com.azure.data.schemaregistry.models.SchemaRegistrySchema;

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
    private final SchemaRegistryAsyncClient asyncClient;

    SchemaRegistryClient(SchemaRegistryAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    /**
     * Gets the fully qualified namespace of the Schema Registry instance.
     *
     * @return The fully qualified namespace of the Schema Registry instance.
     */
    public String getFullyQualifiedNamespace() {
        return asyncClient.getFullyQualifiedNamespace();
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
        return this.asyncClient.registerSchema(groupName, name, schemaDefinition, format).block();
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
        return this.asyncClient.registerSchemaWithResponse(groupName, name, schemaDefinition, format,
            context).block();
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
    public SchemaRegistrySchema getSchema(String schemaId) {
        return this.asyncClient.getSchema(schemaId).block();
    }

    /**
     * Gets the schema properties of the schema associated with the unique schema id.
     *
     * @param schemaId The unique identifier of the schema.
     * @param context The context to pass to the Http pipeline.
     *
     * @return The {@link SchemaProperties} associated with the given {@code schemaId} and its HTTP response.
     *
     * @throws NullPointerException if {@code schemaId} is null.
     * @throws ResourceNotFoundException if a schema with the matching {@code schemaId} could not be found.
     * @throws HttpResponseException if an issue was encountered while fetching the schema.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SchemaRegistrySchema> getSchemaWithResponse(String schemaId, Context context) {
        return this.asyncClient.getSchemaWithResponse(schemaId, context).block();
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
        return this.asyncClient.getSchemaProperties(groupName, name, schemaDefinition, format).block();
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
        return this.asyncClient.getSchemaPropertiesWithResponse(groupName, name, schemaDefinition, format, context)
            .block();
    }
}
