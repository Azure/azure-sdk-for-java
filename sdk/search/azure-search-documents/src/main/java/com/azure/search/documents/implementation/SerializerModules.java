// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation;

import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.search.documents.implementation.util.PrivateFieldAccessHelper;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.SearchResult;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Static helper class that contains Jackson serializer modules. The modules use a pluggable {@link ObjectSerializer} to
 * handle customer objects and their JSON representations.
 */
public final class SerializerModules {
    private static final String SEARCH_ACTION = "@search.action";
    private static final String SEARCH_SCORE = "@search.score";
    private static final String SEARCH_HIGHLIGHTS = "@search.highlights";

    public static Module getIndexActionModule(com.azure.core.util.serializer.JsonSerializer serializer) {
        return new SimpleModule().addSerializer(IndexAction.class, new JsonSerializer<>() {
            @Override
            public void serialize(IndexAction value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
                gen.writeStartObject();

                gen.writeStringField(SEARCH_ACTION, value.getActionType().toString());

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                serializer.serialize(stream, value.getDocument()).block();
                byte[] rawUtf8String = stream.toByteArray();
                gen.writeRawUTF8String(rawUtf8String, 0, rawUtf8String.length);

                gen.writeEndObject();
            }
        });
    }

    public static Module getSearchResultModule(com.azure.core.util.serializer.JsonSerializer serializer) {
        return new SimpleModule().addDeserializer(SearchResult.class, new JsonDeserializer<>() {
            @Override
            public SearchResult deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                SearchResult searchResult = new SearchResult();

                StringBuilder documentJson = new StringBuilder();
                String fieldName;
                while ((fieldName = p.nextFieldName()) != null) {
                    if (SEARCH_SCORE.equalsIgnoreCase(fieldName)) {
                        PrivateFieldAccessHelper.set(searchResult, "score", getSearchScore(p));
                    } else if (SEARCH_HIGHLIGHTS.equalsIgnoreCase(fieldName)) {
                        PrivateFieldAccessHelper.set(searchResult, "highlights", getSearchHighlights(p));
                    } else {
                        handleDocumentProperty(p, documentJson);
                    }
                }

                if (documentJson.length() != 0) {
                    if (documentJson.charAt(0) == '{') {
                        documentJson.append('}');
                    }

                    byte[] documentBytes = documentJson.toString().getBytes(StandardCharsets.UTF_8);
                    Object obj = serializer.deserialize(new ByteArrayInputStream(documentBytes),
                        ctxt.getContextualType().getRawClass()).block();

                    PrivateFieldAccessHelper.set(searchResult, "additionalProperties", obj);
                }

                return searchResult;
            }
        });
    }

    private static double getSearchScore(JsonParser p) throws IOException {
        JsonToken token = p.nextValue();

        if (token != JsonToken.VALUE_NUMBER_FLOAT && token != JsonToken.VALUE_NUMBER_INT) {
            throw new IllegalStateException("Expected number value.");
        }

        return p.getDoubleValue();
    }

    private static Map<String, List<String>> getSearchHighlights(JsonParser p) throws IOException {
        JsonToken token = p.nextValue();

        if (token == JsonToken.VALUE_NULL) {
            return null;
        }

        if (token != JsonToken.START_OBJECT) {
            throw new IllegalStateException("Expected object start.");
        }

        Map<String, List<String>> highlights = new HashMap<>();

        String highlightFieldName = null;
        List<String> highlight = null;
        while ((token = p.nextToken()) != JsonToken.END_OBJECT) {
            if (token == JsonToken.FIELD_NAME) {
                highlightFieldName = p.currentName();
            } else if (token == JsonToken.START_ARRAY) {
                highlight = new ArrayList<>();
            } else if (token == JsonToken.END_ARRAY) {
                highlights.put(highlightFieldName, highlight);
                highlightFieldName = null;
                highlight = null;
            } else {
                if (highlight == null) {
                    highlight = new ArrayList<>();
                }
                highlight.add(p.getText());
            }
        }

        return highlights;
    }

    private static void handleDocumentProperty(JsonParser p, StringBuilder document) throws IOException {
        if (document.length() == 0) {
            document.append("{");
        }

        document.append(p.currentName()).append(":");

        p.nextValue();

        document.append(p.getText());
    }
}
