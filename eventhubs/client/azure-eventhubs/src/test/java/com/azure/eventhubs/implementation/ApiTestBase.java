// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import com.azure.core.amqp.TransportType;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.eventhubs.CredentialInfo;
import com.azure.eventhubs.ProxyConfiguration;
import com.azure.eventhubs.Retry;
import org.apache.qpid.proton.reactor.Reactor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

import static org.mockito.Mockito.mock;

/**
 * Test base for running live and offline tests.
 */
public abstract class ApiTestBase extends TestBase {
    private static final String EVENT_HUB_CONNECTION_STRING_ENV_NAME = "AZURE_EVENTHUBS_CONNECTION_STRING";
    private static final String CONNECTION_STRING = System.getenv(EVENT_HUB_CONNECTION_STRING_ENV_NAME);

    public static final String TEST_CONNECTION_STRING = "Endpoint=sb://test-event-hub.servicebus.windows.net/;SharedAccessKeyName=dummySharedKeyName;SharedAccessKey=dummySharedKeyValue;EntityPath=eventhub1;";

    private CredentialInfo credentialInfo;
    private ReactorHandlerProvider handlerProvider;
    private Reactor reactor = mock(Reactor.class);
    private ReactorDispatcher reactorDispatcher = mock(ReactorDispatcher.class);
    private TokenProvider tokenProvider = mock(TokenProvider.class);
    private ConnectionParameters connectionParameters;

    @Override
    @Before
    public void setupTest() {
        final String connectionString;
        final ReactorProvider provider;

        if (getTestMode() == TestMode.RECORD) {
            connectionString = CONNECTION_STRING;
            provider = new ReactorProvider();

            try {
                tokenProvider = new SharedAccessSignatureTokenProvider(credentialInfo.sharedAccessKeyName(), credentialInfo.sharedAccessKey());
            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                Assert.fail("Could not create tokenProvider :" + e.toString());
            }
        } else {
            connectionString = TEST_CONNECTION_STRING;
            provider = new MockReactorProvider(reactor, reactorDispatcher);
        }

        credentialInfo = CredentialInfo.from(connectionString);

        Scheduler scheduler = Schedulers.newElastic("AMQPConnection");
        handlerProvider = new ReactorHandlerProvider(provider);

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

    protected ReactorDispatcher getReactorDispatcher() {
        return reactorDispatcher;
    }

    protected ReactorHandlerProvider handlerProvider() {
        return handlerProvider;
    }
}
