// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.policy.HttpPipelineSynchronousPolicy;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.credentials.SasTokenCredential;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Policy that adds the SAS token to the request URL's query.
 * @deprecated Use {@link com.azure.core.http.policy.AzureSasCredentialPolicy} instead.
 */
@Deprecated
public final class SasTokenCredentialPolicy extends HttpPipelineSynchronousPolicy {
    private static final ClientLogger LOGGER = new ClientLogger(SasTokenCredentialPolicy.class);

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
    protected void beforeSendingRequest(HttpPipelineCallContext context) {
        try {
            URL requestURL = context.getHttpRequest().getUrl();
            String delimiter = !CoreUtils.isNullOrEmpty(requestURL.getQuery()) ? "&" : "?";

            String newURL = requestURL + delimiter + credential.getSasToken();
            context.getHttpRequest().setUrl(new URL(newURL));
        } catch (MalformedURLException ex) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(ex));
        }
    }
}
