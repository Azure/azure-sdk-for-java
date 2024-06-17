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
    private TokenCredential tokenCredential;
    private RetryOptions retryOptions;
    private boolean useSSL;

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
     * Configure the credential to be used for authentication.
     *
     * @param tokenCredential the token credential
     * @return An updated instance of this builder.
     */
    public AzureJedisClientBuilder credential(TokenCredential tokenCredential) {
        this.tokenCredential = tokenCredential;
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
     * Configure whether SSL connection should be established or not.
     *
     * @param useSSL the flag indicating whether SSL connection should be used or not.
     * @return An updated instance of this builder.
     */
    public AzureJedisClientBuilder useSSL(boolean useSSL) {
        this.useSSL = useSSL;
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
                this.put("credential", tokenCredential);
            }
        });
        return new AzureJedisClient(cacheHostName, port, tokenCredential, useSSL, retryOptions);
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
