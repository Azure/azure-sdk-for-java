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
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#copyBlob(String, String, String, String, CopyBlobOptions)
 * copyBlob} request. These options include an optional server timeout for the
 * operation, an optional source snapshot timestamp value to copy from a
 * particular snapshot of the source blob, blob metadata to set on the
 * destination blob, a blob lease ID to overwrite a blob with an active lease, a
 * source lease ID to copy from a source blob with an active lease, any access
 * conditions to satisfy on the destination, and any access conditions to
 * satisfy on the source.
 */
public class CopyBlobOptions extends BlobServiceOptions {
    private String leaseId;
    private AccessConditionHeader accessCondition;
    private String sourceLeaseId;
    private String sourceSnapshot;
    private HashMap<String, String> metadata = new HashMap<String, String>();
    private AccessConditionHeader sourceAccessCondition;

    /**
     * Sets the optional server request timeout value associated with this
     * {@link CopyBlobOptions} instance.
     * <p>
     * The <em>timeout</em> value only affects calls made on methods where this
     * {@link CopyBlobOptions} instance is passed as a parameter.
     * 
     * @param timeout
     *            The server request timeout value to set in milliseconds.
     * @return A reference to this {@link CopyBlobOptions} instance.
     */
    @Override
    public CopyBlobOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    /**
     * Gets the source snapshot timestamp value set in this
     * {@link CopyBlobOptions} instance.
     * 
     * @return A {@link String} containing the snapshot timestamp value of the
     *         source blob snapshot to list.
     */
    public String getSourceSnapshot() {
        return sourceSnapshot;
    }

    /**
     * Sets the snapshot timestamp value used to identify the particular
     * snapshot of the source blob to copy. The snapshot timestamp value is an
     * opaque value returned by the server to identify a snapshot. When this
     * option is set, the properties, metadata, and content of the snapshot are
     * copied to the destination.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link ListBlobBlocksOptions} instance is passed as a parameter.
     * 
     * @param sourceSnapshot
     *            A {@link String} containing the snapshot timestamp value of
     *            the source blob snapshot to list.
     * @return A reference to this {@link ListBlobBlocksOptions} instance.
     */
    public CopyBlobOptions setSourceSnapshot(String sourceSnapshot) {
        this.sourceSnapshot = sourceSnapshot;
        return this;
    }

    /**
     * Gets the blob metadata collection set in this {@link CopyBlobOptions}
     * instance.
     * 
     * @return A {@link HashMap} of name-value pairs of {@link String}
     *         containing the blob metadata set, if any.
     */
    public HashMap<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Sets the blob metadata collection to associate with the destination blob.
     * Metadata is a collection of name-value {@link String} pairs for client
     * use and is opaque to the server. Metadata names must adhere to the naming
     * rules for <a
     * href="http://msdn.microsoft.com/en-us/library/aa664670(VS.71).aspx">C#
     * identifiers</a>.
     * <p>
     * Note that if any metadata is set with this option, no source blob
     * metadata will be copied to the destination blob.
     * <p>
     * The <em>metadata</em> value only affects calls made on methods where this
     * {@link CreateBlobOptions} instance is passed as a parameter.
     * 
     * @param metadata
     *            A {@link HashMap} of name-value pairs of {@link String}
     *            containing the blob metadata to set.
     * @return A reference to this {@link CreateBlobOptions} instance.
     */
    public CopyBlobOptions setMetadata(HashMap<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Adds a name-value pair to the blob metadata collection associated with
     * this {@link CopyBlobOptions} instance.
     * <p>
     * Note that if any metadata is set with this option, no source blob
     * metadata will be copied to the destination blob.
     * 
     * @param key
     *            A {@link String} containing the name portion of the name-value
     *            pair to add to the metadata collection.
     * @param value
     *            A {@link String} containing the value portion of the
     *            name-value pair to add to the metadata collection.
     * @return A reference to this {@link CopyBlobOptions} instance.
     */
    public CopyBlobOptions addMetadata(String key, String value) {
        this.getMetadata().put(key, value);
        return this;
    }

    /**
     * Gets the lease ID to match for the destination blob set in this
     * {@link CopyBlobOptions} instance.
     * 
     * @return A {@link String} containing the lease ID set, if any.
     */
    public String getLeaseId() {
        return leaseId;
    }

    /**
     * Sets a lease ID value to match on the destination blob. If set, there
     * must be an active lease with a matching lease ID set on the destination
     * blob for the operation to succeed.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link CopyBlobOptions} instance is passed as a parameter.
     * 
     * @param leaseId
     *            A {@link String} containing the lease ID to set.
     * @return A reference to this {@link CopyBlobOptions} instance.
     */
    public CopyBlobOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    /**
     * Gets the lease ID to match for the source blob set in this
     * {@link CopyBlobOptions} instance.
     * 
     * @return A {@link String} containing the source blob lease ID set, if any.
     */
    public String getSourceLeaseId() {
        return sourceLeaseId;
    }

    /**
     * Sets a lease ID value to match on the source blob. If set, there must be
     * an active lease with a matching lease ID set on the source blob for the
     * operation to succeed.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link CopyBlobOptions} instance is passed as a parameter.
     * 
     * @param sourceLeaseId
     *            A {@link String} containing the source blob lease ID to set.
     * @return A reference to this {@link CopyBlobOptions} instance.
     */
    public CopyBlobOptions setSourceLeaseId(String sourceLeaseId) {
        this.sourceLeaseId = sourceLeaseId;
        return this;
    }

    /**
     * Gets the access conditions on the destination blob set in this
     * {@link CopyBlobOptions} instance.
     * 
     * @return An {@link AccessCondition} containing the destination blob access
     *         conditions set, if any.
     */
    public AccessConditionHeader getAccessCondition() {
        return accessCondition;
    }

    /**
     * Sets optional access conditions for the destination blob. The operation
     * will return an error if the access conditions are not met.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link CopyBlobOptions} instance is passed as a parameter.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} containing the destination blob
     *            access conditions to set.
     * @return A reference to this {@link CopyBlobOptions} instance.
     */
    public CopyBlobOptions setAccessCondition(
            AccessConditionHeader accessCondition) {
        this.accessCondition = accessCondition;
        return this;
    }

    /**
     * Gets the access conditions on the source blob set in this
     * {@link CopyBlobOptions} instance.
     * 
     * @return An {@link AccessCondition} containing the source blob access
     *         conditions set, if any.
     */
    public AccessConditionHeader getSourceAccessCondition() {
        return sourceAccessCondition;
    }

    /**
     * Sets optional access conditions for the source blob. The operation will
     * return an error if the access conditions are not met.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link CopyBlobOptions} instance is passed as a parameter.
     * 
     * @param sourceAccessCondition
     *            An {@link AccessCondition} containing the source blob access
     *            conditions to set.
     * @return A reference to this {@link CopyBlobOptions} instance.
     */
    public CopyBlobOptions setSourceAccessCondition(
            AccessConditionHeader sourceAccessCondition) {
        this.sourceAccessCondition = sourceAccessCondition;
        return this;
    }
}
