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
 * HTTPGetterInfo is a passed to the getter function of a reliable download to specify parameters needed for the GET
 * request.
 */
final class HTTPGetterInfo {
    private long offset = 0;

    private Long count = null;

    private String eTag = null;

    /**
     * The start offset that should be used when creating the HTTP GET request's Range header. Defaults to 0.
     */
    public long offset() {
        return offset;
    }

    /**
     * The start offset that should be used when creating the HTTP GET request's Range header. Defaults to 0.
     */
    public HTTPGetterInfo withOffset(long offset) {
        this.offset = offset;
        return this;
    }

    /**
     * The count of bytes that should be used to calculate the end offset when creating the HTTP GET request's Range
     * header. {@code} null is the default and indicates that the entire rest of the blob should be retrieved.
     */
    public Long count() {
        return count;
    }

    /**
     * The count of bytes that should be used to calculate the end offset when creating the HTTP GET request's Range
     * header. {@code} null is the default and indicates that the entire rest of the blob should be retrieved.
     */
    public HTTPGetterInfo withCount(Long count) {
        if (count != null) {
            Utility.assertInBounds("count", count, 0, Long.MAX_VALUE);
        }
        this.count = count;
        return this;
    }

    /**
     * The resource's etag that should be used when creating the HTTP GET request's If-Match header. Note that the
     * Etag is returned with any operation that modifies the resource and by a call to {@link
     * BlobURL#getProperties(BlobAccessConditions, com.microsoft.rest.v2.Context)}. Defaults to null.
     */
    public String eTag() {
        return eTag;
    }

    /**
     * The resource's etag that should be used when creating the HTTP GET request's If-Match header. Note that the
     * Etag is returned with any operation that modifies the resource and by a call to {@link
     * BlobURL#getProperties(BlobAccessConditions, com.microsoft.rest.v2.Context)}. Defaults to null.
     */
    public HTTPGetterInfo withETag(String eTag) {
        this.eTag = eTag;
        return this;
    }
}