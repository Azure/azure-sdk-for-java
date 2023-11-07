package com.azure.storage.stress;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class HttpFaultInjectingHttpClient implements HttpClient {
    private static final ClientLogger LOGGER = new ClientLogger(HttpFaultInjectingHttpClient.class);
    private static final String UPSTREAM_URI_HEADER = "X-Upstream-Base-Uri";
    private static final String HTTP_FAULT_INJECTOR_RESPONSE_HEADER = "x-ms-faultinjector-response-option";
    public static final Object FAULT_TRACKING_CONTEXT_KEY = "fault-tracking";
    private final HttpClient wrappedHttpClient;
    private final boolean https;

    private final List<Tuple2<Double, String>> probabilities;

    public HttpFaultInjectingHttpClient(HttpClient wrappedHttpClient, boolean https, FaultInjectionProbabilities probabilities) {
        this.wrappedHttpClient = wrappedHttpClient;
        this.https = https;

        // f: Full response
        // p: Partial Response (full headers, 50% of body), then wait indefinitely
        // pc: Partial Response (full headers, 50% of body), then close (TCP FIN)
        // pa: Partial Response (full headers, 50% of body), then abort (TCP RST)
        // pn: Partial Response (full headers, 50% of body), then finish normally
        // n: No response, then wait indefinitely
        // nc: No response, then close (TCP FIN)
        // na: No response, then abort (TCP RST)
        this.probabilities = new ArrayList<>();
        this.probabilities.add(Tuples.of(probabilities.getPartialResponseIndefinite(), "p"));
        this.probabilities.add(Tuples.of(probabilities.getPartialResponseClose(), "pc"));
        this.probabilities.add(Tuples.of(probabilities.getPartialResponseAbort(), "pa"));
        this.probabilities.add(Tuples.of(probabilities.getPartialResponseFinishNormal(), "pn"));
        this.probabilities.add(Tuples.of(probabilities.getNoResponseIndefinite(), "n"));
        this.probabilities.add(Tuples.of(probabilities.getNoResponseClose(), "nc"));
        this.probabilities.add(Tuples.of(probabilities.getNoResponseAbort(), "na"));
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        return send(request, Context.NONE);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Mono<HttpResponse> send(HttpRequest request, Context context) {
        URL originalUrl = request.getUrl();
        request.setHeader(UPSTREAM_URI_HEADER, originalUrl.toString()).setUrl(rewriteUrl(originalUrl));
        String faultType = faultInjectorHandling();
        request.setHeader(HTTP_FAULT_INJECTOR_RESPONSE_HEADER, faultType);

        return wrappedHttpClient.send(request, context)
            .map(response -> {
                HttpRequest request1 = response.getRequest();
                request1.getHeaders().remove(UPSTREAM_URI_HEADER);
                request1.setUrl(originalUrl);
                logResponse(faultType, request, response);
                return response;
            })
            .doOnCancel(() -> logResponse(faultType,  request, null))
            .doOnError(e -> logResponse(faultType, request,null));
    }

    private static void logResponse(String faultType, HttpRequest request, HttpResponse response) {
        // TODO (limolkova): move it to policy and leverage SDK logging policy instead.
        LOGGER.atInfo()
            .addKeyValue(HTTP_FAULT_INJECTOR_RESPONSE_HEADER, faultType)
            .addKeyValue("x-ms-client-request-id", request.getHeaders().getValue(HttpHeaderName.X_MS_CLIENT_REQUEST_ID))
            .addKeyValue("x-ms-request-id", response == null ? null : response.getHeaders().getValue("x-ms-request-id"))
            .addKeyValue("responseCode", response == null ? null : response.getStatusCode())
            .log("HTTP response with fault injection");
    }

    @SuppressWarnings("unchecked")
    @Override
    public HttpResponse sendSync(HttpRequest request, Context context) {
        URL originalUrl = request.getUrl();
        request.setHeader(UPSTREAM_URI_HEADER, originalUrl.toString()).setUrl(rewriteUrl(originalUrl));
        String faultType = faultInjectorHandling();
        request.setHeader(HTTP_FAULT_INJECTOR_RESPONSE_HEADER, faultType);

        HttpResponse response = null;
        try {
            response = wrappedHttpClient.sendSync(request, context);
            response.getRequest().setUrl(originalUrl);
            response.getRequest().getHeaders().remove(UPSTREAM_URI_HEADER);
        } finally {
            logResponse(faultType, request, response);
        }
        return response;
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
}
