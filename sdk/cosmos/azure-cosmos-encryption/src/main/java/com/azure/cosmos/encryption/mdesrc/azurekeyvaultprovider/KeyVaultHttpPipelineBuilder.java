/*
 * Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT License.
 */

package com.azure.cosmos.encryption.mdesrc.azurekeyvaultprovider;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.cosmos.encryption.mdesrc.cryptography.MicrosoftDataEncryptionException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * The HTTP pipeline builder which includes all the necessary HTTP pipeline policies that will be applied for
 * sending and receiving HTTP requests to the Key Vault service.
 */
final class KeyVaultHttpPipelineBuilder {

    private final List<HttpPipelinePolicy> policies;
    private AzureKeyVaultProviderTokenCredential credential;
    private HttpLogOptions httpLogOptions;
    private final RetryPolicy retryPolicy;

    /**
     * The constructor with default retry policy and log options.
     */
    KeyVaultHttpPipelineBuilder() {
        retryPolicy = new RetryPolicy();
        httpLogOptions = new HttpLogOptions();
        policies = new ArrayList<>();
    }

    /**
     * Builds the HTTP pipeline with all the necessary HTTP policies included in the pipeline.
     *
     * @return A fully built HTTP pipeline including the default HTTP client.
     * @throws MicrosoftDataEncryptionException If the {@link KeyVaultCustomCredentialPolicy} policy cannot be added to the pipeline.
     */
    HttpPipeline buildPipeline() throws MicrosoftDataEncryptionException {
        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        policies.add(retryPolicy);
        policies.add(new com.azure.cosmos.encryption.mdesrc.azurekeyvaultprovider.KeyVaultCustomCredentialPolicy(credential));
        policies.addAll(this.policies);
        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(httpLogOptions));

        return new HttpPipelineBuilder().policies(policies.toArray(new HttpPipelinePolicy[0])).build();
    }

    /**
     * Sets the credential to use when authenticating HTTP requests.
     *
     * @param credential
     *        The credential to use for authenticating HTTP requests.
     * @return the updated KVHttpPipelineBuilder object.
     * @throws MicrosoftDataEncryptionException
     */
    KeyVaultHttpPipelineBuilder credential(
            AzureKeyVaultProviderTokenCredential credential) throws MicrosoftDataEncryptionException {
        if (null == credential) {
            MessageFormat form = new MessageFormat(MicrosoftDataEncryptionException.getErrString("R_CannotBeNull"));
            Object[] msgArgs1 = {"Credential"};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs1));
        }

        this.credential = credential;
        return this;
    }
}
