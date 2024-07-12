// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.http.policy;

import com.azure.core.v2.credential.AzureKeyCredential;

import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.pipeline.KeyCredentialPolicy;
import java.util.Objects;

/**
 * The {@code AzureKeyCredentialPolicy} class is an implementation of the {@link KeyCredentialPolicy} interface. This
 * policy uses an {@link AzureKeyCredential} to set the authorization key for a request.
 *
 * <p>This class is useful when you need to authorize requests with a key from Azure.</p>
 *
 * <p>Requests sent with this pipeline policy are required to use {@code HTTPS}. If the request isn't using
 * {@code HTTPS} an exception will be thrown to prevent leaking the key.</p>
 *
 * <p><strong>Code sample:</strong></p>
 *
 * <p>In this example, an {@code AzureKeyCredentialPolicy} is created with a key and a header name. The policy
 * can be added to a pipeline. The requests sent by the pipeline will then include the specified header with the
 * key as its value.</p>
 *
 * <!-- src_embed com.azure.core.http.policy.AzureKeyCredentialPolicy.constructor -->
 * <!-- end com.azure.core.http.policy.AzureKeyCredentialPolicy.constructor -->
 *
 * @see com.azure.core.http.policy
 * @see KeyCredentialPolicy
 * @see com.azure.core.v2.credential.AzureKeyCredential
 * @see HttpPipeline
 * @see HttpRequest
 * @see io.clientcore.core.http.models.Response
 */
public final class AzureKeyCredentialPolicy extends KeyCredentialPolicy {
    /**
     * Creates a policy that uses the passed {@link AzureKeyCredential} to set the specified header name.
     *
     * @param name The name of the key header that will be set to {@link AzureKeyCredential#getKey()}.
     * @param credential The {@link AzureKeyCredential} containing the authorization key to use.
     * @throws NullPointerException If {@code name} or {@code credential} is {@code null}.
     * @throws IllegalArgumentException If {@code name} is empty.
     */
    public AzureKeyCredentialPolicy(String name, AzureKeyCredential credential) {
        super(name, credential, null);
    }

    /**
     * Creates a policy that uses the passed {@link AzureKeyCredential} to set the specified header name.
     * <p>
     * The {@code prefix} will be applied before the {@link AzureKeyCredential#getKey()} when setting the header. A
     * space will be inserted between {@code prefix} and credential.
     *
     * @param name The name of the key header that will be set to {@link AzureKeyCredential#getKey()}.
     * @param credential The {@link AzureKeyCredential} containing the authorization key to use.
     * @param prefix The prefix to apply before the credential, for example "SharedAccessKey credential".
     * @throws NullPointerException If {@code name} or {@code credential} is {@code null}.
     * @throws IllegalArgumentException If {@code name} is empty.
     */
    public AzureKeyCredentialPolicy(String name, AzureKeyCredential credential, String prefix) {
        super(name, Objects.requireNonNull(credential, "'credential' cannot be null."), prefix);
    }
}
