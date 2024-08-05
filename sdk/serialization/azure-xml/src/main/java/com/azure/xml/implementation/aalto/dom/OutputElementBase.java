// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package com.azure.xml.implementation.aalto.dom;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Class that encapsulates information about a specific element in virtual
 * output stack for namespace-aware writers.
 * It provides support for URI-to-prefix mappings as well as namespace
 * mapping generation.
 *<p>
 * One noteworthy feature of the class is that it is designed to allow
 * "short-term recycling", ie. instances can be reused within context
 * of a simple document output. While reuse/recycling of such lightweight
 * object is often useless or even counter productive, here it may
 * be worth using, due to simplicity of the scheme (basically using
 * a very simple free-elements linked list).
 */
public abstract class OutputElementBase implements NamespaceContext {
    public final static int PREFIX_UNBOUND = 0;
    public final static int PREFIX_OK = 1;
    public final static int PREFIX_MISBOUND = 2;

    /*
    /**********************************************************************
    /* Namespace binding/mapping information
    /**********************************************************************
     */

    /**
     * Namespace context end application may have supplied, and that
     * (if given) should be used to augment explicitly defined bindings.
     */
    protected NamespaceContext _rootNsContext;

    protected String _defaultNsURI;

    /**
     * Mapping of namespace prefixes to URIs and back.
     */
    protected BijectiveNsMap _nsMapping;

    /**
     * True, if {@link #_nsMapping} is a shared copy from the parent;
     * false if a local copy was created (which happens when namespaces
     * get bound etc).
     */
    protected boolean _nsMapShared;

    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    /**
     * Constructor for the virtual root element
     */
    protected OutputElementBase() {
        _nsMapping = null;
        _nsMapShared = false;
        _defaultNsURI = "";
        _rootNsContext = null;
    }

    protected OutputElementBase(OutputElementBase parent, BijectiveNsMap ns) {
        _nsMapping = ns;
        _nsMapShared = (ns != null);
        _defaultNsURI = parent._defaultNsURI;
        _rootNsContext = parent._rootNsContext;
    }

    /*
    /**********************************************************************
    /* Public API, accessors
    /**********************************************************************
     */

    public abstract boolean isRoot();

    public final String getDefaultNsUri() {
        return _defaultNsURI;
    }

    /*
    /**********************************************************************
    /* Public API, ns binding, checking
    /**********************************************************************
     */

    /**
     * Method similar to {@link #getPrefix}, but one that will not accept
     * the default namespace, only an explicit one. Usually used when
     * trying to find a prefix for attributes.
     */
    public final String getExplicitPrefix(String uri) {
        if (_nsMapping != null) {
            String prefix = _nsMapping.findPrefixByUri(uri);
            if (prefix != null) {
                return prefix;
            }
        }
        if (_rootNsContext != null) {
            String prefix = _rootNsContext.getPrefix(uri);
            if (prefix != null) {
                // Hmmh... still can't use the default NS:
                if (!prefix.isEmpty()) {
                    return prefix;
                }
                // ... should we try to find an explicit one?
            }
        }
        return null;
    }

    /**
     * Method that verifies that passed-in prefix indeed maps to the specified
     * namespace URI; and depending on how it goes returns a status for
     * caller.
     *
     * @param isElement If true, rules for the default NS are those of elements
     *   (ie. empty prefix can map to non-default namespace); if false,
     *   rules are those of attributes (only non-default prefix can map to
     *   a non-default namespace).
     *
     * @return PREFIX_OK, if passed-in prefix matches matched-in namespace URI
     *    in current scope; PREFIX_UNBOUND if it's not bound to anything,
     *    and PREFIX_MISBOUND if it's bound to another URI.
     *
     * @throws XMLStreamException True if default (no) prefix is allowed to
     *    match a non-default URI (elements); false if not (attributes)
     */
    public final int isPrefixValid(String prefix, String nsURI, boolean isElement) throws XMLStreamException {
        // Hmmm.... caller shouldn't really pass null.
        if (nsURI == null) {
            nsURI = "";
        }

        /* First thing is to see if specified prefix is bound to a namespace;
         * and if so, verify it matches with data passed in:
         */

        // Checking default namespace?
        if (prefix == null || prefix.isEmpty()) {
            if (isElement) {
                // It's fine for elements only if the URI actually matches:
                if (nsURI.equals(_defaultNsURI)) {
                    return PREFIX_OK;
                }
            } else {
                /* Attributes never use the default namespace: "no prefix"
                 * can only mean "no namespace"
                 */
                if (nsURI.isEmpty()) {
                    return PREFIX_OK;
                }
            }
            return PREFIX_MISBOUND;
        }

        /* Need to handle 'xml' prefix and its associated
         *   URI; they are always declared by default
         */
        if ("xml".equals(prefix)) {
            // Should we thoroughly verify its namespace matches...?
            // 01-Apr-2005, TSa: Yes, let's always check this
            if (!nsURI.equals(XMLConstants.XML_NS_URI)) {
                throwOutputError("Namespace prefix 'xml' can not be bound to non-default namespace ('" + nsURI
                    + "'); has to be the default '" + XMLConstants.XML_NS_URI + "'");
            }
            return PREFIX_OK;
        }

        // Nope checking some other namespace
        String act;

        if (_nsMapping != null) {
            act = _nsMapping.findUriByPrefix(prefix);
        } else {
            act = null;
        }

        if (act == null && _rootNsContext != null) {
            act = _rootNsContext.getNamespaceURI(prefix);
        }

        // Not (yet) bound...
        if (act == null) {
            return PREFIX_UNBOUND;
        }

        return (act.equals(nsURI)) ? PREFIX_OK : PREFIX_MISBOUND;
    }

    /*
    /**********************************************************************
    /* Public API, mutators
    /**********************************************************************
     */

    public abstract void setDefaultNsUri(String uri);

    public final String generateMapping(String prefixBase, String uri, int[] seqArr) {
        // This is mostly cut'n pasted from addPrefix()...
        if (_nsMapping == null) {
            // Didn't have a mapping yet? Need to create one...
            _nsMapping = BijectiveNsMap.createEmpty();
        } else if (_nsMapShared) {
            /* Was shared with parent(s)? Need to create a derivative, to
             * allow for nesting/scoping of new prefix
             */
            _nsMapping = _nsMapping.createChild();
            _nsMapShared = false;
        }
        return _nsMapping.addGeneratedMapping(prefixBase, _rootNsContext, uri, seqArr);
    }

    public final void addPrefix(String prefix, String uri) {
        if (_nsMapping == null) {
            // Didn't have a mapping yet? Need to create one...
            _nsMapping = BijectiveNsMap.createEmpty();
        } else if (_nsMapShared) {
            /* Was shared with parent(s)? Need to create a derivative, to
             * allow for nesting/scoping of new prefix
             */
            _nsMapping = _nsMapping.createChild();
            _nsMapShared = false;
        }
        _nsMapping.addMapping(prefix, uri);
    }

    /*
    /**********************************************************************
    /* NamespaceContext implementation
    /**********************************************************************
     */

    @Override
    public final String getNamespaceURI(String prefix) {
        if (prefix.isEmpty()) { //default NS
            return _defaultNsURI;
        }
        if (_nsMapping != null) {
            String uri = _nsMapping.findUriByPrefix(prefix);
            if (uri != null) {
                return uri;
            }
        }
        return (_rootNsContext != null) ? _rootNsContext.getNamespaceURI(prefix) : null;
    }

    @Override
    public final String getPrefix(String uri) {
        if (_defaultNsURI.equals(uri)) {
            return "";
        }
        if (_nsMapping != null) {
            String prefix = _nsMapping.findPrefixByUri(uri);
            if (prefix != null) {
                return prefix;
            }
        }
        return (_rootNsContext != null) ? _rootNsContext.getPrefix(uri) : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final Iterator<String> getPrefixes(String uri) {
        List<String> l = null;

        if (_defaultNsURI.equals(uri)) {
            l = new ArrayList<>();
            l.add("");
        }
        if (_nsMapping != null) {
            l = _nsMapping.getPrefixesBoundToUri(uri, l);
        }
        // How about the root namespace context? (if any)
        /* Note: it's quite difficult to properly resolve masking, when
         * combining these things (not impossible, just tricky); for now
         * let's do best effort without worrying about masking:
         */
        if (_rootNsContext != null) {
            Iterator<String> it = _rootNsContext.getPrefixes(uri);
            while (it.hasNext()) {
                String prefix = it.next();
                if (prefix.isEmpty()) { // default NS already checked
                    continue;
                }
                // slow check... but what the heck
                if (l == null) {
                    l = new ArrayList<>();
                } else if (l.contains(prefix)) { // double-defined...
                    continue;
                }
                l.add(prefix);
            }
        }
        if (l == null) {
            return Collections.emptyIterator();
        }
        return l.iterator();
    }

    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */

    protected final void throwOutputError(String msg) throws XMLStreamException {
        throw new XMLStreamException(msg);
    }
}
