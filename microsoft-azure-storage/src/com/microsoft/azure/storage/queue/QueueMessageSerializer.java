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
package com.microsoft.azure.storage.queue;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.core.Utility;

/**
 * RESERVED FOR INTERNAL USE. A class used to serialize message requests to a byte array.
 */
final class QueueMessageSerializer {

    /**
     * Generates the message request body from a string containing the message.
     * The message must be encodable as UTF-8. To be included in a web request,
     * this message request body must be written to the output stream of the web
     * request.
     * 
     * @param message
     *            A <code>String<code> containing the message to wrap in a message request body.
     * 
     * @return An array of <code>byte</code> containing the message request body
     *         encoded as UTF-8.
     * 
     * @throws XMLStreamException
     * @throws StorageException
     *             If the message cannot be encoded as UTF-8.
     */
    public static byte[] generateMessageRequestBody(final String message) throws XMLStreamException, StorageException {
        final StringWriter outWriter = new StringWriter();
        final XMLStreamWriter xmlw = Utility.createXMLStreamWriter(outWriter);

        // default is UTF8
        xmlw.writeStartDocument();
        xmlw.writeStartElement(QueueConstants.QUEUE_MESSAGE_ELEMENT);

        xmlw.writeStartElement(QueueConstants.MESSAGE_TEXT_ELEMENT);
        xmlw.writeCharacters(message);
        xmlw.writeEndElement();

        // end QueueMessage_ELEMENT
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
