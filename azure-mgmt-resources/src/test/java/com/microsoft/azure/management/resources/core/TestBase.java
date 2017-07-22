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
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import com.microsoft.rest.LogLevel;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.interceptors.LoggingInterceptor;
import org.junit.*;
import org.junit.rules.TestName;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

public abstract class TestBase /*extends MockIntegrationTestBase*/ {
    private PrintStream out;

    public static String generateRandomResourceName(String prefix, int maxLen) {
        return SdkContext.randomResourceName(prefix, maxLen);
    }

    protected enum RunCondition {
        MOCK_ONLY,
        LIVE_ONLY,
        BOTH
    }

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

    private static InterceptorManager.TestMode testMode = null;

    private static void initTestMode() throws IOException{
        String azureTestMode = System.getenv("AZURE_TEST_MODE");
        if (azureTestMode != null) {
            if (azureTestMode.equalsIgnoreCase("Record")) {
                testMode = InterceptorManager.TestMode.RECORD;
            } else if (azureTestMode.equalsIgnoreCase("Playback")) {
                testMode = InterceptorManager.TestMode.PLAYBACK;
            } else {
                throw new IOException("Unknown AZURE_TEST_MODE: " + azureTestMode);
            }
        } else {
            System.out.print("Environment variable 'AZURE_TEST_MODE' has not been set yet. Use 'Playback' mode.");
            testMode = InterceptorManager.TestMode.PLAYBACK;
        }
    }


    protected final static String ZERO_SUBSCRIPTION = "00000000-0000-0000-0000-000000000000";
    protected final static String ZERO_TENANT = "00000000-0000-0000-0000-000000000000";
    private static final String FAKE_URI_BASE = "http://localhost:";
    protected static String FAKE_URI;

    @Rule
    public TestName testName = new TestName();

    protected InterceptorManager interceptorManager = null;
    private static DummyServer dummyServer = null;

    public static boolean isPlaybackMode() {
        if (testMode == null) try {
            initTestMode();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("can't read env var");
        }
        return testMode == InterceptorManager.TestMode.PLAYBACK;
    }

    public static boolean isRecordMode() {
        return  !isPlaybackMode();
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        initTestMode();
        if (isPlaybackMode()) {
            dummyServer = DummyServer.createOnAvailablePort();
//            FAKE_URI = FAKE_URI_BASE + "8081";
            FAKE_URI = FAKE_URI_BASE + dummyServer.getPort();
            dummyServer.start();

        } else {
            FAKE_URI = FAKE_URI_BASE +"1234";

        }
    }

    @Before
    public void beforeTest() throws IOException {
        final String skipMessage = shouldCancelTest(isPlaybackMode());
        Assume.assumeTrue(skipMessage, skipMessage == null);

        interceptorManager = InterceptorManager.create(testName.getMethodName(), testMode);

        ApplicationTokenCredentials credentials;
        RestClient restClient;
        String defaultSubscription;

        if (isPlaybackMode()) {
            credentials = new AzureTestCredentials(this.FAKE_URI, ZERO_TENANT, true);
            restClient = buildRestClient(new RestClient.Builder()
                    .withBaseUrl(this.FAKE_URI + "/")
                    .withSerializerAdapter(new AzureJacksonAdapter())
                    .withResponseBuilderFactory(new AzureResponseBuilder.Factory())
                    .withCredentials(credentials)
                    .withLogLevel(LogLevel.NONE)
                    .withNetworkInterceptor(new LoggingInterceptor(LogLevel.BODY_AND_HEADERS))
                    .withNetworkInterceptor(interceptorManager.initInterceptor())
                    ,true);

            defaultSubscription = ZERO_SUBSCRIPTION;
            System.out.println(this.FAKE_URI);
            out = System.out;
            System.setOut(new PrintStream(new OutputStream() {
                public void write(int b) {
                    //DO NOTHING
                }
            }));
        }
        else {
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
                    ,false);

            defaultSubscription = credentials.defaultSubscriptionId();
            interceptorManager.addTextReplacementRule(defaultSubscription, ZERO_SUBSCRIPTION);
            interceptorManager.addTextReplacementRule(credentials.domain(), ZERO_TENANT);
            interceptorManager.addTextReplacementRule("https://management.azure.com/", FAKE_URI + "/");
            interceptorManager.addTextReplacementRule("https://graph.windows.net/", FAKE_URI + "/");
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

//        if (IS_MOCKED) {
//            if (testRecord.networkCallRecords.size() > 0) {
//                System.out.println("Remaining records " + testRecord.networkCallRecords.size() + " :");
//                for (int index = 0; index < testRecord.networkCallRecords.size(); index++) {
//                    NetworkCallRecord record = testRecord.networkCallRecords.get(index);
//                    System.out.println(record.Method + " - " + record.Uri);
//                }
//                Assert.assertEquals(0, testRecord.networkCallRecords.size());
//            }
//            System.setOut(out);
//        }
//        resetTest(name.getMethodName());
    }

    @AfterClass
    public static void afterClass() {
        if (isPlaybackMode()) {
            dummyServer.stop();
        }
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
