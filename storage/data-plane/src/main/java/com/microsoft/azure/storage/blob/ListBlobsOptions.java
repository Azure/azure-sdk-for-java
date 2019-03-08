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
    public static final ListBlobsOptions DEFAULT = new ListBlobsOptions();

    private BlobListingDetails details;

    private String prefix;

    private Integer maxResults;

    public ListBlobsOptions() {
        this.details = BlobListingDetails.NONE;
    }

    /**
     * {@link BlobListingDetails}
     */
    public BlobListingDetails details() {
        return details;
    }

    /**
     * {@link BlobListingDetails}
     */
    public ListBlobsOptions withDetails(BlobListingDetails details) {
        this.details = details;
        return this;
    }

    /**
     * Filters the results to return only blobs whose names begin with the specified prefix. May be null to return
     * all blobs.
     */
    public String prefix() {
        return prefix;
    }

    /**
     * Filters the results to return only blobs whose names begin with the specified prefix. May be null to return
     * all blobs.
     */
    public ListBlobsOptions withPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * Specifies the maximum number of blobs to return, including all BlobPrefix elements. If the request does not
     * specify maxResults or specifies a value greater than 5,000, the server will return up to 5,000 items.
     */
    public Integer maxResults() {
        return maxResults;
    }

    /**
     * Specifies the maximum number of blobs to return, including all BlobPrefix elements. If the request does not
     * specify maxResults or specifies a value greater than 5,000, the server will return up to 5,000 items.
     */
    public ListBlobsOptions withMaxResults(Integer maxResults) {
        if (maxResults != null && maxResults <= 0) {
            throw new IllegalArgumentException("MaxResults must be greater than 0.");
        }
        this.maxResults = maxResults;
        return this;
    }


}
