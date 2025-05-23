// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.models;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.implementation.http.RetryUtils;
import io.clientcore.core.models.CoreException;
import io.clientcore.core.models.binarydata.BinaryData;

/**
 * The exception thrown when an unsuccessful response is received with http status code (e.g. {@code 3XX}, {@code 4XX},
 * {@code 5XX}) from the service request.
 */
@Metadata(properties = MetadataProperties.IMMUTABLE)
public class HttpResponseException extends CoreException {
    /**
     * The HTTP response value.
     */
    private final Object value;

    /**
     * Information about the associated HTTP response.
     */
    private final Response<BinaryData> response;

    /**
     * Indicates whether the exception is retryable.
     */
    private final boolean isRetryable;

    /**
     * Initializes a new instance of the HttpResponseException class.
     *
     * @param message The exception message.
     * @param response The {@link Response} received that is associated to the exception.
     * @param value The deserialized response value.
     */
    public HttpResponseException(final String message, final Response<BinaryData> response, final Object value) {
        super(message, null);

        this.value = value;
        this.response = response;
        this.isRetryable = response == null || RetryUtils.isRetryable(response.getStatusCode());
    }

    /**
     * Initializes a new instance of the HttpResponseException class.
     *
     * @param message The exception message.
     * @param response The {@link Response} received that is associated to the exception.
     * @param cause The {@link Throwable} which caused the creation of this exception.
     */
    public HttpResponseException(final String message, final Response<BinaryData> response, final Throwable cause) {
        super(message, cause);

        this.value = null;
        this.response = response;
        this.isRetryable
            = response != null ? RetryUtils.isRetryable(response.getStatusCode()) : RetryUtils.isRetryable(cause);
    }

    /**
     * Gets the {@link Response} received that is associated to the exception.
     *
     * @return The {@link Response} received that is associated to the exception.
     */
    public Response<BinaryData> getResponse() {
        return response;
    }

    /**
     * Gets the deserialized HTTP response value.
     *
     * @return The deserialized HTTP response value.
     */
    public Object getValue() {
        return value;
    }

    @Override
    public boolean isRetryable() {
        return isRetryable;
    }
}
