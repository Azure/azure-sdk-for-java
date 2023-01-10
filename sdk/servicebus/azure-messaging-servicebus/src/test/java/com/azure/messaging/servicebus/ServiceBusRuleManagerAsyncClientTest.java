// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpShutdownSignal;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.RecoverableReactorConnection;
import com.azure.core.amqp.models.CbsAuthorizationType;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.administration.models.CreateRuleOptions;
import com.azure.messaging.servicebus.administration.models.RuleProperties;
import com.azure.messaging.servicebus.administration.models.SqlRuleFilter;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.implementation.ServiceBusConstants;
import com.azure.messaging.servicebus.implementation.ServiceBusManagementNode;
import com.azure.messaging.servicebus.implementation.ServiceBusReactorAmqpConnection;
import org.apache.qpid.proton.engine.SslDomain;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.HashMap;

import static com.azure.core.amqp.implementation.RetryUtil.getRetryPolicy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ServiceBusRuleManagerAsyncClientTest {
    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusRuleManagerAsyncClientTest.class);

    private static final ClientOptions CLIENT_OPTIONS = new ClientOptions();
    private static final String NAMESPACE = "contoso-shopping.servicebus.windows.net";
    private static final String ENTITY_PATH = "topic-orders/subscriptions/subscription-ingesting";
    private static final MessagingEntityType ENTITY_TYPE = MessagingEntityType.SUBSCRIPTION;
    private static final String RULE_NAME = "foo-bar";

    private ServiceBusRuleManagerAsyncClient ruleManager;
    private RecoverableReactorConnection<ServiceBusReactorAmqpConnection> recoverableConnection;

    private AutoCloseable mocksCloseable;
    private CreateRuleOptions ruleOptions;

    @Mock
    private SqlRuleFilter ruleFilter;

    @Mock
    private TokenCredential tokenCredential;

    @Mock
    private ServiceBusReactorAmqpConnection connection;

    @Mock
    private ServiceBusManagementNode managementNode;

    @Mock
    private Runnable onClientClose;

    @Mock
    private RuleProperties ruleProperties1;

    @Mock
    private RuleProperties ruleProperties2;

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(100));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @BeforeEach
    void setup(TestInfo testInfo) {
        LOGGER.info("[{}] Setting up.", testInfo.getDisplayName());
        mocksCloseable = MockitoAnnotations.openMocks(this);

        ConnectionOptions connectionOptions = new ConnectionOptions(NAMESPACE, tokenCredential,
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, ServiceBusConstants.AZURE_ACTIVE_DIRECTORY_SCOPE,
            AmqpTransportType.AMQP, new AmqpRetryOptions(), ProxyOptions.SYSTEM_DEFAULTS, Schedulers.boundedElastic(),
            CLIENT_OPTIONS, SslDomain.VerifyMode.VERIFY_PEER_NAME, "test-product", "test-version");

        ruleOptions = new CreateRuleOptions(ruleFilter);

        when(connection.connectAndAwaitToActive()).thenReturn(Mono.just(connection));

        when(connection.getManagementNode(ENTITY_PATH, ENTITY_TYPE))
            .thenReturn(Mono.just(managementNode));
        when(connection.isDisposed()).thenReturn(false);
        when(connection.closeAsync(any(AmqpShutdownSignal.class))).thenReturn(Mono.empty());

        recoverableConnection = new RecoverableReactorConnection<>(() -> connection,
            connectionOptions.getFullyQualifiedNamespace(), ENTITY_PATH, getRetryPolicy(connectionOptions.getRetry()),
            new HashMap<>());

        ruleManager = new ServiceBusRuleManagerAsyncClient(ENTITY_PATH, ENTITY_TYPE, recoverableConnection, onClientClose);

    }

    @AfterEach
    void teardown(TestInfo testInfo) throws Exception {
        LOGGER.info("[{}] Tearing down.", testInfo.getDisplayName());

        ruleManager.close();
        Mockito.framework().clearInlineMock(this);
        mocksCloseable.close();
    }

    /**
     * Verifies that create a rule with a {@link CreateRuleOptions}.
     */
    @Test
    void createRuleWithOptions() {
        // Arrange
        when(managementNode.createRule(RULE_NAME, ruleOptions)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(ruleManager.createRule(RULE_NAME, ruleOptions))
            .verifyComplete();
    }

    @Test
    void getRules() {
        // Arrange
        when(managementNode.listRules()).thenReturn(Flux.fromArray(new RuleProperties[]{ruleProperties1, ruleProperties2}));

        // Act & Assert
        StepVerifier.create(ruleManager.listRules()).expectNext(ruleProperties1, ruleProperties2).verifyComplete();
    }

    @Test
    void deleteRule() {
        // Arrange
        when(managementNode.deleteRule(RULE_NAME)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(ruleManager.deleteRule(RULE_NAME)).verifyComplete();
    }
}
