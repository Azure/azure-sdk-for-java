/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.core;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.AzureResponseBuilder;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.resources.fluentcore.utils.ProviderRegistrationInterceptor;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceManagerThrottlingInterceptor;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import com.microsoft.rest.LogLevel;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.interceptors.LoggingInterceptor;
import org.junit.*;
import org.junit.rules.TestName;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public abstract class TestBase {
    private PrintStream out;

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
        RECORD
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

        interceptorManager = InterceptorManager.create(testName.getMethodName(), testMode);

        ApplicationTokenCredentials credentials;
        RestClient restClient;
        String defaultSubscription;

        if (isPlaybackMode()) {
            credentials = new AzureTestCredentials(playbackUri, ZERO_TENANT, true);
            restClient = buildRestClient(new RestClient.Builder()
                    .withBaseUrl(playbackUri + "/")
                    .withSerializerAdapter(new AzureJacksonAdapter())
                    .withResponseBuilderFactory(new AzureResponseBuilder.Factory())
                    .withCredentials(credentials)
                    .withLogLevel(LogLevel.NONE)
                    .withNetworkInterceptor(new LoggingInterceptor(LogLevel.BODY_AND_HEADERS))
                    .withNetworkInterceptor(interceptorManager.initInterceptor())
                    .withInterceptor(new ResourceManagerThrottlingInterceptor())
                    ,true);

            defaultSubscription = ZERO_SUBSCRIPTION;
            System.out.println(playbackUri);
            out = System.out;
            System.setOut(new PrintStream(new OutputStream() {
                public void write(int b) {
                    //DO NOTHING
                }
            }));
        }
        else { // Record mode
            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));
            credentials = ApplicationTokenCredentials.fromFile(credFile);
            restClient = buildRestClient(new RestClient.Builder()
                    .withBaseUrl(AzureEnvironment.AZURE, AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                    .withSerializerAdapter(new AzureJacksonAdapter())
                    .withResponseBuilderFactory(new AzureResponseBuilder.Factory())
                    .withInterceptor(new ProviderRegistrationInterceptor(credentials))
                    .withCredentials(credentials)
                    .withLogLevel(LogLevel.NONE)
                    .withReadTimeout(3, TimeUnit.MINUTES)
                    .withNetworkInterceptor(new LoggingInterceptor(LogLevel.BODY_AND_HEADERS))
                    .withNetworkInterceptor(interceptorManager.initInterceptor())
                    .withInterceptor(new ResourceManagerThrottlingInterceptor())
                    ,false);

            defaultSubscription = credentials.defaultSubscriptionId();
            interceptorManager.addTextReplacementRule(defaultSubscription, ZERO_SUBSCRIPTION);
            interceptorManager.addTextReplacementRule(credentials.domain(), ZERO_TENANT);
            interceptorManager.addTextReplacementRule("https://management.azure.com/", playbackUri + "/");
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

    protected RestClient buildRestClient(RestClient.Builder builder, boolean isMocked) {
        return builder.build();
    }

    protected abstract void initializeClients(RestClient restClient, String defaultSubscription, String domain) throws IOException;
    protected abstract void cleanUpResources();
}
