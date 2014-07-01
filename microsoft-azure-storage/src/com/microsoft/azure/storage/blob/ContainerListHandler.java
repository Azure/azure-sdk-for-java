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
 * RESERVED FOR INTERNAL USE. A class used to deserialize a list of containers.
 */
final class ContainerListHandler extends DefaultHandler {

    private final Stack<String> elementStack = new Stack<String>();
    private StringBuilder bld = new StringBuilder();

    private final CloudBlobClient serviceClient;

    private final ListResponse<CloudBlobContainer> response = new ListResponse<CloudBlobContainer>();
    private BlobContainerAttributes attributes;
    private String containerName;

    private ContainerListHandler(CloudBlobClient serviceClient) {
        this.serviceClient = serviceClient;
    }

    /**
     * Parses a {@link ContainerListResponse} form the given XML stream.
     * 
     * @param serviceClient
     *            a reference to the client object associated with this object.
     * @param stream
     *            the stream from which to parse the container list
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws IOException
     */
    protected static ListResponse<CloudBlobContainer> getContainerList(final InputStream stream,
            final CloudBlobClient serviceClient) throws ParserConfigurationException, SAXException, IOException {
        SAXParser saxParser = Utility.getSAXParser();
        ContainerListHandler handler = new ContainerListHandler(serviceClient);
        saxParser.parse(stream, handler);

        return handler.response;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        this.elementStack.push(localName);

        if (BlobConstants.CONTAINER_ELEMENT.equals(localName)) {
            this.containerName = Constants.EMPTY_STRING;
            this.attributes = new BlobContainerAttributes();
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

        if (BlobConstants.CONTAINER_ELEMENT.equals(currentNode)) {
            try {
                CloudBlobContainer retContainer = this.serviceClient.getContainerReference(this.containerName);
                retContainer.setMetadata(this.attributes.getMetadata());
                retContainer.setProperties(this.attributes.getProperties());

                this.response.getResults().add(retContainer);
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
        else if (BlobConstants.CONTAINER_ELEMENT.equals(parentNode)) {
            if (Constants.NAME_ELEMENT.equals(currentNode)) {
                this.containerName = value;
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

    private void getProperties(String currentNode, String value) throws ParseException, SAXException {

        if (currentNode.equals(Constants.LAST_MODIFIED_ELEMENT)) {
            this.attributes.getProperties().setLastModified(Utility.parseRFC1123DateFromStringInGMT(value));
        }
        else if (currentNode.equals(Constants.ETAG_ELEMENT)) {
            this.attributes.getProperties().setEtag(Utility.formatETag(value));
        }
        else if (currentNode.equals(Constants.LEASE_STATUS_ELEMENT)) {
            final LeaseStatus tempStatus = LeaseStatus.parse(value);
            if (!tempStatus.equals(LeaseStatus.UNSPECIFIED)) {
                this.attributes.getProperties().setLeaseStatus(tempStatus);
            }
            else {
                throw new SAXException(SR.INVALID_RESPONSE_RECEIVED);
            }
        }
        else if (currentNode.equals(Constants.LEASE_STATE_ELEMENT)) {
            final LeaseState tempState = LeaseState.parse(value);
            if (!tempState.equals(LeaseState.UNSPECIFIED)) {
                this.attributes.getProperties().setLeaseState(tempState);
            }
            else {
                throw new SAXException(SR.INVALID_RESPONSE_RECEIVED);
            }
        }
        else if (currentNode.equals(Constants.LEASE_DURATION_ELEMENT)) {
            final LeaseDuration tempDuration = LeaseDuration.parse(value);
            if (!tempDuration.equals(LeaseDuration.UNSPECIFIED)) {
                this.attributes.getProperties().setLeaseDuration(tempDuration);
            }
            else {
                throw new SAXException(SR.INVALID_RESPONSE_RECEIVED);
            }
        }
    }
}
