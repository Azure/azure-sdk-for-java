package com.microsoft.windowsazure.services.serviceBus.models;

/**
 * Specifies the mode when a message is received.
 */
public enum ReceiveMode {
    /**
     * The message is retrieved and locked for processing, until either the receiver deletes the message, unlocks it, or
     * the lock duration expires.
     */
    PEEK_LOCK,
    /**
     * The message is retrieved and deleted.
     */
    RECEIVE_AND_DELETE,
}
