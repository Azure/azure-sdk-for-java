// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Constants;
import com.azure.data.cosmos.internal.HttpConstants;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * This class defines a custom exception type for all operations on
 * DocumentClient in the Azure Cosmos DB database service. Applications are
 * expected to catch CosmosClientException and handle errors as appropriate when
 * calling methods on DocumentClient.
 * <p>
 * Errors coming from the service during normal execution are converted to
 * CosmosClientException before returning to the application with the following
 * exception:
 * <p>
 * When a BE error is encountered during a QueryIterable&lt;T&gt; iteration, an
 * IllegalStateException is thrown instead of CosmosClientException.
 * <p>
 * When a transport level error happens that request is not able to reach the
 * service, an IllegalStateException is thrown instead of CosmosClientException.
 */
public class CosmosClientException extends Exception {
    private static final long serialVersionUID = 1L;

    private final int statusCode;
    private final Map<String, String> responseHeaders;

    private CosmosResponseDiagnostics cosmosResponseDiagnostics;
    private CosmosError cosmosError;

    long lsn;
    String partitionKeyRangeId;
    Map<String, String> requestHeaders;
    URI requestUri;
    String resourceAddress;

    CosmosClientException(int statusCode, String message, Map<String, String> responseHeaders, Throwable cause) {
        super(message, cause, /* enableSuppression */ true, /* writableStackTrace */ false);
        this.statusCode = statusCode;
        this.responseHeaders = responseHeaders == null ? new HashMap<>() : new HashMap<>(responseHeaders);
    }

    /**
     * Creates a new instance of the CosmosClientException class.
     *
     * @param statusCode the http status code of the response.
     */
    CosmosClientException(int statusCode) {
        this(statusCode, null, null, null);
    }

    /**
     * Creates a new instance of the CosmosClientException class.
     *
     * @param statusCode   the http status code of the response.
     * @param errorMessage the error message.
     */
    CosmosClientException(int statusCode, String errorMessage) {
        this(statusCode, errorMessage, null, null);
        this.cosmosError = new CosmosError();
        cosmosError.set(Constants.Properties.MESSAGE, errorMessage);
    }

    /**
     * Creates a new instance of the CosmosClientException class.
     *
     * @param statusCode     the http status code of the response.
     * @param innerException the original exception.
     */
    CosmosClientException(int statusCode, Exception innerException) {
        this(statusCode, null, null, innerException);
    }

    /**
     * Creates a new instance of the CosmosClientException class.
     *
     * @param statusCode      the http status code of the response.
     * @param cosmosErrorResource   the error resource object.
     * @param responseHeaders the response headers.
     */
    CosmosClientException(int statusCode, CosmosError cosmosErrorResource, Map<String, String> responseHeaders) {
        this(/* resourceAddress */ null, statusCode, cosmosErrorResource, responseHeaders);
    }

    /**
     * Creates a new instance of the CosmosClientException class.
     *
     * @param resourceAddress the address of the resource the request is associated with.
     * @param statusCode      the http status code of the response.
     * @param cosmosErrorResource   the error resource object.
     * @param responseHeaders the response headers.
     */

    CosmosClientException(String resourceAddress, int statusCode, CosmosError cosmosErrorResource, Map<String, String> responseHeaders) {
        this(statusCode, cosmosErrorResource == null ? null : cosmosErrorResource.getMessage(), responseHeaders, null);
        this.resourceAddress = resourceAddress;
        this.cosmosError = cosmosErrorResource;
    }

    /**
     * Creates a new instance of the CosmosClientException class.
     * 
     * @param message         the string message.
     * @param statusCode      the http status code of the response.
     * @param exception       the exception object.
     * @param responseHeaders the response headers.
     * @param resourceAddress the address of the resource the request is associated with.
     */
    CosmosClientException(String message, Exception exception, Map<String, String> responseHeaders, int statusCode, String resourceAddress) {
        this(statusCode, message, responseHeaders, exception);
        this.resourceAddress = resourceAddress;
    }

    @Override
    public String getMessage() {
        if (cosmosResponseDiagnostics == null) {
            return innerErrorMessage();
        }
        return innerErrorMessage() + ", " + cosmosResponseDiagnostics.toString();
    }

    /**
     * Gets the activity ID associated with the request.
     *
     * @return the activity ID.
     */
    public String message() {
        if (this.responseHeaders != null) {
            return this.responseHeaders.get(HttpConstants.HttpHeaders.ACTIVITY_ID);
        }

        return null;
    }

    /**
     * Gets the http status code.
     *
     * @return the status code.
     */
    public int statusCode() {
        return this.statusCode;
    }

    /**
     * Gets the sub status code.
     *
     * @return the status code.
     */
    public int subStatusCode() {
        int code = HttpConstants.SubStatusCodes.UNKNOWN;
        if (this.responseHeaders != null) {
            String subStatusString = this.responseHeaders.get(HttpConstants.HttpHeaders.SUB_STATUS);
            if (StringUtils.isNotEmpty(subStatusString)) {
                try {
                    code = Integer.parseInt(subStatusString);
                } catch (NumberFormatException e) {
                    // If value cannot be parsed as Integer, return Unknown.
                }
            }
        }

        return code;
    }

    /**
     * Gets the error code associated with the exception.
     *
     * @return the error.
     */
    public CosmosError error() {
        return this.cosmosError;
    }

    void error(CosmosError cosmosError) {
        this.cosmosError = cosmosError;
    }

    /**
     * Gets the recommended time interval after which the client can retry failed
     * requests
     *
     * @return the recommended time interval after which the client can retry failed
     *         requests.
     */
    public long retryAfterInMilliseconds() {
        long retryIntervalInMilliseconds = 0;

        if (this.responseHeaders != null) {
            String header = this.responseHeaders.get(HttpConstants.HttpHeaders.RETRY_AFTER_IN_MILLISECONDS);

            if (StringUtils.isNotEmpty(header)) {
                try {
                    retryIntervalInMilliseconds = Long.parseLong(header);
                } catch (NumberFormatException e) {
                    // If the value cannot be parsed as long, return 0.
                }
            }
        }

        //
        // In the absence of explicit guidance from the backend, don't introduce
        // any unilateral retry delays here.
        return retryIntervalInMilliseconds;
    }

    /**
     * Gets the response headers as key-value pairs
     *
     * @return the response headers
     */
    public Map<String, String> responseHeaders() {
        return this.responseHeaders;
    }

    /**
     * Gets the resource address associated with this exception.
     *
     * @return the resource address associated with this exception.
     */
    String getResourceAddress() {
        return this.resourceAddress;
    }

    /**
     * Gets the Cosmos Response Diagnostic Statistics associated with this exception.
     *
     * @return Cosmos Response Diagnostic Statistics associated with this exception.
     */
    public CosmosResponseDiagnostics cosmosResponseDiagnostics() {
        return cosmosResponseDiagnostics;
    }

    CosmosClientException cosmosResponseDiagnostics(CosmosResponseDiagnostics cosmosResponseDiagnostics) {
        this.cosmosResponseDiagnostics = cosmosResponseDiagnostics;
        return this;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + "error=" + cosmosError + ", resourceAddress='" + resourceAddress + '\''
                + ", statusCode=" + statusCode + ", message=" + getMessage() + ", causeInfo=" + causeInfo()
                + ", responseHeaders=" + responseHeaders + ", requestHeaders=" + requestHeaders + '}';
    }

    String innerErrorMessage() {
        String innerErrorMessage = super.getMessage();
        if (cosmosError != null) {
            innerErrorMessage = cosmosError.getMessage();
            if (innerErrorMessage == null) {
                innerErrorMessage = String.valueOf(cosmosError.get("Errors"));
            }
        }
        return innerErrorMessage;
    }

    private String causeInfo() {
        Throwable cause = getCause();
        if (cause != null) {
            return String.format("[class: %s, message: %s]", cause.getClass(), cause.getMessage());
        }
        return null;
    }
}
