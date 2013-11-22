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

import java.util.Date;

import com.microsoft.windowsazure.storage.core.SR;
import com.microsoft.windowsazure.storage.core.Utility;

/**
 * Class representing the geo-replication stats.
 */
public class GeoReplicationStats {

    /**
     * Gets a {@link GeoReplicationStatus} from a string.
     * 
     * @param geoReplicationStatus
     *            The geo-replication status string.
     * @return
     *         A {@link GeoReplicationStatus} enumeration.
     */
    public static GeoReplicationStatus getGeoReplicationStatus(String geoReplicationStatus) {
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

    private GeoReplicationStatus status;

    private Date lastSyncTime;

    GeoReplicationStats() {

    }

    /**
     * @return the lastSyncTime. All primary writes preceding this value are guaranteed to be available for read
     *         operations. Primary writes following this point in time may or may not be available for reads.
     */
    public Date getLastSyncTime() {
        return lastSyncTime;
    }

    /**
     * @return the status
     */
    public GeoReplicationStatus getStatus() {
        return status;
    }

    /**
     * @param lastSyncTime
     *            the lastSyncTime to set
     */
    void setLastSyncTime(Date lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }

    /**
     * @param status
     *            the status to set
     */
    void setStatus(GeoReplicationStatus status) {
        this.status = status;
    }
}
