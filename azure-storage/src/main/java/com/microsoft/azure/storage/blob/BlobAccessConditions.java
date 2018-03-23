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
 * Optional access conditions which are specific to blobs.
 */
public final class BlobAccessConditions {

    /**
     * An object representing no access conditions.
     */
    public static final BlobAccessConditions NONE =
            new BlobAccessConditions(null, null, null,
                    null);

    // Optional standard HTTP access conditions which are optionally set
    private final HTTPAccessConditions httpAccessConditions;

    // Optional access conditions for a lease on a container or blob
    private final LeaseAccessConditions leaseAccessConditions;

    // Optional access conditions which are specific to append blobs
    private final AppendBlobAccessConditions appendBlobAccessConditions;

    // Optional access conditions which are specific to page blobs
    private final PageBlobAccessConditions pageBlobAccessConditions;

    /**
     * Access conditions which are specific to blobs. Passing in null for one of the parameters will set it to a
     * default value.
     *
     * @param httpAccessConditions
     *      Optional standard HTTP access conditions.
     * @param leaseAccessConditions
     *      Optional access conditions for a lease on a container or blob.
     * @param appendBlobAccessConditions
     *      Optional access conditions which are specific to append blobs.
     * @param pageBlobAccessConditions
     *      Optional access conditions which are specific to page blobs.
     */
    public BlobAccessConditions(
            HTTPAccessConditions httpAccessConditions,
            LeaseAccessConditions leaseAccessConditions,
            AppendBlobAccessConditions appendBlobAccessConditions,
            PageBlobAccessConditions pageBlobAccessConditions) {
        this.httpAccessConditions = httpAccessConditions == null ?
                HTTPAccessConditions.NONE : httpAccessConditions;
        this.leaseAccessConditions = leaseAccessConditions == null ?
                LeaseAccessConditions.NONE : leaseAccessConditions;
        this.appendBlobAccessConditions = appendBlobAccessConditions == null ?
                AppendBlobAccessConditions.NONE : appendBlobAccessConditions;
        this.pageBlobAccessConditions = pageBlobAccessConditions == null ?
                PageBlobAccessConditions.NONE : pageBlobAccessConditions;
    }

    /**
     * @return
     *      The HttpAccessConditions.
     */
    HTTPAccessConditions getHttpAccessConditions() {
        return httpAccessConditions;
    }

    /**
     * @return
     *      The LeaseAccessConditions.
     */
    LeaseAccessConditions getLeaseAccessConditions() {
        return leaseAccessConditions;
    }

    /**
     * @return
     *      The AppendBlobAccessConditions.
     */
    AppendBlobAccessConditions getAppendBlobAccessConditions() {
        return appendBlobAccessConditions;
    }

    /**
     * @return
     *      The PageBlobAccessConditions.
     */
    PageBlobAccessConditions getPageBlobAccessConditions() {
        return pageBlobAccessConditions;
    }

}
