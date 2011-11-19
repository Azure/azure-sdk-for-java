package com.microsoft.windowsazure.services.core.storage.utils.implementation;

/**
 * RESERVED FOR INTERNAL USE. A class which holds the current context of a listing
 * 
 * Copyright (c)2011 Microsoft. All rights reserved.
 */
public class ListingContext {

    /**
     * The Marker value.
     */
    private String marker;

    /**
     * The MaxResults value.
     */
    private Integer maxResults;

    /**
     * The Prefix value.
     */
    private String prefix;

    /**
     * Initializes a new instance of the ListingContext class.
     * 
     * @param prefix
     *            the listing prefix to use
     * @param maxResults
     *            the maximum number of results to retrieve.
     */
    public ListingContext(final String prefix, final Integer maxResults) {
        this.setPrefix(prefix);
        this.setMaxResults(maxResults);
        this.setMarker(null);
    }

    /**
     * @return the marker
     */
    public final String getMarker() {
        return this.marker;
    }

    /**
     * @return the maxResults
     */
    public final Integer getMaxResults() {
        return this.maxResults;
    }

    /**
     * @return the prefix
     */
    public final String getPrefix() {
        return this.prefix;
    }

    /**
     * @param marker
     *            the marker to set
     */
    public final void setMarker(final String marker) {
        this.marker = marker;
    }

    /**
     * @param maxResults
     *            the maxResults to set
     */
    protected final void setMaxResults(final Integer maxResults) {
        this.maxResults = maxResults;
    }

    /**
     * @param prefix
     *            the prefix to set
     */
    public final void setPrefix(final String prefix) {
        this.prefix = prefix;
    }
}
