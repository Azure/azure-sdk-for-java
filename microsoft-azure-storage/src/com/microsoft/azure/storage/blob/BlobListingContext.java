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
package com.microsoft.azure.storage.blob;

import java.util.EnumSet;

import com.microsoft.azure.storage.core.ListingContext;

/**
 * RESERVED FOR INTERNAL USE. Provides the listing context for blobs including the delimiter and listingdetails
 */
final class BlobListingContext extends ListingContext {

    /**
     * Gets or sets the delimiter for a blob listing operation. The delimiter parameter enables the caller to traverse
     * the blob namespace by using a user-configured delimiter. Using this parameter, it is possible to traverse a
     * virtual hierarchy of blobs as though it were a file system.
     */
    private String delimiter;

    /**
     * Gets or sets the details for the listing operation, which indicates the types of data to include in the response.
     * The include parameter specifies that the response should include one or more of the following subsets: snapshots,
     * metadata, uncommitted blobs.
     */
    private EnumSet<BlobListingDetails> listingDetails;

    /**
     * Initializes a new instance of the BlobListingContext class
     * 
     * @param prefix
     *            the prefix to use.
     * @param maxResults
     *            the maximum results to download.
     * @param delimiter
     *            the delimiter to use
     * @param listingDetails
     *            the BlobListingDetails to use.
     */
    BlobListingContext(final String prefix, final Integer maxResults, final String delimiter,
            final EnumSet<BlobListingDetails> listingDetails) {
        super(prefix, maxResults);
        this.setDelimiter(delimiter);
        this.setListingDetails(listingDetails);
    }

    /**
     * @return the delimiter
     */
    String getDelimiter() {
        return this.delimiter;
    }

    /**
     * @return the listingDetails
     */
    EnumSet<BlobListingDetails> getListingDetails() {
        return this.listingDetails;
    }

    /**
     * @param delimiter
     *            the delimiter to set
     */
    void setDelimiter(final String delimiter) {
        this.delimiter = delimiter;
    }

    /**
     * @param listingDetails
     *            the listingDetails to set
     */
    void setListingDetails(final EnumSet<BlobListingDetails> listingDetails) {
        this.listingDetails = listingDetails;
    }
}
