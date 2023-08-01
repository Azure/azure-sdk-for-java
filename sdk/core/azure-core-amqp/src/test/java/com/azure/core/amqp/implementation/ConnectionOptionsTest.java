// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.implementation.handler.ConnectionHandler;
import com.azure.core.amqp.models.CbsAuthorizationType;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.ClientOptions;
import org.apache.qpid.proton.engine.SslDomain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.scheduler.Scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tests for {@link ConnectionOptions}.
 */
public class ConnectionOptionsTest {
    @Mock
    private TokenCredential tokenCredential;
    @Mock
    private Scheduler scheduler;
    private AutoCloseable mocksCloseable;

    @BeforeEach
    public void beforeEach() {
        mocksCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void afterEach() throws Exception {
        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    @Test
    public void propertiesSet() {
        // Arrange
        final String productName = "test-product";
        final String clientVersion = "1.5.10";
        final String scope = "test-scope";

        final String hostname = "host-name.com";
        final SslDomain.VerifyMode verifyMode = SslDomain.VerifyMode.VERIFY_PEER;
        final AmqpRetryOptions retryOptions = new AmqpRetryOptions();
        final ClientOptions clientOptions = new ClientOptions();

        // Act
        final ConnectionOptions actual = new ConnectionOptions(hostname, tokenCredential,
            CbsAuthorizationType.JSON_WEB_TOKEN, scope, AmqpTransportType.AMQP, retryOptions,
            ProxyOptions.SYSTEM_DEFAULTS, scheduler, clientOptions, verifyMode, productName, clientVersion);

        // Assert
        assertEquals(hostname, actual.getHostname());
        assertEquals(ConnectionHandler.AMQPS_PORT, actual.getPort());
        assertEquals(productName, actual.getProduct());
        assertEquals(clientVersion, actual.getClientVersion());

        assertSame(clientOptions, actual.getClientOptions());

        assertEquals(AmqpTransportType.AMQP, actual.getTransportType());
        assertEquals(scheduler, actual.getScheduler());

        assertEquals(tokenCredential, actual.getTokenCredential());
        assertEquals(CbsAuthorizationType.JSON_WEB_TOKEN, actual.getAuthorizationType());
        assertEquals(scope, actual.getAuthorizationScope());
        assertEquals(retryOptions, actual.getRetry());
        assertEquals(verifyMode, actual.getSslVerifyMode());
    }
}
