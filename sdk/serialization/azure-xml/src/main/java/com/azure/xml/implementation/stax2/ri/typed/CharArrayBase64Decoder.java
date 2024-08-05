// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
/* StAX2 extension for StAX API (JSR-173).
 *
 * Copyright (c) 2005- Tatu Saloranta, tatu.saloranta@iki.fi
 *
 * Licensed under the License specified in file LICENSE, included with
 * the source code.
 * You may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.azure.xml.implementation.stax2.ri.typed;

import java.util.*;

import com.azure.xml.implementation.stax2.typed.Base64Variant;

/**
 * Base64 decoder that can be used to decode base64 encoded content that
 * is passed as char arrays.
 */
@SuppressWarnings("fallthrough")
public class CharArrayBase64Decoder extends Base64DecoderBase {
    // // // Input buffer information

    /**
     * Text segment being currently processed.
     */
    protected char[] _currSegment;

    protected int _currSegmentPtr;
    protected int _currSegmentEnd;

    protected final ArrayList<char[]> _nextSegments = new ArrayList<>();

    protected int _lastSegmentOffset;
    protected int _lastSegmentEnd;

    /**
     * Pointer of the next segment to process (after current one stored
     * in {@link #_currSegment}) within {@link #_nextSegments}.
     */
    protected int _nextSegmentIndex;

    public CharArrayBase64Decoder() {
        super();
    }

    public void init(Base64Variant variant, boolean firstChunk, char[] lastSegment, int lastOffset, int lastLen,
        List<char[]> segments) {
        _variant = variant;
        /* Leftovers only cleared if it is the first chunk (i.e.
         * right after START_ELEMENT)
         */
        if (firstChunk) {
            _state = STATE_INITIAL;
        }
        _nextSegments.clear();
        if (segments == null || segments.isEmpty()) { // no segments, simple
            _currSegment = lastSegment;
            _currSegmentPtr = lastOffset;
            _currSegmentEnd = lastOffset + lastLen;
        } else {
            if (lastSegment == null) { // sanity check
                throw new IllegalArgumentException();
            }

            Iterator<char[]> it = segments.iterator();
            _currSegment = it.next();
            _currSegmentPtr = 0;
            _currSegmentEnd = _currSegment.length;

            while (it.hasNext()) {
                _nextSegments.add(it.next());
            }
            _nextSegmentIndex = 0;

            // Plus, still need to add the last segment
            _nextSegments.add(lastSegment);
            _lastSegmentOffset = lastOffset;
            _lastSegmentEnd = lastOffset + lastLen;
        }
    }

    /**
     * @param resultBuffer Buffer in which decoded bytes are returned
     * @param resultOffset Offset that points to position to put the
     *   first decoded byte in maxLength Maximum number of bytes that can be returned
     *   in given buffer
     *
     * @return Number of bytes decoded and returned in the result buffer
     */
    @Override
    public int decode(byte[] resultBuffer, int resultOffset, int maxLength) throws IllegalArgumentException {
        final int origResultOffset = resultOffset;
        final int resultBufferEnd = resultOffset + maxLength;

        main_loop: while (true) {
            switch (_state) {
                case STATE_INITIAL:
                // first, we'll skip preceding white space, if any
                {
                    char ch;
                    do {
                        if (_currSegmentPtr >= _currSegmentEnd) {
                            if (!nextSegment()) {
                                break main_loop;
                            }
                        }
                        ch = _currSegment[_currSegmentPtr++];
                    } while (ch <= INT_SPACE);
                    int bits = _variant.decodeBase64Char(ch);
                    if (bits < 0) {
                        throw reportInvalidChar(ch, 0);
                    }
                    _decodedData = bits;
                }
                // fall through, "fast" path

                case STATE_VALID_1:
                // then second base64 char; can't get padding yet, nor ws
                {
                    if (_currSegmentPtr >= _currSegmentEnd) {
                        if (!nextSegment()) {
                            _state = STATE_VALID_1; // to cover fall-through case
                            break main_loop;
                        }
                    }
                    char ch = _currSegment[_currSegmentPtr++];
                    int bits = _variant.decodeBase64Char(ch);
                    if (bits < 0) {
                        throw reportInvalidChar(ch, 1);
                    }
                    _decodedData = (_decodedData << 6) | bits;
                }
                // fall through, "fast path"

                case STATE_VALID_2:
                // third base64 char; can be padding, but not ws
                {
                    if (_currSegmentPtr >= _currSegmentEnd) {
                        if (!nextSegment()) {
                            _state = STATE_VALID_2; // to cover fall-through case
                            break main_loop;
                        }
                    }
                    char ch = _currSegment[_currSegmentPtr++];
                    int bits = _variant.decodeBase64Char(ch);
                    if (bits < 0) {
                        if (bits != Base64Variant.BASE64_VALUE_PADDING) {
                            throw reportInvalidChar(ch, 2);
                        }
                        // Padding is off the "fast path", so:
                        _state = STATE_VALID_2_AND_PADDING;
                        continue;
                    }
                    _decodedData = (_decodedData << 6) | bits;
                }
                // fall through, "fast path"

                case STATE_VALID_3:
                // fourth and last base64 char; can be padding, but not ws
                {
                    if (_currSegmentPtr >= _currSegmentEnd) {
                        if (!nextSegment()) {
                            _state = STATE_VALID_3; // to cover fall-through case
                            break main_loop;
                        }
                    }
                    char ch = _currSegment[_currSegmentPtr++];
                    int bits = _variant.decodeBase64Char(ch);
                    if (bits < 0) {
                        if (bits != Base64Variant.BASE64_VALUE_PADDING) {
                            throw reportInvalidChar(ch, 3);
                        }
                        /* With padding we only get 2 bytes; but we have
                         * to shift it a bit so it is identical to triplet
                         * case with partial output.
                         * 3 chars gives 3x6 == 18 bits, of which 2 are
                         * dummies, need to discard:
                         */
                        _decodedData >>= 2;
                        _state = STATE_OUTPUT_2;
                        continue;
                    }
                    // otherwise, our triple is now complete
                    _decodedData = (_decodedData << 6) | bits;
                }
                // still along fast path

                case STATE_OUTPUT_3:
                    if (resultOffset >= resultBufferEnd) { // no room
                        _state = STATE_OUTPUT_3;
                        break main_loop;
                    }
                    resultBuffer[resultOffset++] = (byte) (_decodedData >> 16);
                    // fall through

                case STATE_OUTPUT_2:
                    if (resultOffset >= resultBufferEnd) { // no room
                        _state = STATE_OUTPUT_2;
                        break main_loop;
                    }
                    resultBuffer[resultOffset++] = (byte) (_decodedData >> 8);
                    // fall through

                case STATE_OUTPUT_1:
                    if (resultOffset >= resultBufferEnd) { // no room
                        _state = STATE_OUTPUT_1;
                        break main_loop;
                    }
                    resultBuffer[resultOffset++] = (byte) _decodedData;
                    _state = STATE_INITIAL;
                    continue;

                case STATE_VALID_2_AND_PADDING: {
                    if (_currSegmentPtr >= _currSegmentEnd) {
                        if (!nextSegment()) {
                            // must have valid state already (can't get in via fall-through)
                            break main_loop;
                        }
                    }
                    char ch = _currSegment[_currSegmentPtr++];
                    if (!_variant.usesPaddingChar(ch)) {
                        throw reportInvalidChar(ch, 3,
                            "expected padding character '" + _variant.getPaddingChar() + "'");
                    }
                    // Got 12 bits, only need 8, need to shift
                    _state = STATE_OUTPUT_1;
                    _decodedData >>= 4;
                }
                    continue;

                default:
                    // sanity check: should never happen
                    throw new IllegalStateException("Illegal internal state " + _state);
            }
        }
        return resultOffset - origResultOffset;
    }

    /*
    /**********************************************************************
    /* Internal helper methods for input access
    /**********************************************************************
     */

    private boolean nextSegment() {
        if (_nextSegmentIndex < _nextSegments.size()) {
            _currSegment = _nextSegments.get(_nextSegmentIndex++);
            // last segment may have non-zero ptr, slack at end
            if (_nextSegmentIndex == _nextSegments.size()) {
                _currSegmentPtr = _lastSegmentOffset;
                _currSegmentEnd = _lastSegmentEnd;
            } else {
                _currSegmentPtr = 0;
                _currSegmentEnd = _currSegment.length;
            }
            return true;
        }
        return false;
    }
}
