package com.azure.core.http.httpurlconnection;

import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;

import java.time.Duration;
import java.util.Objects;

import static com.azure.core.util.Configuration.PROPERTY_AZURE_REQUEST_CONNECT_TIMEOUT;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_REQUEST_READ_TIMEOUT;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_REQUEST_RESPONSE_TIMEOUT;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_REQUEST_WRITE_TIMEOUT;
import static com.azure.core.util.CoreUtils.getDefaultTimeoutFromEnvironment;

/**
 * Builder to configure and build an instance of the {@link HttpUrlConnectionClient}
 */
public class HttpUrlConnectionClientBuilder {
    private static final long DEFAULT_CONNECT_TIMEOUT;
    private static final long DEFAULT_WRITE_TIMEOUT;
    private static final long DEFAULT_RESPONSE_TIMEOUT;
    private static final long DEFAULT_READ_TIMEOUT;

    private static final ClientLogger LOGGER = new ClientLogger(HttpUrlConnectionClientBuilder.class);

    static {
        Configuration configuration = Configuration.getGlobalConfiguration();

        DEFAULT_CONNECT_TIMEOUT = getDefaultTimeoutFromEnvironment(configuration,
            PROPERTY_AZURE_REQUEST_CONNECT_TIMEOUT, Duration.ofSeconds(10), LOGGER).toMillis();
        DEFAULT_WRITE_TIMEOUT = getDefaultTimeoutFromEnvironment(configuration,
            PROPERTY_AZURE_REQUEST_WRITE_TIMEOUT, Duration.ofSeconds(60), LOGGER).toMillis();
        DEFAULT_RESPONSE_TIMEOUT = getDefaultTimeoutFromEnvironment(configuration,
            PROPERTY_AZURE_REQUEST_RESPONSE_TIMEOUT, Duration.ofSeconds(60), LOGGER).toMillis();
        DEFAULT_READ_TIMEOUT = getDefaultTimeoutFromEnvironment(configuration,
            PROPERTY_AZURE_REQUEST_READ_TIMEOUT, Duration.ofSeconds(60), LOGGER).toMillis();
    }

    private HttpClient baseHttpClient;
    private Configuration configuration;
    private ProxyOptions proxyOptions;
    private int port = 80;
    private Boolean disableBufferCopy;
    private Duration connectTimeout;

    public HttpUrlConnectionClientBuilder() {
        this.baseHttpClient = null;
    }

    public HttpUrlConnectionClientBuilder(HttpClient HttpUrlClient) {
        this.baseHttpClient = Objects.requireNonNull(HttpUrlClient, "'HttpUrlConnectionClient' cannot be null.");
    }

    /**
     * Sets the configuration
     *
     * @param configuration The configuration to use
     * @return the updated HttpUrlConnectionClientBuilder object
     */
    public HttpUrlConnectionClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Construct the client, and return it
     *
     * @return The constructed client
     */
    public HttpClient build() {
        HttpUrlConnectionClient client = new HttpUrlConnectionClient();
        return client;
    }

    /**
     * Set the proxy options
     *
     * @param proxyOptions the proxy options to use
     * @return the updated HttpUrlConnectionClientBuilder object
     */
    public HttpUrlConnectionClientBuilder proxy(ProxyOptions proxyOptions) {
        this.proxyOptions = proxyOptions;
        return this;
    }

    /**
     * Set the port to use
     *
     * @param port the port number to use
     * @return the updated HttpUrlConnectionClientBuilder object
     */
    public HttpUrlConnectionClientBuilder port(int port) {
        this.port = port;
        return this;
    }

    /**
     * Set whether buffer copy is disabled or not
     *
     * @param disableBufferCopy the boolean to use for this setting
     * @return the updated HttpUrlConnectionClientBuilder object
     */
    public HttpUrlConnectionClientBuilder disableBufferCopy(boolean disableBufferCopy) {
        this.disableBufferCopy = disableBufferCopy;
        return this;
    }

    /**
     * Set the connection timeout
     *
     * @param connectTimeout Duration of timeout length
     * @return the updated HttpUrlConnectionClientBuilder object
     */
    public HttpUrlConnectionClientBuilder connectionTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }
}
