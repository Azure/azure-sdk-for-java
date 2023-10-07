package com.azure.core.http.httpurlconnection;

import com.azure.core.http.httpurlconnection.implementation.HttpUrlConnectionTimeouts;
import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.time.Duration;
import java.util.*;

public class HttpUrlConnectionAsyncClientBuilder {

    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration DEFAULT_WRITE_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration DEFAULT_RESPONSE_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration MINIMUM_TIMEOUT = Duration.ofMillis(1);
    private static final String JAVA_HOME = System.getProperty("java.home");
    private static final String HTTPURLCONNECTIONCLIENT_ALLOW_RESTRICTED_HEADERS = "httpurlconnectionclient.allowRestrictedHeaders";

    static final Set<String> DEFAULT_RESTRICTED_HEADERS;

    static {
        Set<String> treeSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        Collections.addAll(treeSet, "connection", "content-length", "expect", "host", "upgrade");
        DEFAULT_RESTRICTED_HEADERS = Collections.unmodifiableSet(treeSet);
    }

    private static final ClientLogger LOGGER = new ClientLogger(HttpUrlConnectionAsyncClientBuilder.class);

    private Duration connectionTimeout;
    private Duration readTimeout;
    private Duration writeTimeout;
    private Duration responseTimeout;
    private ProxyOptions proxyOptions;
    private Configuration configuration;

    public HttpUrlConnectionAsyncClientBuilder() {
    }
    public HttpUrlConnectionAsyncClientBuilder connectionTimeout(Duration connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    public HttpUrlConnectionAsyncClientBuilder readTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public HttpUrlConnectionAsyncClientBuilder writeTimeout(Duration writeTimeout) {
        this.writeTimeout = writeTimeout;
        return this;
    }

    public HttpUrlConnectionAsyncClientBuilder responseTimeout(Duration responseTimeout) {
        this.responseTimeout = responseTimeout;
        return this;
    }

    static Duration getTimeout(Duration configuredTimeout, Duration defaultTimeout) {
        // Timeout is null, use the default timeout.
        if (configuredTimeout == null) {
            return defaultTimeout;
        }

        // Timeout is less than or equal to zero, return no timeout.
        if (configuredTimeout.isZero() || configuredTimeout.isNegative()) {
            return Duration.ZERO;
        }

        // Return the maximum of the timeout period and the minimum allowed timeout period.
        if (configuredTimeout.compareTo(MINIMUM_TIMEOUT) < 0) {
            return MINIMUM_TIMEOUT;
        } else  {
            return configuredTimeout;
        }
    }

    public HttpUrlConnectionAsyncClientBuilder proxy(ProxyOptions proxyOptions) {
        this.proxyOptions = proxyOptions;
        return this;
    }

    public HttpUrlConnectionAsyncClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }


    public HttpClient build() {
        Configuration buildConfiguration = (configuration == null)
            ? Configuration.getGlobalConfiguration()
            : configuration;

        ProxyOptions buildProxyOptions = (proxyOptions == null)
            ? ProxyOptions.fromConfiguration(buildConfiguration)
            : proxyOptions;

        if (buildProxyOptions != null && buildProxyOptions.getUsername() != null) {
            Authenticator.setDefault(new ProxyAuthenticator(buildProxyOptions.getUsername(),
                buildProxyOptions.getPassword()));
        }

        if (buildProxyOptions != null && buildProxyOptions.getType() != ProxyOptions.Type.HTTP
            && buildProxyOptions.getType() != null) {
            throw new IllegalArgumentException("Invalid proxy");
        }

        HttpUrlConnectionTimeouts timeouts = new HttpUrlConnectionTimeouts(
            getTimeout(connectionTimeout, DEFAULT_CONNECT_TIMEOUT),
            getTimeout(readTimeout, DEFAULT_READ_TIMEOUT),
            getTimeout(writeTimeout, DEFAULT_WRITE_TIMEOUT),
            getTimeout(responseTimeout, DEFAULT_RESPONSE_TIMEOUT)
        );
        return new HttpUrlConnectionAsyncClient(timeouts, buildProxyOptions, buildConfiguration);
    }

    Set<String> getRestrictedHeaders() {
        Set<String> allowRestrictedHeaders = getAllowRestrictedHeaders();
        Set<String> restrictedHeaders = new HashSet<>(DEFAULT_RESTRICTED_HEADERS);
        restrictedHeaders.removeAll(allowRestrictedHeaders);
        return restrictedHeaders;
    }

    private Set<String> getAllowRestrictedHeaders() {
        Properties properties = getNetworkProperties();
        String[] allowRestrictedHeadersNetProperties = properties.getProperty(HTTPURLCONNECTIONCLIENT_ALLOW_RESTRICTED_HEADERS, "").split(",");

        Configuration config = (this.configuration == null) ? Configuration.getGlobalConfiguration() : configuration;
        String[] allowRestrictedHeadersSystemProperties = config.get(HTTPURLCONNECTIONCLIENT_ALLOW_RESTRICTED_HEADERS, "").split(",");

        Set<String> allowRestrictedHeaders = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (String header : allowRestrictedHeadersSystemProperties) {
            allowRestrictedHeaders.add(header.trim());
        }

        for (String header : allowRestrictedHeadersNetProperties) {
            allowRestrictedHeaders.add(header.trim());
        }

        return allowRestrictedHeaders;
    }

    Properties getNetworkProperties() {
        Properties properties = new Properties();
        try {
            properties.load(ClassLoader.getSystemResourceAsStream("net.properties"));
        } catch (Exception e) {
            LOGGER.warning("Cannot read net properties file", e);
        }
        return properties;
    }

    private static class ProxyAuthenticator extends Authenticator {
        private final String userName;
        private final String password;

        ProxyAuthenticator(String userName, String password) {
            this.userName = userName;
            this.password = password;
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(this.userName, password.toCharArray());
        }
    }
}
