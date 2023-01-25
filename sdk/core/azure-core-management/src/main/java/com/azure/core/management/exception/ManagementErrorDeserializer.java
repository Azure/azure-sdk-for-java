// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.exception;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

final class ManagementErrorDeserializer extends JsonDeserializer<ManagementError> {
    @Override
    public ManagementError deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        // If the current token is null move forward in parsing.
        if (p.currentToken() == null) {
            p.nextToken();
        }

        // If the current token is still null or JSON null return null.
        if (p.currentToken() == null || p.currentToken() == JsonToken.VALUE_NULL) {
            return null;
        }

        // Support both deserializing ManagementError from a direct JSON object or a wrapped JSON object if the
        // wrapping property name is 'error'.
        String fieldName = p.nextFieldName();
        boolean wrappedInErrorProperty = false;
        if ("error".equals(fieldName)) {
            // ManagementError is wrapped in an 'error' property, crack it open.
            wrappedInErrorProperty = true;

            if (p.nextToken() == JsonToken.VALUE_NULL) {
                // 'error' property was JSON null, move the parsing forward to reach the end of the property and return
                // null. This ensures that the 'error' property is fully consumed and won't leave Jackson
                // deserialization in an invalid state.
                p.nextToken();
                return null;
            }
        }

        // Deserialize ManagementError.
        ManagementError managementError = deserializeInternal(p, ctxt);

        if (wrappedInErrorProperty) {
            // ManagementError was wrapped in an 'error' property, have to move the parser forward one more token to
            // ensure the 'error' property was fully consumed.
            p.nextToken();
        }

        return managementError;
    }

    private static ManagementError deserializeInternal(JsonParser p, DeserializationContext ctxt) throws IOException {
        ManagementError managementError = new ManagementError();
        while (p.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = p.getCurrentName();
            p.nextToken();

            if ("code".equalsIgnoreCase(fieldName)) {
                managementError.setCode(p.getValueAsString());
            } else if ("message".equalsIgnoreCase(fieldName)) {
                managementError.setMessage(p.getValueAsString());
            } else if ("target".equalsIgnoreCase(fieldName)) {
                managementError.setTarget(p.getValueAsString());
            } else if ("details".equalsIgnoreCase(fieldName)) {
                if (p.currentToken() == JsonToken.START_ARRAY) {
                    List<ManagementError> details = new ArrayList<>();

                    while (p.nextToken() != JsonToken.END_ARRAY) {
                        details.add(deserializeInternal(p, ctxt));
                    }

                    managementError.setDetails(details);
                }
            } else if ("additionalInfo".equals(fieldName)) {
                if (p.currentToken() == JsonToken.START_ARRAY) {
                    List<AdditionalInfo> additionalInfo = new ArrayList<>();

                    while (p.nextToken() != JsonToken.END_ARRAY) {
                        additionalInfo.add(ctxt.readValue(p, AdditionalInfo.class));
                    }

                    managementError.setAdditionalInfo(additionalInfo);
                }
            } else {
                // Unknown property, skip it.
                p.skipChildren();
            }
        }

        return managementError;
    }

    @Override
    public Class<?> handledType() {
        return ManagementError.class;
    }
}
