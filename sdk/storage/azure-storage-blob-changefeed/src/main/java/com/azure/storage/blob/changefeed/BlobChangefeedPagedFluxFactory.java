// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.storage.common.implementation.StorageImplUtils;

import java.time.OffsetDateTime;

/**
 * Factory class for {@link BlobChangefeedPagedFlux}.
 */
class BlobChangefeedPagedFluxFactory {

    private final ChangefeedFactory changefeedFactory;

    /**
     * Creates a BlobChangefeedPagedFluxFactory with the designated factories.
     */
    BlobChangefeedPagedFluxFactory(ChangefeedFactory changefeedFactory) {
        StorageImplUtils.assertNotNull("changefeedFactory", changefeedFactory);
        this.changefeedFactory = changefeedFactory;
    }

    /**
     * Gets a new instance of {@link BlobChangefeedPagedFlux}.
     *
     * @param startTime The {@link OffsetDateTime start time}.
     * @param endTime The {@link OffsetDateTime end time}.
     */
    BlobChangefeedPagedFlux getBlobChangefeedPagedFlux(OffsetDateTime startTime, OffsetDateTime endTime) {
        /* Start and end time are validated in the getChangefeed method. */
        Changefeed changefeed = changefeedFactory.getChangefeed(startTime, endTime);
        return new BlobChangefeedPagedFlux(changefeed);
    }

    /**
     * Gets an new instance of {@link BlobChangefeedPagedFlux}.
     *
     * @param cursor The cursor.
     */
    BlobChangefeedPagedFlux getBlobChangefeedPagedFlux(String cursor) {
        /* Cursor is validated in the getChangefeed method. */
        Changefeed changefeed = changefeedFactory.getChangefeed(cursor);
        return new BlobChangefeedPagedFlux(changefeed);
    }
}
