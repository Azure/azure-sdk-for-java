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

package com.microsoft.azure.cosmosdb.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.microsoft.azure.cosmosdb.GatewayTestUtils;
import com.microsoft.azure.cosmosdb.PartitionKeyRange;
import com.microsoft.azure.cosmosdb.rx.internal.DocumentServiceRequestContext;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceRequest;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceResponse;
import org.apache.commons.io.IOUtils;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static com.microsoft.azure.cosmosdb.rx.internal.Utils.ValueHolder;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link SessionContainer}
 */
public class SessionContainerTest {

    @Test(groups = "unit")
    public void sessionContainer() throws Exception {
        this.sessionContainer(n -> {
            ValueHolder<ISessionToken> sessionToken = ValueHolder.initialize(null);

            assertThat(VectorSessionToken.tryCreate("1#100#4=90#5=" + n, sessionToken)).isTrue();
            return sessionToken.v;
        });
    }

    private void sessionContainer(Function<Integer, ISessionToken> getSessionToken) throws Exception {
        ConcurrentHashMap<Long, ConcurrentHashMap<String, ISessionToken>> ridToSessionToken = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, Long> nameToRid = new ConcurrentHashMap<>();

        int numCollections = 2;
        int numPartitionKeyRangeIds = 5;

        for (int i = 0; i < numCollections; i++) {
            String collName = "dbs/db1/colls/collName_" + i;
            long collId = (long) i;
            nameToRid.put(collName, collId);

            ConcurrentHashMap<String, ISessionToken> idToTokenMap = new ConcurrentHashMap<>();

            for (int j = 0; j < numPartitionKeyRangeIds; j++) {
                String range = "range_" + j;
                ISessionToken token = getSessionToken.apply(j);

                boolean successFlag = (null == idToTokenMap.putIfAbsent(range, token));

                if (!successFlag) {
                    throw new RuntimeException("Add should not fail!");
                }
            }

            boolean successFlag2 = (null == ridToSessionToken.putIfAbsent(collId, idToTokenMap));

            if (!successFlag2) {
                throw new RuntimeException("Add should not fail!");
            }
        }

        SessionContainer sessionContainer = new SessionContainer("127.0.0.1", nameToRid, ridToSessionToken);

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.ReadFeed, ResourceType.DocumentCollection,
            "dbs/db1/colls/collName_1", IOUtils.toInputStream("content1", "UTF-8"), new HashMap<>());

        ISessionToken sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, "range_1");
        assertThat(sessionToken).isEqualTo(getSessionToken.apply(1));

        DocumentServiceRequestContext dsrContext = new DocumentServiceRequestContext();
        PartitionKeyRange resolvedPKRange = new PartitionKeyRange();
        resolvedPKRange.setId("range_" + (numPartitionKeyRangeIds + 10));
        GatewayTestUtils.setParent(resolvedPKRange, ImmutableList.of("range_2", "range_x"));
        dsrContext.resolvedPartitionKeyRange = resolvedPKRange;
        request.requestContext = dsrContext;

        sessionToken = sessionContainer.resolvePartitionLocalSessionToken(request, resolvedPKRange.getId());
        assertThat(sessionToken).isEqualTo(getSessionToken.apply(2));
    }

    @Test(groups = "unit")
    public void setSessionToken_NoSessionTokenForPartitionKeyRangeId() throws Exception {
        String collectionRid = "uf4PAK6T-Cw=";
        long collectionRidAsLong = ResourceId.parse(collectionRid).getUniqueDocumentCollectionId();
        String partitionKeyRangeId = "test_range_id";
        String sessionToken = "1#100#1=20#2=5#3=30";
        String collectionName = "dbs/db1/colls/collName_1";

        ConcurrentHashMap<Long, ConcurrentHashMap<String, ISessionToken>> ridToSessionToken = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, Long> nameToRid = new ConcurrentHashMap<>();

        SessionContainer sessionContainer = new SessionContainer("127.0.0.1", nameToRid, ridToSessionToken);

        RxDocumentServiceRequest request1 = RxDocumentServiceRequest.create(OperationType.Create, ResourceType.Document,
            collectionName + "/docs", IOUtils.toInputStream("content1", "UTF-8"), new HashMap<>());

        Map<String, String> respHeaders = new HashMap<>();
        RxDocumentServiceResponse resp = Mockito.mock(RxDocumentServiceResponse.class);
        Mockito.doReturn(respHeaders).when(resp).getResponseHeaders();
        respHeaders.put(HttpConstants.HttpHeaders.SESSION_TOKEN, partitionKeyRangeId + ":" + sessionToken);
        respHeaders.put(HttpConstants.HttpHeaders.OWNER_FULL_NAME, collectionName);
        respHeaders.put(HttpConstants.HttpHeaders.OWNER_ID, collectionRid);
        sessionContainer.setSessionToken(request1, resp);

        assertThat(nameToRid).hasSize(1);
        assertThat(ridToSessionToken).hasSize(1);
        assertThat(nameToRid.get(collectionName)).isEqualTo(collectionRidAsLong);
        assertThat(ridToSessionToken.get(collectionRidAsLong)).isNotNull();
        assertThat(ridToSessionToken.get(collectionRidAsLong)).hasSize(1);
        assertThat(ridToSessionToken.get(collectionRidAsLong).get(partitionKeyRangeId).convertToString()).isEqualTo(sessionToken);

        RxDocumentServiceRequest request2 = RxDocumentServiceRequest.create(OperationType.Read, ResourceType.Document,
            collectionName + "/docs", IOUtils.toInputStream("", "UTF-8"), new HashMap<>());

        ISessionToken resolvedSessionToken = sessionContainer.resolvePartitionLocalSessionToken(request2, partitionKeyRangeId);
        assertThat(resolvedSessionToken.convertToString()).isEqualTo(sessionToken);
    }

    @Test(groups = "unit")
    public void setSessionToken_MergeOldWithNew() throws Exception {
        String collectionRid = "uf4PAK6T-Cw=";
        long collectionRidAsLong = ResourceId.parse("uf4PAK6T-Cw=").getUniqueDocumentCollectionId();
        String collectionName = "dbs/db1/colls/collName_1";
        String initialSessionToken = "1#100#1=20#2=5#3=30";
        String newSessionTokenInServerResponse = "1#100#1=31#2=5#3=21";
        String partitionKeyRangeId = "test_range_id";
        String expectedMergedSessionToken = "1#100#1=31#2=5#3=30";

        ConcurrentHashMap<Long, ConcurrentHashMap<String, ISessionToken>> ridToSessionToken = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, Long> nameToRid = new ConcurrentHashMap<>();
        nameToRid.put(collectionName, collectionRidAsLong);
        ridToSessionToken.put(collectionRidAsLong, new ConcurrentHashMap(
            ImmutableMap.of(partitionKeyRangeId, parseSessionToken(initialSessionToken))));

        SessionContainer sessionContainer = new SessionContainer("127.0.0.1", nameToRid, ridToSessionToken);

        RxDocumentServiceRequest request1 = RxDocumentServiceRequest.create(OperationType.Create, ResourceType.Document,
            collectionName + "/docs", IOUtils.toInputStream("content1", "UTF-8"), new HashMap<>());

        Map<String, String> respHeaders = new HashMap<>();
        RxDocumentServiceResponse resp = Mockito.mock(RxDocumentServiceResponse.class);
        Mockito.doReturn(respHeaders).when(resp).getResponseHeaders();
        respHeaders.put(HttpConstants.HttpHeaders.SESSION_TOKEN, partitionKeyRangeId + ":" + newSessionTokenInServerResponse);
        respHeaders.put(HttpConstants.HttpHeaders.OWNER_FULL_NAME, collectionName);
        respHeaders.put(HttpConstants.HttpHeaders.OWNER_ID, collectionRid);
        sessionContainer.setSessionToken(request1, resp);

        RxDocumentServiceRequest request2 = RxDocumentServiceRequest.create(OperationType.Read, ResourceType.Document,
            collectionName + "/docs", IOUtils.toInputStream("", "UTF-8"), new HashMap<>());

        ISessionToken resolvedSessionToken = sessionContainer.resolvePartitionLocalSessionToken(request2, partitionKeyRangeId);
        assertThat(resolvedSessionToken.convertToString()).isEqualTo(expectedMergedSessionToken);
    }

    private ISessionToken parseSessionToken(String token) {
        ValueHolder<ISessionToken> valueHolder = ValueHolder.initialize(null);
        VectorSessionToken.tryCreate(token, valueHolder);
        return valueHolder.v;
    }
}



