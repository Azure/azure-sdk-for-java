// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.azure.core.annotation.Fluent;

import java.util.List;

/**
 * Options for retrieving a document from an Azure AI Search index.
 * These options configure request headers and query parameters, not a request body.
 */
@Fluent
public class GetDocumentOptions {
    private String querySourceAuthorization;
    private Boolean enableElevatedRead;
    private List<String> selectedFields;

    /**
     * Creates an instance of {@link GetDocumentOptions}.
     */
    public GetDocumentOptions() {
    }

    /**
     * Gets the token identifying the user for whom the query is executed.
     * The token is sent in the {@code x-ms-query-source-authorization} header to enforce document permissions.
     *
     * @return the authorization token, or null if not set.
     */
    public String getQuerySourceAuthorization() {
        return querySourceAuthorization;
    }

    /**
     * Sets the token identifying the user for whom the query is executed.
     * The token is sent in the {@code x-ms-query-source-authorization} header to enforce document permissions.
     *
     * @param querySourceAuthorization the authorization token, or null to leave the header unset.
     * @return the updated GetDocumentOptions object.
     */
    public GetDocumentOptions setQuerySourceAuthorization(String querySourceAuthorization) {
        this.querySourceAuthorization = querySourceAuthorization;
        return this;
    }

    /**
     * Gets whether elevated read is enabled to bypass document-level permission checks.
     *
     * @return whether elevated read is enabled, or null to use the service default.
     */
    public Boolean isEnableElevatedRead() {
        return enableElevatedRead;
    }

    /**
     * Sets whether elevated read is enabled to bypass document-level permission checks.
     * This value is sent in the {@code x-ms-enable-elevated-read} header.
     *
     * @param enableElevatedRead whether elevated read is enabled, or null to use the service default.
     * @return the updated GetDocumentOptions object.
     */
    public GetDocumentOptions setEnableElevatedRead(Boolean enableElevatedRead) {
        this.enableElevatedRead = enableElevatedRead;
        return this;
    }

    /**
     * Gets the fields to retrieve for the document.
     *
     * @return the selected fields, or null to leave field selection to the service.
     */
    public List<String> getSelectedFields() {
        return selectedFields;
    }

    /**
     * Sets the fields to retrieve for the document using the {@code $select} query parameter.
     * Fields not selected will be missing from the returned document.
     *
     * @param selectedFields the fields to retrieve, or null to leave field selection to the service.
     * @return the updated GetDocumentOptions object.
     */
    public GetDocumentOptions setSelectedFields(List<String> selectedFields) {
        this.selectedFields = selectedFields;
        return this;
    }
}
