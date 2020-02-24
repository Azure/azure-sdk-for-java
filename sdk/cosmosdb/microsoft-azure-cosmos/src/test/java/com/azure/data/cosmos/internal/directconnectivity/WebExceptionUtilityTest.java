// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.internal.directconnectivity.WebExceptionUtility;
import io.netty.channel.ChannelException;
import io.netty.channel.ConnectTimeoutException;
import io.netty.handler.timeout.ReadTimeoutException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

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
