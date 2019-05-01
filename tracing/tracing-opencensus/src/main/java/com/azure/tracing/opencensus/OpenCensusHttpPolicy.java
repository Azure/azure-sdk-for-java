package com.azure.tracing.opencensus;

import com.azure.common.http.HttpPipelineCallContext;
import com.azure.common.http.HttpPipelineNextPolicy;
import com.azure.common.http.HttpResponse;
import com.azure.common.http.policy.HttpPipelinePolicy;
import com.azure.common.http.policy.spi.AfterRetryPolicyProvider;
import reactor.core.publisher.Mono;

// TODO is this policy before or after retry (run once or on every retry?)
public class OpenCensusHttpPolicy implements AfterRetryPolicyProvider, HttpPipelinePolicy {

    public HttpPipelinePolicy create() {
        return this;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        // code below copy / pasted from CookiePolicy for demonstration purposes
//        try {
//            final URI uri = context.httpRequest().url().toURI();
//
//            Map<String, List<String>> cookieHeaders = new HashMap<>();
//            for (HttpHeader header : context.httpRequest().headers()) {
//                cookieHeaders.put(header.name(), Arrays.asList(context.httpRequest().headers().values(header.name())));
//            }
//
//            Map<String, List<String>> requestCookies = cookies.get(uri, cookieHeaders);
//            for (Map.Entry<String, List<String>> entry : requestCookies.entrySet()) {
//                context.httpRequest().headers().set(entry.getKey(), String.join(",", entry.getValue()));
//            }
//
//            return next.process().map(httpResponse -> {
//                Map<String, List<String>> responseHeaders = new HashMap<>();
//                for (HttpHeader header : httpResponse.headers()) {
//                    responseHeaders.put(header.name(), Collections.singletonList(header.value()));
//                }
//
//                try {
//                    cookies.put(uri, responseHeaders);
//                } catch (IOException e) {
//                    throw Exceptions.propagate(e);
//                }
//                return httpResponse;
//            });
//        } catch (URISyntaxException | IOException e) {
//            return Mono.error(e);
//        }
        return Mono.empty();
    }
}
