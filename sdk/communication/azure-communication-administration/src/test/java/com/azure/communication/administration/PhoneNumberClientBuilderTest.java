// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.administration;

import com.azure.communication.administration.implementation.PhoneNumberAdminClientImpl;
import com.azure.communication.common.CommunicationClientCredential;
import com.azure.communication.common.HmacAuthenticationPolicy;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.*;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Execution(value = ExecutionMode.SAME_THREAD)
public class PhoneNumberClientBuilderTest {
    private static final String ENDPOINT = "https://mycommunication.eastus.dev.communications.azure.net/";
    private static final String ACCESSKEY = "QWNjZXNzS2V5";
    private static final Map<String, String> PROPERTIES =
        CoreUtils.getProperties("azure-communication-administration.properties");
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";

    private HttpClient httpClient;
    private PhoneNumberClientBuilder clientBuilder;

    @BeforeEach
    void setUp() {
        this.httpClient = mock(HttpClient.class);
        this.clientBuilder = Mockito.spy(new PhoneNumberClientBuilder());
    }

    @AfterEach
    void tearDown() {
        Mockito.framework().clearInlineMocks();
    }

    @Test()
    public void buildClientWithHttpClientWithCredential() {
        ClientBuilderSpyHelper spyHelper = new ClientBuilderSpyHelper(this.clientBuilder);

        // Build client with required settings
        PhoneNumberClient phoneNumberClient =
            this.setupBuilderWithHttpClientWithCredential(this.clientBuilder).buildClient();

        // Validate client created with expected settings
        assertNotNull(phoneNumberClient);
        validateRequiredSettings(spyHelper);
    }

    @Test()
    public void buildClientWithCustomPipeline() {
        ClientBuilderSpyHelper spyHelper = new ClientBuilderSpyHelper(this.clientBuilder);
        HttpPipeline httpPipeline = mock(HttpPipeline.class);

        // Build client with custom pipeline
        PhoneNumberClient phoneNumberClient =
            this.setupBuilderCustomPipeline(httpPipeline).buildClient();

        // Validate client created with expected settings
        assertNotNull(phoneNumberClient);
        validateCustomPipeline(spyHelper, httpPipeline);
    }

    @Test()
    public void buildClientWithLogOptions() {
        ClientBuilderSpyHelper spyHelper = new ClientBuilderSpyHelper(this.clientBuilder);
        HttpLogOptions logOptions = mock(HttpLogOptions.class);

        // Build client with required settings and mock log options
        PhoneNumberClient phoneNumberClient = this.setupBuilderWithHttpClientWithCredential(this.clientBuilder)
            .httpLogOptions(logOptions)
            .buildClient();

        // Validate client created with expected settings
        assertNotNull(phoneNumberClient);
        validateLogOptions(spyHelper, logOptions);
    }

    @Test()
    public void buildClientWithConfiguration() {
        ClientBuilderSpyHelper spyHelper = new ClientBuilderSpyHelper(this.clientBuilder);
        Configuration configuration = mock(Configuration.class);

        // Build client with required settings and mock configuration
        PhoneNumberClient phoneNumberClient = this.setupBuilderWithHttpClientWithCredential(this.clientBuilder)
            .configuration(configuration)
            .buildClient();

        // Validate client created with expected settings
        assertNotNull(phoneNumberClient);
        validateConfiguration(spyHelper, configuration);
    }

    @Test()
    public void buildClientWithOneAdditionalPolicy() {
        ClientBuilderSpyHelper spyHelper = new ClientBuilderSpyHelper(this.clientBuilder);
        List<HttpPipelinePolicy> additionalPolicies = new ArrayList<>();
        additionalPolicies.add(mock(HttpPipelinePolicy.class));

        // Build client with required settings and mock policies
        PhoneNumberClient phoneNumberClient =
            this.setupBuilderWithPolicies(this.clientBuilder, additionalPolicies)
                .buildClient();

        // Validate client created with expected settings
        assertNotNull(phoneNumberClient);
        validateAdditionalPolicies(spyHelper, additionalPolicies);
    }

    @Test()
    public void buildClientWithMultipleAdditionalPolicies() {
        ClientBuilderSpyHelper spyHelper = new ClientBuilderSpyHelper(this.clientBuilder);
        List<HttpPipelinePolicy> additionalPolicies = new ArrayList<>();
        additionalPolicies.add(mock(HttpPipelinePolicy.class));
        additionalPolicies.add(mock(HttpPipelinePolicy.class));
        additionalPolicies.add(mock(HttpPipelinePolicy.class));

        // Build client with required settings and mock policies
        PhoneNumberClient phoneNumberClient =
            this.setupBuilderWithPolicies(this.clientBuilder, additionalPolicies)
                .buildClient();

        // Validate client created with expected settings
        assertNotNull(phoneNumberClient);
        validateAdditionalPolicies(spyHelper, additionalPolicies);
    }

    @Test()
    public void buildClientNoEndpoint() {
        assertThrows(NullPointerException.class, () -> {
            this.clientBuilder.buildClient();
        });
    }

    @Test()
    public void buildClientNoPipelineNoHttpClient() {
        assertThrows(NullPointerException.class, () -> {
            this.clientBuilder.endpoint(ENDPOINT).accessKey(ACCESSKEY).buildClient();
        });
    }

    @Test()
    public void buildClientNoPipelineNoCredentials() {
        assertThrows(NullPointerException.class, () -> {
            this.clientBuilder.endpoint(ENDPOINT).httpClient(this.httpClient).buildClient();
        });
    }

    @Test()
    public void buildAsyncClientWithHttpClientWithCredential() {
        ClientBuilderSpyHelper spyHelper = new ClientBuilderSpyHelper(this.clientBuilder);

        // Build client with required settings
        PhoneNumberAsyncClient phoneNumberAsyncClient =
            this.setupBuilderWithHttpClientWithCredential(this.clientBuilder).buildAsyncClient();

        // Validate client created with expected settings
        assertNotNull(phoneNumberAsyncClient);
        validateRequiredSettings(spyHelper);
    }

    @Test()
    public void buildAsyncClientWithCustomPipeline() {
        ClientBuilderSpyHelper spyHelper = new ClientBuilderSpyHelper(this.clientBuilder);
        HttpPipeline httpPipeline = mock(HttpPipeline.class);

        // Build client with custom pipeline
        PhoneNumberAsyncClient phoneNumberAsyncClient =
            this.setupBuilderCustomPipeline(httpPipeline).buildAsyncClient();

        // Validate client created with expected settings
        assertNotNull(phoneNumberAsyncClient);
        validateCustomPipeline(spyHelper, httpPipeline);
    }

    @Test()
    public void buildAsyncClientWithLogOptions() {
        ClientBuilderSpyHelper spyHelper = new ClientBuilderSpyHelper(this.clientBuilder);
        HttpLogOptions logOptions = mock(HttpLogOptions.class);

        // Build client with required settings and mock log options
        PhoneNumberAsyncClient phoneNumberAsyncClient =
            this.setupBuilderWithHttpClientWithCredential(this.clientBuilder)
                .httpLogOptions(logOptions)
                .buildAsyncClient();

        // Validate client created with expected settings
        assertNotNull(phoneNumberAsyncClient);
        validateLogOptions(spyHelper, logOptions);
    }

    @Test()
    public void buildAsyncClientWithConfiguration() {
        ClientBuilderSpyHelper spyHelper = new ClientBuilderSpyHelper(this.clientBuilder);
        Configuration configuration = mock(Configuration.class);

        // Build client with required settings and mock configuration
        PhoneNumberAsyncClient phoneNumberAsyncClient =
            this.setupBuilderWithHttpClientWithCredential(this.clientBuilder)
                .configuration(configuration)
                .buildAsyncClient();

        // Validate client created with expected settings
        assertNotNull(phoneNumberAsyncClient);
        validateConfiguration(spyHelper, configuration);
    }

    @Test()
    public void buildAsyncClientWithOneAdditionalPolicy() {
        ClientBuilderSpyHelper spyHelper = new ClientBuilderSpyHelper(this.clientBuilder);
        List<HttpPipelinePolicy> additionalPolicies = new ArrayList<>();
        additionalPolicies.add(mock(HttpPipelinePolicy.class));

        // Build client with required settings and mock policies
        PhoneNumberAsyncClient phoneNumberAsyncClient =
            this.setupBuilderWithPolicies(this.clientBuilder, additionalPolicies)
                .buildAsyncClient();

        // Validate client created with expected settings
        assertNotNull(phoneNumberAsyncClient);
        validateAdditionalPolicies(spyHelper, additionalPolicies);
    }

    @Test()
    public void buildAsyncClientWithMultipleAdditionalPolicies() {
        ClientBuilderSpyHelper spyHelper = new ClientBuilderSpyHelper(this.clientBuilder);
        List<HttpPipelinePolicy> additionalPolicies = new ArrayList<>();
        additionalPolicies.add(mock(HttpPipelinePolicy.class));
        additionalPolicies.add(mock(HttpPipelinePolicy.class));
        additionalPolicies.add(mock(HttpPipelinePolicy.class));

        // Build client with required settings and mock policies
        PhoneNumberAsyncClient phoneNumberAsyncClient =
            this.setupBuilderWithPolicies(this.clientBuilder, additionalPolicies)
                .buildAsyncClient();

        // Validate client created with expected settings
        assertNotNull(phoneNumberAsyncClient);
        validateAdditionalPolicies(spyHelper, additionalPolicies);
    }

    @Test()
    public void buildAsyncClientNoEndpoint() {
        assertThrows(NullPointerException.class, () -> {
            this.clientBuilder.buildClient();
        });
    }

    @Test()
    public void buildAsyncClientNoPipelineNoHttpClient() {
        assertThrows(NullPointerException.class, () -> {
            this.clientBuilder.endpoint(ENDPOINT).accessKey(ACCESSKEY).buildClient();
        });
    }

    @Test()
    public void buildAsyncClientNoPipelineNoCredentials() {
        assertThrows(NullPointerException.class, () -> {
            this.clientBuilder.endpoint(ENDPOINT).httpClient(this.httpClient).buildClient();
        });
    }

    @Test()
    public void setEndpointNull() {
        assertThrows(NullPointerException.class, () -> {
            this.clientBuilder.endpoint(null);
        });
    }


    @Test()
    public void setAccessKeyNull() {
        assertThrows(NullPointerException.class, () -> {
            this.clientBuilder.accessKey(null);
        });
    }

    @Test()
    public void addPolicyNull() {
        assertThrows(NullPointerException.class, () -> {
            this.clientBuilder.addPolicy(null);
        });
    }

    private PhoneNumberClientBuilder setupBuilderWithHttpClientWithCredential(PhoneNumberClientBuilder clientBuilder) {
        return clientBuilder
            .endpoint(ENDPOINT)
            .httpClient(this.httpClient)
            .accessKey(ACCESSKEY);
    }

    private PhoneNumberClientBuilder setupBuilderWithPolicies(
        PhoneNumberClientBuilder clientBuilder, List<HttpPipelinePolicy> policies) {
        clientBuilder = this.setupBuilderWithHttpClientWithCredential(clientBuilder);
        for (HttpPipelinePolicy policy : policies) {
            clientBuilder.addPolicy(policy);
        }

        return clientBuilder;
    }

    private PhoneNumberClientBuilder setupBuilderCustomPipeline(HttpPipeline pipeline) {
        return clientBuilder
            .endpoint(ENDPOINT)
            .pipeline(pipeline);
    }

    private void validateRequiredSettings(ClientBuilderSpyHelper spyHelper) {
        // Inspect client setup
        spyHelper.capturePhoneNumberAdminClientImpl();
        spyHelper.captureHttpPipelineSettings();
        PhoneNumberAdminClientImpl phoneNumberManagementClient = spyHelper.phoneNumberAdminClientArg.getValue();

        // Validate required settings
        assertEquals(ENDPOINT, phoneNumberManagementClient.getEndpoint());
        assertEquals(this.httpClient, phoneNumberManagementClient.getHttpPipeline().getHttpClient());

        // Validate HttpPipelinePolicy settings
        assertEquals(5, phoneNumberManagementClient.getHttpPipeline().getPolicyCount());
        assertEquals(spyHelper.authenticationPolicyRef.get(), phoneNumberManagementClient.getHttpPipeline().getPolicy(0));
        assertEquals(spyHelper.userAgentPolicyRef.get(), phoneNumberManagementClient.getHttpPipeline().getPolicy(1));
        assertEquals(spyHelper.retryPolicyRef.get(), phoneNumberManagementClient.getHttpPipeline().getPolicy(2));
        assertEquals(spyHelper.cookiePolicyRef.get(), phoneNumberManagementClient.getHttpPipeline().getPolicy(3));
        assertEquals(spyHelper.httpLoggingPolicyRef.get(), phoneNumberManagementClient.getHttpPipeline().getPolicy(4));

        // Validate HttpLogOptions
        assertEquals(spyHelper.defaultHttpLogOptionsRef.get(), spyHelper.httpLogOptionsArg.getValue());

        // Validate UserAgentPolicy settings
        assertEquals(spyHelper.defaultHttpLogOptionsRef.get().getApplicationId(), spyHelper.uaPolicyAppIdArg.getValue());
        assertEquals(PROPERTIES.get((SDK_NAME)), spyHelper.uaPolicySdkNameArg.getValue());
        assertEquals(PROPERTIES.get((SDK_VERSION)), spyHelper.uaPolicySdkVersionArg.getValue());
        assertNull(spyHelper.uaPolicyConfigArg.getValue());
    }

    private void validateCustomPipeline(ClientBuilderSpyHelper spyHelper, HttpPipeline expectedPipeline) {
        // Inspect client setup
        spyHelper.capturePhoneNumberAdminClientImpl();
        PhoneNumberAdminClientImpl phoneNumberAdminClient = spyHelper.phoneNumberAdminClientArg.getValue();

        // Validate http pipeline used
        assertEquals(expectedPipeline, phoneNumberAdminClient.getHttpPipeline());
    }

    private void validateLogOptions(ClientBuilderSpyHelper spyHelper, HttpLogOptions expectedLogOptions) {
        // Inspect client setup
        spyHelper.captureHttpPipelineSettings();
        HttpLogOptions actualLogOptions = spyHelper.httpLogOptionsArg.getValue();

        // Validate log options used
        assertEquals(expectedLogOptions, actualLogOptions);
    }

    private void validateConfiguration(ClientBuilderSpyHelper spyHelper, Configuration expectedConfiguration) {
        // Inspect client setup
        spyHelper.captureHttpPipelineSettings();
        Configuration actualConfiguration = spyHelper.uaPolicyConfigArg.getValue();

        // Validate configuration used
        assertEquals(expectedConfiguration, actualConfiguration);
    }

    private void validateAdditionalPolicies(ClientBuilderSpyHelper spyHelper, List<HttpPipelinePolicy> policies) {
        // Inspect client setup
        spyHelper.capturePhoneNumberAdminClientImpl();
        PhoneNumberAdminClientImpl phoneNumberManagementClient = spyHelper.phoneNumberAdminClientArg.getValue();

        // Validate HttpPipelinePolicy settings
        int expectedPolicyCount = 5 + policies.size();
        int lastPolicyIndex = expectedPolicyCount - 1;
        int customPolicyIndex = 4;

        assertEquals(expectedPolicyCount, phoneNumberManagementClient.getHttpPipeline().getPolicyCount());
        assertEquals(spyHelper.authenticationPolicyRef.get(), phoneNumberManagementClient.getHttpPipeline().getPolicy(0));
        assertEquals(spyHelper.userAgentPolicyRef.get(), phoneNumberManagementClient.getHttpPipeline().getPolicy(1));
        assertEquals(spyHelper.retryPolicyRef.get(), phoneNumberManagementClient.getHttpPipeline().getPolicy(2));
        assertEquals(spyHelper.cookiePolicyRef.get(), phoneNumberManagementClient.getHttpPipeline().getPolicy(3));
        assertEquals(spyHelper.httpLoggingPolicyRef.get(), phoneNumberManagementClient.getHttpPipeline().getPolicy(lastPolicyIndex));

        for (HttpPipelinePolicy policy : policies) {
            assertEquals(policy, phoneNumberManagementClient.getHttpPipeline().getPolicy(customPolicyIndex));
            customPolicyIndex++;
        }
    }

    private class ClientBuilderSpyHelper {
        final PhoneNumberClientBuilder clientBuilder;

        final AtomicReference<HmacAuthenticationPolicy> authenticationPolicyRef = new AtomicReference<>();
        final AtomicReference<UserAgentPolicy> userAgentPolicyRef = new AtomicReference<>();
        final AtomicReference<RetryPolicy> retryPolicyRef = new AtomicReference<>();
        final AtomicReference<CookiePolicy> cookiePolicyRef = new AtomicReference<>();
        final AtomicReference<HttpLoggingPolicy> httpLoggingPolicyRef = new AtomicReference<>();
        final AtomicReference<HttpLogOptions> defaultHttpLogOptionsRef = new AtomicReference<>();
        final ArgumentCaptor<PhoneNumberAdminClientImpl> phoneNumberAdminClientArg =
            ArgumentCaptor.forClass(PhoneNumberAdminClientImpl.class);
        final ArgumentCaptor<CommunicationClientCredential> credentialArg =
            ArgumentCaptor.forClass(CommunicationClientCredential.class);
        final ArgumentCaptor<String> uaPolicyAppIdArg = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> uaPolicySdkNameArg = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> uaPolicySdkVersionArg = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Configuration> uaPolicyConfigArg = ArgumentCaptor.forClass(Configuration.class);
        final ArgumentCaptor<HttpLogOptions> httpLogOptionsArg = ArgumentCaptor.forClass(HttpLogOptions.class);

        ClientBuilderSpyHelper(PhoneNumberClientBuilder clientBuilder) {
            this.clientBuilder = clientBuilder;
            this.initializeSpies();
        }

        private void initializeSpies() {
            Answer<HmacAuthenticationPolicy> createCommunicationClientCredentialPolicy = (invocation) -> {
                this.authenticationPolicyRef.set((HmacAuthenticationPolicy) invocation.callRealMethod());
                return this.authenticationPolicyRef.get();
            };
            doAnswer(createCommunicationClientCredentialPolicy).when(this.clientBuilder).createAuthenticationPolicy(any());

            Answer<UserAgentPolicy> createUserAgentPolicy = (invocation) -> {
                this.userAgentPolicyRef.set(mock(UserAgentPolicy.class));
                return this.userAgentPolicyRef.get();
            };
            doAnswer(createUserAgentPolicy).when(this.clientBuilder).createUserAgentPolicy(any(), any(), any(), any());

            Answer<RetryPolicy> createRetryPolicy = (invocation) -> {
                this.retryPolicyRef.set((RetryPolicy) invocation.callRealMethod());
                return this.retryPolicyRef.get();
            };
            doAnswer(createRetryPolicy).when(this.clientBuilder).createRetryPolicy();

            Answer<CookiePolicy> createCookiePolicy = (invocation) -> {
                this.cookiePolicyRef.set((CookiePolicy) invocation.callRealMethod());
                return this.cookiePolicyRef.get();
            };
            doAnswer(createCookiePolicy).when(this.clientBuilder).createCookiePolicy();

            Answer<HttpLoggingPolicy> createHttpLoggingPolicy = (invocation) -> {
                this.httpLoggingPolicyRef.set((HttpLoggingPolicy) invocation.callRealMethod());
                return this.httpLoggingPolicyRef.get();
            };
            doAnswer(createHttpLoggingPolicy).when(this.clientBuilder).createHttpLoggingPolicy(any());

            Answer<HttpLogOptions> createDefaultHttpLogOptions = (invocation) -> {
                this.defaultHttpLogOptionsRef.set((HttpLogOptions) invocation.callRealMethod());
                return this.defaultHttpLogOptionsRef.get();
            };
            doAnswer(createDefaultHttpLogOptions).when(this.clientBuilder).createDefaultHttpLogOptions();
        }

        void capturePhoneNumberAdminClientImpl() {
            verify(this.clientBuilder, times(1))
                .createPhoneNumberAsyncClient(this.phoneNumberAdminClientArg.capture());
        }

        void captureHttpPipelineSettings() {
            verify(this.clientBuilder, times(1))
                .createAuthenticationPolicy(this.credentialArg.capture());
            verify(this.clientBuilder, times(1))
                .createUserAgentPolicy(
                    this.uaPolicyAppIdArg.capture(),
                    this.uaPolicySdkNameArg.capture(),
                    this.uaPolicySdkVersionArg.capture(),
                    this.uaPolicyConfigArg.capture());
            verify(this.clientBuilder, times(1))
                .createHttpLoggingPolicy(this.httpLogOptionsArg.capture());
        }
    }
}
