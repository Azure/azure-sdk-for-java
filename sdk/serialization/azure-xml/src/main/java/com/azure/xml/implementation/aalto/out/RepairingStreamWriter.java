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

import java.util.*;

import javax.xml.namespace.QName;
import javax.xml.stream.*;

import com.azure.xml.implementation.stax2.ri.typed.AsciiValueEncoder;

import com.azure.xml.implementation.aalto.impl.ErrorConsts;

/**
 * Concrete implementation of {@link StreamWriterBase}, which
 * implements the "namespace repairing" mode of operation.
 * This means that the writer ensures correctness and validity
 * of namespace bindings, as based on namespace URIs caller
 * passes, by adding necessary namespace declarations and using
 * prefixes as required to obtain expected results.
 */
public final class RepairingStreamWriter extends StreamWriterBase {
    /*
    /////////////////////////////////////////////////////
    // Prefix generation settings
    /////////////////////////////////////////////////////
     */

    // // // Additional specific config flags base class doesn't have

    final String _cfgAutomaticNsPrefix;

    /*
    ////////////////////////////////////////////////////
    // Additional state
    ////////////////////////////////////////////////////
     */

    /**
     * Sequence number used for generating dynamic namespace prefixes.
     * Array used as a wrapper to allow for easy sharing of the sequence
     * number.
     */
    int[] _autoNsSeq = null;

    String _suggestedDefNs = null;

    /**
     * Map that contains URI-to-prefix entries that point out suggested
     * prefixes for URIs. These are populated by calls to
     * {@link #setPrefix}, and they are only used as hints for binding;
     * if there are conflicts, repairing writer can just use some other
     * prefix.
     */
    HashMap<String, String> _suggestedPrefixes = null;

    /*
    /////////////////////////////////////////////////////
    // Construction, init
    /////////////////////////////////////////////////////
     */

    public RepairingStreamWriter(WriterConfig cfg, XmlWriter writer, WNameTable symbols) {
        super(cfg, writer, symbols);
        _cfgAutomaticNsPrefix = cfg.getAutomaticNsPrefix();
    }

    /*
    /////////////////////////////////////////////////////
    // Implementations of abstract methods from base class,
    // Stax 1.0 methods
    /////////////////////////////////////////////////////
     */

    /**
     * With repairing writer, this is only taken as a suggestion as to how
     * the caller would prefer prefixes to be mapped.
     */
    @Override
    public void setDefaultNamespace(String uri) throws XMLStreamException {
        _suggestedDefNs = (uri == null || uri.length() == 0) ? null : uri;
    }

    @Override
    public void _setPrefix(String prefix, String uri) {
        // note: sub-class has verified that prefix != null, uri != null

        /* Ok; let's assume that passing an empty String as
         * the URI means that we don't want passed prefix to be preferred
         * for any URI (since there's no way to map a prefix to the default
         * namespace)
         */
        if (uri == null || uri.length() == 0) {
            if (_suggestedPrefixes != null) {
                for (Iterator<Map.Entry<String, String>> it = _suggestedPrefixes.entrySet().iterator(); it.hasNext();) {
                    Map.Entry<String, String> en = it.next();
                    if (en.getValue().equals(prefix)) {
                        it.remove();
                    }
                }
            }
        } else {
            if (_suggestedPrefixes == null) {
                _suggestedPrefixes = new HashMap<String, String>(16);
            }
            _suggestedPrefixes.put(uri, prefix);
        }
    }

    //public void writeAttribute(String localName, String value)

    @Override
    public void writeAttribute(String nsURI, String localName, String value) throws XMLStreamException {
        if (!_stateStartElementOpen) {
            throwOutputError(ErrorConsts.WERR_ATTR_NO_ELEM);
        }
        // no URI? No prefix. Otherwise, need to find or bind:
        WName name = (nsURI == null || nsURI.length() == 0)
            ? _symbols.findSymbol(localName)
            : _generateAttrName(null, localName, nsURI);
        _writeAttribute(name, value);
    }

    @Override
    public void writeAttribute(String prefix, String nsURI, String localName, String value) throws XMLStreamException {
        if (!_stateStartElementOpen) {
            throwOutputError(ErrorConsts.WERR_ATTR_NO_ELEM);
        }
        // no URI? No prefix. Otherwise, need to find or bind:
        WName name = (nsURI == null || nsURI.length() == 0)
            ? _symbols.findSymbol(localName)
            : _generateAttrName(prefix, localName, nsURI);
        _writeAttribute(name, value);
    }

    @Override
    public void writeDefaultNamespace(String nsURI) throws XMLStreamException {
        if (!_stateStartElementOpen) {
            throwOutputError(ErrorConsts.WERR_NS_NO_ELEM);
        }
        /* ... We have one complication though: if the current element
         * uses default namespace, can not change it (attributes don't
         * matter -- they never use the default namespace, but either don't
         * belong to a namespace, or belong to one using explicit prefix)
         */

        if (!_currElem.hasPrefix()) {
            _currElem.setDefaultNsURI(nsURI);
            _writeDefaultNamespace(nsURI);
        }
        _writeDefaultNamespace(nsURI);
    }

    //public void writeDTD(String dtd)

    //public void writeEmptyElement(String localName)

    @Override
    public void writeEmptyElement(String nsURI, String localName) throws XMLStreamException {
        _writeStartOrEmpty(null, localName, nsURI, true);
    }

    @Override
    public void writeEmptyElement(String prefix, String localName, String nsURI) throws XMLStreamException {
        _writeStartOrEmpty(prefix, localName, nsURI, true);
    }

    //public void writeEndElement()

    @Override
    public void writeNamespace(String prefix, String nsURI) throws XMLStreamException {
        if (prefix == null || prefix.length() == 0) {
            writeDefaultNamespace(nsURI);
            return;
        }
        if (!_stateStartElementOpen) {
            throwOutputError(ErrorConsts.WERR_NS_NO_ELEM);
        }
        /* Let's only add the declaration if the prefix
         * is as of yet unbound. If we have to re-bind things in future,
         * so be it -- for now, this should suffice (and if we have to
         * add re-binding, must verify that no attribute, nor element
         *  itself, is using overridden prefix)
         */
        if (_currElem.isPrefixUnbound(prefix, _rootNsContext)) {
            _currElem.addPrefix(prefix, nsURI);
            _writeNamespace(prefix, nsURI);
        }
    }

    //public void writeStartElement(String localName)

    @Override
    public void writeStartElement(String nsURI, String localName) throws XMLStreamException {
        _writeStartOrEmpty(null, localName, nsURI, false);
    }

    @Override
    public void writeStartElement(String prefix, String localName, String nsURI) throws XMLStreamException {
        _writeStartOrEmpty(prefix, localName, nsURI, false);
    }

    /*
    /////////////////////////////////////////////////////
    // Implementations of abstract methods from base class,
    // Stax2 Typed API Access (v3.0)
    /////////////////////////////////////////////////////
     */

    @Override
    public void writeTypedAttribute(String prefix, String nsURI, String localName, AsciiValueEncoder enc)
        throws XMLStreamException {
        if (!_stateStartElementOpen) {
            throwOutputError(ErrorConsts.WERR_ATTR_NO_ELEM);
        }
        WName name = (prefix == null || prefix.length() == 0)
            ? _symbols.findSymbol(localName)
            : _symbols.findSymbol(prefix, localName);
        _writeAttribute(name, enc);
    }

    @Override
    protected String _serializeQName(QName name) throws XMLStreamException {
        /* Gets bit more complicated: we need to ensure that given URI
         * is properly bound...
         */
        String uri = name.getNamespaceURI();
        String prefix = name.getPrefix();
        String local = name.getLocalPart();

        // Perhaps prefix is fine as is?
        if (_currElem.isPrefixBoundTo(prefix, uri, _rootNsContext)) {
            if (prefix == null || prefix.length() == 0) {
                return local;
            }
            return prefix + ":" + local;
        }

        /* Nope. Need to find or generate a new one... let's treat like an
         * attribute (i.e. get an explicit prefix, not use default ns).
         * Not optimal, need not be; not a hot spot method.
         */
        WName newName = _generateAttrName(prefix, local, uri);
        return newName.getPrefixedName();
    }

    /*
    /////////////////////////////////////////////////////
    // Internal methods
    /////////////////////////////////////////////////////
     */

    /**
     * @param uri Non-empty namespace URI that will be used for the
     *   attribute
     */
    protected WName _generateAttrName(String suggPrefix, String localName, String uri) throws XMLStreamException {
        if (suggPrefix != null && suggPrefix.length() > 0) { // prefer this prefix
            switch (_currElem.checkPrefixValidity(suggPrefix, uri, _rootNsContext)) {
                case UNBOUND: // ok, need to bind
                    _writeNamespace(suggPrefix, uri);
                    _currElem.addPrefix(suggPrefix, uri);
                    // fall through:
                case OK: // fine as is
                    return _symbols.findSymbol(suggPrefix, localName);

                case MISBOUND: // can't bind, need another prefix
                    // fall through
            }
        }
        /* Had no suggested prefix, or one given didn't work. Either way,
         * need to find a match to URI, or generate new one.
         */
        String prefix = _currElem.getExplicitPrefix(uri, _rootNsContext);
        if (prefix == null) { // none found, generate
            if (_autoNsSeq == null) {
                _autoNsSeq = new int[1];
                _autoNsSeq[0] = 1;
            }
            prefix = _currElem.generatePrefix(_rootNsContext, _cfgAutomaticNsPrefix, _autoNsSeq);
            _writeNamespace(prefix, uri);
            _currElem.addPrefix(prefix, uri);
        }
        return _symbols.findSymbol(prefix, localName);
    }

    public void _writeStartOrEmpty(String prefix, String localName, String nsURI, boolean isEmpty)
        throws XMLStreamException {
        // In repairing mode, better ensure validity.

        // First: do we want the "no namespace"? Separate, distinct handling
        if (nsURI == null || nsURI.length() == 0) {
            // either way, can not have non-empty prefix
            _verifyStartElement(null, localName);
            // must output the start-tag-start to add xmlns decl (if needed)
            WName name = _symbols.findSymbol(localName);
            _writeStartTag(name, isEmpty, null);
            if (!_currElem.hasEmptyDefaultNs()) { // is bound, need to rebind...
                _writeDefaultNamespace(nsURI);
                // also: changes default ns of curr elem etc:
                _currElem.setDefaultNsURI("");
            }
            if (_validator != null) {
                _validator.validateElementStart(localName, "", "");
            }
            return;
        }

        // Otherwise a non-empty namespace. Do we have a suggested prefix?
        if (prefix == null) { // nope, anything goes
            prefix = _currElem.getPrefix(nsURI);
            // And we do have something bound already!
            if (prefix != null) {
                _writeStartAndVerify(prefix, localName, nsURI, isEmpty);
            } else {
                // nothing suitable bound, let's generate
                prefix = _generateElemPrefix(nsURI);
                if (_writeStartAndVerify(prefix, localName, nsURI, isEmpty)) { // default ns
                    _writeDefaultNamespace(nsURI);
                    _currElem.setDefaultNsURI(nsURI);
                } else { // non-default, explicit prefix
                    _writeNamespace(prefix, nsURI);
                    _currElem.addPrefix(prefix, nsURI);
                }
            }
        } else {
            boolean isDef = _writeStartAndVerify(prefix, localName, nsURI, isEmpty);
            // Is the prefix already properly bound?
            if (!_currElem.isPrefixBoundTo(prefix, nsURI, _rootNsContext)) { // yup
                // Nope, need to rebind
                if (isDef) {
                    _writeDefaultNamespace(nsURI);
                    _currElem.setDefaultNsURI(nsURI);
                } else { // explicit prefix
                    _writeNamespace(prefix, nsURI);
                    _currElem.addPrefix(prefix, nsURI);
                }
            }
        }

        // And after all that, validation?
        if (_validator != null) {
            _validator.validateElementStart(localName, "", ((prefix == null) ? "" : prefix));
        }
        return;
    }

    /**
     * @return True, if prefix indicates default namespace (is null or empty);
     *   false otherwise
     */
    private final boolean _writeStartAndVerify(String prefix, String localName, String nsURI, boolean isEmpty)
        throws XMLStreamException {
        if (prefix == null || prefix.length() == 0) { // default ns
            _verifyStartElement(null, localName);
            _writeStartTag(_symbols.findSymbol(localName), isEmpty, nsURI);
            return true;
        }
        // non-default, i.e. real prefix
        _verifyStartElement(prefix, localName);
        _writeStartTag(_symbols.findSymbol(prefix, localName), isEmpty, nsURI);
        return false;
    }

    /**
     * Method called if given URI is not yet bound, and no suggested prefix
     * is given (or one given can't be used). If so, methods is
     * to create a not-yet-bound-prefix for the namespace.
     */
    protected final String _generateElemPrefix(String uri) throws XMLStreamException {
        // First: is it the 'recommended' default ns?
        if (_suggestedDefNs != null && _suggestedDefNs.equals(uri)) {
            return null;
        }
        // If not, maybe one of other 'recommended' bindings has it?
        if (_suggestedPrefixes != null) {
            String prefix = _suggestedPrefixes.get(uri);
            if (prefix != null) {
                return prefix;
            }
        }
        // Otherwise, generate a new unbound prefix

        /* We have 2 choices here, essentially;
         *   could make elements always try to override the def
         *   ns... or can just generate new one. Let's do latter
         *   for now.
         */
        if (_autoNsSeq == null) {
            _autoNsSeq = new int[1];
            _autoNsSeq[0] = 1;
        }
        return _currElem.generatePrefix(_rootNsContext, _cfgAutomaticNsPrefix, _autoNsSeq);
    }
}
