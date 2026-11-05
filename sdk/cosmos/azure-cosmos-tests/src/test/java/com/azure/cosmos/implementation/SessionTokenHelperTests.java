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

import java.util.Arrays;
import java.util.HashMap;
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

        ISessionToken scoped = SessionTokenHelper.getLocalSessionToken(request, original, target.getId());
        assertThat(scoped.convertToString()).isEqualTo(expected);
        assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo(original);
        SessionTokenHelper.setPartitionLocalSessionToken(request, Mockito.mock(ISessionContainer.class));
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

        assertThat(SessionTokenHelper.getLocalSessionToken(request, original, target.getId())).isNull();
        assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo(original);
        SessionTokenHelper.setPartitionLocalSessionToken(request, Mockito.mock(ISessionContainer.class));
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
        return SessionTokenHelper.getLocalSessionToken(request, original, target.getId());
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
