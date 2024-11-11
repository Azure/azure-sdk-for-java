// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query.hybridsearch;

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
    }

    public String getRid() {
        this.rid = this.rid != null ? this.rid : (this.rid = super.getString("_rid"));
        super.setId(this.rid);
        return this.rid;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }

    public List<Double> getComponentScores() {
        if (this.componentScores == null) {
            final Object outerObject = super.get("payload");

            if (outerObject instanceof ObjectNode) {
                JsonNode outerJsonNode = (ObjectNode) outerObject;
                JsonNode componentScoresNode = outerJsonNode.get("componentScores");

                if (componentScoresNode != null && componentScoresNode.isArray()) {
                    this.componentScores = new ArrayList<>();
                    for (JsonNode scoreNode : componentScoresNode) {
                        if (scoreNode != null && scoreNode.isNumber()) {
                            this.componentScores.add(scoreNode.asDouble());
                        }
                    }
                }
            }
        }
        return this.componentScores;
    }

    public void setComponentScores(List<Double> componentScores) {
        this.componentScores = componentScores;
    }

    public Document getPayload() {
        if (this.payload != null) {
            return this.payload;
        }
        final Object outerObject = super.get("payload");
        if (outerObject instanceof ObjectNode) {
            ObjectNode outerObjectNode = (ObjectNode) outerObject;
            JsonNode innerObjectNode = outerObjectNode.get("payload");
            if (innerObjectNode != null && innerObjectNode.isObject()) {
                this.payload = new Document(innerObjectNode.toString());
            }
        }
        return payload;
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
