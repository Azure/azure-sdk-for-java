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

import com.microsoft.azure.cosmosdb.ConnectionMode;
import com.microsoft.azure.cosmosdb.ConnectionPolicy;
import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.ISessionContainer;
import com.microsoft.azure.cosmosdb.internal.QueryCompatibilityMode;
import com.microsoft.azure.cosmosdb.internal.UserAgentContainer;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import com.microsoft.azure.cosmosdb.rx.SpyClientBuilder;
import com.microsoft.azure.cosmosdb.rx.internal.directconnectivity.ReflectionUtils;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.client.RxClient;
import io.reactivex.netty.protocol.http.client.CompositeHttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpResponseHeaders;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static org.mockito.Mockito.doAnswer;

public class SpyClientUnderTestFactory {

    public static abstract class SpyBaseClass<T> extends RxDocumentClientImpl {

        public SpyBaseClass(URI serviceEndpoint, String masterKeyOrResourceToken, ConnectionPolicy connectionPolicy, ConsistencyLevel consistencyLevel, Configs configs) {
            super(serviceEndpoint, masterKeyOrResourceToken, connectionPolicy, consistencyLevel, configs);
        }
        
        public abstract List<T> getCapturedRequests();
        
        public abstract void clearCapturedRequests();
    }
    
    public static class ClientWithGatewaySpy extends SpyBaseClass<RxDocumentServiceRequest> {

        private RxGatewayStoreModel origRxGatewayStoreModel;
        private RxGatewayStoreModel spyRxGatewayStoreModel;

        private List<RxDocumentServiceRequest> requests;


        ClientWithGatewaySpy(URI serviceEndpoint, String masterKey, ConnectionPolicy connectionPolicy, ConsistencyLevel consistencyLevel, Configs configs) {
            super(serviceEndpoint, masterKey, connectionPolicy, consistencyLevel, configs);
            init();
        }

        @Override
        public List<RxDocumentServiceRequest> getCapturedRequests() {
            return requests;
        }

        @Override
        RxGatewayStoreModel createRxGatewayProxy(ISessionContainer sessionContainer,
                                                 ConsistencyLevel consistencyLevel,
                                                 QueryCompatibilityMode queryCompatibilityMode,
                                                 UserAgentContainer userAgentContainer,
                                                 GlobalEndpointManager globalEndpointManager,
                                                 CompositeHttpClient<ByteBuf, ByteBuf> rxClient) {
            this.origRxGatewayStoreModel = super.createRxGatewayProxy(
                    sessionContainer,
                    consistencyLevel,
                    queryCompatibilityMode,
                    userAgentContainer,
                    globalEndpointManager,
                    rxClient);
            this.requests = Collections.synchronizedList(new ArrayList<>());
            this.spyRxGatewayStoreModel = Mockito.spy(this.origRxGatewayStoreModel);
            this.initRequestCapture();
            return this.spyRxGatewayStoreModel;
        }

        protected void initRequestCapture() {
            doAnswer(new Answer() {
                @Override
                public Object answer(InvocationOnMock invocationOnMock)  {
                    RxDocumentServiceRequest req = invocationOnMock.getArgumentAt(0, RxDocumentServiceRequest.class);
                    requests.add(req);
                    return ClientWithGatewaySpy.this.origRxGatewayStoreModel.processMessage(req);
                }
            }).when(ClientWithGatewaySpy.this.spyRxGatewayStoreModel).processMessage(Mockito.any(RxDocumentServiceRequest.class));
        }

        @Override
        public void clearCapturedRequests() {
            requests.clear();
        }

        public RxGatewayStoreModel getSpyGatewayStoreModel() {
            return spyRxGatewayStoreModel;
        }

        public RxGatewayStoreModel getOrigGatewayStoreModel() {
            return origRxGatewayStoreModel;
        }
    }

    public static class ClientUnderTest extends SpyBaseClass<HttpClientRequest<ByteBuf>> {

        CompositeHttpClient<ByteBuf, ByteBuf> origHttpClient;
        CompositeHttpClient<ByteBuf, ByteBuf> spyHttpClient;
        List<Pair<HttpClientRequest<ByteBuf>, Future<HttpResponseHeaders>>> requestsResponsePairs =
                Collections.synchronizedList(new ArrayList<Pair<HttpClientRequest<ByteBuf>, Future<HttpResponseHeaders>>>());

        ClientUnderTest(URI serviceEndpoint, String masterKey, ConnectionPolicy connectionPolicy, ConsistencyLevel consistencyLevel, Configs configs) {
            super(serviceEndpoint, masterKey, connectionPolicy, consistencyLevel, configs);
            init();
        }

        public List<Pair<HttpClientRequest<ByteBuf>, Future<HttpResponseHeaders>>> capturedRequestResponseHeaderPairs() {
            return requestsResponsePairs;
        }

        @Override
        public List<HttpClientRequest<ByteBuf>> getCapturedRequests() {
            return requestsResponsePairs.stream().map(pair -> pair.getLeft()).collect(Collectors.toList());
        }

        void initRequestCapture(CompositeHttpClient<ByteBuf, ByteBuf> spyClient) {

            doAnswer(new Answer() {
                @Override
                public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                    RxClient.ServerInfo serverInfo = invocationOnMock.getArgumentAt(0, RxClient.ServerInfo.class);
                    HttpClientRequest<ByteBuf> httpReq = invocationOnMock.getArgumentAt(1, HttpClientRequest.class);

                    CompletableFuture<HttpResponseHeaders> f = new CompletableFuture<>();
                    requestsResponsePairs.add(Pair.of(httpReq, f));

                    return origHttpClient.submit(serverInfo, httpReq)
                            .doOnNext(
                                    res -> f.complete(res.getHeaders())
                            ).doOnError(
                                    e -> f.completeExceptionally(e)
                            );

                }
            }).when(spyClient).submit(Mockito.any(RxClient.ServerInfo.class), Mockito.any(HttpClientRequest.class));
        }

        @Override
        public void clearCapturedRequests() {
            requestsResponsePairs.clear();
        }

        public ISessionContainer getSessionContainer() {
            try {
                return (ISessionContainer) FieldUtils.readField(this, "sessionContainer", true);
            } catch (Exception e){
                throw new RuntimeException(e);
            }
        }

        public CompositeHttpClient<ByteBuf, ByteBuf> getSpyHttpClient() {
            return spyHttpClient;
        }
    }

    public static class DirectHttpsClientUnderTest extends SpyBaseClass<HttpClientRequest<ByteBuf>> {

        CompositeHttpClient<ByteBuf, ByteBuf> origHttpClient;
        CompositeHttpClient<ByteBuf, ByteBuf> spyHttpClient;
        List<Pair<HttpClientRequest<ByteBuf>, Future<HttpResponseHeaders>>> requestsResponsePairs =
                Collections.synchronizedList(new ArrayList<Pair<HttpClientRequest<ByteBuf>, Future<HttpResponseHeaders>>>());

        DirectHttpsClientUnderTest(URI serviceEndpoint, String masterKey, ConnectionPolicy connectionPolicy, ConsistencyLevel consistencyLevel) {
            // TODO: DANOBLE: ensure the configs instance instantiated here specifies Protocol.Https
            super(serviceEndpoint, masterKey, connectionPolicy, consistencyLevel, new Configs());
            assert connectionPolicy.getConnectionMode() == ConnectionMode.Direct;
            init();
            this.origHttpClient = ReflectionUtils.getDirectHttpsHttpClient(this);
            this.spyHttpClient = Mockito.spy(this.origHttpClient);
            ReflectionUtils.setDirectHttpsHttpClient(this, this.spyHttpClient);
            this.initRequestCapture(this.spyHttpClient);
        }

        public List<Pair<HttpClientRequest<ByteBuf>, Future<HttpResponseHeaders>>> capturedRequestResponseHeaderPairs() {
            return requestsResponsePairs;
        }

        @Override
        public List<HttpClientRequest<ByteBuf>> getCapturedRequests() {
            return requestsResponsePairs.stream().map(pair -> pair.getLeft()).collect(Collectors.toList());
        }

        void initRequestCapture(CompositeHttpClient<ByteBuf, ByteBuf> spyClient) {

            doAnswer(new Answer() {
                @Override
                public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                    RxClient.ServerInfo serverInfo = invocationOnMock.getArgumentAt(0, RxClient.ServerInfo.class);
                    HttpClientRequest<ByteBuf> httpReq = invocationOnMock.getArgumentAt(1, HttpClientRequest.class);

                    CompletableFuture<HttpResponseHeaders> f = new CompletableFuture<>();
                    requestsResponsePairs.add(Pair.of(httpReq, f));

                    return origHttpClient.submit(serverInfo, httpReq)
                            .doOnNext(
                                    res -> f.complete(res.getHeaders())
                            ).doOnError(
                                    e -> f.completeExceptionally(e)
                            );

                }
            }).when(spyClient).submit(Mockito.any(RxClient.ServerInfo.class), Mockito.any(HttpClientRequest.class));
        }

        @Override
        public void clearCapturedRequests() {
            requestsResponsePairs.clear();
        }

        public ISessionContainer getSessionContainer() {
            try {
                return (ISessionContainer) FieldUtils.readField(this, "sessionContainer", true);
            } catch (Exception e){
                throw new RuntimeException(e);
            }
        }

        public CompositeHttpClient<ByteBuf, ByteBuf> getSpyHttpClient() {
            return spyHttpClient;
        }
    }
    
    public static ClientWithGatewaySpy createClientWithGatewaySpy(AsyncDocumentClient.Builder builder) {
        return new SpyClientBuilder(builder).buildWithGatewaySpy();
    }

    public static ClientWithGatewaySpy createClientWithGatewaySpy(URI serviceEndpoint,
                                                                  String masterKey,
                                                                  ConnectionPolicy connectionPolicy,
                                                                  ConsistencyLevel consistencyLevel,
                                                                  Configs configs) {
        return new ClientWithGatewaySpy(serviceEndpoint, masterKey, connectionPolicy, consistencyLevel, configs);
    }

    public static ClientUnderTest createClientUnderTest(AsyncDocumentClient.Builder builder) {
        return new SpyClientBuilder(builder).build();
    }
    
    public static DirectHttpsClientUnderTest createDirectHttpsClientUnderTest(AsyncDocumentClient.Builder builder) {
        return new SpyClientBuilder(builder).buildWithDirectHttps();
    }

    public static ClientUnderTest createClientUnderTest(URI serviceEndpoint,
                                                        String masterKey,
                                                        ConnectionPolicy connectionPolicy,
                                                        ConsistencyLevel consistencyLevel,
                                                        Configs configs) {
        return new ClientUnderTest(serviceEndpoint, masterKey, connectionPolicy, consistencyLevel, configs) {

            @Override
            RxGatewayStoreModel createRxGatewayProxy(ISessionContainer sessionContainer,
                                                     ConsistencyLevel consistencyLevel,
                                                     QueryCompatibilityMode queryCompatibilityMode,
                                                     UserAgentContainer userAgentContainer,
                                                     GlobalEndpointManager globalEndpointManager,
                                                     CompositeHttpClient<ByteBuf, ByteBuf> rxClient) {

                CompositeHttpClient<ByteBuf, ByteBuf> spyClient = Mockito.spy(rxClient);

                this.origHttpClient = rxClient;
                this.spyHttpClient = spyClient;

                this.initRequestCapture(spyHttpClient);

                return super.createRxGatewayProxy(
                        sessionContainer,
                        consistencyLevel,
                        queryCompatibilityMode,
                        userAgentContainer,
                        globalEndpointManager,
                        spyClient);
            }
        };
    }

    public static DirectHttpsClientUnderTest createDirectHttpsClientUnderTest(URI serviceEndpoint, String masterKey,
            ConnectionPolicy connectionPolicy, ConsistencyLevel consistencyLevel) {
        return new DirectHttpsClientUnderTest(serviceEndpoint, masterKey, connectionPolicy, consistencyLevel);
    }
}
