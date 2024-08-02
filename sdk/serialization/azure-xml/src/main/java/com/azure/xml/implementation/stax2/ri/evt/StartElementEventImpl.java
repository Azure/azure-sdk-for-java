// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.ri.evt;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.*;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;

import com.azure.xml.implementation.stax2.XMLStreamWriter2;
import com.azure.xml.implementation.stax2.ri.EmptyIterator;
import com.azure.xml.implementation.stax2.ri.EmptyNamespaceContext;

/**
 * Wstx {@link StartElement} implementation used when event is constructed
 * from already objectified data, for example when constructed by the event
 * factory.
 */
public class StartElementEventImpl extends BaseEventImpl implements StartElement {
    // // // Basic configuration

    protected final QName _name;

    protected final ArrayList<Attribute> _attrs;

    protected final ArrayList<Namespace> _nsDecls;

    /**
     * Enclosing namespace context
     */
    protected NamespaceContext _parentNsCtxt;

    // // // Lazily constructed components

    NamespaceContext _actualNsCtxt = null;

    /*
    /**********************************************************************
    /* Life cycle
    /**********************************************************************
     */

    protected StartElementEventImpl(Location loc, QName name, ArrayList<Attribute> attrs, ArrayList<Namespace> nsDecls,
        NamespaceContext parentNsCtxt) {
        super(loc);
        _name = name;
        _attrs = attrs;
        _nsDecls = nsDecls;
        _parentNsCtxt = (parentNsCtxt == null) ? EmptyNamespaceContext.getInstance() : parentNsCtxt;
    }

    public static StartElementEventImpl construct(Location loc, QName name, Iterator<?> attrIt, Iterator<?> nsDeclIt,
        NamespaceContext nsCtxt) {
        ArrayList<Attribute> attrs;
        if (attrIt == null || !attrIt.hasNext()) {
            attrs = null;
        } else {
            attrs = new ArrayList<>();
            do {
                // Cast is only done for early catching of incorrect types
                attrs.add((Attribute) attrIt.next());
            } while (attrIt.hasNext());
        }

        ArrayList<Namespace> nsDecls;
        if (nsDeclIt == null || !nsDeclIt.hasNext()) {
            nsDecls = null;
        } else {
            nsDecls = new ArrayList<>();
            do {
                nsDecls.add((Namespace) nsDeclIt.next()); // cast to catch type problems early
            } while (nsDeclIt.hasNext());
        }
        return new StartElementEventImpl(loc, name, attrs, nsDecls, nsCtxt);
    }

    /*
    /**********************************************************************
    /* Implementation of abstract base methods, overrides
    /**********************************************************************
     */

    @Override
    public StartElement asStartElement() { // overriden to save a cast
        return this;
    }

    @Override
    public int getEventType() {
        return START_ELEMENT;
    }

    @Override
    public boolean isStartElement() {
        return true;
    }

    @Override
    public void writeAsEncodedUnicode(Writer w) throws XMLStreamException {
        try {
            w.write('<');
            String prefix = _name.getPrefix();
            if (prefix != null && !prefix.isEmpty()) {
                w.write(prefix);
                w.write(':');
            }
            w.write(_name.getLocalPart());

            // Any namespaces?
            if (_nsDecls != null) {
                for (Namespace nsDecl : _nsDecls) {
                    w.write(' ');
                    nsDecl.writeAsEncodedUnicode(w);
                }
            }

            // How about attrs?
            if (_attrs != null) {
                for (Attribute attr : _attrs) {
                    // No point in adding default attributes?
                    if (attr.isSpecified()) {
                        w.write(' ');
                        attr.writeAsEncodedUnicode(w);
                    }
                }
            }

            w.write('>');
        } catch (IOException ie) {
            throw new XMLStreamException(ie);
        }
    }

    @Override
    public void writeUsing(XMLStreamWriter2 sw) throws XMLStreamException {
        QName n = _name;
        sw.writeStartElement(n.getPrefix(), n.getLocalPart(), n.getNamespaceURI());

        // Any namespaces?
        if (_nsDecls != null) {
            for (Namespace ns : _nsDecls) {
                String prefix = ns.getPrefix();
                String uri = ns.getNamespaceURI();
                if (prefix == null || prefix.isEmpty()) {
                    sw.writeDefaultNamespace(uri);
                } else {
                    sw.writeNamespace(prefix, uri);
                }
            }
        }

        // How about attrs?
        if (_attrs != null) {
            for (Attribute attr : _attrs) {
                // No point in adding default attributes?
                if (attr.isSpecified()) {
                    QName name = attr.getName();
                    sw.writeAttribute(name.getPrefix(), name.getNamespaceURI(), name.getLocalPart(), attr.getValue());
                }
            }
        }
    }

    /*
    /**********************************************************************
    /* Public API
    /**********************************************************************
     */

    @Override
    public final QName getName() {
        return _name;
    }

    @Override
    public Iterator<Namespace> getNamespaces() {
        if (_nsDecls == null) {
            return EmptyIterator.getInstance();
        }
        return _nsDecls.iterator();
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        if (_actualNsCtxt == null) {
            if (_nsDecls == null) {
                _actualNsCtxt = _parentNsCtxt;
            } else {
                _actualNsCtxt = MergedNsContext.construct(_parentNsCtxt, _nsDecls);
            }
        }
        return _actualNsCtxt;
    }

    @Override
    public String getNamespaceURI(String prefix) {
        if (_nsDecls != null) {
            if (prefix == null) {
                prefix = "";
            }
            for (Namespace ns : _nsDecls) {
                String thisPrefix = ns.getPrefix();
                if (thisPrefix == null) {
                    thisPrefix = "";
                }
                if (prefix.equals(thisPrefix)) {
                    return ns.getNamespaceURI();
                }
            }
        }

        return null;
    }

    @Override
    public Attribute getAttributeByName(QName nameIn) {
        if (_attrs == null) {
            return null;
        }

        String ln = nameIn.getLocalPart();
        String uri = nameIn.getNamespaceURI();

        boolean notInNs = (uri == null || uri.isEmpty());
        for (Attribute attr : _attrs) {
            QName name = attr.getName();
            if (name.getLocalPart().equals(ln)) {
                String thisUri = name.getNamespaceURI();
                if (notInNs) {
                    if (thisUri == null || thisUri.isEmpty()) {
                        return attr;
                    }
                } else {
                    if (uri.equals(thisUri)) {
                        return attr;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Iterator<Attribute> getAttributes() {
        if (_attrs == null) {
            return EmptyIterator.getInstance();
        }
        return _attrs.iterator();
    }

    /*
    /**********************************************************************
    /* Standard method impl
    /**********************************************************************
     */

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (o == null)
            return false;

        if (!(o instanceof StartElement))
            return false;

        StartElement other = (StartElement) o;

        // First things first: names must match
        if (_name.equals(other.getName())) {
            /* Rest is much trickier. I guess the easiest way is to
             * just blindly iterate through ns decls and attributes.
             * The main issue is whether ordering should matter; it will,
             * if just iterating. Would need to sort to get canonical
             * comparison.
             */
            if (iteratedEquals(getNamespaces(), other.getNamespaces())) {
                return iteratedEquals(getAttributes(), other.getAttributes());
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = _name.hashCode();
        hash = addHash(getNamespaces(), hash);
        hash = addHash(getAttributes(), hash);
        return hash;
    }
}
