// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.http.policy;

import io.clientcore.core.util.Context;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineNextPolicy;
import io.clientcore.core.http.pipeline.HttpPipelinePolicy;

/**
 * The pipeline policy that override or add {@link HttpHeaders} in {@link HttpRequest} by reading values from
 * {@link Context} with key 'azure-http-headers-key'. The value for this key should be of type {@link HttpHeaders} for
 * it to be added in {@link HttpRequest}.
 *
 * <p>
 * <strong>Code Sample: Add multiple HttpHeader in Context and call client</strong>
 * </p>
 * <pre>
 * // Create ConfigurationClient for example
 * ConfigurationClient configurationClient = new ConfigurationClientBuilder()
 * .connectionString("endpoint={endpoint_value};id={id_value};secret={secret_value}")
 * .buildClient();
 * // Add your headers
 * HttpHeaders headers = new HttpHeaders();
 * headers.put("my-header1", "my-header1-value");
 * headers.put("my-header2", "my-header2-value");
 * headers.put("my-header3", "my-header3-value");
 * // Call API by passing headers in Context.
 * configurationClient.addConfigurationSettingWithResponse(
 * new ConfigurationSetting().setKey("key").setValue("value"),
 * new Context(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, headers));
 * // Above three HttpHeader will be added in outgoing HttpRequest.
 * </pre>
 *
 * @see HttpPipelinePolicy
 * @see HttpPipeline
 * @see HttpRequest
 * @see Response
 */
public class AddHeadersFromContextPolicy implements HttpPipelinePolicy {

    /**Key used to override headers in HttpRequest. The Value for this key should be {@link HttpHeaders}.*/
    public static final String AZURE_REQUEST_HTTP_HEADERS_KEY = "azure-http-headers-key";

    /**
     * Creates a new instance of {@link AddHeadersFromContextPolicy}.
     */
    public AddHeadersFromContextPolicy() {
    }

    private static void addHeadersFromContext(HttpRequest httpRequest) {
        final Object obj = httpRequest.getRequestOptions().getContext().get(AZURE_REQUEST_HTTP_HEADERS_KEY);
        if (obj instanceof HttpHeaders) {
            httpRequest.getHeaders().addAll((HttpHeaders) obj);
        }
    }

    @Override
    public Response<?> process(HttpRequest httpRequest, HttpPipelineNextPolicy next) {
        addHeadersFromContext(httpRequest);
        return next.process();
    }
}
