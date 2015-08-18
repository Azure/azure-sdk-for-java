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

import java.util.Date;

/**
 * A wrapper class for the response returned from a Blob Service REST API
 * Snapshot Blob operation. This is returned by calls to implementations of
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#createBlobSnapshot(String, String)} and
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#createBlobSnapshot(String, String, CreateBlobSnapshotOptions)}
 * .
 * <p>
 * See the <a
 * href="http://msdn.microsoft.com/en-us/library/windowsazure/ee691971.aspx"
 * >Snapshot Blob</a> documentation on MSDN for details of the underlying Blob
 * Service REST API operation.
 */
public class CreateBlobSnapshotResult {
    private String snapshot;
    private String etag;
    private Date lastModified;

    /**
     * Gets the snapshot timestamp value returned by the server to uniquely
     * identify the newly created snapshot.
     * <p>
     * The snapshot timestamp value is an opaque value returned by the server to
     * uniquely identify a snapshot version, and may be used in subsequent
     * requests to access the snapshot.
     * <p>
     * 
     * @return A {@link String} containing the snapshot timestamp value of the
     *         newly created snapshot.
     */
    public String getSnapshot() {
        return snapshot;
    }

    /**
     * Reserved for internal use. Sets the snapshot timestamp value from the
     * <code>x-ms-snapshot</code> header returned in the response.
     * <p>
     * This method is invoked by the API to set the value from the Blob Service
     * REST API operation response returned by the server.
     * 
     * @param snapshot
     *            A {@link String} containing the snapshot timestamp value of
     *            the newly created snapshot.
     */
    public void setSnapshot(String snapshot) {
        this.snapshot = snapshot;
    }

    /**
     * Gets the ETag of the snapshot.
     * <p>
     * Note that a snapshot cannot be written to, so the ETag of a given
     * snapshot will never change. However, the ETag of the snapshot will differ
     * from that of the base blob if new metadata was supplied with the create
     * blob snapshot request. If no metadata was specified with the request, the
     * ETag of the snapshot will be identical to that of the base blob at the
     * time the snapshot was taken.
     * 
     * @return A {@link String} containing the server-assigned ETag value for
     *         the snapshot.
     */
    public String getEtag() {
        return etag;
    }

    /**
     * Reserved for internal use. Sets the ETag of the snapshot from the
     * <code>ETag</code> header returned in the response.
     * <p>
     * This method is invoked by the API to set the value from the Blob Service
     * REST API operation response returned by the server.
     * 
     * @param etag
     *            A {@link String} containing the server-assigned ETag value for
     *            the blob.
     */
    public void setEtag(String etag) {
        this.etag = etag;
    }

    /**
     * Gets the last modified time of the snapshot.
     * <p>
     * Note that a snapshot cannot be written to, so the last modified time of a
     * given snapshot will never change. However, the last modified time of the
     * snapshot will differ from that of the base blob if new metadata was
     * supplied with the create blob snapshot request. If no metadata was
     * specified with the request, the last modified time of the snapshot will
     * be identical to that of the base blob at the time the snapshot was taken.
     * 
     * @return A {@link java.util.Date} containing the last modified time of the
     *         snapshot.
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * Reserved for internal use. Sets the last modified time of the snapshot
     * from the <code>Last-Modified</code> header returned in the response.
     * <p>
     * This method is invoked by the API to set the value from the Blob Service
     * REST API operation response returned by the server.
     * 
     * @param lastModified
     *            A {@link java.util.Date} containing the last modified time of
     *            the snapshot.
     */
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }
}
