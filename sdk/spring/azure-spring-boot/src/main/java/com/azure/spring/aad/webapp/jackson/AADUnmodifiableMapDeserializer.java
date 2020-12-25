// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A {@code JsonDeserializer} for {@link Collections#unmodifiableMap(Map)}.
 */
final class AADUnmodifiableMapDeserializer extends JsonDeserializer<Map<?, ?>> {

    @Override
    public Map<?, ?> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        ObjectMapper mapper = (ObjectMapper) parser.getCodec();
        JsonNode mapNode = mapper.readTree(parser);
        Map<String, Object> result = new LinkedHashMap<>();
        if (mapNode != null && mapNode.isObject()) {
            Iterable<Map.Entry<String, JsonNode>> fields = mapNode::fields;
            for (Map.Entry<String, JsonNode> field : fields) {
                result.put(field.getKey(), mapper.readValue(field.getValue().traverse(mapper), Object.class));
            }
        }
        return Collections.unmodifiableMap(result);
    }
}
