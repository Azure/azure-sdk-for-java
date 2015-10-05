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
 * Represents a shared access policy, which specifies the start time, expiry time, and permissions for a shared access
 * signature.
 */
public abstract class SharedAccessPolicy {

    /**
     * The expiry time for a shared access signature associated with this shared access policy.
     */
    private Date sharedAccessExpiryTime;

    /**
     * The start time for a shared access signature associated with this shared access policy.
     */
    private Date sharedAccessStartTime;
    
    /**
     * Creates an instance of the <code>SharedAccessPolicy</code> class.
     */
    public SharedAccessPolicy() {
        // Empty Default Constructor.
    }
    
    /**
     * Gets the expiry time for a shared access signature associated with this shared access policy.
     * 
     * @return A <code>java.util.Date</code> object which contains the shared access signature expiry time.
     */
    public Date getSharedAccessExpiryTime() {
        return this.sharedAccessExpiryTime;
    }

    /**
     * Gets the start time for a shared access signature associated with this shared access policy.
     * 
     * @return A <code>java.util.Date</code> object which contains the shared access signature start time.
     */
    public Date getSharedAccessStartTime() {
        return this.sharedAccessStartTime;
    }

    /**
     * Sets the expiry time for a shared access signature associated with this shared access policy.
     * 
     * @param sharedAccessExpiryTime
     *        A <code>java.util.Date</code> object which represents the expiry time to set for the shared access signature.
     */
    public void setSharedAccessExpiryTime(final Date sharedAccessExpiryTime) {
        this.sharedAccessExpiryTime = sharedAccessExpiryTime;
    }

    /**
     * Sets the start time for a shared access signature associated with this shared access policy.
     * 
     * @param sharedAccessStartTime
     *        A <code>java.util.Date</code> object which represents the start time to set for the shared access signature.
     */
    public void setSharedAccessStartTime(final Date sharedAccessStartTime) {
        this.sharedAccessStartTime = sharedAccessStartTime;
    }

    /**
     * Converts this shared access policy's permissions to a string.
     * 
     * @return A <code>String</code> which represents the shared access permissions.
     */
    public abstract String permissionsToString();

    /**
     * Sets shared access permissions using the specified permissions string.
     * 
     * @param value
     *        A <code>String</code> which represents the shared access permissions.
     **/
    public abstract void setPermissionsFromString(final String value);
}
