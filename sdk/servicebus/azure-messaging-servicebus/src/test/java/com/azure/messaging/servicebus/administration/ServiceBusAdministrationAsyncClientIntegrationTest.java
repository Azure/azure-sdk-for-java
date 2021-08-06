// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration;

import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.ResourceExistsException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.TestBase;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.messaging.servicebus.TestUtils;
import com.azure.messaging.servicebus.administration.models.AccessRights;
import com.azure.messaging.servicebus.administration.models.CreateQueueOptions;
import com.azure.messaging.servicebus.administration.models.CreateRuleOptions;
import com.azure.messaging.servicebus.administration.models.CreateSubscriptionOptions;
import com.azure.messaging.servicebus.administration.models.CreateTopicOptions;
import com.azure.messaging.servicebus.administration.models.EmptyRuleAction;
import com.azure.messaging.servicebus.administration.models.FalseRuleFilter;
import com.azure.messaging.servicebus.administration.models.NamespaceProperties;
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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
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
class ServiceBusAdministrationAsyncClientIntegrationTest extends TestBase {
    private static final Duration TIMEOUT = Duration.ofSeconds(20);

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    static Stream<Arguments> createHttpClients() {
        return Stream.of(
            Arguments.of(new NettyAsyncHttpClientBuilder().build())
        );
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    /**
     * Test to connect to the service bus with an azure identity TokenCredential.
     * com.azure.identity.ClientSecretCredential is used in this test.
     * ServiceBusSharedKeyCredential doesn't need a specific test method because other tests below
     * use connection string, which is converted to a ServiceBusSharedKeyCredential internally.
     */
    void azureIdentityCredentials(HttpClient httpClient) {
        assumeTrue(interceptorManager.isLiveMode(), "Azure Identity test is for live test only");
        final String fullyQualifiedDomainName = TestUtils.getFullyQualifiedDomainName();

        assumeTrue(fullyQualifiedDomainName != null && !fullyQualifiedDomainName.isEmpty(),
            "AZURE_SERVICEBUS_FULLY_QUALIFIED_DOMAIN_NAME variable needs to be set when using credentials.");

        final ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
            .clientId(System.getenv("AZURE_CLIENT_ID"))
            .clientSecret(System.getenv("AZURE_CLIENT_SECRET"))
            .tenantId(System.getenv("AZURE_TENANT_ID"))
            .build();
        ServiceBusAdministrationClient client = new ServiceBusAdministrationClientBuilder()
            .httpClient(httpClient)
            .credential(fullyQualifiedDomainName, clientSecretCredential)
            .buildClient();
        NamespaceProperties np = client.getNamespaceProperties();
        assertNotNull(np.getName());
    }

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
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void createQueueExistingName(HttpClient httpClient) {
        // Arrange
        final String queueName = interceptorManager.isPlaybackMode()
            ? "queue-5"
            : getEntityName(TestUtils.getQueueBaseName(), 5);
        final CreateQueueOptions options = new CreateQueueOptions();
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);

        // Act & Assert
        StepVerifier.create(client.createQueue(queueName, options))
            .expectError(ResourceExistsException.class)
            .verify();
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
            ? new SharedAccessAuthorizationRule(keyName, "Uobo65ke57pwWehaL9JzGXAK30MZgErqVyn5E+rHl1c=",
            "B4ENtK9Ze1nVMQ1mGdDsy9TuuQuGC4/K8q7OnPl8mn0=", accessRights)
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
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void createRule(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);

        final String ruleName = testResourceNamer.randomName("rule", 10);
        final String topicName = interceptorManager.isPlaybackMode()
            ? "topic-13"
            : getEntityName(getTopicBaseName(), 13);
        final String subscriptionName = interceptorManager.isPlaybackMode()
            ? "subscription"
            : getSubscriptionBaseName();
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
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void createRuleDefaults(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);

        final String ruleName = testResourceNamer.randomName("rule", 10);
        final String topicName = interceptorManager.isPlaybackMode()
            ? "topic-13"
            : getEntityName(getTopicBaseName(), 13);
        final String subscriptionName = interceptorManager.isPlaybackMode()
            ? "subscription"
            : getSubscriptionBaseName();

        // Act & Assert
        StepVerifier.create(client.createRule(topicName, subscriptionName, ruleName))
            .assertNext(contents -> {
                assertEquals(ruleName, contents.getName());
                assertTrue(contents.getFilter() instanceof TrueRuleFilter);
                assertTrue(contents.getAction() instanceof EmptyRuleAction);
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void createRuleResponse(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);

        final String ruleName = testResourceNamer.randomName("rule", 10);
        final String topicName = interceptorManager.isPlaybackMode()
            ? "topic-13"
            : getEntityName(getTopicBaseName(), 13);
        final String subscriptionName = interceptorManager.isPlaybackMode()
            ? "subscription"
            : getSubscriptionBaseName();
        final SqlRuleFilter filter = new SqlRuleFilter("sys.To=[parameters('bar')] OR sys.MessageId IS NULL");
        filter.getParameters().put("bar", "foo");
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
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void createSubscription(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = interceptorManager.isPlaybackMode()
            ? "topic-0"
            : getEntityName(getTopicBaseName(), 0);
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
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void createSubscriptionExistingName(HttpClient httpClient) {
        // Arrange
        final String topicName = interceptorManager.isPlaybackMode()
            ? "topic-1"
            : getEntityName(getTopicBaseName(), 1);
        final String subscriptionName = interceptorManager.isPlaybackMode()
            ? "subscription-session"
            : getSessionSubscriptionBaseName();
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);

        // Act & Assert
        StepVerifier.create(client.createSubscription(topicName, subscriptionName))
            .expectError(ResourceExistsException.class)
            .verify();
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
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void deleteQueue(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String queueName = testResourceNamer.randomName("sub", 10);

        client.createQueue(queueName).block(TIMEOUT);

        // Act & Assert
        StepVerifier.create(client.deleteQueue(queueName))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void deleteRule(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String ruleName = testResourceNamer.randomName("rule-", 11);
        final String topicName = interceptorManager.isPlaybackMode()
            ? "topic-13"
            : getEntityName(getTopicBaseName(), 13);
        final String subscriptionName = interceptorManager.isPlaybackMode()
            ? "subscription"
            : getSubscriptionBaseName();

        client.createRule(topicName, subscriptionName, ruleName).block(TIMEOUT);

        // Act & Assert
        StepVerifier.create(client.deleteRule(topicName, subscriptionName, ruleName))
            .verifyComplete();
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
            .verifyComplete();
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
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getQueue(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String queueName = interceptorManager.isPlaybackMode()
            ? "queue-5"
            : getEntityName(TestUtils.getQueueBaseName(), 5);
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
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getNamespace(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String expectedName;
        if (interceptorManager.isPlaybackMode()) {
            expectedName = "ShivangiServiceBus";
        } else {
            final String[] split = TestUtils.getFullyQualifiedDomainName().split("\\.", 2);
            expectedName = split[0];
        }

        // Act & Assert
        StepVerifier.create(client.getNamespaceProperties())
            .assertNext(properties -> {
                assertEquals(NamespaceType.MESSAGING, properties.getNamespaceType());
                assertEquals(expectedName, properties.getName());
            })
            .verifyComplete();
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
            .verify();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getQueueExists(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String queueName = interceptorManager.isPlaybackMode()
            ? "queue-2"
            : getEntityName(TestUtils.getQueueBaseName(), 2);

        // Act & Assert
        StepVerifier.create(client.getQueueExists(queueName))
            .expectNext(true)
            .verifyComplete();
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
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getQueueRuntimeProperties(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String queueName = interceptorManager.isPlaybackMode()
            ? "queue-2"
            : getEntityName(TestUtils.getQueueBaseName(), 2);
        final OffsetDateTime nowUtc = OffsetDateTime.now(Clock.systemUTC());

        // Act & Assert
        StepVerifier.create(client.getQueueRuntimeProperties(queueName))
            .assertNext(RuntimeProperties -> {
                assertEquals(queueName, RuntimeProperties.getName());

                assertNotNull(RuntimeProperties.getCreatedAt());
                assertTrue(nowUtc.isAfter(RuntimeProperties.getCreatedAt()));
                assertNotNull(RuntimeProperties.getAccessedAt());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getRule(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);

        // There is a single default rule created.
        final String ruleName = "$Default";
        final String topicName = interceptorManager.isPlaybackMode()
            ? "topic-13"
            : getEntityName(getTopicBaseName(), 13);
        final String subscriptionName = interceptorManager.isPlaybackMode()
            ? "subscription"
            : getSubscriptionBaseName();

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
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getSubscription(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = interceptorManager.isPlaybackMode() ? "topic-1" : getEntityName(getTopicBaseName(), 1);
        final String subscriptionName = interceptorManager.isPlaybackMode()
            ? "subscription-session"
            : getSessionSubscriptionBaseName();
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
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getSubscriptionDoesNotExist(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = interceptorManager.isPlaybackMode() ? "topic-1" : getEntityName(getTopicBaseName(), 1);
        final String subscriptionName = "subscription-session-not-exist";

        // Act & Assert
        StepVerifier.create(client.getSubscription(topicName, subscriptionName))
            .expectError(ResourceNotFoundException.class)
            .verify();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getSubscriptionExists(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = interceptorManager.isPlaybackMode()
            ? "topic-1"
            : getEntityName(getTopicBaseName(), 1);
        final String subscriptionName = interceptorManager.isPlaybackMode()
            ? "subscription-session"
            : getSessionSubscriptionBaseName();

        // Act & Assert
        StepVerifier.create(client.getSubscriptionExists(topicName, subscriptionName))
            .expectNext(true)
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getSubscriptionExistsFalse(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = interceptorManager.isPlaybackMode() ? "topic-1" : getEntityName(getTopicBaseName(), 1);
        final String subscriptionName = "subscription-session-not-exist";

        // Act & Assert
        StepVerifier.create(client.getSubscriptionExists(topicName, subscriptionName))
            .expectNext(false)
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getSubscriptionRuntimeProperties(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = interceptorManager.isPlaybackMode() ? "topic-1" : getEntityName(getTopicBaseName(), 1);
        final String subscriptionName = interceptorManager.isPlaybackMode()
            ? "subscription-session"
            : getSessionSubscriptionBaseName();
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
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getTopic(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = interceptorManager.isPlaybackMode()
            ? "topic-1"
            : getEntityName(getTopicBaseName(), 1);
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
            .verifyComplete();
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
            .verify();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getTopicExists(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = interceptorManager.isPlaybackMode()
            ? "topic-1"
            : getEntityName(getTopicBaseName(), 1);

        // Act & Assert
        StepVerifier.create(client.getTopicExists(topicName))
            .expectNext(true)
            .verifyComplete();
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
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void getTopicRuntimeProperties(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = interceptorManager.isPlaybackMode()
            ? "topic-1"
            : getEntityName(getTopicBaseName(), 1);
        final OffsetDateTime nowUtc = OffsetDateTime.now(Clock.systemUTC());

        // Act & Assert
        StepVerifier.create(client.getTopicRuntimeProperties(topicName))
            .assertNext(RuntimeProperties -> {
                assertEquals(topicName, RuntimeProperties.getName());

                if (interceptorManager.isPlaybackMode()) {
                    assertEquals(3, RuntimeProperties.getSubscriptionCount());
                } else {
                    assertTrue(RuntimeProperties.getSubscriptionCount() > 1);
                }

                assertNotNull(RuntimeProperties.getCreatedAt());
                assertTrue(nowUtc.isAfter(RuntimeProperties.getCreatedAt()));
                assertNotNull(RuntimeProperties.getAccessedAt());
                assertTrue(nowUtc.isAfter(RuntimeProperties.getAccessedAt()));
                assertEquals(0, RuntimeProperties.getScheduledMessageCount());

            })
            .verifyComplete();
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
            builder.httpClient(httpClient)
                .addPolicy(new RetryPolicy());
        } else {
            builder.httpClient(httpClient)
                .addPolicy(interceptorManager.getRecordPolicy())
                .addPolicy(new RetryPolicy());
        }

        final ServiceBusAdministrationAsyncClient client = builder.buildAsyncClient();

        final String topicName = interceptorManager.isPlaybackMode()
            ? "topic-1"
            : getEntityName(getTopicBaseName(), 1);
        final String subscriptionName = interceptorManager.isPlaybackMode()
            ? "subscription"
            : getSubscriptionBaseName();

        // Act & Assert
        StepVerifier.create(client.getSubscriptionRuntimeProperties(topicName, subscriptionName))
            .verifyErrorMatches(throwable -> throwable instanceof ClientAuthenticationException);
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void listRules(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);

        // There is a single default rule created.
        final String ruleName = "$Default";
        final String topicName = interceptorManager.isPlaybackMode()
            ? "topic-13"
            : getEntityName(getTopicBaseName(), 13);
        final String subscriptionName = interceptorManager.isPlaybackMode()
            ? "subscription"
            : getSubscriptionBaseName();

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
            .verify();
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
            .verify();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void listSubscriptions(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);
        final String topicName = interceptorManager.isPlaybackMode()
            ? "topic-1"
            : getEntityName(getTopicBaseName(), 1);

        // Act & Assert
        StepVerifier.create(client.listSubscriptions(topicName))
            .assertNext(subscription -> {
                assertEquals(topicName, subscription.getTopicName());
                assertNotNull(subscription.getSubscriptionName());
            })
            .expectNextCount(1)
            .thenCancel()
            .verify();
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
            .verify();
    }

    @ParameterizedTest
    @MethodSource("createHttpClients")
    void updateRuleResponse(HttpClient httpClient) {
        // Arrange
        final ServiceBusAdministrationAsyncClient client = createClient(httpClient);

        final String ruleName = testResourceNamer.randomName("rule", 15);
        final String topicName = interceptorManager.isPlaybackMode()
            ? "topic-12"
            : getEntityName(getTopicBaseName(), 12);
        final String subscriptionName = interceptorManager.isPlaybackMode()
            ? "subscription"
            : getSubscriptionBaseName();
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
            .verifyComplete();
    }

    private ServiceBusAdministrationAsyncClient createClient(HttpClient httpClient) {
        final String connectionString = interceptorManager.isPlaybackMode()
            ? "Endpoint=sb://foo.servicebus.windows.net;SharedAccessKeyName=dummyKey;SharedAccessKey=dummyAccessKey"
            : TestUtils.getConnectionString(false);

        final ServiceBusAdministrationClientBuilder builder = new ServiceBusAdministrationClientBuilder()
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .connectionString(connectionString);

        if (interceptorManager.isPlaybackMode()) {
            builder.httpClient(interceptorManager.getPlaybackClient());
        } else if (interceptorManager.isLiveMode()) {
            builder.httpClient(httpClient)
                .addPolicy(new RetryPolicy());
        } else {
            builder.httpClient(httpClient)
                .addPolicy(interceptorManager.getRecordPolicy())
                .addPolicy(new RetryPolicy());
        }

        return builder.buildAsyncClient();
    }

}
