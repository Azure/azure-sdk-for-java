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

import java.time.OffsetDateTime;

/**
 * This type contains standard HTTP Access Conditions. Some methods do take this structure on its own, but it is most
 * commonly used as a member of {@link BlobAccessConditions} or {@link ContainerAccessConditions}. Specifying these
 * conditions is entirely optional, and null may be passed for this structure or any individual field to indicate that
 * none of the conditions should be set. Please refer to the following for more information:
 * https://docs.microsoft.com/en-us/rest/api/storageservices/Specifying-Conditional-Headers-for-Blob-Service-Operations?redirectedfrom=MSDN
 */
public final class HTTPAccessConditions {

    /**
     * An object representing no access conditions.
     */
    public static final HTTPAccessConditions NONE = new HTTPAccessConditions(null, null,
            null, null);

    private final OffsetDateTime ifModifiedSince;

    private final OffsetDateTime ifUnmodifiedSince;

    private final ETag ifMatch;

    private final ETag ifNoneMatch;

    /**
     * Creates a {@code HTTPAccessConditions} object.
     *
     * @param ifModifiedSince
     *      The HTTP If-Modified-Since access condition.
     * @param ifUnmodifiedSince
     *      The HTTP If-Unmodified-Since access condition.
     * @param ifMatch
     *      An ETag for the HTTP If-Match access condition.
     * @param ifNoneMatch
     *      An ETag for the HTTP If-None-Match access condition.
     */
    public HTTPAccessConditions(OffsetDateTime ifModifiedSince, OffsetDateTime ifUnmodifiedSince, ETag ifMatch,
                                ETag ifNoneMatch) {
        this.ifModifiedSince = ifModifiedSince;
        this.ifUnmodifiedSince = ifUnmodifiedSince;

        this.ifMatch = ifMatch == null ? ETag.NONE : ifMatch;
        this.ifNoneMatch = ifNoneMatch == null ? ETag.NONE : ifNoneMatch;
    }

    /**
     * @return
     *      If not null, operations will only succeed if the object has been modified since this time.
     */
    public OffsetDateTime getIfModifiedSince() {
        return ifModifiedSince;
    }

    /**
     * @return
     *      If not null, operations will only succeed if the object has been unmodified since this time.
     */
    public OffsetDateTime getIfUnmodifiedSince() {
        return ifUnmodifiedSince;
    }

    /**
     * @return
     *      If not null, operations will only succeed if the object's etag matches this value.
     */
    public ETag getIfMatch() {
        return ifMatch;
    }

    /**
     * @return
     *      If not null, operations will only succeed if the object's etag does not match this value.
     */
    public ETag getIfNoneMatch() {
        return ifNoneMatch;
    }
}
