/*
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
package com.microsoft.azure.storage.blob;

/**
 * Defines options available to configure the behavior of a call to listBlobsFlatSegment on a {@link ContainerURL}
 * object. See the constructor for details on each of the options.
 */
public final class ListBlobsOptions {

    /**
     * An object representing the default options: no details, prefix, or delimiter. Uses the server default for
     * maxResults.
     */
    public static final ListBlobsOptions DEFAULT = new ListBlobsOptions(
            new BlobListingDetails(false, false, false, false),
            null, null);

    private final BlobListingDetails details;

    private final String prefix;

    private final Integer maxResults;

    /**
     * An object filled with the specified values.
     *
     * @param details
     *      {@link BlobListingDetails}
     * @param prefix
     *      Filters the results to return only blobs whose names begin with the specified prefix.
     * @param maxResults
     *      Specifies the maximum number of blobs to return, including all BlobPrefix elements. If the request does not
     *      specify maxResults or specifies a value greater than 5,000, the server will return up to 5,000 items.
     */
    public ListBlobsOptions(BlobListingDetails details, String prefix, Integer maxResults) {
        if (maxResults != null && maxResults <= 0) {
            throw new IllegalArgumentException("MaxResults must be greater than 0.");
        }
        this.details = details == null ? BlobListingDetails.NONE : details;
        this.prefix = prefix;
        this.maxResults = maxResults;
    }

    /**
     * @return
     *      {@link BlobListingDetails}
     */
    public BlobListingDetails getDetails() {
        return this.details;
    }

    /**
     * @return
     *      Filters the results to return only blobs whose names begin with the specified
     *      prefix.
     */
    public String getPrefix() {
        return this.prefix;
    }

    /**
     * @return
     *      Specifies the maximum number of blobs to return, including all BlobPrefix elements. If the request does
     *      not specify maxresults or specifies a value greater than 5,000, the server will return up to 5,000
     *      items.
     */
    public Integer getMaxResults() {
        return this.maxResults;
    }
}
