// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.FailureValidator;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.IAuthorizationTokenProvider;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import io.reactivex.subscribers.TestSubscriber;
import org.assertj.core.api.Assertions;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;

public class ReplicatedResourceClientTest {
    protected static final int TIMEOUT = 60000;
    private IAddressResolver addressResolver;
    private TransportClient transportClient;
    private boolean enableReadRequestsFallback;
    public boolean forceAddressRefresh;
    private GatewayServiceConfigurationReader serviceConfigReader;
    private IAuthorizationTokenProvider authorizationTokenProvider;

    @BeforeClass(groups = "unit")
    public void before_ReplicatedResourceClientTest() throws Exception {
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
        ReplicatedResourceClient resourceClient = new ReplicatedResourceClient(mockDiagnosticsClientContext(), configs, new AddressSelector(addressResolver, Protocol.HTTPS), null,
                transportClient, serviceConfigReader, authorizationTokenProvider, enableReadRequestsFallback, false);
        FailureValidator validator = FailureValidator.builder().instanceOf(CosmosException.class).build();
        RxDocumentServiceRequest request = Mockito.spy(RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Create, ResourceType.Document));
        request.requestContext.cosmosDiagnostics = request.createCosmosDiagnostics();

        Mockito.when(addressResolver.resolveAsync(ArgumentMatchers.any(), ArgumentMatchers.anyBoolean()))
                .thenReturn(Mono.error(new GoneException()));
        Mono<StoreResponse> response = resourceClient.invokeAsync(request, null);

        validateFailure(response, validator, TIMEOUT);
        //method will fail 6 time (first try , and 5 retries within 30 sec(1,2,4,8,15 wait))
        Mockito.verify(addressResolver, Mockito.times(6)).resolveAsync(ArgumentMatchers.any(), ArgumentMatchers.anyBoolean());
    }

    public static void validateFailure(Mono<StoreResponse> single, FailureValidator validator, long timeout) {

        TestSubscriber<StoreResponse> testSubscriber = new TestSubscriber<>();
        single.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNotComplete();
        testSubscriber.assertTerminated();
        Assertions.assertThat(testSubscriber.errorCount()).isEqualTo(1);
        Throwable throwable = Exceptions.unwrap(testSubscriber.errors().get(0));
        validator.validate(throwable);
    }
}
