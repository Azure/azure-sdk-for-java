package com.azure.monitor.opentelemetry.exporter.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.net.HttpURLConnection;

public class AzureMonitorRedirectCustomPolicy implements HttpPipelinePolicy {
    private final ClientLogger logger = new ClientLogger(AzureMonitorRedirectCustomPolicy.class);
    private final int MAX_REDIRECT_RETRIES;
    private String redirectedEndpointUrl;

    public AzureMonitorRedirectCustomPolicy() {
        this.MAX_REDIRECT_RETRIES = 10;
    }

    public AzureMonitorRedirectCustomPolicy(int max_retries) {
        this.MAX_REDIRECT_RETRIES = max_retries;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return attemptRetry(context, next, context.getHttpRequest(), 0);
    }

    private Mono<HttpResponse> attemptRetry(final HttpPipelineCallContext context,
                                            final HttpPipelineNextPolicy next,
                                            final HttpRequest originalHttpRequest,
                                            final int retryCount) {
        // make sure the context is not modified during retry, except for the URL
        context.setHttpRequest(originalHttpRequest.copy());
        if (this.redirectedEndpointUrl != null) {
            context.getHttpRequest().setUrl(this.redirectedEndpointUrl);
        }
        return next.clone().process()
            .flatMap(httpResponse -> {
                if (shouldRetryWithRedirect(httpResponse.getStatusCode(), retryCount)) {
                    String responseLocation = httpResponse.getHeaderValue("Location");
                    if(responseLocation != null && !responseLocation.equals(context.getHttpRequest().getUrl())) {
                        this.redirectedEndpointUrl = responseLocation;
                        return attemptRetry(context, next, originalHttpRequest, retryCount+1);
                    }
                }
                return Mono.just(httpResponse);
            }).onErrorResume(err -> Mono.error(new RuntimeException(
                String.format("Retry with redirect error details: %s", err.getMessage()),
                err)));
    }

    private boolean shouldRetryWithRedirect(int statusCode, int tryCount) {
        if( tryCount >= MAX_REDIRECT_RETRIES) {
            logger.verbose("Max redirect retries limit reached:%d.",MAX_REDIRECT_RETRIES);
            return false;
        }

        return statusCode == HttpURLConnection.HTTP_MOVED_TEMP
                || statusCode == HttpURLConnection.HTTP_MOVED_PERM
                || statusCode == 308;
    }

}
