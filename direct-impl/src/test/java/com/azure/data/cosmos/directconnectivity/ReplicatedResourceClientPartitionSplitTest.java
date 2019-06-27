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

package com.azure.data.cosmos.directconnectivity;

import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.GoneException;
import com.azure.data.cosmos.internal.PartitionKeyRange;
import com.azure.data.cosmos.internal.Configs;
import com.azure.data.cosmos.internal.DocumentServiceRequestContext;
import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.IAuthorizationTokenProvider;
import com.azure.data.cosmos.internal.OperationType;
import com.azure.data.cosmos.PartitionKeyRangeIsSplittingException;
import com.azure.data.cosmos.internal.ResourceType;
import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import com.azure.data.cosmos.internal.SessionContainer;
import com.azure.data.cosmos.rx.FailureValidator;
import io.reactivex.subscribers.TestSubscriber;
import org.assertj.core.api.Assertions;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

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
        URI secondary1AddressBeforeMove = URI.create("secondary");
        URI secondary1AddressAfterMove = URI.create("secondaryNew");

        URI primaryAddressBeforeMove = URI.create("primary");
        URI primaryAddressAfterMove = URI.create("primaryNew");

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
        ReplicatedResourceClient resourceClient = new ReplicatedResourceClient(new Configs(),
                                                                               addressSelectorWrapper.addressSelector,
                                                                               sessionContainer,
                                                                               transportClientWrapper.transportClient,
                                                                               gatewayServiceConfigurationReaderWrapper.gatewayServiceConfigurationReader,
                                                                               authorizationTokenProvider,
                                                                               false,
                                                                               false);

        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(
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
            FailureValidator validator = FailureValidator.builder().instanceOf(CosmosClientException.class)
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
        validator.validate(testSubscriber.errors().get(0));
    }

    private PartitionKeyRange partitionKeyRangeWithId(String id) {
        PartitionKeyRange partitionKeyRange = Mockito.mock(PartitionKeyRange.class);
        Mockito.doReturn(id).when(partitionKeyRange).id();
        return partitionKeyRange;
    }
}
