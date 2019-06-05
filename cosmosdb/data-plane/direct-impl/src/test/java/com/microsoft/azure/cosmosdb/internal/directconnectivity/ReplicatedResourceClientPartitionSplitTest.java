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

package com.microsoft.azure.cosmosdb.internal.directconnectivity;

import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.PartitionKeyRange;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.internal.OperationType;
import com.microsoft.azure.cosmosdb.internal.ResourceType;
import com.microsoft.azure.cosmosdb.internal.SessionContainer;
import com.microsoft.azure.cosmosdb.rx.FailureValidator;
import com.microsoft.azure.cosmosdb.rx.internal.Configs;
import com.microsoft.azure.cosmosdb.rx.internal.DocumentServiceRequestContext;
import com.microsoft.azure.cosmosdb.rx.internal.IAuthorizationTokenProvider;
import com.microsoft.azure.cosmosdb.rx.internal.PartitionKeyRangeIsSplittingException;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceRequest;
import org.assertj.core.api.Assertions;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import rx.Single;
import rx.functions.Func1;
import rx.observers.TestSubscriber;

import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ReplicatedResourceClientPartitionSplitTest {
    protected static final int TIMEOUT = 120000;

    @DataProvider(name = "partitionIsSplittingArgProvider")
    public Object[][] partitionIsSplittingArgProvider() {
        return new Object[][]{
                // Consistency mode, number of partition splitting exception till split migration completes
                { ConsistencyLevel.Eventual, 1},
                { ConsistencyLevel.Eventual, 2},
                { ConsistencyLevel.Eventual, Integer.MAX_VALUE }, // server side partition split operation never completes
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

        AddressSelectorWrapper addressSelectorWrapper = AddressSelectorWrapper.Builder.ReplicaMoveBuilder.create(Protocol.Https)
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

        GatewayServiceConfiguratorReaderMock gatewayServiceConfigurationReaderWrapper = GatewayServiceConfiguratorReaderMock.from(ConsistencyLevel.Strong,
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
        request.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, consistencyLevel.name());

        Func1<RxDocumentServiceRequest, Single<RxDocumentServiceRequest>> prepareRequestAsyncDelegate = null;
        Single<StoreResponse> storeResponseObs = resourceClient.invokeAsync(request, prepareRequestAsyncDelegate);

        if (partitionIsSplitting < Integer.MAX_VALUE) {

            StoreResponseValidator validator = StoreResponseValidator.create()
                    .withBELSN(lsn)
                    .withRequestCharge(1.1)
                    .build();
            validateSuccess(storeResponseObs, validator);

            addressSelectorWrapper.verifyNumberOfForceCacheRefreshGreaterThanOrEqualTo(1);
        } else {
            FailureValidator validator = FailureValidator.builder().instanceOf(DocumentClientException.class)
                    .statusCode(503).build();
            validateFailure(storeResponseObs, validator, TIMEOUT);
        }
    }

    public static void validateSuccess(Single<List<StoreResult>> single,
                                       MultiStoreResultValidator validator) {
        validateSuccess(single, validator, TIMEOUT);
    }

    public static void validateSuccess(Single<List<StoreResult>> single,
                                       MultiStoreResultValidator validator, long timeout) {
        TestSubscriber<List<StoreResult>> testSubscriber = new TestSubscriber<>();

        single.toObservable().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();
        testSubscriber.assertValueCount(1);
        validator.validate(testSubscriber.getOnNextEvents().get(0));
    }

    public static void validateSuccess(Single<StoreResponse> single,
                                       StoreResponseValidator validator) {
        validateSuccess(single, validator, TIMEOUT);
    }

    public static void validateSuccess(Single<StoreResponse> single,
                                       StoreResponseValidator validator, long timeout) {
        TestSubscriber<StoreResponse> testSubscriber = new TestSubscriber<>();

        single.toObservable().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();
        testSubscriber.assertValueCount(1);
        validator.validate(testSubscriber.getOnNextEvents().get(0));
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

    private PartitionKeyRange partitionKeyRangeWithId(String id) {
        PartitionKeyRange partitionKeyRange = Mockito.mock(PartitionKeyRange.class);
        Mockito.doReturn(id).when(partitionKeyRange).getId();
        return partitionKeyRange;
    }
}
