// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.contentvalidation.StorageCrc64Calculator;
import com.azure.storage.common.implementation.contentvalidation.StructuredMessageDecoder;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks the progress of a single structured-message decode attempt. A new instance is
 * created for each HTTP response (including retries). The aggregate CRC state, when
 * present, is shared across retries to enable end-to-end CRC64 validation.
 */
public class DecoderState {
    private static final ClientLogger LOGGER = new ClientLogger(DecoderState.class);

    private final StructuredMessageDecoder decoder;
    final AggregateCrcState aggregateCrcState;
    final AtomicLong totalBytesDecoded;
    long decodedBytesAtLastCompleteSegment;
    long lastCompleteSegmentStart;
    final AtomicLong decodedBytesToSkip = new AtomicLong(0);
    private boolean segmentsAddedToAggregate;

    DecoderState(long expectedContentLength, AggregateCrcState aggregateCrcState) {
        this.decoder = new StructuredMessageDecoder(expectedContentLength);
        this.totalBytesDecoded = new AtomicLong(0);
        this.decodedBytesAtLastCompleteSegment = 0;
        this.aggregateCrcState = aggregateCrcState;
        this.segmentsAddedToAggregate = false;
    }

    StructuredMessageDecoder getDecoder() {
        return decoder;
    }

    void updateProgress() {
        long currentLastComplete = decoder.getLastCompleteSegmentStart();
        if (lastCompleteSegmentStart != currentLastComplete) {
            decodedBytesAtLastCompleteSegment = decoder.getDecodedBytesAtLastCompleteSegment();
            lastCompleteSegmentStart = currentLastComplete;

            LOGGER.atInfo()
                .addKeyValue("newSegmentBoundary", currentLastComplete)
                .addKeyValue("decodedBytesAtBoundary", decodedBytesAtLastCompleteSegment)
                .log("Segment boundary crossed, updated decoded bytes snapshot");
        }
    }

    void addSegmentsToAggregateIfNeeded() {
        if (segmentsAddedToAggregate || aggregateCrcState == null) {
            return;
        }
        aggregateCrcState.addSegments(decoder.getCompletedSegments());
        segmentsAddedToAggregate = true;
    }

    void setDecodedBytesToSkip(long bytesToSkip) {
        decodedBytesToSkip.set(Math.max(0, bytesToSkip));
    }

    /**
     * Returns true if the decoder has finalized (all segments decoded and validated).
     *
     * @return true if finalized, false otherwise.
     */
    public boolean isFinalized() {
        return decoder.isComplete();
    }

    /**
     * Gets the decoded byte count at the last validated segment boundary.
     *
     * @return the decoded byte count.
     */
    public long getDecodedBytesAtLastCompleteSegment() {
        return decodedBytesAtLastCompleteSegment;
    }

    /**
     * Gets the composed CRC64 over all validated segments.
     *
     * @return the composed CRC64 value.
     */
    public long getComposedCrc64() {
        if (aggregateCrcState != null && aggregateCrcState.hasSegments()) {
            return aggregateCrcState.composeCrc();
        }

        List<StructuredMessageDecoder.SegmentInfo> segments = decoder.getCompletedSegments();
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

    /**
     * Gets the composed decoded payload length represented by validated segments.
     *
     * @return the composed payload length.
     */
    public long getComposedLength() {
        if (aggregateCrcState != null && aggregateCrcState.hasSegments()) {
            return aggregateCrcState.getTotalLength();
        }

        List<StructuredMessageDecoder.SegmentInfo> segments = decoder.getCompletedSegments();
        long totalLength = 0;
        for (StructuredMessageDecoder.SegmentInfo segment : segments) {
            totalLength += segment.getLength();
        }
        return totalLength;
    }

    /**
     * Gets the decoded offset to use for retry requests.
     *
     * @return the retry offset.
     */
    public long getRetryOffset() {
        long retryOffset = decodedBytesAtLastCompleteSegment;
        LOGGER.atInfo()
            .addKeyValue("decoderOffset", decoder.getMessageOffset())
            .addKeyValue("pendingBytes", decoder.getPendingEncodedByteCount())
            .addKeyValue("lastCompleteSegment", decoder.getLastCompleteSegmentStart())
            .log("Computed smart-retry offset from decoder state");
        return retryOffset;
    }
}
