// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.InputFieldMappingEntry;
import com.azure.search.documents.models.OutputFieldMappingEntry;
import com.azure.search.documents.models.WebApiSkill;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.WebApiSkill} and {@link WebApiSkill}.
 */
public final class WebApiSkillConverter {
    private static final ClientLogger LOGGER = new ClientLogger(WebApiSkillConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.WebApiSkill} to {@link WebApiSkill}.
     */
    public static WebApiSkill map(com.azure.search.documents.implementation.models.WebApiSkill obj) {
        if (obj == null) {
            return null;
        }
        WebApiSkill webApiSkill = new WebApiSkill();

        if (obj.getOutputs() != null) {
            List<OutputFieldMappingEntry> _outputs =
                obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());
            webApiSkill.setOutputs(_outputs);
        }

        if (obj.getInputs() != null) {
            List<InputFieldMappingEntry> _inputs =
                obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
            webApiSkill.setInputs(_inputs);
        }

        String _name = obj.getName();
        webApiSkill.setName(_name);

        String _context = obj.getContext();
        webApiSkill.setContext(_context);

        String _description = obj.getDescription();
        webApiSkill.setDescription(_description);

        if (obj.getHttpHeaders() != null) {
            Map<String, String> _httpHeaders =
                obj.getHttpHeaders().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            webApiSkill.setHttpHeaders(_httpHeaders);
        }

        String _httpMethod = obj.getHttpMethod();
        webApiSkill.setHttpMethod(_httpMethod);

        Integer _batchSize = obj.getBatchSize();
        webApiSkill.setBatchSize(_batchSize);

        String _uri = obj.getUri();
        webApiSkill.setUri(_uri);

        Duration _timeout = obj.getTimeout();
        webApiSkill.setTimeout(_timeout);

        Integer _degreeOfParallelism = obj.getDegreeOfParallelism();
        webApiSkill.setDegreeOfParallelism(_degreeOfParallelism);
        return webApiSkill;
    }

    /**
     * Maps from {@link WebApiSkill} to {@link com.azure.search.documents.implementation.models.WebApiSkill}.
     */
    public static com.azure.search.documents.implementation.models.WebApiSkill map(WebApiSkill obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.WebApiSkill webApiSkill =
            new com.azure.search.documents.implementation.models.WebApiSkill();

        if (obj.getOutputs() != null) {
            List<com.azure.search.documents.implementation.models.OutputFieldMappingEntry> _outputs =
                obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());
            webApiSkill.setOutputs(_outputs);
        }

        if (obj.getInputs() != null) {
            List<com.azure.search.documents.implementation.models.InputFieldMappingEntry> _inputs =
                obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
            webApiSkill.setInputs(_inputs);
        }

        String _name = obj.getName();
        webApiSkill.setName(_name);

        String _context = obj.getContext();
        webApiSkill.setContext(_context);

        String _description = obj.getDescription();
        webApiSkill.setDescription(_description);

        if (obj.getHttpHeaders() != null) {
            Map<String, String> _httpHeaders =
                obj.getHttpHeaders().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                    Map.Entry::getValue));
            webApiSkill.setHttpHeaders(_httpHeaders);
        }

        String _httpMethod = obj.getHttpMethod();
        webApiSkill.setHttpMethod(_httpMethod);

        Integer _batchSize = obj.getBatchSize();
        webApiSkill.setBatchSize(_batchSize);

        String _uri = obj.getUri();
        webApiSkill.setUri(_uri);

        Duration _timeout = obj.getTimeout();
        webApiSkill.setTimeout(_timeout);

        Integer _degreeOfParallelism = obj.getDegreeOfParallelism();
        webApiSkill.setDegreeOfParallelism(_degreeOfParallelism);
        return webApiSkill;
    }
}
