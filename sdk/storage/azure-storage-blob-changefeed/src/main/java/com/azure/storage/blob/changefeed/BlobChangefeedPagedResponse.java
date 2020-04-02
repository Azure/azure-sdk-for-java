package com.azure.storage.blob.changefeed;

import com.azure.core.util.IterableStream;
import com.azure.core.util.paging.ContinuablePage;
import com.azure.storage.blob.changefeed.implementation.util.BlobChangefeedCursor;
import com.azure.storage.blob.changefeed.models.BlobChangefeedEvent;

import java.util.List;

public class BlobChangefeedPagedResponse implements ContinuablePage<String, BlobChangefeedEvent> {

    private final List<BlobChangefeedEvent> events;
    private final BlobChangefeedCursor cursor;

    BlobChangefeedPagedResponse(List<BlobChangefeedEvent> events, BlobChangefeedCursor cursor) {
        this.events = events;
        this.cursor = cursor;
    }

    @Override
    public IterableStream<BlobChangefeedEvent> getElements() {
        return new IterableStream<>(this.events);
    }

    public List<BlobChangefeedEvent> getValue() {
        return this.events;
    }

    @Override
    public String getContinuationToken() {
        return cursor.serialize();
    }
}
