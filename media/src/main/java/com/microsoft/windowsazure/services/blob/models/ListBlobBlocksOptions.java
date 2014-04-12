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


/**
 * Represents the options that may be set on a
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#listBlobBlocks(String, String, ListBlobBlocksOptions)
 * listBlobBlocks} request. These options include an optional server timeout for
 * the operation, the lease ID if the blob has an active lease, the snapshot
 * timestamp to get the committed blocks of a snapshot, whether to return the
 * committed block list, and whether to return the uncommitted block list.
 */
public class ListBlobBlocksOptions extends BlobServiceOptions {
    private String leaseId;
    private String snapshot;
    private boolean committedList;
    private boolean uncommittedList;

    /**
     * Sets the optional server request timeout value associated with this
     * {@link ListBlobBlocksOptions} instance.
     * <p>
     * The <em>timeout</em> value only affects calls made on methods where this
     * {@link ListBlobBlocksOptions} instance is passed as a parameter.
     * 
     * @param timeout
     *            The server request timeout value to set in milliseconds.
     * @return A reference to this {@link ListBlobBlocksOptions} instance.
     */
    @Override
    public ListBlobBlocksOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    /**
     * Gets the lease ID to match for the blob set in this
     * {@link ListBlobBlocksOptions} instance.
     * 
     * @return A {@link String} containing the lease ID set, if any.
     */
    public String getLeaseId() {
        return leaseId;
    }

    /**
     * Sets a lease ID value to match when listing the blocks of the blob. If
     * set, the lease must be active and the value must match the lease ID set
     * on the leased blob for the operation to succeed.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link ListBlobBlocksOptions} instance is passed as a parameter.
     * 
     * @param leaseId
     *            A {@link String} containing the lease ID to set.
     * @return A reference to this {@link ListBlobBlocksOptions} instance.
     */
    public ListBlobBlocksOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    /**
     * Gets the snapshot timestamp value set in this
     * {@link ListBlobBlocksOptions} instance.
     * 
     * @return A {@link String} containing the snapshot timestamp value of the
     *         blob snapshot to list.
     */
    public String getSnapshot() {
        return snapshot;
    }

    /**
     * Sets the snapshot timestamp value used to identify the particular
     * snapshot of the blob to list blocks for. The snapshot timestamp value is
     * an opaque value returned by the server to identify a snapshot. When this
     * option is set, only the list of committed blocks of the snapshot is
     * returned.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link ListBlobBlocksOptions} instance is passed as a parameter.
     * 
     * @param snapshot
     *            A {@link String} containing the snapshot timestamp value of
     *            the blob snapshot to list.
     * @return A reference to this {@link ListBlobBlocksOptions} instance.
     */
    public ListBlobBlocksOptions setSnapshot(String snapshot) {
        this.snapshot = snapshot;
        return this;
    }

    /**
     * Gets the flag value indicating whether to return the committed blocks of
     * the blob set in this {@link ListBlobBlocksOptions} instance.
     * 
     * @return A <code>boolean</code> flag value indicating whether to return
     *         the committed blocks of the blob.
     */
    public boolean isCommittedList() {
        return committedList;
    }

    /**
     * Sets a flag indicating whether to return the committed blocks of the blob
     * in the response to the list blob blocks request.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link ListBlobBlocksOptions} instance is passed as a parameter.
     * 
     * @param committedList
     *            Set to <code>true</code> to return the committed blocks of the
     *            blob; otherwise, <code>false</code>.
     * @return A reference to this {@link ListBlobBlocksOptions} instance.
     */
    public ListBlobBlocksOptions setCommittedList(boolean committedList) {
        this.committedList = committedList;
        return this;
    }

    /**
     * Gets the flag value indicating whether to return the uncommitted blocks
     * of the blob set in this {@link ListBlobBlocksOptions} instance.
     * 
     * @return A <code>boolean</code> flag value indicating whether to return
     *         the uncommitted blocks of the blob.
     */
    public boolean isUncommittedList() {
        return uncommittedList;
    }

    /**
     * Sets a flag indicating whether to return the uncommitted blocks of the
     * blob in the response to the list blob blocks request.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link ListBlobBlocksOptions} instance is passed as a parameter.
     * 
     * @param uncommittedList
     *            Set to <code>true</code> to return the uncommitted blocks of
     *            the blob; otherwise, <code>false</code>.
     * @return A reference to this {@link ListBlobBlocksOptions} instance.
     */
    public ListBlobBlocksOptions setUncommittedList(boolean uncommittedList) {
        this.uncommittedList = uncommittedList;
        return this;
    }
}
