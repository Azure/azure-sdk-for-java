// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.credential.AzureKeyCredential;

import java.util.Objects;

/**
 * The {@code AzureKeyCredentialPolicy} class is an implementation of the {@link KeyCredentialPolicy} interface. This
 * policy uses an {@link AzureKeyCredential} to set the authorization key for a request.
 *
 * <p>This class is useful when you need to authorize requests with a key from Azure. It ensures that the requests are
 * sent over HTTPS to prevent the key from being leaked.</p>
 *
 * <p>Requests sent with this pipeline policy are required to use {@code HTTPS}. If the request isn't using
 * {@code HTTPS} an exception will be thrown to prevent leaking the key.</p>
 *
 * <p>Here's a code sample of how to use this class:</p>
 *
 * <pre>
 * {@code
 * AzureKeyCredential credential = new AzureKeyCredential("my_key");
 * AzureKeyCredentialPolicy policy = new AzureKeyCredentialPolicy("my_header", credential);
 *
 * HttpPipeline pipeline = new HttpPipelineBuilder()
 *     .policies(policy, new RetryPolicy(), new CustomPolicy())
 *     .build();
 *
 * HttpRequest request = new HttpRequest(HttpMethod.GET, new URL("https://example.com"));
 * HttpResponse response = pipeline.send(request).block();
 * }
 * </pre>
 *
 * <p>In this example, an {@code AzureKeyCredentialPolicy} is created with a key and a header name. The policy is then
 * added to the pipeline. The pipeline is used to send an HTTP request, and the response is retrieved. The request will
 * include the specified header with the key as its value.</p>
 *
 * @see com.azure.core.http.policy.KeyCredentialPolicy
 * @see com.azure.core.credential.AzureKeyCredential
 * @see com.azure.core.http.HttpPipeline
 * @see com.azure.core.http.HttpRequest
 * @see com.azure.core.http.HttpResponse
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
