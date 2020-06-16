// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed.models;

import java.time.OffsetDateTime;

/**
 * This class contains properties of a BlobChangefeedEvent.
 */
public interface BlobChangefeedEvent {

    /**
     * @return the topic.
     */
    String getTopic();

    /**
     * @return the subject.
     */
    String getSubject();

    /**
     * @return {@link BlobChangefeedEventType}
     */
    BlobChangefeedEventType getEventType();

    /**
     * @return The {@link OffsetDateTime event time}.
     */
    OffsetDateTime getEventTime();

    /**
     * @return the identifer.
     */
    String getId();

    /**
     * @return {@link BlobChangefeedEventData}.
     */
    BlobChangefeedEventData getData();

    /**
     * @return the data version.
     */
    Long getDataVersion();

    /**
     * @return the metadata version.
     */
    String getMetadataVersion();
}
