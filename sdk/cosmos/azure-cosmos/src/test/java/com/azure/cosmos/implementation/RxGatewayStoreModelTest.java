// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpRequest;
import io.netty.handler.timeout.ReadTimeoutException;
import io.reactivex.subscribers.TestSubscriber;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;

public class RxGatewayStoreModelTest {
    private final static int TIMEOUT = 10000;

    @Test(groups = "unit")
    public void readTimeout() throws Exception {
        DiagnosticsClientContext clientContext = mockDiagnosticsClientContext();
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        QueryCompatibilityMode queryCompatibilityMode = QueryCompatibilityMode.Default;
        UserAgentContainer userAgentContainer = new UserAgentContainer();
        GlobalEndpointManager globalEndpointManager = Mockito.mock(GlobalEndpointManager.class);
        Mockito.doReturn(new URI("https://localhost"))
                .when(globalEndpointManager).resolveServiceEndpoint(Mockito.any());
        HttpClient httpClient = Mockito.mock(HttpClient.class);
        Mockito.doReturn(Mono.error(ReadTimeoutException.INSTANCE))
                .when(httpClient).send(Mockito.any(HttpRequest.class), Mockito.any(Duration.class));

        RxGatewayStoreModel storeModel = new RxGatewayStoreModel(clientContext,
                sessionContainer,
                ConsistencyLevel.SESSION,
                queryCompatibilityMode,
                userAgentContainer,
                globalEndpointManager,
                httpClient);

        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(clientContext,
                OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.getHeaders().put("key", "value");
        dsr.requestContext = new DocumentServiceRequestContext();


        Mono<RxDocumentServiceResponse> resp = storeModel.processMessage(dsr);
        validateFailure(resp, FailureValidator.builder()
                .instanceOf(CosmosException.class)
                .causeInstanceOf(ReadTimeoutException.class)
                .documentClientExceptionHeaderRequestContainsEntry("key", "value")
                .statusCode(0).build());
    }

    public void validateFailure(Mono<RxDocumentServiceResponse> observable,
                                FailureValidator validator) {
        validateFailure(observable, validator, TIMEOUT);
    }

    public static void validateFailure(Mono<RxDocumentServiceResponse> observable,
                                       FailureValidator validator,
                                       long timeout) {
        TestSubscriber<RxDocumentServiceResponse> testSubscriber = new TestSubscriber<>();
        observable.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNotComplete();
        testSubscriber.assertTerminated();
        assertThat(testSubscriber.errorCount()).isEqualTo(1);
        validator.validate(testSubscriber.errors().get(0));
    }
}
