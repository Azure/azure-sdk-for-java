// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.contentvalidation;

import com.azure.core.util.logging.ClientLogger;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants.CRC64_LENGTH;
import static com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants.DEFAULT_MESSAGE_VERSION;
import static com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants.V1_HEADER_LENGTH;
import static com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants.V1_SEGMENT_HEADER_LENGTH;

/**
 * Decoder for structured messages with support for segmenting and CRC64 checksums.
 *
 * <p>This decoder properly handles partial headers and segment splits across HTTP chunks
 * by maintaining a pending buffer and only advancing offsets when complete structures
 * have been fully read and validated.</p>
 *
 * <p>Key invariants:
 * <ul>
 *   <li>Never read partial headers - always check buffer remaining &gt;= required bytes</li>
 *   <li>Only advance messageOffset when bytes are fully consumed and validated</li>
 *   <li>lastCompleteSegmentStart always points to a valid segment boundary for retry</li>
 * </ul>
 */
public class StructuredMessageDecoder {
    private static final ClientLogger LOGGER = new ClientLogger(StructuredMessageDecoder.class);

    // Message state
    private long messageLength = -1;
    private StructuredMessageFlags flags;
    private int numSegments = -1;
    private final long expectedContentLength;

    // Offset tracking
    private long messageOffset = 0;  // Absolute encoded bytes consumed from the message
    private long totalDecodedPayloadBytes = 0;  // Total decoded (payload) bytes output

    // Current segment state
    private int currentSegmentNumber = 0;
    private long currentSegmentContentLength = 0;
    private long currentSegmentContentOffset = 0;
    private int lastCompleteSegmentNumber = 0;

    // CRC validation
    private long messageCrc64 = 0;
    private long segmentCrc64 = 0;

    // Smart retry tracking - lastCompleteSegmentStart is the absolute offset where the last
    // fully completed segment ended. This is the safe retry boundary.
    private long lastCompleteSegmentStart = 0;

    // Pending buffer for handling partial headers/segments across chunks
    private final ByteArrayOutputStream pendingBytes = new ByteArrayOutputStream();

    /**
     * Decode result status codes.
     */
    public enum DecodeStatus {
        /** Need more bytes to continue (partial header/segment) */
        NEED_MORE_BYTES,
        /** Decoding completed successfully */
        COMPLETED,
        /** Invalid data encountered */
        INVALID
    }

    /**
     * Result of a decode operation.
     */
    public static class DecodeResult {
        private final DecodeStatus status;
        private final ByteBuffer decodedPayload;
        private final String message;
        private final int bytesConsumed;

        DecodeResult(DecodeStatus status, ByteBuffer decodedPayload, int bytesConsumed, String message) {
            this.status = status;
            this.decodedPayload = decodedPayload;
            this.bytesConsumed = bytesConsumed;
            this.message = message;
        }

        public DecodeStatus getStatus() {
            return status;
        }

        public ByteBuffer getDecodedPayload() {
            return decodedPayload;
        }

        public int getBytesConsumed() {
            return bytesConsumed;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Constructs a new StructuredMessageDecoder.
     *
     * @param expectedContentLength The expected length of the content to be decoded.
     */
    public StructuredMessageDecoder(long expectedContentLength) {
        this.expectedContentLength = expectedContentLength;
    }

    /**
     * Gets the byte offset where the last complete segment ended.
     * This is used for smart retry to resume from a segment boundary.
     *
     * @return The byte offset of the last complete segment boundary.
     */
    public long getLastCompleteSegmentStart() {
        return lastCompleteSegmentStart;
    }

    /**
     * Gets the current message offset (total bytes consumed from the structured message).
     *
     * @return The current message offset.
     */
    public long getMessageOffset() {
        return messageOffset;
    }

    /**
     * Gets the total decoded payload bytes produced so far.
     *
     * @return The total decoded payload bytes.
     */
    public long getTotalDecodedPayloadBytes() {
        return totalDecodedPayloadBytes;
    }

    /**
     * Gets the expected message length from the header.
     *
     * @return The message length, or -1 if header not yet read.
     */
    public long getMessageLength() {
        return messageLength;
    }

    /**
     * Gets the total available bytes (pending + buffer remaining).
     */
    private int getAvailableBytes(ByteBuffer buffer) {
        return pendingBytes.size() + buffer.remaining();
    }

    /**
     * Creates a combined buffer from pending bytes and new buffer.
     * Returns a new buffer with position=0 and LITTLE_ENDIAN order.
     * The original buffer's position is NOT advanced.
     */
    private ByteBuffer getCombinedBuffer(ByteBuffer buffer) {
        if (pendingBytes.size() == 0) {
            ByteBuffer dup = buffer.duplicate();
            dup.order(ByteOrder.LITTLE_ENDIAN);
            return dup;
        }

        byte[] pending = pendingBytes.toByteArray();
        ByteBuffer combined = ByteBuffer.allocate(pending.length + buffer.remaining());
        combined.order(ByteOrder.LITTLE_ENDIAN);
        combined.put(pending);
        combined.put(buffer.duplicate());
        combined.flip();
        return combined;
    }

    /**
     * When {@code flags} require a segment or message CRC footer, reads 8 bytes, validates against {@code expectedCrc64},
     * and advances {@link #messageOffset}. Returns false if the footer is not yet available.
     */
    private boolean tryConsumeCrc64Footer(ByteBuffer buffer, long expectedCrc64, String mismatchDetail) {
        if (getAvailableBytes(buffer) < CRC64_LENGTH) {
            appendToPending(buffer);
            return false;
        }
        ByteBuffer combined = getCombinedBuffer(buffer);
        long reportedCrc = combined.getLong();
        if (expectedCrc64 != reportedCrc) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(enrichExceptionMessage(
                "CRC64 mismatch" + mismatchDetail + ". Expected: " + expectedCrc64 + ", got: " + reportedCrc)));
        }
        consumeBytes(CRC64_LENGTH, buffer);
        messageOffset += CRC64_LENGTH;
        return true;
    }

    private void consumeBytes(int bytesToConsume, ByteBuffer buffer) {
        int pendingSize = pendingBytes.size();
        if (bytesToConsume <= pendingSize) {
            byte[] remaining = pendingBytes.toByteArray();
            pendingBytes.reset();
            if (bytesToConsume < pendingSize) {
                pendingBytes.write(remaining, bytesToConsume, pendingSize - bytesToConsume);
            }
        } else {
            int bytesFromBuffer = bytesToConsume - pendingSize;
            pendingBytes.reset();
            buffer.position(buffer.position() + bytesFromBuffer);
        }
    }

    /**
     * Appends remaining buffer bytes to pending for next chunk.
     */
    private void appendToPending(ByteBuffer buffer) {
        while (buffer.hasRemaining()) {
            pendingBytes.write(buffer.get());
        }
    }

    /**
     * Reads the message header if we have enough bytes.
     *
     * @param buffer The buffer to read from.
     * @return true if header was successfully read, false if more bytes needed.
     */
    private boolean tryReadMessageHeader(ByteBuffer buffer) {
        if (messageLength != -1) {
            return true;
        }

        if (getAvailableBytes(buffer) < V1_HEADER_LENGTH) {
            appendToPending(buffer);
            return false;
        }

        ByteBuffer combined = getCombinedBuffer(buffer);

        int messageVersion = Byte.toUnsignedInt(combined.get());
        if (messageVersion != DEFAULT_MESSAGE_VERSION) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                enrichExceptionMessage("Unsupported structured message version: " + messageVersion)));
        }

        long msgLen = combined.getLong();
        if (msgLen < V1_HEADER_LENGTH) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException(enrichExceptionMessage("Message length too small: " + msgLen)));
        }
        if (msgLen != expectedContentLength) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(enrichExceptionMessage(
                "Structured message length " + msgLen + " did not match content length " + expectedContentLength)));
        }

        flags = StructuredMessageFlags.fromValue(Short.toUnsignedInt(combined.getShort()));
        numSegments = Short.toUnsignedInt(combined.getShort());

        // Consume the bytes from pending/buffer
        consumeBytes(V1_HEADER_LENGTH, buffer);
        messageOffset += V1_HEADER_LENGTH;
        messageLength = msgLen;

        return true;
    }

    /**
     * Reads a segment header if we have enough bytes.
     *
     * @param buffer The buffer to read from.
     * @return true if segment header was read, false if more bytes needed.
     */
    private boolean tryReadSegmentHeader(ByteBuffer buffer) {
        if (getAvailableBytes(buffer) < V1_SEGMENT_HEADER_LENGTH) {
            appendToPending(buffer);
            return false;
        }

        ByteBuffer combined = getCombinedBuffer(buffer);

        int segmentNum = Short.toUnsignedInt(combined.getShort());
        long segmentSize = combined.getLong();

        // Validate segment number
        if (segmentNum != currentSegmentNumber + 1) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(enrichExceptionMessage(
                "Unexpected segment number. Expected: " + (currentSegmentNumber + 1) + ", got: " + segmentNum)));
        }

        long remainingMessageBytes = messageLength - messageOffset - V1_SEGMENT_HEADER_LENGTH;
        if (segmentSize < 0 || segmentSize > remainingMessageBytes) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(enrichExceptionMessage(
                "Invalid segment size detected: " + segmentSize + " (remaining=" + remainingMessageBytes + ")")));
        }

        consumeBytes(V1_SEGMENT_HEADER_LENGTH, buffer);
        messageOffset += V1_SEGMENT_HEADER_LENGTH;
        currentSegmentNumber = segmentNum;
        currentSegmentContentLength = segmentSize;
        currentSegmentContentOffset = 0;

        if (flags == StructuredMessageFlags.STORAGE_CRC64) {
            segmentCrc64 = 0;
        }

        return true;
    }

    /**
     * Reads segment content bytes if available.
     *
     * @param buffer The buffer to read from.
     * @param output The output stream to write decoded payload to.
     * @return The number of payload bytes read, or -1 if more bytes needed for CRC.
     */
    private int tryReadSegmentContent(ByteBuffer buffer, ByteArrayOutputStream output) {
        long remaining = currentSegmentContentLength - currentSegmentContentOffset;
        if (remaining == 0) {
            return 0;
        }

        int available = getAvailableBytes(buffer);
        if (available == 0) {
            return 0;
        }

        int toRead = (int) Math.min(available, remaining);
        ByteBuffer combined = getCombinedBuffer(buffer);

        byte[] content = new byte[toRead];
        combined.get(content);
        output.write(content, 0, toRead);

        if (flags == StructuredMessageFlags.STORAGE_CRC64) {
            segmentCrc64 = StorageCrc64Calculator.compute(content, segmentCrc64);
            messageCrc64 = StorageCrc64Calculator.compute(content, messageCrc64);
        }

        consumeBytes(toRead, buffer);
        messageOffset += toRead;
        currentSegmentContentOffset += toRead;
        totalDecodedPayloadBytes += toRead;

        return toRead;
    }

    /**
     * Reads the segment CRC footer if needed and available.
     *
     * @param buffer The buffer to read from.
     * @return true if footer was read (or not needed), false if more bytes needed.
     */
    private boolean tryReadSegmentFooter(ByteBuffer buffer) {
        if (currentSegmentContentOffset != currentSegmentContentLength) {
            return true;
        }

        if (flags == StructuredMessageFlags.STORAGE_CRC64) {
            if (!tryConsumeCrc64Footer(buffer, segmentCrc64, " in segment " + currentSegmentNumber)) {
                return false;
            }
        }

        lastCompleteSegmentStart = messageOffset;
        lastCompleteSegmentNumber = currentSegmentNumber;

        if (currentSegmentNumber == numSegments) {
            return tryReadMessageFooter(buffer);
        }

        return true;
    }

    /**
     * Reads the message CRC footer if needed and available.
     *
     * @param buffer The buffer to read from.
     * @return true if footer was read (or not needed), false if more bytes needed.
     */
    private boolean tryReadMessageFooter(ByteBuffer buffer) {
        if (flags == StructuredMessageFlags.STORAGE_CRC64) {
            return tryConsumeCrc64Footer(buffer, messageCrc64, " in message footer");
        }
        return true;
    }

    /**
     * Decodes as much as possible from the given buffer.
     * This method properly handles partial headers and segments by buffering
     * incomplete data and returning NEED_MORE_BYTES when more data is required.
     *
     * @param buffer The buffer containing encoded data.
     * @return A DecodeResult indicating the outcome and any decoded payload.
     */
    public DecodeResult decodeChunk(ByteBuffer buffer) {
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        ByteArrayOutputStream decodedContent = new ByteArrayOutputStream();
        int startPos = buffer.position();

        try {
            if (!tryReadMessageHeader(buffer)) {
                return new DecodeResult(DecodeStatus.NEED_MORE_BYTES, null, 0, "Waiting for message header");
            }

            while (messageOffset < messageLength) {
                if (lastCompleteSegmentNumber == currentSegmentNumber && currentSegmentNumber < numSegments) {
                    if (!tryReadSegmentHeader(buffer)) {
                        break;
                    }
                }

                int payloadRead = tryReadSegmentContent(buffer, decodedContent);

                if (currentSegmentContentOffset == currentSegmentContentLength) {
                    if (!tryReadSegmentFooter(buffer)) {
                        break;
                    }
                }

                if (currentSegmentNumber == numSegments && messageOffset >= messageLength) {
                    ByteBuffer result
                        = decodedContent.size() > 0 ? ByteBuffer.wrap(decodedContent.toByteArray()) : null;
                    return new DecodeResult(DecodeStatus.COMPLETED, result, buffer.position() - startPos,
                        "Decode completed");
                }

                if (payloadRead == 0 && getAvailableBytes(buffer) == 0) {
                    break;
                }
            }

            ByteBuffer result = decodedContent.size() > 0 ? ByteBuffer.wrap(decodedContent.toByteArray()) : null;

            if (messageOffset >= messageLength) {
                return new DecodeResult(DecodeStatus.COMPLETED, result, buffer.position() - startPos,
                    "Decode completed");
            }

            return new DecodeResult(DecodeStatus.NEED_MORE_BYTES, result, buffer.position() - startPos,
                "Waiting for more data");

        } catch (IllegalArgumentException e) {
            return new DecodeResult(DecodeStatus.INVALID, null, buffer.position() - startPos, e.getMessage());
        }
    }

    /**
     * Checks if decoding is complete.
     *
     * @return true if all expected bytes have been decoded, false otherwise.
     */
    public boolean isComplete() {
        return messageLength != -1 && messageOffset >= messageLength;
    }

    /**
     * Enriches an exception message with decoder offset information for debugging and retry.
     * Format: "original message [decoderOffset=X,lastCompleteSegment=Y]"
     *
     * @param message The original exception message.
     * @return The enriched message with offset information.
     */
    private String enrichExceptionMessage(String message) {
        return String.format("%s [decoderOffset=%d,lastCompleteSegment=%d]", message, messageOffset,
            lastCompleteSegmentStart);
    }
}
