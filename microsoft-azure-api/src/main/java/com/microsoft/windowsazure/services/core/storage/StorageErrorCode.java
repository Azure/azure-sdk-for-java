package com.microsoft.windowsazure.services.core.storage;

/**
 * 
 * Represents error codes that may be returned by the Windows Azure storage services or the storage client library.
 * 
 * Copyright (c)2011 Microsoft. All rights reserved.
 */
public enum StorageErrorCode {
    /**
     * Access was denied (client-side error).
     */
    ACCESS_DENIED(12),

    /**
     * The specified account was not found (client-side error).
     */
    ACCOUNT_NOT_FOUND(8),

    /**
     * An authentication error occurred (client-side error).
     */
    AUTHENTICATION_FAILURE(11),

    /**
     * There was an error with the gateway used for the request (client-side error).
     */
    BAD_GATEWAY(18),

    /**
     * The request was incorrect or badly formed (client-side error).
     */
    BAD_REQUEST(16),

    /**
     * The specified blob already exists (client-side error).
     */
    BLOB_ALREADY_EXISTS(15),

    /**
     * The specified blob was not found (client-side error).
     */
    BLOB_NOT_FOUND(10),

    /**
     * The specified condition failed (client-side error).
     */
    CONDITION_FAILED(17),

    /**
     * The specified container already exists (client-side error).
     */
    CONTAINER_ALREADY_EXISTS(14),

    /**
     * The specified container was not found (client-side error).
     */
    CONTAINER_NOT_FOUND(9),

    /**
     * The request version header is not supported (client-side error).
     */
    HTTP_VERSION_NOT_SUPPORTED(20),

    /**
     * No error specified.
     */
    NONE(0),

    /**
     * The requested operation is not implemented on the specified resource (client-side error).
     */
    NOT_IMPLEMENTED(19),

    /**
     * The specified resource already exists (client-side error).
     */
    RESOURCE_ALREADY_EXISTS(13),

    /**
     * The specified resource was not found (client-side error).
     */
    RESOURCE_NOT_FOUND(7),

    /**
     * The service returned a bad response (server-side error).
     */
    SERVICE_BAD_REQUEST(6),

    /**
     * A service integrity check failed (server-side error).
     */
    SERVICE_INTEGRITY_CHECK_FAILED(4),

    /**
     * An internal server error occurred (server-side error).
     */
    SERVICE_INTERNAL_ERROR(1),

    /**
     * The service timed out (server-side error).
     */
    SERVICE_TIMEOUT(3),

    /**
     * A transport error occurred (server-side error).
     */
    TRANSPORT_ERROR(5);

    /**
     * Returns the value of this enum.
     */
    public int value;

    /**
     * Sets the value of this enum.
     * 
     * @param val
     *            The value being assigned.
     */
    StorageErrorCode(final int val) {
        this.value = val;
    }
}
