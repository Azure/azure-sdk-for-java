// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ISessionContainer;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.ReadFeedKeyType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.SessionTokenHelper;
import com.azure.cosmos.implementation.ThinClientStoreModel;
import com.azure.cosmos.implementation.UserAgentContainer;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.caches.RxClientCollectionCache;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.directconnectivity.WFConstants;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.routing.CollectionRoutingMap;
import com.azure.cosmos.implementation.routing.HexConvert;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.PartitionKind;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdConstants.RntbdRequestHeader;
import static org.assertj.core.api.Assertions.assertThat;

public class ThinClientRangeHeadersTest {
    @DataProvider
    public Object[][] ranges() {
        return new Object[][] {
            {OperationType.Query, null, null, "05C1D0", "05C1E0"},
            {OperationType.Query, "05C1D8", "05C1DC", "05C1D0", "05C1E0"},
            {OperationType.Query, "", "05C1DC", "", "FF"},
            {OperationType.Query, "05C1D8", "FF", "", "FF"},
            {OperationType.Query, null, null, "", "FF"},
            {OperationType.ReadFeed, null, null, "05C1D0", "05C1E0"},
            {OperationType.ReadFeed, "05C1D8", "05C1DC", "05C1D0", "05C1E0"},
            {OperationType.ReadFeed, "", "05C1DC", "", "FF"},
            {OperationType.ReadFeed, "05C1D8", "FF", "", "FF"},
            {OperationType.ReadFeed, null, null, "", "FF"}
        };
    }

    @Test(groups = "unit", dataProvider = "ranges")
    public void preservesRangeAcrossRepeatedSerialization(
        OperationType operation, String requestedMin, String requestedMax, String physicalMin, String physicalMax)
        throws Exception {

        ThinClientStoreModel model = createModel();
        RxDocumentServiceRequest request = createRequest(operation, physicalMin, physicalMax);
        request.getHeaders().put(WFConstants.BackendHeaders.COLLECTION_RID, "stale-rid");
        if (requestedMin != null) {
            request.getHeaders().put(HttpConstants.HttpHeaders.READ_FEED_KEY_TYPE,
                ReadFeedKeyType.EffectivePartitionKeyRange.name());
            request.getHeaders().put(HttpConstants.HttpHeaders.START_EPK, requestedMin);
            request.getHeaders().put(HttpConstants.HttpHeaders.END_EPK, requestedMax);
        }

        for (int attempt = 0; attempt < 2; attempt++) {
            Map<Short, byte[]> tokens = serialize(model, request);
            assertThat(tokens.get(RntbdRequestHeader.StartEpkHash.id()))
                .containsExactly(HexConvert.hexToBytes(requestedMin == null ? physicalMin : requestedMin));
            assertThat(tokens.get(RntbdRequestHeader.EndEpkHash.id()))
                .containsExactly(HexConvert.hexToBytes(requestedMax == null ? physicalMax : requestedMax));
            assertThat(tokens.get(RntbdRequestHeader.CollectionRid.id()))
                .containsExactly("ccZ1ANCszwk=".getBytes(StandardCharsets.UTF_8));
            assertThat(tokens).doesNotContainKey(RntbdRequestHeader.EffectivePartitionKey.id());
            if (requestedMin == null) {
                assertThat(tokens).doesNotContainKeys(RntbdRequestHeader.ReadFeedKeyType.id(),
                    RntbdRequestHeader.StartEpk.id(), RntbdRequestHeader.EndEpk.id());
            } else {
                assertThat(tokens.get(RntbdRequestHeader.ReadFeedKeyType.id())).containsExactly((byte) 3);
                if (requestedMin.isEmpty()) {
                    assertThat(tokens).doesNotContainKey(RntbdRequestHeader.StartEpk.id());
                } else {
                    assertThat(tokens.get(RntbdRequestHeader.StartEpk.id()))
                        .containsExactly(requestedMin.getBytes(StandardCharsets.UTF_8));
                }
                assertThat(tokens.get(RntbdRequestHeader.EndEpk.id()))
                    .containsExactly(requestedMax.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    @Test(groups = "unit")
    public void narrowedRangeTakesPrecedenceOverHierarchicalPrefixPointRouting() throws Exception {
        RxDocumentServiceRequest request = createRequest(OperationType.Query, "", "FF");
        PartitionKeyDefinition definition = new PartitionKeyDefinition();
        definition.setKind(PartitionKind.MULTI_HASH);
        definition.setPaths(Arrays.asList("/tenantId", "/userId"));
        request.setPartitionKeyDefinition(definition);
        request.setPartitionKeyInternal(PartitionKeyInternal.fromObjectArray(Arrays.asList("tenant"), true));
        request.getHeaders().put(HttpConstants.HttpHeaders.READ_FEED_KEY_TYPE, "effectivepartitionkeyrange");
        request.getHeaders().put(HttpConstants.HttpHeaders.START_EPK, "05C1D8");
        request.getHeaders().put(HttpConstants.HttpHeaders.END_EPK, "05C1DC");

        Map<Short, byte[]> tokens = serialize(createModel(), request);
        assertThat(tokens).doesNotContainKey(RntbdRequestHeader.EffectivePartitionKey.id());
        assertThat(tokens.get(RntbdRequestHeader.StartEpkHash.id())).containsExactly(HexConvert.hexToBytes("05C1D8"));
        assertThat(tokens.get(RntbdRequestHeader.EndEpkHash.id())).containsExactly(HexConvert.hexToBytes("05C1DC"));
        assertThat(tokens.get(RntbdRequestHeader.StartEpk.id())).containsExactly("05C1D8".getBytes(StandardCharsets.UTF_8));
        assertThat(tokens.get(RntbdRequestHeader.EndEpk.id())).containsExactly("05C1DC".getBytes(StandardCharsets.UTF_8));
        assertThat(tokens.get(RntbdRequestHeader.ReadFeedKeyType.id())).containsExactly((byte) 3);
    }

    @Test(groups = "unit")
    public void queryPlanDoesNotAcquirePhysicalRoutingHashes() throws Exception {
        Map<Short, byte[]> tokens = serialize(createModel(), createRequest(OperationType.QueryPlan, "", "FF"));
        assertThat(tokens).doesNotContainKeys(RntbdRequestHeader.StartEpkHash.id(),
            RntbdRequestHeader.EndEpkHash.id(), RntbdRequestHeader.EffectivePartitionKey.id());
    }

    @DataProvider
    public Object[][] compoundSessionTokenTopologies() {
        return new Object[][] {
            {true, false},
            {false, true},
            {false, false}
        };
    }

    @Test(groups = "unit", dataProvider = "compoundSessionTokenTopologies")
    public void scopesCompoundSessionTokenWithAtMostOneRefresh(boolean cachedMatch, boolean refreshedMatch)
        throws Exception {
        HttpClient httpClient = Mockito.mock(HttpClient.class);
        Mockito.when(httpClient.send(Mockito.any(), Mockito.any())).thenReturn(Mono.empty());
        ThinClientStoreModel model = createModel(httpClient);
        RxDocumentServiceRequest request = createRequest(OperationType.Read, "", "FF");
        String originalToken = "0:1#10#1=10,1:1#20#1=20";
        request.getHeaders().put(HttpConstants.HttpHeaders.SESSION_TOKEN, originalToken);
        request.setPartitionKeyRangeIdentity(new PartitionKeyRangeIdentity("0"));
        request.setPartitionKeyInternal(PartitionKeyInternal.fromObjectArray(Collections.singletonList("testPk"), true));
        request.requestContext.resolvedPartitionKeyRange = new PartitionKeyRange("0", "", "FF");
        request.requestContext.sessionToken = SessionTokenHelper.parse("1#99#1=99");

        PartitionKeyDefinition definition = new PartitionKeyDefinition();
        definition.setPaths(Collections.singletonList("/partitionKey"));
        DocumentCollection collection = new DocumentCollection();
        collection.setResourceId("ccZ1ANCszwk=");
        collection.setPartitionKey(definition);
        RxClientCollectionCache collectionCache = Mockito.mock(RxClientCollectionCache.class);
        Mockito.when(collectionCache.resolveCollectionAsync(Mockito.any(), Mockito.eq(request)))
            .thenReturn(Mono.just(new Utils.ValueHolder<>(collection)));
        model.setCollectionCache(collectionCache);

        CollectionRoutingMap cached = Mockito.mock(CollectionRoutingMap.class);
        CollectionRoutingMap refreshed = Mockito.mock(CollectionRoutingMap.class);
        PartitionKeyRange target = new PartitionKeyRange("0", "", "80");
        PartitionKeyRange other = new PartitionKeyRange("1", "80", "FF");
        if (cachedMatch) {
            Mockito.when(cached.getRangeByPartitionKeyRangeId("0")).thenReturn(target);
            Mockito.when(cached.tryGetRangeByPartitionKeyRangeId("0")).thenReturn(target);
            Mockito.when(cached.tryGetRangeByPartitionKeyRangeId("1")).thenReturn(other);
        }
        if (refreshedMatch) {
            Mockito.when(refreshed.getRangeByPartitionKeyRangeId("0")).thenReturn(target);
            Mockito.when(refreshed.tryGetRangeByPartitionKeyRangeId("0")).thenReturn(target);
            Mockito.when(refreshed.tryGetRangeByPartitionKeyRangeId("1")).thenReturn(other);
        }
        RxPartitionKeyRangeCache rangeCache = Mockito.mock(RxPartitionKeyRangeCache.class);
        Mockito.when(rangeCache.tryLookupAsync(
            Mockito.any(), Mockito.eq(collection.getResourceId()), Mockito.isNull(), Mockito.any()))
            .thenReturn(Mono.just(new Utils.ValueHolder<>(cached)));
        Mockito.when(rangeCache.tryLookupAsync(
            Mockito.any(), Mockito.eq(collection.getResourceId()), Mockito.eq(cached), Mockito.any()))
            .thenReturn(Mono.just(new Utils.ValueHolder<>(refreshed)));
        model.setPartitionKeyRangeCache(rangeCache);

        model.performRequestInternal(request, new URI("https://localhost:10250")).block();

        Mockito.verify(httpClient).send(Mockito.any(), Mockito.any());
        Mockito.verify(rangeCache, Mockito.times(cachedMatch ? 0 : 1)).tryLookupAsync(
            Mockito.any(), Mockito.eq(collection.getResourceId()), Mockito.eq(cached), Mockito.any());
        Mockito.verify(rangeCache).tryLookupAsync(
            Mockito.any(), Mockito.eq(collection.getResourceId()), Mockito.isNull(), Mockito.any());
        Mockito.verifyNoMoreInteractions(rangeCache);
        assertThat(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isEqualTo(originalToken);
        if (cachedMatch || refreshedMatch) {
            assertThat(request.requestContext.resolvedPartitionKeyRange).isSameAs(target);
            assertThat(request.requestContext.sessionToken).isEqualTo(SessionTokenHelper.parse("1#10#1=10"));
            assertThat(request.requestContext.getSessionTokenEvaluationResults()).isEmpty();
            assertThat(serialize(model, request).get(RntbdRequestHeader.SessionToken.id()))
                .containsExactly("0:1#10#1=10".getBytes(StandardCharsets.UTF_8));
        } else {
            assertThat(request.requestContext.resolvedPartitionKeyRange).isNull();
            assertThat(request.requestContext.sessionToken).isNull();
            assertThat(request.requestContext.getSessionTokenEvaluationResults()).hasSize(1);
            assertThat(serialize(model, request)).doesNotContainKey(RntbdRequestHeader.SessionToken.id());
        }
    }

    private static ThinClientStoreModel createModel() {
        return createModel(Mockito.mock(HttpClient.class));
    }

    private static ThinClientStoreModel createModel(HttpClient httpClient) {
        DiagnosticsClientContext context = Mockito.mock(DiagnosticsClientContext.class);
        Mockito.when(context.getConfig()).thenReturn(new DiagnosticsClientContext.DiagnosticsClientConfig());
        DatabaseAccount account = Mockito.mock(DatabaseAccount.class);
        Mockito.when(account.getId()).thenReturn("test-account");
        GlobalEndpointManager endpoints = Mockito.mock(GlobalEndpointManager.class);
        Mockito.when(endpoints.getLatestDatabaseAccount()).thenReturn(account);
        return new ThinClientStoreModel(context, Mockito.mock(ISessionContainer.class), ConsistencyLevel.SESSION,
            new UserAgentContainer(), endpoints, httpClient, null);
    }

    private static RxDocumentServiceRequest createRequest(OperationType operation, String min, String max) {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(
            Mockito.mock(DiagnosticsClientContext.class), operation, "/dbs/db/colls/coll/docs", ResourceType.Document);
        request.setResourceId("ccZ1ANCszwk=");
        PartitionKeyRange physicalRange = new PartitionKeyRange();
        physicalRange.setMinInclusive(min);
        physicalRange.setMaxExclusive(max);
        request.requestContext.resolvedPartitionKeyRange = physicalRange;
        return request;
    }

    private static Map<Short, byte[]> serialize(ThinClientStoreModel model, RxDocumentServiceRequest request)
        throws Exception {
        byte[] body = model.wrapInHttpRequest(request, new URI("https://localhost:10250"))
            .body().single().block();
        ByteBuf buffer = Unpooled.wrappedBuffer(body);
        try {
            int headerLength = buffer.readIntLE();
            buffer.skipBytes(20); // Resource type, operation type, and activity ID.
            Map<Short, byte[]> tokens = new HashMap<>();
            while (buffer.readerIndex() < headerLength) {
                short id = buffer.readShortLE();
                int type = buffer.readUnsignedByte();
                int length;
                switch (type) {
                    case 0x00: length = 1; break;
                    case 0x01: length = 2; break;
                    case 0x02:
                    case 0x03:
                    case 0x0D: length = 4; break;
                    case 0x04:
                    case 0x05:
                    case 0x0E: length = 8; break;
                    case 0x06: length = 16; break;
                    case 0x07:
                    case 0x0A: length = buffer.readUnsignedByte(); break;
                    case 0x08:
                    case 0x0B: length = buffer.readUnsignedShortLE(); break;
                    case 0x09:
                    case 0x0C: length = buffer.readIntLE(); break;
                    default: throw new AssertionError("Unexpected RNTBD token type: " + type);
                }
                byte[] value = new byte[length];
                buffer.readBytes(value);
                assertThat(tokens.put(id, value)).as("Duplicate wire token 0x%04X", id).isNull();
            }
            assertThat(buffer.readerIndex()).isEqualTo(headerLength);
            return tokens;
        } finally {
            buffer.release();
        }
    }
}
