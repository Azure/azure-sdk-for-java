// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.util.Context;

import java.net.URL;

public class AddHeadersFromContextPolicyJavaDocCodeSnippet {

    /**
     *
     * @throws Exception in case of error in sending the request.
     */
    public void clientProvidedMultipleHeader() throws Exception {
        Context context = null;
        // BEGIN: com.azure.core.http.policy.AddHeadersFromContextPolicy.provideContexWhenSendingRequest
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new AddHeadersFromContextPolicy())
            .build();

        pipeline.send(new HttpRequest(HttpMethod.GET, new URL("http://localhost/")), context).block();
        // END: com.azure.core.http.policy.AddHeadersFromContextPolicy.provideContexWhenSendingRequest
    }

    /**
     *
     * @throws Exception in case of error in creating Context.
     */
    public void clientProvidedMultipleHeaderInContext() throws Exception {
        // BEGIN: com.azure.core.http.policy.AddHeadersFromContextPolicy.multipleCustomHeaderInContext
        final HttpHeaders headers = new HttpHeaders();
        headers.put("x-ms-client-request-id", "my-custom-request-id-value");
        headers.put("my-header1", "my-header1-value");
        headers.put("my-header2", "my-header2-value");

        new Context(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, headers);
        //The generated HttpRequest will have above three headers set with specified values.
        // END: com.azure.core.http.policy.AddHeadersFromContextPolicy.multipleCustomHeaderInContext
    }
}
