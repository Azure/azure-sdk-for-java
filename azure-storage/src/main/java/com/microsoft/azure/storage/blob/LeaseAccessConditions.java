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
 * Access conditions specific to leases on storage objects.
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
