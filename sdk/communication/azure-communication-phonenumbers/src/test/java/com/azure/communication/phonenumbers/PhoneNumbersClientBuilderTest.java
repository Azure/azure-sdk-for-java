// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.phonenumbers;

import com.azure.communication.phonenumbers.implementation.PhoneNumberAdminClientImpl;
import com.azure.communication.common.implementation.HmacAuthenticationPolicy;
import com.azure.core.credential.AzureKeyCredential;
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
public class PhoneNumbersClientBuilderTest {
    private static final String ENDPOINT = "https://mycommunication.eastus.dev.communications.azure.net/";
    private static final String ACCESSKEY = "QWNjZXNzS2V5";
    private static final Map<String, String> PROPERTIES =
        CoreUtils.getProperties("azure-communication-phonenumbers.properties");
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";

    private HttpClient httpClient;
    private PhoneNumbersClientBuilder clientBuilder;

    @BeforeEach
    void setUp() {
        this.httpClient = mock(HttpClient.class);
        this.clientBuilder = Mockito.spy(new PhoneNumbersClientBuilder());
    }

    @AfterEach
    void tearDown() {
        Mockito.framework().clearInlineMock(this);
    }

    @Test()
    public void buildClientWithHttpClientWithCredential() {
        ClientBuilderSpyHelper spyHelper = new ClientBuilderSpyHelper(this.clientBuilder);

        // Build client with required settings
        PhoneNumbersClient phoneNumberClient =
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
        PhoneNumbersClient phoneNumberClient =
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
        PhoneNumbersClient phoneNumberClient = this.setupBuilderWithHttpClientWithCredential(this.clientBuilder)
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
        PhoneNumbersClient phoneNumberClient = this.setupBuilderWithHttpClientWithCredential(this.clientBuilder)
            .configuration(configuration)
            .buildClient();

        // Validate client created with expected settings
        assertNotNull(phoneNumberClient);
        validateConfiguration(spyHelper, configuration);
    }

    @Test()
    public void buildClientWithServiceVersion() {
        // Build client with required settings and mock configuration
        PhoneNumbersClient phoneNumberClient = this.setupBuilderWithHttpClientWithCredential(this.clientBuilder)
            .serviceVersion(PhoneNumbersServiceVersion.V2021_03_07)
            .buildClient();

        // Validate client created with expected settings
        assertNotNull(phoneNumberClient);
    }

    @Test()
    public void buildClientWithOneAdditionalPolicy() {
        ClientBuilderSpyHelper spyHelper = new ClientBuilderSpyHelper(this.clientBuilder);
        List<HttpPipelinePolicy> additionalPolicies = new ArrayList<>();
        additionalPolicies.add(mock(HttpPipelinePolicy.class));

        // Build client with required settings and mock policies
        PhoneNumbersClient phoneNumberClient =
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
        PhoneNumbersClient phoneNumberClient =
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
    public void buildClientNoPipelineNoCredentials() {
        assertThrows(NullPointerException.class, () -> {
            this.clientBuilder.endpoint(ENDPOINT).httpClient(this.httpClient).buildClient();
        });
    }

    @Test()
    public void buildAsyncClientWithHttpClientWithCredential() {
        ClientBuilderSpyHelper spyHelper = new ClientBuilderSpyHelper(this.clientBuilder);

        // Build client with required settings
        PhoneNumbersAsyncClient phoneNumberAsyncClient =
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
        PhoneNumbersAsyncClient phoneNumberAsyncClient =
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
        PhoneNumbersAsyncClient phoneNumberAsyncClient =
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
        PhoneNumbersAsyncClient phoneNumberAsyncClient =
            this.setupBuilderWithHttpClientWithCredential(this.clientBuilder)
                .configuration(configuration)
                .buildAsyncClient();

        // Validate client created with expected settings
        assertNotNull(phoneNumberAsyncClient);
        validateConfiguration(spyHelper, configuration);
    }


    @Test()
    public void buildAsyncClientWithServiceVersion() {
        // Build client with required settings and mock configuration
        PhoneNumbersAsyncClient phoneNumberAsyncClient =
            this.setupBuilderWithHttpClientWithCredential(this.clientBuilder)
                .serviceVersion(PhoneNumbersServiceVersion.V2021_03_07)
                .buildAsyncClient();

        // Validate client created with expected settings
        assertNotNull(phoneNumberAsyncClient);
    }

    @Test()
    public void buildAsyncClientWithOneAdditionalPolicy() {
        ClientBuilderSpyHelper spyHelper = new ClientBuilderSpyHelper(this.clientBuilder);
        List<HttpPipelinePolicy> additionalPolicies = new ArrayList<>();
        additionalPolicies.add(mock(HttpPipelinePolicy.class));

        // Build client with required settings and mock policies
        PhoneNumbersAsyncClient phoneNumberAsyncClient =
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
        PhoneNumbersAsyncClient phoneNumberAsyncClient =
            this.setupBuilderWithPolicies(this.clientBuilder, additionalPolicies)
                .buildAsyncClient();

        // Validate client created with expected settings
        assertNotNull(phoneNumberAsyncClient);
        validateAdditionalPolicies(spyHelper, additionalPolicies);
    }

    @Test()
    public void buildAsyncClientNoEndpointThrows() {
        assertThrows(NullPointerException.class, () -> {
            this.clientBuilder.buildClient();
        });
    }


    @Test()
    public void buildAsyncClientNoPipelineNoCredentialsThrows() {
        assertThrows(NullPointerException.class, () -> {
            this.clientBuilder.endpoint(ENDPOINT).httpClient(this.httpClient).buildClient();
        });
    }

    @Test()
    public void setEndpointNullThrows() {
        assertThrows(NullPointerException.class, () -> {
            this.clientBuilder.endpoint(null);
        });
    }

    @Test()
    public void addPolicyNullThrows() {
        assertThrows(NullPointerException.class, () -> {
            this.clientBuilder.addPolicy(null);
        });
    }

    private PhoneNumbersClientBuilder setupBuilderWithHttpClientWithCredential(PhoneNumbersClientBuilder clientBuilder) {
        return clientBuilder
            .endpoint(ENDPOINT)
            .httpClient(this.httpClient)
            .credential(new AzureKeyCredential(ACCESSKEY));
    }

    private PhoneNumbersClientBuilder setupBuilderWithPolicies(
        PhoneNumbersClientBuilder clientBuilder, List<HttpPipelinePolicy> policies) {
        clientBuilder = this.setupBuilderWithHttpClientWithCredential(clientBuilder);
        for (HttpPipelinePolicy policy : policies) {
            clientBuilder.addPolicy(policy);
        }

        return clientBuilder;
    }

    private PhoneNumbersClientBuilder setupBuilderCustomPipeline(HttpPipeline pipeline) {
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
        assertEquals(6, phoneNumberManagementClient.getHttpPipeline().getPolicyCount());
        assertEquals(spyHelper.userAgentPolicyRef.get(), phoneNumberManagementClient.getHttpPipeline().getPolicy(0));
        assertEquals(spyHelper.requestIdPolicyRef.get(), phoneNumberManagementClient.getHttpPipeline().getPolicy(1));
        assertEquals(spyHelper.authenticationPolicyRef.get(), phoneNumberManagementClient.getHttpPipeline().getPolicy(3));
        assertEquals(spyHelper.cookiePolicyRef.get(), phoneNumberManagementClient.getHttpPipeline().getPolicy(4));
        assertEquals(spyHelper.httpLoggingPolicyRef.get(), phoneNumberManagementClient.getHttpPipeline().getPolicy(5));

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
        int expectedPolicyCount = 6 + policies.size();
        int lastPolicyIndex = expectedPolicyCount - 1;
        int customPolicyIndex = 5;

        assertEquals(expectedPolicyCount, phoneNumberManagementClient.getHttpPipeline().getPolicyCount());
        assertEquals(spyHelper.userAgentPolicyRef.get(), phoneNumberManagementClient.getHttpPipeline().getPolicy(0));
        assertEquals(spyHelper.requestIdPolicyRef.get(), phoneNumberManagementClient.getHttpPipeline().getPolicy(1));
        assertEquals(spyHelper.authenticationPolicyRef.get(), phoneNumberManagementClient.getHttpPipeline().getPolicy(3));
        assertEquals(spyHelper.cookiePolicyRef.get(), phoneNumberManagementClient.getHttpPipeline().getPolicy(4));
        assertEquals(spyHelper.httpLoggingPolicyRef.get(), phoneNumberManagementClient.getHttpPipeline().getPolicy(lastPolicyIndex));

        for (HttpPipelinePolicy policy : policies) {
            assertEquals(policy, phoneNumberManagementClient.getHttpPipeline().getPolicy(customPolicyIndex));
            customPolicyIndex++;
        }
    }

    private class ClientBuilderSpyHelper {
        final PhoneNumbersClientBuilder clientBuilder;

        final AtomicReference<HmacAuthenticationPolicy> authenticationPolicyRef = new AtomicReference<>();
        final AtomicReference<UserAgentPolicy> userAgentPolicyRef = new AtomicReference<>();
        final AtomicReference<RequestIdPolicy> requestIdPolicyRef = new AtomicReference<>();
        final AtomicReference<RetryPolicy> retryPolicyRef = new AtomicReference<>();
        final AtomicReference<CookiePolicy> cookiePolicyRef = new AtomicReference<>();
        final AtomicReference<HttpLoggingPolicy> httpLoggingPolicyRef = new AtomicReference<>();
        final AtomicReference<HttpLogOptions> defaultHttpLogOptionsRef = new AtomicReference<>();
        final ArgumentCaptor<PhoneNumberAdminClientImpl> phoneNumberAdminClientArg =
            ArgumentCaptor.forClass(PhoneNumberAdminClientImpl.class);
        final ArgumentCaptor<String> uaPolicyAppIdArg = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> uaPolicySdkNameArg = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> uaPolicySdkVersionArg = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Configuration> uaPolicyConfigArg = ArgumentCaptor.forClass(Configuration.class);
        final ArgumentCaptor<HttpLogOptions> httpLogOptionsArg = ArgumentCaptor.forClass(HttpLogOptions.class);

        ClientBuilderSpyHelper(PhoneNumbersClientBuilder clientBuilder) {
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

        void capturePhoneNumberAdminClientImpl() {
            verify(this.clientBuilder, times(1))
                .createPhoneNumberAsyncClient(this.phoneNumberAdminClientArg.capture());
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
