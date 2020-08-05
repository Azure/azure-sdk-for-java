// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.core;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.HostPolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.TimeoutPolicy;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.LogLevel;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class TestBase {
    private PrintStream out;
    private String baseUri;
    protected SdkContext sdkContext = new SdkContext();
    private AuthFile authFile;
    private AzureProfile profile;

    public String generateRandomResourceName(String prefix, int maxLen) {
        return sdkContext.randomResourceName(prefix, maxLen);
    }

    public static String password() {
        // do not record
        String password = new SdkContext().randomResourceName("Pa5$", 12);
        System.out.printf("Password: %s%n", password);
        return password;
    }

    protected enum RunCondition {
        MOCK_ONLY,
        LIVE_ONLY,
        BOTH
    }

    public enum TestMode {
        PLAYBACK,
        RECORD,
        NONE
    }

    protected static final String ZERO_SUBSCRIPTION = "00000000-0000-0000-0000-000000000000";
    protected static final String ZERO_TENANT = "00000000-0000-0000-0000-000000000000";
    private static final String PLAYBACK_URI_BASE = "http://localhost:";
    protected static String playbackUri = null;

    private final RunCondition runCondition;

    protected TestBase() {
        this(RunCondition.BOTH);
    }

    protected TestBase(RunCondition runCondition) {
        this.runCondition = runCondition;
    }

    private String shouldCancelTest(boolean isPlaybackMode) {
        // Determine whether to run the test based on the condition the test has been configured with
        switch (this.runCondition) {
            case MOCK_ONLY:
                return (!isPlaybackMode) ? "Test configured to run only as mocked, not live." : null;
            case LIVE_ONLY:
                return (isPlaybackMode) ? "Test configured to run only as live, not mocked." : null;
            default:
                return null;
        }
    }

    private static TestMode testMode = null;
    protected TestInfo testInfo;

    private static void initTestMode() throws IOException {
        Configuration.getGlobalConfiguration().put(Configuration.PROPERTY_AZURE_LOG_LEVEL, String.valueOf(LogLevel.INFORMATIONAL));
        String azureTestMode = System.getenv("AZURE_TEST_MODE");
        if (azureTestMode != null) {
            if (azureTestMode.equalsIgnoreCase("Record")) {
                testMode = TestMode.RECORD;
            } else if (azureTestMode.equalsIgnoreCase("Playback")) {
                testMode = TestMode.PLAYBACK;
            } else if (azureTestMode.equalsIgnoreCase("None")) {
                testMode = TestMode.NONE;
            } else {
                throw new IOException("Unknown AZURE_TEST_MODE: " + azureTestMode);
            }
        } else {
            //System.out.print("Environment variable 'AZURE_TEST_MODE' has not been set yet. Using 'Playback' mode.");
            testMode = TestMode.PLAYBACK;
        }
    }

    private static void initPlaybackUri() throws IOException {
        playbackUri = PLAYBACK_URI_BASE + "1234";
    }

    public static boolean isPlaybackMode() {
        if (testMode == null) {
            try {
                initTestMode();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Can't init test mode.");
            }
        }
        return testMode == TestMode.PLAYBACK;
    }

    public static boolean isRecordMode() {
        return !isPlaybackMode();
    }

    protected InterceptorManager interceptorManager = null;

    private static void printThreadInfo(String what) {
        long id = Thread.currentThread().getId();
        String name = Thread.currentThread().getName();
        System.out.println(String.format("\n***\n*** [%s:%s] - %s\n***\n", name, id, what));
    }

    @BeforeAll
    public static void beforeClass() throws IOException {
        printThreadInfo("beforeClass");
        initTestMode();
        initPlaybackUri();
    }

    @BeforeEach
    public void beforeTest(TestInfo testInfo) throws IOException {
        this.testInfo = testInfo;
        String testMothodName = testInfo.getTestMethod().get().getName();
        printThreadInfo(String.format("%s: %s", "beforeTest", testMothodName));
        final String skipMessage = shouldCancelTest(isPlaybackMode());
        Assumptions.assumeTrue(skipMessage == null, skipMessage);

        String showOutput = Configuration.getGlobalConfiguration().get("showOutput");
        if (!isPlaybackMode() || (showOutput != null && showOutput.equalsIgnoreCase("true"))) {
            System.out.println("------------------------------------------------------------ Show Output");
        } else {
            System.out.println("------------------------------------------------------------ Skip Output");
            System.setOut(new PrintStream(new OutputStream() {
                @Override
                public void write(int b) throws IOException {

                }
            }));

            System.setErr(new PrintStream(new OutputStream() {
                @Override
                public void write(int b) throws IOException {

                }
            }));
        }

        interceptorManager = InterceptorManager.create(testMothodName, testMode);
        sdkContext.setResourceNamerFactory(new TestResourceNamerFactory(interceptorManager));

        TokenCredential credential;
        HttpPipeline httpPipeline;

        if (isPlaybackMode()) {
            profile = new AzureProfile(
                ZERO_TENANT, ZERO_SUBSCRIPTION,
                new AzureEnvironment(
                    new HashMap<String, String>() {
                        {
                            put("managementEndpointUrl", playbackUri);
                            put("resourceManagerEndpointUrl", playbackUri);
                            put("sqlManagementEndpointUrl", playbackUri);
                            put("galleryEndpointUrl", playbackUri);
                            put("activeDirectoryEndpointUrl", playbackUri);
                            put("activeDirectoryResourceId", playbackUri);
                            put("activeDirectoryGraphResourceId", playbackUri);
                        }}));

            List<HttpPipelinePolicy> policies = new ArrayList<>();
            policies.add(interceptorManager.initInterceptor());
            policies.add(new HostPolicy(playbackUri + "/"));
            policies.add(new ResourceGroupTaggingPolicy());
            policies.add(new CookiePolicy());
            httpPipeline = HttpPipelineProvider.buildHttpPipeline(
                null, profile, null, new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS),
                null, new RetryPolicy("Retry-After", ChronoUnit.SECONDS), policies, null);

            interceptorManager.addTextReplacementRule(PLAYBACK_URI_BASE + "1234", playbackUri);
            System.out.println(playbackUri);
        } else {
            if (System.getenv("AZURE_AUTH_LOCATION") != null) { // Record mode
                final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));
                AuthFile authFile = buildAuthFile(credFile);
                credential = authFile.credential();
                profile = new AzureProfile(authFile.tenantId(), authFile.subscriptionId(), authFile.environment());
            } else {
                String clientId = System.getenv("AZURE_CLIENT_ID");
                String tenantId = System.getenv("AZURE_TENANT_ID");
                String clientSecret = System.getenv("AZURE_CLIENT_SECRET");
                String subscriptionId = System.getenv("AZURE_SUBSCRIPTION_ID");
                if (clientId == null || tenantId == null || clientSecret == null || subscriptionId == null) {
                    throw new IllegalArgumentException("When running tests in record mode either 'AZURE_AUTH_LOCATION' or 'AZURE_CLIENT_ID, AZURE_TENANT_ID, AZURE_CLIENT_SECRET and AZURE_SUBSCRIPTION_ID' needs to be set");
                }

                credential = new ClientSecretCredentialBuilder()
                    .tenantId(tenantId)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .authorityHost(AzureEnvironment.AZURE.getActiveDirectoryEndpoint())
                    .build();
                profile = new AzureProfile(tenantId, subscriptionId, AzureEnvironment.AZURE);

            }

            List<HttpPipelinePolicy> policies = new ArrayList<>();
            policies.add(new ResourceGroupTaggingPolicy());
            policies.add(new TimeoutPolicy(Duration.ofMinutes(1)));
            policies.add(new CookiePolicy());
            if (!interceptorManager.isNoneMode()) {
                policies.add(interceptorManager.initInterceptor());
            }
            httpPipeline = HttpPipelineProvider.buildHttpPipeline(
                credential, profile, null, new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS),
                null, new RetryPolicy("Retry-After", ChronoUnit.SECONDS), policies, generateHttpClientWithProxy(null));

            interceptorManager.addTextReplacementRule(profile.subscriptionId(), ZERO_SUBSCRIPTION);
            interceptorManager.addTextReplacementRule(profile.tenantId(), ZERO_TENANT);
            interceptorManager.addTextReplacementRule(baseUri(), playbackUri + "/");
            interceptorManager.addTextReplacementRule("https://graph.windows.net/", playbackUri + "/");
        }
        initializeClients(httpPipeline, profile);
    }

    @AfterEach
    public void afterTest() throws IOException {
        if (shouldCancelTest(isPlaybackMode()) != null) {
            return;
        }
        cleanUpResources();
        interceptorManager.finalizeInterceptor();
    }

    protected HttpClient generateHttpClientWithProxy(ProxyOptions proxyOptions) {
        NettyAsyncHttpClientBuilder clientBuilder = new NettyAsyncHttpClientBuilder();
        if (proxyOptions != null) {
            clientBuilder.proxy(proxyOptions);
        } else {
            try {
                System.setProperty("java.net.useSystemProxies", "true");
                List<Proxy> proxies = ProxySelector.getDefault().select(new URI(AzureEnvironment.AZURE.getResourceManagerEndpoint()));
                if (!proxies.isEmpty()) {
                    for (Proxy proxy : proxies) {
                        if (proxy.address() instanceof InetSocketAddress) {
                            String host = ((InetSocketAddress) proxy.address()).getHostName();
                            int port = ((InetSocketAddress) proxy.address()).getPort();
                            switch (proxy.type()) {
                                case HTTP:
                                    return clientBuilder.proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress(host, port))).build();
                                case SOCKS:
                                    return clientBuilder.proxy(new ProxyOptions(ProxyOptions.Type.SOCKS5, new InetSocketAddress(host, port))).build();
                                default:
                            }
                        }
                    }
                }
                String host = null;
                int port = 0;
                if (System.getProperty("https.proxyHost") != null && System.getProperty("https.proxyPort") != null) {
                    host = System.getProperty("https.proxyHost");
                    port = Integer.parseInt(System.getProperty("https.proxyPort"));
                } else if (System.getProperty("http.proxyHost") != null && System.getProperty("http.proxyPort") != null) {
                    host = System.getProperty("http.proxyHost");
                    port = Integer.parseInt(System.getProperty("http.proxyPort"));
                }
                if (host != null) {
                    clientBuilder.proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress(host, port)));
                }
            } catch (URISyntaxException e) { }
        }
        return clientBuilder.build();
    }

    protected String baseUri() {
        if (this.baseUri != null) {
            return this.baseUri;
        } else {
            return AzureEnvironment.AZURE.url(AzureEnvironment.Endpoint.RESOURCE_MANAGER);
        }
    }

    protected AuthFile buildAuthFile(File credFile) throws IOException {
        this.authFile = AuthFile.parse(credFile);
        return this.authFile;
    }

    protected TokenCredential credentialFromFile() {
        return this.authFile.credential();
    }

    protected String clientIdFromFile() {
        return authFile.clientId();
    }

    protected AzureProfile profile() {
        return this.profile;
    }

    protected abstract void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) throws IOException;

    protected abstract void cleanUpResources();
}
