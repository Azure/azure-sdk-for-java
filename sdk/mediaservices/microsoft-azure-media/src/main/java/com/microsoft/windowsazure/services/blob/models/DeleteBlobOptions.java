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
package com.microsoft.windowsazure.services.blob.models;

import com.microsoft.windowsazure.core.utils.AccessConditionHeader;

/**
 * Represents the options that may be set on a
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#deleteBlob(String, String, DeleteBlobOptions) deleteBlob}
 * request. These options include an optional server timeout for the operation,
 * a snapshot timestamp to specify an individual snapshot to delete, a blob
 * lease ID to delete a blob with an active lease, a flag indicating whether to
 * delete all snapshots but not the blob, or both the blob and all snapshots,
 * and any access conditions to satisfy.
 */
public class DeleteBlobOptions extends BlobServiceOptions {
    private String snapshot;
    private String leaseId;
    private Boolean deleteSnaphotsOnly;
    private AccessConditionHeader accessCondition;

    /**
     * Sets the optional server request timeout value associated with this
     * {@link DeleteBlobOptions} instance.
     * <p>
     * The <em>timeout</em> value only affects calls made on methods where this
     * {@link DeleteBlobOptions} instance is passed as a parameter.
     * 
     * @param timeout
     *            The server request timeout value to set in milliseconds.
     * @return A reference to this {@link DeleteBlobOptions} instance.
     */
    @Override
    public DeleteBlobOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    /**
     * Gets the snapshot timestamp value set in this {@link DeleteBlobOptions}
     * instance.
     * 
     * @return A {@link String} containing the snapshot timestamp value of the
     *         blob snapshot to get.
     */
    public String getSnapshot() {
        return snapshot;
    }

    /**
     * Reserved for future use. Sets an optional snapshot timestamp value used
     * to identify the particular snapshot of the blob to delete.
     * <p>
     * The snapshot timestamp value is an opaque value returned by the server to
     * identify a snapshot. This option cannot be set if the delete snapshots
     * only option is set to <code>true</code> or <code>false</code>.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link DeleteBlobOptions} instance is passed as a parameter.
     * 
     * @param snapshot
     *            A {@link String} containing the snapshot timestamp value of
     *            the blob snapshot to get.
     * @return A reference to this {@link DeleteBlobOptions} instance.
     */
    public DeleteBlobOptions setSnapshot(String snapshot) {
        this.snapshot = snapshot;
        return this;
    }

    /**
     * Gets the lease ID to match for the blob set in this
     * {@link DeleteBlobOptions} instance.
     * 
     * @return A {@link String} containing the lease ID set, if any.
     */
    public String getLeaseId() {
        return leaseId;
    }

    /**
     * Sets an optional lease ID value to match when deleting the blob. If set,
     * the lease must be active and the value must match the lease ID set on the
     * leased blob for the operation to succeed.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link DeleteBlobOptions} instance is passed as a parameter.
     * 
     * @param leaseId
     *            A {@link String} containing the lease ID to set.
     * @return A reference to this {@link DeleteBlobOptions} instance.
     */
    public DeleteBlobOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    /**
     * Gets the flag indicating whether to delete only snapshots of the blob, or
     * both the blob and all its snapshots set in this {@link DeleteBlobOptions}
     * instance.
     * 
     * @return A value of <code>true</code> to delete only the snapshots, or
     *         <code>false</code> to delete both snapshots and the blob. When
     *         the value <code>null</code> is set, x-ms-delete-snapshots in the
     *         header will not be set.
     */
    public Boolean getDeleteSnaphotsOnly() {
        return deleteSnaphotsOnly;
    }

    /**
     * Sets a flag indicating whether to delete only snapshots of the blob, or
     * both the blob and all its snapshots.
     * <p>
     * If the <em>deleteSnaphotsOnly</em> parameter is set to <code>true</code>,
     * only the snapshots of the blob are deleted by the operation. If the
     * parameter is set to <code>false</code>, both the blob and all its
     * snapshots are deleted by the operation. If this option is not set on a
     * request, and the blob has associated snapshots, the Blob service returns
     * a 409 (Conflict) status code and a {@link com.microsoft.windowsazure.exception.ServiceException} is thrown.
     * <p>
     * This option is not compatible with the snapshot option; if both are set
     * the Blob service returns status code 400 (Bad Request) and a
     * {@link StorageException} is thrown.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link DeleteBlobOptions} instance is passed as a parameter.
     * 
     * @param deleteSnaphotsOnly
     *            Set to <code>true</code> to delete only the snapshots, or
     *            <code>false</code> to delete both snapshots and the blob.
     * @return A reference to this {@link DeleteBlobOptions} instance.
     */
    public DeleteBlobOptions setDeleteSnaphotsOnly(boolean deleteSnaphotsOnly) {
        this.deleteSnaphotsOnly = deleteSnaphotsOnly;
        return this;
    }

    /**
     * Gets the access conditions set in this {@link DeleteBlobOptions}
     * instance.
     * 
     * @return An {@link AccessCondition} containing the access conditions set,
     *         if any.
     */
    public AccessConditionHeader getAccessCondition() {
        return accessCondition;
    }

    /**
     * Sets optional access conditions for getting the blob. The operation will
     * return an error if the access conditions are not met.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link DeleteBlobOptions} instance is passed as a parameter.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} containing the access conditions to
     *            set.
     * @return A reference to this {@link DeleteBlobOptions} instance.
     */
    public DeleteBlobOptions setAccessCondition(
            AccessConditionHeader accessCondition) {
        this.accessCondition = accessCondition;
        return this;
    }
}
