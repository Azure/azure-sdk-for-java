// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.DocumentServiceRequestContext;
import com.azure.cosmos.implementation.FailureValidator;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.IAuthorizationTokenProvider;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.SessionContainer;
import com.azure.cosmos.implementation.StoreResponseBuilder;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;

public class ReplicatedResourceClientGoneForWriteTest {
    @DataProvider(name = "goneOnWriteRefreshesAddressesArgProvider")
    public Object[][] goneOnWriteRefreshesAddressesArgProvider() {
        return new Object[][]{
            // Consistency mode, number of partition splitting exception till split migration completes
            { ConsistencyLevel.EVENTUAL},
        };
    }

    @Test(
        groups = { "unit" },
        dataProvider = "goneOnWriteRefreshesAddressesArgProvider",
        timeOut = ReplicatedResourceClientPartitionSplitTest.TIMEOUT)
    public void gone_RefreshCache_Write(ConsistencyLevel consistencyLevel) {

        Uri primaryAddress = Uri.create("http://primary/");
        List<Uri> secondaryAddresses = new ArrayList<>();
        secondaryAddresses.add(Uri.create("http://secondary-1/"));
        secondaryAddresses.add(Uri.create("http://secondary-2/"));
        secondaryAddresses.add(Uri.create("http://secondary-3/"));

        String partitionKeyRangeId = "1";

        AddressSelectorWrapper addressSelectorWrapper = AddressSelectorWrapper
            .Builder
            .Simple
            .create()
            .withPrimary(primaryAddress)
            .withSecondary(secondaryAddresses)
            .build();

        long lsn = 54;
        long localLsn = 18;

        StoreResponse primaryResponse = StoreResponseBuilder.create()
                                                            .withLSN(lsn)
                                                            .withLocalLSN(localLsn)
                                                            .withHeader(WFConstants.BackendHeaders.QUORUM_ACKED_LOCAL_LSN, Long.toString(localLsn))
                                                            .withRequestCharge(1.1)
                                                            .build();

        TransportClientWrapper.Builder.UriToResultBuilder transportClientWrapperBuilder = TransportClientWrapper.Builder.uriToResultBuilder();

        GoneException goneException = new GoneException();
        BridgeInternal.setSendingRequestStarted(goneException, true);
        transportClientWrapperBuilder
            .exceptionOn(primaryAddress, OperationType.Create, ResourceType.Document, goneException, true);

        TransportClientWrapper transportClientWrapper = transportClientWrapperBuilder.build();

        GatewayServiceConfiguratorReaderMock gatewayServiceConfigurationReaderWrapper = GatewayServiceConfiguratorReaderMock.from(ConsistencyLevel.STRONG,
            4,
            3,
            4,
            3);

        SessionContainer sessionContainer = new SessionContainer("test");

        IAuthorizationTokenProvider authorizationTokenProvider = Mockito.mock(IAuthorizationTokenProvider.class);
        ReplicatedResourceClient resourceClient = new ReplicatedResourceClient(
            mockDiagnosticsClientContext(),
            new Configs(),
            addressSelectorWrapper.addressSelector,
            sessionContainer,
            transportClientWrapper.transportClient,
            gatewayServiceConfigurationReaderWrapper.gatewayServiceConfigurationReader,
            authorizationTokenProvider,
            false,
            false);

        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(
            mockDiagnosticsClientContext(),
            OperationType.Create,
            "/dbs/db/colls/col/docs/docId",
            ResourceType.Document);
        request.requestContext = new DocumentServiceRequestContext();
        request.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, consistencyLevel.toString());

        Function<RxDocumentServiceRequest, Mono<RxDocumentServiceRequest>> prepareRequestAsyncDelegate = null;
        Mono<StoreResponse> storeResponseObs = resourceClient.invokeAsync(request, prepareRequestAsyncDelegate);

        // Address refresh is happening in the background - allowing some time to finish the refresh
        // Because this is all using mocking (no emulator) the delay of a couple hundred ms should be sufficient
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        FailureValidator validator = FailureValidator
            .builder()
            .instanceOf(CosmosException.class)
            .statusCode(410).build();
        ReplicatedResourceClientPartitionSplitTest.validateFailure(
            storeResponseObs,
            validator,
            ReplicatedResourceClientPartitionSplitTest.TIMEOUT);
        addressSelectorWrapper.verifyNumberOfForceCacheRefreshGreaterThanOrEqualTo(1);
    }
}
