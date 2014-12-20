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
import java.net.URISyntaxException;
import java.text.ParseException;
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
 * RESERVED FOR INTERNAL USE. A class used to deserialize a list of files and directories.
 */
final class FileListHandler extends DefaultHandler {

    private final Stack<String> elementStack = new Stack<String>();
    private StringBuilder bld = new StringBuilder();

    private final ListResponse<ListFileItem> response = new ListResponse<ListFileItem>();

    private final CloudFileDirectory directory;

    private FileDirectoryProperties directoryProperties;
    private FileProperties fileProperties;
    private String name;

    private FileListHandler(CloudFileDirectory directory) {
        this.directory = directory;
    }

    /**
     * Parse and return the response.
     * 
     * @param stream
     * @param directory
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static ListResponse<ListFileItem> getFileAndDirectoryList(final InputStream stream,
            final CloudFileDirectory directory) throws ParserConfigurationException, SAXException, IOException {
        SAXParser saxParser = Utility.getSAXParser();
        FileListHandler handler = new FileListHandler(directory);
        saxParser.parse(stream, handler);

        return handler.response;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        this.elementStack.push(localName);

        if (FileConstants.FILE_ELEMENT.equals(localName)) {
            this.name = Constants.EMPTY_STRING;
            this.fileProperties = new FileProperties();
        }

        if (FileConstants.DIRECTORY_ELEMENT.equals(localName)) {
            this.name = Constants.EMPTY_STRING;
            this.directoryProperties = new FileDirectoryProperties();
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

        if (FileConstants.FILE_ELEMENT.equals(currentNode)) {
            CloudFile retFile = null;
            try {
                retFile = this.directory.getFileReference(this.name);
            }
            catch (URISyntaxException e) {
                throw new SAXException(e);
            }
            catch (StorageException e) {
                throw new SAXException(e);
            }
            retFile.setProperties(this.fileProperties);
            this.response.getResults().add(retFile);
        }
        else if (FileConstants.DIRECTORY_ELEMENT.equals(currentNode)) {
            CloudFileDirectory retDirectory = null;
            try {
                retDirectory = this.directory.getDirectoryReference(this.name);
            }
            catch (URISyntaxException e) {
                throw new SAXException(e);
            }
            catch (StorageException e) {
                throw new SAXException(e);
            }
            retDirectory.setProperties(this.directoryProperties);
            this.response.getResults().add(retDirectory);
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
        else if (FileConstants.FILE_ELEMENT.equals(parentNode) || FileConstants.DIRECTORY_ELEMENT.equals(parentNode)) {
            if (Constants.NAME_ELEMENT.equals(currentNode)) {
                this.name = value;
            }
        }
        else if (Constants.PROPERTIES.equals(parentNode)) {
            try {
                this.setProperties(currentNode, value);
            }
            catch (ParseException e) {
                throw new SAXException(e);
            }
        }

        this.bld = new StringBuilder();
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        this.bld.append(ch, start, length);
    }

    private void setProperties(String currentNode, String value) throws ParseException {
        // Called in both the file and the directory case.
        if (Constants.LAST_MODIFIED_ELEMENT.equals(currentNode)) {
            this.directoryProperties.setLastModified(Utility.parseRFC1123DateFromStringInGMT(value));
        }
        else if (Constants.ETAG_ELEMENT.equals(currentNode)) {
            this.directoryProperties.setEtag(Utility.formatETag(value));
        }
        else if (Constants.HeaderConstants.CONTENT_LENGTH.equals(currentNode)) {
            this.fileProperties.setLength(Long.parseLong(value));
        }
    }
}
