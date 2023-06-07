// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

import com.azure.ai.openai.models.NonAzureOpenAIKeyCredential;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Pipeline policy that uses an {@link NonAzureOpenAIKeyCredential} to set the authorization key for a request to
 * an HTTP request.
 * <p>
 * Requests sent with this pipeline policy are required to use {@code HTTPS}. If the request isn't using {@code HTTPS}
 * an exception will be thrown to prevent leaking the key.
 */
public class NonAzureOpenAIKeyCredentialPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(NonAzureOpenAIKeyCredentialPolicy.class);
    private final HttpHeaderName name;
    private final NonAzureOpenAIKeyCredential credential;

    private final String prefix;

    /**
     * Creates a policy that uses the passed {@link NonAzureOpenAIKeyCredential} to set the specified header name.
     * <p>
     * The {@code prefix} will be applied before the {@link NonAzureOpenAIKeyCredential#getKey()} when setting the
     * header. A space will be inserted between {@code prefix} and credential.
     *
     * @param name The name of the key header that will be set to {@link NonAzureOpenAIKeyCredential#getKey()}.
     * @param credential The {@link NonAzureOpenAIKeyCredential} containing the authorization key to use.
     * @param prefix The prefix to apply before the credential, for example "Bearer" or "Basic".
     * @throws NullPointerException If {@code name} or {@code credential} is {@code null}.
     * @throws IllegalArgumentException If {@code name} is empty.
     */
    public NonAzureOpenAIKeyCredentialPolicy(String name, NonAzureOpenAIKeyCredential credential, String prefix) {
        this.name = validateName(name);
        this.credential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.prefix = prefix != null ? prefix.trim() : null;
    }

    private static HttpHeaderName validateName(String name) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        if (name.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'name' cannot be empty."));
        }

        return HttpHeaderName.fromString(name);
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if ("http".equals(context.getHttpRequest().getUrl().getProtocol())) {
            return FluxUtil.monoError(LOGGER,
                new IllegalStateException("Key credentials require HTTPS to prevent leaking the key."));
        }

        setCredential(context.getHttpRequest().getHeaders());
        return next.process();
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        if ("http".equals(context.getHttpRequest().getUrl().getProtocol())) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("Key credentials require HTTPS to prevent leaking the key."));
        }

        setCredential(context.getHttpRequest().getHeaders());
        return next.processSync();
    }

    void setCredential(HttpHeaders headers) {
        String credential = this.credential.getKey();
        headers.set(name, (prefix == null) ? credential : prefix + " " + credential);
    }
}
