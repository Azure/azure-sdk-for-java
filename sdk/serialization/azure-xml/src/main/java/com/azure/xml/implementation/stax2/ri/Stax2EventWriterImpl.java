// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.ri;

import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.*;
import javax.xml.stream.events.*;

import com.azure.xml.implementation.stax2.*;
import com.azure.xml.implementation.stax2.evt.XMLEvent2;

/**
 * Simple implementation of {@link XMLEventWriter}.
 */
public class Stax2EventWriterImpl implements XMLEventWriter, XMLStreamConstants {
    final protected XMLStreamWriter2 _writer;

    /*
    /**********************************************************************
    /* Construction, init
    /**********************************************************************
     */

    public Stax2EventWriterImpl(XMLStreamWriter2 sw) {
        _writer = sw;
    }

    /*
    /**********************************************************************
    /* XMLEventWriter API
    /**********************************************************************
     */

    /**
     * Basic implementation of the method which will use event implementations
     * available as part of the reference implementation.
     *<p>
     * Note: ALL events (except for custom ones ref. impl. itself doesn't
     * produce, and thus may not always be able to deal with) are routed
     * through stream writer. This because it may want to do
     * different kinds of validation
     */
    @Override
    public void add(XMLEvent event) throws XMLStreamException {
        switch (event.getEventType()) {
            /* First events that we have to route via stream writer, to
             * get and/or update namespace information:
             */

            case ATTRIBUTE: // need to pass to stream writer, to get namespace info
            {
                Attribute attr = (Attribute) event;
                QName name = attr.getName();
                _writer.writeAttribute(name.getPrefix(), name.getNamespaceURI(), name.getLocalPart(), attr.getValue());
            }
                break;

            case END_DOCUMENT:
                _writer.writeEndDocument();
                break;

            case END_ELEMENT:
                _writer.writeEndElement();
                break;

            case NAMESPACE: {
                Namespace ns = (Namespace) event;
                _writer.writeNamespace(ns.getPrefix(), ns.getNamespaceURI());
            }
                break;

            case START_DOCUMENT: {
                StartDocument sd = (StartDocument) event;
                if (!sd.encodingSet()) { // encoding defined?
                    _writer.writeStartDocument(sd.getVersion());
                } else if (sd.standaloneSet()) {
                    _writer.writeStartDocument(sd.getVersion(), sd.getCharacterEncodingScheme(), sd.isStandalone());
                } else {
                    _writer.writeStartDocument(sd.getCharacterEncodingScheme(), sd.getVersion());
                }
            }
                break;

            case START_ELEMENT:

            {
                StartElement se = event.asStartElement();
                QName n = se.getName();
                _writer.writeStartElement(n.getPrefix(), n.getLocalPart(), n.getNamespaceURI());
                Iterator<?> it = se.getNamespaces();
                while (it.hasNext()) {
                    Namespace ns = (Namespace) it.next();
                    add(ns);
                }
                it = se.getAttributes();
                while (it.hasNext()) {
                    Attribute attr = (Attribute) it.next();
                    add(attr);
                }
            }
                break;

            /* Then events we could output directly if necessary... but that
             * make sense to route via stream writer, for validation
             * purposes.
             */

            case CHARACTERS: // better pass to stream writer, for prolog/epilog validation
            {
                Characters ch = event.asCharacters();
                String text = ch.getData();
                if (ch.isCData()) {
                    _writer.writeCData(text);
                } else {
                    _writer.writeCharacters(text);
                }
            }
                break;

            case CDATA:
                _writer.writeCData(event.asCharacters().getData());
                break;

            case COMMENT:
                _writer.writeComment(((Comment) event).getText());
                break;

            case DTD:
                _writer.writeDTD(((DTD) event).getDocumentTypeDeclaration());
                break;

            case ENTITY_REFERENCE:
                _writer.writeEntityRef(((EntityReference) event).getName());
                break;

            case PROCESSING_INSTRUCTION: // let's just write directly
            {
                ProcessingInstruction pi = (ProcessingInstruction) event;
                _writer.writeProcessingInstruction(pi.getTarget(), pi.getData());
            }
                break;

            case ENTITY_DECLARATION: // not yet produced by Wstx
            case NOTATION_DECLARATION: // not yet produced by Wstx
            case SPACE: // usually only CHARACTERS events exist...
            default:

                // Easy, if stax2 enabled
                if (event instanceof XMLEvent2) {
                    ((XMLEvent2) event).writeUsing(_writer);
                } else {
                    // Otherwise... well, no real way to do it in generic manner
                    throw new XMLStreamException("Don't know how to output event " + event);
                }
        }
    }

    @Override
    public void add(XMLEventReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            add(reader.nextEvent());
        }
    }

    @Override
    public void close() throws XMLStreamException {
        _writer.close();
    }

    @Override
    public void flush() throws XMLStreamException {
        _writer.flush();
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return _writer.getNamespaceContext();
    }

    @Override
    public String getPrefix(String uri) throws XMLStreamException {
        return _writer.getPrefix(uri);
    }

    @Override
    public void setDefaultNamespace(String uri) throws XMLStreamException {
        _writer.setDefaultNamespace(uri);
    }

    @Override
    public void setNamespaceContext(NamespaceContext ctxt) throws XMLStreamException {
        _writer.setNamespaceContext(ctxt);
    }

    @Override
    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        _writer.setPrefix(prefix, uri);
    }
}
