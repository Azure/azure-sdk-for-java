// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
/* Woodstox Lite ("wool") XML processor
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

package com.azure.xml.implementation.aalto.out;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * This class is similar to {@link com.azure.xml.implementation.aalto.in.PName}, in
 * that it implements an efficient representation of prefixed names,
 * but one used on output (Writer) side.
 *<p>
 * Note: unlike with Reader-side prefixed names, here we can not
 * assume that components are <code>intern()</code>ed.
 */
final class CharWName extends WName {
    final char[] _chars;

    public CharWName(String localName) {
        super(localName);
        _chars = localName.toCharArray();
    }

    public CharWName(String prefix, String localName) {
        super(prefix, localName);
        int plen = prefix.length();
        int llen = localName.length();
        _chars = new char[plen + 1 + llen];
        prefix.getChars(0, plen, _chars, 0);
        _chars[plen] = ':';
        localName.getChars(0, llen, _chars, plen + 1);
    }

    @Override
    public int serializedLength() {
        return _chars.length;
    }

    @Override
    public int appendBytes(byte[] buffer, int offset) {
        throw new RuntimeException("Internal error: appendBytes() should never be called");
    }

    @Override
    public void writeBytes(OutputStream out) {
        throw new RuntimeException("Internal error: writeBytes() should never be called");
    }

    @Override
    public void appendChars(char[] buffer, int offset) {
        int len = _chars.length;
        System.arraycopy(_chars, 0, buffer, offset, len);
    }

    @Override
    public void writeChars(Writer w) throws IOException {
        w.write(_chars);
    }

    @Override
    public String toString() {
        if (_prefix != null) {
            return new String(_chars);
        }
        return _localName;
    }
}
