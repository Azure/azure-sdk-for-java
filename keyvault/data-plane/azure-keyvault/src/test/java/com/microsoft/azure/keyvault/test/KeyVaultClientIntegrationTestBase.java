// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import com.microsoft.azure.AzureResponseBuilder;
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.authentication.KeyVaultCredentials;
import com.microsoft.azure.keyvault.models.Attributes;
import com.microsoft.azure.keyvault.models.DeletedCertificateBundle;
import com.microsoft.azure.keyvault.models.DeletedKeyBundle;
import com.microsoft.azure.keyvault.models.DeletedSecretBundle;
import com.microsoft.azure.management.resources.core.InterceptorManager;
import com.microsoft.azure.management.resources.core.TestBase;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceManagerThrottlingInterceptor;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import com.microsoft.rest.LogLevel;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import com.microsoft.rest.interceptors.LoggingInterceptor;

public class KeyVaultClientIntegrationTestBase {

    private static TestBase.TestMode testMode = null;
    private PrintStream out;

    protected enum RunCondition {
        MOCK_ONLY, LIVE_ONLY, BOTH
    }

    protected static KeyVaultClient keyVaultClient;
    protected static KeyVaultClient alternativeKeyVaultClient;

    protected static final String ZERO_SUBSCRIPTION = "00000000-0000-0000-0000-000000000000";
    protected static final String ZERO_TENANT = "00000000-0000-0000-0000-000000000000";
    private static final String PLAYBACK_URI_BASE = "http://localhost:";
    protected static String playbackUri = null;
    protected static String alternativePlaybackUri = null;

    private final RunCondition runCondition;

    protected KeyVaultClientIntegrationTestBase() {
        this(RunCondition.BOTH);
    }

    protected KeyVaultClientIntegrationTestBase(RunCondition runCondition) {
        this.runCondition = runCondition;
    }

    /**
     * Primary vault URI, used for keys and secrets tests.
     */
    public static String getVaultUri() {
        return getLiveVaultUri1();
    }

    /**
     * Secondary vault URI, used to verify ability to transparently authenticate
     * against a different resource.
     */
    public static String getSecondaryVaultUri() {
        return getLiveVaultUri2();
    }

    private static String getLiveVaultUri1() {
        return getenvOrDefault("KEYVAULT_VAULTURI", "https://javasdktestvault.vault.azure.net");
    }

    private static String getLiveVaultUri2() {
        return getenvOrDefault("KEYVAULT_VAULTURI_ALT", "https://javasdktestvault2.vault.azure.net");
    }

    private static String getenvOrDefault(String varName, String defValue) {
        String value = System.getenv(varName);
        return value != null ? value : defValue;
    }

    protected static void compareAttributes(Attributes expectedAttributes, Attributes actualAttribute) {
        if (expectedAttributes != null) {
            Assert.assertEquals(expectedAttributes.enabled(), actualAttribute.enabled());
            Assert.assertEquals(expectedAttributes.expires(), actualAttribute.expires());
            Assert.assertEquals(expectedAttributes.notBefore(), actualAttribute.notBefore());
        }
    }

    private static AuthenticationResult getAccessToken(String authorization, String resource) throws Exception {

        String clientId = System.getenv("ARM_CLIENTID");

        if (clientId == null) {
            throw new Exception("Please provide ARM_CLIENTID in the environment settings.");
        }

        String clientKey = System.getenv("ARM_CLIENTKEY");
        String username = System.getenv("arm.username");
        String password = System.getenv("arm.password");

        AuthenticationResult result = null;
        ExecutorService service = null;
        try {
            service = Executors.newFixedThreadPool(1);
            AuthenticationContext context = new AuthenticationContext(authorization, false, service);

            Future<AuthenticationResult> future = null;

            if (clientKey != null && password == null) {
                ClientCredential credentials = new ClientCredential(clientId, clientKey);
                future = context.acquireToken(resource, credentials, null);
            }

            if (password != null && clientKey == null) {
                future = context.acquireToken(resource, clientId, username, password, null);
            }

            if (future == null) {
                throw new Exception(
                        "Missing or ambiguous credentials - please inform exactly one of arm.clientkey or arm.password in the environment settings.");
            }

            result = future.get();
        } finally {
            service.shutdown();
        }

        if (result == null) {
            throw new RuntimeException("authentication result was null");
        }
        return result;
    }

    private static ServiceClientCredentials createTestCredentials() throws Exception {
        return new KeyVaultCredentials() {

            @Override
            public String doAuthenticate(String authorization, String resource, String scope) {
                try {

                    if (isRecordMode()) {
                        AuthenticationResult authResult = getAccessToken(authorization, resource);
                        return authResult.getAccessToken();
                    } else {
                        return "";
                    }

                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
    }

    public static String generateRandomResourceName(String prefix, int maxLen) {
        return SdkContext.randomResourceName(prefix, maxLen);
    }

    private String shouldCancelTest(boolean isPlaybackMode) {
        // Determine whether to run the test based on the condition the test has been
        // configured with
        switch (this.runCondition) {
            case MOCK_ONLY:
                return (!isPlaybackMode) ? "Test configured to run only as mocked, not live." : null;
            case LIVE_ONLY:
                return (isPlaybackMode) ? "Test configured to run only as live, not mocked." : null;
            default:
                return null;
        }
    }

    private static void initTestMode() throws IOException {
        String azureTestMode = System.getenv("AZURE_TEST_MODE");
        if (azureTestMode != null) {
            if (azureTestMode.equalsIgnoreCase("Record")) {
                testMode = TestBase.TestMode.RECORD;
            } else if (azureTestMode.equalsIgnoreCase("Playback")) {
                testMode = TestBase.TestMode.PLAYBACK;
            } else {
                throw new IOException("Unknown AZURE_TEST_MODE: " + azureTestMode);
            }
        } else {
            // System.out.print("Environment variable 'AZURE_TEST_MODE' has not been set
            // yet. Using 'Playback' mode.");
            testMode = TestBase.TestMode.PLAYBACK;
        }
    }

    private static void initPlaybackUri() throws IOException {
        if (isPlaybackMode()) {
            Properties mavenProps = new Properties();
            InputStream in = TestBase.class.getResourceAsStream("/maven.properties");
            if (in == null) {
                throw new IOException(
                        "The file \"maven.properties\" has not been generated yet. Please execute \"mvn compile\" to generate the file.");
            }
            mavenProps.load(in);
            String port = mavenProps.getProperty("playbackServerPort");

            // 11080 and 11081 needs to be in sync with values in jetty.xml file
            playbackUri = PLAYBACK_URI_BASE + "11080";
            alternativePlaybackUri = PLAYBACK_URI_BASE + "11081";
        } else {
            playbackUri = PLAYBACK_URI_BASE + "1234";
        }
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

        return testMode == TestBase.TestMode.PLAYBACK;
    }

    public static boolean isRecordMode() {
        return !isPlaybackMode();
    }

    private static void printThreadInfo(String what) {
        long id = Thread.currentThread().getId();
        String name = Thread.currentThread().getName();
        System.out.println(String.format("\n***\n*** [%s:%s] - %s\n***\n", name, id, what));
    }

    @Rule
    public TestName testName = new TestName();

    protected InterceptorManager interceptorManager = null;

    @BeforeClass
    public static void beforeClass() throws IOException {
        printThreadInfo("beforeclass");
        initTestMode();
        initPlaybackUri();
    }

    @Before
    public void beforeMethod() throws Exception {
        printThreadInfo(String.format("%s: %s", "beforeTest", testName.getMethodName()));
        final String skipMessage = shouldCancelTest(isPlaybackMode());
        Assume.assumeTrue(skipMessage, skipMessage == null);

        interceptorManager = InterceptorManager.create(testName.getMethodName(), testMode);

        RestClient restClient;
        String defaultSubscription;
        ServiceClientCredentials credentials = createTestCredentials();

        if (isRecordMode()) {

            restClient = buildRestClient(new RestClient.Builder().withBaseUrl("https://{vaultBaseUrl}")
                    .withSerializerAdapter(new AzureJacksonAdapter())
                    .withResponseBuilderFactory(new AzureResponseBuilder.Factory()).withCredentials(credentials)
                    .withLogLevel(LogLevel.NONE)
                    .withNetworkInterceptor(new LoggingInterceptor(LogLevel.BODY_AND_HEADERS))
                    .withNetworkInterceptor(interceptorManager.initInterceptor())
                    .withInterceptor(new ResourceManagerThrottlingInterceptor()));

            interceptorManager.addTextReplacementRule("https://management.azure.com/", playbackUri + "/");
            interceptorManager.addTextReplacementRule("https://graph.windows.net/", playbackUri + "/");

            keyVaultClient = new KeyVaultClient(restClient);
            alternativeKeyVaultClient = keyVaultClient;
        } else { // is Playback Mode
            defaultSubscription = ZERO_SUBSCRIPTION;

            out = System.out;
            System.setOut(new PrintStream(new OutputStream() {
                public void write(int b) {
                    // DO NOTHING
                }
            }));

            keyVaultClient = new KeyVaultClient(buildPlaybackRestClient(credentials, playbackUri + "/"));
            alternativeKeyVaultClient = new KeyVaultClient(buildPlaybackRestClient(credentials, alternativePlaybackUri + "/"));
        }

    }

    protected static DeletedCertificateBundle pollOnCertificateDeletion(String vaultBaseUrl, String certificateName)
            throws Exception {
        int pendingPollCount = 0;
        while (pendingPollCount < 21) {
            DeletedCertificateBundle certificateBundle = keyVaultClient.getDeletedCertificate(vaultBaseUrl,
                    certificateName);
            if (certificateBundle == null) {
                SdkContext.sleep(10000);
                pendingPollCount += 1;
                continue;
            } else {
                return certificateBundle;
            }
        }
        throw new Exception("Deleting certificate delayed");
    }

    protected static DeletedKeyBundle pollOnKeyDeletion(String vaultBaseUrl, String certificateName) throws Exception {
        int pendingPollCount = 0;
        while (pendingPollCount < 21) {
            DeletedKeyBundle deletedKeyBundle = keyVaultClient.getDeletedKey(vaultBaseUrl, certificateName);
            if (deletedKeyBundle == null) {
                SdkContext.sleep(10000);
                pendingPollCount += 1;
                continue;
            } else {
                return deletedKeyBundle;
            }
        }
        throw new Exception("Deleting key delayed");
    }

    protected static DeletedSecretBundle pollOnSecretDeletion(String vaultBaseUrl, String secretName) throws Exception {
        int pendingPollCount = 0;
        while (pendingPollCount < 50) {
            DeletedSecretBundle deletedSecretBundle = keyVaultClient.getDeletedSecret(vaultBaseUrl, secretName);
            if (deletedSecretBundle == null) {
                SdkContext.sleep(10000);
                pendingPollCount += 1;
                continue;
            } else {
                return deletedSecretBundle;
            }
        }
        throw new Exception("Deleting secret delayed");
    }

    @After
    public void afterMethod() throws IOException {

        if (shouldCancelTest(isPlaybackMode()) != null) {
            return;
        }

        interceptorManager.finalizeInterceptor();
    }

    protected RestClient buildRestClient(RestClient.Builder builder) {
        return builder.build();
    }

    protected RestClient buildPlaybackRestClient(ServiceClientCredentials credentials, String baseUrl) throws IOException {
        return buildRestClient(new RestClient.Builder().withBaseUrl(baseUrl)
                    .withSerializerAdapter(new AzureJacksonAdapter())
                    .withResponseBuilderFactory(new AzureResponseBuilder.Factory()).withCredentials(credentials)
                    .withLogLevel(LogLevel.NONE)
                    .withNetworkInterceptor(new LoggingInterceptor(LogLevel.BODY_AND_HEADERS))
                    .withNetworkInterceptor(interceptorManager.initInterceptor())
                    .withInterceptor(new ResourceManagerThrottlingInterceptor()));
    }
}
