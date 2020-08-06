// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.implementation;

import com.azure.core.http.HttpHeaders;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.CollectionFormat;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.data.tables.implementation.models.TableEntityQueryResponse;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.lang.reflect.Type;
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
    public String serialize(Object object, SerializerEncoding serializerEncoding) throws IOException {
        return super.serialize(object, serializerEncoding);
    }

    @Override
    public String serializeRaw(Object object) {
        return super.serializeRaw(object);
    }

    @Override
    public String serializeList(List<?> list, CollectionFormat format) {
        return super.serializeList(list, format);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <U> U deserialize(String value, Type type, SerializerEncoding serializerEncoding) throws IOException {
        if (type != TableEntityQueryResponse.class) {
            return super.deserialize(value, type, serializerEncoding);
        }

        // Force to deserialize as a Map by using Object.class
        String odataMetadata = null;
        List<Map<String, Object>> values = new ArrayList<>();

        final JsonNode node = super.serializer().readTree(value);
        final Map<String, Object> rootObject = new HashMap<>();
        for (Iterator<Map.Entry<String, JsonNode>> it = node.fields(); it.hasNext();) {
            final Map.Entry<String, JsonNode> entry = it.next();
            final JsonNode childNode = entry.getValue();

            if (entry.getKey().equals(TableConstants.ODATA_METADATA_KEY)) {
                odataMetadata = childNode.asText();
                continue;
            }

            // Represents the entries in the response. It's possible that it is a single or multiple response.
            if (entry.getKey().equals("value")) {
                if (childNode.isArray()) {
                    logger.info("Node is an array of items.");
                }

                throw logger.logExceptionAsError(
                    new UnsupportedOperationException("Multiple return values not supported yet."));
            }

            rootObject.put(entry.getKey(), entry.getValue().asText());
        }

        values.add(0, rootObject);

        return (U) new TableEntityQueryResponse()
            .setOdataMetadata(odataMetadata)
            .setValue(values);
    }

    @Override
    public <U> U deserialize(HttpHeaders httpHeaders, Type type) throws IOException {
        return super.deserialize(httpHeaders, type);
    }
}
