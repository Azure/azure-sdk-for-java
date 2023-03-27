// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.models;

/**
 * The data type of message.
 */
public enum WebPubSubDataType {
    /**
     * the binary data.
     */
    BINARY,

    /**
     * the JSON data.
     */
    JSON,

    /**
     * the text data.
     */
    TEXT,

    /**
     * the Protocol Buffers data.
     */
    PROTOBUF,
}
