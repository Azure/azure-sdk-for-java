// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.GatewayTestUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.mockito.Mockito;
import org.mockito.internal.util.collections.Sets;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link SessionContainer}
 */
public class SessionContainerTest {

    private final static Random random = new Random();

    @Test(groups = "unit")
    public void sessionContainer() throws Exception {
        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");

        int numCollections = 2;
        int numPartitionKeyRangeIds = 5;

        for (int i = 0; i < numCollections; i++) {
            String collectionResourceId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId() + i).getDocumentCollectionId().toString();
            String collectionFullName = "dbs/db1/colls/collName_" + i;

            for (int j = 0; j < numPartitionKeyRangeIds; j++) {

                String partitionKeyRangeId = "range_" + j;
                String lsn = "1#" + j + "#4=90#5=2";

                sessionContainer.setSessionToken(
                        collectionResourceId,
                        collectionFullName,
                        ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, partitionKeyRangeId + ":" + lsn));
            }
        }

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.ReadFeed, ResourceType.DocumentCollection,
                "dbs/db1/colls/collName_1", IOUtils.toInputStream("content1", "UTF-8"), new HashMap<>());

        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_1");
        assertThat(sessionToken.getLSN()).isEqualTo(1);

        DocumentServiceRequestContext dsrContext = new DocumentServiceRequestContext();
        PartitionKeyRange resolvedPKRange = new PartitionKeyRange();
        resolvedPKRange.id("range_" + (numPartitionKeyRangeIds + 10));
        GatewayTestUtils.setParent(resolvedPKRange, ImmutableList.of("range_2", "range_x"));
        dsrContext.resolvedPartitionKeyRange = resolvedPKRange;
        request.requestContext = dsrContext;

        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, resolvedPKRange.id());
        assertThat(sessionToken.getLSN()).isEqualTo(2);
    }

    @Test(groups = "unit")
    public void setSessionToken_NoSessionTokenForPartitionKeyRangeId() throws Exception {
        String collectionRid = "uf4PAK6T-Cw=";
        long collectionRidAsLong = ResourceId.parse(collectionRid).getUniqueDocumentCollectionId();
        String partitionKeyRangeId = "test_range_id";
        String sessionToken = "1#100#1=20#2=5#3=30";
        String collectionName = "dbs/db1/colls/collName_1";

        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");

        RxDocumentServiceRequest request1 = RxDocumentServiceRequest.create(OperationType.Create, ResourceType.Document,
                collectionName + "/docs", IOUtils.toInputStream("content1", "UTF-8"), new HashMap<>());

        Map<String, String> respHeaders = new HashMap<>();
        RxDocumentServiceResponse resp = Mockito.mock(RxDocumentServiceResponse.class);
        Mockito.doReturn(respHeaders).when(resp).getResponseHeaders();
        respHeaders.put(HttpConstants.HttpHeaders.SESSION_TOKEN, partitionKeyRangeId + ":" + sessionToken);
        respHeaders.put(HttpConstants.HttpHeaders.OWNER_FULL_NAME, collectionName);
        respHeaders.put(HttpConstants.HttpHeaders.OWNER_ID, collectionRid);
        sessionContainer.setSessionToken(request1, resp.getResponseHeaders());

        ConcurrentHashMap<String, Long> collectionNameToCollectionResourceId = (ConcurrentHashMap<String, Long>) FieldUtils.readField(sessionContainer, "collectionNameToCollectionResourceId", true);
        ConcurrentHashMap<Long, ConcurrentHashMap<String, ISessionToken>> collectionResourceIdToSessionTokens = (ConcurrentHashMap<Long, ConcurrentHashMap<String, ISessionToken>>) FieldUtils.readField(sessionContainer, "collectionResourceIdToSessionTokens", true);
        assertThat(collectionNameToCollectionResourceId).hasSize(1);
        assertThat(collectionResourceIdToSessionTokens).hasSize(1);
        assertThat(collectionNameToCollectionResourceId.get(collectionName)).isEqualTo(collectionRidAsLong);
        assertThat(collectionResourceIdToSessionTokens.get(collectionRidAsLong)).isNotNull();
        assertThat(collectionResourceIdToSessionTokens.get(collectionRidAsLong)).hasSize(1);
        assertThat(collectionResourceIdToSessionTokens.get(collectionRidAsLong).get(partitionKeyRangeId).convertToString()).isEqualTo(sessionToken);

        RxDocumentServiceRequest request2 = RxDocumentServiceRequest.create(OperationType.Read, ResourceType.Document,
                collectionName + "/docs", IOUtils.toInputStream("", "UTF-8"), new HashMap<>());

        ISessionToken resolvedSessionToken = sessionContainer.resolvePartitionLocalSessionToken(request2, partitionKeyRangeId);
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

        Map<String, String> respHeaders = new HashMap<>();

        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");

        RxDocumentServiceRequest request1 = RxDocumentServiceRequest.create(OperationType.Create, ResourceType.Document,
                collectionName + "/docs", IOUtils.toInputStream("content1", "UTF-8"), new HashMap<>());

        RxDocumentServiceResponse resp = Mockito.mock(RxDocumentServiceResponse.class);
        Mockito.doReturn(respHeaders).when(resp).getResponseHeaders();
        respHeaders.put(HttpConstants.HttpHeaders.SESSION_TOKEN, partitionKeyRangeId + ":" + initialSessionToken);
        respHeaders.put(HttpConstants.HttpHeaders.OWNER_FULL_NAME, collectionName);
        respHeaders.put(HttpConstants.HttpHeaders.OWNER_ID, collectionRid);
        sessionContainer.setSessionToken(request1, resp.getResponseHeaders());

        resp = Mockito.mock(RxDocumentServiceResponse.class);
        Mockito.doReturn(respHeaders).when(resp).getResponseHeaders();
        respHeaders.put(HttpConstants.HttpHeaders.SESSION_TOKEN, partitionKeyRangeId + ":" + newSessionTokenInServerResponse);
        respHeaders.put(HttpConstants.HttpHeaders.OWNER_FULL_NAME, collectionName);
        respHeaders.put(HttpConstants.HttpHeaders.OWNER_ID, collectionRid);
        sessionContainer.setSessionToken(request1, resp.getResponseHeaders());

        RxDocumentServiceRequest request2 = RxDocumentServiceRequest.create(OperationType.Read, ResourceType.Document,
                collectionName + "/docs", IOUtils.toInputStream("", "UTF-8"), new HashMap<>());

        ISessionToken resolvedSessionToken = sessionContainer.resolvePartitionLocalSessionToken(request2, partitionKeyRangeId);
        assertThat(resolvedSessionToken.convertToString()).isEqualTo(expectedMergedSessionToken);
    }


    @Test(groups = "unit")
    public void resolveGlobalSessionTokenReturnsEmptyStringOnEmptyCache() {
        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Read, ResourceType.Document,
                "dbs/db1/colls/collName/docs/doc1", new HashMap<>());
        assertThat(StringUtils.EMPTY).isEqualTo(sessionContainer.resolveGlobalSessionToken(request));
    }

    @Test(groups = "unit")
    public void resolveGlobalSessionTokenReturnsEmptyStringOnCacheMiss() {
        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");
        String partitionKeyRangeId = "range_0";
        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId()).getDocumentCollectionId().toString();
        String initialSessionToken = "1#100#1=20#2=5#3=30";
        sessionContainer.setSessionToken(documentCollectionId, "dbs/db1/colls1/collName",
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, partitionKeyRangeId + ":" + initialSessionToken));
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Read, ResourceType.Document,
                "dbs/db1/colls1/collName2/docs/doc1", new HashMap<>());
        assertThat(StringUtils.EMPTY).isEqualTo(sessionContainer.resolveGlobalSessionToken(request));
    }

    @Test(groups = "unit")
    public void resolveGlobalSessionTokenReturnsTokenMapUsingName() {
        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");
        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId()).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName";

        sessionContainer.setSessionToken(documentCollectionId, collectionFullName,
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#100#1=20#2=5#3=30"));
        sessionContainer.setSessionToken(documentCollectionId, collectionFullName,
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_1:1#101#1=20#2=5#3=30"));

        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(OperationType.Read,
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
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Read,
                documentCollectionId, ResourceType.Document, new HashMap<>());

        sessionContainer.setSessionToken(documentCollectionId, collectionFullName,
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#100#1=20#2=5#3=30"));
        sessionContainer.setSessionToken(documentCollectionId, collectionFullName,
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_1:1#101#1=20#2=5#3=30"));
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

        sessionContainer.setSessionToken(documentCollectionId, collectionFullName,
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#100#1=20#2=5#3=30"));
        sessionContainer.setSessionToken(documentCollectionId, collectionFullName,
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_1:1#101#1=20#2=5#3=30"));

        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(OperationType.Read,
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
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Read,
                documentCollectionId, ResourceType.Document, new HashMap<>());

        sessionContainer.setSessionToken(documentCollectionId, collectionFullName,
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#100#1=20#2=5#3=30"));
        sessionContainer.setSessionToken(documentCollectionId, collectionFullName,
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_1:1#101#1=20#2=5#3=30"));

        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken.getLSN()).isEqualTo(100);
        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_1");
        assertThat(sessionToken.getLSN()).isEqualTo(101);
    }

    @Test(groups = "unit")
    public void resolveLocalSessionTokenReturnsNullOnPartitionMiss() {
        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");
        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId()).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName";
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Read,
                documentCollectionId, ResourceType.Document, new HashMap<>());

        sessionContainer.setSessionToken(documentCollectionId, collectionFullName,
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#100#1=20#2=5#3=30"));
        sessionContainer.setSessionToken(documentCollectionId, collectionFullName,
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_1:1#101#1=20#2=5#3=30"));
        request.requestContext.resolvedPartitionKeyRange = new PartitionKeyRange();
        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_2");
        assertThat(sessionToken).isNull();
    }

    @Test(groups = "unit")
    public void resolveLocalSessionTokenReturnsNullOnCollectionMiss() {
        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");
        int randomCollectionId = getRandomCollectionId();
        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), randomCollectionId).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName";
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Read,
                ResourceId.newDocumentCollectionId(getRandomDbId(), randomCollectionId - 1).getDocumentCollectionId().toString(),
                ResourceType.Document, new HashMap<>());

        sessionContainer.setSessionToken(documentCollectionId, collectionFullName,
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#100#1=20#2=5#3=30"));
        sessionContainer.setSessionToken(documentCollectionId, collectionFullName,
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_1:1#101#1=20#2=5#3=30"));
        request.requestContext.resolvedPartitionKeyRange = new PartitionKeyRange();
        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_1");
        assertThat(sessionToken).isNull();
    }

    @Test(groups = "unit")
    public void resolvePartitionLocalSessionTokenReturnsTokenOnParentMatch() {
        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");
        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId()).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName";
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Read,
                documentCollectionId, ResourceType.Document, new HashMap<>());

        sessionContainer.setSessionToken(documentCollectionId, collectionFullName,
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#100#1=20#2=5#3=30"));
        sessionContainer.setSessionToken(documentCollectionId, collectionFullName,
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_1:1#101#1=20#2=5#3=30"));
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

        sessionContainer.setSessionToken(documentCollectionId, collectionFullName,
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#100#1=20#2=5#3=30"));

        //  Test resourceId based
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Read,
                documentCollectionId, ResourceType.Document, new HashMap<>());
        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken.getLSN()).isEqualTo(100);

        //  Test names based
        request = RxDocumentServiceRequest.createFromName(OperationType.Read,
                collectionFullName + "/docs/doc1", ResourceType.Document);
        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken.getLSN()).isEqualTo(100);

        sessionContainer.clearTokenByCollectionFullName(collectionFullName);

        //  Test resourceId based
        request = RxDocumentServiceRequest.create(OperationType.Read,
                documentCollectionId, ResourceType.Document, new HashMap<>());
        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken).isNull();

        //  Test names based
        request = RxDocumentServiceRequest.createFromName(OperationType.Read,
                collectionFullName + "/docs/doc1", ResourceType.Document);
        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken).isNull();
    }

    @Test(groups = "unit")
    public void clearTokenByResourceIdRemovesToken() {
        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");
        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId()).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName";

        sessionContainer.setSessionToken(documentCollectionId, collectionFullName,
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#100#1=20#2=5#3=30"));

        //  Test resourceId based
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Read,
                documentCollectionId, ResourceType.Document, new HashMap<>());
        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken.getLSN()).isEqualTo(100);

        //  Test names based
        request = RxDocumentServiceRequest.createFromName(OperationType.Read,
                collectionFullName + "/docs/doc1", ResourceType.Document);
        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken.getLSN()).isEqualTo(100);

        sessionContainer.clearTokenByResourceId(documentCollectionId);

        //  Test resourceId based
        request = RxDocumentServiceRequest.create(OperationType.Read,
                documentCollectionId, ResourceType.Document, new HashMap<>());
        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken).isNull();

        //  Test names based
        request = RxDocumentServiceRequest.createFromName(OperationType.Read,
                collectionFullName + "/docs/doc1", ResourceType.Document);
        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken).isNull();
    }

    @Test(groups = "unit")
    public void clearTokenKeepsUnmatchedCollection() {
        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");
        int randomCollectionId = getRandomCollectionId();
        String documentCollectionId1 = ResourceId.newDocumentCollectionId(getRandomDbId(), randomCollectionId).getDocumentCollectionId().toString();
        String collectionFullName1 = "dbs/db1/colls1/collName1";

        sessionContainer.setSessionToken(documentCollectionId1, collectionFullName1,
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#100#1=20#2=5#3=30"));

        //  Test resourceId based
        RxDocumentServiceRequest request1 = RxDocumentServiceRequest.create(OperationType.Read,
                documentCollectionId1, ResourceType.Document, new HashMap<>());
        String documentCollectionId2 = ResourceId.newDocumentCollectionId(getRandomDbId(), randomCollectionId - 1).getDocumentCollectionId().toString();
        String collectionFullName2 = "dbs/db1/colls1/collName2";

        //  Test resourceId based
        RxDocumentServiceRequest request2 = RxDocumentServiceRequest.create(OperationType.Read,
                documentCollectionId2, ResourceType.Document, new HashMap<>());

        sessionContainer.setSessionToken(documentCollectionId2, collectionFullName2,
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#100#1=20#2=5#3=30"));

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

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Read,
                collectionFullName + "/docs/doc1", ResourceType.Document, new HashMap<>());
        request.setResourceId(documentCollectionId);

        assertThat(request.getIsNameBased()).isFalse();
        sessionContainer.setSessionToken(request, ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#100#4=90#5=1"));
        request = RxDocumentServiceRequest.create(OperationType.Read, documentCollectionId, ResourceType.Document, new HashMap<>());
        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken.getLSN()).isEqualTo(100);

        request = RxDocumentServiceRequest.createFromName(OperationType.Read, collectionFullName + "/docs/doc1", ResourceType.Document);
        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken.getLSN()).isEqualTo(100);
    }

    @Test(groups = "unit")
    public void setSessionTokenGivesPriorityToOwnerFullNameOverResourceAddress() {
        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");
        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId()).getDocumentCollectionId().toString();
        String collectionFullName1 = "dbs/db1/colls1/collName1";
        String collectionFullName2 = "dbs/db1/colls1/collName2";

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Read,
                collectionFullName1 + "/docs/doc1", ResourceType.Document, new HashMap<>());
        request.setResourceId(documentCollectionId);
        sessionContainer.setSessionToken(request,
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#100#4=90#5=1",
                        HttpConstants.HttpHeaders.OWNER_FULL_NAME, collectionFullName2));

        request = RxDocumentServiceRequest.createFromName(OperationType.Read, collectionFullName1 + "/docs/doc1", ResourceType.Document);
        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken).isNull();

        request = RxDocumentServiceRequest.createFromName(OperationType.Read, collectionFullName2 + "/docs/doc1", ResourceType.Document);
        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
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

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Read,
                collectionFullName + "/docs/doc1", ResourceType.Document, new HashMap<>());
        request.setResourceId(documentCollectionId1);
        assertThat(request.getIsNameBased()).isFalse();

        sessionContainer.setSessionToken(request,
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#100#4=90#5=1",
                        HttpConstants.HttpHeaders.OWNER_ID, documentCollectionId2));

        request = RxDocumentServiceRequest.create(OperationType.Read,
                documentCollectionId1, ResourceType.Document, new HashMap<>());
        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken.getLSN()).isEqualTo(100);


        request = RxDocumentServiceRequest.create(OperationType.Read,
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

        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(OperationType.Read,
                collectionFullName + "/docs/doc1", ResourceType.Document);
        request.setResourceId(documentCollectionId1);
        assertThat(request.getIsNameBased()).isTrue();

        sessionContainer.setSessionToken(request,
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#100#4=90#5=1",
                        HttpConstants.HttpHeaders.OWNER_ID, documentCollectionId2));

        request = RxDocumentServiceRequest.create(OperationType.Read,
                documentCollectionId1, ResourceType.Document, new HashMap<>());
        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken).isNull();


        request = RxDocumentServiceRequest.create(OperationType.Read,
                documentCollectionId2, ResourceType.Document, new HashMap<>());
        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken.getLSN()).isEqualTo(100);
    }

    @Test(groups = "unit")
    public void setSessionTokenDoesntWorkForMasterQueries() {
        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");
        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId()).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName";

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.ReadFeed,
                collectionFullName + "/docs/doc1", ResourceType.DocumentCollection, new HashMap<>());
        request.setResourceId(documentCollectionId);
        sessionContainer.setSessionToken(request, ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1"));

        request = RxDocumentServiceRequest.create(OperationType.Read,
                documentCollectionId, ResourceType.Document, new HashMap<>());
        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken).isNull();

        request = RxDocumentServiceRequest.createFromName(OperationType.Read, collectionFullName + "/docs/doc1", ResourceType.Document);
        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken).isNull();
    }

    @Test(groups = "unit")
    public void setSessionTokenDoesntOverwriteHigherLSN() {
        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");
        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId()).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName";

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Read,
                collectionFullName + "/docs/doc1", ResourceType.Document, new HashMap<>());
        request.setResourceId(documentCollectionId);
        sessionContainer.setSessionToken(request, ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#105#4=90#5=1"));


        request = RxDocumentServiceRequest.create(OperationType.Read,
                collectionFullName + "/docs/doc1", ResourceType.Document, new HashMap<>());
        request.setResourceId(documentCollectionId);
        sessionContainer.setSessionToken(request, ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#100#4=90#5=1"));

        request = RxDocumentServiceRequest.create(OperationType.Read,
                documentCollectionId, ResourceType.Document, new HashMap<>());
        request.setResourceId(documentCollectionId);
        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken.getLSN()).isEqualTo(105);
    }

    @Test(groups = "unit")
    public void setSessionTokenOverwriteLowerLSN() {
        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");
        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId()).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName";

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Read,
                collectionFullName + "/docs/doc1", ResourceType.Document, new HashMap<>());
        request.setResourceId(documentCollectionId);
        sessionContainer.setSessionToken(request, ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#100#4=90#5=1"));


        request = RxDocumentServiceRequest.create(OperationType.Read,
                collectionFullName + "/docs/doc1", ResourceType.Document, new HashMap<>());
        request.setResourceId(documentCollectionId);
        sessionContainer.setSessionToken(request, ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#105#4=90#5=1"));

        request = RxDocumentServiceRequest.create(OperationType.Read,
                documentCollectionId, ResourceType.Document, new HashMap<>());
        request.setResourceId(documentCollectionId);
        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_0");
        assertThat(sessionToken.getLSN()).isEqualTo(105);
    }

    @Test(groups = "unit")
    public void setSessionTokenDoesNothingOnEmptySessionTokenHeader() {
        SessionContainer sessionContainer = new SessionContainer("127.0.0.1");
        String documentCollectionId = ResourceId.newDocumentCollectionId(getRandomDbId(), getRandomCollectionId()).getDocumentCollectionId().toString();
        String collectionFullName = "dbs/db1/colls1/collName";

        sessionContainer.setSessionToken(documentCollectionId, collectionFullName + "/docs/doc1",
                ImmutableMap.of(HttpConstants.HttpHeaders.SESSION_TOKEN, "range_0:1#100#4=90#5=1"));
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Read,
                documentCollectionId, ResourceType.Document, new HashMap<>());
        String sessionToken = sessionContainer.resolveGlobalSessionToken(request);
        Set<String> tokens = Sets.newSet(sessionToken.split(","));
        assertThat(tokens.size()).isEqualTo(1);
        assertThat(tokens.contains("range_0:1#100#4=90#5=1")).isTrue();

        sessionContainer.setSessionToken(documentCollectionId, collectionFullName, new HashMap<>());
        request = RxDocumentServiceRequest.create(OperationType.Read,
                documentCollectionId, ResourceType.Document, new HashMap<>());
        sessionToken = sessionContainer.resolveGlobalSessionToken(request);
        tokens = Sets.newSet(sessionToken.split(","));
        assertThat(tokens.size()).isEqualTo(1);
        assertThat(tokens.contains("range_0:1#100#4=90#5=1")).isTrue();
    }

    private static int getRandomCollectionId() {
        return random.nextInt(Integer.MAX_VALUE / 2) - (Integer.MAX_VALUE / 2);
    }

    private static int getRandomDbId() {
        return random.nextInt(Integer.MAX_VALUE / 2);
    }
}