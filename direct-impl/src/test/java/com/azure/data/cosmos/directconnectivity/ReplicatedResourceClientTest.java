/*
 *
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and permission notice shall be included in all
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

package com.azure.data.cosmos.directconnectivity;

import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.internal.Configs;
import com.azure.data.cosmos.internal.IAuthorizationTokenProvider;
import com.azure.data.cosmos.internal.OperationType;
import com.azure.data.cosmos.internal.ResourceType;
import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import com.azure.data.cosmos.rx.FailureValidator;
import org.assertj.core.api.Assertions;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import rx.Single;
import rx.observers.TestSubscriber;

import java.util.concurrent.TimeUnit;

public class ReplicatedResourceClientTest {
    protected static final int TIMEOUT = 60000;
    private IAddressResolver addressResolver;
    private TransportClient transportClient;
    private boolean enableReadRequestsFallback;
    public boolean forceAddressRefresh;
    private GatewayServiceConfigurationReader serviceConfigReader;
    private IAuthorizationTokenProvider authorizationTokenProvider;

    @BeforeClass(groups = "unit")
    public void setup() throws Exception {
        addressResolver = Mockito.mock(IAddressResolver.class);
        transportClient = Mockito.mock(TransportClient.class);
        serviceConfigReader = Mockito.mock(GatewayServiceConfigurationReader.class);
        authorizationTokenProvider = Mockito.mock(IAuthorizationTokenProvider.class);
    }

    /**
     * This test will verify that Gone exception will be retired
     *  fixed number of time before throwing error.
     */
    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void invokeAsyncWithGoneException() {
        Configs configs = new Configs();
        ReplicatedResourceClient resourceClient = new ReplicatedResourceClient(configs, new AddressSelector(addressResolver, Protocol.HTTPS), null,
                transportClient, serviceConfigReader, authorizationTokenProvider, enableReadRequestsFallback, false);
        FailureValidator validator = FailureValidator.builder().instanceOf(CosmosClientException.class).build();
        RxDocumentServiceRequest request = Mockito.spy(RxDocumentServiceRequest.create(OperationType.Create, ResourceType.Document));

        Mockito.when(addressResolver.resolveAsync(Matchers.any(), Matchers.anyBoolean()))
                .thenReturn(Single.error(new GoneException()));
        Single<StoreResponse> response = resourceClient.invokeAsync(request, null);

        validateFailure(response, validator, TIMEOUT);
        //method will fail 7 time (first try ,last try , and 5 retries within 30 sec(1,2,4,8,15 wait))
        Mockito.verify(addressResolver, Mockito.times(7)).resolveAsync(Matchers.any(), Matchers.anyBoolean());
    }

    public static void validateFailure(Single<StoreResponse> single, FailureValidator validator, long timeout) {

        TestSubscriber<StoreResponse> testSubscriber = new TestSubscriber<>();
        single.toObservable().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNotCompleted();
        testSubscriber.assertTerminalEvent();
        Assertions.assertThat(testSubscriber.getOnErrorEvents()).hasSize(1);
        validator.validate(testSubscriber.getOnErrorEvents().get(0));
    }
}
