// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.data.tables.implementation.models.TableEntityQueryResponse;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Serializer for Tables responses.
 */
public class TablesJacksonSerializer extends JacksonAdapter {
    private static final ClientLogger LOGGER = new ClientLogger(TablesJacksonSerializer.class);

    @Override
    public void serialize(Object object, SerializerEncoding encoding, OutputStream outputStream) throws IOException {
        outputStream.write(serializeToBytes(object, encoding));
    }

    @Override
    public String serialize(Object object, SerializerEncoding encoding) throws IOException {
        return new String(serializeToBytes(object, encoding), StandardCharsets.UTF_8);
    }

    @Override
    public byte[] serializeToBytes(Object object, SerializerEncoding encoding) throws IOException {
        if (object instanceof Map) {
            return super.serializeToBytes(insertTypeProperties(object), encoding);
        } else {
            return super.serializeToBytes(object, encoding);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> insertTypeProperties(Object o) {
        Map<String, Object> map = (Map<String, Object>) o;
        Map<String, Object> result = new HashMap<>();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String propertyName = entry.getKey();
            Object propertyValue = entry.getValue();

            // Skip entries with null values
            if (propertyValue == null) {
                continue;
            }

            if (propertyValue instanceof Long) {
                // Long values must be represented as a JSON string with a type annotation
                result.put(propertyName, String.valueOf(propertyValue));
            } else {
                result.put(propertyName, propertyValue);
            }

            if (TablesConstants.METADATA_KEYS.contains(propertyName)
                || propertyName.endsWith(TablesConstants.ODATA_TYPE_KEY_SUFFIX)) {
                continue;
            }

            EntityDataModelType typeToTag = EntityDataModelType.forClass(propertyValue.getClass());
            if (typeToTag == null) {
                continue;
            }

            // Use putIfAbsent to avoid overwriting a user's custom OData type annotation
            result.putIfAbsent(propertyName + TablesConstants.ODATA_TYPE_KEY_SUFFIX, typeToTag.getEdmType());
        }

        return result;
    }

    @Override
    public <U> U deserialize(String value, Type type, SerializerEncoding serializerEncoding) throws IOException {
        return deserialize(new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8)), type, serializerEncoding);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U> U deserialize(InputStream inputStream, Type type, SerializerEncoding serializerEncoding)
        throws IOException {
        if (inputStream != null && type == TableEntityQueryResponse.class) {
            try (JsonReader jsonReader = JsonProviders.createReader(inputStream)) {
                return deserializeTableEntityQueryResponse(jsonReader);
            }
        } else if (inputStream != null && shouldGetEntityFieldsAsMap(type)) {
            try (JsonReader jsonReader = JsonProviders.createReader(inputStream)) {
                return (U) getEntityFieldsAsMap(jsonReader);
            }
        } else {
            return super.deserialize(inputStream, type, serializerEncoding);
        }
    }

    @Override
    public <U> U deserialize(byte[] bytes, Type type, SerializerEncoding encoding) throws IOException {
        if (bytes == null || bytes.length == 0) {
            return super.deserialize(bytes, type, encoding);
        } else {
            return deserialize(new ByteArrayInputStream(bytes), type, encoding);
        }
    }

    private static boolean shouldGetEntityFieldsAsMap(Type type) {
        return type instanceof ParameterizedType && ((ParameterizedType) type).getRawType() == Map.class;
    }

    @SuppressWarnings("unchecked")
    public static <U> U deserializeTableEntityQueryResponse(JsonReader jsonReader) throws IOException {
        return (U) jsonReader.readObject(reader -> {
            TableEntityQueryResponse deserializedTableEntityQueryResponse = new TableEntityQueryResponse();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("odata.metadata".equals(fieldName)) {
                    deserializedTableEntityQueryResponse.setOdataMetadata(reader.getString());
                } else if ("value".equals(fieldName)) {
                    deserializedTableEntityQueryResponse.setValue(
                        reader.readArray(TablesJacksonSerializer::getEntityFieldsAsMap));
                } else {
                    // This is not a multiple-entity response.
                    // TODO (alzimmer): Should this just be ignored instead of an exception?
                    throw LOGGER.logExceptionAsError(new IllegalStateException("Unexpected response format. "
                        + "Response containing a 'value' array must not contain other properties."));
                }
            }

            return deserializedTableEntityQueryResponse;
        });
    }

    private static Map<String, Object> getEntityFieldsAsMap(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            Map<String, EntityInformation> rawEntityMap = new LinkedHashMap<>();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if (!TablesConstants.ODATA_METADATA_KEY.equals(fieldName)) {
                    rawEntityMap.put(fieldName, getEntityFieldAsObject(fieldName, reader));
                }
            }

            return processRawEntityMap(rawEntityMap);
        });
    }

    private static Map<String, Object> processRawEntityMap(Map<String, EntityInformation> rawEntityMap)
        throws IOException {
        // Convert to the actual entity map with a second processing. Size the map appropriately as the size is
        // already known.
        Map<String, Object> entityMap = new LinkedHashMap<>((int) (rawEntityMap.size() / 0.7f));

        // First process any @odata.type fields and their corresponding value fields.
        List<String> odataTypeKeys = rawEntityMap.keySet().stream()
            .filter(key -> key.endsWith(TablesConstants.ODATA_TYPE_KEY_SUFFIX))
            .collect(Collectors.toList());

        for (String odataTypeKey : odataTypeKeys) {
            EntityInformation keyInformation = rawEntityMap.remove(odataTypeKey);

            // From getEntityFieldAsObject it is known that these fields already have a value.
            entityMap.put(odataTypeKey, keyInformation.value);

            // Look for the corresponding value field for the @odata.type key.
            String expectedValueField = odataTypeKey.substring(0,
                odataTypeKey.length() - TablesConstants.ODATA_TYPE_KEY_SUFFIX.length());

            EntityInformation entityInformation = rawEntityMap.remove(expectedValueField);
            if (entityInformation != null) {
                EntityDataModelType type = EntityDataModelType.fromString(String.valueOf(keyInformation.value));
                Object value;
                if (type == null) {
                    LOGGER.warning("'{}' value has unknown OData type {}", expectedValueField,
                        keyInformation.value);
                    if (isJsonStruct(entityInformation.entityToken)) {
                        try (JsonReader structReader = JsonProviders.createReader(entityInformation.rawJson)) {
                            value = structReader.readUntyped();
                        }
                    } else {
                        value = entityInformation.value;
                    }
                } else {
                    try {
                        value = type.deserialize(entityInformation.rawJson);
                    } catch (Exception e) {
                        throw LOGGER.logExceptionAsError(new IllegalArgumentException(String.format(
                            "'%s' value is not a valid %s.", expectedValueField, type.getEdmType()), e));
                    }
                }

                entityMap.put(expectedValueField, value);
            }
        }

        // Process the remaining entity fields without checking for @odata.type
        for (Map.Entry<String, EntityInformation> entity : rawEntityMap.entrySet()) {
            Object value;
            if (isJsonStruct(entity.getValue().entityToken)) {
                try (JsonReader structReader = JsonProviders.createReader(entity.getValue().rawJson)) {
                    value = structReader.readUntyped();
                }
            } else {
                value = entity.getValue().value;
            }

            entityMap.put(entity.getKey(), value);
        }

        return entityMap;
    }

    /*
     * This function converts the entity field into a raw JSON representation that may be converted ahead of time
     * for a few cases. Other cases are left to a secondary iteration over the map keys to handle any OData type
     * specifications.
     */
    private static EntityInformation getEntityFieldAsObject(String fieldName, JsonReader jsonReader)
        throws IOException {
        JsonToken entityToken = jsonReader.currentToken();

        // Timestamp is known to be a DateTime, deserialize it as that.
        if (TablesConstants.TIMESTAMP_KEY.equals(fieldName)) {
            return new EntityInformation(entityToken, jsonReader.getText(),
                EntityDataModelType.DATE_TIME.deserialize(jsonReader.getString()));
        }

        // Metadata and OData type keys are known to be Strings.
        if (TablesConstants.METADATA_KEYS.contains(fieldName)
            || fieldName.endsWith(TablesConstants.ODATA_TYPE_KEY_SUFFIX)) {
            return new EntityInformation(entityToken, null, jsonReader.getString());
        }

        // Otherwise read the raw JSON value for the current field and let it be handled later.
        if (isJsonStruct(entityToken)) {
            // JSON arrays and objects can't both read children and produce an object value.
            return new EntityInformation(entityToken, jsonReader.readChildren(), null);
        } else {
            // All other tokens types can be processed multiple times, get the text and processed value.
            return new EntityInformation(entityToken, jsonReader.getText(), jsonReader.readUntyped());
        }
    }

    private static boolean isJsonStruct(JsonToken jsonToken) {
        return jsonToken == JsonToken.START_OBJECT || jsonToken == JsonToken.START_ARRAY;
    }

    private static final class EntityInformation {
        private final JsonToken entityToken;
        private final String rawJson;
        private final Object value;

        EntityInformation(JsonToken entityToken, String rawJson, Object value) {
            this.entityToken = entityToken;
            this.rawJson = rawJson;
            this.value = value;
        }
    }
}
