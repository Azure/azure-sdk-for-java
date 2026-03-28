// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.storage.common.implementation.contentvalidation.StorageCrc64Calculator;
import com.azure.storage.common.implementation.contentvalidation.StructuredMessageDecoder;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregates CRC state across retries so the composed CRC64 can be validated against the
 * running CRC of all decoded payload bytes.
 */
public final class AggregateCrcState {
    private final List<StructuredMessageDecoder.SegmentInfo> segments = new ArrayList<>();
    private long runningCrc = 0;

    /**
     * Creates a new instance of {@link AggregateCrcState}.
     */
    public AggregateCrcState() {
    }

    void appendPayload(ByteBuffer payload) {
        if (payload == null || !payload.hasRemaining()) {
            return;
        }
        ByteBuffer copy = payload.asReadOnlyBuffer();
        byte[] data = new byte[copy.remaining()];
        copy.get(data);
        runningCrc = StorageCrc64Calculator.compute(data, runningCrc);
    }

    void addSegments(List<StructuredMessageDecoder.SegmentInfo> newSegments) {
        if (newSegments == null || newSegments.isEmpty()) {
            return;
        }
        segments.addAll(newSegments);
    }

    boolean hasSegments() {
        return !segments.isEmpty();
    }

    long getRunningCrc() {
        return runningCrc;
    }

    long composeCrc() {
        if (segments.isEmpty()) {
            return 0;
        }
        long composed = segments.get(0).getCrc64();
        long totalLength = segments.get(0).getLength();
        for (int i = 1; i < segments.size(); i++) {
            StructuredMessageDecoder.SegmentInfo next = segments.get(i);
            composed = StorageCrc64Calculator.concat(0, 0, composed, totalLength, 0, next.getCrc64(), next.getLength());
            totalLength += next.getLength();
        }
        return composed;
    }

    long getTotalLength() {
        long totalLength = 0;
        for (StructuredMessageDecoder.SegmentInfo segment : segments) {
            totalLength += segment.getLength();
        }
        return totalLength;
    }
}
