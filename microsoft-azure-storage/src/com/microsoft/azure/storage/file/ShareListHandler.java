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
 * RESERVED FOR INTERNAL USE. A class used to deserialize a list of shares.
 */
final class ShareListHandler extends DefaultHandler {

    private final Stack<String> elementStack = new Stack<String>();
    private StringBuilder bld = new StringBuilder();

    private final CloudFileClient serviceClient;

    private final ListResponse<CloudFileShare> response = new ListResponse<CloudFileShare>();
    private FileShareAttributes attributes;
    private String shareName;

    private ShareListHandler(CloudFileClient serviceClient) {
        this.serviceClient = serviceClient;
    }

    /**
     * Parses a {@link ShareListResponse} form the given XML stream.
     * 
     * @param serviceClient
     *            a reference to the client object associated with this object.
     * @param stream
     *            the stream from which to parse the share list
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws IOException
     */
    protected static ListResponse<CloudFileShare> getShareList(final InputStream stream,
            final CloudFileClient serviceClient) throws ParserConfigurationException, SAXException, IOException {
        SAXParser saxParser = Utility.getSAXParser();
        ShareListHandler handler = new ShareListHandler(serviceClient);
        saxParser.parse(stream, handler);

        return handler.response;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        this.elementStack.push(localName);

        if (FileConstants.SHARE_ELEMENT.equals(localName)) {
            this.shareName = Constants.EMPTY_STRING;
            this.attributes = new FileShareAttributes();
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

        if (FileConstants.SHARE_ELEMENT.equals(currentNode)) {
            try {
                CloudFileShare retShare = this.serviceClient.getShareReference(this.shareName);
                retShare.setMetadata(this.attributes.getMetadata());
                retShare.setProperties(this.attributes.getProperties());

                this.response.getResults().add(retShare);
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
        else if (FileConstants.SHARE_ELEMENT.equals(parentNode)) {
            if (Constants.NAME_ELEMENT.equals(currentNode)) {
                this.shareName = value;
            }
        }
        else if (Constants.PROPERTIES.equals(parentNode)) {
            try {
                getProperties(currentNode, value);
            }
            catch (ParseException e) {
                throw new SAXException(e);
            }
        }
        else if (Constants.METADATA_ELEMENT.equals(parentNode)) {
            if (value == null) {
                value = "";
            }
            this.attributes.getMetadata().put(currentNode, value);
        }

        this.bld = new StringBuilder();
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        this.bld.append(ch, start, length);
    }

    private void getProperties(String currentNode, String value) throws ParseException {
        if (currentNode.equals(Constants.LAST_MODIFIED_ELEMENT)) {
            this.attributes.getProperties().setLastModified(Utility.parseRFC1123DateFromStringInGMT(value));
        }
        else if (currentNode.equals(Constants.ETAG_ELEMENT)) {
            this.attributes.getProperties().setEtag(Utility.formatETag(value));
        }
        else if (currentNode.equals(FileConstants.SHARE_QUOTA_ELEMENT)) {
            this.attributes.getProperties().setShareQuota(Utility.isNullOrEmpty(value) ? null : Integer.parseInt(value));
        }
    }
}