//import java.nio.ByteBuffer;
//import java.nio.ByteOrder;
//import java.util.HashMap;
//import java.util.Map;
//
//public class StructuredMessageEncodeStream {
//    private static final int DEFAULT_MESSAGE_VERSION = 1;
//    private static final int DEFAULT_SEGMENT_SIZE = 1024;
//    private static final int CRC64_LENGTH = 8;
//    private static final int V1_HEADER_LENGTH = 13;
//    private static final int V1_SEGMENT_HEADER_LENGTH = 10;
//
//    private int messageVersion;
//    private int contentLength;
//    private int messageLength;
//    private StructuredMessageProperties flags;
//    private ByteBuffer innerBuffer;
//    private int segmentSize;
//    private int numSegments;
//    private int contentOffset;
//    private int currentSegmentNumber;
//    private SMRegion currentRegion;
//    private int currentRegionLength;
//    private int currentRegionOffset;
//    private int checksumOffset;
//    private long messageCrc64;
//    private Map<Integer, Long> segmentCrc64s;
//
//    public StructuredMessageEncodeStream(ByteBuffer innerBuffer, int contentLength, StructuredMessageProperties flags, int segmentSize) {
//        if (segmentSize < 1) {
//            throw new IllegalArgumentException("Segment size must be greater than 0.");
//        }
//        this.messageVersion = DEFAULT_MESSAGE_VERSION;
//        this.contentLength = contentLength;
//        this.flags = flags;
//        this.innerBuffer = innerBuffer;
//        this.segmentSize = segmentSize;
//        this.numSegments = (int) Math.ceil((double) this.contentLength / this.segmentSize);
//        this.messageLength = calculateMessageLength();
//        this.contentOffset = 0;
//        this.currentSegmentNumber = 0;
//        this.currentRegion = SMRegion.MESSAGE_HEADER;
//        this.currentRegionLength = V1_HEADER_LENGTH;
//        this.currentRegionOffset = 0;
//        this.checksumOffset = 0;
//        this.messageCrc64 = 0;
//        this.segmentCrc64s = new HashMap<>();
//    }
//
//    private int calculateMessageLength() {
//        int length = V1_HEADER_LENGTH;
//        length += (V1_SEGMENT_HEADER_LENGTH + CRC64_LENGTH) * this.numSegments;
//        length += this.contentLength;
//        length += CRC64_LENGTH;
//        return length;
//    }
//
//    private byte[] generateMessageHeader(int version, int size, StructuredMessageProperties flags, int numSegments) {
//        ByteBuffer buffer = ByteBuffer.allocate(13).order(ByteOrder.LITTLE_ENDIAN);
//        buffer.put((byte) version);
//        buffer.putLong(size);
//        buffer.putShort((short) flags.getValue());
//        buffer.putShort((short) numSegments);
//        return buffer.array();
//    }
//
//    private byte[] generateSegmentHeader(int number, int size) {
//        ByteBuffer buffer = ByteBuffer.allocate(10).order(ByteOrder.LITTLE_ENDIAN);
//        buffer.putShort((short) number);
//        buffer.putLong(size);
//        return buffer.array();
//    }
//
//    public int read(ByteBuffer outputBuffer, int size) {
//        if (size == 0) {
//            return 0;
//        }
//        if (size < 0) {
//            size = Integer.MAX_VALUE;
//        }
//        int count = 0;
//        while (count < size && tell() < this.messageLength) {
//            int remaining = size - count;
//            if (this.currentRegion == SMRegion.MESSAGE_HEADER || this.currentRegion == SMRegion.SEGMENT_HEADER || this.currentRegion == SMRegion.SEGMENT_FOOTER || this.currentRegion == SMRegion.MESSAGE_FOOTER) {
//                count += readMetadataRegion(this.currentRegion, remaining, outputBuffer);
//            } else if (this.currentRegion == SMRegion.SEGMENT_CONTENT) {
//                count += readContent(remaining, outputBuffer);
//            } else {
//                throw new IllegalStateException("Invalid SMRegion " + this.currentRegion);
//            }
//        }
//        return count;
//    }
//
//    private int readMetadataRegion(SMRegion region, int size, ByteBuffer outputBuffer) {
//        byte[] metadata = getMetadataRegion(region);
//        int readSize = Math.min(size, this.currentRegionLength - this.currentRegionOffset);
//        outputBuffer.put(metadata, this.currentRegionOffset, readSize);
//        this.currentRegionOffset += readSize;
//        if (this.currentRegionOffset == this.currentRegionLength && this.currentRegion != SMRegion.MESSAGE_FOOTER) {
//            advanceRegion(region);
//        }
//        return readSize;
//    }
//
//    private byte[] getMetadataRegion(SMRegion region) {
//        if (region == SMRegion.MESSAGE_HEADER) {
//            return generateMessageHeader(this.messageVersion, this.messageLength, this.flags, this.numSegments);
//        } else if (region == SMRegion.SEGMENT_HEADER) {
//            int segmentSize = Math.min(this.segmentSize, this.contentLength - this.contentOffset);
//            return generateSegmentHeader(this.currentSegmentNumber, segmentSize);
//        } else if (region == SMRegion.SEGMENT_FOOTER) {
//            if (this.flags.hasCrc64()) {
//                return ByteBuffer.allocate(CRC64_LENGTH).order(ByteOrder.LITTLE_ENDIAN).putLong(this.segmentCrc64s.get(this.currentSegmentNumber)).array();
//            }
//            return new byte[0];
//        } else if (region == SMRegion.MESSAGE_FOOTER) {
//            if (this.flags.hasCrc64()) {
//                return ByteBuffer.allocate(CRC64_LENGTH).order(ByteOrder.LITTLE_ENDIAN).putLong(this.messageCrc64).array();
//            }
//            return new byte[0];
//        } else {
//            throw new IllegalStateException("Invalid metadata SMRegion " + this.currentRegion);
//        }
//    }
//
//    private void advanceRegion(SMRegion current) {
//        this.currentRegionOffset = 0;
//        if (current == SMRegion.MESSAGE_HEADER) {
//            this.currentRegion = SMRegion.SEGMENT_HEADER;
//            incrementCurrentSegment();
//        } else if (current == SMRegion.SEGMENT_HEADER) {
//            this.currentRegion = SMRegion.SEGMENT_CONTENT;
//        } else if (current == SMRegion.SEGMENT_CONTENT) {
//            this.currentRegion = SMRegion.SEGMENT_FOOTER;
//        } else if (current == SMRegion.SEGMENT_FOOTER) {
//            if (this.contentOffset == this.contentLength) {
//                this.currentRegion = SMRegion.MESSAGE_FOOTER;
//            } else {
//                this.currentRegion = SMRegion.SEGMENT_HEADER;
//                incrementCurrentSegment();
//            }
//        } else {
//            throw new IllegalStateException("Invalid SMRegion " + this.currentRegion);
//        }
//        updateCurrentRegionLength();
//    }
//
//    private void incrementCurrentSegment() {
//        this.currentSegmentNumber++;
//        if (this.flags.hasCrc64()) {
//            this.segmentCrc64s.putIfAbsent(this.currentSegmentNumber, 0L);
//        }
//    }
//
//    private void updateCurrentRegionLength() {
//        if (this.currentRegion == SMRegion.MESSAGE_HEADER) {
//            this.currentRegionLength = V1_HEADER_LENGTH;
//        } else if (this.currentRegion == SMRegion.SEGMENT_HEADER) {
//            this.currentRegionLength = V1_SEGMENT_HEADER_LENGTH;
//        } else if (this.currentRegion == SMRegion.SEGMENT_CONTENT) {
//            if (this.currentSegmentNumber == this.numSegments) {
//                this.currentRegionLength = this.contentLength - ((this.currentSegmentNumber - 1) * this.segmentSize);
//            } else {
//                this.currentRegionLength = this.segmentSize;
//            }
//        } else if (this.currentRegion == SMRegion.SEGMENT_FOOTER) {
//            this.currentRegionLength = CRC64_LENGTH;
//        } else if (this.currentRegion == SMRegion.MESSAGE_FOOTER) {
//            this.currentRegionLength = CRC64_LENGTH;
//        } else {
//            throw new IllegalStateException("Invalid SMRegion " + this.currentRegion);
//        }
//    }
//
//    private int readContent(int size, ByteBuffer outputBuffer) {
//        int readSize = Math.min(size, this.currentRegionLength - this.currentRegionOffset);
//        byte[] content = new byte[readSize];
//        this.innerBuffer.get(content, 0, readSize);
//        outputBuffer.put(content);
//        if (this.flags.hasCrc64()) {
//            this.segmentCrc64s.put(this.currentSegmentNumber, calculateCrc64(content, this.segmentCrc64s.get(this.currentSegmentNumber)));
//            this.messageCrc64 = calculateCrc64(content, this.messageCrc64);
//        }
//        this.contentOffset += readSize;
//        this.currentRegionOffset += readSize;
//        if (this.currentRegionOffset == this.currentRegionLength) {
//            advanceRegion(SMRegion.SEGMENT_CONTENT);
//        }
//        return readSize;
//    }
//
//    private long calculateCrc64(byte[] data, long crc) {
//        // Implement CRC64 calculation here
//        return crc;
//    }
//
//    public int tell() {
//        if (this.currentRegion == SMRegion.MESSAGE_HEADER) {
//            return this.currentRegionOffset;
//        } else if (this.currentRegion == SMRegion.SEGMENT_HEADER) {
//            return V1_HEADER_LENGTH + this.contentOffset + (this.currentSegmentNumber - 1) * (V1_SEGMENT_HEADER_LENGTH + CRC64_LENGTH) + this.currentRegionOffset;
//        } else if (this.currentRegion == SMRegion.SEGMENT_CONTENT) {
//            return V1_HEADER_LENGTH + this.contentOffset + (this.currentSegmentNumber - 1) * (V1_SEGMENT_HEADER_LENGTH + CRC64_LENGTH) + V1_SEGMENT_HEADER_LENGTH;
//        } else if (this.currentRegion == SMRegion.SEGMENT_FOOTER) {
//            return V1_HEADER_LENGTH + this.contentOffset + (this.currentSegmentNumber - 1) * (V1_SEGMENT_HEADER_LENGTH + CRC64_LENGTH) + V1_SEGMENT_HEADER_LENGTH + this.currentRegionOffset;
//        } else if (this.currentRegion == SMRegion.MESSAGE_FOOTER) {
//            return V1_HEADER_LENGTH + this.contentOffset + this.currentSegmentNumber * (V1_SEGMENT_HEADER_LENGTH + CRC64_LENGTH) + this.currentRegionOffset;
//        } else {
//            throw new IllegalStateException("Invalid SMRegion " + this.currentRegion);
//        }
//    }
//
//    public void close() {
//        // No specific close operation needed for ByteBuffer
//    }
//
//    public boolean isReadable() {
//        return true;
//    }
//
//    public boolean isSeekable() {
//        return true;
//    }
//
//    public int seek(int offset, int whence) {
//        int position;
//        if (whence == 0) { // SEEK_SET
//            position = offset;
//        } else if (whence == 1) { // SEEK_CUR
//            position = tell() + offset;
//        } else if (whence == 2) { // SEEK_END
//            position = this.messageLength + offset;
//        } else {
//            throw new IllegalArgumentException("Invalid value for whence: " + whence);
//        }
//
//        if (position > tell()) {
//            throw new UnsupportedOperationException("This stream only supports seeking backwards.");
//        }
//
//        if (position < V1_HEADER_LENGTH) {
//            this.currentRegion = SMRegion.MESSAGE_HEADER;
//            this.currentRegionOffset = position;
//            this.contentOffset = 0;
//            this.currentSegmentNumber = 0;
//        } else if (position >= this.messageLength - CRC64_LENGTH) {
//            this.currentRegion = SMRegion.MESSAGE_FOOTER;
//            this.currentRegionOffset = position - (this.messageLength - CRC64_LENGTH);
//            this.contentOffset = this.contentLength;
//            this.currentSegmentNumber = this.numSegments;
//        } else {
//            int fullSegmentSize = V1_SEGMENT_HEADER_LENGTH + this.segmentSize + CRC64_LENGTH;
//            int newSegmentNum = 1 + (position - V1_HEADER_LENGTH) / fullSegmentSize;
//            int segmentPos = (position - V1_HEADER_LENGTH) % fullSegmentSize;
//            int previousSegmentsTotalContentSize = (newSegmentNum - 1) * this.segmentSize;
//            int newSegmentSize = this.segmentSize;
//            if (newSegmentNum == this.numSegments) {
//                newSegmentSize = this.contentLength - previousSegmentsTotalContentSize;
//            }
//
//            if (segmentPos < V1_SEGMENT_HEADER_LENGTH) {
//                this.currentRegion = SMRegion.SEGMENT_HEADER;
//                this.currentRegionOffset = segmentPos;
//                this.contentOffset = previousSegmentsTotalContentSize;
//            } else if (segmentPos < V1_SEGMENT_HEADER_LENGTH + newSegmentSize) {
//                this.currentRegion = SMRegion.SEGMENT_CONTENT;
//                this.currentRegionOffset = segmentPos - V1_SEGMENT_HEADER_LENGTH;
//                this.contentOffset = previousSegmentsTotalContentSize + this.currentRegionOffset;
//            } else {
//                this.currentRegion = SMRegion.SEGMENT_FOOTER;
//                this.currentRegionOffset = segmentPos - V1_SEGMENT_HEADER_LENGTH - newSegmentSize;
//                this.contentOffset = previousSegmentsTotalContentSize + newSegmentSize;
//            }
//            this.currentSegmentNumber = newSegmentNum;
//        }
//        updateCurrentRegionLength();
//        this.innerBuffer.position(this.contentOffset);
//        return position;
//    }
//
//    ----------------------------
//
//    public static ByteBuffer buildStructuredMessage(
//        byte[] data,
//        Object segmentSize, // Can be Integer or List<Integer>
//        int flags,
//        Optional<Integer> invalidateCrcSegment) throws IOException {
//
//        int segmentCount;
//        if (segmentSize instanceof List) {
//            segmentCount = ((List<?>) segmentSize).size();
//        } else {
//            segmentCount = (int) Math.ceil((double) data.length / (int) segmentSize);
//        }
//
//        int segmentFooterLength = (flags & StructuredMessageProperties.CRC64) != 0 ? StructuredMessageConstants.CRC64_LENGTH : 0;
//
//        int messageLength = StructuredMessageConstants.V1_HEADER_LENGTH +
//            ((StructuredMessageConstants.V1_SEGMENT_HEADER_LENGTH + segmentFooterLength) * segmentCount) +
//            data.length +
//            ((flags & StructuredMessageProperties.CRC64) != 0 ? StructuredMessageConstants.CRC64_LENGTH : 0);
//
//        ByteArrayOutputStream message = new ByteArrayOutputStream();
//        long messageCrc = 0;
//
//        // Message Header
//        message.write(0x01); // Version
//        message.write(ByteBuffer.allocate(8).putLong(messageLength).array()); // Message length
//        message.write(ByteBuffer.allocate(2).putShort((short) flags).array()); // Flags
//        message.write(ByteBuffer.allocate(2).putShort((short) segmentCount).array()); // Num segments
//
//        // Special case for 0 length content
//        if (data.length == 0) {
//            long crc = (flags & StructuredMessageProperties.CRC64) != 0 ? 0 : -1;
//            writeSegment(1, data, crc, message);
//        } else {
//            // Segments
//            int[] segmentSizes = segmentSize instanceof List ? ((List<Integer>) segmentSize).stream().mapToInt(i -> i).toArray() : new int[segmentCount];
//            if (!(segmentSize instanceof List)) {
//                int size = (int) segmentSize;
//                for (int i = 0; i < segmentCount; i++) {
//                    segmentSizes[i] = size;
//                }
//            }
//
//            int offset = 0;
//            for (int i = 1; i <= segmentCount; i++) {
//                int size = segmentSizes[i - 1];
//                byte[] segmentData = new byte[size];
//                System.arraycopy(data, offset, segmentData, 0, size);
//                offset += size;
//
//                long segmentCrc = -1;
//                if ((flags & StructuredMessageProperties.CRC64) != 0) {
//                    segmentCrc = computeCrc64(segmentData, 0);
//                    if (i == invalidateCrcSegment.orElse(-1)) {
//                        segmentCrc += 5;
//                    }
//                }
//                writeSegment(i, segmentData, segmentCrc, message);
//
//                messageCrc = computeCrc64(segmentData, messageCrc);
//            }
//        }
//
//        // Message footer
//        if ((flags & StructuredMessageProperties.CRC64) != 0) {
//            if (invalidateCrcSegment.orElse(-1) == -1) {
//                messageCrc += 5;
//            }
//            message.write(ByteBuffer.allocate(StructuredMessageConstants.CRC64_LENGTH).putLong(messageCrc).array());
//        }
//
//        return ByteBuffer.wrap(message.toByteArray());
//    }
//
//    private static void writeSegment(int segmentNumber, byte[] data, long crc, ByteArrayOutputStream message) throws IOException {
//        // Implement the segment writing logic here
//        // This is a placeholder implementation
//        message.write(ByteBuffer.allocate(4).putInt(segmentNumber).array());
//        message.write(data);
//        if (crc != -1) {
//            message.write(ByteBuffer.allocate(8).putLong(crc).array());
//        }
//    }
//
//    private static long computeCrc64(byte[] data, long initialCrc) {
//        // Implement the CRC64 computation here
//        // This is a placeholder implementation
//        long crc = initialCrc;
//        for (byte b : data) {
//            crc += b; // Simplified example, replace with actual CRC64 computation
//        }
//        return crc;
//    }
//}
//}
//
