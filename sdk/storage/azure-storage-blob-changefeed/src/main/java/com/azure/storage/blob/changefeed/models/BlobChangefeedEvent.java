// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed.models;

import java.time.OffsetDateTime;

/**
 * This class contains properties of a BlobChangefeedEvent.
 */
public interface BlobChangefeedEvent {

    /**
     * Gets the topic.
     *
     * @return the topic.
     */
    String getTopic();

    /**
     * Gets the subject.
     *
     * @return the subject.
     */
    String getSubject();

    /**
     * Gets the {@link BlobChangefeedEventType}.
     *
     * @return {@link BlobChangefeedEventType}
     */
    BlobChangefeedEventType getEventType();

    /**
     * Gets the {@link OffsetDateTime event time}.
     *
     * @return The {@link OffsetDateTime event time}.
     */
    OffsetDateTime getEventTime();

    /**
     * Gets the identifier.
     *
     * @return the identifier.
     */
    String getId();

    /**
     * Gets the {@link BlobChangefeedEventData}.
     *
     * @return {@link BlobChangefeedEventData}.
     */
    BlobChangefeedEventData getData();

    /**
     * Gets the data version.
     *
     * @return the data version.
     */
    Long getDataVersion();

    /**
     * Gets the metadata version.
     *
     * @return the metadata version.
     */
    String getMetadataVersion();
}
