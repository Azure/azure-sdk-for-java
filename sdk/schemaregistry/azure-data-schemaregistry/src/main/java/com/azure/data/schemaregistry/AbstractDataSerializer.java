// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.schemaregistry.client.SchemaRegistryClient;
import com.azure.data.schemaregistry.client.SchemaRegistryClientException;
import com.azure.data.schemaregistry.client.SchemaRegistryObject;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * Common implementation for all registry-based serializers.
 */
public abstract class AbstractDataSerializer extends AbstractDataSerDe {
    private final ClientLogger logger = new ClientLogger(AbstractDataSerializer.class);

    public static final Boolean AUTO_REGISTER_SCHEMAS_DEFAULT = false;
    public static final String SCHEMA_GROUP_DEFAULT = "$default";

    protected ByteEncoder byteEncoder = null;
    protected String schemaType;
    protected Boolean autoRegisterSchemas = AbstractDataSerializer.AUTO_REGISTER_SCHEMAS_DEFAULT;
    protected String schemaGroup = AbstractDataSerializer.SCHEMA_GROUP_DEFAULT;

    /**
     * @param schemaRegistryClient registry client to be used for storing schemas.  Not null.
     */
    public AbstractDataSerializer(SchemaRegistryClient schemaRegistryClient) {
        super(schemaRegistryClient);
    }

    /**
     * Special case constructor for Kafka serializer.
     */
    public AbstractDataSerializer() {
    }

    /**
     * Set ByteEncoder class to be used for serialized objects into bytes
     * @param byteEncoder ByteEncoder instance
     */
    protected void setByteEncoder(ByteEncoder byteEncoder) {
        if (this.byteEncoder != null) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Setting multiple encoders on serializer not permitted"));
        }
        this.byteEncoder = byteEncoder;
        this.schemaType = byteEncoder.schemaType();
        this.schemaRegistryClient.addSchemaParser(byteEncoder);
    }

    /**
     * Core implementation of registry-based serialization.
     * ID for data schema is fetched from the registry and prefixed to the encoded byte array
     * representation of the object param.
     *
     * @param object object to be serialized
     * @return byte array containing encoded bytes with prefixed schema ID
     * @throws SerializationException if serialization operation fails during runtime.
     */
    protected <T extends OutputStream> Mono<T> serializeImpl(T s, Object object) {
        if (object == null) {
            return monoError(logger, new SerializationException(
                "Null object, behavior should be defined in concrete serializer implementation."));
        }

        if (byteEncoder == null) {
            return monoError(logger, new SerializationException(
                "Byte encoder null, serializer must be initialized with a byte encoder."));
        }

        if (schemaType == null) {
            schemaType = byteEncoder.schemaType();
        }

        String schemaString = byteEncoder.getSchemaString(object);
        String schemaName = byteEncoder.getSchemaName(object);

        return this.maybeRegisterSchema(this.schemaGroup, schemaName, schemaString, this.schemaType)
            .onErrorResume(e -> {
                if (e instanceof SchemaRegistryClientException) {
                    StringBuilder builder = new StringBuilder();
                    if (this.autoRegisterSchemas) {
                        builder.append(String.format("Error registering Avro schema. Group: %s, name: %s. ",
                            schemaGroup, schemaName));
                    } else {
                        builder.append(String.format("Error retrieving Avro schema. Group: %s, name: %s. ",
                            schemaGroup, schemaName));
                    }

                    if (e.getCause() instanceof HttpResponseException) {
                        HttpResponseException httpException = (HttpResponseException) e.getCause();
                        builder.append("HTTP ")
                            .append(httpException.getResponse().getStatusCode())
                            .append(" ")
                            .append(httpException.getResponse().getBodyAsString());
                    }
                    else {
                        builder.append(e.getCause().getMessage());
                    }

                    return monoError(logger, new SerializationException(builder.toString(), e));
                }
                else {
                    return monoError(logger, new SerializationException(e.getMessage(), e));
                }
            })
            .map(id -> {
                ByteBuffer idBuffer = ByteBuffer.allocate(AbstractDataSerDe.SCHEMA_ID_SIZE)
                    .put(id.getBytes(StandardCharsets.UTF_8));
                try {
                    s.write(idBuffer.array());
                    byteEncoder.encode(object).writeTo(s);
                } catch (IOException e) {
                    throw new SerializationException(e.getMessage(), e);
                }
                return s;
            })
            .onErrorResume(
                SerializationException.class,
                e -> Mono.error(logger.logExceptionAsError(e)));
    }

    /**
     * If auto-registering is enabled, register schema against SchemaRegistryClient.
     * If auto-registering is disabled, fetch schema ID for provided schema. Requires pre-registering of schema
     * against registry.
     *
     * @param schemaGroup Schema group where schema should be registered.
     * @param schemaName name of schema
     * @param schemaString string representation of schema being stored - must match group schema type
     * @param schemaType type of schema being stored, e.g. avro
     * @return string representation of schema ID
     * @throws SchemaRegistryClientException upon registry client operation failure
     */
    private Mono<String> maybeRegisterSchema(
        String schemaGroup, String schemaName, String schemaString, String schemaType) {
        if (this.autoRegisterSchemas) {
            return this.schemaRegistryClient.register(schemaGroup, schemaName, schemaString, schemaType)
                .map(SchemaRegistryObject::getSchemaId);
        } else {
            return this.schemaRegistryClient.getSchemaId(
                schemaGroup, schemaName, schemaString, schemaType);
        }
    }
}
