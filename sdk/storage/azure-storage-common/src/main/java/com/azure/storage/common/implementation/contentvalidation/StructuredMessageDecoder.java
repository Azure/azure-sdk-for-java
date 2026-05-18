// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.contentvalidation;

import com.azure.core.util.logging.ClientLogger;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants.CRC64_LENGTH;
import static com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants.DEFAULT_MESSAGE_VERSION;
import static com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants.V1_HEADER_LENGTH;
import static com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants.V1_SEGMENT_HEADER_LENGTH;

/**
 * Streaming decoder for the storage <em>structured message</em> format used to validate downloaded blob/file/datalake
 * content with CRC64 checksums.
 *
 * <p>This class owns the actual parsing and CRC validation. The pipeline policy hands it raw {@link ByteBuffer}s as
 * they arrive on the wire (via {@link #decodeChunk(ByteBuffer)}); the decoder returns only the payload bytes that
 * have already been CRC-validated and tells the policy when the entire message has been consumed
 * (via {@link #isComplete()}). Any malformed input or CRC mismatch surfaces as an
 * {@link IllegalArgumentException} thrown from {@code decodeChunk} so the policy can translate it into a stream
 * error.</p>
 *
 * <h3>Wire format (V1)</h3>
 *
 * <p>The encoded body has the following layout (all integers little-endian):</p>
 * <pre>
 *   |-- message header (13 B) ----------------------------------------|
 *   |  version (1)  |  total message length (8)  |  flags (2)  |  numSegments (2)  |
 *
 *   for each segment in 1..numSegments:
 *     |-- segment header (10 B) -|
 *     |  segNum (2)  |  segContentLen (8)  |
 *     |-- segment payload (segContentLen B) --|
 *     |-- segment CRC64 footer (8 B; only if STORAGE_CRC64) --|
 *
 *   |-- message CRC64 footer (8 B; only if STORAGE_CRC64) --|
 * </pre>
 *
 * <h3>Emission guarantee</h3>
 *
 * Payload bytes for a segment are never emitted to the caller until that segment's CRC64 footer
 * has been validated. This matches the emission semantics used by {@code BlobDecryptionPolicy}/{@code DecryptorV2}
 * (which only emits a decrypted region after its GCM tag is verified) and ensures that no unvalidated bytes are
 * exposed to consumers, even if the connection is later torn down or the download is retried.
 *
 * <h3>Thread-safety</h3>
 *
 * <p>This class is not thread-safe. A new instance is created for every HTTP response, and the
 * reactive operators in the policy ({@code concatMap}) serialize access to the single instance. Retries produce new
 * HTTP responses and therefore new decoder instances, so a CRC failure on one attempt cannot pollute another.</p>
 */
public class StructuredMessageDecoder {
    private static final ClientLogger LOGGER = new ClientLogger(StructuredMessageDecoder.class);
    private static final int CRC64_SCRATCH_BUFFER_SIZE = 64 * 1024;

    /**
     * Largest segment payload that can be backed by a single Java {@code byte[]}. Segments larger than this are
     * rejected when the segment header is parsed.
     */
    private static final long MAX_SEGMENT_PAYLOAD_SIZE = (long) Integer.MAX_VALUE - 8;

    private long messageLength = -1;
    private StructuredMessageFlags flags;
    private int numSegments = -1;
    private final long expectedEncodedMessageLength;
    // Number of encoded bytes consumed so far (headers + payloads + footers).
    private long messageOffset = 0;
    private int currentSegmentNumber = 0;
    private long currentSegmentContentLength = 0;
    private long currentSegmentContentOffset = 0;
    private boolean segmentHeaderRead = false;
    // Running CRC64 over all payload bytes seen so far (across every segment).
    private long messageCrc64 = 0;
    // Running CRC64 over only the current segment's payload bytes.
    private long segmentCrc64 = 0;
    // Holds bytes left over from a previous decodeChunk() call when the current chunk did not contain a full
    // header or footer.
    private final ByteArrayOutputStream pendingBytes = new ByteArrayOutputStream();
    // Payload bytes for the current segment, copied incrementally from inbound wire buffers in chunk-sized arrays.
    // Unlike pre-allocating {@code new byte[(int) segmentSize]}, this bounds retained memory to bytes read so far
    // plus at most one live wire buffer (see {@link StorageContentValidationDecoderPolicy#decodeStream}).
    private List<byte[]> segmentPayloadChunks;
    private final byte[] crc64ScratchBuffer = new byte[CRC64_SCRATCH_BUFFER_SIZE];
    // When more than one segment validates in a single decodeChunk() call, the first segment is held here while
    // subsequent segments are queued in validatedOutput.
    private ByteBuffer deferredEmission;
    // Collects validated segment buffers when multiple segments complete in one decodeChunk() invocation.
    private final List<ByteBuffer> validatedOutput = new ArrayList<>();

    /**
     * Constructs a new StructuredMessageDecoder.
     *
     * @param expectedEncodedMessageLength The expected encoded structured-message length (typically HTTP
     * {@code Content-Length}).
     */
    public StructuredMessageDecoder(long expectedEncodedMessageLength) {
        this.expectedEncodedMessageLength = expectedEncodedMessageLength;
    }

    /**
     * Reads the 13-byte message header (version + total length + flags + numSegments) the first time the decoder
     * sees enough bytes, and validates each field. Subsequent calls are no-ops.
     *
     * @param buffer The buffer to read from.
     * @return true if the header was successfully read (or had already been read on a previous pass); false if more
     * bytes are still needed.
     */
    private boolean tryReadMessageHeader(ByteBuffer buffer) {
        if (messageLength != -1) {
            // Header already parsed on a previous chunk; nothing to do.
            return true;
        }

        if (getAvailableBytes(buffer) < V1_HEADER_LENGTH) {
            // Not enough bytes for the full header yet; carry over what we have.
            appendToPending(buffer);
            return false;
        }

        ByteBuffer combined = getCombinedBuffer(buffer);

        // Byte 0: protocol version.
        int messageVersion = Byte.toUnsignedInt(combined.get());
        if (messageVersion != DEFAULT_MESSAGE_VERSION) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                enrichExceptionMessage("Unsupported structured message version: " + messageVersion)));
        }

        // Bytes 1-8: total encoded message length. Must be at least the header itself, and must agree with what the
        // HTTP layer told us via Content-Length – any disagreement implies a truncated/extended response.
        long msgLen = combined.getLong();
        if (msgLen < V1_HEADER_LENGTH) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException(enrichExceptionMessage("Message length too small: " + msgLen)));
        }
        if (msgLen != expectedEncodedMessageLength) {
            throw LOGGER
                .logExceptionAsError(new IllegalArgumentException(enrichExceptionMessage("Structured message length "
                    + msgLen + " did not match content length " + expectedEncodedMessageLength)));
        }

        // Bytes 9-10: flags (NONE or STORAGE_CRC64). Bytes 11-12: number of segments.
        flags = StructuredMessageFlags.fromValue(Short.toUnsignedInt(combined.getShort()));
        numSegments = Short.toUnsignedInt(combined.getShort());
        if (numSegments < 1) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                enrichExceptionMessage("Structured message must have at least one segment, got: " + numSegments)));
        }

        // Commit: drop the 13 bytes we just parsed from pending/buffer and record the message length.
        consumeBytes(V1_HEADER_LENGTH, buffer);
        messageOffset += V1_HEADER_LENGTH;
        messageLength = msgLen;

        return true;
    }

    /**
     * Reads the 10-byte header for the next segment (segment number + segment payload length) and resets
     * per-segment state so {@link #tryReadSegmentContent(ByteBuffer)} can begin filling segment payload chunks.
     *
     * <p>Validates that segments arrive in order and that the declared segment size leaves enough room in the
     * remaining message for any subsequent segment headers, payloads, footers, and the trailing message footer –
     * this catches malformed/oversized segment lengths up front instead of waiting until we run off the end of the
     * stream.</p>
     *
     * @param buffer The buffer to read from.
     * @return true if the segment header was read; false if more bytes are needed.
     */
    private boolean tryReadSegmentHeader(ByteBuffer buffer) {
        if (getAvailableBytes(buffer) < V1_SEGMENT_HEADER_LENGTH) {
            appendToPending(buffer);
            return false;
        }

        ByteBuffer combined = getCombinedBuffer(buffer);

        // Bytes 0-1: segment number. Bytes 2-9: declared payload length of this segment.
        int segmentNum = Short.toUnsignedInt(combined.getShort());
        long segmentSize = combined.getLong();

        // Segments must arrive strictly in order so the running CRC and "segment N follows segment N-1" assumption
        // hold. Anything else implies a malformed/reordered response.
        if (segmentNum != currentSegmentNumber + 1) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(enrichExceptionMessage(
                "Unexpected segment number. Expected: " + (currentSegmentNumber + 1) + ", got: " + segmentNum)));
        }

        // Compute an upper bound on the legal segment size: whatever is left in the message, minus the bytes that
        // MUST still appear after this segment's payload (this segment's footer, the headers/payloads/footers of all
        // remaining segments, and the trailing message footer).
        long footerSize = flags == StructuredMessageFlags.STORAGE_CRC64 ? CRC64_LENGTH : 0;
        long remainingSegmentsAfterThis = (long) numSegments - segmentNum;
        long reservedBytes
            = footerSize + remainingSegmentsAfterThis * (V1_SEGMENT_HEADER_LENGTH + footerSize) + footerSize;
        long maxSegmentSize = messageLength - messageOffset - V1_SEGMENT_HEADER_LENGTH - reservedBytes;
        if (segmentSize < 0 || segmentSize > maxSegmentSize) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(enrichExceptionMessage(
                "Invalid segment size detected: " + segmentSize + " (max=" + maxSegmentSize + ")")));
        }
        if (segmentSize > MAX_SEGMENT_PAYLOAD_SIZE) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                enrichExceptionMessage("Segment size " + segmentSize + " exceeds the maximum supported segment size")));
        }

        // Commit: drop the 10 header bytes and set up per-segment state so payload accumulation can start fresh.
        consumeBytes(V1_SEGMENT_HEADER_LENGTH, buffer);
        messageOffset += V1_SEGMENT_HEADER_LENGTH;
        currentSegmentNumber = segmentNum;
        currentSegmentContentLength = segmentSize;
        currentSegmentContentOffset = 0;
        segmentPayloadChunks = new ArrayList<>();

        if (flags == StructuredMessageFlags.STORAGE_CRC64) {
            // Reset only the per-segment running CRC; the message-wide running CRC keeps accumulating across all
            // segments so the final message footer covers the entire payload.
            segmentCrc64 = 0;
        }

        return true;
    }

    /**
     * Pulls as many payload bytes as possible (bounded by what is still owed for the current segment) from the
     * pending+buffer view into {@link #segmentPayload}, updating the running per-segment and per-message CRC64
     * values along the way.
     *
     * <p>Bytes accumulated here are not yet emitted to the caller. They are released only after
     * {@link #tryReadSegmentFooter(ByteBuffer)} validates this segment's CRC. This is the mechanism that enforces
     * "no unvalidated bytes ever leave the decoder".</p>
     *
     * @param buffer The buffer to read from.
     * @return The number of payload bytes read in this call (0 means we either had no bytes available or the
     * current segment's payload was already complete).
     */
    private int tryReadSegmentContent(ByteBuffer buffer) {
        long remaining = currentSegmentContentLength - currentSegmentContentOffset;
        if (remaining == 0) {
            // Segment payload is already complete; nothing to do here. The caller will move on to read the footer.
            return 0;
        }

        int available = getAvailableBytes(buffer);
        if (available == 0) {
            return 0;
        }

        // Read the minimum of "what's available right now" and "what's still owed for this segment" so we never
        // accidentally consume the segment footer here.
        int toRead = (int) Math.min(available, remaining);
        byte[] chunk = new byte[toRead];

        if (pendingBytes.size() == 0) {
            if (flags == StructuredMessageFlags.STORAGE_CRC64) {
                updateCrc64sFromBuffer(buffer, toRead);
            }
            readIntoSegmentPayload(buffer, chunk, 0, toRead);
        } else {
            ByteBuffer combined = getCombinedBuffer(buffer);
            if (flags == StructuredMessageFlags.STORAGE_CRC64) {
                segmentCrc64 = StorageCrc64Calculator.compute(combined.array(),
                    combined.arrayOffset() + combined.position(), toRead, segmentCrc64);
                messageCrc64 = StorageCrc64Calculator.compute(combined.array(),
                    combined.arrayOffset() + combined.position(), toRead, messageCrc64);
            }
            combined.get(chunk);
            consumeBytes(toRead, buffer);
        }
        segmentPayloadChunks.add(chunk);

        messageOffset += toRead;
        currentSegmentContentOffset += toRead;

        return toRead;
    }

    /**
     * Validates the 8-byte segment CRC64 footer for the segment that has just finished accumulating. Pre-condition:
     * {@code currentSegmentContentOffset == currentSegmentContentLength}.
     *
     * <p>This step is intentionally separate from reading the message footer: when the CRC matches, we want to be
     * able to flush the buffered segment payload to the caller right away – even if the trailing message footer is
     * not yet available in the current chunk.</p>
     *
     * @param buffer The buffer to read from.
     * @return true if the footer was successfully read (or no footer is required for this message); false if more
     * bytes are still needed.
     */
    private boolean tryReadSegmentFooter(ByteBuffer buffer) {
        if (currentSegmentContentOffset != currentSegmentContentLength) {
            // Segment payload is not complete yet; wait for more content.
            return true;
        }

        if (flags == StructuredMessageFlags.STORAGE_CRC64) {
            return tryConsumeCrc64Footer(buffer, segmentCrc64, " in segment " + currentSegmentNumber);
        }

        // No CRC was negotiated, so there is no footer to read; the caller can release the buffered payload.
        return true;
    }

    /**
     * Validates the 8-byte message CRC64 footer that follows the last segment.
     *
     * @param buffer The buffer to read from.
     * @return true if the footer was successfully read (or none is required); false if more bytes are still needed.
     */
    private boolean tryReadMessageFooter(ByteBuffer buffer) {
        if (flags == StructuredMessageFlags.STORAGE_CRC64) {
            return tryConsumeCrc64Footer(buffer, messageCrc64, " in message footer");
        }
        return true;
    }

    /**
     * Decodes as much as possible from the given buffer and returns any fully validated
     * payload slices that are now safe to emit downstream.
     *
     * <p>The returned buffers will only ever contain bytes from segments whose CRC (when
     * enabled) has already been verified. If no segments have been fully
     * validated by this invocation the method returns an empty list. Callers distinguish "more bytes needed"
     * from "stream complete" via {@link #isComplete()}.</p>
     *
     * @param buffer The buffer containing encoded data.
     * @return Validated payload slices ready to emit downstream; empty when none are ready yet.
     * @throws IllegalArgumentException if the input is malformed or a CRC64 check fails.
     */
    public List<ByteBuffer> decodeChunk(ByteBuffer buffer) {
        // Decoder always reads little-endian; force the order on the caller's buffer so all our get() calls match
        // the wire format regardless of how the buffer was constructed.
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        validatedOutput.clear();
        deferredEmission = null;

        // Step 1: parse the message header on the first chunk that has enough bytes for it. If this chunk doesn't,
        // bail out early.
        if (!tryReadMessageHeader(buffer)) {
            return finishDecodeChunk();
        }

        // Step 2: walk forward through the message until we either hit the end (messageOffset == messageLength) or
        // we run out of bytes for the current structural element and have to wait for the next chunk.
        while (messageOffset < messageLength) {
            if (!segmentHeaderRead) {
                // We are between segments. If every segment has been processed, only the trailing message footer
                // can still appear in the stream – read it (or wait for it) and exit.
                if (currentSegmentNumber == numSegments) {
                    if (!tryReadMessageFooter(buffer)) {
                        break;
                    }
                    break;
                }
                // Otherwise, parse the next segment's header. May return false if it is split across chunks.
                if (!tryReadSegmentHeader(buffer)) {
                    break;
                }
                segmentHeaderRead = true;
            }

            // Drain as many payload bytes as are available into the per-segment buffer.
            int payloadRead = tryReadSegmentContent(buffer);

            if (currentSegmentContentOffset == currentSegmentContentLength) {
                // Segment payload fully buffered. Validate the CRC footer (if any). When the footer isn't fully
                // available yet, break and resume on the next chunk – segmentPayloadChunks keeps its contents so
                // we can still emit them on the call where the footer arrives.
                if (!tryReadSegmentFooter(buffer)) {
                    break;
                }
                releaseValidatedSegmentPayload();
                segmentHeaderRead = false;
                // Loop continues: either consume the next segment's header or the message footer.
            } else if (payloadRead == 0 && getAvailableBytes(buffer) == 0) {
                // Nothing left to read this pass and the segment is not complete – wait for the next chunk.
                break;
            }
        }

        return finishDecodeChunk();
    }

    /**
     * Hands off validated segment bytes for emission. A single validated segment per {@link #decodeChunk(ByteBuffer)}
     * is returned as one or more {@link ByteBuffer} views over the accumulated chunk arrays without consolidating
     * them; when multiple segments validate in one call, their views are queued in {@link #validatedOutput}.
     */
    private void releaseValidatedSegmentPayload() {
        List<byte[]> validatedChunks = segmentPayloadChunks;
        segmentPayloadChunks = null;

        if (deferredEmission == null && validatedOutput.isEmpty()) {
            if (validatedChunks.size() == 1) {
                deferredEmission = ByteBuffer.wrap(validatedChunks.get(0));
            } else {
                for (byte[] validatedChunk : validatedChunks) {
                    validatedOutput.add(ByteBuffer.wrap(validatedChunk));
                }
            }
        } else {
            if (deferredEmission != null) {
                validatedOutput.add(deferredEmission);
                deferredEmission = null;
            }
            for (byte[] validatedChunk : validatedChunks) {
                validatedOutput.add(ByteBuffer.wrap(validatedChunk));
            }
        }
    }

    private List<ByteBuffer> finishDecodeChunk() {
        if (deferredEmission != null && validatedOutput.isEmpty()) {
            return Collections.singletonList(deferredEmission);
        }
        if (validatedOutput.isEmpty()) {
            return Collections.emptyList();
        }
        if (deferredEmission != null) {
            List<ByteBuffer> combined = new ArrayList<>(validatedOutput.size() + 1);
            combined.add(deferredEmission);
            combined.addAll(validatedOutput);
            return combined;
        }
        return new ArrayList<>(validatedOutput);
    }

    /**
     * @return the total number of bytes the decoder can currently see across the carry-over {@link #pendingBytes}
     * plus the unread tail of the supplied buffer. Used to decide whether a structural element (header /
     * footer) can be parsed in this pass or whether we must defer to the next chunk.
     */
    private int getAvailableBytes(ByteBuffer buffer) {
        return pendingBytes.size() + buffer.remaining();
    }

    /**
     * Returns a single read-only view that logically concatenates {@link #pendingBytes} with the unread tail of
     * a buffer.
     *
     * <p>The position of the supplied buffer is intentionally not advanced here – reads happen on the
     * combined view, and the original buffer's position is moved later by {@link #consumeBytes(int, ByteBuffer)}
     * once we know the parse succeeded.</p>
     *
     * <p>When pendingBytes is empty we avoid the allocation and just return a duplicate of the buffer;
     * otherwise we materialize a fresh array of size {@code pending + buffer.remaining()}.</p>
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
     * Consumes the next 8 bytes as a little-endian CRC64 footer, validates it against expectedCrc64, and
     * advances {@link #messageOffset}. Used for both segment and message footers.
     *
     * <p>If fewer than 8 bytes are available, the remaining buffer bytes are stashed in {@link #pendingBytes} and
     * the method returns false so the caller can break out of the decode loop and wait for the next
     * chunk. On a CRC mismatch, an {@link IllegalArgumentException} is thrown (the decoder is then discarded by
     * the enclosing policy).</p>
     */
    private boolean tryConsumeCrc64Footer(ByteBuffer buffer, long expectedCrc64, String mismatchDetail) {
        if (getAvailableBytes(buffer) < CRC64_LENGTH) {
            // Not enough bytes yet for the footer; carry whatever we have over to the next call.
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

    /**
     * Drains {@code bytesToConsume} bytes from the logical pending+buffer stream that
     * {@link #getCombinedBuffer(ByteBuffer)} produced.
     *
     * <p>Bytes are taken from {@link #pendingBytes} first, then from the live buffer. The pending stream is
     * reset whenever it is fully drained, and any leftover (when {@code bytesToConsume} was less than what was in
     * pending) is rewritten so the carry-over stays compact.</p>
     */
    private void consumeBytes(int bytesToConsume, ByteBuffer buffer) {
        int pendingSize = pendingBytes.size();
        if (bytesToConsume <= pendingSize) {
            // The entire consume fits in pending: rewrite whatever survives back into pending after a reset.
            byte[] remaining = pendingBytes.toByteArray();
            pendingBytes.reset();
            if (bytesToConsume < pendingSize) {
                pendingBytes.write(remaining, bytesToConsume, pendingSize - bytesToConsume);
            }
        } else {
            // Pending is fully drained and the remainder comes from the live buffer; advance its position directly.
            int bytesFromBuffer = bytesToConsume - pendingSize;
            pendingBytes.reset();
            buffer.position(buffer.position() + bytesFromBuffer);
        }
    }

    /**
     * Stashes everything still unread in the buffer into {@link #pendingBytes} so it can be combined with the
     * next chunk on the next call to {@link #decodeChunk(ByteBuffer)}.
     *
     * <p>This is only called when the current chunk does not contain enough bytes for the next structural element,
     * so the carry-over is always small (bounded by the largest header size, currently 13 bytes).</p>
     */
    private void appendToPending(ByteBuffer buffer) {
        while (buffer.hasRemaining()) {
            pendingBytes.write(buffer.get());
        }
    }

    /**
     * Reports whether the decoder has finished consuming the entire structured message and validated everything it
     * was supposed to validate. Used by the pipeline policy to distinguish "stream ended cleanly" from "stream was
     * truncated".
     *
     * <p>The check requires all of:</p>
     * <ul>
     *   <li>The message header has been parsed ({@code messageLength != -1}).</li>
     *   <li>Every byte of the declared message has been consumed.</li>
     *   <li>No carry-over bytes remain in pending.</li>
     *   <li>No segment is currently in progress (no segment header without a matching footer).</li>
     *   <li>The current segment's payload accumulation is itself complete.</li>
     * </ul>
     *
     * @return true if all expected bytes have been decoded and validated; false otherwise.
     */
    public boolean isComplete() {
        return messageLength != -1
            && messageOffset == messageLength
            && pendingBytes.size() == 0
            && !segmentHeaderRead
            && currentSegmentContentOffset == currentSegmentContentLength
            && segmentPayloadChunks == null;
    }

    /**
     * Updates running CRC64 values from the next {@code readSize} bytes in {@code buffer}, mirroring
     * {@link StructuredMessageEncoder#encodeSegmentContent(ByteBuffer)}.
     */
    private void updateCrc64sFromBuffer(ByteBuffer buffer, int readSize) {
        if (buffer.hasArray()) {
            int pos = buffer.arrayOffset() + buffer.position();
            segmentCrc64 = StorageCrc64Calculator.compute(buffer.array(), pos, readSize, segmentCrc64);
            messageCrc64 = StorageCrc64Calculator.compute(buffer.array(), pos, readSize, messageCrc64);
        } else {
            updateCrc64sWithoutAccessibleArray(buffer, readSize);
        }
    }

    private void updateCrc64sWithoutAccessibleArray(ByteBuffer buffer, int readSize) {
        ByteBuffer duplicate = buffer.duplicate();
        duplicate.limit(duplicate.position() + readSize);

        long nextSegmentCrc64 = segmentCrc64;
        long nextMessageCrc64 = messageCrc64;

        while (duplicate.hasRemaining()) {
            int chunkSize = Math.min(duplicate.remaining(), crc64ScratchBuffer.length);
            duplicate.get(crc64ScratchBuffer, 0, chunkSize);
            nextSegmentCrc64 = StorageCrc64Calculator.compute(crc64ScratchBuffer, 0, chunkSize, nextSegmentCrc64);
            nextMessageCrc64 = StorageCrc64Calculator.compute(crc64ScratchBuffer, 0, chunkSize, nextMessageCrc64);
        }

        segmentCrc64 = nextSegmentCrc64;
        messageCrc64 = nextMessageCrc64;
    }

    private static void readIntoSegmentPayload(ByteBuffer src, byte[] dest, int destOffset, int length) {
        if (src.hasArray()) {
            int srcPos = src.position();
            System.arraycopy(src.array(), src.arrayOffset() + srcPos, dest, destOffset, length);
            src.position(srcPos + length);
        } else {
            src.get(dest, destOffset, length);
        }
    }

    /**
     * Appends the current decoder offset to an exception message so failures can be traced back to a specific
     * point in the encoded stream.
     *
     * @param message The original exception message.
     * @return The original message with {@code [decoderOffset=N]} appended.
     */
    private String enrichExceptionMessage(String message) {
        return String.format("%s [decoderOffset=%d]", message, messageOffset);
    }
}
