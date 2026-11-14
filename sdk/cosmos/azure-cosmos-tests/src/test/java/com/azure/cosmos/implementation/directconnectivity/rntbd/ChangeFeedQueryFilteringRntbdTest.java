// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ReadFeedKeyType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.TestUtils;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedMode;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStartFromInternal;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStateV1;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdConstants.RntbdRequestHeader;
import static org.assertj.core.api.Assertions.assertThat;

public class ChangeFeedQueryFilteringRntbdTest {
    @DataProvider(name = "modesAndStarts")
    public Object[][] modesAndStarts() {
        return new Object[][] {
            { ChangeFeedMode.INCREMENTAL, ChangeFeedStartFromInternal.createFromBeginning() },
            { ChangeFeedMode.INCREMENTAL, ChangeFeedStartFromInternal.createFromNow() },
            { ChangeFeedMode.INCREMENTAL, ChangeFeedStartFromInternal.createFromPointInTime(Instant.parse("2025-01-01T00:00:00Z")) },
            { ChangeFeedMode.INCREMENTAL, ChangeFeedStartFromInternal.createFromETagAndFeedRange("\"42\"", FeedRangeEpkImpl.forFullRange()) },
            { ChangeFeedMode.FULL_FIDELITY, ChangeFeedStartFromInternal.createFromNow() },
            { ChangeFeedMode.FULL_FIDELITY, ChangeFeedStartFromInternal.createFromPointInTime(Instant.parse("2025-01-01T00:00:00Z")) },
            { ChangeFeedMode.FULL_FIDELITY, ChangeFeedStartFromInternal.createFromETagAndFeedRange("\"42\"", FeedRangeEpkImpl.forFullRange()) }
        };
    }

    @Test(groups = "unit", dataProvider = "modesAndStarts")
    public void filteredChangeFeedEncodesNativeReadFeedWithBodyAndUnchangedLogicalRequest(
        ChangeFeedMode mode, ChangeFeedStartFromInternal start) {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(TestUtils.mockDiagnosticsClientContext(),
            OperationType.Query, "dbs/db/colls/coll", ResourceType.Document);
        byte[] body = "{\"query\":\"SELECT * FROM c WHERE c.state = \\\"WA\\\"\"}".getBytes(StandardCharsets.UTF_8);
        request.setContentBytes(body);
        request.getHeaders().put(HttpConstants.HttpHeaders.IS_QUERY, "true");
        request.getHeaders().put(HttpConstants.HttpHeaders.CONTENT_TYPE, "application/query+json");
        request.getHeaders().put(HttpConstants.HttpHeaders.IF_MATCH, "\"ordinary-query-match\"");
        new ChangeFeedStateV1("rid", FeedRangeEpkImpl.forFullRange(), mode, start, null).populateRequest(request, 25);
        assertThat(request.getHeaders()).containsEntry(HttpConstants.HttpHeaders.A_IM,
            mode == ChangeFeedMode.INCREMENTAL
                ? HttpConstants.A_IMHeaderValues.INCREMENTAL_FEED : HttpConstants.A_IMHeaderValues.FULL_FIDELITY_FEED);
        assertThat(request.useGatewayMode).isEqualTo(mode == ChangeFeedMode.FULL_FIDELITY);

        for (String routing : new String[] { "partitionKey", "epk", "rangeId" }) {
            request.getHeaders().remove(HttpConstants.HttpHeaders.PARTITION_KEY);
            request.getHeaders().remove(HttpConstants.HttpHeaders.READ_FEED_KEY_TYPE);
            request.getHeaders().remove(HttpConstants.HttpHeaders.START_EPK);
            request.getHeaders().remove(HttpConstants.HttpHeaders.END_EPK);
            request.getHeaders().put(HttpConstants.HttpHeaders.PARTITION_KEY_RANGE_ID, "7");
            if ("partitionKey".equals(routing)) {
                request.getHeaders().put(HttpConstants.HttpHeaders.PARTITION_KEY, "[\"tenant\"]");
            } else if ("epk".equals(routing)) {
                request.getHeaders().put(HttpConstants.HttpHeaders.READ_FEED_KEY_TYPE,
                    ReadFeedKeyType.EffectivePartitionKeyRange.name());
                request.getHeaders().put(HttpConstants.HttpHeaders.START_EPK, "AA");
                request.getHeaders().put(HttpConstants.HttpHeaders.END_EPK, "BB");
            }
            Map<String, String> originalHeaders = new HashMap<>(request.getHeaders());
            // Re-encoding the same logical request also covers reuse for retries or gateway routing.
            for (int attempt = 0; attempt < 2; attempt++) {
                assertEncodedRequest(request, RntbdConstants.RntbdOperationType.ReadFeed,
                    request.getHeaders().get(HttpConstants.HttpHeaders.IF_NONE_MATCH));
                assertThat(request.getOperationType()).isEqualTo(OperationType.Query);
                assertThat(request.getHeaders()).isEqualTo(originalHeaders);
                assertThat(request.getContentAsByteArray()).isEqualTo(body);
                assertThat(request.useGatewayMode).isEqualTo(mode == ChangeFeedMode.FULL_FIDELITY);
            }
        }
    }

    @Test(groups = "unit")
    public void ordinaryQueriesAndUnfilteredChangeFeedKeepTheirWireOperations() {
        for (String aim : new String[] { null, "" }) {
            RxDocumentServiceRequest query = RxDocumentServiceRequest.createFromName(TestUtils.mockDiagnosticsClientContext(),
                OperationType.Query, "dbs/db/colls/coll", ResourceType.Document);
            query.setContentBytes("{\"query\":\"SELECT * FROM c\"}".getBytes(StandardCharsets.UTF_8));
            query.getHeaders().put(HttpConstants.HttpHeaders.IF_MATCH, "\"ordinary-query-match\"");
            if (aim != null) {
                query.getHeaders().put(HttpConstants.HttpHeaders.A_IM, aim);
            }
            assertEncodedRequest(query, RntbdConstants.RntbdOperationType.Query,
                aim == null ? "\"ordinary-query-match\"" : null);
            assertThat(query.getOperationType()).isEqualTo(OperationType.Query);
        }
        RxDocumentServiceRequest unfiltered = RxDocumentServiceRequest.createFromName(TestUtils.mockDiagnosticsClientContext(),
            OperationType.ReadFeed, "dbs/db/colls/coll", ResourceType.Document);
        unfiltered.getHeaders().put(HttpConstants.HttpHeaders.A_IM, HttpConstants.A_IMHeaderValues.INCREMENTAL_FEED);
        unfiltered.getHeaders().put(HttpConstants.HttpHeaders.IF_NONE_MATCH, "\"42\"");
        assertEncodedRequest(unfiltered, RntbdConstants.RntbdOperationType.ReadFeed, "\"42\"");
        assertThat(unfiltered.getContentAsByteArray()).isNull();
        assertThat(unfiltered.getOperationType()).isEqualTo(OperationType.ReadFeed);
    }

    private void assertEncodedRequest(RxDocumentServiceRequest request, RntbdConstants.RntbdOperationType operation,
        String match) {
        EmbeddedChannel channel = new EmbeddedChannel(new RntbdRequestEncoder());
        ByteBuf encoded = null;
        try (RntbdRequestTimer timer = new RntbdRequestTimer(Duration.ofSeconds(30).toNanos(),
            Duration.ofMillis(100).toNanos())) {
            RntbdRequestArgs args = new RntbdRequestArgs(request, new Uri("https://localhost/replica-path"));
            assertThat(channel.writeOutbound(new AsyncRntbdRequestRecord(args, timer))).isTrue();
            encoded = channel.readOutbound();
            int headerLength = encoded.readIntLE();
            RntbdRequestFrame frame = RntbdRequestFrame.decode(encoded);
            assertThat(frame.getResourceType()).isEqualTo(RntbdConstants.RntbdResourceType.Document);
            assertThat(frame.getOperationType()).isEqualTo(operation);
            RntbdRequestHeaders headers = RntbdRequestHeaders.decode(
                encoded.readSlice(headerLength - RntbdRequestFrame.LENGTH));
            String aim = request.getHeaders().get(HttpConstants.HttpHeaders.A_IM);
            assertStringToken(headers, RntbdRequestHeader.A_IM, aim);
            assertStringToken(headers, RntbdRequestHeader.Match, match);
            assertStringToken(headers, RntbdRequestHeader.IfModifiedSince,
                request.getHeaders().get(HttpConstants.HttpHeaders.IF_MODIFIED_SINCE));
            assertStringToken(headers, RntbdRequestHeader.PartitionKeyRangeId,
                request.getHeaders().get(HttpConstants.HttpHeaders.PARTITION_KEY_RANGE_ID));
            assertStringToken(headers, RntbdRequestHeader.PartitionKey,
                request.getHeaders().get(HttpConstants.HttpHeaders.PARTITION_KEY));
            for (RntbdRequestHeader key : Arrays.asList(RntbdRequestHeader.StartEpk, RntbdRequestHeader.EndEpk)) {
                String value = request.getHeaders().get(key == RntbdRequestHeader.StartEpk
                    ? HttpConstants.HttpHeaders.START_EPK : HttpConstants.HttpHeaders.END_EPK);
                assertThat(headers.get(key).isPresent()).isEqualTo(value != null);
                if (value != null) {
                    ByteBuf epk = (ByteBuf) headers.get(key).getValue();
                    try {
                        assertThat(epk.toString(StandardCharsets.UTF_8)).isEqualTo(value);
                    } finally {
                        epk.release();
                    }
                }
            }
            if (request.getHeaders().containsKey(HttpConstants.HttpHeaders.READ_FEED_KEY_TYPE)) {
                assertThat(headers.get(RntbdRequestHeader.ReadFeedKeyType).getValue())
                    .isEqualTo(RntbdConstants.RntbdReadFeedKeyType.EffectivePartitionKeyRange.id());
            }
            byte[] expected = request.getContentAsByteArray();
            boolean hasPayload = expected != null && expected.length > 0;
            assertThat(headers.get(RntbdRequestHeader.PayloadPresent).getValue()).isEqualTo(hasPayload ? (byte) 1 : (byte) 0);
            if (hasPayload) {
                assertThat(encoded.readIntLE()).isEqualTo(expected.length);
                byte[] actual = new byte[encoded.readableBytes()];
                encoded.readBytes(actual);
                assertThat(actual).isEqualTo(expected);
            }
            assertThat(encoded.isReadable()).isFalse();
        } finally {
            if (encoded != null) {
                encoded.release();
            }
            channel.finishAndReleaseAll();
        }
    }

    private void assertStringToken(RntbdRequestHeaders headers, RntbdRequestHeader key, String expected) {
        boolean present = expected != null && !expected.isEmpty();
        assertThat(headers.get(key).isPresent()).isEqualTo(present);
        if (present) {
            assertThat(headers.get(key).getValue()).isEqualTo(expected);
        }
    }
}
