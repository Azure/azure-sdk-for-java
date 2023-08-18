// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.phonenumbers.siprouting;

import com.azure.communication.common.implementation.HmacAuthenticationPolicy;
import com.azure.communication.phonenumbers.siprouting.implementation.SipRoutingAdminClientImpl;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.*;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SipRoutingClientBuilderTest {
    private static final String ENDPOINT = "https://mycommunication.eastus.dev.communications.azure.net/";
    private static final String ACCESSKEY = "QWNjZXNzS2V5";
    private static final Map<String, String> PROPERTIES =
        CoreUtils.getProperties("azure-communication-phonenumbers-siprouting.properties");
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";

    private HttpClient httpClient;
    private SipRoutingClientBuilder clientBuilder;

    @BeforeEach
    public void setUp() {
        this.httpClient = mock(HttpClient.class);
        this.clientBuilder = Mockito.spy(new SipRoutingClientBuilder());
    }

    @AfterEach
    public void tearDown() {
        Mockito.framework().clearInlineMock(this);
    }

    @Test
    public void buildClientWithHttpClientWithCredential() {
        ClientBuilderSpyHelper spyHelper = new ClientBuilderSpyHelper(this.clientBuilder);

        // Build client with required settings
        SipRoutingClient sipRoutingClient =
            this.setupBuilderWithHttpClientWithCredential(this.clientBuilder).buildClient();

        // Validate client created with expected settings
        assertNotNull(sipRoutingClient);
        validateRequiredSettings(spyHelper);
    }

    @Test
    public void buildClientWithCustomPipeline() {
        ClientBuilderSpyHelper spyHelper = new ClientBuilderSpyHelper(this.clientBuilder);
        HttpPipeline httpPipeline = mock(HttpPipeline.class);

        // Build client with custom pipeline
        SipRoutingClient sipRoutingClient =
            this.setupBuilderCustomPipeline(httpPipeline).buildClient();

        // Validate client created with expected settings
        assertNotNull(sipRoutingClient);
        validateCustomPipeline(spyHelper, httpPipeline);
    }

    @Test
    public void buildClientWithLogOptions() {
        ClientBuilderSpyHelper spyHelper = new ClientBuilderSpyHelper(this.clientBuilder);
        HttpLogOptions logOptions = mock(HttpLogOptions.class);

        // Build client with required settings and mock log options
        SipRoutingClient sipRoutingClient = this.setupBuilderWithHttpClientWithCredential(this.clientBuilder)
            .httpLogOptions(logOptions)
            .buildClient();

        // Validate client created with expected settings
        assertNotNull(sipRoutingClient);
        validateLogOptions(spyHelper, logOptions);
    }

    @Test
    public void buildClientWithConfiguration() {
        ClientBuilderSpyHelper spyHelper = new ClientBuilderSpyHelper(this.clientBuilder);
        Configuration configuration = mock(Configuration.class);

        // Build client with required settings and mock configuration
        SipRoutingClient sipRoutingClient = this.setupBuilderWithHttpClientWithCredential(this.clientBuilder)
            .configuration(configuration)
            .buildClient();

        // Validate client created with expected settings
        assertNotNull(sipRoutingClient);
        validateConfiguration(spyHelper, configuration);
    }

    @Test
    public void buildClientWithServiceVersion() {
        // Build client with required settings and mock configuration
        SipRoutingClient sipRoutingClient = this.setupBuilderWithHttpClientWithCredential(this.clientBuilder)
            .serviceVersion(SipRoutingServiceVersion.V2023_03_01)
            .buildClient();

        // Validate client created with expected settings
        assertNotNull(sipRoutingClient);
    }

    @Test
    public void buildClientWithOneAdditionalPolicy() {
        ClientBuilderSpyHelper spyHelper = new ClientBuilderSpyHelper(this.clientBuilder);
        List<HttpPipelinePolicy> additionalPolicies = new ArrayList<>();
        additionalPolicies.add(mock(HttpPipelinePolicy.class));

        // Build client with required settings and mock policies
        SipRoutingClient sipRoutingClient =
            this.setupBuilderWithPolicies(this.clientBuilder, additionalPolicies)
                .buildClient();

        // Validate client created with expected settings
        assertNotNull(sipRoutingClient);
        validateAdditionalPolicies(spyHelper, additionalPolicies);
    }

    @Test
    public void buildClientWithMultipleAdditionalPolicies() {
        ClientBuilderSpyHelper spyHelper = new ClientBuilderSpyHelper(this.clientBuilder);
        List<HttpPipelinePolicy> additionalPolicies = new ArrayList<>();
        additionalPolicies.add(mock(HttpPipelinePolicy.class));
        additionalPolicies.add(mock(HttpPipelinePolicy.class));
        additionalPolicies.add(mock(HttpPipelinePolicy.class));

        // Build client with required settings and mock policies
        SipRoutingClient sipRoutingClient =
            this.setupBuilderWithPolicies(this.clientBuilder, additionalPolicies)
                .buildClient();

        // Validate client created with expected settings
        assertNotNull(sipRoutingClient);
        validateAdditionalPolicies(spyHelper, additionalPolicies);
    }

    @Test
    public void buildClientNoEndpoint() {
        assertThrows(NullPointerException.class, () -> {
            this.clientBuilder.buildClient();
        });
    }

    @Test
    public void buildClientNoPipelineNoCredentials() {
        assertThrows(NullPointerException.class, () -> {
            this.clientBuilder.endpoint(ENDPOINT).httpClient(this.httpClient).buildClient();
        });
    }

    @Test
    public void buildAsyncClientWithHttpClientWithCredential() {
        ClientBuilderSpyHelper spyHelper = new ClientBuilderSpyHelper(this.clientBuilder);

        // Build client with required settings
        SipRoutingAsyncClient sipRoutingAsyncClient =
            this.setupBuilderWithHttpClientWithCredential(this.clientBuilder).buildAsyncClient();

        // Validate client created with expected settings
        assertNotNull(sipRoutingAsyncClient);
        validateRequiredSettings(spyHelper);
    }

    @Test
    public void buildAsyncClientWithCustomPipeline() {
        ClientBuilderSpyHelper spyHelper = new ClientBuilderSpyHelper(this.clientBuilder);
        HttpPipeline httpPipeline = mock(HttpPipeline.class);

        // Build client with custom pipeline
        SipRoutingAsyncClient sipRoutingAsyncClient =
            this.setupBuilderCustomPipeline(httpPipeline).buildAsyncClient();

        // Validate client created with expected settings
        assertNotNull(sipRoutingAsyncClient);
        validateCustomPipeline(spyHelper, httpPipeline);
    }

    @Test
    public void buildAsyncClientWithLogOptions() {
        ClientBuilderSpyHelper spyHelper = new ClientBuilderSpyHelper(this.clientBuilder);
        HttpLogOptions logOptions = mock(HttpLogOptions.class);

        // Build client with required settings and mock log options
        SipRoutingAsyncClient sipRoutingAsyncClient =
            this.setupBuilderWithHttpClientWithCredential(this.clientBuilder)
                .httpLogOptions(logOptions)
                .buildAsyncClient();

        // Validate client created with expected settings
        assertNotNull(sipRoutingAsyncClient);
        validateLogOptions(spyHelper, logOptions);
    }

    @Test
    public void buildAsyncClientWithConfiguration() {
        ClientBuilderSpyHelper spyHelper = new ClientBuilderSpyHelper(this.clientBuilder);
        Configuration configuration = mock(Configuration.class);

        // Build client with required settings and mock configuration
        SipRoutingAsyncClient sipRoutingAsyncClient =
            this.setupBuilderWithHttpClientWithCredential(this.clientBuilder)
                .configuration(configuration)
                .buildAsyncClient();

        // Validate client created with expected settings
        assertNotNull(sipRoutingAsyncClient);
        validateConfiguration(spyHelper, configuration);
    }

    @Test
    public void buildAsyncClientWithServiceVersion() {
        // Build client with required settings and mock configuration
        SipRoutingAsyncClient sipRoutingAsyncClient =
            this.setupBuilderWithHttpClientWithCredential(this.clientBuilder)
                .serviceVersion(SipRoutingServiceVersion.V2023_03_01)
                .buildAsyncClient();

        // Validate client created with expected settings
        assertNotNull(sipRoutingAsyncClient);
    }

    @Test
    public void buildAsyncClientWithOneAdditionalPolicy() {
        ClientBuilderSpyHelper spyHelper = new ClientBuilderSpyHelper(this.clientBuilder);
        List<HttpPipelinePolicy> additionalPolicies = new ArrayList<>();
        additionalPolicies.add(mock(HttpPipelinePolicy.class));

        // Build client with required settings and mock policies
        SipRoutingAsyncClient sipRoutingAsyncClient =
            this.setupBuilderWithPolicies(this.clientBuilder, additionalPolicies)
                .buildAsyncClient();

        // Validate client created with expected settings
        assertNotNull(sipRoutingAsyncClient);
        validateAdditionalPolicies(spyHelper, additionalPolicies);
    }

    @Test
    public void buildAsyncClientWithMultipleAdditionalPolicies() {
        ClientBuilderSpyHelper spyHelper = new ClientBuilderSpyHelper(this.clientBuilder);
        List<HttpPipelinePolicy> additionalPolicies = new ArrayList<>();
        additionalPolicies.add(mock(HttpPipelinePolicy.class));
        additionalPolicies.add(mock(HttpPipelinePolicy.class));
        additionalPolicies.add(mock(HttpPipelinePolicy.class));

        // Build client with required settings and mock policies
        SipRoutingAsyncClient sipRoutingAsyncClient =
            this.setupBuilderWithPolicies(this.clientBuilder, additionalPolicies)
                .buildAsyncClient();

        // Validate client created with expected settings
        assertNotNull(sipRoutingAsyncClient);
        validateAdditionalPolicies(spyHelper, additionalPolicies);
    }

    @Test
    public void buildAsyncClientNoEndpointThrows() {
        assertThrows(NullPointerException.class, () -> {
            this.clientBuilder.buildClient();
        });
    }

    @Test
    public void buildAsyncClientNoPipelineNoCredentialsThrows() {
        assertThrows(NullPointerException.class, () -> {
            this.clientBuilder.endpoint(ENDPOINT).httpClient(this.httpClient).buildClient();
        });
    }

    @Test
    public void setEndpointNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> {
            this.clientBuilder.endpoint(null);
        });
    }

    @Test
    public void addPolicyNullThrows() {
        assertThrows(NullPointerException.class, () -> {
            this.clientBuilder.addPolicy(null);
        });
    }

    private SipRoutingClientBuilder setupBuilderWithHttpClientWithCredential(SipRoutingClientBuilder clientBuilder) {
        return clientBuilder
            .endpoint(ENDPOINT)
            .httpClient(this.httpClient)
            .credential(new AzureKeyCredential(ACCESSKEY));
    }

    private SipRoutingClientBuilder setupBuilderWithPolicies(
        SipRoutingClientBuilder clientBuilder, List<HttpPipelinePolicy> policies) {
        clientBuilder = this.setupBuilderWithHttpClientWithCredential(clientBuilder);
        for (HttpPipelinePolicy policy : policies) {
            clientBuilder.addPolicy(policy);
        }

        return clientBuilder;
    }

    private SipRoutingClientBuilder setupBuilderCustomPipeline(HttpPipeline pipeline) {
        return clientBuilder
            .endpoint(ENDPOINT)
            .pipeline(pipeline);
    }

    private void validateRequiredSettings(ClientBuilderSpyHelper spyHelper) {
        // Inspect client setup
        spyHelper.captureSipRoutingAdminClientImpl();
        spyHelper.captureHttpPipelineSettings();
        SipRoutingAdminClientImpl sipRoutingManagementClient = spyHelper.sipRoutingAdminClientArg.getValue();

        // Validate required settings
        assertEquals(ENDPOINT, sipRoutingManagementClient.getEndpoint());
        assertEquals(this.httpClient, sipRoutingManagementClient.getHttpPipeline().getHttpClient());

        // Validate HttpPipelinePolicy settings
        int policyCount = sipRoutingManagementClient.getHttpPipeline().getPolicyCount();
        assertTrue(policyCount >= 6);
        assertEquals(spyHelper.userAgentPolicyRef.get(), sipRoutingManagementClient.getHttpPipeline().getPolicy(0));
        assertEquals(spyHelper.requestIdPolicyRef.get(), sipRoutingManagementClient.getHttpPipeline().getPolicy(1));
        assertEquals(spyHelper.authenticationPolicyRef.get(), sipRoutingManagementClient.getHttpPipeline().getPolicy(3));
        assertEquals(spyHelper.cookiePolicyRef.get(), sipRoutingManagementClient.getHttpPipeline().getPolicy(4));
        assertEquals(spyHelper.httpLoggingPolicyRef.get(), sipRoutingManagementClient.getHttpPipeline().getPolicy(policyCount - 1));

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
        spyHelper.captureSipRoutingAdminClientImpl();
        SipRoutingAdminClientImpl sipRoutingAdminClient = spyHelper.sipRoutingAdminClientArg.getValue();

        // Validate http pipeline used
        assertEquals(expectedPipeline, sipRoutingAdminClient.getHttpPipeline());
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
        spyHelper.captureSipRoutingAdminClientImpl();
        SipRoutingAdminClientImpl sipRoutingManagementClient = spyHelper.sipRoutingAdminClientArg.getValue();

        // Validate HttpPipelinePolicy settings
        int expectedMinPolicyCount = 6 + policies.size();
        int actualPolicyCount = sipRoutingManagementClient.getHttpPipeline().getPolicyCount();
        int lastPolicyIndex = actualPolicyCount - 1;
        int customPolicyIndex = 5;

        assertTrue(actualPolicyCount >= expectedMinPolicyCount);
        assertEquals(spyHelper.userAgentPolicyRef.get(), sipRoutingManagementClient.getHttpPipeline().getPolicy(0));
        assertEquals(spyHelper.requestIdPolicyRef.get(), sipRoutingManagementClient.getHttpPipeline().getPolicy(1));
        assertEquals(spyHelper.authenticationPolicyRef.get(), sipRoutingManagementClient.getHttpPipeline().getPolicy(3));
        assertEquals(spyHelper.cookiePolicyRef.get(), sipRoutingManagementClient.getHttpPipeline().getPolicy(4));
        assertEquals(spyHelper.httpLoggingPolicyRef.get(), sipRoutingManagementClient.getHttpPipeline().getPolicy(lastPolicyIndex));

        for (HttpPipelinePolicy policy : policies) {
            assertEquals(policy, sipRoutingManagementClient.getHttpPipeline().getPolicy(customPolicyIndex));
            customPolicyIndex++;
        }
    }

    private class ClientBuilderSpyHelper {
        final SipRoutingClientBuilder clientBuilder;

        final AtomicReference<HmacAuthenticationPolicy> authenticationPolicyRef = new AtomicReference<>();
        final AtomicReference<UserAgentPolicy> userAgentPolicyRef = new AtomicReference<>();
        final AtomicReference<RequestIdPolicy> requestIdPolicyRef = new AtomicReference<>();
        final AtomicReference<RetryPolicy> retryPolicyRef = new AtomicReference<>();
        final AtomicReference<CookiePolicy> cookiePolicyRef = new AtomicReference<>();
        final AtomicReference<HttpLoggingPolicy> httpLoggingPolicyRef = new AtomicReference<>();
        final AtomicReference<HttpLogOptions> defaultHttpLogOptionsRef = new AtomicReference<>();
        final ArgumentCaptor<SipRoutingAdminClientImpl> sipRoutingAdminClientArg =
            ArgumentCaptor.forClass(SipRoutingAdminClientImpl.class);
        final ArgumentCaptor<String> uaPolicyAppIdArg = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> uaPolicySdkNameArg = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> uaPolicySdkVersionArg = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Configuration> uaPolicyConfigArg = ArgumentCaptor.forClass(Configuration.class);
        final ArgumentCaptor<HttpLogOptions> httpLogOptionsArg = ArgumentCaptor.forClass(HttpLogOptions.class);

        ClientBuilderSpyHelper(SipRoutingClientBuilder clientBuilder) {
            this.clientBuilder = clientBuilder;
            this.initializeSpies();
        }

        private void initializeSpies() {
            Answer<HmacAuthenticationPolicy> createCommunicationClientCredentialPolicy = (invocation) -> {
                this.authenticationPolicyRef.set((HmacAuthenticationPolicy) invocation.callRealMethod());
                return this.authenticationPolicyRef.get();
            };
            doAnswer(createCommunicationClientCredentialPolicy).when(this.clientBuilder).createAuthenticationPolicy();

            Answer<UserAgentPolicy> createUserAgentPolicy = (invocation) -> {
                this.userAgentPolicyRef.set(mock(UserAgentPolicy.class));
                return this.userAgentPolicyRef.get();
            };
            doAnswer(createUserAgentPolicy).when(this.clientBuilder).createUserAgentPolicy(any(), any(), any(), any());

            Answer<RequestIdPolicy> createRequestIdPolicy = (invocation) -> {
                this.requestIdPolicyRef.set(mock(RequestIdPolicy.class));
                return this.requestIdPolicyRef.get();
            };
            doAnswer(createRequestIdPolicy).when(this.clientBuilder).createRequestIdPolicy();

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

        void captureSipRoutingAdminClientImpl() {
            verify(this.clientBuilder, atMostOnce())
                .createClientImpl(this.sipRoutingAdminClientArg.capture());
            verify(this.clientBuilder, atMostOnce())
                .createAsyncClientImpl(this.sipRoutingAdminClientArg.capture());
        }

        void captureHttpPipelineSettings() {
            verify(this.clientBuilder, times(1))
                .createAuthenticationPolicy();
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
