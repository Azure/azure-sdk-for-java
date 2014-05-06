/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.azure.storage;

/**
 * Represents error codes that may be returned by the Microsoft Azure storage services or the storage client library.
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
    TRANSPORT_ERROR(5),

    /**
     * A lease is required to perform the operation.
     */
    LEASE_ID_MISSING(21),

    /**
     * The given lease ID does not match the current lease.
     */
    LEASE_ID_MISMATCH(22),

    /**
     * A lease ID was used when no lease currently is held.
     */
    LEASE_NOT_PRESENT(23),

    /**
     * The server is currently unavailable.
     */
    SERVER_BUSY(24);

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
