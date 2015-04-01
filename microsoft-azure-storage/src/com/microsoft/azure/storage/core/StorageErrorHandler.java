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
package com.microsoft.azure.storage.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.StorageExtendedErrorInformation;

/**
 * RESERVED FOR INTERNAL USE. A class used to deserialize storage errors.
 */
final class StorageErrorHandler extends DefaultHandler {

    private final Stack<String> elementStack = new Stack<String>();
    private StringBuilder bld = new StringBuilder();

    private final StorageExtendedErrorInformation errorInfo = new StorageExtendedErrorInformation();

    /**
     * Gets the Extended Error information from the response stream.
     * 
     * @return the Extended Error information from the response stream
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public static StorageExtendedErrorInformation getExtendedErrorInformation(final InputStream stream)
            throws SAXException, IOException, ParserConfigurationException {
        SAXParser saxParser = Utility.getSAXParser();
        StorageErrorHandler handler = new StorageErrorHandler();
        saxParser.parse(stream, handler);

        return handler.errorInfo;
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

        String parentNode = null;
        if (!this.elementStack.isEmpty()) {
            parentNode = this.elementStack.peek();
        }

        String value = this.bld.toString();
        if (value.isEmpty()) {
            value = null;
        }

        if (Constants.ERROR_ROOT_ELEMENT.equals(parentNode)) {
            if (Constants.ERROR_CODE.equals(currentNode)) {
                this.errorInfo.setErrorCode(value);
            }
            else if (Constants.ERROR_MESSAGE.equals(currentNode)) {
                this.errorInfo.setErrorMessage(value);
            }
            else {
                // get additional details
                this.errorInfo.getAdditionalDetails().put(currentNode, new String[] { value });
            }
        }
        else if (Constants.ERROR_EXCEPTION.equals(parentNode)) {
            // get additional details
            this.errorInfo.getAdditionalDetails().put(currentNode, new String[] { value });
        }

        this.bld = new StringBuilder();
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        this.bld.append(ch, start, length);
    }
}
