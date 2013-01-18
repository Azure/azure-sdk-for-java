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
package com.microsoft.windowsazure.services.core.storage;

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.xml.stream.XMLStreamException;

import com.microsoft.windowsazure.services.core.storage.utils.implementation.StorageErrorResponse;

/**
 * Represents an exception for the Windows Azure storage service.
 */
public class StorageException extends Exception {

    /**
     * Represents the serialization version number.
     */
    private static final long serialVersionUID = 7972747254288274928L;

    /**
     * Returns extended error information from the specified request and operation context.
     * 
     * @param request
     *            An <code>HttpURLConnection</code> object that represents the request whose extended error information
     *            is being retrieved.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return A {@link StorageExtendedErrorInformation} object that represents the error details for the specified
     *         request.
     */
    protected static StorageExtendedErrorInformation getErrorDetailsFromRequest(final HttpURLConnection request,
            final OperationContext opContext) {
        if (request == null) {
            return null;
        }
        try {
            final StorageErrorResponse response = new StorageErrorResponse(request.getErrorStream());
            return response.getExtendedErrorInformation();
        }
        catch (final XMLStreamException e) {
            return null;
        }
    }

    /**
     * Translates the specified exception into a storage exception.
     * 
     * @param request
     *            An <code>HttpURLConnection</code> object that represents the request whose exception is being
     *            translated.
     * @param cause
     *            An <code>Exception</code> object that represents the exception to translate.
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return A <code>StorageException</code> object that represents translated exception.
     */
    public static StorageException translateException(final HttpURLConnection request, final Exception cause,
            final OperationContext opContext) {
        if (request == null) {
            return new StorageException("Client error",
                    "A Client side exception occurred, please check the inner exception for details",
                    Constants.HeaderConstants.HTTP_UNUSED_306, null, cause);
        }

        final StorageExtendedErrorInformation extendedError = getErrorDetailsFromRequest(request, opContext);
        StorageException translatedException = null;

        String responseMessage = Constants.EMPTY_STRING;
        int responseCode = 0;
        try {
            responseCode = request.getResponseCode();
            responseMessage = request.getResponseMessage();
        }
        catch (final IOException e) {
            // ignore errors
        }

        if (responseMessage == null) {
            responseMessage = Constants.EMPTY_STRING;
        }

        // 1. If extended information is available use it
        if (extendedError != null) {
            translatedException = new StorageException(extendedError.getErrorCode(), responseMessage, responseCode,
                    extendedError, cause);

            if (translatedException != null) {
                return translatedException;
            }
        }

        // 2. If extended information is unavailable, translate exception based
        // on status code
        translatedException = translateFromHttpStatus(responseCode, responseMessage, null, cause);

        if (translatedException != null) {
            return translatedException;
        }

        return new StorageException(StorageErrorCode.SERVICE_INTERNAL_ERROR.toString(),
                "The server encountered an unknown failure: ".concat(responseMessage),
                HttpURLConnection.HTTP_INTERNAL_ERROR, null, cause);
    }

    /**
     * Translates the specified HTTP status code into a storage exception.
     * 
     * @param statusCode
     *            The HTTP status code returned by the operation.
     * @param statusDescription
     *            A <code>String</code> that represents the status description.
     * @param details
     *            A {@link StorageExtendedErrorInformation} object that represents the errror details returned by the
     *            operation.
     * @param inner
     *            An <code>Exception</code> object that represents a reference to the initial exception, if one exists.
     * 
     * @return A <code>StorageException</code> object that represents translated exception.
     **/
    protected static StorageException translateFromHttpStatus(final int statusCode, final String statusDescription,
            final StorageExtendedErrorInformation details, final Exception inner) {
        switch (statusCode) {
            case HttpURLConnection.HTTP_FORBIDDEN:
                return new StorageException(StorageErrorCode.ACCESS_DENIED.toString(), statusDescription, statusCode,
                        details, inner);

            case HttpURLConnection.HTTP_GONE:
            case HttpURLConnection.HTTP_NOT_FOUND:
                return new StorageException(StorageErrorCode.RESOURCE_NOT_FOUND.toString(), statusDescription,
                        statusCode, details, inner);

            case HttpURLConnection.HTTP_BAD_REQUEST:
                return new StorageException(StorageErrorCode.BAD_REQUEST.toString(), statusDescription, statusCode,
                        details, inner);

            case HttpURLConnection.HTTP_PRECON_FAILED:
            case HttpURLConnection.HTTP_NOT_MODIFIED:
                return new StorageException(StorageErrorCode.CONDITION_FAILED.toString(), statusDescription,
                        statusCode, details, inner);

            case HttpURLConnection.HTTP_CONFLICT:
                return new StorageException(StorageErrorCode.RESOURCE_ALREADY_EXISTS.toString(), statusDescription,
                        statusCode, details, inner);

            case HttpURLConnection.HTTP_GATEWAY_TIMEOUT:
                return new StorageException(StorageErrorCode.SERVICE_TIMEOUT.toString(), statusDescription, statusCode,
                        details, inner);

            case 416:
                // RequestedRangeNotSatisfiable - No corresponding enum in HttpURLConnection
                return new StorageException(StorageErrorCode.BAD_REQUEST.toString(), statusDescription, statusCode,
                        details, inner);

            case HttpURLConnection.HTTP_INTERNAL_ERROR:
                return new StorageException(StorageErrorCode.SERVICE_INTERNAL_ERROR.toString(), statusDescription,
                        statusCode, details, inner);

            case HttpURLConnection.HTTP_NOT_IMPLEMENTED:
                return new StorageException(StorageErrorCode.NOT_IMPLEMENTED.toString(), statusDescription, statusCode,
                        details, inner);

            case HttpURLConnection.HTTP_BAD_GATEWAY:
                return new StorageException(StorageErrorCode.BAD_GATEWAY.toString(), statusDescription, statusCode,
                        details, inner);

            case HttpURLConnection.HTTP_VERSION:
                return new StorageException(StorageErrorCode.HTTP_VERSION_NOT_SUPPORTED.toString(), statusDescription,
                        statusCode, details, inner);
            default:
                return null;
        }
    }

    /**
     * Represents the error code returned by the operation.
     */
    protected String errorCode;

    /**
     * Represents the extended error information returned by the operation.
     * 
     * @see StorageExtendedErrorInformation
     */
    protected StorageExtendedErrorInformation extendedErrorInformation;

    /**
     * Represents the HTTP status code returned by the operation.
     */
    private final int httpStatusCode;

    /**
     * Creates an instance of the <code>StorageException</code> class using the specified parameters.
     * 
     * @param errorCode
     *            A <code>String</code> that represents the error code returned by the operation.
     * @param message
     *            A <code>String</code> that represents the error message returned by the operation.
     * @param statusCode
     *            The HTTP status code returned by the operation.
     * @param extendedErrorInfo
     *            A {@link StorageExtendedErrorInformation} object that represents the extended error information
     *            returned by the operation.
     * @param innerException
     *            An <code>Exception</code> object that represents a reference to the initial exception, if one exists.
     * 
     * @see StorageExtendedErrorInformation
     */
    public StorageException(final String errorCode, final String message, final int statusCode,
            final StorageExtendedErrorInformation extendedErrorInfo, final Exception innerException) {
        super(message, innerException);
        this.errorCode = errorCode;
        this.httpStatusCode = statusCode;
        this.extendedErrorInformation = extendedErrorInfo;
    }

    /**
     * @return the errorCode
     */
    public String getErrorCode() {
        return this.errorCode;
    }

    /**
     * @return the extendedErrorInformation
     */
    public StorageExtendedErrorInformation getExtendedErrorInformation() {
        return this.extendedErrorInformation;
    }

    /**
     * @return the httpStatusCode
     */
    public int getHttpStatusCode() {
        return this.httpStatusCode;
    }
}
