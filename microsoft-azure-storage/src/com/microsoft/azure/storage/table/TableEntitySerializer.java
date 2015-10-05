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
import java.util.HashMap;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
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
     * Reserved for internal use. Writes an entity to the stream as a JSON resource, leaving the stream open
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
    static void writeSingleEntityToStream(final OutputStream outStream, final TablePayloadFormat format,
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
     * Reserved for internal use. Writes an entity to the stream as a JSON resource, leaving the stream open
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
    static void writeSingleEntityToString(final StringWriter strWriter, final TablePayloadFormat format,
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

    /**
     * Reserved for internal use. Writes an entity to the specified <code>JsonGenerator</code> as a JSON resource
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
            generator.writeStringField(TableConstants.TIMESTAMP, Utility.getJavaISO8601Time(entity.getTimestamp()));
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
