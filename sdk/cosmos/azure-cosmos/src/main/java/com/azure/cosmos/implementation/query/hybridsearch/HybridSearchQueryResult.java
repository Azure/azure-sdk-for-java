// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query.hybridsearch;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.Document;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;

public class HybridSearchQueryResult<T> extends Document {
    public String rid;
    public List<Double> componentScores;
    public Document payload;
    public Double score;

    public HybridSearchQueryResult(String jsonString) {
        super(jsonString);
        // Initialize rid
        this.rid = super.getResourceId();
        // Initialize componentScores
        final Object outerObject = super.get(Constants.Properties.PAYLOAD);
        if (outerObject instanceof ObjectNode) {
            ObjectNode outerObjectNode = (ObjectNode) outerObject;
            // Initialize componentScores
            JsonNode componentScoresNode = outerObjectNode.get(Constants.Properties.COMPONENT_SCORES);
            if (componentScoresNode != null && componentScoresNode.isArray()) {
                this.componentScores = new ArrayList<>();
                for (JsonNode scoreNode : componentScoresNode) {
                    if (scoreNode != null && scoreNode.isNumber()) {
                        this.componentScores.add(scoreNode.asDouble());
                    }
                }
            }
            // Initialize payload
            JsonNode innerPayloadNode = outerObjectNode.get(Constants.Properties.PAYLOAD);
            if (innerPayloadNode != null && innerPayloadNode.isObject()) {
                this.payload = new Document(innerPayloadNode.toString());
            }
        }

    }

    public String getRid() {
        return this.rid;
    }

    public void setRid(String rid) {
        super.setResourceId(rid);
    }

    public List<Double> getComponentScores() {
        return this.componentScores;
    }

    public void setComponentScores(List<Double> componentScores) {
        this.componentScores = componentScores;
    }

    public Document getPayload() {
        return this.payload;
    }

    public void setPayload(Document payload) {
        this.payload = payload;
    }

    public Double getScore() {
        return this.score;
    }

    public void setScore(Double score) {
        this.score = score;
    }
}
