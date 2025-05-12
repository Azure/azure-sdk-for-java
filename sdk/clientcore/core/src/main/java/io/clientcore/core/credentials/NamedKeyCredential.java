// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.credentials;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.util.Objects;

/**
 * <p>
 * The {@link NamedKeyCredential} is used to authenticate and authorize requests made to services.
 * It is specifically designed for scenarios where you need to authenticate using a key with a name identifier
 * associated with it.
 * </p>
 *
 * <p>
 * A key is a unique identifier or token that is associated with a specific user or application. It serves as a
 * simple form of authentication to ensure that only authorized clients can access the protected resources or APIs.
 * This authentication is commonly used for accessing certain services. Each service may have its own specific way of
 * using API keys, but the general concept remains the same.
 * </p>
 *
 * <p>
 * The {@link NamedKeyCredential} can be created for keys which have a name
 * identifier associated with them.
 * </p>
 *
 * <p>
 * <strong>Code Samples</strong>
 * </p>
 *
 * <p>
 * Create a named credential for a service specific sas key.
 * </p>
 *
 * <!-- src_embed io.clientcore.core.credential.NamedKeyCredential.constructor -->
 * <pre>
 * &#47;&#47; Create a named credential for a service.
 * NamedKeyCredential namedKeyCredential = new NamedKeyCredential&#40;&quot;SERVICE-KEY-NAME&quot;, &quot;SERVICE-KEY&quot;&#41;;
 * </pre>
 * <!-- end io.clientcore.core.credential.NamedKeyCredential.constructor -->
 *
 * @see io.clientcore.core.credentials
 */
@Metadata(properties = MetadataProperties.FLUENT)
public final class NamedKeyCredential {
    // NamedKeyCredential is a commonly used credential type, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(NamedKeyCredential.class);

    private volatile NamedKey credentials;

    /**
     * Creates a credential with specified {@code name} that authorizes request with the given {@code key}.
     *
     * @param name The name of the key credential.
     * @param key The key used to authorize requests.
     * @throws NullPointerException If {@code key} or {@code name} is {@code null}.
     * @throws IllegalArgumentException If {@code key} or {@code name} is an empty string.
     */
    public NamedKeyCredential(String name, String key) {
        validateInputParameters(name, key);
        this.credentials = new NamedKey(name, key);
    }

    /**
     * Retrieves the {@link NamedKey} containing the name and key associated with this credential.
     *
     * @return The {@link NamedKey} containing the name and key .
     */
    public NamedKey getNamedKey() {
        return this.credentials;
    }

    /**
     * Rotates the {@code name} and  {@code key} associated to this credential.
     *
     * @param name The new name of the key credential.
     * @param key The new key to be associated with this credential.
     * @return The updated {@code NamedKeyCredential} object.
     * @throws NullPointerException If {@code key} or {@code name} is {@code null}.
     * @throws IllegalArgumentException If {@code key} or {@code name} is an empty string.
     */
    public NamedKeyCredential update(String name, String key) {
        validateInputParameters(name, key);
        this.credentials = new NamedKey(name, key);
        return this;
    }

    private void validateInputParameters(String name, String key) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        Objects.requireNonNull(key, "'key' cannot be null.");
        if (name.isEmpty()) {
            throw LOGGER.throwableAtError().log("'name' cannot be empty.", IllegalArgumentException::new);
        }
        if (key.isEmpty()) {
            throw LOGGER.throwableAtError().log("'key' cannot be empty.", IllegalArgumentException::new);
        }
    }
}
