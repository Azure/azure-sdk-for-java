package com.microsoft.azure.servicebus;

import java.time.Duration;

import com.microsoft.azure.servicebus.primitives.ClientConstants;
import com.microsoft.azure.servicebus.primitives.RetryPolicy;
import com.microsoft.azure.servicebus.security.TokenProvider;

/**
 * Class encapsulating common client level settings like TokenProvider, RetryPolicy, OperationTimeout.
 * @since 1.2.0
 *
 */
public class ClientSettings {
    
    private TokenProvider tokenProvider;
    private RetryPolicy retryPolicy;
    private Duration operationTimeout;
    
    /**
     * Creates a new instance with the given token provider, default retry policy and default operation timeout.
     * @param tokenProvider {@link TokenProvider} instance
     * 
     * @see RetryPolicy#getDefault()
     */
    public ClientSettings(TokenProvider tokenProvider)
    {
        this(tokenProvider, RetryPolicy.getDefault(), Duration.ofSeconds(ClientConstants.DEFAULT_OPERATION_TIMEOUT_IN_SECONDS));
    }
    
    /**
     * Creates a new instance with the given token provider, retry policy and operation timeout.
     * @param tokenProvider {@link TokenProvider} instance
     * @param retryPolicy {@link RetryPolicy} instance
     * @param operationTimeout default operation timeout to be used for all client operations. Client can override this value by explicitly specifying a timeout in the operation.
     */
    public ClientSettings(TokenProvider tokenProvider, RetryPolicy retryPolicy, Duration operationTimeout)
    {
        this.tokenProvider = tokenProvider;
        this.retryPolicy = retryPolicy;
        this.operationTimeout = operationTimeout;
    }

    /**
     * Gets the token provider contained in this instance.
     * @return TokenProvider contained in this instance
     */
    public TokenProvider getTokenProvider()
    {
        return tokenProvider;
    }

    /**
     * Gets the retry policy contained in this instance.
     * @return RetryPolicy contained in this instance
     */
    public RetryPolicy getRetryPolicy()
    {
        return retryPolicy;
    }

    /**
     * Gets the operation timeout contained in this instance.
     * @return operation timeout contained in this instance
     */
    public Duration getOperationTimeout()
    {
        return operationTimeout;
    }
}
