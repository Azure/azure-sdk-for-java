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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Unit tests for the WorkloadId RNTBD header definition in RntbdConstants.
 * <p>
 *
 * These tests verify that the WorkloadId enum entry exists with the correct wire ID (0x00E7),
 * correct token type (UShort), is not required, and is not in the thin-client ordered header list
 * (so it will be auto-encoded in the second pass of RntbdTokenStream.encode()).
 */
public class RntbdWorkloadIdTests {

    /**
     * Verifies that the WORKLOAD_ID HTTP header constant exists in HttpConstants.HttpHeaders
     * with the correct canonical name "x-ms-cosmos-workload-id" used in Gateway mode and
     * as the lookup key in RntbdRequestHeaders for HTTP-to-RNTBD mapping.
     */
    @Test(groups = { "unit" })
    public void workloadIdConstantExists() {
        assertThat(HttpConstants.HttpHeaders.WORKLOAD_ID).isEqualTo("x-ms-cosmos-workload-id");
    }

    /**
     * Verifies that the WorkloadId enum entry exists in RntbdConstants.RntbdRequestHeader
     * with the correct wire ID (0x00E7). This ID is used to identify the header in the
     * binary RNTBD protocol when communicating in Direct mode and must match the
     * service-side RequestIdentifiers.WorkloadId definition.
     */
    @Test(groups = { "unit" })
    public void workloadIdRntbdHeaderExists() {
        // Verify WorkloadId enum value exists with correct ID
        RntbdConstants.RntbdRequestHeader workloadIdHeader = RntbdConstants.RntbdRequestHeader.WorkloadId;
        assertThat(workloadIdHeader).isNotNull();
        assertThat(workloadIdHeader.id()).isEqualTo((short) 0x00E7);
    }

    /**
     * Verifies that the WorkloadId RNTBD header is defined as UShort token type,
     * matching the service-side token definition. The workload ID value is encoded
     * as an unsigned 16-bit integer on the wire.
     */
    @Test(groups = { "unit" })
    public void workloadIdRntbdHeaderIsUShortType() {
        RntbdConstants.RntbdRequestHeader workloadIdHeader = RntbdConstants.RntbdRequestHeader.WorkloadId;
        assertThat(workloadIdHeader.type()).isEqualTo(RntbdTokenType.UShort);
    }

    /**
     * Verifies that WorkloadId does not reuse the wire ID of another request header.
     * 0x00DB is ThroughputBucket and 0x00DC is reserved by the service for
     * UpdateOfferStateToPendingForThroughputSplit.
     */
    @Test(groups = { "unit" })
    public void workloadIdRntbdHeaderIdIsUnique() {
        for (RntbdConstants.RntbdRequestHeader header : RntbdConstants.RntbdRequestHeader.values()) {
            if (header != RntbdConstants.RntbdRequestHeader.WorkloadId) {
                assertThat(header.id())
                    .as("RNTBD request header %s shares wire ID with WorkloadId", header)
                    .isNotEqualTo(RntbdConstants.RntbdRequestHeader.WorkloadId.id());
            }
        }
        assertThat(RntbdConstants.RntbdRequestHeader.WorkloadId.id()).isNotEqualTo((short) 0x00DC);
    }

    /**
     * Verifies that WorkloadId is not a required RNTBD header. The header is optional —
     * requests without a workload ID are valid and should not be rejected by the SDK.
     */
    @Test(groups = { "unit" })
    public void workloadIdRntbdHeaderIsNotRequired() {
        // WorkloadId should not be a required header
        RntbdConstants.RntbdRequestHeader workloadIdHeader = RntbdConstants.RntbdRequestHeader.WorkloadId;
        assertThat(workloadIdHeader.isRequired()).isFalse();
    }

    /**
     * Verifies that WorkloadId is NOT in the thin client ordered header list. Thin client
     * mode uses a pre-ordered list of headers for its first encoding pass. WorkloadId is
     * excluded from this list and will be auto-encoded in the second pass of
     * RntbdTokenStream.encode() along with other non-ordered headers.
     */
    @Test(groups = { "unit" })
    public void workloadIdNotInThinClientOrderedList() {
        // WorkloadId should NOT be in thinClientHeadersInOrderList
        // It will be automatically encoded in the second pass of RntbdTokenStream.encode()
        assertThat(RntbdConstants.RntbdRequestHeader.thinClientHeadersInOrderList)
            .doesNotContain(RntbdConstants.RntbdRequestHeader.WorkloadId);
    }

    @Test(groups = { "unit" })
    public void workloadIdIsEncodedOnRntbdRequestHeaders() {
        RntbdRequestHeaders requestHeaders = createRequestHeaders("15");

        RntbdToken workloadIdToken = requestHeaders.get(RntbdConstants.RntbdRequestHeader.WorkloadId);
        assertThat(workloadIdToken.isPresent()).isTrue();
        assertThat(workloadIdToken.getValue(Integer.class)).isEqualTo(15);
    }

    @Test(groups = { "unit" })
    public void workloadIdIsEncodedAsTwoByteLittleEndianValue() {
        RntbdRequestHeaders requestHeaders = createRequestHeaders("50");
        RntbdToken workloadIdToken = requestHeaders.get(RntbdConstants.RntbdRequestHeader.WorkloadId);

        ByteBuf buffer = Unpooled.buffer();
        try {
            workloadIdToken.encode(buffer);

            // Token layout: identifier (ushort LE), token type (byte), value (ushort LE)
            assertThat(buffer.readableBytes()).isEqualTo(Short.BYTES + Byte.BYTES + Short.BYTES);
            assertThat(buffer.readUnsignedShortLE()).isEqualTo(0x00E7);
            assertThat(buffer.readByte()).isEqualTo(RntbdTokenType.UShort.id());
            assertThat(buffer.readUnsignedShortLE()).isEqualTo(50);
        } finally {
            buffer.release();
        }
    }

    @Test(groups = { "unit" })
    public void nonNumericWorkloadIdDoesNotThrowAndIsIgnored() {
        assertThatCode(() -> createRequestHeaders("not-a-number")).doesNotThrowAnyException();

        RntbdRequestHeaders requestHeaders = createRequestHeaders("not-a-number");
        RntbdToken workloadIdToken = requestHeaders.get(RntbdConstants.RntbdRequestHeader.WorkloadId);
        assertThat(workloadIdToken.isPresent()).isFalse();
    }

    @Test(groups = { "unit" })
    public void workloadIdAtUShortBoundariesIsEncoded() {
        assertThat(createRequestHeaders("0").get(RntbdConstants.RntbdRequestHeader.WorkloadId).getValue(Integer.class))
            .isEqualTo(0);
        assertThat(createRequestHeaders("65535").get(RntbdConstants.RntbdRequestHeader.WorkloadId).getValue(Integer.class))
            .isEqualTo(65535);
    }

    @Test(groups = { "unit" })
    public void workloadIdOutsideUShortRangeIsNotTruncatedAndIsIgnored() {
        for (String value : new String[] { "65536", "65537", "-1", String.valueOf(Integer.MAX_VALUE) }) {
            assertThatCode(() -> createRequestHeaders(value)).doesNotThrowAnyException();

            RntbdToken workloadIdToken = createRequestHeaders(value).get(RntbdConstants.RntbdRequestHeader.WorkloadId);
            assertThat(workloadIdToken.isPresent())
                .as("workload id %s must not be truncated into a different value", value)
                .isFalse();
        }
    }

    private static RntbdRequestHeaders createRequestHeaders(String workloadId) {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            null,
            OperationType.Create,
            ResourceType.Document);

        request.getHeaders().put(HttpConstants.HttpHeaders.WORKLOAD_ID, workloadId);

        RntbdRequestFrame frame = new RntbdRequestFrame(
            UUID.randomUUID(),
            RntbdConstants.RntbdOperationType.Create,
            RntbdConstants.RntbdResourceType.Document);

        RntbdRequestArgs args = new RntbdRequestArgs(request, new Uri("prefix://someUri"));
        return new RntbdRequestHeaders(args, frame);
    }
}
