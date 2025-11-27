// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.structuredmessage;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.StorageCrc64Calculator;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import static com.azure.storage.common.implementation.structuredmessage.StructuredMessageConstants.CRC64_LENGTH;
import static com.azure.storage.common.implementation.structuredmessage.StructuredMessageConstants.DEFAULT_MESSAGE_VERSION;
import static com.azure.storage.common.implementation.structuredmessage.StructuredMessageConstants.V1_HEADER_LENGTH;
import static com.azure.storage.common.implementation.structuredmessage.StructuredMessageConstants.V1_SEGMENT_HEADER_LENGTH;

/**
 * Decoder for structured messages with support for segmenting and CRC64 checksums.
 * 
 * <p>This decoder properly handles partial headers and segment splits across HTTP chunks
 * by maintaining a pending buffer and only advancing offsets when complete structures
 * have been fully read and validated.</p>
 * 
 * <p>Key invariants:
 * <ul>
 *   <li>Never read partial headers - always check buffer remaining >= required bytes</li>
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

    // CRC validation
    private long messageCrc64 = 0;
    private long segmentCrc64 = 0;
    private final Map<Integer, Long> segmentCrcs = new HashMap<>();

    // Smart retry tracking - lastCompleteSegmentStart is the absolute offset where the last
    // fully completed segment ended. This is the safe retry boundary.
    private long lastCompleteSegmentStart = 0;

    // Pending buffer for handling partial headers/segments across chunks
    private final ByteArrayOutputStream pendingBytes = new ByteArrayOutputStream();

    /**
     * Decode result status codes.
     */
    public enum DecodeStatus {
        /** Decoding succeeded, more data may be available */
        SUCCESS,
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
     * Returns the canonical absolute byte index (0-based) that should be used to resume a failed/incomplete download.
     * This MUST be used directly as the Range header start value: "Range: bytes={retryStartOffset}-"
     *
     * @return The absolute byte index for the retry start offset.
     */
    public long getRetryStartOffset() {
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
     * Advances the message offset by the specified number of bytes.
     * This should be called after consuming an encoded segment to maintain
     * the authoritative encoded offset.
     *
     * @param bytes The number of bytes to advance.
     */
    public void advanceMessageOffset(long bytes) {
        long priorOffset = messageOffset;
        messageOffset += bytes;
        LOGGER.atInfo()
            .addKeyValue("priorOffset", priorOffset)
            .addKeyValue("bytesAdvanced", bytes)
            .addKeyValue("newOffset", messageOffset)
            .log("Advanced message offset");
    }

    /**
     * Resets the decoder position to the last complete segment boundary.
     * This is used during smart retry to ensure the decoder is in sync with
     * the data being provided from the retry offset.
     */
    public void resetToLastCompleteSegment() {
        if (messageOffset != lastCompleteSegmentStart) {
            LOGGER.atInfo()
                .addKeyValue("fromOffset", messageOffset)
                .addKeyValue("toOffset", lastCompleteSegmentStart)
                .addKeyValue("currentSegmentNum", currentSegmentNumber)
                .addKeyValue("currentSegmentContentOffset", currentSegmentContentOffset)
                .addKeyValue("currentSegmentContentLength", currentSegmentContentLength)
                .log("Resetting decoder to last complete segment boundary");
            messageOffset = lastCompleteSegmentStart;
            // Reset current segment state - next decode will read the segment header
            currentSegmentContentOffset = 0;
            currentSegmentContentLength = 0;
            // Clear any pending bytes since we're resetting to a known boundary
            pendingBytes.reset();
        } else {
            LOGGER.atVerbose()
                .addKeyValue("offset", messageOffset)
                .log("Decoder already at last complete segment boundary, no reset needed");
        }
    }

    /**
     * Converts a ByteBuffer range to hex string for diagnostic purposes.
     */
    private static String toHex(ByteBuffer buf, int len) {
        int pos = buf.position();
        int peek = Math.min(len, buf.remaining());
        byte[] out = new byte[peek];
        buf.get(out, 0, peek);
        buf.position(pos);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < out.length; i++) {
            sb.append(String.format("%02X", out[i]));
            if (i < out.length - 1) {
                sb.append(' ');
            }
        }
        return sb.toString();
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
     * Consumes bytes from pending first, then from buffer.
     * Updates the buffer's position to reflect bytes consumed.
     */
    private void consumeBytes(int bytesToConsume, ByteBuffer buffer) {
        int pendingSize = pendingBytes.size();
        if (bytesToConsume <= pendingSize) {
            // All bytes come from pending - remove from pending
            byte[] remaining = pendingBytes.toByteArray();
            pendingBytes.reset();
            if (bytesToConsume < pendingSize) {
                pendingBytes.write(remaining, bytesToConsume, pendingSize - bytesToConsume);
            }
        } else {
            // Consume all pending and some from buffer
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
     * Peeks the next segment length without consuming from the buffer.
     * Used by the policy to calculate encoded segment size before slicing.
     *
     * @param buffer The buffer to peek from.
     * @param relativeIndex The position in the buffer to start reading from.
     * @return The segment content length, or -1 if not enough bytes.
     */
    public long peekNextSegmentLength(ByteBuffer buffer, int relativeIndex) {
        // Need at least V1_SEGMENT_HEADER_LENGTH bytes to read segment number (2) + segment size (8)
        if (relativeIndex + V1_SEGMENT_HEADER_LENGTH > buffer.limit()) {
            return -1;
        }
        // Segment size is at offset 2 (after segment number which is 2 bytes)
        return buffer.getLong(relativeIndex + 2);
    }

    /**
     * Gets the flags for the current message (needed to determine if CRC is present).
     *
     * @return The message flags, or null if header not yet read.
     */
    public StructuredMessageFlags getFlags() {
        return flags;
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
     * Gets the number of segments from the header.
     *
     * @return The number of segments, or -1 if header not yet read.
     */
    public int getNumSegments() {
        return numSegments;
    }

    /**
     * Checks if the message header has been read.
     *
     * @return true if header has been read, false otherwise.
     */
    public boolean isHeaderRead() {
        return messageLength != -1;
    }

    /**
     * Reads the message header if we have enough bytes.
     * 
     * @param buffer The buffer to read from.
     * @return true if header was successfully read, false if more bytes needed.
     */
    private boolean tryReadMessageHeader(ByteBuffer buffer) {
        if (messageLength != -1) {
            return true; // Already read
        }

        int available = getAvailableBytes(buffer);
        if (available < V1_HEADER_LENGTH) {
            LOGGER.atInfo()
                .addKeyValue("available", available)
                .addKeyValue("required", V1_HEADER_LENGTH)
                .addKeyValue("pendingBytes", pendingBytes.size())
                .log("Not enough bytes for message header, waiting for more");
            appendToPending(buffer);
            return false;
        }

        ByteBuffer combined = getCombinedBuffer(buffer);

        int messageVersion = Byte.toUnsignedInt(combined.get());
        if (messageVersion != DEFAULT_MESSAGE_VERSION) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("Unsupported structured message version: " + messageVersion));
        }

        long msgLen = combined.getLong();
        if (msgLen < V1_HEADER_LENGTH) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Message length too small: " + msgLen));
        }
        if (msgLen != expectedContentLength) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "Structured message length " + msgLen + " did not match content length " + expectedContentLength));
        }

        flags = StructuredMessageFlags.fromValue(Short.toUnsignedInt(combined.getShort()));
        numSegments = Short.toUnsignedInt(combined.getShort());

        // Consume the bytes from pending/buffer
        consumeBytes(V1_HEADER_LENGTH, buffer);
        messageOffset += V1_HEADER_LENGTH;
        messageLength = msgLen;

        LOGGER.atInfo()
            .addKeyValue("messageLength", messageLength)
            .addKeyValue("numSegments", numSegments)
            .addKeyValue("flags", flags)
            .addKeyValue("messageOffset", messageOffset)
            .log("Message header read successfully");

        return true;
    }

    /**
     * Reads a segment header if we have enough bytes.
     *
     * @param buffer The buffer to read from.
     * @return true if segment header was read, false if more bytes needed.
     */
    private boolean tryReadSegmentHeader(ByteBuffer buffer) {
        int available = getAvailableBytes(buffer);
        if (available < V1_SEGMENT_HEADER_LENGTH) {
            LOGGER.atInfo()
                .addKeyValue("available", available)
                .addKeyValue("required", V1_SEGMENT_HEADER_LENGTH)
                .addKeyValue("pendingBytes", pendingBytes.size())
                .addKeyValue("decoderOffset", messageOffset)
                .log("Not enough bytes for segment header, waiting for more");
            appendToPending(buffer);
            return false;
        }

        ByteBuffer combined = getCombinedBuffer(buffer);

        // Log the raw bytes we're about to read
        LOGGER.atInfo()
            .addKeyValue("decoderOffset", messageOffset)
            .addKeyValue("bufferPos", combined.position())
            .addKeyValue("bufferRemaining", combined.remaining())
            .addKeyValue("peek16", toHex(combined, 16))
            .addKeyValue("lastCompleteSegment", lastCompleteSegmentStart)
            .log("Decoder about to read segment header");

        int segmentNum = Short.toUnsignedInt(combined.getShort());
        long segmentSize = combined.getLong();

        // Validate segment number
        if (segmentNum != currentSegmentNumber + 1) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "Unexpected segment number. Expected: " + (currentSegmentNumber + 1) + ", got: " + segmentNum));
        }

        // Validate segment size - must be non-negative and reasonable
        // We can't have segments larger than the remaining message length
        long remainingMessageBytes = messageLength - messageOffset - V1_SEGMENT_HEADER_LENGTH;
        if (segmentSize < 0 || segmentSize > remainingMessageBytes) {
            LOGGER.error("Invalid segment length read: segmentLength={}, decoderOffset={}, lastCompleteSegment={}",
                segmentSize, messageOffset, lastCompleteSegmentStart);
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "Invalid segment size detected: " + segmentSize + " (decoderOffset=" + messageOffset + ")"));
        }

        // Consume the bytes and update state
        consumeBytes(V1_SEGMENT_HEADER_LENGTH, buffer);
        messageOffset += V1_SEGMENT_HEADER_LENGTH;
        currentSegmentNumber = segmentNum;
        currentSegmentContentLength = segmentSize;
        currentSegmentContentOffset = 0;

        if (flags == StructuredMessageFlags.STORAGE_CRC64) {
            segmentCrc64 = 0;
        }

        LOGGER.atInfo()
            .addKeyValue("segmentNum", segmentNum)
            .addKeyValue("segmentLength", segmentSize)
            .addKeyValue("decoderOffset", messageOffset)
            .log("Segment header read successfully");

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
            return 0; // All content read, need to read footer
        }

        int available = getAvailableBytes(buffer);
        if (available == 0) {
            return 0; // No bytes available
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
            return true; // Content not fully read yet
        }

        if (flags == StructuredMessageFlags.STORAGE_CRC64) {
            int available = getAvailableBytes(buffer);
            if (available < CRC64_LENGTH) {
                LOGGER.atInfo()
                    .addKeyValue("available", available)
                    .addKeyValue("required", CRC64_LENGTH)
                    .addKeyValue("segmentNum", currentSegmentNumber)
                    .log("Not enough bytes for segment CRC footer, waiting for more");
                appendToPending(buffer);
                return false;
            }

            ByteBuffer combined = getCombinedBuffer(buffer);
            long reportedCrc64 = combined.getLong();

            if (segmentCrc64 != reportedCrc64) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("CRC64 mismatch detected in segment "
                    + currentSegmentNumber + ". Expected: " + segmentCrc64 + ", got: " + reportedCrc64));
            }

            consumeBytes(CRC64_LENGTH, buffer);
            segmentCrcs.put(currentSegmentNumber, segmentCrc64);
            messageOffset += CRC64_LENGTH;
        }

        // Mark that this segment is complete
        lastCompleteSegmentStart = messageOffset;
        LOGGER.atInfo()
            .addKeyValue("segmentNum", currentSegmentNumber)
            .addKeyValue("offset", lastCompleteSegmentStart)
            .addKeyValue("segmentLength", currentSegmentContentLength)
            .log("Segment complete at byte offset");

        // Check if we need to read message footer
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
            int available = getAvailableBytes(buffer);
            if (available < CRC64_LENGTH) {
                LOGGER.atInfo()
                    .addKeyValue("available", available)
                    .addKeyValue("required", CRC64_LENGTH)
                    .log("Not enough bytes for message CRC footer, waiting for more");
                appendToPending(buffer);
                return false;
            }

            ByteBuffer combined = getCombinedBuffer(buffer);
            long reportedCrc = combined.getLong();

            if (messageCrc64 != reportedCrc) {
                throw LOGGER
                    .logExceptionAsError(new IllegalArgumentException("CRC64 mismatch detected in message footer. "
                        + "Expected: " + messageCrc64 + ", got: " + reportedCrc));
            }

            consumeBytes(CRC64_LENGTH, buffer);
            messageOffset += CRC64_LENGTH;
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

        LOGGER.atInfo()
            .addKeyValue("newBytes", buffer.remaining())
            .addKeyValue("pendingBytes", pendingBytes.size())
            .addKeyValue("decoderOffset", messageOffset)
            .addKeyValue("lastCompleteSegment", lastCompleteSegmentStart)
            .log("Received buffer in decode");

        try {
            // Step 1: Read message header if not yet read
            if (!tryReadMessageHeader(buffer)) {
                return new DecodeResult(DecodeStatus.NEED_MORE_BYTES, null, 0, "Waiting for message header");
            }

            // Step 2: Process segments
            while (messageOffset < messageLength) {
                // Read segment header if needed
                if (currentSegmentContentOffset == currentSegmentContentLength) {
                    if (!tryReadSegmentHeader(buffer)) {
                        break; // Need more bytes for segment header
                    }
                }

                // Read segment content
                int payloadRead = tryReadSegmentContent(buffer, decodedContent);

                // Read segment footer (CRC) if content is complete
                if (currentSegmentContentOffset == currentSegmentContentLength) {
                    if (!tryReadSegmentFooter(buffer)) {
                        break; // Need more bytes for segment footer
                    }
                }

                // Check if all segments are complete
                if (currentSegmentNumber == numSegments && messageOffset >= messageLength) {
                    LOGGER.atInfo()
                        .addKeyValue("messageOffset", messageOffset)
                        .addKeyValue("messageLength", messageLength)
                        .addKeyValue("totalDecodedPayload", totalDecodedPayloadBytes)
                        .log("Message decode completed");

                    ByteBuffer result
                        = decodedContent.size() > 0 ? ByteBuffer.wrap(decodedContent.toByteArray()) : null;
                    return new DecodeResult(DecodeStatus.COMPLETED, result, buffer.position() - startPos,
                        "Decode completed");
                }

                // If we couldn't read any bytes and no data available, need more
                if (payloadRead == 0 && getAvailableBytes(buffer) == 0) {
                    break;
                }
            }

            // Return any decoded content even if we need more bytes
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
     * Decodes the structured message from the given buffer up to the specified size.
     * This is a convenience method that wraps decodeChunk for backwards compatibility.
     *
     * @param buffer The buffer containing the structured message.
     * @param size The maximum number of bytes to decode.
     * @return A ByteBuffer containing the decoded message content.
     * @throws IllegalArgumentException if the buffer does not contain a valid structured message.
     */
    public ByteBuffer decode(ByteBuffer buffer, int size) {
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        ByteArrayOutputStream decodedContent = new ByteArrayOutputStream();

        if (messageOffset == 0) {
            if (!tryReadMessageHeader(buffer)) {
                throw LOGGER.logExceptionAsError(
                    new IllegalArgumentException("Content not long enough to contain a valid message header."));
            }
        }

        while (buffer.hasRemaining() && decodedContent.size() < size) {
            if (currentSegmentContentOffset == currentSegmentContentLength) {
                if (!tryReadSegmentHeader(buffer)) {
                    break; // Need more bytes
                }
            }

            tryReadSegmentContent(buffer, decodedContent);

            if (currentSegmentContentOffset == currentSegmentContentLength) {
                if (!tryReadSegmentFooter(buffer)) {
                    break; // Need more bytes
                }
            }
        }

        return ByteBuffer.wrap(decodedContent.toByteArray());
    }

    /**
     * Decodes the entire structured message from the given buffer.
     *
     * @param buffer The buffer containing the structured message.
     * @return A ByteBuffer containing the decoded message content.
     * @throws IllegalArgumentException if the buffer does not contain a valid structured message.
     */
    public ByteBuffer decode(ByteBuffer buffer) {
        return decode(buffer, buffer.remaining());
    }

    /**
     * Finalizes the decoding process and returns any final decoded bytes still buffered internally.
     * The policy should aggregate decoded byte counts and perform the final length comparison.
     *
     * @return A ByteBuffer containing any final decoded bytes, or null if none remain.
     * @throws IllegalArgumentException if the encoded message offset doesn't match expected length.
     */
    public ByteBuffer finalizeDecoding() {
        if (messageOffset != messageLength) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Decoded message length does not match "
                + "expected length. Expected: " + messageLength + ", but was: " + messageOffset));
        }
        // No buffered decoded bytes in current implementation
        return null;
    }

    /**
     * Checks if decoding is complete.
     *
     * @return true if all expected bytes have been decoded, false otherwise.
     */
    public boolean isComplete() {
        return messageLength != -1 && messageOffset >= messageLength;
    }
}
