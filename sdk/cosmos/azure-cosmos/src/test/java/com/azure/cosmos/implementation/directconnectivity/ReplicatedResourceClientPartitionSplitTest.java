// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.DocumentServiceRequestContext;
import com.azure.cosmos.implementation.FailureValidator;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.IAuthorizationTokenProvider;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.PartitionKeyRangeIsSplittingException;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.SessionContainer;
import com.azure.cosmos.implementation.StoreResponseBuilder;
import io.reactivex.subscribers.TestSubscriber;
import org.assertj.core.api.Assertions;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;

public class ReplicatedResourceClientPartitionSplitTest {
    protected static final int TIMEOUT = 120000;

    @DataProvider(name = "partitionIsSplittingArgProvider")
    public Object[][] partitionIsSplittingArgProvider() {
        return new Object[][]{
                // Consistency mode, number of partition splitting exception till split migration completes
                { ConsistencyLevel.EVENTUAL, 1},
                { ConsistencyLevel.EVENTUAL, 2},
                { ConsistencyLevel.EVENTUAL, Integer.MAX_VALUE }, // server side partition split operation never completes
        };
    }

    @Test(groups = { "unit" }, dataProvider = "partitionIsSplittingArgProvider", timeOut = TIMEOUT)
    public void partitionSplit_RefreshCache_Read(ConsistencyLevel consistencyLevel, int partitionIsSplitting) {
        Uri secondary1AddressBeforeMove = Uri.create("secondary");
        Uri secondary1AddressAfterMove = Uri.create("secondaryNew");

        Uri primaryAddressBeforeMove = Uri.create("primary");
        Uri primaryAddressAfterMove = Uri.create("primaryNew");

        String partitionKeyRangeIdBeforeSplit = "1";
        String partitionKeyRangeIdAfterSplit = "2";

        AddressSelectorWrapper addressSelectorWrapper = AddressSelectorWrapper.Builder.ReplicaMoveBuilder.create(Protocol.HTTPS)
                .withPrimaryMove(primaryAddressBeforeMove, primaryAddressAfterMove)
                .withSecondaryMove(secondary1AddressBeforeMove, secondary1AddressAfterMove)
                .newPartitionKeyRangeIdOnRefresh(r -> partitionKeyRangeWithId(partitionKeyRangeIdAfterSplit))
                .build();

        long lsn = 54;
        long localLsn = 18;

        StoreResponse primaryResponse = StoreResponseBuilder.create()
                .withLSN(lsn)
                .withLocalLSN(localLsn)
                .withHeader(WFConstants.BackendHeaders.QUORUM_ACKED_LOCAL_LSN, Long.toString(localLsn))
                .withHeader(WFConstants.BackendHeaders.CURRENT_REPLICA_SET_SIZE, partitionKeyRangeIdAfterSplit)
                .withRequestCharge(1.1)
                .build();
        StoreResponse secondaryResponse1 = StoreResponseBuilder.create()
                .withLSN(lsn)
                .withLocalLSN(localLsn)
                .withHeader(WFConstants.BackendHeaders.QUORUM_ACKED_LOCAL_LSN, Long.toString(localLsn))
                .withHeader(WFConstants.BackendHeaders.CURRENT_REPLICA_SET_SIZE, partitionKeyRangeIdAfterSplit)
                .withRequestCharge(1.1)
                .build();

        TransportClientWrapper.Builder.UriToResultBuilder transportClientWrapperBuilder = TransportClientWrapper.Builder.uriToResultBuilder();

        PartitionKeyRangeIsSplittingException splittingException = new PartitionKeyRangeIsSplittingException();
        if (partitionIsSplitting == Integer.MAX_VALUE) {
            transportClientWrapperBuilder
                    .exceptionOn(primaryAddressBeforeMove, OperationType.Read, ResourceType.Document, splittingException, true)
                    .exceptionOn(secondary1AddressBeforeMove, OperationType.Read, ResourceType.Document, splittingException, true);
        } else {
            for (int i = 0; i < partitionIsSplitting; i++) {
                transportClientWrapperBuilder
                        .exceptionOn(primaryAddressBeforeMove, OperationType.Read, ResourceType.Document, splittingException, false)
                        .exceptionOn(secondary1AddressBeforeMove, OperationType.Read, ResourceType.Document, splittingException, false);
            }
        }

        GoneException goneException = new GoneException();
        transportClientWrapperBuilder
                .exceptionOn(primaryAddressBeforeMove, OperationType.Read, ResourceType.Document, goneException, true)
                .exceptionOn(secondary1AddressBeforeMove, OperationType.Read, ResourceType.Document, goneException, true)
                .storeResponseOn(primaryAddressAfterMove, OperationType.Read, ResourceType.Document, secondaryResponse1, true)
                .storeResponseOn(secondary1AddressAfterMove, OperationType.Read, ResourceType.Document, primaryResponse, true);


        TransportClientWrapper transportClientWrapper = transportClientWrapperBuilder.build();

        GatewayServiceConfiguratorReaderMock gatewayServiceConfigurationReaderWrapper = GatewayServiceConfiguratorReaderMock.from(ConsistencyLevel.STRONG,
                                                                                                                                  4,
                                                                                                                                  3,
                                                                                                                                  4,
                                                                                                                                  3);

        SessionContainer sessionContainer = new SessionContainer("test");

        IAuthorizationTokenProvider authorizationTokenProvider = Mockito.mock(IAuthorizationTokenProvider.class);
        ReplicatedResourceClient resourceClient = new ReplicatedResourceClient(mockDiagnosticsClientContext(),
                                                                               new Configs(),
                                                                               addressSelectorWrapper.addressSelector,
                                                                               sessionContainer,
                                                                               transportClientWrapper.transportClient,
                                                                               gatewayServiceConfigurationReaderWrapper.gatewayServiceConfigurationReader,
                                                                               authorizationTokenProvider,
                                                                               false,
                                                                               false);

        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
                OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        request.requestContext = new DocumentServiceRequestContext();
        request.requestContext.resolvedPartitionKeyRange = partitionKeyRangeWithId(partitionKeyRangeIdBeforeSplit);
        request.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, consistencyLevel.toString());

        Function<RxDocumentServiceRequest, Mono<RxDocumentServiceRequest>> prepareRequestAsyncDelegate = null;
        Mono<StoreResponse> storeResponseObs = resourceClient.invokeAsync(request, prepareRequestAsyncDelegate);

        if (partitionIsSplitting < Integer.MAX_VALUE) {

            StoreResponseValidator validator = StoreResponseValidator.create()
                    .withBELSN(lsn)
                    .withRequestCharge(1.1)
                    .build();
            validateSuccess(storeResponseObs, validator);

            addressSelectorWrapper.verifyNumberOfForceCacheRefreshGreaterThanOrEqualTo(1);
        } else {
            FailureValidator validator = FailureValidator.builder().instanceOf(CosmosException.class)
                    .statusCode(503).build();
            validateFailure(storeResponseObs, validator, TIMEOUT);
        }
    }

    public static void validateSuccess(Mono<List<StoreResult>> single,
                                       MultiStoreResultValidator validator) {
        validateSuccess(single, validator, TIMEOUT);
    }

    public static void validateSuccess(Mono<List<StoreResult>> single,
                                       MultiStoreResultValidator validator, long timeout) {
        TestSubscriber<List<StoreResult>> testSubscriber = new TestSubscriber<>();

        single.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNoErrors();
        testSubscriber.assertComplete();
        testSubscriber.assertValueCount(1);
        validator.validate(testSubscriber.values().get(0));
    }

    public static void validateSuccess(Mono<StoreResponse> single,
                                       StoreResponseValidator validator) {
        validateSuccess(single, validator, TIMEOUT);
    }

    public static void validateSuccess(Mono<StoreResponse> single,
                                       StoreResponseValidator validator, long timeout) {
        TestSubscriber<StoreResponse> testSubscriber = new TestSubscriber<>();

        single.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNoErrors();
        testSubscriber.assertComplete();
        testSubscriber.assertValueCount(1);
        validator.validate(testSubscriber.values().get(0));
    }


    public static void validateFailure(Mono<StoreResponse> single, FailureValidator validator, long timeout) {

        TestSubscriber<StoreResponse> testSubscriber = new TestSubscriber<>();
        single.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNotComplete();
        testSubscriber.assertTerminated();
        Assertions.assertThat(testSubscriber.errorCount()).isEqualTo(1);
        validator.validate(Exceptions.unwrap(testSubscriber.errors().get(0)));
    }

    private PartitionKeyRange partitionKeyRangeWithId(String id) {
        PartitionKeyRange partitionKeyRange = Mockito.mock(PartitionKeyRange.class);
        Mockito.doReturn(id).when(partitionKeyRange).getId();
        return partitionKeyRange;
    }
}
