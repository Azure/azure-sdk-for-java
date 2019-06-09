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

package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.ISessionContainer;
import com.azure.data.cosmos.rx.FailureValidator;
import io.netty.buffer.ByteBuf;
import io.netty.handler.timeout.ReadTimeoutException;
import io.reactivex.netty.client.RxClient;
import io.reactivex.netty.protocol.http.client.CompositeHttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import rx.Observable;
import rx.observers.TestSubscriber;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class RxGatewayStoreModelTest {
    private final static int TIMEOUT = 10000;

    @Test(groups = "unit")
    public void readTimeout() throws Exception {
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        QueryCompatibilityMode queryCompatibilityMode = QueryCompatibilityMode.Default;
        UserAgentContainer userAgentContainer = new UserAgentContainer();
        GlobalEndpointManager globalEndpointManager = Mockito.mock(GlobalEndpointManager.class);
        Mockito.doReturn(new URL("https://localhost"))
                .when(globalEndpointManager).resolveServiceEndpoint(Mockito.any());
        CompositeHttpClient<ByteBuf, ByteBuf> httpClient = Mockito.mock(CompositeHttpClient.class);
        Mockito.doReturn(Observable.error(ReadTimeoutException.INSTANCE))
                .when(httpClient).submit(Mockito.any(RxClient.ServerInfo.class), Mockito.any(HttpClientRequest.class));

        RxGatewayStoreModel storeModel = new RxGatewayStoreModel(
                sessionContainer,
                ConsistencyLevel.SESSION,
                queryCompatibilityMode,
                userAgentContainer,
                globalEndpointManager,
                httpClient);

        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(
                OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.getHeaders().put("key", "value");
        dsr.requestContext = Mockito.mock(DocumentServiceRequestContext.class);

        Observable<RxDocumentServiceResponse> resp = storeModel.processMessage(dsr);
        validateFailure(resp, FailureValidator.builder()
                .instanceOf(CosmosClientException.class)
                .causeInstanceOf(ReadTimeoutException.class)
                .documentClientExceptionHeaderRequestContainsEntry("key", "value")
                .statusCode(0).build());
    }

    public void validateFailure(Observable<RxDocumentServiceResponse> observable,
                                FailureValidator validator) {
        validateFailure(observable, validator, TIMEOUT);
    }

    public static void validateFailure(Observable<RxDocumentServiceResponse> observable,
                                       FailureValidator validator,
                                       long timeout) {
        TestSubscriber<RxDocumentServiceResponse> testSubscriber = new TestSubscriber<>();

        observable.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNotCompleted();
        testSubscriber.assertTerminalEvent();
        assertThat(testSubscriber.getOnErrorEvents()).hasSize(1);
        validator.validate(testSubscriber.getOnErrorEvents().get(0));
    }
}
