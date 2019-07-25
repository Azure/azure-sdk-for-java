// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.internal.Utils;
import io.netty.channel.ChannelException;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;

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
        if (ex instanceof IOException) {
            return true;
        }

        if (ex instanceof ChannelException) {
            return true;
        }

        return false;
    }
}
