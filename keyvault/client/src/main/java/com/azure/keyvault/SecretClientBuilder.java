package com.azure.keyvault;

import com.azure.common.credentials.ServiceClientCredentials;
import com.azure.common.credentials.TokenCredentials;
import com.azure.common.http.HttpClient;
import com.azure.common.http.HttpPipeline;
import com.azure.common.http.policy.*;
import com.azure.keyvault.models.Secret;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Configures and builds instance of {@link SecretClient client} to manage {@link Secret secrets} in the specified key vault.
 *
 * <p> A {@link Secret secret} is a resource managed by Key Vault. It is represented by non-null fields secret.name and secret.value.
 * The secret.expires, secret.contentType and secret.notBefore values in {@code secret} are optional. The secret.enabled
 * field is set to true by Azure Key Vault, if not specified. The secret.id, secret.created, secret.updated, secret.recoveryLevel
 * fields are auto assigned when the secret is created in the key vault. </p>
 *
 * <p> Minimal configuration options required by {@link SecretClientBuilder secretClientBuilder} to build {@link SecretAsyncClient}
 * are {@link String vaultEndPoint} and {@link ServiceClientCredentials credentials}. If a custom {@link HttpPipeline pipeline}
 * is specified as configuration option, then no other configuration option needs to be specified. </p>
 *
 * <pre>
 *    SecretClient secretClient = SecretClient.builder()
 *                                .vaultEndPoint("https://myvault.vault.azure.net/")
 *                                .credentials(keyVaultCredentials)
 *                                .build()
 *
 *    SecretClient secretClientWithCustomPipeline = SecretClient.builder()
 *                                                  .pipeline(customHttpPipeline)
 *                                                  .build()
 * </pre>
 *
 * <p> The {@link HttpLogDetailLevel logdetailLevel}, multiple custom {@link HttpPipeline policies} and custom
 * {@link HttpClient httpClient} be optionally configured in the {@link SecretClientBuilder}.</p>
 *
 * <pre>
 *    SecretClient secretClient = SecretClient.builder()
 *                                .vaultEndPoint("https://myvault.vault.azure.net/")
 *                                .credentials(keyVaultCredentials)
 *                                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
 *                                .addPolicy(customPolicyOne)
 *                                .addPolicy(customPolicyTwo)
 *                                .httpClient(client)
 *                                .build()
 * </pre>
 */
public final class SecretClientBuilder {
    private final List<HttpPipelinePolicy> policies;
    private ServiceClientCredentials credentials;
    private HttpPipeline pipeline;
    private URL vaultEndPoint;
    private HttpClient httpClient;
    private HttpLogDetailLevel httpLogDetailLevel;
    private RetryPolicy retryPolicy;
    private String userAgent;

    SecretClientBuilder() {
        userAgent = String.format("Azure-SDK-For-Java/%s (%s)", AzureKeyVaultConfiguration.SDK_NAME, AzureKeyVaultConfiguration.SDK_VERSION);
        retryPolicy = new RetryPolicy();
        httpLogDetailLevel = HttpLogDetailLevel.NONE;
        policies = new ArrayList<>();
    }

    /**
     * Creates a {@link SecretClient} based on options set in the builder.
     * Every time {@code build()} is called, a new instance of {@link SecretClient} is created.
     *
     * <p> If {@link SecretClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link SecretClientBuilder#vaultEndPoint(String) serviceEndpoint} are used to create the
     * {@link SecretClientBuilder client}. All other builder settings are ignored. </p>
     *
     * @return A SecretClient with the options set from the builder.
     * @throws IllegalStateException If {@link SecretClientBuilder#credentials(ServiceClientCredentials)} or
     * {@link SecretClientBuilder#vaultEndPoint(String)} have not been set.
     */
    public SecretClient build() {

        if (pipeline != null) {
            return new SecretClient(vaultEndPoint, pipeline);
        }

        if (credentials == null) {
            throw new IllegalStateException(KeyVaultErrorCodeStrings.getCredentialsRequired());
        }

        if (vaultEndPoint == null) {
            throw new IllegalStateException(KeyVaultErrorCodeStrings.getVaultEndPointRequired());
        }

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(new UserAgentPolicy(userAgent));
        policies.add(retryPolicy);
        policies.add(new CredentialsPolicy(getTokenCredentials()));
        policies.addAll(this.policies);
        policies.add(new HttpLoggingPolicy(httpLogDetailLevel));

        HttpPipeline pipeline = httpClient == null
            ? new HttpPipeline(policies)
            : new HttpPipeline(httpClient, policies);

        return new SecretClient(vaultEndPoint, pipeline);
    }

    /**
     * Sets the vault endpoint url to send HTTP requests to.
     *
     * @param vaultEndPoint The vault endpoint url is used as destination on Azure to send requests to.
     * @return The updated Builder object.
     * @throws MalformedURLException if {@code vaultEndPoint} is null or it cannot be parsed into a valid URL.
     */
    public SecretClientBuilder vaultEndPoint(String vaultEndPoint) throws MalformedURLException {
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
    public SecretClientBuilder credentials(ServiceClientCredentials credentials) {
        Objects.requireNonNull(credentials);
        this.credentials = credentials;
        return this;
    }

    /**
     * Sets the logging level for HTTP requests and responses.
     *
     * <p> logLevel is optional. If not provided, default value of {@link HttpLogDetailLevel#NONE} is set. </p>
     *
     * @param logLevel The amount of logging output when sending and receiving HTTP requests/responses.
     * @return The updated Builder object.
     * @throws NullPointerException if {@code logLevel} is {@code null}.
     */
    public SecretClientBuilder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        Objects.requireNonNull(logLevel);
        httpLogDetailLevel = logLevel;
        return this;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after
     * {@link SecretClient} required policies.
     *
     * @param policy The {@link HttpPipelinePolicy policy} to be added.
     * @return The updated Builder object.
     * @throws NullPointerException if {@code policy} is {@code null}.
     */
    public SecretClientBuilder addPolicy(HttpPipelinePolicy policy) {
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
    public SecretClientBuilder httpClient(HttpClient client) {
        Objects.requireNonNull(client);
        this.httpClient = client;
        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from
     * {@link SecretClientBuilder#vaultEndPoint(String) vaultEndPoint} to build {@link SecretClient}.
     *
     * @param pipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return The updated {@link SecretClientBuilder} object.
     */
    public SecretClientBuilder pipeline(HttpPipeline pipeline) {
        Objects.requireNonNull(pipeline);
        this.pipeline = pipeline;
        return this;
    }

    private TokenCredentials getTokenCredentials(){
        String token = "";
        try{
            token = credentials.authorizationHeaderValue(vaultEndPoint.toString());
        } catch (IOException e){
            e.printStackTrace();
        }
        return new TokenCredentials("Bearer",token);
    }
}
