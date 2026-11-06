// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.ImmutablePair;
import com.azure.cosmos.implementation.routing.CollectionRoutingMap;
import com.azure.cosmos.implementation.routing.InMemoryCollectionRoutingMap;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

public class SessionTokenHelperTests {
    @DataProvider
    public Object[][] compoundSessionTokens() {
        return new Object[][] {
            {null, false}, {"", false}, {" ", false}, {"0:1#100", false},
            {"0:1#100#1=90#2=-1", false}, {"0:future-format", false},
            {"0:1#100,1:1#200", true}, {"0:1#100#1=90,1:1#200#2=180", true},
            {"0:1#100,", true}, {",0:1#100", true}, {",", true}, {"0:1#100,1:future-format", true}
        };
    }

    @Test(groups = "unit", dataProvider = "compoundSessionTokens")
    public void detectsCompoundTokensWithoutParsing(String token, boolean expected) {
        assertThat(SessionTokenHelper.isCompoundSessionToken(token)).isEqualTo(expected);
        assertThat(SessionTokenHelper.createSessionTokenContextReference(token) != null).isEqualTo(expected);
    }

    @DataProvider
    public Object[][] scopedOutcomes() {
        return new Object[][] {{false}, {true}};
    }

    @Test(groups = "unit", dataProvider = "scopedOutcomes")
    public void concurrentAttemptsComputeEachScopedOutcomeOnce(boolean unmatched) throws Exception {
        DocumentServiceRequestContext operationContext = new DocumentServiceRequestContext();
        String original = "0:1#100,1:1#200";
        AtomicInteger evaluations = new AtomicInteger();
        Supplier<ISessionToken> resolver = () -> {
            evaluations.incrementAndGet();
            return unmatched ? null : SessionTokenHelper.parse("1#100");
        };
        ExecutorService executor = Executors.newFixedThreadPool(8);
        CyclicBarrier start = new CyclicBarrier(8);
        try {
            List<Future<ISessionToken>> results = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                RxDocumentServiceRequest request = scopeRequest("collection", "0", Collections.emptyList());
                results.add(executor.submit(() -> {
                    start.await(5, TimeUnit.SECONDS);
                    return operationContext.getOrResolveSessionToken(request, original, "0", resolver);
                }));
            }
            ISessionToken first = results.get(0).get(5, TimeUnit.SECONDS);
            for (Future<ISessionToken> result : results) {
                assertThat(result.get(5, TimeUnit.SECONDS)).isSameAs(first);
            }
            assertThat(evaluations.get()).isEqualTo(1);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test(groups = "unit")
    public void scopesAreIsolatedByOperationTokenCollectionRangeAndParents() {
        DocumentServiceRequestContext operationContext = new DocumentServiceRequestContext();
        String original = "0:1#100,1:1#200";
        AtomicInteger evaluations = new AtomicInteger();
        Supplier<ISessionToken> resolver = () -> {
            evaluations.incrementAndGet();
            return SessionTokenHelper.parse("1#200");
        };
        RxDocumentServiceRequest request = scopeRequest("collection", "2", Arrays.asList("0", "1"));
        ISessionToken first = operationContext.getOrResolveSessionToken(request, original, "2", resolver);
        request.requestContext.resolvedPartitionKeyRange.setParents(Arrays.asList("1", "0"));
        assertThat(operationContext.getOrResolveSessionToken(request, original, "2", resolver)).isSameAs(first);
        assertThat(evaluations.get()).isEqualTo(1);

        request.requestContext.resolvedPartitionKeyRange.setParents(Collections.singletonList("0"));
        assertThat(operationContext.getOrResolveSessionToken(request, original, "2", resolver)).isNotSameAs(first);
        assertThat(evaluations.get()).isEqualTo(2);
        request.requestContext.resolvedPartitionKeyRange = new PartitionKeyRange("3", "", "FF",
            Collections.singletonList("0"));
        operationContext.getOrResolveSessionToken(request, original, "3", resolver);
        assertThat(evaluations.get()).isEqualTo(3);
        request.requestContext.resolvedCollectionRid = "recreated-collection";
        operationContext.getOrResolveSessionToken(request, original, "3", resolver);
        assertThat(evaluations.get()).isEqualTo(4);
        operationContext.getOrResolveSessionToken(request, "0:1#300,1:1#400", "3", resolver);
        assertThat(evaluations.get()).isEqualTo(5);

        RxDocumentServiceRequest originalScope = scopeRequest("collection", "2", Arrays.asList("0", "1"));
        assertThat(operationContext.getOrResolveSessionToken(originalScope, original, "2", resolver)).isNotSameAs(first);
        assertThat(evaluations.get()).isEqualTo(6);
        assertThat(new DocumentServiceRequestContext().getOrResolveSessionToken(originalScope, original, "2", resolver))
            .isNotSameAs(first);
        assertThat(evaluations.get()).isEqualTo(7);
    }

    @Test(groups = "unit")
    public void failedPreparationDoesNotPublishATokenOrShareAnException() {
        DocumentServiceRequestContext operationContext = new DocumentServiceRequestContext();
        RxDocumentServiceRequest request = scopeRequest("collection", "0", Collections.emptyList());
        String original = "0:1#100,1:invalid";
        AtomicInteger evaluations = new AtomicInteger();
        Supplier<ISessionToken> resolver = () -> {
            evaluations.incrementAndGet();
            return SessionTokenHelper.parse("invalid");
        };
        Throwable first = catchThrowable(
            () -> operationContext.getOrResolveSessionToken(request, original, "0", resolver));
        Throwable second = catchThrowable(
            () -> operationContext.getOrResolveSessionToken(request, original, "0", resolver));
        assertThat(first).hasRootCauseInstanceOf(BadRequestException.class);
        assertThat(second).hasRootCauseInstanceOf(BadRequestException.class).isNotSameAs(first);
        assertThat(second.getCause()).isNotSameAs(first.getCause());
        assertThat(evaluations.get()).isEqualTo(2);
    }

    @Test(groups = "unit")
    public void resettingAttemptTokenDoesNotInvalidateSharedProgress() {
        RxDocumentServiceRequest request = scopeRequest("collection", "0", Collections.emptyList());
        DocumentServiceRequestContext operationContext = request.requestContext;
        AtomicInteger evaluations = new AtomicInteger();
        Supplier<ISessionToken> resolver = () -> {
            evaluations.incrementAndGet();
            return SessionTokenHelper.parse("1#100");
        };
        ISessionToken first = operationContext.getOrResolveSessionToken(request, "0:1#100,1:1#200", "0", resolver);
        request.requestContext.sessionToken = first;
        request.requestContext.sessionToken = null;
        assertThat(operationContext.getOrResolveSessionToken(request, "0:1#100,1:1#200", "0", resolver))
            .isSameAs(first);
        assertThat(evaluations.get()).isEqualTo(1);
    }

    private static RxDocumentServiceRequest scopeRequest(String collectionRid, String rangeId, List<String> parents) {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(
            mockDiagnosticsClientContext(), OperationType.Read, "/dbs/db/colls/coll/docs/item", ResourceType.Document);
        request.requestContext.resolvedCollectionRid = collectionRid;
        request.requestContext.resolvedPartitionKeyRange = new PartitionKeyRange(rangeId, "", "FF", parents);
        return request;
    }

    @DataProvider
    public Object[][] localTokenTargets() {
        return new Object[][] {
            {new PartitionKeyRange("0", "", "80"), "1#100#1=90#2=-1"},
            {new PartitionKeyRange("1", "80", "FF"), "1#200#1=180#2=150"},
            {new PartitionKeyRange("2", "", "80", Arrays.asList("0")), "1#100#1=90#2=-1"}
        };
    }

    @Test(groups = "unit", dataProvider = "localTokenTargets")
    public void explicitScopingMatchesDirectModeForKnownTargetAndParent(
        PartitionKeyRange target, String expected) {
        String original = "0:1#100#1=90#2=-1,1:1#200#1=180#2=150";
        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(
            mockDiagnosticsClientContext(), OperationType.Read, "/dbs/db/colls/coll/docs/item", ResourceType.Document);
        request.requestContext.resolvedPartitionKeyRange = target;
        request.getHeaders().put(HttpConstants.HttpHeaders.SESSION_TOKEN, original);

        SessionTokenHelper.setPartitionLocalSessionToken(request, Mockito.mock(ISessionContainer.class));
        assertThat(request.requestContext.sessionToken.convertToString()).isEqualTo(expected);
        assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN))
            .isEqualTo(target.getId() + ":" + expected);
    }

    @Test(groups = "unit")
    public void unmatchedTokenUsesDirectModeDiagnosticsAndRemoval() {
        PartitionKeyRange target = new PartitionKeyRange("2", "", "FF");
        String original = "0:1#100,1:1#200";
        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(
            mockDiagnosticsClientContext(), OperationType.Read, "/dbs/db/colls/coll/docs/item", ResourceType.Document);
        request.requestContext.resolvedPartitionKeyRange = target;
        request.getHeaders().put(HttpConstants.HttpHeaders.SESSION_TOKEN, original);

        SessionTokenHelper.setPartitionLocalSessionToken(request, Mockito.mock(ISessionContainer.class));
        assertThat(request.requestContext.sessionToken).isNull();
        assertThat(request.getHeaders()).doesNotContainKey(HttpConstants.HttpHeaders.SESSION_TOKEN);
        assertThat(request.requestContext.getSessionTokenEvaluationResults()).hasSize(1);
    }

    @Test(groups = "unit")
    public void scopesCompleteVectorsWithoutChangingCallerProgress() {
        PartitionKeyRange first = new PartitionKeyRange("0", "", "80");
        PartitionKeyRange second = new PartitionKeyRange("1", "80", "FF");
        String original = "0:1#100#1=90#2=-1,1:2#20#3=18";

        assertThat(selectToken(original, first).convertToString())
            .isEqualTo("1#100#1=90#2=-1");
        assertThat(selectToken(original, second).convertToString())
            .isEqualTo("2#20#3=18");
        assertThat(original).isEqualTo("0:1#100#1=90#2=-1,1:2#20#3=18");
    }

    @Test(groups = "unit")
    public void mergesAllKnownParentsAndTargetUsingJavaVectorSemantics() {
        PartitionKeyRange target = new PartitionKeyRange("2", "", "FF", Arrays.asList("0", "1"));
        String original = "0:1#100#1=90#2=5,1:1#110#1=80#2=100,2:1#105#1=95#2=50";
        ISessionToken result = selectToken(original, target);
        VectorSessionToken vector = (VectorSessionToken) result;
        assertThat(vector.getVersion()).isEqualTo(1);
        assertThat(vector.getLSN()).isEqualTo(110);
        assertThat(vector.getLocalLsnByRegion()).containsEntry(1, 95L).containsEntry(2, 100L);
    }

    @Test(groups = "unit")
    public void splitChildrenInheritOriginalParentProgress() {
        PartitionKeyRange left = new PartitionKeyRange("2", "", "40", Arrays.asList("0"));
        PartitionKeyRange right = new PartitionKeyRange("3", "40", "80", Arrays.asList("0"));
        String original = "0:1#100#1=90,1:1#200#2=150";
        for (PartitionKeyRange child : Arrays.asList(left, right)) {
            assertThat(selectToken(original, child).convertToString())
                .isEqualTo("1#100#1=90");
        }
    }

    @Test(groups = "unit")
    public void unrelatedRangesAreIgnoredAndIncompatibleVectorsFailLikeDirectMode() {
        PartitionKeyRange target = new PartitionKeyRange("2", "", "80", Arrays.asList("1"));
        assertThat(selectToken("0:1#300,2:1#100", target).convertToString()).isEqualTo("1#100");
        assertThatThrownBy(() -> selectToken("1:1#100#1=90,2:1#110#2=100", target))
            .isInstanceOf(InternalServerErrorException.class);
    }

    private static ISessionToken selectToken(String original, PartitionKeyRange target) {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(
            mockDiagnosticsClientContext(), OperationType.Read, "/dbs/db/colls/coll/docs/item", ResourceType.Document);
        request.requestContext.resolvedPartitionKeyRange = target;
        request.getHeaders().put(HttpConstants.HttpHeaders.SESSION_TOKEN, original);
        SessionTokenHelper.setPartitionLocalSessionToken(request, Mockito.mock(ISessionContainer.class));
        return request.requestContext.sessionToken;
    }

    static CollectionRoutingMap routingMap(PartitionKeyRange... ranges) {
        return InMemoryCollectionRoutingMap.tryCreateCompleteRoutingMap(
            Arrays.stream(ranges).map(range -> new ImmutablePair<PartitionKeyRange,
                com.azure.cosmos.implementation.routing.IServerIdentity>(range, null)).collect(Collectors.toList()),
            "ccZ1ANCszwk=", null);
    }

    @Test(groups = "unit")
    public void missingPartitionKeyRangeIdInContext() {
        try {
            ISessionContainer mockedSessionContainer = Mockito.mock(ISessionContainer.class);

            RxDocumentServiceRequest req =
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document,
                    "/docs/",
                    new Database(),
                    new HashMap<>());

            SessionTokenHelper.setPartitionLocalSessionToken(req, null, mockedSessionContainer);
            Assert.fail("It should fail with internalServerError due to missing pkRangeId");
        } catch (CosmosException cosmosException) {
            assertThat(cosmosException.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR);
            assertThat(cosmosException.getSubStatusCode()).isEqualTo(HttpConstants.SubStatusCodes.MISSING_PARTITION_KEY_RANGE_ID_IN_CONTEXT);
            assertThat(cosmosException.getMessage()).contains(RMResources.PartitionKeyRangeIdAbsentInContext);
        }
    }
}
