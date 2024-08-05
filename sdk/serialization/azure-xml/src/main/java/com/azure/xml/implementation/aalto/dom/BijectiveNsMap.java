// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package com.azure.xml.implementation.aalto.dom;

import java.util.*;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/* !!! 18-Dec-2008, tatu: Copied from Woodstox almost verbatim, should be
 *   replaced/removed/rewritten soon.
 */
/**
 * Helper class that implements "bijective map" (Map that allows use of values
 * as keys and vice versa, bidirectional access), and is specifically
 * used for storing namespace binding information.
 * One thing worth noting is that Strings stored are NOT assumed to have
 * been unified (interned) -- if they were, different implementation would
 * be more optimal.
 */
public final class BijectiveNsMap {
    /*
    ///////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////
     */

    /**
     * Let's plan for having up to 14 explicit namespace declarations (2
     * defaults, for 'xml' and 'xmlns', are pre-populated)
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

    private BijectiveNsMap(int scopeStart, String[] strs) {
        _scopeStart = _scopeEnd = scopeStart;
        _nsStrings = strs;
    }

    public static BijectiveNsMap createEmpty() {
        String[] strs = new String[DEFAULT_ARRAY_SIZE];

        strs[0] = XMLConstants.XML_NS_PREFIX;
        strs[1] = XMLConstants.XML_NS_URI;
        strs[2] = XMLConstants.XMLNS_ATTRIBUTE;
        strs[3] = XMLConstants.XMLNS_ATTRIBUTE_NS_URI;

        /* Let's consider pre-defined ones to be 'out of scope', i.e.
         * conceptually be part of (missing) parent's mappings.
         */
        return new BijectiveNsMap(4, strs);
    }

    public BijectiveNsMap createChild() {
        return new BijectiveNsMap(_scopeEnd, _nsStrings);
    }

    /*
    ///////////////////////////////////////////////
    // Public API, accessors
    ///////////////////////////////////////////////
     */

    public String findUriByPrefix(String prefix) {
        /* This is quite simple: just need to locate the last mapping
         * for the prefix, if any:
         */
        String[] strs = _nsStrings;
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
                            // Masking... got to continue the main loop:
                            continue main_loop;
                        }
                    }
                }
                // Ok, unmasked one, can return
                return prefix;
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
                            // Masking... got to continue the main loop:
                            continue main_loop;
                        }
                    }
                }
                // Ok, unmasked one, can add
                if (l == null) {
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

    /*
    ///////////////////////////////////////////////
    // Public API, mutators
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
    public String addMapping(String prefix, String uri) {
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
        if (_scopeEnd >= strs.length) {
            // let's just double the array sizes...
            strs = Arrays.copyOf(strs, (strs.length << 1));
            _nsStrings = strs;
        }
        strs[_scopeEnd++] = prefix;
        strs[_scopeEnd++] = uri;

        return null;
    }

    /**
     * Method used to add a dynamic binding, and return the prefix
     * used to bind the specified namespace URI.
     */
    public String addGeneratedMapping(String prefixBase, NamespaceContext ctxt, String uri, int[] seqArr) {
        String[] strs = _nsStrings;
        int seqNr = seqArr[0];
        String prefix;

        main_loop: while (true) {
            /* We better intern the resulting prefix? Or not?
             * TODO: maybe soft cache these for other docs?
             */
            prefix = (prefixBase + seqNr).intern();
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
            /* So far so good... but do we have a root context that might
             * have something too?
             */

            if (ctxt != null && ctxt.getNamespaceURI(prefix) != null) {
                continue;
            }
            break;
        }
        seqArr[0] = seqNr;

        // Ok, good; then let's just add it in...
        if (_scopeEnd >= strs.length) {
            // let's just double the array sizes...
            strs = Arrays.copyOf(strs, (strs.length << 1));
            _nsStrings = strs;
        }
        strs[_scopeEnd++] = prefix;
        strs[_scopeEnd++] = uri;

        return prefix;
    }
}
