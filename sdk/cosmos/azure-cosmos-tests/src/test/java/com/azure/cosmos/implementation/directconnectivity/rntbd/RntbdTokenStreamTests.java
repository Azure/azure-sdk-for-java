// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.SessionTokenHelper;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class RntbdTokenStreamTests {
    private static final Random rnd = new Random();
    @Test(groups = { "unit" })
    public void noReorderingInDirectMode() {
        List<Short> headerIds = createAndEncodeRequestHeaders(false);
        assertThat(headerIds).isNotNull();
        assertThat(headerIds.size()).isEqualTo(6);
        assertThat(headerIds.get(0)).isEqualTo(RntbdConstants.RntbdRequestHeader.PayloadPresent.id());
        assertThat(headerIds.get(1)).isEqualTo(RntbdConstants.RntbdRequestHeader.ReplicaPath.id());
        assertThat(headerIds.get(2)).isEqualTo(RntbdConstants.RntbdRequestHeader.TransportRequestID.id());
        assertThat(headerIds.get(3)).isEqualTo(RntbdConstants.RntbdRequestHeader.EffectivePartitionKey.id());
        assertThat(headerIds.get(4)).isEqualTo(RntbdConstants.RntbdRequestHeader.CorrelatedActivityId.id());
        assertThat(headerIds.get(5)).isEqualTo(RntbdConstants.RntbdRequestHeader.GlobalDatabaseAccountName.id());
    }

    @Test(groups = { "unit" })
    public void withReorderingForThinClient() {
        List<Short> headerIds = createAndEncodeRequestHeaders(true);
        assertThat(headerIds).isNotNull();
        assertThat(headerIds.size()).isEqualTo(4);
        assertThat(headerIds.get(0)).isEqualTo(RntbdConstants.RntbdRequestHeader.EffectivePartitionKey.id());
        assertThat(headerIds.get(1)).isEqualTo(RntbdConstants.RntbdRequestHeader.GlobalDatabaseAccountName.id());
        assertThat(headerIds.get(2)).isEqualTo(RntbdConstants.RntbdRequestHeader.PayloadPresent.id());
        assertThat(headerIds.get(3)).isEqualTo(RntbdConstants.RntbdRequestHeader.CorrelatedActivityId.id());
    }

    @DataProvider
    public Object[][] sessionTokenOverrides() {
        return new Object[][] {{"0:1#10#1=10"}, {null}, {""}};
    }

    @Test(groups = "unit", dataProvider = "sessionTokenOverrides")
    public void sessionTokenOverrideMatchesCloneBasedWireOutput(String override) {
        RxDocumentServiceRequest request = createSessionTokenRequest("0:1#10#1=10,1:1#20#1=20");
        Map<String, String> originalHeaders = new HashMap<>(request.getHeaders());
        RntbdRequestArgs args = new RntbdRequestArgs(request);
        RxDocumentServiceRequest wireRequest = request.clone();
        SessionTokenHelper.setOriginalSessionToken(wireRequest, override);
        RntbdRequestArgs wireArgs = Mockito.spy(args);
        Mockito.doReturn(wireRequest).when(wireArgs).serviceRequest();

        for (boolean forThinClient : new boolean[] {false, true}) {
            assertThat(encode(RntbdRequest.from(args, override), forThinClient))
                .containsExactly(encode(RntbdRequest.from(wireArgs), forThinClient));
        }
        assertThat(request.getHeaders()).isEqualTo(originalHeaders);
    }

    @DataProvider
    public Object[][] originalSessionTokens() {
        return new Object[][] {{null}, {""}, {"0:1#10#1=10"}, {"0:1#10#1=10,1:1#20#1=20"}};
    }

    @Test(groups = "unit", dataProvider = "originalSessionTokens")
    public void defaultFactoryUsesOriginalHeaderNotScopedProgress(String originalToken) {
        RxDocumentServiceRequest request = createSessionTokenRequest(originalToken);
        request.requestContext.sessionToken = SessionTokenHelper.parse("1#99#1=99");
        RntbdRequestArgs args = new RntbdRequestArgs(request);

        for (boolean forThinClient : new boolean[] {false, true}) {
            ByteBuf buffer = Unpooled.wrappedBuffer(encode(RntbdRequest.from(args), forThinClient));
            try {
                int headerLength = buffer.readIntLE();
                RntbdRequestFrame.decode(buffer);
                RntbdRequestHeaders decoded = RntbdRequestHeaders.decode(
                    buffer.readSlice(headerLength - buffer.readerIndex()));
                RntbdToken token = decoded.get(RntbdConstants.RntbdRequestHeader.SessionToken);
                assertThat(token.isPresent()).isEqualTo(originalToken != null && !originalToken.isEmpty());
                if (token.isPresent()) {
                    assertThat(token.getValue()).isEqualTo(originalToken);
                }
            } finally {
                buffer.release();
            }
        }
    }

    @Test(groups = "unit")
    public void concurrentSessionTokenOverridesRemainLocalToWireRequests() throws Exception {
        RxDocumentServiceRequest request = createSessionTokenRequest("0:1#10#1=10,1:1#20#1=20");
        Map<String, String> originalHeaders = new HashMap<>(request.getHeaders());
        RntbdRequestArgs args = new RntbdRequestArgs(request);
        byte[] scoped = encode(RntbdRequest.from(args, "0:1#10#1=10"), true);
        byte[] omitted = encode(RntbdRequest.from(args, null), true);
        CyclicBarrier start = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<byte[]> scopedAttempt = executor.submit(() -> {
                start.await(10, TimeUnit.SECONDS);
                return encode(RntbdRequest.from(args, "0:1#10#1=10"), true);
            });
            Future<byte[]> unscopedAttempt = executor.submit(() -> {
                start.await(10, TimeUnit.SECONDS);
                return encode(RntbdRequest.from(args, null), true);
            });
            assertThat(scopedAttempt.get(10, TimeUnit.SECONDS)).containsExactly(scoped);
            assertThat(unscopedAttempt.get(10, TimeUnit.SECONDS)).containsExactly(omitted);
            assertThat(request.getHeaders()).isEqualTo(originalHeaders);
        } finally {
            executor.shutdownNow();
        }
    }

    private static RxDocumentServiceRequest createSessionTokenRequest(String originalToken) {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            null, OperationType.Query, ResourceType.Document);
        request.setContentBytes("{\"query\":\"SELECT * FROM c\"}".getBytes(StandardCharsets.UTF_8));
        request.getHeaders().put(HttpConstants.HttpHeaders.CONTINUATION, "continuation");
        request.getHeaders().put(HttpConstants.HttpHeaders.GLOBAL_DATABASE_ACCOUNT_NAME, "test-account");
        SessionTokenHelper.setOriginalSessionToken(request, originalToken);
        return request;
    }

    private static byte[] encode(RntbdRequest request, boolean forThinClient) {
        ByteBuf buffer = Unpooled.buffer();
        try {
            request.encode(buffer, forThinClient);
            return ByteBufUtil.getBytes(buffer);
        } finally {
            buffer.release();
        }
    }

    private static List<Short> createAndEncodeRequestHeaders(boolean forThinClient) {
        UUID activityId = UUID.randomUUID();
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            null,
            OperationType.Create,
            ResourceType.Document);

        String correlatedActivityId = UUID.randomUUID().toString();
        request.getHeaders().put(
            HttpConstants.HttpHeaders.CORRELATED_ACTIVITY_ID,
            correlatedActivityId
        );

        request.getHeaders().put(
            HttpConstants.HttpHeaders.GLOBAL_DATABASE_ACCOUNT_NAME,
            "SomeAccount"
        );

        RntbdRequestFrame frame = new RntbdRequestFrame(
            activityId,
            RntbdConstants.RntbdOperationType.Create,
            RntbdConstants.RntbdResourceType.Document);

        RntbdRequestArgs args = new RntbdRequestArgs(request, new Uri("prefix://someUri"));

        byte[] hashValue = new byte[16];
        rnd.nextBytes(hashValue);
        RntbdTokenStream<RntbdConstants.RntbdRequestHeader> input = new RntbdRequestHeaders(args, frame);
        input
            .get(RntbdConstants.RntbdRequestHeader.EffectivePartitionKey)
            .setValue(hashValue);

        ByteBuf out = Unpooled.buffer(1024);
        input.encode(out, forThinClient);

        out.readerIndex(0);

        return getHeaderIdsInOrder(out);
    }

    private static List<Short> getHeaderIdsInOrder(ByteBuf in) {
        List<Short> headerIds = new ArrayList<>();
        while (in.readableBytes() > 0) {

            final short id = in.readShortLE();
            headerIds.add(id);
            final RntbdTokenType type = RntbdTokenType.fromId(in.readByte());

            RntbdToken token = RntbdToken.create(RntbdConstants.RntbdRequestHeader.map.get(id));
            token.decode(in);
        }

        return headerIds;
    }
}
