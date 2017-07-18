/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure;

import com.microsoft.rest.protocol.SerializerAdapter;
import okhttp3.ResponseBody;
import retrofit2.Response;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * The response body contains the status of the specified
 * asynchronous operation, indicating whether it has succeeded, is in
 * progress, or has failed. Note that this status is distinct from the
 * HTTP status code returned for the Get Operation Status operation
 * itself.  If the asynchronous operation succeeded, the response body
 * includes the HTTP status code for the successful request.  If the
 * asynchronous operation failed, the response body includes the HTTP
 * status code for the failed request, and also includes error
 * information regarding the failure.
 */
final class AzureAsyncOperation {
    /**
     * Default delay in seconds for long running operations.
     */
    static final int DEFAULT_DELAY = 30;

    /**
     * Successful status for long running operations.
     */
    static final String SUCCESS_STATUS = "Succeeded";

    /**
     * In progress status for long running operations.
     */
    static final String IN_PROGRESS_STATUS = "InProgress";

    /**
     * Failed status for long running operations.
     */
    static final String FAILED_STATUS = "Failed";

    /**
     * Canceled status for long running operations.
     */
    static final String CANCELED_STATUS = "Canceled";

    /**
     * @return a list of statuses indicating a failed operation
     */
    static List<String> failedStatuses() {
        return Arrays.asList(FAILED_STATUS, CANCELED_STATUS);
    }

    /**
     * @return a list of terminal statuses for long running operations
     */
    static List<String> terminalStatuses() {
        return Arrays.asList(FAILED_STATUS, CANCELED_STATUS, SUCCESS_STATUS);
    }

    /**
     * The status of the asynchronous request.
     */
    private String status;

    /**
     * @return the status of the asynchronous request
     */
    String status() {
        return this.status;
    }

    /**
     * Sets the status of the asynchronous request.
     *
     * @param status the status of the asynchronous request.
     */
    void setStatus(String status) {
        this.status = status;
    }

    /**
     * If the asynchronous operation failed, the response body includes
     * the HTTP status code for the failed request, and also includes
     * error information regarding the failure.
     */
    private CloudError error;

    /**
     * Gets the cloud error.
     *
     * @return the cloud error.
     */
    CloudError getError() {
        return this.error;
    }

    /**
     * Sets the cloud error.
     *
     * @param error the cloud error.
     */
    void setError(CloudError error) {
        this.error = error;
    }

    /**
     * Async operation in string format.
     */
    private String rawString;

    /**
     * @return the raw string
     */
    String rawString() {
        return this.rawString;
    }

    /**
     * Creates AzureAsyncOperation from the given HTTP response.
     *
     * @param serializerAdapter the adapter to use for deserialization
     * @param response the response
     * @return the async operation object
     * @throws CloudException if the deserialization fails or response contains invalid body
     */
    static AzureAsyncOperation fromResponse(SerializerAdapter<?> serializerAdapter, Response<ResponseBody> response) throws CloudException {
        AzureAsyncOperation asyncOperation = null;
        String rawString = null;
        if (response.body() != null) {
            try {
                rawString = response.body().string();
                asyncOperation = serializerAdapter.deserialize(rawString, AzureAsyncOperation.class);
                asyncOperation.rawString = rawString;
            } catch (IOException exception) {
                // Exception will be handled below
            }
            finally {
                response.body().close();
            }
        }
        if (asyncOperation == null || asyncOperation.status() == null) {
            throw new CloudException("polling response does not contain a valid body: " + rawString, response);
        }
        return asyncOperation;
    }
}