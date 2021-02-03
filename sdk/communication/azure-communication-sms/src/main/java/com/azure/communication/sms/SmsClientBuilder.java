// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;

import com.azure.communication.common.implementation.CommunicationConnectionString;
import com.azure.communication.common.implementation.HmacAuthenticationPolicy;
import com.azure.communication.sms.implementation.AzureCommunicationSMSServiceImpl;
import com.azure.communication.sms.implementation.AzureCommunicationSMSServiceImplBuilder;
import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * SmsClientBuilder that creates SmsAsyncClient and SmsClient.
 */
@ServiceClientBuilder(serviceClients = {SmsClient.class, SmsAsyncClient.class})
public final class SmsClientBuilder {
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";
    private static final String APP_CONFIG_PROPERTIES = "azure-communication-sms.properties";

    private final ClientLogger logger = new ClientLogger(SmsClientBuilder.class);
    private String endpoint;
    private AzureKeyCredential accessKeyCredential;
    private TokenCredential tokenCredential;
    private HttpClient httpClient;
    private HttpPipeline pipeline;
    private final Configuration configuration = Configuration.getGlobalConfiguration().clone();
    private final Map<String, String> properties = CoreUtils.getProperties(APP_CONFIG_PROPERTIES);
    private final HttpLogOptions httpLogOptions = new HttpLogOptions();
    private final List<HttpPipelinePolicy> customPolicies = new ArrayList<HttpPipelinePolicy>();

    /**
     * Set endpoint of the service
     *
     * @param endpoint url of the service
     * @return SmsClientBuilder
     */
    public SmsClientBuilder endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Set endpoint of the service
     *
     * @param pipeline HttpPipeline to use, if a pipeline is not
     * supplied, the credential and httpClient fields must be set
     * @return SmsClientBuilder
     */
    public SmsClientBuilder pipeline(HttpPipeline pipeline) {
        this.pipeline = pipeline;
        return this;
    }

    /**
     * Set accessKeyCredential to use
     *
     * @param accessKey access key for initalizing AzureKeyCredential
     * @return SmsClientBuilder
     */
    public SmsClientBuilder accessKey(String accessKey) {
        Objects.requireNonNull(accessKey, "'accessKey' cannot be null.");
        this.accessKeyCredential = new AzureKeyCredential(accessKey);
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authenticate HTTP requests.
     *
     * @param tokenCredential {@link TokenCredential} used to authenticate HTTP requests.
     * @return The updated {@link SmsClientBuilder} object.
     * @throws NullPointerException If {@code tokenCredential} is null.
     */
    public SmsClientBuilder credential(TokenCredential tokenCredential) {
        this.tokenCredential = Objects.requireNonNull(tokenCredential, "'tokenCredential' cannot be null.");
        return this;
    }

     /**
     * Set endpoint and credential to use
     *
     * @param connectionString connection string for setting endpoint and initalizing AzureKeyCredential
     * @return SmsClientBuilder
     */
    public SmsClientBuilder connectionString(String connectionString) {
        Objects.requireNonNull(connectionString, "'connectionString' cannot be null.");
        CommunicationConnectionString connectionStringObject = new CommunicationConnectionString(connectionString);
        String endpoint = connectionStringObject.getEndpoint();
        String accessKey = connectionStringObject.getAccessKey();
        this
            .endpoint(endpoint)
            .accessKey(accessKey);
        return this;
    }


    /**
     * Set httpClient to use
     *
     * @param httpClient httpClient to use, overridden by the pipeline
     * field.
     * @return SmsClientBuilder
     */
    public SmsClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    /**
     * Apply additional HttpPipelinePolicy
     *
     * @param customPolicy HttpPipelinePolicy object to be applied after
     *                       AzureKeyCredentialPolicy, UserAgentPolicy, RetryPolicy, and CookiePolicy
     * @return SmsClientBuilder
     */
    public SmsClientBuilder addPolicy(HttpPipelinePolicy customPolicy) {
        this.customPolicies.add(customPolicy);
        return this;
    }

    /**
     * Create asynchronous client applying HMACAuthenticationPolicy, UserAgentPolicy,
     * RetryPolicy, and CookiePolicy.
     * Additional HttpPolicies specified by additionalPolicies will be applied after them
     *
     * @return SmsAsyncClient instance
     */
    public SmsAsyncClient buildAsyncClient() {
        return new SmsAsyncClient(createServiceImpl());
    }

    /**
     * Create synchronous client applying HmacAuthenticationPolicy, UserAgentPolicy,
     * RetryPolicy, and CookiePolicy.
     * Additional HttpPolicies specified by additionalPolicies will be applied after them
     *
     * @return SmsClient instance
     */
    public SmsClient buildClient() {
        return new SmsClient(buildAsyncClient());
    }

    private AzureCommunicationSMSServiceImpl createServiceImpl() {
        Objects.requireNonNull(endpoint);

        if (this.pipeline == null) {
            Objects.requireNonNull(httpClient);
        }

        HttpPipeline builderPipeline = this.pipeline;
        if (this.pipeline == null) {
            HttpPipelinePolicy[] customPolicyArray = null;
            if (customPolicies.size() > 0) {
                customPolicyArray = new HttpPipelinePolicy[customPolicies.size()];
                customPolicyArray = customPolicies.toArray(customPolicyArray);
            }

            builderPipeline = createHttpPipeline(httpClient,
                createHttpPipelineAuthPolicy(),
                customPolicyArray);
        }

        AzureCommunicationSMSServiceImplBuilder clientBuilder = new AzureCommunicationSMSServiceImplBuilder();
        clientBuilder.endpoint(endpoint)
            .pipeline(builderPipeline);

        return clientBuilder.buildClient();
    }

    private HttpPipelinePolicy createHttpPipelineAuthPolicy() {
        if (this.tokenCredential != null && this.accessKeyCredential != null) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Both 'credential' and 'accessKey' are set. Just one may be used."));
        }
        if (this.tokenCredential != null) {
            return new BearerTokenAuthenticationPolicy(
                this.tokenCredential, "https://communication.azure.com//.default");
        } else if (this.accessKeyCredential != null) {
            return new HmacAuthenticationPolicy(this.accessKeyCredential);
        } else {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Missing credential information while building a client."));
        }
    }


    private HttpPipeline createHttpPipeline(HttpClient httpClient,
                                            HttpPipelinePolicy authorizationPolicy,
                                            HttpPipelinePolicy[] additionalPolicies) {

        HttpPipelinePolicy[] policies = new HttpPipelinePolicy[4];
        if (additionalPolicies != null) {
            policies = new HttpPipelinePolicy[4 + additionalPolicies.length];
            applyAdditionalPolicies(policies, additionalPolicies);
        }
        policies[0] = authorizationPolicy;
        applyRequirePolicies(policies);

        return new HttpPipelineBuilder()
            .policies(policies)
            .httpClient(httpClient)
            .build();
    }

    private void applyRequirePolicies(HttpPipelinePolicy[] policies) {
        String clientName = properties.getOrDefault(SDK_NAME, "UnknownName");
        String clientVersion = properties.getOrDefault(SDK_VERSION, "UnknownVersion");

        policies[1] = new UserAgentPolicy(httpLogOptions.getApplicationId(), clientName, clientVersion, configuration);
        policies[2] = new RetryPolicy();
        policies[3] = new CookiePolicy();
    }

    private void applyAdditionalPolicies(HttpPipelinePolicy[] policies,
                                         HttpPipelinePolicy[] customPolicies) {
        for (int i = 0; i < customPolicies.length; i++) {
            policies[4 + i] = customPolicies[i];

        }
    }
}
