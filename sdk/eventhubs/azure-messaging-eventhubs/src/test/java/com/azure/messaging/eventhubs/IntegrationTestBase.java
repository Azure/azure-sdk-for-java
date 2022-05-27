// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyAuthenticationType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.implementation.ConnectionStringProperties;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mockito;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URLEncoder;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.azure.core.amqp.ProxyOptions.PROXY_PASSWORD;
import static com.azure.core.amqp.ProxyOptions.PROXY_USERNAME;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Test base for running integration tests.
 */
public abstract class IntegrationTestBase extends TestBase {
    // The number of partitions we create in test-resources.json.
    // Partitions 0 and 1 are used for consume-only operations. 2, 3, and 4 are used to publish or consume events.
    protected static final int NUMBER_OF_PARTITIONS = 5;
    protected static final List<String> EXPECTED_PARTITION_IDS = IntStream.range(0, NUMBER_OF_PARTITIONS)
        .mapToObj(String::valueOf)
        .collect(Collectors.toList());
    protected static final Duration TIMEOUT = Duration.ofMinutes(1);
    protected static final AmqpRetryOptions RETRY_OPTIONS = new AmqpRetryOptions().setTryTimeout(TIMEOUT);

    protected final ClientLogger logger;

    private static final String PROXY_AUTHENTICATION_TYPE = "PROXY_AUTHENTICATION_TYPE";
    private static final String EVENT_HUB_CONNECTION_STRING_ENV_NAME = "AZURE_EVENTHUBS_CONNECTION_STRING";

    private static final String AZURE_EVENTHUBS_FULLY_QUALIFIED_DOMAIN_NAME = "AZURE_EVENTHUBS_FULLY_QUALIFIED_DOMAIN_NAME";
    private static final String AZURE_EVENTHUBS_EVENT_HUB_NAME = "AZURE_EVENTHUBS_EVENT_HUB_NAME";
    private static final Object LOCK = new Object();
    private static final Configuration GLOBAL_CONFIGURATION = Configuration.getGlobalConfiguration();

    private static Scheduler scheduler;
    private static Map<String, IntegrationTestEventData> testEventData;

    private String testName;

    protected IntegrationTestBase(ClientLogger logger) {
        this.logger = logger;
    }

    @BeforeAll
    public static void beforeAll() {
        scheduler = Schedulers.newParallel("eh-integration");
        StepVerifier.setDefaultTimeout(TIMEOUT);
    }

    @AfterAll
    public static void afterAll() {
        StepVerifier.resetDefaultTimeout();
        scheduler.dispose();
    }

    @BeforeEach
    public void setupTest(TestInfo testInfo) {
        System.out.printf("----- [%s]: Performing integration test set-up. -----%n", testInfo.getDisplayName());

        testName = testInfo.getDisplayName();
        skipIfNotRecordMode();

        beforeTest();
    }

    // These are overridden because we don't use the Interceptor Manager.
    @Override
    @AfterEach
    public void teardownTest(TestInfo testInfo) {
        System.out.printf("----- [%s]: Performing test clean-up. -----%n", testInfo.getDisplayName());
        afterTest();

        // Tear down any inline mocks to avoid memory leaks.
        // https://github.com/mockito/mockito/wiki/What's-new-in-Mockito-2#mockito-2250
        Mockito.framework().clearInlineMock(this);
    }

    /**
     * Gets the test mode for this API test. If AZURE_TEST_MODE equals {@link TestMode#RECORD} and Event Hubs connection
     * string is set, then we return {@link TestMode#RECORD}. Otherwise, {@link TestMode#PLAYBACK} is returned.
     */
    @Override
    public TestMode getTestMode() {
        if (super.getTestMode() == TestMode.PLAYBACK) {
            return TestMode.PLAYBACK;
        }

        return CoreUtils.isNullOrEmpty(getConnectionString()) ? TestMode.PLAYBACK : TestMode.RECORD;
    }

    static String getConnectionString() {
        return getConnectionString(false);
    }

    static String getConnectionString(boolean withSas) {
        String connectionString = GLOBAL_CONFIGURATION.get(EVENT_HUB_CONNECTION_STRING_ENV_NAME);
        if (withSas) {
            String shareAccessSignatureFormat = "SharedAccessSignature sr=%s&sig=%s&se=%s&skn=%s";
            String connectionStringWithSasAndEntityFormat = "Endpoint=%s;SharedAccessSignature=%s;EntityPath=%s";
            String connectionStringWithSasFormat = "Endpoint=%s;SharedAccessSignature=%s";

            ConnectionStringProperties properties = new ConnectionStringProperties(connectionString);
            URI endpoint = properties.getEndpoint();
            String entityPath = properties.getEntityPath();
            String resourceUrl = entityPath == null || entityPath.trim().length() == 0
                ? endpoint.toString() : endpoint.toString() +  entityPath;

            String utf8Encoding = UTF_8.name();
            OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plus(Duration.ofHours(2L));
            String expiresOnEpochSeconds = Long.toString(expiresOn.toEpochSecond());

            try {
                String audienceUri = URLEncoder.encode(resourceUrl, utf8Encoding);
                String secretToSign = audienceUri + "\n" + expiresOnEpochSeconds;
                byte[] sasKeyBytes = properties.getSharedAccessKey().getBytes(utf8Encoding);

                Mac hmacsha256 = Mac.getInstance("HMACSHA256");
                hmacsha256.init(new SecretKeySpec(sasKeyBytes, "HMACSHA256"));

                byte[] signatureBytes = hmacsha256.doFinal(secretToSign.getBytes(utf8Encoding));
                String signature = Base64.getEncoder().encodeToString(signatureBytes);

                String signatureValue = String.format(Locale.US, shareAccessSignatureFormat,
                    audienceUri,
                    URLEncoder.encode(signature, utf8Encoding),
                    URLEncoder.encode(expiresOnEpochSeconds, utf8Encoding),
                    URLEncoder.encode(properties.getSharedAccessKeyName(), utf8Encoding));

                if (entityPath == null) {
                    return String.format(connectionStringWithSasFormat, endpoint, signatureValue);
                }
                return String.format(connectionStringWithSasAndEntityFormat, endpoint, signatureValue, entityPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return connectionString;
    }

    /**
     * Gets the configured ProxyConfiguration from environment variables.
     */
    protected ProxyOptions getProxyConfiguration() {
        final String address = GLOBAL_CONFIGURATION.get(Configuration.PROPERTY_HTTP_PROXY);

        if (address == null) {
            return null;
        }

        final String[] host = address.split(":");
        if (host.length < 2) {
            logger.warning("Environment variable '{}' cannot be parsed into a proxy. Value: {}",
                Configuration.PROPERTY_HTTP_PROXY, address);
            return null;
        }

        final String hostname = host[0];
        final int port = Integer.parseInt(host[1]);
        final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostname, port));

        final String username = GLOBAL_CONFIGURATION.get(PROXY_USERNAME);

        if (username == null) {
            logger.info("Environment variable '{}' is not set. No authentication used.");
            return new ProxyOptions(ProxyAuthenticationType.NONE, proxy, null, null);
        }

        final String password = GLOBAL_CONFIGURATION.get(PROXY_PASSWORD);
        final String authentication = GLOBAL_CONFIGURATION.get(PROXY_AUTHENTICATION_TYPE);

        final ProxyAuthenticationType authenticationType = CoreUtils.isNullOrEmpty(authentication)
            ? ProxyAuthenticationType.NONE
            : ProxyAuthenticationType.valueOf(authentication);

        return new ProxyOptions(authenticationType, proxy, username, password);
    }

    protected static String getFullyQualifiedDomainName() {
        return GLOBAL_CONFIGURATION.get(AZURE_EVENTHUBS_FULLY_QUALIFIED_DOMAIN_NAME);
    }

    protected static String getEventHubName() {
        return GLOBAL_CONFIGURATION.get(AZURE_EVENTHUBS_EVENT_HUB_NAME);
    }

    /**
     * Creates a new instance of {@link EventHubClientBuilder} with the default integration test settings and uses a
     * connection string to authenticate.
     */
    protected static EventHubClientBuilder createBuilder() {
        return createBuilder(false);
    }

    /**
     * Creates a new instance of {@link EventHubClientBuilder} with the default integration test settings and uses a
     * connection string to authenticate if {@code useCredentials} is false. Otherwise, uses a service principal through
     * {@link com.azure.identity.ClientSecretCredential}.
     */
    protected static EventHubClientBuilder createBuilder(boolean useCredentials) {
        final EventHubClientBuilder builder = new EventHubClientBuilder()
            .proxyOptions(ProxyOptions.SYSTEM_DEFAULTS)
            .retry(RETRY_OPTIONS)
            .transportType(AmqpTransportType.AMQP)
            .scheduler(scheduler);

        if (useCredentials) {
            final ConnectionStringProperties properties = getConnectionStringProperties();
            final String fqdn = properties.getEndpoint().getHost();
            final String eventHubName = properties.getEntityPath();

            Assumptions.assumeTrue(fqdn != null && !fqdn.isEmpty(), AZURE_EVENTHUBS_FULLY_QUALIFIED_DOMAIN_NAME + " variable needs to be set when using credentials.");
            Assumptions.assumeTrue(eventHubName != null && !eventHubName.isEmpty(), AZURE_EVENTHUBS_EVENT_HUB_NAME + " variable needs to be set when using credentials.");

            final ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(GLOBAL_CONFIGURATION.get("AZURE_CLIENT_ID"))
                .clientSecret(GLOBAL_CONFIGURATION.get("AZURE_CLIENT_SECRET"))
                .tenantId(GLOBAL_CONFIGURATION.get("AZURE_TENANT_ID"))
                .build();

            return builder.credential(fqdn, eventHubName, clientSecretCredential);
        } else {
            return builder.connectionString(getConnectionString());
        }
    }

    protected static ConnectionStringProperties getConnectionStringProperties() {
        return new ConnectionStringProperties(getConnectionString(false));
    }

    protected static ConnectionStringProperties getConnectionStringProperties(boolean withSas) {
        return new ConnectionStringProperties(getConnectionString(withSas));
    }

    /**
     * Gets or creates the integration test data.
     */
    protected synchronized Map<String, IntegrationTestEventData> getTestData() {
        if (testEventData != null) {
            return testEventData;
        }

        System.out.println("--> Adding events to Event Hubs.");
        final Map<String, IntegrationTestEventData> integrationData = new HashMap<>();

        try (EventHubProducerClient producer = new EventHubClientBuilder()
            .connectionString(getConnectionString())
            .buildProducerClient()) {

            producer.getPartitionIds().forEach(partitionId -> {
                System.out.printf("--> Adding events to partition: %s%n", partitionId);
                final PartitionProperties partitionProperties = producer.getPartitionProperties(partitionId);
                final String messageId = UUID.randomUUID().toString();
                final int numberOfEvents = 15;
                final List<EventData> events = TestUtils.getEvents(numberOfEvents, messageId);
                final SendOptions options = new SendOptions().setPartitionId(partitionId);

                producer.send(events, options);

                integrationData.put(partitionId,
                    new IntegrationTestEventData(partitionId, partitionProperties, messageId, events));
            });

            if (integrationData.size() != NUMBER_OF_PARTITIONS) {
                System.out.printf("--> WARNING: Number of partitions is different. Expected: %s. Actual %s%n",
                    NUMBER_OF_PARTITIONS, integrationData.size());
            }

            testEventData = Collections.unmodifiableMap(integrationData);
        }

        Assertions.assertNotNull(testEventData, "'testEventData' should have been set.");
        Assertions.assertFalse(testEventData.isEmpty(), "'testEventData' should not be empty.");
        return testEventData;
    }

    /**
     * Disposes of any {@link Closeable} resources.
     *
     * @param closeables The closeables to dispose of. If a closeable is {@code null}, it is skipped.
     */
    protected void dispose(Closeable... closeables) {
        if (closeables == null || closeables.length == 0) {
            return;
        }

        for (final Closeable closeable : closeables) {
            if (closeable == null) {
                continue;
            }

            try {
                closeable.close();
            } catch (IOException error) {
                logger.error("[{}]: {} didn't close properly.", testName, closeable.getClass().getSimpleName(), error);
            }
        }
    }

    private void skipIfNotRecordMode() {
        Assumptions.assumeTrue(getTestMode() != TestMode.PLAYBACK);
    }
}
