// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.WebApiSkill;

import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.WebApiSkill} and {@link WebApiSkill}.
 */
public final class WebApiSkillConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.WebApiSkill} to {@link WebApiSkill}.
     */
    public static WebApiSkill map(com.azure.search.documents.indexes.implementation.models.WebApiSkill obj) {
        if (obj == null) {
            return null;
        }

        WebApiSkill webApiSkill = new WebApiSkill(obj.getInputs(), obj.getOutputs(), obj.getUri());

        String name = obj.getName();
        webApiSkill.setName(name);

        String context = obj.getContext();
        webApiSkill.setContext(context);

        String description = obj.getDescription();
        webApiSkill.setDescription(description);

        if (obj.getHttpHeaders() != null) {
            Map<String, String> httpHeaders =
                obj.getHttpHeaders().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                    Map.Entry::getValue));
            webApiSkill.setHttpHeaders(httpHeaders);
        }

        String httpMethod = obj.getHttpMethod();
        webApiSkill.setHttpMethod(httpMethod);

        Integer batchSize = obj.getBatchSize();
        webApiSkill.setBatchSize(batchSize);

        Duration timeout = obj.getTimeout();
        webApiSkill.setTimeout(timeout);

        Integer degreeOfParallelism = obj.getDegreeOfParallelism();
        webApiSkill.setDegreeOfParallelism(degreeOfParallelism);
        return webApiSkill;
    }

    /**
     * Maps from {@link WebApiSkill} to {@link com.azure.search.documents.indexes.implementation.models.WebApiSkill}.
     */
    public static com.azure.search.documents.indexes.implementation.models.WebApiSkill map(WebApiSkill obj) {
        if (obj == null) {
            return null;
        }

        com.azure.search.documents.indexes.implementation.models.WebApiSkill webApiSkill =
            new com.azure.search.documents.indexes.implementation.models.WebApiSkill(obj.getInputs(), obj.getOutputs(),
                obj.getUri());

        String name = obj.getName();
        webApiSkill.setName(name);

        String context = obj.getContext();
        webApiSkill.setContext(context);

        String description = obj.getDescription();
        webApiSkill.setDescription(description);

        if (obj.getHttpHeaders() != null) {
            Map<String, String> httpHeaders =
                obj.getHttpHeaders().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                    Map.Entry::getValue));
            webApiSkill.setHttpHeaders(httpHeaders);
        }

        String httpMethod = obj.getHttpMethod();
        webApiSkill.setHttpMethod(httpMethod);

        Integer batchSize = obj.getBatchSize();
        webApiSkill.setBatchSize(batchSize);

        Duration timeout = obj.getTimeout();
        webApiSkill.setTimeout(timeout);

        Integer degreeOfParallelism = obj.getDegreeOfParallelism();
        webApiSkill.setDegreeOfParallelism(degreeOfParallelism);

        return webApiSkill;
    }

    private WebApiSkillConverter() {
    }
}
