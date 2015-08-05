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
package com.microsoft.azure.storage.file;

import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.Utility;

/**
 * RESERVED FOR INTERNAL USE. A class used to deserialize share stats.
 */
final class ShareStatsHandler extends DefaultHandler {
    /**
     * The name of the share usage XML element.
     */
    private final static String USAGE_NAME = "ShareUsage";

    private final Stack<String> elementStack = new Stack<String>();
    private StringBuilder builder = new StringBuilder();

    private final ShareStats stats = new ShareStats();

    /**
     * Constructs a {@link ShareStats} object from an XML document received from the service.
     * 
     * @param inStream
     *            The XMLStreamReader object.
     * @return
     *         A {@link ShareStats} object containing the properties in the XML document.
     * 
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws IOException
     */
    public static ShareStats readShareStatsFromStream(final InputStream inStream)
            throws ParserConfigurationException, SAXException, IOException {
        SAXParser saxParser = Utility.getSAXParser();
        ShareStatsHandler handler = new ShareStatsHandler();
        saxParser.parse(inStream, handler);

        return handler.stats;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        this.elementStack.push(localName);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        String currentNode = this.elementStack.pop();

        // if the node popped from the stack and the localName don't match, the xml document is improperly formatted
        if (!localName.equals(currentNode)) {
            throw new SAXException(SR.INVALID_RESPONSE_RECEIVED);
        }

        if (USAGE_NAME.equals(currentNode)) {
            this.stats.setUsage(Integer.parseInt(this.builder.toString()));
        }

        this.builder = new StringBuilder();
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        this.builder.append(ch, start, length);
    }
}