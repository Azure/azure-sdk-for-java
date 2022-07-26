// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.jedis;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.util.logging.ClientLogger;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fluent credential builder for instantiating a {@link AzureJedisClient}.
 *
 * @see AzureJedisClient
 */
public class AzureJedisClientBuilder {
    private String cacheHostName;
    private Integer port;
    private String username;
    private String password;
    private TokenCredential tokenCredential;
    private RetryOptions retryOptions;

    private final ClientLogger clientLogger = new ClientLogger(AzureJedisClientBuilder.class);

    /**
     * Creates an instance of {@link AzureJedisClientBuilder}
     */
    public AzureJedisClientBuilder() {

    }

    /**
     * Sets the Azure Redis Cache Host name to connect to.
     * @param cacheHostName the cache host name.
     * @return An updated instance of this builder.
     */
    public AzureJedisClientBuilder cacheHostName(String cacheHostName) {
        this.cacheHostName = cacheHostName;
        return this;
    }

    /**
     * Sets the port to connect to.
     * @param port the port
     * @return An updated instance of this builder.
     */
    public AzureJedisClientBuilder port(Integer port) {
        this.port = port;
        return this;
    }

    /**
     * Configure the credential to be used for authentication. if a password is configured via
     * {@link AzureJedisClientBuilder#password(String)} then the credential is not required.
     *
     * @param tokenCredential the token credential
     * @return An updated instance of this builder.
     */
    public AzureJedisClientBuilder credential(TokenCredential tokenCredential) {
        this.tokenCredential = tokenCredential;
        return this;
    }

    /**
     * The username to be used for authentication.
     *
     * @param username the username
     * @return An updated instance of this builder.
     */
    public AzureJedisClientBuilder username(String username) {
        this.username = username;
        return this;
    }

    /**
     * The password to be used for authentication. If a @{@link TokenCredential} is provided via
     * {@link AzureJedisClientBuilder#credential(TokenCredential)} then password is not required.
     *
     * @param password the password to be used for authentication
     * @return An updated instance of this builder.
     */
    public AzureJedisClientBuilder password(String password) {
        this.password = password;
        return this;
    }


    /**
     * Configure the retry options.
     *
     * @param retryOptions the configuration to be used when retrying requests.
     * @return An updated instance of this builder.
     */
    public AzureJedisClientBuilder retryOptions(RetryOptions retryOptions) {
        this.retryOptions = retryOptions;
        return this;
    }

    /**
     * Build an instance of the {@link AzureJedisClient}
     *
     * @return An instance of {@link AzureJedisClient}
     */
    public Jedis build() {
        validate(this.getClass().getSimpleName(), new HashMap<String, Object>() {
            {
                this.put("cacheHostName", cacheHostName);
                this.put("port", port);
                this.put("username", username);
            }
        });
        if (this.password != null && this.tokenCredential != null) {
            throw this.clientLogger.logExceptionAsError(new IllegalArgumentException("Both Token Credential and Password are provided in AzureJedisClientBuilder. Only one of them should be provided."));
        } else {
            return tokenCredential != null ?
                new AzureJedisClient(cacheHostName, port, username, tokenCredential, retryOptions) :
                new AzureJedisClient(cacheHostName, port, username, password, retryOptions);
        }
    }

    static void validate(String className, Map<String, Object> parameters) {
        ClientLogger logger = new ClientLogger(className);
        List<String> missing = new ArrayList<>();
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            if (entry.getValue() == null) {
                missing.add(entry.getKey());
            }
        }
        if (missing.size() > 0) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("Must provide non-null values for "
                + String.join(", ", missing) + " properties in " + className));
        }
    }
}
