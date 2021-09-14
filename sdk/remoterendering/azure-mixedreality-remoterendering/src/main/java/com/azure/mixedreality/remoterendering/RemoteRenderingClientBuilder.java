// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.remoterendering;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.mixedreality.authentication.MixedRealityStsAsyncClient;
import com.azure.mixedreality.remoterendering.implementation.MixedRealityRemoteRenderingImplBuilder;
import com.azure.mixedreality.authentication.MixedRealityStsClientBuilder;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** A builder for creating instances of RemoteRenderingClient and RemoteRenderingAsyncClient. */
@ServiceClientBuilder(serviceClients = {RemoteRenderingClient.class, RemoteRenderingAsyncClient.class})
public final class RemoteRenderingClientBuilder {

    private final ClientLogger logger = new ClientLogger(RemoteRenderingClientBuilder.class);

    private final MixedRealityRemoteRenderingImplBuilder builder;
    private UUID accountId;
    private final MixedRealityStsClientBuilder stsBuilder;
    private RemoteRenderingServiceVersion apiVersion;
    private AccessToken accessToken;
    private String endpoint;
    private ClientOptions clientOptions;
    private HttpLogOptions httpLogOptions;

    /** Constructs a new RemoteRenderingClientBuilder instance. */
    public RemoteRenderingClientBuilder() {
        builder = new MixedRealityRemoteRenderingImplBuilder();
        stsBuilder = new MixedRealityStsClientBuilder();
    }

    /** Builds and returns a RemoteRenderingClient instance from the provided parameters.
     *
     * @return the RemoteRenderingClient instance.
     */
    public RemoteRenderingClient buildClient() {
        return new RemoteRenderingClient(buildAsyncClient());
    }

    /** Builds and returns a RemoteRenderingAsyncClient instance from the provided parameters.
     *
     * @return the RemoteRenderingAsyncClient instance.
     */
    public RemoteRenderingAsyncClient buildAsyncClient() {
        String scope = this.endpoint.replaceFirst("/$", "") + "/.default";
        if (accessToken == null) {
            MixedRealityStsAsyncClient stsClient = stsBuilder.buildAsyncClient();
            builder.addPolicy(new BearerTokenAuthenticationPolicy(r -> stsClient.getToken(), scope));
        }
        else {
            builder.addPolicy(new BearerTokenAuthenticationPolicy(r -> Mono.just(this.accessToken), scope));
        }

        if (clientOptions != null) {
            List<HttpHeader> httpHeaderList = new ArrayList<HttpHeader>();
            clientOptions.getHeaders().forEach(header -> httpHeaderList.add(new HttpHeader(header.getName(), header.getValue())));
            builder.addPolicy(new AddHeadersPolicy(new HttpHeaders(httpHeaderList)));

            // generated code uses deprecated httpLogOptions.getApplicationId(), so we set that here.
            if (httpLogOptions == null) {
                httpLogOptions = new HttpLogOptions();
                builder.httpLogOptions(httpLogOptions);
            }
            httpLogOptions.setApplicationId(clientOptions.getApplicationId());
        }

        return new RemoteRenderingAsyncClient(builder.buildClient(), accountId);
    }

    /**
     * Sets the accountId.
     *
     * @param accountId the accountId value.
     * @return the RemoteRenderingClientBuilder.
     */
    public RemoteRenderingClientBuilder accountId(String accountId) {
        Objects.requireNonNull(accountId, "'accountId' cannot be null.");

        try {
            this.accountId = UUID.fromString(accountId);
        } catch (IllegalArgumentException ex) {
            throw logger.logExceptionAsError(new IllegalArgumentException("The 'accountId' must be a UUID formatted value."));
        }

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
        Objects.requireNonNull(accountDomain, "'accountDomain' cannot be null.");

        if (accountDomain.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'accountDomain' cannot be an empty string."));
        }

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
        this.stsBuilder.credential(Objects.requireNonNull(accountKeyCredential, "'accountKeyCredential' cannot be null."));
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
        this.stsBuilder.credential(Objects.requireNonNull(tokenCredential, "'tokenCredential' cannot be null."));
        return this;
    }

    /**
     * Use a {@link AccessToken} for authentication.
     *
     * @param accessToken An access token used to access the specified Azure Remote Rendering account
     * @return the RemoteRenderingClientBuilder.
     */
    public RemoteRenderingClientBuilder accessToken(AccessToken accessToken) {
        this.accessToken = Objects.requireNonNull(accessToken, "'accessToken' cannot be null.");
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
        Objects.requireNonNull(endpoint, "'endpoint' cannot be null.");
        builder.endpoint(endpoint);
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Sets The HTTP client used to send the request.
     *
     * @param httpClient the httpClient value.
     * @return the RemoteRenderingClientBuilder.
     */
    public RemoteRenderingClientBuilder httpClient(HttpClient httpClient) {
        builder.httpClient(Objects.requireNonNull(httpClient, "'httpClient' cannot be null."));
        return this;
    }

    /**
     * Sets The logging configuration for HTTP requests and responses.
     *
     * @param httpLogOptions the httpLogOptions value.
     * @return the RemoteRenderingClientBuilder.
     */
    public RemoteRenderingClientBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
        Objects.requireNonNull(httpLogOptions, "'httpLogOptions' cannot be null.");

        builder.httpLogOptions(httpLogOptions);
        this.httpLogOptions = httpLogOptions;
        return this;
    }

    /**
     * Sets The HTTP pipeline to send requests through.
     *
     * @param pipeline the pipeline value.
     * @return the RemoteRenderingClientBuilder.
     */
    public RemoteRenderingClientBuilder pipeline(HttpPipeline pipeline) {
        builder.pipeline(Objects.requireNonNull(pipeline, "'pipeline' cannot be null."));
        return this;
    }

    /**
     * Sets The retry policy that will attempt to retry failed requests, if applicable.
     *
     * @param retryPolicy the retryPolicy value.
     * @return the RemoteRenderingClientBuilder.
     */
    public RemoteRenderingClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        builder.retryPolicy(Objects.requireNonNull(retryPolicy, "'retryPolicy' cannot be null."));
        return this;
    }

    /**
     * Sets The configuration store that is used during construction of the service client.
     *
     * @param configuration the configuration value.
     * @return the RemoteRenderingClientBuilder.
     */
    public RemoteRenderingClientBuilder configuration(Configuration configuration) {
        builder.configuration(Objects.requireNonNull(configuration, "'configuration' cannot be null."));
        return this;
    }

    /**
     * Adds a custom Http pipeline policy.
     *
     * @param customPolicy The custom Http pipeline policy to add.
     * @return the RemoteRenderingClientBuilder.
     */
    public RemoteRenderingClientBuilder addPolicy(HttpPipelinePolicy customPolicy) {
        builder.addPolicy(Objects.requireNonNull(customPolicy, "'customPolicy' cannot be null."));
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
        this.apiVersion = Objects.requireNonNull(version, "'version' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link ClientOptions} which enables various options to be set on the client.
     *
     * @param clientOptions the {@link ClientOptions} to be set on the client.
     * @return The RemoteRenderingClientBuilder.
     */
    public RemoteRenderingClientBuilder clientOptions(ClientOptions clientOptions) {
        Objects.requireNonNull(clientOptions, "'clientOptions' cannot be null.");
        this.stsBuilder.clientOptions(clientOptions);
        this.clientOptions = clientOptions;
        return this;
    }
}
