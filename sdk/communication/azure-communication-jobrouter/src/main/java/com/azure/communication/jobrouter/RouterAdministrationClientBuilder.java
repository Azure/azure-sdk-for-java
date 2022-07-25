package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.implementation.AzureCommunicationRoutingServiceImpl;
import com.azure.communication.jobrouter.implementation.AzureCommunicationRoutingServiceImplBuilder;
import com.azure.communication.jobrouter.implementation.authentication.CommunicationConnectionString;
import com.azure.communication.jobrouter.implementation.authentication.HmacAuthenticationPolicy;
import com.azure.communication.jobrouter.implementation.utils.BuilderHelper;
import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.client.traits.AzureKeyCredentialTrait;
import com.azure.core.client.traits.ConfigurationTrait;
import com.azure.core.client.traits.ConnectionStringTrait;
import com.azure.core.client.traits.EndpointTrait;
import com.azure.core.client.traits.HttpTrait;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.FixedDelayOptions;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ServiceClientBuilder(serviceClients = {RouterAdministrationAsyncClient.class, RouterAdministrationClient.class})
public class RouterAdministrationClientBuilder implements ConfigurationTrait<RouterAdministrationClientBuilder>,
    EndpointTrait<RouterAdministrationClientBuilder>,
    HttpTrait<RouterAdministrationClientBuilder>,
    ConnectionStringTrait<RouterAdministrationClientBuilder>,
    AzureKeyCredentialTrait<RouterAdministrationClientBuilder> {

    private static final ClientLogger LOGGER = new ClientLogger(RouterAdministrationClientBuilder.class);
    private Configuration configuration;
    private String endpoint;
    private HttpClient httpClient;
    private CommunicationConnectionString connectionString;
    private AzureKeyCredential credential;
    private HttpPipeline httpPipeline;
    private final List<HttpPipelinePolicy> customPolicies = new ArrayList<HttpPipelinePolicy>();
    private RetryOptions retryOptions;
    private HttpLogOptions logOptions;
    private ClientOptions clientOptions;

    @Override
    public RouterAdministrationClientBuilder credential(AzureKeyCredential credential) {
        this.credential = Objects.requireNonNull(
            credential, "'credential' cannot be null.");
        return this;
    }

    @Override
    public RouterAdministrationClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    @Override
    public RouterAdministrationClientBuilder connectionString(String connectionString) {
        this.connectionString = new CommunicationConnectionString(connectionString);
        this.credential(new AzureKeyCredential(this.connectionString.getAccessKey()));
        this.endpoint(this.connectionString.getEndpoint());
        return this;
    }

    @Override
    public RouterAdministrationClientBuilder endpoint(String endpoint) {
        this.endpoint = Objects.requireNonNull(endpoint, "'endpoint' cannot be null.");
        return this;
    }

    @Override
    public RouterAdministrationClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient, "'httpClient' cannot be null.");
        return this;
    }

    @Override
    public RouterAdministrationClientBuilder pipeline(HttpPipeline httpPipeline) {
        this.httpPipeline = httpPipeline;
        return this;
    }

    @Override
    public RouterAdministrationClientBuilder addPolicy(HttpPipelinePolicy customPolicy) {
        this.customPolicies.add(Objects.requireNonNull(customPolicy, "'customPolicy' cannot be null."));
        return this;
    }

    @Override
    public RouterAdministrationClientBuilder retryOptions(RetryOptions retryOptions) {
        this.retryOptions = retryOptions;
        return this;
    }

    @Override
    public RouterAdministrationClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        this.logOptions = Objects.requireNonNull(logOptions, "'logOptions' cannot be null.");
        return this;
    }

    @Override
    public RouterAdministrationClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
        return this;
    }

    public RouterAdministrationClient buildClient() {
        RouterAdministrationAsyncClient asyncClient = buildAsyncClient();
        return new RouterAdministrationClient(asyncClient);
    }

    public RouterAdministrationAsyncClient buildAsyncClient() {
        AzureCommunicationRoutingServiceImpl internalClient = createInternalClient();
        return new RouterAdministrationAsyncClient(internalClient);
    }

    private AzureCommunicationRoutingServiceImpl createInternalClient() {
        HttpPipeline pipeline;
        if (httpPipeline != null) {
            pipeline = httpPipeline;
        } else {
            retryOptions = retryOptions != null ? retryOptions : new RetryOptions(new FixedDelayOptions(3, Duration.ofMillis(5)));
            logOptions = logOptions != null ? logOptions : new HttpLogOptions();
            clientOptions = clientOptions != null ? clientOptions :  new ClientOptions();
            pipeline = BuilderHelper.buildPipeline(
                new HmacAuthenticationPolicy(credential),
                retryOptions,
                logOptions,
                clientOptions,
                httpClient,
                customPolicies,
                null,
                configuration,
                LOGGER);
        }

        AzureCommunicationRoutingServiceImplBuilder clientBuilder = new AzureCommunicationRoutingServiceImplBuilder()
            .endpoint(endpoint)
            .pipeline(pipeline);

        return clientBuilder.buildClient();
    }
}
