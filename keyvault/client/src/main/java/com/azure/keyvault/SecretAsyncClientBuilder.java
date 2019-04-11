package com.azure.keyvault;

import com.azure.common.credentials.AsyncServiceClientCredentials;
import com.azure.common.credentials.ServiceClientCredentials;
import com.azure.common.http.HttpClient;
import com.azure.common.http.HttpMethod;
import com.azure.common.http.HttpPipeline;
import com.azure.common.http.HttpRequest;
import com.azure.common.http.policy.*;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Provides configuration options for instances of {@link SecretAsyncClient}.
 */
public final class SecretAsyncClientBuilder {
    private final List<HttpPipelinePolicy> policies;
    private AsyncServiceClientCredentials credentials;
    private HttpPipeline pipeline;
    private URL vaultEndPoint;
    private HttpClient httpClient;
    private HttpLogDetailLevel httpLogDetailLevel;
    private RetryPolicy retryPolicy;
    private String userAgent;

    SecretAsyncClientBuilder() {
        userAgent = String.format("Azure-SDK-For-Java/%s (%s)", AzureKeyVaultConfiguration.SDK_NAME, AzureKeyVaultConfiguration.SDK_VERSION);
        retryPolicy = new RetryPolicy();
        httpLogDetailLevel = HttpLogDetailLevel.NONE;
        policies = new ArrayList<>();
    }

    /**
     * Creates a {@link SecretAsyncClient} based on options set in the builder.
     * Every time {@code build()} is called, a new instance of {@link SecretAsyncClient} is created.
     *
     * <p>
     * If {@link SecretAsyncClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link SecretAsyncClientBuilder#vaultEndPoint(String) serviceEndpoint} are used to create the
     * {@link SecretAsyncClientBuilder client}. All other builder settings are ignored.
     * </p>
     *
     * @return A SecretAsyncClient with the options set from the builder.
     * @throws IllegalStateException If {@link SecretAsyncClientBuilder#credentials(AsyncServiceClientCredentials)} or
     * {@link SecretAsyncClientBuilder#vaultEndPoint(String)} have not been set.
     */
    public SecretAsyncClient build() {

        if (pipeline != null) {
            return new SecretAsyncClient(vaultEndPoint, pipeline);
        }

        if (credentials == null) {
            throw new IllegalStateException(KeyVaultErrorCodeStrings.CREDENTIALS_REQUIRED);
        }

        if (vaultEndPoint == null) {
            throw new IllegalStateException(KeyVaultErrorCodeStrings.VAULT_END_POINT_REQUIRED);
        }

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(new UserAgentPolicy(userAgent));
        policies.add(retryPolicy);
        policies.add(new AsyncCredentialsPolicy(getAsyncTokenCredentials()));

        policies.addAll(this.policies);

        policies.add(new HttpLoggingPolicy(httpLogDetailLevel));

        HttpPipeline pipeline = httpClient == null
            ? new HttpPipeline(policies)
            : new HttpPipeline(httpClient, policies);

        return new SecretAsyncClient(vaultEndPoint, pipeline);
    }

    /**
     * Sets the vault endpoint url to send HTTP requests to.
     *
     * @param vaultEndPoint The vault endpoint url is used as destination on Azure to send requests to.
     * @return The updated Builder object.
     * @throws MalformedURLException if {@code vaultEndPoint} is null or it cannot be parsed into a valid URL.
     */
    public SecretAsyncClientBuilder vaultEndPoint(String vaultEndPoint) throws MalformedURLException {
        this.vaultEndPoint = new URL(vaultEndPoint);
        return this;
    }

    /**
     * Sets the credentials to use when authenticating HTTP requests.
     *
     * @param credentials The credentials to use for authenticating HTTP requests.
     * @return The updated Builder object.
     * @throws NullPointerException if {@code credentials} is {@code null}.
     */
    public SecretAsyncClientBuilder credentials(AsyncServiceClientCredentials credentials) {
        Objects.requireNonNull(credentials);
        this.credentials = credentials;
        return this;
    }

    /**
     * Sets the logging level for HTTP requests and responses.
     *
     * <p>
     *  logLevel is optional. If not provided, default value of {@link HttpLogDetailLevel#NONE} is set.
     * </p>
     *
     * @param logLevel The amount of logging output when sending and receiving HTTP requests/responses.
     * @return The updated Builder object.
     * @throws NullPointerException if {@code logLevel} is {@code null}.
     */
    public SecretAsyncClientBuilder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        Objects.requireNonNull(logLevel);
        httpLogDetailLevel = logLevel;
        return this;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after
     * {@link SecretAsyncClient} required policies.
     *
     * @param policy The {@link HttpPipelinePolicy policy} to be added.
     * @return The updated Builder object.
     * @throws NullPointerException if {@code policy} is {@code null}.
     */
    public SecretAsyncClientBuilder addPolicy(HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy);
        policies.add(policy);
        return this;
    }

    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param client The HTTP client to use for requests.
     * @return The updated Builder object.
     * @throws NullPointerException If {@code client} is {@code null}.
     */
    public SecretAsyncClientBuilder httpClient(HttpClient client) {
        Objects.requireNonNull(client);
        this.httpClient = client;
        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from
     * {@link SecretAsyncClientBuilder#vaultEndPoint(String) vaultEndPoint} to build {@link SecretAsyncClient}.
     *
     * @param pipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return The updated {@link SecretAsyncClientBuilder} object.
     */
    public SecretAsyncClientBuilder pipeline(HttpPipeline pipeline) {
        Objects.requireNonNull(pipeline);
        this.pipeline = pipeline;
        return this;
    }

    private AsyncTokenCredentials getAsyncTokenCredentials(){
        return new AsyncTokenCredentials("Bearer",credentials.authorizationHeaderValueAsync(new HttpRequest(HttpMethod.POST,vaultEndPoint)));
    }


    private class AsyncTokenCredentials implements AsyncServiceClientCredentials {

        private String scheme;
        private Mono<String> token;

        public AsyncTokenCredentials(String scheme, Mono<String> token){
            this.scheme = scheme;
            this.token = token;
        }

        @Override
        public Mono<String> authorizationHeaderValueAsync(HttpRequest httpRequest) {
            if(scheme == null){
                scheme = "Bearer";
            }
            return token.flatMap(new Function<String, Mono<? extends String>>() {
                @Override
                public Mono<? extends String> apply(String tokenValue) {
                    return Mono.just("Bearer " + tokenValue);
                }
            });
        }
    }




}
