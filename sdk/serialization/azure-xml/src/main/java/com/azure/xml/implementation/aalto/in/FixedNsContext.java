// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package com.azure.xml.implementation.aalto.in;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;

/**
 * Non-transient implementation of {@link NamespaceContext}.
 */
public final class FixedNsContext implements NamespaceContext {
    /**
     * We can share and reuse "no bindings" instance.
     */
    public final static FixedNsContext EMPTY_CONTEXT;
    static {
        EMPTY_CONTEXT = new FixedNsContext(null, new String[0]);
    }

    /*
    /**********************************************************************
    /* Persisted namespace information
    /**********************************************************************
     */

    /**
     * We will keep a reference to the last namespace declaration
     * in effect at point when this instance was created. This is used
     * for lazy invalidation of instances: if last declaration for
     * an instance differs from the last seen by the reader, a new
     * context must be created.
     */
    private final NsDeclaration _lastDeclaration;

    /**
     * Array that contains prefix/namespace-uri pairs, ordered from the
     * most recent declaration to older ones. Array is always exactly
     * sized so there are no empty entries at the end.
     */
    private final String[] _declarationData;

    /**
     * Temporary List used for constructing compact namespace binding
     * information that we will actually use.
     */
    private ArrayList<String> _tmpDecl = null;

    private FixedNsContext(NsDeclaration lastDecl, String[] declData) {
        _lastDeclaration = lastDecl;
        _declarationData = declData;
    }

    /**
     * Method called to either reuse this context or construct a new
     * one. Reuse is ok if the currently active last declaration has
     * not changed since time this instance was created.
     */
    public FixedNsContext reuseOrCreate(final NsDeclaration currLastDecl) {
        if (currLastDecl == _lastDeclaration) {
            return this;
        }
        // [aalto-xml#29]: Do not try reusing EMPTY_CONTEXT
        if (this == EMPTY_CONTEXT) {
            ArrayList<String> tmp = new ArrayList<>();
            for (NsDeclaration curr = currLastDecl; curr != null; curr = curr.getPrev()) {
                tmp.add(curr.getPrefix());
                tmp.add(curr.getCurrNsURI());
            }
            return new FixedNsContext(currLastDecl, tmp.toArray(new String[0]));
        }

        if (_tmpDecl == null) {
            _tmpDecl = new ArrayList<>();
        } else {
            _tmpDecl.clear();
        }
        for (NsDeclaration curr = currLastDecl; curr != null; curr = curr.getPrev()) {
            _tmpDecl.add(curr.getPrefix());
            _tmpDecl.add(curr.getCurrNsURI());
        }
        return new FixedNsContext(currLastDecl, _tmpDecl.toArray(new String[0]));
    }

    /*
    /**********************************************************************
    /* NamespaceContext API
    /**********************************************************************
     */

    @Override
    public String getNamespaceURI(String prefix) {
        // First the known offenders; invalid args, 2 predefined xml
        // namespace prefixes
        if (prefix == null) {
            throw new IllegalArgumentException("Null prefix not allowed");
        }
        if (!prefix.isEmpty()) {
            if (prefix.equals(XMLConstants.XML_NS_PREFIX)) {
                return XMLConstants.XML_NS_URI;
            }
            if (prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
                return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
            }
        }
        // here we count on never having null prefixes, just ""
        String[] ns = _declarationData;
        for (int i = 0, len = ns.length; i < len; i += 2) {
            if (prefix.equals(ns[i])) {
                return ns[i + 1]; // lgtm [java/index-out-of-bounds]
            }
        }
        return null;
    }

    @Override
    public String getPrefix(String nsURI) {
        // First the known offenders; invalid args, 2 predefined xml
        // namespace prefixes
        if (nsURI == null || nsURI.isEmpty()) {
            throw new IllegalArgumentException("Illegal to pass null/empty prefix as argument.");
        }
        if (nsURI.equals(XMLConstants.XML_NS_URI)) {
            return XMLConstants.XML_NS_PREFIX;
        }
        if (nsURI.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
            return XMLConstants.XMLNS_ATTRIBUTE;
        }
        String[] ns = _declarationData;

        main_loop: for (int i = 1, len = ns.length; i < len; i += 2) {
            if (nsURI.equals(ns[i])) {
                // may still suffer from masking, let's check
                String prefix = ns[i - 1];
                for (int j = i + 1; j < len; j += 2) {
                    // Prefixes are interned, can do straight equality check
                    if (Objects.equals(ns[j], prefix)) {
                        continue main_loop; // was masked!
                    }
                }
                return ns[i - 1];
            }
        }
        return null;

    }

    @Override
    public Iterator<String> getPrefixes(String nsURI) {
        // First the known offenders; invalid args, 2 predefined xml
        // namespace prefixes
        if (nsURI == null || nsURI.isEmpty()) {
            throw new IllegalArgumentException("Illegal to pass null/empty prefix as argument.");
        }
        if (nsURI.equals(XMLConstants.XML_NS_URI)) {
            return Collections.singletonList(XMLConstants.XML_NS_PREFIX).iterator();
        }
        if (nsURI.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
            return Collections.singletonList(XMLConstants.XMLNS_ATTRIBUTE).iterator();
        }

        String[] ns = _declarationData;

        String first = null;
        ArrayList<String> all = null;

        main_loop: for (int i = 1, len = ns.length; i < len; i += 2) {
            String currNS = ns[i];
            if (Objects.equals(currNS, nsURI) || currNS.equals(nsURI)) {
                // Need to ensure no masking occurs...
                String prefix = ns[i - 1];
                for (int j = i + 1; j < len; j += 2) {
                    // Prefixes are interned, can do straight equality check
                    if (Objects.equals(ns[j], prefix)) {
                        continue main_loop; // was masked, need to ignore
                    }
                }
                if (first == null) {
                    first = prefix;
                } else {
                    if (all == null) {
                        all = new ArrayList<>();
                        all.add(first);
                    }
                    all.add(prefix);
                }
            }
        }
        if (all != null) {
            return all.iterator();
        }
        if (first != null) {
            return Collections.singletonList(first).iterator();
        }
        return Collections.emptyIterator();
    }

    /*
    /**********************************************************************
    /* Standard method overrides
    /**********************************************************************
     */

    @Override
    public String toString() {
        if (this == EMPTY_CONTEXT) {
            return "[EMPTY non-transient NsContext]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0, len = _declarationData.length; i < len; i += 2) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append('"').append(_declarationData[i]).append("\"->\"");
            sb.append(_declarationData[i + 1]).append('"'); // lgtm [java/index-out-of-bounds]
        }
        sb.append(']');
        return sb.toString();
    }
}
