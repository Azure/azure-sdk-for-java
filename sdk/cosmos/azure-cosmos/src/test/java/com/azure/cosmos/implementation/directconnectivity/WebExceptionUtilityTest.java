// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.fasterxml.jackson.databind.JsonMappingException;
import io.netty.channel.ChannelException;
import io.netty.channel.ConnectTimeoutException;
import io.netty.handler.timeout.ReadTimeoutException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.netty.http.client.PrematureCloseException;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.HttpRetryException;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.UnknownServiceException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.InterruptedByTimeoutException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * validation tests for {@link WebExceptionUtility}
 */
public class WebExceptionUtilityTest {

    @DataProvider(name = "exceptionToIsRetriable")
    public Object[][] exceptionToIsRetriable() {
        return new Object[][]{
                // exception, is retriable
                {
                        new RuntimeException(), false
                },
                {
                        new ConnectException(), true
                },
                {
                        new ConnectTimeoutException(), true
                },
                {
                        new UnknownHostException(), true
                },
                {
                        ReadTimeoutException.INSTANCE, false
                },
                {
                        new SSLHandshakeException("dummy"), true
                },
                {
                        new NoRouteToHostException(), true,
                },
                {
                        new SSLPeerUnverifiedException("dummy"), true
                },
                {
                        new SocketTimeoutException(), false
                },
                {
                        PrematureCloseException.TEST_EXCEPTION, false
                }
        };
    }

    @Test(groups = "unit", dataProvider = "exceptionToIsRetriable")
    public void isWebExceptionRetriable(Exception e, boolean isRetriable) {
        boolean actualRes = WebExceptionUtility.isWebExceptionRetriable(e);
        if (isRetriable) {
            assertThat(actualRes).describedAs(e.toString()).isTrue();
        } else {
            assertThat(actualRes).describedAs(e.toString()).isFalse();
        }
    }

    @DataProvider(name = "networkFailure")
    public Object[][] networkFailure() {
        return new Object[][]{
                // exception, is retriable
                {
                        new RuntimeException(), false
                },
                {
                        new ConnectException(), true
                },
                {
                        new ConnectTimeoutException(), true
                },
                {
                        new UnknownHostException(), true
                },
                {
                        ReadTimeoutException.INSTANCE, true
                },
                {
                        new SSLHandshakeException("dummy"), true
                },
                {
                        new NoRouteToHostException(), true,
                },
                {
                        new SSLPeerUnverifiedException("dummy"), true
                },
                {
                        new SocketTimeoutException(), true
                },
                {
                        new ChannelException(), true
                },
                {
                        new IOException(), false
                },
                {
                        new ClosedChannelException(), true
                },
                {
                        new SocketException(), true
                },
                {
                        new SSLException("dummy"), true
                },
                {
                        new UnknownServiceException(), true
                },
                {
                        new HttpRetryException("dummy", 500), true
                },
                {
                        new InterruptedByTimeoutException(), true
                },
                {
                        new InterruptedIOException(), true
                },
                {
                        new JsonMappingException(null, "dummy"), false
                },
                {
                        PrematureCloseException.TEST_EXCEPTION, true
                }
        };
    }

    @Test(groups = "unit", dataProvider = "networkFailure")
    public void isNetworkFailure(Exception e, boolean isNetworkFailure) {
        boolean actualRes = WebExceptionUtility.isNetworkFailure(e);
        if (isNetworkFailure) {
            assertThat(actualRes).describedAs(e.toString()).isTrue();
        } else {
            assertThat(actualRes).describedAs(e.toString()).isFalse();
        }
    }
}
