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
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
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
 * RESERVED FOR INTERNAL USE. A class used to deserialize a list of blobs.
 */
final class BlobListHandler extends DefaultHandler {

    private final Stack<String> elementStack = new Stack<String>();
    private StringBuilder bld = new StringBuilder();

    private final ListBlobsResponse response = new ListBlobsResponse();

    private final CloudBlobContainer container;

    private BlobProperties properties;
    private HashMap<String, String> metadata;
    private CopyState copyState;
    private String blobName;
    private String snapshotID;

    private BlobListHandler(CloudBlobContainer container) {
        this.container = container;
    }

    /**
     * Parse and return the response.
     * 
     * @param stream
     * @param container
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static ListBlobsResponse getBlobList(final InputStream stream, final CloudBlobContainer container)
            throws ParserConfigurationException, SAXException, IOException {
        SAXParser saxParser = Utility.getSAXParser();
        BlobListHandler handler = new BlobListHandler(container);
        saxParser.parse(stream, handler);

        return handler.response;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        this.elementStack.push(localName);

        if (BlobConstants.BLOB_ELEMENT.equals(localName) || BlobConstants.BLOB_PREFIX_ELEMENT.equals(localName)) {
            this.blobName = Constants.EMPTY_STRING;
            this.snapshotID = null;
            this.properties = new BlobProperties();
            this.metadata = new HashMap<String, String>();
            this.copyState = null;
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

        if (BlobConstants.BLOB_ELEMENT.equals(currentNode)) {
            CloudBlob retBlob = null;
            try {
                if (this.properties.getBlobType() == BlobType.BLOCK_BLOB) {
                    retBlob = this.container.getBlockBlobReference(this.blobName);
                }
                else if (this.properties.getBlobType() == BlobType.PAGE_BLOB) {
                    retBlob = this.container.getPageBlobReference(this.blobName);
                }
                else if (this.properties.getBlobType() == BlobType.APPEND_BLOB) {
                    retBlob = this.container.getAppendBlobReference(this.blobName);
                }
                else {
                    throw new SAXException(SR.INVALID_RESPONSE_RECEIVED);
                }
            }
            catch (StorageException e) {
                throw new SAXException(e);
            }
            catch (URISyntaxException e) {
                throw new SAXException(e);
            }

            retBlob.snapshotID = this.snapshotID;
            retBlob.properties = this.properties;
            retBlob.metadata = this.metadata;
            retBlob.properties.setCopyState(this.copyState);

            this.response.getResults().add(retBlob);
        }
        else if (BlobConstants.BLOB_PREFIX_ELEMENT.equals(currentNode)) {
            try {
                this.response.getResults().add(this.container.getDirectoryReference(this.blobName));
            }
            catch (URISyntaxException e) {
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
            else if (Constants.DELIMITER_ELEMENT.equals(currentNode)) {
                this.response.setDelimiter(value);
            }
        }
        else if (BlobConstants.BLOB_ELEMENT.equals(parentNode)) {
            if (Constants.NAME_ELEMENT.equals(currentNode)) {
                this.blobName = value;
            }
            else if (BlobConstants.SNAPSHOT_ELEMENT.equals(currentNode)) {
                this.snapshotID = value;
            }
        }
        else if (BlobConstants.BLOB_PREFIX_ELEMENT.equals(parentNode)) {
            // Blob or BlobPrefix
            if (Constants.NAME_ELEMENT.equals(currentNode)) {
                this.blobName = value;
            }
        }
        else if (Constants.PROPERTIES.equals(parentNode)) {
            try {
                this.setProperties(currentNode, value);
            }
            catch (ParseException e) {
                throw new SAXException(e);
            }
            catch (URISyntaxException e) {
                throw new SAXException(e);
            }
        }
        else if (Constants.METADATA_ELEMENT.equals(parentNode)) {
            this.metadata.put(currentNode, value);
        }

        this.bld = new StringBuilder();
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        this.bld.append(ch, start, length);
    }

    private void setProperties(String currentNode, String value) throws ParseException, URISyntaxException,
            SAXException {

        if (Constants.LAST_MODIFIED_ELEMENT.equals(currentNode)) {
            this.properties.setLastModified(Utility.parseRFC1123DateFromStringInGMT(value));
        }
        else if (Constants.ETAG_ELEMENT.equals(currentNode)) {
            this.properties.setEtag(Utility.formatETag(value));
        }
        else if (Constants.HeaderConstants.CONTENT_LENGTH.equals(currentNode)) {
            this.properties.setLength(Long.parseLong(value));
        }
        else if (Constants.HeaderConstants.CONTENT_TYPE.equals(currentNode)) {
            this.properties.setContentType(value);
        }
        else if (Constants.HeaderConstants.CONTENT_ENCODING.equals(currentNode)) {
            this.properties.setContentEncoding(value);
        }
        else if (Constants.HeaderConstants.CONTENT_LANGUAGE.equals(currentNode)) {
            this.properties.setContentLanguage(value);
        }
        else if (Constants.HeaderConstants.CONTENT_MD5.equals(currentNode)) {
            this.properties.setContentMD5(value);
        }
        else if (Constants.HeaderConstants.CACHE_CONTROL.equals(currentNode)) {
            this.properties.setCacheControl(value);
        }
        else if (Constants.HeaderConstants.CONTENT_DISPOSITION.equals(currentNode)) {
            this.properties.setContentDisposition(value);
        }
        else if (BlobConstants.BLOB_TYPE_ELEMENT.equals(currentNode)) {
            final String tempString = value;
            if (tempString.equals(BlobConstants.BLOCK_BLOB)) {
                this.properties.setBlobType(BlobType.BLOCK_BLOB);
            }
            else if (tempString.equals(BlobConstants.PAGE_BLOB.toString())) {
                this.properties.setBlobType(BlobType.PAGE_BLOB);
            }
            else if (tempString.equals(BlobConstants.APPEND_BLOB.toString())) {
                this.properties.setBlobType(BlobType.APPEND_BLOB);
            }
            else {
                throw new SAXException(SR.INVALID_RESPONSE_RECEIVED);
            }
        }
        else if (Constants.LEASE_STATUS_ELEMENT.equals(currentNode)) {
            final LeaseStatus tempStatus = LeaseStatus.parse(value);
            if (!tempStatus.equals(LeaseStatus.UNSPECIFIED)) {
                this.properties.setLeaseStatus(tempStatus);
            }
            else {
                throw new SAXException(SR.INVALID_RESPONSE_RECEIVED);
            }
        }
        else if (Constants.LEASE_STATE_ELEMENT.equals(currentNode)) {
            final LeaseState tempState = LeaseState.parse(value);
            if (!tempState.equals(LeaseState.UNSPECIFIED)) {
                this.properties.setLeaseState(tempState);
            }
            else {
                throw new SAXException(SR.INVALID_RESPONSE_RECEIVED);
            }
        }
        else if (Constants.LEASE_DURATION_ELEMENT.equals(currentNode)) {
            final LeaseDuration tempDuration = LeaseDuration.parse(value);
            if (!tempDuration.equals(LeaseDuration.UNSPECIFIED)) {
                this.properties.setLeaseDuration(tempDuration);
            }
            else {
                throw new SAXException(SR.INVALID_RESPONSE_RECEIVED);
            }
        }
        else if (Constants.COPY_ID_ELEMENT.equals(currentNode)) {
            if (this.copyState == null) {
                this.copyState = new CopyState();
            }
            this.copyState.setCopyId(value);
        }
        else if (Constants.COPY_COMPLETION_TIME_ELEMENT.equals(currentNode)) {
            if (this.copyState == null) {
                this.copyState = new CopyState();
            }
            this.copyState.setCompletionTime(Utility.parseRFC1123DateFromStringInGMT(value));
        }
        else if (Constants.COPY_STATUS_ELEMENT.equals(currentNode)) {
            if (this.copyState == null) {
                this.copyState = new CopyState();
            }
            this.copyState.setStatus(CopyStatus.parse(value));
        }
        else if (Constants.COPY_SOURCE_ELEMENT.equals(currentNode)) {
            if (this.copyState == null) {
                this.copyState = new CopyState();
            }
            this.copyState.setSource(new URI(value));
        }
        else if (Constants.COPY_PROGRESS_ELEMENT.equals(currentNode)) {
            if (this.copyState == null) {
                this.copyState = new CopyState();
            }

            final String tempString = value;
            String[] progressSequence = tempString.split("/");
            this.copyState.setBytesCopied(Long.parseLong(progressSequence[0]));
            this.copyState.setTotalBytes(Long.parseLong(progressSequence[1]));
        }
        else if (Constants.COPY_STATUS_DESCRIPTION_ELEMENT.equals(currentNode)) {
            if (this.copyState == null) {
                this.copyState = new CopyState();
            }
            this.copyState.setStatusDescription(value);
        }
    }
}
