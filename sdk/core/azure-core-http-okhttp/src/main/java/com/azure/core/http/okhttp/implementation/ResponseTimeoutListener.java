// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.okhttp.implementation;

import okhttp3.Call;
import okhttp3.Connection;
import okhttp3.EventListener;
import okhttp3.Handshake;
import okhttp3.HttpUrl;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

/**
 * Implementation of {@link EventListener} that watches {@link #requestHeadersEnd(Call, Request)} and
 * {@link #requestBodyEnd(Call, long)} to detect when a request has completed being sent and
 * {@link #responseHeadersStart(Call)} and {@link #responseFailed(Call, IOException)} to detect when a response has
 * started being received. This is used to determine a response timeout for the call.
 * <p>
 * Both completing the request headers and the response body need to be watched as requests without a body will never
 * trigger the request body events.
 */
public class ResponseTimeoutListener extends EventListener {
    private final EventListener delegate;

    /**
     * Creates a new instance of ResponseTimeoutListener.
     *
     * @param delegate The {@link EventListener} to delegate to.
     */
    public ResponseTimeoutListener(EventListener delegate) {
        this.delegate = delegate;
    }

    @Override
    public void requestHeadersEnd(Call call, Request request) {
        PerCallTimeoutCall perCallTimeoutCall = request.tag(PerCallTimeoutCall.class);
        if (perCallTimeoutCall != null) {
            if (request.body() == null) {
                // Start the per call timeout when the request headers have been sent if there isn't a body.
                perCallTimeoutCall.beginPerCallTimeout(call);
            }
        }

        delegate.requestHeadersEnd(call, request);
    }

    @Override
    public void requestFailed(Call call, IOException ioe) {
        PerCallTimeoutCall perCallTimeoutCall = call.request().tag(PerCallTimeoutCall.class);
        if (perCallTimeoutCall != null) {
            // End the per call timeout if the request fails.
            perCallTimeoutCall.endPerCallTimeout();
        }

        delegate.requestFailed(call, ioe);
    }

    @Override
    public void requestBodyEnd(Call call, long byteCount) {
        PerCallTimeoutCall perCallTimeoutCall = call.request().tag(PerCallTimeoutCall.class);
        if (perCallTimeoutCall != null) {
            // Start the per call timeout when the request body has been sent.
            perCallTimeoutCall.beginPerCallTimeout(call);
        }
        delegate.requestBodyEnd(call, byteCount);
    }

    @Override
    public void responseHeadersStart(Call call) {
        PerCallTimeoutCall perCallTimeoutCall = call.request().tag(PerCallTimeoutCall.class);
        if (perCallTimeoutCall != null) {
            // End the per call timeout when the response headers have started being received.
            perCallTimeoutCall.endPerCallTimeout();
        }
        delegate.responseHeadersStart(call);
    }

    @Override
    public void responseFailed(Call call, IOException ioe) {
        PerCallTimeoutCall perCallTimeoutCall = call.request().tag(PerCallTimeoutCall.class);
        if (perCallTimeoutCall != null) {
            // End the per call timeout if the response fails.
            perCallTimeoutCall.endPerCallTimeout();
        }
        delegate.responseFailed(call, ioe);
    }

    @Override
    public void canceled(Call call) {
        PerCallTimeoutCall perCallTimeoutCall = call.request().tag(PerCallTimeoutCall.class);
        if (perCallTimeoutCall != null) {
            // End the per call timeout if the call is cancelled.
            perCallTimeoutCall.endPerCallTimeout();
        }
        delegate.canceled(call);
    }

    @Override
    public void cacheConditionalHit(Call call, Response cachedResponse) {
        delegate.cacheConditionalHit(call, cachedResponse);
    }

    @Override
    public void cacheHit(Call call, Response response) {
        delegate.cacheHit(call, response);
    }

    @Override
    public void cacheMiss(Call call) {
        delegate.cacheMiss(call);
    }

    @Override
    public void callEnd(Call call) {
        delegate.callEnd(call);
    }

    @Override
    public void callFailed(Call call, IOException ioe) {
        delegate.callFailed(call, ioe);
    }

    @Override
    public void callStart(Call call) {
        delegate.callStart(call);
    }

    @Override
    public void connectEnd(Call call, InetSocketAddress inetSocketAddress, Proxy proxy, Protocol protocol) {
        delegate.connectEnd(call, inetSocketAddress, proxy, protocol);
    }

    @Override
    public void connectFailed(Call call, InetSocketAddress inetSocketAddress, Proxy proxy, Protocol protocol,
        IOException ioe) {
        delegate.connectFailed(call, inetSocketAddress, proxy, protocol, ioe);
    }

    @Override
    public void connectStart(Call call, InetSocketAddress inetSocketAddress, Proxy proxy) {
        delegate.connectStart(call, inetSocketAddress, proxy);
    }

    @Override
    public void connectionAcquired(Call call, Connection connection) {
        delegate.connectionAcquired(call, connection);
    }

    @Override
    public void connectionReleased(Call call, Connection connection) {
        delegate.connectionReleased(call, connection);
    }

    @Override
    public void dnsEnd(Call call, String domainName, List<InetAddress> inetAddressList) {
        delegate.dnsEnd(call, domainName, inetAddressList);
    }

    @Override
    public void dnsStart(Call call, String domainName) {
        delegate.dnsStart(call, domainName);
    }

    @Override
    public void proxySelectEnd(Call call, HttpUrl url, List<Proxy> proxies) {
        delegate.proxySelectEnd(call, url, proxies);
    }

    @Override
    public void proxySelectStart(Call call, HttpUrl url) {
        delegate.proxySelectStart(call, url);
    }

    @Override
    public void requestHeadersStart(Call call) {
        delegate.requestHeadersStart(call);
    }

    @Override
    public void requestBodyStart(Call call) {
        delegate.requestBodyStart(call);
    }

    @Override
    public void responseBodyEnd(Call call, long byteCount) {
        delegate.responseBodyEnd(call, byteCount);
    }

    @Override
    public void responseBodyStart(Call call) {
        delegate.responseBodyStart(call);
    }

    @Override
    public void responseHeadersEnd(Call call, Response response) {
        delegate.responseHeadersEnd(call, response);
    }

    @Override
    public void satisfactionFailure(Call call, Response response) {
        delegate.satisfactionFailure(call, response);
    }

    @Override
    public void secureConnectEnd(Call call, Handshake handshake) {
        delegate.secureConnectEnd(call, handshake);
    }

    @Override
    public void secureConnectStart(Call call) {
        delegate.secureConnectStart(call);
    }
}
