// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.data.tables.implementation.models.TableEntityQueryResponse;
import com.azure.data.tables.models.TableEntity;
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

        final JsonNode node = super.serializer().readTree(inputStream);
        for (Iterator<Map.Entry<String, JsonNode>> it = node.fields(); it.hasNext();) {
            final Map.Entry<String, JsonNode> entry = it.next();
            final JsonNode childNode = entry.getValue();

            if (entry.getKey().equals(TablesConstants.ODATA_METADATA_KEY)) {
                odataMetadata = childNode.asText();
                continue;
            }

            // Represents the entries in the response. It's possible that it is a single or multiple response.
            if (entry.getKey().equals("value")) {
                if (childNode.isArray()) {
                    for (JsonNode childEntry : childNode) {
                        values.add(getEntityFieldsAsMap(childEntry));
                    }
                } else {
                    values.add(getEntityFieldsAsMap(childNode));
                }
            }
        }

        return (U) new TableEntityQueryResponse()
            .setOdataMetadata(odataMetadata)
            .setValue(values);
    }

    private Map<String, Object> getEntityFieldsAsMap(JsonNode node) {
        Map<String, Object> result = new HashMap<>();
        for (Iterator<Map.Entry<String, JsonNode>> it = node.fields(); it.hasNext();) {
            Map.Entry<String, JsonNode> entry = it.next();

            String fieldName = entry.getKey();
            if (!TablesConstants.METADATA_KEYS.contains(fieldName) && !fieldName.endsWith("@odata.type")) {
                JsonNode typeNode = node.get(fieldName + "@odata.type");
                if (typeNode != null) {
                    String type = typeNode.asText();
                    switch (type) {
                        case "Edm.DateTime":
                            try {
                                result.put(fieldName, OffsetDateTime.parse(entry.getValue().asText()));
                            } catch (DateTimeParseException e) {
                                throw logger.logExceptionAsError(new IllegalArgumentException(String.format(
                                    "'%s' value is not a valid OffsetDateTime.", TablesConstants.TIMESTAMP_KEY), e));
                            }
                            break;
                        default:
                            result.put(fieldName, entry.getValue().asText());
                            break;
                    }
                } else {
                    result.put(fieldName, entry.getValue().asText());
                }
            } else {
                result.put(fieldName, entry.getValue().asText());
            }
        }
        return result;
    }
}
