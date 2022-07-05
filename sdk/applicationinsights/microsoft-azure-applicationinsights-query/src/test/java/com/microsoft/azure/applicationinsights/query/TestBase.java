// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.applicationinsights.query;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.AzureResponseBuilder;
import com.microsoft.azure.arm.utils.ResourceManagerThrottlingInterceptor;
import com.microsoft.azure.arm.utils.ResourceNamer;
import com.microsoft.azure.arm.utils.ResourceNamerFactory;
import com.microsoft.azure.arm.utils.SdkContext;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import com.microsoft.rest.LogLevel;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.interceptors.LoggingInterceptor;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * From:
 * https://github.com/Azure/autorest-clientruntime-for-java/blob/master/azure-arm-client-runtime/src/test/java/com/microsoft/azure/arm/core/TestBase.java
 */
public abstract class TestBase {
    private PrintStream out;
    private String baseUri;

    public static String generateRandomResourceName(String prefix, int maxLen) {
        return SdkContext.randomResourceName(prefix, maxLen);
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

    private static void initTestMode() throws IOException {
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
        return  !isPlaybackMode();
    }

    @Rule
    public TestName testName = new TestName();

    protected InterceptorManager interceptorManager = null;

    private static void printThreadInfo(String what) {
        long id = Thread.currentThread().getId();
        String name = Thread.currentThread().getName();
        System.out.println(String.format("\n***\n*** [%s:%s] - %s\n***\n", name, id, what));
    }

    @BeforeClass
    public static void beforeClass() throws IOException {
        printThreadInfo("beforeClass");
        initTestMode();
        initPlaybackUri();
    }

    @Before
    public void beforeTest() throws IOException {
        printThreadInfo(String.format("%s: %s", "beforeTest", testName.getMethodName()));
        final String skipMessage = shouldCancelTest(isPlaybackMode());
        Assume.assumeTrue(skipMessage, skipMessage == null);

        // Create a MockWebServer. These are lean enough that you can create a new
        // instance for every unit test.
        MockWebServer server = new MockWebServer();

        // Schedule some responses.
        server.enqueue(new MockResponse().setBody("hello, world!"));

        // Start the server.
        server.start();

        // Ask the server for its URL. You'll need this to make HTTP requests.
        HttpUrl baseUrl = server.url("/");

        interceptorManager = InterceptorManager.create(testName.getMethodName(), testMode);

        ApplicationTokenCredentials credentials;
        RestClient restClient;
        String defaultSubscription;

        if (isPlaybackMode()) {
            credentials = new AzureTestCredentials(playbackUri, ZERO_TENANT, true);
            restClient = buildRestClient(new RestClient.Builder()
                    .withBaseUrl(baseUrl.toString())
                    .withSerializerAdapter(new AzureJacksonAdapter())
                    .withResponseBuilderFactory(new AzureResponseBuilder.Factory())
                    .withCredentials(credentials)
                    .withLogLevel(LogLevel.NONE)
                    .withNetworkInterceptor(new LoggingInterceptor(LogLevel.BODY_AND_HEADERS))
                    .withNetworkInterceptor(interceptorManager.initInterceptor())
                    .withInterceptor(new ResourceManagerThrottlingInterceptor())
                ,true);

            defaultSubscription = ZERO_SUBSCRIPTION;
            interceptorManager.addTextReplacementRule(PLAYBACK_URI_BASE + "1234", playbackUri);
            System.out.println(playbackUri);
            out = System.out;
            System.setOut(new PrintStream(new OutputStream() {
                public void write(int b) {
                    //DO NOTHING
                }
            }));
        }
        else {
            if (System.getenv("AZURE_AUTH_LOCATION") != null) { // Record mode
                final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));
                credentials = ApplicationTokenCredentials.fromFile(credFile);
            } else {
                String clientId = System.getenv("AZURE_CLIENT_ID");
                String tenantId = System.getenv("AZURE_TENANT_ID");
                String clientSecret = System.getenv("AZURE_CLIENT_SECRET");
                String subscriptionId = System.getenv("AZURE_SUBSCRIPTION_ID");
                if (clientId == null || tenantId == null || clientSecret == null || subscriptionId == null) {
                    throw new IllegalArgumentException("When running tests in record mode either 'AZURE_AUTH_LOCATION' or 'AZURE_CLIENT_ID, AZURE_TENANT_ID, AZURE_CLIENT_SECRET and AZURE_SUBSCRIPTION_ID' needs to be set");
                }

                credentials = new ApplicationTokenCredentials(clientId, tenantId, clientSecret, AzureEnvironment.AZURE);
                credentials.withDefaultSubscriptionId(subscriptionId);
            }
            RestClient.Builder builder = new RestClient.Builder()
                .withBaseUrl(this.baseUri())
                .withSerializerAdapter(new AzureJacksonAdapter())
                .withResponseBuilderFactory(new AzureResponseBuilder.Factory())
                .withCredentials(credentials)
                .withLogLevel(LogLevel.NONE)
                .withReadTimeout(3, TimeUnit.MINUTES)
                .withNetworkInterceptor(new LoggingInterceptor(LogLevel.BODY_AND_HEADERS));
            if (!interceptorManager.isNoneMode()) {
                builder.withNetworkInterceptor(interceptorManager.initInterceptor());
            }
            Proxy proxy = proxy();
            if (proxy != null) {
                builder.withProxy(proxy);
            }
            restClient = buildRestClient(builder.withInterceptor(new ResourceManagerThrottlingInterceptor()),false);
            defaultSubscription = credentials.defaultSubscriptionId();
            interceptorManager.addTextReplacementRule(defaultSubscription, ZERO_SUBSCRIPTION);
            interceptorManager.addTextReplacementRule(credentials.domain(), ZERO_TENANT);
            interceptorManager.addTextReplacementRule(baseUri(), playbackUri + "/");
            interceptorManager.addTextReplacementRule("https://graph.windows.net/", playbackUri + "/");
        }
        initializeClients(restClient, defaultSubscription, credentials.domain());
    }

    @After
    public void afterTest() throws IOException {
        if(shouldCancelTest(isPlaybackMode()) != null) {
            return;
        }
        cleanUpResources();
        interceptorManager.finalizeInterceptor();
    }

    protected void addTextReplacementRule(String from, String to ) {
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

    protected RestClient buildRestClient(RestClient.Builder builder, boolean isMocked) {
        return builder.build();
    }

    protected abstract void initializeClients(RestClient restClient, String defaultSubscription, String domain) throws IOException;
    protected abstract void cleanUpResources();

    protected Proxy proxy() {
        return null;
    }

    private static class AzureTestCredentials extends ApplicationTokenCredentials {
        boolean isPlaybackMode;

        private AzureTestCredentials(final String mockUrl, String mockTenant, boolean isPlaybackMode) {
            super("", mockTenant, "", new AzureEnvironment(new HashMap<String, String>() {{
                put("managementEndpointUrl", mockUrl);
                put("resourceManagerEndpointUrl", mockUrl);
                put("sqlManagementEndpointUrl", mockUrl);
                put("galleryEndpointUrl", mockUrl);
                put("activeDirectoryEndpointUrl", mockUrl);
                put("activeDirectoryResourceId", mockUrl);
                put("activeDirectoryGraphResourceId", mockUrl);
            }}));
            this.isPlaybackMode = isPlaybackMode;
        }

        @Override
        public String getToken(String resource) throws IOException {
            if (!isPlaybackMode) {
                super.getToken(resource);
            }
            return "https://asdd.com";
        }
    }
}
