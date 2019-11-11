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

import java.util.HashMap;

import com.microsoft.windowsazure.core.utils.AccessConditionHeader;

/**
 * Represents the options that may be set on a
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#createBlobSnapshot(String, String, CreateBlobSnapshotOptions)
 * createBlobSnapshot} request. These options include an optional server timeout
 * for the operation, blob metadata to set on the snapshot, a blob lease ID to
 * get a blob with an active lease, an optional start and end range for blob
 * content to return, and any access conditions to satisfy.
 */
public class CreateBlobSnapshotOptions extends BlobServiceOptions {
    private HashMap<String, String> metadata = new HashMap<String, String>();
    private String leaseId;
    private AccessConditionHeader accessCondition;

    /**
     * Sets the optional server request timeout value associated with this
     * {@link CreateBlobSnapshotOptions} instance.
     * <p>
     * The <em>timeout</em> value only affects calls made on methods where this
     * {@link CreateBlobSnapshotOptions} instance is passed as a parameter.
     * 
     * @param timeout
     *            The server request timeout value to set in milliseconds.
     * @return A reference to this {@link CreateBlobSnapshotOptions} instance.
     */
    @Override
    public CreateBlobSnapshotOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    /**
     * Gets the metadata collection set in this
     * {@link CreateBlobSnapshotOptions} instance.
     * 
     * @return A {@link HashMap} of name-value pairs of {@link String}
     *         containing the blob metadata set, if any.
     */
    public HashMap<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata collection to associate with a snapshot. Metadata is a
     * collection of name-value {@link String} pairs for client use and is
     * opaque to the server. Metadata names must adhere to the naming rules for
     * <a href="http://msdn.microsoft.com/en-us/library/aa664670(VS.71).aspx">C#
     * identifiers</a>.
     * <p>
     * The <em>metadata</em> value only affects calls made on methods where this
     * {@link CreateBlobSnapshotOptions} instance is passed as a parameter.
     * 
     * @param metadata
     *            A {@link java.util.HashMap} of name-value pairs of
     *            {@link String} containing the names and values of the metadata
     *            to set.
     * @return A reference to this {@link CreateBlobSnapshotOptions} instance.
     */
    public CreateBlobSnapshotOptions setMetadata(
            HashMap<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Adds a name-value pair to the metadata collection associated with this
     * {@link CreateBlobSnapshotOptions} instance.
     * 
     * @param key
     *            A {@link String} containing the name portion of the name-value
     *            pair to add to the metadata collection.
     * @param value
     *            A {@link String} containing the value portion of the
     *            name-value pair to add to the metadata collection.
     * @return A reference to this {@link CreateBlobSnapshotOptions} instance.
     */
    public CreateBlobSnapshotOptions addMetadata(String key, String value) {
        this.getMetadata().put(key, value);
        return this;
    }

    /**
     * Gets the lease ID to match for the blob set in this
     * {@link CreateBlobSnapshotOptions} instance.
     * 
     * @return A {@link String} containing the lease ID set, if any.
     */
    public String getLeaseId() {
        return leaseId;
    }

    /**
     * Sets an optional lease ID value to match when creating a snapshot of the
     * blob. If set, the lease must be active and the value must match the lease
     * ID set on the leased blob for the operation to succeed.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link CreateBlobSnapshotOptions} instance is passed as a parameter.
     * 
     * @param leaseId
     *            A {@link String} containing the lease ID to set.
     * @return A reference to this {@link CreateBlobSnapshotOptions} instance.
     */
    public CreateBlobSnapshotOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    /**
     * Gets the access conditions set in this {@link CreateBlobSnapshotOptions}
     * instance.
     * 
     * @return An {@link AccessCondition} containing the access conditions set,
     *         if any.
     */
    public AccessConditionHeader getAccessCondition() {
        return accessCondition;
    }

    /**
     * Sets optional access conditions for creating a snapshot of the blob. The
     * operation will return an error if the access conditions are not met.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link CreateBlobSnapshotOptions} instance is passed as a parameter.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} containing the access conditions to
     *            set.
     * @return A reference to this {@link CreateBlobSnapshotOptions} instance.
     */
    public CreateBlobSnapshotOptions setAccessCondition(
            AccessConditionHeader accessCondition) {
        this.accessCondition = accessCondition;
        return this;
    }
}
