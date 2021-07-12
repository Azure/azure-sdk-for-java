// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.data.tables.implementation.models.TableEntityQueryResponse;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Serializer for Tables responses.
 */
public class TablesJacksonSerializer extends JacksonAdapter {
    private final ClientLogger logger = new ClientLogger(TablesJacksonSerializer.class);

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
            return deserializeTableEntityQueryResponse(super.serializer().readTree(inputStream));
        } else if (inputStream != null && shouldGetEntityFieldsAsMap(type)) {
            return (U) getEntityFieldsAsMap(super.serializer().readTree(inputStream));
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
        return type instanceof ParameterizedType
            && ((ParameterizedType) type).getRawType() == Map.class;
    }

    @SuppressWarnings("unchecked")
    private <U> U deserializeTableEntityQueryResponse(JsonNode node) throws IOException {
        String odataMetadata = null;
        List<Map<String, Object>> values = new ArrayList<>();

        for (Iterator<Map.Entry<String, JsonNode>> it = node.fields(); it.hasNext();) {
            final Map.Entry<String, JsonNode> entry = it.next();
            final String fieldName = entry.getKey();
            final JsonNode childNode = entry.getValue();

            if (fieldName.equals(TablesConstants.ODATA_METADATA_KEY)) {
                odataMetadata = childNode.asText();
            } else if ("value".equals(fieldName) && childNode.isArray()) {
                // This is a multiple-entity response.
                for (JsonNode childEntry : childNode) {
                    values.add(getEntityFieldsAsMap(childEntry));
                }
            } else {
                // This is not a multiple-entity response.
                throw logger.logExceptionAsError(new IllegalStateException(
                    "Unexpected response format. Response containing a 'value' array must not contain other properties."
                ));
            }
        }

        return (U) new TableEntityQueryResponse()
            .setOdataMetadata(odataMetadata)
            .setValue(values);
    }

    private Map<String, Object> getEntityFieldsAsMap(JsonNode node) throws IOException {
        Map<String, Object> result = new HashMap<>();

        for (Iterator<String> it = node.fieldNames(); it.hasNext();) {
            String fieldName = it.next();

            if (!fieldName.equals(TablesConstants.ODATA_METADATA_KEY)) {
                result.put(fieldName, getEntityFieldAsObject(node, fieldName));
            }
        }

        return result;
    }

    private Object getEntityFieldAsObject(JsonNode parentNode, String fieldName) throws IOException {
        JsonNode valueNode = parentNode.get(fieldName);
        if (TablesConstants.METADATA_KEYS.contains(fieldName)
            || fieldName.endsWith(TablesConstants.ODATA_TYPE_KEY_SUFFIX)) {
            return serializer().treeToValue(valueNode, Object.class);
        }

        JsonNode typeNode = parentNode.get(fieldName + TablesConstants.ODATA_TYPE_KEY_SUFFIX);
        if (typeNode == null) {
            return serializer().treeToValue(valueNode, Object.class);
        }

        String typeString = typeNode.asText();
        EntityDataModelType type = EntityDataModelType.fromString(typeString);
        if (type == null) {
            logger.warning("'{}' value has unknown OData type {}", fieldName, typeString);
            return serializer().treeToValue(valueNode, Object.class);
        }

        try {
            return type.deserialize(valueNode.asText());
        } catch (Exception e) {
            throw logger.logExceptionAsError(new IllegalArgumentException(String.format(
                "'%s' value is not a valid %s.", fieldName, type.getEdmType()), e));
        }
    }
}
