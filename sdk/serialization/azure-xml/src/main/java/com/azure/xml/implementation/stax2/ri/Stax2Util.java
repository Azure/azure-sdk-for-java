// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
/* Stax2 API extension for Streaming Api for Xml processing (StAX).
 *
 * Copyright (c) 2006- Tatu Saloranta, tatu.saloranta@iki.fi
 *
 * Licensed under the License specified in the file LICENSE which is
 * included with the source code.
 * You may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.azure.xml.implementation.stax2.ri;

import javax.xml.stream.XMLStreamConstants;

public final class Stax2Util implements XMLStreamConstants {
    private Stax2Util() {
    } // no instantiation

    /**
     * Method that converts given standard Stax event type into
     * textual representation.
     */
    public static String eventTypeDesc(int type) {
        switch (type) {
            case START_ELEMENT:
                return "START_ELEMENT";

            case END_ELEMENT:
                return "END_ELEMENT";

            case START_DOCUMENT:
                return "START_DOCUMENT";

            case END_DOCUMENT:
                return "END_DOCUMENT";

            case CHARACTERS:
                return "CHARACTERS";

            case CDATA:
                return "CDATA";

            case SPACE:
                return "SPACE";

            case COMMENT:
                return "COMMENT";

            case PROCESSING_INSTRUCTION:
                return "PROCESSING_INSTRUCTION";

            case DTD:
                return "DTD";

            case ENTITY_REFERENCE:
                return "ENTITY_REFERENCE";
        }
        return "[" + type + "]";
    }

    /**
     * Method called to trim leading and/or trailing space that given
     * lexical value has.
     *
     * @return Trimmed value if <code>lexical</code> had at least one
     *   non-space character; null otherwise
     */
    public static String trimSpaces(String lexical) {
        int end = lexical.length();
        int start = 0;

        while (true) {
            if (start >= end) {
                return null;
            }
            if (!_isSpace(lexical.charAt(start))) {
                break;
            }
            ++start;
        }
        // No trailing space? Either original String as is, or just trim leading
        --end;
        if (!_isSpace(lexical.charAt(end))) {
            return (start == 0) ? lexical : lexical.substring(start);
        }

        // Otherwise, at least some trailing ws...
        while (--end > start && _isSpace(lexical.charAt(end))) {
        }

        return lexical.substring(start, end + 1);
    }

    /**
     *<p>
     * Note that it is assumed that any "weird" white space
     * (xml 1.1 LSEP and NEL) have been replaced by canonical
     * alternatives (linefeed for element content, regular space
     * for attributes)
     */
    private static boolean _isSpace(char c) {
        return ((int) c) <= 0x0020;
    }

    /**
     * Helper class used to simplify text gathering while keeping
     * at as efficient as possible.
     */
    public final static class TextBuffer {
        private String mText = null;

        /* !!! JDK 1.5: when we can upgrade to Java 5, can convert
         *  to using <code>StringBuilder</code> instead.
         */
        private StringBuffer mBuilder = null;

        public TextBuffer() {
        }

        public void reset() {
            mText = null;
            mBuilder = null;
        }

        public void append(String text) {
            int len = text.length();
            if (len > 0) {
                // Any prior text?
                if (mText != null) {
                    mBuilder = new StringBuffer(mText.length() + len);
                    mBuilder.append(mText);
                    mText = null;
                }
                if (mBuilder != null) {
                    mBuilder.append(text);
                } else {
                    mText = text;
                }
            }
        }

        public String get() {
            if (mText != null) {
                return mText;
            }
            if (mBuilder != null) {
                return mBuilder.toString();
            }
            return "";
        }

        public boolean isEmpty() {
            return (mText == null) && (mBuilder == null);
        }
    }

    /**
     * Helper class for efficiently reading and aggregating variable length
     * byte content.
     */
    public final static class ByteAggregator {
        private final static byte[] NO_BYTES = new byte[0];

        /**
         * Size of the first block we will allocate.
         */
        private final static int INITIAL_BLOCK_SIZE = 500;

        /**
         * Maximum block size we will use for individual non-aggregated
         * blocks. Let's limit to using 256k chunks.
         */
        //private final static int MAX_BLOCK_SIZE = (1 << 18);

        final static int DEFAULT_BLOCK_ARRAY_SIZE = 100;

        private byte[][] mBlocks;

        private int mBlockCount;

        private int mTotalLen;

        /**
         * Reusable byte buffer block; we retain biggest one from
         * {@link #mBlocks} after aggregation.
         */
        private byte[] mSpareBlock;

        public ByteAggregator() {
        }

        /**
         * Method called to initialize aggregation process.
         *
         * @return Block that can be used to read in content
         */
        public byte[] startAggregation() {
            mTotalLen = 0;
            mBlockCount = 0;
            byte[] result = mSpareBlock;
            if (result == null) {
                result = new byte[INITIAL_BLOCK_SIZE];
            } else {
                mSpareBlock = null;
            }
            return result;
        }

        /**
         * Method used to add bufferful of data to the aggregator, and
         * get another buffer to read more data into. Returned buffer
         * is generally as big as or bigger than the given buffer, to try
         * to improve performance for larger aggregations.
         *
         * @return Buffer in which to read additional data
         */
        public byte[] addFullBlock(byte[] block) {
            int blockLen = block.length;

            if (mBlocks == null) {
                mBlocks = new byte[DEFAULT_BLOCK_ARRAY_SIZE][];
            } else {
                int oldLen = mBlocks.length;
                if (mBlockCount >= oldLen) {
                    byte[][] old = mBlocks;
                    mBlocks = new byte[oldLen + oldLen][];
                    System.arraycopy(old, 0, mBlocks, 0, oldLen);
                }
            }
            mBlocks[mBlockCount] = block;
            ++mBlockCount;
            mTotalLen += blockLen;

            /* Let's allocate block that's half the total size, except
             * never smaller than twice the initial block size.
             * The idea is just to grow with reasonable rate, to optimize
             * between minimal number of chunks and minimal amount of
             * wasted space.
             */
            int newSize = Math.max((mTotalLen >> 1), (INITIAL_BLOCK_SIZE + INITIAL_BLOCK_SIZE));
            return new byte[newSize];
        }

        /**
         * Method called when results are finalized and we can get the
         * full aggregated result buffer to return to the caller
         */
        public byte[] aggregateAll(byte[] lastBlock, int lastLen) {
            int totalLen = mTotalLen + lastLen;

            if (totalLen == 0) { // quick check: nothing aggregated?
                return NO_BYTES;
            }

            byte[] result = new byte[totalLen];
            int offset = 0;

            if (mBlocks != null) {
                for (int i = 0; i < mBlockCount; ++i) {
                    byte[] block = mBlocks[i];
                    int len = block.length;
                    System.arraycopy(block, 0, result, offset, len);
                    offset += len;
                }
            }
            System.arraycopy(lastBlock, 0, result, offset, lastLen);
            // can reuse the last block: should be the biggest one we've handed
            mSpareBlock = lastBlock;
            offset += lastLen;
            if (offset != totalLen) { // just a sanity check
                throw new RuntimeException(
                    "Internal error: total len assumed to be " + totalLen + ", copied " + offset + " bytes");
            }
            return result;
        }
    }
}
