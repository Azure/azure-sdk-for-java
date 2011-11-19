package com.microsoft.windowsazure.services.blob.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.microsoft.windowsazure.services.core.storage.Constants;
import com.microsoft.windowsazure.services.core.storage.LeaseStatus;
import com.microsoft.windowsazure.services.core.storage.StorageErrorCodeStrings;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.core.storage.utils.Utility;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.DeserializationHelper;

/**
 * RESERVED FOR INTERNAL USE. Class to provide object deserialization for blobs and containers.
 */
final class BlobDeserializationHelper {
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
    protected static CloudBlob readBlob(
            final XMLStreamReader xmlr, final CloudBlobClient serviceClient, final CloudBlobContainer container)
            throws XMLStreamException, ParseException, URISyntaxException, StorageException {
        xmlr.require(XMLStreamConstants.START_ELEMENT, null, BlobConstants.BLOB_ELEMENT);

        String blobName = Constants.EMPTY_STRING;

        String snapshotID = null;
        String urlString = null;
        HashMap<String, String> metadata = null;
        BlobProperties properties = null;

        int eventType = xmlr.getEventType();
        // check if there are more events in the input stream
        while (xmlr.hasNext()) {
            eventType = xmlr.next();
            final String name = xmlr.getName().toString();

            if (eventType == XMLStreamConstants.START_ELEMENT) {
                if (name.equals(Constants.URL_ELEMENT)) {
                    urlString = Utility.readElementFromXMLReader(xmlr, Constants.URL_ELEMENT);
                } else if (name.equals(BlobConstants.SNAPSHOT_ELEMENT)) {
                    snapshotID = Utility.readElementFromXMLReader(xmlr, BlobConstants.SNAPSHOT_ELEMENT);
                } else if (name.equals(Constants.NAME_ELEMENT)) {
                    blobName = Utility.readElementFromXMLReader(xmlr, Constants.NAME_ELEMENT);
                } else if (name.equals(BlobConstants.PROPERTIES)) {
                    properties = BlobDeserializationHelper.readBlobProperties(xmlr);
                    xmlr.require(XMLStreamConstants.END_ELEMENT, null, BlobConstants.PROPERTIES);
                } else if (name.equals(Constants.METADATA_ELEMENT)) {
                    metadata = DeserializationHelper.parseMetadateFromXML(xmlr);
                    xmlr.require(XMLStreamConstants.END_ELEMENT, null, Constants.METADATA_ELEMENT);
                }
            } else if (eventType == XMLStreamConstants.END_ELEMENT && name.equals(BlobConstants.BLOB_ELEMENT)) {
                break;
            }
        }

        xmlr.require(XMLStreamConstants.END_ELEMENT, null, BlobConstants.BLOB_ELEMENT);

        // Assemble and return
        if (properties != null) {
            CloudBlob retBlob = null;
            final int blobNameSectionIndex = urlString.lastIndexOf("/".concat(blobName));
            final URI baseUri = new URI(urlString.substring(0, blobNameSectionIndex + 1));
            String query = null;
            if (blobNameSectionIndex + 1 + blobName.length() < urlString.length()) {
                // Snapshot blob URI
                // example:http://<yourstorageaccount>.blob.core.windows.net/<yourcontainer>/<yourblobname>?snapshot=2009-12-03T15%3a26%3a19.4466877Z
                query = urlString.substring(blobNameSectionIndex + blobName.length() + 1);
            }

            final URI blobURI =
                    new URI(baseUri.getScheme(), baseUri.getAuthority(), baseUri.getRawPath().concat(blobName), query,
                            null);

            if (properties.getBlobType() == BlobType.BLOCK_BLOB) {
                retBlob = new CloudBlockBlob(blobURI, serviceClient, container);
            } else if (properties.getBlobType() == BlobType.PAGE_BLOB) {
                retBlob = new CloudPageBlob(blobURI, serviceClient, container);
            } else {
                throw new StorageException(StorageErrorCodeStrings.INVALID_XML_DOCUMENT,
                        "The response recieved is invalid or improperly formatted.",
                        Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
            }

            retBlob.uri = blobURI;
            retBlob.snapshotID = snapshotID;
            retBlob.properties = properties;
            retBlob.metadata = metadata;
            return retBlob;
        } else {
            throw new StorageException(StorageErrorCodeStrings.INVALID_XML_DOCUMENT,
                    "The response recieved is invalid or improperly formatted.",
                    Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
        }
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
    public static ArrayList<BlockEntry> readBlobBlocks(final XMLStreamReader xmlr, final BlockSearchMode searchMode)
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
                        blockName = Utility.readElementFromXMLReader(xmlr, Constants.NAME_ELEMENT);
                    } else if (name.equals(BlobConstants.SIZE_ELEMENT)) {
                        final String sizeString = Utility.readElementFromXMLReader(xmlr, BlobConstants.SIZE_ELEMENT);
                        blockSize = Long.parseLong(sizeString);
                    } else {
                        throw new StorageException(StorageErrorCodeStrings.INVALID_XML_DOCUMENT,
                                "The response recieved is invalid or improperly formatted.",
                                Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
                    }
                } else if (eventType == XMLStreamConstants.END_ELEMENT) {
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
    protected static BlobContainerAttributes readBlobContainerAttributes(final XMLStreamReader xmlr)
            throws XMLStreamException, ParseException, URISyntaxException {
        int eventType = xmlr.getEventType();

        final BlobContainerAttributes attributes = new BlobContainerAttributes();

        while (xmlr.hasNext()) {
            eventType = xmlr.next();
            final String name = xmlr.getName().toString();
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                if (name.equals(BlobConstants.PROPERTIES)) {
                    attributes.setProperties(BlobDeserializationHelper.readBlobContainerProperties(xmlr));
                    xmlr.require(XMLStreamConstants.END_ELEMENT, null, BlobConstants.PROPERTIES);
                } else if (name.equals(Constants.URL_ELEMENT)) {
                    attributes.setUri(new URI(Utility.readElementFromXMLReader(xmlr, Constants.URL_ELEMENT)));
                } else if (name.equals(Constants.NAME_ELEMENT)) {
                    attributes.setName(Utility.readElementFromXMLReader(xmlr, Constants.NAME_ELEMENT));
                } else if (name.equals(Constants.METADATA_ELEMENT)) {
                    // parse metadata
                    attributes.setMetadata(DeserializationHelper.parseMetadateFromXML(xmlr));
                    xmlr.require(XMLStreamConstants.END_ELEMENT, null, Constants.METADATA_ELEMENT);
                }
            } else if (eventType == XMLStreamConstants.END_ELEMENT && name.equals(BlobConstants.CONTAINER_ELEMENT)) {
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
    protected static BlobContainerProperties readBlobContainerProperties(final XMLStreamReader xmlr)
            throws XMLStreamException, ParseException {
        xmlr.require(XMLStreamConstants.START_ELEMENT, null, BlobConstants.PROPERTIES);
        int eventType = xmlr.getEventType();
        final BlobContainerProperties properties = new BlobContainerProperties();

        while (xmlr.hasNext()) {
            eventType = xmlr.next();
            final String name = xmlr.getName().toString();
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                if (name.equals(Constants.LAST_MODIFIED_ELEMENT)) {
                    properties.setLastModified(Utility.parseRFC1123DateFromStringInGMT(Utility
                            .readElementFromXMLReader(xmlr, Constants.LAST_MODIFIED_ELEMENT)));
                } else if (name.equals(Constants.ETAG_ELEMENT)) {
                    properties.setEtag(Utility.readElementFromXMLReader(xmlr, Constants.ETAG_ELEMENT));
                }
            } else {
                // expect end of properties
                xmlr.require(XMLStreamConstants.END_ELEMENT, null, BlobConstants.PROPERTIES);
                break;
            }
        }

        return properties;
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
    public static ArrayList<ListBlobItem> readBlobItems(
            final XMLStreamReader xmlr, final CloudBlobClient serviceClient, final CloudBlobContainer container)
            throws XMLStreamException, ParseException, URISyntaxException, StorageException {
        int eventType = xmlr.getEventType();
        final ArrayList<ListBlobItem> retBlobs = new ArrayList<ListBlobItem>();

        xmlr.require(XMLStreamConstants.START_ELEMENT, null, BlobConstants.BLOBS_ELEMENT);

        // check if there are more events in the input stream
        while (xmlr.hasNext()) {
            eventType = xmlr.next();
            final String name = xmlr.getName().toString();

            if (eventType == XMLStreamConstants.START_ELEMENT) {
                if (name.equals(BlobConstants.BLOB_ELEMENT)) {
                    retBlobs.add(BlobDeserializationHelper.readBlob(xmlr, serviceClient, container));
                } else if (name.equals(BlobConstants.BLOB_PREFIX_ELEMENT)) {
                    retBlobs.add(BlobDeserializationHelper.readDirectory(xmlr, serviceClient, container));
                } else {
                    throw new StorageException(StorageErrorCodeStrings.INVALID_XML_DOCUMENT,
                            "The response recieved is invalid or improperly formatted.",
                            Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
                }
            } else {
                break;
            }
        }

        xmlr.require(XMLStreamConstants.END_ELEMENT, null, BlobConstants.BLOBS_ELEMENT);
        return retBlobs;
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
     */
    protected static BlobProperties readBlobProperties(final XMLStreamReader xmlr)
            throws XMLStreamException, ParseException, StorageException {
        xmlr.require(XMLStreamConstants.START_ELEMENT, null, BlobConstants.PROPERTIES);
        int eventType = xmlr.getEventType();
        final BlobProperties properties = new BlobProperties();

        while (xmlr.hasNext()) {
            eventType = xmlr.next();
            final String name = xmlr.getName().toString();

            if (eventType == XMLStreamConstants.START_ELEMENT) {
                if (name.equals(Constants.LAST_MODIFIED_ELEMENT)) {
                    properties.setLastModified(Utility.parseRFC1123DateFromStringInGMT(Utility
                            .readElementFromXMLReader(xmlr, Constants.LAST_MODIFIED_ELEMENT)));
                } else if (name.equals(Constants.ETAG_ELEMENT)) {
                    properties.setEtag(Utility.readElementFromXMLReader(xmlr, Constants.ETAG_ELEMENT));
                } else if (name.equals(Constants.HeaderConstants.CONTENT_LENGTH)) {
                    final String tempString =
                            Utility.readElementFromXMLReader(xmlr, Constants.HeaderConstants.CONTENT_LENGTH);
                    properties.setLength(Long.parseLong(tempString));
                } else if (name.equals(Constants.HeaderConstants.CONTENT_TYPE)) {
                    properties.setContentType(Utility.readElementFromXMLReader(xmlr,
                            Constants.HeaderConstants.CONTENT_TYPE));
                } else if (name.equals(Constants.HeaderConstants.CONTENT_ENCODING)) {
                    properties.setContentEncoding(Utility.readElementFromXMLReader(xmlr,
                            Constants.HeaderConstants.CONTENT_ENCODING));
                } else if (name.equals(Constants.HeaderConstants.CONTENT_LANGUAGE)) {
                    properties.setContentLanguage(Utility.readElementFromXMLReader(xmlr,
                            Constants.HeaderConstants.CONTENT_LANGUAGE));
                } else if (name.equals(Constants.HeaderConstants.CONTENT_MD5)) {
                    properties.setContentMD5(Utility.readElementFromXMLReader(xmlr,
                            Constants.HeaderConstants.CONTENT_MD5));
                } else if (name.equals(Constants.HeaderConstants.CACHE_CONTROL)) {
                    properties.setCacheControl(Utility.readElementFromXMLReader(xmlr,
                            Constants.HeaderConstants.CACHE_CONTROL));
                } else if (name.equals(Constants.HeaderConstants.CACHE_CONTROL)) {
                    properties.setCacheControl(Utility.readElementFromXMLReader(xmlr,
                            Constants.HeaderConstants.CACHE_CONTROL));
                } else if (name.equals(BlobConstants.SEQUENCE_NUMBER)) {
                    // TODO what do we do with this?
                    Utility.readElementFromXMLReader(xmlr, BlobConstants.SEQUENCE_NUMBER);
                } else if (name.equals(BlobConstants.BLOB_TYPE_ELEMENT)) {
                    final String tempString = Utility.readElementFromXMLReader(xmlr, BlobConstants.BLOB_TYPE_ELEMENT);
                    if (tempString.equals(BlobConstants.BLOCK_BLOB_VALUE)) {
                        properties.setBlobType(BlobType.BLOCK_BLOB);
                    } else if (tempString.equals(BlobConstants.PAGE_BLOB_VALUE)) {
                        properties.setBlobType(BlobType.PAGE_BLOB);
                    } else {
                        throw new StorageException(StorageErrorCodeStrings.INVALID_XML_DOCUMENT,
                                "The response recieved is invalid or improperly formatted.",
                                Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
                    }
                } else if (name.equals(Constants.LEASE_STATUS_ELEMENT)) {
                    final String tempString = Utility.readElementFromXMLReader(xmlr, Constants.LEASE_STATUS_ELEMENT);
                    if (tempString.equals(Constants.LOCKED_VALUE.toLowerCase())) {
                        properties.setLeaseStatus(LeaseStatus.LOCKED);
                    } else if (tempString.equals(Constants.UNLOCKED_VALUE.toLowerCase())) {
                        properties.setLeaseStatus(LeaseStatus.UNLOCKED);
                    } else {
                        throw new StorageException(StorageErrorCodeStrings.INVALID_XML_DOCUMENT,
                                "The response recieved is invalid or improperly formatted.",
                                Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
                    }
                }
            } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                // expect end of properties
                xmlr.require(XMLStreamConstants.END_ELEMENT, null, BlobConstants.PROPERTIES);
                break;
            }
        }

        return properties;
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
    protected static CloudBlobContainer readContainer(final XMLStreamReader xmlr, final CloudBlobClient serviceClient)
            throws XMLStreamException, ParseException, URISyntaxException, StorageException {

        xmlr.require(XMLStreamConstants.START_ELEMENT, null, BlobConstants.CONTAINER_ELEMENT);

        final BlobContainerAttributes attributes = BlobDeserializationHelper.readBlobContainerAttributes(xmlr);

        final CloudBlobContainer retContainer = new CloudBlobContainer(attributes.getUri(), serviceClient);
        retContainer.setMetadata(attributes.getMetadata());
        retContainer.setName(attributes.getName());
        retContainer.setProperties(attributes.getProperties());
        retContainer.setUri(attributes.getUri());

        xmlr.require(XMLStreamConstants.END_ELEMENT, null, BlobConstants.CONTAINER_ELEMENT);
        return retContainer;
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
    public static ArrayList<CloudBlobContainer> readContainers(
            final XMLStreamReader xmlr, final CloudBlobClient serviceClient)
            throws XMLStreamException, ParseException, URISyntaxException, StorageException {
        int eventType = xmlr.getEventType();
        xmlr.require(XMLStreamConstants.START_ELEMENT, null, BlobConstants.CONTAINERS_ELEMENT);

        final ArrayList<CloudBlobContainer> containers = new ArrayList<CloudBlobContainer>();

        eventType = xmlr.next();
        while (eventType == XMLStreamConstants.START_ELEMENT && xmlr.hasName()
                && BlobConstants.CONTAINER_ELEMENT.equals(xmlr.getName().toString())) {
            containers.add(BlobDeserializationHelper.readContainer(xmlr, serviceClient));
            eventType = xmlr.next();
        }

        xmlr.require(XMLStreamConstants.END_ELEMENT, null, BlobConstants.CONTAINERS_ELEMENT);
        return containers;
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
    protected static CloudBlobDirectory readDirectory(
            final XMLStreamReader xmlr, final CloudBlobClient serviceClient, final CloudBlobContainer container)
            throws XMLStreamException, ParseException, URISyntaxException, StorageException {
        xmlr.require(XMLStreamConstants.START_ELEMENT, null, BlobConstants.BLOB_PREFIX_ELEMENT);

        // Move to Name element
        xmlr.next();
        xmlr.require(XMLStreamConstants.START_ELEMENT, null, Constants.NAME_ELEMENT);

        final String prefixName = Utility.readElementFromXMLReader(xmlr, Constants.NAME_ELEMENT);

        // Move from End name element to end prefix element
        xmlr.next();
        xmlr.require(XMLStreamConstants.END_ELEMENT, null, BlobConstants.BLOB_PREFIX_ELEMENT);

        return container.getDirectoryReference(prefixName);
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
    public static ArrayList<PageRange> readPageRanges(final XMLStreamReader xmlr)
            throws XMLStreamException, StorageException {
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
                        final String sizeString = Utility.readElementFromXMLReader(xmlr, BlobConstants.START_ELEMENT);
                        startOffset = Long.parseLong(sizeString);
                    } else if (name.equals(Constants.END_ELEMENT)) {
                        final String sizeString = Utility.readElementFromXMLReader(xmlr, Constants.END_ELEMENT);
                        endOffset = Long.parseLong(sizeString);
                    } else {
                        throw new StorageException(StorageErrorCodeStrings.INVALID_XML_DOCUMENT,
                                "The response recieved is invalid or improperly formatted.",
                                Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
                    }
                } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                    if (startOffset == -1 || endOffset == -1) {
                        throw new StorageException(StorageErrorCodeStrings.INVALID_XML_DOCUMENT,
                                "The response recieved is invalid or improperly formatted.",
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

    /**
     * Private Default Ctor
     */
    private BlobDeserializationHelper() {
        // No op
    }
}
