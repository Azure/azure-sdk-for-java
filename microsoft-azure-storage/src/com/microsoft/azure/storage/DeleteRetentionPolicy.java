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
 * Represents the policy governing the retention of deleted blobs.
 */
public class DeleteRetentionPolicy {
    /**
     * Indicates whether a deleted blob or snapshot is retained or immediately removed by a delete operation.
     */
    private Boolean enabled;

    /**
     * Required only if Enabled is true. Indicates the number of days that deleted blobs are retained.
     * All data older than this value will be permanently deleted.
     * The minimum value you can specify is 1; the largest value is 365.
     */
    private Integer retentionIntervalInDays;

    /**
     * Required only if Enabled is true. Indicates the number of deleted version of each blob should be retained.
     * After reaching this limit blob service permanently deletes the oldest deleted version of blob.
     * The minimum value you can specify is 1; the largest value is 10.
     */
    private Integer retainedVersionsPerBlob;

    /**
     * Return a Boolean indicating whether the DeleteRetentionPolicy is enabled.
     *
     * @return A <code>Boolean</code> indicating whether a deleted blob or snapshot is retained or immediately removed by a delete operation.
     */
    public Boolean getEnabled() {
        return this.enabled;
    }

    /**
     * Get the retention interval(in days) of the DeleteRetentionPolicy.
     *
     * @return An <code>Integer</code> which contains the retention interval.
     */
    public Integer getRetentionIntervalInDays() {
        return this.retentionIntervalInDays;
    }

    /**
     * Get the number of versions retained for each deleted blob.
     *
     * @return An <code>Integer</code> which contains the number of versions retained for each deleted blob.
     */
    public Integer getRetainedVersionsPerBlob() {
        return this.retainedVersionsPerBlob;
    }

    /**
     * Set the Boolean indicating whether the DeleteRetentionPolicy is enabled.
     * @param enabled indicates whether the DeleteRetentionPolicy is enabled.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Set the retention interval in days for the delete retention policy.
     * @param retentionIntervalInDays represents the number of days that a deleted blob is retained.
     */
    public void setRetentionIntervalInDays(final Integer retentionIntervalInDays) {
        this.retentionIntervalInDays = retentionIntervalInDays;
    }

    /**
     * Set the number of versions of a deleted blob to keep.
     * @param retainedVersionsPerBlob represents the number of versions to retain for a deleted blob.
     */
    public void setRetainedVersionsPerBlob(final Integer retainedVersionsPerBlob) {
        this.retainedVersionsPerBlob = retainedVersionsPerBlob;
    }
}
