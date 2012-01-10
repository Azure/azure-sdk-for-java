package com.microsoft.windowsazure.services.table.implementation;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.microsoft.windowsazure.services.blob.implementation.ISO8601DateConverter;
import com.microsoft.windowsazure.services.core.utils.DateFactory;
import com.microsoft.windowsazure.services.table.models.TableEntry;

public class AtomReaderWriter {
    private final XMLStreamFactory xmlStreamFactory;
    private final DateFactory dateFactory;
    private final ISO8601DateConverter iso8601DateConverter;

    @Inject
    public AtomReaderWriter(XMLStreamFactory xmlStreamFactory, DateFactory dateFactory,
            ISO8601DateConverter iso8601DateConverter) {
        this.xmlStreamFactory = xmlStreamFactory;
        this.dateFactory = dateFactory;
        this.iso8601DateConverter = iso8601DateConverter;
    }

    public InputStream getTableNameEntry(String table) {
        String entity = String.format("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?>"
                + "<entry xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" "
                + "xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\""
                + " xmlns=\"http://www.w3.org/2005/Atom\"> " + "<title /> " + "<updated>%s</updated>" + "<author>"
                + "  <name/> " + "</author> " + "  <id/> " + "  <content type=\"application/xml\">"
                + "    <m:properties>" + "      <d:TableName>%s</d:TableName>" + "    </m:properties>"
                + "  </content> " + "</entry>", iso8601DateConverter.format(dateFactory.getDate()), table);

        try {
            return new ByteArrayInputStream(entity.getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public List<TableEntry> parseTableEntries(InputStream stream) {
        try {
            XMLStreamReader xmlr = xmlStreamFactory.getReader(stream);

            expect(xmlr, XMLStreamConstants.START_DOCUMENT);
            expect(xmlr, XMLStreamConstants.START_ELEMENT, "feed");

            List<TableEntry> result = new ArrayList<TableEntry>();
            while (!isEndElement(xmlr, "feed")) {
                // Process "entry" elements only
                if (isStartElement(xmlr, "entry")) {
                    result.add(parseTableEntry(xmlr));
                }
                else {
                    nextSignificant(xmlr);
                }
            }

            expect(xmlr, XMLStreamConstants.END_ELEMENT, "feed");
            expect(xmlr, XMLStreamConstants.END_DOCUMENT);

            return result;
        }
        catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    public TableEntry parseTableEntry(InputStream stream) {
        try {
            XMLStreamReader xmlr = xmlStreamFactory.getReader(stream);

            expect(xmlr, XMLStreamConstants.START_DOCUMENT);
            TableEntry result = parseTableEntry(xmlr);
            expect(xmlr, XMLStreamConstants.END_DOCUMENT);

            return result;
        }
        catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    private TableEntry parseTableEntry(XMLStreamReader xmlr) throws XMLStreamException {
        TableEntry result = new TableEntry();

        expect(xmlr, XMLStreamConstants.START_ELEMENT, "entry");

        while (!isEndElement(xmlr, "entry")) {

            if (isStartElement(xmlr, "TableName")) {
                xmlr.next();
                result.setName(xmlr.getText());

                nextSignificant(xmlr);
                expect(xmlr, XMLStreamConstants.END_ELEMENT, "TableName");
            }
            else {
                nextSignificant(xmlr);
            }
        }

        expect(xmlr, XMLStreamConstants.END_ELEMENT, "entry");

        return result;
    }

    private void nextSignificant(XMLStreamReader xmlr) throws XMLStreamException {
        if (!xmlr.hasNext())
            return;
        xmlr.next();

        while (xmlr.isCharacters()) {
            if (!xmlr.hasNext())
                return;
            xmlr.next();
        }
    }

    private boolean isStartElement(XMLStreamReader xmlr, String localName) {
        return xmlr.isStartElement() && localName.equals(xmlr.getLocalName());
    }

    private boolean isEndElement(XMLStreamReader xmlr, String localName) {
        return xmlr.isEndElement() && localName.equals(xmlr.getLocalName());
    }

    private void expect(XMLStreamReader xmlr, int eventType) throws XMLStreamException {
        expect(xmlr, eventType, null);
    }

    private void expect(XMLStreamReader xmlr, int eventType, String localName) throws XMLStreamException {
        xmlr.require(eventType, null, localName);
        nextSignificant(xmlr);
    }
}
