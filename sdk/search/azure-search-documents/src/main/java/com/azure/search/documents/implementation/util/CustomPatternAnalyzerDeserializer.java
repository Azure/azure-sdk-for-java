// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.util;

import com.azure.search.documents.models.PatternAnalyzer;
import com.azure.search.documents.models.RegexFlags;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Custom deserializer for {@link PatternAnalyzer}, which convert flatten string to the list of {@link RegexFlags}.
 */
public class CustomPatternAnalyzerDeserializer extends JsonDeserializer<PatternAnalyzer> {
    private static final String DELIMITER = "\\|";

    /**
     * {@inheritDoc}
     */
    @Override
    public PatternAnalyzer deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.readTree(p);

        Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
        PatternAnalyzer analyzer = new PatternAnalyzer();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            if ("name".equals(field.getKey())) {
                analyzer.setName(field.getValue().asText());
            } else if ("pattern".equals(field.getKey())) {
                analyzer.setPattern(field.getValue().asText());
            } else if ("flags".equals(field.getKey()) && !"null".equals(field.getValue().asText())) {
                List<RegexFlags> regexFlags = Arrays.stream(field.getValue().asText().split(DELIMITER))
                    .map(RegexFlags::fromString).collect(Collectors.toList());
                analyzer.setFlags(regexFlags);
            } else if ("lowercase".equals(field.getKey())){
                analyzer.setLowerCaseTerms(field.getValue().asBoolean());
            } else if ("stopwords".equals(field.getKey())) {
                List<String> stopWords = new ArrayList<>();
                field.getValue().forEach(
                    jsonNode -> stopWords.add(jsonNode.asText())
                );
                analyzer.setStopwords(stopWords);
            }
        }
        return analyzer;
    }
}
