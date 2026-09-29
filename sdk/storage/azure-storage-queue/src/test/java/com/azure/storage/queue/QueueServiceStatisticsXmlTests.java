// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.storage.queue.models.GeoReplicationStatus;
import com.azure.storage.queue.models.QueueServiceStatistics;
import com.azure.xml.XmlReader;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Pins the XML root element {@link QueueServiceStatistics} deserializes from.
 * <p>
 * Get Queue Service Stats returns {@code <StorageServiceStats>} -- the Storage service-level envelope shared with Blob
 * and File -- while the TypeSpec model is named {@code QueueServiceStats}. azure-xml enforces the root element name, so
 * if the generated default drifts back to {@code "QueueServiceStats"} this operation throws instead of parsing. The
 * name is retargeted by QueueStorageCustomizations.retargetServiceStatsXmlRootName; this test is what makes that
 * customization's removal detectable.
 */
public class QueueServiceStatisticsXmlTests {

    // The documented response body for Get Queue Service Stats.
    private static final String WIRE_PAYLOAD
        = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "<StorageServiceStats><GeoReplication><Status>live</Status>"
            + "<LastSyncTime>Wed, 23 Oct 2013 22:05:54 GMT</LastSyncTime></GeoReplication></StorageServiceStats>";

    @Test
    public void deserializesTheWireRootElement() throws XMLStreamException {
        QueueServiceStatistics stats;
        try (XmlReader reader = XmlReader.fromString(WIRE_PAYLOAD)) {
            stats = QueueServiceStatistics.fromXml(reader);
        }

        assertNotNull(stats.getGeoReplication());
        assertEquals(GeoReplicationStatus.LIVE, stats.getGeoReplication().getStatus());
        assertNotNull(stats.getGeoReplication().getLastSyncTime());
    }
}
