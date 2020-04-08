// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.implementation.Constants;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

@JsonSerialize(using = EncryptionProperties.JsonSerializer.class)
@JsonDeserialize(using = EncryptionProperties.JsonDeserializer.class)
class EncryptionProperties {
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static ObjectReader getObjectReader() {
        return OBJECT_READER;
    }

    public static ObjectWriter getObjectWriter() {
        return OBJECT_WRITER;
    }

    private final static ObjectReader OBJECT_READER = OBJECT_MAPPER.readerFor(EncryptionProperties.class);
    private final static ObjectWriter OBJECT_WRITER = OBJECT_MAPPER.writerFor(EncryptionProperties.class);

    public ObjectNode toObjectNode() {
        ObjectNode objectNode = OBJECT_MAPPER.createObjectNode();
        objectNode.put(Constants.Properties.EncryptionFormatVersion, this.encryptionFormatVersion);
        objectNode.put(Constants.Properties.DataEncryptionKeyRid, this.dataEncryptionKeyRid);
        objectNode.put(Constants.Properties.EncryptedData, this.encryptedData);
        return objectNode;
    }

    public static EncryptionProperties fromObjectNode(ObjectNode objectNode) throws IOException {
        return EncryptionProperties.OBJECT_READER.readValue(objectNode);
    }

    public EncryptionProperties() {
    }

    public int getEncryptionFormatVersion() {
        return encryptionFormatVersion;
    }

    public String getDataEncryptionKeyRid() {
        return dataEncryptionKeyRid;
    }

    public byte[] getEncryptedData() {
        return encryptedData;
    }

    private int encryptionFormatVersion;
    private String dataEncryptionKeyRid;
    private byte[] encryptedData;

    public EncryptionProperties(
        int encryptionFormatVersion,
        String dataEncryptionKeyRid,
        byte[] encryptedData) {
        this.encryptionFormatVersion = encryptionFormatVersion;
        this.dataEncryptionKeyRid = dataEncryptionKeyRid;
        this.encryptedData = encryptedData;
    }

    static final class JsonSerializer extends StdSerializer<EncryptionProperties> {
        private static final long serialVersionUID = 1L;

        JsonSerializer() {
            super(EncryptionProperties.class);
        }

        @Override
        public void serialize(final EncryptionProperties value, final JsonGenerator generator, final SerializerProvider provider) throws IOException {
            generator.writeStartObject();
            generator.writeNumberField(Constants.Properties.EncryptionFormatVersion, value.encryptionFormatVersion);
            generator.writeStringField(Constants.Properties.DataEncryptionKeyRid, value.dataEncryptionKeyRid);
            generator.writeBinaryField(Constants.Properties.EncryptedData, value.encryptedData);
            generator.writeEndObject();
        }
    }

    static final class JsonDeserializer extends StdDeserializer<EncryptionProperties> {
        public JsonDeserializer() {
            super(EncryptionProperties.class);
        }

        @Override
        public EncryptionProperties deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {

            ObjectCodec objectCodec = jsonParser.getCodec();
            JsonNode root;
            try {
                root = objectCodec.readTree(jsonParser);
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }

            validateOrThrow(jsonParser, root.isObject(), "can't deserialize");

            EncryptionProperties encryptionProperties = new EncryptionProperties();

            JsonNode node = root.get(Constants.Properties.EncryptionFormatVersion);

            validateOrThrow(jsonParser,node != null && node.isInt(), Constants.Properties.EncryptionFormatVersion, "can't deserialize");
            encryptionProperties.encryptionFormatVersion = node.asInt();

            node = root.get(Constants.Properties.DataEncryptionKeyRid);
            validateOrThrow(jsonParser, node != null && node.isTextual(), Constants.Properties.DataEncryptionKeyRid, "can't deserialize");
            encryptionProperties.dataEncryptionKeyRid = node.asText();

            node = root.get(Constants.Properties.EncryptedData);
            validateOrThrow(jsonParser, node != null && node.isBinary() || node.isTextual(), Constants.Properties.EncryptedData, "can't deserialize");
            encryptionProperties.encryptedData = node.binaryValue();

            return encryptionProperties;
        }

        private void validateOrThrow(JsonParser jsonParser, boolean expectedToBeTrue, String msg) throws JsonProcessingException {
            validateOrThrow(jsonParser, expectedToBeTrue, null, msg);
        }

        private void validateOrThrow(JsonParser jsonParser, boolean expectedToBeTrue, String fieldName, String msg) throws JsonProcessingException {
            if (!expectedToBeTrue) {
                if (fieldName == null) {
                    throw new JsonParseException(jsonParser, msg);
                } else {
                    throw new JsonParseException(jsonParser, fieldName + " : " + msg);
                }
            }
        }
    }
}
