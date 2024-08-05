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

import java.util.*;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 * Simple helper class to allow resolving of namespace bindings either
 * from prefix to URI, or vice versa.
 *<p>
 * Note: unlike with input side resolvers, here we can not assume that
 * prefixes or URIs given are canonicalized (interned), and identity
 * comparison can not be used exclusively.
 */
final class NsBinder {
    /*
    ///////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////
     */

    /**
     * Let's plan for having up to 14 explicit namespace declarations (in
     * addition to 2 defaults, 'xml' and 'xmlns')
     */
    final static int DEFAULT_ARRAY_SIZE = 2 * 16;

    /*
    ///////////////////////////////////////////////
    // Member vars
    ///////////////////////////////////////////////
     */

    final int _scopeStart;

    /**
     * Array that contains { prefix, ns-uri } pairs, up to (but not including)
     * index {@link #_scopeEnd}.
     */
    String[] _nsStrings;

    int _scopeEnd;

    /*
    ///////////////////////////////////////////////
    // Life-cycle
    ///////////////////////////////////////////////
     */

    private NsBinder(int scopeStart, String[] strs) {
        _scopeStart = _scopeEnd = scopeStart;
        _nsStrings = strs;
    }

    public static NsBinder createEmpty() {
        String[] strs = new String[DEFAULT_ARRAY_SIZE];

        strs[0] = "xml";
        strs[1] = XMLConstants.XML_NS_URI;
        strs[2] = "xmlns";
        strs[3] = XMLConstants.XMLNS_ATTRIBUTE_NS_URI;

        /* Let's consider pre-defined ones to be 'out of scope', i.e.
         * conceptually be part of (missing) parent's mappings.
         */
        return new NsBinder(4, strs);
    }

    public NsBinder createChild() {
        return new NsBinder(_scopeEnd, _nsStrings);
    }

    /*
    ///////////////////////////////////////////////
    // Public API, accessors
    ///////////////////////////////////////////////
     */

    public String findUriByPrefix(String prefix) {
        /* This is quite simple: just need to locate the last mapping
         * for the prefix, if any; no masking is possible.
         */
        String[] strs = _nsStrings;
        // Hash code should differentiate cheaply (beyond length checks)
        int phash = prefix.hashCode();

        for (int ix = _scopeEnd - 2; ix >= 0; ix -= 2) {
            String thisP = strs[ix];
            if (Objects.equals(thisP, prefix) || (thisP.hashCode() == phash && thisP.equals(prefix))) {
                return strs[ix + 1];
            }
        }
        return null;
    }

    public String findPrefixByUri(String uri) {
        /* Finding a valid binding for the given URI is trickier, since
         * mappings can be masked by others... so, we need to first find
         * most recent binding, from the freshest one, and then verify
         * it's still unmasked; if not, continue with the first loop,
         * and so on.
         */
        String[] strs = _nsStrings;
        int uhash = uri.hashCode();

        main_loop: for (int ix = _scopeEnd - 1; ix > 0; ix -= 2) {
            String thisU = strs[ix];
            if (Objects.equals(thisU, uri) || (thisU.hashCode() == uhash && thisU.equals(uri))) {
                // match, but has it been masked?
                String prefix = strs[ix - 1];
                /* only need to check, if it wasn't within current scope
                 * (no masking allowed within scopes)
                 */
                if (ix < _scopeStart) {
                    int phash = prefix.hashCode();
                    for (int j = ix - 1, end = _scopeEnd; j < end; j += 2) {
                        String thisP = strs[ix];
                        if (Objects.equals(thisP, prefix) || (thisP.hashCode() == phash && thisP.equals(prefix))) {
                            // Masking, can't use
                            continue main_loop;
                        }
                    }
                }
                return prefix; // unmasked, safe to return
            }
        }
        return null;
    }

    public List<String> getPrefixesBoundToUri(String uri, List<String> l) {
        /* Same problems (masking) apply here, as well as with
         * findPrefixByUri...
         */
        String[] strs = _nsStrings;
        int uhash = uri.hashCode();

        main_loop: for (int ix = _scopeEnd - 1; ix > 0; ix -= 2) {
            String thisU = strs[ix];
            if (Objects.equals(thisU, uri) || (thisU.hashCode() == uhash && thisU.equals(uri))) {
                String prefix = strs[ix - 1];
                if (ix < _scopeStart) {
                    int phash = prefix.hashCode();
                    for (int j = ix - 1, end = _scopeEnd; j < end; j += 2) {
                        String thisP = strs[ix];
                        if (Objects.equals(thisP, prefix) || (thisP.hashCode() == phash && thisP.equals(prefix))) {
                            // Masking... got to continue the main loop:
                            continue main_loop;
                        }
                    }
                }
                if (l == null) { // unmasked, ok
                    l = new ArrayList<>();
                }
                l.add(prefix);
            }
        }
        return l;
    }

    public int size() {
        return (_scopeEnd >> 1);
    }

    public int localSize() {
        return ((_scopeEnd - _scopeStart) >> 1);
    }

    /*
    ///////////////////////////////////////////////
    // Package API, mutators
    ///////////////////////////////////////////////
     */

    /**
     * Method to add a new prefix-to-URI mapping for the current scope.
     * Note that it should NOT be used for the default namespace
     * declaration
     *
     * @param prefix Prefix to bind
     * @param uri URI to bind to the prefix
     *
     * @return If the prefix was already bound, the URI it was bound to:
     *   null if it's a new binding for the current scope.
     */
    String addMapping(String prefix, String uri) {
        String[] strs = _nsStrings;
        int phash = prefix.hashCode();

        for (int ix = _scopeStart, end = _scopeEnd; ix < end; ix += 2) {
            String thisP = strs[ix];
            if (Objects.equals(thisP, prefix) || (thisP.hashCode() == phash && thisP.equals(prefix))) {
                // Overriding an existing mapping
                String old = strs[ix + 1];
                strs[ix + 1] = uri;
                return old;
            }
        }
        // no previous binding, let's just add it at the end
        if (_scopeEnd >= strs.length) { // no more room? double up
            strs = Arrays.copyOf(strs, strs.length << 1); // JDK 1.6
            _nsStrings = strs;
        }
        strs[_scopeEnd++] = prefix;
        strs[_scopeEnd++] = uri;

        return null;
    }

    /**
     * Method used to generate a new prefix that does not conflict with
     * an existing bound prefix.
     */
    String generatePrefix(String prefixBase, NamespaceContext ctxt, int[] seqArr) {
        String[] strs = _nsStrings;
        int seqNr = seqArr[0];

        main_loop: while (true) {
            // We better intern the resulting prefix? Or not?
            /* TODO: use cheaper canonicalization? (joint cache
             * with input side prefix canonicalization?)
             */
            String prefix = (prefixBase + seqNr).intern();
            ++seqNr;

            /* Ok, let's see if we have a mapping (masked or not) for
             * the prefix. If we do, let's just not use it: we could
             * of course mask it (unless it's in current scope), but
             * it's easier to just get a "virgin" prefix...
             */
            int phash = prefix.hashCode();

            for (int ix = _scopeEnd - 2; ix >= 0; ix -= 2) {
                String thisP = strs[ix];
                if (Objects.equals(thisP, prefix) || (thisP.hashCode() == phash && thisP.equals(prefix))) {
                    continue main_loop;
                }
            }
            /* So far so good... but still need to ensure there's nothing
             * in the root context, if we were given one?
             */
            if (ctxt != null && ctxt.getNamespaceURI(prefix) != null) {
                continue;
            }
            seqArr[0] = seqNr;
            return prefix;
        }
    }

    /*
    ///////////////////////////////////////////////
    // Standard overridden methods
    ///////////////////////////////////////////////
     */

    @Override
    public String toString() {
        return "[" + getClass() + "; " + size() + " entries; of which " + localSize() + " local]";
    }
}
