// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.test;

import com.azure.core.http.HttpClient;
import com.azure.core.implementation.http.HttpClientProviders;
import com.azure.core.test.utils.TestResourceNamer;
import com.azure.core.util.Configuration;
import com.azure.core.util.ServiceVersion;
import com.azure.core.util.logging.ClientLogger;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;

import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Base class for running live and playback tests using {@link InterceptorManager}.
 */
public abstract class TestBase implements BeforeEachCallback {
    // Environment variable name used to determine the TestMode.
    private static final String AZURE_TEST_MODE = "AZURE_TEST_MODE";
    private static final String AZURE_TEST_HTTP_CLIENTS = "AZURE_TEST_HTTP_CLIENTS";
    private static final String AZURE_TEST_HTTP_CLIENTS_VALUE_ALL = "ALL";
    private static final String AZURE_TEST_HTTP_CLIENTS_VALUE_ROLLING = "rolling";
    private static final String AZURE_TEST_HTTP_CLIENTS_VALUE_NETTY = "netty";
    public static final String AZURE_TEST_SERVICE_VERSIONS_VALUE_ALL = "ALL";
    public static final String AZURE_TEST_SERVICE_VERSIONS_VALUE_ROLLING = "rolling";
    public static final int PLATFORM_COUNT = 6;
    private static final List<String> PLATFORM_LIST = buildPlatformList();
    private static final String HTTP_CLIENT_FROM_ENV =
        Configuration.getGlobalConfiguration().get(AZURE_TEST_HTTP_CLIENTS, "netty");

    private static TestMode testMode;

    private final ClientLogger logger = new ClientLogger(TestBase.class);

    protected InterceptorManager interceptorManager;
    protected TestResourceNamer testResourceNamer;
    protected TestContextManager testContextManager;

    private ExtensionContext extensionContext;
    /**
     * Before tests are executed, determines the test mode by reading the {@link TestBase#AZURE_TEST_MODE} environment
     * variable. If it is not set, {@link TestMode#PLAYBACK}
     */
    @BeforeAll
    public static void setupClass() {
        testMode = initializeTestMode();
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        this.extensionContext = extensionContext;
    }

    /**
     * Sets-up the {@link TestBase#testResourceNamer} and {@link TestBase#interceptorManager} before each test case is
     * run. Then calls its implementing class to perform any other set-up commands.
     *
     * @param testInfo {@link TestInfo} to retrieve test method name.
     */
    @BeforeEach
    public void setupTest(TestInfo testInfo) {
        this.testContextManager = new TestContextManager(testInfo.getTestMethod().get(), testMode);
        logger.info("Test Mode: {}, Name: {}", testMode, testContextManager.getTestName());

        try {
            interceptorManager = new InterceptorManager(testContextManager);
        } catch (UncheckedIOException e) {
            logger.error("Could not create interceptor for {}", testContextManager.getTestName(), e);
            Assertions.fail();
        }
        testResourceNamer = new TestResourceNamer(testContextManager, interceptorManager.getRecordedData());

        beforeTest();
    }

    /**
     * Disposes of {@link InterceptorManager} and its inheriting class' resources.
     * @param testInfo the injected testInfo
     */
    @AfterEach
    public void teardownTest(TestInfo testInfo) {
        if (testContextManager.didTestRun()) {
            afterTest();
            interceptorManager.close();
        }
    }

    /**
     * Gets the TestMode that has been initialized.
     *
     * @return The TestMode that has been initialized.
     */
    public TestMode getTestMode() {
        return testMode;
    }

    /**
     * Gets the name of the current test being run.
     *
     * @deprecated This method is deprecated as JUnit 5 provides a simpler mechanism to get the test method name through
     * {@link TestInfo}. Keeping this for backward compatability of other client libraries that still override this
     * method. This method can be deleted when all client libraries remove this method. See {@link
     * #setupTest(TestInfo)}.
     * @return The name of the current test.
     */
    @Deprecated
    protected String getTestName() {
        if (extensionContext != null) {
            return extensionContext.getTestMethod().map(Method::getName).orElse(null);
        }
        return null;
    }

    /**
     * Performs any set-up before each test case. Any initialization that occurs in TestBase occurs first before this.
     * Can be overridden in an inheriting class to add additional functionality during test set-up.
     */
    protected void beforeTest() {
    }

    /**
     * Dispose of any resources and clean-up after a test case runs. Can be overridden in an inheriting class to add
     * additional functionality during test teardown.
     */
    protected void afterTest() {
    }

    /**
     * Get test arguments need to run for the test framework based on the service version.
     *
     * @param serviceVersionList The service version argument for the parameterized tests.
     * @param serviceVersionEnv Indicates whether the service version parameters need to rolling.
     * @return Stream of arguments for parameterized test framework.
     */
    public static Stream<Arguments> getArgumentsFromServiceVersion(List<ServiceVersion> serviceVersionList,
        String serviceVersionEnv) {
        int serviceVersionCount = serviceVersionList.size();
        List<HttpClient> httpClientList = getHttpClients();
        int httpClientCount = httpClientList.size();
        boolean rollingHttpClient = HTTP_CLIENT_FROM_ENV.equalsIgnoreCase(AZURE_TEST_HTTP_CLIENTS_VALUE_ROLLING);
        boolean rollingServiceVersion = serviceVersionEnv != null
            && serviceVersionEnv.equalsIgnoreCase(AZURE_TEST_SERVICE_VERSIONS_VALUE_ROLLING);
        List<Arguments> argumentsList = new ArrayList<>();
        for (ServiceVersion s: serviceVersionList) {
            for (HttpClient h: httpClientList) {
                argumentsList.add(Arguments.of(h, s));
            }
        }
        int offset = getOffset();
        if (rollingServiceVersion && rollingHttpClient) {
            return IntStream.range(0, argumentsList.size())
                .filter(n -> n % PLATFORM_COUNT == offset % argumentsList.size())
                .mapToObj(argumentsList::get)
                .map(TestBase::printout);
        } else if (rollingServiceVersion) {
            return IntStream.range(0, argumentsList.size())
                .filter(n -> (n / httpClientCount) % PLATFORM_COUNT == offset % serviceVersionCount)
                .mapToObj(argumentsList::get)
                .map(TestBase::printout);
        } else if (rollingHttpClient) {
            return IntStream.range(0, argumentsList.size())
                .filter(n -> n % httpClientCount % PLATFORM_COUNT  == offset % httpClientCount)
                .mapToObj(argumentsList::get)
                .map(TestBase::printout);
        }
        for (Arguments arguments: argumentsList) {
            printout(arguments);
        }
        return argumentsList.stream();
    }

    /**
     * Returns a list of {@link HttpClient HttpClients} that should be tested.
     *
     * @return A list of {@link HttpClient HttpClients} to be tested.
     */
    private static List<HttpClient> getHttpClients() {
        if (testMode == TestMode.PLAYBACK) {
            // Call to @MethodSource method happens @BeforeEach call, so the interceptorManager is
            // not yet initialized. So, playbackClient will not be available until later.
            return Arrays.asList(new HttpClient[]{null});
        }
        return HttpClientProviders.getAllHttpClients().stream()
            .filter(TestBase::shouldClientBeTested).collect(toList());
    }

    /**
     * Returns whether the given http clients match the rules of test framework.
     *
     * <ul>
     * <li>Using Netty http client as default if no environment variable is set.</li>
     * <li>If it's set to ALL, all HttpClients in the classpath will be tested.</li>
     * <li>Otherwise, the name of the HttpClient class should match env variable.</li>
     * </ul>
     *
     * Environment values currently supported are: "ALL", "rolling", "netty", "okhttp" which is case insensitive.
     * Use comma to separate http clients want to test.
     * e.g. {@code set AZURE_TEST_HTTP_CLIENTS = NettyAsyncHttpClient, OkHttpAsyncHttpClient}
     *
     * @param client Http client needs to check
     * @return Boolean indicates whether filters out the client or not.
     */
    private static boolean shouldClientBeTested(HttpClient client) {
        if (HTTP_CLIENT_FROM_ENV.trim().toLowerCase(Locale.ROOT).contains("netty")) {
            return client.getClass().getSimpleName().toLowerCase(Locale.ROOT).contains(
                AZURE_TEST_HTTP_CLIENTS_VALUE_NETTY.toLowerCase(Locale.ROOT));
        }
        if (HTTP_CLIENT_FROM_ENV.equalsIgnoreCase(AZURE_TEST_HTTP_CLIENTS_VALUE_ALL)
            || HTTP_CLIENT_FROM_ENV.equalsIgnoreCase(AZURE_TEST_HTTP_CLIENTS_VALUE_ROLLING)) {
            return true;
        }
        String[] configuredHttpClientList = HTTP_CLIENT_FROM_ENV.split(",");
        return Arrays.stream(configuredHttpClientList).anyMatch(configuredHttpClient ->
            client.getClass().getSimpleName().toLowerCase(Locale.ROOT)
                .contains(configuredHttpClient.trim().toLowerCase(Locale.ROOT)));
    }

    private static Arguments printout(Arguments arguments) {
        System.out.println(Arrays.toString(arguments.get()));
        return arguments;
    }

    private static TestMode initializeTestMode() {
        final ClientLogger logger = new ClientLogger(TestBase.class);
        final String azureTestMode = Configuration.getGlobalConfiguration().get(AZURE_TEST_MODE);

        if (azureTestMode != null) {
            try {
                return TestMode.valueOf(azureTestMode.toUpperCase(Locale.US));
            } catch (IllegalArgumentException e) {
                logger.error("Could not parse '{}' into TestEnum. Using 'Playback' mode.", azureTestMode);
                return TestMode.PLAYBACK;
            }
        }

        logger.info("Environment variable '{}' has not been set yet. Using 'Playback' mode.", AZURE_TEST_MODE);
        return TestMode.PLAYBACK;
    }

    /**
     * Sleeps the test for the given amount of milliseconds if {@link TestMode} isn't {@link TestMode#PLAYBACK}.
     *
     * @param millis Number of milliseconds to sleep the test.
     * @throws IllegalStateException If the sleep is interrupted.
     */
    protected void sleepIfRunningAgainstService(long millis) {
        if (testMode == TestMode.PLAYBACK) {
            return;
        }

        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            throw logger.logExceptionAsWarning(new IllegalStateException(ex));
        }
    }

    /**
     * Get offset which determines the rolling strategy.
     *
     * @return The offset according to day of the week and platform information
     */
    private static int getOffset() {
        if (testMode == TestMode.PLAYBACK) {
            return 0;
        }
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        return (today.getDayOfWeek().getValue() + getPlatFormOffset()) % PLATFORM_COUNT;
    }

    private static Integer getPlatFormOffset() {
        String currentOs = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        System.out.println("It is currently running on os: " + currentOs);
        String currentJdk = System.getProperty("java.version").toLowerCase(Locale.ROOT);
        System.out.println("It is using jdk: " + currentJdk);

        for (int i = 0; i < PLATFORM_LIST.size(); i++) {
            if (currentOs.toLowerCase(Locale.ROOT).contains(PLATFORM_LIST.get(i).split(",")[0].toLowerCase(Locale.ROOT))
                && currentJdk.toLowerCase(Locale.ROOT).contains(
                    PLATFORM_LIST.get(i).split(",")[1].toLowerCase(Locale.ROOT))) {
                return i;
            }
        }
        throw new RuntimeException(String.format("Not running on the expected platform. os: %s, jdk: %s",
            currentOs, currentJdk));
    }

    private static List<String> buildPlatformList() {
        List<String> platformList = new ArrayList<>();
        platformList.add("win,8");
        platformList.add("win,11");
        platformList.add("mac,8");
        platformList.add("mac,11");
        platformList.add("linux,8");
        platformList.add("linux,11");
        return platformList;
    }
}
