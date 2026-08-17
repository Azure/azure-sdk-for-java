// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.server.spring;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Spring filter that reroutes requests to dedicated streaming sub-resources,
 * mirroring the protocol's stream/non-stream split:
 * <ul>
 *  <li>{@code POST /responses} with {@code "stream": true} in the body →
 *  {@code POST /responses/streaming}.</li>
 *  <li>{@code GET /responses/{id}?stream=true} (SSE replay) →
 *  {@code GET /responses/{id}/stream}.</li>
 * </ul>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class StreamRoutingFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(StreamRoutingFilter.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Maximum request body size (in bytes) the routing filter will buffer to inspect
     * the {@code "stream"} flag. Requests exceeding this limit are rejected with
     * HTTP 413 to prevent out-of-memory conditions.
     */
    private static final int MAX_BODY_SIZE = 1024 * 1024; // 1 MB

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // GET /responses/{id}?stream=true → SSE replay sub-resource.
        if ("GET".equalsIgnoreCase(method)) {
            if (path.matches("/responses/[^/]+") && "true".equalsIgnoreCase(request.getParameter("stream"))) {
                LOGGER.debug("Routing filter: GET stream replay → {}/stream", path);
                request.getRequestDispatcher(path + "/stream").forward(request, response);
                return;
            }
            filterChain.doFilter(request, response);
            return;
        }

        // Only inspect POST /responses (exact match, not sub-paths)
        if (!"POST".equalsIgnoreCase(method) || !"/responses".equals(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Read the body bytes and wrap the request so downstream can re-read
        byte[] bodyBytes;
        try {
            bodyBytes = request.getInputStream().readNBytes(MAX_BODY_SIZE + 1);
        } catch (IOException e) {
            LOGGER.warn("Failed to read request body for stream routing detection");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unable to read request body");
            return;
        }

        if (bodyBytes.length > MAX_BODY_SIZE) {
            LOGGER.warn("Request body exceeds maximum size of {} bytes", MAX_BODY_SIZE);
            response.sendError(413, "Request body too large");
            return;
        }

        // Wrap the request to allow re-reading the body
        CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(request, bodyBytes);

        try {
            JsonNode jsonNode = MAPPER.readTree(bodyBytes);
            boolean stream = jsonNode.has("stream") && jsonNode.get("stream").asBoolean(false);

            LOGGER.debug("Routing filter: stream={}, contentLength={}", stream, bodyBytes.length);

            if (stream) {
                // Forward to the streaming endpoint
                request.getRequestDispatcher("/responses/streaming").forward(wrappedRequest, response);
                return;
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to parse request body as JSON for stream routing detection");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON in request body");
            return;
        }

        filterChain.doFilter(wrappedRequest, response);
    }

    /**
     * Wraps an {@link HttpServletRequest} so that the body can be read multiple times.
     */
    private static class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

        private final byte[] cachedBody;

        CachedBodyHttpServletRequest(HttpServletRequest request, byte[] body) {
            super(request);
            this.cachedBody = body;
        }

        @Override
        public jakarta.servlet.ServletInputStream getInputStream() {
            return new CachedBodyServletInputStream(cachedBody);
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }

        @Override
        public int getContentLength() {
            return cachedBody.length;
        }

        @Override
        public long getContentLengthLong() {
            return cachedBody.length;
        }
    }

    /**
     * A {@link jakarta.servlet.ServletInputStream} backed by a byte array.
     */
    private static class CachedBodyServletInputStream extends jakarta.servlet.ServletInputStream {

        private final ByteArrayInputStream delegate;

        CachedBodyServletInputStream(byte[] body) {
            this.delegate = new ByteArrayInputStream(body);
        }

        @Override
        public boolean isFinished() {
            return delegate.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(jakarta.servlet.ReadListener readListener) {
            throw new UnsupportedOperationException("setReadListener is not supported");
        }

        @Override
        public int read() {
            return delegate.read();
        }

        @Override
        public int read(byte[] b, int off, int len) {
            return delegate.read(b, off, len);
        }
    }
}

