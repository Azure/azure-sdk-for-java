package com.microsoft.windowsazure.services.queue.client;

/**
 * Reserved for internal use. Specifies queue message type.
 */
public enum QueueMessageType {
    /**
     * Indicates the message object stores the raw text string.
     */
    RAW_STRING,

    /**
     * Indicates the message object stores the Base64-Encoded representation of
     * the raw data.
     */
    BASE_64_ENCODED
}
