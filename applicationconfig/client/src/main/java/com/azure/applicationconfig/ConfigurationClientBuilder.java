package com.azure.applicationconfig;

import com.azure.applicationconfig.credentials.ConfigurationClientCredentials;
import com.azure.applicationconfig.models.ConfigurationSetting;
import com.azure.common.http.HttpClient;
import com.azure.common.http.HttpHeaders;
import com.azure.common.http.HttpPipeline;
import com.azure.common.http.policy.HttpLogDetailLevel;
import com.azure.common.http.policy.HttpPipelinePolicy;

import java.net.MalformedURLException;

/**
 * Builds instances of {@link ConfigurationClient} based on the configuration options stored in the builder when
 * {@link ConfigurationClientBuilder#build()} is called.
 *
 * <p>
 *     To build ConfigurationAsyncClients that can interact with Azure App Configuration the service endpoint and
 *     authentication {@link HttpHeaders} are required. This information can be passed to the builder using two primary
 *     means, first being a {@link ConfigurationClientCredentials} and second using a {@link HttpPipeline} and the
 *     service endpoint.
 * </p>
 *
 * <p>
 *     The ConfigurationClientCredentials object contains the service endpoint and a method to retrieve the authentication
 *     headers. {@link ConfigurationClientBuilder#credentials(ConfigurationClientCredentials)} sets the
 *     {@code serviceEndpoint} member variable to {@link ConfigurationClientCredentials#baseUri()} and {@code credentials}
 *     member variable to the passed ConfigurationClientCredentials. When building the ConfigurationAsyncClient the
 *     builder will use default {@link HttpPipelinePolicy policies}, including the ConfigurationClientCredentials, to
 *     construct a HttpPipeline that will be used by the client to interact with the service; a new pipeline will be
 *     constructed every build.
 *
 * <pre>
 * {@code ConfigurationClientCredentials credentials = new ConfigurationClientCredentials(connectionString);
 *    ConfigurationAsyncClient.builder()
 *        .credentials(credentials)
 *        .build();}
 * </pre>
 * </p>
 *
 * <p>
 *     An HttpPipeline performs the communication between the client and service. Using an HttpPipeline to construct a
 *     ConfigurationAsyncClient requires additional setup as it doesn't generate default policies, but it allows for finer
 *     controller. {@link ConfigurationClientBuilder#pipeline(HttpPipeline)} sets the {@code pipeline} but unlike credentials
 *     doesn't set the service endpoint as a pipeline doesn't have that information, {@link ConfigurationClientBuilder#serviceEndpoint(String)}
 *     must be called to set the service endpoint. When building the ConfigurationAsyncClient the HttpPipeline and service
 *     endpoint as simply passed into the client constructor.
 *
 * <pre>
 * {@code String serviceEndpoint = <App-Configuration-URL>;
 *    HttpPipeline pipeline = new HttpPipeline(<policies>);
 *    ConfigurationClient client = ConfigurationClient.builder()
 *        .pipeline(pipeline)
 *        .serviceEndpoint(serviceEndpoint)
 *        .build();}
 * </pre>
 * </p>
 *
 * @see ConfigurationAsyncClient
 * @see ConfigurationClientCredentials
 */
public final class ConfigurationClientBuilder {
    private final ConfigurationAsyncClientBuilder builder;

    ConfigurationClientBuilder() {
        builder = ConfigurationAsyncClient.builder();
    }

    /**
     * Creates a {@link ConfigurationClient} based on options set in the Builder. Every time {@code build()} is
     * called, a new instance of {@link ConfigurationClient} is created.
     *
     * <p>
     * If {@link ConfigurationClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link ConfigurationClientBuilder#serviceEndpoint(String) serviceEndpoint} are used to create the
     * {@link ConfigurationClient client}. All other builder settings are ignored.
     * </p>
     *
     * @return A ConfigurationAsyncClient with the options set from the builder.
     * @throws NullPointerException If {@code serviceEndpoint} has not been set. This setting is automatically set when
     * {@link ConfigurationClientBuilder#credentials(ConfigurationClientCredentials) credentials} are set through
     * the builder. Or can be set explicitly by calling {@link ConfigurationClientBuilder#serviceEndpoint(String)}.
     * @throws IllegalStateException If {@link ConfigurationClientBuilder#credentials(ConfigurationClientCredentials)}
     * has not been set.
     */
    public ConfigurationClient build() {
        return new ConfigurationClient(builder.build());
    }

    /**
     * Sets the service endpoint for the Azure App Configuration instance.
     *
     * @param serviceEndpoint The URL of the Azure App Configuration instance to send {@link ConfigurationSetting}
     * service requests to and receive responses from.
     * @return The updated ConfigurationClientBuilder object.
     * @throws MalformedURLException if {@code serviceEndpoint} is null or it cannot be parsed into a valid URL.
     */
    public ConfigurationClientBuilder serviceEndpoint(String serviceEndpoint) throws MalformedURLException {
        builder.serviceEndpoint(serviceEndpoint);
        return this;
    }

    /**
     * Sets the credentials to use when authenticating HTTP requests. Also, sets the
     * {@link ConfigurationClientBuilder#serviceEndpoint(String) serviceEndpoint} for this ConfigurationClientBuilder.
     *
     * @param credentials The credentials to use for authenticating HTTP requests.
     * @return The updated ConfigurationClientBuilder object.
     * @throws NullPointerException If {@code credentials} is {@code null}.
     */
    public ConfigurationClientBuilder credentials(ConfigurationClientCredentials credentials) {
        builder.credentials(credentials);
        return this;
    }

    /**
     * Sets the logging level for HTTP requests and responses.
     *
     * @param logLevel The amount of logging output when sending and receiving HTTP requests/responses.
     * @return The updated ConfigurationClientBuilder object.
     */
    public ConfigurationClientBuilder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        builder.httpLogDetailLevel(logLevel);
        return this;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after
     * {@link ConfigurationClient} required policies.
     *
     * @param policy The retry policy for service requests.
     * @return The updated ConfigurationClientBuilder object.
     * @throws NullPointerException If {@code policy} is {@code null}.
     */
    public ConfigurationClientBuilder addPolicy(HttpPipelinePolicy policy) {
        builder.addPolicy(policy);
        return this;
    }

    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param client The HTTP client to use for requests.
     * @return The updated ConfigurationClientBuilder object.
     * @throws NullPointerException If {@code client} is {@code null}.
     */
    public ConfigurationClientBuilder httpClient(HttpClient client) {
        builder.httpClient(client);
        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from
     * {@link ConfigurationClientBuilder#serviceEndpoint(String) serviceEndpoint} to build {@link ConfigurationClient}.
     *
     * @param pipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return The updated ConfigurationClientBuilder object.
     */
    public ConfigurationClientBuilder pipeline(HttpPipeline pipeline) {
        builder.pipeline(pipeline);
        return this;
    }
}
