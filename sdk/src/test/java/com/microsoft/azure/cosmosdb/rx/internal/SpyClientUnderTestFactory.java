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
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import com.microsoft.azure.cosmosdb.rx.SpyClientBuilder;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.client.RxClient;
import io.reactivex.netty.protocol.http.client.CompositeHttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doAnswer;

public class SpyClientUnderTestFactory {

    public static class ClientUnderTest extends RxDocumentClientImpl {

        private volatile boolean captureRequest = false;
        CompositeHttpClient<ByteBuf, ByteBuf> origHttpClient;
        CompositeHttpClient<ByteBuf, ByteBuf> spyHttpClient;
        List<HttpClientRequest<ByteBuf>> requests =
                Collections.synchronizedList(new ArrayList<HttpClientRequest<ByteBuf>>());

        ClientUnderTest(URI serviceEndpoint, String masterKey, ConnectionPolicy connectionPolicy, ConsistencyLevel consistencyLevel) {
            super(serviceEndpoint, masterKey, connectionPolicy, consistencyLevel, -1);
        }

        public List<HttpClientRequest<ByteBuf>> getCapturedRequests() {
            return requests;
        }

        void initRequestCapture(CompositeHttpClient<ByteBuf, ByteBuf> spyClient) {

            doAnswer(new Answer() {
                @Override
                public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                    RxClient.ServerInfo serverInfo = invocationOnMock.getArgumentAt(0, RxClient.ServerInfo.class);
                    HttpClientRequest<ByteBuf> httpReq = invocationOnMock.getArgumentAt(1, HttpClientRequest.class);
                    if (captureRequest) {
                        requests.add(httpReq);
                    }
                    return origHttpClient.submit(serverInfo, httpReq);
                }
            }).when(spyClient).submit(Mockito.any(RxClient.ServerInfo.class), Mockito.any(HttpClientRequest.class));
        }

        public void startCaptureRequests() {
            captureRequest = true;
        }

        public void clearCapturedRequests() {
            requests.clear();
        }

        public void stopCaptureRequests() {
            captureRequest = false;
        }

        public CompositeHttpClient<ByteBuf, ByteBuf> getSpyHttpClient() {
            return spyHttpClient;
        }
    }

    public ClientUnderTest createClientUnderTest(AsyncDocumentClient.Builder builder) {
        return new SpyClientBuilder(builder).build();
    }

    public static ClientUnderTest createClientUnderTest(URI serviceEndpoint,
                                                        String masterKey,
                                                        ConnectionPolicy connectionPolicy,
                                                        ConsistencyLevel consistencyLevel) {
        return new ClientUnderTest(serviceEndpoint, masterKey, connectionPolicy, consistencyLevel) {

            @Override
            RxGatewayStoreModel createRxGatewayProxy(ConnectionPolicy connectionPolicy,
                                                     ConsistencyLevel consistencyLevel,
                                                     QueryCompatibilityMode queryCompatibilityMode,
                                                     String masterKey, Map<String, String> resourceTokens,
                                                     UserAgentContainer userAgentContainer,
                                                     EndpointManager globalEndpointManager,
                                                     CompositeHttpClient<ByteBuf, ByteBuf> rxClient) {

                CompositeHttpClient<ByteBuf, ByteBuf> spyClient = Mockito.spy(rxClient);

                this.origHttpClient = rxClient;
                this.spyHttpClient = spyClient;

                this.initRequestCapture(spyHttpClient);

                return super.createRxGatewayProxy(
                        connectionPolicy,
                        consistencyLevel,
                        queryCompatibilityMode,
                        masterKey,
                        resourceTokens,
                        userAgentContainer,
                        globalEndpointManager,
                        spyClient);
            }
        };
    }

}
