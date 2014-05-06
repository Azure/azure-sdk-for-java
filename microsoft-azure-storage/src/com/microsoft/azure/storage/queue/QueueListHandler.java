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
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.core.ListResponse;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.Utility;

/**
 * RESERVED FOR INTERNAL USE. A class used to deserialize a list of queues.
 */
final class QueueListHandler extends DefaultHandler {

    private final ListResponse<CloudQueue> response = new ListResponse<CloudQueue>();

    private final Stack<String> elementStack = new Stack<String>();
    private StringBuilder bld = new StringBuilder();

    private final CloudQueueClient serviceClient;

    private String queueName;
    private HashMap<String, String> metadata;

    private QueueListHandler(CloudQueueClient serviceClient) {
        this.serviceClient = serviceClient;
    }

    /**
     * Parses the input stream containing the response body of the list queues request result and populates the class
     * data.
     * 
     * @param stream
     *            The <code>InputStream</code> object to deserialize from.
     * @param serviceClient
     *            A {@link CloudQueueClient} object associated with the storage
     *            service.
     * 
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static ListResponse<CloudQueue> getQueues(final InputStream stream, final CloudQueueClient serviceClient)
            throws SAXException, IOException, ParserConfigurationException {
        SAXParser saxParser = Utility.getSAXParser();
        QueueListHandler handler = new QueueListHandler(serviceClient);
        saxParser.parse(stream, handler);

        return handler.response;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        this.elementStack.push(localName);

        if (QueueConstants.QUEUE_ELEMENT.equals(localName)) {
            this.queueName = Constants.EMPTY_STRING;
            this.metadata = new HashMap<String, String>();
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        String currentNode = this.elementStack.pop();

        // if the node popped from the stack and the localName don't match, the xml document is improperly formatted
        if (!localName.equals(currentNode)) {
            throw new SAXException(SR.INVALID_RESPONSE_RECEIVED);
        }

        String parentNode = null;
        if (!this.elementStack.isEmpty()) {
            parentNode = this.elementStack.peek();
        }

        String value = this.bld.toString();
        if (value.isEmpty()) {
            value = null;
        }

        if (QueueConstants.QUEUE_ELEMENT.equals(currentNode)) {
            CloudQueue queue;
            try {
                queue = this.serviceClient.getQueueReference(this.queueName);
                queue.setMetadata(this.metadata);

                this.response.getResults().add(queue);
            }
            catch (URISyntaxException e) {
                throw new SAXException(e);
            }
            catch (StorageException e) {
                throw new SAXException(e);
            }
        }
        else if (ListResponse.ENUMERATION_RESULTS.equals(parentNode)) {
            if (Constants.PREFIX_ELEMENT.equals(currentNode)) {
                this.response.setPrefix(value);
            }
            else if (Constants.MARKER_ELEMENT.equals(currentNode)) {
                this.response.setMarker(value);
            }
            else if (Constants.NEXT_MARKER_ELEMENT.equals(currentNode)) {
                this.response.setNextMarker(value);
            }
            else if (Constants.MAX_RESULTS_ELEMENT.equals(currentNode)) {
                this.response.setMaxResults(Integer.parseInt(value));
            }
        }
        else if (Constants.METADATA_ELEMENT.equals(parentNode)) {
            this.metadata.put(currentNode, value);
        }
        else if (QueueConstants.QUEUE_ELEMENT.equals(parentNode)) {
            if (Constants.NAME_ELEMENT.equals(currentNode)) {
                this.queueName = value;
            }
        }

        this.bld = new StringBuilder();
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        this.bld.append(ch, start, length);
    }
}
