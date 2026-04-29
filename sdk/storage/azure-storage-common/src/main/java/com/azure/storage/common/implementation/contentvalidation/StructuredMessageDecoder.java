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
 * <p>Emission guarantee: payload bytes for a segment are <strong>never</strong> emitted
 * downstream until the segment payload has been fully read and, when CRC64 is enabled,
 * its segment CRC footer has been validated. This matches the emission semantics used by
 * {@code BlobDecryptionPolicy}/{@code DecryptorV2} (which only emits a decrypted region
 * after the GCM tag is verified) and guarantees that no unvalidated bytes are ever
 * exposed to callers, even under retries.</p>
 */
public class StructuredMessageDecoder {
    private static final ClientLogger LOGGER = new ClientLogger(StructuredMessageDecoder.class);

    // Message state
    private long messageLength = -1;
    private StructuredMessageFlags flags;
    private int numSegments = -1;
    private final long expectedContentLength;

    // Offset tracking
    private long messageOffset = 0;

    // Current segment state
    private int currentSegmentNumber = 0;
    private long currentSegmentContentLength = 0;
    private long currentSegmentContentOffset = 0;
    private boolean segmentHeaderRead = false;

    // CRC validation
    private long messageCrc64 = 0;
    private long segmentCrc64 = 0;

    // Pending buffer for handling partial headers/segments across chunks
    private final ByteArrayOutputStream pendingBytes = new ByteArrayOutputStream();

    // Payload bytes accumulated for the current segment. These are held back and NOT
    // emitted until the segment CRC footer has been validated, so callers never observe
    // bytes that could later fail validation.
    private final ByteArrayOutputStream currentSegmentBuffer = new ByteArrayOutputStream();

    /**
     * Constructs a new StructuredMessageDecoder.
     *
     * @param expectedContentLength The expected length of the content to be decoded.
     */
    public StructuredMessageDecoder(long expectedContentLength) {
        this.expectedContentLength = expectedContentLength;
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
        if (numSegments < 1) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                enrichExceptionMessage("Structured message must have at least one segment, got: " + numSegments)));
        }

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

        long footerSize = flags == StructuredMessageFlags.STORAGE_CRC64 ? CRC64_LENGTH : 0;
        long remainingSegmentsAfterThis = (long) numSegments - segmentNum;
        long reservedBytes
            = footerSize + remainingSegmentsAfterThis * (V1_SEGMENT_HEADER_LENGTH + footerSize) + footerSize;
        long maxSegmentSize = messageLength - messageOffset - V1_SEGMENT_HEADER_LENGTH - reservedBytes;
        if (segmentSize < 0 || segmentSize > maxSegmentSize) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(enrichExceptionMessage(
                "Invalid segment size detected: " + segmentSize + " (max=" + maxSegmentSize + ")")));
        }

        consumeBytes(V1_SEGMENT_HEADER_LENGTH, buffer);
        messageOffset += V1_SEGMENT_HEADER_LENGTH;
        currentSegmentNumber = segmentNum;
        currentSegmentContentLength = segmentSize;
        currentSegmentContentOffset = 0;
        currentSegmentBuffer.reset();

        if (flags == StructuredMessageFlags.STORAGE_CRC64) {
            segmentCrc64 = 0;
        }

        return true;
    }

    /**
     * Reads segment content bytes if available, accumulating them into the per-segment
     * buffer. Bytes remain held in the buffer until the segment's CRC footer is
     * validated; they are not returned to the caller here.
     *
     * @param buffer The buffer to read from.
     * @return The number of payload bytes read into the segment buffer.
     */
    private int tryReadSegmentContent(ByteBuffer buffer) {
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
        currentSegmentBuffer.write(content, 0, toRead);

        if (flags == StructuredMessageFlags.STORAGE_CRC64) {
            segmentCrc64 = StorageCrc64Calculator.compute(content, segmentCrc64);
            messageCrc64 = StorageCrc64Calculator.compute(content, messageCrc64);
        }

        consumeBytes(toRead, buffer);
        messageOffset += toRead;
        currentSegmentContentOffset += toRead;

        return toRead;
    }

    /**
     * Reads the segment CRC footer if needed and available. Does not advance into the
     * message footer; callers drive message-footer consumption separately so that
     * segment bytes can be flushed as soon as their CRC passes, even if the message
     * footer is not yet available in the current chunk.
     *
     * @param buffer The buffer to read from.
     * @return true if footer was read (or not needed), false if more bytes needed.
     */
    private boolean tryReadSegmentFooter(ByteBuffer buffer) {
        if (currentSegmentContentOffset != currentSegmentContentLength) {
            return true;
        }

        if (flags == StructuredMessageFlags.STORAGE_CRC64) {
            return tryConsumeCrc64Footer(buffer, segmentCrc64, " in segment " + currentSegmentNumber);
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
     * Decodes as much as possible from the given buffer and returns any fully validated
     * payload bytes that are now safe to emit downstream.
     *
     * <p>The returned buffer will only ever contain bytes from segments whose CRC (when
     * enabled) has already been verified. If no segments have been fully validated by
     * this invocation the method returns {@code null}. Callers distinguish "more bytes
     * needed" from "stream complete" via {@link #isComplete()}.</p>
     *
     * @param buffer The buffer containing encoded data.
     * @return Validated payload bytes ready to emit, or {@code null} if none are ready.
     * @throws IllegalArgumentException if the input is malformed or a CRC64 check fails.
     */
    public ByteBuffer decodeChunk(ByteBuffer buffer) {
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        ByteArrayOutputStream validatedOutput = new ByteArrayOutputStream();

        if (!tryReadMessageHeader(buffer)) {
            return emptyOrNull(validatedOutput);
        }

        while (messageOffset < messageLength) {
            if (!segmentHeaderRead) {
                // All segments are done; only the trailing message footer remains.
                if (currentSegmentNumber == numSegments) {
                    if (!tryReadMessageFooter(buffer)) {
                        break;
                    }
                    break;
                }
                if (!tryReadSegmentHeader(buffer)) {
                    break;
                }
                segmentHeaderRead = true;
            }

            int payloadRead = tryReadSegmentContent(buffer);

            if (currentSegmentContentOffset == currentSegmentContentLength) {
                if (!tryReadSegmentFooter(buffer)) {
                    break;
                }
                // Segment is fully validated: safe to release the buffered payload.
                try {
                    currentSegmentBuffer.writeTo(validatedOutput);
                } catch (java.io.IOException e) {
                    // ByteArrayOutputStream.writeTo(ByteArrayOutputStream) cannot throw.
                    throw LOGGER.logExceptionAsError(new IllegalStateException(e));
                }
                currentSegmentBuffer.reset();
                segmentHeaderRead = false;
                // Loop continues: either consume the next segment header or the message footer.
            } else if (payloadRead == 0 && getAvailableBytes(buffer) == 0) {
                break;
            }
        }

        return emptyOrNull(validatedOutput);
    }

    private static ByteBuffer emptyOrNull(ByteArrayOutputStream output) {
        if (output.size() == 0) {
            return null;
        }
        return ByteBuffer.wrap(output.toByteArray());
    }

    /**
     * Checks if decoding is complete.
     *
     * @return true if all expected bytes have been decoded, false otherwise.
     */
    public boolean isComplete() {
        return messageLength != -1
            && messageOffset >= messageLength
            && pendingBytes.size() == 0
            && !segmentHeaderRead
            && currentSegmentContentOffset == currentSegmentContentLength;
    }

    /**
     * Enriches an exception message with decoder offset information for debugging.
     *
     * @param message The original exception message.
     * @return The enriched message with offset information.
     */
    private String enrichExceptionMessage(String message) {
        return String.format("%s [decoderOffset=%d]", message, messageOffset);
    }
}
