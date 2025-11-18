// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.azure.core.annotation.Fluent;

import java.util.Arrays;
import java.util.List;

/**
 * Additional parameters for getDocument operation.
 * 
 * @param <T> The type of the document to retrieve.
 */
@Fluent
public final class GetDocumentOptions<T> {
    /*
     * The key of the document to retrieve.
     */
    private final String key;

    /*
     * The model class converts search result.
     */
    private final Class<T> modelClass;

    /*
     * The list of fields to retrieve. If unspecified, all fields marked as retrievable in the schema are included.
     */
    private List<String> selectedFields;

    /*
     * A value that specifies whether to enable elevated read for the document retrieval.
     * Elevated read allows the request to read the latest committed index changes and bypass standard ACL filtering.
     */
    private Boolean enableElevatedRead;

    /**
     * Creates an instance of {@link GetDocumentOptions} with required parameters.
     *
     * @param key The key of the document to retrieve.
     * @param modelClass The model class converts search result.
     */
    public GetDocumentOptions(String key, Class<T> modelClass) {
        this.key = key;
        this.modelClass = modelClass;
    }

    /**
     * Get the key property: The key of the document to retrieve.
     *
     * @return the key value.
     */
    public String getKey() {
        return this.key;
    }

    /**
     * Get the modelClass property: The model class converts search result.
     *
     * @return the modelClass value.
     */
    public Class<T> getModelClass() {
        return this.modelClass;
    }

    /**
     * Get the selectedFields property: The list of fields to retrieve. If unspecified, all fields marked as 
     * retrievable in the schema are included.
     *
     * @return the selectedFields value.
     */
    public List<String> getSelectedFields() {
        return this.selectedFields;
    }

    /**
     * Set the selectedFields property: The list of fields to retrieve. If unspecified, all fields marked as 
     * retrievable in the schema are included.
     *
     * @param selectedFields the selectedFields value to set.
     * @return the GetDocumentOptions object itself.
     */
    public GetDocumentOptions<T> setSelectedFields(String... selectedFields) {
        this.selectedFields = (selectedFields == null) ? null : Arrays.asList(selectedFields);
        return this;
    }

    /**
     * Set the selectedFields property: The list of fields to retrieve. If unspecified, all fields marked as 
     * retrievable in the schema are included.
     *
     * @param selectedFields the selectedFields value to set.
     * @return the GetDocumentOptions object itself.
     */
    public GetDocumentOptions<T> setSelectedFields(List<String> selectedFields) {
        this.selectedFields = selectedFields;
        return this;
    }

    /**
     * Get the enableElevatedRead property: A value that specifies whether to enable elevated read for the document 
     * retrieval. Elevated read allows the request to read the latest committed index changes and bypass standard ACL filtering.
     *
     * @return the enableElevatedRead value.
     */
    public Boolean isElevatedReadEnabled() {
        return this.enableElevatedRead;
    }

    /**
     * Set the enableElevatedRead property: A value that specifies whether to enable elevated read for the document 
     * retrieval. Elevated read allows the request to read the latest committed index changes and bypass standard ACL filtering.
     *
     * @param enableElevatedRead the enableElevatedRead value to set.
     * @return the GetDocumentOptions object itself.
     */
    public GetDocumentOptions<T> setElevatedReadEnabled(Boolean enableElevatedRead) {
        this.enableElevatedRead = enableElevatedRead;
        return this;
    }
}
