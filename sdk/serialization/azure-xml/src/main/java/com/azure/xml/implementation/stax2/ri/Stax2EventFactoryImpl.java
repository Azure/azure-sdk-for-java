// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.ri;

import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.*;
import javax.xml.stream.events.*;

import com.azure.xml.implementation.stax2.evt.XMLEventFactory2;
import com.azure.xml.implementation.stax2.evt.DTD2;
import com.azure.xml.implementation.stax2.ri.evt.*;

/**
 * This is an abstract almost complete plain vanilla implementation of
 * {@link XMLEventFactory2}.
 * It can be used as a building block for concrete implementations:
 * the minimal requirement is to implement <code>createQName</code>
 * methods.
 *<p>
 * Note that due to underlying Stax factory being non-generic (it was specified
 * for JDK 1.4, pre-generic), no generic typing can be added to various
 * {@link java.util.Iterator}s in signatures.
 *
 * @author Tatu Saloranta
 *
 * @since 3.0
 */
public abstract class Stax2EventFactoryImpl extends XMLEventFactory2 {
    protected Location mLocation;

    public Stax2EventFactoryImpl() {
    }

    /*
    /**********************************************************************
    /* XMLEventFactory API
    /**********************************************************************
     */

    @Override
    public Attribute createAttribute(QName name, String value) {
        return new AttributeEventImpl(mLocation, name, value, true);
    }

    @Override
    public Attribute createAttribute(String localName, String value) {
        return new AttributeEventImpl(mLocation, localName, null, null, value, true);
    }

    @Override
    public Attribute createAttribute(String prefix, String nsURI, String localName, String value) {
        return new AttributeEventImpl(mLocation, localName, nsURI, prefix, value, true);
    }

    @Override
    public Characters createCData(String content) {
        return new CharactersEventImpl(mLocation, content, true);
    }

    @Override
    public Characters createCharacters(String content) {
        return new CharactersEventImpl(mLocation, content, false);
    }

    @Override
    public Comment createComment(String text) {
        return new CommentEventImpl(mLocation, text);
    }

    /**
     * Note: constructing DTD events this way means that there will be no
     * internal presentation of actual DTD; no parsing is implied by
     * construction.
     */
    @Override
    public DTD createDTD(String dtd) {
        return new DTDEventImpl(mLocation, dtd);
    }

    @Override
    public EndDocument createEndDocument() {
        return new EndDocumentEventImpl(mLocation);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" }) // due to Stax's non-generic-ness
    @Override
    public EndElement createEndElement(QName name, Iterator namespaces) {
        return new EndElementEventImpl(mLocation, name, (Iterator<Namespace>) namespaces);
    }

    @Override
    public EndElement createEndElement(String prefix, String nsURI, String localName) {
        return createEndElement(createQName(nsURI, localName, prefix), null);
    }

    @SuppressWarnings("rawtypes") // due to Stax's non-generic-ness
    @Override
    public EndElement createEndElement(String prefix, String nsURI, String localName, Iterator ns) {
        return createEndElement(createQName(nsURI, localName, prefix), ns);
    }

    @Override
    public EntityReference createEntityReference(String name, EntityDeclaration decl) {
        return new EntityReferenceEventImpl(mLocation, decl);
    }

    @Override
    public Characters createIgnorableSpace(String content) {
        return CharactersEventImpl.createIgnorableWS(mLocation, content);
    }

    @Override
    public Namespace createNamespace(String nsURI) {
        return NamespaceEventImpl.constructDefaultNamespace(mLocation, nsURI);
    }

    @Override
    public Namespace createNamespace(String prefix, String nsURI) {
        return NamespaceEventImpl.constructNamespace(mLocation, prefix, nsURI);
    }

    @Override
    public ProcessingInstruction createProcessingInstruction(String target, String data) {
        return new ProcInstrEventImpl(mLocation, target, data);
    }

    @Override
    public Characters createSpace(String content) {
        return CharactersEventImpl.createNonIgnorableWS(mLocation, content);
    }

    @Override
    public StartDocument createStartDocument() {
        return new StartDocumentEventImpl(mLocation);
    }

    @Override
    public StartDocument createStartDocument(String encoding) {
        return new StartDocumentEventImpl(mLocation, encoding);
    }

    @Override
    public StartDocument createStartDocument(String encoding, String version) {
        return new StartDocumentEventImpl(mLocation, encoding, version);
    }

    @Override
    public StartDocument createStartDocument(String encoding, String version, boolean standalone) {
        return new StartDocumentEventImpl(mLocation, encoding, version, true, standalone);
    }

    @SuppressWarnings("rawtypes") // due to Stax's non-generic-ness
    @Override
    public StartElement createStartElement(QName name, Iterator attr, Iterator ns) {
        return createStartElement(name, attr, ns, null);
    }

    @Override
    public StartElement createStartElement(String prefix, String nsURI, String localName) {
        return createStartElement(createQName(nsURI, localName, prefix), null, null, null);
    }

    @SuppressWarnings("rawtypes") // due to Stax's non-generic-ness
    @Override
    public StartElement createStartElement(String prefix, String nsURI, String localName, Iterator attr, Iterator ns) {
        return createStartElement(createQName(nsURI, localName, prefix), attr, ns, null);
    }

    @SuppressWarnings("rawtypes") // due to Stax's non-generic-ness
    @Override
    public StartElement createStartElement(String prefix, String nsURI, String localName, Iterator attr, Iterator ns,
        NamespaceContext nsCtxt) {
        return createStartElement(createQName(nsURI, localName, prefix), attr, ns, nsCtxt);
    }

    @Override
    public void setLocation(Location loc) {
        mLocation = loc;
    }

    /*
    /**********************************************************************
    /* XMLEventFactory2 methods
    /**********************************************************************
     */

    @Override
    public DTD2 createDTD(String rootName, String sysId, String pubId, String intSubset) {
        return new DTDEventImpl(mLocation, rootName, sysId, pubId, intSubset, null);
    }

    @Override
    public DTD2 createDTD(String rootName, String sysId, String pubId, String intSubset, Object processedDTD) {
        return new DTDEventImpl(mLocation, rootName, sysId, pubId, intSubset, processedDTD);
    }

    /*
    /**********************************************************************
    /* Helper methods, overridable
    /**********************************************************************
     */

    protected abstract QName createQName(String nsURI, String localName);

    protected abstract QName createQName(String nsURI, String localName, String prefix);

    protected StartElement createStartElement(QName name, Iterator<?> attr, Iterator<?> ns, NamespaceContext ctxt) {
        return StartElementEventImpl.construct(mLocation, name, attr, ns, ctxt);
    }
}
