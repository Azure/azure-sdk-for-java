// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.ai.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Internal parser for semantic rerank responses.
 * <p>
 * This class lives in the {@code models} package so it can access package-private setters
 * on {@link SemanticRerankResult} and {@link SemanticRerankScore}.
 * <p>
 * While this class is public, it is not part of our published public APIs.
 * This is meant to be internally used only by our SDK.
 */
public final class InferenceResponseParser {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private InferenceResponseParser() {
    }

    /**
     * Parses the JSON response body into a {@link SemanticRerankResult}.
     *
     * @param responseBody the JSON response body string.
     * @return the parsed {@link SemanticRerankResult}.
     * @throws IOException if parsing fails.
     */
    public static SemanticRerankResult parseRerankResponse(String responseBody) throws IOException {
        JsonNode rootNode = OBJECT_MAPPER.readTree(responseBody);
        SemanticRerankResult result = new SemanticRerankResult();

        // Parse scores
        if (rootNode.has("Scores")) {
            JsonNode scoresNode = rootNode.get("Scores");
            List<SemanticRerankScore> scores = new ArrayList<>();

            if (scoresNode.isArray()) {
                for (JsonNode scoreNode : scoresNode) {
                    SemanticRerankScore score = new SemanticRerankScore();
                    JsonNode indexNode = scoreNode.get("index");
                    JsonNode scoreValNode = scoreNode.get("score");
                    if (indexNode != null) {
                        score.setIndex(indexNode.asInt());
                    }
                    if (scoreValNode != null) {
                        score.setScore(scoreValNode.asDouble());
                    }
                    if (scoreNode.has("document")) {
                        score.setDocument(scoreNode.get("document").asText());
                    }
                    scores.add(score);
                }
            }
            result.setScores(scores);
        }

        // Parse latency
        if (rootNode.has("latency")) {
            Map<String, Object> latency = new HashMap<>();
            rootNode.get("latency").fields().forEachRemaining(
                entry -> latency.put(entry.getKey(), entry.getValue().asDouble()));
            result.setLatency(latency);
        }

        // Parse token usage
        if (rootNode.has("token_usage")) {
            Map<String, Object> tokenUsage = new HashMap<>();
            rootNode.get("token_usage").fields().forEachRemaining(
                entry -> tokenUsage.put(entry.getKey(), entry.getValue().asInt()));
            result.setTokenUsage(tokenUsage);
        }

        return result;
    }
}
