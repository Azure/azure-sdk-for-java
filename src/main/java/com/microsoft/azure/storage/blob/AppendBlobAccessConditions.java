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

import com.microsoft.azure.storage.blob.models.AppendPositionAccessConditions;
import com.microsoft.azure.storage.blob.models.LeaseAccessConditions;
import com.microsoft.azure.storage.blob.models.ModifiedAccessConditions;

/**
 * This class contains values that restrict the successful completion of AppendBlock operations to certain conditions.
 * Any field may be set to null if no access conditions are desired.
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
            new AppendBlobAccessConditions();

    private AppendPositionAccessConditions appendPositionAccessConditions;

    private ModifiedAccessConditions modifiedAccessConditions;

    private LeaseAccessConditions leaseAccessConditions;

    /**
     * {@link AppendPositionAccessConditions}
     */
    public AppendPositionAccessConditions appendPositionAccessConditions() {
        return appendPositionAccessConditions;
    }

    /**
     * {@link AppendPositionAccessConditions}
     */
    public AppendBlobAccessConditions withAppendPositionAccessConditions(
            AppendPositionAccessConditions appendPositionAccessConditions) {
        this.appendPositionAccessConditions = appendPositionAccessConditions;
        return this;
    }

    /**
     * {@link ModifiedAccessConditions}
     */
    public ModifiedAccessConditions modifiedAccessConditions() {
        return modifiedAccessConditions;
    }

    /**
     * {@link ModifiedAccessConditions}
     */
    public AppendBlobAccessConditions withModifiedAccessConditions(ModifiedAccessConditions modifiedAccessConditions) {
        this.modifiedAccessConditions = modifiedAccessConditions;
        return this;
    }

    /**
     * {@link LeaseAccessConditions}
     */
    public LeaseAccessConditions leaseAccessConditions() {
        return leaseAccessConditions;
    }

    /**
     * {@link LeaseAccessConditions}
     */
    public AppendBlobAccessConditions withLeaseAccessConditions(LeaseAccessConditions leaseAccessConditions) {
        this.leaseAccessConditions = leaseAccessConditions;
        return this;
    }

    public AppendBlobAccessConditions() {
        appendPositionAccessConditions = new AppendPositionAccessConditions();
        modifiedAccessConditions = new ModifiedAccessConditions();
        leaseAccessConditions = new LeaseAccessConditions();
   }
}
