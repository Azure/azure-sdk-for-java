/**
 * Copyright 2011 Microsoft Corporation
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

package com.microsoft.windowsazure.services.table.client;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang3.StringEscapeUtils;

import com.microsoft.windowsazure.services.core.storage.Constants;
import com.microsoft.windowsazure.services.core.storage.OperationContext;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.core.storage.utils.Utility;

/**
 * Reserved for internal use. A class used to read and write Table entities in OData AtomPub format requests and
 * responses.
 * <p>
 * For more information about OData, see the <a href="http://www.odata.org/">Open Data Protocol</a> website. For more
 * information about the AtomPub format used in OData, see <a
 * href="http://www.odata.org/developers/protocols/atom-format">OData Protocol Atom Format</a>.
 */
class AtomPubParser {
    /**
     * Reserved for internal use. A static factory method to construct an <code>XMLStreamWriter</code> instance based on
     * the specified <code>OutputStream</code>.
     * 
     * @param outStream
     *            The <code>OutputStream</code> instance to create an <code>XMLStreamWriter</code> on.
     * @return
     *         An <code>XMLStreamWriter</code> instance based on the specified <code>OutputStream</code>.
     * @throws XMLStreamException
     *             if an error occurs while creating the stream.
     */
    protected static XMLStreamWriter generateTableWriter(final OutputStream outStream) throws XMLStreamException {
        final XMLOutputFactory xmlOutFactoryInst = XMLOutputFactory.newInstance();
        return xmlOutFactoryInst.createXMLStreamWriter(outStream, "UTF-8");
    }

    /**
     * Reserved for internal use. Parses the operation response as an entity. Parses the result returned in the
     * specified stream in AtomPub format into a {@link TableResult} containing an entity of the specified class type
     * projected using the specified resolver.
     * 
     * @param xmlr
     *            An <code>XMLStreamReader</code> on the input stream.
     * @param clazzType
     *            The class type <code>T</code> implementing {@link TableEntity} for the entity returned. Set to
     *            <code>null</code> to ignore the returned entity and copy only response properties into the
     *            {@link TableResult} object.
     * @param resolver
     *            An {@link EntityResolver} instance to project the entity into an instance of type <code>R</code>. Set
     *            to <code>null</code> to return the entity as an instance of the class type <code>T</code>.
     * @param opContext
     *            An {@link OperationContext} object used to track the execution of the operation.
     * @return
     *         A {@link TableResult} containing the parsed entity result of the operation.
     * 
     * @throws XMLStreamException
     *             if an error occurs while accessing the stream.
     * @throws ParseException
     *             if an error occurs while parsing the stream.
     * @throws InstantiationException
     *             if an error occurs while constructing the result.
     * @throws IllegalAccessException
     *             if an error occurs in reflection while parsing the result.
     * @throws StorageException
     *             if a storage service error occurs.
     */
    protected static <T extends TableEntity, R> TableResult parseEntity(final XMLStreamReader xmlr,
            final Class<T> clazzType, final EntityResolver<R> resolver, final OperationContext opContext)
            throws XMLStreamException, ParseException, InstantiationException, IllegalAccessException, StorageException {
        int eventType = xmlr.getEventType();
        final TableResult res = new TableResult();

        xmlr.require(XMLStreamConstants.START_ELEMENT, null, ODataConstants.ENTRY);

        res.setEtag(StringEscapeUtils.unescapeHtml4(xmlr.getAttributeValue(ODataConstants.DATA_SERVICES_METADATA_NS,
                ODataConstants.ETAG)));

        while (xmlr.hasNext()) {
            eventType = xmlr.next();
            if (eventType == XMLStreamConstants.CHARACTERS) {
                xmlr.getText();
                continue;
            }

            final String name = xmlr.getName().toString();

            if (eventType == XMLStreamConstants.START_ELEMENT) {
                if (name.equals(ODataConstants.BRACKETED_ATOM_NS + ODataConstants.ID)) {
                    res.setId(Utility.readElementFromXMLReader(xmlr, ODataConstants.ID));
                }
                else if (name.equals(ODataConstants.BRACKETED_DATA_SERVICES_METADATA_NS + ODataConstants.PROPERTIES)) {
                    // Do read properties
                    if (resolver == null && clazzType == null) {
                        return res;
                    }
                    else {
                        res.setProperties(readProperties(xmlr, opContext));
                        break;
                    }
                }
            }
        }

        // Move to end Content
        eventType = xmlr.next();
        if (eventType == XMLStreamConstants.CHARACTERS) {
            eventType = xmlr.next();
        }
        xmlr.require(XMLStreamConstants.END_ELEMENT, null, ODataConstants.CONTENT);

        eventType = xmlr.next();
        if (eventType == XMLStreamConstants.CHARACTERS) {
            eventType = xmlr.next();
        }

        xmlr.require(XMLStreamConstants.END_ELEMENT, null, ODataConstants.ENTRY);

        String rowKey = null;
        String partitionKey = null;
        Date timestamp = null;

        // Remove core properties from map and set individually
        EntityProperty tempProp = res.getProperties().get(TableConstants.PARTITION_KEY);
        if (tempProp != null) {
            res.getProperties().remove(TableConstants.PARTITION_KEY);
            partitionKey = tempProp.getValueAsString();
        }

        tempProp = res.getProperties().get(TableConstants.ROW_KEY);
        if (tempProp != null) {
            res.getProperties().remove(TableConstants.ROW_KEY);
            rowKey = tempProp.getValueAsString();
        }

        tempProp = res.getProperties().get(TableConstants.TIMESTAMP);
        if (tempProp != null) {
            res.getProperties().remove(TableConstants.TIMESTAMP);
            timestamp = tempProp.getValueAsDate();
        }

        if (resolver != null) {
            // Call resolver
            res.setResult(resolver.resolve(partitionKey, rowKey, timestamp, res.getProperties(), res.getEtag()));
        }
        else if (clazzType != null) {
            // Generate new entity and return
            final T entity = clazzType.newInstance();
            entity.setEtag(res.getEtag());

            entity.setPartitionKey(partitionKey);
            entity.setRowKey(rowKey);
            entity.setTimestamp(timestamp);

            entity.readEntity(res.getProperties(), opContext);

            res.setResult(entity);
        }

        return res;
    }

    /**
     * Reserved for internal use. Parses the operation response as a collection of entities. Reads entity data from the
     * specified input stream using the specified class type and optionally projects each entity result with the
     * specified resolver into an {@link ODataPayload} containing a collection of {@link TableResult} objects. .
     * 
     * @param inStream
     *            The <code>InputStream</code> to read the data to parse from.
     * @param clazzType
     *            The class type <code>T</code> implementing {@link TableEntity} for the entities returned. Set to
     *            <code>null</code> to ignore the returned entities and copy only response properties into the
     *            {@link TableResult} objects.
     * @param resolver
     *            An {@link EntityResolver} instance to project the entities into instances of type <code>R</code>. Set
     *            to <code>null</code> to return the entities as instances of the class type <code>T</code>.
     * @param opContext
     *            An {@link OperationContext} object used to track the execution of the operation.
     * @return
     *         An {@link ODataPayload} containing a collection of {@link TableResult} objects with the parsed operation
     *         response.
     * 
     * @throws XMLStreamException
     *             if an error occurs while accessing the stream.
     * @throws ParseException
     *             if an error occurs while parsing the stream.
     * @throws InstantiationException
     *             if an error occurs while constructing the result.
     * @throws IllegalAccessException
     *             if an error occurs in reflection while parsing the result.
     * @throws StorageException
     *             if a storage service error occurs.
     */
    @SuppressWarnings("unchecked")
    protected static <T extends TableEntity, R> ODataPayload<?> parseResponse(final InputStream inStream,
            final Class<T> clazzType, final EntityResolver<R> resolver, final OperationContext opContext)
            throws XMLStreamException, ParseException, InstantiationException, IllegalAccessException, StorageException {
        ODataPayload<T> corePayload = null;
        ODataPayload<R> resolvedPayload = null;
        ODataPayload<?> commonPayload = null;

        if (resolver != null) {
            resolvedPayload = new ODataPayload<R>();
            commonPayload = resolvedPayload;
        }
        else {
            corePayload = new ODataPayload<T>();
            commonPayload = corePayload;
        }

        final XMLStreamReader xmlr = Utility.createXMLStreamReaderFromStream(inStream);
        int eventType = xmlr.getEventType();
        xmlr.require(XMLStreamConstants.START_DOCUMENT, null, null);
        eventType = xmlr.next();

        xmlr.require(XMLStreamConstants.START_ELEMENT, null, ODataConstants.FEED);
        // skip feed chars
        eventType = xmlr.next();

        while (xmlr.hasNext()) {
            eventType = xmlr.next();

            if (eventType == XMLStreamConstants.CHARACTERS) {
                xmlr.getText();
                continue;
            }

            final String name = xmlr.getName().toString();

            if (eventType == XMLStreamConstants.START_ELEMENT) {
                if (name.equals(ODataConstants.BRACKETED_ATOM_NS + ODataConstants.ENTRY)) {
                    final TableResult res = parseEntity(xmlr, clazzType, resolver, opContext);
                    if (corePayload != null) {
                        corePayload.tableResults.add(res);
                    }

                    if (resolver != null) {
                        resolvedPayload.results.add((R) res.getResult());
                    }
                    else {
                        corePayload.results.add((T) res.getResult());
                    }
                }
            }
            else if (eventType == XMLStreamConstants.END_ELEMENT
                    && name.equals(ODataConstants.BRACKETED_ATOM_NS + ODataConstants.FEED)) {
                break;
            }
        }

        xmlr.require(XMLStreamConstants.END_ELEMENT, null, ODataConstants.FEED);
        return commonPayload;
    }

    /**
     * Reserved for internal use. Parses the operation response as an entity. Reads entity data from the specified
     * <code>XMLStreamReader</code> using the specified class type and optionally projects the entity result with the
     * specified resolver into a {@link TableResult} object.
     * 
     * @param xmlr
     *            The <code>XMLStreamReader</code> to read the data to parse from.
     * @param httpStatusCode
     *            The HTTP status code returned with the operation response.
     * @param clazzType
     *            The class type <code>T</code> implementing {@link TableEntity} for the entity returned. Set to
     *            <code>null</code> to ignore the returned entity and copy only response properties into the
     *            {@link TableResult} object.
     * @param resolver
     *            An {@link EntityResolver} instance to project the entity into an instance of type <code>R</code>. Set
     *            to <code>null</code> to return the entitys as instance of the class type <code>T</code>.
     * @param opContext
     *            An {@link OperationContext} object used to track the execution of the operation.
     * @return
     *         A {@link TableResult} object with the parsed operation response.
     * 
     * @throws XMLStreamException
     *             if an error occurs while accessing the stream.
     * @throws ParseException
     *             if an error occurs while parsing the stream.
     * @throws InstantiationException
     *             if an error occurs while constructing the result.
     * @throws IllegalAccessException
     *             if an error occurs in reflection while parsing the result.
     * @throws StorageException
     *             if a storage service error occurs.
     */
    protected static <T extends TableEntity, R> TableResult parseSingleOpResponse(final XMLStreamReader xmlr,
            final int httpStatusCode, final Class<T> clazzType, final EntityResolver<R> resolver,
            final OperationContext opContext) throws XMLStreamException, ParseException, InstantiationException,
            IllegalAccessException, StorageException {
        xmlr.require(XMLStreamConstants.START_DOCUMENT, null, null);
        xmlr.next();

        final TableResult res = parseEntity(xmlr, clazzType, resolver, opContext);
        res.setHttpStatusCode(httpStatusCode);
        return res;
    }

    /**
     * Reserved for internal use. Reads the properties of an entity from the stream into a map of property names to
     * typed values. Reads the entity data as an AtomPub Entry Resource from the specified {@link XMLStreamReader} into
     * a map of <code>String</code> property names to {@link EntityProperty} data typed values.
     * 
     * @param xmlr
     *            The <code>XMLStreamReader</code> to read the data from.
     * @param opContext
     *            An {@link OperationContext} object used to track the execution of the operation.
     * 
     * @return
     *         A <code>java.util.HashMap</code> containing a map of <code>String</code> property names to
     *         {@link EntityProperty} data typed values found in the entity data.
     * @throws XMLStreamException
     *             if an error occurs accessing the stream.
     * @throws ParseException
     *             if an error occurs converting the input to a particular data type.
     */
    protected static HashMap<String, EntityProperty> readProperties(final XMLStreamReader xmlr,
            final OperationContext opContext) throws XMLStreamException, ParseException {
        int eventType = xmlr.getEventType();
        xmlr.require(XMLStreamConstants.START_ELEMENT, null, ODataConstants.PROPERTIES);
        final HashMap<String, EntityProperty> properties = new HashMap<String, EntityProperty>();

        while (xmlr.hasNext()) {
            eventType = xmlr.next();
            if (eventType == XMLStreamConstants.CHARACTERS) {
                xmlr.getText();
                continue;
            }

            if (eventType == XMLStreamConstants.START_ELEMENT
                    && xmlr.getNamespaceURI().equals(ODataConstants.DATA_SERVICES_NS)) {
                final String key = xmlr.getLocalName();
                String val = Constants.EMPTY_STRING;
                String edmType = null;

                if (xmlr.getAttributeCount() > 0) {
                    edmType = xmlr.getAttributeValue(ODataConstants.DATA_SERVICES_METADATA_NS, ODataConstants.TYPE);
                }

                // move to chars
                eventType = xmlr.next();

                if (eventType == XMLStreamConstants.CHARACTERS) {
                    val = xmlr.getText();

                    // end element
                    eventType = xmlr.next();
                }

                xmlr.require(XMLStreamConstants.END_ELEMENT, null, key);

                final EntityProperty newProp = new EntityProperty(val, EdmType.parse(edmType));
                properties.put(key, newProp);
            }
            else if (eventType == XMLStreamConstants.END_ELEMENT
                    && xmlr.getName().toString()
                            .equals(ODataConstants.BRACKETED_DATA_SERVICES_METADATA_NS + ODataConstants.PROPERTIES)) {
                // End read properties
                break;
            }
        }

        xmlr.require(XMLStreamConstants.END_ELEMENT, null, ODataConstants.PROPERTIES);
        return properties;
    }

    /**
     * Reserved for internal use. Writes an entity to the stream as an AtomPub Entry Resource, leaving the stream open
     * for additional writing.
     * 
     * @param entity
     *            The instance implementing {@link TableEntity} to write to the output stream.
     * @param isTableEntry
     *            A flag indicating the entity is a reference to a table at the top level of the storage service when
     *            <code>true<code> and a reference to an entity within a table when <code>false</code>.
     * @param xmlw
     *            The <code>XMLStreamWriter</code> to write the entity to.
     * @param opContext
     *            An {@link OperationContext} object used to track the execution of the operation.
     * 
     * @throws XMLStreamException
     *             if an error occurs accessing the stream.
     * @throws StorageException
     *             if a Storage service error occurs.
     */
    protected static void writeEntityToStream(final TableEntity entity, final boolean isTableEntry,
            final XMLStreamWriter xmlw, final OperationContext opContext) throws XMLStreamException, StorageException {
        final HashMap<String, EntityProperty> properties = entity.writeEntity(opContext);
        if (properties == null) {
            throw new IllegalArgumentException("Entity did not produce properties to serialize");
        }

        if (!isTableEntry) {
            Utility.assertNotNullOrEmpty(TableConstants.PARTITION_KEY, entity.getPartitionKey());
            Utility.assertNotNullOrEmpty(TableConstants.ROW_KEY, entity.getRowKey());
            Utility.assertNotNull(TableConstants.TIMESTAMP, entity.getTimestamp());
        }

        // Begin entry
        xmlw.writeStartElement("entry");
        xmlw.writeNamespace("d", ODataConstants.DATA_SERVICES_NS);
        xmlw.writeNamespace("m", ODataConstants.DATA_SERVICES_METADATA_NS);

        // default namespace
        xmlw.writeNamespace(null, ODataConstants.ATOM_NS);

        // Content
        xmlw.writeStartElement(ODataConstants.CONTENT);
        xmlw.writeAttribute(ODataConstants.TYPE, ODataConstants.ODATA_CONTENT_TYPE);

        // m:properties
        xmlw.writeStartElement("m", ODataConstants.PROPERTIES, ODataConstants.DATA_SERVICES_METADATA_NS);

        if (!isTableEntry) {
            // d:PartitionKey
            xmlw.writeStartElement("d", TableConstants.PARTITION_KEY, ODataConstants.DATA_SERVICES_NS);
            xmlw.writeAttribute("xml", "xml", "space", "preserve");
            xmlw.writeCharacters(entity.getPartitionKey());
            xmlw.writeEndElement();

            // d:RowKey
            xmlw.writeStartElement("d", TableConstants.ROW_KEY, ODataConstants.DATA_SERVICES_NS);
            xmlw.writeAttribute("xml", "xml", "space", "preserve");
            xmlw.writeCharacters(entity.getRowKey());
            xmlw.writeEndElement();

            // d:Timestamp
            if (entity.getTimestamp() == null) {
                entity.setTimestamp(new Date());
            }

            xmlw.writeStartElement("d", TableConstants.TIMESTAMP, ODataConstants.DATA_SERVICES_NS);
            xmlw.writeAttribute("m", ODataConstants.DATA_SERVICES_METADATA_NS, ODataConstants.TYPE,
                    EdmType.DATE_TIME.toString());
            xmlw.writeCharacters(Utility.getTimeByZoneAndFormat(entity.getTimestamp(), Utility.UTC_ZONE,
                    Utility.ISO8061_LONG_PATTERN));
            xmlw.writeEndElement();
        }

        for (final Entry<String, EntityProperty> ent : properties.entrySet()) {
            if (ent.getKey().equals(TableConstants.PARTITION_KEY) || ent.getKey().equals(TableConstants.ROW_KEY)
                    || ent.getKey().equals(TableConstants.TIMESTAMP) || ent.getKey().equals("Etag")) {
                continue;
            }

            EntityProperty currProp = ent.getValue();

            // d:PropName
            xmlw.writeStartElement("d", ent.getKey(), ODataConstants.DATA_SERVICES_NS);

            if (currProp.getEdmType() == EdmType.STRING) {
                xmlw.writeAttribute("xml", "xml", "space", "preserve");
            }
            else if (currProp.getEdmType().toString().length() != 0) {
                String edmTypeString = currProp.getEdmType().toString();
                if (edmTypeString.length() != 0) {
                    xmlw.writeAttribute("m", ODataConstants.DATA_SERVICES_METADATA_NS, ODataConstants.TYPE,
                            edmTypeString);
                }
            }

            if (currProp.getIsNull()) {
                xmlw.writeAttribute("m", ODataConstants.DATA_SERVICES_METADATA_NS, ODataConstants.NULL, Constants.TRUE);
            }

            // Write Value
            xmlw.writeCharacters(currProp.getValueAsString());
            // End d:PropName
            xmlw.writeEndElement();
        }

        // End m:properties
        xmlw.writeEndElement();

        // End content
        xmlw.writeEndElement();

        // End entry
        xmlw.writeEndElement();
    }

    /**
     * Reserved for internal use. Writes a single entity to the specified <code>OutputStream</code> as a complete XML
     * document.
     * 
     * @param entity
     *            The instance implementing {@link TableEntity} to write to the output stream.
     * @param isTableEntry
     *            A flag indicating the entity is a reference to a table at the top level of the storage service when
     *            <code>true<code> and a reference to an entity within a table when <code>false</code>.
     * @param outStream
     *            The <code>OutputStream</code> to write the entity to.
     * @param opContext
     *            An {@link OperationContext} object used to track the execution of the operation.
     * 
     * @throws XMLStreamException
     *             if an error occurs creating or accessing the stream.
     * @throws StorageException
     *             if a Storage service error occurs.
     */
    protected static void writeSingleEntityToStream(final TableEntity entity, final boolean isTableEntry,
            final OutputStream outStream, final OperationContext opContext) throws XMLStreamException, StorageException {
        final XMLStreamWriter xmlw = AtomPubParser.generateTableWriter(outStream);
        writeSingleEntityToStream(entity, isTableEntry, xmlw, opContext);
    }

    /**
     * Reserved for internal use. Writes a single entity to the specified <code>XMLStreamWriter</code> as a complete XML
     * document.
     * 
     * @param entity
     *            The instance implementing {@link TableEntity} to write to the output stream.
     * @param isTableEntry
     *            A flag indicating the entity is a reference to a table at the top level of the storage service when
     *            <code>true<code> and a reference to an entity within a table when <code>false</code>.
     * @param xmlw
     *            The <code>XMLStreamWriter</code> to write the entity to.
     * @param opContext
     *            An {@link OperationContext} object used to track the execution of the operation.
     * 
     * @throws XMLStreamException
     *             if an error occurs creating or accessing the stream.
     * @throws StorageException
     *             if a Storage service error occurs.
     */
    protected static void writeSingleEntityToStream(final TableEntity entity, final boolean isTableEntry,
            final XMLStreamWriter xmlw, final OperationContext opContext) throws XMLStreamException, StorageException {
        // default is UTF8
        xmlw.writeStartDocument("UTF-8", "1.0");

        writeEntityToStream(entity, isTableEntry, xmlw, opContext);

        // end doc
        xmlw.writeEndDocument();
        xmlw.flush();
    }
}
