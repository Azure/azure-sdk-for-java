// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.jedis;

import com.azure.core.http.policy.ExponentialBackoff;
import com.azure.core.http.policy.FixedDelay;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryStrategy;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import redis.clients.jedis.exceptions.JedisException;

import java.util.function.Supplier;

/**
 * Manages Api calls for Azure Redis Client.
 */
class ApiManager {
    private final ClientLogger clientLogger = new ClientLogger(ApiManager.class);
    private Authenticator authenticator;
    private RetryStrategy retryStrategy;

    /**
     * Creates an instance of ApiManager class.
     *
     * @param authenticator the authenticator class used for Redis API calls.
     * @param retryOptions the retry options used for Redis API calls.
     */
    public ApiManager(Authenticator authenticator, RetryOptions retryOptions) {
        this.authenticator = authenticator;
        this.retryStrategy = getRetryStrategy(retryOptions);
    }

    public <T> T execute(Supplier<T> supplier, AzureJedisClient jedisClient) {
        return execute(supplier, jedisClient, retryStrategy);
    }

    <T> T execute(Supplier<T> supplier, AzureJedisClient jedisClient, RetryStrategy retryStrategy) {
        int retries = 0;
        while (retries < retryStrategy.getMaxRetries()) {
            try {
                authenticator.authenticateIfRequired(jedisClient);
                T out = supplier.get();
                String methodName = getMethodName(4);
                clientLogger.log(LogLevel.INFORMATIONAL, () -> "Successfully executed API call: " + methodName);
                return out;
            } catch (Exception e) {
                retries++;
                if (retries >= retryStrategy.getMaxRetries()) {
                    throw clientLogger.logThrowableAsError((e instanceof JedisException ? (JedisException) e : new RuntimeException(e)));
                }
                clientLogger.logThrowableAsWarning((e instanceof JedisException ? (JedisException) e : new RuntimeException(e)));
                clientLogger.log(LogLevel.INFORMATIONAL, () -> "Retrying to execute the command.");
                try {
                    Thread.sleep(retryStrategy.calculateRetryDelay(retries).toMillis());
                } catch (InterruptedException ex) {
                    throw clientLogger.logExceptionAsError(new RuntimeException(ex));
                }
            }
        }
        throw clientLogger.logExceptionAsError(new IllegalStateException("Failed to execute the command"));
    }

    private RetryStrategy getRetryStrategy(RetryOptions retryOptions) {
        RetryStrategy out;
        if (retryOptions != null) {
            if (retryOptions.getExponentialBackoffOptions() != null) {
                out = new ExponentialBackoff(retryOptions.getExponentialBackoffOptions());
            } else if (retryOptions.getFixedDelayOptions() != null) {
                out = new FixedDelay(retryOptions.getFixedDelayOptions());
            } else {
                out = new ExponentialBackoff();
            }
        } else {
            out = new ExponentialBackoff();
        }
        return out;
    }

    private String getMethodName(int level) {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stacktrace[level];//maybe this number needs to be corrected
        return e.getMethodName();
    }
}
