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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketException;

import com.microsoft.azure.storage.core.StorageRequest;
import com.microsoft.azure.storage.core.Utility;

/**
 * Represents an exception for the Microsoft Azure storage service.
 */
public class StorageException extends Exception {
    /**
     * Represents the serialization version number.
     */
    private static final long serialVersionUID = 7972747254288274928L;

    /**
     * RESERVED FOR INTERNAL USE. Translates the specified exception into a storage exception.
     * 
     * @param cause
     *            An <code>Exception</code> object that represents the exception to translate.
     * 
     * @return A <code>StorageException</code> object that represents translated exception.
     */
    public static StorageException translateClientException(final Exception cause) {
        return new StorageException("Client error",
                "A Client side exception occurred, please check the inner exception for details",
                Constants.HeaderConstants.HTTP_UNUSED_306, null, cause);
    }

    /**
     * RESERVED FOR INTERNAL USE. Translates the specified exception into a storage exception.
     * 
     * @param request
     *            An <code>HttpURLConnection</code> object that represents the request whose exception is being
     *            translated.
     * @param cause
     *            An <code>Exception</code> object that represents the exception to translate.
     * 
     * @return A <code>StorageException</code> object that represents translated exception.
     */
    public static StorageException translateException(final StorageRequest<?, ?, ?> request, final Exception cause,
            final OperationContext opContext) {
        if (request == null || request.getConnection() == null) {
            return translateClientException(cause);
        }
        
        if (cause instanceof SocketException) {
            String message = cause == null ? Constants.EMPTY_STRING : cause.getMessage();
            return new StorageException(StorageErrorCode.SERVICE_INTERNAL_ERROR.toString(),
                    "An unknown failure occurred : ".concat(message), HttpURLConnection.HTTP_INTERNAL_ERROR,
                    null, cause);
        }

        StorageException translatedException = null;

        String responseMessage = null;
        int responseCode = 0;
        try {
            responseCode = request.getConnection().getResponseCode();
            responseMessage = request.getConnection().getResponseMessage();
        } catch (final IOException e) {
            // ignore errors
        }

        if (responseMessage == null) {
            responseMessage = Constants.EMPTY_STRING;
        }

        StorageExtendedErrorInformation extendedError = request.parseErrorDetails();
        if (extendedError != null) {
            // 1. If extended information is available use it
            translatedException = new StorageException(extendedError.getErrorCode(), responseMessage, responseCode,
                    extendedError, cause);
        } else {
            // 2. If extended information is unavailable, translate exception based
            // on status code
            translatedException = translateFromHttpStatus(responseCode, responseMessage, cause);   
        }

        if (translatedException != null) {
            Utility.logHttpError(translatedException, opContext);
            return translatedException;
        } else {
            return new StorageException(StorageErrorCode.SERVICE_INTERNAL_ERROR.toString(),
                    "The server encountered an unknown failure: ".concat(responseMessage),
                    HttpURLConnection.HTTP_INTERNAL_ERROR, null, cause);   
        }
    }

    /**
     * Translates the specified HTTP status code into a storage exception.
     * 
     * @param statusCode
     *            The HTTP status code returned by the operation.
     * @param statusDescription
     *            A <code>String</code> that represents the status description.
     * @param inner
     *            An <code>Exception</code> object that represents a reference to the initial exception, if one exists.
     * 
     * @return A <code>StorageException</code> object that represents translated exception.
     **/
    protected static StorageException translateFromHttpStatus(final int statusCode, final String statusDescription,
            final Exception inner) {
        String errorCode;
        switch (statusCode) {
        case HttpURLConnection.HTTP_FORBIDDEN:
            errorCode = StorageErrorCode.ACCESS_DENIED.toString();
            break;
        case HttpURLConnection.HTTP_GONE:
        case HttpURLConnection.HTTP_NOT_FOUND:
            errorCode = StorageErrorCode.RESOURCE_NOT_FOUND.toString();
            break;
        case 416:
        case HttpURLConnection.HTTP_BAD_REQUEST:
            // 416: RequestedRangeNotSatisfiable - No corresponding enum in HttpURLConnection
            errorCode = StorageErrorCode.BAD_REQUEST.toString();
            break;

        case HttpURLConnection.HTTP_PRECON_FAILED:
        case HttpURLConnection.HTTP_NOT_MODIFIED:
            errorCode = StorageErrorCode.CONDITION_FAILED.toString();
            break;

        case HttpURLConnection.HTTP_CONFLICT:
            errorCode = StorageErrorCode.RESOURCE_ALREADY_EXISTS.toString();
            break;

        case HttpURLConnection.HTTP_UNAVAILABLE:
            errorCode = StorageErrorCode.SERVER_BUSY.toString();
            break;

        case HttpURLConnection.HTTP_GATEWAY_TIMEOUT:
            errorCode = StorageErrorCode.SERVICE_TIMEOUT.toString();
            break;

        case HttpURLConnection.HTTP_INTERNAL_ERROR:
            errorCode = StorageErrorCode.SERVICE_INTERNAL_ERROR.toString();
            break;

        case HttpURLConnection.HTTP_NOT_IMPLEMENTED:
            errorCode = StorageErrorCode.NOT_IMPLEMENTED.toString();
            break;

        case HttpURLConnection.HTTP_BAD_GATEWAY:
            errorCode = StorageErrorCode.BAD_GATEWAY.toString();
            break;

        case HttpURLConnection.HTTP_VERSION:
            errorCode = StorageErrorCode.HTTP_VERSION_NOT_SUPPORTED.toString();
            break;
        default:
            errorCode = null;
        }

        if (errorCode == null) {
            return null;
        } else {
            return new StorageException(errorCode, statusDescription, statusCode, null, inner);
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
     * Gets the error code returned by the operation.
     * 
     * @return the errorCode
     */
    public String getErrorCode() {
        return this.errorCode;
    }

    /**
     * Gets the extended error information returned by the operation.
     * 
     * @return the extendedErrorInformation
     */
    public StorageExtendedErrorInformation getExtendedErrorInformation() {
        return this.extendedErrorInformation;
    }

    /**
     * Gets the HTTP status code returned by the operation.
     * 
     * @return the httpStatusCode
     */
    public int getHttpStatusCode() {
        return this.httpStatusCode;
    }
}
