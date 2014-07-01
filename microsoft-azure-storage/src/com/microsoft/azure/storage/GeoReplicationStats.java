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

import java.util.Date;

/**
 * Class representing the geo-replication statistics.
 */
public class GeoReplicationStats {

    /**
     * Specifies the geo-replication status.
     */
    private GeoReplicationStatus status;

    /**
     * Specifies the last synchronization time.
     */
    private Date lastSyncTime;

    /**
     * Default constructor.
     */
    GeoReplicationStats() {
        // no op
    }

    /**
     * Gets the last synchronization time. All primary writes preceding this value are guaranteed to be available
     * for read operations. Primary writes following this point in time may or may not be available for reads.
     * 
     * @return A <code>java.util.Date</code> object which represents the last synchronization time.
     */
    public Date getLastSyncTime() {
        return this.lastSyncTime;
    }

    /**
     * Gets the {@link GeoReplicationStatus} status.
     * 
     * @return The <code>GeoReplicationStatus</code> status.
     */
    public GeoReplicationStatus getStatus() {
        return this.status;
    }

    /**
     * Sets the last synchronization time.
     * 
     * @param lastSyncTime
     *            A <code>java.util.Date</code> object which represents the last synchronization time to set.
     */
    void setLastSyncTime(Date lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }

    /**
     * Sets the geo-replication status.
     * 
     * @param status
     *            The <code>GeoReplicationStatus</code> status to set.
     */
    void setStatus(GeoReplicationStatus status) {
        this.status = status;
    }
}
