// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
/* Reference Implementation of
 * Stax2 extension API (for basic Stax API, JSR-173)
 *
 * Copyright (c) 2008- Tatu Saloranta, tatu.saloranta@iki.fi
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

/**
 * This base class defines interface used for efficient encoding
 * of typed values, by stream writers. The abstraction is necessary
 * to reduce amount of duplicated code while avoiding significant
 * additional overhead. The idea is that the low-level stream
 * writer backend supplies encoder with the result buffer, while
 * encoder itself knows the data and state. Together these allow
 * for efficient serialization with light coupling.
 *<p>
 * General contract for encoding is that caller must call things
 * in following sequence:
 * <ol>
 *  <li>First, {@link #bufferNeedsFlush} is called once; and
 *   if indicated by return value of true, caller must flush
 *   its buffer so it is completely empty (buffer also must have
 *   size of at at least <code>MIN_CHARS_WITHOUT_FLUSH</code>)
 *   </li>
 *  <li>Then, one of {@link #encodeMore} methods is to be called
 *   </li>
 *  <li>After this call, {@link #isCompleted} should be called
 *   to determine if encoding was complete: if it was, there is
 *   nothing more to do.
 *   </li>
 *  <li>Otherwise caller should flush its buffer (since encoder
 *    has encoded as much as it can without flushing), and
 *    repeat cycle of calls to {@link #encodeMore} followed
 *    by a call to {@link #isCompleted} and flushing, as long
 *    as necessary to complete encoding.
 *   </li>
 * </ol>
 *<p>
 * Main restrictions for use are that value serializations must
 * produce only 7-bit ascii characters, and that the value can
 * be produced incrementally using limited size buffers. This
 * is true for all current value types of the Typed Access API.
 *<p>
 * Finally, details of how encoders are created and/or reused
 * is outside scope of this public interface. Stax2 reference
 * implementation handles this using an encoder factory
 * that knows construction details.
 *
 * @since 3.0
 */
public abstract class AsciiValueEncoder {
    /**
     * Constant used to determine when caller should flush buffer
     * before calling encode methods. Strict minimum would be
     * something like 22 (for floating point numbers), but let's
     * pad it a bit.
     */
    protected final static int MIN_CHARS_WITHOUT_FLUSH = 64;

    protected AsciiValueEncoder() {
    }

    /**
     * Method called by writer to check if it should flush its
     * output buffer (which has specified amount of free space)
     * before encoder can encode more data. Flushing is only
     * needed if (a) encoder has more data to output, and
     * (b) free space is not enough to contain smallest
     * segment of encoded value (individual array element
     * or encoded primitive value).
     *
     * @param freeChars Amount of free space (in characters) in
     *   the output buffer
     *
     * @return True if encoder still has data to output and
     *   specified amount of free space is insufficient for
     *   encoding any more data
     */
    public final boolean bufferNeedsFlush(int freeChars) {
        /* 25-Jun-2008, tatu: Although minimum ok sizes differ,
         *   let's simplify this a bit and use uniform threshold:
         *   it just needs to ensure that it's higher than any
         *   individual minimum would be. It may lead to more
         *   flushes, but overall shouldn't matter a whole lot.
         *   But removes need for sub-classes to implement it
         */
        return (freeChars < MIN_CHARS_WITHOUT_FLUSH);
    }

    /**
     * Method that can alternatively be called to determine whether encoder
     * has encoded all data it has. Generally called right after
     * a call to {@link #encodeMore}, to figure out whether buffer flush
     * is needed (there is more data), or encoding is complete.
     */
    public abstract boolean isCompleted();

    /**
     * @return Value of pointer after all remaining data (which
     *   may be "none") that can be encoded (as constrained by
     *   buffer length) has been encoded. Has to exceed 'ptr'
     *   value sent in; will be equal to it if nothing was
     *   encoded (which should only occur when everything has
     *   been encoded, as long as {@link #bufferNeedsFlush}
     *   is appropriately called once before calling this
     *   method)
     */
    public abstract int encodeMore(char[] buffer, int ptr, int end);

    /**
     * @return Value of pointer after all remaining data (which
     *   may be "none") that can be encoded (as constrained by
     *   buffer length) has been encoded. Has to exceed 'ptr'
     *   value sent in; will be equal to it if nothing was
     *   encoded (which should only occur when everything has
     *   been encoded, as long as {@link #bufferNeedsFlush}
     *   is appropriately called once before calling this
     *   method)
     */
    public abstract int encodeMore(byte[] buffer, int ptr, int end);
}
