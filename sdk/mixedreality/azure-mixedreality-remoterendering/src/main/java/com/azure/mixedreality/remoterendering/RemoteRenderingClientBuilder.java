package com.azure.mixedreality.remoterendering;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.Configuration;
import com.azure.mixedreality.remoterendering.implementation.MixedRealityRemoteRenderingImplBuilder;
import com.azure.mixedreality.authentication.MixedRealityStsClientBuilder;
import reactor.core.publisher.Mono;

import java.util.UUID;

@ServiceClientBuilder(serviceClients = {RemoteRenderingClient.class, RemoteRenderingAsyncClient.class})
public final class RemoteRenderingClientBuilder {

    private final MixedRealityRemoteRenderingImplBuilder builder;
    private UUID accountId;
    private final MixedRealityStsClientBuilder stsBuilder;
    private RemoteRenderingServiceVersion apiVersion;
    private AccessToken accessToken;
    private String endpoint;

    public RemoteRenderingClientBuilder() {
        builder = new MixedRealityRemoteRenderingImplBuilder();
        stsBuilder = new MixedRealityStsClientBuilder();
    }

    public RemoteRenderingClient buildClient() {
        return new RemoteRenderingClient(buildAsyncClient());
    }

    public RemoteRenderingAsyncClient buildAsyncClient() {
        String scope = this.endpoint.replaceFirst("/$", "") + "/.default";
        if (accessToken == null)
        {
            var stsClient = stsBuilder.buildAsyncClient();
            builder.addPolicy(new BearerTokenAuthenticationPolicy(r -> stsClient.getToken(), scope));
        }
        else
        {
            builder.addPolicy(new BearerTokenAuthenticationPolicy(r -> Mono.just(this.accessToken), scope));
        }

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
    public RemoteRenderingClientBuilder credential(AzureKeyCredential accountKeyCredential) {
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
     * Use a {@link AccessToken} for authentication.
     *
     * @param accessToken An access token used to access the specified Azure Remote Rendering account
     * @return the RemoteRenderingClientBuilder.
     */
    public RemoteRenderingClientBuilder accessToken(AccessToken accessToken) {
        this.accessToken = accessToken;
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
        builder.endpoint(endpoint);
        this.endpoint = endpoint;
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
