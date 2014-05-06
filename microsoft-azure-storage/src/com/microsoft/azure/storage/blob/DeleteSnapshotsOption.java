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
/**
 * 
 */
package com.microsoft.azure.storage.blob;

/**
 * Specifies options when calling delete operations.
 */
public enum DeleteSnapshotsOption {
    /**
     * Specifies deleting only the blob's snapshots.
     */
    DELETE_SNAPSHOTS_ONLY,

    /**
     * Specifies deleting the blob and its snapshots.
     */
    INCLUDE_SNAPSHOTS,

    /**
     * Specifies deleting the blob only. If the blob has snapshots, this option will result in an error from the
     * service.
     */
    NONE
}
