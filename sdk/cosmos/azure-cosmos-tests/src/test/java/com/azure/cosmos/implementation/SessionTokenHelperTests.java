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

public class SessionTokenHelperTests {
    @Test(groups = "unit")
    public void scopesCompleteVectorsWithoutChangingCallerProgress() {
        PartitionKeyRange first = new PartitionKeyRange("0", "", "80");
        PartitionKeyRange second = new PartitionKeyRange("1", "80", "FF");
        CollectionRoutingMap map = routingMap(first, second);
        String original = "0:1#100#1=90#2=-1,1:2#20#3=18";

        assertThat(SessionTokenHelper.tryScopeExplicitSessionToken(original, first, map).convertToString())
            .isEqualTo("1#100#1=90#2=-1");
        assertThat(SessionTokenHelper.tryScopeExplicitSessionToken(original, second, map).convertToString())
            .isEqualTo("2#20#3=18");
        assertThat(original).isEqualTo("0:1#100#1=90#2=-1,1:2#20#3=18");
    }

    @Test(groups = "unit")
    public void mergesAllKnownParentsAndTargetUsingJavaVectorSemantics() {
        PartitionKeyRange target = new PartitionKeyRange("2", "", "FF", Arrays.asList("0", "1"));
        String original = "0:1#100#1=90#2=5,1:1#110#1=80#2=100,2:1#105#1=95#2=50";
        ISessionToken result = SessionTokenHelper.tryScopeExplicitSessionToken(original, target, routingMap(target));
        VectorSessionToken vector = (VectorSessionToken) result;
        assertThat(vector.getVersion()).isEqualTo(1);
        assertThat(vector.getLSN()).isEqualTo(110);
        assertThat(vector.getLocalLsnByRegion()).containsEntry(1, 95L).containsEntry(2, 100L);
    }

    @Test(groups = "unit")
    public void splitChildrenInheritOriginalParentProgress() {
        PartitionKeyRange left = new PartitionKeyRange("2", "", "40", Arrays.asList("0"));
        PartitionKeyRange right = new PartitionKeyRange("3", "40", "80", Arrays.asList("0"));
        CollectionRoutingMap map = routingMap(left, right, new PartitionKeyRange("1", "80", "FF"));
        String original = "0:1#100#1=90,1:1#200#2=150";
        for (PartitionKeyRange child : Arrays.asList(left, right)) {
            assertThat(SessionTokenHelper.tryScopeExplicitSessionToken(original, child, map).convertToString())
                .isEqualTo("1#100#1=90");
        }
    }

    @DataProvider
    public Object[][] unsafeTokens() {
        return new Object[][] {
            {"0:1#100,unknown:1#200"}, {"1:1#100,1:1#200"}, {"0:1#100#1=90#01=1,1:1#200"},
            {"0:1#100##1=90,1:1#200"}, {"0:1#100,1:future-format"}, {"0:1#100,"},
            {":1#100,1:1#200"}, {"0:1#100,1:1#200:extra"}, {"0:1#100#1==90,1:1#200"},
            {"0:100,1:200"}, {"1:1#100"}
        };
    }

    @Test(groups = "unit", dataProvider = "unsafeTokens")
    public void declinesUnsafeScoping(String token) {
        PartitionKeyRange target = new PartitionKeyRange("0", "", "80");
        assertThat(SessionTokenHelper.tryScopeExplicitSessionToken(
            token, target, routingMap(target, new PartitionKeyRange("1", "80", "FF")))).isNull();
    }

    @Test(groups = "unit")
    public void declinesUnknownLineageOfflineRangesAndIncompatibleVectors() {
        PartitionKeyRange target = new PartitionKeyRange("2", "", "80", Arrays.asList("1"));
        PartitionKeyRange other = new PartitionKeyRange("3", "80", "FF", Arrays.asList("0"));
        CollectionRoutingMap map = routingMap(target, other);
        assertThat(SessionTokenHelper.tryScopeExplicitSessionToken("0:1#300,2:1#100", target, map)).isNull();
        assertThat(SessionTokenHelper.tryScopeExplicitSessionToken(
            "1:1#100#1=90,2:1#110#2=100", target, map)).isNull();
        target.set("status", "offline");
        assertThat(SessionTokenHelper.tryScopeExplicitSessionToken("1:1#300,2:1#100", target, map)).isNull();
        assertThat(SessionTokenHelper.tryScopeExplicitSessionToken("1:1#300,2:1#100", target, null)).isNull();
        assertThat(SessionTokenHelper.tryScopeExplicitSessionToken("1:1#300,2:1#100", null, map)).isNull();
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
