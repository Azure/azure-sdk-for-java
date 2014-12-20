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
package com.microsoft.azure.storage.core;

import java.util.ArrayList;

/**
 * RESERVED FOR INTERNAL USE. A class used to parse a list of objects.
 */
public class ListResponse<T> {

    public static final String ENUMERATION_RESULTS = "EnumerationResults";

    /**
     * Holds the ArrayList<T> of results.
     */
    protected ArrayList<T> results = new ArrayList<T>();

    /**
     * Stores the marker.
     */
    protected String marker;

    /**
     * Stores the max results.
     */
    protected Integer maxResults;

    /**
     * Stores the next marker.
     */
    protected String nextMarker;

    /**
     * Stores the prefix.
     */
    protected String prefix;

    /**
     * Gets the ArrayList<T> of the results.
     * 
     * @return the ArrayList<T> of results
     */
    public ArrayList<T> getResults() {
        return this.results;
    }

    /**
     * Gets the marker.
     * 
     * @return the marker
     */
    public String getMarker() {
        return this.marker;
    }

    /**
     * Gets the max results.
     * 
     * @return the max results
     */
    public Integer getMaxResults() {
        return this.maxResults;
    }

    /**
     * Gets the next marker.
     * 
     * @return the next marker
     */
    public String getNextMarker() {
        return this.nextMarker;
    }

    /**
     * Gets the prefix.
     * 
     * @return the prefix
     */
    public String getPrefix() {
        return this.prefix;
    }

    /**
     * Sets the ArrayList<T> of the results
     * 
     * @param results
     *            the results to set
     */
    public void setResults(ArrayList<T> results) {
        this.results = results;
    }

    /**
     * Sets the marker.
     * 
     * @param marker
     *            the marker to set
     */
    public void setMarker(String marker) {
        this.marker = marker;
    }

    /**
     * Sets the max results.
     * 
     * @param maxResults
     *            the maxResults to set
     */
    public void setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
    }

    /**
     * Sets the next marker.
     * 
     * @param nextMarker
     *            the nextMarker to set
     */
    public void setNextMarker(String nextMarker) {
        this.nextMarker = nextMarker;
    }

    /**
     * Sets the prefix.
     * 
     * @param prefix
     *            the prefix to set
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
