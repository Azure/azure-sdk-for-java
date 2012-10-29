/**
 * Copyright 2012 Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.services.table.implementation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.microsoft.windowsazure.services.blob.implementation.ISO8601DateConverter;
import com.microsoft.windowsazure.services.core.utils.DateFactory;
import com.microsoft.windowsazure.services.table.EdmValueConverter;
import com.microsoft.windowsazure.services.table.models.Entity;
import com.microsoft.windowsazure.services.table.models.Property;
import com.microsoft.windowsazure.services.table.models.TableEntry;

public class AtomReaderWriter {
    private final XMLStreamFactory xmlStreamFactory;
    private final DateFactory dateFactory;
    private final ISO8601DateConverter iso8601DateConverter;
    private final EdmValueConverter edmValueConverter;

    @Inject
    public AtomReaderWriter(XMLStreamFactory xmlStreamFactory, DateFactory dateFactory,
            ISO8601DateConverter iso8601DateConverter, EdmValueConverter edmValueConverter) {
        this.xmlStreamFactory = xmlStreamFactory;
        this.dateFactory = dateFactory;
        this.iso8601DateConverter = iso8601DateConverter;
        this.edmValueConverter = edmValueConverter;
    }

    public InputStream generateTableEntry(String table) {
        final String tableTemp = table;
        return generateEntry(new PropertiesWriter() {
            @Override
            public void write(XMLStreamWriter writer) throws XMLStreamException {
                writer.writeStartElement("d:TableName");
                writer.writeCharacters(tableTemp);
                writer.writeEndElement(); // d:TableName
            }
        });
    }

    public InputStream generateEntityEntry(Entity entity) {
        final Entity entityTemp = entity;
        return generateEntry(new PropertiesWriter() {
            @Override
            public void write(XMLStreamWriter writer) throws XMLStreamException {
                for (Entry<String, Property> entry : entityTemp.getProperties().entrySet()) {
                    writer.writeStartElement("d:" + entry.getKey());

                    String edmType = entry.getValue().getEdmType();
                    if (edmType != null) {
                        writer.writeAttribute("m:type", edmType);
                    }

                    String value = edmValueConverter.serialize(edmType, entry.getValue().getValue());

                    if ((edmType != null) && (edmType == "Edm.String")) {
                        value = encodeNumericCharacterReference(value);
                    }

                    if (value != null) {
                        writer.writeCharacters(value);
                    }
                    else {
                        writer.writeAttribute("m:null", "true");
                    }

                    writer.writeEndElement(); // property name

                }
            }
        });
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

    public List<Entity> parseEntityEntries(InputStream stream) {
        try {
            XMLStreamReader xmlr = xmlStreamFactory.getReader(stream);

            expect(xmlr, XMLStreamConstants.START_DOCUMENT);
            expect(xmlr, XMLStreamConstants.START_ELEMENT, "feed");

            List<Entity> result = new ArrayList<Entity>();
            while (!isEndElement(xmlr, "feed")) {
                // Process "entry" elements only
                if (isStartElement(xmlr, "entry")) {
                    result.add(parseEntityEntry(xmlr));
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

    public Entity parseEntityEntry(InputStream stream) {
        try {
            XMLStreamReader xmlr = xmlStreamFactory.getReader(stream);

            expect(xmlr, XMLStreamConstants.START_DOCUMENT);
            Entity result = parseEntityEntry(xmlr);
            expect(xmlr, XMLStreamConstants.END_DOCUMENT);

            return result;
        }
        catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    private interface PropertiesWriter {
        void write(XMLStreamWriter writer) throws XMLStreamException;
    }

    private InputStream generateEntry(PropertiesWriter propertiesWriter) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            XMLStreamWriter writer = xmlStreamFactory.getWriter(stream);
            writer.writeStartDocument("utf-8", "1.0");

            writer.writeStartElement("entry");
            writer.writeAttribute("xmlns:d", "http://schemas.microsoft.com/ado/2007/08/dataservices");
            writer.writeAttribute("xmlns:m", "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata");
            writer.writeAttribute("xmlns", "http://www.w3.org/2005/Atom");

            writer.writeStartElement("title");
            writer.writeEndElement(); // title

            writer.writeStartElement("updated");
            writer.writeCharacters(iso8601DateConverter.format(dateFactory.getDate()));
            writer.writeEndElement(); // updated

            writer.writeStartElement("author");
            writer.writeStartElement("name");
            writer.writeEndElement(); // name
            writer.writeEndElement(); // author

            writer.writeStartElement("id");
            writer.writeEndElement(); // id

            writer.writeStartElement("content");
            writer.writeAttribute("type", "application/xml");

            writer.writeStartElement("m:properties");
            propertiesWriter.write(writer);
            writer.writeEndElement(); // m:properties

            writer.writeEndElement(); // content

            writer.writeEndElement(); // entry

            writer.writeEndDocument();
            writer.close();

            return new ByteArrayInputStream(stream.toByteArray());
        }
        catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    private TableEntry parseTableEntry(XMLStreamReader xmlr) throws XMLStreamException {
        TableEntry result = new TableEntry();

        expect(xmlr, XMLStreamConstants.START_ELEMENT, "entry");

        while (!isEndElement(xmlr, "entry")) {
            if (isStartElement(xmlr, "properties")) {
                Map<String, Property> properties = parseEntryProperties(xmlr);

                result.setName((String) properties.get("TableName").getValue());
            }
            else {
                nextSignificant(xmlr);
            }
        }

        expect(xmlr, XMLStreamConstants.END_ELEMENT, "entry");

        return result;
    }

    private Entity parseEntityEntry(XMLStreamReader xmlr) throws XMLStreamException {
        Entity result = new Entity();

        result.setEtag(xmlr.getAttributeValue(null, "etag"));
        expect(xmlr, XMLStreamConstants.START_ELEMENT, "entry");

        while (!isEndElement(xmlr, "entry")) {
            if (isStartElement(xmlr, "properties")) {
                result.setProperties(parseEntryProperties(xmlr));
            }
            else {
                nextSignificant(xmlr);
            }
        }

        expect(xmlr, XMLStreamConstants.END_ELEMENT, "entry");

        return result;
    }

    private Map<String, Property> parseEntryProperties(XMLStreamReader xmlr) throws XMLStreamException {
        Map<String, Property> result = new HashMap<String, Property>();

        expect(xmlr, XMLStreamConstants.START_ELEMENT, "properties");

        while (!isEndElement(xmlr, "properties")) {
            String name = xmlr.getLocalName();
            String edmType = xmlr.getAttributeValue(null, "type");

            xmlr.next();

            // Use concatenation instead of StringBuilder as most text is just one element.
            String serializedValue = "";
            while (!xmlr.isEndElement()) {
                serializedValue += xmlr.getText();
                xmlr.next();
            }

            Object value = edmValueConverter.deserialize(edmType, serializedValue);

            result.put(name, new Property().setEdmType(edmType).setValue(value));

            expect(xmlr, XMLStreamConstants.END_ELEMENT, name);
        }

        expect(xmlr, XMLStreamConstants.END_ELEMENT, "properties");

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

    private String encodeNumericCharacterReference(String value) {
        if (value == null) {
            return null;
        }
        else {
            char[] charArray = value.toCharArray();
            StringBuffer stringBuffer = new StringBuffer();
            for (int index = 0; index < charArray.length; index++) {
                if (charArray[index] < 0x20 || charArray[index] > 0x7f)
                    stringBuffer.append("&#x").append(Integer.toHexString(charArray[index])).append(";");
                else
                    stringBuffer.append(charArray[index]);
            }
            return stringBuffer.toString();
        }
    }
}
