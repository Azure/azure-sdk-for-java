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
package com.microsoft.azure.storage.blob;

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
 * RESERVED FOR INTERNAL USE. A class used to deserialize a list of blocks.
 */
class BlockListHandler extends DefaultHandler {

    private final Stack<String> elementStack = new Stack<String>();
    private StringBuilder bld = new StringBuilder();

    private final ArrayList<BlockEntry> blocks = new ArrayList<BlockEntry>();

    private BlockSearchMode searchMode;
    private String blockName;
    private Long blockSize;

    /**
     * Parses a list of {@link BlockEntry} items from the given XML stream.
     * 
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws IOException
     */
    public static ArrayList<BlockEntry> getBlockList(InputStream streamRef) throws ParserConfigurationException,
            SAXException, IOException {
        SAXParser saxParser = Utility.getSAXParser();
        BlockListHandler handler = new BlockListHandler();
        saxParser.parse(streamRef, handler);

        return handler.blocks;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        this.elementStack.push(localName);

        if (BlobConstants.UNCOMMITTED_BLOCKS_ELEMENT.equals(localName)) {
            this.searchMode = BlockSearchMode.UNCOMMITTED;
        }
        else if (BlobConstants.COMMITTED_BLOCKS_ELEMENT.equals(localName)) {
            this.searchMode = BlockSearchMode.COMMITTED;
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

        if (BlobConstants.BLOCK_ELEMENT.equals(currentNode)) {
            final BlockEntry newBlock = new BlockEntry(this.blockName, this.searchMode);
            newBlock.setSize(this.blockSize);
            this.blocks.add(newBlock);
        }
        else if (Constants.NAME_ELEMENT.equals(currentNode)) {
            this.blockName = value;
        }
        else if (BlobConstants.SIZE_ELEMENT.equals(currentNode)) {
            this.blockSize = Long.parseLong(value);
        }

        this.bld = new StringBuilder();
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        this.bld.append(ch, start, length);
    }
}
