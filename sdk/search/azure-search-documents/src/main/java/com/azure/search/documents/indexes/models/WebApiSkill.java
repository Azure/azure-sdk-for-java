// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * A skill that can call a Web API endpoint, allowing you to extend a skillset
 * by having it call your custom code.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@odata.type")
@JsonTypeName("#Microsoft.Skills.Custom.WebApiSkill")
@Fluent
public final class WebApiSkill extends SearchIndexerSkill {
    /*
     * The url for the Web API.
     */
    @JsonProperty(value = "uri", required = true)
    private String uri;

    /*
     * The headers required to make the http request.
     */
    @JsonProperty(value = "httpHeaders")
    private Map<String, String> httpHeaders;

    /*
     * The method for the http request.
     */
    @JsonProperty(value = "httpMethod")
    private String httpMethod;

    /*
     * The desired timeout for the request. Default is 30 seconds.
     */
    @JsonProperty(value = "timeout")
    private Duration timeout;

    /*
     * The desired batch size which indicates number of documents.
     */
    @JsonProperty(value = "batchSize")
    private Integer batchSize;

    /*
     * If set, the number of parallel calls that can be made to the Web API.
     */
    @JsonProperty(value = "degreeOfParallelism")
    private Integer degreeOfParallelism;

    /**
     * Constructor of {@link SearchIndexerSkill}.
     *
     * @param inputs Inputs of the skills could be a column in the source data set, or the
     * output of an upstream skill.
     * @param outputs The output of a skill is either a field in a search index, or a value
     * that can be consumed as an input by another skill.
     * @param uri The url for the Web API.
     */
    public WebApiSkill(List<InputFieldMappingEntry> inputs, List<OutputFieldMappingEntry> outputs, String uri) {
        super(inputs, outputs);
        this.uri = uri;
    }

    /**
     * Get the uri property: The url for the Web API.
     *
     * @return the uri value.
     */
    public String getUri() {
        return this.uri;
    }

    /**
     * Get the httpHeaders property: The headers required to make the http
     * request.
     *
     * @return the httpHeaders value.
     */
    public Map<String, String> getHttpHeaders() {
        return this.httpHeaders;
    }

    /**
     * Set the httpHeaders property: The headers required to make the http
     * request.
     *
     * @param httpHeaders the httpHeaders value to set.
     * @return the WebApiSkill object itself.
     */
    public WebApiSkill setHttpHeaders(Map<String, String> httpHeaders) {
        this.httpHeaders = httpHeaders;
        return this;
    }

    /**
     * Get the httpMethod property: The method for the http request.
     *
     * @return the httpMethod value.
     */
    public String getHttpMethod() {
        return this.httpMethod;
    }

    /**
     * Set the httpMethod property: The method for the http request.
     *
     * @param httpMethod the httpMethod value to set.
     * @return the WebApiSkill object itself.
     */
    public WebApiSkill setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
        return this;
    }

    /**
     * Get the timeout property: The desired timeout for the request. Default
     * is 30 seconds.
     *
     * @return the timeout value.
     */
    public Duration getTimeout() {
        return this.timeout;
    }

    /**
     * Set the timeout property: The desired timeout for the request. Default
     * is 30 seconds.
     *
     * @param timeout the timeout value to set.
     * @return the WebApiSkill object itself.
     */
    public WebApiSkill setTimeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * Get the batchSize property: The desired batch size which indicates
     * number of documents.
     *
     * @return the batchSize value.
     */
    public Integer getBatchSize() {
        return this.batchSize;
    }

    /**
     * Set the batchSize property: The desired batch size which indicates
     * number of documents.
     *
     * @param batchSize the batchSize value to set.
     * @return the WebApiSkill object itself.
     */
    public WebApiSkill setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    /**
     * Get the degreeOfParallelism property: If set, the number of parallel
     * calls that can be made to the Web API.
     *
     * @return the degreeOfParallelism value.
     */
    public Integer getDegreeOfParallelism() {
        return this.degreeOfParallelism;
    }

    /**
     * Set the degreeOfParallelism property: If set, the number of parallel
     * calls that can be made to the Web API.
     *
     * @param degreeOfParallelism the degreeOfParallelism value to set.
     * @return the WebApiSkill object itself.
     */
    public WebApiSkill setDegreeOfParallelism(Integer degreeOfParallelism) {
        this.degreeOfParallelism = degreeOfParallelism;
        return this;
    }
}
