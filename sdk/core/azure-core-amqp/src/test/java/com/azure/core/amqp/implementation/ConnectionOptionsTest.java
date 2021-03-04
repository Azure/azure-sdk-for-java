// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.implementation.handler.ConnectionHandler;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Header;
import org.apache.qpid.proton.engine.SslDomain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.scheduler.Scheduler;

import java.util.Arrays;

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

    @BeforeEach
    public void beforeEach() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void propertiesSet() {
        // Arrange
        final String productName = "test-product";
        final String clientVersion = "1.5.10";

        final String hostname = "host-name.com";
        final SslDomain.VerifyMode verifyMode = SslDomain.VerifyMode.VERIFY_PEER;
        final AmqpRetryOptions retryOptions = new AmqpRetryOptions();
        final ClientOptions clientOptions = new ClientOptions().setHeaders(Arrays.asList(
                new Header(ConnectionOptions.NAME_KEY, productName),
                new Header(ConnectionOptions.VERSION_KEY, clientVersion)));

        // Act
        final ConnectionOptions actual = new ConnectionOptions(hostname, tokenCredential,
            CbsAuthorizationType.JSON_WEB_TOKEN, AmqpTransportType.AMQP, retryOptions, ProxyOptions.SYSTEM_DEFAULTS,
            scheduler, clientOptions, verifyMode);

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
        assertEquals(retryOptions, actual.getRetry());
        assertEquals(verifyMode, actual.getSslVerifyMode());
    }

    /**
     * When there is no "name" or "version" in client options, the default {@link ConnectionOptions#UNKNOWN} is set.
     */
    @Test
    public void defaultSet() {
        // Arrange
        final String productName = "test-product";
        final String clientVersion = "1.5.10";

        final String hostname = "host-name.com";
        final SslDomain.VerifyMode verifyMode = SslDomain.VerifyMode.VERIFY_PEER;
        final AmqpRetryOptions retryOptions = new AmqpRetryOptions();
        final ClientOptions clientOptions = new ClientOptions().setHeaders(
            Arrays.asList(new Header("foo", productName), new Header("bar", clientVersion)));

        // Act
        final ConnectionOptions actual = new ConnectionOptions(hostname, tokenCredential,
            CbsAuthorizationType.JSON_WEB_TOKEN, AmqpTransportType.AMQP, retryOptions, ProxyOptions.SYSTEM_DEFAULTS,
            scheduler, clientOptions, verifyMode);

        // Assert
        assertEquals(ConnectionOptions.UNKNOWN, actual.getProduct());
        assertEquals(ConnectionOptions.UNKNOWN, actual.getClientVersion());
    }
}
