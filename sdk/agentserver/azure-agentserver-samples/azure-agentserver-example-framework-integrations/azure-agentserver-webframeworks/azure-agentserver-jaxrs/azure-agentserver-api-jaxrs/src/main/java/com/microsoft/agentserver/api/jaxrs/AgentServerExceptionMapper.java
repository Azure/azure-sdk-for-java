// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api.jaxrs;

import com.microsoft.agentserver.api.AgentServerError;
import com.microsoft.agentserver.api.AgentServerException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

/**
 * JAX-RS {@link ExceptionMapper} that converts framework-agnostic {@link AgentServerException}
 * instances into the standard error envelope:
 * {@code { "error": { "message", "type", "code", "param"?, "details"?, "additionalInfo"? } }}.
 *
 */
@Provider
public class AgentServerExceptionMapper implements ExceptionMapper<AgentServerException> {

    @Override
    public Response toResponse(AgentServerException exception) {
        int statusCode = exception.getStatusCode();
        AgentServerError error = exception.getError() != null
            ? exception.getError()
            : AgentServerError.serverError("An internal error occurred.");
        return Response.status(statusCode)
            .type(MediaType.APPLICATION_JSON_TYPE)
            .entity(Map.of("error", error))
            .build();
    }
}

