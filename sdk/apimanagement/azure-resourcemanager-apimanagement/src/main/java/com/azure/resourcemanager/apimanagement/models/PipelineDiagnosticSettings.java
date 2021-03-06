// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.apimanagement.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Diagnostic settings for incoming/outgoing HTTP messages to the Gateway. */
@Fluent
public final class PipelineDiagnosticSettings {
    @JsonIgnore private final ClientLogger logger = new ClientLogger(PipelineDiagnosticSettings.class);

    /*
     * Diagnostic settings for request.
     */
    @JsonProperty(value = "request")
    private HttpMessageDiagnostic request;

    /*
     * Diagnostic settings for response.
     */
    @JsonProperty(value = "response")
    private HttpMessageDiagnostic response;

    /**
     * Get the request property: Diagnostic settings for request.
     *
     * @return the request value.
     */
    public HttpMessageDiagnostic request() {
        return this.request;
    }

    /**
     * Set the request property: Diagnostic settings for request.
     *
     * @param request the request value to set.
     * @return the PipelineDiagnosticSettings object itself.
     */
    public PipelineDiagnosticSettings withRequest(HttpMessageDiagnostic request) {
        this.request = request;
        return this;
    }

    /**
     * Get the response property: Diagnostic settings for response.
     *
     * @return the response value.
     */
    public HttpMessageDiagnostic response() {
        return this.response;
    }

    /**
     * Set the response property: Diagnostic settings for response.
     *
     * @param response the response value to set.
     * @return the PipelineDiagnosticSettings object itself.
     */
    public PipelineDiagnosticSettings withResponse(HttpMessageDiagnostic response) {
        this.response = response;
        return this;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (request() != null) {
            request().validate();
        }
        if (response() != null) {
            response().validate();
        }
    }
}
