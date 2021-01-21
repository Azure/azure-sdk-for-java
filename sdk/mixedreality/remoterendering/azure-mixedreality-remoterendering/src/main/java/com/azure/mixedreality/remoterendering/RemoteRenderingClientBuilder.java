package com.azure.mixedreality.remoterendering;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.Configuration;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.mixedreality.remoterendering.implementation.MixedRealityRemoteRenderingImplBuilder;

import java.util.UUID;

@ServiceClientBuilder(serviceClients = {RemoteRenderingClient.class, RemoteRenderingAsyncClient.class})
public class RemoteRenderingClientBuilder {

    private MixedRealityRemoteRenderingImplBuilder builder;
    private UUID accountId;

    public RemoteRenderingClientBuilder() {
        builder = new MixedRealityRemoteRenderingImplBuilder();
    }

    public RemoteRenderingClient buildClient() {
        return new RemoteRenderingClient(buildAsyncClient());
    }

    public RemoteRenderingAsyncClient buildAsyncClient() {
        return new RemoteRenderingAsyncClient(builder.buildClient(), accountId);
    }

    /**
     * Sets the accountId.
     *
     * @param accountId the accountId value.
     * @return the RemoteRenderingClientBuilder.
     */
    public RemoteRenderingClientBuilder accountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    /**
     * Sets server parameter.
     *
     * @param host the host value.
     * @return the RemoteRenderingClientBuilder.
     */
    public RemoteRenderingClientBuilder host(String host) {
        builder.host(host);
        return this;
    }

    /**
     * Sets The HTTP pipeline to send requests through.
     *
     * @param pipeline the pipeline value.
     * @return the RemoteRenderingClientBuilder.
     */
    public RemoteRenderingClientBuilder pipeline(HttpPipeline pipeline) {
        builder.pipeline(pipeline);
        return this;
    }

    /**
     * Sets The serializer to serialize an object into a string.
     *
     * @param serializerAdapter the serializerAdapter value.
     * @return the RemoteRenderingClientBuilder.
     */
    public RemoteRenderingClientBuilder serializerAdapter(SerializerAdapter serializerAdapter) {
        builder.serializerAdapter(serializerAdapter);
        return this;
    }

    /**
     * Sets The HTTP client used to send the request.
     *
     * @param httpClient the httpClient value.
     * @return the RemoteRenderingClientBuilder.
     */
    public RemoteRenderingClientBuilder httpClient(HttpClient httpClient) {
        builder.httpClient(httpClient);
        return this;
    }

    /**
     * Sets The configuration store that is used during construction of the service client.
     *
     * @param configuration the configuration value.
     * @return the RemoteRenderingClientBuilder.
     */
    public RemoteRenderingClientBuilder configuration(Configuration configuration) {
        builder.configuration(configuration);
        return this;
    }

    /**
     * Sets The TokenCredential used for authentication.
     *
     * @param tokenCredential the tokenCredential value.
     * @return the RemoteRenderingClientBuilder.
     */
    public RemoteRenderingClientBuilder credential(TokenCredential tokenCredential) {
        builder.credential(tokenCredential);
        return this;
    }

    /**
     * Sets The logging configuration for HTTP requests and responses.
     *
     * @param httpLogOptions the httpLogOptions value.
     * @return the RemoteRenderingClientBuilder.
     */
    public RemoteRenderingClientBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
        builder.httpLogOptions(httpLogOptions);
        return this;
    }

    /**
     * Sets The retry policy that will attempt to retry failed requests, if applicable.
     *
     * @param retryPolicy the retryPolicy value.
     * @return the RemoteRenderingClientBuilder.
     */
    public RemoteRenderingClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        builder.retryPolicy(retryPolicy);
        return this;
    }

    /**
     * Adds a custom Http pipeline policy.
     *
     * @param customPolicy The custom Http pipeline policy to add.
     * @return the RemoteRenderingClientBuilder.
     */
    public RemoteRenderingClientBuilder addPolicy(HttpPipelinePolicy customPolicy) {
        builder.addPolicy(customPolicy);
        return this;
    }
}
