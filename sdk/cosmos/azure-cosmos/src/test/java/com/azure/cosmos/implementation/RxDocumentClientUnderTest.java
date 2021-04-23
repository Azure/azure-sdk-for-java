// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.cosmos.ClientUnderTestBuilder;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.http.HttpResponse;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.doAnswer;

/**
 * This class in conjunction with {@link ClientUnderTestBuilder}
 * provides the functionality for spying the client behavior and the http requests sent.
 */
public class RxDocumentClientUnderTest extends RxDocumentClientImpl {

    public HttpClient spyHttpClient;
    public HttpClient origHttpClient;

    public List<HttpRequest> httpRequests = Collections.synchronizedList(new ArrayList<>());

    public RxDocumentClientUnderTest(URI serviceEndpoint,
                                     String masterKey,
                                     ConnectionPolicy connectionPolicy,
                                     ConsistencyLevel consistencyLevel,
                                     Configs configs,
                                     AzureKeyCredential credential,
                                     boolean contentResponseOnWriteEnabled) {
        super(serviceEndpoint, masterKey, connectionPolicy, consistencyLevel, configs, credential, null, false,
              false, contentResponseOnWriteEnabled, null);
        init(null, null);
    }

    RxGatewayStoreModel createRxGatewayProxy(
            ISessionContainer sessionContainer,
            ConsistencyLevel consistencyLevel,
            QueryCompatibilityMode queryCompatibilityMode,
            UserAgentContainer userAgentContainer,
            GlobalEndpointManager globalEndpointManager,
            HttpClient rxOrigClient) {

        origHttpClient = rxOrigClient;
        spyHttpClient = Mockito.spy(rxOrigClient);

        doAnswer((Answer<Mono<HttpResponse>>) invocationOnMock -> {
            HttpRequest httpRequest = invocationOnMock.getArgument(0, HttpRequest.class);
            Duration responseTimeout = invocationOnMock.getArgument(1, Duration.class);
            httpRequests.add(httpRequest);
            return origHttpClient.send(httpRequest, responseTimeout);
        }).when(spyHttpClient).send(Mockito.any(HttpRequest.class), Mockito.any(Duration.class));

        return super.createRxGatewayProxy(sessionContainer,
                consistencyLevel,
                queryCompatibilityMode,
                userAgentContainer,
                globalEndpointManager,
                spyHttpClient);
    }
}
