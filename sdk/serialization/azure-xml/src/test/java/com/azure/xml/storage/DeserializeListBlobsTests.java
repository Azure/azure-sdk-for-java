// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml.storage;

import com.azure.xml.XmlReader;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DeserializeListBlobsTests {
    @Test
    public void minimumListing() throws XMLStreamException {
        String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
            + "<EnumerationResults ServiceEndpoint=\"https://kasobolcanadacentral.blob.core.windows.net/\" ContainerName=\"b9a86bdc0b9a86bdca9a521096fb76e1772d14cbaabf\">"
            + "<Blobs />" + "<NextMarker />" + "</EnumerationResults>";

        ListBlobsFlatSegmentResponse expected = new ListBlobsFlatSegmentResponse()
            .setServiceEndpoint("https://kasobolcanadacentral.blob.core.windows.net/")
            .setContainerName("b9a86bdc0b9a86bdca9a521096fb76e1772d14cbaabf")
            .setSegment(new BlobFlatListSegment());

        ListBlobsFlatSegmentResponse actual = ListBlobsFlatSegmentResponse.fromXml(XmlReader.fromString(xml));

        validateSegmentResponse(expected, actual);
    }

    @Test
    public void pagedListing() throws XMLStreamException {
        String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
            + "<EnumerationResults ServiceEndpoint=\"https://kasobolcanadacentral.blob.core.windows.net/\" ContainerName=\"34fbcbbc034fbcbbc26a33948aa1becb5a91f4ae995b\">"
            + "<Marker>2!120!MDAwMDQ1IWMzNGZiY2JiYzIzNGZiY2JiYzI2YTgzMDgzZWVhMDI2YjYxNWNhNGJlOGI0NSEwMDAwMjghOTk5OS0xMi0zMVQyMzo1OTo1OS45OTk5OTk5WiE-</Marker>"
            + "<MaxResults>2</MaxResults>" + "<Blobs>"
            + "<Blob><Name>c34fbcbbc234fbcbbc26a83083eea026b615ca4be8b45</Name><Properties><Creation-Time>Thu, 15 Jul 2021 17:40:59 GMT</Creation-Time><Last-Modified>Thu, 15 Jul 2021 17:40:59 GMT</Last-Modified><Etag>0x8D947B7B4FE7512</Etag><Content-Length>512</Content-Length><Content-Type>application/octet-stream</Content-Type><Content-Encoding /><Content-Language /><Content-MD5 /><Cache-Control /><Content-Disposition /><x-ms-blob-sequence-number>0</x-ms-blob-sequence-number><BlobType>PageBlob</BlobType><LeaseStatus>unlocked</LeaseStatus><LeaseState>available</LeaseState><CopyId>8ae85440-2394-487a-b23b-00c25b75e7c6</CopyId><CopySource>https://kasobolcanadacentral.blob.core.windows.net/34fbcbbc034fbcbbc26a33948aa1becb5a91f4ae995b/a34fbcbbc134fbcbbc26a18108a93ba402c8674eb2ac9</CopySource><CopyStatus>success</CopyStatus><CopyProgress>512/512</CopyProgress><CopyCompletionTime>Thu, 15 Jul 2021 17:40:59 GMT</CopyCompletionTime><ServerEncrypted>true</ServerEncrypted></Properties><OrMetadata /></Blob>"
            + "<Blob><Name>m34fbcbbc334fbcbbc26a5500231fa08e4205441e8908</Name><Properties><Creation-Time>Thu, 15 Jul 2021 17:41:04 GMT</Creation-Time><Last-Modified>Thu, 15 Jul 2021 17:41:04 GMT</Last-Modified><Etag>0x8D947B7B821553B</Etag><Content-Length>512</Content-Length><Content-Type>application/octet-stream</Content-Type><Content-Encoding /><Content-Language /><Content-MD5 /><Cache-Control /><Content-Disposition /><x-ms-blob-sequence-number>0</x-ms-blob-sequence-number><BlobType>PageBlob</BlobType><LeaseStatus>unlocked</LeaseStatus><LeaseState>available</LeaseState><ServerEncrypted>true</ServerEncrypted></Properties><OrMetadata /></Blob>"
            + "</Blobs>"
            + "<NextMarker>2!120!MDAwMDQ1IXQzNGZiY2JiYzQzNGZiY2JiYzI2YTgwMjA0YzRkMTlhYTliYTIxNDQ0NWFjYyEwMDAwMjghOTk5OS0xMi0zMVQyMzo1OTo1OS45OTk5OTk5WiE-</NextMarker>"
            + "</EnumerationResults>";

        List<BlobItemInternal> expectedBlobs = new ArrayList<>();
        expectedBlobs.add(new BlobItemInternal()
            .setName(new BlobName().setContent("c34fbcbbc234fbcbbc26a83083eea026b615ca4be8b45"))
            .setProperties(new BlobItemPropertiesInternal()
                .setCreationTime(DateTimeRfc1123.fromString("Thu, 15 Jul 2021 17:40:59 GMT").getDateTime())
                .setLastModified(DateTimeRfc1123.fromString("Thu, 15 Jul 2021 17:40:59 GMT").getDateTime())
                .setETag("0x8D947B7B4FE7512")
                .setContentLength(512L)
                .setContentType("application/octet-stream")
                .setBlobSequenceNumber(0L)
                .setBlobType(BlobType.PAGE_BLOB)
                .setLeaseStatus(LeaseStatusType.UNLOCKED)
                .setLeaseState(LeaseStateType.AVAILABLE)
                .setCopyId("8ae85440-2394-487a-b23b-00c25b75e7c6")
                .setCopySource(
                    "https://kasobolcanadacentral.blob.core.windows.net/34fbcbbc034fbcbbc26a33948aa1becb5a91f4ae995b/a34fbcbbc134fbcbbc26a18108a93ba402c8674eb2ac9")
                .setCopyStatus(CopyStatusType.SUCCESS)
                .setCopyProgress("512/512")
                .setCopyCompletionTime(DateTimeRfc1123.fromString("Thu, 15 Jul 2021 17:40:59 GMT").getDateTime())
                .setServerEncrypted(true)));

        expectedBlobs.add(
            new BlobItemInternal().setName(new BlobName().setContent("m34fbcbbc334fbcbbc26a5500231fa08e4205441e8908"))
                .setProperties(new BlobItemPropertiesInternal()
                    .setCreationTime(DateTimeRfc1123.fromString("Thu, 15 Jul 2021 17:41:04 GMT").getDateTime())
                    .setLastModified(DateTimeRfc1123.fromString("Thu, 15 Jul 2021 17:41:04 GMT").getDateTime())
                    .setETag("0x8D947B7B821553B")
                    .setContentLength(512L)
                    .setContentType("application/octet-stream")
                    .setBlobSequenceNumber(0L)
                    .setBlobType(BlobType.PAGE_BLOB)
                    .setLeaseStatus(LeaseStatusType.UNLOCKED)
                    .setLeaseState(LeaseStateType.AVAILABLE)
                    .setServerEncrypted(true)));

        ListBlobsFlatSegmentResponse expected = new ListBlobsFlatSegmentResponse()
            .setServiceEndpoint("https://kasobolcanadacentral.blob.core.windows.net/")
            .setContainerName("34fbcbbc034fbcbbc26a33948aa1becb5a91f4ae995b")
            .setMarker(
                "2!120!MDAwMDQ1IWMzNGZiY2JiYzIzNGZiY2JiYzI2YTgzMDgzZWVhMDI2YjYxNWNhNGJlOGI0NSEwMDAwMjghOTk5OS0xMi0zMVQyMzo1OTo1OS45OTk5OTk5WiE-")
            .setMaxResults(2)
            .setSegment(new BlobFlatListSegment().setBlobItems(expectedBlobs))
            .setNextMarker(
                "2!120!MDAwMDQ1IXQzNGZiY2JiYzQzNGZiY2JiYzI2YTgwMjA0YzRkMTlhYTliYTIxNDQ0NWFjYyEwMDAwMjghOTk5OS0xMi0zMVQyMzo1OTo1OS45OTk5OTk5WiE-");

        ListBlobsFlatSegmentResponse actual = ListBlobsFlatSegmentResponse.fromXml(XmlReader.fromString(xml));

        validateSegmentResponse(expected, actual);
    }

    @Test
    public void metadataListing() throws XMLStreamException {
        String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
            + "<EnumerationResults ServiceEndpoint=\"https://kasobolcanadacentral.blob.core.windows.net/\" ContainerName=\"42735743042735743c8d29610ac1e2478dbcc4df1a99\">"
            + "<Blobs>"
            + "<Blob><Name>a42735743142735743c8d9381852879576c1764347b65</Name><Properties><Creation-Time>Thu, 15 Jul 2021 17:40:58 GMT</Creation-Time><Last-Modified>Thu, 15 Jul 2021 17:40:58 GMT</Last-Modified><Etag>0x8D947B7B4C818F4</Etag><Content-Length>512</Content-Length><Content-Type>application/octet-stream</Content-Type><Content-Encoding /><Content-Language /><Content-MD5 /><Cache-Control /><Content-Disposition /><x-ms-blob-sequence-number>0</x-ms-blob-sequence-number><BlobType>PageBlob</BlobType><LeaseStatus>unlocked</LeaseStatus><LeaseState>available</LeaseState><ServerEncrypted>true</ServerEncrypted></Properties><Metadata /><OrMetadata /></Blob>"
            + "<Blob><Name>c42735743242735743c8d834098b54a65a62e3420492a</Name><Properties><Creation-Time>Thu, 15 Jul 2021 17:40:59 GMT</Creation-Time><Last-Modified>Thu, 15 Jul 2021 17:40:59 GMT</Last-Modified><Etag>0x8D947B7B4FDFFD3</Etag><Content-Length>512</Content-Length><Content-Type>application/octet-stream</Content-Type><Content-Encoding /><Content-Language /><Content-MD5 /><Cache-Control /><Content-Disposition /><x-ms-blob-sequence-number>0</x-ms-blob-sequence-number><BlobType>PageBlob</BlobType><LeaseStatus>unlocked</LeaseStatus><LeaseState>available</LeaseState><ServerEncrypted>true</ServerEncrypted></Properties><Metadata /><OrMetadata /></Blob>"
            + "<Blob><Name>m42735743342735743c8d22425502d8ff84f624929914</Name><Properties><Creation-Time>Thu, 15 Jul 2021 17:41:04 GMT</Creation-Time><Last-Modified>Thu, 15 Jul 2021 17:41:04 GMT</Last-Modified><Etag>0x8D947B7B8212E2F</Etag><Content-Length>512</Content-Length><Content-Type>application/octet-stream</Content-Type><Content-Encoding /><Content-Language /><Content-MD5 /><Cache-Control /><Content-Disposition /><x-ms-blob-sequence-number>0</x-ms-blob-sequence-number><BlobType>PageBlob</BlobType><LeaseStatus>unlocked</LeaseStatus><LeaseState>available</LeaseState><ServerEncrypted>true</ServerEncrypted></Properties><Metadata><foo>bar</foo></Metadata><OrMetadata /></Blob>"
            + "<Blob><Name>t42735743442735743c8d782813c3cddcf99614508b1b</Name><Properties><Creation-Time>Thu, 15 Jul 2021 17:41:04 GMT</Creation-Time><Last-Modified>Thu, 15 Jul 2021 17:41:04 GMT</Last-Modified><Etag>0x8D947B7B830C157</Etag><Content-Length>512</Content-Length><Content-Type>application/octet-stream</Content-Type><Content-Encoding /><Content-Language /><Content-MD5 /><Cache-Control /><Content-Disposition /><x-ms-blob-sequence-number>0</x-ms-blob-sequence-number><BlobType>PageBlob</BlobType><LeaseStatus>unlocked</LeaseStatus><LeaseState>available</LeaseState><ServerEncrypted>true</ServerEncrypted><TagCount>1</TagCount></Properties><Metadata /><OrMetadata /></Blob>"
            + "</Blobs>" + "<NextMarker />" + "</EnumerationResults>";

        List<BlobItemInternal> expectedBlobs = new ArrayList<>();

        expectedBlobs.add(
            new BlobItemInternal().setName(new BlobName().setContent("a42735743142735743c8d9381852879576c1764347b65"))
                .setProperties(new BlobItemPropertiesInternal()
                    .setCreationTime(DateTimeRfc1123.fromString("Thu, 15 Jul 2021 17:40:58 GMT").getDateTime())
                    .setLastModified(DateTimeRfc1123.fromString("Thu, 15 Jul 2021 17:40:58 GMT").getDateTime())
                    .setETag("0x8D947B7B4C818F4")
                    .setContentLength(512L)
                    .setContentType("application/octet-stream")
                    .setBlobSequenceNumber(0L)
                    .setBlobType(BlobType.PAGE_BLOB)
                    .setLeaseStatus(LeaseStatusType.UNLOCKED)
                    .setLeaseState(LeaseStateType.AVAILABLE)
                    .setServerEncrypted(true)));

        expectedBlobs.add(
            new BlobItemInternal().setName(new BlobName().setContent("c42735743242735743c8d834098b54a65a62e3420492a"))
                .setProperties(new BlobItemPropertiesInternal()
                    .setCreationTime(DateTimeRfc1123.fromString("Thu, 15 Jul 2021 17:40:59 GMT").getDateTime())
                    .setLastModified(DateTimeRfc1123.fromString("Thu, 15 Jul 2021 17:40:59 GMT").getDateTime())
                    .setETag("0x8D947B7B4FDFFD3")
                    .setContentLength(512L)
                    .setContentType("application/octet-stream")
                    .setBlobSequenceNumber(0L)
                    .setBlobType(BlobType.PAGE_BLOB)
                    .setLeaseStatus(LeaseStatusType.UNLOCKED)
                    .setLeaseState(LeaseStateType.AVAILABLE)
                    .setServerEncrypted(true)));

        expectedBlobs.add(
            new BlobItemInternal().setName(new BlobName().setContent("m42735743342735743c8d22425502d8ff84f624929914"))
                .setProperties(new BlobItemPropertiesInternal()
                    .setCreationTime(DateTimeRfc1123.fromString("Thu, 15 Jul 2021 17:41:04 GMT").getDateTime())
                    .setLastModified(DateTimeRfc1123.fromString("Thu, 15 Jul 2021 17:41:04 GMT").getDateTime())
                    .setETag("0x8D947B7B8212E2F")
                    .setContentLength(512L)
                    .setContentType("application/octet-stream")
                    .setBlobSequenceNumber(0L)
                    .setBlobType(BlobType.PAGE_BLOB)
                    .setLeaseStatus(LeaseStatusType.UNLOCKED)
                    .setLeaseState(LeaseStateType.AVAILABLE)
                    .setServerEncrypted(true))
                .setMetadata(Collections.singletonMap("foo", "bar")));

        expectedBlobs.add(
            new BlobItemInternal().setName(new BlobName().setContent("t42735743442735743c8d782813c3cddcf99614508b1b"))
                .setProperties(new BlobItemPropertiesInternal()
                    .setCreationTime(DateTimeRfc1123.fromString("Thu, 15 Jul 2021 17:41:04 GMT").getDateTime())
                    .setLastModified(DateTimeRfc1123.fromString("Thu, 15 Jul 2021 17:41:04 GMT").getDateTime())
                    .setETag("0x8D947B7B830C157")
                    .setContentLength(512L)
                    .setContentType("application/octet-stream")
                    .setBlobSequenceNumber(0L)
                    .setBlobType(BlobType.PAGE_BLOB)
                    .setLeaseStatus(LeaseStatusType.UNLOCKED)
                    .setLeaseState(LeaseStateType.AVAILABLE)
                    .setServerEncrypted(true)
                    .setTagCount(1)));

        ListBlobsFlatSegmentResponse expected = new ListBlobsFlatSegmentResponse()
            .setServiceEndpoint("https://kasobolcanadacentral.blob.core.windows.net/")
            .setContainerName("42735743042735743c8d29610ac1e2478dbcc4df1a99")
            .setSegment(new BlobFlatListSegment().setBlobItems(expectedBlobs));

        ListBlobsFlatSegmentResponse actual = ListBlobsFlatSegmentResponse.fromXml(XmlReader.fromString(xml));

        validateSegmentResponse(expected, actual);
    }

    @Test
    public void tagsListing() throws XMLStreamException {
        String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
            + "<EnumerationResults ServiceEndpoint=\"https://kasobolcanadacentral.blob.core.windows.net/\" ContainerName=\"3f9c116203f9c11629bd22488e44f35974dcc48c2aa0\">"
            + "<Blobs>"
            + "<Blob><Name>a3f9c116213f9c11629bd63732ac2e67370c044bf89eb</Name><Properties><Creation-Time>Thu, 15 Jul 2021 17:40:58 GMT</Creation-Time><Last-Modified>Thu, 15 Jul 2021 17:40:58 GMT</Last-Modified><Etag>0x8D947B7B4C8B550</Etag><Content-Length>512</Content-Length><Content-Type>application/octet-stream</Content-Type><Content-Encoding /><Content-Language /><Content-MD5 /><Cache-Control /><Content-Disposition /><x-ms-blob-sequence-number>0</x-ms-blob-sequence-number><BlobType>PageBlob</BlobType><LeaseStatus>unlocked</LeaseStatus><LeaseState>available</LeaseState><ServerEncrypted>true</ServerEncrypted></Properties><OrMetadata /></Blob>"
            + "<Blob><Name>c3f9c116223f9c11629bd72727d615e5f71fe64dfcad8</Name><Properties><Creation-Time>Thu, 15 Jul 2021 17:40:59 GMT</Creation-Time><Last-Modified>Thu, 15 Jul 2021 17:40:59 GMT</Last-Modified><Etag>0x8D947B7B4FDFFD3</Etag><Content-Length>512</Content-Length><Content-Type>application/octet-stream</Content-Type><Content-Encoding /><Content-Language /><Content-MD5 /><Cache-Control /><Content-Disposition /><x-ms-blob-sequence-number>0</x-ms-blob-sequence-number><BlobType>PageBlob</BlobType><LeaseStatus>unlocked</LeaseStatus><LeaseState>available</LeaseState><ServerEncrypted>true</ServerEncrypted></Properties><OrMetadata /></Blob>"
            + "<Blob><Name>m3f9c116233f9c11629bd84991e132664066c1470cb71</Name><Properties><Creation-Time>Thu, 15 Jul 2021 17:41:04 GMT</Creation-Time><Last-Modified>Thu, 15 Jul 2021 17:41:04 GMT</Last-Modified><Etag>0x8D947B7B8210711</Etag><Content-Length>512</Content-Length><Content-Type>application/octet-stream</Content-Type><Content-Encoding /><Content-Language /><Content-MD5 /><Cache-Control /><Content-Disposition /><x-ms-blob-sequence-number>0</x-ms-blob-sequence-number><BlobType>PageBlob</BlobType><LeaseStatus>unlocked</LeaseStatus><LeaseState>available</LeaseState><ServerEncrypted>true</ServerEncrypted></Properties><OrMetadata /></Blob>"
            + "<Blob><Name>t3f9c116243f9c11629bd353117a5155f1caf24c4b881</Name><Properties><Creation-Time>Thu, 15 Jul 2021 17:41:04 GMT</Creation-Time><Last-Modified>Thu, 15 Jul 2021 17:41:04 GMT</Last-Modified><Etag>0x8D947B7B840548C</Etag><Content-Length>512</Content-Length><Content-Type>application/octet-stream</Content-Type><Content-Encoding /><Content-Language /><Content-MD5 /><Cache-Control /><Content-Disposition /><x-ms-blob-sequence-number>0</x-ms-blob-sequence-number><BlobType>PageBlob</BlobType><LeaseStatus>unlocked</LeaseStatus><LeaseState>available</LeaseState><ServerEncrypted>true</ServerEncrypted><TagCount>1</TagCount></Properties><Tags><TagSet><Tag><Key>3f9c1162778907eb</Key><Value>3f9c116276514d71</Value></Tag></TagSet></Tags><OrMetadata /></Blob>"
            + "</Blobs>" + "<NextMarker />" + "</EnumerationResults>";

        List<BlobItemInternal> expectedBlobs = new ArrayList<>();

        expectedBlobs.add(
            new BlobItemInternal().setName(new BlobName().setContent("a3f9c116213f9c11629bd63732ac2e67370c044bf89eb"))
                .setProperties(new BlobItemPropertiesInternal()
                    .setCreationTime(DateTimeRfc1123.fromString("Thu, 15 Jul 2021 17:40:58 GMT").getDateTime())
                    .setLastModified(DateTimeRfc1123.fromString("Thu, 15 Jul 2021 17:40:58 GMT").getDateTime())
                    .setETag("0x8D947B7B4C8B550")
                    .setContentLength(512L)
                    .setContentType("application/octet-stream")
                    .setBlobSequenceNumber(0L)
                    .setBlobType(BlobType.PAGE_BLOB)
                    .setLeaseStatus(LeaseStatusType.UNLOCKED)
                    .setLeaseState(LeaseStateType.AVAILABLE)
                    .setServerEncrypted(true)));

        expectedBlobs.add(
            new BlobItemInternal().setName(new BlobName().setContent("c3f9c116223f9c11629bd72727d615e5f71fe64dfcad8"))
                .setProperties(new BlobItemPropertiesInternal()
                    .setCreationTime(DateTimeRfc1123.fromString("Thu, 15 Jul 2021 17:40:59 GMT").getDateTime())
                    .setLastModified(DateTimeRfc1123.fromString("Thu, 15 Jul 2021 17:40:59 GMT").getDateTime())
                    .setETag("0x8D947B7B4FDFFD3")
                    .setContentLength(512L)
                    .setContentType("application/octet-stream")
                    .setBlobSequenceNumber(0L)
                    .setBlobType(BlobType.PAGE_BLOB)
                    .setLeaseStatus(LeaseStatusType.UNLOCKED)
                    .setLeaseState(LeaseStateType.AVAILABLE)
                    .setServerEncrypted(true)));

        expectedBlobs.add(
            new BlobItemInternal().setName(new BlobName().setContent("m3f9c116233f9c11629bd84991e132664066c1470cb71"))
                .setProperties(new BlobItemPropertiesInternal()
                    .setCreationTime(DateTimeRfc1123.fromString("Thu, 15 Jul 2021 17:41:04 GMT").getDateTime())
                    .setLastModified(DateTimeRfc1123.fromString("Thu, 15 Jul 2021 17:41:04 GMT").getDateTime())
                    .setETag("0x8D947B7B8210711")
                    .setContentLength(512L)
                    .setContentType("application/octet-stream")
                    .setBlobSequenceNumber(0L)
                    .setBlobType(BlobType.PAGE_BLOB)
                    .setLeaseStatus(LeaseStatusType.UNLOCKED)
                    .setLeaseState(LeaseStateType.AVAILABLE)
                    .setServerEncrypted(true)));

        expectedBlobs.add(
            new BlobItemInternal().setName(new BlobName().setContent("t3f9c116243f9c11629bd353117a5155f1caf24c4b881"))
                .setProperties(new BlobItemPropertiesInternal()
                    .setCreationTime(DateTimeRfc1123.fromString("Thu, 15 Jul 2021 17:41:04 GMT").getDateTime())
                    .setLastModified(DateTimeRfc1123.fromString("Thu, 15 Jul 2021 17:41:04 GMT").getDateTime())
                    .setETag("0x8D947B7B840548C")
                    .setContentLength(512L)
                    .setContentType("application/octet-stream")
                    .setBlobSequenceNumber(0L)
                    .setBlobType(BlobType.PAGE_BLOB)
                    .setLeaseStatus(LeaseStatusType.UNLOCKED)
                    .setLeaseState(LeaseStateType.AVAILABLE)
                    .setServerEncrypted(true)
                    .setTagCount(1))
                .setBlobTags(new BlobTags().setBlobTagSet(
                    Collections.singletonList(new BlobTag().setKey("3f9c1162778907eb").setValue("3f9c116276514d71")))));

        ListBlobsFlatSegmentResponse expected = new ListBlobsFlatSegmentResponse()
            .setServiceEndpoint("https://kasobolcanadacentral.blob.core.windows.net/")
            .setContainerName("3f9c116203f9c11629bd22488e44f35974dcc48c2aa0")
            .setSegment(new BlobFlatListSegment().setBlobItems(expectedBlobs));

        ListBlobsFlatSegmentResponse actual = ListBlobsFlatSegmentResponse.fromXml(XmlReader.fromString(xml));

        validateSegmentResponse(expected, actual);
    }

    @Test
    public void restApiExample() throws XMLStreamException {
        String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
            + "<EnumerationResults ServiceEndpoint=\"http://myaccount.blob.core.windows.net/\"  ContainerName=\"mycontainer\">"
            + "<Prefix>string-value</Prefix>" + "<Marker>string-value</Marker>" + "<MaxResults>5</MaxResults>"
            + "<Blobs>"
            + "<Blob><Name Encoded=\"true\">blob-name</Name><Snapshot>Thu, 15 Jul 2021 17:41:04 GMT</Snapshot><VersionId>Thu, 15 Jul 2021 17:41:04 GMT</VersionId><IsCurrentVersion>true</IsCurrentVersion><Deleted>true</Deleted><Properties><Creation-Time>Thu, 15 Jul 2021 17:41:04 GMT</Creation-Time><Last-Modified>Thu, 15 Jul 2021 17:41:04 GMT</Last-Modified><Etag>etag</Etag><Content-Length>10000000000</Content-Length><Content-Type>application/octet-stream</Content-Type><Content-Encoding>utf-8</Content-Encoding><Content-Language>en-US</Content-Language><Content-MD5>7a6d667ea5ed4467c017b2ed6ea07e78</Content-MD5><Cache-Control>no-cache</Cache-Control><x-ms-blob-sequence-number>123</x-ms-blob-sequence-number><BlobType>BlockBlob</BlobType><AccessTier>Hot</AccessTier><LeaseStatus>locked</LeaseStatus><LeaseState>leased</LeaseState><LeaseDuration>infinite</LeaseDuration><CopyId>id</CopyId><CopyStatus>pending</CopyStatus><CopySource>source url</CopySource><CopyProgress>bytes copied/bytes total</CopyProgress><CopyCompletionTime>Thu, 15 Jul 2021 17:41:04 GMT</CopyCompletionTime><CopyStatusDescription>error string</CopyStatusDescription><ServerEncrypted>true</ServerEncrypted><CustomerProvidedKeySha256>encryption-key-sha256</CustomerProvidedKeySha256><EncryptionScope>encryption-scope-name</EncryptionScope><IncrementalCopy>true</IncrementalCopy><AccessTierInferred>true</AccessTierInferred><AccessTierChangeTime>Thu, 15 Jul 2021 17:41:04 GMT</AccessTierChangeTime><DeletedTime>Thu, 15 Jul 2021 17:41:04 GMT</DeletedTime><RemainingRetentionDays>1</RemainingRetentionDays><TagCount>10</TagCount><RehydratePriority>High</RehydratePriority><Expiry-Time>Thu, 15 Jul 2021 17:41:04 GMT</Expiry-Time></Properties><Metadata><Name>value</Name></Metadata><Tags><TagSet><Tag><Key>TagName</Key><Value>TagValue</Value></Tag></TagSet></Tags><OrMetadata><Name>value</Name></OrMetadata></Blob>"
            + "</Blobs>" + "<NextMarker />" + "</EnumerationResults>";

        List<BlobItemInternal> expectedBlobs = new ArrayList<>();

        expectedBlobs.add(new BlobItemInternal().setName(new BlobName().setEncoded(true).setContent("blob-name"))
            .setSnapshot("Thu, 15 Jul 2021 17:41:04 GMT")
            .setVersionId("Thu, 15 Jul 2021 17:41:04 GMT")
            .setIsCurrentVersion(true)
            .setDeleted(true)
            .setProperties(new BlobItemPropertiesInternal()
                .setCreationTime(DateTimeRfc1123.fromString("Thu, 15 Jul 2021 17:41:04 GMT").getDateTime())
                .setLastModified(DateTimeRfc1123.fromString("Thu, 15 Jul 2021 17:41:04 GMT").getDateTime())
                .setETag("etag")
                .setContentLength(10000000000L)
                .setContentType("application/octet-stream")
                .setContentEncoding("utf-8")
                .setContentLanguage("en-US")
                .setContentMd5(Base64.getDecoder().decode("7a6d667ea5ed4467c017b2ed6ea07e78"))
                .setCacheControl("no-cache")
                .setBlobSequenceNumber(123L)
                .setBlobType(BlobType.BLOCK_BLOB)
                .setAccessTier(AccessTier.HOT)
                .setLeaseStatus(LeaseStatusType.LOCKED)
                .setLeaseState(LeaseStateType.LEASED)
                .setLeaseDuration(LeaseDurationType.INFINITE)
                .setCopyId("id")
                .setCopyStatus(CopyStatusType.PENDING)
                .setCopySource("source url")
                .setCopyProgress("bytes copied/bytes total")
                .setCopyCompletionTime(DateTimeRfc1123.fromString("Thu, 15 Jul 2021 17:41:04 GMT").getDateTime())
                .setCopyStatusDescription("error string")
                .setServerEncrypted(true)
                .setCustomerProvidedKeySha256("encryption-key-sha256")
                .setEncryptionScope("encryption-scope-name")
                .setIncrementalCopy(true)
                .setAccessTierInferred(true)
                .setAccessTierChangeTime(DateTimeRfc1123.fromString("Thu, 15 Jul 2021 17:41:04 GMT").getDateTime())
                .setDeletedTime(DateTimeRfc1123.fromString("Thu, 15 Jul 2021 17:41:04 GMT").getDateTime())
                .setRemainingRetentionDays(1)
                .setTagCount(10)
                .setRehydratePriority(RehydratePriority.HIGH)
                .setExpiresOn(DateTimeRfc1123.fromString("Thu, 15 Jul 2021 17:41:04 GMT").getDateTime()))
            .setMetadata(Collections.singletonMap("Name", "value"))
            .setBlobTags(new BlobTags()
                .setBlobTagSet(Collections.singletonList(new BlobTag().setKey("TagName").setValue("TagValue"))))
            .setObjectReplicationMetadata(Collections.singletonMap("Name", "value")));

        ListBlobsFlatSegmentResponse expected
            = new ListBlobsFlatSegmentResponse().setServiceEndpoint("http://myaccount.blob.core.windows.net/")
                .setContainerName("mycontainer")
                .setPrefix("string-value")
                .setMarker("string-value")
                .setMaxResults(5)
                .setSegment(new BlobFlatListSegment().setBlobItems(expectedBlobs));

        ListBlobsFlatSegmentResponse actual = ListBlobsFlatSegmentResponse.fromXml(XmlReader.fromString(xml));

        validateSegmentResponse(expected, actual);
    }

    private static void validateSegmentResponse(ListBlobsFlatSegmentResponse expected,
        ListBlobsFlatSegmentResponse actual) {
        assertEquals(expected.getServiceEndpoint(), actual.getServiceEndpoint());
        assertEquals(expected.getContainerName(), actual.getContainerName());
        assertEquals(expected.getPrefix(), actual.getPrefix());
        assertEquals(expected.getMarker(), actual.getMarker());
        assertEquals(expected.getMaxResults(), actual.getMaxResults());

        List<BlobItemInternal> expectedBlobItems = expected.getSegment().getBlobItems();
        List<BlobItemInternal> actualBlobItems = actual.getSegment().getBlobItems();
        if (expectedBlobItems == null) {
            assertNull(actualBlobItems);
        } else {
            assertEquals(expectedBlobItems.size(), actualBlobItems.size());
            for (int i = 0; i < expectedBlobItems.size(); i++) {
                validateBlobItemInternal(expectedBlobItems.get(i), actualBlobItems.get(i));
            }
        }

        assertEquals(expected.getNextMarker(), actual.getNextMarker());
    }

    private static void validateBlobItemInternal(BlobItemInternal expected, BlobItemInternal actual) {
        validateBlobName(expected.getName(), actual.getName());

        assertEquals(expected.isDeleted(), actual.isDeleted());
        assertEquals(expected.getSnapshot(), actual.getSnapshot());
        assertEquals(expected.getVersionId(), actual.getVersionId());
        assertEquals(expected.isCurrentVersion(), actual.isCurrentVersion());

        validateBlobItemInternalProperties(expected.getProperties(), actual.getProperties());

        assertEquals(expected.getMetadata(), actual.getMetadata());

        validateBlobTags(expected.getBlobTags(), actual.getBlobTags());

        assertEquals(expected.getObjectReplicationMetadata(), actual.getObjectReplicationMetadata());
        assertEquals(expected.isHasVersionsOnly(), actual.isHasVersionsOnly());
        assertEquals(expected.isPrefix(), actual.isPrefix());
    }

    private static void validateBlobName(BlobName expected, BlobName actual) {
        assertEquals(expected.isEncoded(), actual.isEncoded());
        assertEquals(expected.getContent(), actual.getContent());
    }

    private static void validateBlobItemInternalProperties(BlobItemPropertiesInternal expected,
        BlobItemPropertiesInternal actual) {
        assertEquals(expected.getCreationTime(), actual.getCreationTime());
        assertEquals(expected.getLastModified(), actual.getLastModified());
        assertEquals(expected.getETag(), actual.getETag());
        assertEquals(expected.getContentLength(), actual.getContentLength());
        assertEquals(expected.getContentType(), actual.getContentType());
        assertEquals(expected.getContentEncoding(), actual.getContentEncoding());
        assertEquals(expected.getContentLanguage(), actual.getContentLanguage());
        assertArrayEquals(expected.getContentMd5(), actual.getContentMd5());
        assertEquals(expected.getContentDisposition(), actual.getContentDisposition());
        assertEquals(expected.getCacheControl(), actual.getCacheControl());
        assertEquals(expected.getBlobSequenceNumber(), actual.getBlobSequenceNumber());
        assertEquals(expected.getBlobType(), actual.getBlobType());
        assertEquals(expected.getLeaseStatus(), actual.getLeaseStatus());
        assertEquals(expected.getLeaseState(), actual.getLeaseState());
        assertEquals(expected.getLeaseDuration(), actual.getLeaseDuration());
        assertEquals(expected.getCopyId(), actual.getCopyId());
        assertEquals(expected.getCopyStatus(), actual.getCopyStatus());
        assertEquals(expected.getCopySource(), actual.getCopySource());
        assertEquals(expected.getCopyProgress(), actual.getCopyProgress());
        assertEquals(expected.getCopyCompletionTime(), actual.getCopyCompletionTime());
        assertEquals(expected.getCopyStatusDescription(), actual.getCopyStatusDescription());
        assertEquals(expected.isServerEncrypted(), actual.isServerEncrypted());
        assertEquals(expected.isIncrementalCopy(), actual.isIncrementalCopy());
        assertEquals(expected.getDestinationSnapshot(), actual.getDestinationSnapshot());
        assertEquals(expected.getDeletedTime(), actual.getDeletedTime());
        assertEquals(expected.getRemainingRetentionDays(), actual.getRemainingRetentionDays());
        assertEquals(expected.getAccessTier(), actual.getAccessTier());
        assertEquals(expected.isAccessTierInferred(), actual.isAccessTierInferred());
        assertEquals(expected.getArchiveStatus(), actual.getArchiveStatus());
        assertEquals(expected.getCustomerProvidedKeySha256(), actual.getCustomerProvidedKeySha256());
        assertEquals(expected.getEncryptionScope(), actual.getEncryptionScope());
        assertEquals(expected.getAccessTierChangeTime(), actual.getAccessTierChangeTime());
        assertEquals(expected.getTagCount(), actual.getTagCount());
        assertEquals(expected.getExpiresOn(), actual.getExpiresOn());
        assertEquals(expected.isSealed(), actual.isSealed());
        assertEquals(expected.getRehydratePriority(), actual.getRehydratePriority());
        assertEquals(expected.getLastAccessedOn(), actual.getLastAccessedOn());
        assertEquals(expected.getImmutabilityPolicyExpiresOn(), actual.getImmutabilityPolicyExpiresOn());
        assertEquals(expected.getImmutabilityPolicyMode(), actual.getImmutabilityPolicyMode());
        assertEquals(expected.isLegalHold(), actual.isLegalHold());
    }

    private static void validateBlobTags(BlobTags expected, BlobTags actual) {
        if (expected == null) {
            assertNull(actual);
            return;
        }

        List<BlobTag> expectedTags = expected.getBlobTagSet();
        List<BlobTag> actualTags = actual.getBlobTagSet();

        if (expectedTags == null) {
            assertNull(actualTags);
            return;
        }

        assertEquals(expectedTags.size(), actualTags.size());
        for (int i = 0; i < expectedTags.size(); i++) {
            validateBlobTag(expectedTags.get(i), actualTags.get(i));
        }
    }

    private static void validateBlobTag(BlobTag expected, BlobTag actual) {
        assertEquals(expected.getKey(), actual.getKey());
        assertEquals(expected.getValue(), actual.getValue());
    }
}
