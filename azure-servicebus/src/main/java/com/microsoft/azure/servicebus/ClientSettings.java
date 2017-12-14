package com.microsoft.azure.servicebus;

import java.time.Duration;

import com.microsoft.azure.servicebus.primitives.ClientConstants;
import com.microsoft.azure.servicebus.primitives.RetryPolicy;
import com.microsoft.azure.sevicebus.security.TokenProvider;

public class ClientSettings {
    
    private TokenProvider tokenProvider;
    private RetryPolicy retryPolicy;
    private Duration operationTimeout;
    
    public ClientSettings(TokenProvider tokenProvider)
    {
        this(tokenProvider, RetryPolicy.getDefault(), Duration.ofSeconds(ClientConstants.DEFAULT_OPERATION_TIMEOUT_IN_SECONDS));
    }
    
    public ClientSettings(TokenProvider tokenProvider, RetryPolicy retryPolicy, Duration operationTimeout)
    {
        this.tokenProvider = tokenProvider;
        this.retryPolicy = retryPolicy;
        this.operationTimeout = operationTimeout;
    }

    public TokenProvider getTokenProvider()
    {
        return tokenProvider;
    }

    public RetryPolicy getRetryPolicy()
    {
        return retryPolicy;
    }

    public Duration getOperationTimeout()
    {
        return operationTimeout;
    }
}
