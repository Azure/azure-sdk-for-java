/*
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
package com.microsoft.azure.storage.blob;

/**
 * By specifying a leaseID as a member of this class, operations on storage resources will only succeed if the active
 * lease on the blob matches the string contained here. Some methods do take this structure on its own, but it is most
 * commonly used as a member of {@link BlobAccessConditions} or {@link ContainerAccessConditions}. Specifying these
 * conditions is entirely optional, and null may be passed for this structure or any individual field to indicate that
 * none of the conditions should be set. Please refer to the <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/lease-blob">Azure Docs</a>
 * for more information.
 *
 */
public final class LeaseAccessConditions {

    /**
     * An object representing no lease access conditions.
     */
    public static final LeaseAccessConditions NONE = new LeaseAccessConditions(null);

    private final String leaseId;

    /**
     * Creates a {@link LeaseAccessConditions} object.
     *
     * @param leaseId
     *      A {@code String} representing the lease access conditions for a container or blob.
     */
    public LeaseAccessConditions(String leaseId) {
        this.leaseId = leaseId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LeaseAccessConditions)){
            return false;
        }
        if (this.leaseId == null) {
            return ((LeaseAccessConditions) obj).leaseId == null;
        }
        return this.leaseId.equals(obj);
    }

    /**
     * @return
     *      The id of the lease.
     */
    public String getLeaseId() {
        return this.leaseId;
    }
}
