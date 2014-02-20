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
package com.microsoft.windowsazure.storage.blob;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.microsoft.windowsazure.storage.Constants;
import com.microsoft.windowsazure.storage.LeaseDuration;
import com.microsoft.windowsazure.storage.LeaseState;
import com.microsoft.windowsazure.storage.LeaseStatus;
import com.microsoft.windowsazure.storage.StorageErrorCodeStrings;
import com.microsoft.windowsazure.storage.StorageException;
import com.microsoft.windowsazure.storage.StorageUri;
import com.microsoft.windowsazure.storage.core.DeserializationHelper;
import com.microsoft.windowsazure.storage.core.ListResponse;
import com.microsoft.windowsazure.storage.core.PathUtility;
import com.microsoft.windowsazure.storage.core.Utility;

/**
 * RESERVED FOR INTERNAL USE.
 */
public class BlobDeserializer {
    public static ListBlobsResponse getBlobList(final InputStream stream, final CloudBlobClient serviceClient,
            final CloudBlobContainer container) throws XMLStreamException, StorageException {
        final XMLStreamReader xmlr = DeserializationHelper.createXMLStreamReaderFromStream(stream);

        final ListBlobsResponse blobs = new ListBlobsResponse();

        String tempParseString = null;

        // Start document
        int eventType = xmlr.getEventType();
        xmlr.require(XMLStreamConstants.START_DOCUMENT, null, null);

        // 1. get enumerationResults Header
        eventType = xmlr.next();
        xmlr.require(XMLStreamConstants.START_ELEMENT, null, "EnumerationResults");

        // check if there are more events in the input stream
        while (xmlr.hasNext()) {
            eventType = xmlr.next();

            if (eventType == XMLStreamConstants.START_ELEMENT || eventType == XMLStreamConstants.END_ELEMENT) {
                final String name = xmlr.getName().toString();
                if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(Constants.MAX_RESULTS_ELEMENT)) {
                    tempParseString = DeserializationHelper.readElementFromXMLReader(xmlr,
                            Constants.MAX_RESULTS_ELEMENT);
                    blobs.setMaxResults(Integer.parseInt(tempParseString));
                }
                else if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(Constants.MARKER_ELEMENT)) {
                    blobs.setMarker(DeserializationHelper.readElementFromXMLReader(xmlr, Constants.MARKER_ELEMENT));
                }
                else if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(Constants.NEXT_MARKER_ELEMENT)) {
                    blobs.setNextMarker(DeserializationHelper.readElementFromXMLReader(xmlr,
                            Constants.NEXT_MARKER_ELEMENT));
                }
                else if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(Constants.PREFIX_ELEMENT)) {
                    blobs.setPrefix(DeserializationHelper.readElementFromXMLReader(xmlr, Constants.PREFIX_ELEMENT));
                }
                else if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(Constants.DELIMITER_ELEMENT)) {
                    blobs.setDelimiter(DeserializationHelper
                            .readElementFromXMLReader(xmlr, Constants.DELIMITER_ELEMENT));
                }
                else if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(BlobConstants.BLOBS_ELEMENT)) {
                    try {
                        blobs.setResults(readBlobItems(xmlr, serviceClient, container));
                    }
                    catch (final URISyntaxException e) {
                        throw new XMLStreamException(e);
                    }
                    catch (final ParseException e) {
                        throw new XMLStreamException(e);
                    }

                    xmlr.require(XMLStreamConstants.END_ELEMENT, null, BlobConstants.BLOBS_ELEMENT);
                }
                else if (eventType == XMLStreamConstants.END_ELEMENT && "EnumerationResults".equals(name)) {
                    break;
                }
            }
        }

        return blobs;
    }

    /**
     * Reads BlobItems from the XMLStreamReader, reader must be at Start element of BlobsElement
     * 
     * @param xmlr
     *            the XMLStreamReader to read from
     * @param serviceClient
     *            the CloudBlobClient associated with the objects.
     * @param container
     *            the container associated with the objects.
     * @return the BlobItems from the stream
     * @throws XMLStreamException
     *             if there is an error parsing the stream
     * @throws ParseException
     *             if there is an error in parsing a date
     * @throws URISyntaxException
     *             if the uri is invalid
     * @throws StorageException
     */
    private static ArrayList<ListBlobItem> readBlobItems(final XMLStreamReader xmlr,
            final CloudBlobClient serviceClient, final CloudBlobContainer container) throws XMLStreamException,
            ParseException, URISyntaxException, StorageException {
        int eventType = xmlr.getEventType();
        final ArrayList<ListBlobItem> retBlobs = new ArrayList<ListBlobItem>();

        xmlr.require(XMLStreamConstants.START_ELEMENT, null, BlobConstants.BLOBS_ELEMENT);

        // check if there are more events in the input stream
        while (xmlr.hasNext()) {
            eventType = xmlr.next();
            final String name = xmlr.getName().toString();

            if (eventType == XMLStreamConstants.START_ELEMENT) {
                if (name.equals(BlobConstants.BLOB_ELEMENT)) {
                    retBlobs.add(readBlob(xmlr, serviceClient, container));
                }
                else if (name.equals(BlobConstants.BLOB_PREFIX_ELEMENT)) {
                    retBlobs.add(readDirectory(xmlr, container));
                }
                else {
                    throw new StorageException(StorageErrorCodeStrings.INVALID_XML_DOCUMENT,
                            "The response received is invalid or improperly formatted.",
                            Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
                }
            }
            else {
                break;
            }
        }

        xmlr.require(XMLStreamConstants.END_ELEMENT, null, BlobConstants.BLOBS_ELEMENT);
        return retBlobs;
    }

    /**
     * Reserved for internal use. Populates the blob from an XMLStreamReader, reader must be at Start element of Blob
     * 
     * @param xmlr
     *            the XMLStreamReader to read from
     * @param serviceClient
     *            the CloudBlobClient associated with the objects.
     * @throws XMLStreamException
     *             if there is an error parsing the stream
     * @throws ParseException
     *             if there is an error in parsing a date
     * @throws URISyntaxException
     *             if the uri is invalid
     * @throws StorageException
     */
    private static CloudBlob readBlob(final XMLStreamReader xmlr, final CloudBlobClient serviceClient,
            final CloudBlobContainer container) throws XMLStreamException, ParseException, URISyntaxException,
            StorageException {
        xmlr.require(XMLStreamConstants.START_ELEMENT, null, BlobConstants.BLOB_ELEMENT);

        String blobName = Constants.EMPTY_STRING;

        String snapshotID = null;
        HashMap<String, String> metadata = null;
        BlobProperties properties = null;

        int eventType = xmlr.getEventType();
        // check if there are more events in the input stream
        while (xmlr.hasNext()) {
            eventType = xmlr.next();
            final String name = xmlr.getName().toString();

            if (eventType == XMLStreamConstants.START_ELEMENT) {
                if (name.equals(Constants.NAME_ELEMENT)) {
                    blobName = DeserializationHelper.readElementFromXMLReader(xmlr, Constants.NAME_ELEMENT);
                }
                else if (name.equals(BlobConstants.SNAPSHOT_ELEMENT)) {
                    snapshotID = DeserializationHelper.readElementFromXMLReader(xmlr, BlobConstants.SNAPSHOT_ELEMENT);
                }
                else if (name.equals(BlobConstants.PROPERTIES)) {
                    properties = readBlobProperties(xmlr);
                    xmlr.require(XMLStreamConstants.END_ELEMENT, null, BlobConstants.PROPERTIES);
                }
                else if (name.equals(Constants.METADATA_ELEMENT)) {
                    metadata = DeserializationHelper.parseMetadateFromXML(xmlr);
                    xmlr.require(XMLStreamConstants.END_ELEMENT, null, Constants.METADATA_ELEMENT);
                }
            }
            else if (eventType == XMLStreamConstants.END_ELEMENT && name.equals(BlobConstants.BLOB_ELEMENT)) {
                break;
            }
        }

        xmlr.require(XMLStreamConstants.END_ELEMENT, null, BlobConstants.BLOB_ELEMENT);

        // Assemble and return
        if (properties != null) {
            CloudBlob retBlob = null;

            StorageUri blobUri = serviceClient.getStorageUri();
            blobUri = PathUtility.appendPathToUri(blobUri, container.getName());
            blobUri = PathUtility.appendPathToUri(blobUri, blobName);

            if (properties.getBlobType() == BlobType.BLOCK_BLOB) {
                retBlob = new CloudBlockBlob(blobUri, serviceClient, container);
            }
            else if (properties.getBlobType() == BlobType.PAGE_BLOB) {
                retBlob = new CloudPageBlob(blobUri, serviceClient, container);
            }
            else {
                throw new StorageException(StorageErrorCodeStrings.INVALID_XML_DOCUMENT,
                        "The response received is invalid or improperly formatted.",
                        Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
            }

            retBlob.setStorageUri(blobUri);
            retBlob.snapshotID = snapshotID;
            retBlob.properties = properties;
            retBlob.metadata = metadata;
            return retBlob;
        }
        else {
            throw new StorageException(StorageErrorCodeStrings.INVALID_XML_DOCUMENT,
                    "The response received is invalid or improperly formatted.",
                    Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
        }
    }

    /**
     * Populates the CloudBlobDirectory from an XMLStreamReader, reader must be at Start element of BlobPrefix
     * 
     * @param xmlr
     *            the XMLStreamReader to read from
     * @param serviceClient
     *            the CloudBlobClient associated with the objects.
     * @param container
     *            the container associated with the objects.
     * @return a CloudBlobDirectory parsed from the stream.
     * @throws XMLStreamException
     *             if there is an error parsing the stream
     * @throws ParseException
     *             if there is an error in parsing a date
     * @throws URISyntaxException
     *             if the uri is invalid
     * @throws StorageException
     */
    private static CloudBlobDirectory readDirectory(final XMLStreamReader xmlr, final CloudBlobContainer container)
            throws XMLStreamException, ParseException, URISyntaxException, StorageException {
        xmlr.require(XMLStreamConstants.START_ELEMENT, null, BlobConstants.BLOB_PREFIX_ELEMENT);

        // Move to Name element
        xmlr.next();
        xmlr.require(XMLStreamConstants.START_ELEMENT, null, Constants.NAME_ELEMENT);

        final String prefixName = DeserializationHelper.readElementFromXMLReader(xmlr, Constants.NAME_ELEMENT);

        // Move from End name element to end prefix element
        xmlr.next();
        xmlr.require(XMLStreamConstants.END_ELEMENT, null, BlobConstants.BLOB_PREFIX_ELEMENT);

        return container.getDirectoryReference(prefixName);
    }

    /**
     * Populates the object from the XMLStreamReader, reader must be at Start element of Properties
     * 
     * @param xmlr
     *            the XMLStreamReader object
     * @return the BlobProperties that was read.
     * @throws XMLStreamException
     *             if there is a parsing exception
     * @throws ParseException
     *             if a date value is not correctly encoded
     * @throws StorageException
     * @throws URISyntaxException
     */
    private static BlobProperties readBlobProperties(final XMLStreamReader xmlr) throws XMLStreamException,
            ParseException, StorageException, URISyntaxException {
        xmlr.require(XMLStreamConstants.START_ELEMENT, null, BlobConstants.PROPERTIES);
        int eventType = xmlr.getEventType();
        final BlobProperties properties = new BlobProperties();
        CopyState copyState = null;

        while (xmlr.hasNext()) {
            eventType = xmlr.next();
            final String name = xmlr.getName().toString();

            if (eventType == XMLStreamConstants.START_ELEMENT) {
                if (name.equals(Constants.LAST_MODIFIED_ELEMENT)) {
                    properties.setLastModified(Utility.parseRFC1123DateFromStringInGMT(DeserializationHelper
                            .readElementFromXMLReader(xmlr, Constants.LAST_MODIFIED_ELEMENT)));
                }
                else if (name.equals(Constants.ETAG_ELEMENT)) {
                    properties.setEtag(DeserializationHelper.readETagFromXMLReader(xmlr));
                }
                else if (name.equals(Constants.HeaderConstants.CONTENT_LENGTH)) {
                    final String tempString = DeserializationHelper.readElementFromXMLReader(xmlr,
                            Constants.HeaderConstants.CONTENT_LENGTH);
                    properties.setLength(Long.parseLong(tempString));
                }
                else if (name.equals(Constants.HeaderConstants.CONTENT_TYPE)) {
                    properties.setContentType(DeserializationHelper.readElementFromXMLReader(xmlr,
                            Constants.HeaderConstants.CONTENT_TYPE));
                }
                else if (name.equals(Constants.HeaderConstants.CONTENT_ENCODING)) {
                    properties.setContentEncoding(DeserializationHelper.readElementFromXMLReader(xmlr,
                            Constants.HeaderConstants.CONTENT_ENCODING));
                }
                else if (name.equals(Constants.HeaderConstants.CONTENT_LANGUAGE)) {
                    properties.setContentLanguage(DeserializationHelper.readElementFromXMLReader(xmlr,
                            Constants.HeaderConstants.CONTENT_LANGUAGE));
                }
                else if (name.equals(Constants.HeaderConstants.CONTENT_MD5)) {
                    properties.setContentMD5(DeserializationHelper.readElementFromXMLReader(xmlr,
                            Constants.HeaderConstants.CONTENT_MD5));
                }
                else if (name.equals(Constants.HeaderConstants.CACHE_CONTROL)) {
                    properties.setCacheControl(DeserializationHelper.readElementFromXMLReader(xmlr,
                            Constants.HeaderConstants.CACHE_CONTROL));
                }
                else if (name.equals(Constants.HeaderConstants.CONTENT_DISPOSITION)) {
                    properties.setContentDisposition(DeserializationHelper.readElementFromXMLReader(xmlr,
                            Constants.HeaderConstants.CONTENT_DISPOSITION));
                }
                else if (name.equals(BlobConstants.SEQUENCE_NUMBER)) {
                    DeserializationHelper.readElementFromXMLReader(xmlr, BlobConstants.SEQUENCE_NUMBER);
                }
                else if (name.equals(BlobConstants.BLOB_TYPE_ELEMENT)) {
                    final String tempString = DeserializationHelper.readElementFromXMLReader(xmlr,
                            BlobConstants.BLOB_TYPE_ELEMENT);
                    if (tempString.equals(BlobConstants.BLOCK_BLOB_VALUE)) {
                        properties.setBlobType(BlobType.BLOCK_BLOB);
                    }
                    else if (tempString.equals(BlobConstants.PAGE_BLOB_VALUE)) {
                        properties.setBlobType(BlobType.PAGE_BLOB);
                    }
                    else {
                        throw new StorageException(StorageErrorCodeStrings.INVALID_XML_DOCUMENT,
                                "The response received is invalid or improperly formatted.",
                                Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
                    }
                }
                else if (name.equals(Constants.LEASE_STATUS_ELEMENT)) {
                    final LeaseStatus tempStatus = LeaseStatus.parse(DeserializationHelper.readElementFromXMLReader(
                            xmlr, Constants.LEASE_STATUS_ELEMENT));
                    if (!tempStatus.equals(LeaseStatus.UNSPECIFIED)) {
                        properties.setLeaseStatus(tempStatus);
                    }
                    else {
                        throw new StorageException(StorageErrorCodeStrings.INVALID_XML_DOCUMENT,
                                "The response received is invalid or improperly formatted.",
                                Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
                    }
                }
                else if (name.equals(Constants.LEASE_STATE_ELEMENT)) {
                    final LeaseState tempState = LeaseState.parse(DeserializationHelper.readElementFromXMLReader(xmlr,
                            Constants.LEASE_STATE_ELEMENT));
                    if (!tempState.equals(LeaseState.UNSPECIFIED)) {
                        properties.setLeaseState(tempState);
                    }
                    else {
                        throw new StorageException(StorageErrorCodeStrings.INVALID_XML_DOCUMENT,
                                "The response received is invalid or improperly formatted.",
                                Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
                    }
                }
                else if (name.equals(Constants.LEASE_DURATION_ELEMENT)) {
                    final LeaseDuration tempDuration = LeaseDuration.parse(DeserializationHelper
                            .readElementFromXMLReader(xmlr, Constants.LEASE_DURATION_ELEMENT));
                    if (!tempDuration.equals(LeaseDuration.UNSPECIFIED)) {
                        properties.setLeaseDuration(tempDuration);
                    }
                    else {
                        throw new StorageException(StorageErrorCodeStrings.INVALID_XML_DOCUMENT,
                                "The response received is invalid or improperly formatted.",
                                Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
                    }
                }
                else if (name.equals(Constants.COPY_ID_ELEMENT)) {
                    if (copyState == null) {
                        copyState = new CopyState();
                    }
                    copyState
                            .setCopyId(DeserializationHelper.readElementFromXMLReader(xmlr, Constants.COPY_ID_ELEMENT));
                }
                else if (name.equals(Constants.COPY_COMPLETION_TIME_ELEMENT)) {
                    if (copyState == null) {
                        copyState = new CopyState();
                    }
                    copyState.setCompletionTime(Utility.parseRFC1123DateFromStringInGMT(DeserializationHelper
                            .readElementFromXMLReader(xmlr, Constants.COPY_COMPLETION_TIME_ELEMENT)));
                }
                else if (name.equals(Constants.COPY_STATUS_ELEMENT)) {
                    if (copyState == null) {
                        copyState = new CopyState();
                    }
                    copyState.setStatus(CopyStatus.parse(DeserializationHelper.readElementFromXMLReader(xmlr,
                            Constants.COPY_STATUS_ELEMENT)));
                }
                else if (name.equals(Constants.COPY_SOURCE_ELEMENT)) {
                    if (copyState == null) {
                        copyState = new CopyState();
                    }
                    copyState.setSource(new URI(DeserializationHelper.readElementFromXMLReader(xmlr,
                            Constants.COPY_SOURCE_ELEMENT)));
                }
                else if (name.equals(Constants.COPY_PROGRESS_ELEMENT)) {
                    if (copyState == null) {
                        copyState = new CopyState();
                    }

                    final String tempString = DeserializationHelper.readElementFromXMLReader(xmlr,
                            Constants.COPY_PROGRESS_ELEMENT);
                    String[] progressSequence = tempString.split("/");
                    copyState.setBytesCopied(Long.parseLong(progressSequence[0]));
                    copyState.setTotalBytes(Long.parseLong(progressSequence[1]));
                }
                else if (name.equals(Constants.COPY_STATUS_DESCRIPTION_ELEMENT)) {
                    if (copyState == null) {
                        copyState = new CopyState();
                    }
                    copyState.setStatusDescription(DeserializationHelper.readElementFromXMLReader(xmlr,
                            Constants.COPY_STATUS_DESCRIPTION_ELEMENT));
                }
                else if (name.equals(Constants.COPY_ID_ELEMENT)) {
                    if (copyState == null) {
                        copyState = new CopyState();
                    }
                    copyState
                            .setCopyId(DeserializationHelper.readElementFromXMLReader(xmlr, Constants.COPY_ID_ELEMENT));
                }
                else if (name.equals(Constants.COPY_COMPLETION_TIME_ELEMENT)) {
                    if (copyState == null) {
                        copyState = new CopyState();
                    }
                    copyState.setCompletionTime(Utility.parseRFC1123DateFromStringInGMT(DeserializationHelper
                            .readElementFromXMLReader(xmlr, Constants.COPY_COMPLETION_TIME_ELEMENT)));
                }
                else if (name.equals(Constants.COPY_STATUS_ELEMENT)) {
                    if (copyState == null) {
                        copyState = new CopyState();
                    }
                    copyState.setStatus(CopyStatus.parse(DeserializationHelper.readElementFromXMLReader(xmlr,
                            Constants.COPY_STATUS_ELEMENT)));
                }
                else if (name.equals(Constants.COPY_SOURCE_ELEMENT)) {
                    if (copyState == null) {
                        copyState = new CopyState();
                    }
                    copyState.setSource(new URI(DeserializationHelper.readElementFromXMLReader(xmlr,
                            Constants.COPY_SOURCE_ELEMENT)));
                }
                else if (name.equals(Constants.COPY_PROGRESS_ELEMENT)) {
                    if (copyState == null) {
                        copyState = new CopyState();
                    }

                    final String tempString = DeserializationHelper.readElementFromXMLReader(xmlr,
                            Constants.COPY_PROGRESS_ELEMENT);
                    String[] progressSequence = tempString.split("/");
                    copyState.setBytesCopied(Long.parseLong(progressSequence[0]));
                    copyState.setTotalBytes(Long.parseLong(progressSequence[1]));
                }
                else if (name.equals(Constants.COPY_STATUS_DESCRIPTION_ELEMENT)) {
                    if (copyState == null) {
                        copyState = new CopyState();
                    }
                    copyState.setStatusDescription(DeserializationHelper.readElementFromXMLReader(xmlr,
                            Constants.COPY_STATUS_DESCRIPTION_ELEMENT));
                }
            }
            else if (eventType == XMLStreamConstants.END_ELEMENT) {
                // expect end of properties
                xmlr.require(XMLStreamConstants.END_ELEMENT, null, BlobConstants.PROPERTIES);
                break;
            }
        }
        properties.setCopyState(copyState);

        return properties;
    }

    /**
     * Parses the XML stream.
     * 
     * @param serviceClient
     *            a reference to the client object associated with this object.
     * @throws XMLStreamException
     * @throws StorageException
     */
    public static ListResponse<CloudBlobContainer> getContainerList(final InputStream stream,
            final CloudBlobClient serviceClient) throws XMLStreamException, StorageException {
        final XMLStreamReader xmlr = DeserializationHelper.createXMLStreamReaderFromStream(stream);

        final ListResponse<CloudBlobContainer> containers = new ListResponse<CloudBlobContainer>();

        String tempParseString = null;

        // Start document
        int eventType = xmlr.getEventType();
        xmlr.require(XMLStreamConstants.START_DOCUMENT, null, null);

        // 1. get enumerationResults Header
        eventType = xmlr.next();
        xmlr.require(XMLStreamConstants.START_ELEMENT, null, "EnumerationResults");

        // check if there are more events in the input stream
        while (xmlr.hasNext()) {
            eventType = xmlr.next();

            if (eventType == XMLStreamConstants.START_ELEMENT || eventType == XMLStreamConstants.END_ELEMENT) {
                final String name = xmlr.getName().toString();
                if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(Constants.MAX_RESULTS_ELEMENT)) {
                    tempParseString = DeserializationHelper.readElementFromXMLReader(xmlr,
                            Constants.MAX_RESULTS_ELEMENT);
                    containers.setMaxResults(Integer.parseInt(tempParseString));
                }
                else if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(Constants.MARKER_ELEMENT)) {
                    containers
                            .setMarker(DeserializationHelper.readElementFromXMLReader(xmlr, Constants.MARKER_ELEMENT));
                }
                else if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(Constants.NEXT_MARKER_ELEMENT)) {
                    containers.setNextMarker(DeserializationHelper.readElementFromXMLReader(xmlr,
                            Constants.NEXT_MARKER_ELEMENT));
                }
                else if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(Constants.PREFIX_ELEMENT)) {
                    containers
                            .setPrefix(DeserializationHelper.readElementFromXMLReader(xmlr, Constants.PREFIX_ELEMENT));
                }
                else if (eventType == XMLStreamConstants.START_ELEMENT && name.equals(BlobConstants.CONTAINERS_ELEMENT)) {
                    try {
                        containers.setResults(readContainers(xmlr, serviceClient));
                    }
                    catch (final URISyntaxException e) {
                        throw new XMLStreamException(e);
                    }
                    catch (final ParseException e) {
                        throw new XMLStreamException(e);
                    }

                    xmlr.require(XMLStreamConstants.END_ELEMENT, null, BlobConstants.CONTAINERS_ELEMENT);
                    // eventType = xmlr.next();
                }
                else if (eventType == XMLStreamConstants.END_ELEMENT && "EnumerationResults".equals(name)) {
                    break;
                }
            }
        }

        return containers;
    }

    /**
     * Populates the container from an XMLStreamReader
     * 
     * @param xmlr
     *            the XMLStreamReader to read from
     * @throws XMLStreamException
     *             if there is an error parsing the stream
     * @throws ParseException
     *             if there is an error in parsing a date
     * @throws URISyntaxException
     *             if the uri is invalid
     * @throws StorageException
     */
    private static CloudBlobContainer readContainer(final XMLStreamReader xmlr, final CloudBlobClient serviceClient)
            throws XMLStreamException, ParseException, URISyntaxException, StorageException {

        xmlr.require(XMLStreamConstants.START_ELEMENT, null, BlobConstants.CONTAINER_ELEMENT);

        final BlobContainerAttributes attributes = readBlobContainerAttributes(xmlr);

        StorageUri containerUri = serviceClient.getStorageUri();
        containerUri = PathUtility.appendPathToUri(containerUri, attributes.getName());

        attributes.setStorageUri(containerUri);

        final CloudBlobContainer retContainer = new CloudBlobContainer(attributes.getStorageUri(), serviceClient);
        retContainer.setMetadata(attributes.getMetadata());
        retContainer.setName(attributes.getName());
        retContainer.setProperties(attributes.getProperties());
        retContainer.setStorageUri(attributes.getStorageUri());

        xmlr.require(XMLStreamConstants.END_ELEMENT, null, BlobConstants.CONTAINER_ELEMENT);
        return retContainer;
    }

    /**
     * Populates the object from the XMLStreamReader
     * 
     * @param xmlr
     *            the XMLStreamReader to read from
     * @throws XMLStreamException
     *             if there is an error parsing the stream
     * @throws ParseException
     *             if there is an error in parsing a date
     * @throws URISyntaxException
     *             if the uri is invalid
     */
    private static BlobContainerAttributes readBlobContainerAttributes(final XMLStreamReader xmlr)
            throws XMLStreamException, ParseException, URISyntaxException {
        int eventType = xmlr.getEventType();

        final BlobContainerAttributes attributes = new BlobContainerAttributes();

        while (xmlr.hasNext()) {
            eventType = xmlr.next();
            final String name = xmlr.getName().toString();
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                if (name.equals(Constants.NAME_ELEMENT)) {
                    attributes.setName(DeserializationHelper.readElementFromXMLReader(xmlr, Constants.NAME_ELEMENT));
                }
                else if (name.equals(BlobConstants.PROPERTIES)) {
                    attributes.setProperties(readBlobContainerProperties(xmlr));
                    xmlr.require(XMLStreamConstants.END_ELEMENT, null, BlobConstants.PROPERTIES);
                }
                else if (name.equals(Constants.METADATA_ELEMENT)) {
                    // parse metadata
                    attributes.setMetadata(DeserializationHelper.parseMetadateFromXML(xmlr));
                    xmlr.require(XMLStreamConstants.END_ELEMENT, null, Constants.METADATA_ELEMENT);
                }
            }
            else if (eventType == XMLStreamConstants.END_ELEMENT && name.equals(BlobConstants.CONTAINER_ELEMENT)) {
                break;
            }
        }

        return attributes;
    }

    /**
     * Populates the object from the XMLStreamReader, reader must be at Start element of Properties
     * 
     * @param xmlr
     *            the XMLStreamReader object
     * @throws XMLStreamException
     *             if there is a parsing exception
     * @throws ParseException
     *             if a date value is not correctly encoded
     */
    private static BlobContainerProperties readBlobContainerProperties(final XMLStreamReader xmlr)
            throws XMLStreamException, ParseException {
        xmlr.require(XMLStreamConstants.START_ELEMENT, null, BlobConstants.PROPERTIES);
        int eventType = xmlr.getEventType();
        final BlobContainerProperties properties = new BlobContainerProperties();

        while (xmlr.hasNext()) {
            eventType = xmlr.next();
            final String name = xmlr.getName().toString();
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                if (name.equals(Constants.LAST_MODIFIED_ELEMENT)) {
                    properties.setLastModified(Utility.parseRFC1123DateFromStringInGMT(DeserializationHelper
                            .readElementFromXMLReader(xmlr, Constants.LAST_MODIFIED_ELEMENT)));
                }
                else if (name.equals(Constants.ETAG_ELEMENT)) {
                    properties.setEtag(DeserializationHelper.readETagFromXMLReader(xmlr));
                }
                else if (name.equals(Constants.LEASE_STATUS_ELEMENT)) {
                    properties.setLeaseStatus(LeaseStatus.parse(DeserializationHelper.readElementFromXMLReader(xmlr,
                            Constants.LEASE_STATUS_ELEMENT)));
                }
                else if (name.equals(Constants.LEASE_STATE_ELEMENT)) {
                    properties.setLeaseState(LeaseState.parse(DeserializationHelper.readElementFromXMLReader(xmlr,
                            Constants.LEASE_STATE_ELEMENT)));
                }
                else if (name.equals(Constants.LEASE_DURATION_ELEMENT)) {
                    properties.setLeaseDuration(LeaseDuration.parse(DeserializationHelper.readElementFromXMLReader(
                            xmlr, Constants.LEASE_DURATION_ELEMENT)));
                }
            }
            else {
                // expect end of properties
                xmlr.require(XMLStreamConstants.END_ELEMENT, null, BlobConstants.PROPERTIES);
                break;
            }
        }

        return properties;
    }

    /**
     * Populates CloudBlobContainer objects from the XMLStreamReader, reader must be at Start element of
     * ContainersElement
     * 
     * @param xmlr
     *            the XMLStreamReader object
     * @param serviceClient
     *            the CloudBlobClient associated with the objects.
     * @return an ArrayList of CloudBlobContainer from the stream.
     * @throws XMLStreamException
     *             if there is a parsing exception
     * @throws ParseException
     *             if a date value is not correctly encoded
     * @throws URISyntaxException
     * @throws StorageException
     */
    private static ArrayList<CloudBlobContainer> readContainers(final XMLStreamReader xmlr,
            final CloudBlobClient serviceClient) throws XMLStreamException, ParseException, URISyntaxException,
            StorageException {
        int eventType = xmlr.getEventType();
        xmlr.require(XMLStreamConstants.START_ELEMENT, null, BlobConstants.CONTAINERS_ELEMENT);

        final ArrayList<CloudBlobContainer> containers = new ArrayList<CloudBlobContainer>();

        eventType = xmlr.next();
        while (eventType == XMLStreamConstants.START_ELEMENT && xmlr.hasName()
                && BlobConstants.CONTAINER_ELEMENT.equals(xmlr.getName().toString())) {
            containers.add(readContainer(xmlr, serviceClient));
            eventType = xmlr.next();
        }

        xmlr.require(XMLStreamConstants.END_ELEMENT, null, BlobConstants.CONTAINERS_ELEMENT);
        return containers;
    }

    /**
     * Parses the XML stream.
     * 
     * @throws XMLStreamException
     * @throws StorageException
     */
    public static ArrayList<BlockEntry> getBlockList(InputStream streamRef) throws XMLStreamException, StorageException {
        final XMLStreamReader xmlr = DeserializationHelper.createXMLStreamReaderFromStream(streamRef);

        final ArrayList<BlockEntry> blocks = new ArrayList<BlockEntry>();

        // Start document
        int eventType = xmlr.getEventType();
        xmlr.require(XMLStreamConstants.START_DOCUMENT, null, null);

        // 1. get BlockList Header
        eventType = xmlr.next();
        xmlr.require(XMLStreamConstants.START_ELEMENT, null, BlobConstants.BLOCK_LIST_ELEMENT);

        // check if there are more events in the input stream
        while (xmlr.hasNext()) {
            eventType = xmlr.next();
            if (eventType == XMLStreamConstants.START_ELEMENT || eventType == XMLStreamConstants.END_ELEMENT) {
                final String name = xmlr.getName().toString();
                if (name.equals(BlobConstants.COMMITTED_BLOCKS_ELEMENT)) {
                    // Move to block element
                    eventType = xmlr.next();
                    if (eventType == XMLStreamConstants.START_ELEMENT
                            && BlobConstants.BLOCK_ELEMENT.equals(xmlr.getName().toString())) {
                        blocks.addAll(readBlobBlocks(xmlr, BlockSearchMode.COMMITTED));
                        xmlr.require(XMLStreamConstants.END_ELEMENT, null, BlobConstants.COMMITTED_BLOCKS_ELEMENT);
                    }
                }
                else if (name.equals(BlobConstants.UNCOMMITTED_BLOCKS_ELEMENT)) {
                    // Move to block element
                    eventType = xmlr.next();
                    if (eventType == XMLStreamConstants.START_ELEMENT
                            && BlobConstants.BLOCK_ELEMENT.equals(xmlr.getName().toString())) {
                        blocks.addAll(readBlobBlocks(xmlr, BlockSearchMode.UNCOMMITTED));
                        xmlr.require(XMLStreamConstants.END_ELEMENT, null, BlobConstants.UNCOMMITTED_BLOCKS_ELEMENT);
                    }
                }
                else if (name.equals(BlobConstants.BLOCK_LIST_ELEMENT) && eventType == XMLStreamConstants.END_ELEMENT) {
                    break;
                }
                else {
                    throw new StorageException(StorageErrorCodeStrings.INVALID_XML_DOCUMENT,
                            "The response received is invalid or improperly formatted.",
                            Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
                }
            }
        }

        return blocks;
    }

    /**
     * Reads BlobItems from the XMLStreamReader, reader must be at Start element of BlobsElement
     * 
     * @param xmlr
     *            the XMLStreamReader to read from
     * @param searchMode
     *            the block search mode
     * @return BlockEntry from the stream.
     * @throws XMLStreamException
     *             if there is an error parsing the stream
     * @throws ParseException
     *             if there is an error in parsing a date
     * @throws URISyntaxException
     *             if the uri is invalid
     * @throws StorageException
     */
    private static ArrayList<BlockEntry> readBlobBlocks(final XMLStreamReader xmlr, final BlockSearchMode searchMode)
            throws XMLStreamException, StorageException {
        int eventType = xmlr.getEventType();
        final ArrayList<BlockEntry> retBlocks = new ArrayList<BlockEntry>();

        xmlr.require(XMLStreamConstants.START_ELEMENT, null, BlobConstants.BLOCK_ELEMENT);

        // check if there are more events in the input stream
        while (xmlr.hasNext() && BlobConstants.BLOCK_ELEMENT.equals(xmlr.getName().toString())) {
            String blockName = null;
            long blockSize = -1;

            // Read a block
            while (xmlr.hasNext()) {
                eventType = xmlr.next();
                final String name = xmlr.getName().toString();

                if (eventType == XMLStreamConstants.START_ELEMENT) {
                    if (name.equals(Constants.NAME_ELEMENT)) {
                        blockName = DeserializationHelper.readElementFromXMLReader(xmlr, Constants.NAME_ELEMENT);
                    }
                    else if (name.equals(BlobConstants.SIZE_ELEMENT)) {
                        final String sizeString = DeserializationHelper.readElementFromXMLReader(xmlr,
                                BlobConstants.SIZE_ELEMENT);
                        blockSize = Long.parseLong(sizeString);
                    }
                    else {
                        throw new StorageException(StorageErrorCodeStrings.INVALID_XML_DOCUMENT,
                                "The response received is invalid or improperly formatted.",
                                Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
                    }
                }
                else if (eventType == XMLStreamConstants.END_ELEMENT) {
                    final BlockEntry newBlock = new BlockEntry(blockName, searchMode);
                    newBlock.setSize(blockSize);
                    retBlocks.add(newBlock);
                    break;
                }
            }

            eventType = xmlr.next();
        }

        return retBlocks;
    }

    /**
     * Returns an ArrayList of Page Ranges for the given page blob.
     * 
     * @return an ArrayList of Page Ranges for the given page blob.
     * @throws XMLStreamException
     * @throws StorageException
     */
    public static ArrayList<PageRange> getPageRanges(InputStream streamRef) throws XMLStreamException, StorageException {
        final XMLStreamReader xmlr = DeserializationHelper.createXMLStreamReaderFromStream(streamRef);

        ArrayList<PageRange> pageRanges = new ArrayList<PageRange>();

        // Start document
        int eventType = xmlr.getEventType();
        xmlr.require(XMLStreamConstants.START_DOCUMENT, null, null);

        // 1. get BlockList Header
        eventType = xmlr.next();
        xmlr.require(XMLStreamConstants.START_ELEMENT, null, BlobConstants.PAGE_LIST_ELEMENT);

        // check if there are more events in the input stream
        while (xmlr.hasNext()) {
            eventType = xmlr.next();
            if (eventType == XMLStreamConstants.START_ELEMENT || eventType == XMLStreamConstants.END_ELEMENT) {
                final String name = xmlr.getName().toString();

                if (name.equals(BlobConstants.PAGE_RANGE_ELEMENT)) {
                    pageRanges = readPageRanges(xmlr);
                }
                else if (name.equals(BlobConstants.PAGE_LIST_ELEMENT) && eventType == XMLStreamConstants.END_ELEMENT) {
                    break;
                }
                else {
                    throw new StorageException(StorageErrorCodeStrings.INVALID_XML_DOCUMENT,
                            "The response received is invalid or improperly formatted.",
                            Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
                }
            }
        }

        return pageRanges;
    }

    /**
     * Reads PageRanges from the XMLStreamReader, reader must be at Start element of PageRangeElement
     * 
     * @param xmlr
     *            the XMLStreamReader to read from
     * @return the PageRange from the stream.
     * @throws XMLStreamException
     *             if there is an error parsing the stream
     * @throws ParseException
     *             if there is an error in parsing a date
     * @throws URISyntaxException
     *             if the uri is invalid
     * @throws StorageException
     */
    private static ArrayList<PageRange> readPageRanges(final XMLStreamReader xmlr) throws XMLStreamException,
            StorageException {
        int eventType = xmlr.getEventType();
        final ArrayList<PageRange> retRanges = new ArrayList<PageRange>();

        xmlr.require(XMLStreamConstants.START_ELEMENT, null, BlobConstants.PAGE_RANGE_ELEMENT);

        // check if there are more events in the input stream
        while (xmlr.hasNext() && BlobConstants.PAGE_RANGE_ELEMENT.equals(xmlr.getName().toString())) {
            long startOffset = -1;
            long endOffset = -1;

            // Read a Page Range
            while (xmlr.hasNext()) {
                eventType = xmlr.next();
                final String name = xmlr.getName().toString();

                if (eventType == XMLStreamConstants.START_ELEMENT) {
                    if (name.equals(BlobConstants.START_ELEMENT)) {
                        final String sizeString = DeserializationHelper.readElementFromXMLReader(xmlr,
                                BlobConstants.START_ELEMENT);
                        startOffset = Long.parseLong(sizeString);
                    }
                    else if (name.equals(Constants.END_ELEMENT)) {
                        final String sizeString = DeserializationHelper.readElementFromXMLReader(xmlr,
                                Constants.END_ELEMENT);
                        endOffset = Long.parseLong(sizeString);
                    }
                    else {
                        throw new StorageException(StorageErrorCodeStrings.INVALID_XML_DOCUMENT,
                                "The response received is invalid or improperly formatted.",
                                Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
                    }
                }
                else if (eventType == XMLStreamConstants.END_ELEMENT) {
                    if (startOffset == -1 || endOffset == -1) {
                        throw new StorageException(StorageErrorCodeStrings.INVALID_XML_DOCUMENT,
                                "The response received is invalid or improperly formatted.",
                                Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
                    }

                    final PageRange pageRef = new PageRange(startOffset, endOffset);
                    retRanges.add(pageRef);
                    break;
                }
            }

            eventType = xmlr.next();
        }

        return retRanges;
    }
}
