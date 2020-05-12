/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.schemaregistry;

import com.azure.schemaregistry.client.SchemaRegistryClient;
import com.azure.schemaregistry.client.SchemaRegistryClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public abstract class AbstractDataSerializer extends AbstractDataSerDe {
    private static final Logger log = LoggerFactory.getLogger(AbstractDataSerializer.class);

    public static Boolean AUTO_REGISTER_SCHEMAS_DEFAULT = false;
    public static String SCHEMA_GROUP_DEFAULT = "$default";

    protected ByteEncoder byteEncoder = null;
    protected String serializationFormat;
    protected Boolean autoRegisterSchemas = AbstractDataSerializer.AUTO_REGISTER_SCHEMAS_DEFAULT;
    protected String schemaGroup = AbstractDataSerializer.SCHEMA_GROUP_DEFAULT;

    public AbstractDataSerializer(SchemaRegistryClient schemaRegistryClient) {
        super(schemaRegistryClient);
    }

    // special case for KafkaAvroSerializer
    public AbstractDataSerializer() {
    }

    protected void setByteEncoder(ByteEncoder byteEncoder) {
        if (this.byteEncoder != null) {
            throw new IllegalArgumentException("Setting multiple encoders on serializer not permitted");
        }
        this.byteEncoder = byteEncoder;
        this.schemaRegistryClient.loadSchemaParser(byteEncoder.serializationFormat(), byteEncoder::parseSchemaString);
    }

    protected byte[] serializeImpl(Object object) throws SerializationException {
        if (object == null) {
            throw new SerializationException(
                    "Null object.  Null object case should be handled in concrete serializer class." +
                            "Please file an issue.");
        }

        if (byteEncoder == null) {
            throw new SerializationException("Byte encoder null, serializer must be initialized with a byte encoder.");
        }

        if (serializationFormat == null) {
            serializationFormat = byteEncoder.serializationFormat();
        }

        String schemaString = byteEncoder.getSchemaString(object);
        String schemaName = byteEncoder.getSchemaName(object);

        try {
            String schemaGuid = maybeRegisterSchema(this.schemaGroup, schemaName, schemaString, this.serializationFormat);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            // utf8 swap todo
            ByteBuffer guidBuffer = ByteBuffer.allocate(AbstractDataSerDe.idSize)
                    .put(schemaGuid.getBytes(StandardCharsets.UTF_8));
            out.write(guidBuffer.array());
            byteEncoder.encode(object).writeTo(out);
            return out.toByteArray();
        } catch (SchemaRegistryClientException | IOException e) {
            if (this.autoRegisterSchemas) {
                throw new SerializationException(
                        String.format("Error registering Avro schema. Group: %s, name: %s", schemaGroup, schemaName),
                        e);
            } else {
                throw new SerializationException(
                        String.format("Error retrieving Avro schema. Group: %s, name: %s", schemaGroup, schemaName),
                        e);
            }
        }
    }

    private String maybeRegisterSchema(String schemaGroup, String schemaName, String schemaString, String serializationFormatString)
            throws IOException, SchemaRegistryClientException {
        if (this.autoRegisterSchemas) {
            return this.schemaRegistryClient.register(schemaGroup, schemaName, schemaString,
                    serializationFormatString).schemaGuid;
        } else {
            return this.schemaRegistryClient.getGuid(schemaGroup, schemaName, schemaString, serializationFormatString);
        }
    }
}
