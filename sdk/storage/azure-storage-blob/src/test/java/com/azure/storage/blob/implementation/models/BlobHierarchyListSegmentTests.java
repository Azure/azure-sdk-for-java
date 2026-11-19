// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.models;

import com.azure.xml.XmlReader;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Hierarchical listing pages may hold only prefixes, only blobs, or neither; both lists must deserialize to non-null
 * lists in every case.
 */
public class BlobHierarchyListSegmentTests {
    @Test
    public void prefixesOnly() throws XMLStreamException {
        BlobHierarchyListSegment segment
            = parse("<Blobs><BlobPrefix><Name>dir1/</Name></BlobPrefix><BlobPrefix><Name>dir2/</Name></BlobPrefix>"
                + "</Blobs>");

        assertTrue(segment.getBlobItems().isEmpty());
        assertEquals(2, segment.getBlobPrefixes().size());
        assertEquals("dir1/", segment.getBlobPrefixes().get(0).getName().getContent());
    }

    @Test
    public void blobsAndPrefixes() throws XMLStreamException {
        BlobHierarchyListSegment segment = parse("<Blobs><Blob><Name>file</Name><Deleted>false</Deleted>"
            + "<Properties /></Blob><BlobPrefix><Name>dir/</Name></BlobPrefix></Blobs>");

        assertEquals(1, segment.getBlobItems().size());
        assertEquals(1, segment.getBlobPrefixes().size());
    }

    @Test
    public void empty() throws XMLStreamException {
        BlobHierarchyListSegment segment = parse("<Blobs />");

        assertTrue(segment.getBlobItems().isEmpty());
        assertTrue(segment.getBlobPrefixes().isEmpty());
    }

    private static BlobHierarchyListSegment parse(String xml) throws XMLStreamException {
        try (XmlReader reader = XmlReader.fromString(xml)) {
            return BlobHierarchyListSegment.fromXml(reader, "Blobs");
        }
    }
}
