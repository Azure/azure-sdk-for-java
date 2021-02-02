package com.azure.mixedreality.remoterendering;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.Configuration;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.mixedreality.authentication.MixedRealityStsServiceVersion;
import com.azure.mixedreality.remoterendering.implementation.MixedRealityRemoteRenderingImplBuilder;
import com.azure.mixedreality.authentication.MixedRealityStsClientBuilder;

import java.util.Objects;
import java.util.UUID;

@ServiceClientBuilder(serviceClients = {RemoteRenderingClient.class, RemoteRenderingAsyncClient.class})
public class RemoteRenderingClientBuilder {

    private MixedRealityRemoteRenderingImplBuilder builder;
    private UUID accountId;
    private MixedRealityStsClientBuilder stsBuilder;
    private RemoteRenderingServiceVersion apiVersion;

    public RemoteRenderingClientBuilder() {
        builder = new MixedRealityRemoteRenderingImplBuilder();
        stsBuilder = new MixedRealityStsClientBuilder();
    }

    public RemoteRenderingClient buildClient() {
        return new RemoteRenderingClient(buildAsyncClient());
    }

    public RemoteRenderingAsyncClient buildAsyncClient() {

        var accessToken = stsBuilder.buildAsyncClient().getToken();

        builder.addPolicy(new BearerTokenAuthenticationPolicy(r -> accessToken));

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
        this.stsBuilder.accountId(accountId);
        return this;
    }

    /**
     * Sets the accountDomain.
     *
     * @param accountDomain the accountDomain value.
     * @return the RemoteRenderingClientBuilder.
     */
    public RemoteRenderingClientBuilder accountDomain(String accountDomain) {
        this.stsBuilder.accountDomain(accountDomain);
        return this;
    }

    /**
     * Sets the accountKeyCredential to use for authentication.
     *
     * @param accountKeyCredential the accountKeyCredential value.
     * @return the RemoteRenderingClientBuilder.
     */
    public RemoteRenderingClientBuilder accountKeyCredential(AzureKeyCredential accountKeyCredential) {
        this.stsBuilder.credential(accountKeyCredential);
        return this;
    }

    /**
     * Use a {@link TokenCredential} for authentication.
     *
     * @param tokenCredential The {@link TokenCredential} used to authenticate HTTP requests.
     * @return the RemoteRenderingClientBuilder.
     * @throws NullPointerException If {@code tokenCredential} is null.
     */
    public RemoteRenderingClientBuilder credential(TokenCredential tokenCredential) {
        this.stsBuilder.credential(tokenCredential);
        return this;
    }

    /**
     * Sets the Remote Rendering service endpoint.
     * <p>
     * For converting assets, it is preferable to pick a region close to the storage containing the assets.
     * For rendering, it is strongly recommended that you pick the closest region to the devices using the service. 
     * The time taken to communicate with the server impacts the quality of the experience.
     *
     * @param endpoint the host value.
     * @return the RemoteRenderingClientBuilder.
     */
    public RemoteRenderingClientBuilder endpoint(String endpoint) {
        builder.host(endpoint);
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

    /**
     * Sets the {@link RemoteRenderingServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version the client library will have the result of potentially moving to a newer service version.
     *
     * @param version {@link RemoteRenderingServiceVersion} of the service to be used when making requests.
     * @return The RemoteRenderingClientBuilder.
     */
    public RemoteRenderingClientBuilder serviceVersion(RemoteRenderingServiceVersion version) {
        this.apiVersion = version;
        return this;
    }
}
