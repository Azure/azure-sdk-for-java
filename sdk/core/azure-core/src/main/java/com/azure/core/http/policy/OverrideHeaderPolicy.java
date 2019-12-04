package com.azure.core.http.policy;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;
import com.azure.core.http.HttpRequest;
import static com.azure.core.util.Context.AZURE_REQUEST_HTTP_HEADERS_KEY;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * The pipeline policy that override or add  {@link HttpHeader} in {@link HttpRequest} by reading values from
 * {@link Context} with key  'azure-request-override-http-headers-key'.
 */
public class OverrideHeaderPolicy implements HttpPipelinePolicy {

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if (!Objects.isNull(context.getData(AZURE_REQUEST_HTTP_HEADERS_KEY))) {
            HttpHeaders customHttpHeaders = (HttpHeaders) context.getData(AZURE_REQUEST_HTTP_HEADERS_KEY).get();
            // loop through customHttpHeaders and add header in HttpRequest
            for (HttpHeader httpHeader : customHttpHeaders) {
                context.getHttpRequest().getHeaders().put(httpHeader.getName(), httpHeader.getValue());
            }
        }
        return next.process();

    }
}
