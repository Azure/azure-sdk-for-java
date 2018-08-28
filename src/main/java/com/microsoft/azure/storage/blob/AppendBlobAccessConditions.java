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
 * This class contains values that restrict the successful completion of AppendBlock operations to certain conditions.
 * An instance of this class is set as a member of {@link BlobAccessConditions} when needed. Any field may be set to
 * null if no access conditions are desired.
 *
 * Please refer to the request header section
 * <a href=https://docs.microsoft.com/en-us/rest/api/storageservices/append-block>here</a> for more conceptual
 * information.
 */
public final class AppendBlobAccessConditions {

    /**
     * An object representing no access conditions.
     */
    public static final AppendBlobAccessConditions NONE = new AppendBlobAccessConditions();

    private Long ifAppendPositionEquals;

    private Long ifMaxSizeLessThanOrEqual;

    public AppendBlobAccessConditions() {

    }

    /**
     * Ensures that the AppendBlock operation succeeds only if the append position is equal to the value.
     */
    public Long ifAppendPositionEquals() {
        return ifAppendPositionEquals;
    }

    /**
     * Ensures that the AppendBlock operation succeeds only if the append position is equal to the value.
     */
    public AppendBlobAccessConditions withIfAppendPositionEquals(Long ifAppendPositionEquals) {
        if(ifAppendPositionEquals != null && ifAppendPositionEquals < 0) {
            throw new IllegalArgumentException("Append blob access conditions can't be less than -1");
        }
        this.ifAppendPositionEquals = ifAppendPositionEquals;
        return this;
    }

    /**
     * Ensures that the AppendBlock operation succeeds only if the append blob's size is less than or equal to the
     * value.
     */
    public Long ifMaxSizeLessThanOrEqual() {
        return ifMaxSizeLessThanOrEqual;
    }

    /**
     * Ensures that the AppendBlock operation succeeds only if the append blob's size is less than or equal to the
     * value.
     */
    public AppendBlobAccessConditions withIfMaxSizeLessThanOrEqual(Long ifMaxSizeLessThanOrEqual) {
        if (ifMaxSizeLessThanOrEqual != null && ifMaxSizeLessThanOrEqual < 0) {
            throw new IllegalArgumentException("Append blob access conditions cannot be less than -1");
        }
        this.ifMaxSizeLessThanOrEqual = ifMaxSizeLessThanOrEqual;
        return this;
    }
}
