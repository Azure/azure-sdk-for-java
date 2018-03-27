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
 * This class contains values that restrict the successful completion of PageBlob operations to certain conditions.
 * An instance of this class is set as a member of {@link BlobAccessConditions} when needed. It may also be set to null
 * if no access conditions are desired.
 *
 * Please refer to the request header section here for more conceptual information:
 * https://docs.microsoft.com/en-us/rest/api/storageservices/put-page
 */
public final class PageBlobAccessConditions {

    /**
     * An object representing no access conditions.
     */
    public static final PageBlobAccessConditions NONE = new PageBlobAccessConditions(null,
            null, null);

    private final Long ifSequenceNumberLessThan;

    private final Long ifSequenceNumberLessThanOrEqual;

    private final Long ifSequenceNumberEqual;

    /**
     * Creates a set of conditions under which a request to a PageBlob will succeed.
     *
     * @param ifSequenceNumberLessThan
     *      Ensures that the page blob operation succeeds only if the blob's sequence number is less than a value.
     * @param ifSequenceNumberLessThanOrEqual
     *      Ensures that the page blob operation succeeds only if the blob's sequence number is less than or equal to a
     *      value.
     * @param ifSequenceNumberEqual
     *      Ensures that the page blob operation succeeds only if the blob's sequence number is equal to a value.
     */
    public PageBlobAccessConditions(Long ifSequenceNumberLessThan, Long ifSequenceNumberLessThanOrEqual,
                                    Long ifSequenceNumberEqual) {
        if ((ifSequenceNumberEqual != null && ifSequenceNumberEqual < -1) ||
                (ifSequenceNumberLessThan != null && ifSequenceNumberLessThan < -1) ||
                (ifSequenceNumberLessThanOrEqual != null && ifSequenceNumberLessThanOrEqual < -1)) {
            throw new IllegalArgumentException("Sequence number access conditions cannot be less than -1");
        }
        this.ifSequenceNumberLessThan = ifSequenceNumberLessThan;
        this.ifSequenceNumberLessThanOrEqual = ifSequenceNumberLessThanOrEqual;
        this.ifSequenceNumberEqual = ifSequenceNumberEqual;
    }

    public Long getIfSequenceNumberLessThan() {
        return ifSequenceNumberLessThan;
    }

    public Long getIfSequenceNumberLessThanOrEqual() {
        return ifSequenceNumberLessThanOrEqual;
    }

    public Long getIfSequenceNumberEqual() {
        return ifSequenceNumberEqual;
    }
}
