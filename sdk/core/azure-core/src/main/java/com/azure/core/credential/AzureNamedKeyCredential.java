package com.azure.core.credential;

import com.azure.core.util.logging.ClientLogger;

import java.util.Objects;

/**
 * Represents a credential that uses a key to authenticate to an Azure Service.
 */
public final class AzureNamedKeyCredential {
    private final ClientLogger logger = new ClientLogger(AzureNamedKeyCredential.class);

    private volatile Tuple<String, String> credentials;

    /**
     * Creates a credential that authorizes request with the given key.
     *
     * @param name The name of the key credential.
     * @param key The key used to authorize requests.
     * @throws NullPointerException If {@code key} or {@code name} is {@code null}.
     * @throws IllegalArgumentException If {@code key} or {@code name} is an empty string.
     */
    public AzureNamedKeyCredential(String name, String key) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        Objects.requireNonNull(key, "'key' cannot be null.");
        if (name.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'name' cannot be empty."));
        }
        if (key.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'key' cannot be empty."));
        }
        this.credentials = new Tuple<>(name, key);
    }

    /**
     * Retrieves the key associated to this credential.
     *
     * @return The key being used to authorize requests.
     */
    public String getKey() {
        return credentials.getRight();
    }

    /**
     * Retrieves the name associated to this credential.
     *
     * @return The key being used to authorize requests.
     */
    public String getName() {
        return credentials.getLeft();
    }

    /**
     * Rotates the {@code name} and  {@code key} associated to this credential.
     *
     * @param name The name of the key credential.
     * @param key The new key to associated with this credential.
     * @return The updated {@code ApiKeyCredential} object.
     * @throws NullPointerException If {@code key} or {@code name} is {@code null}.
     * @throws IllegalArgumentException If {@code key} or {@code name} is an empty string.
     */
    public AzureNamedKeyCredential update(String name, String key) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        Objects.requireNonNull(key, "'key' cannot be null.");
        if (name.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'name' cannot be empty."));
        }
        if (key.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'key' cannot be empty."));
        }
        this.credentials = new Tuple<>(name, key);
        return this;
    }

    private static class Tuple<X, Y> {
        private final X left;
        private final Y right;
        public Tuple(X left, Y right) {
            this.left = left;
            this.right = right;
        }

        public X getLeft(){
            return left;
        }

        public Y getRight() {
            return right;
        }
    }
}
