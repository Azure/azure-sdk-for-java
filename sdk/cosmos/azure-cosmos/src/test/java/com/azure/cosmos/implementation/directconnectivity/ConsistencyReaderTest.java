// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.NotFoundException;
import com.azure.cosmos.implementation.RequestRateTooLargeException;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.DocumentServiceRequestContext;
import com.azure.cosmos.implementation.FailureValidator;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.IAuthorizationTokenProvider;
import com.azure.cosmos.implementation.ISessionContainer;
import com.azure.cosmos.implementation.ISessionToken;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.RequestChargeTracker;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.StoreResponseBuilder;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.VectorSessionToken;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import io.reactivex.subscribers.TestSubscriber;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.azure.cosmos.implementation.Utils.ValueHolder;
import static org.assertj.core.api.Assertions.assertThat;
import static com.azure.cosmos.implementation.TestUtils.*;

public class ConsistencyReaderTest {
    private final Configs configs = new Configs();
    private static final int TIMEOUT = 30000;
    @DataProvider(name = "deduceReadModeArgProvider")
    public Object[][] deduceReadModeArgProvider() {
        return new Object[][]{
                // account consistency, request consistency, expected readmode, expected consistency to use, whether use session
                {  ConsistencyLevel.STRONG, null, ReadMode.Strong, ConsistencyLevel.STRONG, false},
                {  ConsistencyLevel.STRONG, ConsistencyLevel.EVENTUAL, ReadMode.Any, ConsistencyLevel.EVENTUAL, false},
                {  ConsistencyLevel.STRONG, ConsistencyLevel.SESSION, ReadMode.Any, ConsistencyLevel.SESSION, true},
                {  ConsistencyLevel.SESSION, ConsistencyLevel.EVENTUAL, ReadMode.Any, ConsistencyLevel.EVENTUAL, false},
                {  ConsistencyLevel.SESSION, ConsistencyLevel.SESSION, ReadMode.Any, ConsistencyLevel.SESSION, true},
                {  ConsistencyLevel.SESSION, ConsistencyLevel.EVENTUAL, ReadMode.Any, ConsistencyLevel.EVENTUAL, false},
                {  ConsistencyLevel.SESSION, null, ReadMode.Any, ConsistencyLevel.SESSION, true},
                {  ConsistencyLevel.EVENTUAL, ConsistencyLevel.EVENTUAL, ReadMode.Any, ConsistencyLevel.EVENTUAL, false},
                {  ConsistencyLevel.EVENTUAL, null, ReadMode.Any, ConsistencyLevel.EVENTUAL, false},
        };
    }

    @Test(groups = "unit", dataProvider = "deduceReadModeArgProvider")
    public void deduceReadMode(ConsistencyLevel accountConsistencyLevel, ConsistencyLevel requestConsistency, ReadMode expectedReadMode,
                               ConsistencyLevel expectedConsistencyToUse, boolean expectedToUseSession) {
        AddressSelector addressSelector = Mockito.mock(AddressSelector.class);
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        TransportClient transportClient = Mockito.mock(TransportClient.class);
        GatewayServiceConfiguratorReaderMock gatewayServiceConfigurationReaderWrapper = GatewayServiceConfiguratorReaderMock.from(accountConsistencyLevel);
        IAuthorizationTokenProvider authorizationTokenProvider = Mockito.mock(IAuthorizationTokenProvider.class);
        ConsistencyReader consistencyReader = new ConsistencyReader(mockDiagnosticsClientContext(),
                                                                    configs,
                                                                    addressSelector,
                                                                    sessionContainer,
                                                                    transportClient,
                                                                    gatewayServiceConfigurationReaderWrapper.gatewayServiceConfigurationReader,
                                                                    authorizationTokenProvider);

        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
                OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        if (requestConsistency != null) {
            request.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, requestConsistency.toString());
        }

        ValueHolder<ConsistencyLevel> consistencyLevel = ValueHolder.initialize(null);
        ValueHolder<Boolean> useSession = ValueHolder.initialize(null);

        ReadMode readMode = consistencyReader.deduceReadMode(request, consistencyLevel, useSession);

        assertThat(readMode).isEqualTo(expectedReadMode);
        assertThat(consistencyLevel.v).isEqualTo(expectedConsistencyToUse);
        assertThat(useSession.v).isEqualTo(expectedToUseSession);
    }

    @DataProvider(name = "getMaxReplicaSetSizeArgProvider")
    public Object[][] getMaxReplicaSetSizeArgProvider() {
        return new Object[][]{
                // system max replica count, system min replica count, user max replica count, user min replica, is reading from master operation
                {  4, 3, 4, 3, false },
                {  4, 3, 4, 3, true },

                {  4, 3, 3, 2, false },
                {  4, 3, 3, 2, true }
        };
    }

    @Test(groups = "unit", dataProvider = "getMaxReplicaSetSizeArgProvider")
    public void replicaSizes(int systemMaxReplicaCount,
                                     int systemMinReplicaCount,
                                     int userMaxReplicaCount,
                                     int userMinReplicaCount,
                                     boolean isReadingFromMasterOperation) {
        AddressSelector addressSelector = Mockito.mock(AddressSelector.class);
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        TransportClient transportClient = Mockito.mock(TransportClient.class);
        GatewayServiceConfiguratorReaderMock gatewayServiceConfigurationReaderWrapper = GatewayServiceConfiguratorReaderMock.from(ConsistencyLevel.STRONG,
                                                                                                                        systemMaxReplicaCount,
                                                                                                                        systemMinReplicaCount,
                                                                                                                        userMaxReplicaCount,
                                                                                                                        userMinReplicaCount);
        IAuthorizationTokenProvider authorizationTokenProvider = Mockito.mock(IAuthorizationTokenProvider.class);
        ConsistencyReader consistencyReader = new ConsistencyReader(mockDiagnosticsClientContext(),
                                                                    configs,
                                                                    addressSelector,
                                                                    sessionContainer,
                                                                    transportClient,
                                                                    gatewayServiceConfigurationReaderWrapper.gatewayServiceConfigurationReader,
                                                                    authorizationTokenProvider);

        RxDocumentServiceRequest request;
        if (isReadingFromMasterOperation) {
            request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
                    OperationType.ReadFeed, "/dbs/db/colls/col", ResourceType.DocumentCollection);
        } else {
            request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
                    OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        }

        assertThat(consistencyReader.getMaxReplicaSetSize(request)).isEqualTo(isReadingFromMasterOperation? systemMaxReplicaCount : userMaxReplicaCount);
        assertThat(consistencyReader.getMinReplicaSetSize(request)).isEqualTo(isReadingFromMasterOperation? systemMinReplicaCount : userMinReplicaCount);
    }

    @Test(groups = "unit")
    public void readAny() {
        List<Uri> secondaries = ImmutableList.of(Uri.create("secondary1"), Uri.create("secondary2"), Uri.create("secondary3"));
        Uri primaryAddress = Uri.create("primary");
        AddressSelectorWrapper addressSelectorWrapper = AddressSelectorWrapper.Builder.Simple.create()
                .withPrimary(primaryAddress)
                .withSecondary(secondaries)
                .build();

        StoreResponse primaryResponse = StoreResponseBuilder.create()
                .withLSN(54)
                .withLocalLSN(18)
                .withRequestCharge(1.1)
                .build();
        StoreResponse secondaryResponse1 = StoreResponseBuilder.create()
                .withLSN(53)
                .withLocalLSN(17)
                .withRequestCharge(1.1)
                .build();
        StoreResponse secondaryResponse2 = StoreResponseBuilder.create()
                .withLSN(52)
                .withLocalLSN(16)
                .withRequestCharge(1.1)
                .build();
        StoreResponse secondaryResponse3 = StoreResponseBuilder.create()
                .withLSN(51)
                .withLocalLSN(15)
                .withRequestCharge(1.1)
                .build();
        TransportClientWrapper transportClientWrapper = TransportClientWrapper.Builder.uriToResultBuilder()
                .storeResponseOn(primaryAddress, OperationType.Read, ResourceType.Document, primaryResponse, true)
                .storeResponseOn(secondaries.get(0), OperationType.Read, ResourceType.Document, secondaryResponse1, true)
                .storeResponseOn(secondaries.get(1), OperationType.Read, ResourceType.Document, secondaryResponse2, true)
                .storeResponseOn(secondaries.get(2), OperationType.Read, ResourceType.Document, secondaryResponse3, true)
                .build();

        GatewayServiceConfiguratorReaderMock gatewayServiceConfigurationReaderWrapper = GatewayServiceConfiguratorReaderMock.from(ConsistencyLevel.STRONG);
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);

        IAuthorizationTokenProvider authorizationTokenProvider = Mockito.mock(IAuthorizationTokenProvider.class);
        ConsistencyReaderUnderTest consistencyReader = new ConsistencyReaderUnderTest(addressSelectorWrapper.addressSelector,
                                                                                      sessionContainer,
                                                                                      transportClientWrapper.transportClient,
                                                                                      gatewayServiceConfigurationReaderWrapper.gatewayServiceConfigurationReader,
                                                                                      authorizationTokenProvider);

        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
                OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        request.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, ConsistencyLevel.EVENTUAL.toString());

        TimeoutHelper timeout = Mockito.mock(TimeoutHelper.class);
        boolean forceRefresh = false;
        boolean isInRetry = false;
        Mono<StoreResponse> storeResponseSingle = consistencyReader.readAsync(request, timeout, isInRetry, forceRefresh);

        StoreResponseValidator validator = StoreResponseValidator.create()
                .withBELSNGreaterThanOrEqualTo(51)
                .withRequestCharge(1.1)
                .in(primaryResponse, secondaryResponse1, secondaryResponse2, secondaryResponse3)
                .build();
        validateSuccess(storeResponseSingle, validator);

        Mockito.verifyZeroInteractions(consistencyReader.getSpyQuorumReader());


        Mockito.verify(consistencyReader.getSpyStoreReader(), Mockito.times(1))
                .readMultipleReplicaAsync(Mockito.any(RxDocumentServiceRequest.class),
                                          Mockito.anyBoolean(),
                                          Mockito.anyInt(),
                                          Mockito.anyBoolean(),
                                          Mockito.anyBoolean(),
                                          Mockito.any(),
                                          Mockito.anyBoolean(),
                                          Mockito.anyBoolean());

        transportClientWrapper.validate()
                .verifyNumberOfInvocations(1);

        addressSelectorWrapper.validate()
                .verifyTotalInvocations(1)
                .verifyNumberOfForceCachRefresh(0)
                .verifyVesolvePrimaryUriAsyncCount(0)
                .verifyResolveAllUriAsync(1);
    }

    @Test(groups = "unit")
    public void readSessionConsistency_SomeReplicasLagBehindAndReturningResponseWithLowerLSN_FindAnotherReplica() {
        long slowReplicaLSN = 651176;
        String partitionKeyRangeId = "1";
        long fasterReplicaLSN = 651177;

        List<Uri> secondaries = ImmutableList.of(Uri.create("secondary1"), Uri.create("secondary2"), Uri.create("secondary3"));
        Uri primaryAddress = Uri.create("primary");
        AddressSelectorWrapper addressSelectorWrapper = AddressSelectorWrapper.Builder.Simple.create()
                .withPrimary(primaryAddress)
                .withSecondary(secondaries)
                .build();

        StoreResponse primaryResponse = StoreResponseBuilder.create()
                .withSessionToken(partitionKeyRangeId + ":-1#" + slowReplicaLSN)
                .withLSN(slowReplicaLSN)
                .withLocalLSN(slowReplicaLSN)
                .withQuorumAckecdLsn(slowReplicaLSN)
                .withQuorumAckecdLocalLsn(slowReplicaLSN)
                .withGlobalCommittedLsn(-1)
                .withItemLocalLSN(slowReplicaLSN)
                .withRequestCharge(1.1)
                .build();
        StoreResponse secondaryResponse1 = StoreResponseBuilder.create()
                .withSessionToken(partitionKeyRangeId + ":-1#" + slowReplicaLSN)
                .withLSN(slowReplicaLSN)
                .withLocalLSN(slowReplicaLSN)
                .withQuorumAckecdLsn(slowReplicaLSN)
                .withQuorumAckecdLocalLsn(slowReplicaLSN)
                .withGlobalCommittedLsn(-1)
                .withItemLocalLSN(slowReplicaLSN)
                .withRequestCharge(1.1)
                .build();
        StoreResponse secondaryResponse2 = StoreResponseBuilder.create()
                .withSessionToken(partitionKeyRangeId + ":-1#" + fasterReplicaLSN)
                .withLSN(fasterReplicaLSN)
                .withLocalLSN(fasterReplicaLSN)
                .withQuorumAckecdLsn(fasterReplicaLSN)
                .withQuorumAckecdLocalLsn(fasterReplicaLSN)
                .withGlobalCommittedLsn(-1)
                .withItemLocalLSN(fasterReplicaLSN)
                .withRequestCharge(1.1)
                .build();
        StoreResponse secondaryResponse3 = StoreResponseBuilder.create()
                .withSessionToken(partitionKeyRangeId + ":-1#" + slowReplicaLSN)
                .withLSN(slowReplicaLSN)
                .withLocalLSN(slowReplicaLSN)
                .withQuorumAckecdLsn(slowReplicaLSN)
                .withQuorumAckecdLocalLsn(slowReplicaLSN)
                .withGlobalCommittedLsn(-1)
                .withItemLocalLSN(slowReplicaLSN)
                .withRequestCharge(1.1)
                .build();
        TransportClientWrapper transportClientWrapper = TransportClientWrapper.Builder.uriToResultBuilder()
                .storeResponseOn(primaryAddress, OperationType.Read, ResourceType.Document, primaryResponse, true)
                .storeResponseOn(secondaries.get(0), OperationType.Read, ResourceType.Document, secondaryResponse1, true)
                .storeResponseOn(secondaries.get(1), OperationType.Read, ResourceType.Document, secondaryResponse2, true)
                .storeResponseOn(secondaries.get(2), OperationType.Read, ResourceType.Document, secondaryResponse3, true)
                .build();

        GatewayServiceConfiguratorReaderMock gatewayServiceConfigurationReaderWrapper = GatewayServiceConfiguratorReaderMock.from(ConsistencyLevel.STRONG);
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);

        IAuthorizationTokenProvider authorizationTokenProvider = Mockito.mock(IAuthorizationTokenProvider.class);
        ConsistencyReaderUnderTest consistencyReader = new ConsistencyReaderUnderTest(addressSelectorWrapper.addressSelector,
                                                                                      sessionContainer,
                                                                                      transportClientWrapper.transportClient,
                                                                                      gatewayServiceConfigurationReaderWrapper.gatewayServiceConfigurationReader,
                                                                                      authorizationTokenProvider);

        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
                OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        request.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, ConsistencyLevel.SESSION.toString());
        request.requestContext = new DocumentServiceRequestContext();
        Utils.ValueHolder<ISessionToken> sessionToken = Utils.ValueHolder.initialize(null);
        assertThat(VectorSessionToken.tryCreate("-1#" + fasterReplicaLSN , sessionToken)).isTrue();
        request.requestContext.timeoutHelper = Mockito.mock(TimeoutHelper.class);
        request.requestContext.resolvedPartitionKeyRange = partitionKeyRangeWithId(partitionKeyRangeId);
        request.requestContext.requestChargeTracker = new RequestChargeTracker();

        Mockito.doReturn(sessionToken.v).when(sessionContainer).resolvePartitionLocalSessionToken(Mockito.eq(request), Mockito.anyString());


        TimeoutHelper timeout = Mockito.mock(TimeoutHelper.class);
        boolean forceRefresh = false;
        boolean isInRetry = false;
        Mono<StoreResponse> storeResponseSingle = consistencyReader.readAsync(request, timeout, isInRetry, forceRefresh);

        StoreResponseValidator validator = StoreResponseValidator.create()
                .withBELSN(fasterReplicaLSN)
                .withRequestChargeGreaterThanOrEqualTo(1.1)
                .in(primaryResponse, secondaryResponse1, secondaryResponse2, secondaryResponse3)
                .build();
        validateSuccess(storeResponseSingle, validator);

        Mockito.verifyZeroInteractions(consistencyReader.getSpyQuorumReader());


        Mockito.verify(consistencyReader.getSpyStoreReader(), Mockito.times(1))
                .readMultipleReplicaAsync(Mockito.any(RxDocumentServiceRequest.class),
                                          Mockito.anyBoolean(),
                                          Mockito.anyInt(),
                                          Mockito.anyBoolean(),
                                          Mockito.anyBoolean(),
                                          Mockito.any(),
                                          Mockito.anyBoolean(),
                                          Mockito.anyBoolean());

        assertThat(transportClientWrapper.validate()
                .getNumberOfInvocations())
                .isGreaterThanOrEqualTo(1)
                .isLessThanOrEqualTo(4);

        addressSelectorWrapper.validate()
                .verifyTotalInvocations(1)
                .verifyNumberOfForceCachRefresh(0)
                .verifyVesolvePrimaryUriAsyncCount(0)
                .verifyResolveAllUriAsync(1);
    }

    /**
     * reading in session consistency, if the requested session token cannot be supported by some replicas
     * tries others till we find a replica which can support the given session token
     */
    @Test(groups = "unit")
    public void sessionNotAvailableFromSomeReplicasThrowingNotFound_FindReplicaSatisfyingRequestedSession() {
        long slowReplicaLSN = 651175;
        long globalCommittedLsn = 651174;

        long fasterReplicaLSN = 651176;
        String partitionKeyRangeId = "1";

        NotFoundException foundException = new NotFoundException();
        foundException.getResponseHeaders().put(HttpConstants.HttpHeaders.SESSION_TOKEN, partitionKeyRangeId + ":-1#" + slowReplicaLSN);
        foundException.getResponseHeaders().put(WFConstants.BackendHeaders.LSN, Long.toString(slowReplicaLSN));
        foundException.getResponseHeaders().put(WFConstants.BackendHeaders.LOCAL_LSN, Long.toString(slowReplicaLSN));
        foundException.getResponseHeaders().put(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, Long.toString(globalCommittedLsn));

        StoreResponse storeResponse = StoreResponseBuilder.create()
                .withSessionToken(partitionKeyRangeId + ":-1#" + fasterReplicaLSN)
                .withLSN(fasterReplicaLSN)
                .withLocalLSN(fasterReplicaLSN)
                .withQuorumAckecdLsn(fasterReplicaLSN)
                .withQuorumAckecdLocalLsn(fasterReplicaLSN)
                .withGlobalCommittedLsn(-1)
                .withItemLocalLSN(fasterReplicaLSN)
                .withRequestCharge(1.1)
                .build();

        TransportClientWrapper transportClientWrapper = new TransportClientWrapper.Builder.ReplicaResponseBuilder
                .SequentialBuilder()
                .then(foundException) // 1st replica read returns not found
                .then(foundException) // 2nd replica read returns not found
                .then(foundException) // 3rd replica read returns not found
                .then(storeResponse)  // 4th replica read returns storeResponse satisfying requested session token
                .build();

        Uri primaryUri = Uri.create("primary");
        Uri secondaryUri1 = Uri.create("secondary1");
        Uri secondaryUri2 = Uri.create("secondary2");
        Uri secondaryUri3 = Uri.create("secondary3");

        AddressSelectorWrapper addressSelectorWrapper = AddressSelectorWrapper.Builder.Simple.create()
                .withPrimary(primaryUri)
                .withSecondary(ImmutableList.of(secondaryUri1, secondaryUri2, secondaryUri3))
                .build();
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);

        Configs configs = new Configs();
        GatewayServiceConfiguratorReaderMock gatewayServiceConfigurationReaderWrapper = GatewayServiceConfiguratorReaderMock.from(ConsistencyLevel.STRONG,
                                                                                                                                  4,
                                                                                                                                  3,
                                                                                                                                  4,
                                                                                                                                  3);

        IAuthorizationTokenProvider authTokenProvider = Mockito.mock(IAuthorizationTokenProvider.class);
        ConsistencyReader consistencyReader = new ConsistencyReader(mockDiagnosticsClientContext(),
                                                                    configs,
                                                                    addressSelectorWrapper.addressSelector,
                                                                    sessionContainer,
                                                                    transportClientWrapper.transportClient,
                                                                    gatewayServiceConfigurationReaderWrapper.gatewayServiceConfigurationReader,
                                                                    authTokenProvider);


        TimeoutHelper timeoutHelper = Mockito.mock(TimeoutHelper.class);
        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
                OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, ConsistencyLevel.SESSION.toString());
        dsr.requestContext = new DocumentServiceRequestContext();
        Utils.ValueHolder<ISessionToken> sessionToken = Utils.ValueHolder.initialize(null);
        assertThat(VectorSessionToken.tryCreate("-1#" + fasterReplicaLSN , sessionToken)).isTrue();
        dsr.requestContext.timeoutHelper = timeoutHelper;
        dsr.requestContext.resolvedPartitionKeyRange = partitionKeyRangeWithId(partitionKeyRangeId);
        dsr.requestContext.requestChargeTracker = new RequestChargeTracker();

        Mockito.doReturn(sessionToken.v).when(sessionContainer).resolvePartitionLocalSessionToken(Mockito.eq(dsr), Mockito.anyString());

        Mono<StoreResponse> storeResponseSingle = consistencyReader.readAsync(dsr, timeoutHelper, false, false);

        StoreResponseValidator validator = StoreResponseValidator.create().isSameAs(storeResponse).isSameAs(storeResponse).build();
        validateSuccess(storeResponseSingle, validator);
        transportClientWrapper.verifyNumberOfInvocations(4);
    }

    /**
     * reading in session consistency, if the requested session token cannot be supported by some replicas
     * tries others till we find a replica which can support the given session token
     */
    @Test(groups = "unit")
    public void sessionNotAvailableFromAllReplicasThrowingNotFound_FindReplicaSatisfyingRequestedSessionOnRetry() {
        long slowReplicaLSN = 651175;
        long globalCommittedLsn = 651174;

        long fasterReplicaLSN = 651176;
        String partitionKeyRangeId = "1";

        NotFoundException notFoundException = new NotFoundException();
        notFoundException.getResponseHeaders().put(
            HttpConstants.HttpHeaders.SESSION_TOKEN,
            partitionKeyRangeId + ":-1#" + slowReplicaLSN);
        notFoundException.getResponseHeaders().put(
            WFConstants.BackendHeaders.LSN,
            Long.toString(slowReplicaLSN));
        notFoundException.getResponseHeaders().put(
            WFConstants.BackendHeaders.LOCAL_LSN,
            Long.toString(slowReplicaLSN));
        notFoundException.getResponseHeaders().put(
            WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN,
            Long.toString(globalCommittedLsn));

        StoreResponse storeResponse =
            StoreResponseBuilder.create()
                                .withSessionToken(partitionKeyRangeId + ":-1#" + fasterReplicaLSN)
                                .withLSN(fasterReplicaLSN)
                                .withLocalLSN(fasterReplicaLSN)
                                .withQuorumAckecdLsn(fasterReplicaLSN)
                                .withQuorumAckecdLocalLsn(fasterReplicaLSN)
                                .withGlobalCommittedLsn(-1)
                                .withItemLocalLSN(fasterReplicaLSN)
                                .withRequestCharge(1.1)
                                .build();

        TransportClientWrapper transportClientWrapper = new TransportClientWrapper.Builder.ReplicaResponseBuilder
            .SequentialBuilder()
            .then(notFoundException) // 1st replica read returns not found
            .then(notFoundException) // 2nd replica read returns not found
            .then(notFoundException) // 3rd replica read returns not found
            .then(notFoundException) // 4th replica read returns not found
            .then(notFoundException) // 1st replica read returns not found - 1st retry
            .then(notFoundException) // 2nd replica read returns not found - 1st retry
            .then(notFoundException) // 3rd replica read returns not found - 1st retry
            .then(notFoundException) // 4th replica read returns not found - 1st retry
            .then(notFoundException) // 1st replica read returns not found - 2nd retry
            .then(notFoundException) // 2nd replica read returns not found - 2nd retry
            .then(notFoundException) // 3rd replica read returns not found - 2nd retry
            .then(storeResponse)  // 4th replica returns storeResponse satisfying requested session token on 2nd retry
            .build();

        Uri primaryUri = Uri.create("primary");
        Uri secondaryUri1 = Uri.create("secondary1");
        Uri secondaryUri2 = Uri.create("secondary2");
        Uri secondaryUri3 = Uri.create("secondary3");
        ImmutableList<Uri> secondaryUris = ImmutableList.of(secondaryUri1, secondaryUri2,
            secondaryUri3);

        AddressSelectorWrapper addressSelectorWrapper =
            AddressSelectorWrapper.Builder.Simple.create()
                                                 .withPrimary(primaryUri)
                                                 .withSecondary(secondaryUris)
                                                 .build();
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);

        Configs configs = new Configs();
        GatewayServiceConfiguratorReaderMock gatewayServiceConfigurationReaderWrapper =
            GatewayServiceConfiguratorReaderMock.from(ConsistencyLevel.STRONG,
            4,
            3,
            4,
            3);

        IAuthorizationTokenProvider authTokenProvider = Mockito.mock(IAuthorizationTokenProvider.class);
        ConsistencyReader consistencyReader = new ConsistencyReader(mockDiagnosticsClientContext(),
            configs,
            addressSelectorWrapper.addressSelector,
            sessionContainer,
            transportClientWrapper.transportClient,
            gatewayServiceConfigurationReaderWrapper.gatewayServiceConfigurationReader,
            authTokenProvider);


        TimeoutHelper timeoutHelper = Mockito.mock(TimeoutHelper.class);
        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
            OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, ConsistencyLevel.SESSION.toString());
        dsr.requestContext = new DocumentServiceRequestContext();
        Utils.ValueHolder<ISessionToken> sessionToken = Utils.ValueHolder.initialize(null);
        assertThat(VectorSessionToken.tryCreate("-1#" + fasterReplicaLSN, sessionToken)).isTrue();
        dsr.requestContext.timeoutHelper = timeoutHelper;
        dsr.requestContext.resolvedPartitionKeyRange = partitionKeyRangeWithId(partitionKeyRangeId);
        dsr.requestContext.requestChargeTracker = new RequestChargeTracker();

        Mockito
            .doReturn(sessionToken.v)
            .when(sessionContainer)
            .resolvePartitionLocalSessionToken(Mockito.eq(dsr), Mockito.anyString());

        Mono<StoreResponse> storeResponseSingle =
            consistencyReader.readAsync(dsr, timeoutHelper, false, false);

        StoreResponseValidator validator = StoreResponseValidator
            .create()
            .isSameAs(storeResponse)
            .isSameAs(storeResponse)
            .build();

        validateSuccess(storeResponseSingle, validator);
        transportClientWrapper.verifyNumberOfInvocations(12);
    }

    /**
     * Reading with session consistency, replicas have session token with higher than requested and return not found
     */
    @Test(groups = "unit")
    public void sessionRead_LegitimateNotFound() {
        long lsn = 651175;
        long globalCommittedLsn = 651174;
        String partitionKeyRangeId = "73";

        NotFoundException foundException = new NotFoundException();
        foundException.getResponseHeaders().put(HttpConstants.HttpHeaders.SESSION_TOKEN, partitionKeyRangeId + ":-1#" + lsn);
        foundException.getResponseHeaders().put(WFConstants.BackendHeaders.LSN, Long.toString(lsn));
        foundException.getResponseHeaders().put(WFConstants.BackendHeaders.LOCAL_LSN, Long.toString(lsn));
        foundException.getResponseHeaders().put(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, Long.toString(globalCommittedLsn));

        TransportClientWrapper transportClientWrapper = new TransportClientWrapper.Builder.ReplicaResponseBuilder
                .SequentialBuilder()
                .then(foundException) // 1st replica read returns not found lsn(response) >= lsn(request)
                .then(foundException) // 2nd replica read returns not found lsn(response) >= lsn(request)
                .then(foundException) // 3rd replica read returns not found lsn(response) >= lsn(request)
                .then(foundException) // 4th replica read returns not found lsn(response) >= lsn(request)
                .build();

        Uri primaryUri = Uri.create("primary");
        Uri secondaryUri1 = Uri.create("secondary1");
        Uri secondaryUri2 = Uri.create("secondary2");
        Uri secondaryUri3 = Uri.create("secondary3");

        AddressSelectorWrapper addressSelectorWrapper = AddressSelectorWrapper.Builder.Simple.create()
                .withPrimary(primaryUri)
                .withSecondary(ImmutableList.of(secondaryUri1, secondaryUri2, secondaryUri3))
                .build();
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        TimeoutHelper timeoutHelper = Mockito.mock(TimeoutHelper.class);
        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
                OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, ConsistencyLevel.SESSION.toString());
        dsr.requestContext = new DocumentServiceRequestContext();
        Utils.ValueHolder<ISessionToken> sessionToken = Utils.ValueHolder.initialize(null);
        assertThat(VectorSessionToken.tryCreate("-1#" + lsn , sessionToken)).isTrue();
        dsr.requestContext.timeoutHelper = timeoutHelper;
        dsr.requestContext.resolvedPartitionKeyRange = partitionKeyRangeWithId(partitionKeyRangeId);
        dsr.requestContext.requestChargeTracker = new RequestChargeTracker();

        Mockito.doReturn(sessionToken.v).when(sessionContainer).resolvePartitionLocalSessionToken(Mockito.eq(dsr), Mockito.anyString());

        Configs configs = new Configs();
        GatewayServiceConfiguratorReaderMock gatewayServiceConfigurationReaderWrapper = GatewayServiceConfiguratorReaderMock.from(ConsistencyLevel.STRONG,
                                                                                                                                  4,
                                                                                                                                  3,
                                                                                                                                  4,
                                                                                                                                  3);

        IAuthorizationTokenProvider authTokenProvider = Mockito.mock(IAuthorizationTokenProvider.class);
        ConsistencyReader consistencyReader = new ConsistencyReader(mockDiagnosticsClientContext(),
                                                                    configs,
                                                                    addressSelectorWrapper.addressSelector,
                                                                    sessionContainer,
                                                                    transportClientWrapper.transportClient,
                                                                    gatewayServiceConfigurationReaderWrapper.gatewayServiceConfigurationReader,
                                                                    authTokenProvider);

        Mono<StoreResponse> storeResponseSingle = consistencyReader.readAsync(dsr, timeoutHelper, false, false);

        FailureValidator failureValidator = FailureValidator.builder().resourceNotFound().instanceOf(NotFoundException.class).unknownSubStatusCode().build();
        validateException(storeResponseSingle, failureValidator);
    }

    /**
     * reading in session consistency, no replica support requested lsn
     */
    @Test(groups = "unit")
    public void sessionRead_ReplicasDoNotHaveTheRequestedLSN() {
        long lsn = 651175;
        long globalCommittedLsn = 651174;
        String partitionKeyRangeId = "73";
        NotFoundException foundException = new NotFoundException();
        foundException.getResponseHeaders().put(HttpConstants.HttpHeaders.SESSION_TOKEN, partitionKeyRangeId + ":-1#" + lsn);
        foundException.getResponseHeaders().put(WFConstants.BackendHeaders.LSN, Long.toString(651175));
        foundException.getResponseHeaders().put(WFConstants.BackendHeaders.LOCAL_LSN, Long.toString(651175));
        foundException.getResponseHeaders().put(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, Long.toString(globalCommittedLsn));

        TransportClientWrapper transportClientWrapper = new TransportClientWrapper.Builder.ReplicaResponseBuilder
                .SequentialBuilder()
                .then(foundException) // 1st replica read lsn lags behind the request lsn
                .then(foundException) // 2nd replica read lsn lags behind the request lsn
                .then(foundException) // 3rd replica read lsn lags behind the request lsn
                .then(foundException) // 4th replica read lsn lags behind the request lsn
                .build();

        Uri primaryUri = Uri.create("primary");
        Uri secondaryUri1 = Uri.create("secondary1");
        Uri secondaryUri2 = Uri.create("secondary2");
        Uri secondaryUri3 = Uri.create("secondary3");

        AddressSelectorWrapper addressSelectorWrapper = AddressSelectorWrapper.Builder.Simple.create()
                .withPrimary(primaryUri)
                .withSecondary(ImmutableList.of(secondaryUri1, secondaryUri2, secondaryUri3))
                .build();
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        TimeoutHelper timeoutHelper = Mockito.mock(TimeoutHelper.class);
        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
                OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, ConsistencyLevel.SESSION.toString());
        dsr.requestContext = new DocumentServiceRequestContext();
        Utils.ValueHolder<ISessionToken> sessionToken = Utils.ValueHolder.initialize(null);
        assertThat(VectorSessionToken.tryCreate("-1#" + (lsn + 1) , sessionToken)).isTrue();
        dsr.requestContext.timeoutHelper = timeoutHelper;
        dsr.requestContext.resolvedPartitionKeyRange = partitionKeyRangeWithId(partitionKeyRangeId);
        dsr.requestContext.requestChargeTracker = new RequestChargeTracker();

        Mockito.doReturn(sessionToken.v).when(sessionContainer).resolvePartitionLocalSessionToken(Mockito.eq(dsr), Mockito.anyString());

        Configs configs = new Configs();
        GatewayServiceConfiguratorReaderMock gatewayServiceConfigurationReaderWrapper = GatewayServiceConfiguratorReaderMock.from(ConsistencyLevel.STRONG,
                                                                                                                                  4,
                                                                                                                                  3,
                                                                                                                                  4,
                                                                                                                                  3);

        IAuthorizationTokenProvider authTokenProvider = Mockito.mock(IAuthorizationTokenProvider.class);
        ConsistencyReader consistencyReader = new ConsistencyReader(mockDiagnosticsClientContext(),
                                                                    configs,
                                                                    addressSelectorWrapper.addressSelector,
                                                                    sessionContainer,
                                                                    transportClientWrapper.transportClient,
                                                                    gatewayServiceConfigurationReaderWrapper.gatewayServiceConfigurationReader,
                                                                    authTokenProvider);

        Mono<StoreResponse> storeResponseSingle = consistencyReader.readAsync(dsr, timeoutHelper, false, false);

        FailureValidator failureValidator = FailureValidator.builder().resourceNotFound().instanceOf(NotFoundException.class).subStatusCode(HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE).build();
        validateException(storeResponseSingle, failureValidator);
    }

    @Test(groups = "unit")
    public void requestRateTooLarge_BubbleUp() {
        long lsn = 651175;
        long globalCommittedLsn = 651174;
        String partitionKeyRangeId = "73";

        RequestRateTooLargeException requestTooLargeException = new RequestRateTooLargeException();
        requestTooLargeException.getResponseHeaders().put(HttpConstants.HttpHeaders.SESSION_TOKEN, partitionKeyRangeId + ":-1#" + lsn);
        requestTooLargeException.getResponseHeaders().put(WFConstants.BackendHeaders.LSN, Long.toString(651175));
        requestTooLargeException.getResponseHeaders().put(WFConstants.BackendHeaders.LOCAL_LSN, Long.toString(651175));
        requestTooLargeException.getResponseHeaders().put(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, Long.toString(globalCommittedLsn));

        TransportClientWrapper transportClientWrapper = new TransportClientWrapper.Builder.ReplicaResponseBuilder
                .SequentialBuilder()
                .then(requestTooLargeException) // 1st replica read result in throttling
                .then(requestTooLargeException) // 2nd replica read result in throttling
                .then(requestTooLargeException) // 3rd replica read result in throttling
                .then(requestTooLargeException) // 4th replica read result in throttling
                .build();

        Uri primaryUri = Uri.create("primary");
        Uri secondaryUri1 = Uri.create("secondary1");
        Uri secondaryUri2 = Uri.create("secondary2");
        Uri secondaryUri3 = Uri.create("secondary3");

        AddressSelectorWrapper addressSelectorWrapper = AddressSelectorWrapper.Builder.Simple.create()
                .withPrimary(primaryUri)
                .withSecondary(ImmutableList.of(secondaryUri1, secondaryUri2, secondaryUri3))
                .build();
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        TimeoutHelper timeoutHelper = Mockito.mock(TimeoutHelper.class);
        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
                OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, ConsistencyLevel.SESSION.toString());
        dsr.requestContext = new DocumentServiceRequestContext();
        Utils.ValueHolder<ISessionToken> sessionToken = Utils.ValueHolder.initialize(null);
        assertThat(VectorSessionToken.tryCreate("-1#" + lsn , sessionToken)).isTrue();
        dsr.requestContext.timeoutHelper = timeoutHelper;
        dsr.requestContext.resolvedPartitionKeyRange = partitionKeyRangeWithId(partitionKeyRangeId);
        dsr.requestContext.requestChargeTracker = new RequestChargeTracker();

        Mockito.doReturn(sessionToken.v).when(sessionContainer).resolvePartitionLocalSessionToken(Mockito.eq(dsr), Mockito.anyString());

        Configs configs = new Configs();
        GatewayServiceConfiguratorReaderMock gatewayServiceConfigurationReaderWrapper = GatewayServiceConfiguratorReaderMock.from(ConsistencyLevel.STRONG,
                                                                                                                                  4,
                                                                                                                                  3,
                                                                                                                                  4,
                                                                                                                                  3);

        IAuthorizationTokenProvider authTokenProvider = Mockito.mock(IAuthorizationTokenProvider.class);
        ConsistencyReader consistencyReader = new ConsistencyReader(mockDiagnosticsClientContext(),
                                                                    configs,
                                                                    addressSelectorWrapper.addressSelector,
                                                                    sessionContainer,
                                                                    transportClientWrapper.transportClient,
                                                                    gatewayServiceConfigurationReaderWrapper.gatewayServiceConfigurationReader,
                                                                    authTokenProvider);

        Mono<StoreResponse> storeResponseSingle = consistencyReader.readAsync(dsr, timeoutHelper, false, false);


        FailureValidator failureValidator = FailureValidator.builder().instanceOf(RequestRateTooLargeException.class).unknownSubStatusCode().build();
        validateException(storeResponseSingle, failureValidator);
    }

    @DataProvider(name = "simpleReadStrongArgProvider")
    public Object[][] simpleReadStrongArgProvider() {
        return new Object[][]{
                {  1, ReadMode.Strong },
                {  2, ReadMode.Strong },
                {  3, ReadMode.Strong },
        };
    }

    @Test(groups = "unit", dataProvider = "simpleReadStrongArgProvider")
    public void basicReadStrong_AllReplicasSameLSN(int replicaCountToRead, ReadMode readMode) {
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        Uri primaryReplicaURI = Uri.create("primary");
        ImmutableList<Uri> secondaryReplicaURIs = ImmutableList.of(Uri.create("secondary1"), Uri.create("secondary2"), Uri.create("secondary3"));
        AddressSelectorWrapper addressSelectorWrapper = AddressSelectorWrapper.Builder.Simple.create()
                .withPrimary(primaryReplicaURI)
                .withSecondary(secondaryReplicaURIs)
                .build();

        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
                OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);

        request.requestContext = new DocumentServiceRequestContext();
        request.requestContext.timeoutHelper = Mockito.mock(TimeoutHelper.class);
        request.requestContext.resolvedPartitionKeyRange = Mockito.mock(PartitionKeyRange.class);
        request.requestContext.requestChargeTracker = new RequestChargeTracker();

        BigDecimal requestChargePerRead = new BigDecimal(1.1);

        StoreResponse primaryResponse = StoreResponseBuilder.create()
                .withLSN(51)
                .withLocalLSN(18)
                .withRequestCharge(requestChargePerRead.doubleValue())
                .build();
        StoreResponse secondaryResponse1 = StoreResponseBuilder.create()
                .withLSN(51)
                .withLocalLSN(18)
                .withRequestCharge(requestChargePerRead.doubleValue())
                .build();
        StoreResponse secondaryResponse2 = StoreResponseBuilder.create()
                .withLSN(51)
                .withLocalLSN(18)
                .withRequestCharge(requestChargePerRead.doubleValue())
                .build();
        StoreResponse secondaryResponse3 = StoreResponseBuilder.create()
                .withLSN(51)
                .withLocalLSN(18)
                .withRequestCharge(requestChargePerRead.doubleValue())
                .build();

        TransportClientWrapper transportClientWrapper = TransportClientWrapper.Builder.uriToResultBuilder()
                .storeResponseOn(primaryReplicaURI, OperationType.Read, ResourceType.Document, primaryResponse, false)
                .storeResponseOn(secondaryReplicaURIs.get(0), OperationType.Read, ResourceType.Document, secondaryResponse1, false)
                .storeResponseOn(secondaryReplicaURIs.get(1), OperationType.Read, ResourceType.Document, secondaryResponse2, false)
                .storeResponseOn(secondaryReplicaURIs.get(2), OperationType.Read, ResourceType.Document, secondaryResponse3, false)
                .build();

        StoreReader storeReader = new StoreReader(transportClientWrapper.transportClient, addressSelectorWrapper.addressSelector, sessionContainer);
        GatewayServiceConfigurationReader serviceConfigurator = Mockito.mock(GatewayServiceConfigurationReader.class);
        IAuthorizationTokenProvider authTokenProvider = Mockito.mock(IAuthorizationTokenProvider.class);
        QuorumReader quorumReader = new QuorumReader(mockDiagnosticsClientContext(), configs, transportClientWrapper.transportClient, addressSelectorWrapper.addressSelector, storeReader, serviceConfigurator, authTokenProvider);

        Mono<StoreResponse> storeResponseSingle = quorumReader.readStrongAsync(mockDiagnosticsClientContext(), request, replicaCountToRead, readMode);

        StoreResponseValidator validator = StoreResponseValidator.create()
                .withBELSN(51)
                .withRequestCharge(requestChargePerRead.multiply(BigDecimal.valueOf(replicaCountToRead)).setScale(2, RoundingMode.FLOOR).doubleValue())
                .build();
        validateSuccess(storeResponseSingle, validator);

        transportClientWrapper.validate()
                .verifyNumberOfInvocations(replicaCountToRead);
        addressSelectorWrapper.validate()
                .verifyNumberOfForceCachRefresh(0)
                .verifyVesolvePrimaryUriAsyncCount(0)
                .verifyTotalInvocations(1);
    }

    // TODO: add more mocking tests for when one replica lags behind and we need to do barrier request.

    public static void validateSuccess(Mono<List<StoreResult>> single,
                                       MultiStoreResultValidator validator) {
        validateSuccess(single, validator, 10000);
    }

    public static void validateSuccess(Mono<List<StoreResult>> single,
                                       MultiStoreResultValidator validator,
                                       long timeout) {
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
        validateSuccess(single, validator, 10000);
    }

    public static void validateSuccess(Mono<StoreResponse> single,
                                       StoreResponseValidator validator,
                                       long timeout) {
        TestSubscriber<StoreResponse> testSubscriber = new TestSubscriber<>();

        single.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNoErrors();
        testSubscriber.assertComplete();
        testSubscriber.assertValueCount(1);
        validator.validate(testSubscriber.values().get(0));
    }


    public static <T> void validateException(Mono<T> single,
                                             FailureValidator validator,
                                             long timeout) {
        TestSubscriber<T> testSubscriber = new TestSubscriber<>();

        single.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNotComplete();
        testSubscriber.assertTerminated();
        assertThat(testSubscriber.errorCount()).isEqualTo(1);
        validator.validate(testSubscriber.errors().get(0));
    }

    public static <T> void validateException(Mono<T> single,
                                             FailureValidator validator) {
        validateException(single, validator, TIMEOUT);
    }

    private PartitionKeyRange partitionKeyRangeWithId(String id) {
        PartitionKeyRange partitionKeyRange = Mockito.mock(PartitionKeyRange.class);
        Mockito.doReturn(id).when(partitionKeyRange).getId();
        return partitionKeyRange;
    }
}
