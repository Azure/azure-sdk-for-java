// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.util;

import com.azure.search.documents.models.PatternAnalyzer;
import com.azure.search.documents.models.RegexFlags;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Custom serializer for {@link PatternAnalyzer}, which flatten the list of {@link RegexFlags}.
 */
public class CustomPatternAnalyzerSerializer extends JsonSerializer<PatternAnalyzer> {
    private static final String DELIMITER = "|";

    /**
     * {@inheritDoc}
     *
     * @param analyzer The {@link PatternAnalyzer} needs to serialize
     * @param gen Generator used to output resulting Json content
     * @param serializers Provider that can be used to get serializers for
     *   serializing Objects value contains, if any.
     * @param typeSer Type serializer to use for including type information
     * @throws IOException If IO operation error occurs.
     */
    @Override
    public void serializeWithType(PatternAnalyzer analyzer, JsonGenerator gen, SerializerProvider serializers,
        TypeSerializer typeSer) throws IOException {
        gen.writeStartObject();
        gen.writeStringField(typeSer.getPropertyName(), typeSer.getTypeIdResolver().idFromValue(analyzer));
        serialize(analyzer, gen, serializers);
        gen.writeEndObject();
    }

    /**
     * {@inheritDoc}
     *
     * @param analyzer The {@link PatternAnalyzer} needs to serialize
     * @param jsonGenerator Generator used to output resulting Json content
     * @param serializerProvider Provider that can be used to get serializers for
     *   serializing Objects value contains, if any.
     * @throws IOException If IO operation error occurs.
     */
    @Override
    public void serialize(final PatternAnalyzer analyzer, final JsonGenerator jsonGenerator,
        final SerializerProvider serializerProvider) throws IOException {
        if(analyzer.getName() != null) {
            jsonGenerator.writeStringField("name", analyzer.getName());
        }
        if (analyzer.isLowerCaseTerms() != null) {
            jsonGenerator.writeBooleanField("lowercase", analyzer.isLowerCaseTerms());
        }
        if (analyzer.getPattern() != null) {
            jsonGenerator.writeStringField("pattern", analyzer.getPattern());
        }
        if (analyzer.getFlags() != null) {
            String flattenFlags = analyzer.getFlags().stream().map(RegexFlags::toString)
                .collect(Collectors.joining(DELIMITER));
            jsonGenerator.writeStringField("flags", flattenFlags);
        }
        if (analyzer.getStopwords() != null) {
            jsonGenerator.writeFieldName("stopwords");
            jsonGenerator.writeStartArray();
            for (String arg: analyzer.getStopwords()) {
                jsonGenerator.writeString(arg);
            }
            jsonGenerator.writeEndArray();
        }
    }
}
