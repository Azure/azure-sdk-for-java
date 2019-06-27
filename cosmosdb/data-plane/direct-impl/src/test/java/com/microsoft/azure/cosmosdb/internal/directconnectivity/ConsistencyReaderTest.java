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

import com.google.common.collect.ImmutableList;
import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.ISessionContainer;
import com.microsoft.azure.cosmosdb.PartitionKeyRange;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.internal.ISessionToken;
import com.microsoft.azure.cosmosdb.internal.OperationType;
import com.microsoft.azure.cosmosdb.internal.RequestChargeTracker;
import com.microsoft.azure.cosmosdb.internal.ResourceType;
import com.microsoft.azure.cosmosdb.internal.VectorSessionToken;
import com.microsoft.azure.cosmosdb.rx.FailureValidator;
import com.microsoft.azure.cosmosdb.rx.internal.Configs;
import com.microsoft.azure.cosmosdb.rx.internal.DocumentServiceRequestContext;
import com.microsoft.azure.cosmosdb.rx.internal.IAuthorizationTokenProvider;
import com.microsoft.azure.cosmosdb.rx.internal.NotFoundException;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceRequest;
import com.microsoft.azure.cosmosdb.rx.internal.Utils;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import rx.Single;
import rx.observers.TestSubscriber;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.microsoft.azure.cosmosdb.rx.internal.Utils.ValueHolder;
import static org.assertj.core.api.Assertions.assertThat;

public class ConsistencyReaderTest {
    private final Configs configs = new Configs();
    private static final int TIMEOUT = 30000;
    @DataProvider(name = "deduceReadModeArgProvider")
    public Object[][] deduceReadModeArgProvider() {
        return new Object[][]{
                // account consistency, request consistency, expected readmode, expected consistency to use, whether use session
                {  ConsistencyLevel.Strong, null, ReadMode.Strong, ConsistencyLevel.Strong, false},
                {  ConsistencyLevel.Strong, ConsistencyLevel.Eventual, ReadMode.Any, ConsistencyLevel.Eventual, false},
                {  ConsistencyLevel.Strong, ConsistencyLevel.Session, ReadMode.Any, ConsistencyLevel.Session, true},
                {  ConsistencyLevel.Session, ConsistencyLevel.Eventual, ReadMode.Any, ConsistencyLevel.Eventual, false},
                {  ConsistencyLevel.Session, ConsistencyLevel.Session, ReadMode.Any, ConsistencyLevel.Session, true},
                {  ConsistencyLevel.Session, ConsistencyLevel.Eventual, ReadMode.Any, ConsistencyLevel.Eventual, false},
                {  ConsistencyLevel.Session, null, ReadMode.Any, ConsistencyLevel.Session, true},
                {  ConsistencyLevel.Eventual, ConsistencyLevel.Eventual, ReadMode.Any, ConsistencyLevel.Eventual, false},
                {  ConsistencyLevel.Eventual, null, ReadMode.Any, ConsistencyLevel.Eventual, false},
        };
    }

    @Test(groups = "unit", dataProvider = "deduceReadModeArgProvider")
    public void deduceReadMode(ConsistencyLevel accountConsistencyLevel, ConsistencyLevel requestConsistency, ReadMode expectedReadMode,
                               ConsistencyLevel expectedConsistencyToUse, boolean expectedToUseSession) throws DocumentClientException {
        AddressSelector addressSelector = Mockito.mock(AddressSelector.class);
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        TransportClient transportClient = Mockito.mock(TransportClient.class);
        GatewayServiceConfiguratorReaderMock gatewayServiceConfigurationReaderWrapper = GatewayServiceConfiguratorReaderMock.from(accountConsistencyLevel);
        IAuthorizationTokenProvider authorizationTokenProvider = Mockito.mock(IAuthorizationTokenProvider.class);
        ConsistencyReader consistencyReader = new ConsistencyReader(configs,
                                                                    addressSelector,
                                                                    sessionContainer,
                                                                    transportClient,
                                                                    gatewayServiceConfigurationReaderWrapper.gatewayServiceConfigurationReader,
                                                                    authorizationTokenProvider);

        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(
                OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        if (requestConsistency != null) {
            request.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, requestConsistency.name());
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
        GatewayServiceConfiguratorReaderMock gatewayServiceConfigurationReaderWrapper = GatewayServiceConfiguratorReaderMock.from(ConsistencyLevel.Strong,
                                                                                                                        systemMaxReplicaCount,
                                                                                                                        systemMinReplicaCount,
                                                                                                                        userMaxReplicaCount,
                                                                                                                        userMinReplicaCount);
        IAuthorizationTokenProvider authorizationTokenProvider = Mockito.mock(IAuthorizationTokenProvider.class);
        ConsistencyReader consistencyReader = new ConsistencyReader(configs,
                                                                    addressSelector,
                                                                    sessionContainer,
                                                                    transportClient,
                                                                    gatewayServiceConfigurationReaderWrapper.gatewayServiceConfigurationReader,
                                                                    authorizationTokenProvider);

        RxDocumentServiceRequest request;
        if (isReadingFromMasterOperation) {
            request = RxDocumentServiceRequest.createFromName(
                    OperationType.ReadFeed, "/dbs/db/colls/col", ResourceType.DocumentCollection);
        } else {
            request = RxDocumentServiceRequest.createFromName(
                    OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        }

        assertThat(consistencyReader.getMaxReplicaSetSize(request)).isEqualTo(isReadingFromMasterOperation? systemMaxReplicaCount : userMaxReplicaCount);
        assertThat(consistencyReader.getMinReplicaSetSize(request)).isEqualTo(isReadingFromMasterOperation? systemMinReplicaCount : userMinReplicaCount);
    }

    @Test(groups = "unit")
    public void readAny() {
        List<URI> secondaries = ImmutableList.of(URI.create("secondary1"), URI.create("secondary2"), URI.create("secondary3"));
        URI primaryAddress = URI.create("primary");
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

        GatewayServiceConfiguratorReaderMock gatewayServiceConfigurationReaderWrapper = GatewayServiceConfiguratorReaderMock.from(ConsistencyLevel.Strong);
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);

        IAuthorizationTokenProvider authorizationTokenProvider = Mockito.mock(IAuthorizationTokenProvider.class);
        ConsistencyReaderUnderTest consistencyReader = new ConsistencyReaderUnderTest(addressSelectorWrapper.addressSelector,
                                                                                      sessionContainer,
                                                                                      transportClientWrapper.transportClient,
                                                                                      gatewayServiceConfigurationReaderWrapper.gatewayServiceConfigurationReader,
                                                                                      authorizationTokenProvider);

        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(
                OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        request.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, ConsistencyLevel.Eventual.name());

        TimeoutHelper timeout = Mockito.mock(TimeoutHelper.class);
        boolean forceRefresh = false;
        boolean isInRetry = false;
        Single<StoreResponse> storeResponseSingle = consistencyReader.readAsync(request, timeout, isInRetry, forceRefresh);

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

        List<URI> secondaries = ImmutableList.of(URI.create("secondary1"), URI.create("secondary2"), URI.create("secondary3"));
        URI primaryAddress = URI.create("primary");
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

        GatewayServiceConfiguratorReaderMock gatewayServiceConfigurationReaderWrapper = GatewayServiceConfiguratorReaderMock.from(ConsistencyLevel.Strong);
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);

        IAuthorizationTokenProvider authorizationTokenProvider = Mockito.mock(IAuthorizationTokenProvider.class);
        ConsistencyReaderUnderTest consistencyReader = new ConsistencyReaderUnderTest(addressSelectorWrapper.addressSelector,
                                                                                      sessionContainer,
                                                                                      transportClientWrapper.transportClient,
                                                                                      gatewayServiceConfigurationReaderWrapper.gatewayServiceConfigurationReader,
                                                                                      authorizationTokenProvider);

        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(
                OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        request.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, ConsistencyLevel.Session.name());
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
        Single<StoreResponse> storeResponseSingle = consistencyReader.readAsync(request, timeout, isInRetry, forceRefresh);

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

        URI primaryUri = URI.create("primary");
        URI secondaryUri1 = URI.create("secondary1");
        URI secondaryUri2 = URI.create("secondary2");
        URI secondaryUri3 = URI.create("secondary3");

        AddressSelectorWrapper addressSelectorWrapper = AddressSelectorWrapper.Builder.Simple.create()
                .withPrimary(primaryUri)
                .withSecondary(ImmutableList.of(secondaryUri1, secondaryUri2, secondaryUri3))
                .build();
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);

        Configs configs = new Configs();
        GatewayServiceConfiguratorReaderMock gatewayServiceConfigurationReaderWrapper = GatewayServiceConfiguratorReaderMock.from(ConsistencyLevel.Strong,
                                                                                                                                  4,
                                                                                                                                  3,
                                                                                                                                  4,
                                                                                                                                  3);

        IAuthorizationTokenProvider authTokenProvider = Mockito.mock(IAuthorizationTokenProvider.class);
        ConsistencyReader consistencyReader = new ConsistencyReader(configs,
                                                                    addressSelectorWrapper.addressSelector,
                                                                    sessionContainer,
                                                                    transportClientWrapper.transportClient,
                                                                    gatewayServiceConfigurationReaderWrapper.gatewayServiceConfigurationReader,
                                                                    authTokenProvider);


        TimeoutHelper timeoutHelper = Mockito.mock(TimeoutHelper.class);
        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(
                OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, ConsistencyLevel.Session.name());
        dsr.requestContext = new DocumentServiceRequestContext();
        Utils.ValueHolder<ISessionToken> sessionToken = Utils.ValueHolder.initialize(null);
        assertThat(VectorSessionToken.tryCreate("-1#" + fasterReplicaLSN , sessionToken)).isTrue();
        dsr.requestContext.timeoutHelper = timeoutHelper;
        dsr.requestContext.resolvedPartitionKeyRange = partitionKeyRangeWithId(partitionKeyRangeId);
        dsr.requestContext.requestChargeTracker = new RequestChargeTracker();

        Mockito.doReturn(sessionToken.v).when(sessionContainer).resolvePartitionLocalSessionToken(Mockito.eq(dsr), Mockito.anyString());

        Single<StoreResponse> storeResponseSingle = consistencyReader.readAsync(dsr, timeoutHelper, false, false);

        StoreResponseValidator validator = StoreResponseValidator.create().isSameAs(storeResponse).isSameAs(storeResponse).build();
        validateSuccess(storeResponseSingle, validator);
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

        URI primaryUri = URI.create("primary");
        URI secondaryUri1 = URI.create("secondary1");
        URI secondaryUri2 = URI.create("secondary2");
        URI secondaryUri3 = URI.create("secondary3");

        AddressSelectorWrapper addressSelectorWrapper = AddressSelectorWrapper.Builder.Simple.create()
                .withPrimary(primaryUri)
                .withSecondary(ImmutableList.of(secondaryUri1, secondaryUri2, secondaryUri3))
                .build();
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        TimeoutHelper timeoutHelper = Mockito.mock(TimeoutHelper.class);
        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(
                OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, ConsistencyLevel.Session.name());
        dsr.requestContext = new DocumentServiceRequestContext();
        Utils.ValueHolder<ISessionToken> sessionToken = Utils.ValueHolder.initialize(null);
        assertThat(VectorSessionToken.tryCreate("-1#" + lsn , sessionToken)).isTrue();
        dsr.requestContext.timeoutHelper = timeoutHelper;
        dsr.requestContext.resolvedPartitionKeyRange = partitionKeyRangeWithId(partitionKeyRangeId);
        dsr.requestContext.requestChargeTracker = new RequestChargeTracker();

        Mockito.doReturn(sessionToken.v).when(sessionContainer).resolvePartitionLocalSessionToken(Mockito.eq(dsr), Mockito.anyString());

        Configs configs = new Configs();
        GatewayServiceConfiguratorReaderMock gatewayServiceConfigurationReaderWrapper = GatewayServiceConfiguratorReaderMock.from(ConsistencyLevel.Strong,
                                                                                                                                  4,
                                                                                                                                  3,
                                                                                                                                  4,
                                                                                                                                  3);

        IAuthorizationTokenProvider authTokenProvider = Mockito.mock(IAuthorizationTokenProvider.class);
        ConsistencyReader consistencyReader = new ConsistencyReader(configs,
                                                                    addressSelectorWrapper.addressSelector,
                                                                    sessionContainer,
                                                                    transportClientWrapper.transportClient,
                                                                    gatewayServiceConfigurationReaderWrapper.gatewayServiceConfigurationReader,
                                                                    authTokenProvider);

        Single<StoreResponse> storeResponseSingle = consistencyReader.readAsync(dsr, timeoutHelper, false, false);

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

        URI primaryUri = URI.create("primary");
        URI secondaryUri1 = URI.create("secondary1");
        URI secondaryUri2 = URI.create("secondary2");
        URI secondaryUri3 = URI.create("secondary3");

        AddressSelectorWrapper addressSelectorWrapper = AddressSelectorWrapper.Builder.Simple.create()
                .withPrimary(primaryUri)
                .withSecondary(ImmutableList.of(secondaryUri1, secondaryUri2, secondaryUri3))
                .build();
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        TimeoutHelper timeoutHelper = Mockito.mock(TimeoutHelper.class);
        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(
                OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, ConsistencyLevel.Session.name());
        dsr.requestContext = new DocumentServiceRequestContext();
        Utils.ValueHolder<ISessionToken> sessionToken = Utils.ValueHolder.initialize(null);
        assertThat(VectorSessionToken.tryCreate("-1#" + (lsn + 1) , sessionToken)).isTrue();
        dsr.requestContext.timeoutHelper = timeoutHelper;
        dsr.requestContext.resolvedPartitionKeyRange = partitionKeyRangeWithId(partitionKeyRangeId);
        dsr.requestContext.requestChargeTracker = new RequestChargeTracker();

        Mockito.doReturn(sessionToken.v).when(sessionContainer).resolvePartitionLocalSessionToken(Mockito.eq(dsr), Mockito.anyString());

        Configs configs = new Configs();
        GatewayServiceConfiguratorReaderMock gatewayServiceConfigurationReaderWrapper = GatewayServiceConfiguratorReaderMock.from(ConsistencyLevel.Strong,
                                                                                                                                  4,
                                                                                                                                  3,
                                                                                                                                  4,
                                                                                                                                  3);

        IAuthorizationTokenProvider authTokenProvider = Mockito.mock(IAuthorizationTokenProvider.class);
        ConsistencyReader consistencyReader = new ConsistencyReader(configs,
                                                                    addressSelectorWrapper.addressSelector,
                                                                    sessionContainer,
                                                                    transportClientWrapper.transportClient,
                                                                    gatewayServiceConfigurationReaderWrapper.gatewayServiceConfigurationReader,
                                                                    authTokenProvider);

        Single<StoreResponse> storeResponseSingle = consistencyReader.readAsync(dsr, timeoutHelper, false, false);

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

        URI primaryUri = URI.create("primary");
        URI secondaryUri1 = URI.create("secondary1");
        URI secondaryUri2 = URI.create("secondary2");
        URI secondaryUri3 = URI.create("secondary3");

        AddressSelectorWrapper addressSelectorWrapper = AddressSelectorWrapper.Builder.Simple.create()
                .withPrimary(primaryUri)
                .withSecondary(ImmutableList.of(secondaryUri1, secondaryUri2, secondaryUri3))
                .build();
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        TimeoutHelper timeoutHelper = Mockito.mock(TimeoutHelper.class);
        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(
                OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);
        dsr.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, ConsistencyLevel.Session.name());
        dsr.requestContext = new DocumentServiceRequestContext();
        Utils.ValueHolder<ISessionToken> sessionToken = Utils.ValueHolder.initialize(null);
        assertThat(VectorSessionToken.tryCreate("-1#" + lsn , sessionToken)).isTrue();
        dsr.requestContext.timeoutHelper = timeoutHelper;
        dsr.requestContext.resolvedPartitionKeyRange = partitionKeyRangeWithId(partitionKeyRangeId);
        dsr.requestContext.requestChargeTracker = new RequestChargeTracker();

        Mockito.doReturn(sessionToken.v).when(sessionContainer).resolvePartitionLocalSessionToken(Mockito.eq(dsr), Mockito.anyString());

        Configs configs = new Configs();
        GatewayServiceConfiguratorReaderMock gatewayServiceConfigurationReaderWrapper = GatewayServiceConfiguratorReaderMock.from(ConsistencyLevel.Strong,
                                                                                                                                  4,
                                                                                                                                  3,
                                                                                                                                  4,
                                                                                                                                  3);

        IAuthorizationTokenProvider authTokenProvider = Mockito.mock(IAuthorizationTokenProvider.class);
        ConsistencyReader consistencyReader = new ConsistencyReader(configs,
                                                                    addressSelectorWrapper.addressSelector,
                                                                    sessionContainer,
                                                                    transportClientWrapper.transportClient,
                                                                    gatewayServiceConfigurationReaderWrapper.gatewayServiceConfigurationReader,
                                                                    authTokenProvider);

        Single<StoreResponse> storeResponseSingle = consistencyReader.readAsync(dsr, timeoutHelper, false, false);


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
        URI primaryReplicaURI = URI.create("primary");
        ImmutableList<URI> secondaryReplicaURIs = ImmutableList.of(URI.create("secondary1"), URI.create("secondary2"), URI.create("secondary3"));
        AddressSelectorWrapper addressSelectorWrapper = AddressSelectorWrapper.Builder.Simple.create()
                .withPrimary(primaryReplicaURI)
                .withSecondary(secondaryReplicaURIs)
                .build();

        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(
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
        QuorumReader quorumReader = new QuorumReader(configs, transportClientWrapper.transportClient, addressSelectorWrapper.addressSelector, storeReader, serviceConfigurator, authTokenProvider);

        Single<StoreResponse> storeResponseSingle = quorumReader.readStrongAsync(request, replicaCountToRead, readMode);

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

    public static void validateSuccess(Single<List<StoreResult>> single,
                                       MultiStoreResultValidator validator) {
        validateSuccess(single, validator, 10000);
    }

    public static void validateSuccess(Single<List<StoreResult>> single,
                                       MultiStoreResultValidator validator,
                                       long timeout) {
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
        validateSuccess(single, validator, 10000);
    }

    public static void validateSuccess(Single<StoreResponse> single,
                                       StoreResponseValidator validator,
                                       long timeout) {
        TestSubscriber<StoreResponse> testSubscriber = new TestSubscriber<>();

        single.toObservable().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();
        testSubscriber.assertValueCount(1);
        validator.validate(testSubscriber.getOnNextEvents().get(0));
    }


    public static <T> void validateException(Single<T> single,
                                             FailureValidator validator,
                                             long timeout) {
        TestSubscriber<T> testSubscriber = new TestSubscriber<>();

        single.toObservable().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNotCompleted();
        testSubscriber.assertTerminalEvent();
        assertThat(testSubscriber.getOnErrorEvents()).hasSize(1);
        validator.validate(testSubscriber.getOnErrorEvents().get(0));
    }

    public static <T> void validateException(Single<T> single,
                                             FailureValidator validator) {
        validateException(single, validator, TIMEOUT);
    }

    private PartitionKeyRange partitionKeyRangeWithId(String id) {
        PartitionKeyRange partitionKeyRange = Mockito.mock(PartitionKeyRange.class);
        Mockito.doReturn(id).when(partitionKeyRange).getId();
        return partitionKeyRange;
    }
}
