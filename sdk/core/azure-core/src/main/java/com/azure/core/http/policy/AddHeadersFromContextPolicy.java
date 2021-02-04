// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;
import com.azure.core.http.HttpRequest;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * The pipeline policy that override or add  {@link HttpHeaders} in {@link HttpRequest} by reading values from
 * {@link Context} with key 'azure-http-headers-key'. The value for this key should be of type {@link HttpHeaders} for
 * it to be added in {@link HttpRequest}.
 *
 * <p><strong>Code Sample: Add multiple HttpHeader in Context and call client</strong></p>
 * <pre>
 * // Create ConfigurationClient for example
 * ConfigurationClient configurationClient = new ConfigurationClientBuilder()
 *       .connectionString("endpoint={endpoint_value};id={id_value};secret={secret_value}")
 *       .buildClient();
 * // Add your headers
 * HttpHeaders headers = new HttpHeaders();
 * headers.put("my-header1", "my-header1-value");
 * headers.put("my-header2", "my-header2-value");
 * headers.put("my-header3", "my-header3-value");
 * // Call API by passing headers in Context.
 * configurationClient.addConfigurationSettingWithResponse(
 *       new ConfigurationSetting().setKey("key").setValue("value"),
 *       new Context(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, headers));
 * // Above three HttpHeader will be added in outgoing HttpRequest.
 * </pre>
 */
public class AddHeadersFromContextPolicy implements HttpPipelinePolicy {

    /**Key used to override headers in HttpRequest. The Value for this key should be {@link HttpHeaders}.*/
    public static final String AZURE_REQUEST_HTTP_HEADERS_KEY = "azure-http-headers-key";

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        context.getData(AZURE_REQUEST_HTTP_HEADERS_KEY).ifPresent(headers -> {
            if (headers instanceof HttpHeaders) {
                HttpHeaders customHttpHeaders = (HttpHeaders) headers;
                // loop through customHttpHeaders and add headers in HttpRequest
                for (HttpHeader httpHeader : customHttpHeaders) {
                    if (!Objects.isNull(httpHeader.getName()) && !Objects.isNull(httpHeader.getValue())) {
                        context.getHttpRequest().getHeaders().set(httpHeader.getName(), httpHeader.getValue());
                    }
                }
            }
        });
        return next.process();

    }
}
