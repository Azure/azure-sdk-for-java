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
package com.microsoft.windowsazure.storage.blob;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.microsoft.windowsazure.storage.OperationContext;
import com.microsoft.windowsazure.storage.StorageException;
import com.microsoft.windowsazure.storage.core.Utility;

/**
 * RESERVED FOR INTERNAL USE. A class used to serialize a block list to a byte array.
 */
final class BlockEntryListSerializer {

    /**
     * Writes a Block List and returns the corresponding UTF8 bytes.
     * 
     * @param blockList
     *            the Iterable of BlockEntry to write
     * @param opContext
     *            a tracking object for the request
     * @return a byte array of the UTF8 bytes representing the serialized block list.
     * @throws XMLStreamException
     *             if there is an error writing the block list.
     * @throws StorageException
     */
    public static byte[] writeBlockListToStream(final Iterable<BlockEntry> blockList, final OperationContext opContext)
            throws XMLStreamException, StorageException {

        final StringWriter outWriter = new StringWriter();
        final XMLOutputFactory xmlOutFactoryInst = XMLOutputFactory.newInstance();
        final XMLStreamWriter xmlw = xmlOutFactoryInst.createXMLStreamWriter(outWriter);

        // default is UTF8
        xmlw.writeStartDocument();
        xmlw.writeStartElement(BlobConstants.BLOCK_LIST_ELEMENT);

        for (final BlockEntry block : blockList) {
            if (block.searchMode == BlockSearchMode.COMMITTED) {
                xmlw.writeStartElement(BlobConstants.COMMITTED_ELEMENT);
            }
            else if (block.searchMode == BlockSearchMode.UNCOMMITTED) {
                xmlw.writeStartElement(BlobConstants.UNCOMMITTED_ELEMENT);
            }
            else if (block.searchMode == BlockSearchMode.LATEST) {
                xmlw.writeStartElement(BlobConstants.LATEST_ELEMENT);
            }

            xmlw.writeCharacters(block.getId());
            xmlw.writeEndElement();
        }

        // end BlockListElement
        xmlw.writeEndElement();

        // end doc
        xmlw.writeEndDocument();
        try {
            return outWriter.toString().getBytes("UTF8");
        }
        catch (final UnsupportedEncodingException e) {
            throw Utility.generateNewUnexpectedStorageException(e);
        }
    }
}
