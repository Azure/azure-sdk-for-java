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
 * A wrapper class for the response returned from a Blob Service REST API Lease
 * Blob operation. This is returned by calls to implementations of
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#acquireLease(String, String)},
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#acquireLease(String, String, AcquireLeaseOptions)},
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#renewLease(String, String, String, BlobServiceOptions)},
 * and {@link com.microsoft.windowsazure.services.blob.BlobContract#renewLease(String, String, String)}.
 * <p>
 * See the <a
 * href="http://msdn.microsoft.com/en-us/library/windowsazure/ee691972.aspx"
 * >Lease Blob</a> documentation on MSDN for details of the underlying Blob
 * Service REST API operation.
 */
public class AcquireLeaseResult {
    private String leaseId;

    /**
     * Gets the lease ID of the blob.
     * <p>
     * This value is used when updating or deleting a blob with an active lease,
     * and when renewing or releasing the lease.
     * 
     * @return A {@link String} containing the server-assigned lease ID for the
     *         blob.
     */
    public String getLeaseId() {
        return leaseId;
    }

    /**
     * Reserved for internal use. Sets the lease ID of the blob from the
     * <strong>x-ms-lease-id</strong> header of the response.
     * <p>
     * This method is invoked by the API to set the value from the Blob Service
     * REST API operation response returned by the server.
     * 
     * @param leaseId
     *            A {@link String} containing the server-assigned lease ID for
     *            the blob.
     */
    public void setLeaseId(String leaseId) {
        this.leaseId = leaseId;
    }
}
