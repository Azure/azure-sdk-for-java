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
package com.microsoft.windowsazure.services.blob.client;

import java.util.Date;

import com.microsoft.windowsazure.services.core.storage.AccessCondition;
import com.microsoft.windowsazure.services.core.storage.LeaseDuration;
import com.microsoft.windowsazure.services.core.storage.LeaseState;
import com.microsoft.windowsazure.services.core.storage.LeaseStatus;

/**
 * Represents the system properties for a container.
 */
public final class BlobContainerProperties
{

    /**
     * Represents the ETag value for the container.
     * <p>
     * The ETag value is a unique identifier that is updated when a write
     * operation is performed against the container. It may be used to perform
     * operations conditionally, providing concurrency control and improved
     * efficiency.
     * <p>
     * The {@link AccessCondition#ifMatch} and
     * {@link AccessCondition#ifNoneMatch} methods take an ETag value and return
     * an {@link AccessCondition} object that may be specified on the request.
     */
    private String etag;

    /**
     * Represents the container's last-modified time.
     */
    private Date lastModified;

    /**
     * Represents the container's lease status.
     */
    private LeaseStatus leaseStatus;

    /**
     * Represents the container's lease state.
     */
    private LeaseState leaseState;

    /**
     * Represents the container's lease duration.
     */
    private LeaseDuration leaseDuration;

    /**
     * @return the etag
     */
    public String getEtag()
    {
        return this.etag;
    }

    /**
     * @return the lastModified
     */
    public Date getLastModified()
    {
        return this.lastModified;
    }

    /**
     * Gets the lease status of the container.
     * 
     * @return The lease status as a <code>LeaseStatus</code> object.
     */
    public LeaseStatus getLeaseStatus()
    {
        return this.leaseStatus;
    }

    /**
     * Gets the lease state of the container.
     * 
     * @return The lease state as a <code>LeaseState</code> object.
     */
    public LeaseState getLeaseState()
    {
        return this.leaseState;
    }

    /**
     * Gets the lease duration of the container.
     * 
     * @return The lease duration as a <code>LeaseDuration</code> object.
     */
    public LeaseDuration getLeaseDuration()
    {
        return this.leaseDuration;
    }

    /**
     * Sets the ETag value on the container.
     * 
     * @param etag
     *            The ETag value to set, as a string.
     */
    public void setEtag(final String etag)
    {
        this.etag = etag;
    }

    /**
     * Sets the last modified time on the container.
     * 
     * @param lastModified
     *            The last modified time to set, as a <code>Date</code> object.
     */
    public void setLastModified(final Date lastModified)
    {
        this.lastModified = lastModified;
    }

    /**
     * Sets the lease status on the container. Reserved for internal use.
     * 
     * @param leaseStatus
     *            The lease status to set, as a <code>LeaseStatus</code> object.
     */
    public void setLeaseStatus(final LeaseStatus leaseStatus)
    {
        this.leaseStatus = leaseStatus;
    }

    /**
     * Sets the lease status on the container. Reserved for internal use.
     * 
     * @param leaseState
     *            The lease state to set, as a <code>LeaseState</code> object.
     */
    public void setLeaseState(final LeaseState leaseState)
    {
        this.leaseState = leaseState;
    }

    /**
     * Sets the lease duration on the container. Reserved for internal use.
     * 
     * @param leaseDuration
     *            The lease duration to set, as a <code>LeaseDuration</code>
     *            object.
     */
    public void setLeaseDuration(final LeaseDuration leaseDuration)
    {
        this.leaseDuration = leaseDuration;
    }
}
