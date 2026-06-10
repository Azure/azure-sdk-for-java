// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api.jaxrs;

import com.microsoft.agentserver.api.ApiError;
import com.microsoft.agentserver.api.ApiException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

/**
 * JAX-RS {@link ExceptionMapper} that converts framework-agnostic {@link ApiException}
 * instances into the standard error envelope:
 * {@code { "error": { "message", "type", "code", "param"?, "details"?, "additionalInfo"? } }}.
 *
 */
@Provider
public class ApiExceptionMapper implements ExceptionMapper<ApiException> {

    @Override
    public Response toResponse(ApiException exception) {
        int statusCode = exception.getStatusCode();
        ApiError error = exception.getError() != null
            ? exception.getError()
            : ApiError.serverError("An internal error occurred.");
        return Response.status(statusCode)
            .type(MediaType.APPLICATION_JSON_TYPE)
            .entity(Map.of("error", error))
            .build();
    }
}

