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

import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.Utility;

/**
 * Enumeration representing the state of geo-replication in a service.
 */
public enum GeoReplicationStatus {

    /**
     * Status of geo-replication is unavailable.
     */
    UNAVAILABLE,

    /**
     * Geo-replication is live.
     */
    LIVE,

    /**
     * Data is being bootstrapped from primary to secondary.
     */
    BOOTSTRAP;

    /**
     * Gets a {@link GeoReplicationStatus} from a string.
     * 
     * @param geoReplicationStatus
     *            The geo-replication status string.
     * @return
     *         A {@link GeoReplicationStatus} enumeration.
     */
    protected static GeoReplicationStatus parse(String geoReplicationStatus) {
        if (geoReplicationStatus != null) {
            if (geoReplicationStatus.equals(Constants.GEO_UNAVAILABLE_VALUE)) {
                return GeoReplicationStatus.UNAVAILABLE;
            }
            else if (geoReplicationStatus.equals(Constants.GEO_LIVE_VALUE)) {
                return GeoReplicationStatus.LIVE;
            }
            else if (geoReplicationStatus.equals(Constants.GEO_BOOTSTRAP_VALUE)) {
                return GeoReplicationStatus.BOOTSTRAP;
            }
        }
        throw new IllegalArgumentException(String.format(Utility.LOCALE_US, SR.INVALID_GEO_REPLICATION_STATUS,
                geoReplicationStatus));
    }
}
