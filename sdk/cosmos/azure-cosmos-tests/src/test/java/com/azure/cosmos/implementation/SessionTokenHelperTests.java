// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.ImmutablePair;
import com.azure.cosmos.implementation.routing.CollectionRoutingMap;
import com.azure.cosmos.implementation.routing.InMemoryCollectionRoutingMap;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    }

    @DataProvider
    public Object[][] scopedOutcomes() {
        return new Object[][] {{false}, {true}};
    }

    @Test(groups = "unit", dataProvider = "scopedOutcomes")
    public void explicitSelectionAndApplicationUseExistingRequestFields(boolean unmatched) {
        RxDocumentServiceRequest request = scopeRequest("ccZ1ANCszwk=", "0", Collections.emptyList());
        String original = unmatched ? "1:1#100,1:1#200" : "0:1#100,1:1#200";
        ISessionToken token = SessionTokenHelper.resolveExplicitSessionToken(request, original);
        SessionTokenHelper.applyScopedSessionToken(request, original, token);
        for (int i = 0; i < 8; i++) {
            RxDocumentServiceRequest clone = request.clone();
            assertThat(clone.requestContext.sessionToken).isSameAs(token);
            assertThat(clone.getOriginalSessionToken()).isEqualTo(original);
            assertThat(clone.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN))
                .isEqualTo(unmatched ? null : "0:1#100");
        }
        if (unmatched) {
            assertThat(token).isNull();
        } else {
            assertThat(token.convertToString()).isEqualTo("1#100");
        }
    }

    @Test(groups = "unit")
    public void malformedExplicitProgressDoesNotMutateSiblingRequests() {
        RxDocumentServiceRequest request = scopeRequest("ccZ1ANCszwk=", "1", Collections.emptyList());
        request.setOriginalSessionToken("0:1#100,2:future");
        assertThatThrownBy(() -> SessionTokenHelper.resolveExplicitSessionToken(request, request.getOriginalSessionToken()))
            .hasRootCauseInstanceOf(BadRequestException.class);
        RxDocumentServiceRequest sibling = request.clone();
        sibling.requestContext.resolvedPartitionKeyRange =
            new PartitionKeyRange("2", "80", "FF", Collections.singletonList("1"));
        sibling.setPartitionKeyRangeIdentity(new PartitionKeyRangeIdentity("2"));
        assertThatThrownBy(() -> SessionTokenHelper.resolveExplicitSessionToken(sibling, sibling.getOriginalSessionToken()))
            .hasRootCauseInstanceOf(BadRequestException.class);
        sibling.setOriginalSessionToken("0:1#100,2:1#200");
        SessionTokenHelper.applyScopedSessionToken(sibling, sibling.getOriginalSessionToken(),
            SessionTokenHelper.resolveExplicitSessionToken(sibling, sibling.getOriginalSessionToken()));
        assertThat(sibling.requestContext.sessionToken.convertToString()).isEqualTo("1#200");
        assertThat(request.requestContext.sessionToken).isNull();
        assertThat(request.getOriginalSessionToken()).isEqualTo("0:1#100,2:future");
    }

    private static RxDocumentServiceRequest scopeRequest(String collectionRid, String rangeId, List<String> parents) {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(
            mockDiagnosticsClientContext(), OperationType.Read, "/dbs/db/colls/coll/docs/item", ResourceType.Document);
        request.requestContext.resolvedCollectionRid = collectionRid;
        request.requestContext.resolvedPartitionKeyRange = new PartitionKeyRange(rangeId, "", "FF", parents);
        request.setPartitionKeyRangeIdentity(new PartitionKeyRangeIdentity(rangeId));
        return request;
    }

    @Test(groups = "unit")
    public void recognizesOnlyActiveRangesAndTargetParents() {
        CollectionRoutingMap beforeSplit = Mockito.spy(routingMap(
            new PartitionKeyRange("0", "", "80"), new PartitionKeyRange("1", "80", "FF")));
        String original = "0:1#100,1:1#200";
        RxDocumentServiceRequest request = scopeRequest(beforeSplit.getCollectionUniqueId(), "0", Collections.emptyList());
        assertThat(SessionTokenHelper.areSessionTokenRangesKnown(original, beforeSplit, request.requestContext.resolvedPartitionKeyRange)).isTrue();
        assertThat(SessionTokenHelper.areSessionTokenRangesKnown("0:1#100,2:1#200", beforeSplit, request.requestContext.resolvedPartitionKeyRange)).isFalse();

        CollectionRoutingMap afterSplit = routingMap(
            new PartitionKeyRange("0", "", "80"),
            new PartitionKeyRange("2", "80", "FF", Collections.singletonList("1")));
        request.requestContext.resolvedPartitionKeyRange = afterSplit.getRangeByPartitionKeyRangeId("2");
        assertThat(SessionTokenHelper.areSessionTokenRangesKnown("1:1#100,2:1#200", afterSplit, request.requestContext.resolvedPartitionKeyRange)).isTrue();
        assertThat(SessionTokenHelper.areSessionTokenRangesKnown("1:1#100,3:1#200", afterSplit, request.requestContext.resolvedPartitionKeyRange)).isFalse();
        CollectionRoutingMap unrelatedParent = routingMap(
            new PartitionKeyRange("2", "", "80", Collections.singletonList("1")),
            new PartitionKeyRange("3", "80", "FF", Collections.singletonList("0")));
        request.requestContext.resolvedPartitionKeyRange = unrelatedParent.getRangeByPartitionKeyRangeId("2");
        assertThat(SessionTokenHelper.areSessionTokenRangesKnown("0:1#100,3:1#200", unrelatedParent, request.requestContext.resolvedPartitionKeyRange)).isFalse();
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
        return routingMap("ccZ1ANCszwk=", ranges);
    }

    static CollectionRoutingMap routingMap(String collectionRid, PartitionKeyRange... ranges) {
        return InMemoryCollectionRoutingMap.tryCreateCompleteRoutingMap(
            Arrays.stream(ranges).map(range -> new ImmutablePair<PartitionKeyRange,
                com.azure.cosmos.implementation.routing.IServerIdentity>(range, null)).collect(Collectors.toList()),
            collectionRid, null);
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
