// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import com.azure.core.amqp.Retry;
import com.azure.core.amqp.TransportType;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.eventhubs.CredentialInfo;
import com.azure.eventhubs.EventData;
import com.azure.eventhubs.EventHubClient;
import com.azure.eventhubs.EventHubClientBuilder;
import com.azure.eventhubs.EventSender;
import com.azure.eventhubs.EventSenderOptions;
import com.azure.eventhubs.ProxyConfiguration;
import org.apache.qpid.proton.reactor.Reactor;
import org.apache.qpid.proton.reactor.Selectable;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test base for running live and offline tests.
 */
public abstract class ApiTestBase extends TestBase {
    private static final String EVENT_HUB_CONNECTION_STRING_ENV_NAME = "AZURE_EVENTHUBS_CONNECTION_STRING";
    private static final String CONNECTION_STRING = System.getenv(EVENT_HUB_CONNECTION_STRING_ENV_NAME);
    private static final String TEST_CONNECTION_STRING = "Endpoint=sb://test-event-hub.servicebus.windows.net/;SharedAccessKeyName=dummySharedKeyName;SharedAccessKey=dummySharedKeyValue;EntityPath=eventhub1;";

    private CredentialInfo credentialInfo;
    private Reactor reactor = mock(Reactor.class);
    private TokenProvider tokenProvider = mock(TokenProvider.class);
    private ReactorProvider reactorProvider;
    private ConnectionParameters connectionParameters;

    @Override
    @Before
    public void setupTest() {
        final String connectionString;

        if (getTestMode() == TestMode.RECORD) {
            connectionString = CONNECTION_STRING;
            credentialInfo = CredentialInfo.from(connectionString);
            reactorProvider = new ReactorProvider();

            try {
                tokenProvider = new SharedAccessSignatureTokenProvider(credentialInfo.sharedAccessKeyName(), credentialInfo.sharedAccessKey());
            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                Assert.fail("Could not create tokenProvider :" + e.toString());
            }
        } else {
            connectionString = TEST_CONNECTION_STRING;
            credentialInfo = CredentialInfo.from(connectionString);

            when(reactor.selectable()).thenReturn(mock(Selectable.class));
            ReactorDispatcher reactorDispatcher = null;
            try {
                reactorDispatcher = new ReactorDispatcher(reactor);
            } catch (IOException e) {
                Assert.fail("Could not create dispatcher.");
            }
            reactorProvider = new MockReactorProvider(reactor, reactorDispatcher);
        }

        Scheduler scheduler = Schedulers.newElastic("AMQPConnection");

        connectionParameters = new ConnectionParameters(credentialInfo, Duration.ofSeconds(45), tokenProvider,
            TransportType.AMQP, Retry.getDefaultRetry(), ProxyConfiguration.SYSTEM_DEFAULTS, scheduler);

        // These are overridden because we don't use the Interceptor Manager.
        beforeTest();
    }

    @Override
    @After
    public void teardownTest() {
        // These are overridden because we don't use the Interceptor Manager.
        afterTest();
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

    protected ConnectionParameters getConnectionParameters() {
        return connectionParameters;
    }

    protected CredentialInfo getCredentialInfo() {
        return credentialInfo;
    }

    protected TokenProvider getTokenProvider() {
        return tokenProvider;
    }

    protected Reactor getReactor() {
        return reactor;
    }

    protected ReactorProvider getReactorProvider() {
        return reactorProvider;
    }


    protected EventHubClientBuilder getEventHubClientBuilder() {
        return EventHubClient.builder().credentials(CredentialInfo.from(CONNECTION_STRING));
    }

    protected static String getConsumerGroupName() {
        return "$Default";
    }

    protected static Mono<Void> pushEventsToPartition(final EventHubClient client, final String partitionId, final int noOfEvents) {
        EventSender sender = client.createSender(new EventSenderOptions().partitionId(partitionId));
        final Flux<EventData> map = Flux.range(0, noOfEvents).map(number -> {
            final EventData data = new EventData("testString".getBytes(UTF_8));
            return data;
        });
        return sender.send(map);
    }
}
