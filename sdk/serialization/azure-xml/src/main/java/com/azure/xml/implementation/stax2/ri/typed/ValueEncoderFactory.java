// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
/* Reference Implementation of
 * Stax2 extension API (for basic Stax API, JSR-173)
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

/**
 * Factory class used to construct all
 * {@link AsciiValueEncoder} instances needed by a single
 * stream writer instance. Simple encoders are also recycled
 * (for the lifetime of an encoder, which is same as its owners,
 * i.e. stream reader or writer's) to minimize overhead.
 * More complex ones (array based, long string) are not recycled.
 *<p>
 * Since encoders are recycled, instances are not thread-safe.
 *
 * @since 3.0
 */
public final class ValueEncoderFactory {
    final static byte BYTE_SPACE = (byte) ' ';

    // // // Lazily-constructed, recycled encoder instances

    private TokenEncoder _tokenEncoder = null;
    private IntEncoder _intEncoder = null;
    private LongEncoder _longEncoder = null;
    private FloatEncoder _floatEncoder = null;
    private DoubleEncoder _doubleEncoder = null;

    public ValueEncoderFactory() {
    }

    // // // Scalar encoder access

    public ScalarEncoder getScalarEncoder(String value) {
        // Short or long?
        if (value.length() > AsciiValueEncoder.MIN_CHARS_WITHOUT_FLUSH) { // short
            if (_tokenEncoder == null) {
                _tokenEncoder = new TokenEncoder();
            }
            _tokenEncoder.reset(value);
            return _tokenEncoder;
        }
        // Nope, long: need segmented
        return new StringEncoder(value);
    }

    public ScalarEncoder getEncoder(boolean value) {
        // !!! TBI: optimize
        return getScalarEncoder(value ? "true" : "false");
    }

    public IntEncoder getEncoder(int value) {
        if (_intEncoder == null) {
            _intEncoder = new IntEncoder();
        }
        _intEncoder.reset(value);
        return _intEncoder;
    }

    public LongEncoder getEncoder(long value) {
        if (_longEncoder == null) {
            _longEncoder = new LongEncoder();
        }
        _longEncoder.reset(value);
        return _longEncoder;
    }

    public FloatEncoder getEncoder(float value) {
        if (_floatEncoder == null) {
            _floatEncoder = new FloatEncoder();
        }
        _floatEncoder.reset(value);
        return _floatEncoder;
    }

    public DoubleEncoder getEncoder(double value) {
        if (_doubleEncoder == null) {
            _doubleEncoder = new DoubleEncoder();
        }
        _doubleEncoder.reset(value);
        return _doubleEncoder;
    }

    // // // Array encoder access

    public IntArrayEncoder getEncoder(int[] values, int from, int length) {
        return new IntArrayEncoder(values, from, from + length);
    }

    public LongArrayEncoder getEncoder(long[] values, int from, int length) {
        return new LongArrayEncoder(values, from, from + length);
    }

    public FloatArrayEncoder getEncoder(float[] values, int from, int length) {
        return new FloatArrayEncoder(values, from, from + length);
    }

    public DoubleArrayEncoder getEncoder(double[] values, int from, int length) {
        return new DoubleArrayEncoder(values, from, from + length);
    }

    // // // And special one for Base64

    public Base64Encoder getEncoder(Base64Variant v, byte[] data, int from, int length) {
        return new Base64Encoder(v, data, from, from + length);
    }

    /*
    ////////////////////////////////////////////////////////////////
    // Implementation classes; first, scalar (single-value) encoders
    ////////////////////////////////////////////////////////////////
     */

    /**
     * Intermediate base class for encoders that deal with single
     * primitive values.
     *<p>
     * No default implementations, because textual and typed
     * (non-textual) sub-classes differ significantly.
     * In a way, this is just a tag class
     */
    abstract static class ScalarEncoder extends AsciiValueEncoder {
        protected ScalarEncoder() {
        }
    }

    /**
     * Implementation of textual encoder that operates on short
     * textual values ("tokens"). As such, it can count on being able
     * to output the whole output in one pass, without tracking
     * location
     */
    final static class TokenEncoder extends ScalarEncoder {
        String _value;

        private TokenEncoder() {
            super();
        }

        private void reset(String value) {
            _value = value;
        }

        @Override
        public boolean isCompleted() {
            return (_value == null);
        }

        @Override
        public int encodeMore(char[] buffer, int ptr, int end) {
            String str = _value;
            _value = null;
            int len = str.length();
            str.getChars(0, len, buffer, ptr);
            ptr += len;
            return ptr;
        }

        @Override
        public int encodeMore(byte[] buffer, int ptr, int end) {
            String str = _value;
            _value = null;
            int len = str.length();
            for (int i = 0; i < len; ++i) {
                buffer[ptr++] = (byte) str.charAt(i);
            }
            return ptr;
        }
    }

    /**
     * Implementation of textual encoder that operates on longer
     * textual values. Because of length, it is possible that output
     * has to be done in multiple pieces. As a result, there is need
     * to track current position withing text.
     *<p>
     * In addition, instances of this class are not recycled, as
     * it seems less beneficial (less likely to need to be reused,
     * or offer performance improvements if they would be)
     */
    final static class StringEncoder extends ScalarEncoder {
        String _value;

        int _offset;

        private StringEncoder(String value) {
            super();
            _value = value;
        }

        @Override
        public boolean isCompleted() {
            return (_value == null);
        }

        @Override
        public int encodeMore(char[] buffer, int ptr, int end) {
            int left = _value.length() - _offset;
            int free = end - ptr;
            if (free >= left) { // completed, simple
                _value.getChars(_offset, left, buffer, ptr);
                _value = null;
                return (ptr + left);
            }
            _value.getChars(_offset, free, buffer, ptr);
            _offset += free;
            return end;
        }

        @Override
        public int encodeMore(byte[] buffer, int ptr, int end) {
            int left = _value.length() - _offset;
            int free = end - ptr;
            if (free >= left) { // completed, simple
                String str = _value;
                _value = null;
                for (int last = str.length(), offset = _offset; offset < last; ++offset) {
                    buffer[ptr++] = (byte) str.charAt(offset);
                }
                return ptr;
            }
            for (; ptr < end; ++ptr) {
                buffer[ptr] = (byte) _value.charAt(_offset++);
            }
            return ptr;
        }
    }

    /**
     * Intermediate base class for typed (non-textual) scalar values
     */
    abstract static class TypedScalarEncoder extends ScalarEncoder {
        protected TypedScalarEncoder() {
        }

        /**
         * Since scalar typed values are guaranteed to always be
         * written in one go, they will always be completed by
         * time method is called./
         */
        @Override
        public final boolean isCompleted() {
            return true;
        }
    }

    final static class IntEncoder extends TypedScalarEncoder {
        int _value;

        private IntEncoder() {
            super();
        }

        private void reset(int value) {
            _value = value;
        }

        @Override
        public int encodeMore(char[] buffer, int ptr, int end) {
            return NumberUtil.writeInt(_value, buffer, ptr);
        }

        @Override
        public int encodeMore(byte[] buffer, int ptr, int end) {
            return NumberUtil.writeInt(_value, buffer, ptr);
        }
    }

    final static class LongEncoder extends TypedScalarEncoder {
        long _value;

        private LongEncoder() {
            super();
        }

        private void reset(long value) {
            _value = value;
        }

        @Override
        public int encodeMore(char[] buffer, int ptr, int end) {
            return NumberUtil.writeLong(_value, buffer, ptr);
        }

        @Override
        public int encodeMore(byte[] buffer, int ptr, int end) {
            return NumberUtil.writeLong(_value, buffer, ptr);
        }
    }

    final static class FloatEncoder extends TypedScalarEncoder {
        float _value;

        private FloatEncoder() {
            super();
        }

        private void reset(float value) {
            _value = value;
        }

        @Override
        public int encodeMore(char[] buffer, int ptr, int end) {
            return NumberUtil.writeFloat(_value, buffer, ptr);
        }

        @Override
        public int encodeMore(byte[] buffer, int ptr, int end) {
            return NumberUtil.writeFloat(_value, buffer, ptr);
        }
    }

    final static class DoubleEncoder extends TypedScalarEncoder {
        double _value;

        private DoubleEncoder() {
            super();
        }

        private void reset(double value) {
            _value = value;
        }

        @Override
        public int encodeMore(char[] buffer, int ptr, int end) {
            return NumberUtil.writeDouble(_value, buffer, ptr);
        }

        @Override
        public int encodeMore(byte[] buffer, int ptr, int end) {
            return NumberUtil.writeDouble(_value, buffer, ptr);
        }
    }

    /*
    ////////////////////////////////////////////////////////////////
    // Implementation classes; array encoders
    ////////////////////////////////////////////////////////////////
     */

    /**
     * Intermediate base class for encoders that deal with arrays
     * of values.
     */
    abstract static class ArrayEncoder extends AsciiValueEncoder {
        int _ptr;
        final int _end;

        protected ArrayEncoder(int ptr, int end) {
            _ptr = ptr;
            _end = end;
        }

        @Override
        public final boolean isCompleted() {
            return (_ptr >= _end);
        }

        @Override
        public abstract int encodeMore(char[] buffer, int ptr, int end);
    }

    /**
     * Concrete implementation used for encoding int[] content.
     */
    final static class IntArrayEncoder extends ArrayEncoder {
        final int[] _values;

        private IntArrayEncoder(int[] values, int from, int length) {
            super(from, length);
            _values = values;
        }

        @Override
        public int encodeMore(char[] buffer, int ptr, int end) {
            int lastOk = end - (1 + NumberUtil.MAX_INT_CLEN);
            while (ptr <= lastOk && _ptr < _end) {
                buffer[ptr++] = ' ';
                ptr = NumberUtil.writeInt(_values[_ptr++], buffer, ptr);
            }
            return ptr;
        }

        @Override
        public int encodeMore(byte[] buffer, int ptr, int end) {
            int lastOk = end - (1 + NumberUtil.MAX_INT_CLEN);
            while (ptr <= lastOk && _ptr < _end) {
                buffer[ptr++] = BYTE_SPACE;
                ptr = NumberUtil.writeInt(_values[_ptr++], buffer, ptr);
            }
            return ptr;
        }
    }

    final static class LongArrayEncoder extends ArrayEncoder {
        final long[] _values;

        private LongArrayEncoder(long[] values, int from, int length) {
            super(from, length);
            _values = values;
        }

        @Override
        public int encodeMore(char[] buffer, int ptr, int end) {
            int lastOk = end - (1 + NumberUtil.MAX_LONG_CLEN);
            while (ptr <= lastOk && _ptr < _end) {
                buffer[ptr++] = ' ';
                ptr = NumberUtil.writeLong(_values[_ptr++], buffer, ptr);
            }
            return ptr;
        }

        @Override
        public int encodeMore(byte[] buffer, int ptr, int end) {
            int lastOk = end - (1 + NumberUtil.MAX_LONG_CLEN);
            while (ptr <= lastOk && _ptr < _end) {
                buffer[ptr++] = BYTE_SPACE;
                ptr = NumberUtil.writeLong(_values[_ptr++], buffer, ptr);
            }
            return ptr;
        }
    }

    final static class FloatArrayEncoder extends ArrayEncoder {
        final float[] _values;

        private FloatArrayEncoder(float[] values, int from, int length) {
            super(from, length);
            _values = values;
        }

        @Override
        public int encodeMore(char[] buffer, int ptr, int end) {
            int lastOk = end - (1 + NumberUtil.MAX_FLOAT_CLEN);
            while (ptr <= lastOk && _ptr < _end) {
                buffer[ptr++] = ' ';
                ptr = NumberUtil.writeFloat(_values[_ptr++], buffer, ptr);
            }
            return ptr;
        }

        @Override
        public int encodeMore(byte[] buffer, int ptr, int end) {
            int lastOk = end - (1 + NumberUtil.MAX_FLOAT_CLEN);
            while (ptr <= lastOk && _ptr < _end) {
                buffer[ptr++] = BYTE_SPACE;
                ptr = NumberUtil.writeFloat(_values[_ptr++], buffer, ptr);
            }
            return ptr;
        }
    }

    final static class DoubleArrayEncoder extends ArrayEncoder {
        final double[] _values;

        private DoubleArrayEncoder(double[] values, int from, int length) {
            super(from, length);
            _values = values;
        }

        @Override
        public int encodeMore(char[] buffer, int ptr, int end) {
            int lastOk = end - (1 + NumberUtil.MAX_DOUBLE_CLEN);
            while (ptr <= lastOk && _ptr < _end) {
                buffer[ptr++] = ' ';
                ptr = NumberUtil.writeDouble(_values[_ptr++], buffer, ptr);
            }
            return ptr;
        }

        @Override
        public int encodeMore(byte[] buffer, int ptr, int end) {
            int lastOk = end - (1 + NumberUtil.MAX_DOUBLE_CLEN);
            while (ptr <= lastOk && _ptr < _end) {
                buffer[ptr++] = BYTE_SPACE;
                ptr = NumberUtil.writeDouble(_values[_ptr++], buffer, ptr);
            }
            return ptr;
        }
    }

    /*
    ////////////////////////////////////////////////////////////////
    // Implementation classes: binary (base64) encoder
    ////////////////////////////////////////////////////////////////
     */

    final static class Base64Encoder extends AsciiValueEncoder {
        final static char PAD_CHAR = '=';
        final static byte PAD_BYTE = (byte) PAD_CHAR;

        /* Hmmh. Base64 specs suggest \r\n... but for xml, \n is the
         * canonical one. Let's take xml's choice here, more compact too.
         */
        final static byte LF_CHAR = '\n';
        final static byte LF_BYTE = (byte) LF_CHAR;

        final Base64Variant _variant;

        final byte[] _input;

        int _inputPtr;

        final int _inputEnd;

        /**
         * We need a counter to know when to add mandatory
         * linefeed.
         */
        int _chunksBeforeLf;

        private Base64Encoder(Base64Variant v, byte[] values, int from, int end) {
            _variant = v;
            _input = values;
            _inputPtr = from;
            _inputEnd = end;
            _chunksBeforeLf = _variant.getMaxLineLength() >> 2;
        }

        @Override
        public boolean isCompleted() {
            return (_inputPtr >= _inputEnd);
        }

        @Override
        public int encodeMore(char[] buffer, int outPtr, int outEnd) {
            // Encoding is by chunks of 3 input, 4 output chars, so:
            int inEnd = _inputEnd - 3;
            // But let's also reserve room for lf char
            outEnd -= 5;

            while (_inputPtr <= inEnd) {
                if (outPtr > outEnd) { // no more room: need to return for flush
                    return outPtr;
                }
                // First, mash 3 bytes into lsb of 32-bit int
                int b24 = ((int) _input[_inputPtr++]) << 8;
                b24 |= ((int) _input[_inputPtr++]) & 0xFF;
                b24 = (b24 << 8) | (((int) _input[_inputPtr++]) & 0xFF);
                outPtr = _variant.encodeBase64Chunk(b24, buffer, outPtr);

                if (--_chunksBeforeLf <= 0) {
                    buffer[outPtr++] = LF_CHAR;
                    _chunksBeforeLf = _variant.getMaxLineLength() >> 2;
                }
            }
            // main stuff done, any partial data to output?
            int inputLeft = (_inputEnd - _inputPtr); // 0, 1 or 2
            if (inputLeft > 0) { // yes, but do we have room for output?
                if (outPtr <= outEnd) { // yup (and we do have room for it all)
                    int b24 = ((int) _input[_inputPtr++]) << 16;
                    if (inputLeft == 2) {
                        b24 |= (((int) _input[_inputPtr++]) & 0xFF) << 8;
                    }
                    outPtr = _variant.encodeBase64Partial(b24, inputLeft, buffer, outPtr);
                }
            }
            return outPtr;
        }

        @Override
        public int encodeMore(byte[] buffer, int outPtr, int outEnd) {
            int inEnd = _inputEnd - 3;
            outEnd -= 5;

            while (_inputPtr <= inEnd) {
                if (outPtr > outEnd) { // no more room: need to return for flush
                    return outPtr;
                }
                // First, mash 3 bytes into lsb of 32-bit int
                int b24 = ((int) _input[_inputPtr++]) << 8;
                b24 |= ((int) _input[_inputPtr++]) & 0xFF;
                b24 = (b24 << 8) | (((int) _input[_inputPtr++]) & 0xFF);
                outPtr = _variant.encodeBase64Chunk(b24, buffer, outPtr);

                if (--_chunksBeforeLf <= 0) {
                    buffer[outPtr++] = LF_BYTE;
                    _chunksBeforeLf = _variant.getMaxLineLength() >> 2;
                }
            }
            // main stuff done, any leftovers?
            int inputLeft = (_inputEnd - _inputPtr);
            if (inputLeft > 0) { // yes, but do we have room for output?
                if (outPtr <= outEnd) { // yup
                    int b24 = ((int) _input[_inputPtr++]) << 16;
                    if (inputLeft == 2) {
                        b24 |= (((int) _input[_inputPtr++]) & 0xFF) << 8;
                    }
                    outPtr = _variant.encodeBase64Partial(b24, inputLeft, buffer, outPtr);
                }
            }
            return outPtr;
        }
    }
}
