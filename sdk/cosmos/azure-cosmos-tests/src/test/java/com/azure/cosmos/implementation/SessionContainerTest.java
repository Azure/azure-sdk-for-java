// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.DirectConnectionConfig;
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

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Tests for {@link SessionContainer}
 */
public class SessionContainerTest {

    private static final
    ImplementationBridgeHelpers.CosmosDiagnosticsHelper.CosmosDiagnosticsAccessor diagnosticsAccessor =
        ImplementationBridgeHelpers.CosmosDiagnosticsHelper.getCosmosDiagnosticsAccessor();

    private final static URI DefaultEndpoint = createUrl("https://default.documents.azure.com");
    private final static URI Location1Endpoint = createUrl("https://location1.documents.azure.com");
    private final static URI Location2Endpoint = createUrl("https://location2.documents.azure.com");
    private final static URI Location3Endpoint = createUrl("https://location3.documents.azure.com");
    private final static URI Location4Endpoint = createUrl("https://location4.documents.azure.com");

    private final static Random random = new Random();

    @DataProvider(name = "sessionContainerDataProvider")
    public Object[][] sessionContainerDataProvider() {
        // 1. regionCount - no. of regions a request can be routed to
        // 2. requestCount - no. of requests to be tracked by the session container
        // 3. pkRangeId
        // 4. list of session token to region mappings each mapped to a request
        // 5. list of partition key value mapped to a partition key definition each mapped to a request
        // 6. writable regions/locations
        // 7. readable regions/locations
        // 8. connection policy
        // 9. can use multiple writable locations
        // 10. pkRangeId for which session token is to be resolved
        // 11. pk for which the session token is to be resolved
        // 12. expected resultant session token
        // NOTE:
        // 1. region format - region{some serial no} - e,g.: location1, location2, ..., regionN
        return new Object[][] {
            {
                ImmutableList.of(
                    constructRequestMetadataInstance(
                        OperationType.Read,
                        ResourceType.Document,
                        new PartitionKey("pk1"),
                        new PartitionKeyDefinition().setPaths(ImmutableList.of("/mypk")),
                        "dbs/db1/colls/coll1",
                        ImmutableMap.of("range_0:1#210#1=101#2=87#3=106", "location1"),
                        ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#210#1=101#2=87#3=106")
                    ),
                    constructRequestMetadataInstance(
                        OperationType.Read,
                        ResourceType.Document,
                        new PartitionKey("pk2"),
                        new PartitionKeyDefinition().setPaths(ImmutableList.of("/mypk")),
                        "dbs/db1/colls/coll1",
                        ImmutableMap.of("range_0:1#225#1=103#2=90#3=107", "location2"),
                        ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#225#1=103#2=90#3=107")
                    ),
                    constructRequestMetadataInstance(
                        OperationType.Read,
                        ResourceType.Document,
                        new PartitionKey("pk1"),
                        new PartitionKeyDefinition().setPaths(ImmutableList.of("/mypk")),
                        "dbs/db1/colls/coll1",
                        ImmutableMap.of("range_0:1#211#1=111#2=87#3=116", "location1"),
                        ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#211#1=111#2=87#3=116")
                    ),
                    constructRequestMetadataInstance(
                        OperationType.Read,
                        ResourceType.Document,
                        new PartitionKey("pk1"),
                        new PartitionKeyDefinition().setPaths(ImmutableList.of("/mypk")),
                        "dbs/db1/colls/coll1",
                        ImmutableMap.of("range_0:1#210#1=101#2=87#3=106", "location1"),
                        ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#210#1=101#2=87#3=106")
                    )
                ),
                ImmutableList.of(
                    Pair.of("location1", Location1Endpoint),
                    Pair.of("location2", Location2Endpoint),
                    Pair.of("location3", Location3Endpoint)
                ),
                ImmutableList.of(
                    Pair.of("location1", Location1Endpoint),
                    Pair.of("location2", Location2Endpoint),
                    Pair.of("location3", Location3Endpoint)
                ),
                new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig())
                    .setPreferredRegions(ImmutableList.of("location1", "location2")),
                true,
                "range_0",
                "pk1",
                "1#211#1=111#2=87#3=116"
            },
            {
                ImmutableList.of(
                    constructRequestMetadataInstance(
                        OperationType.Read,
                        ResourceType.Document,
                        new PartitionKey("pk1"),
                        new PartitionKeyDefinition().setPaths(ImmutableList.of("/mypk")),
                        "dbs/db1/colls/coll1",
                        ImmutableMap.of("range_0:1#221#1=101#2=87#3=106", "location1"),
                        ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#221#1=101#2=87#3=106")
                    ),
                    constructRequestMetadataInstance(
                        OperationType.Read,
                        ResourceType.Document,
                        new PartitionKey("pk2"),
                        new PartitionKeyDefinition().setPaths(ImmutableList.of("/mypk")),
                        "dbs/db1/colls/coll1",
                        ImmutableMap.of("range_0:1#225#1=103#2=90#3=107", "location3"),
                        ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#225#1=103#2=90#3=107")
                    ),
                    constructRequestMetadataInstance(
                        OperationType.Read,
                        ResourceType.Document,
                        new PartitionKey("pk1"),
                        new PartitionKeyDefinition().setPaths(ImmutableList.of("/mypk")),
                        "dbs/db1/colls/coll1",
                        ImmutableMap.of("range_0:1#211#1=113#2=87#3=116", "location2"),
                        ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#211#1=113#2=87#3=116")
                    ),
                    constructRequestMetadataInstance(
                        OperationType.Read,
                        ResourceType.Document,
                        new PartitionKey("pk1"),
                        new PartitionKeyDefinition().setPaths(ImmutableList.of("/mypk")),
                        "dbs/db1/colls/coll1",
                        ImmutableMap.of("range_0:1#210#1=101#2=87#3=119", "location1"),
                        ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#210#1=101#2=87#3=119")
                    )
                ),
                ImmutableList.of(
                    Pair.of("location1", Location1Endpoint),
                    Pair.of("location2", Location2Endpoint),
                    Pair.of("location3", Location3Endpoint)
                ),
                ImmutableList.of(
                    Pair.of("location1", Location1Endpoint),
                    Pair.of("location2", Location2Endpoint),
                    Pair.of("location3", Location3Endpoint)
                ),
                new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig())
                    .setPreferredRegions(ImmutableList.of("location1", "location2")),
                true,
                "range_0",
                "pk1",
                "1#221#1=113#2=87#3=119"
            },
            {
                ImmutableList.of(
                    constructRequestMetadataInstance(
                        OperationType.Read,
                        ResourceType.Document,
                        new PartitionKey("pk1"),
                        new PartitionKeyDefinition().setPaths(ImmutableList.of("/mypk")),
                        "dbs/db1/colls/coll1",
                        ImmutableMap.of("range_0:1#221#1=101#2=87#3=106", "location1"),
                        ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#221#1=101#2=87#3=106")
                    ),
                    constructRequestMetadataInstance(
                        OperationType.Read,
                        ResourceType.Document,
                        new PartitionKey("pk2"),
                        new PartitionKeyDefinition().setPaths(ImmutableList.of("/mypk")),
                        "dbs/db1/colls/coll1",
                        ImmutableMap.of("range_0:1#225#1=103#2=90#3=107", "location3"),
                        ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#225#1=103#2=90#3=107")
                    ),
                    constructRequestMetadataInstance(
                        OperationType.Read,
                        ResourceType.Document,
                        new PartitionKey("pk1"),
                        new PartitionKeyDefinition().setPaths(ImmutableList.of("/mypk")),
                        "dbs/db1/colls/coll1",
                        ImmutableMap.of("range_0:1#211#1=113#2=87#3=116", "location2"),
                        ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#211#1=113#2=87#3=116")
                    ),
                    constructRequestMetadataInstance(
                        OperationType.Read,
                        ResourceType.Document,
                        new PartitionKey("pk1"),
                        new PartitionKeyDefinition().setPaths(ImmutableList.of("/mypk")),
                        "dbs/db1/colls/coll1",
                        ImmutableMap.of("range_0:1#210#1=101#2=87#3=119", "location1"),
                        ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#210#1=101#2=87#3=119")
                    )
                ),
                ImmutableList.of(
                    Pair.of("location1", Location1Endpoint),
                    Pair.of("location2", Location2Endpoint),
                    Pair.of("location3", Location3Endpoint)
                ),
                ImmutableList.of(
                    Pair.of("location1", Location1Endpoint),
                    Pair.of("location2", Location2Endpoint),
                    Pair.of("location3", Location3Endpoint)
                ),
                new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig())
                    .setPreferredRegions(ImmutableList.of("location1", "location2")),
                true,
                "range_0",
                "pk3",
                "1#221#1=101#2=87#3=119"
            }
        };
    }

    @Test(groups = "unit")
    public void sessionContainer() throws Exception {
        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");

        int numCollections = 2;
        int numPartitionKeyRangeIds = 5;
        String regionContacted = "location1";

        for (int i = 0; i < numCollections; i++) {
            String collectionResourceId =
                ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId() + i).getDocumentCollectionId().toString();
            String collectionFullName = "dbs/db1/colls/collName_" + i;

            for (int j = 0; j < numPartitionKeyRangeIds; j++) {

                String partitionKeyRangeId = "range_" + j;
                String lsn = "1#" + j + "#4=90#5=2";
                String resultantSessionToken = partitionKeyRangeId + ":" + lsn;

                RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
                ConcurrentHashMap<String, String> sessionTokenToRegionMappings = new ConcurrentHashMap<>();
                sessionTokenToRegionMappings.put(resultantSessionToken, regionContacted);

                setSessionTokenToRegionMappingsOnCosmosDiagnostics(request, sessionTokenToRegionMappings);

                sessionContainer.setSessionToken(
                        request,
                        collectionResourceId,
                        collectionFullName,
                        ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, resultantSessionToken));
            }
        }

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.ReadFeed, ResourceType.DocumentCollection,
                "dbs/db1/colls/collName_1", Utils.getUTF8Bytes("content1"), new HashMap<>());

        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_1");
        assertThat(sessionToken.getLSN()).isEqualTo(1);

        DocumentServiceRequestContext dsrContext = new DocumentServiceRequestContext();
        PartitionKeyRange resolvedPKRange = new PartitionKeyRange();
        resolvedPKRange.setId("range_" + (numPartitionKeyRangeIds + 10));
        GatewayTestUtils.setParent(resolvedPKRange, ImmutableList.of("range_2", "range_x"));
        dsrContext.resolvedPartitionKeyRange = resolvedPKRange;
        request.requestContext = dsrContext;

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
        String regionContacted = "location1";

        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");

        RxDocumentServiceRequest request1 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Create, ResourceType.Document,
                collectionName + "/docs",  Utils.getUTF8Bytes("content1"), new HashMap<>());
        String sessionTokenWithPkRangeIdForRequest1 = partitionKeyRangeId + ":" + sessionToken;
        
        Map<String, String> respHeaders = new HashMap<>();
        RxDocumentServiceResponse resp = Mockito.mock(RxDocumentServiceResponse.class);
        Mockito.doReturn(respHeaders).when(resp).getResponseHeaders();
        respHeaders.put(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionTokenWithPkRangeIdForRequest1);
        respHeaders.put(HttpConstants.HttpHeaders.OWNER_FULL_NAME, collectionName);
        respHeaders.put(HttpConstants.HttpHeaders.OWNER_ID, collectionRid);
        
        ConcurrentHashMap<String, String> sessionTokenToRegionMappings = new ConcurrentHashMap<>();
        sessionTokenToRegionMappings.put(sessionTokenWithPkRangeIdForRequest1, regionContacted);
        
        setSessionTokenToRegionMappingsOnCosmosDiagnostics(request1, sessionTokenToRegionMappings);
        sessionContainer.setSessionToken(request1, resp.getResponseHeaders());

        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, Long> collectionNameToCollectionResourceId = (ConcurrentHashMap<String, Long>) FieldUtils.readField(sessionContainer, "collectionNameToCollectionResourceId", true);
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<Long, PkRangeBasedRegionScopedSessionTokenRegistry> collectionResourceIdToRegionScopedSessionTokens = (ConcurrentHashMap<Long, PkRangeBasedRegionScopedSessionTokenRegistry>) FieldUtils.readField(sessionContainer, "collectionResourceIdToRegionScopedSessionTokens", true);
        assertThat(collectionNameToCollectionResourceId).hasSize(1);
        assertThat(collectionResourceIdToRegionScopedSessionTokens).hasSize(1);
        assertThat(collectionNameToCollectionResourceId.get(collectionName)).isEqualTo(collectionRidAsLong);
        assertThat(collectionResourceIdToRegionScopedSessionTokens.get(collectionRidAsLong)).isNotNull();
        assertThat(collectionResourceIdToRegionScopedSessionTokens.get(collectionRidAsLong).getPkRangeIdToRegionScopedSessionTokens()).isNotNull();
        assertThat(collectionResourceIdToRegionScopedSessionTokens.get(collectionRidAsLong).getPkRangeIdToRegionScopedSessionTokens().get(partitionKeyRangeId)).isNotNull();
        assertThat(collectionResourceIdToRegionScopedSessionTokens.get(collectionRidAsLong).getPkRangeIdToRegionScopedSessionTokens().get(partitionKeyRangeId).get(regionContacted)).isNotNull();
        assertThat(collectionResourceIdToRegionScopedSessionTokens.get(collectionRidAsLong).getPkRangeIdToRegionScopedSessionTokens().get(partitionKeyRangeId).get(regionContacted).convertToString()).isEqualTo(sessionToken);

        RxDocumentServiceRequest request2 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read, ResourceType.Document,
                collectionName + "/docs",  Utils.getUTF8Bytes(""), new HashMap<>());

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
        String regionContacted = "location1";

        Map<String, String> respHeaders = new HashMap<>();

        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");

        RxDocumentServiceRequest request1 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Create, ResourceType.Document,
                collectionName + "/docs",  Utils.getUTF8Bytes("content1"), new HashMap<>());
        String initialSessionTokenWithPkRangeId = partitionKeyRangeId + ":" + initialSessionToken;
        
        ConcurrentHashMap<String, String> sessionTokenToRegionMappings = new ConcurrentHashMap<>();
        sessionTokenToRegionMappings.put(initialSessionTokenWithPkRangeId, regionContacted);
        
        setSessionTokenToRegionMappingsOnCosmosDiagnostics(request1, sessionTokenToRegionMappings);

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

        sessionTokenToRegionMappings = new ConcurrentHashMap<>();
        sessionTokenToRegionMappings.put(newSessionTokenWithPkRangeId, regionContacted);

        setSessionTokenToRegionMappingsOnCosmosDiagnostics(request1, sessionTokenToRegionMappings);
        sessionContainer.setSessionToken(request1, resp.getResponseHeaders());

        RxDocumentServiceRequest request2 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read, ResourceType.Document,
                collectionName + "/docs", Utils.getUTF8Bytes(""), new HashMap<>());

        ISessionToken resolvedSessionToken = sessionContainer.resolvePartitionLocalSessionToken(request2, partitionKeyRangeId);
        assertThat(resolvedSessionToken).isNotNull();
        assertThat(resolvedSessionToken.convertToString()).isEqualTo(expectedMergedSessionToken);
    }


    @Test(groups = "unit")
    public void resolveGlobalSessionTokenReturnsEmptyStringOnEmptyCache() {
        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read, ResourceType.Document,
                "dbs/db1/colls/collName/docs/doc1", new HashMap<>());
        assertThat(StringUtils.EMPTY).isEqualTo(sessionContainer.resolveGlobalSessionToken(request));
    }

    @Test(groups = "unit")
    public void resolveGlobalSessionTokenReturnsEmptyStringOnCacheMiss() {
        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");
        String partitionKeyRangeId = "range_0";
        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId()).getDocumentCollectionId().toString();
        String initialSessionToken = "1#100#1=20#2=5#3=30";
        String regionContacted = "location1";
        String resultantSessionToken = partitionKeyRangeId + ":" + initialSessionToken;

        RxDocumentServiceRequest collectionCreateRequest = RxDocumentServiceRequest.create(
            mockDiagnosticsClientContext(), OperationType.Create, ResourceType.DocumentCollection);
        ConcurrentHashMap<String, String> sessionTokenToRegionMappings = new ConcurrentHashMap<>();
        sessionTokenToRegionMappings.put(resultantSessionToken, regionContacted);

        setSessionTokenToRegionMappingsOnCosmosDiagnostics(collectionCreateRequest, sessionTokenToRegionMappings);
        sessionContainer.setSessionToken(collectionCreateRequest, documentCollectionId, "dbs/db1/colls1/collName",
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, partitionKeyRangeId + ":" + initialSessionToken));
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read, ResourceType.Document,
                "dbs/db1/colls1/collName2/docs/doc1", new HashMap<>());
        assertThat(StringUtils.EMPTY).isEqualTo(sessionContainer.resolveGlobalSessionToken(request));
    }

    @Test(groups = "unit")
    public void resolveGlobalSessionTokenReturnsTokenMapUsingName() {
        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");
        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId()).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName";
        String regionContacted = "location1";

        RxDocumentServiceRequest request1 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        String sessionTokenForRequest1 = "range_0:1#100#1=20#2=5#3=30";

        ConcurrentHashMap<String, String> sessionTokenToRegionMappingsForRequest1 = new ConcurrentHashMap<>();
        sessionTokenToRegionMappingsForRequest1.put(sessionTokenForRequest1, regionContacted);

        setSessionTokenToRegionMappingsOnCosmosDiagnostics(request1, sessionTokenToRegionMappingsForRequest1);
        sessionContainer.setSessionToken(request1, documentCollectionId, collectionFullName,
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionTokenForRequest1));

        RxDocumentServiceRequest request2 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        String sessionTokenForRequest2 = "range_1:1#101#1=20#2=5#3=30";

        ConcurrentHashMap<String, String> sessionTokenToRegionMappingsForRequest2 = new ConcurrentHashMap<>();
        sessionTokenToRegionMappingsForRequest2.put(sessionTokenForRequest2, regionContacted);

        setSessionTokenToRegionMappingsOnCosmosDiagnostics(request2, sessionTokenToRegionMappingsForRequest2);
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
        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");
        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId()).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName";
        String regionContacted = "location1";

        RxDocumentServiceRequest request1 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        String sessionTokenForRequest1 = "range_0:1#100#1=20#2=5#3=30";

        ConcurrentHashMap<String, String> sessionTokenToRegionMappingsForRequest1 = new ConcurrentHashMap<>();
        sessionTokenToRegionMappingsForRequest1.put(sessionTokenForRequest1, regionContacted);

        setSessionTokenToRegionMappingsOnCosmosDiagnostics(request1, sessionTokenToRegionMappingsForRequest1);
        sessionContainer.setSessionToken(request1, documentCollectionId, collectionFullName,
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#100#1=20#2=5#3=30"));

        RxDocumentServiceRequest request2 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        String sessionTokenForRequest2 = "range_1:1#101#1=20#2=5#3=30";

        ConcurrentHashMap<String, String> sessionTokenToRegionMappingsForRequest2 = new ConcurrentHashMap<>();
        sessionTokenToRegionMappingsForRequest2.put(sessionTokenForRequest2, regionContacted);

        setSessionTokenToRegionMappingsOnCosmosDiagnostics(request2, sessionTokenToRegionMappingsForRequest2);
        sessionContainer.setSessionToken(request2, documentCollectionId, collectionFullName,
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_1:1#101#1=20#2=5#3=30"));

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
            documentCollectionId, ResourceType.Document, new HashMap<>());

        String sessionToken = sessionContainer.resolveGlobalSessionToken(request);

        Set<String> tokens = Sets.newSet(sessionToken.split(","));
        assertThat(tokens.size()).isEqualTo(2);
        assertThat(tokens.contains("range_0:1#100#1=20#2=5#3=30")).isTrue();
        assertThat(tokens.contains("range_1:1#101#1=20#2=5#3=30")).isTrue();
    }


    @Test(groups = "unit")
    public void resolveLocalSessionTokenReturnsTokenMapUsingName() {
        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");
        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId()).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName";
        String regionContacted = "location1";

        RxDocumentServiceRequest request1 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        String sessionTokenForRequest1 = "range_0:1#100#1=20#2=5#3=30";

        ConcurrentHashMap<String, String> sessionTokenToRegionMappingsForRequest1 = new ConcurrentHashMap<>();
        sessionTokenToRegionMappingsForRequest1.put(sessionTokenForRequest1, regionContacted);

        setSessionTokenToRegionMappingsOnCosmosDiagnostics(request1, sessionTokenToRegionMappingsForRequest1);
        sessionContainer.setSessionToken(request1, documentCollectionId, collectionFullName,
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#100#1=20#2=5#3=30"));

        RxDocumentServiceRequest request2 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        String sessionTokenForRequest2 = "range_1:1#101#1=20#2=5#3=30";

        ConcurrentHashMap<String, String> sessionTokenToRegionMappingsForRequest2 = new ConcurrentHashMap<>();
        sessionTokenToRegionMappingsForRequest2.put(sessionTokenForRequest2, regionContacted);

        setSessionTokenToRegionMappingsOnCosmosDiagnostics(request2, sessionTokenToRegionMappingsForRequest2);
        sessionContainer.setSessionToken(request2, documentCollectionId, collectionFullName,
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_1:1#101#1=20#2=5#3=30"));

        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),OperationType.Read,
                collectionFullName + "/docs/doc1", ResourceType.Document);
        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken.getLSN()).isEqualTo(100);
        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_1");
        assertThat(sessionToken.getLSN()).isEqualTo(101);
    }

    @Test(groups = "unit")
    public void resolveLocalSessionTokenReturnsTokenMapUsingResourceId() {
        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");
        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId()).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName";
        String regionContacted = "location1";

        RxDocumentServiceRequest request1 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        String sessionTokenWithPkRangeIdForRequest1 = "range_0:1#100#1=20#2=5#3=30";
        ConcurrentHashMap<String, String> sessionTokenWithRegionMappingsForRequest1 = new ConcurrentHashMap<>();
        sessionTokenWithRegionMappingsForRequest1.put(sessionTokenWithPkRangeIdForRequest1, regionContacted);

        setSessionTokenToRegionMappingsOnCosmosDiagnostics(request1, sessionTokenWithRegionMappingsForRequest1);
        sessionContainer.setSessionToken(request1, documentCollectionId, collectionFullName,
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionTokenWithPkRangeIdForRequest1));

        RxDocumentServiceRequest request2 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        String sessionTokenWithPkRangeIdForRequest2 = "range_1:1#101#1=20#2=5#3=30";
        ConcurrentHashMap<String, String> sessionTokenWithRegionMappingsForRequest2 = new ConcurrentHashMap<>();
        sessionTokenWithRegionMappingsForRequest2.put(sessionTokenWithPkRangeIdForRequest2, regionContacted);

        setSessionTokenToRegionMappingsOnCosmosDiagnostics(request2, sessionTokenWithRegionMappingsForRequest2);
        sessionContainer.setSessionToken(request2, documentCollectionId, collectionFullName,
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionTokenWithPkRangeIdForRequest2));

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
            documentCollectionId, ResourceType.Document, new HashMap<>());

        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken).isNotNull();
        assertThat(sessionToken.getLSN()).isEqualTo(100);

        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_1");
        assertThat(sessionToken).isNotNull();
        assertThat(sessionToken.getLSN()).isEqualTo(101);
    }

    @Test(groups = "unit")
    public void resolveLocalSessionTokenReturnsNullOnPartitionMiss() {
        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");
        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId()).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName";
        String regionContacted = "location1";

        RxDocumentServiceRequest request1 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        String sessionTokenWithPkRangeIdForRequest1 = "range_0:1#100#1=20#2=5#3=30";
        ConcurrentHashMap<String, String> sessionTokenWithRegionMappingsForRequest1 = new ConcurrentHashMap<>();

        setSessionTokenToRegionMappingsOnCosmosDiagnostics(request1, sessionTokenWithRegionMappingsForRequest1);
        sessionContainer.setSessionToken(request1, documentCollectionId, collectionFullName,
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionTokenWithPkRangeIdForRequest1));

        RxDocumentServiceRequest request2 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        String sessionTokenWithPkRangeIdForRequest2 = "range_1:1#101#1=20#2=5#3=30";
        ConcurrentHashMap<String, String> sessionTokenWithRegionMappingsForRequest2 = new ConcurrentHashMap<>();
        sessionTokenWithRegionMappingsForRequest2.put(sessionTokenWithPkRangeIdForRequest2, regionContacted);

        setSessionTokenToRegionMappingsOnCosmosDiagnostics(request2, sessionTokenWithRegionMappingsForRequest2);

        sessionContainer.setSessionToken(request2, documentCollectionId, collectionFullName,
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionTokenWithPkRangeIdForRequest2));

        RxDocumentServiceRequest requestToResultInPkRangeIdBasedMiss = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
            documentCollectionId, ResourceType.Document, new HashMap<>());
        requestToResultInPkRangeIdBasedMiss.requestContext.resolvedPartitionKeyRange = new PartitionKeyRange();
        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(requestToResultInPkRangeIdBasedMiss, "range_2");
        assertThat(sessionToken).isNull();
    }

    @Test(groups = "unit")
    public void resolveLocalSessionTokenReturnsNullOnCollectionMiss() {
        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");
        int randomCollectionId = getRandomCollectionId();
        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), randomCollectionId).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName";
        String regionContacted = "location1";

        RxDocumentServiceRequest request1 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        String sessionTokenWithPkRangeIdForRequest1 = "range_0:1#100#1=20#2=5#3=30";
        ConcurrentHashMap<String, String> sessionTokenWithRegionMappingsForRequest1 = new ConcurrentHashMap<>();

        setSessionTokenToRegionMappingsOnCosmosDiagnostics(request1, sessionTokenWithRegionMappingsForRequest1);

        sessionContainer.setSessionToken(request1, documentCollectionId, collectionFullName,
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionTokenWithPkRangeIdForRequest1));

        RxDocumentServiceRequest request2 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        String sessionTokenWithPkRangeIdForRequest2 = "range_1:1#101#1=20#2=5#3=30";
        ConcurrentHashMap<String, String> sessionTokenWithRegionMappingsForRequest2 = new ConcurrentHashMap<>();
        sessionTokenWithRegionMappingsForRequest2.put(sessionTokenWithPkRangeIdForRequest2, regionContacted);

        setSessionTokenToRegionMappingsOnCosmosDiagnostics(request2, sessionTokenWithRegionMappingsForRequest2);

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
        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");
        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId()).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName";
        String regionContacted = "location1";

        RxDocumentServiceRequest request1 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        String sessionTokenWithPkRangeIdForRequest1 = "range_0:1#100#1=20#2=5#3=30";
        ConcurrentHashMap<String, String> sessionTokenWithRegionMappingsForRequest1 = new ConcurrentHashMap<>();

        setSessionTokenToRegionMappingsOnCosmosDiagnostics(request1, sessionTokenWithRegionMappingsForRequest1);
        sessionContainer.setSessionToken(request1, documentCollectionId, collectionFullName,
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionTokenWithPkRangeIdForRequest1));

        RxDocumentServiceRequest request2 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        String sessionTokenWithPkRangeIdForRequest2 = "range_1:1#101#1=20#2=5#3=30";
        ConcurrentHashMap<String, String> sessionTokenWithRegionMappingsForRequest2 = new ConcurrentHashMap<>();
        sessionTokenWithRegionMappingsForRequest2.put(sessionTokenWithPkRangeIdForRequest2, regionContacted);

        setSessionTokenToRegionMappingsOnCosmosDiagnostics(request2, sessionTokenWithRegionMappingsForRequest2);
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
        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");
        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId()).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName";
        String regionContacted = "location1";
        String unparsedSessionToken = "range_0:1#100#1=20#2=5#3=30";

        RxDocumentServiceRequest documentCollectionCreateRequest = createMockCollectionCreateRequest();
        ConcurrentHashMap<String, String> sessionTokenToRegionMappingsForCollCreateRequest = new ConcurrentHashMap<>();
        sessionTokenToRegionMappingsForCollCreateRequest.put(unparsedSessionToken, regionContacted);
        setSessionTokenToRegionMappingsOnCosmosDiagnostics(documentCollectionCreateRequest, sessionTokenToRegionMappingsForCollCreateRequest);

        sessionContainer.setSessionToken(documentCollectionCreateRequest, documentCollectionId, collectionFullName,
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, unparsedSessionToken));

        //  Test getResourceId based
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
                documentCollectionId, ResourceType.Document, new HashMap<>());
        ConcurrentHashMap<String, String> sessionTokenToRegionMappings = new ConcurrentHashMap<>();
        sessionTokenToRegionMappings.put(unparsedSessionToken, regionContacted);
        setSessionTokenToRegionMappingsOnCosmosDiagnostics(request, sessionTokenToRegionMappings);

        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken.getLSN()).isEqualTo(100);

        //  Test names based
        request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),OperationType.Read,
                collectionFullName + "/docs/doc1", ResourceType.Document);
        sessionTokenToRegionMappings = new ConcurrentHashMap<>();
        sessionTokenToRegionMappings.put(unparsedSessionToken, regionContacted);
        setSessionTokenToRegionMappingsOnCosmosDiagnostics(request, sessionTokenToRegionMappings);

        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken.getLSN()).isEqualTo(100);

        sessionContainer.clearTokenByCollectionFullName(collectionFullName);

        //  Test resourceId based
        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
                documentCollectionId, ResourceType.Document, new HashMap<>());
        sessionTokenToRegionMappings = new ConcurrentHashMap<>();
        sessionTokenToRegionMappings.put(unparsedSessionToken, regionContacted);
        setSessionTokenToRegionMappingsOnCosmosDiagnostics(request, sessionTokenToRegionMappings);

        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken).isNull();

        //  Test names based
        request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),OperationType.Read,
                collectionFullName + "/docs/doc1", ResourceType.Document);
        sessionTokenToRegionMappings = new ConcurrentHashMap<>();
        sessionTokenToRegionMappings.put(unparsedSessionToken, regionContacted);
        setSessionTokenToRegionMappingsOnCosmosDiagnostics(request, sessionTokenToRegionMappings);

        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken).isNull();
    }

    @Test(groups = "unit")
    public void clearTokenByResourceIdRemovesToken() {
        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");
        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId()).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName";
        String regionContacted = "location1";
        String unparsedSessionToken = "range_0:1#100#1=20#2=5#3=30";

        RxDocumentServiceRequest documentCollectionCreateRequest = createMockCollectionCreateRequest();
        ConcurrentHashMap<String, String> sessionTokenToRegionMappingsForCollectionCreateRequest = new ConcurrentHashMap<>();
        sessionTokenToRegionMappingsForCollectionCreateRequest.put(unparsedSessionToken, regionContacted);
        setSessionTokenToRegionMappingsOnCosmosDiagnostics(documentCollectionCreateRequest, sessionTokenToRegionMappingsForCollectionCreateRequest);

        sessionContainer.setSessionToken(documentCollectionCreateRequest, documentCollectionId, collectionFullName,
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, unparsedSessionToken));

        //  Test resourceId based
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
                documentCollectionId, ResourceType.Document, new HashMap<>());
        ConcurrentHashMap<String, String> sessionTokenToRegionMappingsForRequest = new ConcurrentHashMap<>();
        sessionTokenToRegionMappingsForRequest.put(unparsedSessionToken, regionContacted);
        setSessionTokenToRegionMappingsOnCosmosDiagnostics(request, sessionTokenToRegionMappingsForRequest);

        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken).isNotNull();
        assertThat(sessionToken.getLSN()).isEqualTo(100);

        //  Test names based
        request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),OperationType.Read,
                collectionFullName + "/docs/doc1", ResourceType.Document);
        sessionTokenToRegionMappingsForRequest = new ConcurrentHashMap<>();
        sessionTokenToRegionMappingsForRequest.put(unparsedSessionToken, regionContacted);
        setSessionTokenToRegionMappingsOnCosmosDiagnostics(request, sessionTokenToRegionMappingsForRequest);

        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken).isNotNull();
        assertThat(sessionToken.getLSN()).isEqualTo(100);

        sessionContainer.clearTokenByResourceId(documentCollectionId);

        //  Test resourceId based
        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
                documentCollectionId, ResourceType.Document, new HashMap<>());
        sessionTokenToRegionMappingsForRequest = new ConcurrentHashMap<>();
        sessionTokenToRegionMappingsForRequest.put(unparsedSessionToken, regionContacted);
        setSessionTokenToRegionMappingsOnCosmosDiagnostics(request, sessionTokenToRegionMappingsForRequest);

        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken).isNull();

        //  Test names based
        request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),OperationType.Read,
                collectionFullName + "/docs/doc1", ResourceType.Document);
        sessionTokenToRegionMappingsForRequest = new ConcurrentHashMap<>();
        sessionTokenToRegionMappingsForRequest.put(unparsedSessionToken, regionContacted);
        setSessionTokenToRegionMappingsOnCosmosDiagnostics(request, sessionTokenToRegionMappingsForRequest);

        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken).isNull();
    }

    @Test(groups = "unit")
    public void clearTokenKeepsUnmatchedCollection() {
        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");
        int randomCollectionId = getRandomCollectionId();
        String documentCollectionId1 = ResourceId.newDocumentCollectionId(getRandomDbId(), randomCollectionId).getDocumentCollectionId().toString();
        String collectionFullName1 = "dbs/db1/colls1/collName1";
        String regionContacted = "location1";
        String unparsedSessionToken = "range_0:1#100#1=20#2=5#3=30";

        RxDocumentServiceRequest documentCollectionCreateRequest = createMockCollectionCreateRequest();
        ConcurrentHashMap<String, String> sessionTokenToRegionMappings = new ConcurrentHashMap<>();
        sessionTokenToRegionMappings.put(unparsedSessionToken, regionContacted);
        setSessionTokenToRegionMappingsOnCosmosDiagnostics(documentCollectionCreateRequest, sessionTokenToRegionMappings);

        sessionContainer.setSessionToken(documentCollectionCreateRequest, documentCollectionId1, collectionFullName1,
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, unparsedSessionToken));

        //  Test resourceId based
        RxDocumentServiceRequest request1 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
                documentCollectionId1, ResourceType.Document, new HashMap<>());
        ConcurrentHashMap<String, String> sessionTokenToRegionMappingsForRequest1 = new ConcurrentHashMap<>();
        sessionTokenToRegionMappingsForRequest1.put(unparsedSessionToken, regionContacted);
        setSessionTokenToRegionMappingsOnCosmosDiagnostics(request1, sessionTokenToRegionMappingsForRequest1);

        String documentCollectionId2 = ResourceId.newDocumentCollectionId(getRandomDbId(), randomCollectionId - 1).getDocumentCollectionId().toString();
        String collectionFullName2 = "dbs/db1/colls1/collName2";

        //  Test resourceId based
        RxDocumentServiceRequest request2 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
                documentCollectionId2, ResourceType.Document, new HashMap<>());
        ConcurrentHashMap<String, String> sessionTokenToRegionMappingsForRequest2 = new ConcurrentHashMap<>();
        sessionTokenToRegionMappingsForRequest2.put(unparsedSessionToken, regionContacted);
        setSessionTokenToRegionMappingsOnCosmosDiagnostics(request2, sessionTokenToRegionMappingsForRequest2);

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
        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");
        sessionContainer.setSessionToken(null, new HashMap<>());
    }

    @Test(groups = "unit")
    public void setSessionTokenSetsTokenWhenRequestIsntNameBased() {
        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");
        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId()).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName";
        String regionContacted = "location1";

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
                collectionFullName + "/docs/doc1", ResourceType.Document, new HashMap<>());
        String sessionTokenWithPkRangeIdForRequest = "range_0:1#100#4=90#5=1";
        ConcurrentHashMap<String, String> sessionTokenToRegionMappingsForRequest = new ConcurrentHashMap<>();
        sessionTokenToRegionMappingsForRequest.put(sessionTokenWithPkRangeIdForRequest, regionContacted);
        setSessionTokenToRegionMappingsOnCosmosDiagnostics(request, sessionTokenToRegionMappingsForRequest);
        request.setResourceId(documentCollectionId);

        assertThat(request.getIsNameBased()).isFalse();
        sessionContainer.setSessionToken(request, ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionTokenWithPkRangeIdForRequest));
        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read, documentCollectionId, ResourceType.Document, new HashMap<>());
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
        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");
        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId()).getDocumentCollectionId().toString();
        String collectionFullName1 = "dbs/db1/colls1/collName1";
        String collectionFullName2 = "dbs/db1/colls1/collName2";
        String regionContacted = "location1";

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
                collectionFullName1 + "/docs/doc1", ResourceType.Document, new HashMap<>());
        String sessionTokenWithPkRangeIdForRequest = "range_0:1#100#4=90#5=1";
        ConcurrentHashMap<String, String> sessionTokenToRegionMappingsForRequest = new ConcurrentHashMap<>();
        sessionTokenToRegionMappingsForRequest.put(sessionTokenWithPkRangeIdForRequest, regionContacted);
        setSessionTokenToRegionMappingsOnCosmosDiagnostics(request, sessionTokenToRegionMappingsForRequest);
        request.setResourceId(documentCollectionId);

        sessionContainer.setSessionToken(request,
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionTokenWithPkRangeIdForRequest,
                        HttpConstants.HttpHeaders.OWNER_FULL_NAME, collectionFullName2));

        request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),OperationType.Read, collectionFullName1 + "/docs/doc1", ResourceType.Document);
        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken).isNull();

        request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),OperationType.Read, collectionFullName2 + "/docs/doc1", ResourceType.Document);
        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken).isNotNull();
        assertThat(sessionToken.getLSN()).isEqualTo(100);
    }

    @Test(groups = "unit")
    public void setSessionTokenIgnoresOwnerIdWhenRequestIsntNameBased() {
        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");
        int randomCollectionId = getRandomCollectionId();
        int randomDbId = getRandomDbId();
        String documentCollectionId1 = ResourceId.newDocumentCollectionId(randomDbId, randomCollectionId).getDocumentCollectionId().toString();
        String documentCollectionId2 = ResourceId.newDocumentCollectionId(randomDbId, randomCollectionId - 1).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName1";
        String regionContacted = "location1";

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
                collectionFullName + "/docs/doc1", ResourceType.Document, new HashMap<>());
        request.setResourceId(documentCollectionId1);

        String sessionTokenWithPkRangeIdForRequest = "range_0:1#100#4=90#5=1";
        ConcurrentHashMap<String, String> sessionTokenToRegionMappingsForRequest = new ConcurrentHashMap<>();
        sessionTokenToRegionMappingsForRequest.put(sessionTokenWithPkRangeIdForRequest, regionContacted);
        setSessionTokenToRegionMappingsOnCosmosDiagnostics(request, sessionTokenToRegionMappingsForRequest);

        assertThat(request.getIsNameBased()).isFalse();

        sessionContainer.setSessionToken(request,
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#100#4=90#5=1",
                        HttpConstants.HttpHeaders.OWNER_ID, documentCollectionId2));

        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
                documentCollectionId1, ResourceType.Document, new HashMap<>());
        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken).isNotNull();
        assertThat(sessionToken.getLSN()).isEqualTo(100);


        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
                documentCollectionId2, ResourceType.Document, new HashMap<>());
        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken).isNull();
    }

    @Test(groups = "unit")
    public void setSessionTokenGivesPriorityToOwnerIdOverResourceIdWhenRequestIsNameBased() {
        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");
        int randomCollectionId = getRandomCollectionId();
        int randomDbId = getRandomDbId();
        String documentCollectionId1 = ResourceId.newDocumentCollectionId(randomDbId, randomCollectionId).getDocumentCollectionId().toString();
        String documentCollectionId2 = ResourceId.newDocumentCollectionId(randomDbId, randomCollectionId - 1).getDocumentCollectionId().toString();

        String collectionFullName = "dbs/db1/colls1/collName1";
        String regionContacted = "location1";

        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),OperationType.Read,
                collectionFullName + "/docs/doc1", ResourceType.Document);
        request.setResourceId(documentCollectionId1);

        String sessionTokenWithPkRangeIdForRequest = "range_0:1#100#4=90#5=1";
        ConcurrentHashMap<String, String> sessionTokenToRegionMappingsForRequest = new ConcurrentHashMap<>();
        sessionTokenToRegionMappingsForRequest.put(sessionTokenWithPkRangeIdForRequest, regionContacted);

        setSessionTokenToRegionMappingsOnCosmosDiagnostics(request, sessionTokenToRegionMappingsForRequest);
        assertThat(request.getIsNameBased()).isTrue();

        sessionContainer.setSessionToken(request,
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionTokenWithPkRangeIdForRequest,
                        HttpConstants.HttpHeaders.OWNER_ID, documentCollectionId2));

        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
                documentCollectionId1, ResourceType.Document, new HashMap<>());
        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken).isNull();

        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
                documentCollectionId2, ResourceType.Document, new HashMap<>());
        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken.getLSN()).isEqualTo(100);
    }

    @Test(groups = "unit")
    public void setSessionTokenDoesntWorkForMasterQueries() {
        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");
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
        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");
        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId()).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName";
        String regionContacted = "location1";

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
                collectionFullName + "/docs/doc1", ResourceType.Document, new HashMap<>());

        String sessionTokenWithPkRangeIdForRequest = "range_0:1#100#4=90#5=1";
        ConcurrentHashMap<String, String> sessionTokenToRegionMappingsForRequest = new ConcurrentHashMap<>();
        sessionTokenToRegionMappingsForRequest.put(sessionTokenWithPkRangeIdForRequest, regionContacted);
        setSessionTokenToRegionMappingsOnCosmosDiagnostics(request, sessionTokenToRegionMappingsForRequest);

        request.setResourceId(documentCollectionId);

        sessionContainer.setSessionToken(request, ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionTokenWithPkRangeIdForRequest));

        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
                collectionFullName + "/docs/doc1", ResourceType.Document, new HashMap<>());

        String sessionTokenWithPkRangeIdToBeOverwritten = "range_0:1#105#4=90#5=1";

        sessionTokenToRegionMappingsForRequest = new ConcurrentHashMap<>();
        sessionTokenToRegionMappingsForRequest.put(sessionTokenWithPkRangeIdToBeOverwritten, regionContacted);
        setSessionTokenToRegionMappingsOnCosmosDiagnostics(request, sessionTokenToRegionMappingsForRequest);

        request.setResourceId(documentCollectionId);
        sessionContainer.setSessionToken(request, ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionTokenWithPkRangeIdToBeOverwritten));

        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
                documentCollectionId, ResourceType.Document, new HashMap<>());
        request.setResourceId(documentCollectionId);
        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken).isNotNull();
        assertThat(sessionToken.getLSN()).isEqualTo(105);
    }

    @Test(groups = "unit")
    public void setSessionTokenOverwriteLowerLSN() {
        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");
        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId()).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName";
        String regionContacted = "location1";

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
                collectionFullName + "/docs/doc1", ResourceType.Document, new HashMap<>());

        String sessionTokenWithPkRangeIdForRequest = "range_0:1#105#4=90#5=1";
        ConcurrentHashMap<String, String> sessionTokenToRegionMappingsForRequest = new ConcurrentHashMap<>();
        sessionTokenToRegionMappingsForRequest.put(sessionTokenWithPkRangeIdForRequest, regionContacted);
        setSessionTokenToRegionMappingsOnCosmosDiagnostics(request, sessionTokenToRegionMappingsForRequest);
        request.setResourceId(documentCollectionId);

        sessionContainer.setSessionToken(request, ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionTokenWithPkRangeIdForRequest));

        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
                collectionFullName + "/docs/doc1", ResourceType.Document, new HashMap<>());

        String sessionTokenWithPkRangeIdToBeOverwritten = "range_0:1#100#4=90#5=1";

        sessionTokenToRegionMappingsForRequest = new ConcurrentHashMap<>();
        sessionTokenToRegionMappingsForRequest.put(sessionTokenWithPkRangeIdToBeOverwritten, regionContacted);
        setSessionTokenToRegionMappingsOnCosmosDiagnostics(request, sessionTokenToRegionMappingsForRequest);
        request.setResourceId(documentCollectionId);

        sessionContainer.setSessionToken(request, ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionTokenWithPkRangeIdToBeOverwritten));

        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
                documentCollectionId, ResourceType.Document, new HashMap<>());
        request.setResourceId(documentCollectionId);
        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken).isNotNull();
        assertThat(sessionToken.getLSN()).isEqualTo(105);
    }

    @Test(groups = "unit")
    public void setSessionTokenDoesNothingOnEmptySessionTokenHeader() {
        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");
        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId()).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName";
        String regionContacted = "location1";

        RxDocumentServiceRequest docReadRequest1 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);

        String sessionTokenWithPkRangeIdForDocReadRequest1 = "range_0:1#100#4=90#5=1";
        ConcurrentHashMap<String, String> sessionTokenToRegionMappingsForDocReadRequest1 = new ConcurrentHashMap<>();
        sessionTokenToRegionMappingsForDocReadRequest1.put(sessionTokenWithPkRangeIdForDocReadRequest1, regionContacted);
        setSessionTokenToRegionMappingsOnCosmosDiagnostics(docReadRequest1, sessionTokenToRegionMappingsForDocReadRequest1);

        sessionContainer.setSessionToken(docReadRequest1, documentCollectionId, collectionFullName + "/docs/doc1",
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionTokenWithPkRangeIdForDocReadRequest1));
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
                documentCollectionId, ResourceType.Document, new HashMap<>());
        String sessionToken = sessionContainer.resolveGlobalSessionToken(request);
        Set<String> tokens = Sets.newSet(sessionToken.split(","));
        assertThat(tokens.size()).isEqualTo(1);
        assertThat(tokens.contains(sessionTokenWithPkRangeIdForDocReadRequest1)).isTrue();

        RxDocumentServiceRequest docReadRequest2 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        sessionContainer.setSessionToken(docReadRequest2, documentCollectionId, collectionFullName, new HashMap<>());

        request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
                documentCollectionId, ResourceType.Document, new HashMap<>());
        sessionToken = sessionContainer.resolveGlobalSessionToken(request);
        tokens = Sets.newSet(sessionToken.split(","));
        assertThat(tokens.size()).isEqualTo(1);
        assertThat(tokens.contains(sessionTokenWithPkRangeIdForDocReadRequest1)).isTrue();
    }

    @Test(groups = "unit")
    public void sessionCapturingDisabled() throws Exception {
        SessionContainer sessionContainer = new SessionContainer("127.0.0.1", true);

        int numCollections = 2;
        int numPartitionKeyRangeIds = 5;
        String regionContacted = "location1";

        for (int i = 0; i < numCollections; i++) {
            String collectionResourceId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId() + i).getDocumentCollectionId().toString();
            String collectionFullName = "dbs/db1/colls/collName_" + i;

            for (int j = 0; j < numPartitionKeyRangeIds; j++) {

                String partitionKeyRangeId = "range_" + j;
                String lsn = "1#" + j + "#4=90#5=2";
                String resultantSessionToken = partitionKeyRangeId + ":" + lsn;

                RxDocumentServiceRequest documentReadRequest = RxDocumentServiceRequest
                    .create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);

                ConcurrentHashMap<String, String> sessionTokenToRegionMappingsForDocumentReadRequest = new ConcurrentHashMap<>();
                sessionTokenToRegionMappingsForDocumentReadRequest.put(resultantSessionToken, regionContacted);

                setSessionTokenToRegionMappingsOnCosmosDiagnostics(documentReadRequest, sessionTokenToRegionMappingsForDocumentReadRequest);

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
        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");

        int randomCollectionId = getRandomCollectionId();
        String documentCollectionId1 = ResourceId.newDocumentCollectionId(getRandomDbId(), randomCollectionId).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName1";
        String regionContacted = "location1";

        // Set token for the parent
        String parentPKRangeId = "0";
        String parentSession = "1#100#4=90#5=1";
        String resultantParentSessionToken = parentPKRangeId + ":" + parentSession;

        RxDocumentServiceRequest request1 = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);

        ConcurrentHashMap<String, String> sessionTokenToRegionMappingsForRequest1 = new ConcurrentHashMap<>();
        sessionTokenToRegionMappingsForRequest1.put(resultantParentSessionToken, regionContacted);

        setSessionTokenToRegionMappingsOnCosmosDiagnostics(request1, sessionTokenToRegionMappingsForRequest1);
        sessionContainer.setSessionToken(
            request1,
            documentCollectionId1,
            collectionFullName,
            ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, resultantParentSessionToken));

        // send requests for children
        String childPKRangeId = "1";
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
            documentCollectionId1, ResourceType.Document, new HashMap<>());
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
        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");

        int randomCollectionId = getRandomCollectionId();
        String documentCollectionId1 = ResourceId.newDocumentCollectionId(getRandomDbId(), randomCollectionId).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName1";
        String regionContacted = "location1";

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
        ConcurrentHashMap<String, String> sessionTokenToRegionMappingsForRequest1 = new ConcurrentHashMap<>();
        sessionTokenToRegionMappingsForRequest1.put(parent1SessionToken, regionContacted);

        setSessionTokenToRegionMappingsOnCosmosDiagnostics(request1, sessionTokenToRegionMappingsForRequest1);
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
        ConcurrentHashMap<String, String> sessionTokenToRegionMappingsForRequest2 = new ConcurrentHashMap<>();
        sessionTokenToRegionMappingsForRequest2.put(parent2SessionToken, regionContacted);

        setSessionTokenToRegionMappingsOnCosmosDiagnostics(request2, sessionTokenToRegionMappingsForRequest2);
        sessionContainer.setSessionToken(
            request2,
            documentCollectionId1,
            collectionFullName,
            ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, parent2SessionToken));

        // send requests for children
        String childPKRangeId = "2";
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(),OperationType.Read,
            documentCollectionId1, ResourceType.Document, new HashMap<>());
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

    // 1. regionCount - no. of regions a request can be routed to
    // 2. requestCount - no. of requests to be tracked by the session container
    // 3. pkRangeId
    // 4. list of session token to region mappings each mapped to a request
    // 5. list of partition key value mapped to a partition key definition each mapped to a request
    // 6. writable regions/locations
    // 7. readable regions/locations
    // 8. connection policy
    @Test(groups = {"unit"}, dataProvider = "sessionContainerDataProvider")
    public void resolvePartitionLocalSessionContainer(
        List<RequestMetadata> requestMetadataList,
        List<Pair<String, URI>> writableLocationToURIMappings,
        List<Pair<String, URI>> readableLocationToURIMappings,
        ConnectionPolicy connectionPolicy,
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
        SessionContainer sessionContainer = null;

        List<URI> writableURIs = writableLocationToURIMappings
            .stream()
            .map(locationToURIMapping -> locationToURIMapping.getValue())
            .collect(Collectors.toList());
        List<String> writableLocations = writableLocationToURIMappings
            .stream()
            .map(locationToURIMapping -> locationToURIMapping.getKey())
            .collect(Collectors.toList());
        List<URI> readableURIs = readableLocationToURIMappings
            .stream()
            .map(locationToURIMapping -> locationToURIMapping.getValue())
            .collect(Collectors.toList());
        List<String> readableLocations = readableLocationToURIMappings
            .stream()
            .map(locationToURIMapping -> locationToURIMapping.getKey())
            .collect(Collectors.toList());

        DatabaseAccount databaseAccount = ModelBridgeUtils.createDatabaseAccount(
            readableLocationToURIMappings.stream().map(locationToURIMapping -> createDatabaseAccountLocation(locationToURIMapping.getKey(), locationToURIMapping.getValue().toString())).collect(Collectors.toList()),
            writableLocationToURIMappings.stream().map(locationToURIMapping -> createDatabaseAccountLocation(locationToURIMapping.getKey(), locationToURIMapping.getValue().toString())).collect(Collectors.toList()),
            canUseMultipleWritableLocations);

//        try {
            globalEndpointManagerMock = Mockito.mock(GlobalEndpointManager.class);

            Mockito
                .when(globalEndpointManagerMock.getLatestDatabaseAccount())
                .thenReturn(databaseAccount);

            Mockito
                .when(globalEndpointManagerMock.getConnectionPolicy())
                .thenReturn(connectionPolicy);

            Mockito
                .when(globalEndpointManagerMock.getApplicableWriteEndpoints(Mockito.anyList()))
                .thenReturn(new UnmodifiableList<>(writableURIs));

            Mockito
                .when(globalEndpointManagerMock.canUseMultipleWriteLocations(Mockito.any()))
                .thenReturn(true);

            sessionContainer = new SessionContainer(hostName, disableSessionCapturing, globalEndpointManagerMock);

            for (RequestMetadata requestMetadata : requestMetadataList) {
                sessionContainer.setSessionToken(
                    requestMetadata.request, documentCollectionId1, collectionFullName, requestMetadata.responseHeaders);
            }

            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
            request.setResourceId(documentCollectionId1);
            request.setPartitionKeyInternal(ModelBridgeInternal.getPartitionKeyInternal(new PartitionKey(pkToBeUsedForSessionTokenResolution)));
            request.setPartitionKeyDefinition(new PartitionKeyDefinition().setPaths(ImmutableList.of("/mypk")));

            if (!Strings.isNullOrEmpty(pkRangeIdToBeUsedForSessionTokenResolution)) {
                ISessionToken resolvedSessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, pkRangeIdToBeUsedForSessionTokenResolution);
                assertThat(resolvedSessionToken).isNotNull();
                assertThat(resolvedSessionToken.convertToString()).isEqualTo(expectedSessionToken);
            }

//        } catch (Exception exception) {
//            fail("A failure occurred for reason : " + exception);
//        } finally {
//
//            if (globalEndpointManagerMock != null) {
//                globalEndpointManagerMock.close();
//            }
//        }
    }

    private static int getRandomCollectionId() {
        return random.nextInt(Integer.MAX_VALUE / 2) - (Integer.MAX_VALUE / 2);
    }

    private static int getRandomDbId() {
        return random.nextInt(Integer.MAX_VALUE / 2);
    }

    private static RequestMetadata constructRequestMetadataInstance(
        OperationType operationType,
        ResourceType resourceType,
        PartitionKey partitionKey,
        PartitionKeyDefinition partitionKeyDefinition,
        String collectionResourceId,
        Map<String, String> sessionTokenToRegionMappings,
        Map<String, String> responseHeaders) {

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            mockDiagnosticsClientContext(),
            operationType,
            resourceType);

        request.setResourceId(collectionResourceId);
        request.setPartitionKeyInternal(BridgeInternal.getPartitionKeyInternal(partitionKey));
        request.setPartitionKeyDefinition(partitionKeyDefinition);

        setSessionTokenToRegionMappingsOnCosmosDiagnostics(request, new ConcurrentHashMap<>(sessionTokenToRegionMappings));

        return new RequestMetadata(request, responseHeaders);
    }

    private static RxDocumentServiceRequest createMockCollectionCreateRequest() {

        RxDocumentServiceRequest collectionCreateRequest = RxDocumentServiceRequest.create(
            mockDiagnosticsClientContext(),
            OperationType.Create,
            ResourceType.DocumentCollection);

        collectionCreateRequest.requestContext.cosmosDiagnostics = Mockito.mock(CosmosDiagnostics.class);

        return collectionCreateRequest;
    }

    private static void setSessionTokenToRegionMappingsOnCosmosDiagnostics(RxDocumentServiceRequest request, ConcurrentHashMap<String, String> sessionTokenToRegionMapping) {
        request.requestContext.cosmosDiagnostics = diagnosticsAccessor.create(mockDiagnosticsClientContext(), 1);
        diagnosticsAccessor.setSessionTokenToRegionMappings(request.requestContext.cosmosDiagnostics, sessionTokenToRegionMapping);
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
