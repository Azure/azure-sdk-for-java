/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.services.core.storage.utils.implementation;

/**
 * RESERVED FOR INTERNAL USE. A class which holds the current context of a listing
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
