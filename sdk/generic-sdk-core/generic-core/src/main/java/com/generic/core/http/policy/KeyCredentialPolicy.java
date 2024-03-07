// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.policy;

import com.generic.core.credential.KeyCredential;
import com.generic.core.http.Response;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.pipeline.HttpPipelineNextPolicy;
import com.generic.core.http.pipeline.HttpPipelinePolicy;
import com.generic.core.models.HeaderName;
import com.generic.core.models.Headers;
import com.generic.core.util.ClientLogger;

import java.util.Objects;

/**
 * Pipeline policy that uses an {@link KeyCredential} to set the authorization key for a request.
 *
 * <p>Requests sent with this pipeline policy are required to use {@code HTTPS}. If the request isn't using {@code HTTPS}
 * an exception will be thrown to prevent leaking the key.</p>
 */
public class KeyCredentialPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(KeyCredentialPolicy.class);
    private final HeaderName name;
    private final KeyCredential credential;
    private final String prefix;

    /**
     * Creates a policy that uses the passed {@link KeyCredential} to set the specified header name.
     *
     * <p>The {@code prefix} will be applied before the {@link KeyCredential#getKey()} when setting the header. A space
     * will be inserted between {@code prefix} and credential.</p>
     *
     * @param name The name of the key header that will be set to {@link KeyCredential#getKey()}.
     * @param credential The {@link KeyCredential} containing the authorization key to use.
     * @param prefix The prefix to apply before the credential, for example "SharedAccessKey credential".
     *
     * @throws NullPointerException If {@code name} or {@code credential} is {@code null}.
     * @throws IllegalArgumentException If {@code name} is empty.
     */
    public KeyCredentialPolicy(String name, KeyCredential credential, String prefix) {
        this(validateName(name), Objects.requireNonNull(credential, "'credential' cannot be null."), prefix);
    }

    private static HeaderName validateName(String name) {
        Objects.requireNonNull(name, "'name' cannot be null.");

        if (name.isEmpty()) {
            throw LOGGER.logThrowableAsError(new IllegalArgumentException("'name' cannot be empty."));
        }

        return HeaderName.fromString(name);
    }

    KeyCredentialPolicy(HeaderName name, KeyCredential credential, String prefix) {
        this.name = name;
        this.credential = credential;
        this.prefix = prefix != null ? prefix.trim() : null;
    }

    @Override
    public Response<?> process(HttpRequest httpRequest, HttpPipelineNextPolicy next) {
        if ("http".equals(httpRequest.getUrl().getProtocol())) {
            throw LOGGER.logThrowableAsError(
                new IllegalStateException("Key credentials require HTTPS to prevent leaking the key."));
        }

        setCredential(httpRequest.getHeaders());

        return next.process();
    }

    void setCredential(Headers headers) {
        String credential = this.credential.getKey();

        headers.set(name, (prefix == null) ? credential : prefix + " " + credential);
    }
}
