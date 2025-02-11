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

package io.clientcore.core.serialization.xml.implementation.aalto.in;

import javax.xml.namespace.QName;
import java.util.Objects;

/**
 * An alternate implementation of PName: instead of coming straight from
 * byte contents, it is actually just built from a character array.
 *<p>
 * Note: one unfortunate result of this being a somewhat different PName
 * is that equality comparison between this and other implementations will not
 * work as expected. As such, these should only be used as temporary names.
 */
public final class PNameC {
    private final String _prefixedName;
    private final String _prefix;
    private final String _localName;

    /**
     * Binding of this qualified/prefixed name. Null if there is no
     * prefix; in which case name is either bound to the default namespace
     * (when element name), or no namespace (when other name, like attribute)
     */
    private NsBinding _namespaceBinding = null;

    /**
     * Since the hash may be calculated different from the way eventual
     * String's hash will be (right now it is not), we better store
     * "our" hash here.
     */
    private final int mHash;

    public PNameC(String pname, String prefix, String ln, int hash) {
        _prefixedName = pname;
        _prefix = prefix;
        _localName = ln;
        mHash = hash;
    }

    public PNameC createBoundName(NsBinding nsb) {
        PNameC newName = new PNameC(_prefixedName, _prefix, _localName, mHash);
        newName._namespaceBinding = nsb;
        return newName;
    }

    public static PNameC construct(String pname) {
        return construct(pname, calcHash(pname));
    }

    public static PNameC construct(String pname, int hash) {
        int colonIx = pname.indexOf(':');
        if (colonIx < 0) {
            return new PNameC(pname, null, pname, hash);
        }
        return new PNameC(pname, pname.substring(0, colonIx), pname.substring(colonIx + 1), hash);
    }

    /*
    /**********************************************************************
    /* Accessors
    /**********************************************************************
     */

    public String getPrefixedName() {
        return _prefixedName;
    }

    /**
     * @return Prefix of this name, if it has one; null if not.
     */
    public String getPrefix() {
        return _prefix;
    }

    public String getLocalName() {
        return _localName;
    }

    public String getNsUri() {
        return (_namespaceBinding == null) ? null : _namespaceBinding.mURI;
    }

    public QName constructQName() {
        String pr = _prefix;
        String uri = (_namespaceBinding == null) ? null : _namespaceBinding.mURI;
        // Stupid QName: some impls barf on nulls...
        return new QName((uri == null) ? "" : uri, _localName, (pr == null) ? "" : pr);
    }

    /**
     * Method called to construct a QName representation of elemented
     * represented by this PName. Because of namespace defaulting,
     * current default namespace binding also needs to be passed
     * (since only explicit ones get bound to PName instances).
     */
    public QName constructQName(NsBinding defaultNs) {
        String pr = _prefix;
        if (pr == null) { // QName barfs on nulls
            pr = "";
        }
        // Do we have a local binding?
        if (_namespaceBinding != null) {
            String uri = _namespaceBinding.mURI;
            if (uri != null) { // yup
                return new QName(uri, _localName, pr);
            }
        }
        // Nope. Default ns?
        String uri = defaultNs.mURI;
        return new QName((uri == null) ? "" : uri, _localName, pr);
    }

    /*
    /**********************************************************************
    /* Namespace binding
    /**********************************************************************
     */

    /**
     * @return True if the name as described either has no prefix (either
     *    belongs to the default ns [elems], or to 'no namespace' [attrs]),
     *    or has a prefix that is bound currently. False if name has a prefix
     *    that is unbound.
     */
    public boolean isBound() {
        return (_namespaceBinding == null) || (_namespaceBinding.mURI != null);
    }

    /**
     * Method that compares two bound PNames for semantic equality. This
     * means that the local name, as well as bound URI are compared.
     */
    public boolean boundEquals(PNameC other) {
        if (other == null || !Objects.equals(other._localName, _localName)) {
            return false;
        }
        // Let's assume URIs are canonicalized at least on per-doc basis?
        return Objects.equals(other.getNsUri(), getNsUri());
    }

    public boolean unboundEquals(PNameC other) {
        return (Objects.equals(other._prefixedName, _prefixedName));
    }

    public boolean boundEquals(String nsUri, String ln) {
        if (!_localName.equals(ln)) {
            return false;
        }
        String thisUri = getNsUri();
        if (nsUri == null || nsUri.isEmpty()) {
            return (thisUri == null);
        }
        return nsUri.equals(thisUri);
    }

    public int unboundHashCode() {
        return _prefixedName.hashCode();
    }

    public int boundHashCode() {
        /* How often do we have same local name, but differing URI?
         * Probably not often... thus, let's only use local name's hash.
         */
        return _localName.hashCode();
    }

    public static int boundHashCode(String localName) {
        return localName.hashCode();
    }

    /*
    //////////////////////////////////////////////////////////
    // Sub-class API
    //////////////////////////////////////////////////////////
     */

    public boolean equalsPName(char[] buffer, int start, int len, int hash) {
        if (hash != mHash) {
            return false;
        }
        String pname = _prefixedName;
        int plen = pname.length();
        if (len != plen) {
            return false;
        }
        for (int i = 0; i < len; ++i) {
            if (buffer[start + i] != pname.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    public int getCustomHash() {
        return mHash;
    }

    public static int calcHash(String key) {
        int hash = key.charAt(0);
        for (int i = 1, len = key.length(); i < len; ++i) {
            hash = (hash * 31) + (int) key.charAt(i);

        }
        return hash;
    }

    /*
    //////////////////////////////////////////////////////////
    // Redefined standard methods
    //////////////////////////////////////////////////////////
     */

    @Override
    public String toString() {
        return _prefixedName;
    }

    /**
     * Whether we should use internal hash, or the hash of prefixed
     * name string itself is an open question. For now, let's use
     * former.
     */
    @Override
    public int hashCode() {
        return mHash;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof PNameC)) {
            return false;
        }
        PNameC other = (PNameC) o;
        /* Only prefix and ln are interned, not the full prefixed name...
         * so let's compare separately. Can use identity comparison with
         * those though:
         */
        return (Objects.equals(other._prefix, _prefix)) && (Objects.equals(other._localName, _localName));
    }
}
