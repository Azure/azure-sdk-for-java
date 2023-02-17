// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Pipeline policy that uses an {@link AzureKeyCredential} to set the authorization key for a request.
 * <p>
 * Requests sent with this pipeline policy are required to use {@code HTTPS}. If the request isn't using {@code HTTPS}
 * an exception will be thrown to prevent leaking the key.
 */
public final class AzureKeyCredentialPolicy implements HttpPipelinePolicy {
    // AzureKeyCredentialPolicy can be a commonly used policy, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(AzureKeyCredentialPolicy.class);
    private final HttpHeaderName[] names;
    private final AzureKeyCredential credential;

    private final HttpPipelineSyncPolicy inner = new HttpPipelineSyncPolicy() {
        @Override
        protected void beforeSendingRequest(HttpPipelineCallContext context) {
            if ("http".equals(context.getHttpRequest().getUrl().getProtocol())) {
                throw LOGGER.logExceptionAsError(
                    new IllegalStateException("Key credentials require HTTPS to prevent leaking the key."));
            }

            HttpHeaders headers = context.getHttpRequest().getHeaders();
            String[] keys = credential.getKeys();
            for (int i = 0; i < names.length; i++) {
                headers.set(names[i], keys[i]);
            }
        }
    };

    /**
     * Creates a policy that uses the passed {@link AzureKeyCredential} to set the specified header name.
     * <p>
     * If the number of header names and keys provided by the credential differ this will throw an
     * {@link IllegalArgumentException}.
     *
     * @param name The name of the key header that will be set to {@link AzureKeyCredential#getKey()}.
     * @param credential The {@link AzureKeyCredential} containing the authorization key to use.
     * @throws NullPointerException If {@code name} or {@code credential} is null.
     * @throws IllegalArgumentException If {@code name} is empty or the number of keys provided by the credential isn't
     * one.
     */
    public AzureKeyCredentialPolicy(String name, AzureKeyCredential credential) {
        Objects.requireNonNull(credential, "The 'credential' must not be null as null AzureKeyCredential cannot "
            + "provide a key value.");
        if (credential.getKeys().length > 1) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("The number of keys provided by 'credential' "
                + "must be one."));
        }

        this.names = new HttpHeaderName[] {validateName(name)};
        this.credential = credential;
    }

    /**
     * Creates a policy that uses the passed {@link AzureKeyCredential} to set the specified header names.
     * <p>
     * The order of the {@code names} must match the order of keys provided by {@code credential}. For example, if the
     * names were, in this order, {@code api-key-name}, {@code subscription-key-name} the order of the keys in the
     * credential must be {@code api-key-value}, {@code subscription-key-value}. This policy provides no safety guards
     * on incorrect ordering of names and keys, the only guarantee provided is that if the number of names and keys
     * differ an {@link IllegalArgumentException} will be thrown.
     *
     * @param credential The {@link AzureKeyCredential} containing authorization keys to use.
     * @param names The names of the key headers that will be set to {@link AzureKeyCredential#getKeys()}. The ordering
     * of names must align the ordering of the keys in the credential.
     * @throws NullPointerException If {@code names} or {@code credential} is null or any name in {@code names} is null.
     * @throws IllegalArgumentException If any name in {@code names} is empty or the number of names doesn't match the
     * number of keys provided by the credential.
     */
    public AzureKeyCredentialPolicy(AzureKeyCredential credential, String... names) {
        Objects.requireNonNull(credential, "The 'credential' must not be null as null AzureKeyCredential cannot "
            + "provide a key value.");
        Objects.requireNonNull(names, "The 'names' must not be null as null names cannot provide a header to set for "
            + "authorization.");
        if (names.length != credential.getKeys().length) {
            throw new IllegalArgumentException("The number of 'names' and keys provided by 'credential' must be the "
                + "same, otherwise there will be a header without a key or a key without a header name.");
        }

        this.names = new HttpHeaderName[names.length];
        this.credential = credential;

        for (int i = 0; i < names.length; i++) {
            this.names[i] = validateName(names[i]);
        }
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return inner.process(context, next);
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        return inner.processSync(context, next);
    }

    private static HttpHeaderName validateName(String name) {
        Objects.requireNonNull(name, "The 'name' provided must not be null as null header names are invalid.");
        if (name.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("The 'name' provided must not be empty as "
                + "empty header names are invalid."));
        }

        return HttpHeaderName.fromString(name);
    }
}
