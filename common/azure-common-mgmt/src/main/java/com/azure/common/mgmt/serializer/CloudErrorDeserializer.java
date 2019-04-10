/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.mgmt.serializer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.azure.common.mgmt.CloudError;

import java.io.IOException;

/**
 * Custom serializer for serializing {@link CloudError} objects.
 */
final class CloudErrorDeserializer extends JsonDeserializer<CloudError> {
    /** Object mapper for default deserializations. */
    private ObjectMapper mapper;

    /**
     * Creates an instance of CloudErrorDeserializer.
     *
     * @param mapper the object mapper for default deserializations.
     */
    private CloudErrorDeserializer(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Gets a module wrapping this serializer as an adapter for the Jackson
     * ObjectMapper.
     *
     * @param mapper the object mapper for default deserializations.
     * @return a simple module to be plugged onto Jackson ObjectMapper.
     */
    static SimpleModule getModule(ObjectMapper mapper) {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(CloudError.class, new CloudErrorDeserializer(mapper));
        return module;
    }

    @Override
    public CloudError deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode errorNode = p.readValueAsTree();
        if (errorNode == null) {
            return null;
        }
        if (errorNode.get("error") != null) {
            errorNode = errorNode.get("error");
        }
        String nodeContent = errorNode.toString();
        nodeContent = nodeContent.replaceFirst("(?i)\"code\"", "\"code\"")
                .replaceFirst("(?i)\"message\"", "\"message\"")
                .replaceFirst("(?i)\"target\"", "\"target\"")
                .replaceFirst("(?i)\"details\"", "\"details\"");
        JsonParser parser = new JsonFactory().createParser(nodeContent);
        parser.setCodec(mapper);
        return parser.readValueAs(CloudError.class);
    }
}
