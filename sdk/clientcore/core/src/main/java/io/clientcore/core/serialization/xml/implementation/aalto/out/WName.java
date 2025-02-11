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

package io.clientcore.core.serialization.xml.implementation.aalto.out;

import io.clientcore.core.serialization.xml.implementation.aalto.in.PNameC;

import java.io.IOException;
import java.io.Writer;
import java.util.Objects;

/**
 * This class is similar to {@link PNameC}, in
 * that it implements an efficient representation of prefixed names,
 * but one used on output (Writer) side.
 *<p>
 * Note: unlike with Reader-side prefixed names, here we can not
 * assume that components are <code>intern()</code>ed.
 */
public final class WName {
    final String _prefix;
    final String _localName;

    final char[] _chars;

    /*
    //////////////////////////////////////////////////////////
    // Life-cycle
    //////////////////////////////////////////////////////////
     */

    WName(String ln) {
        _prefix = null;
        _localName = ln;
        _chars = ln.toCharArray();
    }

    WName(String prefix, String ln) {
        _prefix = prefix;
        _localName = ln;
        int plen = prefix.length();
        int llen = ln.length();
        _chars = new char[plen + 1 + llen];
        prefix.getChars(0, plen, _chars, 0);
        _chars[plen] = ':';
        ln.getChars(0, llen, _chars, plen + 1);
    }

    /*
    //////////////////////////////////////////////////////////
    // Accessors
    //////////////////////////////////////////////////////////
     */

    /**
     * @return Length of full (qualified) name, in native
     *   serialization units (bytes or characters)
     */
    public int serializedLength() {
        return _chars.length;
    }

    public String getPrefix() {
        return _prefix;
    }

    public String getLocalName() {
        return _localName;
    }

    /*
    //////////////////////////////////////////////////////////
    // Serialization
    //////////////////////////////////////////////////////////
     */

    public void appendChars(char[] buffer, int offset) {
        int len = _chars.length;
        System.arraycopy(_chars, 0, buffer, offset, len);
    }

    public void writeChars(Writer w) throws IOException {
        w.write(_chars);
    }

    /*
    //////////////////////////////////////////////////////////
    // Redefined standard methods
    //////////////////////////////////////////////////////////
     */

    @Override
    public String toString() {
        if (_prefix != null) {
            return new String(_chars);
        }
        return _localName;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        // Straight identity comparison faster...
        if (o.getClass() != getClass()) {
            return false;
        }

        WName other = (WName) o;
        // Can not assume names are intern()ed, need to call equals()
        if (other._localName.equals(_localName)) {
            if (_prefix == null) {
                return other._prefix == null;
            }
            return _prefix.equals(other._prefix);
        }
        return false;
    }

    /**
     * Whether we should use internal hash, or the hash of prefixed
     * name string itself is an open question. For now, let's use
     * former.
     */
    @Override
    public int hashCode() {
        int hash = _localName.hashCode();
        if (_prefix != null) {
            hash ^= _prefix.hashCode();
        }
        return hash;
    }

    /*
    //////////////////////////////////////////////////////////
    // Efficient comparison methods
    //////////////////////////////////////////////////////////
     */

    public boolean hasName(String localName) {
        if (_prefix != null) {
            return false;
        }
        return _localName.equals(localName);
    }

    public boolean hasName(String prefix, String localName) {
        if (Objects.equals(_localName, localName)) { // very likely match
            if (prefix == null) {
                return (_prefix == null);
            }
            return prefix.equals(_prefix);
        }

        // hashCode() already computed for both (for index etc)
        if (_localName.hashCode() != localName.hashCode()) {
            return false;
        }
        if (prefix == null) {
            if (_prefix != null) {
                return false;
            }
        } else {
            if (!prefix.equals(_prefix)) {
                return false;
            }
        }
        return _localName.equals(localName);
    }
}
