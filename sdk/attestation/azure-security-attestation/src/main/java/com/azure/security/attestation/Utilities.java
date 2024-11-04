// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation;

import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceExistsException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.models.ResponseError;
import com.azure.security.attestation.implementation.models.CloudErrorBody;
import com.azure.security.attestation.implementation.models.CloudErrorException;
import com.azure.security.attestation.models.AttestationResponse;
import com.azure.security.attestation.models.AttestationToken;

/**
 * Utility class with helper functions.
 */
class Utilities {

    /**
     * Generates a new public response type from an internal model type.
     * @param response Response from the generated API
     * @param value Value to be included in the new response
     * @param <T> Type of `value`.
     * @param <R> Ignored.
     * @return Returns a newly created Response type.
     */
    static <T, R> ResponseBase<Void, T> generateResponseFromModelType(Response<R> response, T value) {
        return new ResponseBase<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), value, null);
    }

    /**
     * Generates a new public response type from an internal model type.
     * @param response Response from the generated API
     * @param value Value to be included in the new response
     * @param <T> Type of `value`.
     * @param <R> Ignored.
     * @return Returns a newly created Response type.
     */
    static <T, R> AttestationResponse<T> generateAttestationResponseFromModelType(Response<R> response,
        AttestationToken token, T value) {
        return new AttestationResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), value,
            token);
    }

    /**
     * This method converts the API response codes into well known exceptions.
     * @param exception The exception returned by the rest client.
     * @return The exception returned by the public methods.
     */
    static Throwable mapException(Throwable exception) {
        CloudErrorException cloudErrorException = null;

        if (exception instanceof CloudErrorException) {
            cloudErrorException = ((CloudErrorException) exception);
        } else if (exception instanceof RuntimeException) {
            RuntimeException runtimeException = (RuntimeException) exception;
            Throwable throwable = runtimeException.getCause();
            if (throwable instanceof CloudErrorException) {
                cloudErrorException = (CloudErrorException) throwable;
            }
        }

        if (cloudErrorException == null) {
            return exception;
        }

        final HttpResponse errorHttpResponse = cloudErrorException.getResponse();
        final int statusCode = errorHttpResponse.getStatusCode();

        CloudErrorBody body = cloudErrorException.getValue().getError();

        // Create a ResponseError from the code and message in the CloudErrorBody.
        final ResponseError responseError = new ResponseError(body.getCode(), body.getMessage());
        final String errorDetail = cloudErrorException.getMessage();

        switch (statusCode) {
            case 401:
                return new ClientAuthenticationException(errorDetail, cloudErrorException.getResponse(), responseError);

            case 404:
                return new ResourceNotFoundException(errorDetail, cloudErrorException.getResponse(), responseError);

            case 409:
                return new ResourceExistsException(errorDetail, cloudErrorException.getResponse(), responseError);

            case 412:
                return new ResourceModifiedException(errorDetail, cloudErrorException.getResponse(), responseError);

            default:
                return new HttpResponseException(errorDetail, cloudErrorException.getResponse(), responseError);
        }
    }
}
