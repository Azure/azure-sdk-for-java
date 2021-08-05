// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed.models;

import com.azure.storage.blob.models.BlobType;

/**
 * This class contains properties of a BlobChangefeedEventData.
 */
public interface BlobChangefeedEventData {

    /**
     * @return The api.
     */
    String getApi();

    /**
     * @return The client request id.
     */
    String getClientRequestId();

    /**
     * @return The request id.
     */
    String getRequestId();

    /**
     * @return The eTag.
     */
    String getETag();

    /**
     * @return The content type.
     */
    String getContentType();

    /**
     * @return The content length.
     */
    Long getContentLength();

    /**
     * @return {@link BlobType}.
     */
    BlobType getBlobType();

    /**
     * @return The content offset.
     */
    Long getContentOffset();

    /**
     * @return The destination url.
     */
    String getDestinationUrl();

    /**
     * @return The source url.
     */
    String getSourceUrl();

    /**
     * @return The blob url.
     */
    String getBlobUrl();

    /**
     * @return Whether or not this operation was recursive.
     */
    boolean isRecursive();

    /**
     * @return The sequencer.
     */
    String getSequencer();

}
