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

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.Utility;

/**
 * RESERVED FOR INTERNAL USE. A class used to deserialize a list of queue messages.
 */
final class QueueMessageHandler extends DefaultHandler {

    private final Stack<String> elementStack = new Stack<String>();
    private StringBuilder bld = new StringBuilder();

    private final ArrayList<CloudQueueMessage> messages = new ArrayList<CloudQueueMessage>();

    private final boolean shouldEncodeMessage;

    private CloudQueueMessage message = new CloudQueueMessage();

    private QueueMessageHandler(final boolean shouldEncodeMessage) {
        this.shouldEncodeMessage = shouldEncodeMessage;
    }

    /**
     * Populates CloudQueueMessage objects from the XMLStreamReader; the reader must be at the Start element of
     * QueuesElement.
     * 
     * @param stream
     *            The <code>InputStream</code> object to deserialize from.
     * @param shouldEncodeMessage
     *            A flag indicating whether messages should be base-64 encoded.
     * 
     * @return An <code>ArrayList</code> of {@link CloudQueueMessage} from the
     *         stream.
     * 
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static ArrayList<CloudQueueMessage> readMessages(final InputStream stream, final boolean shouldEncodeMessage)
            throws SAXException, IOException, ParserConfigurationException {
        SAXParser saxParser = Utility.getSAXParser();
        QueueMessageHandler handler = new QueueMessageHandler(shouldEncodeMessage);
        saxParser.parse(stream, handler);

        return handler.messages;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        this.elementStack.push(localName);

        if (QueueConstants.QUEUE_MESSAGE_ELEMENT.equals(localName)) {
            this.message = new CloudQueueMessage();
            this.message.setMessageType(this.shouldEncodeMessage ? QueueMessageType.BASE_64_ENCODED
                    : QueueMessageType.RAW_STRING);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        String currentNode = this.elementStack.pop();

        // if the node popped from the stack and the localName don't match, the xml document is improperly formatted
        if (!localName.equals(currentNode)) {
            throw new SAXException(SR.INVALID_RESPONSE_RECEIVED);
        }

        String value = this.bld.toString();
        if (value.isEmpty()) {
            value = null;
        }

        if (QueueConstants.QUEUE_MESSAGE_ELEMENT.equals(localName)) {
            this.messages.add(this.message);
        }
        else if (QueueConstants.MESSAGE_ID_ELEMENT.equals(currentNode)) {
            this.message.setMessageId(value);
        }
        else if (QueueConstants.INSERTION_TIME_ELEMENT.equals(currentNode)) {
            try {
                this.message.setInsertionTime(Utility.parseRFC1123DateFromStringInGMT(value));
            }
            catch (ParseException e) {
                throw new SAXException(e);
            }
        }
        else if (QueueConstants.EXPIRATION_TIME_ELEMENT.equals(currentNode)) {
            try {
                this.message.setExpirationTime(Utility.parseRFC1123DateFromStringInGMT(value));
            }
            catch (ParseException e) {
                throw new SAXException(e);
            }
        }
        else if (QueueConstants.POP_RECEIPT_ELEMENT.equals(currentNode)) {
            this.message.setPopReceipt(value);
        }
        else if (QueueConstants.TIME_NEXT_VISIBLE_ELEMENT.equals(currentNode)) {
            try {
                this.message.setNextVisibleTime(Utility.parseRFC1123DateFromStringInGMT(value));
            }
            catch (ParseException e) {
                throw new SAXException(e);
            }
        }
        else if (QueueConstants.DEQUEUE_COUNT_ELEMENT.equals(currentNode)) {
            this.message.setDequeueCount(Integer.parseInt(value));
        }
        else if (QueueConstants.MESSAGE_TEXT_ELEMENT.equals(currentNode)) {
            this.message.messageContent = value;
        }

        this.bld = new StringBuilder();
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        this.bld.append(ch, start, length);
    }
}
