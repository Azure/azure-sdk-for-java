// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.ResourceExistsException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.InterceptorManager;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxyRequestMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.CoreUtils;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.TestUtils;
import com.azure.messaging.servicebus.administration.models.AccessRights;
import com.azure.messaging.servicebus.administration.models.CreateQueueOptions;
import com.azure.messaging.servicebus.administration.models.CreateRuleOptions;
import com.azure.messaging.servicebus.administration.models.CreateSubscriptionOptions;
import com.azure.messaging.servicebus.administration.models.CreateTopicOptions;
import com.azure.messaging.servicebus.administration.models.EmptyRuleAction;
import com.azure.messaging.servicebus.administration.models.FalseRuleFilter;
import com.azure.messaging.servicebus.administration.models.NamespaceType;
import com.azure.messaging.servicebus.administration.models.QueueRuntimeProperties;
import com.azure.messaging.servicebus.administration.models.RuleProperties;
import com.azure.messaging.servicebus.administration.models.SharedAccessAuthorizationRule;
import com.azure.messaging.servicebus.administration.models.SqlRuleAction;
import com.azure.messaging.servicebus.administration.models.SqlRuleFilter;
import com.azure.messaging.servicebus.administration.models.SubscriptionRuntimeProperties;
import com.azure.messaging.servicebus.administration.models.TopicProperties;
import com.azure.messaging.servicebus.administration.models.TopicRuntimeProperties;
import com.azure.messaging.servicebus.administration.models.TrueRuleFilter;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static com.azure.messaging.servicebus.TestUtils.assertAuthorizationRules;
import static com.azure.messaging.servicebus.TestUtils.getEntityName;
import static com.azure.messaging.servicebus.TestUtils.getSessionSubscriptionBaseName;
import static com.azure.messaging.servicebus.TestUtils.getSubscriptionBaseName;
import static com.azure.messaging.servicebus.TestUtils.getTopicBaseName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests {@link ServiceBusAdministrationAsyncClient}.
 */
@Tag("integration")
class ServiceBusAdministrationAsyncClientIntegrationTest extends TestProxyTestBase {
    private static final Duration TIMEOUT = Duration.ofSeconds(20);
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

    /**
     * Sanitizer to remove header values for ServiceBusDlqSupplementaryAuthorization and
     * ServiceBusSupplementaryAuthorization.
     */
    static final TestProxySanitizer AUTHORIZATION_HEADER;

    static final List<TestProxySanitizer> TEST_PROXY_SANITIZERS;

    static final List<TestProxyRequestMatcher> TEST_PROXY_REQUEST_MATCHERS;

    static {
        AUTHORIZATION_HEADER = new TestProxySanitizer("SupplementaryAuthorization", null,
            "SharedAccessSignature sr=https%3A%2F%2Ffoo.servicebus.windows.net&sig=dummyValue%3D&se=1687267490&skn=dummyKey",
            TestProxySanitizerType.HEADER);
        TEST_PROXY_SANITIZERS = Collections.singletonList(AUTHORIZATION_HEADER);

        final List<String> skippedHeaders = Arrays.asList("ServiceBusDlqSupplementaryAuthorization",
            "ServiceBusSupplementaryAuthorization");
        final CustomMatcher customMatcher = new CustomMatcher().setExcludedHeaders(skippedHeaders);

        TEST_PROXY_REQUEST_MATCHERS = Collections.singletonList(customMatcher);
    }

    private final AtomicReference<TokenCredential> credentialCached = new AtomicReference<>();

    public static Stream<Arguments> createHttpClients() {
        return Stream.of(Arguments.of(new NettyAsyncHttpClientBuilder().build()));
    }

    /**
     * Test to connect to the service bus using com.azure.identity.ClientSecretCredential.
     * <p>
     * This is a potential test eligible to run in auxiliary tenant with secret auth. The CI Pipeline cannot be enabled
     * for both Federated Managed Identity auth and Secret auth, the ARM deployment will fail if an attempt is made to
     * enable two auth types - Exception calling "Invoke" with "0" argument(s): "Cannot process command because of one or
     * more missing mandatory parameters: testApplicationSecret.". Hence, this test is disabled until auxiliary tenant is
     * available.
     * </p>
     */
    @ParameterizedTest
    @MethodSource("createHttpClients")
    @Disabled("The CI Pipeline cannot be enabled for both Federated Managed Identity auth and Secret auth")
    void azureClientSecretCredential(HttpClient httpClient) {
        final String fullyQualifiedDomainName = TestUtils.getFullyQualifiedDomainName(true);
        final TokenCredential tokenCredential;
        if (interceptorManager.isPlaybackMode()) {
            tokenCredential = request -> Mono.fromCallable(() ->
                new AccessToken("foo-bar", OffsetDateTime.now().plus(Duration.ofMinutes(5))));
        } else {
            tokenCredential = new DefaultAzureCredentialBuilder().build();
        }

        final ServiceBusAdministrationClientBuilder builder = new ServiceBusAdministrationClientBuilder();

        if (interceptorManager.isPlaybackMode()) {
            builder.httpClient(interceptorManager.getPlaybackClient());
        } else if (interceptorManager.isLiveMode()) {
            builder.httpClient(httpClient);
        } else {
            builder.httpClient(httpClient).addPolicy(interceptorManager.getRecordPolicy());
        }

        final ServiceBusAdministrationAsyncClient client = builder
            .credential(fullyQualifiedDomainName, tokenCredential)
            .buildAsyncClient();

        StepVerifier.create(client.getNamespaceProperties())
            .assertNext(properties -> {
                assertNotNull(properties);
                if (!interceptorManager.isPlaybackMode()) {
                    final String[] split = TestUtils.getFullyQualifiedDomainName(true).split("\\.", 2);
                    assertEquals(split[0], properties.getName());
                }
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    //region Create tests

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void createQueue(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String queueName = testResourceNamer.randomName("test", 10);
        final CreateQueueOptions expected = new CreateQueueOptions()
            .setMaxSizeInMegabytes(1024)
            .setMaxDeliveryCount(7)
            .setLockDuration(Duration.ofSeconds(45))
            .setSessionRequired(true)
            .setDuplicateDetectionRequired(true)
            .setDuplicateDetectionHistoryTimeWindow(Duration.ofMinutes(2))
            .setUserMetadata("some-metadata-for-testing");

        // Act & Assert
        StepVerifier.create(client.createQueue(queueName, expected))
            .assertNext(actual -> {
                assertEquals(queueName, actual.getName());

                assertEquals(expected.getLockDuration(), actual.getLockDuration());
                assertEquals(expected.getMaxDeliveryCount(), actual.getMaxDeliveryCount());
                assertEquals(expected.getMaxSizeInMegabytes(), actual.getMaxSizeInMegabytes());
                assertEquals(expected.getUserMetadata(), actual.getUserMetadata());

                assertEquals(expected.isDeadLetteringOnMessageExpiration(), actual.isDeadLetteringOnMessageExpiration());
                assertEquals(expected.isPartitioningEnabled(), actual.isPartitioningEnabled());
                assertEquals(expected.isDuplicateDetectionRequired(), actual.isDuplicateDetectionRequired());
                assertEquals(expected.isSessionRequired(), actual.isSessionRequired());

                final QueueRuntimeProperties runtimeProperties = new QueueRuntimeProperties(actual);
                assertEquals(0, runtimeProperties.getTotalMessageCount());
                assertEquals(0, runtimeProperties.getSizeInBytes());
                assertNotNull(runtimeProperties.getCreatedAt());
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void createQueueExistingName(HttpClient httpClient) {
        // Arrange
        final String queueName = getEntityName(TestUtils.getQueueBaseName(), 5);
        final CreateQueueOptions options = new CreateQueueOptions();
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);

        // Act & Assert
        StepVerifier.create(client.createQueue(queueName, options))
            .expectError(ResourceExistsException.class)
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void createQueueWithForwarding(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String queueName = testResourceNamer.randomName("test", 10);
        final String forwardToEntityName = getEntityName(TestUtils.getQueueBaseName(), 5);
        final CreateQueueOptions expected = new CreateQueueOptions()
            .setForwardTo(forwardToEntityName)
            .setForwardDeadLetteredMessagesTo(forwardToEntityName);

        // Act & Assert
        StepVerifier.create(client.createQueue(queueName, expected))
            .assertNext(actual -> {
                assertEquals(queueName, actual.getName());

                // The URLs will be fake in playback mode.
                if (!interceptorManager.isPlaybackMode()) {
                    assertEquals(expected.getForwardTo(), actual.getForwardTo());
                    assertEquals(expected.getForwardDeadLetteredMessagesTo(), actual.getForwardDeadLetteredMessagesTo());
                }

                final QueueRuntimeProperties runtimeProperties = new QueueRuntimeProperties(actual);
                assertNotNull(runtimeProperties.getCreatedAt());
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void createQueueAuthorizationRules(HttpClient httpClient) {
        // Arrange
        final String keyName = "test-rule";
        final List<AccessRights> accessRights = Collections.singletonList(AccessRights.SEND);
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String queueName = testResourceNamer.randomName("test", 10);
        final SharedAccessAuthorizationRule rule = interceptorManager.isPlaybackMode()
            ? new SharedAccessAuthorizationRule(keyName, "REDACTED",
            "REDACTED", accessRights)
            : new SharedAccessAuthorizationRule(keyName, accessRights);

        final CreateQueueOptions expected = new CreateQueueOptions()
            .setMaxSizeInMegabytes(1024)
            .setMaxDeliveryCount(7)
            .setLockDuration(Duration.ofSeconds(45))
            .setSessionRequired(true)
            .setDuplicateDetectionRequired(true)
            .setDuplicateDetectionHistoryTimeWindow(Duration.ofMinutes(2))
            .setUserMetadata("some-metadata-for-testing");

        expected.getAuthorizationRules().add(rule);

        // Act & Assert
        StepVerifier.create(client.createQueue(queueName, expected))
            .assertNext(actual -> {
                assertEquals(queueName, actual.getName());

                assertEquals(expected.getLockDuration(), actual.getLockDuration());
                assertEquals(expected.getMaxDeliveryCount(), actual.getMaxDeliveryCount());
                assertEquals(expected.getMaxSizeInMegabytes(), actual.getMaxSizeInMegabytes());
                assertEquals(expected.getUserMetadata(), actual.getUserMetadata());

                assertEquals(expected.isDeadLetteringOnMessageExpiration(), actual.isDeadLetteringOnMessageExpiration());
                assertEquals(expected.isPartitioningEnabled(), actual.isPartitioningEnabled());
                assertEquals(expected.isDuplicateDetectionRequired(), actual.isDuplicateDetectionRequired());
                assertEquals(expected.isSessionRequired(), actual.isSessionRequired());

                final QueueRuntimeProperties runtimeProperties = new QueueRuntimeProperties(actual);
                assertEquals(0, runtimeProperties.getTotalMessageCount());
                assertEquals(0, runtimeProperties.getSizeInBytes());
                assertNotNull(runtimeProperties.getCreatedAt());

                assertAuthorizationRules(expected.getAuthorizationRules(), actual.getAuthorizationRules());
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void createRule(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);

        final String ruleName = testResourceNamer.randomName("rule", 10);
        final String topicName = getEntityName(getTopicBaseName(), 13);
        final String subscriptionName = getSubscriptionBaseName();
        final SqlRuleAction action = new SqlRuleAction("SET Label = 'test'");
        final CreateRuleOptions options = new CreateRuleOptions()
            .setAction(action)
            .setFilter(new FalseRuleFilter());

        // Act & Assert
        StepVerifier.create(client.createRule(topicName, subscriptionName, ruleName, options))
            .assertNext(contents -> {

                assertNotNull(contents);
                assertEquals(ruleName, contents.getName());

                assertNotNull(contents.getAction());
                assertTrue(contents.getAction() instanceof SqlRuleAction);
                assertEquals(action.getSqlExpression(), ((SqlRuleAction) contents.getAction()).getSqlExpression());

                assertNotNull(contents.getFilter());
                assertTrue(contents.getFilter() instanceof FalseRuleFilter);

            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void createRuleDefaults(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);

        final String ruleName = testResourceNamer.randomName("rule", 10);
        final String topicName = getEntityName(getTopicBaseName(), 13);
        final String subscriptionName = getSubscriptionBaseName();

        // Act & Assert
        StepVerifier.create(client.createRule(topicName, subscriptionName, ruleName))
            .assertNext(contents -> {
                assertEquals(ruleName, contents.getName());
                assertTrue(contents.getFilter() instanceof TrueRuleFilter);
                assertTrue(contents.getAction() instanceof EmptyRuleAction);
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void createRuleResponse(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);

        final String ruleName = testResourceNamer.randomName("rule", 10);
        final String topicName = getEntityName(getTopicBaseName(), 13);
        final String subscriptionName = getSubscriptionBaseName();

        final SqlRuleFilter filter = new SqlRuleFilter("sys.To=@MyParameter OR sys.MessageId IS NULL");
        filter.getParameters().put("@MyParameter", "My-Parameter-Value");

        final CreateRuleOptions options = new CreateRuleOptions()
            .setAction(new EmptyRuleAction())
            .setFilter(filter);

        // Act & Assert
        StepVerifier.create(client.createRuleWithResponse(topicName, subscriptionName, ruleName, options))
            .assertNext(response -> {
                assertEquals(201, response.getStatusCode());

                final RuleProperties contents = response.getValue();

                assertNotNull(contents);
                assertEquals(ruleName, contents.getName());

                assertNotNull(contents.getFilter());
                assertTrue(contents.getFilter() instanceof SqlRuleFilter);

                final SqlRuleFilter actualFilter = (SqlRuleFilter) contents.getFilter();
                assertEquals(filter.getSqlExpression(), actualFilter.getSqlExpression());

                assertNotNull(contents.getAction());
                assertTrue(contents.getAction() instanceof EmptyRuleAction);
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void createSubscription(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = getEntityName(getTopicBaseName(), 0);
        final String subscriptionName = testResourceNamer.randomName("sub", 10);
        final CreateSubscriptionOptions expected = new CreateSubscriptionOptions()
            .setMaxDeliveryCount(7)
            .setLockDuration(Duration.ofSeconds(45))
            .setUserMetadata("some-metadata-for-testing-subscriptions");

        // Act & Assert
        StepVerifier.create(client.createSubscription(topicName, subscriptionName, expected))
            .assertNext(actual -> {
                assertEquals(topicName, actual.getTopicName());
                assertEquals(subscriptionName, actual.getSubscriptionName());

                assertEquals(expected.getLockDuration(), actual.getLockDuration());
                assertEquals(expected.getMaxDeliveryCount(), actual.getMaxDeliveryCount());
                assertEquals(expected.getUserMetadata(), actual.getUserMetadata());

                assertEquals(expected.isDeadLetteringOnMessageExpiration(), actual.isDeadLetteringOnMessageExpiration());
                assertEquals(expected.isSessionRequired(), actual.isSessionRequired());
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void createSubscriptionWithRule(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = getEntityName(getTopicBaseName(), 0);
        final String subscriptionName = testResourceNamer.randomName("sub", 10);
        final CreateSubscriptionOptions subscriptionOptions = new CreateSubscriptionOptions()
            .setMaxDeliveryCount(7)
            .setLockDuration(Duration.ofSeconds(45))
            .setUserMetadata("some-metadata-for-testing-subscriptions");

        final String ruleName = testResourceNamer.randomName("rule", 10);
        final CreateRuleOptions ruleOptions = new CreateRuleOptions(new SqlRuleFilter("color='red'"));
        // Act & Assert
        StepVerifier.create(client.createSubscription(topicName, subscriptionName, ruleName, subscriptionOptions, ruleOptions)
                .flatMap(s -> client.getRule(topicName, subscriptionName, ruleName)))
            .assertNext(rule -> {
                assertEquals(ruleName, rule.getName());
                assertTrue(rule.getFilter() instanceof SqlRuleFilter);
                assertEquals("color='red'", ((SqlRuleFilter) rule.getFilter()).getSqlExpression());
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void createSubscriptionExistingName(HttpClient httpClient) {
        // Arrange
        final String topicName = getEntityName(getTopicBaseName(), 1);
        final String subscriptionName = getSessionSubscriptionBaseName();
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);

        // Act & Assert
        StepVerifier.create(client.createSubscription(topicName, subscriptionName))
            .expectError(ResourceExistsException.class)
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void createSubscriptionWithForwarding(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = getEntityName(getTopicBaseName(), 3);
        final String subscriptionName = testResourceNamer.randomName("sub", 50);
        final String forwardToTopic = getEntityName(getTopicBaseName(), 4);
        final CreateSubscriptionOptions expected = new CreateSubscriptionOptions()
            .setForwardTo(forwardToTopic)
            .setForwardDeadLetteredMessagesTo(forwardToTopic);

        // Act & Assert
        StepVerifier.create(client.createSubscription(topicName, subscriptionName, expected))
            .assertNext(actual -> {
                assertEquals(topicName, actual.getTopicName());
                assertEquals(subscriptionName, actual.getSubscriptionName());

                // URLs are redacted so they will not match.
                if (!interceptorManager.isPlaybackMode()) {
                    assertEquals(expected.getForwardTo(), actual.getForwardTo());
                    assertEquals(expected.getForwardDeadLetteredMessagesTo(), actual.getForwardDeadLetteredMessagesTo());
                }
            })
            .expectComplete()
            .verify(TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void createTopicWithResponse(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = testResourceNamer.randomName("test", 10);
        final CreateTopicOptions expected = new CreateTopicOptions()
            .setMaxSizeInMegabytes(2048L)
            .setDuplicateDetectionRequired(true)
            .setDuplicateDetectionHistoryTimeWindow(Duration.ofMinutes(2))
            .setUserMetadata("some-metadata-for-testing-topic");

        // Act & Assert
        StepVerifier.create(client.createTopicWithResponse(topicName, expected))
            .assertNext(response -> {
                assertEquals(201, response.getStatusCode());

                // Assert values on a topic.
                final TopicProperties actual = response.getValue();

                assertEquals(topicName, actual.getName());

                assertEquals(expected.getMaxSizeInMegabytes(), actual.getMaxSizeInMegabytes());
                assertEquals(expected.getUserMetadata(), actual.getUserMetadata());

                assertEquals(expected.isPartitioningEnabled(), actual.isPartitioningEnabled());
                assertEquals(expected.isDuplicateDetectionRequired(), actual.isDuplicateDetectionRequired());

                final TopicRuntimeProperties runtimeProperties = new TopicRuntimeProperties(actual);
                assertEquals(0, runtimeProperties.getSubscriptionCount());
                assertEquals(0, runtimeProperties.getSizeInBytes());
                assertNotNull(runtimeProperties.getCreatedAt());
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void createTopicExistingName(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = getEntityName(getTopicBaseName(), 3);
        final CreateTopicOptions expected = new CreateTopicOptions()
            .setMaxSizeInMegabytes(2048L)
            .setDuplicateDetectionRequired(true)
            .setDuplicateDetectionHistoryTimeWindow(Duration.ofMinutes(2))
            .setUserMetadata("some-metadata-for-testing-topic");

        // Act & Assert
        StepVerifier.create(client.createTopicWithResponse(topicName, expected))
            .expectError(ResourceExistsException.class)
            .verify(DEFAULT_TIMEOUT);
    }

    //endregion

    //region Delete tests

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void deleteQueue(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String queueName = testResourceNamer.randomName("sub", 10);

        client.createQueue(queueName)
            .onErrorResume(ResourceExistsException.class, e -> Mono.empty())
            .block(TIMEOUT);

        // Act & Assert
        StepVerifier.create(client.deleteQueue(queueName))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void deleteQueueDoesNotExist(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String queueName = testResourceNamer.randomName("queue", 10);

        // Act & Assert
        StepVerifier.create(client.deleteQueue(queueName))
            .expectError(ResourceNotFoundException.class)
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void deleteRule(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String ruleName = testResourceNamer.randomName("rule-", 11);
        final String topicName = getEntityName(getTopicBaseName(), 13);
        final String subscriptionName = getSubscriptionBaseName();

        client.createRule(topicName, subscriptionName, ruleName).block(TIMEOUT);

        // Act & Assert
        StepVerifier.create(client.deleteRule(topicName, subscriptionName, ruleName))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void deleteRuleDoesNotExist(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String ruleName = testResourceNamer.randomName("rule-", 11);
        final String topicName = getEntityName(getTopicBaseName(), 13);
        final String subscriptionName = getSubscriptionBaseName();

        // Act & Assert
        StepVerifier.create(client.deleteRule(topicName, subscriptionName, ruleName))
            .expectError(ResourceNotFoundException.class)
            .verify(TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void deleteSubscription(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = testResourceNamer.randomName("topic", 10);
        final String subscriptionName = testResourceNamer.randomName("sub", 7);

        client.createTopic(topicName).block(TIMEOUT);
        client.createSubscription(topicName, subscriptionName).block(TIMEOUT);

        // Act & Assert
        StepVerifier.create(client.deleteSubscription(topicName, subscriptionName))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void deleteSubscriptionDoesNotExist(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = testResourceNamer.randomName("topic", 10);
        final String subscriptionName = testResourceNamer.randomName("sub", 7);

        // The topic exists but the subscription does not.
        client.createTopic(topicName).block(TIMEOUT);

        // Act & Assert
        StepVerifier.create(client.deleteSubscription(topicName, subscriptionName))
            .expectError(ResourceNotFoundException.class)
            .verify(TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void deleteTopic(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = testResourceNamer.randomName("topic", 10);

        client.createTopic(topicName).block(TIMEOUT);

        // Act & Assert
        StepVerifier.create(client.deleteTopic(topicName))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void deleteTopicDoesNotExist(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = testResourceNamer.randomName("topic", 10);

        // Act & Assert
        StepVerifier.create(client.deleteTopic(topicName))
            .expectError(ResourceNotFoundException.class)
            .verify(TIMEOUT);
    }

    //endregion

    //region Get and exists tests

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getQueue(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String queueName = getEntityName(TestUtils.getQueueBaseName(), 5);
        final OffsetDateTime nowUtc = OffsetDateTime.now(Clock.systemUTC());

        // Act & Assert
        StepVerifier.create(client.getQueue(queueName))
            .assertNext(queueDescription -> {
                assertEquals(queueName, queueDescription.getName());

                assertFalse(queueDescription.isPartitioningEnabled());
                assertFalse(queueDescription.isSessionRequired());
                assertNotNull(queueDescription.getLockDuration());

                final QueueRuntimeProperties runtimeProperties = new QueueRuntimeProperties(queueDescription);
                assertNotNull(runtimeProperties.getCreatedAt());
                assertTrue(nowUtc.isAfter(runtimeProperties.getCreatedAt()));
                assertNotNull(runtimeProperties.getAccessedAt());
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getNamespace(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);

        // Act & Assert
        StepVerifier.create(client.getNamespaceProperties())
            .assertNext(properties -> {
                assertEquals(NamespaceType.MESSAGING, properties.getNamespaceType());
                if (!interceptorManager.isPlaybackMode()) {
                    final String[] split = TestUtils.getFullyQualifiedDomainName(true).split("\\.", 2);
                    assertEquals(split[0], properties.getName());
                }
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getQueueDoesNotExist(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String queueName = testResourceNamer.randomName("exist", 10);

        // Act & Assert
        StepVerifier.create(client.getQueue(queueName))
            .expectError(ResourceNotFoundException.class)
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getQueueExists(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String queueName = getEntityName(TestUtils.getQueueBaseName(), 2);

        // Act & Assert
        StepVerifier.create(client.getQueueExists(queueName))
            .expectNext(true)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getQueueExistsFalse(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String queueName = testResourceNamer.randomName("exist", 10);

        // Act & Assert
        StepVerifier.create(client.getQueueExists(queueName))
            .expectNext(false)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getQueueRuntimeProperties(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String queueName = getEntityName(TestUtils.getQueueBaseName(), 2);
        final OffsetDateTime nowUtc = OffsetDateTime.now(Clock.systemUTC());

        // Act & Assert
        StepVerifier.create(client.getQueueRuntimeProperties(queueName))
            .assertNext(RuntimeProperties -> {
                assertEquals(queueName, RuntimeProperties.getName());

                assertNotNull(RuntimeProperties.getCreatedAt());
                assertTrue(nowUtc.isAfter(RuntimeProperties.getCreatedAt()));
                assertNotNull(RuntimeProperties.getAccessedAt());
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getRule(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);

        // There is a single default rule created.
        final String ruleName = "$Default";
        final String topicName = getEntityName(getTopicBaseName(), 13);
        final String subscriptionName = getSubscriptionBaseName();

        // Act & Assert
        StepVerifier.create(client.getRuleWithResponse(topicName, subscriptionName, ruleName))
            .assertNext(response -> {
                assertEquals(200, response.getStatusCode());

                final RuleProperties contents = response.getValue();

                assertNotNull(contents);
                assertEquals(ruleName, contents.getName());
                assertNotNull(contents.getFilter());
                assertTrue(contents.getFilter() instanceof TrueRuleFilter);

                assertNotNull(contents.getAction());
                assertTrue(contents.getAction() instanceof EmptyRuleAction);
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getRuleDoesNotExist(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);

        final String ruleName = "does-not-exist-rule";
        final String topicName = getEntityName(getTopicBaseName(), 13);
        final String subscriptionName = getSubscriptionBaseName();

        // Act & Assert
        StepVerifier.create(client.getRuleWithResponse(topicName, subscriptionName, ruleName))
            .expectError(ResourceNotFoundException.class)
            .verify(TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getSubscription(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = getEntityName(getTopicBaseName(), 1);
        final String subscriptionName = getSessionSubscriptionBaseName();
        final OffsetDateTime nowUtc = OffsetDateTime.now(Clock.systemUTC());

        // Act & Assert
        StepVerifier.create(client.getSubscription(topicName, subscriptionName))
            .assertNext(description -> {
                assertEquals(topicName, description.getTopicName());
                assertEquals(subscriptionName, description.getSubscriptionName());

                assertTrue(description.isSessionRequired());
                assertNotNull(description.getLockDuration());

                final SubscriptionRuntimeProperties runtimeProperties = new SubscriptionRuntimeProperties(description);
                assertNotNull(runtimeProperties.getCreatedAt());
                assertTrue(nowUtc.isAfter(runtimeProperties.getCreatedAt()));
                assertNotNull(runtimeProperties.getAccessedAt());
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getSubscriptionDoesNotExist(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = getEntityName(getTopicBaseName(), 1);
        final String subscriptionName = "subscription-session-not-exist";

        // Act & Assert
        StepVerifier.create(client.getSubscription(topicName, subscriptionName))
            .expectError(ResourceNotFoundException.class)
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getSubscriptionExists(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = getEntityName(getTopicBaseName(), 1);
        final String subscriptionName = getSessionSubscriptionBaseName();

        // Act & Assert
        StepVerifier.create(client.getSubscriptionExists(topicName, subscriptionName))
            .expectNext(true)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getSubscriptionExistsFalse(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = getEntityName(getTopicBaseName(), 1);
        final String subscriptionName = "subscription-session-not-exist";

        // Act & Assert
        StepVerifier.create(client.getSubscriptionExists(topicName, subscriptionName))
            .expectNext(false)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getSubscriptionRuntimeProperties(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = getEntityName(getTopicBaseName(), 1);
        final String subscriptionName = getSessionSubscriptionBaseName();
        final OffsetDateTime nowUtc = OffsetDateTime.now(Clock.systemUTC());

        // Act & Assert
        StepVerifier.create(client.getSubscriptionRuntimeProperties(topicName, subscriptionName))
            .assertNext(description -> {
                assertEquals(topicName, description.getTopicName());
                assertEquals(subscriptionName, description.getSubscriptionName());

                assertTrue(description.getTotalMessageCount() >= 0);
                assertEquals(0, description.getActiveMessageCount());
                assertEquals(0, description.getTransferDeadLetterMessageCount());
                assertEquals(0, description.getTransferMessageCount());
                assertTrue(description.getDeadLetterMessageCount() >= 0);

                assertNotNull(description.getCreatedAt());
                assertTrue(nowUtc.isAfter(description.getCreatedAt()));
                assertNotNull(description.getAccessedAt());
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getTopic(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = getEntityName(getTopicBaseName(), 1);
        final OffsetDateTime nowUtc = OffsetDateTime.now(Clock.systemUTC());

        // Act & Assert
        StepVerifier.create(client.getTopic(topicName))
            .assertNext(topicDescription -> {
                assertEquals(topicName, topicDescription.getName());

                assertTrue(topicDescription.isBatchedOperationsEnabled());
                assertFalse(topicDescription.isDuplicateDetectionRequired());
                assertNotNull(topicDescription.getDuplicateDetectionHistoryTimeWindow());
                assertNotNull(topicDescription.getDefaultMessageTimeToLive());
                assertFalse(topicDescription.isPartitioningEnabled());

                final TopicRuntimeProperties runtimeProperties = new TopicRuntimeProperties(topicDescription);
                assertNotNull(runtimeProperties.getCreatedAt());
                assertTrue(nowUtc.isAfter(runtimeProperties.getCreatedAt()));
                assertNotNull(runtimeProperties.getAccessedAt());
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getTopicDoesNotExist(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = testResourceNamer.randomName("exists", 10);

        // Act & Assert
        StepVerifier.create(client.getTopic(topicName))
            .consumeErrorWith(error -> {
                assertTrue(error instanceof ResourceNotFoundException);

                final ResourceNotFoundException notFoundError = (ResourceNotFoundException) error;
                final HttpResponse response = notFoundError.getResponse();

                assertNotNull(response);
                assertEquals(200, response.getStatusCode());
                StepVerifier.create(response.getBody())
                    .verifyComplete();
            })
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getTopicExists(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = getEntityName(getTopicBaseName(), 1);

        // Act & Assert
        StepVerifier.create(client.getTopicExists(topicName))
            .expectNext(true)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getTopicExistsFalse(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = testResourceNamer.randomName("exists", 10);

        // Act & Assert
        StepVerifier.create(client.getTopicExists(topicName))
            .expectNext(false)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getTopicRuntimeProperties(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = getEntityName(getTopicBaseName(), 1);
        final OffsetDateTime nowUtc = OffsetDateTime.now(Clock.systemUTC());

        // Act & Assert
        StepVerifier.create(client.getTopicRuntimeProperties(topicName))
            .assertNext(RuntimeProperties -> {
                assertEquals(topicName, RuntimeProperties.getName());

                assertTrue(RuntimeProperties.getSubscriptionCount() > 1);

                assertNotNull(RuntimeProperties.getCreatedAt());
                assertTrue(nowUtc.isAfter(RuntimeProperties.getCreatedAt()));
                assertNotNull(RuntimeProperties.getAccessedAt());
                assertTrue(nowUtc.isAfter(RuntimeProperties.getAccessedAt()));
                assertEquals(0, RuntimeProperties.getScheduledMessageCount());

            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getSubscriptionRuntimePropertiesUnauthorizedClient(HttpClient httpClient) {
        // Arrange
        final String connectionString = interceptorManager.isPlaybackMode()
            ? "Endpoint=sb://foo.servicebus.windows.net;SharedAccessKeyName=dummyKey;SharedAccessKey=dummyAccessKey"
            : TestUtils.getConnectionString(false);

        final String connectionStringUpdated = connectionString.replace("SharedAccessKey=",
            "SharedAccessKey=fake-key-");

        final ServiceBusAdministrationClientBuilder builder = new ServiceBusAdministrationClientBuilder()
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .connectionString(connectionStringUpdated);

        if (interceptorManager.isPlaybackMode()) {
            builder.httpClient(interceptorManager.getPlaybackClient());
        } else if (interceptorManager.isLiveMode()) {
            builder.httpClient(httpClient);
        } else {
            builder.httpClient(httpClient)
                .addPolicy(interceptorManager.getRecordPolicy());
        }

        final ServiceBusAdministrationAsyncClient client = builder.buildAsyncClient();

        final String topicName = getEntityName(getTopicBaseName(), 1);
        final String subscriptionName = getEntityName(getSubscriptionBaseName(), 2);

        // Act & Assert
        StepVerifier.create(client.getSubscriptionRuntimeProperties(topicName, subscriptionName))
            .expectErrorMatches(throwable -> throwable instanceof ClientAuthenticationException)
            .verify(DEFAULT_TIMEOUT);
    }

    //endregion

    //region List tests

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void listRules(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);

        // There is a single default rule created.
        final String ruleName = "$Default";
        final String topicName = getEntityName(getTopicBaseName(), 13);
        final String subscriptionName = getSubscriptionBaseName();

        // Act & Assert
        StepVerifier.create(client.listRules(topicName, subscriptionName))
            .assertNext(response -> {
                assertEquals(ruleName, response.getName());
                assertNotNull(response.getFilter());
                assertTrue(response.getFilter() instanceof TrueRuleFilter);

                assertNotNull(response.getAction());
                assertTrue(response.getAction() instanceof EmptyRuleAction);
            })
            .thenCancel()
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void listQueues(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);

        // Act & Assert
        StepVerifier.create(client.listQueues())
            .assertNext(queueDescription -> {
                assertNotNull(queueDescription.getName());
                assertTrue(queueDescription.isBatchedOperationsEnabled());
                assertFalse(queueDescription.isDuplicateDetectionRequired());
                assertFalse(queueDescription.isPartitioningEnabled());
            })
            .expectNextCount(9)
            .thenCancel()
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void listSubscriptions(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = getEntityName(getTopicBaseName(), 1);

        // Act & Assert
        StepVerifier.create(client.listSubscriptions(topicName))
            .assertNext(subscription -> {
                assertEquals(topicName, subscription.getTopicName());
                assertNotNull(subscription.getSubscriptionName());
            })
            .expectNextCount(1)
            .thenCancel()
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void listTopics(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);

        // Act & Assert
        StepVerifier.create(client.listTopics())
            .assertNext(topics -> {
                assertNotNull(topics.getName());
                assertTrue(topics.isBatchedOperationsEnabled());
                assertFalse(topics.isPartitioningEnabled());
            })
            .expectNextCount(2)
            .thenCancel()
            .verify(DEFAULT_TIMEOUT);
    }

    //endregion

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void updateRuleResponse(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);

        final String ruleName = testResourceNamer.randomName("rule", 15);
        final String topicName = getEntityName(getTopicBaseName(), 12);
        final String subscriptionName = getSubscriptionBaseName();
        final SqlRuleAction expectedAction = new SqlRuleAction("SET MessageId = 'matching-id'");
        final SqlRuleFilter expectedFilter = new SqlRuleFilter("sys.To = 'telemetry-event'");

        final RuleProperties existingRule = client.createRule(topicName, subscriptionName, ruleName).block(TIMEOUT);
        assertNotNull(existingRule);

        existingRule.setAction(expectedAction).setFilter(expectedFilter);

        // Act & Assert
        StepVerifier.create(client.updateRule(topicName, subscriptionName, existingRule))
            .assertNext(contents -> {
                assertNotNull(contents);
                assertEquals(ruleName, contents.getName());

                assertTrue(contents.getFilter() instanceof SqlRuleFilter);
                assertEquals(expectedFilter.getSqlExpression(),
                    ((SqlRuleFilter) contents.getFilter()).getSqlExpression());

                assertTrue(contents.getAction() instanceof SqlRuleAction);
                assertEquals(expectedAction.getSqlExpression(),
                    ((SqlRuleAction) contents.getAction()).getSqlExpression());
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    private ServiceBusAdministrationAsyncClient createClient(HttpClient httpClient) {
        final ServiceBusAdministrationClientBuilder builder = new ServiceBusAdministrationClientBuilder()
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));
        configure(builder, httpClient, interceptorManager, credentialCached);
        return builder.buildAsyncClient();
    }

    static void configure(ServiceBusAdministrationClientBuilder builder,
        HttpClient httpClient, InterceptorManager interceptorManager, AtomicReference<TokenCredential> credentialCached) {
        if (interceptorManager.isPlaybackMode()) {
            builder.credential(TestUtils.getFullyQualifiedDomainName(true), new MockTokenCredential());
            builder.httpClient(interceptorManager.getPlaybackClient());
        } else if (interceptorManager.isLiveMode()) {
            final String fullyQualifiedDomainName = TestUtils.getFullyQualifiedDomainName(false);
            assumeTrue(!CoreUtils.isNullOrEmpty(fullyQualifiedDomainName), "FullyQualifiedDomainName is not set.");
            final TokenCredential credential = TestUtils.getPipelineCredential(credentialCached);
            builder.credential(fullyQualifiedDomainName, credential);
            if (httpClient != null) {
                builder.httpClient(httpClient);
            }
        } else {
            // Record Mode.
            final String connectionString = TestUtils.getConnectionString(false);
            if (CoreUtils.isNullOrEmpty(connectionString)) {
                final String fullyQualifiedDomainName = TestUtils.getFullyQualifiedDomainName(false);
                assumeTrue(!CoreUtils.isNullOrEmpty(fullyQualifiedDomainName), "FullyQualifiedDomainName is not set.");
                final TokenCredential credential = new DefaultAzureCredentialBuilder().build();
                builder.credential(fullyQualifiedDomainName, credential);
            } else {
                builder.connectionString(connectionString);
            }
            builder.httpClient(httpClient).addPolicy(interceptorManager.getRecordPolicy());
        }

        if (!interceptorManager.isLiveMode()) {
            interceptorManager.addSanitizers(TEST_PROXY_SANITIZERS);
            interceptorManager.addMatchers(TEST_PROXY_REQUEST_MATCHERS);
        }
    }
}
