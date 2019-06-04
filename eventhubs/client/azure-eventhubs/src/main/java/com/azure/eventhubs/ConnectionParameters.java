package com.azure.eventhubs;

import com.azure.core.amqp.TransportType;
import com.azure.eventhubs.implementation.TokenProvider;
import reactor.core.scheduler.Scheduler;

import java.time.Duration;
import java.util.Objects;

class ConnectionParameters {
    private CredentialInfo credentials;
    private Duration timeout;
    private TokenProvider tokenProvider;
    private TransportType transport;
    private Retry retryPolicy;
    private ProxyConfiguration proxyConfiguration;
    private Scheduler scheduler;

    ConnectionParameters(CredentialInfo credentials, Duration timeout, TokenProvider tokenProvider,
                         TransportType transport, Retry retryPolicy,
                         ProxyConfiguration proxyConfiguration, Scheduler scheduler) {
        Objects.requireNonNull(credentials);
        Objects.requireNonNull(timeout);
        Objects.requireNonNull(tokenProvider);
        Objects.requireNonNull(transport);
        Objects.requireNonNull(retryPolicy);
        Objects.requireNonNull(proxyConfiguration);
        Objects.requireNonNull(scheduler);

        this.credentials = credentials;
        this.timeout = timeout;
        this.tokenProvider = tokenProvider;
        this.transport = transport;
        this.retryPolicy = retryPolicy;
        this.proxyConfiguration = proxyConfiguration;
        this.scheduler = scheduler;
    }

    public CredentialInfo getCredentials() {
        return credentials;
    }

    public void setCredentials(CredentialInfo credentials) {
        this.credentials = credentials;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public TokenProvider getTokenProvider() {
        return tokenProvider;
    }

    public void setTokenProvider(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    public TransportType getTransport() {
        return transport;
    }

    public void setTransport(TransportType transport) {
        this.transport = transport;
    }

    public Retry getRetryPolicy() {
        return retryPolicy;
    }

    public void setRetryPolicy(Retry retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    public ProxyConfiguration getProxyConfiguration() {
        return proxyConfiguration;
    }

    public void setProxyConfiguration(ProxyConfiguration proxyConfiguration) {
        this.proxyConfiguration = proxyConfiguration;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
}
