// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed.models;

import com.azure.storage.blob.models.BlobType;

/**
 * This class contains properties of a BlobChangefeedEventData.
 */
public interface BlobChangefeedEventData {

    /**
     * Gets the api.
     *
     * @return The api.
     */
    String getApi();

    /**
     * Gets the client request id.
     *
     * @return The client request id.
     */
    String getClientRequestId();

    /**
     * Gets the request id.
     *
     * @return The request id.
     */
    String getRequestId();

    /**
     * Gets the eTag.
     *
     * @return The eTag.
     */
    String getETag();

    /**
     * Gets the content type.
     *
     * @return The content type.
     */
    String getContentType();

    /**
     * Gets the content length.
     *
     * @return The content length.
     */
    Long getContentLength();

    /**
     * Gets the {@link BlobType}.
     *
     * @return {@link BlobType}.
     */
    BlobType getBlobType();

    /**
     * Gets the content offset.
     *
     * @return The content offset.
     */
    Long getContentOffset();

    /**
     * Gets the destination url.
     *
     * @return The destination url.
     */
    String getDestinationUrl();

    /**
     * Gets the source url.
     *
     * @return The source url.
     */
    String getSourceUrl();

    /**
     * Gets the blob url.
     *
     * @return The blob url.
     */
    String getBlobUrl();

    /**
     * Gets whether this operation was recursive.
     *
     * @return Whether this operation was recursive.
     */
    boolean isRecursive();

    /**
     * Gets the sequencer.
     *
     * @return The sequencer.
     */
    String getSequencer();

}
