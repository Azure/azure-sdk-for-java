// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.GoneException;
import com.azure.data.cosmos.internal.directconnectivity.*;
import com.azure.data.cosmos.internal.Configs;
import com.azure.data.cosmos.internal.IAuthorizationTokenProvider;
import com.azure.data.cosmos.internal.OperationType;
import com.azure.data.cosmos.internal.ResourceType;
import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import com.azure.data.cosmos.internal.FailureValidator;
import com.azure.data.cosmos.internal.directconnectivity.*;
import io.reactivex.subscribers.TestSubscriber;
import org.assertj.core.api.Assertions;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

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
                .thenReturn(Mono.error(new GoneException()));
        Mono<StoreResponse> response = resourceClient.invokeAsync(request, null);

        validateFailure(response, validator, TIMEOUT);
        //method will fail 7 time (first try ,last try , and 5 retries within 30 sec(1,2,4,8,15 wait))
        Mockito.verify(addressResolver, Mockito.times(7)).resolveAsync(Matchers.any(), Matchers.anyBoolean());
    }

    public static void validateFailure(Mono<StoreResponse> single, FailureValidator validator, long timeout) {

        TestSubscriber<StoreResponse> testSubscriber = new TestSubscriber<>();
        single.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNotComplete();
        testSubscriber.assertTerminated();
        Assertions.assertThat(testSubscriber.errorCount()).isEqualTo(1);
        validator.validate(testSubscriber.errors().get(0));
    }
}
