// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.models;

/**
 * Configuration for how semantic search captions search results.
 */
public final class QueryCaption {
    private QueryCaptionType captionType;
    private Boolean highlightEnabled;

    /**
     * Creates a new instance of {@link QueryCaption}.
     */
    public QueryCaption() {
    }

    /**
     * Gets the type of captions to generate.
     *
     * @return The type of captions to generate.
     */
    public QueryCaptionType getCaptionType() {
        return captionType;
    }

    /**
     * Sets the type of captions to generate.
     *
     * @param captionType The type of captions to generate.
     * @return The QueryCaption object itself.
     */
    public QueryCaption setCaptionType(QueryCaptionType captionType) {
        this.captionType = captionType;
        return this;
    }

    /**
     * Whether to highlight the captioned text in the result.
     *
     * @return Whether to highlight the captioned text in the result.
     */
    public Boolean isHighlightEnabled() {
        return highlightEnabled;
    }

    /**
     * Sets whether to highlight the captioned text in the result.
     *
     * @param highlightEnabled Whether to highlight the captioned text in the result.
     * @return The QueryCaption object itself.
     */
    public QueryCaption setHighlightEnabled(Boolean highlightEnabled) {
        this.highlightEnabled = highlightEnabled;
        return this;
    }
}
