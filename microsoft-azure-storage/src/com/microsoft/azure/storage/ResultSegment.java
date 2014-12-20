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
package com.microsoft.azure.storage;

import java.util.ArrayList;

/**
 * Represents a segment of results and contains continuation and pagination information.
 * 
 * @param <T>
 *            The type of the result that the segment contains.
 */
public class ResultSegment<T> {
    /**
     * Stores the continuation token used to retrieve the next segment of results.
     */
    private final ResultContinuation continuationToken;

    /**
     * Holds the number of the results.
     */
    private final int length;

    /**
     * Holds the size of the requested page.
     */
    private final Integer pageSize;

    /**
     * Holds the ArrayList of results.
     */
    private final ArrayList<T> results;

    /**
     * Reserved for internal use. Creates an instance of the <code>ResultSegment</code> class.
     * 
     * @param results
     *            An <code>ArrayList</code> object that represents the results for the segment.
     * @param pageSize
     *            The number of elements in a page of results.
     * @param token
     *            A {@link ResultContinuation} object that represents the continuation token.
     */
    public ResultSegment(final ArrayList<T> results, final Integer pageSize, final ResultContinuation token) {
        this.results = results;
        this.length = results.size();
        this.pageSize = pageSize;
        this.continuationToken = token;
    }

    /**
     * Returns the continuation token for the result segment.
     * 
     * @return A {@link ResultContinuation} object that represents the continuation token.
     */
    public ResultContinuation getContinuationToken() {
        return this.continuationToken;
    }

    /**
     * Returns a value that indicates whether there are more results available from the server.
     * 
     * @return <code>true</code> if there are more results available from the server; otherwise, <code>false</code>.
     */
    public boolean getHasMoreResults() {
        return this.continuationToken != null;
    }

    /**
     * Returns a value that indicates whether the page has more results.
     * 
     * @return <code>true</code> if the page has more results; otherwise, <code>false</code>.
     */
    public boolean getIsPageComplete() {
        return (new Integer(this.length)).equals(this.pageSize);
    }

    /**
     * Returns the number of results in the segment.
     * 
     * @return The actual number of the results in the segment.
     */
    public int getLength() {
        return this.length;
    }

    /**
     * Returns the size of the requested page.
     * 
     * @return The size of the requested page.
     */
    public Integer getPageSize() {
        return this.pageSize;
    }

    /**
     * Returns the count of remaining results needed to fulfill the requested page size.
     * 
     * @return The count of remaining results needed to fulfill the requested page size.
     */
    public int getRemainingPageResults() {
        return this.pageSize - this.length;
    }

    /**
     * Returns an enumerable set of results from the service.
     * 
     * @return The results retrieved from the service.
     */
    public ArrayList<T> getResults() {
        return this.results;
    }
}
