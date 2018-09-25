package com.microsoft.azure.servicebus;

import java.time.Duration;

import com.microsoft.azure.servicebus.primitives.ClientConstants;
import com.microsoft.azure.servicebus.primitives.RetryPolicy;
import com.microsoft.azure.servicebus.primitives.TransportType;
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
    private TransportType transportType;

    private String proxyHostName = null;
    private int proxyHostPort = 0;
    private String proxyUserName = null;
    private String proxyPassword = null;

    /**
     * Creates a new instance with the given token provider, default retry policy and default operation timeout.
     * @param tokenProvider {@link TokenProvider} instance
     * 
     * @see RetryPolicy#getDefault()
     */
    public ClientSettings(TokenProvider tokenProvider)
    {
        this(tokenProvider, RetryPolicy.getDefault(), Duration.ofSeconds(ClientConstants.DEFAULT_OPERATION_TIMEOUT_IN_SECONDS), TransportType.AMQP);
    }

    /**
     * Creates a new instance with the given token provider, retry policy and operation timeout.
     * @param tokenProvider {@link TokenProvider} instance
     * @param retryPolicy {@link RetryPolicy} instance
     * @param operationTimeout default operation timeout to be used for all client operations. Client can override this value by explicitly specifying a timeout in the operation.
     */
    public ClientSettings(TokenProvider tokenProvider, RetryPolicy retryPolicy, Duration operationTimeout)
    {
        this(tokenProvider, retryPolicy, operationTimeout, TransportType.AMQP);
    }

    /**
     * Creates a new instance with the given token provider, retry policy and operation timeout.
     * @param tokenProvider {@link TokenProvider} instance
     * @param retryPolicy {@link RetryPolicy} instance
     * @param operationTimeout default operation timeout to be used for all client operations. Client can override this value by explicitly specifying a timeout in the operation.
     * @param transportType {@link TransportType} instance
     */
    public ClientSettings(TokenProvider tokenProvider, RetryPolicy retryPolicy, Duration operationTimeout, TransportType transportType)
    {
        this.tokenProvider = tokenProvider;
        this.retryPolicy = retryPolicy;
        this.operationTimeout = operationTimeout;
        this.transportType = transportType;
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

    /**
     * Gets the transport type for this instance
     * @return transport type for the instance
     */
    public TransportType getTransportType() { return transportType; }

    /**
     * Sets the proxy hostname. Required for proxy connection
     * Proxy settings are only valid with transport type AMQP_WEB_SOCKETS
     * @param proxyHostName
     */
    public void setProxyHostName(String proxyHostName) { this.proxyHostName = proxyHostName; }

    /**
     * Sets the proxy host port. Required for proxy connection
     * Proxy settings are only valid with transport type AMQP_WEB_SOCKETS
     * @param proxyHostPort
     */
    public void setProxyHostPort(int proxyHostPort) { this.proxyHostPort = proxyHostPort; }

    /**
     * Sets the proxy username
     * Proxy settings are only valid with transport type AMQP_WEB_SOCKETS
     * @param proxyUserName
     */
    public void setProxyUserName(String proxyUserName) { this.proxyUserName = proxyUserName; }

    /**
     * Sets the proxy password
     * Proxy settings are only valid with transport type AMQP_WEB_SOCKETS
     * @param proxyPassword
     */
    public void setProxyPassword(String proxyPassword) { this.proxyPassword = proxyPassword; }

    /**
     * Gets the proxy host name
     * @return proxy host name
     */
    public String getProxyHostName() { return proxyHostName; }

    /**
     * Gets the proxy port
     * @return proxy port
     */
    public int getProxyHostPort() { return proxyHostPort; }

    /**
     * Gets the proxy username
     * @return proxy username
     */
    public String getProxyUserName() { return proxyUserName; }

    /**
     * Gets the proxy password
     * @return proxy password
     */
    public String getProxyPassword() { return proxyPassword; }
}
