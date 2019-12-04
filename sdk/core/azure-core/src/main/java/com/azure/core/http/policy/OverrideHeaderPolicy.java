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
import java.util.Optional;

/**
 * The pipeline policy that override or add  {@link HttpHeader} in {@link HttpRequest} by reading values from
 * {@link Context} with key  'azure-request-override-http-headers-key'. The value for this key should be of type
 * {@link HttpHeaders} for it to be added in {@link HttpRequest}.
 */
public class OverrideHeaderPolicy implements HttpPipelinePolicy {

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {

        Optional<Object> customHttpHeadersObject = context.getData(AZURE_REQUEST_HTTP_HEADERS_KEY);
        if (!Objects.isNull(customHttpHeadersObject) && customHttpHeadersObject.get() instanceof HttpHeaders) {
            HttpHeaders customHttpHeaders = (HttpHeaders) customHttpHeadersObject.get();
            // loop through customHttpHeaders and add header in HttpRequest
            for (HttpHeader httpHeader : customHttpHeaders) {
                if (!Objects.isNull(httpHeader.getName()) && !Objects.isNull(httpHeader.getValue())) {
                    context.getHttpRequest().getHeaders().put(httpHeader.getName(), httpHeader.getValue());
                }
            }
        }
        return next.process();

    }
}
