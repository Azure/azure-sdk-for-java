/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.microsoft.azure.cosmosdb.rx.internal;

import com.microsoft.azure.cosmosdb.ConnectionPolicy;
import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.internal.EndpointManager;
import com.microsoft.azure.cosmosdb.internal.QueryCompatibilityMode;
import com.microsoft.azure.cosmosdb.internal.UserAgentContainer;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.client.RxClient;
import io.reactivex.netty.protocol.http.client.CompositeHttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import rx.Observable;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doAnswer;

/**
 * This class in conjunction with {@link com.microsoft.azure.cosmosdb.rx.ClientUnderTestBuilder}
 * provides the functionality for spying the client behavoiur and the http requests sent.
 */
public class RxDocumentClientUnderTest extends RxDocumentClientImpl {

    public CompositeHttpClient<ByteBuf, ByteBuf> spyHttpClient;
    public CompositeHttpClient<ByteBuf, ByteBuf> origHttpClient;

    public List<HttpClientRequest<ByteBuf>> httpRequests = Collections.synchronizedList(
            new ArrayList<HttpClientRequest<ByteBuf>>());

    public RxDocumentClientUnderTest(URI serviceEndpoint,
                                     String masterKey,
                                     ConnectionPolicy connectionPolicy,
                                     ConsistencyLevel consistencyLevel,
                                     int eventLoopSize) {
        super(serviceEndpoint, masterKey, connectionPolicy, consistencyLevel, eventLoopSize);
    }

    RxGatewayStoreModel createRxGatewayProxy(
            ConnectionPolicy connectionPolicy,
            ConsistencyLevel consistencyLevel,
            QueryCompatibilityMode queryCompatibilityMode,
            String masterKey, Map<String, String> resourceTokens,
            UserAgentContainer userAgentContainer,
            EndpointManager globalEndpointManager,
            CompositeHttpClient<ByteBuf, ByteBuf> rxOrigClient) {

        origHttpClient = rxOrigClient;
        spyHttpClient = Mockito.spy(rxOrigClient);

        doAnswer((Answer<Observable<HttpClientResponse<ByteBuf>>>) invocationOnMock -> {

            RxClient.ServerInfo serverInfo =
                    invocationOnMock.getArgumentAt(0, RxClient.ServerInfo.class);

            HttpClientRequest<ByteBuf> request
                    = invocationOnMock.getArgumentAt(1, HttpClientRequest.class);

            httpRequests.add(request);

            Observable<HttpClientResponse<ByteBuf>> httpRespObs =
                    origHttpClient.submit(serverInfo, request);

            return httpRespObs;
        }).when(spyHttpClient).submit( anyObject(),
                (HttpClientRequest) anyObject());

        return super.createRxGatewayProxy(connectionPolicy,
                consistencyLevel,
                queryCompatibilityMode,
                masterKey,
                resourceTokens,
                userAgentContainer,
                globalEndpointManager,
                spyHttpClient);
    }
}
