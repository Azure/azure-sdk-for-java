// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.credentials.SasTokenCredential;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Policy that adds the SAS token to the request URL's query.
 */
public final class SasTokenCredentialPolicy implements HttpPipelinePolicy {
    private final ClientLogger logger = new ClientLogger(SasTokenCredentialPolicy.class);

    private final SasTokenCredential credential;

    /**
     * Creates a SAS token credential policy that appends the SAS token to the request URL's query.
     *
     * @param credential SAS token credential
     */
    public SasTokenCredentialPolicy(SasTokenCredential credential) {
        this.credential = credential;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        try {
            URL requestURL = context.getHttpRequest().getUrl();
            String delimiter = !CoreUtils.isNullOrEmpty(requestURL.getQuery()) ? "&" : "?";

            String newURL = requestURL.toString() + delimiter + credential.getSasToken();
            context.getHttpRequest().setUrl(new URL(newURL));
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(new IllegalStateException(ex));
        }

        return next.process();
    }
}
