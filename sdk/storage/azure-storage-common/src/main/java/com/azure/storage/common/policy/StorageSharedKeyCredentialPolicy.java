// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.policy.HttpPipelineSynchronousPolicy;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.Constants;

/**
 * Policy that adds the SharedKey into the request's Authorization header.
 */
public final class StorageSharedKeyCredentialPolicy extends HttpPipelineSynchronousPolicy {
    private final StorageSharedKeyCredential credential;

    /**
     * Creates a SharedKey pipeline policy that adds the SharedKey into the request's authorization header.
     *
     * @param credential the SharedKey credential used to create the policy.
     */
    public StorageSharedKeyCredentialPolicy(StorageSharedKeyCredential credential) {
        this.credential = credential;
    }

    /**
     * @return the {@link StorageSharedKeyCredential} linked to the policy.
     */
    public StorageSharedKeyCredential sharedKeyCredential() {
        return this.credential;
    }

    @Override
    protected void beforeSendingRequest(HttpPipelineCallContext context) {
        String authorizationValue = credential.generateAuthorizationHeader(context.getHttpRequest().getUrl(),
            context.getHttpRequest().getHttpMethod().toString(),
            context.getHttpRequest().getHeaders(),
            Boolean.TRUE.equals(context.getData(Constants.STORAGE_LOG_STRING_TO_SIGN).orElse(false)));
        context.getHttpRequest().setHeader("Authorization", authorizationValue);
    }
}
