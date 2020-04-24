// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.util;

import com.azure.search.documents.models.PatternTokenizer;
import com.azure.search.documents.models.RegexFlags;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Custom serializer for {@link PatternTokenizer}, which flatten the list of {@link RegexFlags}.
 */
public class CustomPatternTokenizerSerializer extends JsonSerializer<PatternTokenizer> {
    private static final String DELIMITER = "|";

    /**
     * {@inheritDoc}
     *
     * @param tokenizer The {@link PatternTokenizer} needs to serialize.
     * @param gen Generator used to output resulting Json content
     * @param serializers Provider that can be used to get serializers for
     *   serializing Objects value contains, if any.
     * @param typeSer Type serializer to use for including type information
     * @throws IOException if IO operation error occurs.
     */
    @Override
    public void serializeWithType(PatternTokenizer tokenizer, JsonGenerator gen, SerializerProvider serializers,
        TypeSerializer typeSer) throws IOException {
        gen.writeStartObject();
        gen.writeStringField(typeSer.getPropertyName(), typeSer.getTypeIdResolver().idFromValue(tokenizer));
        serialize(tokenizer, gen, serializers);
        gen.writeEndObject();
    }

    /**
     * {@inheritDoc}
     *
     * @param tokenizer The {@link PatternTokenizer} needs to serialize.
     * @param jsonGenerator Generator used to output resulting Json content
     * @param serializerProvider Provider that can be used to get serializers for
     *   serializing Objects value contains, if any.
     * @throws IOException if IO operation error occurs.
     */
    @Override
    public void serialize(final PatternTokenizer tokenizer, final JsonGenerator jsonGenerator,
        final SerializerProvider serializerProvider) throws IOException {
        if (tokenizer.getName() != null) {
            jsonGenerator.writeStringField("name", tokenizer.getName());
        }
        if (tokenizer.getPattern() != null) {
            jsonGenerator.writeStringField("pattern", tokenizer.getPattern());
        }
        if (tokenizer.getFlags() != null) {
            String flattenFlags = tokenizer.getFlags().stream().map(RegexFlags::toString)
                .collect(Collectors.joining(DELIMITER));
            jsonGenerator.writeStringField("flags", flattenFlags);
        }
        if (tokenizer.getGroup() != null) {
            jsonGenerator.writeNumberField("group", tokenizer.getGroup());
        }
    }
}
