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
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
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
    public <U> U deserialize(String value, Type type, SerializerEncoding serializerEncoding) throws IOException {
        if (type == TableEntityQueryResponse.class) {
            return deserialize(new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8)), type,
                serializerEncoding);
        } else {
            return super.deserialize(value, type, serializerEncoding);
        }
    }

    @Override
    public <U> U deserialize(InputStream inputStream, Type type, SerializerEncoding serializerEncoding)
        throws IOException {
        if (type == TableEntityQueryResponse.class) {
            return deserializeTableEntityQueryResponse(inputStream);
        } else {
            return super.deserialize(inputStream, type, serializerEncoding);
        }
    }

    @SuppressWarnings("unchecked")
    private <U> U deserializeTableEntityQueryResponse(InputStream inputStream) throws IOException {
        String odataMetadata = null;
        List<Map<String, Object>> values = new ArrayList<>();

        // Represents the entries in the response. It's possible that it is a single or multiple response.
        final JsonNode node = super.serializer().readTree(inputStream);
        Map<String, Object> singleValue = null;

        for (Iterator<Map.Entry<String, JsonNode>> it = node.fields(); it.hasNext();) {
            final Map.Entry<String, JsonNode> entry = it.next();
            final String fieldName = entry.getKey();
            final JsonNode childNode = entry.getValue();

            if (fieldName.equals(TablesConstants.ODATA_METADATA_KEY)) {
                odataMetadata = childNode.asText();
            } else if (fieldName.equals("value")) {
                if (childNode.isArray()) {
                    // This is a multiple-entity response.
                    for (JsonNode childEntry : childNode) {
                        values.add(getEntityFieldsAsMap(childEntry));
                    }
                } else {
                    // This is a single-entity response where the user just happened to use the key "value".
                    if (singleValue == null) {
                        singleValue = new HashMap<>();
                    }
                    singleValue.put(fieldName, getEntityFieldAsObject(node, fieldName));
                }
            } else {
                // This is a single-entity response.
                if (singleValue == null) {
                    singleValue = new HashMap<>();
                }
                singleValue.put(fieldName, getEntityFieldAsObject(node, fieldName));
            }
        }

        if (singleValue != null) {
            if (values.size() > 0) {
                throw logger.logExceptionAsError(new IllegalStateException(
                    "Unexpected response format. Response containing a 'value' array must not contain other properties."
                ));
            }
            values.add(singleValue);
        }

        return (U) new TableEntityQueryResponse()
            .setOdataMetadata(odataMetadata)
            .setValue(values);
    }

    private Map<String, Object> getEntityFieldsAsMap(JsonNode node) {
        Map<String, Object> result = new HashMap<>();
        for (Iterator<String> it = node.fieldNames(); it.hasNext();) {
            String fieldName = it.next();
            result.put(fieldName, getEntityFieldAsObject(node, fieldName));
        }
        return result;
    }

    private Object getEntityFieldAsObject(JsonNode parentNode, String fieldName) {
        JsonNode valueNode = parentNode.get(fieldName);
        if (!TablesConstants.METADATA_KEYS.contains(fieldName) && !fieldName.endsWith("@odata.type")) {
            JsonNode typeNode = parentNode.get(fieldName + "@odata.type");
            if (typeNode != null) {
                String type = typeNode.asText();
                switch (type) {
                    case "Edm.DateTime":
                        try {
                            return OffsetDateTime.parse(valueNode.asText());
                        } catch (DateTimeParseException e) {
                            throw logger.logExceptionAsError(new IllegalArgumentException(String.format(
                                "'%s' value is not a valid OffsetDateTime.", TablesConstants.TIMESTAMP_KEY), e));
                        }
                    default:
                        logger.warning(String.format("'%s' value has unknown OData type '%s'", fieldName, type));
                        break; // Fallthrough to the default return value
                }
            }
        }
        return valueNode.asText();
    }
}
