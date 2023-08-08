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
 * A wrapper class for the response returned from a Blob Service REST API Break
 * Lease Blob operation. This is returned by calls to implementations of
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#breakLease(String, String, BlobServiceOptions)}
 * ,
 * <p>
 * See the <a
 * href="http://msdn.microsoft.com/en-us/library/windowsazure/ee691972.aspx"
 * >Lease Blob</a> documentation on MSDN for details of the underlying Blob
 * Service REST API operation.
 */
public class BreakLeaseResult {
    private int remainingLeaseTimeInSeconds;

    public int getRemainingLeaseTimeInSeconds() {
        return remainingLeaseTimeInSeconds;
    }

    public void setRemainingLeaseTimeInSeconds(int remainingLeaseTimeInSeconds) {
        this.remainingLeaseTimeInSeconds = remainingLeaseTimeInSeconds;
    }
}
