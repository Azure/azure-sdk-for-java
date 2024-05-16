// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.GatewayTestUtils;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.azure.cosmos.implementation.guava25.collect.ImmutableMap;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.ModelBridgeUtils;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.mockito.Mockito;
import org.mockito.internal.util.collections.Sets;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.TestSuiteBase.logger;
import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Tests for {@link RegionScopedSessionContainer}
 */
public class RegionScopedSessionContainerTest {

    private final static URI DefaultEndpoint = createUrl("https://default.documents.azure.com");
    private final static Pair<URI, String> LocationEastUsEndpointToLocationPair = Pair.of(createUrl("https://contoso-east-us.documents.azure.com"), "eastus");
    private final static Pair<URI, String> LocationEastUs2EndpointToLocationPair = Pair.of(createUrl("https://contoso-east-us-2.documents.azure.com"), "eastus2");
    private final static Pair<URI, String> LocationCentralUsEndpointToLocationPair = Pair.of(createUrl("https://contoso-central-us.documents.azure.com"), "centralus");
    private final static Pair<URI, String> LocationWestUsEndpointToLocationPair = Pair.of(createUrl("https://contoso-west-us.documents.azure.com"), "westus");

    private final static Random random = new Random();

    @DataProvider(name = "sessionContainerDataProvider")
    public Object[][] sessionContainerDataProvider() {
        // 1. A list / series of requests seen by the SDK
        // 2. Write regions configured for the account.
        // 3. Read regions configured for the account.
        // 4. ConnectionPolicy built with list of preferred regions.
        // 5. Is multi write locations configured?
        // 6. pkRangeId for which session token is to be resolved
        // 7. Logical partition / partition key for which the session token is to be resolved (assume partition key definition: /mypk)
        // 8. Expected resolved session token
        // NOTE:
        // 1. region format - region{some serial no} - e,g.: location1, location2, ..., regionN
        return new Object[][] {
            // Case 1: Session token resolution for PK1 should not include progress from East US 2
            // to which PK2 saw cross-regional call on
            {
                ImmutableList.of(
                    constructRequestInstance(
                        OperationType.Read,
                        ResourceType.Document,
                        new PartitionKey("pk1"),
                        new PartitionKeyDefinition().setPaths(ImmutableList.of("/mypk")),
                        "dbs/db1/colls/coll1",
                        LocationEastUsEndpointToLocationPair.getKey(),
                        ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#9#2=11#3=9")
                    ),
                    constructRequestInstance(
                        OperationType.Read,
                        ResourceType.Document,
                        new PartitionKey("pk2"),
                        new PartitionKeyDefinition().setPaths(ImmutableList.of("/mypk")),
                        "dbs/db1/colls/coll1",
                        LocationEastUs2EndpointToLocationPair.getKey(),
                        ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#10#2=16#3=12")
                    ),
                    constructRequestInstance(
                        OperationType.Read,
                        ResourceType.Document,
                        new PartitionKey("pk1"),
                        new PartitionKeyDefinition().setPaths(ImmutableList.of("/mypk")),
                        "dbs/db1/colls/coll1",
                        LocationEastUsEndpointToLocationPair.getKey(),
                        ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#11#2=13#3=12")
                    ),
                    constructRequestInstance(
                        OperationType.Read,
                        ResourceType.Document,
                        new PartitionKey("pk1"),
                        new PartitionKeyDefinition().setPaths(ImmutableList.of("/mypk")),
                        "dbs/db1/colls/coll1",
                        LocationEastUsEndpointToLocationPair.getKey(),
                        ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#12#2=14#3=14")
                    )
                ),
                ImmutableList.of(
                    LocationEastUsEndpointToLocationPair,
                    LocationEastUs2EndpointToLocationPair,
                    LocationCentralUsEndpointToLocationPair),
                ImmutableList.of(
                    LocationEastUsEndpointToLocationPair,
                    LocationEastUs2EndpointToLocationPair,
                    LocationCentralUsEndpointToLocationPair),
                ImmutableList.of(LocationEastUsEndpointToLocationPair.getLeft(), LocationEastUs2EndpointToLocationPair.getLeft(), LocationCentralUsEndpointToLocationPair.getLeft()),
                true,
                "range_0",
                "pk1",
                "1#12#2=14#3=14"
            },
            // Case 2: PK1 itself saw a cross-regional calls (East US and East US 2) but it shouldn't include session
            // information of Central US where PK2 saw a call
            {
                ImmutableList.of(
                    constructRequestInstance(
                        OperationType.Read,
                        ResourceType.Document,
                        new PartitionKey("pk1"),
                        new PartitionKeyDefinition().setPaths(ImmutableList.of("/mypk")),
                        "dbs/db1/colls/coll1",
                        LocationEastUsEndpointToLocationPair.getKey(),
                        ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#9#2=10#3=2")
                    ),
                    constructRequestInstance(
                        OperationType.Read,
                        ResourceType.Document,
                        new PartitionKey("pk2"),
                        new PartitionKeyDefinition().setPaths(ImmutableList.of("/mypk")),
                        "dbs/db1/colls/coll1",
                        LocationCentralUsEndpointToLocationPair.getKey(),
                        ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#10#2=10#3=14")
                    ),
                    constructRequestInstance(
                        OperationType.Read,
                        ResourceType.Document,
                        new PartitionKey("pk1"),
                        new PartitionKeyDefinition().setPaths(ImmutableList.of("/mypk")),
                        "dbs/db1/colls/coll1",
                        LocationEastUs2EndpointToLocationPair.getKey(),
                        ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#12#2=10#3=11")
                    ),
                    constructRequestInstance(
                        OperationType.Read,
                        ResourceType.Document,
                        new PartitionKey("pk1"),
                        new PartitionKeyDefinition().setPaths(ImmutableList.of("/mypk")),
                        "dbs/db1/colls/coll1",
                        LocationEastUsEndpointToLocationPair.getKey(),
                        ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#11#2=11#3=11")
                    )
                ),
                ImmutableList.of(
                    LocationEastUsEndpointToLocationPair,
                    LocationEastUs2EndpointToLocationPair,
                    LocationCentralUsEndpointToLocationPair),
                ImmutableList.of(
                    LocationEastUsEndpointToLocationPair,
                    LocationEastUs2EndpointToLocationPair,
                    LocationCentralUsEndpointToLocationPair),
                ImmutableList.of(LocationEastUsEndpointToLocationPair.getLeft(), LocationEastUs2EndpointToLocationPair.getLeft(), LocationCentralUsEndpointToLocationPair.getLeft()),
                true,
                "range_0",
                "pk1",
                "1#12#2=11#3=11"
            },
            // Case 3: Session token resolution for a new logical partition - PK3 should use session information only from
            // the base region (East US)
            {
                ImmutableList.of(
                    constructRequestInstance(
                        OperationType.Read,
                        ResourceType.Document,
                        new PartitionKey("pk1"),
                        new PartitionKeyDefinition().setPaths(ImmutableList.of("/mypk")),
                        "dbs/db1/colls/coll1",
                        LocationEastUsEndpointToLocationPair.getKey(),
                        ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#13#2=14#3=15")
                    ),
                    constructRequestInstance(
                        OperationType.Read,
                        ResourceType.Document,
                        new PartitionKey("pk2"),
                        new PartitionKeyDefinition().setPaths(ImmutableList.of("/mypk")),
                        "dbs/db1/colls/coll1",
                        LocationCentralUsEndpointToLocationPair.getKey(),
                        ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#14#2=21#3=8")
                    ),
                    constructRequestInstance(
                        OperationType.Read,
                        ResourceType.Document,
                        new PartitionKey("pk1"),
                        new PartitionKeyDefinition().setPaths(ImmutableList.of("/mypk")),
                        "dbs/db1/colls/coll1",
                        LocationEastUs2EndpointToLocationPair.getKey(),
                        ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#11#2=14#3=11")
                    ),
                    constructRequestInstance(
                        OperationType.Read,
                        ResourceType.Document,
                        new PartitionKey("pk1"),
                        new PartitionKeyDefinition().setPaths(ImmutableList.of("/mypk")),
                        "dbs/db1/colls/coll1",
                        LocationEastUsEndpointToLocationPair.getKey(),
                        ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#12#2=13#3=13")
                    )
                ),
                ImmutableList.of(
                    LocationEastUsEndpointToLocationPair,
                    LocationEastUs2EndpointToLocationPair,
                    LocationCentralUsEndpointToLocationPair),
                ImmutableList.of(
                    LocationEastUsEndpointToLocationPair,
                    LocationEastUs2EndpointToLocationPair,
                    LocationCentralUsEndpointToLocationPair),
                ImmutableList.of(LocationEastUsEndpointToLocationPair.getLeft(), LocationEastUs2EndpointToLocationPair.getLeft()),
                true,
                "range_0",
                "pk3",
                "1#13#2=14#3=15"
            },
            // Case 4:
            // 1. PK3 got routed to a new region (West US) after the region got added on the account-level
            // this entails a version increment
            // 2. When a subsequent request comes in for PK3 - session information should combine that of East US (base / first preferred region)
            // and that of West US along with version information maxed.
            {
                ImmutableList.of(
                    constructRequestInstance(
                        OperationType.Read,
                        ResourceType.Document,
                        new PartitionKey("pk1"),
                        new PartitionKeyDefinition().setPaths(ImmutableList.of("/mypk")),
                        "dbs/db1/colls/coll1",
                        LocationEastUsEndpointToLocationPair.getKey(),
                        ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#13#2=14#3=15")
                    ),
                    constructRequestInstance(
                        OperationType.Read,
                        ResourceType.Document,
                        new PartitionKey("pk2"),
                        new PartitionKeyDefinition().setPaths(ImmutableList.of("/mypk")),
                        "dbs/db1/colls/coll1",
                        LocationCentralUsEndpointToLocationPair.getKey(),
                        ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#14#2=21#3=8")
                    ),
                    constructRequestInstance(
                        OperationType.Read,
                        ResourceType.Document,
                        new PartitionKey("pk1"),
                        new PartitionKeyDefinition().setPaths(ImmutableList.of("/mypk")),
                        "dbs/db1/colls/coll1",
                        LocationEastUs2EndpointToLocationPair.getKey(),
                        ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#11#2=14#3=11")
                    ),
                    constructRequestInstance(
                        OperationType.Read,
                        ResourceType.Document,
                        new PartitionKey("pk1"),
                        new PartitionKeyDefinition().setPaths(ImmutableList.of("/mypk")),
                        "dbs/db1/colls/coll1",
                        LocationEastUsEndpointToLocationPair.getKey(),
                        ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#12#2=13#3=13")
                    ),
                    constructRequestInstance(
                        OperationType.Read,
                        ResourceType.Document,
                        new PartitionKey("pk3"),
                        new PartitionKeyDefinition().setPaths(ImmutableList.of("/mypk")),
                        "dbs/db1/colls/coll1",
                        LocationWestUsEndpointToLocationPair.getKey(),
                        ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:2#13#2=13#3=13#7=1")
                    )
                ),
                ImmutableList.of(
                    LocationEastUsEndpointToLocationPair,
                    LocationEastUs2EndpointToLocationPair,
                    LocationCentralUsEndpointToLocationPair,
                    LocationWestUsEndpointToLocationPair),
                ImmutableList.of(
                    LocationEastUsEndpointToLocationPair,
                    LocationEastUs2EndpointToLocationPair,
                    LocationCentralUsEndpointToLocationPair,
                    LocationWestUsEndpointToLocationPair),
                ImmutableList.of(LocationEastUsEndpointToLocationPair.getLeft(), LocationEastUs2EndpointToLocationPair.getLeft(), LocationCentralUsEndpointToLocationPair.getLeft(), LocationWestUsEndpointToLocationPair.getLeft()),
                true,
                "range_0",
                "pk3",
                "2#13#2=14#3=15#7=1"
            },
            // Case 5:
            // Session token resolution for a request not targeted to a logical partition should simply use combined session information
            // from all regions
            {
                ImmutableList.of(
                    constructRequestInstance(
                        OperationType.Read,
                        ResourceType.Document,
                        new PartitionKey("pk1"),
                        new PartitionKeyDefinition().setPaths(ImmutableList.of("/mypk")),
                        "dbs/db1/colls/coll1",
                        LocationEastUsEndpointToLocationPair.getKey(),
                        ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#13#2=14#3=15")
                    ),
                    constructRequestInstance(
                        OperationType.Read,
                        ResourceType.Document,
                        new PartitionKey("pk2"),
                        new PartitionKeyDefinition().setPaths(ImmutableList.of("/mypk")),
                        "dbs/db1/colls/coll1",
                        LocationCentralUsEndpointToLocationPair.getKey(),
                        ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#14#2=21#3=8")
                    ),
                    constructRequestInstance(
                        OperationType.Read,
                        ResourceType.Document,
                        new PartitionKey("pk1"),
                        new PartitionKeyDefinition().setPaths(ImmutableList.of("/mypk")),
                        "dbs/db1/colls/coll1",
                        LocationEastUs2EndpointToLocationPair.getKey(),
                        ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#11#2=14#3=11")
                    ),
                    constructRequestInstance(
                        OperationType.Read,
                        ResourceType.Document,
                        new PartitionKey("pk1"),
                        new PartitionKeyDefinition().setPaths(ImmutableList.of("/mypk")),
                        "dbs/db1/colls/coll1",
                        LocationEastUsEndpointToLocationPair.getKey(),
                        ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#12#2=13#3=13")
                    ),
                    constructRequestInstance(
                        OperationType.Read,
                        ResourceType.Document,
                        new PartitionKey("pk3"),
                        new PartitionKeyDefinition().setPaths(ImmutableList.of("/mypk")),
                        "dbs/db1/colls/coll1",
                        LocationWestUsEndpointToLocationPair.getKey(),
                        ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:2#13#2=13#3=13#7=1")
                    )
                ),
                ImmutableList.of(
                    LocationEastUsEndpointToLocationPair,
                    LocationEastUs2EndpointToLocationPair,
                    LocationCentralUsEndpointToLocationPair,
                    LocationWestUsEndpointToLocationPair),
                ImmutableList.of(
                    LocationEastUsEndpointToLocationPair,
                    LocationEastUs2EndpointToLocationPair,
                    LocationCentralUsEndpointToLocationPair,
                    LocationWestUsEndpointToLocationPair),
                ImmutableList.of(LocationEastUsEndpointToLocationPair.getLeft(), LocationEastUs2EndpointToLocationPair.getLeft(), LocationCentralUsEndpointToLocationPair.getLeft(), LocationWestUsEndpointToLocationPair.getLeft()),
                true,
                "range_0",
                null,
                "2#14#2=21#3=15#7=1"
            }
        };
    }

    @Test(groups = "unit")
    public void sessionContainer() throws Exception {
        GlobalEndpointManager globalEndpointManagerMock = Mockito.mock(GlobalEndpointManager.class);
        ISessionContainer sessionContainer = new RegionScopedSessionContainer("127.0.0.1", false, globalEndpointManagerMock);

        int numCollections = 2;
        int numPartitionKeyRangeIds = 5;
        String regionContacted = LocationEastUsEndpointToLocationPair.getRight();

        UnmodifiableList<URI> endpoints = new UnmodifiableList<>(ImmutableList.of(LocationEastUsEndpointToLocationPair.getLeft(), LocationEastUs2EndpointToLocationPair.getLeft(), LocationCentralUsEndpointToLocationPair.getLeft()));

        Mockito.when(globalEndpointManagerMock.getReadEndpoints()).thenReturn(endpoints);
        Mockito.when(globalEndpointManagerMock.getRegionName(Mockito.eq(LocationEastUsEndpointToLocationPair.getLeft()), Mockito.any())).thenReturn(regionContacted);

        for (int i = 0; i < numCollections; i++) {
            String collectionResourceId =
                ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId() + i).getDocumentCollectionId().toString();
            String collectionFullName = "dbs/db1/colls/collName_" + i;

            for (int j = 0; j < numPartitionKeyRangeIds; j++) {

                String partitionKeyRangeId = "range_" + j;
                String lsn = "1#" + j + "#4=90#5=2";
                String resultantSessionToken = partitionKeyRangeId + ":" + lsn;

                RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
                request.requestContext.locationEndpointToRoute = LocationEastUsEndpointToLocationPair.getKey();

                sessionContainer.setSessionToken(
                    request,
                    collectionResourceId,
                    collectionFullName,
                    ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, resultantSessionToken));
            }
        }

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.ReadFeed, ResourceType.DocumentCollection,
            "dbs/db1/colls/collName_1", Utils.getUTF8Bytes("content1"), new HashMap<>());

        request.requestContext.locationEndpointToRoute = LocationEastUsEndpointToLocationPair.getLeft();

        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_1");
        assertThat(sessionToken.getLSN()).isEqualTo(1);

        DocumentServiceRequestContext dsrContext = new DocumentServiceRequestContext();
        PartitionKeyRange resolvedPKRange = new PartitionKeyRange();
        resolvedPKRange.setId("range_" + (numPartitionKeyRangeIds + 10));
        GatewayTestUtils.setParent(resolvedPKRange, ImmutableList.of("range_2", "range_x"));
        dsrContext.resolvedPartitionKeyRange = resolvedPKRange;
        request.requestContext = dsrContext;
        request.requestContext.locationEndpointToRoute = LocationEastUsEndpointToLocationPair.getLeft();

        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, resolvedPKRange.getId());
        assertThat(sessionToken.getLSN()).isEqualTo(2);
    }

    @Test(groups = "unit")
    public void setSessionToken_NoSessionTokenForPartitionKeyRangeId() throws Exception {
        String collectionRid = "uf4PAK6T-Cw=";
        long collectionRidAsLong = ResourceId.parse(collectionRid).getUniqueDocumentCollectionId();
        String partitionKeyRangeId = "test_range_id";
        String sessionToken = "1#100#1=20#2=5#3=30";
        String collectionName = "dbs/db1/colls/collName_1";
        String regionContacted = LocationEastUsEndpointToLocationPair.getRight();
        URI endpointContacted = LocationEastUsEndpointToLocationPair.getLeft();

        GlobalEndpointManager globalEndpointManagerMock = Mockito.mock(GlobalEndpointManager.class);
        ISessionContainer sessionContainer = new RegionScopedSessionContainer("127.0.0.1", false, globalEndpointManagerMock);

        UnmodifiableList<URI> endpoints = new UnmodifiableList<>(ImmutableList.of(LocationEastUsEndpointToLocationPair.getLeft(), LocationEastUs2EndpointToLocationPair.getLeft(), LocationCentralUsEndpointToLocationPair.getLeft()));

        Mockito.when(globalEndpointManagerMock.getReadEndpoints()).thenReturn(endpoints);
        Mockito.when(globalEndpointManagerMock.getRegionName(Mockito.eq(endpointContacted), Mockito.any())).thenReturn(regionContacted);

        RxDocumentServiceRequest request1 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Create, ResourceType.Document,
            collectionName + "/docs",  Utils.getUTF8Bytes("content1"), new HashMap<>());
        request1.requestContext.locationEndpointToRoute = endpointContacted;

        String sessionTokenWithPkRangeIdForRequest1 = partitionKeyRangeId + ":" + sessionToken;

        Map<String, String> respHeaders = new HashMap<>();
        RxDocumentServiceResponse resp = Mockito.mock(RxDocumentServiceResponse.class);
        Mockito.doReturn(respHeaders).when(resp).getResponseHeaders();
        respHeaders.put(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionTokenWithPkRangeIdForRequest1);
        respHeaders.put(HttpConstants.HttpHeaders.OWNER_FULL_NAME, collectionName);
        respHeaders.put(HttpConstants.HttpHeaders.OWNER_ID, collectionRid);

        sessionContainer.setSessionToken(request1, resp.getResponseHeaders());

        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, Long> collectionNameToCollectionResourceId = (ConcurrentHashMap<String, Long>) FieldUtils.readField(sessionContainer, "collectionNameToCollectionResourceId", true);
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<Long, PartitionScopedRegionLevelProgress> collectionResourceIdToRegionScopedSessionTokens = (ConcurrentHashMap<Long, PartitionScopedRegionLevelProgress>) FieldUtils.readField(sessionContainer, "collectionResourceIdToPartitionScopedRegionLevelProgress", true);
        assertThat(collectionNameToCollectionResourceId).hasSize(1);
        assertThat(collectionResourceIdToRegionScopedSessionTokens).hasSize(1);
        assertThat(collectionNameToCollectionResourceId.get(collectionName)).isEqualTo(collectionRidAsLong);
        assertThat(collectionResourceIdToRegionScopedSessionTokens.get(collectionRidAsLong)).isNotNull();
        assertThat(collectionResourceIdToRegionScopedSessionTokens.get(collectionRidAsLong).getPartitionKeyRangeIdToRegionLevelProgress()).isNotNull();
        assertThat(collectionResourceIdToRegionScopedSessionTokens.get(collectionRidAsLong).getPartitionKeyRangeIdToRegionLevelProgress().get(partitionKeyRangeId)).isNotNull();

        PartitionScopedRegionLevelProgress.RegionLevelProgress globalProgress = collectionResourceIdToRegionScopedSessionTokens.get(collectionRidAsLong).getPartitionKeyRangeIdToRegionLevelProgress().get(partitionKeyRangeId).get(PartitionScopedRegionLevelProgress.GLOBAL_PROGRESS_KEY);

        assertThat(globalProgress).isNotNull();
        assertThat(globalProgress.getSessionToken()).isNotNull();
        assertThat(globalProgress.getSessionToken().convertToString()).isNotNull();
        assertThat(globalProgress.getSessionToken().convertToString()).isEqualTo(sessionToken);

        RxDocumentServiceRequest request2 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document,
            collectionName + "/docs",  Utils.getUTF8Bytes(""), new HashMap<>());
        request2.requestContext.locationEndpointToRoute = endpointContacted;

        ISessionToken resolvedSessionToken = sessionContainer.resolvePartitionLocalSessionToken(request2, partitionKeyRangeId);
        assertThat(resolvedSessionToken).isNotNull();
        assertThat(resolvedSessionToken.convertToString()).isEqualTo(sessionToken);
    }

    @Test(groups = "unit")
    public void setSessionToken_MergeOldWithNew() throws Exception {
        String collectionRid = "uf4PAK6T-Cw=";
        String collectionName = "dbs/db1/colls/collName_1";
        String initialSessionToken = "1#100#1=20#2=5#3=30";
        String newSessionTokenInServerResponse = "1#100#1=31#2=5#3=21";
        String partitionKeyRangeId = "test_range_id";
        String expectedMergedSessionToken = "1#100#1=31#2=5#3=30";
        String regionContacted = LocationEastUsEndpointToLocationPair.getRight();
        URI locationEndpointContacted = LocationEastUsEndpointToLocationPair.getLeft();

        Map<String, String> respHeaders = new HashMap<>();

        GlobalEndpointManager globalEndpointManagerMock = Mockito.mock(GlobalEndpointManager.class);
        RegionScopedSessionContainer sessionContainer = new RegionScopedSessionContainer("127.0.0.1", false, globalEndpointManagerMock);

        UnmodifiableList<URI> endpoints = new UnmodifiableList<>(ImmutableList.of(LocationEastUsEndpointToLocationPair.getLeft(), LocationEastUs2EndpointToLocationPair.getLeft(), LocationCentralUsEndpointToLocationPair.getLeft()));

        Mockito.when(globalEndpointManagerMock.getReadEndpoints()).thenReturn(endpoints);
        Mockito.when(globalEndpointManagerMock.getRegionName(Mockito.eq(locationEndpointContacted), Mockito.any())).thenReturn(regionContacted);

        RxDocumentServiceRequest request1 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Create, ResourceType.Document,
            collectionName + "/docs",  Utils.getUTF8Bytes("content1"), new HashMap<>());
        request1.requestContext.locationEndpointToRoute = locationEndpointContacted;

        String initialSessionTokenWithPkRangeId = partitionKeyRangeId + ":" + initialSessionToken;

        RxDocumentServiceResponse resp = Mockito.mock(RxDocumentServiceResponse.class);
        Mockito.doReturn(respHeaders).when(resp).getResponseHeaders();
        respHeaders.put(HttpConstants.HttpHeaders.SESSION_TOKEN, initialSessionTokenWithPkRangeId);
        respHeaders.put(HttpConstants.HttpHeaders.OWNER_FULL_NAME, collectionName);
        respHeaders.put(HttpConstants.HttpHeaders.OWNER_ID, collectionRid);
        sessionContainer.setSessionToken(request1, resp.getResponseHeaders());

        String newSessionTokenWithPkRangeId = partitionKeyRangeId + ":" + newSessionTokenInServerResponse;
        resp = Mockito.mock(RxDocumentServiceResponse.class);
        Mockito.doReturn(respHeaders).when(resp).getResponseHeaders();
        respHeaders.put(HttpConstants.HttpHeaders.SESSION_TOKEN, newSessionTokenWithPkRangeId);
        respHeaders.put(HttpConstants.HttpHeaders.OWNER_FULL_NAME, collectionName);
        respHeaders.put(HttpConstants.HttpHeaders.OWNER_ID, collectionRid);

        sessionContainer.setSessionToken(request1, resp.getResponseHeaders());

        RxDocumentServiceRequest request2 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read, ResourceType.Document,
            collectionName + "/docs", Utils.getUTF8Bytes(""), new HashMap<>());
        request2.requestContext.locationEndpointToRoute = locationEndpointContacted;

        ISessionToken resolvedSessionToken = sessionContainer.resolvePartitionLocalSessionToken(request2, partitionKeyRangeId);
        assertThat(resolvedSessionToken).isNotNull();
        assertThat(resolvedSessionToken.convertToString()).isEqualTo(expectedMergedSessionToken);
    }


    @Test(groups = "unit")
    public void resolveGlobalSessionTokenReturnsEmptyStringOnEmptyCache() {
        RegionScopedSessionContainer sessionContainer = new RegionScopedSessionContainer("127.0.0.1");
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read, ResourceType.Document,
            "dbs/db1/colls/collName/docs/doc1", new HashMap<>());
        assertThat(StringUtils.EMPTY).isEqualTo(sessionContainer.resolveGlobalSessionToken(request));
    }

    @Test(groups = "unit")
    public void resolveGlobalSessionTokenReturnsEmptyStringOnCacheMiss() {

        String partitionKeyRangeId = "range_0";
        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId()).getDocumentCollectionId().toString();
        String initialSessionToken = "1#100#1=20#2=5#3=30";
        String regionContacted = LocationEastUsEndpointToLocationPair.getRight();
        URI locationEndpointContacted = LocationEastUsEndpointToLocationPair.getLeft();
        String resultantSessionToken = partitionKeyRangeId + ":" + initialSessionToken;

        GlobalEndpointManager globalEndpointManagerMock = Mockito.mock(GlobalEndpointManager.class);
        RegionScopedSessionContainer sessionContainer = new RegionScopedSessionContainer("127.0.0.1", false, globalEndpointManagerMock);

        UnmodifiableList<URI> endpoints = new UnmodifiableList<>(ImmutableList.of(LocationEastUsEndpointToLocationPair.getLeft(), LocationEastUs2EndpointToLocationPair.getLeft(), LocationCentralUsEndpointToLocationPair.getLeft()));

        Mockito.when(globalEndpointManagerMock.getReadEndpoints()).thenReturn(endpoints);
        Mockito.when(globalEndpointManagerMock.getRegionName(Mockito.eq(locationEndpointContacted), Mockito.any())).thenReturn(regionContacted);

        RxDocumentServiceRequest collectionCreateRequest = RxDocumentServiceRequest.create(
            mockDiagnosticsClientContext(), OperationType.Create, ResourceType.DocumentCollection);
        collectionCreateRequest.requestContext.locationEndpointToRoute = locationEndpointContacted;

        sessionContainer.setSessionToken(collectionCreateRequest, documentCollectionId, "dbs/db1/colls1/collName",
            ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, partitionKeyRangeId + ":" + initialSessionToken));
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read, ResourceType.Document,
            "dbs/db1/colls1/collName2/docs/doc1", new HashMap<>());
        request.requestContext.locationEndpointToRoute = locationEndpointContacted;

        assertThat(StringUtils.EMPTY).isEqualTo(sessionContainer.resolveGlobalSessionToken(request));
    }

    @Test(groups = "unit")
    public void resolveGlobalSessionTokenReturnsTokenMapUsingName() {
        GlobalEndpointManager globalEndpointManagerMock = Mockito.mock(GlobalEndpointManager.class);
        RegionScopedSessionContainer sessionContainer = new RegionScopedSessionContainer("127.0.0.1", false, globalEndpointManagerMock);

        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId()).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName";
        String regionContacted = LocationEastUsEndpointToLocationPair.getRight();
        URI locationEndpointContacted = LocationEastUsEndpointToLocationPair.getLeft();

        UnmodifiableList<URI> endpoints = new UnmodifiableList<>(ImmutableList.of(LocationEastUsEndpointToLocationPair.getLeft(), LocationEastUs2EndpointToLocationPair.getLeft(), LocationCentralUsEndpointToLocationPair.getLeft()));

        Mockito.when(globalEndpointManagerMock.getReadEndpoints()).thenReturn(endpoints);
        Mockito.when(globalEndpointManagerMock.getRegionName(Mockito.eq(locationEndpointContacted), Mockito.any())).thenReturn(regionContacted);

        RxDocumentServiceRequest request1 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        request1.requestContext.locationEndpointToRoute = locationEndpointContacted;

        String sessionTokenForRequest1 = "range_0:1#100#1=20#2=5#3=30";

        sessionContainer.setSessionToken(request1, documentCollectionId, collectionFullName,
            ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionTokenForRequest1));

        RxDocumentServiceRequest request2 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        request2.requestContext.locationEndpointToRoute = locationEndpointContacted;

        String sessionTokenForRequest2 = "range_1:1#101#1=20#2=5#3=30";

        sessionContainer.setSessionToken(request2, documentCollectionId, collectionFullName,
            ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionTokenForRequest2));

        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),OperationType.Read,
            collectionFullName + "/docs/doc1", ResourceType.Document);
        String sessionToken = sessionContainer.resolveGlobalSessionToken(request);
        Set<String> tokens = Sets.newSet(sessionToken.split(","));

        assertThat(tokens.size()).isEqualTo(2);
        assertThat(tokens.contains("range_0:1#100#1=20#2=5#3=30")).isTrue();
        assertThat(tokens.contains("range_1:1#101#1=20#2=5#3=30")).isTrue();
    }

    @Test(groups = "unit")
    public void resolveGlobalSessionTokenReturnsTokenMapUsingResourceId() {

        GlobalEndpointManager globalEndpointManagerMock = Mockito.mock(GlobalEndpointManager.class);
        RegionScopedSessionContainer sessionContainer = new RegionScopedSessionContainer("127.0.0.1", false, globalEndpointManagerMock);

        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId()).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName";
        String regionContacted = LocationEastUsEndpointToLocationPair.getRight();
        URI locationEndpointContacted = LocationEastUsEndpointToLocationPair.getLeft();

        UnmodifiableList<URI> endpoints = new UnmodifiableList<>(ImmutableList.of(LocationEastUsEndpointToLocationPair.getLeft(), LocationEastUs2EndpointToLocationPair.getLeft(), LocationCentralUsEndpointToLocationPair.getLeft()));

        Mockito.when(globalEndpointManagerMock.getReadEndpoints()).thenReturn(endpoints);
        Mockito.when(globalEndpointManagerMock.getRegionName(Mockito.eq(locationEndpointContacted), Mockito.any())).thenReturn(regionContacted);

        RxDocumentServiceRequest request1 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        request1.requestContext.locationEndpointToRoute = locationEndpointContacted;

        String sessionTokenForRequest1 = "range_0:1#100#1=20#2=5#3=30";

        sessionContainer.setSessionToken(request1, documentCollectionId, collectionFullName,
            ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionTokenForRequest1));

        RxDocumentServiceRequest request2 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        request2.requestContext.locationEndpointToRoute = locationEndpointContacted;

        String sessionTokenForRequest2 = "range_1:1#101#1=20#2=5#3=30";

        sessionContainer.setSessionToken(request2, documentCollectionId, collectionFullName,
            ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionTokenForRequest2));

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
            documentCollectionId, ResourceType.Document, new HashMap<>());
        request.requestContext.locationEndpointToRoute = locationEndpointContacted;

        String sessionToken = sessionContainer.resolveGlobalSessionToken(request);

        Set<String> tokens = Sets.newSet(sessionToken.split(","));
        assertThat(tokens.size()).isEqualTo(2);
        assertThat(tokens.contains(sessionTokenForRequest1)).isTrue();
        assertThat(tokens.contains(sessionTokenForRequest2)).isTrue();
    }


    @Test(groups = "unit")
    public void resolveLocalSessionTokenReturnsTokenMapUsingName() {
        GlobalEndpointManager globalEndpointManagerMock = Mockito.mock(GlobalEndpointManager.class);
        RegionScopedSessionContainer sessionContainer = new RegionScopedSessionContainer("127.0.0.1", false, globalEndpointManagerMock);

        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId()).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName";
        String regionContacted = LocationEastUsEndpointToLocationPair.getRight();
        URI locationEndpointContacted = LocationEastUsEndpointToLocationPair.getLeft();

        UnmodifiableList<URI> endpoints = new UnmodifiableList<>(ImmutableList.of(LocationEastUsEndpointToLocationPair.getLeft(), LocationEastUs2EndpointToLocationPair.getLeft(), LocationCentralUsEndpointToLocationPair.getLeft()));

        Mockito.when(globalEndpointManagerMock.getReadEndpoints()).thenReturn(endpoints);
        Mockito.when(globalEndpointManagerMock.getRegionName(Mockito.eq(locationEndpointContacted), Mockito.any())).thenReturn(regionContacted);

        RxDocumentServiceRequest request1 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        request1.requestContext.locationEndpointToRoute = locationEndpointContacted;

        String sessionTokenForRequest1 = "range_0:1#100#1=20#2=5#3=30";

        sessionContainer.setSessionToken(request1, documentCollectionId, collectionFullName,
            ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionTokenForRequest1));

        RxDocumentServiceRequest request2 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        request2.requestContext.locationEndpointToRoute = locationEndpointContacted;

        String sessionTokenForRequest2 = "range_1:1#101#1=20#2=5#3=30";

        sessionContainer.setSessionToken(request2, documentCollectionId, collectionFullName,
            ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionTokenForRequest2));

        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),OperationType.Read,
            collectionFullName + "/docs/doc1", ResourceType.Document);
        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken.getLSN()).isEqualTo(100);
        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_1");
        assertThat(sessionToken.getLSN()).isEqualTo(101);
    }

    @Test(groups = "unit")
    public void resolveLocalSessionTokenReturnsTokenMapUsingResourceId() {

        GlobalEndpointManager globalEndpointManagerMock = Mockito.mock(GlobalEndpointManager.class);
        RegionScopedSessionContainer sessionContainer = new RegionScopedSessionContainer("127.0.0.1", false, globalEndpointManagerMock);

        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId()).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName";
        String regionContacted = LocationEastUsEndpointToLocationPair.getRight();
        URI locationEndpointContacted = LocationEastUsEndpointToLocationPair.getLeft();

        UnmodifiableList<URI> endpoints = new UnmodifiableList<>(ImmutableList.of(LocationEastUsEndpointToLocationPair.getLeft(), LocationEastUs2EndpointToLocationPair.getLeft(), LocationCentralUsEndpointToLocationPair.getLeft()));

        Mockito.when(globalEndpointManagerMock.getReadEndpoints()).thenReturn(endpoints);
        Mockito.when(globalEndpointManagerMock.getRegionName(Mockito.eq(locationEndpointContacted), Mockito.any())).thenReturn(regionContacted);

        RxDocumentServiceRequest request1 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        request1.requestContext.locationEndpointToRoute = locationEndpointContacted;

        String sessionTokenWithPkRangeIdForRequest1 = "range_0:1#100#1=20#2=5#3=30";

        sessionContainer.setSessionToken(request1, documentCollectionId, collectionFullName,
            ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionTokenWithPkRangeIdForRequest1));

        RxDocumentServiceRequest request2 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        request2.requestContext.locationEndpointToRoute = locationEndpointContacted;

        String sessionTokenWithPkRangeIdForRequest2 = "range_1:1#101#1=20#2=5#3=30";

        sessionContainer.setSessionToken(request2, documentCollectionId, collectionFullName,
            ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionTokenWithPkRangeIdForRequest2));

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
            documentCollectionId, ResourceType.Document, new HashMap<>());
        request.requestContext.locationEndpointToRoute = locationEndpointContacted;

        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken).isNotNull();
        assertThat(sessionToken.getLSN()).isEqualTo(100);

        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_1");
        assertThat(sessionToken).isNotNull();
        assertThat(sessionToken.getLSN()).isEqualTo(101);
    }

    @Test(groups = "unit")
    public void resolveLocalSessionTokenReturnsNullOnPartitionMiss() {

        GlobalEndpointManager globalEndpointManagerMock = Mockito.mock(GlobalEndpointManager.class);

        RegionScopedSessionContainer sessionContainer = new RegionScopedSessionContainer("127.0.0.1", false, globalEndpointManagerMock);
        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId()).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName";
        String regionContacted = LocationEastUsEndpointToLocationPair.getRight();
        URI locationEndpointContacted = LocationEastUsEndpointToLocationPair.getLeft();

        UnmodifiableList<URI> endpoints = new UnmodifiableList<>(ImmutableList.of(LocationEastUsEndpointToLocationPair.getLeft(), LocationEastUs2EndpointToLocationPair.getLeft(), LocationCentralUsEndpointToLocationPair.getLeft()));

        Mockito.when(globalEndpointManagerMock.getReadEndpoints()).thenReturn(endpoints);
        Mockito.when(globalEndpointManagerMock.getRegionName(Mockito.eq(locationEndpointContacted), Mockito.any())).thenReturn(regionContacted);

        RxDocumentServiceRequest request1 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        request1.requestContext.locationEndpointToRoute = locationEndpointContacted;

        String sessionTokenWithPkRangeIdForRequest1 = "range_0:1#100#1=20#2=5#3=30";

        sessionContainer.setSessionToken(request1, documentCollectionId, collectionFullName,
            ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionTokenWithPkRangeIdForRequest1));

        RxDocumentServiceRequest request2 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        request2.requestContext.locationEndpointToRoute = locationEndpointContacted;

        String sessionTokenWithPkRangeIdForRequest2 = "range_1:1#101#1=20#2=5#3=30";

        sessionContainer.setSessionToken(request2, documentCollectionId, collectionFullName,
            ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionTokenWithPkRangeIdForRequest2));

        RxDocumentServiceRequest requestToResultInPkRangeIdBasedMiss = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
            documentCollectionId, ResourceType.Document, new HashMap<>());
        requestToResultInPkRangeIdBasedMiss.requestContext.locationEndpointToRoute = locationEndpointContacted;

        requestToResultInPkRangeIdBasedMiss.requestContext.resolvedPartitionKeyRange = new PartitionKeyRange();
        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(requestToResultInPkRangeIdBasedMiss, "range_2");
        assertThat(sessionToken).isNull();
    }

    @Test(groups = "unit")
    public void resolveLocalSessionTokenReturnsNullOnCollectionMiss() {
        GlobalEndpointManager globalEndpointManagerMock = Mockito.mock(GlobalEndpointManager.class);
        RegionScopedSessionContainer sessionContainer = new RegionScopedSessionContainer("127.0.0.1", false, globalEndpointManagerMock);

        int randomCollectionId = getRandomCollectionId();
        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), randomCollectionId).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName";
        String regionContacted = LocationEastUsEndpointToLocationPair.getRight();
        URI locationEndpointContacted = LocationEastUsEndpointToLocationPair.getLeft();

        UnmodifiableList<URI> endpoints = new UnmodifiableList<>(ImmutableList.of(LocationEastUsEndpointToLocationPair.getLeft(), LocationEastUs2EndpointToLocationPair.getLeft(), LocationCentralUsEndpointToLocationPair.getLeft()));

        Mockito.when(globalEndpointManagerMock.getReadEndpoints()).thenReturn(endpoints);
        Mockito.when(globalEndpointManagerMock.getRegionName(Mockito.eq(locationEndpointContacted), Mockito.any())).thenReturn(regionContacted);

        RxDocumentServiceRequest request1 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        request1.requestContext.locationEndpointToRoute = locationEndpointContacted;

        String sessionTokenWithPkRangeIdForRequest1 = "range_0:1#100#1=20#2=5#3=30";

        sessionContainer.setSessionToken(request1, documentCollectionId, collectionFullName,
            ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionTokenWithPkRangeIdForRequest1));

        RxDocumentServiceRequest request2 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        request2.requestContext.locationEndpointToRoute = locationEndpointContacted;

        String sessionTokenWithPkRangeIdForRequest2 = "range_1:1#101#1=20#2=5#3=30";

        sessionContainer.setSessionToken(request2, documentCollectionId, collectionFullName,
            ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionTokenWithPkRangeIdForRequest2));

        RxDocumentServiceRequest requestToResultInCollectionBasedMiss = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
            ResourceId.newDocumentCollectionId(getRandomDbId(), randomCollectionId - 1).getDocumentCollectionId().toString(),
            ResourceType.Document, new HashMap<>());
        requestToResultInCollectionBasedMiss.requestContext.resolvedPartitionKeyRange = new PartitionKeyRange();
        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(requestToResultInCollectionBasedMiss, "range_1");
        assertThat(sessionToken).isNull();
    }

    @Test(groups = "unit")
    public void resolvePartitionLocalSessionTokenReturnsTokenOnParentMatch() {
        GlobalEndpointManager globalEndpointManagerMock = Mockito.mock(GlobalEndpointManager.class);
        RegionScopedSessionContainer sessionContainer = new RegionScopedSessionContainer("127.0.0.1", false, globalEndpointManagerMock);

        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId()).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName";
        String regionContacted = LocationEastUsEndpointToLocationPair.getRight();
        URI locationEndpointContacted = LocationEastUsEndpointToLocationPair.getLeft();

        UnmodifiableList<URI> endpoints = new UnmodifiableList<>(ImmutableList.of(LocationEastUsEndpointToLocationPair.getLeft(), LocationEastUs2EndpointToLocationPair.getLeft(), LocationCentralUsEndpointToLocationPair.getLeft()));

        Mockito.when(globalEndpointManagerMock.getReadEndpoints()).thenReturn(endpoints);
        Mockito.when(globalEndpointManagerMock.getRegionName(Mockito.eq(locationEndpointContacted), Mockito.any())).thenReturn(regionContacted);

        RxDocumentServiceRequest request1 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        request1.requestContext.locationEndpointToRoute = locationEndpointContacted;

        String sessionTokenWithPkRangeIdForRequest1 = "range_0:1#100#1=20#2=5#3=30";

        sessionContainer.setSessionToken(request1, documentCollectionId, collectionFullName,
            ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionTokenWithPkRangeIdForRequest1));

        RxDocumentServiceRequest request2 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        request2.requestContext.locationEndpointToRoute = locationEndpointContacted;

        String sessionTokenWithPkRangeIdForRequest2 = "range_1:1#101#1=20#2=5#3=30";

        sessionContainer.setSessionToken(request2, documentCollectionId, collectionFullName,
            ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionTokenWithPkRangeIdForRequest2));

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
            documentCollectionId, ResourceType.Document, new HashMap<>());

        request.requestContext.resolvedPartitionKeyRange = new PartitionKeyRange();
        GatewayTestUtils.setParent(request.requestContext.resolvedPartitionKeyRange, ImmutableList.of("range_1"));
        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_2");
        assertThat(sessionToken.getLSN()).isEqualTo(101);
    }

    @Test(groups = "unit")
    public void clearTokenByCollectionFullNameRemovesToken() {

        GlobalEndpointManager globalEndpointManagerMock = Mockito.mock(GlobalEndpointManager.class);

        RegionScopedSessionContainer sessionContainer = new RegionScopedSessionContainer("127.0.0.1", false, globalEndpointManagerMock);
        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId()).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName";
        String regionContacted = LocationEastUsEndpointToLocationPair.getRight();
        URI locationEndpointContacted = LocationEastUsEndpointToLocationPair.getLeft();
        String unparsedSessionToken = "range_0:1#100#1=20#2=5#3=30";

        UnmodifiableList<URI> endpoints = new UnmodifiableList<>(ImmutableList.of(LocationEastUsEndpointToLocationPair.getLeft(), LocationEastUs2EndpointToLocationPair.getLeft(), LocationCentralUsEndpointToLocationPair.getLeft()));

        Mockito.when(globalEndpointManagerMock.getReadEndpoints()).thenReturn(endpoints);
        Mockito.when(globalEndpointManagerMock.getRegionName(Mockito.eq(locationEndpointContacted), Mockito.any())).thenReturn(regionContacted);

        RxDocumentServiceRequest documentCollectionCreateRequest = createRequestEntity(OperationType.Create, ResourceType.DocumentCollection, LocationEastUsEndpointToLocationPair.getLeft());
        documentCollectionCreateRequest.requestContext.locationEndpointToRoute = locationEndpointContacted;

        sessionContainer.setSessionToken(documentCollectionCreateRequest, documentCollectionId, collectionFullName,
            ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, unparsedSessionToken));

        //  Test getResourceId based
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
            documentCollectionId, ResourceType.Document, new HashMap<>());
        request.requestContext.locationEndpointToRoute = locationEndpointContacted;

        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken.getLSN()).isEqualTo(100);

        //  Test names based
        request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),OperationType.Read,
            collectionFullName + "/docs/doc1", ResourceType.Document);
        request.requestContext.locationEndpointToRoute = locationEndpointContacted;

        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken.getLSN()).isEqualTo(100);

        sessionContainer.clearTokenByCollectionFullName(collectionFullName);

        //  Test resourceId based
        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
            documentCollectionId, ResourceType.Document, new HashMap<>());
        request.requestContext.locationEndpointToRoute = locationEndpointContacted;

        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken).isNull();

        //  Test names based
        request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),OperationType.Read,
            collectionFullName + "/docs/doc1", ResourceType.Document);
        request.requestContext.locationEndpointToRoute = locationEndpointContacted;

        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken).isNull();
    }

    @Test(groups = "unit")
    public void clearTokenByResourceIdRemovesToken() {

        GlobalEndpointManager globalEndpointManagerMock = Mockito.mock(GlobalEndpointManager.class);

        RegionScopedSessionContainer sessionContainer = new RegionScopedSessionContainer("127.0.0.1", false, globalEndpointManagerMock);
        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId()).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName";
        String regionContacted = LocationEastUsEndpointToLocationPair.getRight();
        URI locationEndpointContacted = LocationEastUsEndpointToLocationPair.getLeft();

        String unparsedSessionToken = "range_0:1#100#1=20#2=5#3=30";

        UnmodifiableList<URI> endpoints = new UnmodifiableList<>(ImmutableList.of(LocationEastUsEndpointToLocationPair.getLeft(), LocationEastUs2EndpointToLocationPair.getLeft(), LocationCentralUsEndpointToLocationPair.getLeft()));

        Mockito.when(globalEndpointManagerMock.getReadEndpoints()).thenReturn(endpoints);
        Mockito.when(globalEndpointManagerMock.getRegionName(Mockito.eq(locationEndpointContacted), Mockito.any())).thenReturn(regionContacted);

        RxDocumentServiceRequest documentCollectionCreateRequest = createRequestEntity(OperationType.Create, ResourceType.DocumentCollection, LocationEastUsEndpointToLocationPair.getLeft());
        documentCollectionCreateRequest.requestContext.locationEndpointToRoute = locationEndpointContacted;

        sessionContainer.setSessionToken(documentCollectionCreateRequest, documentCollectionId, collectionFullName,
            ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, unparsedSessionToken));

        //  Test resourceId based
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
            documentCollectionId, ResourceType.Document, new HashMap<>());
        request.requestContext.locationEndpointToRoute = locationEndpointContacted;

        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken).isNotNull();
        assertThat(sessionToken.getLSN()).isEqualTo(100);

        //  Test names based
        request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),OperationType.Read,
            collectionFullName + "/docs/doc1", ResourceType.Document);
        request.requestContext.locationEndpointToRoute = locationEndpointContacted;

        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken).isNotNull();
        assertThat(sessionToken.getLSN()).isEqualTo(100);

        sessionContainer.clearTokenByResourceId(documentCollectionId);

        //  Test resourceId based
        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
            documentCollectionId, ResourceType.Document, new HashMap<>());
        request.requestContext.locationEndpointToRoute = locationEndpointContacted;

        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken).isNull();

        //  Test names based
        request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),OperationType.Read,
            collectionFullName + "/docs/doc1", ResourceType.Document);
        request.requestContext.locationEndpointToRoute = locationEndpointContacted;

        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken).isNull();
    }

    @Test(groups = "unit")
    public void clearTokenKeepsUnmatchedCollection() {
        GlobalEndpointManager globalEndpointManagerMock = Mockito.mock(GlobalEndpointManager.class);
        RegionScopedSessionContainer sessionContainer = new RegionScopedSessionContainer("127.0.0.1", false, globalEndpointManagerMock);

        int randomCollectionId = getRandomCollectionId();
        String documentCollectionId1 = ResourceId.newDocumentCollectionId(getRandomDbId(), randomCollectionId).getDocumentCollectionId().toString();
        String collectionFullName1 = "dbs/db1/colls1/collName1";
        String regionContacted = LocationEastUsEndpointToLocationPair.getRight();
        URI locationEndpointContacted = LocationEastUsEndpointToLocationPair.getLeft();
        String unparsedSessionToken = "range_0:1#100#1=20#2=5#3=30";

        UnmodifiableList<URI> endpoints = new UnmodifiableList<>(ImmutableList.of(LocationEastUsEndpointToLocationPair.getLeft(), LocationEastUs2EndpointToLocationPair.getLeft(), LocationCentralUsEndpointToLocationPair.getLeft()));

        Mockito.when(globalEndpointManagerMock.getReadEndpoints()).thenReturn(endpoints);
        Mockito.when(globalEndpointManagerMock.getRegionName(Mockito.eq(locationEndpointContacted), Mockito.any())).thenReturn(regionContacted);

        RxDocumentServiceRequest documentCollectionCreateRequest = createRequestEntity(OperationType.Create, ResourceType.DocumentCollection, LocationEastUsEndpointToLocationPair.getLeft());
        documentCollectionCreateRequest.requestContext.locationEndpointToRoute = locationEndpointContacted;

        sessionContainer.setSessionToken(documentCollectionCreateRequest, documentCollectionId1, collectionFullName1,
            ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, unparsedSessionToken));

        //  Test resourceId based
        RxDocumentServiceRequest request1 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
            documentCollectionId1, ResourceType.Document, new HashMap<>());
        request1.requestContext.locationEndpointToRoute = locationEndpointContacted;

        String documentCollectionId2 = ResourceId.newDocumentCollectionId(getRandomDbId(), randomCollectionId - 1).getDocumentCollectionId().toString();
        String collectionFullName2 = "dbs/db1/colls1/collName2";

        //  Test resourceId based
        RxDocumentServiceRequest request2 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
            documentCollectionId2, ResourceType.Document, new HashMap<>());
        request2.requestContext.locationEndpointToRoute = locationEndpointContacted;

        sessionContainer.setSessionToken(request2, documentCollectionId2, collectionFullName2,
            ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, unparsedSessionToken));

        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request1, "range_0");
        assertThat(sessionToken.getLSN()).isEqualTo(100);
        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request2, "range_0");
        assertThat(sessionToken.getLSN()).isEqualTo(100);

        sessionContainer.clearTokenByResourceId(documentCollectionId2);

        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request1, "range_0");
        assertThat(sessionToken.getLSN()).isEqualTo(100);
        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request2, "range_0");
        assertThat(sessionToken).isNull();
    }

    @Test(groups = "unit")
    public void setSessionTokenDoesntFailOnEmptySessionTokenHeader() {
        RegionScopedSessionContainer sessionContainer = new RegionScopedSessionContainer("127.0.0.1");
        sessionContainer.setSessionToken(null, new HashMap<>());
    }

    @Test(groups = "unit")
    public void setSessionTokenSetsTokenWhenRequestIsntNameBased() {
        GlobalEndpointManager globalEndpointManagerMock = Mockito.mock(GlobalEndpointManager.class);
        RegionScopedSessionContainer sessionContainer = new RegionScopedSessionContainer("127.0.0.1", false, globalEndpointManagerMock);
        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId()).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName";
        String regionContacted = LocationEastUsEndpointToLocationPair.getRight();
        URI locationEndpointContacted = LocationEastUsEndpointToLocationPair.getLeft();

        UnmodifiableList<URI> endpoints = new UnmodifiableList<>(ImmutableList.of(LocationEastUsEndpointToLocationPair.getLeft(), LocationEastUs2EndpointToLocationPair.getLeft(), LocationCentralUsEndpointToLocationPair.getLeft()));

        Mockito.when(globalEndpointManagerMock.getReadEndpoints()).thenReturn(endpoints);
        Mockito.when(globalEndpointManagerMock.getRegionName(Mockito.eq(locationEndpointContacted), Mockito.any())).thenReturn(regionContacted);

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
            collectionFullName + "/docs/doc1", ResourceType.Document, new HashMap<>());
        request.requestContext.locationEndpointToRoute = locationEndpointContacted;

        String sessionTokenWithPkRangeIdForRequest = "range_0:1#100#4=90#5=1";
        request.setResourceId(documentCollectionId);

        assertThat(request.getIsNameBased()).isFalse();
        sessionContainer.setSessionToken(request, ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionTokenWithPkRangeIdForRequest));
        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read, documentCollectionId, ResourceType.Document, new HashMap<>());
        request.requestContext.locationEndpointToRoute = locationEndpointContacted;

        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken).isNotNull();
        assertThat(sessionToken.getLSN()).isEqualTo(100);

        request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),OperationType.Read, collectionFullName + "/docs/doc1", ResourceType.Document);
        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken).isNotNull();
        assertThat(sessionToken.getLSN()).isEqualTo(100);
    }

    @Test(groups = "unit")
    public void setSessionTokenGivesPriorityToOwnerFullNameOverResourceAddress() {
        GlobalEndpointManager globalEndpointManagerMock = Mockito.mock(GlobalEndpointManager.class);
        RegionScopedSessionContainer sessionContainer = new RegionScopedSessionContainer("127.0.0.1", false, globalEndpointManagerMock);

        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId()).getDocumentCollectionId().toString();
        String collectionFullName1 = "dbs/db1/colls1/collName1";
        String collectionFullName2 = "dbs/db1/colls1/collName2";
        String regionContacted = LocationEastUsEndpointToLocationPair.getRight();
        URI locationEndpointContacted = LocationEastUsEndpointToLocationPair.getLeft();

        UnmodifiableList<URI> endpoints = new UnmodifiableList<>(ImmutableList.of(LocationEastUsEndpointToLocationPair.getLeft(), LocationEastUs2EndpointToLocationPair.getLeft(), LocationCentralUsEndpointToLocationPair.getLeft()));

        Mockito.when(globalEndpointManagerMock.getReadEndpoints()).thenReturn(endpoints);
        Mockito.when(globalEndpointManagerMock.getRegionName(Mockito.eq(locationEndpointContacted), Mockito.any())).thenReturn(regionContacted);

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
            collectionFullName1 + "/docs/doc1", ResourceType.Document, new HashMap<>());
        request.requestContext.locationEndpointToRoute = locationEndpointContacted;

        String sessionTokenWithPkRangeIdForRequest = "range_0:1#100#4=90#5=1";
        request.setResourceId(documentCollectionId);

        sessionContainer.setSessionToken(request,
            ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionTokenWithPkRangeIdForRequest,
                HttpConstants.HttpHeaders.OWNER_FULL_NAME, collectionFullName2));

        request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),OperationType.Read, collectionFullName1 + "/docs/doc1", ResourceType.Document);
        request.requestContext.locationEndpointToRoute = locationEndpointContacted;

        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken).isNull();

        request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),OperationType.Read, collectionFullName2 + "/docs/doc1", ResourceType.Document);
        request.requestContext.locationEndpointToRoute = locationEndpointContacted;

        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken).isNotNull();
        assertThat(sessionToken.getLSN()).isEqualTo(100);
    }

    @Test(groups = "unit")
    public void setSessionTokenIgnoresOwnerIdWhenRequestIsntNameBased() {
        GlobalEndpointManager globalEndpointManagerMock = Mockito.mock(GlobalEndpointManager.class);
        RegionScopedSessionContainer sessionContainer = new RegionScopedSessionContainer("127.0.0.1", false, globalEndpointManagerMock);

        int randomCollectionId = getRandomCollectionId();
        int randomDbId = getRandomDbId();
        String documentCollectionId1 = ResourceId.newDocumentCollectionId(randomDbId, randomCollectionId).getDocumentCollectionId().toString();
        String documentCollectionId2 = ResourceId.newDocumentCollectionId(randomDbId, randomCollectionId - 1).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName1";
        String regionContacted = LocationEastUsEndpointToLocationPair.getRight();
        URI locationEndpointContacted = LocationEastUsEndpointToLocationPair.getLeft();

        UnmodifiableList<URI> endpoints = new UnmodifiableList<>(ImmutableList.of(LocationEastUsEndpointToLocationPair.getLeft(), LocationEastUs2EndpointToLocationPair.getLeft(), LocationCentralUsEndpointToLocationPair.getLeft()));

        Mockito.when(globalEndpointManagerMock.getReadEndpoints()).thenReturn(endpoints);
        Mockito.when(globalEndpointManagerMock.getRegionName(Mockito.eq(locationEndpointContacted), Mockito.any())).thenReturn(regionContacted);

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
            collectionFullName + "/docs/doc1", ResourceType.Document, new HashMap<>());
        request.requestContext.locationEndpointToRoute = locationEndpointContacted;

        request.setResourceId(documentCollectionId1);

        String sessionTokenWithPkRangeIdForRequest = "range_0:1#100#4=90#5=1";

        assertThat(request.getIsNameBased()).isFalse();

        sessionContainer.setSessionToken(request,
            ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionTokenWithPkRangeIdForRequest,
                HttpConstants.HttpHeaders.OWNER_ID, documentCollectionId2));

        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
            documentCollectionId1, ResourceType.Document, new HashMap<>());
        request.requestContext.locationEndpointToRoute = locationEndpointContacted;

        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken).isNotNull();
        assertThat(sessionToken.getLSN()).isEqualTo(100);


        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
            documentCollectionId2, ResourceType.Document, new HashMap<>());
        request.requestContext.locationEndpointToRoute = locationEndpointContacted;

        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken).isNull();
    }

    @Test(groups = "unit")
    public void setSessionTokenGivesPriorityToOwnerIdOverResourceIdWhenRequestIsNameBased() {
        GlobalEndpointManager globalEndpointManagerMock = Mockito.mock(GlobalEndpointManager.class);
        RegionScopedSessionContainer sessionContainer = new RegionScopedSessionContainer("127.0.0.1", false, globalEndpointManagerMock);

        int randomCollectionId = getRandomCollectionId();
        int randomDbId = getRandomDbId();
        String documentCollectionId1 = ResourceId.newDocumentCollectionId(randomDbId, randomCollectionId).getDocumentCollectionId().toString();
        String documentCollectionId2 = ResourceId.newDocumentCollectionId(randomDbId, randomCollectionId - 1).getDocumentCollectionId().toString();

        String collectionFullName = "dbs/db1/colls1/collName1";
        String regionContacted = LocationEastUsEndpointToLocationPair.getRight();
        URI locationEndpointContacted = LocationEastUsEndpointToLocationPair.getLeft();

        UnmodifiableList<URI> endpoints = new UnmodifiableList<>(ImmutableList.of(LocationEastUsEndpointToLocationPair.getLeft(), LocationEastUs2EndpointToLocationPair.getLeft(), LocationCentralUsEndpointToLocationPair.getLeft()));

        Mockito.when(globalEndpointManagerMock.getReadEndpoints()).thenReturn(endpoints);
        Mockito.when(globalEndpointManagerMock.getRegionName(Mockito.eq(LocationEastUsEndpointToLocationPair.getLeft()), Mockito.any())).thenReturn(regionContacted);

        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),OperationType.Read,
            collectionFullName + "/docs/doc1", ResourceType.Document);
        request.requestContext.locationEndpointToRoute = locationEndpointContacted;

        request.setResourceId(documentCollectionId1);

        String sessionTokenWithPkRangeIdForRequest = "range_0:1#100#4=90#5=1";
        assertThat(request.getIsNameBased()).isTrue();

        sessionContainer.setSessionToken(request,
            ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionTokenWithPkRangeIdForRequest,
                HttpConstants.HttpHeaders.OWNER_ID, documentCollectionId2));

        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
            documentCollectionId1, ResourceType.Document, new HashMap<>());
        request.requestContext.locationEndpointToRoute = locationEndpointContacted;

        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken).isNull();

        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
            documentCollectionId2, ResourceType.Document, new HashMap<>());
        request.requestContext.locationEndpointToRoute = locationEndpointContacted;

        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken.getLSN()).isEqualTo(100);
    }

    @Test(groups = "unit")
    public void setSessionTokenDoesntWorkForMasterQueries() {
        RegionScopedSessionContainer sessionContainer = new RegionScopedSessionContainer("127.0.0.1");
        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId()).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName";

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.ReadFeed,
            collectionFullName + "/docs/doc1", ResourceType.DocumentCollection, new HashMap<>());
        request.setResourceId(documentCollectionId);
        sessionContainer.setSessionToken(request, ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1"));

        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
            documentCollectionId, ResourceType.Document, new HashMap<>());
        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken).isNull();

        request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),OperationType.Read, collectionFullName + "/docs/doc1", ResourceType.Document);
        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken).isNull();
    }

    @Test(groups = "unit")
    public void setSessionTokenDoesntOverwriteHigherLSN() {
        GlobalEndpointManager globalEndpointManagerMock = Mockito.mock(GlobalEndpointManager.class);
        RegionScopedSessionContainer sessionContainer = new RegionScopedSessionContainer("127.0.0.1", false, globalEndpointManagerMock);

        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId()).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName";
        String regionContacted = LocationEastUsEndpointToLocationPair.getRight();
        URI locationEndpointContacted = LocationEastUsEndpointToLocationPair.getLeft();

        UnmodifiableList<URI> endpoints = new UnmodifiableList<>(ImmutableList.of(LocationEastUsEndpointToLocationPair.getLeft(), LocationEastUs2EndpointToLocationPair.getLeft(), LocationCentralUsEndpointToLocationPair.getLeft()));

        Mockito.when(globalEndpointManagerMock.getReadEndpoints()).thenReturn(endpoints);
        Mockito.when(globalEndpointManagerMock.getRegionName(Mockito.eq(locationEndpointContacted), Mockito.any())).thenReturn(regionContacted);

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
            collectionFullName + "/docs/doc1", ResourceType.Document, new HashMap<>());
        request.requestContext.locationEndpointToRoute = locationEndpointContacted;

        String sessionTokenWithPkRangeIdForRequest = "range_0:1#100#4=90#5=1";

        request.setResourceId(documentCollectionId);

        sessionContainer.setSessionToken(request, ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionTokenWithPkRangeIdForRequest));

        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
            collectionFullName + "/docs/doc1", ResourceType.Document, new HashMap<>());
        request.requestContext.locationEndpointToRoute = locationEndpointContacted;

        String sessionTokenWithPkRangeIdToBeOverwritten = "range_0:1#105#4=90#5=1";

        request.setResourceId(documentCollectionId);
        sessionContainer.setSessionToken(request, ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionTokenWithPkRangeIdToBeOverwritten));

        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
            documentCollectionId, ResourceType.Document, new HashMap<>());
        request.requestContext.locationEndpointToRoute = locationEndpointContacted;

        request.setResourceId(documentCollectionId);
        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken).isNotNull();
        assertThat(sessionToken.getLSN()).isEqualTo(105);
    }

    @Test(groups = "unit")
    public void setSessionTokenOverwriteLowerLSN() {
        GlobalEndpointManager globalEndpointManagerMock = Mockito.mock(GlobalEndpointManager.class);
        RegionScopedSessionContainer sessionContainer = new RegionScopedSessionContainer("127.0.0.1", false, globalEndpointManagerMock);

        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId()).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName";
        String regionContacted = LocationEastUsEndpointToLocationPair.getRight();
        URI locationEndpointContacted = LocationEastUsEndpointToLocationPair.getLeft();

        UnmodifiableList<URI> endpoints = new UnmodifiableList<>(ImmutableList.of(LocationEastUsEndpointToLocationPair.getLeft(), LocationEastUs2EndpointToLocationPair.getLeft(), LocationCentralUsEndpointToLocationPair.getLeft()));

        Mockito.when(globalEndpointManagerMock.getReadEndpoints()).thenReturn(endpoints);
        Mockito.when(globalEndpointManagerMock.getRegionName(Mockito.eq(locationEndpointContacted), Mockito.any())).thenReturn(regionContacted);

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
            collectionFullName + "/docs/doc1", ResourceType.Document, new HashMap<>());
        request.requestContext.locationEndpointToRoute = locationEndpointContacted;

        String sessionTokenWithPkRangeIdForRequest = "range_0:1#105#4=90#5=1";
        request.setResourceId(documentCollectionId);

        sessionContainer.setSessionToken(request, ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionTokenWithPkRangeIdForRequest));

        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
            collectionFullName + "/docs/doc1", ResourceType.Document, new HashMap<>());
        request.requestContext.locationEndpointToRoute = locationEndpointContacted;

        String sessionTokenWithPkRangeIdToBeOverwritten = "range_0:1#100#4=90#5=1";

        request.setResourceId(documentCollectionId);

        sessionContainer.setSessionToken(request, ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionTokenWithPkRangeIdToBeOverwritten));

        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
            documentCollectionId, ResourceType.Document, new HashMap<>());
        request.requestContext.locationEndpointToRoute = locationEndpointContacted;

        request.setResourceId(documentCollectionId);
        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken).isNotNull();
        assertThat(sessionToken.getLSN()).isEqualTo(105);
    }

    @Test(groups = "unit")
    public void setSessionTokenDoesNothingOnEmptySessionTokenHeader() {
        GlobalEndpointManager globalEndpointManagerMock = Mockito.mock(GlobalEndpointManager.class);
        RegionScopedSessionContainer sessionContainer = new RegionScopedSessionContainer("127.0.0.1", false, globalEndpointManagerMock);
        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId()).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName";
        String regionContacted = LocationEastUsEndpointToLocationPair.getRight();
        URI locationEndpointContacted = LocationEastUsEndpointToLocationPair.getLeft();

        UnmodifiableList<URI> endpoints = new UnmodifiableList<>(ImmutableList.of(LocationEastUsEndpointToLocationPair.getLeft(), LocationEastUs2EndpointToLocationPair.getLeft(), LocationCentralUsEndpointToLocationPair.getLeft()));

        Mockito.when(globalEndpointManagerMock.getReadEndpoints()).thenReturn(endpoints);
        Mockito.when(globalEndpointManagerMock.getRegionName(Mockito.eq(locationEndpointContacted), Mockito.any())).thenReturn(regionContacted);

        RxDocumentServiceRequest docReadRequest1 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        docReadRequest1.requestContext.locationEndpointToRoute = locationEndpointContacted;

        String sessionTokenWithPkRangeIdForDocReadRequest1 = "range_0:1#100#4=90#5=1";

        sessionContainer.setSessionToken(docReadRequest1, documentCollectionId, collectionFullName + "/docs/doc1",
            ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionTokenWithPkRangeIdForDocReadRequest1));
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
            documentCollectionId, ResourceType.Document, new HashMap<>());
        request.requestContext.locationEndpointToRoute = locationEndpointContacted;

        String sessionToken = sessionContainer.resolveGlobalSessionToken(request);
        Set<String> tokens = Sets.newSet(sessionToken.split(","));
        assertThat(tokens.size()).isEqualTo(1);
        assertThat(tokens.contains(sessionTokenWithPkRangeIdForDocReadRequest1)).isTrue();

        RxDocumentServiceRequest docReadRequest2 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        docReadRequest2.requestContext.locationEndpointToRoute = locationEndpointContacted;
        sessionContainer.setSessionToken(docReadRequest2, documentCollectionId, collectionFullName, new HashMap<>());

        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
            documentCollectionId, ResourceType.Document, new HashMap<>());
        request.requestContext.locationEndpointToRoute = locationEndpointContacted;

        sessionToken = sessionContainer.resolveGlobalSessionToken(request);
        tokens = Sets.newSet(sessionToken.split(","));
        assertThat(tokens.size()).isEqualTo(1);
        assertThat(tokens.contains(sessionTokenWithPkRangeIdForDocReadRequest1)).isTrue();
    }

    @Test(groups = "unit")
    public void sessionCapturingDisabled() throws Exception {
        RegionScopedSessionContainer sessionContainer = new RegionScopedSessionContainer("127.0.0.1", true);

        int numCollections = 2;
        int numPartitionKeyRangeIds = 5;
        String regionContacted = LocationEastUsEndpointToLocationPair.getRight();

        for (int i = 0; i < numCollections; i++) {
            String collectionResourceId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId() + i).getDocumentCollectionId().toString();
            String collectionFullName = "dbs/db1/colls/collName_" + i;

            for (int j = 0; j < numPartitionKeyRangeIds; j++) {

                String partitionKeyRangeId = "range_" + j;
                String lsn = "1#" + j + "#4=90#5=2";
                String resultantSessionToken = partitionKeyRangeId + ":" + lsn;

                RxDocumentServiceRequest documentReadRequest = RxDocumentServiceRequest
                    .create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);

                sessionContainer.setSessionToken(
                    documentReadRequest,
                    collectionResourceId,
                    collectionFullName,
                    ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, partitionKeyRangeId + ":" + lsn));
            }
        }

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.ReadFeed, ResourceType.DocumentCollection,
            "dbs/db1/colls/collName_1",  Utils.getUTF8Bytes("content1"), new HashMap<>());

        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_1");
        assertThat(sessionToken).isNull();

        DocumentServiceRequestContext dsrContext = new DocumentServiceRequestContext();
        PartitionKeyRange resolvedPKRange = new PartitionKeyRange();
        resolvedPKRange.setId("range_" + (numPartitionKeyRangeIds + 10));
        GatewayTestUtils.setParent(resolvedPKRange, ImmutableList.of("range_2", "range_x"));
        dsrContext.resolvedPartitionKeyRange = resolvedPKRange;
        request.requestContext = dsrContext;

        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, resolvedPKRange.getId());
        assertThat(sessionToken).isNull();
    }

    @Test(groups = "unit")
    public void useParentSessionTokenAfterSplit() {
        GlobalEndpointManager globalEndpointManagerMock = Mockito.mock(GlobalEndpointManager.class);
        RegionScopedSessionContainer sessionContainer = new RegionScopedSessionContainer("127.0.0.1", false, globalEndpointManagerMock);

        int randomCollectionId = getRandomCollectionId();
        String documentCollectionId1 = ResourceId.newDocumentCollectionId(getRandomDbId(), randomCollectionId).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName1";
        String regionContacted = LocationEastUsEndpointToLocationPair.getRight();
        URI locationEndpointContacted = LocationEastUsEndpointToLocationPair.getLeft();

        UnmodifiableList<URI> endpoints = new UnmodifiableList<>(ImmutableList.of(LocationEastUsEndpointToLocationPair.getLeft(), LocationEastUs2EndpointToLocationPair.getLeft(), LocationCentralUsEndpointToLocationPair.getLeft()));

        Mockito.when(globalEndpointManagerMock.getReadEndpoints()).thenReturn(endpoints);
        Mockito.when(globalEndpointManagerMock.getRegionName(Mockito.eq(locationEndpointContacted), Mockito.any())).thenReturn(regionContacted);

        // Set token for the parent
        String parentPKRangeId = "0";
        String parentSession = "1#100#4=90#5=1";
        String resultantParentSessionToken = parentPKRangeId + ":" + parentSession;

        RxDocumentServiceRequest request1 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        request1.requestContext.locationEndpointToRoute = locationEndpointContacted;

        sessionContainer.setSessionToken(
            request1,
            documentCollectionId1,
            collectionFullName,
            ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, resultantParentSessionToken));

        // send requests for children
        String childPKRangeId = "1";
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
            documentCollectionId1, ResourceType.Document, new HashMap<>());
        request.requestContext.locationEndpointToRoute = locationEndpointContacted;

        request.requestContext.resolvedPartitionKeyRange =
            new PartitionKeyRange(
                childPKRangeId,
                "AA",
                "BB",
                Arrays.asList(parentPKRangeId));

        ISessionToken sessionTokenForChild1 = sessionContainer.resolvePartitionLocalSessionToken(request, childPKRangeId);
        assertThat(sessionTokenForChild1).isNotNull();
        assertThat(sessionTokenForChild1.convertToString()).isEqualTo(parentSession);
    }

    @Test(groups = "unit")
    public void useParentSessionTokenAfterMerge() {
        GlobalEndpointManager globalEndpointManagerMock = Mockito.mock(GlobalEndpointManager.class);
        RegionScopedSessionContainer sessionContainer = new RegionScopedSessionContainer("127.0.0.1", false, globalEndpointManagerMock);

        int randomCollectionId = getRandomCollectionId();
        String documentCollectionId1 = ResourceId.newDocumentCollectionId(getRandomDbId(), randomCollectionId).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName1";
        String regionContacted = LocationEastUsEndpointToLocationPair.getRight();
        URI locationEndpointContacted = LocationEastUsEndpointToLocationPair.getLeft();

        UnmodifiableList<URI> endpoints = new UnmodifiableList<>(ImmutableList.of(LocationEastUsEndpointToLocationPair.getLeft(), LocationEastUs2EndpointToLocationPair.getLeft(), LocationCentralUsEndpointToLocationPair.getLeft()));

        Mockito.when(globalEndpointManagerMock.getReadEndpoints()).thenReturn(endpoints);
        Mockito.when(globalEndpointManagerMock.getRegionName(Mockito.eq(locationEndpointContacted), Mockito.any())).thenReturn(regionContacted);

        // Set token for the parent
        // Set tokens for the parents
        int maxGlobalLsn = 100;
        int maxLsnRegion1 = 200;
        int maxLsnRegion2 = 300;
        int maxLsnRegion3 = 400;

        // Generate 2 tokens, one has max global but lower regional, the other lower global but higher regional
        // Expect the merge to contain all the maxes
        String parent1PKRangeId = "0";
        String parent1Session = String.format(
            "1#%s#1=%s#2=%s#3=%s",
            maxGlobalLsn,
            maxLsnRegion1 - 1,
            maxLsnRegion2,
            maxLsnRegion3 - 1);

        String parent1SessionToken = parent1PKRangeId + ":" + parent1Session;

        RxDocumentServiceRequest request1 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        request1.requestContext.locationEndpointToRoute = locationEndpointContacted;

        sessionContainer.setSessionToken(
            request1,
            documentCollectionId1,
            collectionFullName,
            ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, parent1SessionToken));

        String parent2PKRangeId = "1";
        String parent2Session = String.format(
            "1#%s#1=%s#2=%s#3=%s",
            maxGlobalLsn - 1,
            maxLsnRegion1,
            maxLsnRegion2 - 1,
            maxLsnRegion3);
        String parent2SessionToken = parent2PKRangeId + ":" + parent2Session;

        RxDocumentServiceRequest request2 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        request2.requestContext.locationEndpointToRoute = locationEndpointContacted;

        sessionContainer.setSessionToken(
            request2,
            documentCollectionId1,
            collectionFullName,
            ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, parent2SessionToken));

        // send requests for children
        String childPKRangeId = "2";
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
            documentCollectionId1, ResourceType.Document, new HashMap<>());
        request.requestContext.locationEndpointToRoute = locationEndpointContacted;

        request.requestContext.resolvedPartitionKeyRange =
            new PartitionKeyRange(
                childPKRangeId,
                "AA",
                "BB",
                Arrays.asList(parent1PKRangeId, parent2PKRangeId));
        ISessionToken sessionTokenForChild1 = sessionContainer.resolvePartitionLocalSessionToken(request, childPKRangeId);

        String expectedChildSessionToken = String.format(
            "1#%s#1=%s#2=%s#3=%s",
            maxGlobalLsn,
            maxLsnRegion1,
            maxLsnRegion2,
            maxLsnRegion3);

        assertThat(sessionTokenForChild1).isNotNull();
        assertThat(sessionTokenForChild1.convertToString()).isEqualTo(expectedChildSessionToken);
    }

    @Test(groups = {"unit"}, dataProvider = "sessionContainerDataProvider")
    public void resolvePartitionLocalSessionToken(
        List<RequestMetadata> requestMetadataList,
        List<Pair<URI, String>> writableURIToLocationMappings,
        List<Pair<URI, String>> readableURIToLocationMappings,
        List<URI> readEndpoints,
        boolean canUseMultipleWritableLocations,
        String pkRangeIdToBeUsedForSessionTokenResolution,
        String pkToBeUsedForSessionTokenResolution,
        String expectedSessionToken) {

        String hostName = "127.0.0.1";
        int randomCollectionId = getRandomCollectionId();
        String documentCollectionId1 = ResourceId.newDocumentCollectionId(getRandomDbId(), randomCollectionId).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName1";
        boolean disableSessionCapturing = false;
        GlobalEndpointManager globalEndpointManagerMock = null;
        RegionScopedSessionContainer sessionContainer = null;

        List<URI> writableURIs = writableURIToLocationMappings
            .stream()
            .map(uriToLocationMappings -> uriToLocationMappings.getLeft())
            .collect(Collectors.toList());

        DatabaseAccount databaseAccount = ModelBridgeUtils.createDatabaseAccount(
            readableURIToLocationMappings.stream().map(uriToLocationMapping -> createDatabaseAccountLocation(uriToLocationMapping.getRight(), uriToLocationMapping.getLeft().toString())).collect(Collectors.toList()),
            writableURIToLocationMappings.stream().map(uriToLocationMapping -> createDatabaseAccountLocation(uriToLocationMapping.getRight(), uriToLocationMapping.getLeft().toString())).collect(Collectors.toList()),
            canUseMultipleWritableLocations);

        try {
            globalEndpointManagerMock = Mockito.mock(GlobalEndpointManager.class);

            Mockito
                .when(globalEndpointManagerMock.getLatestDatabaseAccount())
                .thenReturn(databaseAccount);

            UnmodifiableList<URI> readEndpointsInUnmodifiableList = new UnmodifiableList<>(readEndpoints);

            Mockito
                .when(globalEndpointManagerMock.getReadEndpoints())
                .thenReturn(readEndpointsInUnmodifiableList);

            Mockito
                .when(globalEndpointManagerMock.getApplicableWriteEndpoints(Mockito.anyList()))
                .thenReturn(new UnmodifiableList<>(writableURIs));

            Mockito
                .when(globalEndpointManagerMock.canUseMultipleWriteLocations(Mockito.any()))
                .thenReturn(true);

            Mockito.when(globalEndpointManagerMock.getRegionName(Mockito.eq(LocationEastUsEndpointToLocationPair.getLeft()), Mockito.any())).thenReturn(LocationEastUsEndpointToLocationPair.getRight());
            Mockito.when(globalEndpointManagerMock.getRegionName(Mockito.eq(LocationEastUs2EndpointToLocationPair.getLeft()), Mockito.any())).thenReturn(LocationEastUs2EndpointToLocationPair.getRight());
            Mockito.when(globalEndpointManagerMock.getRegionName(Mockito.eq(LocationCentralUsEndpointToLocationPair.getLeft()), Mockito.any())).thenReturn(LocationCentralUsEndpointToLocationPair.getRight());
            Mockito.when(globalEndpointManagerMock.getRegionName(Mockito.eq(LocationWestUsEndpointToLocationPair.getLeft()), Mockito.any())).thenReturn(LocationWestUsEndpointToLocationPair.getRight());

            sessionContainer = new RegionScopedSessionContainer(hostName, disableSessionCapturing, globalEndpointManagerMock);

            for (RequestMetadata requestMetadata : requestMetadataList) {
                sessionContainer.setSessionToken(
                    requestMetadata.request, documentCollectionId1, collectionFullName, requestMetadata.responseHeaders);
            }

            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);

            // doesn't matter for a request for which the session token has to be resolved
            request.requestContext.locationEndpointToRoute = LocationEastUsEndpointToLocationPair.getLeft();
            request.setResourceId(documentCollectionId1);

            if (pkToBeUsedForSessionTokenResolution != null) {
                request.setPartitionKeyInternal(ModelBridgeInternal.getPartitionKeyInternal(new PartitionKey(pkToBeUsedForSessionTokenResolution)));
            }

            request.setPartitionKeyDefinition(new PartitionKeyDefinition().setPaths(ImmutableList.of("/mypk")));

            if (!Strings.isNullOrEmpty(pkRangeIdToBeUsedForSessionTokenResolution)) {
                ISessionToken resolvedSessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, pkRangeIdToBeUsedForSessionTokenResolution);
                assertThat(resolvedSessionToken).isNotNull();
                assertThat(resolvedSessionToken.convertToString()).isEqualTo(expectedSessionToken);
            }

        } catch (Exception exception) {
            logger.error("resolvePartitionLocalSessionToken test failed with error : ", exception);
            fail("A failure occurred for reason : " + exception);
        } finally {

            if (globalEndpointManagerMock != null) {
                globalEndpointManagerMock.close();
            }
        }
    }

    private static int getRandomCollectionId() {
        return random.nextInt(Integer.MAX_VALUE / 2) - (Integer.MAX_VALUE / 2);
    }

    private static int getRandomDbId() {
        return random.nextInt(Integer.MAX_VALUE / 2);
    }

    private static RequestMetadata constructRequestInstance(
        OperationType operationType,
        ResourceType resourceType,
        PartitionKey partitionKey,
        PartitionKeyDefinition partitionKeyDefinition,
        String collectionResourceId,
        URI locationEndpointToRoute,
        Map<String, String> responseHeaders) {

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            mockDiagnosticsClientContext(),
            operationType,
            resourceType);

        request.setResourceId(collectionResourceId);
        request.setPartitionKeyInternal(BridgeInternal.getPartitionKeyInternal(partitionKey));
        request.setPartitionKeyDefinition(partitionKeyDefinition);
        request.requestContext.locationEndpointToRoute = locationEndpointToRoute;

        return new RequestMetadata(request, responseHeaders);
    }

    private static RxDocumentServiceRequest createRequestEntity(OperationType operationType, ResourceType resourceType, URI locationEndpointToRoute) {

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            mockDiagnosticsClientContext(),
            operationType,
            resourceType);

        request.requestContext.locationEndpointToRoute = locationEndpointToRoute;

        return request;
    }

    // todo (abhmohanty): move to a test utility class
    private static URI createUrl(String url) {
        try {
            return new URI(url);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static DatabaseAccountLocation createDatabaseAccountLocation(String name, String endpoint) {
        DatabaseAccountLocation dal = new DatabaseAccountLocation();
        dal.setName(name);
        dal.setEndpoint(endpoint);

        return dal;
    }

    private static class RequestMetadata {
        private final RxDocumentServiceRequest request;
        private final Map<String, String> responseHeaders;

        public RequestMetadata(RxDocumentServiceRequest request, Map<String, String> responseHeaders) {
            this.request = request;
            this.responseHeaders = responseHeaders;
        }
    }
}
