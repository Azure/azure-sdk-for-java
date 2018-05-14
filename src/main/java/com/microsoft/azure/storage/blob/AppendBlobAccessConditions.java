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
    public static final AppendBlobAccessConditions NONE =
            new AppendBlobAccessConditions(null, null);

    private final Long ifAppendPositionEquals;

    private final Long ifMaxSizeLessThanOrEqual;

    /**
     * Creates a {@code AppendBlobAccessConditions} object.
     *
     * @param ifAppendPositionEquals
     *      Ensures that the AppendBlock operation succeeds only if the append position is equal to a value.
     * @param ifMaxSizeLessThanOrEqual
     *      Ensures that the AppendBlock operation succeeds only if the append blob's size is less than or
     *      equal to a value.
     */
    public AppendBlobAccessConditions(Long ifAppendPositionEquals, Long ifMaxSizeLessThanOrEqual) {
        if ((ifAppendPositionEquals != null && ifAppendPositionEquals < -1) ||
                (ifMaxSizeLessThanOrEqual != null && ifMaxSizeLessThanOrEqual < -1)) {
            throw new IllegalArgumentException("Append blob access conditions can't be less than -1.");
        }
        this.ifAppendPositionEquals = ifAppendPositionEquals;
        this.ifMaxSizeLessThanOrEqual = ifMaxSizeLessThanOrEqual;
    }

    /**
     * @return
     *      Ensures that the AppendBlock operation succeeds only if the append position is equal to the value.
     */
    public Long getIfAppendPositionEquals() {
        return ifAppendPositionEquals;
    }

    /**
     * @return
     *      Ensures that the AppendBlock operation succeeds only if the append blob's size is less than or equal to the
     *      value.
     */
    public Long getIfMaxSizeLessThanOrEqual() {
        return ifMaxSizeLessThanOrEqual;
    }
}
