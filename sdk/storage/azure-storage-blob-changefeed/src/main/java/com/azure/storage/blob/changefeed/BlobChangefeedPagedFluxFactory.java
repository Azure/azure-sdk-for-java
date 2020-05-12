package com.azure.storage.blob.changefeed;

import com.azure.storage.blob.BlobContainerAsyncClient;

import java.time.OffsetDateTime;

/**
 * Factory class for {@link BlobChangefeedPagedFlux}.
 */
class BlobChangefeedPagedFluxFactory {

    private final ChangefeedFactory changefeedFactory;

    /**
     * Creates a default instance of the BlobChangefeedPagedFluxFactory.
     */
    BlobChangefeedPagedFluxFactory() {
        this.changefeedFactory = new ChangefeedFactory();
    }

    /**
     * Creates a BlobChangefeedPagedFluxFactory with the designated factories.
     */
    BlobChangefeedPagedFluxFactory(ChangefeedFactory changefeedFactory) {
        this.changefeedFactory = changefeedFactory;
    }

    /**
     * Gets a new instance of {@link BlobChangefeedPagedFlux}.
     *
     * @param client The {@link BlobContainerAsyncClient changefeed client}.
     * @param startTime The {@link OffsetDateTime start time}.
     * @param endTime The {@link OffsetDateTime end time}.
     */
    BlobChangefeedPagedFlux getBlobChangefeedPagedFlux(BlobContainerAsyncClient client, OffsetDateTime startTime,
        OffsetDateTime endTime) {
        Changefeed changefeed = changefeedFactory.getChangefeed(client, startTime, endTime);
        return new BlobChangefeedPagedFlux(changefeed);
    }

    /**
     * Gets an new instance of {@link BlobChangefeedPagedFlux}.
     *
     * @param client The {@link BlobContainerAsyncClient changefeed client}.
     * @param cursor The cursor.
     */
    BlobChangefeedPagedFlux getBlobChangefeedPagedFlux(BlobContainerAsyncClient client, String cursor) {
        Changefeed changefeed = changefeedFactory.getChangefeed(client, cursor);
        return new BlobChangefeedPagedFlux(changefeed);
    }
}
