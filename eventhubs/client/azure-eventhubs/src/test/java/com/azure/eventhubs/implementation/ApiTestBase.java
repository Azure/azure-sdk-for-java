// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import com.azure.core.amqp.Retry;
import com.azure.core.amqp.TransportType;
import com.azure.core.credentials.TokenCredential;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.eventhubs.EventHubSharedAccessKeyCredential;
import com.azure.eventhubs.EventHubClient;
import com.azure.eventhubs.EventHubClientBuilder;
import com.azure.eventhubs.ProxyConfiguration;
import org.apache.qpid.proton.reactor.Reactor;
import org.apache.qpid.proton.reactor.Selectable;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.mockito.Mockito;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test base for running live and offline tests.
 */
public abstract class ApiTestBase extends TestBase {
    private static final String EVENT_HUB_CONNECTION_STRING_ENV_NAME = "AZURE_EVENTHUBS_CONNECTION_STRING";
    private static final String CONNECTION_STRING = System.getenv(EVENT_HUB_CONNECTION_STRING_ENV_NAME);
    private static final String TEST_CONNECTION_STRING = "Endpoint=sb://test-event-hub.servicebus.windows.net/;SharedAccessKeyName=myaccount;SharedAccessKey=ctzMq410TV3wS7upTBcunJTDLEJwMAZuFPfr0mrrA08=;EntityPath=eventhub1;";

    private ConnectionStringProperties properties;
    private Reactor reactor = mock(Reactor.class);
    private TokenCredential tokenCredential;
    private ReactorProvider reactorProvider;
    private ConnectionOptions connectionOptions;

    // These are overridden because we don't use the Interceptor Manager.
    @Override
    @Before
    public void setupTest() {
        final Scheduler scheduler = Schedulers.newElastic("AMQPConnection");
        final String connectionString = getTestMode() == TestMode.RECORD
            ? CONNECTION_STRING
            : TEST_CONNECTION_STRING;

        properties = new ConnectionStringProperties(connectionString);
        reactorProvider = new ReactorProvider();

        try {
            tokenCredential = new EventHubSharedAccessKeyCredential(properties.sharedAccessKeyName(),
                properties.sharedAccessKey(), ClientConstants.TOKEN_VALIDITY);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            Assert.fail("Could not create tokenProvider :" + e.toString());
        }

        if (getTestMode() != TestMode.RECORD) {
            when(reactor.selectable()).thenReturn(mock(Selectable.class));
            ReactorDispatcher reactorDispatcher = null;
            try {
                reactorDispatcher = new ReactorDispatcher(reactor);
            } catch (IOException e) {
                Assert.fail("Could not create dispatcher.");
            }
            reactorProvider = new MockReactorProvider(reactor, reactorDispatcher);
        }

        connectionOptions = new ConnectionOptions(properties.endpoint().getHost(), properties.eventHubPath(),
            tokenCredential, getAuthorizationType(), Duration.ofSeconds(45), TransportType.AMQP,
            Retry.getDefaultRetry(), ProxyConfiguration.SYSTEM_DEFAULTS, scheduler);

        beforeTest();
    }

    // These are overridden because we don't use the Interceptor Manager.
    @Override
    @After
    public void teardownTest() {
        afterTest();

        // Tear down any inline mocks to avoid memory leaks.
        // https://github.com/mockito/mockito/wiki/What's-new-in-Mockito-2#mockito-2250
        Mockito.framework().clearInlineMocks();
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

        return ImplUtils.isNullOrEmpty(CONNECTION_STRING) ? TestMode.PLAYBACK : TestMode.RECORD;
    }

    protected String getConnectionString() {
        return getTestMode() == TestMode.RECORD ? CONNECTION_STRING : TEST_CONNECTION_STRING;
    }

    protected void skipIfNotRecordMode() {
        Assume.assumeTrue(getTestMode() == TestMode.RECORD);
    }

    protected ConnectionOptions getConnectionOptions() {
        return connectionOptions;
    }

    protected ConnectionStringProperties getConnectionStringProperties() {
        return properties;
    }

    protected TokenCredential getTokenCredential() {
        return tokenCredential;
    }

    protected Reactor getReactor() {
        return reactor;
    }

    protected ReactorProvider getReactorProvider() {
        return reactorProvider;
    }

    protected CBSAuthorizationType getAuthorizationType() {
        return CBSAuthorizationType.SHARED_ACCESS_SIGNATURE;
    }

    protected EventHubClientBuilder getEventHubClientBuilder() {
        return EventHubClient.builder().credential(TEST_CONNECTION_STRING);
    }

    protected String getConsumerGroupName() {
        return "$Default";
    }
}
