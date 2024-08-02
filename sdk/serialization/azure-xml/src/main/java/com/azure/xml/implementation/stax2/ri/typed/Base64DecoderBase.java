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

import com.azure.xml.implementation.stax2.typed.Base64Variant;
import com.azure.xml.implementation.stax2.ri.Stax2Util;

/**
 * Abstract base class used to share functionality between concrete
 * base64 decoders.
 *<p>
 * Mostly what follows is just shared definitions of the state machine
 * states to use, but there is also shared convenience functionality
 * for convenience decoding into simple byte arrays.
 */
abstract class Base64DecoderBase {
    // // // Constants for the simple state machine used for decoding

    /**
     * Initial state is where we start, and where white space
     * is accepted.
     */
    final static int STATE_INITIAL = 0;

    /**
     * State in which we have gotten one valid non-padding base64 encoded
     * character
     */
    final static int STATE_VALID_1 = 1;

    /**
     * State in which we have gotten two valid non-padding base64 encoded
     * characters.
     */
    final static int STATE_VALID_2 = 2;

    /**
     * State in which we have gotten three valid non-padding base64 encoded
     * characters.
     */
    final static int STATE_VALID_3 = 3;

    /**
     * State in which we have succesfully decoded a full triplet, but not
     * yet output any characters
     */
    final static int STATE_OUTPUT_3 = 4;

    /**
     * State in which we have 2 decoded bytes to output (either due to
     * partial triplet, or having output one byte from full triplet).
     */
    final static int STATE_OUTPUT_2 = 5;

    /**
     * State in which we have 1 decoded byte to output (either due to
     * partial triplet, or having output some of decoded bytes earlier)
     */
    final static int STATE_OUTPUT_1 = 6;

    /**
     * State in which we have gotten two valid non-padding base64 encoded
     * characters, followed by a single padding character. This means
     * that we must get one more padding character to be able to decode
     * the single encoded byte
     */
    final static int STATE_VALID_2_AND_PADDING = 7;

    // // // Character constants

    final static int INT_SPACE = 0x0020;

    // // // Base64 variant info

    /**
     * Details of base64 variant (alphabet in use, padding, line length)
     * are contained in and accessed via this object. It is passed
     * through init methods.
     */
    Base64Variant _variant;

    // // // Decoding State

    /**
     * State of the state machine
     */
    int _state = STATE_INITIAL;

    /**
     * Data decoded and/or ready to be output. Alignment and storage format
     * depend on state: during decoding things are appended from lowest
     * significant bits, and during output, flushed from more significant
     * bytes.
     */
    int _decodedData;

    // // // Reused state for convenience byte[] accessors

    Stax2Util.ByteAggregator _byteAggr = null;

    /*
    //////////////////////////////////////////////////////////////
    // Life-cycle
    //////////////////////////////////////////////////////////////
     */

    protected Base64DecoderBase() {
    }

    /*
    //////////////////////////////////////////////////////////////
    // Shared base API
    //////////////////////////////////////////////////////////////
     */

    /**
     * Method that does actual decoding
     */
    public abstract int decode(byte[] resultBuffer, int resultOffset, int maxLength) throws IllegalArgumentException;

    /**
     * Method that can be called to check if this decoder is in has unflushed
     * data ready to be returned.
     */
    public final boolean hasData() {
        return (_state >= STATE_OUTPUT_3) && (_state <= STATE_OUTPUT_1);
    }

    /**
     * Method called to indicate that we have no more encoded content to
     * process, and decoding is to finish. Depending base64 variant in
     * use, this means one of three things:
     *<ul>
     * <li>We are waiting for start of a new segment; no data to decode,
     *   ok to quit (returns 0)
     *  </li>
     * <li>We are half-way through decoding for padding variant (or,
     *   non-padding with just partial byte [single char]); error case.
     *  (returns -1)
     * <li>We are half-way through decoding for non-padding variant, and
     *    thereby have 1 or 2 bytes of data (which was not earlier recognized
     *    because of missing padding characters)
     *  (returns 1 or 2, number of bytes made available)
     *  </li>
     *</ul>
     */
    public final int endOfContent() {
        // If we are in a state where we don't have partial triplet, we are good to go
        if ((_state == STATE_INITIAL)
            || (_state == STATE_OUTPUT_3)
            || (_state == STATE_OUTPUT_2)
            || (_state == STATE_OUTPUT_1)) {
            return 0;
        }
        // Otherwise, only ok if no padding is used
        if (_variant.usesPadding()) {
            return -1;
        }

        // We do have 2 possible valid incomplete states
        if (_state == STATE_VALID_2) { // 2 chars -> 1 output byte
            // Got 12 bits, only need 8, need to shift
            _state = STATE_OUTPUT_1;
            _decodedData >>= 4;
            return 1;
        } else if (_state == STATE_VALID_3) { // 3 chars -> 2 output bytes
            // Got 18 bits, of which 16 data
            _decodedData >>= 2;
            _state = STATE_OUTPUT_2;
            return 2;
        } else { // other states either handled, or can not be valid terminal states (STATE_VALID1)
            return -1;
        }
    }

    /*
    //////////////////////////////////////////////////////////////
    // Convenience accessors
    //////////////////////////////////////////////////////////////
     */

    /**
     * Method that can be called to completely decode content that this
     * decoder has been initialized with.
     */
    public byte[] decodeCompletely() {
        Stax2Util.ByteAggregator aggr = getByteAggregator();
        byte[] buffer = aggr.startAggregation();
        while (true) {
            // Ok let's read full buffers each round
            int offset = 0;
            int len = buffer.length;

            do {
                int readCount = decode(buffer, offset, len);
                // note: can return 0; converted to -1 by front-end
                if (readCount < 1) { // all done!
                    // but we must be in a valid state too:
                    /* Just need to verify we don't have partial stuff
                     * (missing one to three characters of a full quartet
                     * that encodes 1 - 3 bytes). Also: non-padding
                     * variants can be in incomplete state, from which
                     * data may need to be flushed...
                     */
                    int left = endOfContent();
                    if (left < 0) { // incomplete, error
                        throw new IllegalArgumentException("Incomplete base64 triplet at the end of decoded content");
                    } else if (left > 0) { // 1 or 2 more bytes of data to add
                        continue;
                    }
                    return aggr.aggregateAll(buffer, offset);
                }
                offset += readCount;
                len -= readCount;
            } while (len > 0);

            // and if we got it, hand out results, get a new buffer
            buffer = aggr.addFullBlock(buffer);
        }
    }

    public Stax2Util.ByteAggregator getByteAggregator() {
        if (_byteAggr == null) {
            _byteAggr = new Stax2Util.ByteAggregator();
        }
        return _byteAggr;
    }

    /*
    //////////////////////////////////////////////////////////////
    // Internal helper methods error reporting
    //////////////////////////////////////////////////////////////
     */

    protected IllegalArgumentException reportInvalidChar(char ch, int bindex) throws IllegalArgumentException {
        return reportInvalidChar(ch, bindex, null);
    }

    /**
     * @param bindex Relative index within base64 character unit; between 0
     *   and 3 (as unit has exactly 4 characters)
     */
    protected IllegalArgumentException reportInvalidChar(char ch, int bindex, String msg)
        throws IllegalArgumentException {
        String base;
        if (ch <= INT_SPACE) {
            base = "Illegal white space character (code 0x" + Integer.toHexString(ch) + ") as character #"
                + (bindex + 1) + " of 4-char base64 unit: can only used between units";
        } else if (_variant.usesPaddingChar(ch)) {
            base = "Unexpected padding character ('" + _variant.getPaddingChar() + "') as character #" + (bindex + 1)
                + " of 4-char base64 unit: padding only legal as 3rd or 4th character";
        } else if (!Character.isDefined(ch) || Character.isISOControl(ch)) {
            // Not sure if we can really get here... ? (most illegal xml chars are caught at lower level)
            base = "Illegal character (code 0x" + Integer.toHexString(ch) + ") in base64 content";
        } else {
            base = "Illegal character '" + ch + "' (code 0x" + Integer.toHexString(ch) + ") in base64 content";
        }
        if (msg != null) {
            base = base + ": " + msg;
        }
        return new IllegalArgumentException(base);
    }
}
