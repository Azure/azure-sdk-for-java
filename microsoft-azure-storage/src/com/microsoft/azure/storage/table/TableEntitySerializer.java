/**
 * Copyright Microsoft Corporation
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
package com.microsoft.azure.storage.table;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.core.Utility;

/**
 * RESERVED FOR INTERNAL USE. A class used to serialize table entities to a byte array.
 */
final class TableEntitySerializer {
    /**
     * Used to create Json parsers and generators.
     */
    private static JsonFactory jsonFactory = new JsonFactory();

    /**
     * Reserved for internal use. Writes an entity to the stream, leaving the stream open for additional writing.
     * 
     * @param outStream
     *            The <code>OutputStream</code> to write the entity to.
     * @param format
     *            The {@link TablePayloadFormat} to use for parsing.
     * @param entity
     *            The instance implementing {@link TableEntity} to write to the output stream.
     * @param isTableEntry
     *            A flag indicating the entity is a reference to a table at the top level of the storage service when
     *            <code>true<code> and a reference to an entity within a table when <code>false</code>.
     * @param opContext
     *            An {@link OperationContext} object used to track the execution of the operation.
     * @throws XMLStreamException
     *             if an error occurs while accessing the stream with AtomPub.
     * @throws StorageException
     *             if a Storage service error occurs.
     * @throws IOException
     *             if an error occurs while accessing the stream with Json.
     */
    @SuppressWarnings("deprecation")
    static void writeSingleEntityToStream(final OutputStream outStream, final TablePayloadFormat format,
            final TableEntity entity, final boolean isTableEntry, final OperationContext opContext)
            throws XMLStreamException, StorageException, IOException {
        if (format == TablePayloadFormat.AtomPub) {
            writeSingleAtomEntity(outStream, entity, isTableEntry, opContext);
        }
        else {
            writeSingleJsonEntity(outStream, format, entity, isTableEntry, opContext);
        }
    }

    /**
     * Reserved for internal use. Writes an entity to the stream, leaving the stream open for additional writing.
     * 
     * @param strWriter
     *            The <code>StringWriter</code> to write the entity to.
     * @param format
     *            The {@link TablePayloadFormat} to use for parsing.
     * @param entity
     *            The instance implementing {@link TableEntity} to write to the output stream.
     * @param isTableEntry
     *            A flag indicating the entity is a reference to a table at the top level of the storage service when
     *            <code>true<code> and a reference to an entity within a table when <code>false</code>.
     * @param opContext
     *            An {@link OperationContext} object used to track the execution of the operation.
     * @throws XMLStreamException
     *             if an error occurs while accessing the stream with AtomPub.
     * @throws StorageException
     *             if a Storage service error occurs.
     * @throws IOException
     *             if an error occurs while accessing the stream with Json.
     */
    @SuppressWarnings("deprecation")
    static void writeSingleEntityToString(final StringWriter strWriter, final TablePayloadFormat format,
            final TableEntity entity, final boolean isTableEntry, final OperationContext opContext)
            throws XMLStreamException, StorageException, IOException {
        if (format == TablePayloadFormat.AtomPub) {
            writeSingleAtomEntity(strWriter, entity, isTableEntry, opContext);
        }
        else {
            writeSingleJsonEntity(strWriter, format, entity, isTableEntry, opContext);
        }
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
    private static void writeAtomEntity(final TableEntity entity, final boolean isTableEntry,
            final XMLStreamWriter xmlw, final OperationContext opContext) throws XMLStreamException, StorageException {
        HashMap<String, EntityProperty> properties = entity.writeEntity(opContext);
        if (properties == null) {
            properties = new HashMap<String, EntityProperty>();
        }

        if (!isTableEntry) {
            Utility.assertNotNull(TableConstants.PARTITION_KEY, entity.getPartitionKey());
            Utility.assertNotNull(TableConstants.ROW_KEY, entity.getRowKey());
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
     * @param outStream
     *            The <code>OutputStream</code> to write the entity to.
     * @param entity
     *            The instance implementing {@link TableEntity} to write to the output stream.
     * @param isTableEntry
     *            A flag indicating the entity is a reference to a table at the top level of the storage service when
     *            <code>true<code> and a reference to an entity within a table when <code>false</code>.
     * @param opContext
     *            An {@link OperationContext} object used to track the execution of the operation.
     * @throws XMLStreamException
     *             if an error occurs creating or accessing the stream.
     * @throws StorageException
     *             if a Storage service error occurs.
     */
    private static void writeSingleAtomEntity(final OutputStream outStream, final TableEntity entity,
            final boolean isTableEntry, final OperationContext opContext) throws XMLStreamException, StorageException {
        final XMLOutputFactory xmlOutFactoryInst = XMLOutputFactory.newInstance();
        XMLStreamWriter xmlw = xmlOutFactoryInst.createXMLStreamWriter(outStream, Constants.UTF8_CHARSET);

        // default is UTF8
        xmlw.writeStartDocument(Constants.UTF8_CHARSET, "1.0");

        writeAtomEntity(entity, isTableEntry, xmlw, opContext);

        // end doc
        xmlw.writeEndDocument();
        xmlw.flush();
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
     * @param opContext
     *            An {@link OperationContext} object used to track the execution of the operation.
     * @param outStream
     *            The <code>OutputStream</code> to write the entity to.
     * 
     * @throws XMLStreamException
     *             if an error occurs creating or accessing the stream.
     * @throws StorageException
     *             if a Storage service error occurs.
     */
    private static void writeSingleAtomEntity(final StringWriter strWriter, final TableEntity entity,
            final boolean isTableEntry, final OperationContext opContext) throws XMLStreamException, StorageException {
        final XMLOutputFactory xmlOutFactoryInst = XMLOutputFactory.newInstance();
        XMLStreamWriter xmlw = xmlOutFactoryInst.createXMLStreamWriter(strWriter);

        // default is UTF8
        xmlw.writeStartDocument(Constants.UTF8_CHARSET, "1.0");

        writeAtomEntity(entity, isTableEntry, xmlw, opContext);

        // end doc
        xmlw.writeEndDocument();
        xmlw.flush();
    }

    /**
     * Reserved for internal use. Writes an entity to the specified <code>JsonGenerator</code> as an JSON resource
     * 
     * @param generator
     *            The <code>JsonGenerator</code> to write the entity to.
     * @param format
     *            The {@link TablePayloadFormat} to use for parsing.
     * @param entity
     *            The instance implementing {@link TableEntity} to write to the output stream.
     * @param isTableEntry
     *            A flag indicating the entity is a reference to a table at the top level of the storage service when
     *            <code>true<code> and a reference to an entity within a table when <code>false</code>.
     * @param opContext
     *            An {@link OperationContext} object used to track the execution of the operation.
     * 
     * @throws StorageException
     *             if a Storage service error occurs.
     * @throws IOException
     *             if an error occurs while accessing the stream.
     */
    private static void writeJsonEntity(final JsonGenerator generator, TablePayloadFormat format,
            final TableEntity entity, final boolean isTableEntry, final OperationContext opContext)
            throws StorageException, IOException {

        HashMap<String, EntityProperty> properties = entity.writeEntity(opContext);
        if (properties == null) {
            properties = new HashMap<String, EntityProperty>();
        }

        // start object
        generator.writeStartObject();

        if (!isTableEntry) {
            Utility.assertNotNull(TableConstants.PARTITION_KEY, entity.getPartitionKey());
            Utility.assertNotNull(TableConstants.ROW_KEY, entity.getRowKey());
            Utility.assertNotNull(TableConstants.TIMESTAMP, entity.getTimestamp());

            // PartitionKey
            generator.writeStringField(TableConstants.PARTITION_KEY, entity.getPartitionKey());

            // RowKey
            generator.writeStringField(TableConstants.ROW_KEY, entity.getRowKey());

            // Timestamp
            generator.writeStringField(TableConstants.TIMESTAMP, Utility.getTimeByZoneAndFormat(entity.getTimestamp(),
                    Utility.UTC_ZONE, Utility.ISO8061_LONG_PATTERN));
        }

        for (final Entry<String, EntityProperty> ent : properties.entrySet()) {
            if (ent.getKey().equals(TableConstants.PARTITION_KEY) || ent.getKey().equals(TableConstants.ROW_KEY)
                    || ent.getKey().equals(TableConstants.TIMESTAMP) || ent.getKey().equals("Etag")) {
                continue;
            }

            EntityProperty currProp = ent.getValue();
            if (currProp.getEdmType().mustAnnotateType()) {
                final String edmTypeString = currProp.getEdmType().toString();

                // property type
                generator.writeStringField(ent.getKey() + ODataConstants.ODATA_TYPE_SUFFIX, edmTypeString);

                // property key and value
                generator.writeStringField(ent.getKey(), ent.getValue().getValueAsString());
            }
            else if (currProp.getEdmType() == EdmType.DOUBLE && currProp.getIsNull() == false) {
                final String edmTypeString = currProp.getEdmType().toString();
                final Double value = currProp.getValueAsDouble();

                // property type, if needed
                if (value.equals(Double.POSITIVE_INFINITY) || value.equals(Double.NEGATIVE_INFINITY)
                        || value.equals(Double.NaN)) {
                    generator.writeStringField(ent.getKey() + ODataConstants.ODATA_TYPE_SUFFIX, edmTypeString);

                    // property key and value
                    generator.writeStringField(ent.getKey(), ent.getValue().getValueAsString());
                }
                else {
                    writeJsonProperty(generator, ent);
                }

            }
            else {
                writeJsonProperty(generator, ent);
            }
        }

        // end object
        generator.writeEndObject();
    }

    /**
     * Reserved for internal use. Writes an entity to the stream as an JSON resource, leaving the stream open
     * for additional writing.
     * 
     * @param outStream
     *            The <code>OutputStream</code> to write the entity to.
     * @param format
     *            The {@link TablePayloadFormat} to use for parsing.
     * @param entity
     *            The instance implementing {@link TableEntity} to write to the output stream.
     * @param isTableEntry
     *            A flag indicating the entity is a reference to a table at the top level of the storage service when
     *            <code>true<code> and a reference to an entity within a table when <code>false</code>.
     * @param opContext
     *            An {@link OperationContext} object used to track the execution of the operation.
     * 
     * @throws StorageException
     *             if a Storage service error occurs.
     * @throws IOException
     *             if an error occurs while accessing the stream.
     */
    private static void writeSingleJsonEntity(final OutputStream outStream, TablePayloadFormat format,
            final TableEntity entity, final boolean isTableEntry, final OperationContext opContext)
            throws StorageException, IOException {
        JsonGenerator generator = jsonFactory.createGenerator(outStream);

        try {
            // write to stream
            writeJsonEntity(generator, format, entity, isTableEntry, opContext);
        }
        finally {
            generator.close();
        }
    }

    /**
     * Reserved for internal use. Writes an entity to the stream as an JSON resource, leaving the stream open
     * for additional writing.
     * 
     * @param strWriter
     *            The <code>StringWriter</code> to write the entity to.
     * @param format
     *            The {@link TablePayloadFormat} to use for parsing.
     * @param entity
     *            The instance implementing {@link TableEntity} to write to the output stream.
     * @param isTableEntry
     *            A flag indicating the entity is a reference to a table at the top level of the storage service when
     *            <code>true<code> and a reference to an entity within a table when <code>false</code>.
     * @param opContext
     *            An {@link OperationContext} object used to track the execution of the operation.
     * 
     * @throws StorageException
     *             if a Storage service error occurs.
     * @throws IOException
     *             if an error occurs while accessing the stream.
     */
    private static void writeSingleJsonEntity(final StringWriter strWriter, TablePayloadFormat format,
            final TableEntity entity, final boolean isTableEntry, final OperationContext opContext)
            throws StorageException, IOException {
        JsonGenerator generator = jsonFactory.createGenerator(strWriter);

        try {
            // write to stream
            writeJsonEntity(generator, format, entity, isTableEntry, opContext);
        }
        finally {
            generator.close();
        }
    }

    private static void writeJsonProperty(JsonGenerator generator, Entry<String, EntityProperty> prop)
            throws JsonGenerationException, IOException {
        EdmType edmType = prop.getValue().getEdmType();
        if (prop.getValue().getIsNull()) {
            generator.writeNullField(prop.getKey());
        }
        else if (edmType == EdmType.BOOLEAN) {
            generator.writeBooleanField(prop.getKey(), prop.getValue().getValueAsBoolean());
        }
        else if (edmType == EdmType.DOUBLE) {
            generator.writeNumberField(prop.getKey(), prop.getValue().getValueAsDouble());
        }
        else if (edmType == EdmType.INT32) {
            generator.writeNumberField(prop.getKey(), prop.getValue().getValueAsInteger());
        }
        else {
            generator.writeStringField(prop.getKey(), prop.getValue().getValueAsString());
        }
    }
}
