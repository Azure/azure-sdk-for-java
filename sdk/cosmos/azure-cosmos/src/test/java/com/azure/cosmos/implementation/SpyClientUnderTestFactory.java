// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.directconnectivity.Protocol;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.http.HttpResponse;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

public class SpyClientUnderTestFactory {

    public static abstract class SpyBaseClass<T> extends RxDocumentClientImpl {

        public SpyBaseClass(URI serviceEndpoint, String masterKeyOrResourceToken, ConnectionPolicy connectionPolicy, ConsistencyLevel consistencyLevel, Configs configs, AzureKeyCredential credential, boolean contentResponseOnWriteEnabled) {

            super(serviceEndpoint, masterKeyOrResourceToken, connectionPolicy, consistencyLevel, configs, credential,
                null, false, false,
                contentResponseOnWriteEnabled, null);
        }

        public abstract List<T> getCapturedRequests();

        public abstract void clearCapturedRequests();

        protected static Configs createConfigsSpy(final Protocol protocol) {
            final Configs configs = Mockito.spy(new Configs());
            doAnswer((Answer<Protocol>) invocation -> protocol).when(configs).getProtocol();
            return configs;
        }
    }

    public static class ClientWithGatewaySpy extends SpyBaseClass<RxDocumentServiceRequest> {

        private RxGatewayStoreModel origRxGatewayStoreModel;
        private RxGatewayStoreModel spyRxGatewayStoreModel;

        private List<RxDocumentServiceRequest> requests;


        ClientWithGatewaySpy(URI serviceEndpoint, String masterKey, ConnectionPolicy connectionPolicy,
                             ConsistencyLevel consistencyLevel, Configs configs, AzureKeyCredential credential,
                             boolean contentResponseOnWriteEnabled) {
            super(serviceEndpoint, masterKey, connectionPolicy, consistencyLevel, configs, credential,
                contentResponseOnWriteEnabled);
            init(null, null);
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
                                                 HttpClient rxClient) {
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
            doAnswer(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocationOnMock)  {
                    RxDocumentServiceRequest req = invocationOnMock.getArgument(0, RxDocumentServiceRequest.class);
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

    public static class ClientUnderTest extends SpyBaseClass<HttpRequest> {

        HttpClient origHttpClient;
        HttpClient spyHttpClient;
        List<Pair<HttpRequest, Future<HttpResponse>>> requestsResponsePairs =
            Collections.synchronizedList(new ArrayList<>());


        ClientUnderTest(URI serviceEndpoint, String masterKey, ConnectionPolicy connectionPolicy,
                        ConsistencyLevel consistencyLevel, Configs configs, AzureKeyCredential credential,
                        boolean contentResponseOnWriteEnabled) {
            super(serviceEndpoint, masterKey, connectionPolicy, consistencyLevel, configs, credential,
                contentResponseOnWriteEnabled);
            init(null, this::initHttpRequestCapture);
        }

        private Mono<HttpResponse> captureHttpRequest(InvocationOnMock invocationOnMock) {
            HttpRequest httpRequest = invocationOnMock.getArgument(0, HttpRequest.class);
            Duration responseTimeout = Duration.ofSeconds(Configs.getHttpResponseTimeoutInSeconds());
            if (invocationOnMock.getArguments().length == 2) {
                responseTimeout = invocationOnMock.getArgument(1, Duration.class);
            }
            CompletableFuture<HttpResponse> f = new CompletableFuture<>();
            this.requestsResponsePairs.add(Pair.of(httpRequest, f));

            return origHttpClient
                .send(httpRequest, responseTimeout)
                .doOnNext(httpResponse -> f.complete(httpResponse.buffer()))
                .doOnError(f::completeExceptionally);
        }

        private HttpClient initHttpRequestCapture(HttpClient originalClient) {
            HttpClient spyHttpClient = spy(originalClient);
            doAnswer(this::captureHttpRequest)
                .when(spyHttpClient)
                .send(Mockito.any(HttpRequest.class));
            doAnswer(this::captureHttpRequest)
                .when(spyHttpClient)
                .send(Mockito.any(HttpRequest.class), Mockito.any(Duration.class));

            this.origHttpClient = originalClient;
            this.spyHttpClient = spyHttpClient;

            return spyHttpClient;
        }

        public List<Pair<HttpRequest, Future<HttpResponse>>> capturedRequestResponseHeaderPairs() {
            return requestsResponsePairs;
        }

        @Override
        public List<HttpRequest> getCapturedRequests() {
            return requestsResponsePairs.stream().map(Pair::getLeft).collect(Collectors.toList());
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

        public HttpClient getOrigHttpClient() {
            return origHttpClient;
        }

        public HttpClient getSpyHttpClient() {
            return spyHttpClient;
        }
    }

    public static class DirectHttpsClientUnderTest extends SpyBaseClass<HttpRequest> {

        HttpClient origHttpClient;
        HttpClient spyHttpClient;
        List<Pair<HttpRequest, Future<HttpHeaders>>> requestsResponsePairs =
            Collections.synchronizedList(new ArrayList<>());

        DirectHttpsClientUnderTest(URI serviceEndpoint, String masterKey, ConnectionPolicy connectionPolicy,
                                   ConsistencyLevel consistencyLevel, AzureKeyCredential credential,
                                   boolean contentResponseOnWriteEnabled) {
            super(serviceEndpoint, masterKey, connectionPolicy, consistencyLevel, createConfigsSpy(Protocol.HTTPS),
                credential, contentResponseOnWriteEnabled);
            assert connectionPolicy.getConnectionMode() == ConnectionMode.DIRECT;
            init(null, null);

            this.origHttpClient = ReflectionUtils.getDirectHttpsHttpClient(this);
            this.spyHttpClient = spy(this.origHttpClient);
            ReflectionUtils.setDirectHttpsHttpClient(this, this.spyHttpClient);
            this.initRequestCapture(this.spyHttpClient);
        }

        public List<Pair<HttpRequest, Future<HttpHeaders>>> capturedRequestResponseHeaderPairs() {
            return requestsResponsePairs;
        }

        @Override
        public List<HttpRequest> getCapturedRequests() {
            return requestsResponsePairs.stream().map(Pair::getLeft).collect(Collectors.toList());
        }

        void initRequestCapture(HttpClient spyClient) {
            doAnswer(invocationOnMock -> {
                HttpRequest httpRequest = invocationOnMock.getArgument(0, HttpRequest.class);
                Duration responseTimeout = invocationOnMock.getArgument(1, Duration.class);
                CompletableFuture<HttpHeaders> f = new CompletableFuture<>();
                requestsResponsePairs.add(Pair.of(httpRequest, f));

                return origHttpClient
                    .send(httpRequest, responseTimeout)
                    .doOnNext(httpResponse -> f.complete(httpResponse.headers()))
                    .doOnError(f::completeExceptionally);

            }).when(spyClient).send(Mockito.any(HttpRequest.class), Mockito.any(Duration.class));
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

        public HttpClient getSpyHttpClient() {
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
                                                                  Configs configs,
                                                                  AzureKeyCredential credential,
                                                                  boolean contentResponseOnWriteEnabled) {
        return new ClientWithGatewaySpy(serviceEndpoint, masterKey, connectionPolicy, consistencyLevel, configs,
            credential, contentResponseOnWriteEnabled);
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
                                                        Configs configs,
                                                        AzureKeyCredential credential,
                                                        boolean contentResponseOnWriteEnabled) {
        return new ClientUnderTest(serviceEndpoint, masterKey, connectionPolicy, consistencyLevel, configs,
            credential, contentResponseOnWriteEnabled);
    }

    public static DirectHttpsClientUnderTest createDirectHttpsClientUnderTest(
        URI serviceEndpoint, String masterKey, ConnectionPolicy connectionPolicy, ConsistencyLevel consistencyLevel,
        AzureKeyCredential credential, boolean contentResponseOnWriteEnabled) {
        return new DirectHttpsClientUnderTest(serviceEndpoint, masterKey, connectionPolicy, consistencyLevel,
            credential, contentResponseOnWriteEnabled);
    }
}