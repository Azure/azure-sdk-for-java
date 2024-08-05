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

package com.azure.xml.implementation.aalto.in;

import javax.xml.namespace.QName;
import java.util.Objects;

/**
 * Prefixed Name is similar to {@link javax.xml.namespace.QName} (qualified name),
 * but only contains information about local name optionally prefixed by
 * a prefix and colon, without namespace binding information.
 */
public abstract class PName {
    protected final String _prefixedName;
    protected final String _prefix;
    protected final String _localName;

    /**
     * Binding of this qualified/prefixed name. Null if there is no
     * prefix; in which case name is either bound to the default namespace
     * (when element name), or no namespace (when other name, like attribute)
     */
    protected NsBinding _namespaceBinding = null;

    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    protected PName(String pname, String prefix, String ln) {
        _prefixedName = pname;
        _prefix = prefix;
        _localName = ln;
    }

    public abstract PName createBoundName(NsBinding nsb);

    /*
      // 26-Jun-2006, TSa: Doesn't seem to be needed any more...
    protected void bind(NsBinding nsb)
    {
        if (mNsBinding != null) { // !!! Temporary assertion
            throw new RuntimeException("Trying to re-set binding (for '"+getPrefixedName()+"'), was: "+mNsBinding+", new: "+nsb);
        }
        mNsBinding = nsb;
    }
    */

    /*
    /**********************************************************************
    /* Accessors
    /**********************************************************************
     */

    public final String getPrefixedName() {
        return _prefixedName;
    }

    /**
     * @return Prefix of this name, if it has one; null if not.
     */
    public final String getPrefix() {
        return _prefix;
    }

    public final String getLocalName() {
        return _localName;
    }

    public final String getNsUri() {
        return (_namespaceBinding == null) ? null : _namespaceBinding.mURI;
    }

    public final QName constructQName() {
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
    public final QName constructQName(NsBinding defaultNs) {
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
    public final boolean isBound() {
        return (_namespaceBinding == null) || (_namespaceBinding.mURI != null);
    }

    /**
     * Method that compares two bound PNames for semantic equality. This
     * means that the local name, as well as bound URI are compared.
     */
    public final boolean boundEquals(PName other) {
        if (other == null || !Objects.equals(other._localName, _localName)) {
            return false;
        }
        // Let's assume URIs are canonicalized at least on per-doc basis?
        return Objects.equals(other.getNsUri(), getNsUri());
    }

    public final boolean unboundEquals(PName other) {
        return (Objects.equals(other._prefixedName, _prefixedName));
    }

    public final boolean boundEquals(String nsUri, String ln) {
        if (!_localName.equals(ln)) {
            return false;
        }
        String thisUri = getNsUri();
        if (nsUri == null || nsUri.isEmpty()) {
            return (thisUri == null);
        }
        return nsUri.equals(thisUri);
    }

    public final int unboundHashCode() {
        return _prefixedName.hashCode();
    }

    public final int boundHashCode() {
        /* How often do we have same local name, but differing URI?
         * Probably not often... thus, let's only use local name's hash.
         */
        return _localName.hashCode();
    }

    public static int boundHashCode(String localName) {
        return localName.hashCode();
    }

    /*
    /**********************************************************************
    /* Redefined standard methods
    /**********************************************************************
     */

    @Override
    public final String toString() {
        return _prefixedName;
    }

    @Override
    public int hashCode() {
        return _prefixedName.hashCode();
    }

    @Override
    public final boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof PName)) {
            return false;
        }
        PName other = (PName) o;
        /* Only prefix and ln are interned, not the full prefixed name...
         * so let's compare separately. Can use identity comparison with
         * those though:
         */
        return (Objects.equals(other._prefix, _prefix)) && (Objects.equals(other._localName, _localName));
    }

    /*
    /**********************************************************************
    /* Methods for package/core parser
    /**********************************************************************
     */

    /* Note: These 3 methods really should be in the byte-based sub-class...
     * but there are performance reasons to keep there, to remove
     * some otherwise necessary casts.
     */

    public abstract int sizeInQuads();

    public abstract int getQuad(int index);

}
