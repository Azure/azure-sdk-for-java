// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.DocumentServiceRequestContext;
import com.azure.cosmos.implementation.DocumentServiceRequestContextValidator;
import com.azure.cosmos.implementation.DocumentServiceRequestValidator;
import com.azure.cosmos.implementation.IAuthorizationTokenProvider;
import com.azure.cosmos.implementation.ISessionContainer;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.RequestChargeTracker;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.StoreResponseBuilder;
import com.azure.cosmos.implementation.guava25.base.Stopwatch;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import io.reactivex.subscribers.TestSubscriber;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.Assertions.assertThat;

public class QuorumReaderTest {
    private final Duration timeResolution = Duration.ofMillis(10);
    private final Configs configs;

    public QuorumReaderTest() {
        configs = new Configs();
    }

    @DataProvider(name = "simpleReadStrongArgProvider")
    public Object[][] simpleReadStrongArgProvider() {
        return new Object[][]{
                //int replicaCountToRead, ReadMode readMode, Long lsn, Long localLSN
                {  1, ReadMode.Strong, 51l, 18l },
                {  2, ReadMode.Strong, 51l, 18l },
                {  3, ReadMode.Strong, 51l, 18l },

                {  2, ReadMode.Any, 51l, 18l },
                {  1, ReadMode.Any, 51l, 18l },

                {  2, ReadMode.Any, null, 18l },
                {  1, ReadMode.Any, null, 18l },
        };
    }

    private StoreResponse storeResponse(Long lsn, Long localLSN, Double rc) {
        StoreResponseBuilder srb = StoreResponseBuilder.create();
        if (rc != null) {
            srb.withRequestCharge(rc);
        }

        if (lsn != null) {
            srb.withLSN(lsn);
        }

        if (localLSN != null) {
            srb.withLocalLSN(localLSN);
        }

        return srb.build();
    }

    @Test(groups = "unit", dataProvider = "simpleReadStrongArgProvider")
    public void basicReadStrong_AllReplicasSameLSN(int replicaCountToRead, ReadMode readMode, Long lsn, Long localLSN) {
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

        StoreResponse primaryResponse = storeResponse(lsn, localLSN, requestChargePerRead.doubleValue());
        StoreResponse secondaryResponse1 = storeResponse(lsn, localLSN, requestChargePerRead.doubleValue());
        StoreResponse secondaryResponse2 = storeResponse(lsn, localLSN, requestChargePerRead.doubleValue());
        StoreResponse secondaryResponse3 = storeResponse(lsn, localLSN, requestChargePerRead.doubleValue());

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

        StoreResponseValidator.Builder validatorBuilder = StoreResponseValidator.create()
                .withBELocalLSN(localLSN)
                .withRequestCharge(requestChargePerRead.multiply(BigDecimal.valueOf(replicaCountToRead)).setScale(2, RoundingMode.FLOOR).doubleValue());

        if (lsn != null) {
            validatorBuilder.withBELSN(lsn);
        }

        validateSuccess(storeResponseSingle, validatorBuilder.build());

        transportClientWrapper.validate()
                .verifyNumberOfInvocations(replicaCountToRead);
        addressSelectorWrapper.validate()
                .verifyNumberOfForceCachRefresh(0)
                .verifyVesolvePrimaryUriAsyncCount(0)
                .verifyTotalInvocations(1);
    }

    @DataProvider(name = "readStrong_RequestBarrierArgProvider")
    public Object[][] readStrong_RequestBarrierArgProvider() {
        return new Object[][]{
                {  1 },
                {  2 },
                { configs.getMaxNumberOfReadBarrierReadRetries() - 1 },
                { configs.getMaxNumberOfReadBarrierReadRetries() },
        };
    }

    @Test(groups = "unit", dataProvider = "readStrong_RequestBarrierArgProvider")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void readStrong_OnlySecondary_RequestBarrier_Success(int numberOfBarrierRequestTillCatchUp) {
        // scenario: we get lsn l1, l2 where l1 > l2
        // we do barrier request and send it to all replicas till we have two replicas with at least l1 lsn

        ReadMode readMode = ReadMode.Strong;
        int replicaCountToRead = 2;

        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        Uri primaryReplicaURI = Uri.create("primary");
        ImmutableList<Uri> secondaryReplicaURIs = ImmutableList.of(Uri.create("secondary1"), Uri.create("secondary2"));
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
        BigDecimal requestChargePerHead = BigDecimal.ZERO;

        long expectedQuorumLsn = 53;
        long expectedQuorumLocalLSN = 20;

        StoreResponse primaryResponse = StoreResponseBuilder.create()
                .withLSN(expectedQuorumLsn - 2)
                .withLocalLSN(expectedQuorumLocalLSN - 2)
                .withRequestCharge(requestChargePerRead)
                .build();

        TransportClientWrapper.Builder.UriToResultBuilder builder = TransportClientWrapper.Builder.uriToResultBuilder()
                .storeResponseOn(primaryReplicaURI, OperationType.Read, ResourceType.Document, primaryResponse, false);

        // slow replica
        StoreResponse readResponse = StoreResponseBuilder.create()
                .withLSN(expectedQuorumLsn - 1)
                .withLocalLSN(expectedQuorumLocalLSN -1)
                .withRequestCharge(requestChargePerRead)
                .build();
        builder.storeResponseOn(secondaryReplicaURIs.get(0), OperationType.Read, ResourceType.Document, readResponse, false);

        for(int i = 0; i < numberOfBarrierRequestTillCatchUp; i++) {
            int lsnIncrement = (i == numberOfBarrierRequestTillCatchUp - 1) ? 1 : 0;
            readResponse = StoreResponseBuilder.create()
                    .withLSN(expectedQuorumLsn - 1 + lsnIncrement)
                    .withLocalLSN(expectedQuorumLocalLSN - 1 + lsnIncrement)
                    .withRequestCharge(requestChargePerRead)
                    .build();
            builder.storeResponseOn(secondaryReplicaURIs.get(0), OperationType.Read, ResourceType.Document, readResponse, false);

            StoreResponse headResponse = StoreResponseBuilder.create()
                    .withLSN(expectedQuorumLsn - 1 + lsnIncrement)
                    .withLocalLSN(expectedQuorumLocalLSN - 1 + lsnIncrement)
                    .withRequestCharge(requestChargePerHead)
                    .build();
            builder.storeResponseOn(secondaryReplicaURIs.get(0), OperationType.Head, ResourceType.DocumentCollection, headResponse, false);
        }

        // faster replica
        readResponse = StoreResponseBuilder.create()
                .withLSN(expectedQuorumLsn)
                .withLocalLSN(expectedQuorumLocalLSN)
                .withRequestCharge(requestChargePerRead)
                .build();
        builder.storeResponseOn(secondaryReplicaURIs.get(1), OperationType.Read, ResourceType.Document, readResponse, false);
        for(int i = 0; i < numberOfBarrierRequestTillCatchUp; i++) {
            StoreResponse headResponse = StoreResponseBuilder.create()
                    .withLSN(expectedQuorumLsn + 10 * (i + 1))
                    .withLocalLSN(expectedQuorumLocalLSN + 10 * (i + 1))
                    .withRequestCharge(requestChargePerHead)
                    .build();
            builder.storeResponseOn(secondaryReplicaURIs.get(1), OperationType.Head, ResourceType.DocumentCollection, headResponse, false);
        }

        TransportClientWrapper transportClientWrapper = builder.build();

        StoreReader storeReader = new StoreReader(transportClientWrapper.transportClient, addressSelectorWrapper.addressSelector, sessionContainer);

        GatewayServiceConfigurationReader serviceConfigurator = Mockito.mock(GatewayServiceConfigurationReader.class);
        IAuthorizationTokenProvider authTokenProvider = Mockito.mock(IAuthorizationTokenProvider.class);
        QuorumReader quorumReader = new QuorumReader(mockDiagnosticsClientContext(), configs, transportClientWrapper.transportClient, addressSelectorWrapper.addressSelector, storeReader, serviceConfigurator, authTokenProvider);

        int expectedNumberOfReads = 2;
        int expectedNumberOfHeads = 2 * numberOfBarrierRequestTillCatchUp;

        double expectedRequestCharge = requestChargePerRead.multiply(BigDecimal.valueOf(expectedNumberOfReads)).add(
                requestChargePerHead.multiply(BigDecimal.valueOf(expectedNumberOfHeads))).setScale(4, RoundingMode.FLOOR).doubleValue();

        Stopwatch stopwatch = Stopwatch.createStarted();

        Mono<StoreResponse> storeResponseSingle = quorumReader.readStrongAsync(mockDiagnosticsClientContext(), request, replicaCountToRead, readMode);

        StoreResponseValidator validator = StoreResponseValidator.create()
                .withBELSN(expectedQuorumLsn)
                .withRequestCharge(expectedRequestCharge)
                .build();

        validateSuccess(storeResponseSingle, validator);

        assertThat(stopwatch.elapsed().plus(timeResolution)).isGreaterThanOrEqualTo(Duration.ofMillis(
                numberOfBarrierRequestTillCatchUp * configs.getDelayBetweenReadBarrierCallsInMs()));

        transportClientWrapper.validate()
                .verifyNumberOfInvocations(expectedNumberOfReads + expectedNumberOfHeads);
        addressSelectorWrapper.validate()
                .verifyNumberOfForceCachRefresh(0)
                .verifyVesolvePrimaryUriAsyncCount(0)
                .verifyTotalInvocations(1 + numberOfBarrierRequestTillCatchUp);

        AddressSelectorWrapper.InOrderVerification.Verifier addressSelectorVerifier = AddressSelectorWrapper.InOrderVerification.Verifier.builder()
                .resolveAllUriAsync_IncludePrimary(false)
                .resolveAllUriAsync_ForceRefresh(false)
                .build();

        addressSelectorWrapper.getInOrderVerification()
                .verifyNumberOfInvocations(1 + numberOfBarrierRequestTillCatchUp)
                .verifyOnAll(addressSelectorVerifier);

        DocumentServiceRequestValidator requestValidator = DocumentServiceRequestValidator.builder()
                .add(DocumentServiceRequestContextValidator.builder()
                             .qurorumSelectedLSN(0l)
                             .globalCommittedSelectedLSN(0l)
                             .storeResponses(null)
                             .build())
                .build();
        requestValidator.validate(request);
    }

    @DataProvider(name = "readStrong_SecondaryReadBarrierExhausted_ReadBarrierOnPrimary_SuccessArgProvider")
    public Object[][] readStrong_SecondaryReadBarrierExhausted_ReadBarrierOnPrimary_SuccessArgProvider() {
        return new Object[][]{
                {  1 },
                {  2 },
                { configs.getMaxNumberOfReadBarrierReadRetries() - 1 },
                { configs.getMaxNumberOfReadBarrierReadRetries() },
        };
    }

    @Test(groups = "unit", dataProvider = "readStrong_SecondaryReadBarrierExhausted_ReadBarrierOnPrimary_SuccessArgProvider")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void readStrong_SecondaryReadBarrierExhausted_ReadBarrierOnPrimary_Success(int numberOfHeadBarriersWithPrimaryIncludedTillQuorumMet) {
        // scenario: we exhaust all barrier request retries on secondaries
        // after that we start barrier requests including the primary

        int numberOfBarrierRequestTillCatchUp = configs.getMaxNumberOfReadBarrierReadRetries() + numberOfHeadBarriersWithPrimaryIncludedTillQuorumMet;

        ReadMode readMode = ReadMode.Strong;
        int replicaCountToRead = 2;

        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        Uri primaryReplicaURI = Uri.create("primary");
        ImmutableList<Uri> secondaryReplicaURIs = ImmutableList.of(Uri.create("secondary1"), Uri.create("secondary2"));
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
        BigDecimal requestChargePerHead = BigDecimal.ZERO;

        TransportClientWrapper.Builder.UriToResultBuilder builder = TransportClientWrapper.Builder.uriToResultBuilder();

        long expectedQuorumLsn = 53;
        long expectedQuorumLocalLSN = 20;

        for(int i = 0; i < numberOfHeadBarriersWithPrimaryIncludedTillQuorumMet; i++) {
            int lsnIncrement = (i == numberOfHeadBarriersWithPrimaryIncludedTillQuorumMet - 1) ? 1 : 0;
            StoreResponse headResponse = StoreResponseBuilder.create()
                    .withLSN(expectedQuorumLsn - 1 + lsnIncrement)
                    .withLocalLSN(expectedQuorumLocalLSN - 1 + lsnIncrement)
                    .withRequestCharge(requestChargePerHead)
                    .build();
            builder.storeResponseOn(primaryReplicaURI, OperationType.Head, ResourceType.DocumentCollection, headResponse, false);
        }

        // slow replica
        StoreResponse readResponse = StoreResponseBuilder.create()
                .withLSN(expectedQuorumLsn - 1)
                .withLocalLSN(expectedQuorumLocalLSN - 1)
                .withRequestCharge(requestChargePerRead)
                .build();
        builder.storeResponseOn(secondaryReplicaURIs.get(0), OperationType.Read, ResourceType.Document, readResponse, false);

        for(int i = 0; i < numberOfBarrierRequestTillCatchUp; i++) {
            int lsnIncrement = (i == numberOfBarrierRequestTillCatchUp - 1) ? 1 : 0;
            readResponse = StoreResponseBuilder.create()
                    .withLSN(expectedQuorumLsn - 1 + lsnIncrement)
                    .withLocalLSN(expectedQuorumLocalLSN - 1 + lsnIncrement)
                    .withRequestCharge(requestChargePerRead)
                    .build();
            builder.storeResponseOn(secondaryReplicaURIs.get(0), OperationType.Read, ResourceType.Document, readResponse, false);

            StoreResponse headResponse = StoreResponseBuilder.create()
                    .withLSN(expectedQuorumLsn - 1  + lsnIncrement)
                    .withLocalLSN(expectedQuorumLocalLSN - 1 + lsnIncrement)
                    .withRequestCharge(requestChargePerHead)
                    .build();
            builder.storeResponseOn(secondaryReplicaURIs.get(0), OperationType.Head, ResourceType.DocumentCollection, headResponse, false);
        }

        // faster replica
        readResponse = StoreResponseBuilder.create()
                .withLSN(expectedQuorumLsn)
                .withLocalLSN(expectedQuorumLocalLSN)
                .withRequestCharge(requestChargePerRead)
                .build();
        builder.storeResponseOn(secondaryReplicaURIs.get(1), OperationType.Read, ResourceType.Document, readResponse, false);
        for(int i = 0; i < numberOfBarrierRequestTillCatchUp; i++) {
            StoreResponse headResponse = StoreResponseBuilder.create()
                    .withLSN(expectedQuorumLsn + 10 * (i + 1))
                    .withLocalLSN(expectedQuorumLocalLSN + 10 * (i + 1))
                    .withRequestCharge(requestChargePerHead)
                    .build();
            builder.storeResponseOn(secondaryReplicaURIs.get(1), OperationType.Head, ResourceType.DocumentCollection, headResponse, false);
        }

        TransportClientWrapper transportClientWrapper = builder.build();

        StoreReader storeReader = new StoreReader(transportClientWrapper.transportClient, addressSelectorWrapper.addressSelector, sessionContainer);

        GatewayServiceConfigurationReader serviceConfigurator = Mockito.mock(GatewayServiceConfigurationReader.class);
        IAuthorizationTokenProvider authTokenProvider = Mockito.mock(IAuthorizationTokenProvider.class);
        QuorumReader quorumReader = new QuorumReader(mockDiagnosticsClientContext(), configs, transportClientWrapper.transportClient, addressSelectorWrapper.addressSelector, storeReader, serviceConfigurator, authTokenProvider);

        int beforeSecondariesRetriesExhausted_expectedNumberOfReads = 2;
        int beforeSecondariesRetriesExhausted_expectedNumberOfHeads = 2 * configs.getMaxNumberOfReadBarrierReadRetries();

        int numberOfHeadRetriesRequestWhenPrimaryIncluded = 3 * numberOfHeadBarriersWithPrimaryIncludedTillQuorumMet;

        double expectedRequestCharge = requestChargePerRead.multiply(BigDecimal.valueOf(beforeSecondariesRetriesExhausted_expectedNumberOfReads))
                .add(requestChargePerHead.multiply(BigDecimal.valueOf(beforeSecondariesRetriesExhausted_expectedNumberOfHeads)))
                .setScale(4, RoundingMode.FLOOR).doubleValue();

        Stopwatch stopwatch = Stopwatch.createStarted();

        Mono<StoreResponse> storeResponseSingle = quorumReader.readStrongAsync(mockDiagnosticsClientContext(), request, replicaCountToRead, readMode);

        StoreResponseValidator validator = StoreResponseValidator.create()
                .withBELSN(expectedQuorumLsn)
                .withRequestCharge(expectedRequestCharge)
                .build();

        validateSuccess(storeResponseSingle, validator);

        assertThat(stopwatch.elapsed().plus(timeResolution)).isGreaterThanOrEqualTo(Duration.ofMillis(
                numberOfBarrierRequestTillCatchUp * configs.getDelayBetweenReadBarrierCallsInMs()));

        transportClientWrapper.validate()
                .verifyNumberOfInvocations(beforeSecondariesRetriesExhausted_expectedNumberOfReads
                                                   + beforeSecondariesRetriesExhausted_expectedNumberOfHeads
                                                   + numberOfHeadRetriesRequestWhenPrimaryIncluded);
        addressSelectorWrapper.validate()
                .verifyNumberOfForceCachRefresh(0)
                .verifyVesolvePrimaryUriAsyncCount(0)
                .verifyTotalInvocations(1 + numberOfBarrierRequestTillCatchUp);

        AddressSelectorWrapper.InOrderVerification.Verifier primaryNotIncludedVerifier = AddressSelectorWrapper
                .InOrderVerification.Verifier.builder()
                .resolveAllUriAsync_IncludePrimary(false)
                .resolveAllUriAsync_ForceRefresh(false)
                .build();

        AddressSelectorWrapper.InOrderVerification.Verifier primaryIncludedVerifier = AddressSelectorWrapper
                .InOrderVerification.Verifier.builder()
                .resolveAllUriAsync_IncludePrimary(true)
                .resolveAllUriAsync_ForceRefresh(false)
                .build();

        int numberOfAddressResolutionWithoutPrimary = configs.getMaxNumberOfReadBarrierReadRetries()+ 1;
        int numberOfAddressResolutionWithPrimary = 1;

        AddressSelectorWrapper.InOrderVerification ov = addressSelectorWrapper.getInOrderVerification();

        for(int i = 0; i < numberOfAddressResolutionWithoutPrimary; i++) {
            ov.verifyNext(primaryNotIncludedVerifier);
        }

        for(int i = 0; i < numberOfAddressResolutionWithPrimary; i++) {
            ov.verifyNext(primaryIncludedVerifier);
        }

        DocumentServiceRequestValidator requestValidator = DocumentServiceRequestValidator.builder()
                .add(DocumentServiceRequestContextValidator.builder()
                             .qurorumSelectedLSN(0l)
                             .globalCommittedSelectedLSN(0l)
                             .storeResponses(null)
                             .build())
                .build();
        requestValidator.validate(request);
    }

    @Test(groups = "unit")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void readStrong_QuorumNotSelected_ReadPrimary() {
        // scenario: attempts to read from secondaries,
        // only one secondary is available so ends in QuorumNotSelected State
        // reads from Primary and succeeds

        ReadMode readMode = ReadMode.Strong;
        int replicaCountToRead = 2;

        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        Uri primaryReplicaURI = Uri.create("primary");
        ImmutableList<Uri> secondaryReplicaURIs = ImmutableList.of(Uri.create("secondary1"));
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
        BigDecimal requestChargePerHead = BigDecimal.ZERO;

        TransportClientWrapper.Builder.UriToResultBuilder builder = TransportClientWrapper.Builder.uriToResultBuilder();

        long primaryLSN = 52;
        long primaryLocalLSN = 19;

        StoreResponse headResponse = StoreResponseBuilder.create()
                .withLSN(primaryLSN)
                .withLocalLSN(primaryLocalLSN)
                .withHeader(WFConstants.BackendHeaders.CURRENT_REPLICA_SET_SIZE, "2")
                .withHeader(WFConstants.BackendHeaders.QUORUM_ACKED_LSN, Long.toString(primaryLSN))
                .withHeader(WFConstants.BackendHeaders.QUORUM_ACKED_LOCAL_LSN, Long.toString(primaryLocalLSN))
                .withRequestCharge(requestChargePerRead)
                .build();
        builder.storeResponseOn(primaryReplicaURI, OperationType.Read, ResourceType.Document, headResponse, false);

        StoreResponse readResponse = StoreResponseBuilder.create()
                .withLSN(primaryLSN)
                .withLocalLSN(primaryLocalLSN)
                .withRequestCharge(requestChargePerRead)
                .build();
        builder.storeResponseOn(secondaryReplicaURIs.get(0), OperationType.Read, ResourceType.Document, readResponse, false);

        TransportClientWrapper transportClientWrapper = builder.build();

        StoreReader storeReader = new StoreReader(transportClientWrapper.transportClient, addressSelectorWrapper.addressSelector, sessionContainer);

        GatewayServiceConfigurationReader serviceConfigurator = Mockito.mock(GatewayServiceConfigurationReader.class);
        IAuthorizationTokenProvider authTokenProvider = Mockito.mock(IAuthorizationTokenProvider.class);
        QuorumReader quorumReader = new QuorumReader(mockDiagnosticsClientContext(), configs, transportClientWrapper.transportClient, addressSelectorWrapper.addressSelector, storeReader, serviceConfigurator, authTokenProvider);

        double expectedRequestCharge = requestChargePerRead.multiply(BigDecimal.valueOf(1))
                .add(requestChargePerHead.multiply(BigDecimal.valueOf(0)))
                .setScale(4, RoundingMode.FLOOR).doubleValue();

        Mono<StoreResponse> storeResponseSingle = quorumReader.readStrongAsync(mockDiagnosticsClientContext(), request, replicaCountToRead, readMode);

        StoreResponseValidator validator = StoreResponseValidator.create()
                .withBELSN(primaryLSN)
                .withBELocalLSN(primaryLocalLSN)
                .withRequestCharge(expectedRequestCharge)
                .build();

        validateSuccess(storeResponseSingle, validator);

        transportClientWrapper.validate()
                .verifyNumberOfInvocations(1);

        addressSelectorWrapper.validate()
                .verifyNumberOfForceCachRefresh(0)
                .verifyVesolvePrimaryUriAsyncCount(1)
                .verifyTotalInvocations(2);

        AddressSelectorWrapper.InOrderVerification.Verifier primaryNotIncludedVerifier = AddressSelectorWrapper
                .InOrderVerification.Verifier.builder()
                .resolveAllUriAsync_IncludePrimary(false)
                .resolveAllUriAsync_ForceRefresh(false)
                .build();

        AddressSelectorWrapper.InOrderVerification.Verifier resolvePrimaryVerifier = AddressSelectorWrapper
                .InOrderVerification.Verifier.builder()
                .resolvePrimaryUriAsync()
                .build();

        AddressSelectorWrapper.InOrderVerification ov = addressSelectorWrapper.getInOrderVerification();
        ov.verifyNext(primaryNotIncludedVerifier);
        ov.verifyNext(resolvePrimaryVerifier);

        DocumentServiceRequestValidator requestValidator = DocumentServiceRequestValidator.builder()
                .add(DocumentServiceRequestContextValidator.builder()
                             .qurorumSelectedLSN(0l)
                             .globalCommittedSelectedLSN(0l)
                             .storeResponses(null)
                             .build())
                .build();
        requestValidator.validate(request);
    }

    @DataProvider(name = "readPrimaryArgProvider")
    public Object[][] readPrimaryArgProvider() {
        return new Object[][]{
                // endpoint, verifier for endpoint expected result, verifying the StoreResponse returned
                {
                        EndpointMock.noSecondaryReplicaBuilder()
                                .response(StoreResponseBuilder.create()
                                                  .withLSN(52)
                                                  .withLocalLSN(19)
                                                  .withHeader(WFConstants.BackendHeaders.CURRENT_REPLICA_SET_SIZE, "1")
                                                  .withHeader(WFConstants.BackendHeaders.QUORUM_ACKED_LSN, "19")
                                                  .withHeader(WFConstants.BackendHeaders.QUORUM_ACKED_LOCAL_LSN, "19")
                                                  .withRequestCharge(0)
                                                  .build())
                                .build(),

                        EndpointMock.EndpointMockVerificationBuilder.builder()
                                .withAddressSelectorValidation(AddressSelectorWrapper
                                                                       .InOrderVerificationBuilder
                                                                       .create()
                                                                       .verifyNumberOfInvocations(2)
                                                                       .verifyNext(AddressSelectorWrapper.InOrderVerification.Verifier.builder()
                                                                                           .resolveAllUriAsync_IncludePrimary(false)
                                                                                           .resolveAllUriAsync_ForceRefresh(false)
                                                                                           .build())
                                                                       .verifyNext(AddressSelectorWrapper.InOrderVerification.Verifier.builder()
                                                                                           .resolvePrimaryUriAsync()
                                                                                           .build()))
                                .withTransportClientValidation(TransportClientWrapper.TransportClientWrapperVerificationBuilder.create().verifyNumberOfInvocations(1)),

                        StoreResponseValidator.create()
                                .withBELSN(52)
                                .build()
                }
        };
    }

    @Test(groups = "unit", dataProvider = "readPrimaryArgProvider")
    public void readPrimary(EndpointMock endpointMock,
                            EndpointMock.EndpointMockVerificationBuilder verification,
                            StoreResponseValidator storeResponseValidator) {
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);

        GatewayServiceConfigurationReader serviceConfigurator = Mockito.mock(GatewayServiceConfigurationReader.class);
        IAuthorizationTokenProvider authTokenProvider = Mockito.mock(IAuthorizationTokenProvider.class);

        QuorumReader quorumReader = new QuorumReader(mockDiagnosticsClientContext(), configs, endpointMock.transportClientWrapper.transportClient,
                                                     endpointMock.addressSelectorWrapper.addressSelector,
                                                     new StoreReader(endpointMock.transportClientWrapper.transportClient,
                                                                     endpointMock.addressSelectorWrapper.addressSelector,
                                                                     sessionContainer),
                                                     serviceConfigurator,
                                                     authTokenProvider);

        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
                OperationType.Read, "/dbs/db/colls/col/docs/docId", ResourceType.Document);

        request.requestContext = new DocumentServiceRequestContext();
        request.requestContext.timeoutHelper = Mockito.mock(TimeoutHelper.class);
        request.requestContext.resolvedPartitionKeyRange = Mockito.mock(PartitionKeyRange.class);
        request.requestContext.requestChargeTracker = new RequestChargeTracker();

        int replicaCountToRead = 1;
        ReadMode readMode = ReadMode.Strong;
        Mono<StoreResponse> storeResponseSingle = quorumReader.readStrongAsync(mockDiagnosticsClientContext(), request, replicaCountToRead, readMode);

        validateSuccess(storeResponseSingle, storeResponseValidator);
        endpointMock.validate(verification);
    }

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
}
