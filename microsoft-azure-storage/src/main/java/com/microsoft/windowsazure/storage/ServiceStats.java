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
package com.microsoft.windowsazure.storage;

import java.io.InputStream;
import java.text.ParseException;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.microsoft.windowsazure.storage.core.SR;
import com.microsoft.windowsazure.storage.core.Utility;

/**
 * Class representing a set of stats pertaining to a cloud storage service.
 */
public class ServiceStats {
    /**
     * The name of the geo-replication XML element.
     */
    private final static String GEO_REPLICATION_NAME = "GeoReplication";

    /**
     * The name of the status XML element.
     */
    private final static String STATUS_NAME = "Status";

    /**
     * The name of the last sync time XML element.
     */
    private final static String LAST_SYNC_TIME_NAME = "LastSyncTime";

    /**
     * The geo-replication stats.
     */
    private GeoReplicationStats geoReplication;

    private ServiceStats() {

    }

    /**
     * @return the geoReplication
     */
    public GeoReplicationStats getGeoReplication() {
        return geoReplication;
    }

    /**
     * @param geoReplication
     *            the geoReplication to set
     */
    private void setGeoReplication(GeoReplicationStats geoReplication) {
        this.geoReplication = geoReplication;
    }

    /**
     * Constructs a {@link ServiceStats} object from an XML document received from the service.
     * 
     * @param inStream
     *            The XMLStreamReader object.
     * @param context
     *            an object used to track the execution of the operation
     * @return
     *         A {@link ServiceStats} object containing the properties in the XML document.
     * @throws XMLStreamException
     *             If there is an XML parsing error.
     * @throws ParseException
     *             If the specified last sync time string is invalid.
     * @throws StorageException
     */
    public static ServiceStats readServiceStatsFromStream(final InputStream inStream, final OperationContext context)
            throws XMLStreamException, StorageException, ParseException {
        ServiceStats serviceStats = new ServiceStats();
        GeoReplicationStats geoReplicationStats = new GeoReplicationStats();
        final XMLStreamReader xmlr = Utility.createXMLStreamReaderFromStream(inStream);

        // Start document
        int eventType = xmlr.getEventType();
        xmlr.require(XMLStreamConstants.START_DOCUMENT, null, null);

        // Get StorageServiceProperties Header
        eventType = xmlr.next();
        xmlr.require(XMLStreamConstants.START_ELEMENT, null, Constants.AnalyticsConstants.STORAGE_SERVICE_STATS);

        // Get the GeoReplication XML element
        eventType = xmlr.next();
        xmlr.require(XMLStreamConstants.START_ELEMENT, null, GEO_REPLICATION_NAME);

        // Check if there are more events in the input stream
        while (xmlr.hasNext()) {
            eventType = xmlr.next();
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                final String name = xmlr.getName().toString();
                if (name.equals(STATUS_NAME)) {
                    geoReplicationStats.setStatus(GeoReplicationStats.getGeoReplicationStatus(Utility
                            .readElementFromXMLReader(xmlr, STATUS_NAME)));
                    xmlr.require(XMLStreamConstants.END_ELEMENT, null, STATUS_NAME);
                }
                else if (name.equals(LAST_SYNC_TIME_NAME)) {
                    String lastSyncTime = Utility.readElementFromXMLReader(xmlr, LAST_SYNC_TIME_NAME);
                    geoReplicationStats.setLastSyncTime(Utility.isNullOrEmpty(lastSyncTime) ? null : Utility
                            .parseRFC1123DateFromStringInGMT(lastSyncTime));
                    xmlr.require(XMLStreamConstants.END_ELEMENT, null, LAST_SYNC_TIME_NAME);
                }
                else {
                    throw new StorageException(StorageErrorCodeStrings.INVALID_XML_DOCUMENT,
                            SR.INVALID_RESPONSE_RECEIVED, Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
                }
            }
            else if (eventType == XMLStreamConstants.END_ELEMENT) {
                if (xmlr.getName().toString().equals(Constants.AnalyticsConstants.STORAGE_SERVICE_STATS)) {
                    break;
                }
            }
        }
        serviceStats.setGeoReplication(geoReplicationStats);
        return serviceStats;
    }
}
