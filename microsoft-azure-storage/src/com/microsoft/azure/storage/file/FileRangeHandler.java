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
import java.util.ArrayList;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.Utility;

/**
 * RESERVED FOR INTERNAL USE. A class used to deserialize a list of file ranges.
 */
final class FileRangeHandler extends DefaultHandler {

    private final Stack<String> elementStack = new Stack<String>();
    private StringBuilder bld = new StringBuilder();

    private final ArrayList<FileRange> fileRanges = new ArrayList<FileRange>();

    private long startOffset;
    private long endOffset;

    /**
     * Returns an ArrayList of File Ranges for the given stream.
     * 
     * @return an ArrayList of File Ranges for the given stream.
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws IOException
     */
    protected static ArrayList<FileRange> getFileRanges(InputStream streamRef) throws ParserConfigurationException,
            SAXException, IOException {
        SAXParser saxParser = Utility.getSAXParser();
        FileRangeHandler handler = new FileRangeHandler();
        saxParser.parse(streamRef, handler);

        return handler.fileRanges;
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

        String value = this.bld.toString();
        if (value.isEmpty()) {
            value = null;
        }

        if (FileConstants.FILE_RANGE_ELEMENT.equals(currentNode)) {
            final FileRange fileRangeRef = new FileRange(this.startOffset, this.endOffset);
            this.fileRanges.add(fileRangeRef);
        }
        else if (FileConstants.START_ELEMENT.equals(currentNode)) {
            this.startOffset = Long.parseLong(value);
        }
        else if (Constants.END_ELEMENT.equals(currentNode)) {
            this.endOffset = Long.parseLong(value);
        }

        this.bld = new StringBuilder();
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        this.bld.append(ch, start, length);
    }
}
