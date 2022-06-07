// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.azure.core.annotation.Fluent;
import com.azure.search.documents.implementation.converters.IndexActionHelper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.Map;

/**
 * Represents an index action that operates on a document.
 */
@Fluent
public final class IndexAction<T> {
    /*
     * The document on which the action will be performed.
     */
    @JsonUnwrapped
    private T document;

    @JsonIgnore
    private Map<String, Object> properties;

    /*
     * The operation to perform on a document in an indexing batch. Possible
     * values include: 'Upload', 'Merge', 'MergeOrUpload', 'Delete'
     */
    @JsonProperty(value = "@search.action")
    private IndexActionType actionType;

    static {
        IndexActionHelper.setAccessor(new IndexActionHelper.IndexActionAccessor() {
            @Override
            public <U> void setProperties(IndexAction<U> indexAction, Map<String, Object> properties) {
                indexAction.setProperties(properties);
            }

            @Override
            public <U> Map<String, Object> getProperties(IndexAction<U> indexAction) {
                return indexAction.getProperties();
            }
        });
    }
    /**
     * Get the document on which the action will be performed; Fields other than the key are ignored for delete actions.
     *
     * @return the document value.
     */
    @SuppressWarnings("unchecked")
    public T getDocument() {
        if (this.properties != null) {
            return (T) this.properties;
        }
        return this.document;
    }

    /**
     * Get the document on which the action will be performed; Fields other than the key are ignored for delete actions.
     *
     * @param document the document value to set.
     * @return the IndexAction object itself.
     */
    @SuppressWarnings("unchecked")
    public IndexAction<T> setDocument(T document) {
        if (document instanceof Map) {
            this.properties = (Map<String, Object>) document;
            this.document = null;
        } else {
            this.document = document;
            this.properties = null;
        }
        return this;
    }

    /**
     * Get the actionType property: The operation to perform on a document in
     * an indexing batch. Possible values include: 'Upload', 'Merge',
     * 'MergeOrUpload', 'Delete'.
     *
     * @return the actionType value.
     */
    public IndexActionType getActionType() {
        return this.actionType;
    }

    /**
     * Set the actionType property: The operation to perform on a document in
     * an indexing batch. Possible values include: 'Upload', 'Merge',
     * 'MergeOrUpload', 'Delete'.
     *
     * @param actionType the actionType value to set.
     * @return the IndexAction object itself.
     */
    public IndexAction<T> setActionType(IndexActionType actionType) {
        this.actionType = actionType;
        return this;
    }

    /**
     * The private setter to set the properties property
     * via {@link IndexActionHelper.IndexActionAccessor}.
     *
     * @param properties The properties.
     */
    private void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    /**
     * The private getter to get the properties property
     * via {@link IndexActionHelper.IndexActionAccessor}.
     * @return The properties
     */
    private Map<String, Object> getProperties() {
       return this.properties;
    }
}
