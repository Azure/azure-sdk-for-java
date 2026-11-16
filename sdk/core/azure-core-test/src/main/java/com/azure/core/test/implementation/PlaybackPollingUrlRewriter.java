// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.implementation;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.CoreUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Rewrites long-running operation polling URLs in test proxy playback responses onto the host and port of the request
 * that produced the response.
 * <p>
 * Recordings keep polling URLs with sanitized or real service hosts, such as
 * {@code https://REDACTED.cognitiveservices.azure.com/...} or {@code https://management.azure.com/...}, while playback
 * requests are sent to the endpoint configured for playback, such as {@code https://localhost:8080}. Rewriting the
 * polling URLs keeps polling on the playback endpoint.
 * <p>
 * Rewritten URLs still match the recorded requests. The default URL sanitizer replaces the host and port of request
 * URIs with {@code REDACTED} both when recording and when matching in playback, so changing them has no effect on
 * matching. The scheme isn't sanitized, so it's kept as recorded.
 */
public final class PlaybackPollingUrlRewriter {
    private static final String HTTP = "http";
    private static final String HTTPS = "https";

    private PlaybackPollingUrlRewriter() {
    }

    /**
     * Rewrites the {@code Operation-Location}, {@code Azure-AsyncOperation}, and {@code Location} response headers
     * onto the host and port of the response's request.
     * <p>
     * Only absolute HTTP and HTTPS URLs are rewritten, keeping their scheme, path, query, and fragment and dropping any
     * user information. Relative and other values are left as recorded. {@code Location} isn't rewritten on redirect
     * responses as it's followed by the redirect policy rather than used for polling.
     *
     * @param response The playback response, with its request URL restored to the original request URL.
     * @return A response with the rewritten polling URLs, or {@code response} if no polling URL needs rewriting.
     */
    public static HttpResponse rewritePollingUrls(HttpResponse response) {
        URL requestUrl = response.getRequest().getUrl();
        HttpHeaders headers = response.getHeaders();

        HttpHeaders rewritten = rewriteHeader(headers, null, HttpHeaderName.OPERATION_LOCATION, requestUrl);
        rewritten = rewriteHeader(headers, rewritten, HttpHeaderName.AZURE_ASYNCOPERATION, requestUrl);

        int statusCode = response.getStatusCode();
        if (statusCode < 300 || statusCode >= 400) {
            rewritten = rewriteHeader(headers, rewritten, HttpHeaderName.LOCATION, requestUrl);
        }

        return (rewritten == null) ? response : new RewrittenHeadersHttpResponse(response, rewritten);
    }

    private static HttpHeaders rewriteHeader(HttpHeaders original, HttpHeaders rewritten, HttpHeaderName headerName,
        URL requestUrl) {
        HttpHeader header = original.get(headerName);
        if (header == null) {
            return rewritten;
        }

        List<String> values = header.getValuesList();
        List<String> rewrittenValues = new ArrayList<>(values.size());
        boolean changed = false;
        for (String value : values) {
            String rewrittenValue = rewriteUrl(value, requestUrl);
            changed |= !Objects.equals(rewrittenValue, value);
            rewrittenValues.add(rewrittenValue);
        }

        if (!changed) {
            return rewritten;
        }

        HttpHeaders updated = (rewritten == null) ? new HttpHeaders(original) : rewritten;
        return updated.set(headerName, rewrittenValues);
    }

    private static String rewriteUrl(String url, URL requestUrl) {
        if (CoreUtils.isNullOrEmpty(url)) {
            return url;
        }

        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException ignored) {
            // Values that aren't valid URIs are left as recorded.
            return url;
        }

        String scheme = uri.getScheme();
        if (uri.getRawAuthority() == null || !(HTTP.equalsIgnoreCase(scheme) || HTTPS.equalsIgnoreCase(scheme))) {
            return url;
        }

        StringBuilder builder
            = new StringBuilder(url.length() + 32).append(scheme).append("://").append(requestUrl.getHost());
        if (requestUrl.getPort() != -1) {
            builder.append(':').append(requestUrl.getPort());
        }
        if (uri.getRawPath() != null) {
            builder.append(uri.getRawPath());
        }
        if (uri.getRawQuery() != null) {
            builder.append('?').append(uri.getRawQuery());
        }
        if (uri.getRawFragment() != null) {
            builder.append('#').append(uri.getRawFragment());
        }

        return builder.toString();
    }

    private static final class RewrittenHeadersHttpResponse extends HttpResponse {
        private final HttpResponse response;
        private final HttpHeaders headers;

        RewrittenHeadersHttpResponse(HttpResponse response, HttpHeaders headers) {
            super(response.getRequest());
            this.response = response;
            this.headers = headers;
        }

        @Override
        public int getStatusCode() {
            return response.getStatusCode();
        }

        @Override
        @Deprecated
        public String getHeaderValue(String name) {
            return headers.getValue(name);
        }

        @Override
        public String getHeaderValue(HttpHeaderName headerName) {
            return headers.getValue(headerName);
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }

        @Override
        public Flux<ByteBuffer> getBody() {
            return response.getBody();
        }

        @Override
        public BinaryData getBodyAsBinaryData() {
            return response.getBodyAsBinaryData();
        }

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            return response.getBodyAsByteArray();
        }

        @Override
        public Mono<String> getBodyAsString() {
            return response.getBodyAsString();
        }

        @Override
        public Mono<String> getBodyAsString(Charset charset) {
            return response.getBodyAsString(charset);
        }

        @Override
        public Mono<InputStream> getBodyAsInputStream() {
            return response.getBodyAsInputStream();
        }

        @Override
        public InputStream getBodyAsInputStreamSync() {
            return response.getBodyAsInputStreamSync();
        }

        @Override
        public Mono<Void> writeBodyToAsync(AsynchronousByteChannel channel) {
            return response.writeBodyToAsync(channel);
        }

        @Override
        public void writeBodyTo(WritableByteChannel channel) throws IOException {
            response.writeBodyTo(channel);
        }

        @Override
        public void close() {
            response.close();
        }
    }
}
