package com.azure.messaging.signalr.implementation.client;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;

/**
 * Initializes a new instance of the AzureWebSocketServiceRestAPI type.
 */
public final class AzureWebSocketServiceRestAPI {
    /**
     * server parameter.
     */
    private String host;

    /**
     * Gets server parameter.
     * 
     * @return the host value.
     */
    public String getHost() {
        return this.host;
    }

    /**
     * Sets server parameter.
     * 
     * @param host the host value.
     * @return the service client itself.
     */
    public AzureWebSocketServiceRestAPI setHost(String host) {
        this.host = host;
        return this;
    }

    /**
     * The HTTP pipeline to send requests through.
     */
    private HttpPipeline httpPipeline;

    /**
     * Gets The HTTP pipeline to send requests through.
     * 
     * @return the httpPipeline value.
     */
    public HttpPipeline getHttpPipeline() {
        return this.httpPipeline;
    }

    /**
     * The HealthApis object to access its operations.
     */
    private HealthApis healthApis;

    /**
     * Gets the HealthApis object to access its operations.
     * 
     * @return the HealthApis object.
     */
    public HealthApis healthApis() {
        return this.healthApis;
    }

    /**
     * The WebSocketConnectionApis object to access its operations.
     */
    private WebSocketConnectionApis webSocketConnectionApis;

    /**
     * Gets the WebSocketConnectionApis object to access its operations.
     * 
     * @return the WebSocketConnectionApis object.
     */
    public WebSocketConnectionApis webSocketConnectionApis() {
        return this.webSocketConnectionApis;
    }

    /**
     * Initializes an instance of AzureWebSocketServiceRestAPI client.
     */
    public AzureWebSocketServiceRestAPI() {
        this(new HttpPipelineBuilder().policies(new UserAgentPolicy(), new RetryPolicy(), new CookiePolicy()).build());
    }

    /**
     * Initializes an instance of AzureWebSocketServiceRestAPI client.
     * 
     * @param httpPipeline The HTTP pipeline to send requests through.
     */
    public AzureWebSocketServiceRestAPI(HttpPipeline httpPipeline) {
        this.httpPipeline = httpPipeline;
        this.healthApis = new HealthApis(this);
        this.webSocketConnectionApis = new WebSocketConnectionApis(this);
    }
}
