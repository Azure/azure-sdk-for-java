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
package com.microsoft.azure.storage;

/**
 * Class representing a set of statistics pertaining to a cloud storage service.
 */
public class ServiceStats {

    /**
     * The geo-replication statistics.
     */
    private GeoReplicationStats geoReplication;

    protected ServiceStats() {

    }

    /**
     * Gets the <code>{@link GeoReplicationStats}</code> for a cloud storage service.
     * 
     * @return The <code>GeoReplicationStats</code> for the cloud storage service.
     */
    public GeoReplicationStats getGeoReplication() {
        return this.geoReplication;
    }

    /**
     * Sets the <code>{@link GeoReplicationStats}</code> for a cloud storage service.
     * 
     * @param geoReplication
     *            The <code>GeoReplicationStats</code> to set for the cloud storage service.
     */
    protected void setGeoReplication(GeoReplicationStats geoReplication) {
        this.geoReplication = geoReplication;
    }
}
