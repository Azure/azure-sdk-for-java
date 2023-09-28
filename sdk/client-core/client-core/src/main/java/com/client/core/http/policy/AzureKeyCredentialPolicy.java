// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.client.core.http.policy;

import com.client.core.credential.ClientKeyCredential;

import java.util.Objects;

/**
 * Pipeline policy that uses an {@link ClientKeyCredential} to set the authorization key for a request.
 * <p>
 * Requests sent with this pipeline policy are required to use {@code HTTPS}. If the request isn't using {@code HTTPS}
 * an exception will be thrown to prevent leaking the key.
 */
public final class ClientKeyCredentialPolicy extends KeyCredentialPolicy {
    /**
     * Creates a policy that uses the passed {@link ClientKeyCredential} to set the specified header name.
     *
     * @param name The name of the key header that will be set to {@link ClientKeyCredential#getKey()}.
     * @param credential The {@link ClientKeyCredential} containing the authorization key to use.
     * @throws NullPointerException If {@code name} or {@code credential} is {@code null}.
     * @throws IllegalArgumentException If {@code name} is empty.
     */
    public ClientKeyCredentialPolicy(String name, ClientKeyCredential credential) {
        super(name, credential, null);
    }

    /**
     * Creates a policy that uses the passed {@link ClientKeyCredential} to set the specified header name.
     * <p>
     * The {@code prefix} will be applied before the {@link ClientKeyCredential#getKey()} when setting the header. A
     * space will be inserted between {@code prefix} and credential.
     *
     * @param name The name of the key header that will be set to {@link ClientKeyCredential#getKey()}.
     * @param credential The {@link ClientKeyCredential} containing the authorization key to use.
     * @param prefix The prefix to apply before the credential, for example "SharedAccessKey credential".
     * @throws NullPointerException If {@code name} or {@code credential} is {@code null}.
     * @throws IllegalArgumentException If {@code name} is empty.
     */
    public ClientKeyCredentialPolicy(String name, ClientKeyCredential credential, String prefix) {
        super(name, Objects.requireNonNull(credential, "'credential' cannot be null."), prefix);
    }
}
