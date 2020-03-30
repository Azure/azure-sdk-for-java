/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources.core;

import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.HostPolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.TimeoutPolicy;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.serializer.AzureJacksonAdapter;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.LogLevel;
import com.azure.management.ApplicationTokenCredential;
import com.azure.management.RestClient;
import com.azure.management.RestClientBuilder;
import com.azure.management.resources.fluentcore.utils.SdkContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.List;
import java.util.Properties;

public abstract class TestBase {
    private PrintStream out;
    private String baseUri;
    protected SdkContext sdkContext = new SdkContext();

    public String generateRandomResourceName(String prefix, int maxLen) {
        return sdkContext.randomResourceName(prefix, maxLen);
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

    protected final static String ZERO_SUBSCRIPTION = "00000000-0000-0000-0000-000000000000";
    protected final static String ZERO_TENANT = "00000000-0000-0000-0000-000000000000";
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
        if (isPlaybackMode()) {
            Properties mavenProps = new Properties();
            InputStream in = TestBase.class.getResourceAsStream("/maven.properties");
            if (in == null) {
                throw new IOException("The file \"maven.properties\" has not been generated yet. Please execute \"mvn compile\" to generate the file.");
            }
            mavenProps.load(in);
            String port = mavenProps.getProperty("playbackServerPort");
            playbackUri = PLAYBACK_URI_BASE + port;
        } else {
            playbackUri = PLAYBACK_URI_BASE + "1234";
        }
    }

    public static boolean isPlaybackMode() {
        if (testMode == null) try {
            initTestMode();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Can't init test mode.");
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

        interceptorManager = InterceptorManager.create(testMothodName, testMode);
        sdkContext.setResourceNamerFactory(new TestResourceNamerFactory(interceptorManager));

        ApplicationTokenCredential credentials;
        RestClient restClient;
        String defaultSubscription;

        if (isPlaybackMode()) {
            credentials = new AzureTestCredential(playbackUri, ZERO_TENANT, true);
            restClient = buildRestClient(new RestClientBuilder()
                    .withBaseUrl(playbackUri + "/")
                    .withSerializerAdapter(new AzureJacksonAdapter())
                    .withHttpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.NONE))
                    .withPolicy(interceptorManager.initInterceptor())
                    .withPolicy(new HostPolicy(playbackUri + "/"))
                    .withPolicy(new ResourceGroupTaggingPolicy())
                    .withPolicy(new CookiePolicy()), true);

            defaultSubscription = ZERO_SUBSCRIPTION;
            interceptorManager.addTextReplacementRule(PLAYBACK_URI_BASE + "1234", playbackUri);
            System.out.println(playbackUri);
            out = System.out;
            System.setOut(new PrintStream(new OutputStream() {
                public void write(int b) {
                    //DO NOTHING
                }
            }));
        } else {
            if (System.getenv("AZURE_AUTH_LOCATION") != null) { // Record mode
                final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));
                credentials = ApplicationTokenCredential.fromFile(credFile);
            } else {
                String clientId = System.getenv("AZURE_CLIENT_ID");
                String tenantId = System.getenv("AZURE_TENANT_ID");
                String clientSecret = System.getenv("AZURE_CLIENT_SECRET");
                String subscriptionId = System.getenv("AZURE_SUBSCRIPTION_ID");
                if (clientId == null || tenantId == null || clientSecret == null || subscriptionId == null) {
                    throw new IllegalArgumentException("When running tests in record mode either 'AZURE_AUTH_LOCATION' or 'AZURE_CLIENT_ID, AZURE_TENANT_ID, AZURE_CLIENT_SECRET and AZURE_SUBSCRIPTION_ID' needs to be set");
                }

                credentials = new ApplicationTokenCredential(clientId, tenantId, clientSecret, AzureEnvironment.AZURE);
                credentials.defaultSubscriptionId(subscriptionId);
            }
            RestClientBuilder builder = new RestClientBuilder()
                    .withBaseUrl(this.baseUri())
                    .withSerializerAdapter(new AzureJacksonAdapter())
                    .withCredential(credentials)
                    .withHttpClient(generateHttpClientWithProxy(null))
                    .withHttpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                    .withPolicy(new ResourceGroupTaggingPolicy())
                    .withPolicy(new TimeoutPolicy(Duration.ofMinutes(1)))
                    .withPolicy(new CookiePolicy());
            if (!interceptorManager.isNoneMode()) {
                builder.withPolicy(interceptorManager.initInterceptor());
            }

            restClient = buildRestClient(builder, false);
            defaultSubscription = credentials.getDefaultSubscriptionId();
            interceptorManager.addTextReplacementRule(defaultSubscription, ZERO_SUBSCRIPTION);
            interceptorManager.addTextReplacementRule(credentials.getDomain(), ZERO_TENANT);
            interceptorManager.addTextReplacementRule(baseUri(), playbackUri + "/");
            interceptorManager.addTextReplacementRule("https://graph.windows.net/", playbackUri + "/");
        }
        initializeClients(restClient, defaultSubscription, credentials.getDomain());
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
            } catch (URISyntaxException e) {}
        }
        return clientBuilder.build();
    }

    protected void addTextReplacementRule(String from, String to) {
        interceptorManager.addTextReplacementRule(from, to);
    }

    protected void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    protected String baseUri() {
        if (this.baseUri != null) {
            return this.baseUri;
        } else {
            return AzureEnvironment.AZURE.url(AzureEnvironment.Endpoint.RESOURCE_MANAGER);
        }
    }

    protected synchronized RestClient buildRestClient(RestClientBuilder builder, boolean isMocked) {
        return builder.buildClient();
    }

    protected abstract void initializeClients(RestClient restClient, String defaultSubscription, String domain) throws IOException;

    protected abstract void cleanUpResources();
}
