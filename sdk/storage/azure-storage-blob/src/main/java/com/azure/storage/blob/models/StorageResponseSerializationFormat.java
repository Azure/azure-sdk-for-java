// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

/**
 * Defines the serialization format the service uses for list-blobs responses.
 */
public enum StorageResponseSerializationFormat {
    /**
     * Let the SDK choose the serialization format that is most appropriate for the request.
     * <p>
     * The exact format selected by {@code AUTO} is an implementation detail and may change
     * between SDK releases. Choose {@link #XML} or {@link #ARROW} explicitly if you require
     * a specific format.
     */
    AUTO,

    /**
     * XML response format.
     */
    XML,

    /**
     * Apache Arrow response format.
     */
    ARROW
}
