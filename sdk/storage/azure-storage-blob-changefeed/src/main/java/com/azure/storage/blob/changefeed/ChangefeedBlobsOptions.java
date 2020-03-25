// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;

/**
 * Defines options available to configure the behavior of a call to getEvents on a {@link
 * BlobChangefeedClient} object. See the constructor for details on each of the options.
 */
@Fluent
public final class ChangefeedBlobsOptions {
    private final ClientLogger logger = new ClientLogger(ChangefeedBlobsOptions.class);

    private Integer maxResultsPerPage;

    /**
     * Constructs an unpopulated {@link ChangefeedBlobsOptions}.
     */
    public ChangefeedBlobsOptions() {
    }

    /**
     * Specifies the maximum number of events to return. If the request does not specify maxResultsPerPage or
     * specifies a value greater than 5,000, we will return up to 5,000 items.
     *
     * @return the number of events that will be returned in a single response
     */
    public Integer getMaxResultsPerPage() {
        if (maxResultsPerPage == null || maxResultsPerPage > 5000) {
            return 5000;
        }
        return maxResultsPerPage;
    }

    /**
     * Specifies the maximum number of events to return. If the request does not specify maxResultsPerPage or
     * specifies a value greater than 5,000, we will return up to 5,000 items.
     *
     * @param maxResultsPerPage The number of events to returned in a single response
     * @return the updated ChangefeedBlobsOptions object
     * @throws IllegalArgumentException If {@code maxResultsPerPage} is less than or equal to {@code 0}.
     */
    public ChangefeedBlobsOptions setMaxResultsPerPage(Integer maxResultsPerPage) {
        if (maxResultsPerPage != null && maxResultsPerPage <= 0) {
            throw logger.logExceptionAsError(new IllegalArgumentException("MaxResultsPerPage must be greater than 0."));
        }
        this.maxResultsPerPage = maxResultsPerPage;
        return this;
    }


}
