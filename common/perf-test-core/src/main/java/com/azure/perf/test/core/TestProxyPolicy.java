// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.CoreUtils;

import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URI;

public class TestProxyPolicy implements HttpPipelinePolicy {
    private final URI uri;
    
    private String recordingId;
    private String mode;

    public TestProxyPolicy(URI uri) {
        this.uri = uri;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getRecordingId() {
        return recordingId;
    }

    public void setRecordingId(String recordingId) {
        this.recordingId = recordingId;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if (!CoreUtils.isNullOrEmpty(recordingId) && !CoreUtils.isNullOrEmpty(mode)) {
            RedirectToTestProxy(context.getHttpRequest());
        }

        return next.process();
    }

    private void RedirectToTestProxy(HttpRequest request) {
        request.setHeader("x-recording-id", recordingId);
        request.setHeader("x-recording-mode", mode);
        request.setHeader("x-recording-remove", Boolean.toString(false));

        try {
            URI requestUri = request.getUrl().toURI();

            // Ensure x-recording-upstream-base-uri header is only set once, since the same HttpRequest may be reused on retries
            if (request.getHeaders().get("x-recording-upstream-base-uri") == null) {
                URI baseUri = new URI(requestUri.getScheme(), requestUri.getUserInfo(), requestUri.getHost(), requestUri.getPort(),
                    null, null, null);
                request.setHeader("x-recording-upstream-base-uri", baseUri.toString());
            }

            URI testProxyUri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(),
                requestUri.getPath(), requestUri.getQuery(), requestUri.getFragment());
            request.setUrl(testProxyUri.toURL());
        }
        catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
        catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public HttpPipelinePosition getPipelinePosition() {
        // The policy should be ran per retry in case calls are made to a secondary, fail-over host.
        return HttpPipelinePosition.PER_RETRY;
    }
}
