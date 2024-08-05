// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
/* Aalto XML processor
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

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Simple container for information regarding an open element within
 * stream writer output.
 *<p>
 * Note: these elements are designed to be reused within context of
 * a single document output, ie. they are owned by the stream writer,
 * and can be recycled by it, as necessary.
 */
final class OutputElement {

    /**
     * Reference to either the parent (enclosing element) of this
     * element, when part of active output context; or link to next
     * reusable unused element after this one (if not part of active
     * context).
     */
    private OutputElement _parent;

    /**
     * Prefixed name used for serialization.
     */
    private WName _name;

    /**
     * Namespace of the element, whatever prefix part of {@link #_name}
     * maps to. Non-final to allow reuse.
     */
    private String _uri;

    /*
    /**********************************************************************
    /* Namespace binding/mapping information
    /**********************************************************************
     */

    /**
     * Namespace context end application may have supplied, and that
     * (if given) should be used to augment explicitly defined bindings.
     */
    //NamespaceContext _rootNsContext;

    private String _defaultNsURI;

    private NsBinder _nsBinder;

    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    private OutputElement() {
        _parent = null;
        _name = null;
        _uri = null;
        _nsBinder = null;
        _defaultNsURI = "";
    }

    private OutputElement(OutputElement parent, WName name, String uri, NsBinder binder) {
        _parent = parent;
        _name = name;
        _uri = uri;
        _nsBinder = binder;
        _defaultNsURI = parent._defaultNsURI;
    }

    static OutputElement createRoot() {
        return new OutputElement();
    }

    /**
     * Simplest factory method, which gets called when non-namespace
     * element output method is called. It is, then, assumed to
     * use the default namespce.
     */
    OutputElement createChild(WName name) {
        return new OutputElement(this, name, _defaultNsURI, _nsBinder);
    }

    /**
     * Full factory method, used for 'normal' namespace qualified output
     * methods.
     */
    OutputElement createChild(WName name, String uri) {
        return new OutputElement(this, name, uri, _nsBinder);
    }

    /*
    /**********************************************************************
    /* Instance reuse support
    /**********************************************************************
     */

    /**
     * @return New head of the recycle pool
     */
    OutputElement reuseAsChild(OutputElement parent, WName name) {
        OutputElement poolHead = _parent;
        relink(parent, name, _defaultNsURI);
        return poolHead;
    }

    OutputElement reuseAsChild(OutputElement parent, WName name, String nsURI) {
        OutputElement poolHead = _parent;
        relink(parent, name, nsURI);
        return poolHead;
    }

    /**
     * Method called to reuse a pooled instance, but with different
     * name
     */
    private void relink(OutputElement parent, WName name, String uri) {
        _parent = parent;
        _name = name;
        _uri = uri;
        _nsBinder = parent._nsBinder;
        _defaultNsURI = parent._defaultNsURI;
    }

    /**
     * Method called to temporarily link this instance to a pool, to
     * allow reusing of instances with the same reader.
     */
    void addToPool(OutputElement poolHead) {
        _parent = poolHead;
    }

    /*
    /**********************************************************************
    /* Public API, accessors
    /**********************************************************************
     */

    public OutputElement getParent() {
        return _parent;
    }

    public boolean isRoot() {
        // (Virtual) Root element has no parent...
        return (_parent == null);
    }

    public WName getName() {
        return _name;
    }

    public String getLocalName() {
        return _name.getLocalName();
    }

    public String getNonNullPrefix() {
        String p = _name.getPrefix();
        return (p == null) ? "" : null;
    }

    /**
     * @return String presentation of the fully-qualified name, in
     *   "prefix:localName" format (no URI). Useful for error and
     *   debugging messages.
     */
    public String getNameDesc() {
        return _name.toString();
    }

    public String getNonNullNamespaceURI() {
        return (_uri == null) ? "" : _uri;
    }

    public QName getQName() {
        return new QName(_uri, _name.getLocalName(), _name.getPrefix());
    }

    /*
    /**********************************************************************
    /* Public API, mutators
    /**********************************************************************
     */

    public void setDefaultNsURI(String uri) {
        _defaultNsURI = uri;
    }

    public void addPrefix(String prefix, String uri) {
        if (_nsBinder == null) {
            // Didn't have a mapping yet? Need to create one...
            _nsBinder = NsBinder.createEmpty();
        } else {
            if (_parent != null && _parent._nsBinder == _nsBinder) {
                // Shared with parent(s)? Need to branch off to be able to modify
                _nsBinder = _nsBinder.createChild();
            }
        }
        _nsBinder.addMapping(prefix, uri);
    }

    /*
    /**********************************************************************
    /* NamespaceContext implementation, other ns funcs
    /**********************************************************************
     */

    public String getNamespaceURI(String prefix) {
        if (prefix.isEmpty()) {
            return _defaultNsURI;
        }
        if (_nsBinder != null) {
            return _nsBinder.findUriByPrefix(prefix);
        }
        return null;
    }

    public String getPrefix(String uri) {
        if (_defaultNsURI.equals(uri)) {
            return "";
        }
        if (_nsBinder != null) {
            return _nsBinder.findPrefixByUri(uri);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public Iterator<String> getPrefixes(String uri, NamespaceContext rootNsContext) {
        List<String> l = null;

        if (_defaultNsURI.equals(uri)) {
            l = new ArrayList<>();
            l.add("");
        }
        if (_nsBinder != null) {
            l = _nsBinder.getPrefixesBoundToUri(uri, l);
        }

        // How about the root namespace context? (if any)
        // Note: it's quite difficult to properly resolve masking

        if (rootNsContext != null) {
            Iterator<String> it = rootNsContext.getPrefixes(uri);
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

    /**
     * Method similar to {@link #getPrefix}, but one that will not accept
     * the default namespace, only an explicit one. Usually used when
     * trying to find a prefix for attributes.
     */
    public String getExplicitPrefix(String uri, NamespaceContext rootNsContext) {
        if (_nsBinder != null) {
            String prefix = _nsBinder.findPrefixByUri(uri);
            if (prefix != null) {
                return prefix;
            }
        }
        if (rootNsContext != null) {
            String prefix = rootNsContext.getPrefix(uri);
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

    /*
    /**********************************************************************
    /* Comparison (etc) methods
    /**********************************************************************
     */

    // 04-Jan-2021, tatu: Does not seem necessary or useful; no matching "equals()" so
    //    should probably remove. But no harm right now so leaving for the moment
    @Override
    public int hashCode() {
        return _name.hashCode();
    }
}
