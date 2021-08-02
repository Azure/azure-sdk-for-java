// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.Utils;
import io.netty.channel.ChannelException;
import io.netty.handler.timeout.ReadTimeoutException;
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
import java.net.UnknownHostException;
import java.net.UnknownServiceException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.InterruptedByTimeoutException;

public class WebExceptionUtility {
    public static boolean isWebExceptionRetriable(Exception ex) {
        Exception iterator = ex;

        while (iterator != null) {
            if (WebExceptionUtility.isWebExceptionRetriableInternal(iterator)) {
                return true;
            }

            Throwable t = iterator.getCause();
            iterator = Utils.as(t, Exception.class);
        }

        return false;
    }

    private static boolean isWebExceptionRetriableInternal(Exception ex) {

        IOException webEx = Utils.as(ex, IOException.class);
        if (webEx == null) {
            return false;
        }

        // any network failure for which we are certain the request hasn't reached the service endpoint.
        if (webEx instanceof ConnectException ||
                webEx instanceof UnknownHostException ||
                webEx instanceof SSLHandshakeException ||
                webEx instanceof NoRouteToHostException ||
                webEx instanceof SSLPeerUnverifiedException) {
            return true;
        }

        return false;
    }

    public static boolean isNetworkFailure(Exception ex) {
        Exception iterator = ex;

        while (iterator != null) {
            if (WebExceptionUtility.isNetworkFailureInternal(iterator)) {
                return true;
            }

            Throwable t = iterator.getCause();
            iterator = Utils.as(t, Exception.class);
        }

        return false;
    }

    private static boolean isNetworkFailureInternal(Exception ex) {
        //  We have seen these cases in CRIs
        if (ex instanceof ClosedChannelException
            || ex instanceof SocketException
            || ex instanceof SSLException
            || ex instanceof UnknownHostException
            || ex instanceof PrematureCloseException) {
            return true;
        }

        //  These cases might be related, but we haven't seen them ever
        if (ex instanceof UnknownServiceException
            || ex instanceof HttpRetryException
            || ex instanceof InterruptedByTimeoutException
            || ex instanceof InterruptedIOException) {
            return true;
        }

        if (ex instanceof ChannelException) {
            return true;
        }

        return false;
    }

    public static boolean isReadTimeoutException(Exception ex) {
        Exception iterator = ex;

        while (iterator != null) {
            if (WebExceptionUtility.isReadTimeoutExceptionInternal(iterator)) {
                return true;
            }

            Throwable t = iterator.getCause();
            iterator = Utils.as(t, Exception.class);
        }

        return false;
    }

    private static boolean isReadTimeoutExceptionInternal(Exception ex) {
        if (ex instanceof ReadTimeoutException) {
            return true;
        }

        return false;
    }
}
