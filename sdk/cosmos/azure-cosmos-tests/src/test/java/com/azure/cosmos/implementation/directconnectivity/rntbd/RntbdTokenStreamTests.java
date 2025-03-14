// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

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
