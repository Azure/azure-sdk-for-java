// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.stress;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.azure.core.http.HttpHeaderName.X_MS_CLIENT_REQUEST_ID;

public class FaultInjectingHttpPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(FaultInjectingHttpPolicy.class);
    private static final HttpHeaderName TRACEPARENT_HEADER = HttpHeaderName.fromString("traceparent");
    private static final HttpHeaderName UPSTREAM_URI_HEADER = HttpHeaderName.fromString("X-Upstream-Base-Uri");
    private static final HttpHeaderName HTTP_FAULT_INJECTOR_RESPONSE_HEADER = HttpHeaderName.fromString("x-ms-faultinjector-response-option");
    private static final HttpHeaderName SERVER_REQUEST_ID_HEADER = HttpHeaderName.fromString("x-ms-request-id");
    private final boolean https;


    private final List<Tuple2<Double, String>> probabilities;

    public FaultInjectingHttpPolicy(boolean https, FaultInjectionProbabilities probabilities, boolean isUploadFaultsEnabled) {
        this.https = https;
        this.probabilities = new ArrayList<>();
        if (isUploadFaultsEnabled) {
            addRequestFaultedProbabilities(probabilities);
        } else {
            addResponseFaultedProbabilities(probabilities);
        }
    }

    // may remove afterwards since this is for local debugging only
    private static void logResponse(String faultType, HttpRequest request, HttpResponse response) {
        LOGGER.atInfo()
            .addKeyValue(HTTP_FAULT_INJECTOR_RESPONSE_HEADER.getCaseInsensitiveName(), faultType)
            .addKeyValue(X_MS_CLIENT_REQUEST_ID.getCaseInsensitiveName(), request.getHeaders().getValue(X_MS_CLIENT_REQUEST_ID))
            .addKeyValue(SERVER_REQUEST_ID_HEADER.getCaseInsensitiveName(), response == null ? null : response.getHeaders().getValue(SERVER_REQUEST_ID_HEADER))
            .addKeyValue(TRACEPARENT_HEADER.getCaseInsensitiveName(), request.getHeaders().getValue(TRACEPARENT_HEADER))
            .addKeyValue("responseCode", response == null ? null : response.getStatusCode())
            .log("HTTP response with fault injection");
    }

    private URL rewriteUrl(URL originalUrl) {
        try {
            return UrlBuilder.parse(originalUrl)
                .setScheme(https ? "https" : "http")
                .setHost("localhost")
                .setPort(https ? 7778 : 7777)
                .toUrl();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private String faultInjectorHandling() {

        double random = Math.random();
        double sum = 0d;

        for (Tuple2<Double, String> tup : probabilities) {
            if (random < sum + tup.getT1()) {
                return tup.getT2();
            }
            sum += tup.getT1();
        }
        return "f";
    }


    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext httpPipelineCallContext, HttpPipelineNextPolicy next) {
        HttpRequest request = httpPipelineCallContext.getHttpRequest();
        URL originalUrl = request.getUrl();
        String faultType = injectFault(request);

        return next.process()
            .map(response -> {
                logResponse(faultType, request, response);
                return cleanup(response, originalUrl);
            })
            .doOnCancel(() -> logResponse(faultType, request, null))
            .doOnError(e -> logResponse(faultType, request, null));

    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext httpPipelineCallContext, HttpPipelineNextSyncPolicy next) {
        HttpRequest request = httpPipelineCallContext.getHttpRequest();
        URL originalUrl = request.getUrl();

        String faultType = injectFault(request);
        try {
            logResponse(faultType, request, null);
            return cleanup(next.processSync(), originalUrl);
        } catch (Exception e) {
            logResponse(faultType, request, null);
            throw e;
        }
    }

    private String injectFault(HttpRequest request) {
        URL originalUrl = request.getUrl();
        request.setHeader(UPSTREAM_URI_HEADER, originalUrl.toString()).setUrl(rewriteUrl(originalUrl));
        String faultType = faultInjectorHandling();
        request.setHeader(HTTP_FAULT_INJECTOR_RESPONSE_HEADER, faultType);
        return faultType;
    }

    private HttpResponse cleanup(HttpResponse response, URL originalUrl) {
        response.getRequest().setUrl(originalUrl);
        response.getRequest().getHeaders().remove(UPSTREAM_URI_HEADER);
        return response;
    }

    private void addRequestFaultedProbabilities(FaultInjectionProbabilities probabilities) {
        // pq: Partial Request (full headers, 50% of body), then wait indefinitely
        // pqc: Partial Request (full headers, 50% of body), then close (TCP FIN)
        // pqa: Partial Request (full headers, 50% of body), then abort (TCP RST)
        // nq: No Request, then wait indefinitely
        // nqc: No Request, then close (TCP FIN)
        // nqa: No Request, then abort (TCP RST)
        this.probabilities.add(Tuples.of(probabilities.getPartialRequestIndefinite(), "pq"));
        this.probabilities.add(Tuples.of(probabilities.getPartialRequestClose(), "pqc"));
        this.probabilities.add(Tuples.of(probabilities.getPartialRequestAbort(), "pqa"));
        this.probabilities.add(Tuples.of(probabilities.getNoRequestIndefinite(), "nq"));
        this.probabilities.add(Tuples.of(probabilities.getNoRequestClose(), "nqc"));
        this.probabilities.add(Tuples.of(probabilities.getNoRequestAbort(), "nqa"));
    }

    private void addResponseFaultedProbabilities(FaultInjectionProbabilities probabilities) {
        // f: Full response
        // p: Partial Response (full headers, 50% of body), then wait indefinitely
        // pc: Partial Response (full headers, 50% of body), then close (TCP FIN)
        // pa: Partial Response (full headers, 50% of body), then abort (TCP RST)
        // pn: Partial Response (full headers, 50% of body), then finish normally
        // n: No response, then wait indefinitely
        // nc: No response, then close (TCP FIN)
        // na: No response, then abort (TCP RST)
        this.probabilities.add(Tuples.of(probabilities.getPartialResponseIndefinite(), "p"));
        this.probabilities.add(Tuples.of(probabilities.getPartialResponseClose(), "pc"));
        this.probabilities.add(Tuples.of(probabilities.getPartialResponseAbort(), "pa"));
        this.probabilities.add(Tuples.of(probabilities.getPartialResponseFinishNormal(), "pn"));
        this.probabilities.add(Tuples.of(probabilities.getNoResponseIndefinite(), "n"));
        this.probabilities.add(Tuples.of(probabilities.getNoResponseClose(), "nc"));
        this.probabilities.add(Tuples.of(probabilities.getNoResponseAbort(), "na"));
    }
}
