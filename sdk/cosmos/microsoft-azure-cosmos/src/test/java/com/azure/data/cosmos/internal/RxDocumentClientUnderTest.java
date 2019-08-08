// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.ClientUnderTestBuilder;
import com.azure.data.cosmos.ConnectionPolicy;
import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.CosmosKeyCredential;
import com.azure.data.cosmos.internal.http.HttpClient;
import com.azure.data.cosmos.internal.http.HttpRequest;
import com.azure.data.cosmos.internal.http.HttpResponse;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import reactor.core.publisher.Mono;

import java.net.URI;
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
                                     CosmosKeyCredential cosmosKeyCredential) {
        super(serviceEndpoint, masterKey, connectionPolicy, consistencyLevel, configs, cosmosKeyCredential);
        init();
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
            HttpRequest httpRequest = invocationOnMock.getArgumentAt(0, HttpRequest.class);
            httpRequests.add(httpRequest);
            return origHttpClient.send(httpRequest);
        }).when(spyHttpClient).send(Mockito.any(HttpRequest.class));

        return super.createRxGatewayProxy(sessionContainer,
                consistencyLevel,
                queryCompatibilityMode,
                userAgentContainer,
                globalEndpointManager,
                spyHttpClient);
    }
}
