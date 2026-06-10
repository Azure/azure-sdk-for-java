// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api.jaxrs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * JAX-RS pre-matching filter that reroutes requests to dedicated streaming
 * sub-resources, mirroring the protocol's stream/non-stream split:
 * <ul>
 *  <li>{@code POST /responses} with {@code "stream": true} in the body →
 *  {@code POST /responses/streaming}.</li>
 *  <li>{@code GET /responses/{id}?stream=true} (SSE replay) →
 *  {@code GET /responses/{id}/stream}.</li>
 * </ul>
 */
@Provider
@PreMatching
public class RoutingFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoutingFilter.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Maximum request body size (in bytes) the routing filter will buffer to inspect
     * the {@code "stream"} flag. Requests exceeding this limit are rejected with
     * HTTP 413 to prevent out-of-memory conditions.
     */
    private static final int MAX_BODY_SIZE = 1024 * 1024; // 1 MB

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String method = requestContext.getMethod();
        var uriInfo = requestContext.getUriInfo();
        String path = uriInfo.getPath();

        // GET /responses/{id}?stream=true → SSE replay sub-resource.
        if ("GET".equals(method)) {
            if (path.matches("responses/[^/]+")
                && "true".equalsIgnoreCase(uriInfo.getQueryParameters().getFirst("stream"))) {
                var newUri = uriInfo.getRequestUriBuilder()
                    .replacePath(uriInfo.getBaseUri().getPath() + path + "/stream")
                    .build();
                LOGGER.debug("Routing filter: GET stream replay → {}", newUri);
                requestContext.setRequestUri(newUri);
            }
            return;
        }

        // POST /responses with "stream": true → streaming create.
        if (!"responses".equals(path)) {
            return;
        }

        byte[] bodyBytes;
        try {
            bodyBytes = requestContext.getEntityStream().readNBytes(MAX_BODY_SIZE + 1);
        } catch (IOException e) {
            LOGGER.warn("Failed to read request body for stream routing detection");
            abortBadRequest(requestContext, "Unable to read request body");
            return;
        }

        if (bodyBytes.length > MAX_BODY_SIZE) {
            LOGGER.warn("Request body exceeds maximum size of {} bytes", MAX_BODY_SIZE);
            requestContext.abortWith(
                Response.status(413)
                    .entity("Request body too large")
                    .build());
            return;
        }

        // Re-set the entity stream so downstream handlers can read it
        requestContext.setEntityStream(new ByteArrayInputStream(bodyBytes));

        try {
            JsonNode jsonNode = MAPPER.readTree(bodyBytes);
            boolean stream = jsonNode.has("stream") && jsonNode.get("stream").asBoolean(false);

            LOGGER.debug("Routing filter: stream={}, contentLength={}", stream, bodyBytes.length);

            if (stream) {
                requestContext.setRequestUri(requestContext.getUriInfo().getBaseUriBuilder()
                    .path("responses/streaming")
                    .build());
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to parse request body as JSON for stream routing detection");
            abortBadRequest(requestContext, "Invalid JSON in request body");
        }
    }

    private static void abortBadRequest(ContainerRequestContext requestContext, String message) {
        requestContext.abortWith(
            Response.status(Response.Status.BAD_REQUEST)
                .entity(message)
                .build());
    }
}
