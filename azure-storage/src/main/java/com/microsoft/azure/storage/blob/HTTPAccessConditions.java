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

import org.joda.time.DateTime;

import java.util.Date;

/**
 * HTTP Access Conditions.
 */
public final class HTTPAccessConditions {

    /**
     * An object representing no access conditions.
     */
    public static final HTTPAccessConditions NONE = new HTTPAccessConditions(null, null,
            null, null);

    private final Date ifModifiedSince;

    private final Date ifUnmodifiedSince;

    private final ETag ifMatch;

    private final ETag ifNoneMatch;

    /**
     * Creates a {@link HTTPAccessConditions} object.
     *
     * @param ifModifiedSince
     *      A {@code java.util.Date} if modified since condition.
     * @param ifUnmodifiedSince
     *      A {@code java.util.Date} if unmodified since condition.
     * @param ifMatch
     *      An {@link ETag} if match condition.
     * @param ifNoneMatch
     *      An {@link ETag} if none match condition.
     */
    public HTTPAccessConditions(Date ifModifiedSince, Date ifUnmodifiedSince, ETag ifMatch, ETag ifNoneMatch) {
        this.ifModifiedSince = ifModifiedSince == null ? null : new Date(ifModifiedSince.getTime());
        this.ifUnmodifiedSince = ifUnmodifiedSince == null ? null : new Date(ifUnmodifiedSince.getTime());

        this.ifMatch = ifMatch == null ? ETag.NONE : ifMatch;
        this.ifNoneMatch = ifNoneMatch == null ? ETag.NONE : ifNoneMatch;
    }

    // TODO: Change to java.util.Date and remove null check

    /**
     * @return
     *      If not null, operations will only succeed if the object has been modified since this time.
     */
    public DateTime getIfModifiedSince() {
        return ifModifiedSince == null ? null : new DateTime(ifModifiedSince);
    }

    /**
     * @return
     *      If not null, operations will only succeed if the object has been unmodified since this time.
     */
    public DateTime getIfUnmodifiedSince() {
        return ifUnmodifiedSince == null ? null : new DateTime(ifUnmodifiedSince);
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
